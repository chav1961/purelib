package chav1961.purelib.sql;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ServiceLoader;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

/**
 * <p>This class is an utility class containing useful methods to work with JDBC drivers.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
public class JDBCUtils {
	public static final String	KEY_JDBC_USER = "user";
	public static final String	KEY_JDBC_PASSWORD = "password";
	
	public static final String	KEY_ILLEGAL_DRIVER = "JDBCUtils.illegal.driver";
	public static final String	KEY_DATABASE_ERROR = "JDBCUtils.database.error";
	public static final String	KEY_IO_ERROR = "JDBCUtils.io.error";
	public static final String	KEY_VALIDATE_NOT_ABSOLUTE = "JDBCUtils.validate.notAbsolute";
	public static final String	KEY_VALIDATE_NOT_JDBC = "JDBCUtils.validate.notJDBC";
	public static final String	KEY_VALIDATE_SUBSCHEME_MISSING = "JDBCUtils.validate.subschemeMissing";
	/**
	 * <p>Load JDBC driver by it's name<p>
	 * @param loader loader to load JDBC driver to. Can't be null
	 * @param jdbcDriver JDBC driver file to load driver from. Can't be null
	 * @return jdbc driver loaded. Can't be null
	 * @throws NullPointerException when any parameter is null
	 * @throws ContentException on any errors when loading JDBC driver
	 */
	public static Driver loadJdbcDriver(final SimpleURLClassLoader loader, final File jdbcDriver) throws NullPointerException, ContentException {
		if (loader == null) {
			throw new NullPointerException("Loader to load driver into can't be null");
		}
		else if (jdbcDriver == null) {
			throw new NullPointerException("JDBC driver file can't be null");
		}
		else {
			try{loader.addURL(jdbcDriver.toURI().toURL());
			
				for(Driver	drv : ServiceLoader.load(Driver.class, loader)) {
					return drv;
				}		
				throw new ContentException("No any JDBC drivers found in ["+jdbcDriver.getAbsolutePath()+"]");
			} catch (MalformedURLException e) {
				throw new ContentException(e);
			}
		}
	}
	
	/**
	 * <p>Get connection from JDBC driver.</p>
	 * @param driver driver to get connection from. Can't be null
	 * @param connURI connection string. Can't be null
	 * @param user user name to get connection for. Can't be null
	 * @param password password for user. Can't be null
	 * @return connection created. Can't be null
	 * @throws SQLException on any SQL error
	 */
	public static Connection getConnection(final Driver driver, final URI connURI, final String user, final char[] password) throws NullPointerException, SQLException {
		if (driver == null) {
			throw new NullPointerException("JDBC driver can't be null");
		}
		else if (connURI == null) {
			throw new NullPointerException("Connection URI can't be null");
		}
		else if (user == null || user.isEmpty()) {
			throw new IllegalArgumentException("User can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else {
			final Connection	conn = driver.connect(connURI.toString(), Utils.mkProps(KEY_JDBC_USER, user, KEY_JDBC_PASSWORD, new String(password)));
			
			if (conn != null) {
				return conn;
			}
			else {
				throw new SQLException("Driver returns null for the connection string ["+connURI+"] and user ["+user+"]");
			}
		}
	}
	
	/**
	 * <p>Test connection parameters.</p>
	 * @param jdbcDriver JDBC driver file to load driver from. Can't be null
	 * @param connURI connection string. Can't be null
	 * @param user user name to get connection for. Can't be null
	 * @param password password for user. Can't be null
	 * @param logger logger to send error messages to. Can't be null
	 * @return true if all is ok, false otherwise
	 * @throws IllegalArgumentException on user or password is null or empty
	 * @throws NullPointerException on any other parameter is null
	 */
	public static boolean testConnection(final File jdbcDriver, final URI connURI, final String user, final char[] password, final LoggerFacade logger) throws NullPointerException, IllegalArgumentException {
		if (jdbcDriver == null) {
			throw new NullPointerException("JDBC driver can't be null");
		}
		else if (connURI == null) {
			throw new NullPointerException("Connection URI can't be null");
		}
		else if (user == null || user.isEmpty()) {
			throw new IllegalArgumentException("User can't be null or empty");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Password can't be null or empty array");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			try(final SimpleURLClassLoader	sucl = new SimpleURLClassLoader(new URL[] {jdbcDriver.toURI().toURL()})) {
				for(Driver	drv : ServiceLoader.load(Driver.class, sucl)) {
					final String	connString = connURI.toString();
					
					try{if (drv.acceptsURL(connString)) {
							try(final Connection	conn = drv.connect(connString, Utils.mkProps(KEY_JDBC_USER, user, KEY_JDBC_PASSWORD, new String(password)))) {
								if (conn != null) {
									return true;
								}
							}
						}
					} catch (SQLException e) {
						logger.message(Severity.error, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(KEY_DATABASE_ERROR, e.getLocalizedMessage()));
						return false;
					}
				}		
				logger.message(Severity.error, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(KEY_ILLEGAL_DRIVER, jdbcDriver.getAbsolutePath(), connURI.toString()));
				return false;
			} catch (IOException e) {
				logger.message(Severity.error, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(KEY_IO_ERROR, e.getLocalizedMessage()));
				return false;
			}
		}
	}

	/**
	 * <p>Is JDBC driver file valid</p>
	 * @param jdbcDriver file with JDBC driver. Can't be null
	 * @param logger logger to send error messages to. Can't be null
	 * @return true if all is ok, false otherwise
	 * @throws NullPointerException on any argument is null
	 */
	public static boolean isJDBCDriverValid(final File jdbcDriver, final LoggerFacade logger)  throws NullPointerException {
		if (jdbcDriver == null) {
			throw new NullPointerException("JDBC driver file can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			try(final SimpleURLClassLoader	sucl = new SimpleURLClassLoader(new URL[] {jdbcDriver.toURI().toURL()})) {
				for(Driver	drv : ServiceLoader.load(Driver.class, sucl)) {
					return true;
				}
			} catch (IOException e) {
				logger.message(Severity.error, "I/O error reading file ["+jdbcDriver.getAbsolutePath()+"] : "+e.getLocalizedMessage());
			}
			return false;
		}		
	}
	
	/**
	 * <p>Is connection string valid</p>
	 * @param connURI String to test. Can't be null or empty
	 * @param logger logger to send error messages to. Can't be null
	 * @return true if all is ok, false otherwise
	 * @throws IllegalArgumentException on connection string is null or empty
	 * @throws NullPointerException on logger is null
	 */
	public static boolean isConnectionStringValid(final URI connURI, final LoggerFacade logger)  throws NullPointerException, IllegalArgumentException {
		if (connURI == null) {
			throw new NullPointerException("Connection URI can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			if (!connURI.isAbsolute()) {
				logger.message(Severity.error, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(KEY_VALIDATE_NOT_ABSOLUTE));
				return false;
			}
			else if (!"jdbc".equalsIgnoreCase(connURI.getScheme())) {
				logger.message(Severity.error, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(KEY_VALIDATE_NOT_JDBC));
				return false;
			}
			else if (!URI.create(connURI.getSchemeSpecificPart()).isAbsolute()) {
				logger.message(Severity.error, ()->PureLibSettings.PURELIB_LOCALIZER.getValue(KEY_VALIDATE_SUBSCHEME_MISSING));
				return false;
			}
			else {
				return true;
			}
		}
	}
}
