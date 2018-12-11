/*package chav1961.purelib.sql.fsys;

import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.sql.AbstractStatement;
import chav1961.purelib.sql.interfaces.QueryExecutor;
import chav1961.purelib.sql.util.SimpleQueryExecutor;

class FSysStatement extends AbstractStatement {
	private final FileSystemInterface	fsi;

	FSysStatement(final Connection conn, final FileSystemInterface fsi, final int type, final int concurrency, final int holdability) {
		super(conn,type,concurrency,holdability);
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else {
			this.fsi = fsi;
		}
	}
	
	@Override
	protected QueryExecutor createQueryExecutor(final String sql) throws SQLException {
		return SimpleQueryExecutor.parse(sql.toCharArray(),fsi,false,false);
	}
}
*/