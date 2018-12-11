/*package chav1961.purelib.sql.fsys;

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.sql.Statement;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.sql.AbstractConnection;

class FSysConnection extends AbstractConnection {
	private final FileSystemInterface 	fsi;
	
	FSysConnection(final FileSystemInterface fsi, final boolean readOnly) {
		super(readOnly);
		this.fsi = fsi;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return new FSysMetaData(this,fsi);
	}

	@Override
	public void commit() throws SQLException {
		throw new SQLFeatureNotSupportedException("Commit is not supported for this implementation");
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		throw new SQLFeatureNotSupportedException("Rollback is not supported for this implementation");
	}

	@Override
	public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
		throw new SQLFeatureNotSupportedException("Release savepoint is not supported for this implementation");
	}

	@Override
	protected Savepoint createSavePoint(final String name) throws SQLException {
		throw new SQLFeatureNotSupportedException("Create savepoint is not supported for this implementation");
	}

	@Override
	protected Statement newStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		return new FSysStatement(this,fsi,resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	@Override
	protected PreparedStatement newPreparedStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final GeneratedKeyParameters keys) throws SQLException {
		return new FSysPreparedStatement(this,fsi,sql,resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	@Override
	protected CallableStatement newCallableStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new FSysCallableStatement(this,fsi,sql,resultSetType,resultSetConcurrency,resultSetHoldability);
	}
	
	
}
*/