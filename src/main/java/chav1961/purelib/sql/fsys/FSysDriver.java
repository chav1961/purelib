package chav1961.purelib.sql.fsys;

import java.net.URI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import chav1961.purelib.basic.PureLogger;

class FSysDriver implements Driver {
	public static final int			DRIVER_MAJOR = 0;
	public static final int			DRIVER_MINOR = 0;
	public static final String		JDBC_SUBSCHEME = "fsys";
	
	private final PureLogger		logger = new PureLogger("chav1961.purelib.sql.fsys.FSysDriver", null);
	
	public FSysDriver(){
	}

	@Override
	public Connection connect(final String url, final Properties info) throws SQLException {
		if (url == null) {
			throw new NullPointerException("URL connection string can't be null"); 
		}
		else if (info == null) {
			throw new NullPointerException("URL connection properties can't be null"); 
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public boolean acceptsURL(final String url) throws SQLException {
		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException("URL connection string can't be null or empty"); 
		}
		else {
			final URI		uri = URI.create(url);
			
			if ("jdbc".equalsIgnoreCase(uri.getScheme())) {
				final URI	uriTail = URI.create(uri.getRawSchemeSpecificPart());
				
				return JDBC_SUBSCHEME.equalsIgnoreCase(uriTail.getScheme());
			}
			else {
				return false;
			}
		}
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException("URL connection string can't be null or empty"); 
		}
		else if (info == null) {
			throw new NullPointerException("URL connection properties can't be null"); 
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public int getMajorVersion() {
		return DRIVER_MAJOR;
	}

	@Override
	public int getMinorVersion() {
		return DRIVER_MINOR;
	}

	@Override
	public boolean jdbcCompliant() {
		return false;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return logger;
	}
}
