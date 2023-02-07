package chav1961.purelib.i18n.interfaces;

import java.util.Locale;

import chav1961.purelib.basic.exceptions.LocalizationException;

public interface MutableLocalizedString extends LocalizedString {
	void setId(String id) throws LocalizationException;
	void addValue(Locale lang, String value) throws LocalizationException;
	void setValue(Locale lang, String value) throws LocalizationException;
	void removeValue(Locale lang) throws LocalizationException;
}
