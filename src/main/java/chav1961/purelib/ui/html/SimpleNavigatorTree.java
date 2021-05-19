package chav1961.purelib.ui.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.html.interfaces.HtmlSerializable;
import chav1961.purelib.ui.html.interfaces.SessionSpecificInstance;

public class SimpleNavigatorTree<Session> implements LocaleChangeListener, SessionSpecificInstance<Session, SimpleNavigatorTree<Session>>, HtmlSerializable {
	private final SimpleNavigatorTree<Session>	parent;

	public SimpleNavigatorTree(final Localizer localizer, final ContentNodeMetadata metadata) throws NullPointerException, IllegalArgumentException, LocalizationException {
		this(localizer,metadata,false);
	}
	
	public SimpleNavigatorTree(final Localizer localizer, final ContentNodeMetadata metadata, final boolean lazyLoading) throws NullPointerException, IllegalArgumentException, LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (!metadata.getRelativeUIPath().toString().contains(Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
			throw new IllegalArgumentException("Metadata must be referred to navigation top node");
		}
		else {
			this.parent = null;
			fillLocalizedStrings();
		}
	}

	protected SimpleNavigatorTree(final SimpleNavigatorTree<Session> parent) throws NullPointerException, IllegalArgumentException, LocalizationException {
		this.parent = parent;
		fillLocalizedStrings();
	}	
	
	@Override
	public SimpleNavigatorTree<Session> getInstance(final Session session) throws ContentException {
		// TODO Auto-generated method stub
		if (session == null) {
			throw new NullPointerException("Session can't be null");
		}
		else if (parent != null) {
			throw new IllegalStateException("Attempt to call method on child instance"); 
		}
		else {
			return null;
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serialize(final Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		
	}
}
