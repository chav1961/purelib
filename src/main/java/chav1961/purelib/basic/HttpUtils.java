package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import chav1961.purelib.i18n.interfaces.SupportedLanguages;

public class HttpUtils {
	public static SupportedLanguages[] extractSupportedLanguages(final String supportedLanguages, final SupportedLanguages defaultLanguage) {
		if (defaultLanguage == null) {
			throw new NullPointerException("Default language can't be null");
		}
		else if (supportedLanguages == null || supportedLanguages.isEmpty()) {
			return new SupportedLanguages[] {defaultLanguage};
		}
		else {
			final List<SupportedLanguages>	result = new ArrayList<>();
			
			for (String lang : supportedLanguages.split(";")) {
				final String[]				parts = lang.split(",");
				final Locale				locale = Locale.forLanguageTag(parts[0]);
				final SupportedLanguages	sl = SupportedLanguages.of(locale); 
				
				result.add(sl);
			}
			return result.toArray(new SupportedLanguages[result.size()]);
		}
	}
}
