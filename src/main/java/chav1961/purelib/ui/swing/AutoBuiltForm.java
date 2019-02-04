package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.NullLoggerFacade;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfacers.Action;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

/**
 * <p>This class is a simplest for builder for any class. It supports the {@linkplain FormManager} interface to process user actions in the generated form.</p>
 * <p>Form was build has two columns:</p>
 * <ul>
 * <li>field label<li>      
 * <li>field to input content<li>
 * </ul>
 * <p>Fields of the class to show must be annotated with {@linkplain LocaleResource} annotations. These annotations are source for the field labels and field tooltips 
 * in the form built. Form can also contains a set of buttons to process actions on it. These buttons are not represents as fields in the class to show, but as 
 * {@linkplain Action} annotation before class description. Any user actions on the form showing fire {@linkplain FormManager#onField(Object, Object, String, Object)} or
 * {@linkplain FormManager#onAction(Object, Object, String, Object)} calls on the {@linkplain FormManager} interface. Yo simplify code, it's recommended that class to show
 * also implements {@linkplain FormManager} interface itself.</p>
 * <p>Form built doesn't contain any predefined buttons ("OK", "Cancel" and so on). You must close this form yourself</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 */


public class AutoBuiltForm<T> extends JPanel implements LocaleChangeListener, AutoCloseable, JComponentMonitor {
	private static final long 				serialVersionUID = 4920624779261769348L;
	private static final int				GAP_SIZE = 5; 

