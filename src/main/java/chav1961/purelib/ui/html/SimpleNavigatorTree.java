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
	private final Localizer						localizer;
	private final SimpleNavigatorTree<Session>	parent;
	private final ContentNodeMetadata 			metadata;

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
			this.metadata = metadata;
			this.localizer = localizer;
			fillLocalizedStrings();
		}
	}

	protected SimpleNavigatorTree(final SimpleNavigatorTree<Session> parent) throws NullPointerException, IllegalArgumentException, LocalizationException {
		this.parent = parent;
		this.metadata = null;
		this.localizer = null;
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
		if (parent != null) {
			parent.serialize(writer);
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			sb.append("<ul id=\"").append(metadata.getUIPath()).append("\">");
			for (ContentNodeMetadata item : metadata) {
				appendContent(sb,item);
			}
			sb.append("</ul>");
			
			writer.write(sb.toString());
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		
	}

	private void appendContent(final StringBuilder sb, final ContentNodeMetadata item) {
		if (item.getChildrenCount() > 0) {
			sb.append("<li id=\"").append(item.getUIPath()).append("\" class=\"tooltip\"><span class=\"caret\">").append(getValue(item.getLabelId())).append("</span>");
			if (item.getIcon() != null) {
				
			}
			if (item.getTooltipId() != null) {
				sb.append("<span class=\"tooltiptext\">").append(getValue(item.getTooltipId())).append("</span>");
			}
			sb.append("<ul class=\"nested\">");
			for (ContentNodeMetadata child : item) {
				appendContent(sb,child);
			}
			sb.append("</ul></li>");
		}
		else {
			sb.append("<li id=\"").append(item.getUIPath()).append("\" class=\"tooltip\">");
			if (item.getIcon() != null) {
				
			}
			sb.append("<a href=\"#\">").append(getValue(item.getLabelId())).append("</a></li>");
			if (item.getTooltipId() != null) {
				sb.append("<span class=\"tooltiptext\">").append(getValue(item.getTooltipId())).append("</span>");
			}
		}
	}

	private String getValue(final String value) {
		try{return localizer.getValue(value);
		} catch (LocalizationException e) {
			return value;
		}
	}
	
}
