package chav1961.purelib.fsys;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class FileSystemOnXMLReadOnly extends AbstractFileSystem {
	private final XPathFactory 	factory = XPathFactory.newInstance();
	private final URI 			rootPath;
	private final Document		doc;

	public FileSystemOnXMLReadOnly(){
		this.rootPath = null;
		this.doc = null;
	}

	public FileSystemOnXMLReadOnly(final URI rootPath) throws IOException {
		if (rootPath == null) {
			throw new IllegalArgumentException("Root path can't be null");
		}
		else if (!rootPath.getScheme().equals("xmlReadOnly")) {
			throw new IllegalArgumentException("Root path ["+rootPath+"] not contains 'xmlReadOnly' as scheme");
		}
		else {
			this.rootPath = URI.create(rootPath.getSchemeSpecificPart());
			
			final DocumentBuilderFactory 	dbf = DocumentBuilderFactory.newInstance();
			try(final InputStream			is = this.rootPath.toURL().openStream()) {
				final DocumentBuilder 		db = dbf.newDocumentBuilder();
				Document					result = db.parse(is);
				
				result.normalizeDocument();
				doc = result;
			} catch (ParserConfigurationException | SAXException e) {
				throw new IOException(e.getMessage(),e);
			}
		}
	}

	private FileSystemOnXMLReadOnly(final FileSystemOnXMLReadOnly another) {
		super(another);
		this.rootPath = another.rootPath;
		this.doc = (Document) another.doc.cloneNode(true);
	}

	@Override
	public boolean canServe(final String uriSchema) {
		return "xmlReadOnly".equals(uriSchema);
	}	
	
	@Override
	public FileSystemInterface clone() {
		return new FileSystemOnXMLReadOnly(this);
	}

	@Override
	public DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException {
		try{return new XMLDataWrapper(actualPath,(Node)factory.newXPath().compile(actualPath.toString()).evaluate(doc,XPathConstants.NODE));
		} catch (XPathExpressionException e) {
			throw new IOException(e.getMessage());
		}
	}

	private static class XMLDataWrapper implements DataWrapperInterface {
		private final URI	item;
		private final Node	node;
		
		public XMLDataWrapper(final URI item, final Node node) {
			this.item = item;
			this.node = node;
		}

		@Override
		public URI[] list(Pattern pattern) throws IOException {
			if (node != null) {
				final List<String>	names = new ArrayList<String>();
				final NodeList		list = node.getChildNodes();
				
				for (int index = 0; index < list.getLength(); index++) {
					final String	name = list.item(index).getNodeName();
					
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
	}
}
