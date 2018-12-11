/*package chav1961.purelib.sql.fsys;

import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.sql.AbstractCallableStatement;
import chav1961.purelib.sql.interfaces.QueryExecutor;
import chav1961.purelib.sql.util.SimpleQueryExecutor;

class FSysCallableStatement extends AbstractCallableStatement {
	private final FileSystemInterface 	fsi;
	
	public FSysCallableStatement(Connection conn, FileSystemInterface fsi, String sql, int type, int concurrency, int holdability) throws SQLException {
		super(conn, sql, type, concurrency, holdability);
		if (fsi == null) {
			throw new IllegalArgumentException("File system interface can't be null or empty");
		}
		else if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty");
		}
		else {
			this.fsi = fsi;
		}
	}

	@Override
	protected QueryExecutor createQueryExecutor(final String sql) throws SQLException {
		return SimpleQueryExecutor.parse(sql.toCharArray(),fsi,true,true);
	}
}
*/