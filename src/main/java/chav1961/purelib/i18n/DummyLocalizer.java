package chav1961.purelib.i18n;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class is a special implementation of localizer to use in non-multilanguaged applications. It doesn't make any language-specific conversions, but 
 * transparently passes it arguments to output. It supports all the functionality of the {@linkplain Localizer}, but doesh't have it's own URI scheme and 
 * can't be accessed thru the {@linkplain LocalizerFactory} mechanism. Create it explicitly with the default constructor if you need.</p>
 *   
 * @author Alexander Chernomyrdin aka chav1961
 * @see Localizer
 * @since 0.0.3
 */

public class DummyLocalizer extends AbstractLocalizer {
	private static final List<String>	keys = new ArrayList<>();

	/**
	 * <p>Create dummy localizer</p>
	 * @throws LocalizationException not currently fired
	 * @throws NullPointerException not currently fired
	 */
	public DummyLocalizer() throws LocalizationException, NullPointerException {
		super();
	}

	@Override
	public String getLocalizerId() {
		return "dummy:";
	}

	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		return false;
	}

	@Override
	public Localizer newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		throw new EnvironmentException("Dummy localizer instance must be created by default constructor only!");
	}

	@Override
	public Iterable<String> localKeys() {
		return keys;
	}

	@Override
	public String getLocalValue(String key) throws LocalizationException, IllegalArgumentException {
		return key;
	}

	@Override
	protected void loadResource(Locale newLocale) throws LocalizationException, NullPointerException {
	}

	@Override
	protected String getHelp(String helpId) throws LocalizationException, IllegalArgumentException {
		return helpId;
	}
}
