package chav1961.purelib.sql;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public abstract class AbstractResultSetMetaData implements ResultSetMetaData {
	protected final RsMetaDataElement[]	columns;
	protected final boolean				readOnly;
	
	public AbstractResultSetMetaData(final RsMetaDataElement[] columns, final boolean readOnly) {
		if (columns == null || columns.length == 0) {
			throw new IllegalArgumentException("Column dscriptor's list can't be null or empty array");
		}
		else {
			this.columns = columns;
			this.readOnly = readOnly;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
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
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Class to unwrap can't be null"); 
		}
		else {
			return iface.isAssignableFrom(this.getClass()); 
		}
	}

	@Override
	public int getColumnCount() throws SQLException {
		return columns.length;
	}

	@Override public abstract String getSchemaName(int column) throws SQLException;
	@Override public abstract String getTableName(int column) throws SQLException;
	@Override public abstract String getCatalogName(int column) throws SQLException;

	@Override
	public int isNullable(final int column) throws SQLException {
		checkColumnNumber(column);
		return ResultSetMetaData.columnNullableUnknown;
	}

	@Override 
	public boolean isSigned(int column) throws SQLException {
		checkColumnNumber(column);
		return true;
	}
	
	@Override 
	public boolean isAutoIncrement(final int column) throws SQLException {
		checkColumnNumber(column);
		return false;
	}
	
	@Override 
	public boolean isSearchable(int column) throws SQLException {
		checkColumnNumber(column);
		return false;
	}
	
	@Override 
	public boolean isReadOnly(int column) throws SQLException {
		checkColumnNumber(column);
		return readOnly;
	}
	
	@Override 
	public boolean isWritable(int column) throws SQLException {
		checkColumnNumber(column);
		return !readOnly;
	}

	@Override 
	public boolean isCurrency(final int column) throws SQLException {
		checkColumnNumber(column);
		return getColumnClass(column) == BigDecimal.class;
	}

	@Override 
	public boolean isCaseSensitive(int column) throws SQLException {
		return true;
	}
	
	@Override
	public boolean isDefinitelyWritable(final int column) throws SQLException {
		checkColumnNumber(column);
		return isWritable(column);
	}
	
	@Override
	public int getColumnDisplaySize(final int column) throws SQLException {
		checkColumnNumber(column);
		return columns[column-1].getLength();
	}

	@Override
	public String getColumnLabel(final int column) throws SQLException {
		checkColumnNumber(column);
		return columns[column-1].getDescription();
	}

	@Override
	public String getColumnName(final int column) throws SQLException {
		checkColumnNumber(column);
		return columns[column-1].getName();
	}

	@Override
	public int getPrecision(final int column) throws SQLException {
		checkColumnNumber(column);
		return columns[column-1].getLength();
	}

	@Override
	public int getScale(final int column) throws SQLException {
		checkColumnNumber(column);
		return columns[column-1].getFrac();
	}

	@Override
	public int getColumnType(final int column) throws SQLException {
		checkColumnNumber(column);
		return columns[column-1].getType();
	}

	@Override
	public String getColumnTypeName(final int column) throws SQLException {
		checkColumnNumber(column);
		return columns[column-1].getTypeName();
	}

	@Override
	public String getColumnClassName(final int column) throws SQLException {
		checkColumnNumber(column);
		return getColumnClass(column).getName();
	}

	protected void checkColumnNumber(final int column) {
		if (column < 1 || column > columns.length) {
			throw new IllegalArgumentException("Column number ["+column+"] out of range 1.."+columns.length);
		}
	}
	
	protected Class<?> getColumnClass(int column) throws SQLException {
		if (SQLUtils.DEFAULT_CONVERTOR.containsKey(columns[column-1].getTypeName())) {
			return SQLUtils.DEFAULT_CONVERTOR.get(columns[column-1].getTypeName());
		}
		else {
			return Object.class;
		}
	}
}
