package chav1961.purelib.ui.swing.useful.inner;


import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelib.ui.swing.useful.JContentMetadataEditor/chav1961/purelib/i18n/localization.xml")
@LocaleResource(value="JContentMetadataEditor.root.caption",tooltip="JContentMetadataEditor.root.caption.tt")
public class RootDescriptor implements FormManager<Object,RootDescriptor> {
	
	@LocaleResource(value="JContentMetadataEditor.root.fieldName",tooltip="JContentMetadataEditor.root.fieldName.tt")
	@Format("30m")
	public String		fieldName = "<name>";

	private final LoggerFacade	logger;
	
	public RootDescriptor(final LoggerFacade logger) {
		this.logger = logger;
	}

	
	@Override
	public RefreshMode onField(RootDescriptor inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
}