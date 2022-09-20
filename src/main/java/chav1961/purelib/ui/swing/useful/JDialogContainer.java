package chav1961.purelib.ui.swing.useful;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.interfaces.WizardStep.StepType;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class implements dialog container for both single screen and wizard steps.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @param <Common> any instance contains common information, shared between all content items. Treat it as global store tor them
 * @param <ErrorType> type of errors on wizard steps
 * @param <Content> content for showing on wizard steps
 * @since 0.0.4
 * @lastUpdate 0.0.5
 */
public class JDialogContainer<Common, ErrorType extends Enum<?>, Content extends Component> extends JDialog implements LocaleChangeListener, LoggerFacadeOwner {
	private static final long 	serialVersionUID = 8956769935164098957L;
	private static final String	OK_TEXT = "OK";
	private static final String	CANCEL_TEXT = "CANCEL";
	private static final String	PREV_TEXT = "PREV";
	private static final String	NEXT_TEXT = "NEXT";
	private static final String	FINISH_TEXT = "FINISH";
	private static final String	OK_TEXT_TT = "OK.tooltip";
	private static final String	CANCEL_TEXT_TT = "CANCEL.tooltip";
	private static final String	PREV_TEXT_TT = "PREV.tooltip";
	private static final String	NEXT_TEXT_TT = "NEXT.tooltip";
	private static final String	FINISH_TEXT_TT = "FINISH.tooltip";
	
	private final boolean		isWizard;
	private final ModalityType	isModal;
	private final Localizer		localizer;
	private final Component		parent;
	private final JComponent	inner;
	private final JLabel		wizardDescription = new JLabel("", JLabel.CENTER);
	private final JLabel		wizardHelp = new JLabel("");
	private final Common		common;
	private final JStateString	state;
	private final JButton		okButton = new JButton();
	private final JButton		cancelButton = new JButton();
	private final JButton		prevButton = new JButton();
	private final JButton		nextButton = new JButton();
	private final Map<String,Object>	temporary = new HashMap<>();
	private final WizardStep<Common,ErrorType,Content>[]	steps;
	private final History		history;

	private ErrorProcessing<Common,ErrorType>			err;
	private Thread				processingThread = null;
	private String				captionId = null;
	protected String			initialStep, currentStep;
	protected Component			currentComponent;
	protected boolean			result;
	
	/**
	 * <p>Create class instance for single modal dialog form</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param captionId caption id for dialog. Can't be null or empty. Id must contain in the localizer passed
	 * @param inner content of dialog. Can't be null
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 */
	public JDialogContainer(final Localizer localizer, final JFrame parent, final String captionId, final JComponent inner) throws LocalizationException, NullPointerException, IllegalArgumentException {
		this(localizer, parent, captionId, inner, ModalityType.DOCUMENT_MODAL);
	}

	/**
	 * <p>Create class instance for single modal dialog form</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param captionId caption id for dialog. Can't be null or empty. Id must contain in the localizer passed
	 * @param inner content of dialog. Can't be null
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 */
	public JDialogContainer(final Localizer localizer, final JDialog parent, final String captionId, final JComponent inner) throws LocalizationException, NullPointerException, IllegalArgumentException {
		this(localizer, parent, captionId, inner, ModalityType.DOCUMENT_MODAL);
	}

	/**
	 * <p>Create class instance for single modal or modeless dialog form</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param captionId caption id for dialog. Can't be null or empty. Id must contain in the localizer passed
	 * @param inner content of dialog. Can't be null
	 * @param modal true for modal dialog, false otherwise
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 * @deprecated since 0.0.5. Use {@linkplain #JDialogContainer(Localizer, JDialog, String, JComponent, ModalityType)} instead
	 */
	public JDialogContainer(final Localizer localizer, final JFrame parent, final String captionId, final JComponent inner, final boolean modal) throws LocalizationException, NullPointerException, IllegalArgumentException {
		this(localizer, parent, captionId, inner, modal ? ModalityType.DOCUMENT_MODAL : ModalityType.MODELESS);
	}
	
