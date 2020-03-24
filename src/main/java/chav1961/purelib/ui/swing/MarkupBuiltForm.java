package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleExporter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.FormMonitor;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.FormManagedUtils.FormManagerParserCallback;
import chav1961.purelib.ui.swing.FormManagedUtils.MarkupParserCallback;
import chav1961.purelib.ui.swing.MarkupBuiltForm.Paginator.PageMoving;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.ScaledLayout;
import chav1961.purelib.ui.swing.useful.ScaledLayout.AlignmentPolicy;
import chav1961.purelib.ui.swing.useful.ScaledLayout.FillPolicy;

public class MarkupBuiltForm<T> extends JPanel implements LocaleChangeListener, AutoCloseable, JComponentMonitor, ModuleExporter {
	private static final long 				serialVersionUID = -1828992791881237479L;
	public static final int					DEFAULT_WIDTH = 80;
	public static final int					DEFAULT_HEIGHT = 25;
	
	public static final String				WIDTH = "width";
	public static final String				HEIGHT = "height";
	public static final String				CAPTION = "caption";
	public static final String				TOOLTIP = "tooltip";
	public static final String				HELP = "help";
	
	private final ContentMetadataInterface	metadata;
	private final Localizer					localizer;
	private final FormMonitor<T>			monitor;
	private final FormManager<Object,T> 	formMgr;
	private final PresentationDescriptor	desc;
	private final Map<URI,GetterAndSetter>	accessors = new HashMap<>();	
	private final JStateString				state;
	private final JLabel					pageMark;
	private final Paginator					paginator;
	private final JLabel					pageCaption;
	private final Component[]				lastFocused;
	private int								currentPage = 1;
	
	public MarkupBuiltForm(final ContentMetadataInterface metadata, final Localizer localizer, final LoggerFacade logger, final String markupDescriptor, final T instance, final FormManager<Object,T> formMgr, final boolean tooltipsOnFocus) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
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
			this.metadata = metadata;
			this.localizer = localizer;
			this.formMgr = formMgr;
			this.state = new JStateString(localizer);
			
			setLayout(new BorderLayout());
			
			final List<JButtonWithMeta>		actions = new ArrayList<>();
			final Map<String,JComponent>	components = new HashMap<>();

