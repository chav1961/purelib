package chav1961.purelib.i18n.interfaces;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.i18n.AbstractLocalizer;

/**
 * <p>This enumerations contains all locales currently supported by Pure Library</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.5
 */
public enum SupportedLanguages {
	en(Locale.forLanguageTag("en")), 
	ru(Locale.forLanguageTag("ru"));
	
	private final Locale	locale;
	private final Icon		icon;
	private final URI		iconUri;
	
	private SupportedLanguages(final Locale locale) {
		this.locale = locale;
		
		try{this.iconUri = this.getClass().getResource(name()+".png").toURI();
			this.icon = new ImageIcon(iconUri.toURL());
		} catch (URISyntaxException | MalformedURLException e) {
			throw new PreparationException(e); 
		}
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public URI getIconURI() {
		return iconUri;
	}
}