package chav1961.purelib.ui.swing;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleDescriptor;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.interfaces.WizardStep.StepType;

/**
 * <p>This class is a swing-oriented simple <i>wizard</i> to use with the {@linkplain WizardStep} implementations. The class extends JDialog
 * functionality and can be used in the similar way. The class implements {@linkplain AutoCloseable} interface and would be used in the
 * <b>try-with-resource</b> statements. The class is fully compatible with the WizardStep contract and supports WizardStep lifeCycle as described.
 * Advanced options for this class are:</p>
 * <ul>
 * <li>automatic localization support (see {@linkplain Localizer})</li> 
 * <li>ability to show Language selection drop-down in the wizard.</li> 
 * </ul>
 * <p>To prevent unpedictable changes in the life WizardStep's cycle, protected method {@linkplain #animate(int, Object, BlockingQueue)} in the class is marked as <b>final</b>.</p>
 * <p>This class is not thread-safe</p> 
 * @param <Common> data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
 * @param <ErrorType> type of the errors can be detected on the wizard execution stages
 * @author Alexander Chernomyrdin aka chav1961
 * @see WizardStep
 * @see chav1961.purelib.streams JUnit tests
 * @since 0.0.2
 * @last.update 0.0.5
 */
public class SimpleWizard<Common, ErrorType extends Enum<?>> extends JDialog implements AutoCloseable, ErrorProcessing<Common,ErrorType> {
	private static final long 							serialVersionUID = 750522918265155456L;
	
	public static final String							PROP_LOCALIZER = "localizer";			
	public static final String							PROP_SHOW_LOCALIZATION_BOX = "showLocalizationBox";			
	public static final String							PROP_PREFERRED_SIZE = "preferredSize";			
	public static final String							PREV_BUTTON_NAME = "SimpleWizard.prev";			
	public static final String							NEXT_BUTTON_NAME = "SimpleWizard.next";			
	public static final String							CANCEL_BUTTON_NAME = "SimpleWizard.cancel";			

	protected static final String						ACTION_PREV = "PREV";
	protected static final String						ACTION_NEXT = "NEXT";
	protected static final String						ACTION_CANCEL = "CANCEL";
	protected static final String						ACTION_FINISH = "FINISH";
	
	protected static final String						CAPTION_PREV = "wizardPrev";
	protected static final String						CAPTION_NEXT = "wizardNext";
	protected static final String						CAPTION_CANCEL = "wizardCancel";
	protected static final String						CAPTION_FINISH = "wizardFinish";
	protected static final String						CAPTION_WARNING = "wizardWarning";
	protected static final String						CAPTION_ERROR = "wizardError";
	
	protected static final String						DEFAULT_CAPTION_PREV = "< Back";
	protected static final String						DEFAULT_CAPTION_NEXT = "> Next";
	protected static final String						DEFAULT_CAPTION_CANCEL = "Cancel";
	protected static final String						DEFAULT_CAPTION_FINISH = "Finish";
	protected static final String						DEFAULT_CAPTION_WARNING = "Warning!";
	protected static final String						DEFAULT_CAPTION_ERROR = "ERROR!";
	
	protected static final int							INITIAL_WIDTH = 640;
	protected static final int							INITIAL_HEIGHT = 480;
	protected static final int							INNER_BORDER_GAP = 5;
	protected static final int							LEFT_LIST_BORDER_GAP = 10;
	
	protected static final Dimension					INITIAL_DIMENSION = new Dimension(INITIAL_WIDTH,INITIAL_HEIGHT);