	/**
	 * <p>Create class instance for single modal or modeless dialog form</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param captionId caption id for dialog. Can't be null or empty. Id must contain in the localizer passed
	 * @param inner content of dialog. Can't be null
	 * @param modal true for modal dialog, false otherwise
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 * @deprecated since 0.0.5. Use {@linkplain #JDialogContainer(Localizer, JDialog, String, JComponent, ModalityType)} instead
	 */
	public JDialogContainer(final Localizer localizer, final JFrame parent, final String captionId, final JComponent inner, final ModalityType modal) throws LocalizationException, NullPointerException, IllegalArgumentException {
		super(parent,modal);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (captionId == null || captionId.isEmpty()) {
			throw new IllegalArgumentException("Caption id can't be null or empty");
		}
		else if (inner == null) {
			throw new NullPointerException("Inner component can't be null");
		}
		else {
			this.parent = parent;
			this.inner = inner;
			this.isWizard = false;
			this.isModal = modal;
			this.localizer = localizer;
			this.common = null;
			this.err = null;
			this.steps = null;
			this.captionId = captionId;
			this.state = new JStateString(localizer);
			this.history = new History(localizer,1);
			
			prepareSimpleDialog(inner);
			fillLocalizedStrings();
		}
	}

	/**
	 * <p>Create class instance for single modal or modeless dialog form</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param captionId caption id for dialog. Can't be null or empty. Id must contain in the localizer passed
	 * @param inner content of dialog. Can't be null
	 * @param modal modality of the dialog. Can't be null
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 * @since 0.0.5
	 */
	public JDialogContainer(final Localizer localizer, final JDialog parent, final String captionId, final JComponent inner, final boolean modal) throws LocalizationException, NullPointerException, IllegalArgumentException {
		this(localizer, parent, captionId, inner, modal ? ModalityType.DOCUMENT_MODAL : ModalityType.MODELESS);
	}
	
	/**
	 * <p>Create class instance for single modal or modeless dialog form</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param captionId caption id for dialog. Can't be null or empty. Id must contain in the localizer passed
	 * @param inner content of dialog. Can't be null
	 * @param modal modality of the dialog. Can't be null
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 * @since 0.0.5
	 */
	public JDialogContainer(final Localizer localizer, final JDialog parent, final String captionId, final JComponent inner, final ModalityType modal) throws LocalizationException, NullPointerException, IllegalArgumentException {
		super(parent,modal);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (captionId == null || captionId.isEmpty()) {
			throw new IllegalArgumentException("Caption id can't be null or empty");
		}
		else if (inner == null) {
			throw new NullPointerException("Inner component can't be null");
		}
		else {
			this.parent = parent;
			this.inner = inner;
			this.isWizard = false;
			this.isModal = modal;
			this.localizer = localizer;
			this.common = null;
			this.err = null;
			this.steps = null;
			this.captionId = captionId;
			this.state = new JStateString(localizer);
			this.history = new History(localizer,1);
			
			prepareSimpleDialog(inner);
			fillLocalizedStrings();
		}
	}
	
