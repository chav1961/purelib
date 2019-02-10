package chav1961.purelib.sql.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.InternalUtils;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;

public class CsvContentParser implements ResultSetContentParser {
	private static final String		DATA_SUFFIX = ":VARCHAR(32768)";

	private final char				splitter;
	private ResultSetMetaData		metadata = null;
	private final AbstractContent	content;
	private boolean					firstLineWasParsed = false;
	
	public CsvContentParser() {
		this.splitter = ',';
		this.content = null;
	}
	
	protected CsvContentParser(final InputStream content, final String encoding, final char splitter) throws IOException, SyntaxException {
		final List<Object[]>		values = new ArrayList<>();
		
		this.splitter = splitter;
		try(final LineByLineProcessor	lblp = new LineByLineProcessor(new LineByLineProcessorCallback() {
											@Override
											public void processLine(long displacement, int lineNo, char[] data, int from, int length) throws IOException, SyntaxException {
												processLineInternal(lineNo,data,from,length,values);
											}
										}
									);
			final Reader 	rdr = new InputStreamReader(content,encoding)) {
				
			lblp.write(rdr);
		}
		final Object[][] result = new Object[values.size()][];
		
		for (int index = 0; index < result.length; index++) {
			result[index] = values.get(index);
		}
		values.clear();
		this.content = new ArrayContent((Object[][])result);
	}
	
	@Override
	public boolean canServe(final URI request) {
		if (request == null) {
			throw new NullPointerException("Request to serve can't be null");
		}
		else if (request.getScheme() == null) {
			throw new IllegalArgumentException("Request ["+request+"] is not absolute (scheme is missing)");
		}
		else {
			return "csv".equalsIgnoreCase(request.getScheme());
		}
	}

	@Override
	public ResultSetContentParser newInstance(final URL access, final URI request) throws IOException {
		if (access == null) {
			throw new NullPointerException("Access URL can't be null");
		}
		else if (request == null) {
			throw new NullPointerException("Request URI can't be null");
		}
		else {
			try(final InputStream	is = access.openStream()) {
				final Hashtable<String,String[]> 	queries = NanoServiceFactory.parseQuery(request.getQuery()); 
						
				return new CsvContentParser(is
						,queries.containsKey("encoding") ? queries.get("encoding")[0] : "UTF-8"
						,queries.containsKey("splitter") ? queries.get("splitter")[0].charAt(0) : splitter); 
			} catch (SyntaxException e) {
				throw new IOException("Syntax error in content: "+e.getLocalizedMessage());
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

	protected void processLineInternal(final int lineNo, final char[] data, int from, final int length, final List<Object[]> content) throws IOException, SyntaxException {
		final int	to = from + length, begin = from;
		
		if (!firstLineWasParsed) {
			final List<String>	columns = new ArrayList<>(), metaData = new ArrayList<>();
			
			processSingleLine(lineNo,data,from,length,columns);
			for (String item : columns) {
				metaData.add(item+DATA_SUFFIX);
			}
			this.metadata = new AbstractResultSetMetaData(InternalUtils.prepareMetadata(metaData.toArray(new String[metaData.size()])),false) {
									@Override public String getTableName(int column) throws SQLException {return "table";}
									@Override public String getSchemaName(int column) throws SQLException {return "schema";}
									@Override public String getCatalogName(int column) throws SQLException {return "catalog";}
								};
 			firstLineWasParsed = true;
		}
		else {
			final List<String>	record = new ArrayList<>();
			
			processSingleLine(lineNo,data,from,length,record);
			content.add(record.toArray(new Object[record.size()]));
		}
	}

	protected void processSingleLine(final int lineNo, final char[] data, int from, final int length, final List<String> result) throws IOException, SyntaxException {
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
	
	private int skipBlank(final char[] data, int from) {
		while (from < data.length && data[from] <= ' ' && data[from] != '\n') {
			from++;
		}
		return from;
	}
}
