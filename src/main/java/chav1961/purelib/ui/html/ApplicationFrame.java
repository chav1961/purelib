package chav1961.purelib.ui.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.UUID;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.html.interfaces.HtmlSerializable;
import chav1961.purelib.ui.html.interfaces.SessionSpecificInstance;

public class ApplicationFrame<Session> implements LocaleChangeListener, SessionSpecificInstance<Session, ApplicationFrame<Session>>, HtmlSerializable, AutoCloseable, NodeMetadataOwner {
	private final ApplicationFrame<Session>	parent;
	private final Localizer					localizer;
	private final ContentMetadataInterface	mdi;
	
	public ApplicationFrame(final Localizer localizer, final ContentMetadataInterface mdi) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else {
			this.parent = null;
			this.localizer = localizer;
			this.mdi = mdi;
		}
	}
	
	protected ApplicationFrame(final ApplicationFrame<Session> parent, final Session session) {
		this.parent = parent;
		this.localizer = null;
		this.mdi = null;
		
		fillLocalizedStrings();
	}
	
	@Override
	public ApplicationFrame<Session> getInstance(final Session session) throws NullPointerException, ContentException {
		if (session == null) {
			throw new NullPointerException("Session ID can't be null");
		}
		else {
			return new ApplicationFrame<Session>(this, session);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		if (parent != null) {
			fillLocalizedStrings();
		}
	}

	@Override
	public void serialize(final Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processRequest(final JsonNode request, final Writer response) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		if (parent != null) {
			
		}
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public <T> UUID registerControl(final T control) {
		return null;
	}

	public void unregisterControl(final UUID controlId) {
		
	}
	
	public <T> T getControl(final UUID controlId, final Class<T> awaited) {
		return null;
	}

	protected Localizer getLocalizer() {
		if (parent == null) {
			return localizer;
		}
		else {
			return parent.localizer;
		}
	}
	
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}
}