	/**
	 * <p>Create class instance for modal wizard</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param instance shared instance for steps. Can't be null
	 * @param err error processing from wizards. Can't be null. Can use lambdas
	 * @param steps list of wizard steps. Can't be null or empty
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 */
	public JDialogContainer(final Localizer localizer, final JFrame parent, final Common instance, final ErrorProcessing<Common, ErrorType> err, @SuppressWarnings("unchecked") final WizardStep<Common,ErrorType,Content>... steps) throws LocalizationException, NullPointerException, IllegalArgumentException {
		super(parent,true);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Common instance can't be null");
		}
		else if (err == null) {
			throw new NullPointerException("Error processor can't be null");
		}
		else if (steps == null || steps.length == 0) {
			throw new NullPointerException("Wizard stepe can't be null or empty array");
		}
		else {
			this.parent = parent;
			this.inner = new JPanel(new BorderLayout());
			this.isWizard = true;
			this.isModal = ModalityType.DOCUMENT_MODAL;
			this.localizer = localizer;
			this.common = instance;
			this.err = err;
			this.steps = steps;
			this.state = new JStateString(localizer);
			this.history = new History(localizer,steps.length);
			
			try{prepareWizardDialog(steps);
			} catch (FlowException e) {
				throw new IllegalArgumentException(e); 
			}
		}
	}

	/**
	 * <p>Create class instance for modal wizard</p>
	 * @param localizer localizer to use. Can't be null
	 * @param parent owner of the dialog. Can be null
	 * @param instance shared instance for steps. Can't be null
	 * @param err error processing from wizards. Can't be null. Can use lambdas
	 * @param steps list of wizard steps. Can't be null or empty
	 * @throws LocalizationException on any localization errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on invalid arguments
	 */
	public JDialogContainer(final Localizer localizer, final JDialog parent, final Common instance, final ErrorProcessing<Common, ErrorType> err, @SuppressWarnings("unchecked") final WizardStep<Common,ErrorType,Content>... steps) throws LocalizationException, NullPointerException, IllegalArgumentException {
		super(parent,true);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Common instance can't be null");
		}
		else if (err == null) {
			throw new NullPointerException("Error processor can't be null");
		}
		else if (steps == null || steps.length == 0) {
			throw new NullPointerException("Wizard stepe can't be null or empty array");
		}
		else {
			this.parent = parent;
			this.inner = new JPanel(new BorderLayout());
			this.isWizard = true;
			this.isModal = ModalityType.DOCUMENT_MODAL;
			this.localizer = localizer;
			this.common = instance;
			this.err = err;
			this.steps = steps;
			this.state = new JStateString(localizer);
			this.history = new History(localizer,steps.length);
			
			try{prepareWizardDialog(steps);
			} catch (FlowException e) {
				throw new IllegalArgumentException(e); 
			}
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	/**
	 * <p>Set options for container. Default implementation is empty</p>
	 * @param props options to set. Can't be null
	 * @return self
	 */
	public JDialogContainer<Common,ErrorType,Content> setOptions(final SubstitutableProperties props) {
		return this;
	}

	/**
	 * <p>Set options for container. Default implementation is empty</p>
	 * @param props options to set. Can't be null
	 * @return self
	 */
	public JDialogContainer<Common,ErrorType,Content> setOptions(final Map<String,Object> props) {
		return this;
	}
	
	/**
	 * <p>Show modal dialog or wizard and return true on 'OK' and false on 'Cancel'</p>
	 * @return true on 'OK', false otherwise
	 * @throws LocalizationException on any localization error
	 * @throws IllegalStateException when call on modeless dialog
	 */
	public boolean showDialog() throws LocalizationException, IllegalStateException {
		if (isModal == ModalityType.MODELESS) {
			throw new IllegalStateException("showDialog call is applicable to modal dialog only");
		}
		else if (isWizard) {
			for (WizardStep<Common, ErrorType, Content> item : steps) {
				item.prepare(common,temporary);
			}
			this.result = false;
			pack();
			setVisible(true);
			dispose();
			for (WizardStep<Common, ErrorType, Content> item : steps) {
				item.unprepare(common,temporary);
			}
			return this.result;
		}
		else {
			final Dimension	innerSize = inner.getPreferredSize();

			if (innerSize == null) {
				SwingUtils.centerMainWindow(this,0.5f);
			}
			else {
				setSize(new Dimension(innerSize.width + 10, innerSize.height + 50));
				setLocationRelativeTo(parent);
			}
			
			this.result = false;
			
			pack();
			setVisible(true);
			dispose();
			return this.result;
		}
	}
	
	@Override
	public void setVisible(final boolean visibility) {
		if (visibility) {
			localizer.addLocaleChangeListener(this);
			temporary.clear();
			if (isWizard) {
				try{prepareCurrentComponent(currentStep);
				} catch (FlowException exc) {
					getLogger().message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			}
			super.setVisible(true);
		}
		else {
			if (isWizard) {
				try{steps[stepIndexById(currentStep)].afterShow(common,temporary,err);
				} catch (FlowException exc) {
					getLogger().message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			}
			super.setVisible(false);
			temporary.clear();
			localizer.removeLocaleChangeListener(this);
		}
	}

	@Override
	public LoggerFacade getLogger() {
		return state;
	}
	
	protected void ok() {
		result = true;
		setVisible(false);
	}

	protected void cancel() {
		if (processingThread != null) {
			processingThread.interrupt();
			processingThread = null;
		}
		result = false;
		setVisible(false);
	}

	protected void prev() throws FlowException {
		String	prev = steps[stepIndexById(currentStep)].getPrevStep();
		
		if (prev == null) {
			prev = steps[stepIndexById(currentStep) - 1].getStepId(); 
		}
		placeCurrentComponent(currentStep, prev);
		history.pop();
		refreshButtons();
	}

	protected void next() throws FlowException {
		if (steps[stepIndexById(currentStep)].validate(common, temporary, err)) {
			if (steps[stepIndexById(currentStep)].getStepType().isFinal()) {
				if (steps[stepIndexById(currentStep)].getStepType() != StepType.TERM_FAILURE) {
					ok();
				}
			}
			else {
				String	next = steps[stepIndexById(currentStep)].getNextStep();
				
				if (next == null) {
					next = steps[stepIndexById(currentStep)+1].getStepId(); 
				}
				placeCurrentComponent(currentStep,next);
				history.push(steps[stepIndexById(currentStep)].getStepType(),steps[stepIndexById(currentStep)].getCaption());
				refreshButtons();
			}
		}
	}
	
	protected void refreshButtons() {
		if (isWizard) {
			prevButton.setEnabled(!steps[stepIndexById(currentStep)].getStepType().isInitial());
			if (steps[stepIndexById(currentStep)].getStepType().isFinal()) {
				nextButton.setText(localizer.getValue(FINISH_TEXT));
				nextButton.setToolTipText(localizer.getValue(FINISH_TEXT_TT));
			}
			else {
				nextButton.setText(localizer.getValue(NEXT_TEXT));
				nextButton.setToolTipText(localizer.getValue(NEXT_TEXT_TT));
			}
			nextButton.setEnabled(steps[stepIndexById(currentStep)].getStepType() != StepType.TERM_FAILURE);
		}
	}

	private void fillLocalizedStrings() throws LocalizationException {
		setTitle(localizer.getValue(captionId));
		prevButton.setText(localizer.getValue(PREV_TEXT));
		prevButton.setToolTipText(localizer.getValue(PREV_TEXT_TT));
		okButton.setText(localizer.getValue(OK_TEXT));
		okButton.setToolTipText(localizer.getValue(OK_TEXT_TT));
		cancelButton.setText(localizer.getValue(CANCEL_TEXT));
		cancelButton.setToolTipText(localizer.getValue(CANCEL_TEXT_TT));
	}

	private void prepareSimpleDialog(final JComponent inner) {
		try(final LoggerFacade	trans = PureLibSettings.CURRENT_LOGGER.transaction(this.getClass().getSimpleName())) {
			final JPanel		bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
			final JPanel		south = new JPanel(new GridLayout(2,1,2,2));
			
			bottom.add(okButton);
			okButton.addActionListener((e)->{ok();});
			bottom.add(cancelButton);
			cancelButton.addActionListener((e)->{cancel();});
			state.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			south.add(bottom);
			south.add(state);
			getContentPane().add(inner,BorderLayout.CENTER);
			getContentPane().add(south,BorderLayout.SOUTH);
			assignKeys();
			trans.rollback();
		}
	}

	private void prepareWizardDialog(WizardStep<Common, ErrorType, Content>[] steps) throws LocalizationException, FlowException {
		try(final LoggerFacade	trans = PureLibSettings.CURRENT_LOGGER.transaction(this.getClass().getSimpleName())) {
			final JPanel		bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
			final JPanel		south = new JPanel(new GridLayout(2,1,2,2));
			
			bottom.add(prevButton);
			prevButton.addActionListener((e)->{
				try{prev();
				} catch (FlowException exc) {
					getLogger().message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			});
			bottom.add(nextButton);
			nextButton.addActionListener((e)->{
				try{next();
				} catch (FlowException exc) {
					getLogger().message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			});
			bottom.add(cancelButton);
			cancelButton.addActionListener((e)->{cancel();});
			state.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			south.add(bottom);
			south.add(state);

			if (steps.length == 1) {
				if (steps[0].getStepType() == StepType.THE_ONLY) {
					initialStep = currentStep = steps[0].getStepId();
				}
				else {
					throw new IllegalArgumentException("Only ["+StepType.THE_ONLY+"] step type can be used when step list length == 1"); 
				}
			}
			else {
				
				final Set<String>	stepIds = new HashSet<>();
				boolean		hasInitial = false, hasTerminal = false;
				
				for (WizardStep<Common, ErrorType, Content> item : steps) {
					if (item.getStepType() == StepType.INITIAL) {
						hasInitial = true;
						initialStep = currentStep = item.getStepId();
					}
					else if (item.getStepType() == StepType.TERM_SUCCESS || item.getStepType() == StepType.TERM_FAILURE) {
						hasTerminal = true;
					}
					if (item.getStepId() == null || item.getStepId().isEmpty()) {
						throw new IllegalArgumentException("Null or empty step id in the wizard step list"); 
					}
					else if (stepIds.contains(item.getStepId())) {
						throw new IllegalArgumentException("Duplicate step id ["+item.getStepId()+"] in the wizard step list"); 
					}
					else {
						stepIds.add(item.getStepId());
					}
				}
				if (!hasInitial) {
					throw new IllegalArgumentException("Wizard steps don't contain initial step"); 
				}
				else if (!hasTerminal) {
					throw new IllegalArgumentException("Wizard steps don't contain any terminal steps"); 
				}
			}
			
			getContentPane().add(wizardDescription, BorderLayout.NORTH);
			getContentPane().add(inner, BorderLayout.CENTER);
			inner.add(wizardHelp, BorderLayout.NORTH);
			
			prepareCurrentComponent(initialStep);
			refreshButtons();
			
			SwingUtils.assignActionKey(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_HELP, (e)->{
				final String	help = steps[stepIndexById(currentStep)].getHelpId();
				
				if (help != null && !help.isEmpty()) {
					final URI	uri = URI.create(help);
					
					if (uri.isAbsolute() && Desktop.isDesktopSupported()) {
						try{Desktop.getDesktop().browse(uri);
						} catch (IOException exc) {
							state.message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
						}
					}
					else {
						try{SwingUtils.showCreoleHelpWindow(JDialogContainer.this,uri);
						} catch (IOException exc) {
							state.message(Severity.error,exc,"Help window error: "+exc.getLocalizedMessage());
						}
					}
				}
			}, SwingUtils.ACTION_HELP);
			SwingUtils.centerMainWindow(this,0.5f);
			
			final JPanel		historyPanel = new JPanel(new BorderLayout());
			
			history.push(stepById(initialStep).getStepType(),stepById(initialStep).getCaption());
			historyPanel.setPreferredSize(new Dimension(200,0));
			historyPanel.setBorder(new EmptyBorder(20, 10, 10, 10));
			historyPanel.add(history, BorderLayout.CENTER);
			
			getContentPane().add(historyPanel,BorderLayout.WEST);
			getContentPane().add(south,BorderLayout.SOUTH);
			assignKeys();
			trans.rollback();
		}
	}

	private void assignKeys() {
		SwingUtils.assignActionKey(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_ACCEPT,(e) ->{
			if (isWizard) {
				try{next();
				} catch (FlowException exc) {
					state.message(Severity.error,exc,"Next jump error: "+exc.getLocalizedMessage());
				}
			}
			else {
				ok();
			}
		}, SwingUtils.ACTION_ACCEPT);
		SwingUtils.assignActionKey(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_EXIT,(e)->{
			cancel();
		}, SwingUtils.ACTION_EXIT);
	}
	
	private void placeCurrentComponent(final String currentState, final String newState) throws LocalizationException, FlowException {
		steps[stepIndexById(currentState)].afterShow(common,temporary,err);
		removeCurrentComponent();
		currentStep = newState;
		if (steps[stepIndexById(newState)].getStepType() == StepType.PROCESSING) {
			processingThread = new Thread(()->{
								try{steps[stepIndexById(newState)].beforeShow(common,temporary, err);
									steps[stepIndexById(newState)].afterShow(common,temporary, err);
								} catch (FlowException e) {
								} finally {
									SwingUtilities.invokeLater(()->{
										try{next();
										} catch (FlowException e) {
										}
									});
								}
							});
			processingThread.setDaemon(true);
			prevButton.setEnabled(false);
			nextButton.setEnabled(false);
			prepareCurrentComponent(currentStep);
			processingThread.start();
		}
		else {
			prepareCurrentComponent(currentStep);
		}
	}

	private void removeCurrentComponent() throws FlowException {
		inner.remove(currentComponent);
	}
	
	private void prepareCurrentComponent(final String step) throws FlowException {
		final int	stepNo = stepIndexById(step); 
		
		captionId = steps[stepNo].getCaption();
		currentComponent = steps[stepNo].getContent();
		
		fillLocalizedStrings();
		
		wizardDescription.setText(localizer.getValue(steps[stepNo].getDescription()));
		wizardHelp.setText(localizer.getValue(steps[stepNo].getHelpId()));
		inner.add(currentComponent, BorderLayout.CENTER);
		inner.revalidate();
		inner.repaint();
		
		steps[stepNo].beforeShow(common,temporary,err);
	}
	
	private WizardStep<Common, ErrorType, Content> stepById(final String stepId) {
		final int	stepIndex = stepIndexById(stepId);
		
		if (stepIndex == -1) {
			throw new IllegalArgumentException("Wizard step id ["+stepId+"] not found");
		}
		else {
			return steps[stepIndex];
		}
	}

	private int stepIndexById(final String stepId) {
		for (int index = 0; index < steps.length; index++) {
			if (stepId.equals(steps[index].getStepId())) {
				return index;
			}
		}
		return -1;
	}
	
	private static class History extends JEditorPane implements LocaleChangeListener {
		private static final long 	serialVersionUID = 1L;
		
		private final List<String>	steps = new ArrayList<>();
		private final Localizer		localizer;
		
		History(final Localizer localizer, final int maximumSize) {
			super("text/html","");
			this.localizer = localizer;
			setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			setEditable(false);
		}
		
		public void push(final StepType type, final String step) {
			steps.add(step);
			refreshContent();
		}

		public void pop() throws LocalizationException {
			steps.remove(steps.size()-1);
			refreshContent();
		}
		
		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			refreshContent();
		}

		private void refreshContent() throws IllegalArgumentException {
			final StringBuilder	sb = new StringBuilder();
			
			sb.append("<html><body>");
			for (String step : steps) {
				sb.append(localizer.getValue(step)).append("<br>");
			}
			sb.append("</body></html>");
			setText(sb.toString());
		}
	}
}
