package chav1961.purelib.ui.swing.useful;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.interfaces.WizardStep.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep.StepType;
import chav1961.purelib.ui.swing.SwingUtils;

public class JDialogContainer<Common,ErrorType extends Enum<?>, Content> extends JDialog implements LocaleChangeListener {
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
	
	private final boolean		isWizard, isModal;
	private final Localizer		localizer;
	private final Common		common;
	private final JPanel		center = new JPanel(new BorderLayout());
	private final JStateString	state;
	private final JButton		okButton = new JButton();
	private final JButton		cancelButton = new JButton();
	private final JButton		prevButton = new JButton();
	private final JButton		nextButton = new JButton();
	private final Map<String,Object>	temporary = new HashMap<>();
	private final WizardStep<Common,ErrorType,Content>[]	steps; 

	private ErrorProcessing<Common,ErrorType>			err;
	private Thread				processingThread = null;
	private String				captionId, helpId;
	protected String			initialStep, currentStep;
	protected Component			currentComponent;
	protected boolean			result;
	
	public JDialogContainer(final Localizer localizer, final JFrame parent, final String captionId, final JComponent inner) {
		this(localizer, parent, captionId, inner, true);
	}

	public JDialogContainer(final Localizer localizer, final JDialog parent, final String captionId, final JComponent inner) {
		this(localizer, parent, captionId, inner, true);
	}
	