	private final LoggerFacade							logger;
	private final Window								parent;
	private final String								caption; 
	private final boolean								showLocalizer;
	private final LocaleChangeListener					lcl = new LocaleChangeListener(){
															@Override
															public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
																setTitle(localizer.getValue(caption));
																refreshLocale(newLocale);
																refreshButtonsState(currentStep);
															}
														};
	private final JButton								prev = new JButton(), next = new JButton(), cancel = new JButton();
	private final DefaultListModel<WizardStep<?,?,?>>	hist = new DefaultListModel<>();
	private final JTextPane								stepCaption = new JTextPane();
	private final JTextPane								stepDescription = new JTextPane();
	
	protected final Localizer							localizer;
	protected final Locale								oldLocale;
	protected final Map<String,Object>					properties = new HashMap<>();
	protected final WizardStep<Common,ErrorType,JComponent>	steps[];
	protected final Localizer[]							localizers;
	protected final List<WizardStep<Common,ErrorType,JComponent>>	history = new ArrayList<>();

	private int											currentStep = 0;

	protected enum ActionButton {
		PREV, NEXT, CANCEL, FINISH
	}

	@SafeVarargs
	public SimpleWizard(final Window parent, final String caption, final ModalityType modality, final Map<String,Object> properties, final WizardStep<Common,ErrorType,JComponent>... steps) throws NullPointerException, IllegalArgumentException, LocalizationException {
		this(PureLibSettings.CURRENT_LOGGER,parent,caption,modality,properties,steps);
	}
	
	@SafeVarargs
	public SimpleWizard(final LoggerFacade logger, final Window parent, final String caption, final ModalityType modality, final Map<String,Object> properties, final WizardStep<Common,ErrorType,JComponent>... steps) throws NullPointerException, IllegalArgumentException, LocalizationException {
		super(parent,modality);
		if (caption == null || caption.isEmpty()) {
			throw new IllegalArgumentException("Caption string can;t be null or empty");
		}
		else if (modality == null) {
			throw new NullPointerException("Modality can't be null");
		}
		else if (properties == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else if (steps == null || steps.length == 0) {
			throw new IllegalArgumentException("Steps can't be null or zero-length list");
		}
		else {
			boolean		wasInitial = false, wasTermSuccess = false;
			
			for(int index = 0; index < steps.length; index++) {
				if (steps[index] == null) {
					throw new NullPointerException("Steps has null element at ["+index+"] index");
				}
				else {
					switch (steps[index].getStepType()) {
						case INITIAL 		: wasInitial = true; break;
						case TERM_SUCCESS	: wasTermSuccess = true; break;
						default				: break;
					}
				}
			}
			if (!wasInitial) {
				throw new IllegalArgumentException("No any steps with [INITIAL] type was detected!");
			}
			else if (!wasTermSuccess && steps.length > 1) {
				throw new IllegalArgumentException("No any steps with [TERM_SUCCESS] type was detected!");
			}
			
			this.prev.setName(PREV_BUTTON_NAME);
			this.next.setName(NEXT_BUTTON_NAME);
			this.cancel.setName(CANCEL_BUTTON_NAME);

			this.logger = logger;
			this.parent = parent;
			this.caption = caption;
			this.properties.putAll(properties);
			this.steps = steps;
			this.localizers = new Localizer[steps.length];
			
			if (this.properties.containsKey(PROP_LOCALIZER)) {
				if (this.properties.get(PROP_LOCALIZER) instanceof Localizer) {
					this.localizer = (Localizer) this.properties.get(PROP_LOCALIZER);
					this.oldLocale = localizer.currentLocale().getLocale(); 
				}
				else {
					throw new IllegalArgumentException("Properties list contains ["+PROP_LOCALIZER+"] with non-Localizer instance associated!");
				}
			}
			else {
				this.localizer = null;
				this.oldLocale = null;
			}
			if (this.properties.containsKey(PROP_SHOW_LOCALIZATION_BOX)) {
				if (this.properties.get(PROP_SHOW_LOCALIZATION_BOX) instanceof Boolean) {
					if (this.localizer != null) {
						this.showLocalizer = (Boolean) this.properties.get(PROP_SHOW_LOCALIZATION_BOX);
					}
					else {
						throw new IllegalArgumentException("Properties list contains ["+PROP_SHOW_LOCALIZATION_BOX+"] but ["+PROP_LOCALIZER+"] is missing!");
					}
				}
				else {
					throw new IllegalArgumentException("Properties list contains ["+PROP_SHOW_LOCALIZATION_BOX+"] with non-Boolean instance associated!");
				}
			}
			else {
				this.showLocalizer = false;
			}
			if (this.properties.containsKey(PROP_PREFERRED_SIZE)) {
				if (!(this.properties.get(PROP_PREFERRED_SIZE) instanceof Dimension)) {
					throw new IllegalArgumentException("Properties list contains ["+PROP_PREFERRED_SIZE+"] with non-DImension instance associated!");
				}
			}
			if (this.localizer != null) {
				setTitle(localizer.getValue(caption));
				localizer.addLocaleChangeListener(lcl);
			}
			else {
				setTitle(caption);
			}
		}
	}

	public boolean animate(final Common cargo) throws PreparationException, FlowException, InterruptedException, LocalizationException {
		for (int index = 0; index < steps.length; index++) {
			if (steps[index].getStepType() == StepType.INITIAL) {
				return animate(index,cargo, new ArrayBlockingQueue<>(1000));
			}
		}
		throw new IllegalStateException("Unwaited problem: steps has no any [INITIAL] step to use");
	}
	
	public boolean animate(final String startId, final Common cargo) throws IllegalArgumentException, PreparationException, FlowException, InterruptedException, LocalizationException {
		final int	stepIndex;
		
		if (startId == null || startId.isEmpty()) {
			throw new IllegalArgumentException("Start Id can't be null or empty");
		}
		else if ((stepIndex = getStepIndexById(startId)) < 0) {
			throw new IllegalArgumentException("Start Id ["+startId+"] is missing in the steps");
		}
		else {
			return animate(stepIndex,cargo,new ArrayBlockingQueue<>(1000));
		}
	}

	@Override
	public void processError(final Common content, final ErrorType err, final Object... parameters) throws FlowException, LocalizationException {
		if (err == null) {
			throw new NullPointerException("Error type can't be null");
		}
		else if (parameters == null) {
			throw new NullPointerException("Message parameters can't be null ref");
		}
		else {
			final String	format = getMessageByType(err);
			final String	message = parameters.length == 0 ? format : String.format(format,parameters);
			
			if (logger != null) {
				logger.message(Severity.warning,message);
			}
			else if (isVisible()) {
				JOptionPane.showMessageDialog(this,message,extractLocalizedValue(localizer,CAPTION_ERROR,DEFAULT_CAPTION_ERROR),JOptionPane.ERROR_MESSAGE|JOptionPane.OK_OPTION);
			}
			throw new FlowException(message);
		}
	}

	@Override
	public void processWarning(final Common content, final ErrorType err, final Object... parameters) throws LocalizationException {
		if (err == null) {
			throw new NullPointerException("Error type can't be null");
		}
		else if (parameters == null) {
			throw new NullPointerException("Message parameters can't be null ref");
		}
		else {
			final String	format = getMessageByType(err);
			final String	message = parameters.length == 0 ? format : String.format(format,parameters);
			
			if (logger != null) {
				logger.message(Severity.warning,message);
			}
			else if (isVisible()) {
				JOptionPane.showMessageDialog(this,message,extractLocalizedValue(localizer,CAPTION_WARNING,DEFAULT_CAPTION_WARNING),JOptionPane.WARNING_MESSAGE|JOptionPane.OK_OPTION);
			}
		}
	}
	
	@Override
	public void close() throws LocalizationException {
		if (localizer != null) {
			localizer.pop();
			localizer.removeLocaleChangeListener(lcl);
			localizer.setCurrentLocale(oldLocale);
		}
		dispose();
	}

	protected int getStepIndexById(final String stepId) {
		for (int index = 0; index < steps.length; index++) {
			if (stepId.equals(steps[index].getStepId())) {
				return index;
			}
		}
		return -1;
	}
	
	protected void refreshLocale(final Locale newLocale) throws LocalizationException {
		if (localizer != null) {
			for (int index = 0; index < steps.length; index++) {
				if (localizers[index] != null) {
					try{LocalizerFactory.fillLocalizedContent(localizers[index],steps[index],(localizer,inst,f,value)->{return postprocess(value);});
					} catch (IOException e) {
					}			
				}
			}
		}
	}

	protected String getMessageByType(final ErrorType type) throws LocalizationException, IllegalArgumentException {
		if (localizer != null && localizer.containsKey(type.toString())) {
			return postprocess(localizer.getValue(type.toString())); 
		}
		else {
			return postprocess(type.toString());
		}
	}
	
	protected String extractLocalizedValue(final Localizer localizer,final String item, final String defaultValue) throws LocalizationException {
		if (localizer == null || !localizer.containsKey(item)) {
			return postprocess(defaultValue);
		}
		else {
			return postprocess(localizer.getValue(item));
		}
	}

	protected void pushHistory(final WizardStep<Common,ErrorType,JComponent> step) {
		history.add(0,step);
		fillHistList();
	}

	protected void popHistory(final WizardStep<Common,ErrorType,JComponent> step) {
		while (history.size() > 0 && history.get(0) != step) {
			history.remove(0);
		}
		fillHistList();
	}

	protected void fillHistList() {
		hist.removeAllElements();
		for (int index = history.size() - 1 ; index >= 0; index--) {
			hist.addElement(history.get(index));
		}
	}
	
	protected void prepareLeftPanel(final JPanel panel) {
		final JList<WizardStep<?,?,?>>				histList = new JList<>(hist);
		final ListCellRenderer<WizardStep<?,?,?>>	renderer = (list,value,index,isSelected,cellHasFocus)-> {
														try{final String	content = extractLocalizedValue(localizer,value.getTabName(),value.getTabName());
															final JLabel 	result = new JLabel((index + 1)+": "+content);
														
															result.setToolTipText(extractLocalizedValue(localizer,value.getDescription(),value.getDescription()));
															return result;
														} catch (LocalizationException e) {
															return new JLabel((index + 1)+": "+value.getTabName());
														}
													}; 
		
		panel.setLayout(new BorderLayout(LEFT_LIST_BORDER_GAP,LEFT_LIST_BORDER_GAP));
		histList.setCellRenderer(renderer);
		histList.setBackground(this.getBackground());
		histList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		histList.setMinimumSize(new Dimension((int)(0.25*INITIAL_WIDTH),(int)(0.8*INITIAL_HEIGHT)));
		histList.setPreferredSize(new Dimension((int)(0.25*INITIAL_WIDTH),(int)(0.8*INITIAL_HEIGHT)));
		panel.add(histList,BorderLayout.CENTER);
	}

	protected String postprocess(final String value) {
		return value;
	}

	protected int getCurrentStep() {
		return currentStep;
	}
	
	protected final boolean animate(final int stepIndex, final Common cargo, final BlockingQueue<ActionButton> stepper) throws PreparationException, FlowException, InterruptedException, LocalizationException {
		return animateInternal(stepIndex,cargo,stepper);
	}
	
	private boolean animateInternal(final int stepIndex, final Common cargo, final BlockingQueue<ActionButton> stepper) throws PreparationException, FlowException, InterruptedException, LocalizationException {
		final JComponent	container = prepareContainer(stepper);
		final CardLayout	layout = (CardLayout) container.getLayout();
		final boolean[]		result = {false};

		for (int index = 0; index < steps.length; index++) {
			steps[index].prepare(cargo,properties);
			
			final JComponent	item = steps[index].getContent();
			
			if (item.getPreferredSize() != null) {
				final JPanel	panel = new JPanel();

				panel.add(item);
				container.add(panel, steps[index].getStepId());
			}
			else {
				container.add(item, steps[index].getStepId());
			}
			
			if (localizer != null && steps[index].getClass().isAnnotationPresent(LocaleResourceLocation.class)) {
				final Localizer		nestedLocalizer = LocalizerFactory.getLocalizer(URI.create(steps[index].getClass().getAnnotation(LocaleResourceLocation.class).value()));
				
				if ((localizers[index] = nestedLocalizer) != null && !localizer.isInParentChain(nestedLocalizer)) { 
					localizer.push(localizers[index]);
				}
				else {
					localizers[index] = null;
				}
			}
			else {
				localizers[index] = null;
			}
		} 
		if (localizer != null) {
			refreshLocale(localizer.currentLocale().getLocale());
		}
		
		if (getModalityType() != ModalityType.MODELESS) {	// Protection from modality, because setVisible is synchronous call in this case!
			final Thread	t = new Thread(()->{
								try{result[0] = processSteps(stepIndex, cargo, layout, container, stepper);
								} catch (LocalizationException | FlowException | InterruptedException e) {
									logger.message(Severity.error, e, e.getLocalizedMessage());
								} finally {
									setVisible(false);
									dispose();
								}
							});
			t.setDaemon(true);
			t.start();
			setVisible(true);
		}
		else {
			setVisible(true);
			try{result[0] = processSteps(stepIndex, cargo, layout, container, stepper);
			} catch (LocalizationException | FlowException | InterruptedException e) {
				logger.message(Severity.error, e, e.getLocalizedMessage());
			} finally {
				setVisible(false);
				dispose();
			}
		}
		
		return result[0];
	}

	private boolean processSteps(final int initialStep, final Common cargo, final CardLayout layout, final JComponent container, final BlockingQueue<ActionButton> stepper) throws LocalizationException, FlowException, InterruptedException {
		final SimpleAttributeSet	center = new SimpleAttributeSet();
		int							newStep;
		String						newStepName;
		ActionButton				ab;
		
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		
		try{currentStep = initialStep;
			pushHistory(steps[currentStep]);
			steps[currentStep].beforeShow(cargo,properties,this);	// Prevent beforeShow call when validation failed	
			for (;;) {
				final StyledDocument 		doc = stepCaption.getStyledDocument();
				
				stepCaption.setText(extractLocalizedValue(localizer,steps[currentStep].getCaption(),steps[currentStep].getCaption()));
				doc.setParagraphAttributes(0, doc.getLength(), center, false);				
				stepDescription.setText(extractLocalizedValue(localizer,steps[currentStep].getDescription(),steps[currentStep].getDescription()));
				
				layout.show(container,steps[currentStep].getStepId());
				refreshButtonsState(currentStep);
				if (steps[currentStep].getStepType() == StepType.PROCESSING) {
					final Thread	t = new Thread(new Runnable(){
										@Override
										public void run() {
											try{if (steps[currentStep].validate(cargo,properties,SimpleWizard.this)) {
													try{stepper.put(ActionButton.NEXT);
													} catch (InterruptedException e) {
													}
												}
												else {
													try{stepper.put(ActionButton.CANCEL);
													} catch (InterruptedException e) {
													}
												}
											} catch (Throwable t) {
												try{stepper.put(ActionButton.CANCEL);
												} catch (InterruptedException e) {
												}
											}
										}
									});
					
					t.setDaemon(true);
					t.start();
				}
				ab = stepper.take();
				switch(ab) {
					case PREV	:
						if ((newStepName = steps[currentStep].getPrevStep()) != null) {
							if ((newStep = getStepIndexById(newStepName)) < 0) {
								throw new FlowException("Step ["+steps[currentStep].getStepId()+"]: jump is referenced to non-existent step ["+newStep+"]");
							}
							popHistory(steps[newStep]);
						}
						else if (currentStep > 0) {
							newStep = currentStep - 1;
							popHistory(steps[newStep]);
						}
						else {
							throw new FlowException("Step ["+steps[currentStep].getStepId()+"]: default jump out of step range");
						}
						break;
					case NEXT	:
						if (steps[currentStep].getStepType() == StepType.PROCESSING || steps[currentStep].validate(cargo,properties,this)) {
							if ((newStepName = steps[currentStep].getNextStep()) != null) {
								if ((newStep = getStepIndexById(newStepName)) < 0) {
									throw new FlowException("Step ["+steps[currentStep].getStepId()+"]: jump is referenced to non-existent step ["+newStep+"]");
								}
								pushHistory(steps[newStep]);
							}
							else if (currentStep < steps.length - 1) {
								newStep = currentStep + 1;
								pushHistory(steps[newStep]);
							}
							else {
								throw new FlowException("Step ["+steps[currentStep].getStepId()+"]: default jump out of step range");
							}
						}
						else {
							continue;
						}
						break;
					case CANCEL	:
						steps[currentStep].afterShow(cargo,properties,this);
						return false;
					case FINISH	:
						if (steps[currentStep].validate(cargo,properties,this)) {
							steps[currentStep].afterShow(cargo,properties,this);
							return true;
						}
						else {
							continue;
						}
					default : throw new UnsupportedOperationException("Button action ["+ab+"] is not implemented yet");
				}
				steps[currentStep].afterShow(cargo,properties,this);
				currentStep = newStep;
				steps[currentStep].beforeShow(cargo,properties,this);	// Prevent beforeShow call when validation failed
			}
		} finally {
			setVisible(false);
			for (int index = 0; index < steps.length; index++) {
				steps[index].unprepare(cargo,properties);
			}
			if (localizer != null) {
				localizer.pop();
			}
		}
	}
	
	private JComponent prepareContainer(final BlockingQueue<ActionButton> stepper) throws LocalizationException {
		final JPanel		top = new JPanel();
		final JPanel		left = new JPanel();
		final JPanel		buttons = new JPanel();
		final JPanel		centerPanel = new JPanel(new BorderLayout());
		final CardLayout	card = new CardLayout();
		final JPanel		centerBottomPanel = new JPanel(card);
		final Dimension		wizardSize = (Dimension)properties.get(PROP_PREFERRED_SIZE);
		
		setMinimumSize(wizardSize != null ? wizardSize : INITIAL_DIMENSION);
		setPreferredSize(wizardSize != null ? wizardSize : INITIAL_DIMENSION);
		setLocationRelativeTo(parent);
		setIconImages(Arrays.asList(new ImageIcon(SimpleWizard.class.getResource("wizard.png")).getImage()));
		getContentPane().setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		addWindowListener(new WindowListener() {
			@Override public void windowOpened(WindowEvent e) {}
			@Override public void windowIconified(WindowEvent e) {}
			@Override public void windowDeiconified(WindowEvent e) {}
			@Override public void windowDeactivated(WindowEvent e) {}
			@Override public void windowClosing(WindowEvent e) {}
			@Override public void windowActivated(WindowEvent e) {}
			
			@Override 
			public void windowClosed(WindowEvent e) {
				try{stepper.put(ActionButton.CANCEL);
				} catch (InterruptedException e1) {
				}
			}
		});

		top.setLayout(new BorderLayout(INNER_BORDER_GAP,INNER_BORDER_GAP));
		stepCaption.setBackground(this.getBackground());
		stepCaption.setEditable(false);
		top.add(stepCaption,BorderLayout.CENTER);
		
		if (showLocalizer && localizer != null) {
			final DefaultComboBoxModel<Localizer.LocaleDescriptor>	cbm = new DefaultComboBoxModel<>(); 
			final JComboBox<Localizer.LocaleDescriptor>				combo = new JComboBox<>(cbm);
			final JPanel											comboKeeper = new JPanel();
			final ListCellRenderer<Localizer.LocaleDescriptor> 		renderer = new ListCellRenderer<Localizer.LocaleDescriptor>() {
																		@Override
																		public Component getListCellRendererComponent(JList<? extends LocaleDescriptor> list, LocaleDescriptor value, int index, boolean isSelected, boolean cellHasFocus) {
																			final JLabel	result = new JLabel(value.getIcon());
																			
																			result.setToolTipText(value.getLanguage());
																			result.setBackground(isSelected ? combo.getBackground() : Color.BLUE);
																			return result;
																		}
																	}; 
																	
			int maxHeight = 0;
			
			combo.setRenderer(renderer);
			for (LocaleDescriptor item : localizer.supportedLocales()) {
				cbm.addElement(item);
				if (item.getIcon().getIconHeight() > maxHeight) {
					maxHeight = item.getIcon().getIconHeight();
				}
			}
			combo.addItemListener((e) -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					try{localizer.setCurrentLocale(((LocaleDescriptor)e.getItem()).getLocale());
					} catch (LocalizationException exc) {
					}
				}
			});
			comboKeeper.add(combo);			
			top.add(comboKeeper,BorderLayout.EAST);
		}
		
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(prev);		
		prev.addActionListener((e) -> {
			try{stepper.put(ActionButton.valueOf(e.getActionCommand()));
			} catch (Exception exc) {
			}
		});
		buttons.add(next);		
		next.addActionListener((e) -> {
			try{stepper.put(ActionButton.valueOf(e.getActionCommand()));
			} catch (Exception exc) {
			}
		});
		buttons.add(cancel);	
		cancel.addActionListener((e) -> {
			try{stepper.put(ActionButton.valueOf(e.getActionCommand()));
			} catch (Exception exc) {
			}
		});
		refreshButtonsState(getCurrentStep());
	
		stepDescription.setBackground(this.getBackground());
		stepDescription.setEditable(false);
		centerPanel.add(stepDescription, BorderLayout.NORTH);
		centerPanel.add(centerBottomPanel, BorderLayout.CENTER);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(INNER_BORDER_GAP,INNER_BORDER_GAP,INNER_BORDER_GAP,INNER_BORDER_GAP));
		
		left.setBorder(BorderFactory.createEmptyBorder(INNER_BORDER_GAP,INNER_BORDER_GAP,INNER_BORDER_GAP,INNER_BORDER_GAP));
		prepareLeftPanel(left);
		
		getContentPane().add(left,BorderLayout.WEST);
		getContentPane().add(buttons,BorderLayout.SOUTH);
		getContentPane().add(centerPanel,BorderLayout.CENTER);
		getContentPane().add(top,BorderLayout.NORTH);

		return centerBottomPanel;
	}
	
	private void refreshButtonsState(final int currentStep) throws LocalizationException {
		prev.setText(extractLocalizedValue(localizer,CAPTION_PREV,DEFAULT_CAPTION_PREV));
		prev.setActionCommand(ACTION_PREV);
		cancel.setText(extractLocalizedValue(localizer,CAPTION_CANCEL,DEFAULT_CAPTION_CANCEL));
		cancel.setActionCommand(ACTION_CANCEL);
		switch (steps[currentStep].getStepType()) {
			case INITIAL		:
				prev.setEnabled(false);
				next.setEnabled(true);
				if (steps.length == 1) {
					next.setText(extractLocalizedValue(localizer,CAPTION_FINISH,DEFAULT_CAPTION_FINISH));
					next.setActionCommand(ACTION_FINISH);
				}
				else {
					next.setText(extractLocalizedValue(localizer,CAPTION_NEXT,DEFAULT_CAPTION_NEXT));
					next.setActionCommand(ACTION_NEXT);
				}
				break;
			case ORDINAL		:
				prev.setEnabled(true);
				next.setEnabled(true);
				next.setText(extractLocalizedValue(localizer,CAPTION_NEXT,DEFAULT_CAPTION_NEXT));
				next.setActionCommand(ACTION_NEXT);
				break;
			case TERM_SUCCESS	:
				prev.setEnabled(true);
				next.setEnabled(true);
				next.setText(extractLocalizedValue(localizer,CAPTION_FINISH,DEFAULT_CAPTION_FINISH));
				next.setActionCommand(ACTION_FINISH);
				break;
			case TERM_FAILURE	:
				prev.setEnabled(true);
				next.setEnabled(false);
				next.setText(extractLocalizedValue(localizer,CAPTION_FINISH,DEFAULT_CAPTION_FINISH));
				next.setActionCommand(ACTION_FINISH);
				break;
			case PROCESSING		:
				prev.setEnabled(false);
				next.setEnabled(false);
				next.setText(extractLocalizedValue(localizer,CAPTION_NEXT,DEFAULT_CAPTION_NEXT));
				next.setActionCommand(ACTION_NEXT);
				break;
			default : throw new UnsupportedOperationException("Step type ["+steps[currentStep].getStepType()+"] is not supported yet");
		}
	}
}
