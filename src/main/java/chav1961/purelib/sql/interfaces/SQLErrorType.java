package chav1961.purelib.sql.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public enum SQLErrorType {
	NOT_EXISTS(new ErrorDescriptor(DatabaseType.POSTGRESQL, "42P01", "3F000")),
	PERMISSION(new ErrorDescriptor(DatabaseType.POSTGRESQL, "42501")),
	OTHER;

	private final ErrorDescriptor[]	desc;
	
	private SQLErrorType() {
		this.desc = new ErrorDescriptor[0];
	}
	
	private SQLErrorType(final ErrorDescriptor... desc) {
		this.desc = desc;
	}
	
	public static SQLErrorType valueOf(final Connection conn, final SQLException exc) {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (exc == null) {
			throw new NullPointerException("SQL exception can't be null");
		}
		else {
			final DatabaseType	dbType = DatabaseType.valueOf(conn);
			final String		state = exc.getSQLState();
			
			for (SQLErrorType item : values()) {
				for (ErrorDescriptor desc : item.desc) {
					if (desc.dbType == dbType) {
						for (String msg : desc.errorCodes) {
							if (state.equals(msg)) {
								return item;
							}
						}
					}
				}
			}
			return OTHER;
		}
	}
	
	private static class ErrorDescriptor {
		private final DatabaseType	dbType;
		private final String[]		errorCodes;
		
		private ErrorDescriptor(final DatabaseType dbType, final String... errorCodes) {
			this.dbType = dbType;
			this.errorCodes = errorCodes;
		}

		@Override
		public String toString() {
			return "ErrorDescriptor [dbType=" + dbType + ", errorCodes=" + Arrays.toString(errorCodes) + "]";
		}
	}
}
