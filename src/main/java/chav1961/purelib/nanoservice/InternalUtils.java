package chav1961.purelib.nanoservice;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.net.httpserver.Headers;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.json.JsonSerializer;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class is used for support of some internal operations of the {@linkplain NanoServiceFactory} class.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 *
 */
@SuppressWarnings("restriction")  
public class InternalUtils {
	private static final MimetypesFileTypeMap	typeMap = new MimetypesFileTypeMap();
	private static final char[]					TRUE = "true".toCharArray();
	private static final char[]					FALSE = "false".toCharArray();
	private static final MimeType[]				NULL_MIME = new MimeType[0];
	
	public static boolean mimesAreCompatible(final MimeType from, final MimeType with) {
		if (from == null) { 
			throw new NullPointerException("Type to compare from can't be null");
		}
		else if (with == null) {
			throw new NullPointerException("Type to compare with can't be null");
		}
		else {
			return from.getPrimaryType().equals(with.getPrimaryType()) && from.getSubType().equals(with.getSubType());
		}
	}

	public static boolean mimesAreCompatible(final MimeType[] from, final MimeType with) {
		if (from == null || from.length == 0) {
			throw new IllegalArgumentException("Types to compare from can't be null or empty array");
		}
		else if (with == null) {
			throw new NullPointerException("Type to compare with can't be null");
		}
		else {
			for (int index = 0; index < from.length; index++) {
				if (from[index] == null) {
					throw new NullPointerException("Type to compare from contains nulls at index ["+index+"]");
				}
				else if (mimesAreCompatible(from[index],with)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public static MimeType[] buildMime(final String... source) throws MimeTypeParseException {
		if (source == null) {
			throw new NullPointerException("Source MIME list can't be null");
		}
		else if (source.length == 0) {
			return NULL_MIME;
		}
		else {
			int	pos, start, counter = 0;
			
			for (int index = 0; index < source.length; index++) {
				if (source[index] == null || source[index].isEmpty()) {
					throw new IllegalArgumentException("Source item at index ["+index+"] is null or empty!");
				}
				else {
					final String	temp = source[index];
					
					pos = 0;
					while ((pos = temp.indexOf(',',pos)) != -1) {
						counter++;
						pos++;
					}
					counter++;
				}
			}
			
			final MimeType[]	result = new MimeType[counter];

			counter = 0;
			for (int index = 0; index < source.length; index++) {
				final String	temp = source[index];
				int				semicolon;
				
				pos = start = 0;
				while ((pos = temp.indexOf(',',start)) != -1) {
					String	currentMime = temp.substring(start,pos);
					
					if ((semicolon = currentMime.indexOf(';')) >= 0) {
						currentMime = currentMime.substring(0,semicolon); 
					}
					result[counter++] = new MimeType(currentMime);
					start = pos + 1;
				}
				String	currentMime = temp.substring(start);
				
				if ((semicolon = currentMime.indexOf(';')) >= 0) {
					currentMime = currentMime.substring(0,semicolon); 
				}
				result[counter++] = new MimeType(currentMime);
			}
			return result;
		}
	}

	public static MimeType[] defineMimeByExtension(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name to define MIME for can't be null or empty");
		}
		else if (fileName.endsWith(".cre")) {
			return new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT};
		}
		else if (fileName.endsWith(".css")) {
			return new MimeType[]{PureLibSettings.MIME_CSS_TEXT};
		}
		else if (fileName.contains("favicon.ico")) {
			return new MimeType[]{PureLibSettings.MIME_FAVICON};
		}
		else {
			try{return new MimeType[]{new MimeType(typeMap.getContentType(fileName))};
			} catch (MimeTypeParseException e) {
				return new MimeType[]{PureLibSettings.MIME_OCTET_STREAM};
			}
		}
	}

	public static boolean mimesIntersect(final MimeType[] left, final MimeType[] right) {
		if (left == null) {
			throw new NullPointerException("Left MIME can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right MIME can't be null"); 
		}
		else {
			for (MimeType fromLeft : left) {
				if (fromLeft == null) {
					throw new NullPointerException("Left MIME contains nulls inside"); 
				}
				else {
					for (MimeType fromRight : right) {
						if (fromRight == null) {
							throw new NullPointerException("Right MIME contains nulls inside"); 
						}
						else if ((fromLeft.getPrimaryType().equals(fromRight.getPrimaryType())
							 || "*".equals(fromRight.getPrimaryType())
							)
							&& 
							(fromLeft.getSubType().equals(fromRight.getSubType()) 
							 || "*".equals(fromRight.getSubType())
							)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	public static boolean theSameMimes(final MimeType[] left, final MimeType[] right) {
		if (left == null) {
			throw new NullPointerException("Left MIME can't be null"); 
		}
		else if (right == null) {
			throw new NullPointerException("Right MIME can't be null"); 
		}
		else if (left.length != right.length) {
			return false;
		}
		else {
			int		count = 0;
			
			for (MimeType fromLeft : left) {
				for (MimeType fromRight : right) {
					if (fromLeft.getPrimaryType().equals(fromRight.getPrimaryType()) && fromLeft.getSubType().equals(fromRight.getSubType())) {
						count++;
					}
				}
			}
			return count == left.length;
		}
	}
	
	public static boolean buildBoolean(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else if (CharUtils.compare(source,0,TRUE)) {
			return true;
		}
		else if (CharUtils.compare(source,0,FALSE)) {
			return false;
		}
		else {
			throw new IllegalArgumentException("Illegal boolean value ["+new String(source)+"]");
		}
	}
	
	public static byte buildByte(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			return (byte) buildInt(source);
		}
	}

	public static short buildShort(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			return (short) buildInt(source);
		}
	}

	public static int buildInt(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final int[]	result = new int[2];
			
			CharUtils.parseInt(source,0, result,false);
			return result[0];
		}
	}

	public static long buildLong(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final long[]	result = new long[2];
			
			CharUtils.parseLong(source,0, result,false);
			return result[0];
		}
	}

	public static float buildFloat(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			return (float) buildDouble(source);
		}
	}

	public static double buildDouble(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final double[]	result = new double[2];
			
			CharUtils.parseSignedDouble(source,0,result,false);
			return result[0];
		}
	}
	
	public static boolean[] buildBooleanArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final boolean[]	result = new boolean[calculateNL(source)+1];
			
			for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
				if (CharUtils.compare(source,start,TRUE)) {
					result[index] = true;
					start += TRUE.length;
				}
				else if (CharUtils.compare(source,start,FALSE)) {
					result[index] = false;
					start += FALSE.length;
				}
				else {
					throw new IllegalArgumentException("Illegal boolean value ["+new String(source)+"]");
				}
			}
			return result;
		}
	}
	
	public static byte[] buildByteArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final byte[]	result = new byte[calculateNL(source)+1];
			final int[]		fill = new int[2];
			
			for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
				start = CharUtils.parseSignedInt(source,start,fill,false);
				result[index] = (byte) fill[0];
			}
			return result;
		}
	}

	public static short[] buildShortArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final short[]	result = new short[calculateNL(source)+1];
			final int[]		fill = new int[2];
			
			for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
				start = CharUtils.parseSignedInt(source,start,fill,false);
				result[index] = (short) fill[0];
			}
			return result;
		}
	}

	public static int[] buildIntArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final int[]		result = new int[calculateNL(source)+1];
			final int[]		fill = new int[2];
			
			for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
				start = CharUtils.parseSignedInt(source,start,fill,false);
				result[index] = fill[0];
			}
			return result;
		}
	}

	public static long[] buildLongArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final long[]	result = new long[calculateNL(source)+1];
			final long[]	fill = new long[2];
			
			for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
				start = CharUtils.parseSignedLong(source,start,fill,false);
				result[index] = fill[0];
			}
			return result;
		}
	}

	public static float[] buildFloatArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final float[]	result = new float[calculateNL(source)+1];
			final float[]	fill = new float[1];
			
			for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
				start = CharUtils.parseSignedFloat(source,start,fill,false);
				result[index] = fill[0];
			}
			return result;
		}
	}

	public static double[] buildDoubleArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final double[]	result = new double[calculateNL(source)+1];
			final double[]	fill = new double[1];
			
			for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
				start = CharUtils.parseSignedDouble(source,start,fill,false);
				result[index] = fill[0];
			}
			return result;
		}
	}

	public static String[] buildStringArray(final char[] source) {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else {
			final String[]	result = new String[calculateNL(source)+1];
			int				start = 0, len = source.length, from, to, currentElement = 0;
			
			while (start < len) {
				while (start < len && source[start] <= ' ' && source[start] != '\n') {
					start++;
				}
				from = start;
				while (start < len && source[start] != '\n') {
					start++;
				}
				to = start;
				while (start > from && source[start-1] <= ' ') {
					start--;
				}
				result[currentElement++] = new String(source,from,start-from);
				start = to + 1;
			}
			return result;
		}
	}
	
	public static Writer buildWriter(final OutputStream os, final Headers requestHeaders) {
		if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else {
			final String	encoding = requestHeaders.getFirst(NanoServiceFactory.HEAD_ACCEPT_CHARSET);
			
			if (encoding == null || encoding.isEmpty()) {
				return new OutputStreamWriter(os);
			}
			else {
				try{return new OutputStreamWriter(os,encoding);
				} catch (UnsupportedEncodingException e) {
					return new OutputStreamWriter(os);
				}
			}
		}
	}

	public static Reader buildReader(final InputStream is, final Headers requestHeaders) {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null"); 
		}
		else {
			final String	encoding = requestHeaders.getFirst(NanoServiceFactory.HEAD_CONTENT_TYPE);
			
			if (encoding == null || encoding.isEmpty()) {
				return new InputStreamReader(is);
			}
			else {
				try{return new InputStreamReader(is,encoding);
				} catch (UnsupportedEncodingException e) {
					return new InputStreamReader(is);
				}
			}
		}
	}
	
	public static XMLStreamWriter buildXMLStreamWriter(final OutputStream os, final Headers requestHeaders) throws XMLStreamException, FactoryConfigurationError {
		return XMLOutputFactory.newInstance().createXMLStreamWriter(buildWriter(os,requestHeaders));
	}

	public static XMLStreamReader buildXMLStreamReader(final InputStream is, final Headers requestHeaders) throws XMLStreamException, FactoryConfigurationError {
		return XMLInputFactory.newInstance().createXMLStreamReader(buildReader(is,requestHeaders));
	}
	
	public static Document buildDocument(final OutputStream os, final Headers requestHeaders) throws ParserConfigurationException {
		return new DocumentWrapper(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(),buildWriter(os,requestHeaders));
	}

	public static Document buildDocument(final InputStream is, final Headers requestHeaders) throws ParserConfigurationException, SAXException, IOException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(buildReader(is,requestHeaders)));
	}
	
	public static CharacterTarget buildCharacterTarget(final OutputStream os, final Headers requestHeaders) {
		return null;
	}

	public static String buildFromList(final List<?> content) {
		final StringBuilder	sb = new StringBuilder();
		
		for (Object item : content) {
			sb.append('\n').append(item);
		}
		return sb.toString().substring(1);
	}
	
	public static void dumpToOuputStream(final StringBuilder source, final OutputStream os, final Headers requestHeaders) throws IOException {
		final Writer	wr = buildWriter(os,requestHeaders);

		wr.write(source.toString());
		wr.flush();
	}

	public static void dumpToJsonOutputStream(final Object source, final OutputStream os, final Headers requestHeaders) throws IOException, EnvironmentException, PrintingException {
		final Writer					wr = buildWriter(os,requestHeaders);
		@SuppressWarnings("unchecked")
		final JsonSerializer<Object>	ser = (JsonSerializer<Object>) JsonSerializer.buildSerializer(source.getClass());
		
		try(final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
			ser.serialize(source, prn);
		}
	}

	public static Object loadFromJsonInputStream(final String sourceClass, final InputStream is, final Headers requestHeaders) throws IOException, EnvironmentException, PrintingException, ContentException, SyntaxException, ClassNotFoundException {
		final Reader					rdr = buildReader(is,requestHeaders);
		@SuppressWarnings("unchecked")
		final JsonSerializer<Object>	ser = (JsonSerializer<Object>) JsonSerializer.buildSerializer(Class.forName(sourceClass));
		
		try(final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
			parser.next();
			return ser.deserialize(parser);
		}
	}
	
	
	private static int calculateNL(final char[] source) {
		int	count = 0;
		
		for (int index = 0, maxIndex = source.length; index < maxIndex; index++) {
			final char 	item = source[index];
		
			if (item == '\n' && index < maxIndex-1) {	// Not the same last '\n'
				count++;
			}
		}
		return count;
	}
	
	private static int skipBlank(final char[] source, int from) {
		while (from < source.length && source[from] <= ' ') {
			from++;
		}
		if (from >= source.length) {
			return -1;
		}
		else {
			return from;
		}
	}

	private static class DocumentWrapper implements Document, Closeable {
		private final Document	delegate;
		private final Writer	writer;
		
		private DocumentWrapper(final Document delegate, final Writer writer) {
			this.delegate = delegate;
			this.writer = writer;
		}

		@Override public String getNodeName() {return delegate.getNodeName();}
		@Override public String getNodeValue() throws DOMException {return delegate.getNodeValue();}
		@Override public void setNodeValue(String nodeValue) throws DOMException {delegate.setNodeValue(nodeValue);}
		@Override public short getNodeType() {return delegate.getNodeType();}
		@Override public Node getParentNode() {return delegate.getParentNode();}
		@Override public NodeList getChildNodes() {return delegate.getChildNodes();}
		@Override public Node getFirstChild() {return delegate.getFirstChild();}
		@Override public Node getLastChild() {return delegate.getLastChild();}
		@Override public Node getPreviousSibling() {return delegate.getPreviousSibling();}
		@Override public Node getNextSibling() {return delegate.getNextSibling();}
		@Override public NamedNodeMap getAttributes() {return delegate.getAttributes();}
		@Override public Document getOwnerDocument() {return delegate.getOwnerDocument();}
		@Override public Node insertBefore(Node newChild, Node refChild) throws DOMException {return delegate.insertBefore(newChild, refChild);}
		@Override public Node replaceChild(Node newChild, Node oldChild) throws DOMException {return delegate.replaceChild(newChild, oldChild);}
		@Override public Node removeChild(Node oldChild) throws DOMException {return delegate.removeChild(oldChild);}
		@Override public Node appendChild(Node newChild) throws DOMException {return delegate.appendChild(newChild);}
		@Override public boolean hasChildNodes() {return delegate.hasChildNodes();}
		@Override public Node cloneNode(boolean deep) {return delegate.cloneNode(deep);}
		@Override public void normalize() {delegate.normalize();}
		@Override public boolean isSupported(String feature, String version) {return delegate.isSupported(feature, version);}
		@Override public String getNamespaceURI() {return delegate.getNamespaceURI();}
		@Override public String getPrefix() {return delegate.getPrefix();}
		@Override public void setPrefix(String prefix) throws DOMException {delegate.setPrefix(prefix);}
		@Override public String getLocalName() {return delegate.getLocalName();}
		@Override public boolean hasAttributes() {return delegate.hasAttributes();}
		@Override public String getBaseURI() {return delegate.getBaseURI();}
		@Override public short compareDocumentPosition(Node other) throws DOMException {return delegate.compareDocumentPosition(other);}
		@Override public String getTextContent() throws DOMException {return delegate.getTextContent();}
		@Override public void setTextContent(String textContent) throws DOMException {delegate.setTextContent(textContent);}
		@Override public boolean isSameNode(Node other) {return delegate.isSameNode(other);}
		@Override public String lookupPrefix(String namespaceURI) {return delegate.lookupPrefix(namespaceURI);}
		@Override public boolean isDefaultNamespace(String namespaceURI) {return delegate.isDefaultNamespace(namespaceURI);}
		@Override public String lookupNamespaceURI(String prefix) {return delegate.lookupNamespaceURI(prefix);}
		@Override public boolean isEqualNode(Node arg) {return delegate.isEqualNode(arg);}
		@Override public Object getFeature(String feature, String version) {return delegate.getFeature(feature, version);}
		@Override public Object setUserData(String key, Object data, UserDataHandler handler) {return delegate.setUserData(key, data, handler);}
		@Override public Object getUserData(String key) {return delegate.getUserData(key);}
		@Override public DocumentType getDoctype() {return delegate.getDoctype();}
		@Override public DOMImplementation getImplementation() {return delegate.getImplementation();}
		@Override public Element getDocumentElement() {return delegate.getDocumentElement();}
		@Override public Element createElement(String tagName) throws DOMException {return delegate.createElement(tagName);}
		@Override public DocumentFragment createDocumentFragment() {return delegate.createDocumentFragment();}
		@Override public Text createTextNode(String data) {return delegate.createTextNode(data);}
		@Override public Comment createComment(String data) {return delegate.createComment(data);}
		@Override public CDATASection createCDATASection(String data) throws DOMException {return delegate.createCDATASection(data);}
		@Override public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {return delegate.createProcessingInstruction(target, data);}
		@Override public Attr createAttribute(String name) throws DOMException {return delegate.createAttribute(name);}
		@Override public EntityReference createEntityReference(String name) throws DOMException {return delegate.createEntityReference(name);}
		@Override public NodeList getElementsByTagName(String tagname) {return delegate.getElementsByTagName(tagname);}
		@Override public Node importNode(Node importedNode, boolean deep) throws DOMException {return delegate.importNode(importedNode, deep);}
		@Override public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {return delegate.createElementNS(namespaceURI, qualifiedName);}
		@Override public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {return delegate.createAttributeNS(namespaceURI, qualifiedName);}
		@Override public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {return delegate.getElementsByTagNameNS(namespaceURI, localName);}
		@Override public Element getElementById(String elementId) {return delegate.getElementById(elementId);}
		@Override public String getInputEncoding() {return delegate.getInputEncoding();}
		@Override public String getXmlEncoding() {return delegate.getXmlEncoding();}
		@Override public boolean getXmlStandalone() {return delegate.getXmlStandalone();}
		@Override public void setXmlStandalone(boolean xmlStandalone) throws DOMException {delegate.setXmlStandalone(xmlStandalone);}
		@Override public String getXmlVersion() {return delegate.getXmlVersion();}
		@Override public void setXmlVersion(String xmlVersion) throws DOMException {delegate.setXmlVersion(xmlVersion);}
		@Override public boolean getStrictErrorChecking() {return delegate.getStrictErrorChecking();}
		@Override public void setStrictErrorChecking(boolean strictErrorChecking) {delegate.setStrictErrorChecking(strictErrorChecking);}
		@Override public String getDocumentURI() {return delegate.getDocumentURI();}
		@Override public void setDocumentURI(String documentURI) {delegate.setDocumentURI(documentURI);}
		@Override public Node adoptNode(Node source) throws DOMException {return delegate.adoptNode(source);}
		@Override public DOMConfiguration getDomConfig() {return delegate.getDomConfig();}
		@Override public void normalizeDocument() {delegate.normalizeDocument();}
		@Override public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException {return delegate.renameNode(n, namespaceURI, qualifiedName);}

		@Override
		public void close() throws IOException {
			try{final TransformerFactory	transformerFactory = TransformerFactory.newInstance();
            	final Transformer 			transformer = transformerFactory.newTransformer();
                final DOMSource 			source = new DOMSource(delegate);
                final StreamResult 			target= new StreamResult(writer);

                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, target);
                writer.flush();
			} catch (TransformerException exc) {
				throw new IOException("I/O error: "+exc.getLocalizedMessage(),exc);
			}
		}
	}
}
