package chav1961.purelib.sql.interfaces;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.sql.Types;

public enum DomainType {
    BIT(Types.BIT, Boolean.class, false),
    TINYINT(Types.TINYINT, Byte.class, (byte)0),
    SMALLINT(Types.SMALLINT, Short.class, (short)0),
    INTEGER(Types.INTEGER, Integer.class, 0),
    BIGINT(Types.BIGINT, Long.class, 0L),
    FLOAT(Types.FLOAT, Float.class, 0.0f),
    REAL(Types.REAL, Float.class, 0.0f),
    DOUBLE(Types.DOUBLE, Double.class, 0.0),
    NUMERIC(Types.NUMERIC, BigDecimal.class, BigDecimal.ZERO),
    DECIMAL(Types.DECIMAL, BigDecimal.class, BigDecimal.ZERO),
    CHAR(Types.CHAR, String.class, ""),
    VARCHAR(Types.VARCHAR, String.class, ""),
    LONGVARCHAR(Types.LONGVARCHAR, String.class, ""),
    DATE(Types.DATE, Date.class, new Date(0)),
    TIME(Types.TIME, Timestamp.class, new Timestamp(0)),
    TIMESTAMP(Types.TIMESTAMP, Timestamp.class, new Timestamp(0)),
    BINARY(Types.BINARY, byte[].class, new byte[0]),
    VARBINARY(Types.VARBINARY, byte[].class, new byte[0]),
    LONGVARBINARY(Types.LONGVARBINARY, byte[].class, new byte[0]),
    NULL(Types.NULL, Object.class, null),
    OTHER(Types.OTHER, Object.class, new Object()),
    JAVA_OBJECT(Types.JAVA_OBJECT, Object.class, new Object()),
    DISTINCT(Types.DISTINCT, Object.class, new Object()),
    STRUCT(Types.STRUCT, Struct.class, null),
    ARRAY(Types.ARRAY, Array.class, null),
    BLOB(Types.BLOB, Blob.class, null),
    CLOB(Types.CLOB, Clob.class, null),
    REF(Types.REF, Object.class, null),
    DATALINK(Types.DATALINK, Object.class, null),
    BOOLEAN(Types.BOOLEAN, Boolean.class, false),
    ROWID(Types.ROWID, RowId.class, null),
    NCHAR(Types.NCHAR, String.class, ""),
    NVARCHAR(Types.NVARCHAR, String.class, ""),
    LONGNVARCHAR(Types.LONGNVARCHAR, String.class, ""),
    NCLOB(Types.NCLOB, NClob.class, null),
    SQLXML(Types.SQLXML, SQLXML.class, null),
    REF_CURSOR(Types.REF_CURSOR, ResultSet.class, null),
    TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE, Timestamp.class, new Timestamp(0)),
    TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE, Timestamp.class, new Timestamp(0));

	private final int		sqlType;
	private final Class<?>	nativeClass;
	private final Object	emptyValue;
	
	private <T> DomainType(final int sqlType, final Class<T> nativeClass, final T emptyValue) {
		this.sqlType = sqlType;
		this.nativeClass = nativeClass;
		this.emptyValue = emptyValue;
	}
	
	public int getSqlType() {
		return sqlType;
	}

	public Class<?> getNativeClass() {
		return nativeClass;
	}
	
	public Object getEmptyValue() {
		return emptyValue;
	}
	
	public void bindPreparedStatement(final PreparedStatement ps, final int parameterIndex, final Object value) throws SQLException {
		if (ps == null) {
			throw new NullPointerException("Prepared statement can't be null"); 
		}
		else if (parameterIndex < 1 || parameterIndex > ps.getParameterMetaData().getParameterCount()) {
			throw new IllegalArgumentException("Parameter index [" + parameterIndex + "] out of range 1.." + ps.getParameterMetaData().getParameterCount()); 
		}
		else if (value == null) {
			ps.setNull(parameterIndex, getSqlType());
		}
		else if (value.getClass() == getNativeClass()) {
			ps.setObject(parameterIndex, value);
		}
		else {
			ps.setObject(parameterIndex, value, getSqlType());
		}
	}

	public void bindResultSet(final ResultSet rs, final int columnIndex, final Object value) throws SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null"); 
		}
		else if (columnIndex < 1 || columnIndex > rs.getMetaData().getColumnCount()) {
			throw new IllegalArgumentException("Column index [" + columnIndex + "] out of range 1.." + rs.getMetaData().getColumnCount()); 
		}
		else {
			rs.updateObject(columnIndex, value, getSqlType());
		}
	}

	public void bindResultSet(final ResultSet rs, final String columnName, final Object value) throws SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null"); 
		}
		else if (columnName == null || columnName.isEmpty()) {
			throw new IllegalArgumentException("Column name can't be null or empty"); 
		}
		else {
			bindResultSet(rs, rs.findColumn(columnName), value);
		}
	}
	
	public static DomainType valueOf(final int type) throws IllegalArgumentException {
		for (DomainType item : values()) {
			if (item.sqlType == type) {
				return item;
			}
		}
		throw new IllegalArgumentException("SQL type ["+type+"] not exists in the domain");
	}
}
