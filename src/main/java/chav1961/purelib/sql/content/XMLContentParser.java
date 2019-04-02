package chav1961.purelib.sql.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.streams.JsonSaxParser;

public class XMLContentParser implements ResultSetContentParser {
	private static final URI		URI_TEMPLATE = URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":xml:");
	
	private final AbstractContent	content;
	private ResultSetMetaData		metadata = null;
	
	public XMLContentParser() {
		this.content = null;
	}
	
	protected XMLContentParser(final InputStream content, final String encoding) throws IOException, SyntaxException {
		final List<Object[]>		values = new ArrayList<>();
//		final SaxHandler			handler = new SaxHandler(values);
		
//		try(final LineByLineProcessor	lblp = new LineByLineProcessor(new JsonSaxParser(handler));
//			final Reader 	rdr = new InputStreamReader(content,encoding)) {
//				
//			lblp.write(rdr);
//		}
		final Object[][] result = values.toArray(new Object[values.size()][]);
		
		values.clear();
		this.content = new ArrayContent((Object[][])result);
	}

	@Override
	public boolean canServe(final URI request) {
		return Utils.canServeURI(request,URI_TEMPLATE);
	}

	@Override
	public Hashtable<String, String[]> filter(Hashtable<String, String[]> source) {
		final Hashtable<String, String[]>	result = new Hashtable<>();
		
		for (Entry<String, String[]> item : result.entrySet()) {
			if (!"encoding".equals(item.getKey())) {
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
			throw new IllegalArgumentException("Content can't be null or empty array");
		}
		else {
			try(final InputStream	is = access.openStream()) {
						
				return new JsonContentParser(is,options.containsKey("encoding") ? options.getProperty("encoding",String.class) : "UTF-8",content);
			} catch (SyntaxException e) {
				throw new IOException("Syntax error in content: "+e.getLocalizedMessage());
			}
		}
	}

	@Override
	public ResultSetMetaData getMetaData() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractContent getAccessContent() {
		// TODO Auto-generated method stub
		return null;
	}
}
