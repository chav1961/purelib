package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JComponent;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.WizardStep;
import chav1961.purelib.ui.interfaces.RecordFormManager.RecordAction;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;

public class JModelBasedWizardStep<Common, Err extends Enum<?>> extends JComponent implements WizardStep<Common, Err, JComponent>, NodeMetadataOwner, LocaleChangeListener, AutoCloseable {
	private static final long serialVersionUID = 1965509994262064521L;

	private final ContentNodeMetadata		metadata;
	private final ContentMetadataInterface	inner;
	private final LoggerFacade				logger;
	private final StepType					type;
	private final Object					instance;
	private final AutoBuiltForm<?,?>		comp;

	public JModelBasedWizardStep(final ContentNodeMetadata metadata, final Localizer localizer, final LoggerFacade logger, final StepType type) throws ContentException {
		this(metadata, localizer, logger, type, new JModelBasedWizard.DefaultProducer(metadata, null));
	}	
	
	public JModelBasedWizardStep(final ContentNodeMetadata metadata, final Localizer localizer, final LoggerFacade logger, final StepType type, final Function<Class<?>,Object> producer) throws ContentException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Step type can't be null");
		}
		else {
			this.metadata = metadata;
			this.logger = logger;
			this.type = type;
			this.inner = ContentModelFactory.forAnnotatedClass(metadata.getType());
			this.instance = producer.apply(metadata.getType());
			this.comp = new AutoBuiltForm(inner, localizer, PureLibSettings.INTERNAL_LOADER, instance, (FormManager<?,?>)instance);
			
			((ModuleAccessor)instance).allowUnnamedModuleAccess(comp.getUnnamedModules());
			setLayout(new BorderLayout());
			add(comp, BorderLayout.CENTER);
		}
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(comp, oldLocale, newLocale);
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
		((FormManager)instance).onRecord(RecordAction.REFRESH, logger, null, instance, null, instance);
	}

	@Override
	public boolean validate(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, Err> err) throws FlowException {
		final RefreshMode mode = ((FormManager)instance).onRecord(RecordAction.CHECK, logger, null, instance, null, instance);
		
		switch (mode) {
			case EXIT : case FIELD_ONLY : case NONE : case RECORD_ONLY : case TOTAL :
				return true;
			case DEFAULT	:
				return true;
			case REJECT		:
				return false;
			default:
				throw new UnsupportedOperationException("Refresh mode ["+mode+"] is not supported yet");
		}
	}

	@Override
	public void afterShow(final Common content, final Map<String, Object> temporary, final ErrorProcessing<Common, Err> err) throws FlowException {
		((FormManager)instance).onRecord(RecordAction.UPDATE, logger, null, instance, null, instance);
	}
}
