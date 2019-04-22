package chav1961.purelib.sql.junit;

import java.lang.reflect.Array;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.RsMetaDataElement;

class JUnitTableImpl<T> extends JUnitEntityImpl {
	private final String						schemaName;
	private final String						tableName;
	private final Class<T>						rowType;
	private final ObjectGetterAndSetter<T[]>	access;
	private final JUnitFieldImpl[]				fields;
	private final ResultSetMetaData				rsmd;
	
	JUnitTableImpl(final String schemaName, final String tableName, final Class<T> rowType, final ObjectGetterAndSetter<T[]> access, final JUnitFieldImpl... fields) {
		final int	nullIndex;
		
		if (schemaName == null || schemaName.isEmpty()) {
			throw new IllegalArgumentException("Schema name can't be null or empty");
		}
		else if (tableName == null || tableName.isEmpty()) {
			throw new IllegalArgumentException("Table name for schema ["+schemaName+"] can't be null or empty");
		}
		else if (rowType == null) {
			throw new NullPointerException("Row type descriptor for table ["+tableName+"] can't be null");
		}
		else if (access == null) {
			throw new NullPointerException("Getter/setter for table ["+tableName+"] can't be null");
		}
		else if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("Field list for table ["+tableName+"] can't be null or empty array");
		}
		else if ((nullIndex = Utils.checkArrayContent4Nulls(fields)) >= 0) {
			throw new NullPointerException("Field's list for table ["+tableName+"] contains null at index ["+nullIndex+"]");
		}
		else {
			final RsMetaDataElement[]	content = new RsMetaDataElement[fields.length];
			
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.rowType = rowType;
			this.access = access;
			this.fields = fields;
			
			for (int index = 0; index < content.length; index++) {
				content[index] = fields[index].getMetaData();
			}
			this.rsmd = new AbstractResultSetMetaData(content,false) {
				@Override
				public String getTableName(int column) throws SQLException {
					return JUnitTableImpl.this.getTableName();
				}
				
				@Override
				public String getSchemaName(int column) throws SQLException {
					return JUnitTableImpl.this.getSchemaName();
				}
				
				@Override
				public String getCatalogName(int column) throws SQLException {
					return null;
				}
			};
		}
	}
	
	@Override
	public JUnitEntityType getType() {
		return JUnitEntityType.TABLE;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getTableName() {
		return tableName;
	}
	
	public ResultSetMetaData getMetadata() {
		return null;
	}
	
	public int getRowCount(final Object instance) throws ContentException {
		if (instance == null) {
			throw new NullPointerException("Intance can't be null");
		}
		else {
			return Array.getLength(access.get(instance));
		}
	}
	
	public <T> T getRow(final Object instance, final int rowId) throws ContentException {
		if (instance == null) {
			throw new NullPointerException("Intance can't be null");
		}
		else if (rowId < 0 || rowId >= Array.getLength(access.get(instance))) {
			throw new IllegalArgumentException("Row id ["+rowId+"] out of range 0.."+(Array.getLength(access.get(instance))-1));
		} 
		else {
			return (T)Array.get(access.get(instance),rowId);
		}
	}

	public void insert(final Object instance, final T newRow) throws ContentException {
		if (instance == null) {
			throw new NullPointerException("Intance can't be null");
		}
		else if (newRow == null) {
			throw new NullPointerException("Row to insert can't be null");
		}
		else {
			final Object[]	oldContent = (Object[]) access.get(instance);
			final Object[]	newContent = (Object[]) Array.newInstance(rowType,oldContent.length+1);
			
			System.arraycopy(oldContent,0,newContent,0,oldContent.length);
			Array.set(newContent,oldContent.length,newRow);
			access.set(instance,(T[])newContent);
		}
	}

	public void delete(final Object instance, final int rowId) throws ContentException {
		if (instance == null) {
			throw new NullPointerException("Intance can't be null");
		}
		else if (rowId < 0 || rowId >= Array.getLength(access.get(instance))) {
			throw new IllegalArgumentException("Row id ["+rowId+"] out of range 0.."+(Array.getLength(access.get(instance))-1));
		} 
		else {
			final Object[]	oldContent = (Object[]) access.get(instance);
			final Object[]	newContent = (Object[]) Array.newInstance(rowType,oldContent.length-1);
	
			if (rowId != 0) {
				System.arraycopy(oldContent,0,newContent,0,rowId);
			}
			System.arraycopy(oldContent,rowId+1,newContent,rowId,oldContent.length-rowId);
			access.set(instance,(T[])newContent);
		}
	}
	
	public JUnitFieldImpl getFieldDesc(final int columnNumber) {
		if (columnNumber < 0 || columnNumber >= fields.length) {
			throw new IllegalArgumentException("Column number ["+columnNumber+"] out of range 0.."+(fields.length-1));
		}
		else {
			return fields[columnNumber];
		}
	}
}