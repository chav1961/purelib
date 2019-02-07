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
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.InternalUtils;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.streams.JsonSaxParser;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;

public class JsonContentParser implements ResultSetContentParser {
	private static final String		DATA_SUFFIX = ":VARCHAR(32768)";

	private ResultSetMetaData		metadata = null;
	private final AbstractContent	content;
	private boolean					firstLineWasParsed = false;
	
	public JsonContentParser() {
		this.content = null;
	}
	
	protected JsonContentParser(final InputStream content, final String encoding) throws IOException, SyntaxException {
		final List<Object[]>		values = new ArrayList<>();
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor(new JsonSaxParser(new SaxHandler(values)));
			final Reader 	rdr = new InputStreamReader(content,encoding)) {
				
			lblp.write(rdr);
		}
		final Object[][] result = values.toArray(new Object[values.size()][]);
		
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
			return "json".equalsIgnoreCase(request.getScheme());
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
						
				return new JsonContentParser(is,queries.containsKey("encoding") ? queries.get("encoding")[0] : "UTF-8");
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

	private static class SaxHandler implements JsonSaxHandler {
		private SaxHandler(final List<Object[]> forContent) {
			
		}

		@Override
		public void startDoc() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endDoc() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startObj() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endObj() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startArr() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endArr() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startName(char[] data, int from, int len) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startName(String name) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startName(long id) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endName() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void startIndex(int index) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endIndex() throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void value(char[] data, int from, int len) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void value(String data) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void value(long data) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void value(double data) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void value(boolean data) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void value() throws ContentException {
			// TODO Auto-generated method stub
			
		}
		
	}
}