			FormManagedUtils.parseModel4Form(logger,metadata,localizer,instance.getClass(),this,new FormManagerParserCallback() {
				@Override
				public void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, boolean isModifiable) throws ContentException {
					accessors.put(metadata.getUIPath(),gas);
					components.put(metadata.getName(),fieldComponent);
				}
				
				@Override
				public void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException {
					actions.add(button);
				}
			});

			this.monitor = new FormMonitor<T>(localizer,state,instance,formMgr,accessors,tooltipsOnFocus) {
								@Override
								protected JComponentInterface findComponentByName(final URI uiPath) throws ContentException {
									return (JComponentInterface)SwingUtils.findComponentByName(MarkupBuiltForm.this, uiPath.toString());
								}
								
								@Override
								protected boolean processExit(final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) {
									return MarkupBuiltForm.this.processExit(metadata, component, parameters);
								}
							};
			
			this.desc = buildPresentation(metadata, components, this, markupDescriptor, instance.getClass());
			this.lastFocused = new JComponent[desc.getPageCount()];

			final Component	toolBar = !actions.isEmpty() ? buildToolbar(actions,this,instance,formMgr) : null;

			boolean	wasCaptionOrTooltip = false;	// Page caption required?
			
			for (int index = 0, maxIndex = desc.getPageCount(); !wasCaptionOrTooltip && index < maxIndex; index++) {
				wasCaptionOrTooltip |= desc.getPage(index).containsProperty(CAPTION) | desc.getPage(index).containsProperty(TOOLTIP); 
			}
			
			if (wasCaptionOrTooltip) {		// Place caption, toolbar or both to the NORTH location
				pageCaption = new JLabel("",JLabel.CENTER);
				if (toolBar != null) {
					final JPanel	north = new JPanel(new BorderLayout());
					
					north.add(pageCaption,BorderLayout.NORTH);
					north.add(toolBar,BorderLayout.CENTER);
					add(north,BorderLayout.NORTH);
				}
				else {
					add(pageCaption,BorderLayout.NORTH);
				}
			}
			else {
				pageCaption = null;
				if (toolBar != null) {
					add(toolBar,BorderLayout.NORTH);
				}
			}
			
			if (desc.getPageCount() > 1) {	// Page bar required
				this.pageMark = new JLabel("/");
				this.paginator = new Paginator(this,desc,(currentPage,oldPage,totalPages)->{setPage(currentPage,totalPages);});
				
				for (int index = 0; index < desc.getPageCount(); index++) {
					final PageDescriptor	page = desc.getPage(index); 
					
					prepareCrossPageMoving(page,index == 0, index == desc.getPageCount()-1);
				}
				final JPanel	bottom = new JPanel(new BorderLayout());
				
				bottom.add(state,BorderLayout.CENTER);
				bottom.add(pageMark,BorderLayout.EAST);
				add(bottom,BorderLayout.SOUTH);
				
				paginator.movePage(PageMoving.FIRST);
			}
			else {
				this.pageMark = null;
				this.paginator = null;
				add(desc.getPage(0).getComponent(),BorderLayout.CENTER);
				if (desc.getPage(0).containsProperty(HELP)) {
					SwingUtils.assignActionKey(desc.getPage(0).getComponent(),JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,SwingUtils.KS_HELP,(e)->{
						try{SwingUtils.showCreoleHelpWindow(MarkupBuiltForm.this,URI.create(e.getActionCommand()));
						} catch (NullPointerException | IOException exc) {
							message(Severity.severe,exc.getLocalizedMessage());
						}
					},desc.getPage(0).getProperty(HELP)[0]);
				}
				add(state,BorderLayout.SOUTH);
			}
			
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		state.localeChanged(oldLocale, newLocale);
		for (int index = 0; index < desc.getPageCount(); index++) {
			SwingUtils.refreshLocale(desc.getPage(index).getComponent(),oldLocale,newLocale);
		}
	}

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
	}

	@Override
	public Module[] getUnnamedModules() {
		for (Entry<URI, GetterAndSetter> item : accessors.entrySet()) {
			return new Module[] {item.getValue().getClass().getClassLoader().getUnnamedModule()};
		}
		return null;
	}
	
	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
		return monitor.process(event, metadata, component, parameters);
	}

	/**
	 * <p>Get localizer associated with the form</p>
	 * @return localizer associated. Can't be null
	 */
	public Localizer getLocalizerAssociated() {
		return localizer;
	}

	/**
	 * <p>Get form manager associated with the form</p>
	 * @return form manager associated. Can't be null
	 */
	public FormManager<Object,T> getFormManagerAssociated() {
		return formMgr;
	}
	
	/**
	 * <p>Get content model for instance </p>
	 * @return content model. Can't be null
	 */
	public ContentMetadataInterface getContentModel() {
		return metadata;
	}
	
	protected boolean processExit(final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) {
		return true;
	}

	protected void message(final Severity level, final String format, final Object... parameters) {
		state.message(level, format, parameters);
	}
	
	protected void message(final Severity level, final Throwable t, final String format, final Object... parameters) {
		state.message(level, t, format, parameters);
	}
	
	private void setPage(final int currentPage, final int totalPages) {
		pageMark.setText(currentPage+"/"+desc.getPageCount());
		fillPageCaption();
	}
	
	private static <T> PresentationDescriptor buildPresentation(final ContentMetadataInterface metadata, final Map<String,JComponent> componentList, final JComponentMonitor monitor, final String markupDescriptor, final Class<T> clazz) throws SyntaxException {
		final List<PageDescriptor>	pages = new ArrayList<>();
		Hashtable<String,String[]>	props;
		String						pageContent;
		int							width = 0, height = 0;
		int							start = 0, end, nl;

		if (markupDescriptor.startsWith(">>")) {
			nl = markupDescriptor.indexOf('\n');
			if (nl > start+2) {
				props = URIUtils.parseQuery(markupDescriptor.substring(start+2,nl));
			}
			else {
				props = new Hashtable<>();
			}
			
			width = props.contains(WIDTH) ? Integer.valueOf(props.get(WIDTH)[0]) : DEFAULT_WIDTH;
			height = props.contains(HEIGHT) ? Integer.valueOf(props.get(HEIGHT)[0]) : DEFAULT_HEIGHT;
			start = nl + 1;
			
			while ((end = markupDescriptor.indexOf("\n>>",start)) >= 0) {
				pageContent = markupDescriptor.substring(start,end);
				pages.add(new PageDescriptor(buildPage(metadata,componentList,monitor,clazz,width,height,pageContent),props));
				start = end+1;
				nl = markupDescriptor.indexOf('\n',start);
				if (nl > start+2) {
					props = URIUtils.parseQuery(markupDescriptor.substring(start+2,nl));
				}
				else {
					props = new Hashtable<>();
				}
				start = nl + 1;
			}
			pages.add(new PageDescriptor(buildPage(metadata,componentList,monitor,clazz,width,height,markupDescriptor.substring(start)),props));
		}
		return new PresentationDescriptor(width,height,pages.toArray(new PageDescriptor[pages.size()]));
	}
	
	private static <T> JPanel buildPage(final ContentMetadataInterface metadata, final Map<String, JComponent> componentList, final JComponentMonitor monitor, final Class<T> clazz, final int width, final int height, final String pageContent) throws SyntaxException, IllegalArgumentException, NullPointerException {
		final List<JPanel>			stack = new ArrayList<>();
		final MarkupParserCallback	callback = new MarkupParserCallback() {
										@Override
										public void placePlainText(final int x, final int y, final int width, final int height, final boolean bold, final boolean italic, final boolean caption, final String content) throws ContentException {
											final Font		font = new Font("monospace",(bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0),1);
											final Color		color = caption ? Color.BLUE : Color.BLACK;
											final JLabel	label = new JLabel(content);
											
											label.setForeground(color);
											label.setFont(font);
											stack.get(0).add(label, new Rectangle(x,y,width,height));
										}
							
										@Override
										public void placeSeparator(final int x, final int y, final int width, final int height) throws ContentException {
											stack.get(0).add(new JSeparator(width > height ? JSeparator.HORIZONTAL : JSeparator.VERTICAL),new Rectangle(x,y,width,height));
										}
							
										@Override
										public void placeField(final int x, final int y, final int width, final int height, final String componentName, final String initialValue) throws ContentException {
											if (componentList.containsKey(componentName)) {
												final JComponent	component = componentList.get(componentName); 
												
												stack.get(0).add(component,new Rectangle(x,y,width,height));
											}
											else {
												throw new ContentException("Field ["+componentName+"] referenced from the form not found");
											}
										}
							
										@Override
										public void pushContent(final int x, final int y, final int width, final int height, final String caption) throws ContentException {
											final JPanel	nested = new JPanel(new ScaledLayout(width, height, FillPolicy.FILL_MINIMUM, AlignmentPolicy.CENTER));
											
											stack.get(0).add(nested,new Rectangle(x,y,width,height));
											stack.add(0,nested);
										}
							
										@Override
										public void popContent() throws ContentException {
											stack.remove(0);
										}
									};
		stack.add(new JPanel(new ScaledLayout(width,height,FillPolicy.FILL_MINIMUM,AlignmentPolicy.CENTER)));
		FormManagedUtils.parseMarkup(pageContent,callback);
		
		return stack.remove(0);
	}

	private Component buildToolbar(final List<JButtonWithMeta> actions, final JComponentMonitor monitor, T instance, final FormManager<Object, T> formMgr) throws LocalizationException, ContentException {
		final JToolBar	bar = new JToolBar(JToolBar.HORIZONTAL);
		
		for (JButtonWithMeta item : actions) {
			bar.add(item);
			item.addActionListener((e)->{
				try {
					formMgr.onAction(instance,null,((NodeMetadataOwner)item).getNodeMetadata().getApplicationPath().toString(),null);
				} catch (LocalizationException | FlowException exc) {
					message(Severity.error,exc,exc.getLocalizedMessage());
				}
			});
		}
		return bar;
	}

	private void prepareCrossPageMoving(final PageDescriptor page, final boolean first, final boolean last) {
		Component	current, next;
		
		if (first) {
			current = next = null;
			for (int index = 0; index < page.getComponent().getComponentCount() && (current == null || next == null); index++) {
				if (page.getComponent().getComponent(index).isFocusable()) {
					if (current == null) {
						current = page.getComponent().getComponent(index);
					}
					else if (next == null) {
						next = page.getComponent().getComponent(index);
					}
				}
			}
			if (current != null && next != null) {
				buildFocusListener(current,next,PageMoving.PREV);
			}
		}
		
		if (last) {
			current = next = null;
			for (int index = page.getComponent().getComponentCount()-1; index >= 0 && (current == null || next == null); index--) {
				if (page.getComponent().getComponent(index).isFocusable()) {
					if (current == null) {
						current = page.getComponent().getComponent(index);
					}
					else if (next == null) {
						next = page.getComponent().getComponent(index);
					}
				}
			}
			if (current != null && next != null) {
				buildFocusListener(current,next,PageMoving.NEXT);
			}
		}
	}

	
	private void buildFocusListener(final Component from, final Component to, final PageMoving action) {
		from.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.getOppositeComponent() != to) {
					paginator.movePage(action);
				}
			}
			
			@Override public void focusGained(FocusEvent e) {}
		});
	}

	private void fillLocalizedStrings() {
		fillPageCaption();
	}
	
	private void fillPageCaption() {
		if (pageCaption != null) {
			if (desc.getPage(currentPage-1).containsProperty(CAPTION)) {
				try{pageCaption.setText(getLocalizerAssociated().getValue(desc.getPage(currentPage-1).getProperty(CAPTION)[0]));
					if (desc.getPage(currentPage-1).containsProperty(TOOLTIP)) {
						pageCaption.setToolTipText(getLocalizerAssociated().getValue(desc.getPage(currentPage-1).getProperty(TOOLTIP)[0]));
					}
				} catch (LocalizationException e) {
					message(Severity.error,e.getLocalizedMessage());
				}
			}
			else {
				pageCaption.setText("");
				pageCaption.setToolTipText("");
			}
		}
	}
	
	static class Paginator {
		private final JScrollBar				pageBar;
		private final JPanel					centralPanel;
		private final PresentationDescriptor	desc;
		private final PageIndicator				indicator;
		private final Component[]				lastFocused;
		private int								currentPage = 1;
		
		@FunctionalInterface
		interface PageIndicator {
			void setCurrentPage(final int pageNo, final int lastPageNo, final int pageTotal);
			default void showHelp(final int pageNo, final URI helpURI) {}
		}

		enum PageMoving {
			FIRST, NEXT, PREV, LAST
		}
		
		Paginator(final JComponent container, final PresentationDescriptor desc, final PageIndicator indicator) {
			this.pageBar = new JScrollBar(JScrollBar.VERTICAL,1,1,1,desc.getPageCount());
			this.centralPanel = new JPanel();
			this.desc = desc;
			this.indicator = indicator;
			this.lastFocused = new Component[desc.getPageCount()];

			container.add(pageBar,BorderLayout.EAST);
			SwingUtils.assignActionKey(container,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0),(e)->{movePage(PageMoving.valueOf(e.getActionCommand()));},PageMoving.NEXT.name());
			SwingUtils.assignActionKey(container,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,InputEvent.CTRL_DOWN_MASK),(e)->{movePage(PageMoving.valueOf(e.getActionCommand()));},PageMoving.LAST.name());
			SwingUtils.assignActionKey(container,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0),(e)->{movePage(PageMoving.valueOf(e.getActionCommand()));},PageMoving.PREV.name());
			SwingUtils.assignActionKey(container,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,InputEvent.CTRL_DOWN_MASK),(e)->{movePage(PageMoving.valueOf(e.getActionCommand()));},PageMoving.FIRST.name());
			
			centralPanel.setLayout(new CardLayout());
			for (int index = 0; index < desc.getPageCount(); index++) {
				final PageDescriptor	page = desc.getPage(index); 
				
				centralPanel.add(page.getComponent(),"p"+(index+1));
				if (page.containsProperty(HELP)) {
					SwingUtils.assignActionKey(page.getComponent(),JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,SwingUtils.KS_HELP,(e)->{
						indicator.showHelp(currentPage,URI.create(e.getActionCommand()));
					},page.getProperty(HELP)[0]);
				}
			}
			container.add(centralPanel,BorderLayout.CENTER);

			movePage(PageMoving.FIRST);
		}
		
		void movePage(final PageMoving action) {
			final int	lastPageNo = currentPage;
			
			lastFocused[currentPage-1] = FocusManager.getCurrentManager().getFocusOwner();
			
			switch (action) {
				case FIRST	:
					currentPage = 1;
					break;
				case PREV	:
					if (currentPage > 1) {
						currentPage--;
					}
					break;
				case NEXT	:
					if (currentPage < desc.getPageCount()) {
						currentPage++;
					}
					break;
				case LAST	:
					currentPage = desc.getPageCount();
					break;
				default : throw new UnsupportedOperationException("Moving option ["+action+"] is not supported yet"); 
			}

			((CardLayout)centralPanel.getLayout()).show(centralPanel,"p"+currentPage);
			pageBar.setValue(currentPage);
			indicator.setCurrentPage(currentPage,lastPageNo,desc.getPageCount());
			
			if (lastFocused[currentPage-1] != null) {
				lastFocused[currentPage-1].requestFocus();
			}
			else {
				final JPanel	current = desc.getPage(currentPage-1).getComponent();
				
				for (int index = 0, maxIndex = current.getComponentCount(); index < maxIndex; index++) {
					if (!(current.getComponent(index) instanceof JLabel)) {
						current.getComponent(index).requestFocus();
					}
				}
			}
		}
	}
	
	
	private static class PageDescriptor {
		private final JPanel						content;
		private final Hashtable<String,String[]>	props;
		
		PageDescriptor(final JPanel content, final Hashtable<String,String[]> props) {
			this.content = content;
			this.props = props;
		}
		
		JPanel getComponent() {
			return content;
		}
		
		String[] getProperty(final String key) {
			return props.get(key);
		}
		
		boolean containsProperty(final String key) {
			return props.contains(key);
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
		
		int getWidth() {
			return width;
		}

		int getHeight() {
			return height;
		}
		
		int getPageCount() {
			return pages.length;
		}
		
		PageDescriptor getPage(final int index) {
			return pages[index];
		}
	}

}
