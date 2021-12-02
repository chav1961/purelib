package chav1961.purelib.ui.swing.inner;

import java.net.URI;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelib.ui.swing.JReferenceListWithMeta/chav1961/purelib/i18n/localization.xml")
@LocaleResource(value="JReferenceListWithMeta.editor.title",tooltip="JReferenceListWithMeta.editor.title.tt",help="JReferenceListWithMeta.editor.title.help")
public class ReferenceAndCommentEditor implements FormManager<Object, ReferenceAndCommentEditor> {
	private final LoggerFacade	logger;
	
	@LocaleResource(value="JReferenceListWithMeta.editor.ref",tooltip="JReferenceListWithMeta.editor.ref.tt")
	@Format("30ms")
	public URI			ref = URI.create("http:/");
	@LocaleResource(value="JReferenceListWithMeta.editor.comment",tooltip="JReferenceListWithMeta.editor.comment.tt")
	@Format("30ms")
	public String		comment = "";

	public ReferenceAndCommentEditor(final LoggerFacade logger) {
		this.logger = logger;
	}
	
	@Override
	public RefreshMode onField(final ReferenceAndCommentEditor inst, final Object id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
}