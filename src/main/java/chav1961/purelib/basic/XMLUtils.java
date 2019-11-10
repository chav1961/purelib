package chav1961.purelib.basic;

import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.enumerations.XSDCollection;

public class XMLUtils {
	private static final String 	W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	/**
	 * <p>Validate XML content by it's XSD</p>
	 * @param xml XML content to validate
	 * @param xsd XSD to check validation
	 * @return true is the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 */
	public static boolean validateXMLByXSD(final InputStream xml, final InputStream xsd) throws NullPointerException {
		return validateXMLByXSD(xml,xsd, new AbstractLoggerFacade() {
			@Override protected void toLogger(Severity level, String text, Throwable throwable) {}
			@Override protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {return this;}
		});
	}

	/**
	 * <p>Validate XML content by it's XSD</p>
	 * @param xml XML content to validate
	 * @param xsd XSD to check validation
	 * @param logger logger facade to print error messages
	 * @return true is the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 * @since 0.0.2
	 */
	public static boolean validateXMLByXSD(final InputStream xml, final InputStream xsd, final LoggerFacade logger) throws NullPointerException {
		if (xml == null) {
			throw new NullPointerException("XML input stream can't be null");
		}
		else if (xsd == null) {
			throw new NullPointerException("XSD input stream can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			try{final DocumentBuilderFactory 	dbf = DocumentBuilderFactory.newInstance();
			
				dbf.setNamespaceAware(true);
				dbf.setValidating(true);
				dbf.setAttribute(XSDConst.SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				dbf.setAttribute(XSDConst.SCHEMA_SOURCE, new InputSource(xsd));
				
			    final DocumentBuilder 			db = dbf.newDocumentBuilder();
			    
			    db.setErrorHandler(new ErrorHandler() {
					@Override public void warning(SAXParseException exception) throws SAXException {logger.message(Severity.warning,exception.toString());}
					@Override public void error(SAXParseException exception) throws SAXException {logger.message(Severity.error,exception.toString()); throw exception;}
					@Override public void fatalError(SAXParseException exception) throws SAXException {logger.message(Severity.severe,exception.toString()); throw exception;}
					}
			    );
			    db.parse(new InputSource(xml));
			    
	            return true;
	        } catch (IOException | SAXException | ParserConfigurationException e) {
	            return false;
	        }			
		}
	}

	/**
	 * <p>Validate XML content by it's XSD and load DOM</p>
	 * @param xml XML content to validate and load
	 * @param xsd XSD to check validation
	 * @param logger logger facade to print error messages
	 * @return DOM if the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 * @throws ContentException on any validation problems
	 * @since 0.0.3
	 */
	public static Document validateAndLoadXML(final InputStream xml, final InputStream xsd, final LoggerFacade logger) throws NullPointerException, ContentException {
		if (xml == null) {
			throw new NullPointerException("XML input stream can't be null");
		}
		else if (xsd == null) {
			throw new NullPointerException("XSD input stream can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			try(final LoggerFacade	tran = logger.transaction("validateAndLoadXML")) {
				final DocumentBuilderFactory 	dbf = DocumentBuilderFactory.newInstance();
			
				dbf.setNamespaceAware(true);
				dbf.setValidating(true);
				dbf.setAttribute(XSDConst.SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				dbf.setAttribute(XSDConst.SCHEMA_SOURCE, new InputSource(xsd));
				
			    final DocumentBuilder 			db = dbf.newDocumentBuilder();
			    
			    db.setErrorHandler(new ErrorHandler() {
					@Override public void warning(SAXParseException exception) throws SAXException {logger.message(Severity.warning,exception.toString());}
					@Override public void error(SAXParseException exception) throws SAXException {logger.message(Severity.error,exception.toString()); throw exception;}
					@Override public void fatalError(SAXParseException exception) throws SAXException {logger.message(Severity.severe,exception.toString()); throw exception;}
					}
			    );
			    final Document	doc = db.parse(new InputSource(xml));
			    
			    tran.rollback();
	            return doc;
	        } catch (IOException | SAXException | ParserConfigurationException e) {
	            throw new ContentException(e.getLocalizedMessage(),e);
	        }			
		}
	}
	
	/**
	 * <p>Get XSD from purelib XSD collection.</p> 
	 * @param item xsd type to get
	 * @return content of the XSD
	 * @throws NullPointerException if item is null
	 */
	public static InputStream getPurelibXSD(final XSDCollection item) throws NullPointerException {
		if (item == null) {
			throw new NullPointerException("XSD connection item can't be null");
		}
		else {
			return Utils.class.getResourceAsStream("xsd/"+item+".xsd");
		}
	}
	
	@FunctionalInterface
	public interface XMLWalkerCallback {
		ContinueMode process(NodeEnterMode mode, Element node) throws ContentException;
	}
	
	public static ContinueMode walkDownXML(final Element root, final XMLWalkerCallback callback) throws ContentException {
		return walkDownXML(root,-1L,callback);
	}

	public static ContinueMode walkDownXML(final Element root, final long nodeTypes, final XMLWalkerCallback callback) throws ContentException {
		if (root == null) {
			throw new NullPointerException("Root element can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Walker callback can't be null"); 
		}
		else {
			return walkDownXMLInternal(root, nodeTypes, callback); 
		}
	}

	public static <T> T getAttribute(final Element node, final String attribute, final Class<T> awaited) {
		return getAttribute(node,attribute,awaited,null);
	}
	
	public static <T> T getAttribute(final Element node, final String attribute, final Class<T> awaited, final T defaultValue) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (attribute == null || attribute.isEmpty()) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else if (!node.hasAttribute(attribute)) {
			return defaultValue;
		}
		else {
			return SubstitutableProperties.convert(attribute,node.getAttribute(attribute),awaited);
		}
	}
	
	public static Properties getAttributes(final Element node) {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else {
			final Properties	result = new Properties();
			final NamedNodeMap 	map = node.getAttributes();
			
			for (int index = 0, maxIndex = map.getLength(); index < maxIndex; index++) {
				result.setProperty(map.item(index).getNodeName(),map.item(index).getNodeValue());
			}
			return result;
		}
	}
	
	public static Properties joinAttributes(final Element node, final Properties toJoin, final boolean retainExistent, final boolean assignJoined) throws NullPointerException {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (toJoin == null) {
			throw new NullPointerException("Properties to join can't be null");
		}
		else {
			final Properties	current = getAttributes(node);
			
			if (retainExistent) {
				for (Entry<Object, Object> item : toJoin.entrySet()) {
					current.putIfAbsent(item.getKey(),item.getValue());
				}
			}
			else {
				current.putAll(toJoin);
			}
			if (assignJoined) {
				for (Entry<Object, Object> item : current.entrySet()) {
					node.setAttribute(item.getKey().toString(),item.getValue().toString());
				}
			}
			return current;
		}
	}
	
	private static ContinueMode walkDownXMLInternal(final Element node, final long nodeTypes, final XMLWalkerCallback callback) throws ContentException {
		ContinueMode	before = null, after = ContinueMode.CONTINUE;
		
		if (node != null && (nodeTypes & (1 << node.getNodeType())) != 0) {
			switch (before = callback.process(NodeEnterMode.ENTER, node)) {
				case CONTINUE		:
					final NodeList	list = node.getChildNodes();
					
					for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
						final Node	item = list.item(index);
						
						if (item instanceof Element) {
							if ((after = walkDownXMLInternal((Element)item,nodeTypes,callback)) != ContinueMode.CONTINUE) {
								break;
							}
						}
					}
					after = Utils.resolveContinueMode(after,callback.process(NodeEnterMode.EXIT, node));
					break;
				case SKIP_CHILDREN : case STOP :
					after = callback.process(NodeEnterMode.EXIT, node);
					break;
				default:
					throw new IllegalStateException("Unwaited continue mode ["+before+"] for walking down");
			}
			return Utils.resolveContinueMode(before, after);
		}
		else {
			return ContinueMode.CONTINUE;
		}
	}
	
}
