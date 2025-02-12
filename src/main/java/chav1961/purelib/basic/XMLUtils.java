package chav1961.purelib.basic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import chav1961.purelib.basic.CSSUtils.BaseUnit;
import chav1961.purelib.basic.CSSUtils.Unit;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.enumerations.XSDCollection;

/**
 * <p>This class contains implementation of the useful actions with the XML content.</p> 
 * 
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.8
 */
public class XMLUtils {
	private static final String 	W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	/**
	 * <p>This enumeration describes max severity for validation messages, that must me thrown</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum ValidationSeverity {
		WARNING(1),
		ERROR(2),
		FATAL(3);
		
		private final int	level;
		
		private ValidationSeverity(final int level) {
			this.level = level;
		}
		
		public int getLevel() {
			return level;
		}
	}
	
	
	/**
	 * <p>Validate XML content by it's XSD</p>
	 * @param xml XML content to validate
	 * @param xsd XSD to check validation
	 * @return true is the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 */
	public static boolean validateXMLByXSD(final InputStream xml, final InputStream xsd) throws NullPointerException {
		return validateXMLByXSD(xml,xsd, PureLibSettings.CURRENT_LOGGER);
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
		return validateXMLByXSD(xml, xsd, ValidationSeverity.ERROR, logger);
	}	
	
