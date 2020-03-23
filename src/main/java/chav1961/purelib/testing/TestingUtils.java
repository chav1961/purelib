package chav1961.purelib.testing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.DebuggingException;

public class TestingUtils {
	public static PrintStream err() {
//		if ("true".equals(System.getProperty("suppress.junit.trace"))) {
			return new PrintStream(new OutputStream() {
				@Override public void write(int b) throws IOException {}
			});
//		}
//		else {
//			return System.err;
//		}
	}
	
	public static Connection getTestConnection() throws DebuggingException {
		try{Class.forName(PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_DRIVER));
		
			final URI			connString = URI.create(PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_URI));

			final Connection	conn = DriverManager.getConnection(connString.toString()
								,PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_USER)
								,PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_PASSWORD));
			final Map<String,String[]>	query = URIUtils.parseQuery(URIUtils.extractQueryFromURI(connString));
			
			if (query.containsKey("currentSchema")) {
				conn.setSchema(query.get("currentSchema")[0]);
			}
			return conn;
		} catch (ClassNotFoundException | SQLException exc) {
			throw new DebuggingException("Test connection failed: ["+exc.getClass().getCanonicalName()+"]: "+exc.getLocalizedMessage());
		}
	}
	
	public static boolean prepareDatabase(final String... sql) throws DebuggingException {
		try (final Connection	conn = TestingUtils.getTestConnection()) {
			try(final Statement	stmt = conn.createStatement()) {
				boolean			allOK = true;
				
				for (String item : sql) {
					try {
						stmt.executeUpdate(item);
					} catch (SQLException exc) {
						allOK = false;
					}
				}
				return allOK;
			} catch (SQLException e) {
				throw new DebuggingException(e.getLocalizedMessage(),e);
			}
		} catch (SQLException e) {
			throw new DebuggingException(e.getLocalizedMessage(),e);
		}
	}
}
