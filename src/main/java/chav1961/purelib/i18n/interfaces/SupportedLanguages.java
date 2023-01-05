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
 * @last.update 0.0.6
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

	/**
	 * <p>Get locale associated with the enumeration item</p>
	 * @return locale associated. Can't be null
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * <p>Get icon associated with the enumeration item</p>
	 * @return icon associated. Can't be null
	 */
	public Icon getIcon() {
		return icon;
	}
	
	/**
	 * <p>Get icon URI associated with the enumeration item</p>
	 * @return icon URI associated. Can't be null 
	 */
	public URI getIconURI() {
		return iconUri;
	}
	
	/**
	 * <p>Get enumeration for default system settings</p>
	 * @return enumeration for default system settings or {@linkplain SupportedLanguages#en} when not identified</p>
	 */
	public static SupportedLanguages getDefaultLanguage() {
		return of(Locale.getDefault());
//		for (SupportedLanguages item : values()) {
//			if (item.getLocale().getLanguage().equals(Locale.getDefault().getLanguage())) {
//				return item;
//			}
//		}
//		return en;
	}
	
	/**
	 * <p>Get enumeration for the given locale</p>
	 * @param locale locale to get enumeration for. Can't be null
	 * @return enumeration for default system settings or {@linkplain SupportedLanguages#en} when not identified</p>
	 * @throws NullPointerException when parameter is null
	 * @since 0.0.6
	 */
	public static SupportedLanguages of(final Locale locale) throws NullPointerException {
		if (locale == null) {
			throw new NullPointerException("Locale can't be null");
		}
		else {
			for (SupportedLanguages item : values()) {
				if (item.getLocale().getLanguage().equals(locale.getLanguage())) {
					return item;
				}
			}
			return en;
		}
	}
}