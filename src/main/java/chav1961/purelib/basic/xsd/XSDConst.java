package chav1961.purelib.basic.xsd;

import java.io.InputStream;
import java.net.URL;

/**
 * <p>This class can be used as an anchor to get access to the XSD schemes inside the package. Example of usage is:</p>
 * <code><b>try</b>(<b>final</b> InputStream xsdStream = XSDConst.<b>class</b>.getResource("CreoleXML.xsd")) {<br>
 * . . .<br>
 * }<br>
 * </code>
 * <p>Alternative way to get requested XSD scheme is using {@linkplain chav1961.purelib.basic.XMLUtils#getPurelibXSD(chav1961.purelib.enumerations.XSDCollection)} method</p> 
 * 
 * @see chav1961.purelib.basic.XMLUtils#getPurelibXSD(chav1961.purelib.enumerations.XSDCollection)
 * @see chav1961.purelib.basic.XMLUtils#validateXMLByXSD(java.io.InputStream, java.io.InputStream)
 * @see chav1961.purelib.basic.XMLUtils#validateXMLByXSD(java.io.InputStream, java.io.InputStream, chav1961.purelib.basic.interfaces.LoggerFacade)
 * @see chav1961.purelib.enumerations.XSDCollection
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.4
 */
public abstract class XSDConst {
	/**
	 * <p>Validation scheme language URI used for XSD validation</p> 
	 */
	public static final String 	SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	
	/**
	 * <p>Validation scheme source URI used for XSD validation</p>
	 */
	public static final String 	SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	public static URL getResource(final String resource) throws IllegalArgumentException {
		if (resource == null || resource.isEmpty()) {
			throw new IllegalArgumentException("Resource to get URL for can't be null or empty");
		}
		else {
			final URL	result = XSDConst.class.getResource(resource);
			
			if (result != null) {
				return result;
			}
			else {
				throw new IllegalArgumentException("Resource ["+resource+"] to get URL for not found");
			}
		}
	}

	public static InputStream getResourceAsStream(final String resource) throws IllegalArgumentException {
		if (resource == null || resource.isEmpty()) {
			throw new IllegalArgumentException("Resource to get URL for can't be null or empty");
		}
		else {
			final InputStream	result = XSDConst.class.getResourceAsStream(resource);
			
			if (result != null) {
				return result;
			}
			else {
				throw new IllegalArgumentException("Resource ["+resource+"] to get URL for not found");
			}
		}
	}
}
