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

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.StreamContent;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.interfaces.JsonStaxParserInterface;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class JsonContentParser implements ResultSetContentParser {
	private static final URI			URI_TEMPLATE = URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":json:/");
	private static final int			PARSER_BUFFER_SIZE = 8192;
	
	private final AbstractContent		content;
	private final ResultSetMetaData		metadata;
	
	public JsonContentParser() {
		this.content = null;
		this.metadata = null;
	}
	
	protected JsonContentParser(final JsonStaxParser parser, final ResultSetMetaData metadata) throws IOException, SyntaxException, SQLException {
		this.content = new StreamContent(new Object[metadata.getColumnCount()],
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
		this.metadata = metadata;
	}

	protected JsonContentParser(final Object[][] content, final RsMetaDataElement[] fields) throws IOException, SyntaxException {
		this.content = new ArrayContent(content);
		this.metadata = new FakeResultSetMetaData(fields,true);
	}
	
	@Override
	public boolean canServe(final URI request) {
		return URIUtils.canServeURI(request,URI_TEMPLATE);
	}

	@Override
	public Hashtable<String, String[]> filter(Hashtable<String, String[]> source) {
		final Hashtable<String, String[]>	result = new Hashtable<>();
		
		for (Entry<String, String[]> item : source.entrySet()) {
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
		else if (resultSetType != ResultSet.TYPE_FORWARD_ONLY && resultSetType != ResultSet.TYPE_SCROLL_SENSITIVE && resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE) {
			throw new IllegalArgumentException("Illegal result set type ["+resultSetType+"]. Can be ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_SENSITIVE or ResultSet.TYPE_SCROLL_INSENSITIVE only");
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content can't be null or empty array");
		}
		else if (options == null) {
			throw new NullPointerException("Options can't be null");
		}
		else {
			final SyntaxTreeInterface<RsMetaDataElement>	names = new AndOrTree<>();
			
			for (int index = 0; index < content.length; index++) {
				names.placeName(content[index].getName(),index,content[index]);
			}
			
			if (resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
				final InputStream		is = access.openStream();
				final Reader			rdr = new InputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING));
				final JsonStaxParser	parser = new JsonStaxParser(rdr,PARSER_BUFFER_SIZE,names);
				
				if (!parser.hasNext()) {
					try{parser.close();
						return new JsonContentParser(new Object[0][],content);
					} catch (SyntaxException exc) {
						throw new IOException(exc.getLocalizedMessage(),exc); 
					}
				}
				else if (parser.next() != JsonStaxParserLexType.START_ARRAY) {
					parser.close();
					throwIOException(parser.row(),parser.col(),"Array nesting is too big or exhausted");
					return null;
				}
				else {
					try{return new JsonContentParser(parser,new FakeResultSetMetaData(content, true));
					} catch (SyntaxException | SQLException exc) {
						parser.close();
						throw new IOException(exc.getLocalizedMessage(),exc); 
					}
				}
			}
			else {
				final List<Object[]>		data = new ArrayList<>();
				
				try(final InputStream		is = access.openStream();
					final Reader			rdr = new InputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING));
					final JsonStaxParser	parser = new JsonStaxParser(rdr,PARSER_BUFFER_SIZE,names)) {
					int						nesting = 0;

					for (JsonStaxParserLexType item : parser) {
						final Object[]		forData = new Object[content.length];
						
						switch (item) {
							case START_ARRAY	:
								if (nesting == 0) {
									nesting++;
								}
								else {
									throwIOException(parser.row(),parser.col(),"Array nesting is too big or exhausted");
								}
								break;
							case START_OBJECT	:
								if (nesting == 1) {
									try(final JsonStaxParserInterface inner = parser.nested()) {
										fillRow(inner,forData);
									}
									data.add(forData);
								}
								else {
									throwIOException(parser.row(),parser.col(),"Structure nesting is too big or exhausted");
								}
								break;
							case LIST_SPLITTER	:
								break;
							default:
								break;
						}
					}
					
					try{return new JsonContentParser(data.toArray(new Object[data.size()][]),content);
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
					throwIOException(parser.row(),parser.col(),"Array nesting is too big");
				case START_OBJECT	:
					throwIOException(parser.row(),parser.col(),"Object nesting is too big");
				case STRING_VALUE	:
					row[nameIndex] = parser.stringValue();
					break;
				default:
					break;
			}
		}
		throwIOException(parser.row(),parser.col(),"Empty structure");
	}
	
	private static void throwIOException(final long row, final long col, final String message) throws IOException {
		final SyntaxException	excO = new SyntaxException(row,col,message);
		
		throw new IOException(excO.getLocalizedMessage(),excO); 
	}
}
