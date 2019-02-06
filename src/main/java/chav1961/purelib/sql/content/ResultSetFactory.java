package chav1961.purelib.sql.content;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ServiceLoader;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractReadOnlyResultSet;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;

public class ResultSetFactory {
	public static final String	RESULTSET_PARSERS_SCHEMA = "rsps";
	
	public static ResultSet buildResultSet(final Statement owner, final URI resource, final int resultSetType) throws IOException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else if (!RESULTSET_PARSERS_SCHEMA.equals(resource.getScheme())) {
			throw new IllegalArgumentException("Resource scheme ["+resource.getScheme()+"] is not ["+RESULTSET_PARSERS_SCHEMA+"]");
		}
		else {
			final URI	source = URI.create(resource.getRawSchemeSpecificPart()); 
			
			for (ResultSetContentParser item : ServiceLoader.load(ResultSetContentParser.class)) {
				if (item.canServe(source)) {
					final ResultSetContentParser	parser = item.newInstance(URI.create(source.getRawSchemeSpecificPart()).toURL(),source);

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
		}
	}
}
