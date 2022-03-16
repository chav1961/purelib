package chav1961.purelib.i18n.interfaces;

import java.io.Reader;
import java.net.URI;
import java.util.Locale;

import javax.swing.ImageIcon;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.streams.char2char.CreoleWriter;

/**
 * <p>This interface describes a wrapper for the standard Java internationalization mechanism. It supports a set of features:</p>
 * <ul>
 * <li>list of supported locales with it's descriptors, language and icons</li>
 * <li>locale listeners mechanism to call on locale changing</li>
 * <li>substitutions in the locale values content (including URLs)</li>
 * <li>automatic <b>Creole markup language</b> processing for value content</li>
 * <li>dynamic hierarchy of the localizers</li> 
 * </ul>
 * <p>All Localizers in the system can be used by two ways:</p>
 * <ul>
 * <li>stand-alone localizer</li>
 * <li>multi-root tree of the localizers</li>
 * </ul>
 * <p>Any stand-alone localizers can be organized to a multi-root tree to get a special behavior for them. To build a multi-root tree, the following methods can be used:</p>
 * <ul>
 * <li>{@linkplain #add(Localizer)} - to add sibling localizer for the given tree node or stand-alone localizer</li>
 * <li>{@linkplain #remove(Localizer)} - to remove sibling localizer from the given tree node</li>
 * <li>{@linkplain #push(Localizer)} - to create a child for the given tree node or stand-alone localizer, and place a localizer to it</li>
 * <li>{@linkplain #close()} - to remove a child was created earlier by {@linkplain #push(Localizer)} from the tree. This method allows Localizer to use it in the <b>try-with-resource</b> statements</li>
 * <li>{@linkplain #pop()} - to remove all children for the given tree node</li>
 * <li>{@linkplain #setParent(Localizer)} - to link/unlink parent for the given Localizer</li>
 * </ul>
 * <p>All the localizers at the given tree node have <i>united</i> resource keys, so resource keys must have unique names. Any child localizer can have the same name of the resource 
 * keys and this names <i>blind</i> the same ones in their parents. Attempt to get resource value by it's name sequentially resolves from the current level of the tree to it's root.
 * Attempt to change locale for the given Localizer expands to all of it's siblings and children in the tree node. Methods {@linkplain #walkDown(LocaleWalking)} and 
 * {@linkplain #walkUp(LocaleWalking)} need be used to implement this functionality.</p>
 * <p>Any content of the values returned can contain a <b>substitution</b> (see {@linkplain CharUtils#substitute(String, String, SubstitutionSource)} 
 * method and {@linkplain SubstitutableProperties} class for details). Any of the also can contain an {@linkplain java.net.URL} reference to include it's content into the value string. Syntax of url
 * reference is:</p> 
 * <p><code>... <b>url(</b>&lt;any_valid_url&gt;<b>)</b></code></p>
 * <p>URL reference can be absolute (including scheme) and relative. Relative URL is always a resource reference inside the project (see {@linkplain Class#getResource(String)} method).</p>
 * @see CreoleWriter
 * @see SubstitutableProperties
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.6
 */
public interface Localizer extends AutoCloseable, SpiService<Localizer> {
	/**
	 * <p>Scheme name for the localizers</p>
	 */
	public static final String	LOCALIZER_SCHEME = "i18n";
	
	/**
	 * <p>Default localizer encoding</p>
	 * @since 0.0.5
	 */
	public static final String	LOCALIZER_DEFAULT_ENCODING = PureLibSettings.DEFAULT_CONTENT_ENCODING;
	
	/**
	 * <p>This interface describes descriptor of the supported locale.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public interface LocaleDescriptor {
		/**
		 * <p>Get locale description</p>
		 * @return locale description. Can't be null
		 */
		Locale getLocale();
		
		/**
		 * <p>Get language description</p>
		 * @return language description. Can't be null or empty
		 */
		String getLanguage();
		
		/**
		 * <p>Get locale description.</p>
		 * @return Locale description (optional)
		 */
		String getDescription();
		
