package chav1961.purelib.ui;

import java.util.Map;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;


/**
 * <p>This class is a template for the wizard steps and implements it's typical functionality.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @see WizardStep 
 * @since 0.0.2
 * @lastUpdate 0.0.6
 */

public abstract class AbstractWizardStep<Common,ErrorType extends Enum<?>, Content extends JComponent> implements WizardStep<Common,ErrorType,Content> {
	@Override public abstract StepType getStepType();
	@Override public abstract String getCaption();
	@Override public abstract Content getContent();
	@Override public abstract void beforeShow(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, ErrorType> err) throws FlowException;
	@Override public abstract void afterShow(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, ErrorType> err) throws FlowException;
	
	@Override
	public String getStepId() {
		return this.getClass().getName();
	}

	@Override
	public String getNextStep() {
		return null;
	}

	@Override
	public String getPrevStep() {
		return null;
	}

	@Override
	public String getTabName() {
		return getStepId();
	}

	@Override
	public String getDescription() {
		return getCaption();
	}

	@Override
	public String getHelpId() {
		return getStepId();
	}

	@Override
	public void prepare(final Common content, final Map<String, Object> temporary) throws PreparationException {
	}
	
	@Override
	public boolean validate(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, ErrorType> err) throws FlowException {
		return true;
	}

	@Override
	public void unprepare(final Common content, final Map<String, Object> temporary) {
	}
}
