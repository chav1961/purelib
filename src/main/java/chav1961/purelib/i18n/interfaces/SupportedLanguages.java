package chav1961.purelib.i18n.interfaces;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import chav1961.purelib.basic.exceptions.PreparationException;

/**
 * <p>This enumerations contains all locales currently supported by Pure Library</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.6
 */

@LocaleResourceLocation("i18n:xml:root://chav1961.purelib.i18n.interfaces.SupportedLanguages/chav1961/purelib/i18n/localization.xml")
public enum SupportedLanguages {
	@LocaleResource(value="en",tooltip="en.tt")
	en(Locale.forLanguageTag("en")), 
	@LocaleResource(value="ru",tooltip="ru.tt")
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