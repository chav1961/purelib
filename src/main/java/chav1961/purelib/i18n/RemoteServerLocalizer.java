package chav1961.purelib.i18n;

import java.io.Reader;
import java.net.URI;
import java.util.Locale;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;

public class RemoteServerLocalizer implements Localizer {

	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Localizer newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocaleDescriptor currentLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer setCurrentLocale(Locale newLocale) throws LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<LocaleDescriptor> supportedLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsKey(String key) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterable<String> availableKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<String> localKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue(String key) throws LocalizationException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue(String key, Object... parameters) throws LocalizationException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void associateValue(String key, LocaleParametersGetter parametersGetter) throws IllegalArgumentException, NullPointerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalValue(String key) throws LocalizationException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reader getContent(String key) throws LocalizationException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Reader getContent(String key, MimeType sourceType, MimeType targetType) throws LocalizationException, IllegalArgumentException, NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getLocalizerId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsLocalizerHere(URI localizerId) throws NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsLocalizerAnywhere(URI localizerId) throws NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Localizer getLocalizerById(URI localizerId) throws NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer add(Localizer newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer add(URI newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer remove(Localizer localizer) throws LocalizationException, NullPointerException, IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer push(Localizer newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer push(URI newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer pop(Localizer oldLocalizer) throws LocalizationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer pop() throws LocalizationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer setParent(Localizer parent) throws LocalizationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer addLocaleChangeListener(LocaleChangeListener listener) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Localizer removeLocaleChangeListener(LocaleChangeListener listener) throws NullPointerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContinueMode walkUp(LocaleWalking processor) throws NullPointerException, LocalizationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContinueMode walkDown(LocaleWalking processor) throws NullPointerException, LocalizationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
}
