package chav1961.purelib.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public abstract class AbstractReadOnlyResultSet implements ResultSet {
	protected static final String		ERR_READ_ONLY = "This result set is read only";
	
	protected final ResultSetMetaData 	rsmd;
	protected final int					resultSetType;
	protected final Calendar			defaultCalendar = Calendar.getInstance();
	protected int						fetchDirection = FETCH_FORWARD, fetchSize = 0;
	protected boolean					beforeFirst, afterLast, wasNull = true, closed = false;
	
	protected AbstractReadOnlyResultSet(final ResultSetMetaData rsmd, final int resultSetType) {
		if (rsmd == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (resultSetType != TYPE_FORWARD_ONLY && resultSetType != TYPE_SCROLL_INSENSITIVE && resultSetType != TYPE_SCROLL_SENSITIVE) {
			throw new NullPointerException("Illegal result set type ["+resultSetType+"]. Only [TYPE_FORWARD_ONLY], [TYPE_SCROLL_INSENSITIVE] and [TYPE_SCROLL_SENSITIVE] are available for this value"); 
		}
		else {
			this.rsmd = rsmd;
			this.resultSetType = resultSetType;
			beforeFirst = true;
			afterLast = false; 
		}
	}

	@Override public abstract Statement getStatement() throws SQLException;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Class to unwrap can't be null"); 
		}
		else if (!isWrapperFor(iface)) {
			throw new SQLException("This instance is not an implementation of the class ["+iface.getName()+"]"); 
		}
		else {
			return (T)this;
		}
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Class to unwrap can't be null"); 
		}
		else {
			return iface.isAssignableFrom(this.getClass()); 
		}
	}

	@Override
	public boolean next() throws SQLException {
		if (getType() == TYPE_FORWARD_ONLY) {
			beforeFirst = false;
			wasNull = true;
			if (getContent().getCurrentRow() < getContent().getRowCount()) {
				getContent().setCurrentRow(getContent().getCurrentRow()+1);
				return true;
			}
			else {
				afterLast = true; 
				return false;
			}
		}
		else {
			return relative(getFetchDirection() == FETCH_REVERSE ? -1 : 1);
		}		
	}

	@Override
	public void close() throws SQLException {
		closed = true;
	}

	@Override
	public boolean wasNull() throws SQLException {
		return wasNull;
	}

	@Override
	public String getString(final int columnIndex) throws SQLException {
		return getObject(columnIndex,String.class);
	}

	@Override
	public boolean getBoolean(final int columnIndex) throws SQLException {
		return getObject(columnIndex,boolean.class);
	}

	@Override
	public byte getByte(final int columnIndex) throws SQLException {
		return getObject(columnIndex,byte.class);
	}

	@Override
	public short getShort(final int columnIndex) throws SQLException {
		return getObject(columnIndex,short.class);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return getObject(columnIndex,int.class);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return getObject(columnIndex,long.class);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return getObject(columnIndex,float.class);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return getObject(columnIndex,double.class);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return getObject(columnIndex,BigDecimal.class);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return getObject(columnIndex,byte[].class);
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return getDate(columnIndex,defaultCalendar);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return getTime(columnIndex,defaultCalendar);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return getTimestamp(columnIndex,defaultCalendar);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return getObject(columnIndex,InputStream.class);
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return getObject(columnIndex,InputStream.class);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return getObject(columnIndex,InputStream.class);
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return getObject(columnLabel,String.class);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return getObject(columnLabel,boolean.class);
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return getObject(columnLabel,byte.class);
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return getObject(columnLabel,short.class);
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return getObject(columnLabel,int.class);
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return getObject(columnLabel,long.class);
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return getObject(columnLabel,float.class);
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return getObject(columnLabel,double.class);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return getObject(columnLabel,BigDecimal.class);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return getObject(columnLabel,byte[].class);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return getDate(columnLabel,defaultCalendar);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return getTime(columnLabel,defaultCalendar);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(columnLabel,defaultCalendar);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return getObject(columnLabel,InputStream.class);
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return getObject(columnLabel,InputStream.class);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return getObject(columnLabel,InputStream.class);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
	}

	@Override
	public String getCursorName() throws SQLException {
		return null;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return rsmd;
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		return getObject(columnIndex,InternalUtils.DEFAULT_CONVERTOR);
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getObject(toColumnIndex(columnLabel));
	}

	@Override
	public int findColumn(final String columnLabel) throws SQLException {
		if (columnLabel == null || columnLabel.isEmpty()) {
			throw new IllegalArgumentException("Column label can't be null or empty");
		}
		else {
			for (int index = 1; index <= rsmd.getColumnCount(); index++) {
				if (columnLabel.equals(rsmd.getColumnName(index))) {
					return index;
				}
			}
			return 0;
		}
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return getObject(columnIndex,Reader.class);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return getObject(columnLabel,Reader.class);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return getObject(columnIndex,BigDecimal.class);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return getObject(columnLabel,BigDecimal.class);
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return beforeFirst;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return afterLast;
	}

	@Override
	public boolean isFirst() throws SQLException {
		if (!isBeforeFirst() && !isAfterLast()) {
			if (getContent().getRowCount() > 0) {
				return getContent().getCurrentRow() == 1;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isLast() throws SQLException {
		if (!isBeforeFirst() && !isAfterLast()) {
			if (getContent().getRowCount() > 0) {
				return getContent().getCurrentRow() == getContent().getRowCount();
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public void beforeFirst() throws SQLException {
		absolute(0);
	}

	@Override
	public void afterLast() throws SQLException {
		absolute(getContent().getRowCount()+1);
	}

	@Override
	public boolean first() throws SQLException {
		return absolute(1);
	}

	@Override
	public boolean last() throws SQLException {
		return absolute(getContent().getRowCount());
	}

	@Override
	public int getRow() throws SQLException {
		return getContent().getCurrentRow();
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		if (resultSetType == TYPE_FORWARD_ONLY) {
			throw new SQLException("This cursor is forward only!"); 
		}
		else if (row < 1 || row > getContent().getRowCount()) {
			beforeFirst = row < 1; 
			afterLast = row > getContent().getRowCount(); 
			return false;
		}
		else {
			beforeFirst = afterLast = false; 
			getContent().setCurrentRow(row);
			return true;
		}
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		if (isBeforeFirst()) {
			return absolute(0+(getFetchDirection() == FETCH_REVERSE ? -1 : 1) * rows);
		}
		else if (isAfterLast()) {
			return absolute((getContent().getRowCount()+1)+(getFetchDirection() == FETCH_REVERSE ? -1 : 1) * rows);
		}
		else {
			return absolute(getRow()+(getFetchDirection() == FETCH_REVERSE ? -1 : 1) * rows);
		}
	}

	@Override
	public boolean previous() throws SQLException {
		return relative(getFetchDirection() == FETCH_REVERSE ? 1 : -1);
	}

	@Override
	public void setFetchDirection(final int direction) throws SQLException {
		if (direction != FETCH_FORWARD && direction != FETCH_REVERSE) {
			throw new IllegalArgumentException("Illegal fetch direction. Only 'FETCH_FORWARD' and 'FETCH_REVERSE' are available for the given 'database'");
		}
		else {
			this.fetchDirection = direction;
		}
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return fetchDirection;
	}

	@Override
	public void setFetchSize(final int rows) throws SQLException {
		if (rows < 0) {
			throw new IllegalArgumentException("Fetch size ["+rows+"] os negative");
		}
		else {
			this.fetchSize = rows;
		}
	}

	@Override
	public int getFetchSize() throws SQLException {
		return fetchSize;
	}

	@Override
	public int getType() throws SQLException {
		return resultSetType;
	}

	@Override
	public int getConcurrency() throws SQLException {
		return CONCUR_READ_ONLY;
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		return false;
	}

	@Override
	public boolean rowInserted() throws SQLException {
		return false;
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		return false;
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void insertRow() throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateRow() throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void deleteRow() throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void refreshRow() throws SQLException {
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
	}


	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		return getObject(columnIndex,Ref.class);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return getObject(columnIndex,Blob.class);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return getObject(columnIndex,Clob.class);
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		return getObject(columnIndex,Array.class);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		return getObject(toColumnIndex(columnLabel),map);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return getObject(columnLabel,Ref.class);
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return getObject(columnLabel,Blob.class);
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return getObject(columnLabel,Clob.class);
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return getObject(columnLabel,Array.class);
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		final Date	date = getObject(columnIndex,Date.class);
		
		if (date != null) {
			cal.setTime(date);
			return new Date(cal.getTimeInMillis());
		}
		else {
			return null;
		}		
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return getDate(toColumnIndex(columnLabel),cal);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		final Time	time = getObject(columnIndex,Time.class);
		
		if (time != null) {
			cal.setTime(time);
			return new Time(cal.getTimeInMillis());
		}
		else {
			return null;
		}		
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return getTime(toColumnIndex(columnLabel),cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		final Timestamp	timestamp = getObject(columnIndex,Timestamp.class);
		
		if (timestamp != null) {
			cal.setTimeInMillis(timestamp.getTime());
			return new Timestamp(cal.getTimeInMillis());
		}
		else {
			return null;
		}		
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return getTimestamp(toColumnIndex(columnLabel),cal);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return getObject(columnIndex,URL.class);
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return getObject(columnLabel,URL.class);
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return getObject(columnIndex,RowId.class);
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return getObject(columnLabel,RowId.class);
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public int getHoldability() throws SQLException {
		return CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return getObject(columnIndex,NClob.class);
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return getObject(columnLabel,NClob.class);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return getObject(columnIndex,SQLXML.class);
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return getObject(columnLabel,SQLXML.class);
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return getObject(columnIndex,String.class);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return getObject(columnLabel,String.class);
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return getObject(columnIndex,Reader.class);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return getObject(columnLabel,Reader.class);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new SQLException(ERR_READ_ONLY);
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		final Object	result = getObject(columnIndex);
		
		if (result != null) {
			return InternalUtils.convert(getRow(),columnIndex,type,result);
		}
		else {
			return null;
		}
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return getObject(toColumnIndex(columnLabel),type);
	}
	
	@Override
	public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
		if (columnIndex < 1 || columnIndex > rsmd.getColumnCount()) {
			throw new SQLException("Column number ["+columnIndex+"] outside the range 1.."+rsmd.getColumnCount());
		}
		else if (map == null) {
			throw new NullPointerException("Map to convert data can't be null");
		}
		else if (isBeforeFirst() || isAfterLast()) {
			throw new SQLException("Result set is not positioned properly. Use next() or similar to make it");
		}
		else {
			final String	sourceType = getMetaData().getColumnTypeName(columnIndex);
			final Object	value = getContent().getRow(getContent().getCurrentRow())[columnIndex-1];

			if (value != null) {
				wasNull = false;
				if (map.containsKey(sourceType)) {
					return InternalUtils.convert(getRow(),columnIndex,map.get(sourceType),value); 
				}
				else {
					return InternalUtils.convert(getRow(),columnIndex,Object.class,value); 
				}
			}
			else {
				wasNull = true;
				return null;
			}			
		}
	}
	
	protected abstract AbstractContent getContent();

	protected int toColumnIndex(final String columnName) throws SQLException {
		final int	result = findColumn(columnName);
		
		if (result == 0) {
			throw new SQLException("Column name ["+columnName+"] is missing in the result set");
		}
		else {
			return result;
		}
	}

}