	/**
	 * <p>Validate XML content by it's XSD</p>
	 * @param xml XML content to validate. Can't be null
	 * @param xsd XSD to check validation. Can't be null
	 * @param maxSeverity max severity to accept validation. Can't be null
	 * @param logger logger facade to print error messages. Can't be null
	 * @return true is the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 * @since 0.0.7
	 */
	public static boolean validateXMLByXSD(final InputStream xml, final InputStream xsd, final ValidationSeverity maxSeverity, final LoggerFacade logger) throws NullPointerException {
		if (xml == null) {
			throw new NullPointerException("XML input stream can't be null");
		}
		else if (xsd == null) {
			throw new NullPointerException("XSD input stream can't be null");
		}
		else if (maxSeverity == null) {
			throw new NullPointerException("Max validation severity can't be null");
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
			    
			    db.setErrorHandler(new XMLValidationErrorHandler(maxSeverity, logger));
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
		return validateAndLoadXML(xml, xsd, ValidationSeverity.ERROR, logger);
	}
	
	/**
	 * <p>Validate XML content by it's XSD and load DOM</p>
	 * @param xml XML content to validate and load
	 * @param xsd XSD to check validation
	 * @param maxSeverity max severity to accept validation. Can't be null
	 * @param logger logger facade to print error messages
	 * @return DOM if the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 * @throws ContentException on any validation problems
	 * @since 0.0.7
	 */
	public static Document validateAndLoadXML(final InputStream xml, final InputStream xsd, final ValidationSeverity maxSeverity, final LoggerFacade logger) throws NullPointerException, ContentException {
		if (xml == null) {
			throw new NullPointerException("XML input stream can't be null");
		}
		else if (xsd == null) {
			throw new NullPointerException("XSD input stream can't be null");
		}
		else if (maxSeverity == null) {
			throw new NullPointerException("Max validation severity can't be null");
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
			    
			    db.setErrorHandler(new XMLValidationErrorHandler(maxSeverity, logger));
			    final Document	doc = db.parse(new InputSource(xml));
			    
			    tran.rollback();
	            return doc;
	        } catch (IOException | SAXException | ParserConfigurationException e) {
	            throw new ContentException(e.getLocalizedMessage(),e);
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
	 * @since 0.0.7
	 */
	public static Document validateAndLoadXML(final InputStream xml, final URL xsd, final LoggerFacade logger) throws NullPointerException, ContentException {
		return validateAndLoadXML(xml, xsd, ValidationSeverity.ERROR, logger);
	}	
	
	/**
	 * <p>Validate XML content by it's XSD and load DOM</p>
	 * @param xml XML content to validate and load
	 * @param xsd XSD to check validation
	 * @param maxSeverity max severity to accept validation. Can't be null
	 * @param logger logger facade to print error messages
	 * @return DOM if the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 * @throws ContentException on any validation problems
	 * @since 0.0.7
	 */
	public static Document validateAndLoadXML(final InputStream xml, final URL xsd, final ValidationSeverity maxSeverity, final LoggerFacade logger) throws NullPointerException, ContentException {
		if (xml == null) {
			throw new NullPointerException("XML input stream can't be null");
		}
		else if (xsd == null) {
			throw new NullPointerException("XSD input stream can't be null");
		}
		else if (maxSeverity == null) {
			throw new NullPointerException("Max validation severity can't be null");
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
				dbf.setAttribute(XSDConst.SCHEMA_SOURCE, xsd.toString());
				
			    final DocumentBuilder 			db = dbf.newDocumentBuilder();
			    
			    db.setErrorHandler(new XMLValidationErrorHandler(maxSeverity, logger));
			    final Document	doc = db.parse(new InputSource(xml));
			    
			    tran.rollback();
	            return doc;
	        } catch (IOException | SAXException | ParserConfigurationException e) {
	            throw new ContentException(e.getLocalizedMessage(),e);
	        }			
		}
	}
	
	/**
	 * <p>Parse html stream and build XML document</p>
	 * @param html html stream to parse. Can't be null
	 * @param logger logger to print errors. Can't be null
	 * @return document parsed. Can't be null
	 * @throws NullPointerException when any parameter is null
	 * @throws ContentException on any parsing errors
	 * @since 0.0.7
	 */
	public static Document loadHtml(final InputStream html, final LoggerFacade logger) throws NullPointerException, ContentException {
		if (html == null) {
			throw new NullPointerException("Html stream can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else {
			try(final LoggerFacade				tran = logger.transaction("loadHTML")) {
				final DocumentBuilderFactory	dbf = DocumentBuilderFactory.newInstance();
				final DocumentBuilder			docBuilder = dbf.newDocumentBuilder();
				final Document					doc = docBuilder.newDocument();
				final HTMLEditorKit.Parser		parser = new ParserDelegator();
				final HTMLTableParser			parserCallback = new HTMLTableParser(doc);
				
				parser.parse(new InputStreamReader(html, PureLibSettings.DEFAULT_CONTENT_ENCODING), parserCallback, true);
				tran.rollback();
				return doc;
			} catch (IOException | ParserConfigurationException e) {
				throw new ContentException(e.getLocalizedMessage(), e);
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

	/**
	 * <p>Get XSD from purelib XSD collection.</p> 
	 * @param item xsd type to get
	 * @return content of the XSD
	 * @throws NullPointerException if item is null
	 * @since 0.0.7
	 */
	public static URL getPurelibXSDURL(final XSDCollection item) throws NullPointerException {
		if (item == null) {
			throw new NullPointerException("XSD connection item can't be null");
		}
		else {
			return Utils.class.getResource("xsd/"+item+".xsd");
		}
	}
	
	/**
	 * <p>This interface describes callback for walking by DOM XML tree</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	@FunctionalInterface
	public interface XMLWalkerCallback {
		/**
		 * <p>Process current DOM XML node</p> 
		 * @param mode enter mode for the given call
		 * @param node node to prcoess for the given call
		 * @return continuation (see {@linkplain ContinueMode} for details). Can't be null
		 * @throws ContentException on any processing exceptions
		 */
		ContinueMode process(NodeEnterMode mode, Element node) throws ContentException;
	}
	
	/**
	 * <p>Walk down by DOM XML tree</p>
	 * @param root DOM XML tree root
	 * @param callback callback to process on every DOM XML node
	 * @return Continue mode after last node exited. Can't be null
	 * @throws ContentException on any processing exceptions
	 * @throws NullPointerException if any parameter is null
	 */
	public static ContinueMode walkDownXML(final Element root, final XMLWalkerCallback callback) throws ContentException, NullPointerException {
		return walkDownXML(root,-1L,callback);
	}

	/**
	 * <p>Walk down by DOM XML tree</p>
	 * @param root DOM XML tree root
	 * @param nodeTypes filter for node types (see {@linkplain XMLConstants}) for details
	 * @param callback callback to process on every DOM XML node
	 * @return Continue mode after last node exited. Can't be null
	 * @throws ContentException on any processing exceptions
	 * @throws NullPointerException if any parameter is null
	 */
	public static ContinueMode walkDownXML(final Element root, final long nodeTypes, final XMLWalkerCallback callback) throws ContentException, NullPointerException {
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

	/**
	 * <p>Extract attribute value from current node and convert it to the given type</p>
	 * @param <T> attribute type
	 * @param node node to extract attribute from
	 * @param attribute attribute name
	 * @param awaited attribute type awaited
	 * @return attribute extracted or null if missing 
	 * @throws NullPointerException if any parameter is null
	 * @throws IllegalArgumentException if conversion failed for the given attribute
	 */
	public static <T> T getAttribute(final Element node, final String attribute, final Class<T> awaited) throws NullPointerException, IllegalArgumentException {
		return getAttribute(node,attribute,awaited,null);
	}

	/**
	 * <p>Extract attribute value from current node and convert it to the given type</p>
	 * @param <T> attribute type
	 * @param node node to extract attribute from
	 * @param attribute attribute name
	 * @param awaited attribute type awaited
	 * @param defaultValue default value when attribute is missing
	 * @return attribute extracted or null if missing 
	 * @throws NullPointerException if any parameter is null
	 * @throws IllegalArgumentException if conversion failed for the given attribute
	 */
	public static <T> T getAttribute(final Element node, final String attribute, final Class<T> awaited, final T defaultValue) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (Utils.checkEmptyOrNullString(attribute)) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else if (!node.hasAttribute(attribute)) {
			return defaultValue;
		}
		else {
			return SubstitutableProperties.convert(attribute,node.getAttribute(attribute),awaited);
		}
	}

	/**
	 * <p>Extract numeric attribute value from current node and process it's measure unit</p>
	 * @param node node to extract attribute from
	 * @param attribute attribute name
	 * @param baseUnit base unit to get value for.
	 * @param defaultValue default value when attribute is missing
	 * @return attribute value reduced to base unit
	 * @throws NullPointerException any parameters is null.
	 * @throws IllegalArgumentException attribute name is null or empty or invalid attribute value.
	 * @since 0.0.8
	 */
	public static Number getAttribute(final Element node, final String attribute, final BaseUnit baseUnit, final Number defaultValue) throws NullPointerException, IllegalArgumentException {
		if (node == null) {
			throw new NullPointerException("Node can't be null");
		}
		else if (Utils.checkEmptyOrNullString(attribute)) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else if (!node.hasAttribute(attribute)) {
			return defaultValue;
		}
		else {
			return Unit.baseValueOf(node.getAttribute(attribute), baseUnit);
		}
	}	
	
	/**
	 * <p>Get attributer from current node</p>
	 * @param node node to extract attributes from
	 * @return attributes extracted. Can't be null
	 * @throws NullPointerException if node is null
	 */
	public static Properties getAttributes(final Element node) throws NullPointerException {
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

	/**
	 * <p>Join a set of attributes with existent attributes of the given node, and optionally replace the given node with attributes joined</p> 
	 * @param node note to join attributer for
	 * @param toJoin set of attributes to join
	 * @param retainExistent true - prevent existent attribute from overwrite
	 * @param assignJoined true - really replace attributes for the given node, false - join attributes, but not replace then in the node
	 * @return a set of joined attributes. Can't be null
	 * @throws NullPointerException if any parameter is null
	 */
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
	
	private static class HTMLTableParser extends HTMLEditorKit.ParserCallback {
		private final Document		doc;
		private final List<Tag>		stack = new ArrayList<>();
		private final List<Element>	treeStack = new ArrayList<>();
		
		private HTMLTableParser(final Document doc) {
			this.doc = doc;
		}
		
		@Override
	    public void handleText(char[] data, int pos) {
			treeStack.get(0).appendChild(doc.createTextNode(new String(data)));
	    }

		@Override
	    public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
	    	processTag(t, a);
			stack.add(0,t);
	    }

		@Override
	    public void handleEndTag(Tag t, int pos) {
			stack.remove(0);
	    	placeTag();
	    }
	    
	    @Override
	    public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos) {
	    	processTag(t, a);
	    	placeTag();
	    }
	    
	    private void processTag(Tag t, MutableAttributeSet a) {
	    	final Element	el = doc.createElement(t.toString());
	    	
	    	for(Object key : Utils.enumeration2Iterable(a.getAttributeNames())) {
	    		final Object	val = a.getAttribute(key);
	    		el.setAttribute(key.toString(), val.toString());
	    	}
	    	treeStack.add(0, el);
	    }
	    
	    private void placeTag() {
	    	final Element	el = treeStack.remove(0);
	    	
	    	if (!treeStack.isEmpty()) {
		    	treeStack.get(0).appendChild(el);
	    	}
	    	else {
	    		doc.appendChild(el);
	    	}
	    }
	}
	
	private static class XMLValidationErrorHandler implements ErrorHandler {
		private final ValidationSeverity	severity;
		private final LoggerFacade 			logger;
		
		private XMLValidationErrorHandler(final ValidationSeverity severity, final LoggerFacade logger) {
			this.severity = severity;
			this.logger = logger;
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			logger.message(Severity.warning, exception.toString());
			if (severity.getLevel() <= 1) {
				throw exception;
			}
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			logger.message(Severity.error, exception.toString());
			if (severity.getLevel() <= 2) {
				throw exception;
			}
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			logger.message(Severity.severe, exception.toString());
			if (severity.getLevel() <= 3) {
				throw exception;
			}
		}
	}
}
