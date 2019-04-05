package chav1961.purelib.sql.content;

import java.io.BufferedReader;
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

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.StreamContent;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;

public class CsvContentParser implements ResultSetContentParser {
	private static final URI			URI_TEMPLATE = URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":csv:/");

	private ResultSetMetaData			metadata;
	private final AbstractContent		content;
	
	public CsvContentParser() {
		this.content = null;
		this.metadata = null;
	}
	
	protected CsvContentParser(final BufferedReader content, final char splitter, final RsMetaDataElement[] fields, final int[] moveTo) throws IOException, SyntaxException {
		this.content = new StreamContent(new Object[fields.length],
			(forData)->{
				try{final String	line = content.readLine();
					
					if (line == null) {
						return false;
					}
					else {
						final char[]	lineContent = (line+"\n").toCharArray();
						
						try{final Object[]	result = processLineInternal(0,lineContent,0,lineContent.length,splitter,moveTo);
						
							System.arraycopy(result, 0, forData, 0, forData.length);
						} catch (SyntaxException e) {
							throw new SQLException(e.getMessage(),e); 
						}
						return true;
					}
				} catch (IOException e) {
					throw new SQLException(e.getLocalizedMessage(),e);
				}
			},
			()->{
				try{content.close();
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

	protected CsvContentParser(final Object[][] content, final RsMetaDataElement[] fields) throws IOException, SyntaxException {
		this.content = new ArrayContent((Object[][])content);
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
		
		for (Entry<String, String[]> item : source.entrySet()) {
			if (!SQLContentUtils.OPTION_ENCODING.equals(item.getKey()) && !SQLContentUtils.OPTION_SEPARATOR.equals(item.getKey()) && !SQLContentUtils.OPTION_FIRST_LINE_ARE_NAMES.equals(item.getKey())) {
				result.put(item.getKey(),item.getValue());
			}
		}
		return result;
	}
	
	@Override
	public ResultSetContentParser newInstance(final URL access, final int resultSetType, final RsMetaDataElement[] content, final SubstitutableProperties options) throws IOException, IllegalArgumentException, NullPointerException {
		if (access == null) {
			throw new NullPointerException("Access URL can't be null");
		}
		else if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content can't be null or empty array");
		}
		else if (resultSetType != ResultSet.TYPE_FORWARD_ONLY && resultSetType != ResultSet.TYPE_SCROLL_SENSITIVE && resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE) {
			throw new IllegalArgumentException("Illegal result set type ["+resultSetType+"]. Can be ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_SENSITIVE or ResultSet.TYPE_SCROLL_INSENSITIVE only");
		}
		else {
			final String	separator = options.getProperty(SQLContentUtils.OPTION_SEPARATOR,String.class,",");
			final boolean	processNames = options.getProperty(SQLContentUtils.OPTION_FIRST_LINE_ARE_NAMES,boolean.class,"true"); 
			final int[]		moveTo = new int[content.length];
			
			if (separator.length() == 0) {
				throw new IOException("Splitter option is empty in the query string!"); 
			}
			final char				splitter = separator.charAt(0);

			if (!processNames) {
				for (int index = 0; index < moveTo.length; index++) {
					moveTo[index] = index;
				}
			}
			
			if (resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
				final InputStream		is = access.openStream();
				final Reader			rdr = new InputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING));
				final BufferedReader	brdr = new BufferedReader(rdr); 
				
				if (processNames) {
					final String		line = brdr.readLine();
					
					if (line == null) {
						brdr.close();
						throw new IOException("Empty source was detected, but 'processing first line as names' as required!"); 
					}
					else {
						final char[]	lineContent = (line+"\n").toCharArray();
						
						try{processFirstLineInternal(0,lineContent,0,lineContent.length,splitter,content,moveTo);
						} catch (SyntaxException e) {
							brdr.close();
							throw new IOException(e.getMessage(),e); 
						}
					}
				}
				
				try{return new CsvContentParser(brdr,splitter,content,moveTo);
				} catch (SyntaxException e) {
					throw new IOException(e.getLocalizedMessage(),e);
				}
			}
			else {
				try(final InputStream		is = access.openStream()) {
					final List<Object[]>	values = new ArrayList<>();
					final boolean[]			completed = new boolean[]{false};
					
					try(final LineByLineProcessor	lblp = new LineByLineProcessor(new LineByLineProcessorCallback() {
														@Override
														public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
															if (lineNo == 1 && processNames) {
																processFirstLineInternal(lineNo,data,from,length,splitter,content,moveTo);
																completed[0] = true;
															}
															else {
																values.add(processLineInternal(lineNo,data,from,length,splitter,moveTo));
															}
														}
													}
												);
						final Reader 	rdr = new InputStreamReader(is,options.getProperty(SQLContentUtils.OPTION_ENCODING,String.class,SQLContentUtils.DEFAULT_OPTION_ENCODING))) {
							
						lblp.write(rdr);
					}
					if (!completed[0]) {
						throw new IOException("Empty source was detected, but 'processing first line as names' as required!"); 
					}
					
					final Object[][] result = new Object[values.size()][];
					
					for (int index = 0; index < result.length; index++) {
						result[index] = values.get(index);
					}
					values.clear();
					
					return new CsvContentParser(result,content);
				} catch (SyntaxException e) {
					throw new IOException("Syntax error in content: "+e.getLocalizedMessage());
				}
			}
		}
	}

	@Override
	public ResultSetMetaData getMetaData() throws IOException {
		if (metadata == null) {
			throw new IOException("No one lines in the input stream or the same forst line not contants colimns description"); 
		}
		else {
			return metadata;
		}
	}

	@Override
	public AbstractContent getAccessContent() {
		return content;
	}

	private static void processFirstLineInternal(final int lineNo, final char[] data, int from, final int length, final char splitter, final RsMetaDataElement[] fields, final int[] moves) throws IOException, SyntaxException {
		final List<String>	columns = new ArrayList<>();
		
		processSingleLine(lineNo,data,from,length,splitter,columns);
loop:	for (int index = 0, maxIndex = columns.size(); index < maxIndex; index++) {
			final String	name = columns.get(index);
			
			for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
				if (name.equals(fields[fieldIndex].getName())) {
					moves[index] = fieldIndex;
					continue loop;
				}
			}
			throw new SyntaxException(0,0,"Field type description for ["+name+"] is missing in the query string");
		}
	}
	
	private static Object[] processLineInternal(final int lineNo, final char[] data, int from, final int length, final char splitter, final int[] moves) throws IOException, SyntaxException {
		final List<String>	record = new ArrayList<>();
		
		processSingleLine(lineNo,data,from,length,splitter,record);
		final Object[]	row = new Object[moves.length];
		
		for (int index = 0, maxSize = record.size(); index < moves.length; index++) {
			if (moves[index] < maxSize) {
				row[index] = record.get(moves[index]);
			}
			else {
				row[index] = null;
			}
		}
		return row;
	}

	private static void processSingleLine(final int lineNo, final char[] data, int from, final int length, final char splitter, final List<String> result) throws IOException, SyntaxException {
		final int	to = from + length, begin = from;
		
		from--;
		do {final int	startName, startParsing = from+1;
			int			endName;
			
			from = skipBlank(data,from+1);
		
			if (from < to && data[from] == '\"') {
				boolean	duplicatedQuote = false;
				
				startName = ++from;
				while (from < to) {
					if (data[from] == '\"') {
						if (from >= to || data[from+1] != '\"') {
							break;
						}
						else {
							duplicatedQuote = true;
							from += 2;
						}
					}
					else {
						from++;
					}
				}
				if (from < to && data[from] == '\"') {
					final String	name = new String(data,startName,from-startName);
					
					result.add(duplicatedQuote ? name.replace("\"\"","\"") : name);
					from++;
				}
				else {
					throw new SyntaxException(lineNo,from-begin,"Unpaired quote in the column name");
				}
			}
			else {
				startName = from;
				while (from < to && data[from] != splitter && data[from] != '\n') {
					from++;
				}
				endName = from - 1;
				while(endName > startParsing && data[endName] <= ' ' && data[endName] != '\n') {
					endName--;
				}
				if (endName == startParsing) {
					throw new SyntaxException(lineNo,endName-begin,"Empty column name in the input stream");
				}
				else {
					result.add(new String(data,startName,endName-startName+1));
				}
			}
			from = skipBlank(data,from);
		} while (from < to && data[from] == splitter);
	}
	
	private static int skipBlank(final char[] data, int from) {
		while (from < data.length && data[from] <= ' ' && data[from] != '\n') {
			from++;
		}
		return from;
	}
}
