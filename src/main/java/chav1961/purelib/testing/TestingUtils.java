package chav1961.purelib.testing;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.DebuggingException;

/**
 * <p>This class is used in JUnit tests.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @lastUpdate 0.0.5
 */
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
	
	/**
	 * <p>Create connection to database to use in JUnit tests.</p>
	 * @return test connection. Can't be null
	 * @throws DebuggingException on any errors
	 */
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

	/**
	 * <p>Create connection to database to use in JUnit tests.</p>
	 * @param loader class loader to load JDBC driver into. Can't be null
	 * @return test connection. Can't be null
	 * @throws DebuggingException on any errors
	 * @since 0.0.5
	 */
	public static Connection getTestConnection(final SimpleURLClassLoader loader) throws DebuggingException {
		final String	connString = PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_URI);
		
		try{final File	driver = PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_DRIVER, File.class);
			
			if (driver.exists() && driver.isFile() && driver.canRead() && driver.getName().endsWith(".jar")) {
				loader.addURL(driver.toURI().toURL());
				
				for (Driver item : ServiceLoader.load(Driver.class, loader)) {
					if (item.acceptsURL(connString)) {
						final Properties	props = Utils.mkProps("user", PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_USER), "password", PureLibSettings.instance().getProperty(PureLibSettings.TEST_CONNECTION_PASSWORD));
						final Connection	conn = item.connect(connString, props);
						
						if (conn != null) {
							final Map<String,String[]>	query = URIUtils.parseQuery(URIUtils.extractQueryFromURI(URI.create(connString)));
							
							if (query.containsKey("currentSchema")) {
								conn.setSchema(query.get("currentSchema")[0]);
							}
							conn.setAutoCommit(false);
							return conn;
						}
						else {
							throw new DebuggingException("Test connection failed ["+connString+"]: connection returned is null");
						}
					}
				}
				throw new DebuggingException("Test connection failed ["+connString+"]: no suitable driver found in the ["+driver.getAbsolutePath()+"]");
			}
			else {
				throw new DebuggingException("Test connection failed ["+connString+"]: JDBC driver ["+driver.getAbsolutePath()+"] not exists or not available, is not a file or it's name doens't end with '.jar'");
			}
		} catch (SQLException | MalformedURLException exc) {
			throw new DebuggingException("Test connection failed ["+connString+"]: "+exc.getLocalizedMessage());
		}
	}
	
	/**
	 * <p>Prepare test database.</p>
	 * @param sql sequence of SQL to execute. Can't be null and can't contain nulls inside
	 * @return true if all SQLs were processed successfully, false otherwise
	 * @throws DebuggingException on ay errors
	 */
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

	/**
	 * <p>Prepare test database.</p>
	 * @param conn connection to database to prepare. Can't be null
	 * @param sql sequence of SQL to execute. Can't be null and can't contain nulls inside
	 * @return true if all SQLs were processed successfully, false otherwise
	 * @throws DebuggingException on ay errors
	 * @since 0.0.5
	 */
	public static boolean prepareDatabase(final Connection conn, final String... sql) throws DebuggingException {
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
	}
}
