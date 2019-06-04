package chav1961.purelib.i18n;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

public class XMLLocalizer extends AbstractLocalizer {
	private static final String			SUBSCHEME = "xml";
	private static final URI			SERVE = URI.create(Localizer.LOCALIZER_SCHEME+":"+SUBSCHEME+":/");
	
	private final URI					resourceAddress;
	private final Document				doc;
	private final Map<String,String>	keysAndValues = new HashMap<>();
	private final Map<String,Object>	helpRefs = new HashMap<>();
	private String						localizerURI = "unknown:/";
	
	public XMLLocalizer() throws LocalizationException, NullPointerException {
		this.resourceAddress = null;
		this.doc = null;
	}
	
	protected XMLLocalizer(final URI resourceAddress) throws LocalizationException, NullPointerException {
		if (resourceAddress == null) {
			throw new NullPointerException("Resource address can't be null");
		}
		else {
			this.resourceAddress = resourceAddress;
			
			if (resourceAddress.getScheme() != null) {
				try{final URL 	url = resourceAddress.toURL();
				
					try(final InputStream	is = url.openStream()) {
						this.doc = loadDom(is);
					}
				} catch (IOException e) {
					throw new LocalizationException(e.getLocalizedMessage(),e);
				}
			}
			else {
				try(final InputStream	is = new FileInputStream(resourceAddress.getPath())) {
					this.doc = loadDom(is);
				} catch (IOException e) {
					throw new LocalizationException(e.getLocalizedMessage(),e);
				}
			}
			loadResource(currentLocale().getLocale());
		}
	}

	@Override
	public String getLocalizerId() {
		return localizerURI == null ? "debug:/" : localizerURI;
	}

	@Override
	public boolean canServe(final URI localizer) throws NullPointerException {
		return Utils.canServeURI(localizer, SERVE); 
	}

	@Override
	public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return new XMLLocalizer(URI.create(URI.create(resource.getRawSchemeSpecificPart()).getRawSchemeSpecificPart()));
	}

	@Override
	public Iterable<String> localKeys() {
		return keysAndValues.keySet();
	}

	@Override
	public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get value for can't be null or empty"); 
		}
		else {
			return keysAndValues.get(key);
		}
	}

	@Override
	protected String getHelp(final String helpId) throws LocalizationException, IllegalArgumentException {
		if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help id to get value for can't be null or empty"); 
		}
		else {
			final Object	value = helpRefs.get(helpId);
			
			if (value == null) {
				return "";
			}
			else if (value instanceof String) {
				return (String)value;
			}
			else {
				return null;
			}
		}
	}

	@Override
	protected void loadResource(final Locale newLocale) throws LocalizationException, NullPointerException {
		if (newLocale == null) {
			throw new NullPointerException("New locale can't be null"); 
		}
		else {
			final XPathFactory 			xPathfactory = XPathFactory.newInstance();
			final XPath 				xpath = xPathfactory.newXPath();
			
			try{final XPathExpression 	expr = xpath.compile("/localization/lang[@name='"+newLocale.getLanguage()+"']/keys");
				final NodeList 			nl = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
				
				for (int index = 0, maxIndex = nl.getLength(); index < maxIndex; index++) {
					final Node 			item = nl.item(index);
	
					keysAndValues.put(item.getAttributes().getNamedItem("name").getNodeName(),item.getTextContent());
				}
			} catch (XPathExpressionException e) {
				throw new LocalizationException("Error seeking keys in the XML: "+e.getLocalizedMessage(),e); 
			}
			
			try{final XPathExpression 	expr = xpath.compile("/localization/lang[@name='"+newLocale.getLanguage()+"']/refs");
				final NodeList 			nl = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
				
				for (int index = 0, maxIndex = nl.getLength(); index < maxIndex; index++) {
					final Node 			item = nl.item(index);
					final String		name = item.getAttributes().getNamedItem("name").getNodeName();
					final Node			ref = item.getAttributes().getNamedItem("ref");
		
					if (ref != null) {
						helpRefs.put(name,URI.create(ref.getNodeValue()));
					}
					else {
						helpRefs.put(name,item.getTextContent());
					}
				}
			} catch (XPathExpressionException e) {
				throw new LocalizationException("Error seeking refs in the XML: "+e.getLocalizedMessage(),e); 
			}
		}
	}

	private Document loadDom(final InputStream is) throws IOException {
		final DocumentBuilderFactory 	dbFactory = DocumentBuilderFactory.newInstance();
		
		try{final DocumentBuilder 		dBuilder = dbFactory.newDocumentBuilder();
			final Document 				doc = dBuilder.parse(is);
			
			doc.getDocumentElement().normalize();
			return doc;
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException(e.getLocalizedMessage(),e);
		}
//		NodeList nList = doc.getElementsByTagName("staff");
	}
}
