package chav1961.purelib.sql.content;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ServiceLoader;

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
		else if (!resource.getScheme().equalsIgnoreCase(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			throw new IllegalArgumentException("Resource scheme ["+resource.getScheme()+"] is not supported. Only ["+FileSystemInterface.FILESYSTEM_URI_SCHEME+"] can be used");
		}
		else if (resource.getFragment() == null || resource.getFragment().isEmpty()) {
			throw new IllegalArgumentException("Resource ["+resource+"]: mandatory fragment part is missing!");
		}
		else {
			final URL	url = new URL((URL)null,resource.toString(),new FileSystemURLStreamHandler());
			final URI	resourceParser = URI.create(RESULTSET_PARSERS_SCHEMA+":"+resource.getFragment());

			for (ResultSetContentParser item : ServiceLoader.load(ResultSetContentParser.class)) {
				if (item.canServe(resourceParser)) {
					final ResultSetContentParser	parser = item.newInstance(url,resourceParser);

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
			throw new IOException("No any parser were found for ["+resourceParser+"] request");
		}
	}
}
