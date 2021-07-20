package chav1961.purelib.i18n;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class is a special implementation to use in the JUnit tests only. It supports all the functionality of the {@linkplain Localizer}, but doesh't have it's own
 * URI scheme and can't be accessed thru the {@linkplain LocalizerFactory} mechanism. Use this class <i>for debugging purposes only</i> and don't include it in your
 * production code.</p>
 *   
 * @author Alexander Chernomyrdin aka chav1961
 * @see Localizer
 * @since 0.0.2
 */

public class DebuggingLocalizer extends AbstractLocalizer {
	private static final Map<String,String>	EMPTY_HELP = new HashMap<>();
	
	private static final URI				DEBUG_URI = URI.create("debug:/");

	private final Map<String,SubstitutableProperties>	keysAndValues;
	private final Map<String,String>					helps;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param keysAndValues map with the language and key/value pairs associated with it. Can't be null and need contains at least one element
	 * @throws LocalizationException On any localization errors
	 * @throws IllegalArgumentException if keysAndValues map is noll or empty
	 * @throws NullPointerException if some paramaters are null
	 */
	public DebuggingLocalizer(final Map<String,SubstitutableProperties> keysAndValues) throws LocalizationException, IllegalArgumentException, NullPointerException {
		this(keysAndValues,EMPTY_HELP);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param keysAndValues map with the language and key/value pairs associated with it. Can't be null and need contains at least one element
	 * @param helps map with help ids (values similar uri('URI name')) and the text associated. Can't be null
	 * @throws LocalizationException On any localization errors
	 * @throws NullPointerException if some parameters are null
	 */
	public DebuggingLocalizer(final Map<String,SubstitutableProperties> keysAndValues, final Map<String,String> helps) throws LocalizationException, IllegalArgumentException, NullPointerException {
		super();
		if (keysAndValues == null) {
			throw new NullPointerException("Key/value map can't be null map");
		}
		else if (helps == null) {
			throw new NullPointerException("Helps map can't be null");
		}
		else {
			this.keysAndValues = keysAndValues;
			this.helps = helps;
			setCurrentLocale(Locale.getDefault());
		}
	}
	
	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		return false;
	}

	@Override
	public URI getLocalizerId() {
		return DEBUG_URI;
	}
	
	@Override
	public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		throw new EnvironmentException("This localizer can be used for the debugging purposes only and must not be included in the SPI descriptors");
	}

	@Override
	public Iterable<String> localKeys() {
		final Set<String>	result = new HashSet<>();
		
		for (Entry<String, SubstitutableProperties> localeDesc : keysAndValues.entrySet()) {
			for (Entry<Object, Object> item : localeDesc.getValue().entrySet()) {
				result.add(item.getKey().toString());
			}
		}
		return result;
	}

	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		if (keysAndValues.containsKey(currentLocale().getLocale().getLanguage())) {
			return keysAndValues.get(currentLocale().getLocale().getLanguage()).getProperty(key);
		}
		else {
			throw new LocalizationException("Any key/value pairs are missing for the current locale");
		}
	}

	@Override
	public String getLocalValue(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
		if (keysAndValues.containsKey(currentLocale().getLocale().getLanguage())) {
			return keysAndValues.get(locale.getLanguage()).getProperty(key);
		}
		else {
			throw new LocalizationException("Any key/value pairs are missing for the locale");
		}
	}
	
	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
	}

	@Override
	protected String getHelp(final String helpId, final String encoding) throws LocalizationException, IllegalArgumentException {
		if (helps.containsKey(helpId)) {
			return helps.get(helpId);
		}
		else {
			return "Help id ["+helpId+"] is missing in the helps";
		}
	}
}
