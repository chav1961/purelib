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
import chav1961.purelib.i18n.interfaces.Localizer;

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
			
			try(final LoggerFacade	trans = facade.transaction(this.getClass().getName())) {
				if (resourceAddress.getScheme() != null) {
					try{final URL 	url = resourceAddress.toURL();
					
						try(final InputStream	is = url.openStream()) {
							loadDom(is,trans);
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
								loadDom(is,trans);
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
	public String getLocalizerId() {
		return localizerURI == null ? "debug:/" : localizerURI;
	}

	@Override
	public boolean canServe(final URI localizer) throws NullPointerException {
		return URIUtils.canServeURI(localizer, SERVE); 
	}

	@Override
	public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return new XMLLocalizer(URI.create(URI.create(resource.getRawSchemeSpecificPart()).getRawSchemeSpecificPart()));
	}

	@Override
	public Iterable<String> localKeys() {
		return SequenceIterator.iterable(currentCollection.keysAndValues.keySet().iterator(),currentCollection.helpRefs.keySet().iterator());
	}

	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty"); 
		}
		else if (currentCollection.keysAndValues.containsKey(key)) {
			return currentCollection.keysAndValues.get(key);
		}
		else if (currentCollection.helpRefs.containsKey(key)) {
			return "uri("+currentCollection.helpRefs.get(key)+")";
		}
		else {
			return null;
		}
	}

	@Override
	protected String getHelp(final String helpId, final String encoding) throws LocalizationException, IllegalArgumentException {
		if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help id to get value for can't be null or empty"); 
		}
		else {
			try{return new String(URIUtils.loadCharsFromURI(URI.create("file:"+helpId),encoding));
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
		
		XMLUtils.walkDownXML(XMLUtils.validateAndLoadXML(is,XSDConst.class.getResourceAsStream("XMLLocalizerContent.xsd"),logger).getDocumentElement(), (mode,node)->{
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
		
		final Set<String>	totalKeys = new HashSet<>();	// Check key definitions in all languages
		final StringBuilder	sb = new StringBuilder();
		boolean				wereProblems = false;
		
		for (Entry<String, KeyCollection> item : keys.entrySet()) {
			totalKeys.addAll(item.getValue().keysAndValues.keySet());
			totalKeys.addAll(item.getValue().helpRefs.keySet());
		}
		
		for (Entry<String, KeyCollection> item : keys.entrySet()) {
			final Set<String>	currentKeys = new HashSet<>();
			
			currentKeys.addAll(totalKeys);
			currentKeys.removeAll(item.getValue().keysAndValues.keySet());
			currentKeys.removeAll(item.getValue().helpRefs.keySet());
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
	
	private static class KeyCollection {
		private final Map<String,String>	keysAndValues = new HashMap<>();
		private final Map<String,URI>		helpRefs = new HashMap<>();

		private KeyCollection(final Map<String,String> keysAndValues, final Map<String,URI> helpRefs) {
			this.keysAndValues.putAll(keysAndValues);
			this.helpRefs.putAll(helpRefs);
		}
		
		@Override
		public String toString() {
			return "KeyCollection [keysAndValues=" + keysAndValues + ", helpRefs=" + helpRefs + "]";
		}
	}
}