		/**
		 * <p>Get locale icon (usually flag picture)</p>
		 * @return locale icon. Can't be null
		 */
		ImageIcon getIcon();
	}

	/**
	 * <p>This interface describes listener to process locale changes.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface LocaleChangeListener {
		/**
		 * <p>Process changing if the current locale. Call after any locale changing</p>
		 * @param oldLocale old locale before changing. Can't be null
		 * @param newLocale new locale after changing. Can't be null
		 * @throws LocalizationException if any localization problems were detected
		 */
		void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException;
	}
	
	/**
	 * <p>This interface describes walking processor for all Localizers tree</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface LocaleWalking {
		ContinueMode process(Localizer current, int depth) throws LocalizationException;
	}

	/**
	 * <p>This interface describes associations for the givan key with the given parameters (to build substitutable content)</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface LocaleParametersGetter {
		Object[] getParameters() throws Exception;
	}
	
	/**
	 * <p>Get current locale descriptor</p>
	 * @return current locale desriptor. Can't be null
	 */
	LocaleDescriptor currentLocale();
	
	/**
	 * <p>Set new locale.</p>
	 * @param newLocale new locale
	 * @return self
	 * @throws LocalizationException if new locale is not supported
	 * @throws NullPointerException if new locale is null
	 */
	Localizer setCurrentLocale(Locale newLocale) throws LocalizationException, NullPointerException;
	
	/**
	 * <p>Get list of the supported locales.</p>
	 * @return list of supported locales. Can't be null. It's content is joined from all added/pushed localizers
	 * @see #add(Localizer)
	 * @see #remove(Localizer)
	 * @see #push(Localizer)
	 */
	Iterable<LocaleDescriptor> supportedLocales();
	
	/**
	 * <p>Checks the key was defined anywhere in the localizers.</p>
	 * @param key key to check existance. Key content is case-sensitive
	 * @return true if the key is defined anywhere. Result is ORed from all all added/pushed localizers
	 * @throws IllegalArgumentException if key to test is null or empty
	 */
	boolean containsKey(String key) throws IllegalArgumentException;
	
	/**
	 * <p>Get list of all available keys defined anywhere</p>
	 * @return list of the defined keys. Can't be null. It's content is joined from all added/pushed localizers. 
	 * @see #getValue(String)
	 */
	Iterable<String> availableKeys();
	
	/**
	 * <p>Get list available keys in the current Localizer only</p>
	 * @return list of the defined keys. Can't be null. It's content is joined from all added/pushed localizers. 
	 * @see #getLocalValue(String)
	 */
	Iterable<String> localKeys();
	
	/**
	 * <p>Get localization value for the given key. Extracts and substitute values (see {@linkplain SubstitutableProperties}) from localization content. Also processed a set of URLs inside the localization string. 
	 * Each url inside the localization string has syntax:</p>
	 * <p><code><b>url(</b>&lt;valid_url&gt;[<b>,</b>&lt;valid_MIME&gt;]<b>)</b></code></p>
	 * <p>Relative URL (with the missing scheme) points to resource inside the JRE class tree. Duplicated keys in the localizer hierarchy always are resolved to the deepest localizer content</p>
	 * @param key key to get localization string for. Key content is case-sensitive
	 * @return string localized
	 * @throws LocalizationException if the key is missing anywhere.
	 * @throws IllegalArgumentException if key to get is null or empty
	 */
	String getValue(String key) throws LocalizationException, IllegalArgumentException;

	/**
	 * <p>Get localization value for the given key. Extracts and substitute values (see {@linkplain SubstitutableProperties}) from localization content. Also processed a set of URLs inside the localization string. 
	 * Each url inside the localization string has syntax:</p>
	 * <p><code><b>url(</b>&lt;valid_url&gt;[<b>,</b>&lt;valid_MIME&gt;]<b>)</b></code></p>
	 * <p>Relative URL (with the missing scheme) points to resource inside the JRE class tree. Duplicated keys in the localizer hierarchy always are resolved to the deepest localizer content</p>
	 * @param key key to get localization string for. Key content is case-sensitive
	 * @param parameters additional parameters for string formatter
	 * @return string localized
	 * @throws LocalizationException if the key is missing anywhere.
	 * @throws IllegalArgumentException if key to get is null or empty
	 * @since 0.0.3
	 */
	String getValue(String key, Object... parameters) throws LocalizationException, IllegalArgumentException;

	/**
	 * <p>Associate key with localized parameters. Uses for automatic substitutions for the localized parameters</p>
	 * @param key key to associate getter for
	 * @param parametersGetter getter associated
	 * @throws IllegalArgumentException if key to get is null or empty
	 * @throws NullPointerException if getter is null
	 * @since 0.0.3
	 */
	void associateValue(String key, LocaleParametersGetter parametersGetter) throws IllegalArgumentException, NullPointerException;
	
	/**
	 * <p>Get localization value for the given key in the current Localizer only.</p>
	 * @param key key to get localization string for. Key content is case-sensitive
	 * @return string localized
	 * @throws LocalizationException if the key is missing anywhere.
	 * @throws IllegalArgumentException if key to get is null or empty
	 * @see #getValue(String), #getLocalValue(String, Locale)
	 */
	String getLocalValue(String key) throws LocalizationException, IllegalArgumentException;

	/**
	 * <p>Get localization value for the given key and locale in the current Localizer only.</p>
	 * @param key key to get localization string for. Key content is case-sensitive. Can't be null.
	 * @param locale locale to get key for. Can't be null.
	 * @return string localized
	 * @throws LocalizationException if the key is missing anywhere.
	 * @throws IllegalArgumentException if key to get is null or empty
	 * @see #getValue(String), #getLocalValue(String)
	 * @since 0.0.5
	 */
	default String getLocalValue(String key, Locale locale) throws LocalizationException, IllegalArgumentException {
		return getLocalValue(key);
	}
	
	/**
	 * <p>Get localization value for the given key as Reader.</p> 
	 * @param key key to get localization content for
	 * @return localized content
	 * @throws LocalizationException if the key is missing anywhere.
	 * @throws IllegalArgumentException if key to get is null or empty
	 * @see #getValue(String)
	 */
	Reader getContent(String key) throws LocalizationException, IllegalArgumentException;
	
	/**
	 * <p>Get and convert localization value for the given key as Reader. Valid MIME types to source MIME are:</p>
	 * <ul>
	 * <li>text/plain [charset=...]</li>
	 * <li>text/x-wikicreole [charset=...]</li>
	 * </ul>
	 * <p>Valid MIME types to target MIME are:</p>
	 * <ul>
	 * <li>text/plain</li>
	 * <li>text/html</li>
	 * </ul>
	 * @param key key to get localization content for
	 * @param sourceType MIME type for the source content (for example 'text/x-wikicreole charset=UTF-8;')
	 * @param targetType MIME type for the target content (for example 'text/html')
	 * @return localized content
	 * @throws LocalizationException if the key is missing anywhere or conversion errors at the runtime
	 * @throws IllegalArgumentException if key to get is null or empty, also unsupported or unknown conversion combination
	 * @throws NullPointerException if source or target type is null
	 */
	Reader getContent(String key, MimeType sourceType, MimeType targetType) throws LocalizationException, IllegalArgumentException, NullPointerException;
	
	/**
	 * <p>Get unique identifier of the localizer</p>
	 * @return unique identifier of the localized. Can't be null or empty. It's strongly recommended to use localizer URI as value returned
	 */
	URI getLocalizerId();

	/**
	 * <p>Test that localizer level contains localizer with the given id</p>
	 * @param localizerId localizer id (see @{@linkplain #getLocalizerId()})
	 * @return true if localizer presents
	 * @throws NullPointerException string is null
	 * @throws IllegalArgumentException string is empty
	 */
	boolean containsLocalizerHere(URI localizerId) throws NullPointerException, IllegalArgumentException;

	/**
	 * <p>Test that localizer hierarchy contains localizer with the given id</p>
	 * @param localizerId localizer id (see @{@linkplain #getLocalizerId()})
	 * @return true if localizer presents
	 * @throws NullPointerException string is null
	 * @throws IllegalArgumentException string is empty
	 */
	boolean containsLocalizerAnywhere(URI localizerId) throws NullPointerException, IllegalArgumentException;

	/**
	 * <p>Get localizer description by it's id
	 * @param localizerId localizer id to get localizer for
	 * @return localizer found or null if missing
	 * @throws NullPointerException string is null
	 * @throws IllegalArgumentException string is empty
	 */
	Localizer getLocalizerById(URI localizerId) throws NullPointerException, IllegalArgumentException;
	
	/**
	 * <p>Add new localizer to the current level of the hierarchy. Adding the localizer doesn't change it's own parent, siblings and children. Parent for
	 * the localizer to add will be set to <b>this</b> parent.</p>
	 * @param newLocalizer localizer to add.
	 * @return self
	 * @throws LocalizationException localizer to add has duplicated keys at the given hierarchy level
	 * @throws NullPointerException localizer to add is null
	 * @throws IllegalArgumentException if the localized already was added
	 */
	Localizer add(Localizer newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException;

	/**
	 * <p>Add new localizer to the current level of the hierarchy. Adding the localizer doesn't change it's own parent, siblings and children.</p>
	 * @param newLocalizer new localizer URI to add
	 * @return self
	 * @throws LocalizationException localizer to add has duplicated keys at the given hierarchy level
	 * @throws NullPointerException localizer to add is null
	 * @throws IllegalArgumentException if the localized already was added
	 * @since 0.0.4
	 */
	Localizer add(URI newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException;
	
	/**
	 * <p>Remove the localizer from any level of the hierarchy. Removing the localizer doesn't remove it's children from it. Parent of the localizer to remove will be cleared</p>
	 * @param localizer localizer to remove
	 * @return self
	 * @throws LocalizationException when any localization problems were detected   
	 * @throws NullPointerException localizer to remove is null
	 * @throws IllegalArgumentException attempt to remove self
	 * @throws IllegalStateException localizer was removed earlier or was not added 
	 */
	Localizer remove(Localizer localizer) throws LocalizationException, NullPointerException, IllegalArgumentException, IllegalStateException;
	
	/**
	 * <p>Create new level or the hierarchy, and push localizer here. Localizer added can be removed by {@linkplain #remove(Localizer)} or by {@linkplain #pop(Localizer)} method.
	 * If child level of the hierarchy already exists, simply adds new localizer in it. Parent for the localizer to push will be set to <b>this</b> localizer.
	 * Duplicated keys in the localizer blind the same keys in the hierarchy tail</p> 
	 * @param newLocalizer localizer to push.
	 * @return Added localizer. Calling {@linkplain #close()} on this instance should automatically calls the {@linkplain #pop(Localizer)} method on the 'parent' localizer 
	 * @throws LocalizationException when any localization problems were detected   
	 * @throws NullPointerException localizer to add is null
	 * @throws IllegalArgumentException if the localized already was added
	 * @since 0.0.4
	 */
	Localizer push(Localizer newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException;

	/**
	 * <p>Create new level or the hierarchy, and push localizer here. Localizer added can be removed by {@linkplain #remove(Localizer)} or by {@linkplain #pop(Localizer)} method.
	 * If child level of the hierarchy already exists, simply adds new localizer in it. Parent for the localizer to push will be set to <b>this</b> localizer.
	 * Duplicated keys in the localizer blind the same keys in the hierarchy tail</p> 
	 * @param newLocalizer localizer URI to push
	 * @return Added localizer. Calling {@linkplain #close()} on this instance should automatically calls the {@linkplain #pop(Localizer)} method on the 'parent' localizer 
	 * @throws LocalizationException when any localization problems were detected   
	 * @throws NullPointerException localizer to add is null
	 * @throws IllegalArgumentException if the localized already was added
	 */
	Localizer push(URI newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException;
	
	/**
	 * <p>Remove all localizers from the current hierarchy level and pop it</p>
	 * @param oldLocalizer localizer to pop.
	 * @return parent localizer
	 * @throws LocalizationException when any localization problems were detected
	 * @since 0.0.4   
	 */
	Localizer pop(Localizer oldLocalizer) throws LocalizationException;
	
	/**
	 * <p>Remove all localizers from the current hierarchy level and pop it</p>
	 * @return self
	 * @throws LocalizationException when any localization problems were detected   
	 */
	Localizer pop() throws LocalizationException;
	
	/**
	 * <p>Get parent localizer for this localizer.</p>
	 * @return parent localizer. Can be null
	 */
	Localizer getParent();
	
	/**
	 * <p>St parent localizer for the given localizer</p>
	 * @param parent parent localizer to set. Can be null
	 * @return self
	 * @throws LocalizationException on recursive dependencies
	 */
	Localizer setParent(Localizer parent) throws LocalizationException;
	
	/**
	 * <p>Test the localizer is in parent chain</p>
	 * @param test localizer to test
	 * @return true if yes
	 * @throws LocalizationException when any localization problems were detected
	 */
	boolean isInParentChain(Localizer test) throws LocalizationException;
	
	/**
	 * <p>Add listener to notify about locale changes. Listener about to remove should clear it's notification lists automatically</p>
	 * @param listener listener to add
	 * @return self
	 * @throws NullPointerException listened to add is null
	 */
	Localizer addLocaleChangeListener(LocaleChangeListener listener) throws NullPointerException;
	
	/**
	 * <p>Remove listener from the current localizer.</p>
	 * @param listener listener to remove
	 * @return self
	 * @throws NullPointerException listener to remove is null
	 */
	Localizer removeLocaleChangeListener(LocaleChangeListener listener) throws NullPointerException;
	
	/**
	 * <p>Walk Localizers tree from the current to the root. Every sibling (but not it's children) on every level need be processed.</p> 
	 * @param processor processor to process every node in the tree
	 * @return self
	 * @throws NullPointerException processor is null
	 * @throws LocalizationException any errors when processing
	 */
	ContinueMode walkUp(LocaleWalking processor) throws NullPointerException, LocalizationException;
	
	/**
	 * <p>Walk Localizers tree from the current to all the children. Every sibling on every level need be processed.</p> 
	 * @param processor processor to process every node in the tree
	 * @return self
	 * @throws NullPointerException processor is null
	 * @throws LocalizationException any errors when processing
	 */
	ContinueMode walkDown(LocaleWalking processor) throws NullPointerException, LocalizationException;
	
	/**
	 * <p>Close the localizer. Localizer to close purges it's notification list, removes self from parent localizers and calls {@linkplain #pop()}
	 * method on the 'parent' localizer</p> 
	 * @throws LocalizationException any errors when processing
	 */
	@Override
	void close() throws LocalizationException;
	
	/**
	 * <p>This class is a factory to get localizer by it's URI. It implements a 'Factory' template and wraps call to {@linkplain LocalizerFactory#getLocalizer(URI)}</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public final static class Factory {
		private Factory() {}
		
		/**
		 * <p>Get localizer by URI.</p> 
		 * @param localizerUri localizer URI to get localizer for. Can't be null and must have scheme {@value Localizer#LOCALIZER_SCHEME}
		 * @return localizer found
		 * @throws IllegalArgumentException when localizer URI is null or doesn't have {@value Localizer#LOCALIZER_SCHEME} scheme
		 * @throws LocalizationException on any errors on creation localizer
		 */
		public static Localizer newInstance(final URI localizerUri) throws IllegalArgumentException, LocalizationException {
			if (localizerUri == null || !LOCALIZER_SCHEME.equals(localizerUri.getScheme())) {
				throw new IllegalArgumentException("Localizer URI can't be null and must have scheme ["+LOCALIZER_SCHEME+"]"); 
			}
			else {
				return LocalizerFactory.getLocalizer(localizerUri);
			}
		}
	}
}
