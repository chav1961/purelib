package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class JCreoleHelpWindow extends JEditorPane implements LocaleChangeListener {
	private static final long 			serialVersionUID = -2121747413508372083L;
	private static final Set<String> 	HEADERS = Set.of("h1","h2","h3","h4","h5","h6");

	private final Localizer				localizer;
	private String						lastContent;
	
	public JCreoleHelpWindow(final Localizer localizer, final String root) throws LocalizationException {
		super(PureLibSettings.MIME_HTML_TEXT.toString(),"");
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (root == null || root.isEmpty()) {
			throw new IllegalArgumentException("Root string ID can't be null or empty"); 
		}
		else {
			this.localizer = localizer;
			this.lastContent = root;
			
			try{loadContent(root);
			} catch (IOException exc) {
				throw new LocalizationException(exc.getLocalizedMessage(),exc);
			}
			addHyperlinkListener((e)->{
				if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					((JComponent)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}				
				else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
					((Component)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}				
				else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					
					if (e.getURL() != null) {
						if (Desktop.isDesktopSupported()) {
							try{Desktop.getDesktop().browse(e.getURL().toURI());
							} catch (IOException | URISyntaxException exc) {
								PureLibSettings.CURRENT_LOGGER.message(Severity.error, exc.getLocalizedMessage(), exc);
							}
						}
					}
					else {
						final URI	uri = URI.create(e.getDescription());
						
						if (uri.getPath() != null && !uri.getPath().isEmpty()) {
							try{loadContent(lastContent = URIUtils.removeFragmentFromURI(URIUtils.removeQueryFromURI(uri)).toString());
							} catch (IOException | LocalizationException exc) {
								PureLibSettings.CURRENT_LOGGER.message(Severity.error, exc.getLocalizedMessage(), exc);
							}
						}
						final String	fragment = uri.getFragment();
						
						if (fragment != null) {
							for (Element item : getDocument().getRootElements()) {
								try{final int	found = findHtmlReference(fragment, item);

									if (found >= 0) {
										scrollRectToVisible(modelToView2D(found).getBounds());
										break;
									}
								} catch (BadLocationException e1) {
								}
							}
						}
					}
				}
			});
			setEditable(false);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		try{loadContent(lastContent);
		} catch (IOException e) {
			throw new LocalizationException(e.getLocalizedMessage(),e);
		}
	}
	
	private int findHtmlReference(final String fragment, final Element element) throws BadLocationException {
		if (HEADERS.contains(element.getName()) && fragment.equals(element.getDocument().getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset()).trim())) {
			return element.getStartOffset();
		}
		else {
			for (int index = 0; index < element.getElementCount(); index++) {
				final int	result = findHtmlReference(fragment, element.getElement(index));
				
				if (result >= 0) {
					return result;
				}
			}
			return -1;
		}
	}
	
	private void loadContent(final String uri) throws LocalizationException, IOException {
		setText(Utils.fromResource(localizer.getContent(uri, PureLibSettings.MIME_CREOLE_TEXT, PureLibSettings.MIME_HTML_TEXT)));
	}
}
