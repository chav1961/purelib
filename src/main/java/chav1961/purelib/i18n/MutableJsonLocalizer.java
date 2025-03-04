package chav1961.purelib.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SequenceIterator;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.LocalizedString;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.MutableLocalizedString;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;


/**
 * <p>This class is a mutable collection of key/value pairs for different languages. Resource for this class is any valid URL reference,
 * containing valid JSON. JSON format is:</p>
 * <code>
 * [<br>
 * { "lang" : "&lt;lang_id&gt;,<br>
 *   "keys" : [<br>
 *   	{"key" : "&lt;key_name&gt;", "value" : "key_value"}, ...<br>
 *   ]<br>
 * }, ...<br>
 * ]<br>
 * </code>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.7
 */
public class MutableJsonLocalizer extends AbstractLocalizer {
	private static final String			SUBSCHEME = "json";
	private static final URI			SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+SUBSCHEME+":/");
	private static final String			MUTABLE_SUBSCHEME = "mutablejson";
	private static final URI			MUTABLE_SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+MUTABLE_SUBSCHEME+":/");
	
	private static final String			F_LANG = "lang";
	private static final String			F_KEYS = "keys";
	private static final String			F_KEY = "key";
	private static final String			F_VALUE = "value";
	
	public interface LocalizerTableModel extends TableModel {
		int insert();
		int duplicate(int sourceRow);
		void delete(int row);
		void commit() throws ContentException;
	}
	
	private final URI					resourceAddress;
	private final Map<String,KeyCollection>	localKeys = new HashMap<>();
	private final boolean				isReadOnly;
	private final String				localizerURI;
	private KeyCollection				currentCollection;
	
	/**
	 * <p>Constructor of the class</p>
	 * @throws LocalizationException inherited from parent
	 * @throws NullPointerException inherited from parent
	 */
	public MutableJsonLocalizer() throws LocalizationException, NullPointerException {
		this.resourceAddress = null;
		this.isReadOnly = true;
		this.localizerURI = "unknown:/";
	}

	protected MutableJsonLocalizer(final URI resourceAddress) throws LocalizationException, NullPointerException {
		this(resourceAddress,PureLibSettings.CURRENT_LOGGER);
	}
	
	protected MutableJsonLocalizer(final URI resourceAddress, final LoggerFacade facade) throws LocalizationException, NullPointerException {
		if (resourceAddress == null) {
			throw new NullPointerException("Resource address can't be null");
		}
		else if (facade == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			this.resourceAddress = resourceAddress;
			this.localizerURI = resourceAddress.toString();
			this.isReadOnly = !URIUtils.canServeURI(resourceAddress, MUTABLE_SERVE);
			
			try(final LoggerFacade	trans = facade.transaction(this.getClass().getName())) {
				if (resourceAddress.getScheme() != null) {
					try{final URL 	url = resourceAddress.toURL();
					
						try(final InputStream	is = url.openStream()) {
							if (is == null) {
								throw new ContentException("JSON localizer error: URL ["+url+"] is not exists or it's content not acsessible"); 
							}
							else {
								loadJson(is, trans, localKeys);
							}
						}
					} catch (ContentException | IOException e) {
						throw new LocalizationException(e.getLocalizedMessage(),e);
					}
				}
				else {
					final String	resourcePath = resourceAddress.getPath();
					final File		resource = new File(resourcePath);
					
					if (resource.exists() && resource.isFile()) {
						try(final InputStream	is = new FileInputStream(resourceAddress.getPath())) {
							
							loadJson(is, trans, localKeys);
						} catch (ContentException | IOException e) {
							throw new LocalizationException(e.getLocalizedMessage(),e);
						}
					}
					else {
						final URL	possibleLocalResource = Thread.currentThread().getContextClassLoader().getResource(resourcePath.replace('\\','/'));

						if (possibleLocalResource != null) {
							try(final InputStream	is = possibleLocalResource.openStream()) {
								if (is == null) {
									throw new ContentException("JSON localizer error: URL ["+possibleLocalResource+"] is not exists or it's content not acsessible"); 
								}
								else {
									loadJson(is, trans, localKeys);
								}
							} catch (ContentException | IOException e) {
								throw new LocalizationException(e.getLocalizedMessage(),e);
							}
						}
						else {
							throw new LocalizationException("Resource name ["+resourcePath+"] not found anywhere");
						}
					}
				}
				loadResource(currentLocale().getLocale());
				trans.rollback();
			}
		}
	}

	@Override
	public URI getLocalizerId() {
		return URI.create(localizerURI);
	}

	@Override
	public String getSubscheme() {
		return isReadOnly ? SUBSCHEME : MUTABLE_SUBSCHEME;
	}
	
	@Override
	public boolean canServe(final URI localizer) throws NullPointerException {
		return URIUtils.canServeURI(localizer, SERVE) || URIUtils.canServeURI(localizer, MUTABLE_SERVE); 
	}

	@Override
	public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (URIUtils.canServeURI(resource, SERVE)) {
			return new MutableJsonLocalizer(URIUtils.extractSubURI(resource,Localizer.LOCALIZER_SCHEME,SUBSCHEME));
		}
		else {
			return new MutableJsonLocalizer(URIUtils.extractSubURI(resource,Localizer.LOCALIZER_SCHEME,MUTABLE_SUBSCHEME));
		}
	}

	@Override
	public Iterable<String> localKeys() {
		return SequenceIterator.iterable(currentCollection.keysIterator(),currentCollection.helpsIterator());
	}

	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty"); 
		}
		else if (currentCollection.containsHelp(key)) {
			return "uri("+currentCollection.getHelpURI(key)+")";
		}
		else {
			return null;
		}
	}

	@Override
	public String getLocalValue(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty"); 
		}
		else if (locale == null) {
			throw new NullPointerException("Locale can't be null"); 
		}
		else {
			final String	lang = locale.getLanguage();
			
			if (localKeys.containsKey(lang) && localKeys.get(lang).containsHelp(key)) {
				return "uri("+localKeys.get(lang).getHelpURI(key)+")";
			}
			else {
				return localKeys.get(lang).getValue(key);
			}
		}
	}
	
	public TableModel getTableModel(final LoggerFacade facade) {
		if (isReadOnly) {
			throw new IllegalStateException("Attempt to get table model for read-only localizer"); 
		}
		else if (facade == null) {
			throw new NullPointerException("Logger to get table model can't be null"); 
		}
		else {
			return null;
		}
	}	

	public MutableLocalizedString createLocalValue(final String key) {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Key to add can't be null or empty"); 
		}
		else {
			for (String item : localKeys()) {
				if (key.equals(item)) {
					throw new IllegalArgumentException("Duplicate key ["+key+"] to create local value"); 
				}
			}
			for (Entry<String, KeyCollection> item : localKeys.entrySet()) {
				item.getValue().addValue(key, item.getKey());
			}
			
			return new MutableLocalizedString() {
				private String	currentKey = key;

				@Override
				public String toString() {
					return "MutableLocalizedString["+getId()+"="+getValue()+"]";
				}
				
				@Override
				public Localizer getLocalizer() {
					return MutableJsonLocalizer.this;
				}
				
				@Override
				public boolean isLanguageSupported(final Locale lang) throws LocalizationException {
					if (lang == null) {
						throw new NullPointerException("Locale can't be null"); 
					}
					else {
						return localKeys.containsKey(lang.getLanguage());
					}
				}
				
				@Override
				public String getValueOrDefault(final Locale lang) throws LocalizationException {
					if (lang == null) {
						throw new NullPointerException("Locale can't be null"); 
					}
					else if (isLanguageSupported(lang)) {
						return getValue(lang);
					}
					else {
						return getId();
					}
				}
				
				@Override
				public String getValue(final Locale lang) throws LocalizationException {
					if (lang == null) {
						throw new NullPointerException("Locale can't be null"); 
					}
					else {
						return getLocalValue(getId(), lang);
					}
				}
				
				@Override
				public String getValue() throws LocalizationException {
					return getValue(currentLocale().getLocale());
				}
				
				@Override
				public String getId() throws LocalizationException {
					return currentKey;
				}
				
				@Override
				public void setValue(final Locale lang, final String value) throws LocalizationException {
					if (lang == null) {
						throw new NullPointerException("Locale can't be null"); 
					}
					else if (Utils.checkEmptyOrNullString(value)) {
						throw new IllegalArgumentException("Value to set can't be null or empty"); 
					}
					else if (!isLanguageSupported(lang)) {
						throw new IllegalStateException("Locale ["+lang.getLanguage()+"] is not defined for key ["+getId()+"]. Call addValue(...) instead"); 
					}
					else {
						localKeys.get(lang.getLanguage()).setValue(getId(), value);
					}
				}
				
				@Override
				public void setId(final String id) throws LocalizationException {
					if (Utils.checkEmptyOrNullString(key)) {
						throw new IllegalArgumentException("Id to set can't be null or empty");
					}
					else {
						for (String item : localKeys()) {
							if (key.equals(item)) {
								throw new IllegalArgumentException("Duplicate key ["+key+"] to change"); 
							}
						}
						for (int index = 0; index < SupportedLanguages.values().length; index++) {
							final String	lang = SupportedLanguages.values()[index].getLocale().getLanguage();
							final String	val = localKeys.get(lang).getValue(getId());
							
							localKeys.get(lang).removeKey(getId());
							localKeys.get(lang).addValue(id, val);
						}
						currentKey = id;
					}
					
				}
				
				@Override
				public void removeValue(final Locale lang) throws LocalizationException {
					if (lang == null) {
						throw new NullPointerException("Locale can't be null"); 
					}
					else if (!isLanguageSupported(lang)) {
						throw new IllegalStateException("Locale ["+lang.getLanguage()+"] is not defined already for key ["+getId()+"]."); 
					}
					else {
						localKeys.get(lang.getLanguage()).removeKey(getId());
					}
				}
				
				@Override
				public void addValue(final Locale lang, final String value) throws LocalizationException {
					if (lang == null) {
						throw new NullPointerException("Locale can't be null"); 
					}
					else if (value == null) {
						throw new NullPointerException("Value to set can't be null or empty"); 
					}
					else if (isLanguageSupported(lang)) {
						throw new IllegalStateException("Locale ["+lang.getLanguage()+"] is already supporetd for key ["+getId()+"]. Call setValue(...) instead"); 
					}
					else {
						
					}
				}
				
				@Override
				public Object clone() throws CloneNotSupportedException {
					return super.clone();
				}
			};
		}
	}

	public void removeLocalValue(final String key) {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Key to remove can't be null or empty"); 
		}
		else {
			for (Entry<String, KeyCollection> item : localKeys.entrySet()) {
				item.getValue().removeKey(key);
			}
		}
	}

	public void clear() {
		localKeys.clear();
	}
	
	public void saveContent(final JsonStaxPrinter printer) throws IOException, PrintingException {
		if (printer == null) {
			throw new NullPointerException("Printer to cave content to can't be null");
		}
		else {
			printer.startArray();
				boolean	theSameFirstLang = true;
				
				for (Entry<String, KeyCollection> entity : localKeys.entrySet()) {
					final KeyCollection	kc = entity.getValue();
					
					if (!theSameFirstLang) {
						printer.splitter();
					}
					theSameFirstLang = false;
					printer.startObject();
						boolean	theSameFirstKey = true;
					
						printer.name(F_LANG).value(entity.getKey()).splitter().name(F_KEYS);
						printer.startArray();
							for (String key : kc.keys()) {
								if (!theSameFirstKey) {
									printer.splitter();
								}
								theSameFirstKey = false;
								printer.startObject();
									printer.name(F_KEY).value(key).splitter().name(F_VALUE).value(kc.getValue(key));
								printer.endObject();
							}
						printer.endArray();
					printer.endObject();
				}
			printer.endArray();
		}
	}
	
	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
		if (newLocale == null) {
			throw new NullPointerException("New locale can't be null"); 
		}
		else if (localKeys.containsKey(newLocale.getLanguage())) {
			currentCollection = localKeys.get(newLocale.getLanguage());
		}
		else {
			throw new LocalizationException("Language ["+newLocale.getLanguage()+"] is not supported for localizer ["+resourceAddress+"]");
		}
	}

	@Override
	protected String getHelp(String helpId, final Locale locale, String encoding) throws LocalizationException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(helpId)) {
			throw new IllegalArgumentException("Help id to get value for can't be null or empty"); 
		}
		else {
			try{
				if (resourceAddress.getScheme() != null) {
					return new String(URIUtils.loadCharsFromURI(URIUtils.appendRelativePath2URI(resourceAddress,"../help/"+locale.getLanguage()+"/"+helpId),encoding));
				}
				else {
					return new String(URIUtils.loadCharsFromURI(URIUtils.appendRelativePath2URI(new File(resourceAddress.getPath()).getAbsoluteFile().toURI(),"../help/"+locale.getLanguage()+"/"+helpId),encoding));
				}
			} catch (IOException e) {
				throw new LocalizationException(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	protected boolean isLocaleSupported(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(key)) {
			throw new IllegalArgumentException("Help id to get value for can't be null or empty"); 
		}
		else {
			return localKeys.containsKey(locale.getLanguage()) && localKeys.get(locale.getLanguage()).containsKey(key);
		}
	}
	
	@Override
	protected LocalizedString buildLocalizedString(final String key) {
		return new MutableLocalizedString() {
				String	currentKey = key;
			
				@Override
				public String toString() {
					return "MutableLocalizedString["+getId()+"="+getValue()+"]";
				}
				
				@Override
				public Localizer getLocalizer() {
					return MutableJsonLocalizer.this;
				}
				
				@Override
				public boolean isLanguageSupported(final Locale lang) throws LocalizationException {
					return isLocaleSupported(getId(), lang);
				}
				
				@Override
				public String getValueOrDefault(Locale lang) throws LocalizationException {
					if (localKeys.containsKey(lang.getLanguage())) {
						return getLocalValue(getId(), lang);
					}
					else {
						return getLocalValue(getId()); 
					}
				}
				
				@Override
				public String getValue(final Locale lang) throws LocalizationException {
					return substitute(getId(), getLocalValue(getId(), lang), lang);
				}
				
				@Override
				public String getValue() throws LocalizationException {
					return getValue(currentLocale().getLocale());
				}
				
				@Override
				public String getId() throws LocalizationException {
					return currentKey;
				}

				@Override
				public void setId(final String id) throws LocalizationException {
					for (SupportedLanguages lang : SupportedLanguages.values()) {
						if (localKeys.containsKey(lang.getLocale().getLanguage())) {
							if (localKeys.get(lang.getLocale().getLanguage()).containsKey(id)) {
								throw new LocalizationException("Key id to set ["+id+"] already exists in the localizer"); 
							}
						}
					}

					for (SupportedLanguages lang : SupportedLanguages.values()) {
						if (localKeys.containsKey(lang.getLocale().getLanguage())) {
							localKeys.get(lang.getLocale().getLanguage()).replaceKey(getId(), id);
						}
					}
					currentKey = id;
				}
				
				@Override
				public void setValue(final Locale lang, final String value) throws LocalizationException {
					if (localKeys.containsKey(lang.getLanguage())) {
						localKeys.get(lang.getLanguage()).setValue(getId(), value);
					}
				}
				
				@Override
				public void removeValue(Locale lang) throws LocalizationException {
					if (localKeys.containsKey(lang.getLanguage())) {
						localKeys.get(lang.getLanguage()).removeKey(getId());
					}
				}
				
				@Override
				public void addValue(final Locale lang, final String value) throws LocalizationException {
					if (localKeys.containsKey(lang.getLanguage())) {
						localKeys.get(lang.getLanguage()).addValue(getId(), value);
					}
				}
				
				@Override
				public Object clone() throws CloneNotSupportedException {
					return super.clone();
				}
			};
	}	
	
	private static void loadJson(final InputStream is, final LoggerFacade trans, final Map<String,KeyCollection> keys) throws SyntaxException, IOException {
		try(final Reader			rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
			String			name = null;
			String			lang = null;
			String			keyName = null;
			String			keyValue = null;
			Map<String, String> localKeys = null;;
			Map<String, URI> localHelps = null;
			int				depth = 0;

			for (JsonStaxParserLexType item : parser) {
				switch (item) {
					case END_ARRAY		:
						switch (depth) {
							case 1 :
								depth--;
								break;
							case 3 :
								keys.put(lang, new KeyCollection(localKeys, localHelps));
								depth--;
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "End array is not supported here");
						}
						break;
					case END_OBJECT		:
						switch (depth) {
							case 2 :
								depth--;
								break;
							case 4 :
								localKeys.put(keyName, keyValue);
								depth--;
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "End object is not supported here");
						}
						break;
					case NAME			:
						switch (depth) {
							case 2 :
								switch (parser.name()) {
									case F_LANG : case F_KEYS :
										name = parser.name(); 
										break;
									default :
										throw new SyntaxException(parser.row(), parser.col(), "Name ["+parser.name()+"] is not supported here");
								}
								break;
							case 4 :
								switch (parser.name()) {
									case F_KEY : case F_VALUE :
										name = parser.name(); 
										break;
									default :
										throw new SyntaxException(parser.row(), parser.col(), "Name ["+parser.name()+"] is not supported here");
								}
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "End object is not supported here");
						}
						break;
					case NAME_SPLITTER : case LIST_SPLITTER :
						break;
					case ERROR	:
						throw new SyntaxException(parser.row(), parser.col(), parser.getLastError().getLocalizedMessage(), parser.getLastError());
					case BOOLEAN_VALUE : case INTEGER_VALUE : case NULL_VALUE : case REAL_VALUE :
						throw new SyntaxException(parser.row(), parser.col(), "This value type is not supported");
					case START_ARRAY	:
						switch (depth) {
							case 0 :
								depth++;
								break;
							case 2 :
								localKeys = new HashMap<>();
								localHelps = new HashMap<>();
								depth++;
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "Start array is not supported here");
						}
						break;
					case START_OBJECT	:
						switch (depth) {
							case 1 :
								depth++;
								break;
							case 3 :
								depth++;
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "Start object is not supported here");
						}
						break;
					case STRING_VALUE	:
						switch (depth) {
							case 2 :
								switch (name) {
									case F_LANG :
										lang = parser.stringValue(); 
										break;
									case F_KEYS :
										break;
									default :
										throw new SyntaxException(parser.row(), parser.col(), "Name ["+parser.name()+"] is not supported here");
								}
								break;
							case 4 :
								switch (name) {
									case F_KEY		:
										keyName = parser.stringValue(); 
										break;
									case F_VALUE	:
										keyValue = parser.stringValue(); 
										break;
									default :
										throw new SyntaxException(parser.row(), parser.col(), "Name ["+parser.name()+"] is not supported here");
								}
								break;
							default :
								throw new SyntaxException(parser.row(), parser.col(), "End object is not supported here");
						}
						break;
					default:
						throw new UnsupportedOperationException("Lexema type ["+parser.current()+"] is not supported yet");
				}
			}
		}
	}

	private class InnerTableModel implements LocalizerTableModel {
		private final LightWeightListenerList<TableModelListener>	listeners = new LightWeightListenerList<>(TableModelListener.class);
		
		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return true;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addTableModelListener(final TableModelListener l) {
			if (l == null) {
				throw new NullPointerException("Listener to add can't be null");
			}
			else {
				listeners.addListener(l);
			}
		}

		@Override
		public void removeTableModelListener(final TableModelListener l) {
			if (l == null) {
				throw new NullPointerException("Listener to remove can't be null");
			}
			else {
				listeners.removeListener(l);
			}
		}

		@Override
		public int insert() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int duplicate(final int sourceRow) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void delete(final int row) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void commit() throws ContentException {
			// TODO Auto-generated method stub
			
		}
		
	}
}
