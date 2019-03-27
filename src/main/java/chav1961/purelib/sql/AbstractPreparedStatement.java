package chav1961.purelib.sql;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.sql.interfaces.QueryExecutor;

public abstract class AbstractPreparedStatement extends AbstractStatement implements PreparedStatement {
	protected final Calendar			defaultCalendar = Calendar.getInstance();
	protected final List<Object[]>		batchList = new ArrayList<>();
	
	private final QueryExecutor			sqe;
	private final Object[]				parm;
	private final boolean				isQuery;
	
	public AbstractPreparedStatement(final Connection conn, final String sql, final int type, final int concurrency, final int holdability) throws SQLException {
		super(conn, type, concurrency, holdability);
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty");
		}
		else {
			this.isQuery = isThisAQueryString(sql); 
			this.sqe = createQueryExecutor(sql);
			this.parm = new Object[sqe.getParmMetaData().getParameterCount()];
		}
	}

	@Override
	public void addBatch(final String batch) throws SQLException {
		throw new SQLFeatureNotSupportedException("This methods can't be used for the PreparedStatement. Use addBatch() only");
	}
	
	@Override
	public void clearBatch() throws SQLException {
		batchList.clear();
		batchResult = null;
	}

	@Override
	public int[] executeBatch() throws SQLException {
		testClosing();
		final int[]		result = new int[batchList.size()];
		
		batchResult	= new BatchResult[result.length];
		for (int index = 0; index < result.length; index++) {
			final Object[]	sql = batchList.get(index);
		
			if (isQuery) {
				try{batchResult[index] = new BatchResult(new InMemoryReadOnlyResultSet(this,sqe.getRsMetaData(),getResultSetType(),new ArrayContent(sqe.executeQuery(sql))));
					result[index] = SUCCESS_NO_INFO;
				} catch (SQLException exc) {
					result[index] = EXECUTE_FAILED;
				}
			}
			else {
				try{batchResult[index] = new BatchResult(sqe.executeUpdate(sql));
					result[index] = batchResult[index].updateCount;
				} catch (SQLException exc) {
					result[index] = EXECUTE_FAILED;
				}
			}
		}
		batchCursor = 0;
		return result;
	}
	
	
	@Override
	public void addBatch() throws SQLException {
		checkParametersList();
		batchList.add(parm);
	}

	@Override
	public void clearParameters() throws SQLException {
		Arrays.fill(parm,null);
	}

	@Override
	public boolean execute() throws SQLException {
		checkParametersList();
		if (isQuery) {
			rs = executeQuery();
			return wasResultSet = true;
		}
		else {
			updateCount = executeUpdate();
			return wasResultSet = false;
		}
	}

	@Override
	public ResultSet executeQuery() throws SQLException {
		checkParametersList();
		wasResultSet = true;
		updateCount = -1;
		return rs = new InMemoryReadOnlyResultSet(this,sqe.getRsMetaData(),getResultSetType(),new ArrayContent(sqe.executeQuery(parm)));
	}

	@Override
	public int executeUpdate() throws SQLException {
		checkParametersList();
		wasResultSet = false;
		updateCount = -1;
		return sqe.executeUpdate(parm);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return sqe.getRsMetaData();
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return sqe.getParmMetaData();
	}

	@Override
	public void setArray(final int parameterIndex, final Array x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Array to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
		checkParameter(parameterIndex);
		try{setCharacterStream(parameterIndex,new InputStreamReader(x,"ASCII"));
		} catch (UnsupportedEncodingException e) {
			throw new SQLException(e.getLocalizedMessage());
		}
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		checkParameter(parameterIndex);
		setAsciiStream(parameterIndex,new TruncatedInputStream(x,length));
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		checkParameter(parameterIndex);
		setAsciiStream(parameterIndex,new TruncatedInputStream(x,length));
	}

	@Override
	public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Big decimal to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
		checkParameter(parameterIndex);
		setBlob(parameterIndex,x);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		checkParameter(parameterIndex);
		setBinaryStream(parameterIndex,new TruncatedInputStream(x,length));
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		checkParameter(parameterIndex);
		setBinaryStream(parameterIndex,new TruncatedInputStream(x,length));
	}

	@Override
	public void setBlob(final int parameterIndex, final Blob x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Blob to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
		checkParameter(parameterIndex);
		if (inputStream == null) {
			throw new SQLException("Input stream to set can't be null. Use setNull() to set null values");
		}
		else {
			final Blob	b = new InMemoryLittleBlob();
			
			try(final OutputStream 	os = b.setBinaryStream(1)) {
				Utils.copyStream(inputStream,os);
				os.flush();
			} catch (IOException e) {
				throw new SQLException(e.getLocalizedMessage());
			}
			setBlob(parameterIndex,b);
		}
	}

	@Override
	public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
		checkParameter(parameterIndex);
		setBlob(parameterIndex,new TruncatedInputStream(inputStream,length));
	}

	@Override
	public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
		checkParameter(parameterIndex);
		parm[parameterIndex-1] = x;
	}

	@Override
	public void setByte(final int parameterIndex, final byte x) throws SQLException {
		checkParameter(parameterIndex);
		parm[parameterIndex-1] = x;
	}

	@Override
	public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Byte array to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
		checkParameter(parameterIndex);
		setClob(parameterIndex,reader);
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
		checkParameter(parameterIndex);
		setCharacterStream(parameterIndex,new TruncatedReader(reader,length));
	}

	@Override
	public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
		checkParameter(parameterIndex);
		setCharacterStream(parameterIndex,new TruncatedReader(reader,length));
	}

	@Override
	public void setClob(final int parameterIndex, final Clob x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Clob to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
		checkParameter(parameterIndex);
		if (reader == null) {
			throw new SQLException("Reader to set can't be null. Use setNull() to set null values");
		}
		else {
			final Clob	c = new InMemoryLittleClob();
			
			try(final Writer	wr = c.setCharacterStream(1)) {
				Utils.copyStream(reader,wr);
				wr.flush();
			} catch (IOException e) {
				throw new SQLException(e.getLocalizedMessage());
			}
			setClob(parameterIndex,c);
		}
	}

	@Override
	public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
		checkParameter(parameterIndex);
		setClob(parameterIndex,new TruncatedReader(reader,length));
	}

	@Override
	public void setDate(final int parameterIndex, final Date x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Date to set can't be null. Use setNull() to set null values");
		}
		else {
			setDate(parameterIndex,x,defaultCalendar);
		}
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Date to set can't be null. Use setNull() to set null values");
		}
		else if (cal == null) {
			throw new SQLException("Calendar to set can't be null. Use setNull() to set null values");
		}
		else {
			cal.setTimeInMillis(x.getTime());
			parm[parameterIndex-1] = new Date(cal.getTimeInMillis());
		}
	}

	@Override
	public void setDouble(final int parameterIndex, final double x) throws SQLException {
		checkParameter(parameterIndex);
		parm[parameterIndex-1] = x;
	}

	@Override
	public void setFloat(final int parameterIndex, final float x) throws SQLException {
		checkParameter(parameterIndex);
		parm[parameterIndex-1] = x;
	}

	@Override
	public void setInt(final int parameterIndex, final int x) throws SQLException {
		checkParameter(parameterIndex);
		parm[parameterIndex-1] = x;
	}

	@Override
	public void setLong(final int parameterIndex, final long x) throws SQLException {
		checkParameter(parameterIndex);
		parm[parameterIndex-1] = x;
	}

	@Override
	public void setNCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
		checkParameter(parameterIndex);
		setCharacterStream(parameterIndex,value);
	}

	@Override
	public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) throws SQLException {
		checkParameter(parameterIndex);
		setNCharacterStream(parameterIndex,new TruncatedReader(value,length));
	}

	@Override
	public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
		checkParameter(parameterIndex);
		if (value == null) {
			throw new SQLException("NClob to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = value;
		}
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		checkParameter(parameterIndex);
		setClob(parameterIndex,reader);
	}

	@Override
	public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
		checkParameter(parameterIndex);
		setNClob(parameterIndex,new TruncatedReader(reader,length));
	}

	@Override
	public void setNString(final int parameterIndex, final String value) throws SQLException {
		checkParameter(parameterIndex);
		if (value == null) {
			throw new SQLException("String to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = value;
		}
	}

	@Override
	public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
		checkParameter(parameterIndex);
		setNull(parameterIndex,sqlType,SQLUtils.typeNameByTypeId(sqlType));
	}

	@Override
	public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
		checkParameter(parameterIndex);
		if (typeName == null || typeName.isEmpty()) {
			throw new IllegalArgumentException("Type name can't be null or empty"); 
		}
		else {
			parm[parameterIndex-1] = new NullValue(sqlType,typeName);
		}
	}

	@Override
	public void setObject(final int parameterIndex, final Object x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Object to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
		checkParameter(parameterIndex);
		setObject(parameterIndex,x,targetSqlType,0);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Object to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = SQLUtils.convert(1,parameterIndex,SQLUtils.classBySqlTypeName(SQLUtils.typeNameByTypeId(targetSqlType)),x);
		}
	}

	@Override
	public void setRef(final int parameterIndex, final Ref x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Ref to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("RowId to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
		checkParameter(parameterIndex);
		if (xmlObject == null) {
			throw new SQLException("SQLXML to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = xmlObject;
		}
	}

	@Override
	public void setShort(final int parameterIndex, final short x) throws SQLException {
		checkParameter(parameterIndex);
		parm[parameterIndex-1] = x;
	}

	@Override
	public void setString(final int parameterIndex, final String x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("String to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setTime(final int parameterIndex, final Time x) throws SQLException {
		checkParameter(parameterIndex);
		setTime(parameterIndex,x,defaultCalendar);
	}

	@Override
	public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Time to set can't be null. Use setNull() to set null values");
		}
		else if (cal == null) {
			throw new SQLException("Calendar to set can't be null. Use setNull() to set null values");
		}
		else {
			cal.setTimeInMillis(x.getTime());
			parm[parameterIndex-1] = new Time(cal.getTimeInMillis());
		}
	}

	@Override
	public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
		checkParameter(parameterIndex);
		setTimestamp(parameterIndex,x,defaultCalendar);
	}

	@Override
	public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("Timestamp to set can't be null. Use setNull() to set null values");
		}
		else if (cal == null) {
			throw new SQLException("Calendar to set can't be null. Use setNull() to set null values");
		}
		else {
			cal.setTimeInMillis(x.getTime());
			parm[parameterIndex-1] = new Timestamp(cal.getTimeInMillis());
		}
	}

	@Override
	public void setURL(final int parameterIndex, final URL x) throws SQLException {
		checkParameter(parameterIndex);
		if (x == null) {
			throw new SQLException("URL to set can't be null. Use setNull() to set null values");
		}
		else {
			parm[parameterIndex-1] = x;
		}
	}

	@Override
	public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
		checkParameter(parameterIndex);
		try{setCharacterStream(parameterIndex,new InputStreamReader(new TruncatedInputStream(x,length),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new SQLException(e.getLocalizedMessage());
		}
	}

	protected void checkParameter(final int parmIndex) throws SQLException {
		if (parmIndex < 1 || parmIndex > getParameterMetaData().getParameterCount()) {
			throw new SQLException("Parameter number ["+parmIndex+"] out of range 1.."+getParameterMetaData().getParameterCount());
		}
	}

	protected void checkParametersList() throws SQLException {
		for (int index = 0; index < parm.length; index++) {
			if (parm[index] == null) {
				throw new SQLException("Parameter ["+(index+1)+"] is not bound yet. Use appropriative setZZZ() to set it's value");
			}
		}
	}

	
	static class NullValue {
		final int		type;
		final String	name;
		
		NullValue(int type, String name) {
			this.type = type;
			this.name = name;
		}

		int getType() {
			return type;
		}

		String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "NullValue [type=" + type + ", name=" + name + "]";
		}
	}

	static class OutputValue {
		final int		type;
		final String	name;
		
		OutputValue(int type, String name) {
			this.type = type;
			this.name = name;
		}

		int getType() {
			return type;
		}

		String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "OutputValue [type=" + type + ", name=" + name + "]";
		}
	}
	
	private static class TruncatedInputStream extends InputStream {
		private final InputStream	nested;
		private long				len;
		
		TruncatedInputStream(final InputStream nested, final long len) {
			this.nested = nested;
			this.len = len;					
		}

		@Override
		public int read() throws IOException {
			if (len >= 0) {
				len--;
				return nested.read();
			}
			else {
				return -1;
			}
		}
		
		@Override
		public void close() throws IOException {
			nested.close();
			super.close();
		}
	}

	private static class TruncatedReader extends Reader {
		private final Reader	nested;
		private long			length;
		
		TruncatedReader(final Reader nested, final long length) {
			this.nested = nested;
			this.length = length;
		}

		@Override
		public void close() throws IOException {
			nested.close();
		}

		@Override
		public int read(final char[] cbuf, final int off, final int len) throws IOException {
			final int	read = nested.read(cbuf, off, len);
			
			if (read > length) {
				return (int)length;
			}
			else if (read == -1) {
				return (int)(length = -1);
			}
			else {
				length -= read;
				return read;
			}
		}
	}
}
