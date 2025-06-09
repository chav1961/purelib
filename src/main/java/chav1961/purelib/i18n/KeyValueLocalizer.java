package chav1961.purelib.i18n;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.AbstractLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

/**
 * <p>This class implements mutable key/value localizer to modify it's content programmatically.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.7
 */
public class KeyValueLocalizer extends AbstractLocalizer {
	private static final String			SUBSCHEME = "keyvalue";
	private static final AtomicInteger	UNIQUE = new AtomicInteger();
	
	private final Map<String,String[]>	keys = new HashMap<>();
	private final Map<String,String[]>	help = new HashMap<>();
	private final URI		uniqueId = URI.create("unique:/"+UNIQUE.incrementAndGet()); 

	@Override
	public URI getLocalizerId() {
		return uniqueId;
	}

	@Override
	public String getSubscheme() {
		return SUBSCHEME;
	}
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		return false;
	}

	@Override
	public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		throw new UnsupportedOperationException("This localizer can't be instantianetd by SPI");
	}

	@Override
	public Iterable<String> localKeys() {
		final Set<String>	result = new HashSet<>();
		
		result.addAll(keys.keySet());
		result.addAll(help.keySet());
		return result;
	}

	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		return getLocalValue(key, currentLocale().getLocale());
	}

	@Override
	public String getLocalValue(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Key can't be null or empty");
		}
		else if (locale == null) {
			throw new NullPointerException("Locale can't be null");
		}
		else if (isLocaleSupported(key, locale)) {
			return keys.containsKey(key) ? keys.get(key)[SupportedLanguages.of(locale).ordinal()] : help.get(key)[SupportedLanguages.of(locale).ordinal()];
		}
		else {
			throw new LocalizationException("Key ["+key+"] is not supported for locale ["+locale+"]");
		}
	}

	/**
	 * <p>Add key/value pair for the given locale.</p>
	 * @param key key name to add. Can be neither null nor empty.
	 * @param locale Locale to add value for. Can't be null.
	 * @param value key value to add. Can be neither null nor empty.
	 * @throws NullPointerException locale is null
	 * @throws IllegalArgumentException any string argument is null or empty
	 */
	public void addKey(final String key, final Locale locale, final String value) throws NullPointerException, IllegalArgumentException{
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Key can't be null or empty");
		}
		else if (locale == null) {
			throw new NullPointerException("Locale can't be null");
		}
		else if (Utils.checkEmptyOrNullString(value)) {
			throw new IllegalArgumentException("Value can't be null or empty");
		}
		else {
			final SupportedLanguages	lang = SupportedLanguages.of(locale);
			
			if (!keys.containsKey(key)) {
				keys.put(key, new String[SupportedLanguages.values().length]);
			}
			keys.get(key)[lang.ordinal()] = value;
		}
	}

	/**
	 * <p>Add key/value help content for the given locale.</p>
	 * @param key key name to add. Can be neither null nor empty.
	 * @param locale Locale to add value for. Can't be null.
	 * @param value key value to add. Can be neither null nor empty.
	 * @throws NullPointerException locale is null
	 * @throws IllegalArgumentException any string argument is null or empty
	 */
	public void addHelp(final String key, final Locale locale, final String value) {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Key can't be null or empty");
		}
		else if (locale == null) {
			throw new NullPointerException("Locale can't be null");
		}
		else if (Utils.checkEmptyOrNullString(value)) {
			throw new IllegalArgumentException("Value can't be null or empty");
		}
		else {
			final SupportedLanguages	lang = SupportedLanguages.of(locale);
			
			if (!help.containsKey(key)) {
				help.put(key, new String[SupportedLanguages.values().length]);
			}
			help.get(key)[lang.ordinal()] = value;
		}
	}
	
	@Override
	protected boolean isLocaleSupported(String key, Locale locale) throws LocalizationException, IllegalArgumentException {
		return keys.containsKey(key) && keys.get(key)[SupportedLanguages.of(locale).ordinal()] != null 
			|| help.containsKey(key) && help.get(key)[SupportedLanguages.of(locale).ordinal()] != null;
	}

	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
	}

	@Override
	public String getHelp(final String helpId, final Locale locale, final String encoding) throws LocalizationException, IllegalArgumentException {
		return help.get(helpId)[SupportedLanguages.of(locale).ordinal()];
	}
}
