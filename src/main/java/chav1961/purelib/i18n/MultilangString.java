package chav1961.purelib.i18n;

import java.util.Arrays;
import java.util.Locale;

/**
 * <p>This class is a multilanguage string. It contains all available values for all the languages supported.
 * Every value associates with the appropriative locale for it. Getting string value defines by current locale
 * on the computer</p> 
 * 
 * @see MultilangStringRepo
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class MultilangString {
	private static final int	LANG_STEP = 2; 
	
	private Locale[]			locales = new Locale[LANG_STEP]; 
	private String[]			contents = new String[LANG_STEP];
	private int					index = 0;
	
	/**
	 * <p>Add string value for the given locale or replace it's value if the given locale already exists</p>
	 * @param locale locale to add
	 * @param content text representation for the given locale
	 * @return self
	 */
	public MultilangString add(final Locale locale, final String content) {
		if (locale == null) {
			throw new IllegalArgumentException("Locale can't be null"); 
		}
		else if (content == null) {
			throw new IllegalArgumentException("Content string can't be null"); 
		}
		else {
			for (int curs = 0; curs < index; curs++) {
				if (locales[curs] == locale) {
					contents[curs] = content;
					return this;
				}
			}
			if (index >= locales.length) {
				final Locale[]	newLocales = new Locale[locales.length+LANG_STEP];
				final String[]	newContents = new String[contents.length+LANG_STEP];
				
				System.arraycopy(locales,0,newLocales,0,locales.length);
				System.arraycopy(contents,0,newContents,0,contents.length);
				locales = newLocales;
				contents = newContents;
			}
			locales[index] = locale;
			contents[index++] = content;
			return this;
		}
	}
	
	/**
	 * <p>Get string value according to the current locale</p>
	 * @return string value gotten
	 */
	public String get() {
		return get(Locale.getDefault());
	}

	/**
	 * <p>Get string value for the given locale</p>
	 * @param locale locale to get string for
	 * @return string value gotten or a special text if the locale is missing in this string
	 */
	public String get(final Locale locale) {
		if (locale == null) {
			throw new IllegalArgumentException("Locale can't be null"); 
		}
		else {
			for (int index = 0, maxIndex = locales.length; index < maxIndex; index++) {
				if (locales[index] == locale) {
					return contents[index];
				}
			}
			return "<No data for the locale ["+locale+"]>";
		}
	}

	@Override
	public String toString() {
		return "MultilangString [locales=" + Arrays.toString(locales) + ", contents=" + Arrays.toString(contents) + ", index=" + index + "]";
	}
}
