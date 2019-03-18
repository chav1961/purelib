package chav1961.purelib.i18n;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.streams.StreamsUtil;

/**
 * <p>This is an abstract class to implements any localizers. This class supports a lot of localizer's functionality described (see {@linkplain Localizer}). Current localization support 
 * in the class includes English and Russian localization content</p>
 * 
 * @see Localizer
 * @see chav1961.purelib.i18n JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 */

public abstract class AbstractLocalizer implements Localizer {
	public static final String						DEFAULT_CONTENT_ENCODING = "UTF-8";

	/**
	 * <p>This enumerations contains all locales currently supported</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	public enum SupportedLanguages {
		en, ru
	}

	/**
	 * <p>Yhjis interface is used to enumerate all locales supported</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface SupportedLocalesIterator {
		void process(final SupportedLanguages lang, final String langName, final Icon icon);
	}
	
	private static final Pattern					URI_PATTERN = Pattern.compile("uri\\((?<uri>.*)\\)");
	private static final LocaleDescriptor[]			LOCALES = new LocaleDescriptor[]{
															 new LocaleDescriptorImpl(new Locale.Builder().setLanguage(SupportedLanguages.en.name()).build(),SupportedLanguages.en,"English",new ImageIcon(AbstractLocalizer.class.getResource(SupportedLanguages.en.name()+".png")))
															,new LocaleDescriptorImpl(new Locale.Builder().setLanguage(SupportedLanguages.ru.name()).build(),SupportedLanguages.ru,"Russian",new ImageIcon(AbstractLocalizer.class.getResource(SupportedLanguages.ru.name()+".png")))
														};
	private static final Iterable<LocaleDescriptor>	LOCALE_ITERATOR = new Iterable<LocaleDescriptor>(){
															@Override
															public Iterator<LocaleDescriptor> iterator() {
																return new Iterator<LocaleDescriptor>(){
																	private int		index = 0;
																	
																	@Override 
																	public boolean hasNext() {
																		return index < LOCALES.length;
																	}
																	
																	@Override 
																	public LocaleDescriptor next() {
																		if (!hasNext()) {
																			throw new NoSuchElementException("Call next() wwhen hasNext() == false");
																		}
																		else {
																			return LOCALES[index++];
																		}
																	}
																};
															}
														};
	
	private final List<LocaleChangeListener>		listeners = new ArrayList<>();
	private LocaleDescriptor						currentDesc = new LocaleDescriptorImpl(Locale.getDefault(),SupportedLanguages.valueOf(Locale.getDefault().getLanguage()),"",new ImageIcon());
	private Localizer								parent = null;
	private LocalizerNode							node = new LocalizerNode(this);

	protected AbstractLocalizer() throws LocalizationException, NullPointerException {
	}

	@Override public abstract Iterable<String> localKeys();
	@Override public abstract String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException;
	protected abstract void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException;
	protected abstract String getHelp(final String helpId) throws LocalizationException, IllegalArgumentException;
	
	@Override
	public LocaleDescriptor currentLocale() {
		return currentDesc;
	}

	@Override
	public Localizer setCurrentLocale(final Locale newLocale) throws LocalizationException, NullPointerException {
		final Locale				oldLocale = currentDesc.getLocale();
		
		if (newLocale == null) {
			throw new NullPointerException("Locale to set can't be null");
		}
		else if (!oldLocale.getLanguage().equals(newLocale.getLanguage())) {
			for (LocaleDescriptor item : supportedLocales()) {
				if (item.getLocale().getLanguage().equals(newLocale.getLanguage())) {
					loadResource(newLocale);
					final Set<Localizer>	set = new HashSet<>();
					
					currentDesc = item;
					walkDown((current,depth)->{
						set.add(current);
						return ContinueMode.CONTINUE;
					});
					for (Localizer entity : set) {
						entity.setCurrentLocale(newLocale);
					}
					synchronized(listeners) {
						listeners.forEach((listener)->{
							try {listener.localeChanged(oldLocale, newLocale);
							} catch (LocalizationException e) {
							}
						});
					}
					return this;
				}
			}
			throw new LocalizationException("Locale to set ["+newLocale+"] is not supported in this localizer");
		}
		else {
			return this;
		}
	}

	@Override
	public Iterable<LocaleDescriptor> supportedLocales() {
		return LOCALE_ITERATOR;
	}

	@Override
	public boolean containsKey(final String key) throws IllegalArgumentException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to check can't be null or empty");
		}
		else {
			for (String item : availableKeys()) {
				if (item.equals(key)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public Iterable<String> availableKeys() {
		final Set<String>	result = new HashSet<>();
		
		try{walkUp((current,depth)->{
				for (String item : current.localKeys()) {
					result.add(item);
				}
				return ContinueMode.CONTINUE;
			});
		} catch (NullPointerException | LocalizationException e) {
			e.printStackTrace();
		}
		
		return result;
	}


	@Override
	public String getValue(final String key) throws LocalizationException, IllegalArgumentException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to check can't be null or empty");
		}
		else if (!containsKey(key)) {
			containsKey(key);
			throw new LocalizationException("Key ["+key+"] to get value is missing anywhere");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			walkUp((current,depth)->{
				for (String item : current.localKeys()) {
					if (item.equals(key)) {
						sb.append(substitute(key,current.getLocalValue(key)));
						return ContinueMode.STOP;
					}
				}
				return ContinueMode.CONTINUE;
			});
			String			result = sb.toString();
			final Matcher	m = URI_PATTERN.matcher(result);
		
			if (m.matches()) {
				final String		uriName = m.group("uri"); 
				final URI			uriRef = URI.create(uriName);		
				
				if (uriRef.getScheme() == null) {
					final String	temp = getHelp(uriRef.getPath()); 
					
					if (uriRef.getQuery() != null) {
						try{final Hashtable<String,String[]>	mimes = NanoServiceFactory.parseQuery(uriRef.getQuery());
							final MimeType		fromMime = mimes.containsKey("mime") ? PureLibSettings.MIME_PLAIN_TEXT : new MimeType(mimes.get("sourceMime")[0]);
							final MimeType 		toMime = mimes.containsKey("mime") ? new MimeType(mimes.get("mime")[0]) : new MimeType(mimes.get("targetMime")[0]);
						
							try(final StringWriter	wr = new StringWriter();
								final Writer		nested = StreamsUtil.getStreamClassForOutput(wr, fromMime, toMime)) {

								nested.write(temp);
								nested.flush();
								return wr.toString(); 
							} catch (IOException exc) {
								return temp; 
							}
						} catch (MimeTypeParseException exc) {
							return temp; 
						}
					}
					else {
						return temp; 
					}
				}
				else {
					try{return Utils.fromResource(uriRef.toURL());
					} catch (IOException e) {
						return "URL ["+uriRef.toString()+"]: I/O error ("+e.getLocalizedMessage()+")";
					}
				}
			}
			else {
				return result;
			}
		}
	}

	
	@Override
	public Reader getContent(final String key) throws LocalizationException, IllegalArgumentException {
		return getContent(key,PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_PLAIN_TEXT);
	}

	@Override
	public Reader getContent(final String key, final MimeType sourceType, final MimeType targetType) throws LocalizationException, IllegalArgumentException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get content for can't be null or empty"); 
		}
		else if (sourceType == null) {
			throw new NullPointerException("Source MIME type can't be null"); 
		}
		else if (targetType == null) {
			throw new NullPointerException("Target MIME type can't be null"); 
		}
		else {
			final String	value = getValue(key);
			
			if (sourceType.equals(targetType)) {
				return new StringReader(value);
			}
			else {
				try(final CharArrayWriter	wr = new CharArrayWriter();
					final Writer			conv = StreamsUtil.getStreamClassForOutput(wr,sourceType,targetType)) {
					
					conv.write(value);
					conv.flush();
					return new CharArrayReader(wr.toCharArray());
				} catch (IOException e) {
					throw new LocalizationException("I/O error converting string content from ["+sourceType+"] to ["+targetType+"]: "+e.getLocalizedMessage(),e);
				}
			}
		}
	}

	@Override
	public boolean containsLocalizerHere(final String localizerId) throws NullPointerException, IllegalArgumentException {
		if (localizerId == null || localizerId.isEmpty()) {
			throw new IllegalArgumentException("Localizer id can't be null or empty");
		}
		else {
			LocalizerNode	item = node;
			
			do {if (localizerId.equals(item.item.getLocalizerId())) {
					return true;
				}
				else {
					item = item.sibling;
				}
			} while (item != node);
			return false;
		}
	}

	@Override
	public boolean containsLocalizerAnywhere(final String localizerId) throws NullPointerException, IllegalArgumentException {
		if (localizerId == null || localizerId.isEmpty()) {
			throw new IllegalArgumentException("Localizer id can't be null or empty");
		}
		else {
			final boolean[]		result = new boolean[] {false};
			
			try{walkUp((current,depth)->{
					if (localizerId.equals(current.getLocalizerId())) {
						result[0] = true;
						return ContinueMode.STOP;
					}
					else {
						return ContinueMode.CONTINUE;
					}
				});
				if (!result[0]) {
					walkDown((current,depth)->{
						if (localizerId.equals(current.getLocalizerId())) {
							result[0] = true;
							return ContinueMode.STOP;
						}
						else {
							return ContinueMode.CONTINUE;
						}
					});	
				}
				return result[0];
			} catch (LocalizationException e) {
				return false;
			}
		}
	}

	@Override
	public Localizer getLocalizerById(final String localizerId) throws NullPointerException, IllegalArgumentException {
		if (localizerId == null || localizerId.isEmpty()) {
			throw new IllegalArgumentException("Localizer id can't be null or empty");
		}
		else {
			final Localizer[]	result = new Localizer[] {null};
			
			try{walkUp((current,depth)->{
					if (localizerId.equals(current.getLocalizerId())) {
						result[0] = current;
						return ContinueMode.STOP;
					}
					else {
						return ContinueMode.CONTINUE;
					}
				});
				if (result[0] == null) {
					walkDown((current,depth)->{
						if (localizerId.equals(current.getLocalizerId())) {
							result[0] = current;
							return ContinueMode.STOP;
						}
						else {
							return ContinueMode.CONTINUE;
						}
					});
				}
				return result[0];
			} catch (LocalizationException e) {
				return null;
			}
		}
	}
	
	@Override
	public Localizer add(final Localizer newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException {
		if (newLocalizer == null) {
			throw new NullPointerException("Localizer to add can't be null"); 
		}
		else if (newLocalizer == this) {
			throw new IllegalArgumentException("Attempt to add self to localizer chain"); 
		}
		else {
			walkUp((current,depth)->{
				for (String item : current.localKeys()) {
					for (String key : newLocalizer.localKeys()) {
						if (key.equals(item)) {
							throw new LocalizationException("Localizer to add contains duplicate key ["+item+"]");
						}
					}
				}
				return ContinueMode.SIBLINGS_ONLY;
			});
			final LocalizerNode		newNode = new LocalizerNode(newLocalizer);
			
			node.sibling = newNode;
			newNode.sibling = node;
			newLocalizer.setParent(getParent());
			newLocalizer.setCurrentLocale(currentLocale().getLocale());
			return this;
		}
	}

	@Override
	public Localizer remove(final Localizer localizer) throws LocalizationException, NullPointerException, IllegalStateException {
		if (localizer == null) {
			throw new NullPointerException("Localizer to remove can't be null"); 
		}
		else if (localizer == this) {
			throw new IllegalArgumentException("Attempt to remove self from localizer chain"); 
		}
		else {
			LocalizerNode	cursor = node;
			boolean			deleted = false;
			
loop:		do {if (cursor.child != null) {
					for (int index = 0; index < cursor.child.length; index++) {
						if (cursor.child[index].item == localizer) {
							if (cursor.child.length == 1) {
								cursor.child = null;
							}
							else {
								System.arraycopy(cursor.child,index+1,cursor.child,index,cursor.child.length-index-1);
								cursor.child = Arrays.copyOf(cursor.child,cursor.child.length-1);
							}
							deleted = true;
							break loop;
						}
					}				
				}
				if (cursor.sibling.item == localizer) {
					cursor.sibling.item = null;
					cursor.sibling = cursor.sibling.sibling;
					deleted = true;
					break;
				}
				cursor = cursor.sibling;
			} while (cursor != node);
			
			if (!deleted) {
				throw new IllegalStateException("Localizer to remove not found in the localizer chain"); 
			}
			else {
				localizer.setParent(null);
				return this;
			}
		}
	}

	@Override
	public Localizer push(final Localizer newLocalizer) throws LocalizationException, NullPointerException, IllegalArgumentException {
		if (newLocalizer == null) {
			throw new NullPointerException("Localizer to add can't be null"); 
		}
		else if (newLocalizer == this) {
			throw new IllegalArgumentException("Attempt to push self to localizer chain"); 
		}
		else if (node.child != null) {
			node.child = Arrays.copyOf(node.child,node.child.length+1);
			node.child[node.child.length-1] = new LocalizerNode(newLocalizer);
			newLocalizer.setParent(this);
			return this;
		} 
		else {
			final LocalizerNode		newNode = new LocalizerNode(newLocalizer);
			
			node.child = new LocalizerNode[]{newNode};
			newLocalizer.setParent(this);
			newLocalizer.setCurrentLocale(currentLocale().getLocale());
			return this;
		}
	}

	@Override
	public Localizer pop() throws LocalizationException {
		if (node.child != null) {
			for (LocalizerNode item : node.child) {
				item.item.close();
				item.item.setParent(null);
			}
			node.child = null;
		}
		return this;
	}

	@Override
	public Localizer getParent() {
		return parent;
	}

	@Override
	public Localizer setParent(Localizer parent) throws LocalizationException {
		if ((this.parent = parent) != null) {
			final Set<Localizer>	path = new HashSet<>();
			Localizer				current = this;
			
			do {if (path.contains(current)) {
					throw new LocalizationException("Recursive dependencies detected in the path from 'this' to the root"); 
				}
				else {
					path.add(current);
				}
				current = current.getParent();
			} while (current != null);
			path.clear();
		}
		return this;
	}
	
	@Override
	public Localizer addLocaleChangeListener(final LocaleChangeListener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			synchronized(listeners) {
				listeners.add(listener);
			}
			return this;
		}
	}

	@Override
	public Localizer removeLocaleChangeListener(final LocaleChangeListener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			synchronized(listeners) {
				listeners.remove(listener);
			}
			return this;
		}
	}

	@Override
	public ContinueMode walkUp(final LocaleWalking processor) throws NullPointerException, LocalizationException {
		if (processor == null) {
			throw new NullPointerException("Walking processor can't be null");
		}
		else {
			LocalizerNode	cursor = node;
			ContinueMode	mode;
			
loop:		do {mode = processor.process(cursor.item,0);
				switch (mode) {
					case CONTINUE		:
					case SKIP_CHILDREN	: 
					case SIBLINGS_ONLY	:
						break;
					case SKIP_SIBLINGS		: 
					case PARENT_ONLY	:
						break loop;
					case SKIP_PARENT	:
						break loop;
					case STOP			:
						break loop;
					default : throw new UnsupportedOperationException("Continue mode ["+mode+"] is not supported yet"); 
				}
				cursor = cursor.sibling;
			} while (cursor != node);
			
			switch (mode) {
				case SIBLINGS_ONLY	:
				case SKIP_PARENT	:
				case STOP			:
					break;
				case SKIP_CHILDREN	: 
				case CONTINUE		:
				case SKIP_SIBLINGS		: 
				case PARENT_ONLY	:
					final Localizer	ref = getParent();
					
					if (ref != null) {
						ref.walkUp(processor);
					}
					break;
				default : throw new UnsupportedOperationException("Continue mode ["+mode+"] is not supported yet"); 
			}
			return mode;
		}
	}

	@Override
	public ContinueMode walkDown(final LocaleWalking processor) throws NullPointerException, LocalizationException {
		if (processor == null) {
			throw new NullPointerException("Walking processor can't be null");
		}
		else {
			LocalizerNode	cursor = node;
			ContinueMode	mode;
			
loop:		do {mode = processor.process(cursor.item,0);
sw:				for(;;) {
					switch (mode) {
						case CONTINUE		:
							if (cursor.child != null) {
								for (LocalizerNode child : cursor.child) {
									if ((mode = child.item.walkDown(processor)) != ContinueMode.CONTINUE) {
										break;
									}
								}
							}
						case SIBLINGS_ONLY	:
						case SKIP_CHILDREN	: 
							break sw;
						case SKIP_SIBLINGS		: 
						case PARENT_ONLY	:
						case SKIP_PARENT	:
							break loop;
						case STOP			:
							break loop;
						default : throw new UnsupportedOperationException("Continue mode ["+mode+"] is not supported yet"); 
					}
				}
				cursor = cursor.sibling;
			} while (cursor != node);
			
			switch (mode) {
				case CONTINUE		:
				case SKIP_PARENT	:
					break;
				case SKIP_CHILDREN	: 
				case SIBLINGS_ONLY	:
					break;
				case STOP			:
					break;
				case SKIP_SIBLINGS		: 
				case PARENT_ONLY	:
					break;
				default : throw new UnsupportedOperationException("Continue mode ["+mode+"] is not supported yet"); 
			}
			return mode;
		}
	}
	
	@Override
	public void close() throws LocalizationException {
		synchronized(listeners) {
			listeners.clear();
		}
	}

	/**
	 * <p>Enumerate all locales supported</p>
	 * @param iterator iterator to process all locale supported
	 * @throws NullPointerException
	 * @since 0.0.3
	 */
	public static void enumerateLocales(final SupportedLocalesIterator iterator) throws NullPointerException {
		if (iterator == null) {
			throw new NullPointerException("Iterator can't be null");
		}
		else {
			for (LocaleDescriptor item : LOCALES) {
				iterator.process(SupportedLanguages.valueOf(item.getLanguage()),item.getDescription(),item.getIcon());
			}
		}
	}	
	
	protected String substitute(final String key, final String localValue) {
		return CharUtils.substitute(key,localValue,(key2substitute)->{
			try{return getValue(key2substitute);
			} catch (LocalizationException | IllegalArgumentException exc) {
				return "Error getting value for ["+key2substitute+"] key : "+exc.getLocalizedMessage();
			}
		});
	}

	private static class LocaleDescriptorImpl implements LocaleDescriptor {
		final Locale				locale;
		final SupportedLanguages	lang;
		final String				description;
		final ImageIcon				icon;
		
		public LocaleDescriptorImpl(final Locale locale, final SupportedLanguages lang, final String description, final ImageIcon icon) {
			this.locale = locale;
			this.lang = lang;
			this.description = description;
			this.icon = icon;
		}

		@Override public Locale getLocale() {return locale;}
		@Override public String getLanguage() {return lang.name();}
		@Override public String getDescription() {return description;}
		@Override public ImageIcon getIcon() {return icon;}
	}
	
	private static class LocalizerNode {
		Localizer		item;
		LocalizerNode	sibling = this;
		LocalizerNode[]	child = null;
		
		public LocalizerNode(Localizer item) {
			this.item = item;
		}		
	}
}
