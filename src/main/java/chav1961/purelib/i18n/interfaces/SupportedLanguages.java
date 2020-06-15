package chav1961.purelib.i18n.interfaces;

import java.util.Locale;

/**
 * <p>This enumerations contains all locales currently supported by Pure Library</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
public enum SupportedLanguages {
	en(Locale.forLanguageTag("en")), 
	ru(Locale.forLanguageTag("ru"));
	
	private final Locale	locale;
	
	SupportedLanguages(final Locale locale) {
		this.locale = locale;
	}
	
	public Locale getLocale() {
		return locale;
	}
}