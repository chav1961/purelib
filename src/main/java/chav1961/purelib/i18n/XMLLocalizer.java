package chav1961.purelib.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SequenceIterator;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.XMLUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class is a wrapper to standard Java {@linkplain ResourceBundle} class. This class is an Java SPI service and available thru 
 * {@linkplain LocalizerFactory}. URI scheme-specific part for it must be:</p>
 * <code>{@value #SUBSCHEME}:/valid_path_to_xml_content</code>
 * <p>XML format for the localizer is validating by XSD scheme XMLLocalizerContext.xsd (see {@linkplain XSDCollection#XMLLocalizerContent}). 
 * Example of using XML format see localization files inside the Pure library (/chav1961/purelib/i18n/localization.xml)</p>
 *   
 * @see Localizer
 * @see chav1961.purelib.fsys
 * @see chav1961.purelib.i18n JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class XMLLocalizer extends AbstractLocalizer {
	private static final String			SUBSCHEME = "xml";
	private static final URI			SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+SUBSCHEME+":/");
	
	private final URI					resourceAddress;
	private final Map<String,KeyCollection>	keys = new HashMap<>();
	private KeyCollection				currentCollection;
	private String						localizerURI = "unknown:/";
	
	public XMLLocalizer() throws LocalizationException, NullPointerException {
		this.resourceAddress = null;
	}

	protected XMLLocalizer(final URI resourceAddress) throws LocalizationException, NullPointerException {
		this(resourceAddress,PureLibSettings.CURRENT_LOGGER);
	}
	
	protected XMLLocalizer(final URI resourceAddress, final LoggerFacade facade) throws LocalizationException, NullPointerException {
		if (resourceAddress == null) {
			throw new NullPointerException("Resource address can't be null");
		}
		else if (facade == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			this.resourceAddress = resourceAddress;
			this.localizerURI = resourceAddress.toString();
			
			try(final LoggerFacade	trans = facade.transaction(this.getClass().getName())) {
				if (resourceAddress.getScheme() != null) {
					try{final URL 	url = resourceAddress.toURL();
					
						try(final InputStream	is = url.openStream()) {
							if (is == null) {
								throw new ContentException("XML localizer error: URL ["+url+"] is not exists or it's content not acsessible"); 
							}
							else {
								loadDom(is,trans);
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
							loadDom(is,trans);
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
									loadDom(is,trans);
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
		return URIUtils.canServeURI(localizer, SERVE); 
	}

	@Override
	public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return new XMLLocalizer(URIUtils.extractSubURI(resource,Localizer.LOCALIZER_SCHEME,SUBSCHEME));
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
		else if (currentCollection.containsKey(key)) {
			return currentCollection.getValue(key);
		}
		else if (currentCollection.containsHelp(key)) {
			return "uri("+currentCollection.getHelpURI(key)+")";
		}
		else {
			return null;
		}
	}

	@Override
	public String getLocalValue(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException, NullPointerException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty"); 
		}
		else if (locale == null) {
			throw new NullPointerException("Locale can't be null"); 
		}
		else {
			final KeyCollection	kc = keys.get(locale.getLanguage());
			
			if (kc != null) {
				if (kc.containsKey(key)) {
					return kc.getValue(key);
				}
				else if (kc.containsHelp(key)) {
					return "uri("+kc.getHelpURI(key)+")";
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
			
		}
	}
	
	@Override
	public String toString() {
		return "XMLLocalizer [resourceAddress=" + resourceAddress + ", localizerURI=" + localizerURI + ", currentLocale=" + currentLocale().getLanguage() + "]";
	}

	@Override
	protected String getHelp(final String helpId, final Locale locale, final String encoding) throws LocalizationException, IllegalArgumentException {
		if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help id to get value for can't be null or empty"); 
		}
		else {
			try{return new String(URIUtils.loadCharsFromURI(URIUtils.appendRelativePath2URI(resourceAddress,"../help/"+locale.getLanguage()+"/"+helpId),encoding));
			} catch (IOException e) {
				throw new LocalizationException(e.getLocalizedMessage(),e);
			}
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

	private void loadDom(final InputStream is, final LoggerFacade logger) throws ContentException {
		final Map<String,String>	keysAndValues = new HashMap<>();
		final Map<String,URI>		helpRefs = new HashMap<>();
		final String[]				langName = new String[1];
		
		try(final InputStream	xsd = XSDConst.getResourceAsStream("XMLLocalizerContent.xsd")) {
			XMLUtils.walkDownXML(XMLUtils.validateAndLoadXML(is,xsd,logger).getDocumentElement(), (mode,node)->{
				switch (mode) {
					case ENTER	:
						switch (node.getNodeName()) {
							case "lang"	:
								langName[0] = node.getAttributes().getNamedItem("name").getTextContent();
								keysAndValues.clear();
								helpRefs.clear();
								break;
							case "key"	:
								keysAndValues.put(node.getAttributes().getNamedItem("name").getTextContent(),node.getTextContent());
								break;
							case "ref"	:
								final String		name = node.getAttributes().getNamedItem("name").getTextContent();
								final String		ref = node.getAttributes().getNamedItem("ref").getTextContent();
					
								helpRefs.put(name,URI.create(ref));
								break;
						}
						break;
					case EXIT	:
						switch (node.getNodeName()) {
							case "lang"	:
								keys.put(langName[0],new KeyCollection(keysAndValues,helpRefs));
								langName[0] = null;
								break;
						}
						break;
					default		:
						throw new UnsupportedOperationException("Mode ["+mode+"] is not supported yet");
				}
				return ContinueMode.CONTINUE;
			});
		} catch (IOException e) {
			throw new ContentException("Localizer ["+resourceAddress+"] - XSD scheme close error: "+e.getLocalizedMessage()); 
		}
		
		final Set<String>	totalKeys = new HashSet<>();	// Check key definitions in all languages
		final StringBuilder	sb = new StringBuilder();
		boolean				wereProblems = false;
		
		for (Entry<String, KeyCollection> item : keys.entrySet()) {
			for (String key : item.getValue().keys()) {
				totalKeys.add(key);
			}
			for (String key : item.getValue().helps()) {
				totalKeys.add(key);
			}
		}
		
		for (Entry<String, KeyCollection> item : keys.entrySet()) {
			final Set<String>	currentKeys = new HashSet<>();
			
			currentKeys.addAll(totalKeys);
			for (String key : item.getValue().keys()) {
				currentKeys.remove(key);
			}
			for (String key : item.getValue().helps()) {
				currentKeys.remove(key);
			}
			if (currentKeys.size() > 0) {
				wereProblems = true;
				sb.append("Lang [").append(item.getKey()+"]: ").append(currentKeys).append(' ');
			}
			else {
				currentKeys.clear();
			}
		}
		if (wereProblems) {
			throw new ContentException("Localizer ["+resourceAddress+"] - some keys are undefined in some languages! "+sb.toString()); 
		}
		else {
			totalKeys.clear();
			keysAndValues.clear();
			helpRefs.clear();
		}
	}
}
