package chav1961.purelib.sql.content;

import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.ServiceLoader;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractReadOnlyResultSet;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;

public class ResultSetFactory {
	public static final String			RESULTSET_PARSERS_SCHEMA = "rsps";
	
	public static ResultSet buildResultSet(final Statement owner, final URI resource, final int resultSetType) throws IOException {
		final String	query;
		
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else if (!RESULTSET_PARSERS_SCHEMA.equals(resource.getScheme())) {
			throw new IllegalArgumentException("Resource scheme ["+resource.getScheme()+"] is not ["+RESULTSET_PARSERS_SCHEMA+"]");
		}
		else if ((query = URIUtils.extractQueryFromURI(resource)) == null) {
			throw new IllegalArgumentException("Resource ["+resource+"]: query string is missing in the URI!");
		}
		else {
			try{final URI		source = URI.create(resource.getRawSchemeSpecificPart());
			
				for (ResultSetContentParser item : ServiceLoader.load(ResultSetContentParser.class)) {
					if (item.canServe(resource)) {
						final Hashtable<String,String[]>	content = Utils.parseQuery(query);
						final RsMetaDataElement[]			fields = SQLContentUtils.buildMetadataFromQueryString(query,item.filter(content));
						final ResultSetContentParser		parser = item.newInstance(URIUtils.removeQueryFromURI(URI.create(source.getRawSchemeSpecificPart())).toURL()
																	,resultSetType
																	,fields
																	,SQLContentUtils.extractOptions(content,item.filter(content)));
	
						return new AbstractReadOnlyResultSet(parser.getMetaData(),resultSetType) {
							@Override
							public Statement getStatement() throws SQLException {
								return owner;
							}
							
							@Override
							protected AbstractContent getContent() {
								return parser.getAccessContent();
							}
						};
					}
				}
				throw new IOException("No any parser were found for ["+resource+"] request");
			} catch (SyntaxException e) {
				throw new IOException(e.getMessage(),e);
			}
		}
	}
}