	public JDialogContainer(final Localizer localizer, final JFrame parent, final String captionId, final JComponent inner, final boolean modal) {
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
			this.isWizard = false;
			this.isModal = modal;
			this.localizer = localizer;
			this.common = null;
			this.err = null;
			this.steps = null;
			this.captionId = captionId;
			this.state = new JStateString(localizer);

			prepareSimpleDialog(inner);
		}
	}

	public JDialogContainer(final Localizer localizer, final JDialog parent, final String captionId, final JComponent inner, final boolean modal) {
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
			this.isWizard = false;
			this.isModal = modal;
			this.localizer = localizer;
			this.common = null;
			this.err = null;
			this.steps = null;
			this.captionId = captionId;
			this.state = new JStateString(localizer);
			
			prepareSimpleDialog(inner);
		}
	}
	
	public JDialogContainer(final Localizer localizer, final JFrame parent, final Common instance, final ErrorProcessing<Common, ErrorType> err, @SuppressWarnings("unchecked") final WizardStep<Common,ErrorType,Content>... steps) {
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
			this.isWizard = true;
			this.isModal = true;
			this.localizer = localizer;
			this.common = instance;
			this.err = err;
			this.steps = steps;
			this.state = new JStateString(localizer);
			
			prepareWizardDialog(steps);
		}
	}

	public JDialogContainer(final Localizer localizer, final JDialog parent, final Common instance, final ErrorProcessing<Common, ErrorType> err, @SuppressWarnings("unchecked") final WizardStep<Common,ErrorType,Content>... steps) {
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
			this.isWizard = true;
			this.isModal = true;
			this.localizer = localizer;
			this.common = instance;
			this.err = err;
			this.steps = steps;
			this.state = new JStateString(localizer);
			
			prepareWizardDialog(steps);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	public JDialogContainer<Common,ErrorType,Content> setOptions(final SubstitutableProperties props) {
		return this;
	}

	public JDialogContainer<Common,ErrorType,Content> setOptions(final Map<String,Object> props) {
		return this;
	}
	
	public boolean showDialog() throws LocalizationException {
		if (!isModal) {
			throw new IllegalStateException("showDialog call is applicable to modal dialog only");
		}
		else if (isWizard) {
			for (WizardStep<Common, ErrorType, Content> item : steps) {
				item.prepare(common,temporary);
			}
			this.result = false;
			setVisible(true);
			dispose();
			for (WizardStep<Common, ErrorType, Content> item : steps) {
				item.unprepare(common,temporary);
			}
			return this.result;
		}
		else {
			this.result = false;
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
				try{prepareCurrentComponent();
				} catch (LocalizationException | FlowException exc) {
					state.message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			}
			super.setVisible(true);
		}
		else {
			if (isWizard) {
				try{steps[stepIndexById(currentStep)].afterShow(common,temporary,err);
				} catch (LocalizationException | FlowException exc) {
					state.message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			}
			super.setVisible(false);
			temporary.clear();
			localizer.removeLocaleChangeListener(this);
		}
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

	protected void prev() throws LocalizationException, FlowException {
		if (steps[stepIndexById(currentStep)].validate(common, temporary, err)) {
			String	prev = steps[stepIndexById(currentStep)].getPrevStep();
			
			if (prev == null) {
				prev = steps[stepIndexById(currentStep)-1].getStepId(); 
			}
			placeCurrentComponent(currentStep,prev);
		}
	}

	protected void next() throws LocalizationException, FlowException {
		if (steps[stepIndexById(currentStep)].validate(common, temporary, err)) {
			if (steps[stepIndexById(currentStep)].getStepType() == StepType.TERM_FAILURE || steps[stepIndexById(currentStep)].getStepType() == StepType.TERM_SUCCESS) {
				if (steps[stepIndexById(currentStep)].getStepType() == StepType.TERM_SUCCESS) {
					ok();
				}
			}
			else {
				String	next = steps[stepIndexById(currentStep)].getNextStep();
				
				if (next == null) {
					next = steps[stepIndexById(currentStep)-1].getStepId(); 
				}
				placeCurrentComponent(currentStep,next);
			}
		}
	}
	
	protected void refreshButtons() {
		if (isWizard) {
			prevButton.setEnabled(steps[stepIndexById(currentStep)].getStepType() != StepType.INITIAL);
			if (steps[stepIndexById(currentStep)].getStepType() == StepType.TERM_FAILURE || steps[stepIndexById(currentStep)].getStepType() == StepType.TERM_FAILURE) {
				try{nextButton.setText(localizer.getValue(FINISH_TEXT));
					nextButton.setToolTipText(localizer.getValue(FINISH_TEXT_TT));
				} catch (LocalizationException | IllegalArgumentException exc) {
					state.message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			}
			else {
				try{nextButton.setText(localizer.getValue(NEXT_TEXT));
					nextButton.setToolTipText(localizer.getValue(NEXT_TEXT_TT));
				} catch (LocalizationException | IllegalArgumentException exc) {
					state.message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
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
		try(final LoggerFacade	lf = new SystemErrLoggerFacade();
			final LoggerFacade	trans = lf.transaction(this.getClass().getSimpleName())) {
			final JPanel		bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
			
			bottom.add(okButton);
			okButton.addActionListener((e)->{ok();});
			bottom.add(cancelButton);
			cancelButton.addActionListener((e)->{cancel();});
			center.add(inner,BorderLayout.CENTER);
			center.add(bottom,BorderLayout.SOUTH);
			
			getContentPane().add(center,BorderLayout.CENTER);
			getContentPane().add(state,BorderLayout.SOUTH);
			SwingUtils.centerMainWindow(this);
			trans.rollback();
		}
	}

	private void prepareWizardDialog(WizardStep<Common, ErrorType, Content>[] steps) {
		try(final LoggerFacade	lf = new SystemErrLoggerFacade();
			final LoggerFacade	trans = lf.transaction(this.getClass().getSimpleName())) {
			final JPanel		bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
			
			bottom.add(prevButton);
			prevButton.addActionListener((e)->{
				try{prev();
				} catch (LocalizationException | FlowException exc) {
					state.message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			});
			bottom.add(nextButton);
			nextButton.addActionListener((e)->{
				try{next();
				} catch (LocalizationException | FlowException exc) {
					state.message(Severity.error,exc,"Browser start error: "+exc.getLocalizedMessage());
				}
			});
			bottom.add(cancelButton);
			cancelButton.addActionListener((e)->{cancel();});
			center.add(bottom,BorderLayout.SOUTH);

			boolean		hasInitial = false, hasTerminal = false;
			
			for (WizardStep<Common, ErrorType, Content> item : steps) {
				if (item.getStepType() == StepType.INITIAL) {
					hasInitial = true;
					initialStep = currentStep = item.getStepId();
				}
				else if (item.getStepType() == StepType.TERM_SUCCESS || item.getStepType() == StepType.TERM_FAILURE) {
					hasTerminal = true;
				}
			}
			if (!hasInitial) {
				throw new IllegalArgumentException("Wizard steps don't contain initial step"); 
			}
			else if (!hasTerminal) {
				throw new IllegalArgumentException("Wizard steps don't contain any terminal steps"); 
			}
			final Object	centerId = stepById(initialStep).getContent();
			
			if (!(centerId instanceof Component)) {
				throw new IllegalArgumentException("Wizard step content for step id ["+initialStep+"] is not a Swing component"); 
			}
			else {
				center.add(currentComponent = (Component)centerId,BorderLayout.CENTER);
				refreshButtons();
			}
			((JPanel)getContentPane()).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0),"help");
			((JPanel)getContentPane()).getActionMap().put("help",new AbstractAction(){
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					final URI	uri = URI.create(helpId);
					
					if (Desktop.isDesktopSupported()) {
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
			});
			
			getContentPane().add(center,BorderLayout.CENTER);
			getContentPane().add(state,BorderLayout.SOUTH);
			SwingUtils.centerMainWindow(this);
			trans.rollback();
		}
	}

	private void placeCurrentComponent(final String currentState, final String newState) throws LocalizationException, FlowException {
		steps[stepIndexById(currentState)].afterShow(common,temporary,err);
		getContentPane().remove(currentComponent);
		currentStep = newState;
		if (steps[stepIndexById(newState)].getStepType() == StepType.PROCESSING) {
			processingThread = new Thread(()->{
								try{steps[stepIndexById(newState)].beforeShow(common,temporary, err);
									steps[stepIndexById(newState)].afterShow(common,temporary, err);
								} catch (LocalizationException | FlowException e) {
								} finally {
									SwingUtilities.invokeLater(()->{
										try{next();
										} catch (LocalizationException | FlowException e) {
										}
									});
								}
							});
			processingThread.setDaemon(true);
			prevButton.setEnabled(false);
			nextButton.setEnabled(false);
			prepareCurrentComponent();
			processingThread.start();
		}
		else {
			prepareCurrentComponent();
		}
	}

	private void prepareCurrentComponent() throws LocalizationException, FlowException {
		captionId = steps[stepIndexById(currentStep)].getCaption();
		fillLocalizedStrings();
		steps[stepIndexById(currentStep)].beforeShow(common,temporary,err);
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
}
