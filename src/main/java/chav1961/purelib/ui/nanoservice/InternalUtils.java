package chav1961.purelib.ui.nanoservice;

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
 * @since 0.0.2
 *
 */
@SuppressWarnings("restriction")
public class InternalUtils {
	private static final MimetypesFileTypeMap	typeMap = new MimetypesFileTypeMap();
	private static final char[]					TRUE = "true".toCharArray();
	private static final char[]					FALSE = "false".toCharArray();
	
	static boolean mimesAreCompatible(final MimeType from, final MimeType with) {
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

	static boolean mimesAreCompatible(final MimeType[] from, final MimeType with) {
		if (from == null || from.length == 0) {
			throw new IllegalArgumentException("Types to compare from can't be null or empty array");
		}
		else if (with == null) {
			throw new NullPointerException("Type to compare with can't be null");
		}
		else {
			for (MimeType item : from) {
				if (mimesAreCompatible(item,with)) {
					return true;
				}
			}
			return false;
		}
	}
	
	static MimeType[] buildMime(final String... source) {
		if (source == null) {
			throw new NullPointerException("Source MIME list can't be null");
		}
		else if (source.length == 0) {
			return new MimeType[0];
		}
		else {
			final MimeType[]	result = new MimeType[source.length];
			
			for (int index = 0; index < source.length; index++) {
				if (source[index] == null || source[index].isEmpty()) {
					throw new IllegalArgumentException("Source item at index ["+index+"] is null or empty!");
				}
				else {
					try{result[index] = new MimeType(source[index]);
					} catch (MimeTypeParseException e) {
						result[index] = PureLibSettings.MIME_OCTET_STREAM; 
					}
				}
			}
			return result;
		}
	}

	static MimeType[] defineMimeByExtension(final String fileName) {
		try{return new MimeType[]{new MimeType(typeMap.getContentType(fileName))};
		} catch (MimeTypeParseException e) {
			return new MimeType[]{PureLibSettings.MIME_OCTET_STREAM};
		}
	}

	public static boolean buildBoolean(final char[] source) {
		if (CharUtils.compare(source,0,TRUE)) {
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
		return (byte) buildInt(source);
	}

	public static short buildShort(final char[] source) {
		return (short) buildInt(source);
	}

	public static int buildInt(final char[] source) {
		final int[]	result = new int[2];
		
		CharUtils.parseInt(source,0, result,false);
		return result[0];
	}

	public static long buildLong(final char[] source) {
		final long[]	result = new long[2];
		
		CharUtils.parseLong(source,0, result,false);
		return result[0];
	}

	public static float buildFloat(final char[] source) {
		return (float) buildDouble(source);
	}

	public static double buildDouble(final char[] source) {
		final double[]	result = new double[2];
		
		CharUtils.parseSignedDouble(source,0,result,false);
		return result[0];
	}
	
	public static boolean[] buildBooleanArray(final char[] source) {
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
	
	public static byte[] buildByteArray(final char[] source) {
		final byte[]	result = new byte[calculateNL(source)+1];
		final int[]		fill = new int[2];
		
		for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
			start = CharUtils.parseInt(source,start,fill,false);
			result[index] = (byte) fill[0];
		}
		return result;
	}

	public static short[] buildShortArray(final char[] source) {
		final short[]	result = new short[calculateNL(source)+1];
		final int[]		fill = new int[2];
		
		for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
			start = CharUtils.parseInt(source,start,fill,false);
			result[index] = (short) fill[0];
		}
		return result;
	}

	public static int[] buildIntArray(final char[] source) {
		final int[]		result = new int[calculateNL(source)+1];
		final int[]		fill = new int[2];
		
		for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
			start = CharUtils.parseInt(source,start,fill,false);
			result[index] = fill[0];
		}
		return result;
	}

	public static long[] buildLongArray(final char[] source) {
		final long[]	result = new long[calculateNL(source)+1];
		final long[]	fill = new long[2];
		
		for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
			start = CharUtils.parseLong(source,start,fill,false);
			result[index] = fill[0];
		}
		return result;
	}

	public static float[] buildFloatArray(final char[] source) {
		final float[]	result = new float[calculateNL(source)+1];
		final double[]	fill = new double[2];
		
		for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
			start = CharUtils.parseDouble(source,start,fill,false);
			result[index] = (float) fill[0];
		}
		return result;
	}

	public static double[] buildDoubleArray(final char[] source) {
		final double[]	result = new double[calculateNL(source)+1];
		final double[]	fill = new double[2];
		
		for (int index = 0, start =  skipBlank(source,0); index < result.length; index++, start =  skipBlank(source,start)) {
			start = CharUtils.parseDouble(source,start,fill,false);
			result[index] = fill[0];
		}
		return result;
	}

	public static String[] buildStringArray(final char[] source) {
		int		start = 0, currentElement = 0, nlCount = 1;
		
		for (char item : source) {
			if (item == '\n') {
				nlCount++;
			}
		}
		final String[]	result = new String[nlCount];
		
		for (int index = 0, maxIndex = source.length; index < maxIndex; index++) {
			if (source[index] == '\n') {
				result[currentElement++] = new String(source,start,index-start);
				start = index + 1;
			}
		}
		result[currentElement] = new String(source,start,source.length-start);
		return result;
	}
	
	public static Writer buildWriter(final OutputStream os, final Headers requestHeaders) {
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

	public static Reader buildReader(final InputStream is, final Headers requestHeaders) {
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
		
		for (char item : source) {
			if (item == '\n') {
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
