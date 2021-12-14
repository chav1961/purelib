package chav1961.purelib.sql.interfaces;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.basic.URIUtils;

public enum DatabaseType {
	POSTGRESQL(URI.create("jdbc:postgresql:/")),
	UNKNOWN;
	
	private final URI	serve;
	
	private DatabaseType() {
		this.serve = null;
	}

	private DatabaseType(final URI serve) {
		this.serve = serve;
	}
	
	public static DatabaseType valueOf(final Connection conn) {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else {
			try{final URI	uri = URI.create(conn.getMetaData().getURL());
			
				for (DatabaseType item : values()) {
					if (item.serve != null && URIUtils.canServeURI(uri, item.serve)) {
						return item;
					}
				}
				return UNKNOWN;
			} catch (SQLException e) {
				return UNKNOWN;
			}
		}
	}
}
