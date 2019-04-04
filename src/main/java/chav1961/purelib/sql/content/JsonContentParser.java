package chav1961.purelib.sql.content;

import java.io.IOException;
import java.io.InputStream;
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

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.StreamContent;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.byte2char.BufferedInputStreamReader;
import chav1961.purelib.streams.interfaces.JsonStaxParserInterface;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class JsonContentParser implements ResultSetContentParser {
	private static final URI			URI_TEMPLATE = URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":json:");
	private static final int			PARSER_BUFFER_SIZE = 8192;
	
	private final AbstractContent		content;
	private final ResultSetMetaData		metadata;
	
	public JsonContentParser() {
		this.content = null;
		this.metadata = null;
	}
	
	protected JsonContentParser(final JsonStaxParser parser, final RsMetaDataElement[] fields) throws IOException, SyntaxException {
		this.content = new StreamContent(new Object[fields.length],
				(forData)->{
loop:				for (JsonStaxParserLexType item : parser) {
						switch (item) {
							case END_ARRAY		:
								break loop;
							case ERROR			:
								final Exception	err = parser.getLastError();
								
								throw new SQLException(err.getLocalizedMessage(),err);
							case LIST_SPLITTER	:
								break;
							case START_OBJECT	:
								try(final JsonStaxParserInterface inner = parser.nested()) {
									
									fillRow(inner,forData);
								} catch (IOException e) {
									throw new SQLException(e.getLocalizedMessage(),e);
								}
								return true;
							default:
								break;						
						}
					}
					return false;
				},
				()->{
					try{parser.close();
					} catch (IOException e) {
						throw new SQLException(e.getLocalizedMessage(),e);
					}
				}
			);
		this.metadata = new AbstractResultSetMetaData(fields,true) {
			@Override public String getTableName(int column) throws SQLException {return "table";}
			@Override public String getSchemaName(int column) throws SQLException {return "schema";}
			@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
		};
	}

	protected JsonContentParser(final Object[][] content, final RsMetaDataElement[] fields) throws IOException, SyntaxException {
		this.content = new ArrayContent(content);
		this.metadata = new AbstractResultSetMetaData(fields,true) {
			@Override public String getTableName(int column) throws SQLException {return "table";}
			@Override public String getSchemaName(int column) throws SQLException {return "schema";}
			@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
		};
	}
	
	@Override
	public boolean canServe(final URI request) {
		return Utils.canServeURI(request,URI_TEMPLATE);
	}

	@Override
	public Hashtable<String, String[]> filter(Hashtable<String, String[]> source) {
		final Hashtable<String, String[]>	result = new Hashtable<>();
		
		for (Entry<String, String[]> item : result.entrySet()) {
			if (!SQLContentUtils.OPTION_ENCODING.equals(item.getKey())) {
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
		else {
			final SyntaxTreeInterface<RsMetaDataElement>	names = new AndOrTree<>();
			
			for (int index = 0; index < content.length; index++) {
				names.placeName(content[index].getName(),index+1,content[index]);
			}
			
			if (resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
				final InputStream		is = access.openStream();
				final Reader			rdr = new BufferedInputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING));
				final JsonStaxParser	parser = new JsonStaxParser(rdr,PARSER_BUFFER_SIZE,names);
				
				if (!parser.hasNext()) {
					try{parser.close();
						return new JsonContentParser(new Object[0][],content);
					} catch (SyntaxException exc) {
						parser.close();
						throw new IOException(exc.getLocalizedMessage(),exc); 
					}
				}
				else if (parser.next() != JsonStaxParserLexType.START_ARRAY) {
					final SyntaxException	exc = new SyntaxException(parser.row(),parser.col(),"Array nesting is too big or exhausted");

					parser.close();
					throw new IOException(exc.getLocalizedMessage(),exc); 
				}
				else {
					try{return new JsonContentParser(parser,content);
					} catch (SyntaxException exc) {
						parser.close();
						throw new IOException(exc.getLocalizedMessage(),exc); 
					}
				}
			}
			else {
				final List<Object[]>		data = new ArrayList<>();
				
				try(final InputStream		is = access.openStream();
					final Reader			rdr = new BufferedInputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING));
					final JsonStaxParser	parser = new JsonStaxParser(rdr,PARSER_BUFFER_SIZE,names)) {

					for (JsonStaxParserLexType item : parser) {
						switch (item) {
							case START_ARRAY	:
								try(final JsonStaxParserInterface inner = parser.nested()) {
								}
								break;
							case LIST_SPLITTER	:
								break;
							default:
								break;
						}
					}
					
					try{return new JsonContentParser((Object[][])data.toArray(),content);
					} catch (SyntaxException exc) {
						throw new IOException(exc.getLocalizedMessage(),exc); 
					}
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

	private static void fillRow(final JsonStaxParserInterface parser, final Object[] row) throws IOException {
		int			nameIndex = 0;
		
		for (JsonStaxParserLexType item : parser) {
			switch (item) {
				case BOOLEAN_VALUE	:
					row[nameIndex] = parser.booleanValue();
					break;
				case END_ARRAY		:
					break;
				case END_OBJECT		:
					return;
				case ERROR			:
					final Exception	ex = parser.getLastError();
					
					throw new IOException(ex.getLocalizedMessage(),ex); 
				case INTEGER_VALUE	:
					row[nameIndex] = parser.intValue();
					break;
				case NAME			:
					nameIndex = parser.nameId();
					break;
				case NULL_VALUE		:
					row[nameIndex] = null;
					break;
				case REAL_VALUE		:
					row[nameIndex] = parser.realValue();
					break;
				case START_ARRAY	:
					final SyntaxException	excA = new SyntaxException(parser.row(),parser.col(),"Array nesting is too big");
					
					throw new IOException(excA.getLocalizedMessage(),excA); 
				case START_OBJECT	:
					final SyntaxException	excO = new SyntaxException(parser.row(),parser.col(),"Object nesting is too big");
					
					throw new IOException(excO.getLocalizedMessage(),excO); 
				case STRING_VALUE	:
					row[nameIndex] = parser.stringValue();
					break;
				default:
					break;
			}
		}
		final SyntaxException	excO = new SyntaxException(parser.row(),parser.col(),"Empty structure");
		
		throw new IOException(excO.getLocalizedMessage(),excO); 
	}	
}
