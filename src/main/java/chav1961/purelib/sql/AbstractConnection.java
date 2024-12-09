package chav1961.purelib.sql;

import java.net.URI;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

abstract class AbstractConnection implements Connection {
	protected final Properties				props = new Properties();
	protected final Map<String, Class<?>>	types = new HashMap<>();

	URI										connString;
	String									user;
	private boolean							closed = false, readOnly = false, autoCommit;
	private String							catalog = null, schema = null;
	private int								isolationLevel = Connection.TRANSACTION_NONE, networkTimeout = 0;
	
	protected AbstractConnection(final URI connString, final String user, final boolean readOnly) {
		this.connString = connString;
		this.user = user;
		this.readOnly = readOnly;
	}

	@Override public abstract DatabaseMetaData getMetaData() throws SQLException;
	@Override public abstract void commit() throws SQLException;
	@Override public abstract void rollback(final Savepoint savepoint) throws SQLException;
	@Override public abstract void releaseSavepoint(final Savepoint savepoint) throws SQLException;
	protected abstract Savepoint createSavePoint(final String name) throws SQLException;
	protected abstract Statement newStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException;
	protected abstract PreparedStatement newPreparedStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final GeneratedKeyParameters keys) throws SQLException;
	protected abstract CallableStatement newCallableStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException;
	
	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Interface to convert to can't ne null");
		}
		else {
			return iface.cast(this);
		}
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Interface to convert to can't ne null");
		}
		else {
			return iface.isAssignableFrom(this.getClass());
		}
	}

	@Override
	public Statement createStatement() throws SQLException {
		return createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	public PreparedStatement prepareStatement(final String sql) throws SQLException {
		return prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	public CallableStatement prepareCall(final String sql) throws SQLException {
		return prepareCall(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	public String nativeSQL(final String sql) throws SQLException {
		return sql;
	}

	@Override
	public void setAutoCommit(final boolean autoCommit) throws SQLException {
		this.autoCommit = autoCommit;
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return autoCommit;
	}

	@Override
	public void rollback() throws SQLException {
		rollback(null);
	}

	@Override
	public void close() throws SQLException {
		this.closed = true;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	@Override
	public void setReadOnly(final boolean readOnly) throws SQLException {
		this.readOnly = readOnly;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return readOnly;
	}

	@Override
	public void setCatalog(final String catalog) throws SQLException {
		this.catalog = catalog;
	}

	@Override
	public String getCatalog() throws SQLException {
		return this.catalog;
	}

	@Override
	public void setTransactionIsolation(final int level) throws SQLException {
		if (level != Connection.TRANSACTION_NONE && level != Connection.TRANSACTION_READ_COMMITTED && level != Connection.TRANSACTION_READ_UNCOMMITTED  && level != Connection.TRANSACTION_REPEATABLE_READ && level != Connection.TRANSACTION_SERIALIZABLE) {
			throw new IllegalArgumentException("Illegal transaction isolation level ["+level+"]. Only [TRANSACTION_NONE], [TRANSACTION_READ_COMMITTED], [TRANSACTION_READ_UNCOMMITTED], [TRANSACTION_REPEATABLE_READ] and [TRANSACTION_SERIALIZABLE] can be used");
		}
		else {
			this.isolationLevel = level;
		}
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return this.isolationLevel;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
	}

	@Override
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
		return createStatement(resultSetType, resultSetConcurrency, ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
		return prepareStatement(sql, resultSetType, resultSetConcurrency, ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	@Override
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
		return prepareCall(sql, resultSetType, resultSetConcurrency, ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return types;
	}

	@Override
	public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
		if (map == null) {
			throw new NullPointerException("Map can't be null"); 
		}
		else {
			types.putAll(map);
		}
	}

	@Override
	public void setHoldability(final int holdability) throws SQLException {
		checkResultSetHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return createSavePoint(null);
	}

	@Override
	public Savepoint setSavepoint(final String name) throws SQLException {
		return createSavePoint(name);
	}

	@Override
	public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		checkResultSetType(resultSetType);
		checkResultSetConcurrency(resultSetConcurrency);
		checkResultSetHoldability(resultSetHoldability);
		return newStatement(resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		checkResultSetType(resultSetType);
		checkResultSetConcurrency(resultSetConcurrency);
		checkResultSetHoldability(resultSetHoldability);
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty"); 
		}
		else {
			return newPreparedStatement(sql,resultSetType,resultSetConcurrency,resultSetHoldability,new GeneratedKeyParameters());
		}
	}

	@Override
	public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
		checkResultSetType(resultSetType);
		checkResultSetConcurrency(resultSetConcurrency);
		checkResultSetHoldability(resultSetHoldability);
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty"); 
		}
		else {
			return newCallableStatement(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
		}
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty"); 
		}
		else {
			final GeneratedKeyParameters	keys = new GeneratedKeyParameters();

			keys.generatedKeys = autoGeneratedKeys;
			return newPreparedStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT,keys);
		}
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty"); 
		}
		else {
			final GeneratedKeyParameters	keys = new GeneratedKeyParameters();

			keys.generatedIndices = columnIndexes;
			return newPreparedStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT,keys);
		}
	}

	@Override
	public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty"); 
		}
		else {
			final GeneratedKeyParameters	keys = new GeneratedKeyParameters();

			keys.generatedNames = columnNames;
			return newPreparedStatement(sql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT,keys);
		}
	}

	@Override
	public Clob createClob() throws SQLException {
		return new InMemoryLittleClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return new InMemoryLittleBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return new InMemoryLittleNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return new InMemoryLittleSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return true;
	}

	@Override
	public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Property name can't be null or empty");
		}
		else if (value == null) {
			props.remove(name);
		}
		else {
			props.setProperty(name,value);
		}
	}

	@Override
	public void setClientInfo(final Properties properties) throws SQLClientInfoException {
		if (properties == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else {
			props.clear();
			props.putAll(properties);
		}
	}

	@Override
	public String getClientInfo(final String name) throws SQLException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Property name can't be null or empty");
		}
		else {
			return props.getProperty(name);
		}
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return (Properties)props.clone();
	}

	@Override
	public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
		if (typeName == null || typeName.isEmpty()) {
			throw new IllegalArgumentException("Property name can't be null or empty");
		}
		else {
			return new InMemoryLittleArray(SQLUtils.typeIdByTypeName(typeName),elements);
		}		
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		throw new SQLFeatureNotSupportedException("Structure creation is not supported for the given 'database'");
	}

	@Override
	public void setSchema(final String schema) throws SQLException {
		this.schema = schema;
	}

	@Override
	public String getSchema() throws SQLException {
		return schema;
	}

	@Override
	public void abort(final Executor executor) throws SQLException {
		throw new SQLFeatureNotSupportedException("Network timeout feature is not supported for this 'database'");
	}

	@Override
	public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
		if (executor == null) {
			throw new NullPointerException("Executor can't be null");
		}
		else if (milliseconds < 0) {
			throw new IllegalArgumentException("Milliseconds ["+milliseconds+"] can't be negative");
		}
		else {
			this.networkTimeout = milliseconds;
		}
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return this.networkTimeout;
	}

	protected void checkResultSetType(final int resultSetType) throws SQLFeatureNotSupportedException {
		if (resultSetType != ResultSet.TYPE_FORWARD_ONLY && resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE && resultSetType != ResultSet.TYPE_SCROLL_SENSITIVE) {
			throw new SQLFeatureNotSupportedException("Illegal result set type. Only [TYPE_FORWARD_ONLY], [TYPE_SCROLL_INSENSITIVE] and [TYPE_SCROLL_SENSITIVE] are available");
		}
	}

	protected void checkResultSetConcurrency(final int resultSetConcurrency) throws SQLFeatureNotSupportedException {
		if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY && resultSetConcurrency != ResultSet.CONCUR_UPDATABLE) {
			throw new SQLFeatureNotSupportedException("Illegal result set concurrency. Only [CONCUR_READ_ONLY] and [CONCUR_UPDATABLE] are available for the given 'database'");
		}
	}

	protected void checkResultSetHoldability(final int holdability) throws SQLFeatureNotSupportedException {
		if (holdability != ResultSet.CLOSE_CURSORS_AT_COMMIT && holdability != ResultSet.HOLD_CURSORS_OVER_COMMIT) {
			throw new SQLFeatureNotSupportedException("Illegal holdability. Only [CLOSE_CURSORS_AT_COMMIT] and [HOLD_CURSORS_OVER_COMMIT] are available");
		}
	}
	
	protected static class GeneratedKeyParameters {
		int			generatedKeys = 0;
		int[]		generatedIndices = null;
		String[]	generatedNames = null;
	}
}
