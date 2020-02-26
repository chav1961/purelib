package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.FormManagedUtils.FormManagerParserCallback;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.ScaledLayout;

public class MarkupBuiltForm<T> extends JPanel implements LocaleChangeListener, AutoCloseable, JComponentMonitor {
	private static final long 				serialVersionUID = -1828992791881237479L;
	public static final int					DEFAULT_WIDTH = 80;
	public static final int					DEFAULT_HEIGHT = 25;
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentMetadataInterface	metadata;
	private final T							instance;
	private final FormManager<Object,T>		formMgr;
	private final boolean					tooltipsOnFocus;
	private final PresentationDescriptor	desc;
	private final Map<String,GetterAndSetter>	accessors = new HashMap<>();	
	private final JStateString				state;
	private final JLabel					pageMark;
	private final JPanel					centralPanel;
	private final JScrollBar				pageBar;
	private final Component[]				lastFocused;
	private int								currentPage = 1;
	
	public MarkupBuiltForm(final Localizer localizer, final LoggerFacade logger, final ContentMetadataInterface metadata, final String markupDescriptor, final T instance, final FormManager<Object,T> formMgr, final boolean tooltipsOnFocus) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (markupDescriptor == null || markupDescriptor.isEmpty()) {
			throw new IllegalArgumentException("Markup descriptor can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance to edit can't be null");
		}
		else if (formMgr == null) {
			throw new NullPointerException("Form manager to edit can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.metadata = metadata;
			this.instance = instance;
			this.formMgr = formMgr;
			this.tooltipsOnFocus = tooltipsOnFocus;
			this.state = new JStateString(localizer);
			
			setLayout(new BorderLayout());
			
			final List<JButtonWithMeta>		actions = new ArrayList<>();
			final Map<String,JComponent>	components = new HashMap<>();

			FormManagedUtils.parseModel4Form(logger,metadata,instance.getClass(),this,new FormManagerParserCallback() {
				@Override
				public void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, boolean isModifiable) throws ContentException {
					accessors.put(metadata.getUIPath().toString(),gas);
					components.put(metadata.getName(),fieldComponent);
				}
				
				@Override
				public void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException {
					actions.add(button);
				}
			});

			this.desc = buildPresentation(components, this, markupDescriptor, instance.getClass());
			this.lastFocused = new JComponent[desc.getPageCount()];
			
			if (!actions.isEmpty()) {
				add(buildToolbar(actions,this,instance,formMgr),BorderLayout.NORTH);
			}
			
			if (desc.getPageCount() > 1) {	// Page bar required
				this.pageBar = new JScrollBar(JScrollBar.VERTICAL,1,1,1,desc.getPageCount());
				this.centralPanel = new JPanel();
				this.pageMark = new JLabel("/");

				add(pageBar,BorderLayout.EAST);
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0),(e)->{movePage(e.getActionCommand());},"next");
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,InputEvent.CTRL_DOWN_MASK),(e)->{movePage(e.getActionCommand());},"last");
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0),(e)->{movePage(e.getActionCommand());},"prev");
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,InputEvent.CTRL_DOWN_MASK),(e)->{movePage(e.getActionCommand());},"first");
				
				centralPanel.setLayout(new CardLayout());
				for (int index = 0; index < desc.getPageCount(); index++) {
					final PageDescriptor	page = desc.getPage(0); 
					
					centralPanel.add(page,"p"+(index+1));
					prepareCrossPageMoving(page,index == 0, index == desc.getPageCount()-1);
				}
				add(centralPanel,BorderLayout.CENTER);

				final JPanel	bottom = new JPanel(new BorderLayout());
				
				bottom.add(state,BorderLayout.CENTER);
				bottom.add(pageMark,BorderLayout.EAST);
				add(bottom,BorderLayout.SOUTH);
				
				movePage("first");
			}
			else {
				this.pageBar = null;
				this.centralPanel = null;
				this.pageMark = null;
				add(desc.getPage(0),BorderLayout.CENTER);
				add(state,BorderLayout.SOUTH);
			}
			
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		state.localeChanged(oldLocale, newLocale);
		if (pageBar != null) {
			SwingUtils.refreshLocale(pageBar,oldLocale,newLocale);
		}
		for (int index = 0; index < desc.getPageCount(); index++) {
			SwingUtils.refreshLocale(desc.getPage(index),oldLocale,newLocale);
		}
	}

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) throws ContentException {
		switch (event) {
			case Action:
				if (metadata.getApplicationPath().toString().contains("().")) {
					try{switch (FormManagedUtils.seekAndCall(instance,metadata.getApplicationPath())) {
							case REJECT : case FIELD_ONLY : case DEFAULT : case NONE :
								break;
							case TOTAL : case RECORD_ONLY :
								for (ContentNodeMetadata item : metadata.getParent()) {
									final JComponent	comp = (JComponent) SwingUtils.findComponentByName(this, item.getUIPath().toString());
									
									process(MonitorEvent.Loading,item,comp);
								}
								break;
							case EXIT :
								return process(MonitorEvent.Exit,metadata,component,parameters);
							default	:
								break;
						}
					} catch (Exception exc) {
						logger.message(Severity.error,exc,"Action [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					}
				}
				else {
					try{switch (formMgr.onAction(instance,null,metadata.getApplicationPath().toString(),null)) {
							case REJECT : case FIELD_ONLY : case DEFAULT : case NONE :
								break;
							case TOTAL : case RECORD_ONLY :
								for (ContentNodeMetadata item : metadata.getParent()) {
									final JComponent	comp = (JComponent) SwingUtils.findComponentByName(this, item.getUIPath().toString());
									
									process(MonitorEvent.Loading,item,comp);
								}
								break;
							case EXIT :
								return process(MonitorEvent.Exit,metadata,component,parameters);
							default	:
								break;
						}
					} catch (LocalizationException | FlowException exc) {
						logger.message(Severity.error,exc,"Action [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					}
				}
				break;
			case FocusGained:
				try{if (tooltipsOnFocus) {
						message(Severity.trace,SwingUtils.prepareHtmlMessage(Severity.trace, getLocalizerAssociated().getValue(metadata.getTooltipId())));
					}
				} catch (LocalizationException  exc) {
					logger.message(Severity.error,exc,"FocusGained for [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					if (tooltipsOnFocus) {
						message(Severity.trace,"");
					}
				}
				break;
			case FocusLost:
				message(Severity.trace,"");
				break;
			case Loading:
				final GetterAndSetter	gas = accessors.get(metadata.getUIPath().toString());
				
				if (gas == null) {
					//System.err.println("SD1");
				}
				else {
					final Object			value = ModelUtils.getValueByGetter(instance, gas, metadata);
					
					if (value == null || component == null) {
						//System.err.println("SD2");
					}
					((JComponentInterface)component).assignValueToComponent(value);
				}
				break;
			case Rollback:
				message(Severity.trace,"");
				break;
			case Saving:
				try{final Object	oldValue = ((JComponentInterface)component).getValueFromComponent();
				
					ModelUtils.setValueBySetter(instance, ((JComponentInterface)component).getChangedValueFromComponent(), accessors.get(metadata.getUIPath().toString()), metadata);
					switch (formMgr.onField(instance,null,metadata.getName(),oldValue)) {
						case FIELD_ONLY : case DEFAULT : case NONE :
							break;
						case TOTAL : case RECORD_ONLY :
							for (ContentNodeMetadata item : metadata.getParent()) {
								final JComponent	comp = (JComponent) SwingUtils.findComponentByName(this, item.getUIPath().toString());
								
								process(MonitorEvent.Loading,item,comp);
							}
							break;
						case REJECT		:
							ModelUtils.setValueBySetter(instance, oldValue, accessors.get(metadata.getUIPath().toString()), metadata);
							((JComponentInterface)component).assignValueToComponent(oldValue);
							break;
						case EXIT :
							return process(MonitorEvent.Exit,metadata,component,parameters);
						default	:
							break;
					}
				} catch (LocalizationException | FlowException exc) {
					logger.message(Severity.error,exc,"Saving for [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
				}
				break;
			case Validation:
				final Object	changed = ((JComponentInterface)component).getChangedValueFromComponent(); 
				final String	error = ((JComponentInterface)component).standardValidation(changed == null ? null : changed.toString());
				
				if (error != null) {
					message(Severity.error,SwingUtils.prepareHtmlMessage(Severity.error, error));
					return false;
				}
				else {
					message(Severity.trace,"");
					return true;
				}
			case Exit :
				return processExit(metadata,component,parameters);
			default:
				break;
		}
		return true;
	}

	public Localizer getLocalizerAssociated() {
		return localizer;
	}
	
	protected boolean processExit(final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) {
		return true;
	}

	protected void message(final Severity level, final String format, final Object... parameters) {
		state.message(level, format, parameters);
	}
	
	protected void message(final Severity level, final Throwable t, final String format, final Object... parameters) {
		state.message(level, t, format, parameters);
	}
	
	private void movePage(final String moving) {
		lastFocused[currentPage-1] = FocusManager.getCurrentManager().getFocusOwner();
		
		switch (moving) {
			case "first"	:
				currentPage = 1;
				break;
			case "prev"		:
				if (currentPage > 1) {
					currentPage--;
				}
				break;
			case "next"		:
				if (currentPage < desc.getPageCount()) {
					currentPage++;
				}
				break;
			case "last"		:
				currentPage = desc.getPageCount();
				break;
			default : throw new UnsupportedOperationException("Moving option ["+moving+"] is not supported yet"); 
		}

		((CardLayout)centralPanel.getLayout()).show(centralPanel,"p"+currentPage);
		pageBar.setValue(currentPage);
		pageMark.setText(currentPage+"/"+desc.getPageCount());
		
		if (lastFocused[currentPage-1] != null) {
			lastFocused[currentPage-1].requestFocus();
		}
		else {
			final JPanel	current = desc.getPage(currentPage-1);
			
			for (int index = 0, maxIndex = current.getComponentCount(); index < maxIndex; index++) {
				if (!(current.getComponent(index) instanceof JLabel)) {
					current.getComponent(index).requestFocus();
				}
			}
		}
	}
	
	private static <T> PresentationDescriptor buildPresentation(final Map<String,JComponent> componentList, final JComponentMonitor monitor, final String markupDescriptor, final  Class<T> clazz) throws SyntaxException {
		final List<JComponent>		components = new ArrayList<>();
		final List<Rectangle>		constraints = new ArrayList<>();
		final List<PageDescriptor>	pages = new ArrayList<>();
		final int[]					lastLineNoWidthAndHeight = new int[3];
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)-> {
				if (data[from] == '>' && data[from+1] == '>') {
					if (!components.isEmpty()) {
						pages.add(new PageDescriptor(lastLineNoWidthAndHeight[1],lastLineNoWidthAndHeight[2], components, constraints));
						components.clear();
						constraints.clear();
					}
					else {
						if (data[from+2] >= '0' && data[from+2] <= '9') {
							from = CharUtils.parseInt(data,from+2,lastLineNoWidthAndHeight,true);
							if (data[from] == 'x') {
								lastLineNoWidthAndHeight[1] = lastLineNoWidthAndHeight[0]; 
								from = CharUtils.parseInt(data,from+1,lastLineNoWidthAndHeight,true);
								lastLineNoWidthAndHeight[2] = lastLineNoWidthAndHeight[0]; 
							}
							else {
								lastLineNoWidthAndHeight[1] = lastLineNoWidthAndHeight[0]; 
								lastLineNoWidthAndHeight[2] = DEFAULT_HEIGHT;
							}
						}
						else {
							lastLineNoWidthAndHeight[1] = DEFAULT_WIDTH; 
							lastLineNoWidthAndHeight[2] = DEFAULT_HEIGHT;
						}
					}
					lastLineNoWidthAndHeight[0] = lineNo;
				}
				else {
					processLine(displacement,lineNo-lastLineNoWidthAndHeight[0],data,from,length,componentList,components,constraints);
				}
			})) {
			
			lblp.write(markupDescriptor.toCharArray(),0,markupDescriptor.length());
		} catch (IOException e) {
			throw new SyntaxException(0,0,e.getLocalizedMessage());
		}
		pages.add(new PageDescriptor(lastLineNoWidthAndHeight[1],lastLineNoWidthAndHeight[2], components, constraints));
		components.clear();
		constraints.clear();

		final PageDescriptor[]	pageList = pages.toArray(new PageDescriptor[pages.size()]);
		
		pages.clear();
		return new PresentationDescriptor(lastLineNoWidthAndHeight[1],lastLineNoWidthAndHeight[2],pageList);
	}
	
	private static void processLine(final long displacement, final int lineNo, final char[] data, int from, final int length, final Map<String,JComponent> componentList, final List<JComponent> components, final List<Rectangle> constraints) throws IOException, SyntaxException {
		final int	begin = from; 
		int 		start, result[] = new int[2], size[] = new int[1], location = 0;
		
		for (;;) {
			start = from;
			
			while (data[from] != '$' && data[from] != '\n') {	// Parse constant
				from++;
			}
			components.add(new JLabel(new String(data,start,from-start)));
			constraints.add(new Rectangle(lineNo,location,from-start,1));
			location += from - start;
			
			if (data[from] == '$' && data[from+1] == '{') {		// Parse field description
				from = CharUtils.parseName(data,from+2, result);
				if (data[from] == ':') {
					from = CharUtils.parseInt(data,from+1,size,true);
					if (data[from] == '}') {
						final String		name = new String(data,result[0],result[1]-result[0]);
						final JComponent	currentComponent = componentList.get(name);

						components.add(currentComponent);
						constraints.add(new Rectangle(lineNo,location,size[0],1));
						location += size[0];
						from++;
					}
					else {
						throw new SyntaxException(lineNo,from-begin,"Missing '}'"); 
					}
				}
			}
			else {
				break;
			}
		}
	}

	private Component buildToolbar(final List<JButtonWithMeta> actions, final JComponentMonitor monitor, T instance, final FormManager<Object, T> formMgr) throws LocalizationException, ContentException {
		final JToolBar	bar = new JToolBar(JToolBar.HORIZONTAL);
		
		for (JButtonWithMeta item : actions) {
			bar.add(item);
			item.addActionListener((e)->{
				try {
					formMgr.onAction(instance,null,((NodeMetadataOwner)item).getNodeMetadata().getApplicationPath().toString(),null);
				} catch (LocalizationException | FlowException exc) {
					exc.printStackTrace();
				}
			});
		}
		return bar;
	}

	private void prepareCrossPageMoving(final PageDescriptor page, final boolean first, final boolean last) {
		Component	current, next;
		
		if (first) {
			current = next = null;
			for (int index = 0; index < page.getComponentCount() && (current == null || next == null); index++) {
				if (page.getComponent(index).isFocusable()) {
					if (current == null) {
						current = page.getComponent(index);
					}
					else if (next == null) {
						next = page.getComponent(index);
					}
				}
			}
			if (current != null && next != null) {
				buildFocusListener(current,next,"prev");
			}
		}
		
		if (last) {
			current = next = null;
			for (int index = page.getComponentCount()-1; index >= 0 && (current == null || next == null); index--) {
				if (page.getComponent(index).isFocusable()) {
					if (current == null) {
						current = page.getComponent(index);
					}
					else if (next == null) {
						next = page.getComponent(index);
					}
				}
			}
			if (current != null && next != null) {
				buildFocusListener(current,next,"next");
			}
		}
	}

	
	private void buildFocusListener(final Component from, final Component to, final String action) {
		from.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.getOppositeComponent() != to) {
					movePage(action);
				}
			}
			
			@Override public void focusGained(FocusEvent e) {}
		});
	}

	private void fillLocalizedStrings() {
	}
	
	private static class PageDescriptor extends JPanel {
		private static final long serialVersionUID = 5773037141534896393L;
		
		PageDescriptor(final int width, final int height, final List<JComponent> components, final List<Rectangle> constraints) {
			setLayout(new ScaledLayout(width,height,ScaledLayout.FillPolicy.FILL_MINIMUM,ScaledLayout.AlignmentPolicy.CENTER));
			
			for (int index = 0, maxIndex = components.size(); index < maxIndex; index++) {
				add(components.get(index),constraints.get(index));
			}
		}		
	}

	private static class PresentationDescriptor {
		private final int				width, height;
		private final PageDescriptor[]	pages;
		
		PresentationDescriptor(final int width, final int height, final PageDescriptor... pages) {
			this.width = width;
			this.height = height;
			this.pages = pages;
		}
		
		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
		
		public int getPageCount() {
			return pages.length;
		}
		
		public PageDescriptor getPage(final int index) {
			return pages[index];
		}
	}
}