	private final T							instance;
	private final LoggerFacade				logger;
	private final Localizer					localizer, personalLocalizer;
	private final FormManager<Object,T>		formManager;
	private final ContentMetadataInterface	mdi;
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class);
	private final Set<String>				labelIds = new HashSet<>(), modifiableLabelIds = new HashSet<>();
	private final Map<String,GetterAndSetter>	accessors = new HashMap<>();	
	private final JLabel					messages = new JLabel("",JLabel.LEFT);
	private boolean							closed = false, localizerPushed = false;

	public AutoBuiltForm(final Localizer localizer, final T instance, final FormManager<Object,T> formMgr) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer, null, instance, formMgr, 1);
	}

	public AutoBuiltForm(final Localizer localizer, final T instance, final FormManager<Object,T> formMgr, final int columns) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer, null, instance, formMgr, columns);
	}

	public AutoBuiltForm(final Localizer localizer, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer, leftIcon, instance, formMgr, 1);
	}

	public AutoBuiltForm(final Localizer localizer, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final int numberOfBars) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(localizer,new NullLoggerFacade(),leftIcon,instance,formMgr,numberOfBars);
	}
	
	public AutoBuiltForm(final Localizer localizer, final LoggerFacade logger, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final int numberOfBars) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance can't be null");
		}
		else if (formMgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else if (numberOfBars < 1) {
			throw new IllegalArgumentException("Bars count must be positive");
		}
		else {
			final BorderLayout				totalLayout = new BorderLayout(GAP_SIZE, GAP_SIZE);
			final JPanel					childPanel = new JPanel(new LabelledLayout(numberOfBars, GAP_SIZE, GAP_SIZE, LabelledLayout.VERTICAL_FILLING));
			final JPanel					buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			final Class<?>					instanceClass = instance.getClass();

			this.logger = logger;
			
			try(final LoggerFacade			trans = logger.transaction(this.getClass().getSimpleName())) {
				this.instance = instance;
				this.formManager = formMgr;
				this.mdi = ContentModelFactory.forAnnotatedClass(instanceClass);
				
				if (mdi.getRoot().getLocalizerAssociated() != null) {
					trans.message(Severity.trace, "Localizer associated=%1$s",mdi.getRoot().getLocalizerAssociated());
					if (!localizer.containsLocalizerHere(mdi.getRoot().getLocalizerAssociated().toString())) {
						try{this.personalLocalizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
							this.localizer = localizer.push(this.personalLocalizer);
							this.localizerPushed = true;
							trans.message(Severity.trace, "Localizer push=%1$s",mdi.getRoot().getLocalizerAssociated());
						} catch (IOException e) {
							trans.message(Severity.error,e, "personal localizer faulure");
							throw new ContentException(e); 
						}
					}
					else {
						this.localizer = localizer;
						this.personalLocalizer = localizer.getLocalizerById(mdi.getRoot().getLocalizerAssociated().toString());
						trans.message(Severity.trace, "Localizer found=%1$s",mdi.getRoot().getLocalizerAssociated());
					}
				}
				else {
					this.localizer = localizer;
					this.personalLocalizer = null;
					trans.message(Severity.trace, "No localizers associated");
				}
	
				buttonPanel.add(messages);
				mdi.walkDown((mode,applicationPath,uiPath,node)->{
					if (mode == NodeEnterMode.ENTER) {
						if (node.getApplicationPath() != null){ 
							if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_ACTION)) {
								final JButton		button = new JButton();
								
								button.setName(node.getUIPath().toString());
								trans.message(Severity.trace,"Append button [%1$s]",node.getApplicationPath());
	
								button.setActionCommand(node.getApplicationPath().toString());
								button.addActionListener((e)->{
									listeners.fireEvent((l)->{
										if (l instanceof ActionListener) {
											((ActionListener)l).actionPerformed(e);
										}
									});
									try{process(MonitorEvent.Action,node,button);
									} catch (ContentException exc) {
										logger.message(Severity.error,exc,"Button [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
									}
								});
								buttonPanel.add(button);							
							}
							if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
								try{final JLabel		label = new JLabel();
									final FieldFormat	ff = node.getFormatAssociated();
									final JComponent 	field = SwingUtils.prepareRenderer(node, ff, this);
								
									label.setName(node.getUIPath().toString()+"/label");
									childPanel.add(label,LabelledLayout.LABEL_AREA);
									field.setName(node.getUIPath().toString());
									childPanel.add(field,LabelledLayout.CONTENT_AREA);
									trans.message(Severity.trace,"Append control [%1$s] type [%2$s]",node.getUIPath(),field.getClass().getCanonicalName());
									labelIds.add(node.getLabelId());
									if (!ff.isReadOnly(false) && !ff.isReadOnly(true)) {
										modifiableLabelIds.add(node.getLabelId());
									}
									accessors.put(node.getUIPath().toString(),GettersAndSettersFactory.buildGetterAndSetter(instance.getClass(),node.getName()));
									process(MonitorEvent.Loading,node,field);
								} catch (LocalizationException | ContentException exc) {
									logger.message(Severity.error,exc,"Control [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
								}
							}
						}
					}
					return ContinueMode.CONTINUE;
				}, mdi.getRoot().getUIPath());
	
				setLayout(totalLayout);
				childPanel.validate();
				add(childPanel,BorderLayout.CENTER);
				add(buttonPanel,BorderLayout.SOUTH);
				
				if (leftIcon != null) {
					add(new JLabel(new ImageIcon(leftIcon)),BorderLayout.WEST);
				}
				
				fillLocalizedStrings();
				trans.rollback();
			}
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException, NullPointerException {
		if (oldLocale == null) {
			throw new NullPointerException("Old locale can't be null");
		}
		else if (newLocale == null) {
			throw new NullPointerException("New locale can't be null");
		}
		else {
			fillLocalizedStrings();
		}
	}

	@Override
	public void close() {
		if (!closed){
			closed = true;
		}
	}
	
	public Localizer getLocalizerAssociated() {
		return personalLocalizer != null ? personalLocalizer : localizer;
	}

	public FormManager<Object,T> getFormManagerAssociated() {
		return formManager;
	}
	
	public boolean doClick(final String actionCommand) {
		if (actionCommand == null || actionCommand.isEmpty()) {
			throw new IllegalArgumentException("Action command string can't be null or empty");
		}
		else {
			final boolean[]	result = new boolean[]{false};
			
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (node.getApplicationPath() != null){ 
						if(actionCommand.equals(node.getApplicationPath().toString())) {
							final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString());
							
							result[0] = true;
							button.doClick();
							return ContinueMode.STOP;
						}
					}
				}
				return ContinueMode.CONTINUE;
			}, mdi.getRoot().getUIPath());
			return result[0];
		}
	}
	
	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null"); 
		}
		else {
			listeners.addListener(listener);
		}
	}
	
	public void removeActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null"); 
		}
		else {
			listeners.removeListener(listener);
		}
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) throws ContentException {
		switch (event) {
			case Action:
				try{switch (formManager.onAction(instance,null,metadata.getApplicationPath().toString(),null)) {
						case REJECT : case FIELD_ONLY : case DEFAULT : case NONE :
							break;
						case TOTAL : case RECORD_ONLY :
							for (ContentNodeMetadata item : metadata.getParent()) {
								final JComponent	comp = (JComponent) SwingUtils.findComponentByName(this, item.getUIPath().toString());
								
								process(MonitorEvent.Loading,item,comp);
							}
							break;
						default	:
							break;
					}
				} catch (LocalizationException | FlowException exc) {
					logger.message(Severity.error,exc,"Action [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
				}
				break;
			case FocusGained:
				try{messages.setText(SwingUtils.prepareMessage(Severity.trace, getLocalizerAssociated().getValue(metadata.getTooltipId())));
				} catch (LocalizationException  exc) {
					logger.message(Severity.error,exc,"FocusGained for [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					messages.setText("");
				}
				break;
			case FocusLost:
				messages.setText("");
				break;
			case Loading:
				((JComponentInterface)component).assignValueToComponent(ModelUtils.getValueByGetter(instance, accessors.get(metadata.getName()), metadata));
				break;
			case Rollback:
				messages.setText("");
				break;
			case Saving:
				try{final Object	oldValue = ((JComponentInterface)component).getChangedValueFromComponent();
				
					ModelUtils.setValueBySetter(instance, ((JComponentInterface)component).getValueFromComponent(), accessors.get(metadata.getName()), metadata);
					switch (formManager.onField(instance,null,metadata.getName(),oldValue)) {
						case FIELD_ONLY : case DEFAULT : case NONE :
							break;
						case TOTAL : case RECORD_ONLY :
							for (ContentNodeMetadata item : metadata.getParent()) {
								final JComponent	comp = (JComponent) SwingUtils.findComponentByName(this, item.getUIPath().toString());
								
								process(MonitorEvent.Loading,item,comp);
							}
							break;
						case REJECT		:
							ModelUtils.setValueBySetter(instance, oldValue, accessors.get(metadata.getName()), metadata);
							((JComponentInterface)component).assignValueToComponent(oldValue);
							break;
						default	:
							break;
					}
				} catch (LocalizationException | FlowException exc) {
					logger.message(Severity.error,exc,"Saving for [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
				}
				break;
			case Validation:
				final String	error = ((JComponentInterface)component).standardValidation(((JComponentInterface)component).getChangedValueFromComponent().toString());
				
				if (error != null) {
					messages.setText(SwingUtils.prepareMessage(Severity.error, error));
					return false;
				}
				else {
					messages.setText("");
					return true;
				}
			default:
				break;
		}
		return true;
	}
	
	public String[] getLabelIds() {
		return labelIds.toArray(new String[labelIds.size()]); 
	}

	public String[] getModifiableLabelIds() {
		return modifiableLabelIds.toArray(new String[labelIds.size()]); 
	}

	private void fillLocalizedStrings() {
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_ACTION)) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{button.setText(localizer.getValue(node.getLabelId()));
						button.setToolTipText(localizer.getValue(node.getTooltipId()));
						} catch (LocalizationException exc) {
							logger.message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
						}
					}
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
						final JLabel		label = (JLabel) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/label");
						final JComponent	field = (JComponent) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{label.setText(localizer.getValue(node.getLabelId()));
							field.setToolTipText(localizer.getValue(node.getTooltipId()));
						} catch (LocalizationException exc) {
							logger.message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
						}
					}
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
	}
}
