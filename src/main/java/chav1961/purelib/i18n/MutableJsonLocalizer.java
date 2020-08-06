package chav1961.purelib.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SequenceIterator;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;


/**
 * <p>This class is a mutable collection of key/value pairs for different languages. Resource for this class is any valid URL reference,
 * containing valid JSON. JSON format is:</p>
 * <code>
 * [<br/>
 * { "lang" : "<lang_id>,<br/>
 *   "keys" : [<br/>
 *   	{"key" : "<key_name>", "value" : "key_value"}, ...<br/>
 *   ]<br/>
 * }, ...<br/>
 * ]<br/>
 * </code>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class MutableJsonLocalizer extends AbstractLocalizer {
	private static final String			SUBSCHEME = "json";
	private static final URI			SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+SUBSCHEME+":/");
	private static final String			MUTABLE_SUBSCHEME = "mutablejson";
	private static final URI			MUTABLE_SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+MUTABLE_SUBSCHEME+":/");
	
	public interface LocalizerTableModel extends TableModel {
		int insert();
		int duplicate(int sourceRow);
		void delete(int row);
		void commit() throws ContentException;
	}
	
	private final URI					resourceAddress;
	private final Map<String,KeyCollection>	keys = new HashMap<>();
	private final boolean				isReadOnly;
	private final JsonNode				root;
	private KeyCollection				currentCollection;
	private String						localizerURI = "unknown:/";
	
	/**
	 * <p>Constructor of the class</p>
	 * @throws LocalizationException inherited from parent
	 * @throws NullPointerException inherited from parent
	 */
	public MutableJsonLocalizer() throws LocalizationException, NullPointerException {
		this.resourceAddress = null;
		this.isReadOnly = true;
		this.root = new JsonNode(JsonNodeType.JsonObject);
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
								this.root = loadJson(is,trans);
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
							
							this.root = loadJson(is,trans);
						} catch (ContentException | IOException e) {
							throw new LocalizationException(e.getLocalizedMessage(),e);
						}
					}
					else {
						final URL	possibleLocalResource = Thread.currentThread().getContextClassLoader().getResource(resourcePath.replace('\\','/'));

						if (possibleLocalResource != null) {
							try(final InputStream	is = possibleLocalResource.openStream()) {
								if (is == null) {
									throw new ContentException("XML localizer error: URL ["+possibleLocalResource+"] is not exists or it's content not acsessible"); 
								}
								else {
									this.root = loadJson(is,trans);
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
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty"); 
		}
		else if (currentCollection.containsHelp(key)) {
			return "uri("+currentCollection.getHelpURI(key)+")";
		}
		else {
			return null;
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

	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
		if (newLocale == null) {
			throw new NullPointerException("New locale can't be null"); 
		}
		else if (keys.containsKey(newLocale.getLanguage())) {
			currentCollection = keys.get(newLocale.getLanguage());
		}
		else {
			throw new LocalizationException("Language ["+newLocale.getLanguage()+"] is not supported for localizer ["+resourceAddress+"]");
		}
	}

	@Override
	protected String getHelp(String helpId, String encoding) throws LocalizationException, IllegalArgumentException {
		// TODO Auto-generated method stub
		if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help id to get value for can't be null or empty"); 
		}
		else {
			try{return new String(URIUtils.loadCharsFromURI(URIUtils.appendRelativePath2URI(resourceAddress,"../help/"+currentLocale().getLanguage()+"/"+helpId),encoding));
			} catch (IOException e) {
				throw new LocalizationException(e.getLocalizedMessage(),e);
			}
		}
	}

	private static JsonNode loadJson(final InputStream is, final LoggerFacade trans) throws SyntaxException, IOException {
		try(final Reader			rdr = new InputStreamReader(is);
			final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
			
			return JsonUtils.loadJsonTree(parser);
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
