package chav1961.purelib.ui.swing.useful;

import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

public class JModelBasedWizardStep<Common, Err extends Enum<?>> extends JComponent implements WizardStep<Common, Err, JComponent>, NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = 1965509994262064521L;

	private final ContentNodeMetadata	metadata;
	private final StepType				type; 
	
	public JModelBasedWizardStep(final ContentNodeMetadata metadata, final StepType type) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Step type can't be null");
		}
		else {
			this.metadata = metadata;
			this.type = type;
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
	}
	
	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public String getStepId() {
		return getNodeMetadata().getName();
	}

	@Override
	public StepType getStepType() {
		return type;
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
		return this;
	}

	@Override
	public void beforeShow(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, Err> err) throws FlowException {
	}

	@Override
	public boolean validate(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, Err> err) throws FlowException {
		return false;
	}

	@Override
	public void afterShow(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, Err> err) throws FlowException {
	}
}
