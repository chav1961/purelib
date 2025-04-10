package chav1961.purelib.fsys.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.logs.SystemErrLoggerFacade;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.fsys.AbstractFileSystem;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterfaceDescriptor;
import chav1961.purelib.i18n.internal.PureLibLocalizer;

/**
 * <p>This class implements the file system interface on the XML file content. The URI to use this class is 
 * <code>URI.create("xmlReadOnly:source_url");</code> (for example <code>URI.create("xmlReadOnly:http://mysite/myxml.xml");</code>).
 * Good practice is to use XML <i>resource</i> URL (got it by <code>MyClass.class.getResource("resource_name")</code>). </p>
 * 
 * <p>Content of XML is interpreted as directory and file tree. XSD scheme to validate this XML is named "XmlReadOnlyFSys.xsd"
 * and located at {@linkplain chav1961.purelib.basic.xsd} package. The main usability of the file system is to use it as a tree 
 * to mount other file systems on it ('xfs:link' tags inside the XML).</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface
 * @see chav1961.purelib.basic.xsd XSD schemas of the Pure Library
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.5
 */

public class FileSystemOnXMLReadOnly extends AbstractFileSystem implements FileSystemInterfaceDescriptor {
	private static final URI	SERVE = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":xmlReadOnly:/");
	private static final String	NAMESPACE_PREFIX = "xfs";
	private static final String	NAMESPACE_VALUE = "http://www.fsys.purelib.chav1961.ru/";
	private static final String	DESCRIPTION = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnXMLReadOnly.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_DESCRIPTION_SUFFIX;
	private static final String	VENDOR = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnXMLReadOnly.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_VENDOR_SUFFIX;
	private static final String	LICENSE = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnXMLReadOnly.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_SUFFIX;
	private static final String	LICENSE_CONTENT = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnXMLReadOnly.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_CONTENT_SUFFIX;
	private static final String	HELP = FileSystemFactory.FILESYSTEM_LOCALIZATION_PREFIX+'.'+FileSystemOnXMLReadOnly.class.getSimpleName()+'.'+FileSystemFactory.FILESYSTEM_LICENSE_HELP_SUFFIX;
	private static final Icon	ICON = new ImageIcon(FileSystemOnXMLReadOnly.class.getResource("xmlIcon.png"));
	
	private final XPathFactory 				factory = XPathFactory.newInstance();
	private final FileSystemOnXMLReadOnly	another;
	private final URI 						rootPath;
	private final Document					doc;
	private final NodeList					mountPoints;
	private final InMemoryFileSystemLocker	lock;

	/**
	 * <p>This constructor is an entry for the SPI service only. Don't use it in any purposes</p> 
	 */
	public FileSystemOnXMLReadOnly(){
		this.rootPath = null;
		this.doc = null;
		this.mountPoints = null;
		this.lock = new InMemoryFileSystemLocker(false);
		this.another = null;
	}

	public FileSystemOnXMLReadOnly(final URI rootPath) throws IOException {
		super(rootPath);
		if (!rootPath.getScheme().equals("xmlReadOnly")) {
			throw new IllegalArgumentException("Root path ["+rootPath+"] not contains 'xmlReadOnly' as scheme");
		}
		else {
			this.rootPath = URI.create(rootPath.getSchemeSpecificPart());
			this.lock = new InMemoryFileSystemLocker(false);
			this.another = null;
			
			final DocumentBuilderFactory 	dbf = DocumentBuilderFactory.newInstance();
			
			dbf.setNamespaceAware(true);
			dbf.setValidating(true);
			dbf.setAttribute(XSDConst.SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
			dbf.setAttribute(XSDConst.SCHEMA_SOURCE, XSDConst.getResource("XMLReadOnlyFSys.xsd").toString());
			
			try(final LoggerFacade			log = new SystemErrLoggerFacade()){
				final DocumentBuilder 		dBuilder = dbf.newDocumentBuilder();
			    
			    dBuilder.setErrorHandler(new ErrorHandler(){
					@Override
					public void error(final SAXParseException exc) throws SAXException {
						log.message(Severity.error, String.format("Line %1$d, col %2$d: %3$s",exc.getLineNumber(),exc.getColumnNumber(),exc.getLocalizedMessage()));
						throw exc;
					}
	
					@Override
					public void fatalError(final SAXParseException exc) throws SAXException {
						error(exc);
					}
	
					@Override
					public void warning(final SAXParseException exc) throws SAXException {
						log.message(Severity.warning, String.format("Line %1$d, col %2$d: %3$s",exc.getLineNumber(),exc.getColumnNumber(),exc.getLocalizedMessage()));
					}
				});
			    
				try(final InputStream		is = this.rootPath.toURL().openStream()) {
					Document				result = dBuilder.parse(is);
					
					result.normalizeDocument();
					doc = result;
					
					final XPathFactory 		xPathfactory = XPathFactory.newInstance();
					final XPath 			xpath = xPathfactory.newXPath();
					final NamespaceContext	nsc = new NamespaceContext() {
												@Override
												public Iterator<String> getPrefixes(final String namespaceURI) {
													return null;
												}
												
												@Override
												public String getPrefix(final String namespaceURI) {
													return null;
												}
												
												@Override
												public String getNamespaceURI(final String prefix) {
													if (prefix.equals(NAMESPACE_PREFIX)) {
														return NAMESPACE_VALUE;
													}
													else {
														return null;
													}
												}
											};
											
					xpath.setNamespaceContext(nsc);
					final XPathExpression	expr = xpath.compile("//xfs:link");
					
					this.mountPoints = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
					
					for (int index = 0; index < this.mountPoints.getLength(); index++) {
						final Node		node = this.mountPoints.item(index);
						final String	ref = node.getAttributes().getNamedItem("ref").getNodeValue();
						
						this.open(buildPath(node)).mount(FileSystemFactory.createFileSystem(URI.create(substitutePredefinedValues(ref))));
					}
					this.open("/");
				}
			} catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
				throw new IOException(e.getMessage(),e);
			}
		}
	}

	private FileSystemOnXMLReadOnly(final FileSystemOnXMLReadOnly another) {
		super(another);
		this.rootPath = another.rootPath;
		this.doc = (Document) another.doc.cloneNode(true);
		this.mountPoints = another.mountPoints;
		this.lock = null;
		this.another = another;
	}

	@Override
	public boolean canServe(final URI resource) {
		return URIUtils.canServeURI(resource,SERVE);
	}
	
	@Override
	public FileSystemInterface newInstance(final URI resource) throws EnvironmentException {
		if (!canServe(resource)) {
			throw new EnvironmentException("Resource URI ["+resource+"] is not supported by the class. Valid URI must be ["+SERVE+"...]");
		}
		else {
			try{return new FileSystemOnXMLReadOnly(URI.create(resource.getRawSchemeSpecificPart()));
			} catch (IOException e) {
				throw new EnvironmentException("I/O error creationg dile system on XML: "+e.getLocalizedMessage(),e);
			}
		}
	}
	
	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnXMLReadOnly(this);
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		try{final StringBuilder	sb = new StringBuilder("/");
		
			for (String item : URLDecoder.decode(actualPath.toString(),"UTF-8").split("/")) {
				if (!item.isEmpty()) {
					sb.append("/*[@name='").append(item).append("']");
				}
			}
			
			return new XMLDataWrapper(actualPath,(Node)factory.newXPath().compile(sb.length() == 0 ? "/" : sb.toString()).evaluate(doc,XPathConstants.NODE));
		} catch (XPathExpressionException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getVersion() {
		return PureLibSettings.CURRENT_VERSION;
	}

	@Override
	public URI getLocalizerAssociated() {
		return PureLibLocalizer.LOCALIZER_SCHEME_URI;
	}

	@Override
	public String getDescriptionId() {
		return DESCRIPTION;
	}

	@Override
	public Icon getIcon() {
		return ICON;
	}
	
	@Override
	public String getVendorId() {
		return VENDOR;
	}

	@Override
	public String getLicenseId() {
		return LICENSE;
	}

	@Override
	public String getLicenseContentId() {
		return LICENSE_CONTENT;
	}

	@Override
	public String getHelpId() {
		return HELP;
	}

	@Override
	public URI getUriTemplate() {
		return SERVE;
	}

	@Override
	public FileSystemInterface getInstance() throws EnvironmentException {
		return this;
	}

	@Override
	public boolean testConnection(final URI connection, final LoggerFacade logger) throws IOException {
		if (connection == null) {
			throw new NullPointerException("Connection to test can't be null");
		}
		else {
			try(final FileSystemInterface	inst  = newInstance(connection)) {
				
				return inst.exists();
			} catch (EnvironmentException e) {
				if (logger != null) {
					logger.message(Severity.error, e, "Error testing connection [%1$s]: %2$s",connection,e.getLocalizedMessage());
				}
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	private String buildPath(Node item) {
		final StringBuilder	sb = new StringBuilder();
		
		for(;;) {
			if (!"xfs:root".equals(item.getNodeName())) {
				sb.insert(0,'/').insert(1,item.getAttributes().getNamedItem("name").getNodeValue());
				item = item.getParentNode();
			}
			else {
				break;
			}
		}
		return sb.toString();
	}
	
	private class XMLDataWrapper implements DataWrapperInterface {
		private final Node	node;
		
		public XMLDataWrapper(final URI item, final Node node) {
			this.node = node;
		}

		@Override
		public URI[] list(Pattern pattern) throws IOException {
			if (node != null) {
				final List<String>	names = new ArrayList<String>();
				final NodeList		list = node.getChildNodes();
				
				for (int index = 0; index < list.getLength(); index++) {
					final Node		node = list.item(index);
					String			name;
					
					switch (node.getNodeName()) {
						case "xfs:root"	: 
							name = "/"; 
							break;
						case "xfs:dir" : case "xfs:file" : case "xfs:link" :
							name = node.getAttributes().getNamedItem("name").getNodeValue();
							break;
						default : continue;
					}
					
					if (list.item(index).getNodeType() == Node.ELEMENT_NODE && name != null && pattern.matcher(name).matches()) {
						names.add(name);
					}
				}
				final URI[]	result = new URI[names.size()];
				
				for (int index = 0; index < result.length; index++) {
					result[index] = URI.create(names.get(index));
				}
				return result;
			}
			else {
				return new URI[0];
			}
		}

		@Override
		public void mkDir() throws IOException {
			throw new IOException("Read-only file system is not supports directory creation");
		}

		@Override
		public void create() throws IOException {
			throw new IOException("Read-only file system is not supports file creation");
		}

		@Override
		public void setName(String name) throws IOException {
			throw new IOException("Read-only file system is not supports renaming");
		}

		@Override
		public void delete() throws IOException {
			throw new IOException("Read-only file system is not supports deletion");
		}

		@Override
		public OutputStream getOutputStream(boolean append) throws IOException {
			throw new IOException("Read-only file system is not supports write content operation");
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (node != null) {
				final String	content = node.getTextContent();
				
				return new ByteArrayInputStream(content != null && !content.isEmpty() ? content.getBytes() : new byte[0]);
			}
			else {
				throw new IOException("Attempt to read non-existent file");
			}
		}

		@Override
		public Map<String, Object> getAttributes() throws IOException {
			if (node != null) {
				final NamedNodeMap 			nnm = node.getAttributes();
				final boolean 				isDirectory = isDirectory(node);
				final String				content = node.getTextContent();
				final Map<String, Object>	info = Utils.mkMap(ATTR_SIZE, !isDirectory ? content.getBytes().length : 0
																, ATTR_NAME, node.getNodeName()
																, ATTR_ALIAS, node.getNodeName()
																, ATTR_LASTMODIFIED, 0
																, ATTR_DIR, isDirectory
																, ATTR_EXIST, true
																, ATTR_CANREAD, true
																, ATTR_CANWRITE, false
															);
				if (nnm != null) {
					for (int index = 0; index < nnm.getLength(); index++) {
						info.put(nnm.item(index).getNodeName(),nnm.item(index).getNodeName());
					}
				}
				return info;
			}
			else {
				return Utils.mkMap(ATTR_EXIST,false);
			}
		}

		@Override
		public void linkAttributes(Map<String, Object> attributes) throws IOException {
		}
		
		private boolean isDirectory(final Node node){
			final NodeList	nm = node.getChildNodes();
			
			for (int index = 0; index < nm.getLength(); index++) {
				if (nm.item(index).getNodeType() == Node.ELEMENT_NODE) {
					return true;
				}
			}
			return node.getTextContent() == null || node.getTextContent().isEmpty();
		}

		@Override
		public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
			if (another == null) {
				return lock.tryLock(path, sharedMode);
			}
			else {
				return another.tryLock(path, sharedMode);
			}
		}

		@Override
		public void lock(final String path, final boolean sharedMode) throws IOException {
			if (another == null) {
				lock.lock(path, sharedMode);
			}
			else {
				another.lock(path, sharedMode);
			}
		}

		@Override
		public void unlock(final String path, final boolean sharedMode) throws IOException {
			if (another == null) {
				lock.unlock(path, sharedMode);
			}
			else {
				another.unlock(path, sharedMode);
			}
		}		
	}
}
