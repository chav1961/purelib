package chav1961.purelib.i18n;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class describes a repository for {@link MultilangString} entites. Every entity identifies in the repository
 * by it;s unique <i><key</i>. Repository can be filled by individual call by {@link #add(String, MultilangString)} method,
 * by bulk method {@link MultilangStringRepo#add(MultilangStringRepo)} or by import repository content from XML DOM tree. 
 * This class can also substitute multilang strings into the given string content (method {@link MultilangStringRepo#substitute(String)})</p>
 *  
 * <p>Repository content can be any subtree of the XML DOM tree and is a sequence of items:</p>
 * <p><code>&lt;item key="key"&gt;&lt;value locale="Locale.NAME"&gt;CONTENT&lt;/value&gt;...&lt;/item&gt;</code></p>
 * 
 * @see MultilangString
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class MultilangStringRepo implements AutoCloseable {
	private static final String		TAG_ITEM = "item";
	private static final String		TAG_VALUE = "value";

	private static final String		ATTR_KEY = "key";
	private static final String		ATTR_LOCALE = "locale";

	private static final Map<String,Locale>	SUBST = new HashMap<>();
	
	static {
		for (Locale locale : Locale.getAvailableLocales()) {
			SUBST.put(locale.toString(),locale);
		}
	}
	
	private final Map<String,MultilangString>	repo = new HashMap<>();

	@Override
	public void close() throws Exception {
		repo.clear();
	}

	/**
	 * <p>Import data from XML DOM subtree</p>
	 * @param root XML DOM subtree containing data
	 * @return self
	 * @throws ContentException illegal data format in the DOM
	 */
	public MultilangStringRepo importData(final Element root) throws ContentException {
		if (root == null) {
			throw new IllegalArgumentException("Root repository element can't be null");
		}
		else {
			final NodeList				list = root.getChildNodes();
			
			for (int index = 0; index < list.getLength(); index++) {
				final Object		temp = list.item(index);
				
				if (temp instanceof Element) {
					final Element	entity = (Element)temp;
					
					switch (entity.getTagName()) {
						case TAG_ITEM	:
							final NodeList			items = entity.getChildNodes();
							final MultilangString	string = new MultilangString(); 
							final String			key = entity.getAttribute(ATTR_KEY);
							
							if (key == null || key.isEmpty()) {
								throw new IllegalArgumentException("Tag ["+TAG_ITEM+"] not contains mandatory attribute ["+ATTR_KEY+"] or it is empty"); 
							}
							else {
								for (int itemIndex = 0; itemIndex < items.getLength(); itemIndex++) {
									final Object		tempItem = items.item(itemIndex);
									
									if (tempItem instanceof Element) {
										final Element	value = (Element)tempItem;
										
										switch (value.getTagName()) {
											case TAG_VALUE	:
												final String	localeName = value.getAttribute(ATTR_LOCALE);
												final String	text = value.getTextContent().trim();
												
												if (!SUBST.containsKey(localeName)) {
													throw new ContentException("Unknown 'locale' attribute value ["+localeName+"]. Available locales are "+SUBST.keySet());
												}
												else {
													string.add(SUBST.get(localeName),text);
												}
												break;
											default :
												throw new ContentException("Unsupported tag ["+value.getTagName()+"] in the DOM");
										}
									}
								}
								add(key,string);
							}
							break;
						default			:
							throw new ContentException("Unsupported tag ["+entity.getTagName()+"] in the DOM");
					}
				}
			}
		}
		return this;
	}

	/**
	 * <p>Get repository size</p>
	 * @return repository size
	 */
	public int size() {
		return repo.size();
	}
	
	/**
	 * <p>Get key list from the repository</p>
	 * @return
	 */
	public Iterable<String> keys() {
		return repo.keySet();
	}

	/**
	 * <p>Test the key contains in the repository</p>
	 * @param key key to test
	 * @return true if contains
	 */
	public boolean contains(final String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key can't be null or empty"); 
		}
		else {
			return repo.containsKey(key); 
		}
	}
	
	/**
	 * <p>Get multilanguage string by associated key</p>
	 * @param key key associated with the multilanguage string. Need be unique
	 * @return value found
	 */
	public MultilangString get(final String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key can't be null or empty"); 
		}
		else if (!contains(key)) {
			throw new IllegalArgumentException("Key ["+key+"] is missing in the repo"); 
		}
		else {
			return repo.get(key); 
		}
	}
	
	/**
	 * <p>Add new multilanguale string to the repository</p>
	 * @param key unique key associated
	 * @param item multilanguage string to add
	 * @return self
	 */
	public MultilangStringRepo add(final String key, final MultilangString item) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key can't be null or empty"); 
		}
		else if (item == null) {
			throw new IllegalArgumentException("item can't be null"); 
		}
		else {
			repo.put(key,item);
			return this;
		}
	}

	/**
	 * <p>Join two repositories</p>
	 * @param repo repository to add to the given one
	 * @return self
	 */
	public MultilangStringRepo add(final MultilangStringRepo repo) {
		if (repo == null) {
			throw new IllegalArgumentException("Repo to add can't be null"); 
		}
		else {
			this.repo.putAll(repo.repo);
			return this;
		}
	}
	
	/**
	 * <p>Remove multilanguage string associated with the key</p>
	 * @param key key associated with the string
	 * @return self
	 */
	public MultilangString remove(final String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key can't be null or empty"); 
		}
		else if (!contains(key)) {
			throw new IllegalArgumentException("Key ["+key+"] is missing in the repo"); 
		}
		else {
			return repo.remove(key); 
		}
	}

	/**
	 * <p>Substitute string with the multilanguage string context. Source string can contains:</p>
	 * <p><code>TEXT1${KEY1}TEXT2${KEY2}TEXT3</code></p>
	 * <p>All keys containing in the repo will be replaces with it's localized values. If the given key is missing, 
	 * substitution retains <code>${KEYN}</code> mark unchanged. It allows to use <i>chained</i> substitutions</p> 
	 * @param source string to substitute data to. Can't be null
	 * @return substituted string
	 * @see #substitute(String,String)
	 * @throws ContentException when substitution format corrupted
	 */
	public String substitute(final String source) throws ContentException {
		if (source == null) {
			throw new IllegalArgumentException("String can't be null");
		}
		else {
			return substitute(source,"");
		}
	}

	/**
	 * <p>Substitute string (or default value if source string is null) with the multilanguage string context.</p> 
	 * @param source string to substitute data to
	 * @param defaultValue default value for the source string if null. Can't be null
	 * @return substituted string
	 * @see #substitute(String)
	 */
	public String substitute(final String source, final String defaultValue) throws ContentException {
		if (defaultValue == null) {
			throw new IllegalArgumentException("String default value can't be null");
		}
		else if (source == null) {
			return substitute(defaultValue,defaultValue);
		}
		else if (source.indexOf("${") < 0) {
			return source;
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			String				key;
			int					from = 0, to, closed;
			
			while ((to = source.indexOf("${",from)) >= 0) {
				sb.append(source.substring(from,to));
				closed = source.indexOf('}',to+2);
				
				if (closed > to) {
					key = source.substring(to+2,closed);
					if (contains(key)) {
						sb.append(get(key).get());
					}
					else {
						sb.append(source.substring(to+1,closed));
					}
					from = closed + 1;
				}
				else {
					throw new IllegalArgumentException("Substituted format ["+source+"] illegal - '}' is missing");
				}
			}
			
			return sb.append(source.substring(from)).toString();
		}
	}
	
	@Override
	public String toString() {
		return "MultilangStringRepo [repo=" + repo + "]";
	}
}
