package chav1961.purelib.sql.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.XMLUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.StreamContent;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;

public class XMLContentParser implements ResultSetContentParser {
	private static final URI		URI_TEMPLATE = URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":xml:/");
	
	private final AbstractContent	content;
	private final ResultSetMetaData	metadata;
	
	public XMLContentParser() {
		this.content = null;
		this.metadata = null;
	}
	
	protected XMLContentParser(final XMLStreamReader reader, final ResultSetMetaData metadata, final String rowTag, final SyntaxTreeInterface<?> names) throws IOException, SyntaxException, SQLException {
		this.content = new StreamContent(new Object[metadata.getColumnCount()],
				(forData)->{
					boolean	found = false;
					int		nameIndex = 0;
					String	value = null;
					
					try{while (reader.hasNext()) {
							switch (reader.next()) {
								case XMLStreamConstants.START_ELEMENT	:
									if (rowTag.equals(reader.getLocalName())) {
										found = true;
									}
									else if (found) {
										nameIndex = (int)names.seekName(reader.getLocalName()); 
									}
									break;
								case XMLStreamConstants.END_ELEMENT	:
									if (rowTag.equals(reader.getLocalName())) {
										return true;
									}
									else if (found && nameIndex >= 0) {
										forData[nameIndex] = value;
									}
									break;
								case XMLStreamConstants.CHARACTERS	:
									if (!reader.isWhiteSpace()) {
										value = reader.getText();
									}
									break;									
								case XMLStreamConstants.END_DOCUMENT	:
									return false;
								default :
									break;
							}
						}
						return false;
					} catch (XMLStreamException e) {
						throw new SQLException(e.getLocalizedMessage(),e);
					}
				},
				()->{
					try{reader.close();
					} catch (XMLStreamException e) {
						throw new SQLException(e.getLocalizedMessage(),e);
					}
				}
			);
		this.metadata = metadata;
	}

	protected XMLContentParser(final Object[][] content, final ResultSetMetaData metadata) throws IOException, SyntaxException {
		this.content = new ArrayContent(content);
		this.metadata = metadata;
	}
	
	
	@Override
	public boolean canServe(final URI request) {
		return Utils.canServeURI(request,URI_TEMPLATE);
	}

	@Override
	public Hashtable<String, String[]> filter(Hashtable<String, String[]> source) {
		final Hashtable<String, String[]>	result = new Hashtable<>();
		
		for (Entry<String, String[]> item : source.entrySet()) {
			if (!SQLContentUtils.OPTION_ENCODING.equals(item.getKey()) && !SQLContentUtils.OPTION_ROW_TAG.equals(item.getKey())) {
				result.put(item.getKey(),item.getValue());
			}
		}
		return result;
	}
	
	@Override
	public ResultSetContentParser newInstance(final URL access, final int resultSetType, final RsMetaDataElement[] content, final SubstitutableProperties options) throws IOException {
		if (access == null) {
			throw new NullPointerException("Access URL can't be null");
		}
		else if (content == null || content.length == 0) {
			throw new NullPointerException("Content can't be null or empty array");
		}
		else if (resultSetType != ResultSet.TYPE_FORWARD_ONLY && resultSetType != ResultSet.TYPE_SCROLL_SENSITIVE && resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE) {
			throw new IllegalArgumentException("Illegal result set type ["+resultSetType+"]. Can be ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_SENSITIVE or ResultSet.TYPE_SCROLL_INSENSITIVE only");
		}
		else if (!options.containsKey(SQLContentUtils.OPTION_ROW_TAG)) {
			throw new IllegalArgumentException("Mandatory option ["+SQLContentUtils.OPTION_ROW_TAG+"] is missing in the URI query string");
		}
		else {
			final SyntaxTreeInterface<RsMetaDataElement>	names = new OrdinalSyntaxTree<>();
			final String 	rowTag = options.getProperty(SQLContentUtils.OPTION_ROW_TAG,String.class);
			
			for (int index = 0; index < content.length; index++) {
				names.placeName(content[index].getName(),index,content[index]);
			}
			
			if (resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
				try{final InputStream		is = access.openStream();
					final Reader			rdr = new InputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING));
					final XMLInputFactory 	factory = XMLInputFactory.newInstance();
					factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
					final XMLStreamReader 	xmlStream = factory.createXMLStreamReader(rdr);	
				
					return new XMLContentParser(xmlStream, new FakeResultSetMetaData(content,true), rowTag, names); 
				} catch (XMLStreamException | SyntaxException | SQLException exc) {
					throw new IOException(exc.getLocalizedMessage(),exc); 
				}
			}
			else {
				final List<Object[]>		data = new ArrayList<>();
				
				try(final InputStream		is = access.openStream();
					final Reader			rdr = new InputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING))) {
					final DocumentBuilderFactory	factory = DocumentBuilderFactory.newInstance();
					final DocumentBuilder 			builder = factory.newDocumentBuilder();
					final Document doc = 	builder.parse(new InputSource(rdr));
					final NodeList			list = doc.getDocumentElement().getElementsByTagName(rowTag);
					
					for (int index = 0, maxIndex = list.getLength(); index < maxIndex; index++) {
						final Object[]		result = new Object[content.length];
						
						XMLUtils.walkDownXML((Element)list.item(index),(mode,xmlNode)->{
							if (mode == NodeEnterMode.ENTER) {
								if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {
									final long	found = names.seekName(xmlNode.getNodeName());
									
									if (found >= 0) {
										result[(int)found] = xmlNode.getTextContent();
									}
								}
							}
							return ContinueMode.CONTINUE;
						});
						data.add(result);
					}

					return new XMLContentParser(data.toArray(new Object[data.size()][]),new FakeResultSetMetaData(content,true));
				} catch (SyntaxException | ParserConfigurationException | SAXException exc) {
					throw new IOException(exc.getLocalizedMessage(),exc); 
				} finally {
					data.clear();
					names.clear();
				}
			}
		}
	}

	@Override
	public ResultSetMetaData getMetaData() throws IOException {
		return metadata;
	}

	@Override
	public AbstractContent getAccessContent() {
		return content;
	}
}
