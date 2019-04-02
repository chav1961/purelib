package chav1961.purelib.sql.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;
import chav1961.purelib.streams.JsonSaxParser;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;

public class JsonContentParser implements ResultSetContentParser {
	private static final URI		URI_TEMPLATE = URI.create(ResultSetFactory.RESULTSET_PARSERS_SCHEMA+":json:");
	
	private final SyntaxTreeInterface<Object>	names = new AndOrTree<>();
	private final AbstractContent		content;
	private final RsMetaDataElement[]	fields;
	private ResultSetMetaData			metadata = null;
	
	public JsonContentParser() {
		this.content = null;
		this.fields = null;
	}
	
	protected JsonContentParser(final InputStream content, final String encoding, final RsMetaDataElement[] fields) throws IOException, SyntaxException {
		final List<Object[]>		values = new ArrayList<>();

		for (int index = 0; index < fields.length; index++) {
			names.placeName(fields[index].getName(), index, null);
		}
		
		final Object[][] result = values.toArray(new Object[values.size()][]);
		
		values.clear();
		this.content = new ArrayContent((Object[][])result);
		this.fields = null;
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
			throw new NullPointerException("Content can't be null or empty array");
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

}
