package chav1961.purelib.ui.swing.useful;

import java.util.Map;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

public class JModelBasedWizardStep<Common, Err extends Enum<?>> extends JComponent implements WizardStep<Common, Err, JComponent>, NodeMetadataOwner {
	private static final long serialVersionUID = 1965509994262064521L;

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public String getStepId() {
		return getNodeMetadata().getName();
	}

	@Override
	public StepType getStepType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCaption() {
		return getNodeMetadata().getLabelId();
	}

	@Override
	public String getDescription() {
		return getNodeMetadata().getTooltipId();
	}

	@Override
	public String getHelpId() {
		return getNodeMetadata().getHelpId();
	}

	@Override
	public JComponent getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeShow(Common content, Map<String, Object> temporary, ErrorProcessing<Common, Err> err) throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validate(Common content, Map<String, Object> temporary, ErrorProcessing<Common, Err> err) throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterShow(Common content, Map<String, Object> temporary, ErrorProcessing<Common, Err> err) throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		
	}
}
