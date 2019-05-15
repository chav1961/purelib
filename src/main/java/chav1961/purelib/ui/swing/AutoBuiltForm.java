package chav1961.purelib.ui.swing;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
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
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;
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
	private static final URI[]				DUMMY_OK_AND_CANCEL = new URI[0];
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
	private final boolean					tooltipsOnFocus;
	private boolean							closed = false, localizerPushed = false;
	private Color							oldForeground4Label;

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
		this(localizer,new SystemErrLoggerFacade(),leftIcon,instance,formMgr,numberOfBars,false);
	}
	
	public AutoBuiltForm(final Localizer localizer, final LoggerFacade logger, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final int numberOfBars, final boolean tooltipsOnFocus) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
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
			this.tooltipsOnFocus = tooltipsOnFocus; 
			
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
							trans.message(Severity.error,e, "personal localizer failure");
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
								
								button.setName(Utils.removeQueryFromURI(node.getUIPath()).toString());
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
								
									label.setName(Utils.removeQueryFromURI(node.getUIPath()).toString()+"/label");
									childPanel.add(label,LabelledLayout.LABEL_AREA);
									field.setName(Utils.removeQueryFromURI(node.getUIPath()).toString());
									childPanel.add(field,LabelledLayout.CONTENT_AREA);
									trans.message(Severity.trace,"Append control [%1$s] type [%2$s]",node.getUIPath(),field.getClass().getCanonicalName());
									labelIds.add(node.getLabelId());
									if (!ff.isReadOnly(false) && !ff.isReadOnly(true)) {
										modifiableLabelIds.add(node.getLabelId());
									}
									accessors.put(node.getUIPath().toString(),GettersAndSettersFactory.buildGetterAndSetter(instance.getClass(),node.getName()));
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
				
				if (leftIcon != null) {
					add(new JLabel(new ImageIcon(leftIcon)),BorderLayout.WEST);
				}

				add(childPanel,BorderLayout.CENTER);
				add(buttonPanel,BorderLayout.SOUTH);
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
			listeners.clear();
			closed = true;
		}
	}

	/**
	 * <p>Get localizer associated with the form</p>
	 * @return localizer associated. Can't be null
	 */
	public Localizer getLocalizerAssociated() {
		return personalLocalizer != null ? personalLocalizer : localizer;
	}

	/**
	 * <p>Get form manager associated with the form</p>
	 * @return form manager associated. Can't be null
	 */
	public FormManager<Object,T> getFormManagerAssociated() {
		return formManager;
	}
	
	/**
	 * <p>Simulate button pressing</p>
	 * @param actionCommand application path of the command to execute
	 * @return true if the button was 'pressed'
	 */
	public boolean doClick(final URI actionCommand) {
		if (actionCommand == null) {
			throw new NullPointerException("Action command can't be null");
		}
		else {
			final boolean[]	result = new boolean[]{false};
			
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (node.getApplicationPath() != null){ 
						if(actionCommand.equals(node.getApplicationPath())) {
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
	
	/**
	 * <p>Add action listener to all the buttons in the form</p>
	 * @param listener listener to add. Can't be null
	 */
	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null"); 
		}
		else {
			listeners.addListener(listener);
		}
	}
	
	/**
	 * <p>remove action listener from all the buttons in the form</p>
	 * @param listener listener to remove. Can't be null
	 */
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
				if (metadata.getApplicationPath().toString().contains("().")) {
					try{switch (seekAndCall(instance,metadata.getApplicationPath())) {
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
					try{switch (formManager.onAction(instance,null,metadata.getApplicationPath().toString(),null)) {
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
				final URI s1 = metadata.getUIPath();
				final URI s2 = Utils.appendRelativePath2URI(metadata.getUIPath(),"./label");
				
				try{final JLabel	label = (JLabel)SwingUtils.findComponentByName(this,Utils.appendRelativePath2URI(metadata.getUIPath(),"./label").toString());
				
					oldForeground4Label = label.getForeground();
					label.setForeground(Color.BLUE);
					if (tooltipsOnFocus) {
						messages.setText(SwingUtils.prepareMessage(Severity.trace, getLocalizerAssociated().getValue(metadata.getTooltipId())));
					}
				} catch (LocalizationException  exc) {
					logger.message(Severity.error,exc,"FocusGained for [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					if (tooltipsOnFocus) {
						messages.setText("");
					}
				}
				break;
			case FocusLost:
				final JLabel	label = (JLabel)SwingUtils.findComponentByName(this,Utils.appendRelativePath2URI(metadata.getUIPath(),"./label").toString());
				
				label.setForeground(oldForeground4Label);
				messages.setText("");
				break;
			case Loading:
				final GetterAndSetter	gas = accessors.get(metadata.getUIPath().toString());
				
				if (gas == null) {
					System.err.println("SD1");
				}
				else {
					final Object			value = ModelUtils.getValueByGetter(instance, gas, metadata);
					
					if (value == null || component == null) {
						System.err.println("SD2");
					}
					((JComponentInterface)component).assignValueToComponent(value);
				}
				break;
			case Rollback:
				messages.setText("");
				break;
			case Saving:
				try{final Object	oldValue = ((JComponentInterface)component).getValueFromComponent();
				
					ModelUtils.setValueBySetter(instance, ((JComponentInterface)component).getChangedValueFromComponent(), accessors.get(metadata.getUIPath().toString()), metadata);
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
					messages.setText(SwingUtils.prepareMessage(Severity.error, error));
					return false;
				}
				else {
					messages.setText("");
					return true;
				}
			case Exit :
				return processExit(metadata,component,parameters);
			default:
				break;
		}
		return true;
	}
	
	private RefreshMode seekAndCall(final T instance, final URI appPath) throws Exception {
		final String[]	parts = URI.create(appPath.getSchemeSpecificPart()).getPath().split("/");
		Class<?>		cl = instance.getClass();
		
		while (cl != null) {
			for (Method m : cl.getDeclaredMethods()) {
				if (parts[2].startsWith(m.getName()+"().")) {
					m.setAccessible(true);
					if (m.getReturnType() == void.class) {
						m.invoke(instance);
						return RefreshMode.DEFAULT;
					}
					else {
						return (RefreshMode)m.invoke(instance);
					}
				}
			}
			cl = cl.getSuperclass();
		}
		return RefreshMode.DEFAULT;
	}

	/**
	 * <p>Get Ids of all field labels created</p> 
	 * @return list of all the labels. Can be empty but not null
	 */
	public String[] getLabelIds() {
		return labelIds.toArray(new String[labelIds.size()]); 
	}

	/**
	 * <p>Get Ids of all modifiable field labels created</p>
	 * @return list of all the labels. Can be empty but not null
	 */
	public String[] getModifiableLabelIds() {
		return modifiableLabelIds.toArray(new String[labelIds.size()]); 
	}

	/**
	 * <p>Process exit refreshing mode.</p>
	 * @param metadata metadata of the control then fires {@linkplain RefreshMode RefreshMode.Exit}
	 * @param component visual component of the control
	 * @param parameters advanced parameters (usually empty)
	 * @return returned value is passed to {@linkplain #process(chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent, ContentNodeMetadata, JComponent, Object...)} method
	 */
	protected boolean processExit(final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) {
		return true;
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
	
	/**
	 * <p>Create dialog with the form</p>
	 * @param window parent window of the dialog
	 * @param localizer localizer to use with the dialog
	 * @param form form built
	 * @return true if the 'OK' was pressed, false otherwise
	 * @throws LocalizationException on any localization problems
	 * @throws IllegalArgumentException on any parameter's problems
	 */
	public static boolean ask(final Dialog window, final Localizer localizer, final AutoBuiltForm<?> form) throws LocalizationException, IllegalArgumentException {
		return askInternal(window,new JDialog(window,true), localizer, form, DUMMY_OK_AND_CANCEL);
	}

	/**
	 * <p>Create dialog with the form</p>
	 * @param window parent window of the dialog
	 * @param localizer localizer to use with the dialog
	 * @param form form built
	 * @return true if the 'OK' was pressed, false otherwise
	 * @throws LocalizationException on any localization problems
	 * @throws IllegalArgumentException on any parameter's problems
	 */
	public static boolean ask(final Frame window, final Localizer localizer, final AutoBuiltForm<?> form) throws LocalizationException, IllegalArgumentException {
		return askInternal(window,new JDialog(window,true), localizer, form, DUMMY_OK_AND_CANCEL);
	}

	/**
	 * <p>Create dialog with the form</p>
	 * @param window parent window of the dialog
	 * @param localizer localizer to use with the dialog
	 * @param form form built
	 * @param okAndCancel application paths of the form buttons to use as 'OK' and 'Cancel'. Can be empty array (means don't use), array[1] (means use 'Cancel'), and array[2](means use 'OK' and 'Cancel');
	 * @return true if the 'OK' was pressed, false otherwise
	 * @throws LocalizationException on any localization problems
	 * @throws IllegalArgumentException on any parameter's problems
	 */
	public static boolean ask(final Dialog window, final Localizer localizer, final AutoBuiltForm<?> form, final URI[] okAndCancel) throws LocalizationException, IllegalArgumentException {
		return askInternal(window,new JDialog(window,true), localizer, form, okAndCancel);
	}

	/**
	 * <p>Create dialog with the form</p>
	 * @param window parent window of the dialog
	 * @param localizer localizer to use with the dialog
	 * @param form form built
	 * @param okAndCancel application paths of the form buttons to use as 'OK' and 'Cancel'. Can be empty array (means don't use), array[1] (means use 'Cancel'), and array[2](means use 'OK' and 'Cancel');
	 * @return true if the 'OK' was pressed, false otherwise
	 * @throws LocalizationException on any localization problems
	 * @throws IllegalArgumentException on any parameter's problems
	 */
	public static boolean ask(final Frame window, final Localizer localizer, final AutoBuiltForm<?> form, final URI[] okAndCancel) throws LocalizationException, IllegalArgumentException {
		return askInternal(window,new JDialog(window,true), localizer, form, okAndCancel);
	}
	
	private static boolean askInternal(final Window parent, final JDialog dlg, final Localizer localizer, final AutoBuiltForm<?> form, final URI[] okAndCancel) throws LocalizationException, IllegalArgumentException {
		if (okAndCancel.length > 2) {
			throw new IllegalArgumentException("Ok and cancel URI array length is too long. Only 0..2 are available");
		}
		else {
			final boolean[]	result = new boolean[] {false};
			final ActionListener okListener = (e)->{result[0] = true; dlg.setVisible(false);}; 
			final ActionListener cancelListener = (e)->{result[0] = false; dlg.setVisible(false);}; 
			
			form.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"ask.accept");
			form.getActionMap().put("ask.accept",new AbstractAction() {
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					okListener.actionPerformed(e);
				}
			});			
			
			form.mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if(node.getApplicationPath() != null) {
						if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_CLASS)) {
							try{dlg.setTitle(localizer.getValue(node.getLabelId()));
							} catch (LocalizationException exc) {
								form.formManager.getLogger().message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
							}
						}
						else if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
							final JComponent	item = (JComponent) SwingUtils.findComponentByName(form,uiPath.toString());
							
							item.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"ask.cancel");
							item.getActionMap().put("ask.cancel",new AbstractAction() {
								private static final long serialVersionUID = 1L;
								@Override
								public void actionPerformed(ActionEvent e) {
									cancelListener.actionPerformed(e);
								}
							});			
						}
					}
				}
				return ContinueMode.CONTINUE;
			}, form.mdi.getRoot().getUIPath());

			if (okAndCancel.length == 0) {
				final JButton	okButton = new JButton(localizer.getValue(PureLibLocalizer.BUTTON_OK));
				final JButton	cancelButton = new JButton(localizer.getValue(PureLibLocalizer.BUTTON_CANCEL));
				final JPanel	bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				
				okButton.addActionListener(okListener);
				cancelButton.addActionListener(cancelListener);
				bottomPanel.add(okButton);
				bottomPanel.add(cancelButton);
				dlg.getContentPane().add(bottomPanel,BorderLayout.SOUTH);
			}
			else if (okAndCancel.length == 1) {
				addActionListener(form,form.mdi,okAndCancel[0],cancelListener);
			}
			else {
				addActionListener(form,form.mdi,okAndCancel[0],okListener);
				addActionListener(form,form.mdi,okAndCancel[1],cancelListener);
			}
			dlg.getContentPane().add(form,BorderLayout.CENTER);
			dlg.pack();
			dlg.setLocationRelativeTo(parent);
			
			try{dlg.setVisible(true);
				dlg.dispose();
				return result[0];
			} finally {
				if (okAndCancel.length == 1) {	// Exclude memory leaks by subscribing
					removeActionListener(form,form.mdi,okAndCancel[0],cancelListener);
				}
				else if (okAndCancel.length == 2) {
					removeActionListener(form,form.mdi,okAndCancel[0],okListener);
					removeActionListener(form,form.mdi,okAndCancel[1],cancelListener);
				}
			}
		}
	}

	private static void addActionListener(final AutoBuiltForm<?> form, final ContentMetadataInterface mdi, final URI appPath, final ActionListener listener) {
		final boolean[]	result = new boolean[]{false};
		
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if (node.getApplicationPath() != null){ 
					if(appPath.equals(node.getApplicationPath())) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(form,node.getUIPath().toString());
						
						button.addActionListener(listener);
						return ContinueMode.STOP;
					}
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
		if (!result[0]) {
			throw new IllegalArgumentException("Application path ["+appPath+"] of the button to use as 'OK'/'Cancel' not found enywhere");  
		}
	}

	private static void removeActionListener(final AutoBuiltForm<?> form, final ContentMetadataInterface mdi, final URI appPath, final ActionListener listener) {
		final boolean[]	result = new boolean[]{false};
		
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if (node.getApplicationPath() != null){ 
					if(appPath.equals(node.getApplicationPath())) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(form,node.getUIPath().toString());
						
						button.removeActionListener(listener);
						return ContinueMode.STOP;
					}
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
		if (!result[0]) {
			throw new IllegalArgumentException("Application path ["+appPath+"] of the button to use as 'OK'/'Cancel' not found enywhere");  
		}
	}
}
