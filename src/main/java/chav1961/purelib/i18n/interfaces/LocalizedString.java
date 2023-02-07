package chav1961.purelib.i18n.interfaces;

import java.util.Locale;

import chav1961.purelib.basic.exceptions.LocalizationException;

public interface LocalizedString extends LocalizerOwner, Cloneable {
	String getId() throws LocalizationException;
	String getValue() throws LocalizationException;
	String getValue(final Locale lang) throws LocalizationException;
	String getValueOrDefault(final Locale lang) throws LocalizationException;
	boolean isLanguageSupported(final Locale lang) throws LocalizationException;
	Object clone() throws CloneNotSupportedException;
}
