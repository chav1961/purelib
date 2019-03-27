package chav1961.purelib.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import chav1961.purelib.sql.interfaces.QueryExecutor;

public abstract class AbstractCallableStatement extends AbstractPreparedStatement implements CallableStatement {
	private final QueryExecutor			sqe;
	private final Object[]				parm;
	private final Class<?>[]			result;
	private boolean						wasNull = false;
	
	public AbstractCallableStatement(Connection conn, String sql, int type, int concurrency, int holdability) throws SQLException {
		super(conn, sql, type, concurrency, holdability);
		if (sql == null || sql.isEmpty()) {
			throw new IllegalArgumentException("SQL string can't be null or empty");
		}
		else {
			this.sqe = createQueryExecutor(sql);
			this.parm = new Object[sqe.getParmMetaData().getParameterCount()];
			this.result = new Class<?>[sqe.getParmMetaData().getParameterCount()];
		}
	}

	@Override
	public Array getArray(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Array.class);
	}

	@Override
	public Array getArray(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getArray(findParameter(parameterName));
	}

	@Override
	public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getBigDecimal(parameterIndex,0);
	}

	@Override
	public BigDecimal getBigDecimal(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getBigDecimal(findParameter(parameterName));
	}

	@Override
	public BigDecimal getBigDecimal(final int parameterIndex, final int scale) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,BigDecimal.class);
	}

	@Override
	public Blob getBlob(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Blob.class);
	}

	@Override
	public Blob getBlob(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getBlob(findParameter(parameterName));
	}

	@Override
	public boolean getBoolean(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,boolean.class);
	}

	@Override
	public boolean getBoolean(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getBoolean(findParameter(parameterName));
	}

	@Override
	public byte getByte(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,byte.class);
	}

	@Override
	public byte getByte(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getByte(findParameter(parameterName));
	}

	@Override
	public byte[] getBytes(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,byte[].class);
	}

	@Override
	public byte[] getBytes(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getBytes(findParameter(parameterName));
	}

	@Override
	public Reader getCharacterStream(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Reader.class);
	}

	@Override
	public Reader getCharacterStream(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getCharacterStream(findParameter(parameterName));
	}

	@Override
	public Clob getClob(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Clob.class);
	}

	@Override
	public Clob getClob(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getClob(findParameter(parameterName));
	}

	@Override
	public Date getDate(final int parameterIndex) throws SQLException {
		return getDate(parameterIndex,defaultCalendar);
	}

	@Override
	public Date getDate(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getDate(findParameter(parameterName));
	}

	@Override
	public Date getDate(final int parameterIndex, final Calendar cal) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Date.class);
	}

	@Override
	public Date getDate(final String parameterName, final Calendar cal) throws SQLException {
		checkParameterName(parameterName);
		return getDate(findParameter(parameterName),cal);
	}

	@Override
	public double getDouble(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,double.class);
	}

	@Override
	public double getDouble(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getDouble(findParameter(parameterName));
	}

	@Override
	public float getFloat(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,float.class);
	}

	@Override
	public float getFloat(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getFloat(findParameter(parameterName));
	}

	@Override
	public int getInt(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,int.class);
	}

	@Override
	public int getInt(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getInt(findParameter(parameterName));
	}

	@Override
	public long getLong(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,long.class);
	}

	@Override
	public long getLong(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getLong(findParameter(parameterName));
	}

	@Override
	public Reader getNCharacterStream(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Reader.class);
	}

	@Override
	public Reader getNCharacterStream(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getNCharacterStream(findParameter(parameterName));
	}

	@Override
	public NClob getNClob(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,NClob.class);
	}

	@Override
	public NClob getNClob(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getNClob(findParameter(parameterName));
	}

	@Override
	public String getNString(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,String.class);
	}

	@Override
	public String getNString(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getNString(findParameter(parameterName));
	}

	@Override
	public Object getObject(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,result[parameterIndex-1]);
	}

	@Override
	public Object getObject(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getObject(findParameter(parameterName));
	}

	@Override
	public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		if (map == null) {
			throw new NullPointerException("Map to convert data can't be null");
		}
		else {
			final String	sourceType = getParameterMetaData().getParameterTypeName(parameterIndex);
			final Object	value = parm[parameterIndex-1];

			if (value != null) {
				wasNull = false;
				if (map.containsKey(sourceType)) {
					return SQLUtils.convert(0,parameterIndex,map.get(sourceType),value); 
				}
				else {
					return SQLUtils.convert(0,parameterIndex,Object.class,value); 
				}
			}
			else {
				wasNull = true;
				return null;
			}
		}
	}

	@Override
	public Object getObject(final String parameterName, final Map<String, Class<?>> map) throws SQLException {
		checkParameterName(parameterName);
		return getObject(findParameter(parameterName),map);
	}

	@Override
	public <T> T getObject(final int parameterIndex, final Class<T> type) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return SQLUtils.convert(0,parameterIndex,type,parm[parameterIndex-1]);
	}

	@Override
	public <T> T getObject(final String parameterName, final Class<T> type) throws SQLException {
		checkParameterName(parameterName);
		return getObject(findParameter(parameterName),type);
	}

	@Override
	public Ref getRef(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Ref.class);
	}

	@Override
	public Ref getRef(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getRef(findParameter(parameterName));
	}

	@Override
	public RowId getRowId(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,RowId.class);
	}

	@Override
	public RowId getRowId(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getRowId(findParameter(parameterName));
	}

	@Override
	public SQLXML getSQLXML(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,SQLXML.class);
	}

	@Override
	public SQLXML getSQLXML(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getSQLXML(findParameter(parameterName));
	}

	@Override
	public short getShort(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,short.class);
	}

	@Override
	public short getShort(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getShort(findParameter(parameterName));
	}

	@Override
	public String getString(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,String.class);
	}

	@Override
	public String getString(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getString(findParameter(parameterName));
	}

	@Override
	public Time getTime(final int parameterIndex) throws SQLException {
		return getTime(parameterIndex,defaultCalendar);
	}

	@Override
	public Time getTime(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getTime(findParameter(parameterName));
	}

	@Override
	public Time getTime(final int parameterIndex, final Calendar cal) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Time.class);
	}

	@Override
	public Time getTime(final String parameterName, final Calendar cal) throws SQLException {
		checkParameterName(parameterName);
		return getTime(findParameter(parameterName),cal);
	}

	@Override
	public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
		return getTimestamp(parameterIndex,defaultCalendar);
	}

	@Override
	public Timestamp getTimestamp(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getTimestamp(findParameter(parameterName));
	}

	@Override
	public Timestamp getTimestamp(final int parameterIndex, final Calendar cal) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,Timestamp.class);
	}

	@Override
	public Timestamp getTimestamp(final String parameterName, final Calendar cal) throws SQLException {
		checkParameterName(parameterName);
		return getTimestamp(findParameter(parameterName),cal);
	}

	@Override
	public URL getURL(final int parameterIndex) throws SQLException {
		checkParameterIndex(parameterIndex,true);
		return getObject(parameterIndex,URL.class);
	}

	@Override
	public URL getURL(final String parameterName) throws SQLException {
		checkParameterName(parameterName);
		return getURL(findParameter(parameterName));
	}

	@Override
	public void registerOutParameter(final int parameterIndex, final int sqlType) throws SQLException {
		registerOutParameter(parameterIndex,sqlType,SQLUtils.typeNameByTypeId(sqlType));
	}

	@Override
	public void registerOutParameter(final String parameterName, final int sqlType) throws SQLException {
		checkParameterName(parameterName);
		registerOutParameter(findParameter(parameterName),sqlType);
	}

	@Override
	public void registerOutParameter(final int parameterIndex, final int sqlType, final int scale) throws SQLException {
		checkParameterIndex(parameterIndex,false);
		result[parameterIndex-1] = SQLUtils.classBySqlTypeName(SQLUtils.typeNameByTypeId(sqlType));
		parm[parameterIndex-1] = new OutputValue(sqlType,SQLUtils.typeNameByTypeId(sqlType));
	}

	@Override
	public void registerOutParameter(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
		registerOutParameter(parameterIndex,sqlType,0);
	}

	@Override
	public void registerOutParameter(final String parameterName, final int sqlType, final int scale) throws SQLException {
		checkParameterName(parameterName);
		registerOutParameter(findParameter(parameterName),sqlType,scale);
	}

	@Override
	public void registerOutParameter(final String parameterName, final int sqlType, final String typeName) throws SQLException {
		checkParameterName(parameterName);
		registerOutParameter(findParameter(parameterName),sqlType,typeName);
	}

	@Override
	public void setAsciiStream(final String parameterName, final InputStream x) throws SQLException {
		checkParameterName(parameterName);
		setAsciiStream(findParameter(parameterName),x);
	}

	@Override
	public void setAsciiStream(final String parameterName, final InputStream x, final int length) throws SQLException {
		checkParameterName(parameterName);
		setAsciiStream(findParameter(parameterName),x,length);
	}

	@Override
	public void setAsciiStream(final String parameterName, final InputStream x, final long length) throws SQLException {
		checkParameterName(parameterName);
		setAsciiStream(findParameter(parameterName),x,length);
	}

	@Override
	public void setBigDecimal(final String parameterName, final BigDecimal x) throws SQLException {
		checkParameterName(parameterName);
		setBigDecimal(findParameter(parameterName),x);
	}

	@Override
	public void setBinaryStream(final String parameterName, final InputStream x) throws SQLException {
		checkParameterName(parameterName);
		setBinaryStream(findParameter(parameterName),x);
	}

	@Override
	public void setBinaryStream(final String parameterName, final InputStream x, final int length) throws SQLException {
		checkParameterName(parameterName);
		setBinaryStream(findParameter(parameterName),x,length);
	}

	@Override
	public void setBinaryStream(final String parameterName, final InputStream x, final long length) throws SQLException {
		checkParameterName(parameterName);
		setBinaryStream(findParameter(parameterName),x,length);
	}

	@Override
	public void setBlob(final String parameterName, final Blob x) throws SQLException {
		checkParameterName(parameterName);
		setBlob(findParameter(parameterName),x);
	}

	@Override
	public void setBlob(final String parameterName, final InputStream inputStream) throws SQLException {
		checkParameterName(parameterName);
		setBlob(findParameter(parameterName),inputStream);
	}

	@Override
	public void setBlob(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
		checkParameterName(parameterName);
		setBlob(findParameter(parameterName),inputStream,length);
	}

	@Override
	public void setBoolean(final String parameterName, final boolean x) throws SQLException {
		checkParameterName(parameterName);
		setBoolean(findParameter(parameterName),x);
	}

	@Override
	public void setByte(final String parameterName, final byte x) throws SQLException {
		checkParameterName(parameterName);
		setByte(findParameter(parameterName),x);
	}

	@Override
	public void setBytes(final String parameterName, final byte[] x) throws SQLException {
		checkParameterName(parameterName);
		setBytes(findParameter(parameterName),x);
	}

	@Override
	public void setCharacterStream(final String parameterName, final Reader reader) throws SQLException {
		checkParameterName(parameterName);
		setCharacterStream(findParameter(parameterName),reader);
	}

	@Override
	public void setCharacterStream(final String parameterName, final Reader reader, final int length) throws SQLException {
		checkParameterName(parameterName);
		setCharacterStream(findParameter(parameterName),reader,length);
	}

	@Override
	public void setCharacterStream(final String parameterName, final Reader reader, final long length) throws SQLException {
		checkParameterName(parameterName);
		setCharacterStream(findParameter(parameterName),reader,length);
	}

	@Override
	public void setClob(final String parameterName, final Clob x) throws SQLException {
		checkParameterName(parameterName);
		setClob(findParameter(parameterName),x);
	}

	@Override
	public void setClob(final String parameterName, final Reader reader) throws SQLException {
		checkParameterName(parameterName);
		setClob(findParameter(parameterName),reader);
	}

	@Override
	public void setClob(final String parameterName, final Reader reader, final long length) throws SQLException {
		checkParameterName(parameterName);
		setClob(findParameter(parameterName),reader,length);
	}

	@Override
	public void setDate(final String parameterName, final Date x) throws SQLException {
		checkParameterName(parameterName);
		setDate(findParameter(parameterName),x);
	}

	@Override
	public void setDate(final String parameterName, final Date x, final Calendar cal) throws SQLException {
		checkParameterName(parameterName);
		setDate(findParameter(parameterName),x,cal);
	}

	@Override
	public void setDouble(final String parameterName, final double x) throws SQLException {
		checkParameterName(parameterName);
		setDouble(findParameter(parameterName),x);
	}

	@Override
	public void setFloat(final String parameterName, final float x) throws SQLException {
		checkParameterName(parameterName);
		setFloat(findParameter(parameterName),x);
	}

	@Override
	public void setInt(final String parameterName, final int x) throws SQLException {
		checkParameterName(parameterName);
		setInt(findParameter(parameterName),x);
	}

	@Override
	public void setLong(final String parameterName, final long x) throws SQLException {
		checkParameterName(parameterName);
		setLong(findParameter(parameterName),x);
	}

	@Override
	public void setNCharacterStream(final String parameterName, final Reader value) throws SQLException {
		checkParameterName(parameterName);
		setNCharacterStream(findParameter(parameterName),value);
	}

	@Override
	public void setNCharacterStream(final String parameterName, final Reader value, final long length) throws SQLException {
		checkParameterName(parameterName);
		setNCharacterStream(findParameter(parameterName),value,length);
	}

	@Override
	public void setNClob(final String parameterName, final NClob value) throws SQLException {
		checkParameterName(parameterName);
		setNClob(findParameter(parameterName),value);
	}

	@Override
	public void setNClob(final String parameterName, final Reader reader) throws SQLException {
		checkParameterName(parameterName);
		setNClob(findParameter(parameterName),reader);
	}

	@Override
	public void setNClob(final String parameterName, final Reader reader, final long length) throws SQLException {
		checkParameterName(parameterName);
		setNClob(findParameter(parameterName),reader,length);
	}

	@Override
	public void setNString(final String parameterName, final String value) throws SQLException {
		checkParameterName(parameterName);
		setNString(findParameter(parameterName),value);
	}

	@Override
	public void setNull(final String parameterName, final int sqlType) throws SQLException {
		checkParameterName(parameterName);
		setNull(findParameter(parameterName),sqlType);
	}

	@Override
	public void setNull(final String parameterName, final int sqlType, final String typeName) throws SQLException {
		checkParameterName(parameterName);
		setNull(findParameter(parameterName),sqlType,typeName);
	}

	@Override
	public void setObject(final String parameterName, final Object x) throws SQLException {
		checkParameterName(parameterName);
		setObject(findParameter(parameterName),x);
	}

	@Override
	public void setObject(final String parameterName, final Object x, final int targetSqlType) throws SQLException {
		setObject(parameterName,x,targetSqlType,0);
	}

	@Override
	public void setObject(final String parameterName, final Object x, final int targetSqlType, final int scale) throws SQLException {
		checkParameterName(parameterName);
		setObject(findParameter(parameterName),x,targetSqlType,scale);
	}

	@Override
	public void setRowId(final String parameterName, final RowId x) throws SQLException {
		checkParameterName(parameterName);
		setRowId(findParameter(parameterName),x);
	}

	@Override
	public void setSQLXML(final String parameterName, final SQLXML xmlObject) throws SQLException {
		checkParameterName(parameterName);
		setSQLXML(findParameter(parameterName),xmlObject);
	}

	@Override
	public void setShort(final String parameterName, final short x) throws SQLException {
		checkParameterName(parameterName);
		setShort(findParameter(parameterName),x);
	}

	@Override
	public void setString(final String parameterName, final String x) throws SQLException {
		checkParameterName(parameterName);
		setString(findParameter(parameterName),x);
	}

	@Override
	public void setTime(final String parameterName, final Time x) throws SQLException {
		checkParameterName(parameterName);
		setTime(findParameter(parameterName),x);
	}

	@Override
	public void setTime(final String parameterName, final Time x, final Calendar cal) throws SQLException {
		checkParameterName(parameterName);
		setTime(findParameter(parameterName),x,cal);
	}

	@Override
	public void setTimestamp(final String parameterName, final Timestamp x) throws SQLException {
		checkParameterName(parameterName);
		setTimestamp(findParameter(parameterName),x);
	}

	@Override
	public void setTimestamp(final String parameterName, final Timestamp x, final Calendar cal) throws SQLException {
		checkParameterName(parameterName);
		setTimestamp(findParameter(parameterName),x,cal);
	}

	@Override
	public void setURL(String parameterName, URL val) throws SQLException {
		checkParameterName(parameterName);
		setURL(findParameter(parameterName),val);
	}

	@Override
	public boolean wasNull() throws SQLException {
		return wasNull;
	}

	protected void checkParameterIndex(final int parameterIndex, final boolean asOutput) throws SQLException {
		if (parameterIndex < 1 || parameterIndex > getParameterMetaData().getParameterCount()) {
			throw new SQLException("Parameter index ["+parameterIndex+"] out of range 1.."+getParameterMetaData().getParameterCount());
		}
		if (asOutput) {
			if (result[parameterIndex] == null) {
				throw new SQLException("Parameter index ["+parameterIndex+"] is not registered as output! Call registerOutputParameter() firstly");
			}
		}
	}
	
	protected void checkParameterName(final String parameterName) throws SQLFeatureNotSupportedException {
		if (parameterName == null || parameterName.isEmpty()) {
			throw new IllegalArgumentException("Parameter name can't be null or empty"); 
		}
		else {
			throw new SQLFeatureNotSupportedException("Named parameters are not supported by this statement");
		}
	}
	
	protected int findParameter(final String parameterName) throws SQLException {
		throw new SQLFeatureNotSupportedException("Named parameters are not supported by this statement");
	}
}
