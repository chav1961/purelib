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

import chav1961.purelib.basic.Utils;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.sql.interfaces.ResultSetMarker;
import chav1961.purelib.sql.interfaces.ResultSetMarkerChangedListener;

public class ResultSetStream implements ResultSet, ResultSetMarker {
	private final LightWeightListenerList<ResultSetMarkerChangedListener>	listeners = new LightWeightListenerList<>(ResultSetMarkerChangedListener.class);
	private final ResultSetContainer[] 	content;
	private int							rowNumber = 0;
	private int							currentIndex = 0;
	private int							currentFetchSize = 1;
	private ResultSetContainer			previous = null;
	private ResultSetContainer			current;
	private boolean						lastEventFired = false;
	private boolean						isClosed = false;
	
	public ResultSetStream(final ResultSetContainer... content) throws SQLException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Stream content can't be null or empty list");
		}
		else if (Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new IllegalArgumentException("Stream content contains nulls inside");
		}
		else {
			this.content = content;
			this.current = content[0];
			
			for (ResultSetContainer item : content) {
				if (item.getType() != ResultSet.TYPE_FORWARD_ONLY) {
					throw new IllegalArgumentException("Stream content must contain forward-only result sets, but some of these are not");
				}
				else {
					item.addResultSetMarkerChangedListener((e)->fireEvent(e));
				}
			}
		}
	}
	
	@Override
	public String getResultSetMarker() {
		return current != null ? current.getResultSetMarker() : null;
	}

	@Override
	public void addResultSetMarkerChangedListener(final ResultSetMarkerChangedListener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}

	@Override
	public void removeResultSetMarkerChangedListener(ResultSetMarkerChangedListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(listener);
		}
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		return getCurrentResultSet().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return getCurrentResultSet().isWrapperFor(iface);
	}

	@Override
	public boolean next() throws SQLException {
		final boolean	result = getCurrentResultSet().next();
		
		rowNumber++;
		if (!result) {
			if (++currentIndex < content.length) {
				previous = current;
				current = content[currentIndex];
				current.setFetchSize(currentFetchSize);
				return true;
			}
			else {
				fireEvent(new ResultSetMarkerChangedEvent(current.getResultSetMarker(),current,null,null));
				lastEventFired = true;
				return false;
			}
		}
		else {
			return true;
		}
	}

	@Override
	public void close() throws SQLException {
		if (!lastEventFired) {
			fireEvent(new ResultSetMarkerChangedEvent(current.getResultSetMarker(),current,null,null));
		}
		for (ResultSetContainer item : content) {
			item.close();
		}
	}

	@Override
	public boolean wasNull() throws SQLException {
		return getCurrentResultSet().wasNull();
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		return getCurrentResultSet().getString(columnIndex);
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return getCurrentResultSet().getBoolean(columnIndex);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		return getCurrentResultSet().getByte(columnIndex);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		return getCurrentResultSet().getShort(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return getCurrentResultSet().getInt(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return getCurrentResultSet().getLong(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return getCurrentResultSet().getFloat(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return getCurrentResultSet().getDouble(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return getCurrentResultSet().getBigDecimal(columnIndex, scale);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return getCurrentResultSet().getBytes(columnIndex);
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return getCurrentResultSet().getDate(columnIndex);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return getCurrentResultSet().getTime(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return getCurrentResultSet().getTimestamp(columnIndex);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return getCurrentResultSet().getAsciiStream(columnIndex);
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return getCurrentResultSet().getUnicodeStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return getCurrentResultSet().getBinaryStream(columnIndex);
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return getCurrentResultSet().getString(columnLabel);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return getCurrentResultSet().getBoolean(columnLabel);
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return getCurrentResultSet().getByte(columnLabel);
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return getCurrentResultSet().getShort(columnLabel);
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return getCurrentResultSet().getInt(columnLabel);
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return getCurrentResultSet().getLong(columnLabel);
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return getCurrentResultSet().getFloat(columnLabel);
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return getCurrentResultSet().getDouble(columnLabel);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return getCurrentResultSet().getBigDecimal(columnLabel,scale);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return getCurrentResultSet().getBytes(columnLabel);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return getCurrentResultSet().getDate(columnLabel);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return getCurrentResultSet().getTime(columnLabel);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getCurrentResultSet().getTimestamp(columnLabel);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return getCurrentResultSet().getAsciiStream(columnLabel);
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return getCurrentResultSet().getUnicodeStream(columnLabel);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return getCurrentResultSet().getBinaryStream(columnLabel);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return getCurrentResultSet().getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		getCurrentResultSet().clearWarnings();
	}

	@Override
	public String getCursorName() throws SQLException {
		return getCurrentResultSet().getCursorName();
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return getCurrentResultSet().getMetaData();
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		return getCurrentResultSet().getObject(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getCurrentResultSet().getObject(columnLabel);
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		return getCurrentResultSet().findColumn(columnLabel);
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return getCurrentResultSet().getCharacterStream(columnIndex);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return getCurrentResultSet().getCharacterStream(columnLabel);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return getCurrentResultSet().getBigDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return getCurrentResultSet().getBigDecimal(columnLabel);
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean isFirst() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean isLast() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public void beforeFirst() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public void afterLast() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean first() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean last() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public int getRow() throws SQLException {
		return rowNumber;
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean previous() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		getCurrentResultSet().setFetchSize(currentFetchSize = rows);
	}

	@Override
	public int getFetchSize() throws SQLException {
		return getCurrentResultSet().getFetchSize();
	}

	@Override
	public int getType() throws SQLException {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	@Override
	public int getConcurrency() throws SQLException {
		return getCurrentResultSet().getConcurrency();
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean rowInserted() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		throw new IllegalStateException("Result set stream is forward only");
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		getCurrentResultSet().updateNull(columnIndex);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		getCurrentResultSet().updateBoolean(columnIndex,x);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		getCurrentResultSet().updateByte(columnIndex,x);
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		getCurrentResultSet().updateShort(columnIndex,x);
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		getCurrentResultSet().updateInt(columnIndex,x);
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		getCurrentResultSet().updateLong(columnIndex,x);
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		getCurrentResultSet().updateFloat(columnIndex,x);
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		getCurrentResultSet().updateDouble(columnIndex,x);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		getCurrentResultSet().updateBigDecimal(columnIndex,x);
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		getCurrentResultSet().updateString(columnIndex,x);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		getCurrentResultSet().updateBytes(columnIndex,x);
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		getCurrentResultSet().updateDate(columnIndex,x);
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		getCurrentResultSet().updateTime(columnIndex,x);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		getCurrentResultSet().updateTimestamp(columnIndex,x);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		getCurrentResultSet().updateAsciiStream(columnIndex,x,length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		getCurrentResultSet().updateBinaryStream(columnIndex,x,length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		getCurrentResultSet().updateCharacterStream(columnIndex,x,length);
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		getCurrentResultSet().updateObject(columnIndex,x,scaleOrLength);
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		getCurrentResultSet().updateObject(columnIndex,x);
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		getCurrentResultSet().updateNull(columnLabel);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		getCurrentResultSet().updateBoolean(columnLabel,x);
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		getCurrentResultSet().updateByte(columnLabel,x);
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		getCurrentResultSet().updateShort(columnLabel,x);
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		getCurrentResultSet().updateInt(columnLabel,x);
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		getCurrentResultSet().updateLong(columnLabel,x);
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		getCurrentResultSet().updateFloat(columnLabel,x);
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		getCurrentResultSet().updateDouble(columnLabel,x);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		getCurrentResultSet().updateBigDecimal(columnLabel,x);
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		getCurrentResultSet().updateString(columnLabel,x);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		getCurrentResultSet().updateBytes(columnLabel,x);
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		getCurrentResultSet().updateDate(columnLabel,x);
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		getCurrentResultSet().updateTime(columnLabel,x);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		getCurrentResultSet().updateTimestamp(columnLabel,x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		getCurrentResultSet().updateAsciiStream(columnLabel,x,length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		getCurrentResultSet().updateBinaryStream(columnLabel,x,length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		getCurrentResultSet().updateCharacterStream(columnLabel,reader,length);
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		getCurrentResultSet().updateObject(columnLabel,x,scaleOrLength);
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		getCurrentResultSet().updateObject(columnLabel,x);
	}

	@Override
	public void insertRow() throws SQLException {
		getCurrentResultSet().insertRow();
	}

	@Override
	public void updateRow() throws SQLException {
		getCurrentResultSet().updateRow();
	}

	@Override
	public void deleteRow() throws SQLException {
		getCurrentResultSet().deleteRow();
	}

	@Override
	public void refreshRow() throws SQLException {
		getCurrentResultSet().refreshRow();
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		getCurrentResultSet().cancelRowUpdates();
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		getCurrentResultSet().moveToInsertRow();
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		getCurrentResultSet().moveToCurrentRow();
	}

	@Override
	public Statement getStatement() throws SQLException {
		return getCurrentResultSet().getStatement();
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		return getCurrentResultSet().getObject(columnIndex,map);
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		return getCurrentResultSet().getRef(columnIndex);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		return getCurrentResultSet().getBlob(columnIndex);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		return getCurrentResultSet().getClob(columnIndex);
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		return getCurrentResultSet().getArray(columnIndex);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		return getCurrentResultSet().getObject(columnLabel,map);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		return getCurrentResultSet().getRef(columnLabel);
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		return getCurrentResultSet().getBlob(columnLabel);
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return getCurrentResultSet().getClob(columnLabel);
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		return getCurrentResultSet().getArray(columnLabel);
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return getCurrentResultSet().getDate(columnIndex,cal);
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return getCurrentResultSet().getDate(columnLabel,cal);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return getCurrentResultSet().getTime(columnIndex,cal);
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return getCurrentResultSet().getTime(columnLabel,cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return getCurrentResultSet().getTimestamp(columnIndex,cal);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return getCurrentResultSet().getTimestamp(columnLabel,cal);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		return getCurrentResultSet().getURL(columnIndex);
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return getCurrentResultSet().getURL(columnLabel);
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		getCurrentResultSet().updateRef(columnIndex,x);
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		getCurrentResultSet().updateRef(columnLabel,x);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		getCurrentResultSet().updateBlob(columnIndex,x);
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		getCurrentResultSet().updateBlob(columnLabel,x);
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		getCurrentResultSet().updateClob(columnIndex,x);
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		getCurrentResultSet().updateClob(columnLabel,x);
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		getCurrentResultSet().updateArray(columnIndex,x);
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		getCurrentResultSet().updateArray(columnLabel,x);
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return getCurrentResultSet().getRowId(columnIndex);
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return getCurrentResultSet().getRowId(columnLabel);
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		getCurrentResultSet().updateRowId(columnIndex,x);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		getCurrentResultSet().updateRowId(columnLabel,x);
	}

	@Override
	public int getHoldability() throws SQLException {
		return getCurrentResultSet().getHoldability();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return isClosed;
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		getCurrentResultSet().updateNString(columnIndex,nString);
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		getCurrentResultSet().updateNString(columnLabel,nString);
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		getCurrentResultSet().updateNClob(columnIndex,nClob);
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		getCurrentResultSet().updateClob(columnLabel,nClob);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return getCurrentResultSet().getNClob(columnIndex);
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return getCurrentResultSet().getNClob(columnLabel);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return getCurrentResultSet().getSQLXML(columnIndex);
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return getCurrentResultSet().getSQLXML(columnLabel);
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		getCurrentResultSet().updateSQLXML(columnIndex,xmlObject);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		getCurrentResultSet().updateSQLXML(columnLabel,xmlObject);
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return getCurrentResultSet().getNString(columnIndex);
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return getCurrentResultSet().getNString(columnLabel);
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return getCurrentResultSet().getNCharacterStream(columnIndex);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return getCurrentResultSet().getNCharacterStream(columnLabel);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		getCurrentResultSet().updateNCharacterStream(columnIndex,x,length);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		getCurrentResultSet().updateNCharacterStream(columnLabel,reader,length);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		getCurrentResultSet().updateAsciiStream(columnIndex,x,length);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		getCurrentResultSet().updateBinaryStream(columnIndex,x,length);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		getCurrentResultSet().updateCharacterStream(columnIndex,x,length);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		getCurrentResultSet().updateAsciiStream(columnLabel,x,length);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		getCurrentResultSet().updateBinaryStream(columnLabel,x,length);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		getCurrentResultSet().updateCharacterStream(columnLabel,reader,length);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		getCurrentResultSet().updateBlob(columnIndex,inputStream,length);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		getCurrentResultSet().updateBlob(columnLabel,inputStream,length);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		getCurrentResultSet().updateClob(columnIndex,reader,length);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		getCurrentResultSet().updateClob(columnLabel,reader,length);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		getCurrentResultSet().updateNClob(columnIndex,reader,length);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		getCurrentResultSet().updateNClob(columnLabel,reader,length);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		getCurrentResultSet().updateNCharacterStream(columnIndex,x);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		getCurrentResultSet().updateNCharacterStream(columnLabel,reader);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		getCurrentResultSet().updateAsciiStream(columnIndex,x);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		getCurrentResultSet().updateBinaryStream(columnIndex,x);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		getCurrentResultSet().updateCharacterStream(columnIndex,x);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		getCurrentResultSet().updateAsciiStream(columnLabel,x);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		getCurrentResultSet().updateBinaryStream(columnLabel,x);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		getCurrentResultSet().updateCharacterStream(columnLabel,reader);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		getCurrentResultSet().updateBlob(columnIndex,inputStream);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		getCurrentResultSet().updateBlob(columnLabel,inputStream);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		getCurrentResultSet().updateClob(columnIndex,reader);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		getCurrentResultSet().updateClob(columnLabel,reader);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		getCurrentResultSet().updateClob(columnIndex,reader);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		getCurrentResultSet().updateClob(columnLabel,reader);
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return getCurrentResultSet().getObject(columnIndex,type);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return getCurrentResultSet().getObject(columnLabel,type);
	}

	private ResultSet getCurrentResultSet() {
		if (current != previous) {
			if (previous == null) {
				fireEvent(new ResultSetMarkerChangedEvent(null,null,current.getResultSetMarker(),current));
			}
			else {
				fireEvent(new ResultSetMarkerChangedEvent(previous.getResultSetMarker(),previous,current.getResultSetMarker(),current));
			}
			previous = current;
		}
		return current;
	}
	
	private void fireEvent(final ResultSetMarkerChangedEvent e) {
		listeners.fireEvent((c)->c.markerChanged(e));
	}
}
