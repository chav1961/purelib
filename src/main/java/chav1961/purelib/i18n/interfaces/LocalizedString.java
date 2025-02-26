package chav1961.purelib.i18n.interfaces;

import java.util.Locale;

import chav1961.purelib.basic.exceptions.LocalizationException;

/**
 * <p>This interface describes localized string. Localized string is a pair of a key (named <b>id</b>) and a few number of associated strings 
 * (named <b>value</b>). Each value in the pair is associated with the appropriative {@linkplain Locale}. String can be used anywhere to localize
 * any messages by it's id.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public interface LocalizedString extends LocalizerOwner, Cloneable {
	/**
	 * <p>Get localized string id.</p>
	 * @return localized string id. Can't be null
	 * @throws LocalizationException on any localization error.
	 */
	String getId() throws LocalizationException;
	
	/**
	 * <p>Get value associated with the given id and current locale.</p>
	 * @return value associated (can't be null but can be empty)
	 * @throws LocalizationException on any localization error.
	 */
	String getValue() throws LocalizationException;
	
	/**
	 * <p>Get value associated with the given id and given locale.</p>
	 * @param lang locale to get value for. Can't be null.
	 * @return value associated (can't be null but can be empty)
	 * @throws LocalizationException on any localization error.
	 */
	String getValue(final Locale lang) throws LocalizationException;
	
	/**
	 * <p>Get value associated with the given id and given or default locale.</p>
	 * @param lang locale to get value for. Can't be null.
	 * @return value associated (can't be null but can be empty)
	 * @throws LocalizationException on any localization error.
	 */
	String getValueOrDefault(final Locale lang) throws LocalizationException;
	
	/**
	 * <p>Is checking locale supported by current localized string</p>
	 * @param lang locale to check. Can't be null.
	 * @return true if supported, false otherwise
	 * @throws LocalizationException on any localization error.
	 */
	boolean isLanguageSupported(final Locale lang) throws LocalizationException;

	/**
	 * <p>Clone current localized string.</p>
	 * @return string cloned. Can't be null
	 * @throws CloneNotSupportedException never thrown.
	 */
	Object clone() throws CloneNotSupportedException;
}
