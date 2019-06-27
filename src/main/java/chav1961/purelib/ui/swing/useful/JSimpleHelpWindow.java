package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Hashtable;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.i18n.AbstractLocalizer;
import chav1961.purelib.streams.StreamsUtil;

public class JSimpleHelpWindow {
	public JSimpleHelpWindow(final Component parent, final URI source) throws ContentException {
		if (parent == null) {
			throw new NullPointerException("Parent component can't be null"); 
		}
		else if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final Hashtable<String,String[]>	parsed = Utils.parseQuery(source);
				final char[]		content = Utils.loadCharsFromURI(source, parsed.containsKey(AbstractLocalizer.CONTENT_ENCODING) ? parsed.get(AbstractLocalizer.CONTENT_ENCODING)[0] : Charset.defaultCharset().name());
				final JEditorPane	editor = new JEditorPane("text/html",convertContent(content,extractSourceMime(parsed),extractTargetMime(parsed)));
				final JScrollPane	scroll = new JScrollPane(editor);
				final Point			parentPointUL = new Point(0,0), parentPointDR = new Point(parent.getWidth(),parent.getHeight());
				final Dimension		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				final Dimension		parentSize = parent.getSize();
				final Dimension		helpSize = new Dimension(400,250); 
				
				scroll.setPreferredSize(helpSize);				
				SwingUtilities.convertPointToScreen(parentPointUL,parent);
				SwingUtilities.convertPointToScreen(parentPointDR,parent);
				
				final Popup			popup = PopupFactory.getSharedInstance().getPopup(parent,scroll,parentPointUL.x-parentSize.width/2,parentPointUL.y-parentSize.height/2);
				
				editor.addFocusListener(new FocusListener() {
					@Override
					public void focusLost(FocusEvent e) {
						popup.hide();
					}
					
					@Override
					public void focusGained(FocusEvent e) {
					}
				});
				popup.show();
				editor.requestFocusInWindow();
			} catch (IOException | MimeTypeParseException e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	static MimeType extractSourceMime(final Hashtable<String,String[]> parm) throws MimeTypeParseException {
		if (parm.contains(AbstractLocalizer.CONTENT_MIME)) {
			return new MimeType(parm.get(AbstractLocalizer.CONTENT_MIME)[0]);
		}
		else if (parm.contains(AbstractLocalizer.CONTENT_MIME_SOURCE)) {
			return new MimeType(parm.get(AbstractLocalizer.CONTENT_MIME_SOURCE)[0]);
		}
		else {
			return PureLibSettings.MIME_PLAIN_TEXT;
		}
	}

	static MimeType extractTargetMime(final Hashtable<String,String[]> parm) throws MimeTypeParseException {
		if (parm.contains(AbstractLocalizer.CONTENT_MIME_TARGET)) {
			return new MimeType(parm.get(AbstractLocalizer.CONTENT_MIME_TARGET)[0]);
		}
		else {
			return PureLibSettings.MIME_HTML_TEXT;
		}
	}
	
	static String convertContent(final char[] source, final MimeType mimeFrom, final MimeType mimeTo) throws IOException {
		try(final Writer	wr = new StringWriter();
			final Writer	wrapped = StreamsUtil.getStreamClassForOutput(wr, mimeFrom, mimeTo)) {
			
			Utils.copyStream(new CharArrayReader(source),wrapped);
			return wr.toString();
		}
	}
}
