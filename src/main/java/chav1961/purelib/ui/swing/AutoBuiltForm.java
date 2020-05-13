package chav1961.purelib.ui.swing;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleExporter;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.FormMonitor;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.FormManagedUtils.FormManagerParserCallback;
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
 * <p>If number of fields in the form is too long, you can split form to a set of two-column pairs (named <i>bars</b>). Fields of the class to show must be annotated at least with {@linkplain LocaleResource} annotations. These annotations are source for the field labels and field tooltips 
 * in the form to build. Form can also contains a set of buttons to process actions on it. These buttons are not represents as fields in the class to show, but as 
 * {@linkplain Action} annotation before class description. Any user actions on the form fire {@linkplain FormManager#onField(Object, Object, String, Object)} or
 * {@linkplain FormManager#onAction(Object, Object, String, Object)} calls on the {@linkplain FormManager} interface. To simplify code, it's recommended that class to show
 * also implements {@linkplain FormManager} interface itself.</p>
 * <p>Form built doesn't contain any predefined buttons ("OK", "Cancel" and similar). You must close this form yourself, if required</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.4
 */

public class AutoBuiltForm<T> extends JPanel implements LocaleChangeListener, AutoCloseable, JComponentMonitor, ModuleExporter {
	private static final long 				serialVersionUID = 4920624779261769348L;
	
	public static final String				DEFAULT_OK_BUTTON_NAME = "ask.accept";
	public static final String				DEFAULT_CANCEL_BUTTON_NAME = "ask.cancel";
	
	private static final URI[]				DUMMY_OK_AND_CANCEL = new URI[0];
	private static final int				GAP_SIZE = 5; 

	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final JPanel					childPanel;
	private final JLabel					leftIconLabel;
	private final FormManager<Object,T>		formManager;
	private final FormMonitor<T>			monitor;
	private final ContentMetadataInterface	mdi;
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class);
	private final Set<String>				labelIds = new HashSet<>(), modifiableLabelIds = new HashSet<>();
	private final Map<URI,GetterAndSetter>	accessors = new HashMap<>();	
	private final JLabel					messages = new JLabel("",JLabel.LEFT);
	private boolean							closed = false;

	/**
	 * <p>Constructor of the class</p>
	 * @param mdi metadata for the instance will be showed
	 * @param localizer localizer associated with the given instance
	 * @param instance instance to show
	 * @param formMgr form manager for the instance. It's strongly recommended for instance to implement this interface self
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException any errors in arguments
	 * @throws SyntaxException errors in class or fields annotations
	 * @throws LocalizationException errors in localizer
	 * @throws ContentException errors in class or fields annotations
	 */
	public AutoBuiltForm(final ContentMetadataInterface mdi, final Localizer localizer, final T instance, final FormManager<Object,T> formMgr) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(mdi, localizer, instance, formMgr, 1);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param mdi metadata for the instance will be showed
	 * @param localizer localizer associated with the given instance
	 * @param instance instance to show
	 * @param formMgr form manager for the instance. It's strongly recommended for instance to implement this interface self
	 * @param columns number of bars in the form
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException any errors in arguments
	 * @throws SyntaxException errors in class or fields annotations
	 * @throws LocalizationException errors in localizer
	 * @throws ContentException errors in class or fields annotations
	 */
	public AutoBuiltForm(final ContentMetadataInterface mdi, final Localizer localizer, final T instance, final FormManager<Object,T> formMgr, final int columns) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(mdi, localizer, PureLibSettings.CURRENT_LOGGER, null, instance, formMgr, columns, false);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param mdi metadata for the instance will be showed
	 * @param localizer localizer associated with the given instance
	 * @param leftIcon icon will be shown on the left of the form built
	 * @param instance instance to show
	 * @param formMgr form manager for the instance. It's strongly recommended for instance to implement this interface self
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException any errors in arguments
	 * @throws SyntaxException errors in class or fields annotations
	 * @throws LocalizationException errors in localizer
	 * @throws ContentException errors in class or fields annotations
	 */
	public AutoBuiltForm(final ContentMetadataInterface mdi, final Localizer localizer, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		this(mdi, localizer, PureLibSettings.CURRENT_LOGGER, leftIcon, instance, formMgr, 1, false);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param mdi metadata for the instance will be showed
	 * @param localizer localizer associated with the given instance
	 * @param logger logger to get diagnostics while form is built. Defaults use current logger of the {@linkplain PureLibSettings} class. 
	 * @param leftIcon icon will be shown on the left of the form built
	 * @param instance instance to show
	 * @param formMgr form manager for the instance. It's strongly recommended for instance to implement this interface self
	 * @param columns number of bars in the form
	 * @param tooltipsOnFocus true if tooltips require in the state string when field gets focus
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException any errors in arguments
	 * @throws SyntaxException errors in class or fields annotations
	 * @throws LocalizationException errors in localizer
	 * @throws ContentException errors in class or fields annotations
	 */
	public AutoBuiltForm(final ContentMetadataInterface mdi, final Localizer localizer, final LoggerFacade logger, final URL leftIcon, final T instance, final FormManager<Object,T> formMgr, final int numberOfBars, final boolean tooltipsOnFocus) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (localizer == null) {
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
			final BorderLayout	totalLayout = new BorderLayout(GAP_SIZE, GAP_SIZE);
			final JPanel		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			this.mdi = mdi;
			this.logger = logger;
			this.formManager = formMgr;
			childPanel = new JPanel(new LabelledLayout(numberOfBars, GAP_SIZE, GAP_SIZE, LabelledLayout.VERTICAL_FILLING));
			
			try(final LoggerFacade		trans = logger.transaction(this.getClass().getSimpleName())) {
				final List<JComponent>	ordinalFocused = new ArrayList<>(), outputFocused = new ArrayList<>();
				
				this.localizer = localizer.push(mdi.getRoot().getLocalizerAssociated());
				
				buttonPanel.add(messages);
				
				FormManagedUtils.parseModel4Form(logger,mdi,localizer,instance.getClass(),this,new FormManagerParserCallback() {
					boolean	firstFocused = false;
					
					@Override
					public void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, boolean isModifiable) throws ContentException {
						childPanel.add(fieldLabel,LabelledLayout.LABEL_AREA);
						childPanel.add(fieldComponent,LabelledLayout.CONTENT_AREA);
						if (!firstFocused) {
							firstFocused = true;
							fieldComponent.requestFocusInWindow();
						}
						trans.message(Severity.trace,"Append control [%1$s] type [%2$s]",metadata.getUIPath(),metadata.getClass().getCanonicalName());
						labelIds.add(metadata.getLabelId());
						if (!metadata.getFormatAssociated().isReadOnly(false) && !metadata.getFormatAssociated().isReadOnly(true)) {
							modifiableLabelIds.add(metadata.getLabelId());
						}
						accessors.put(metadata.getUIPath(),gas);
						if (metadata.getFormatAssociated().isOutput()) {
							outputFocused.add(fieldComponent);
						}
						else {
							ordinalFocused.add(fieldComponent);
						}
					}
					
			 		@Override
					public void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException {
						button.addActionListener((e)->{
							listeners.fireEvent((l)->{
								if (l instanceof ActionListener) {
									((ActionListener)l).actionPerformed(e);
								}
							});
							try{process(MonitorEvent.Action,metadata,button);
							} catch (ContentException exc) {
								logger.message(Severity.error,exc,"Button [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
							}
						});
						
						buttonPanel.add(button);							
						ordinalFocused.add(button);
					}
				});
				
				this.monitor = new FormMonitor<T>(localizer,logger,instance,formMgr,accessors,tooltipsOnFocus) {
									@Override
									protected JComponentInterface findComponentByName(final URI uiPath) throws ContentException {
										return (JComponentInterface)SwingUtils.findComponentByName(AutoBuiltForm.this, uiPath.toString());
									}
									
									@Override
									protected boolean processExit(final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) {
										return AutoBuiltForm.this.processExit(metadata, component, parameters);
									}
								};

				if (!outputFocused.isEmpty()) {
					setFocusTraversalPolicy(new ABFFocusTraversalPolicy(ordinalFocused, outputFocused));
					setFocusCycleRoot(true);
				}

				setLayout(totalLayout);
				childPanel.validate(); 
				
				if (leftIcon != null) {
					add(leftIconLabel = new JLabel(new ImageIcon(leftIcon)),BorderLayout.WEST);
				}
				else {
					leftIconLabel = null;
				}

				add(childPanel,BorderLayout.CENTER);
				add(buttonPanel,BorderLayout.SOUTH);
				fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
				
				trans.rollback();
			}
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException, NullPointerException {
		fillLocalizedStrings(oldLocale,newLocale);
	}

	@Override
	public void close() {
		if (!closed){
			listeners.clear();
			try{localizer.close();
			} catch (LocalizationException e) {
			}
			closed = true;
		}
	}

	@Override
	public Module[] getUnnamedModules() {
		for (Entry<URI, GetterAndSetter> item : accessors.entrySet()) {
			return new Module[] {item.getValue().getClass().getClassLoader().getUnnamedModule()};
		}
		return null;
	}

	@Override
	public void setPreferredSize(final Dimension preferredSize) {
		final Dimension	childPanelPrefSize = childPanel.getPreferredSize(); 	
		final Dimension	leftIconPrefSize = leftIconLabel != null ? leftIconLabel.getPreferredSize() : new Dimension(0,0); 	
		
		super.setPreferredSize(preferredSize);
		childPanel.setPreferredSize(new Dimension(preferredSize.width-leftIconPrefSize.width,childPanelPrefSize.height));
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
		return formManager;
	}
	
	/**
	 * <p>Get content model for instance </p>
	 * @return content model. Can.t be null
	 */
	public ContentMetadataInterface getContentModel() {
		return mdi;
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
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
		final JLabel		label = (JLabel) SwingUtils.findComponentByName(this,metadata.getUIPath().toString()+"/label");
		
		switch(event) {
			case FocusGained	:
				if (label != null) {
					label.setForeground(Color.BLUE);
				}
				break;
			case FocusLost		:
				if (label != null) {
					label.setForeground(Color.BLACK);
				}
				break;
			default:
				break;
		}
		return monitor.process(event, metadata, component, parameters);
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
	protected boolean processExit(final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) {
		return true;
	}

	private void fillLocalizedStrings(final Locale oldLocale, final Locale newLocale) {
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION)) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{button.setText(getLocalizerAssociated().getValue(node.getLabelId()));
							if (node.getTooltipId() != null && !node.getTooltipId().trim().isEmpty()) {
								button.setToolTipText(getLocalizerAssociated().getValue(node.getTooltipId().trim()));
							}
						} catch (LocalizationException exc) {
							logger.message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
						}
					}
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
						final JLabel		label = (JLabel) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/label");
						final JComponent	field = (JComponent) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{label.setText(getLocalizerAssociated().getValue(node.getLabelId()));
							if (node.getTooltipId() != null && !node.getTooltipId().trim().isEmpty()) {
								field.setToolTipText(getLocalizerAssociated().getValue(node.getTooltipId().trim()));
							}
							if (field instanceof LocaleChangeListener) {
								((LocaleChangeListener)field).localeChanged(oldLocale, newLocale);
							}
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
	
	static boolean askInternal(final Window parent, final JDialog dlg, final Localizer localizer, final AutoBuiltForm<?> form, final URI[] okAndCancel) throws LocalizationException, IllegalArgumentException {
		if (okAndCancel.length > 2) {
			throw new IllegalArgumentException("Ok and cancel URI array length is too long. Only 0..2 are available");
		}
		else {
			final boolean[]			result = new boolean[] {false};
			final ActionListener 	okListener = (e)->{result[0] = true; dlg.setVisible(false);}; 
			final ActionListener 	cancelListener = (e)->{result[0] = false; dlg.setVisible(false);}; 
			
			SwingUtils.assignActionKey(form,WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,SwingUtils.KS_ACCEPT,(e)->okListener.actionPerformed(e),DEFAULT_OK_BUTTON_NAME);
			SwingUtils.assignActionKey(form,WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,SwingUtils.KS_EXIT,(e)->cancelListener.actionPerformed(e),DEFAULT_CANCEL_BUTTON_NAME);
			form.mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if(node.getApplicationPath() != null) {
						if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_CLASS)) {
							try{dlg.setTitle(localizer.getValue(node.getLabelId()));
							} catch (LocalizationException exc) {
								dlg.setTitle(node.getLabelId());
								form.formManager.getLogger().message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
							}
						}
					}
				}
				return ContinueMode.CONTINUE;
			}, form.mdi.getRoot().getUIPath());

			final JPanel	bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			
			if (okAndCancel.length == 0) {
				final JButton	okButton = new JButton(localizer.getValue(PureLibLocalizer.BUTTON_OK));
				final JButton	cancelButton = new JButton(localizer.getValue(PureLibLocalizer.BUTTON_CANCEL));
				
				okButton.setName(DEFAULT_OK_BUTTON_NAME);
				cancelButton.setName(DEFAULT_CANCEL_BUTTON_NAME);
				okButton.addActionListener(okListener);
				cancelButton.addActionListener(cancelListener);
				bottomPanel.add(okButton);
				bottomPanel.add(cancelButton);
				dlg.getContentPane().add(bottomPanel,BorderLayout.SOUTH);
			}
			else if (okAndCancel.length == 1) {
				makeActionListener(form,form.mdi,okAndCancel[0],cancelListener,true);
			}
			else {
				makeActionListener(form,form.mdi,okAndCancel[0],okListener,true);
				makeActionListener(form,form.mdi,okAndCancel[1],cancelListener,true);
			}
			dlg.getContentPane().add(form,BorderLayout.CENTER);
			dlg.pack();
			dlg.setLocationRelativeTo(parent);

			try{dlg.setVisible(true);
				return result[0];
			} finally {
				dlg.getContentPane().remove(form);
				if (okAndCancel.length == 1) {	// Exclude memory leaks by subscribing
					makeActionListener(form,form.mdi,okAndCancel[0],cancelListener,false);
				}
				else if (okAndCancel.length == 2) {
					makeActionListener(form,form.mdi,okAndCancel[0],okListener,false);
					makeActionListener(form,form.mdi,okAndCancel[1],cancelListener,false);
				}
				else {
					dlg.getContentPane().remove(bottomPanel);
				}
				SwingUtils.removeActionKey(form,WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,SwingUtils.KS_ACCEPT,"ask.accept");
				SwingUtils.removeActionKey(form,WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,SwingUtils.KS_EXIT,"ask.cancel");
			}
		}
	}

	private static void makeActionListener(final AutoBuiltForm<?> form, final ContentMetadataInterface mdi, final URI appPath, final ActionListener listener, final boolean add) {
		final boolean[]	result = new boolean[]{false};
		
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if (node.getApplicationPath() != null && appPath.equals(node.getApplicationPath())) {
					final JButton		button = (JButton) SwingUtils.findComponentByName(form,node.getUIPath().toString());

					if (add) {
						button.addActionListener(listener);
					}
					else {
						button.removeActionListener(listener);
					}
					result[0] = true;
					return ContinueMode.STOP;
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
		if (!result[0]) {
			throw new IllegalArgumentException("Application path ["+appPath+"] of the button to use as 'OK'/'Cancel' not found enywhere");  
		}
	}

	private static class ABFFocusTraversalPolicy extends FocusTraversalPolicy {
		private final JComponent[]	content;
		private final int			ordinalBound;
		
		ABFFocusTraversalPolicy(final List<JComponent> ordinal, final List<JComponent> output) {
			this.content = new JComponent[ordinal.size()+output.size()];
			this.ordinalBound = ordinal.size()-1;
			for (int index = 0, maxIndex = ordinal.size(); index < maxIndex; index++) {
				content[index] = ordinal.get(index);
			}
			for (int index = 0, displ = ordinal.size(), maxIndex = output.size(); index < maxIndex; index++) {
				content[index+displ] = output.get(index);
			}
		}
		
	    public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
	    	for (int index = 0, maxIndex = content.length; index < maxIndex; index++) {
	    		if (content[index] == aComponent) {
	    			if (index == ordinalBound || index == maxIndex-1) {
	    				break;
	    			}
	    			else {
	    				return content[index+1];
	    			}
	    		}
	    	}
	    	return getFirstComponent(focusCycleRoot);
	    }

	    public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
	    	for (int index = 0, maxIndex = content.length; index < maxIndex; index++) {
	    		if (content[index] == aComponent) {
	    			if (index == 0) {
	    				break;
	    			}
	    			else {
	    				return content[index-1];
	    			}
	    		}
	    	}
	    	return getLastComponent(focusCycleRoot);
	    }

	    public Component getDefaultComponent(Container focusCycleRoot) {
	        return content[0];
	    }

	    public Component getLastComponent(Container focusCycleRoot) {
	        return content[ordinalBound];
	    }

	    public Component getFirstComponent(Container focusCycleRoot) {
	        return content[0];
	    }
	}
}
