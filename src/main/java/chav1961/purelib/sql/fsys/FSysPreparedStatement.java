/*package chav1961.purelib.sql.fsys;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.sql.AbstractPreparedStatement;
import chav1961.purelib.sql.interfaces.QueryExecutor;
import chav1961.purelib.sql.util.SimpleQueryExecutor;

class FSysPreparedStatement extends AbstractPreparedStatement implements PreparedStatement {
	private final FileSystemInterface	fsi;

	FSysPreparedStatement(final Connection conn, final FileSystemInterface fsi, final String sql, final int type, final int concurrency, final int holdability) throws SQLException {
		super(conn, sql, type, concurrency, holdability);
		if (fsi == null) {
			throw new IllegalArgumentException("File system interface can't be null or empty");
		}
		else {
			this.fsi = fsi; 
		}
	}
	
	@Override
	protected QueryExecutor createQueryExecutor(final String sql) throws SQLException {
		return SimpleQueryExecutor.parse(sql.toCharArray(),fsi,true,false);
	}
}
*/