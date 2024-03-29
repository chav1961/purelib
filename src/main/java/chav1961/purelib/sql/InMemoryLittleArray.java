package chav1961.purelib.sql;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.CompilerUtils;

/**
 * <p>This class implements in-memory {@linkplain Array} to use in the SQL.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.sql
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class InMemoryLittleArray implements Array {
	private final int		contentType;
	private final Object[]	content;
	private final Class<?>	returnedClass, wrappedClass;

	/**
	 * <p>Constructor of the class</p>
	 * @param contentType array content type (see {@linkplain Types})
	 * @param content array content
	 * @throws SQLException in any SQL errors
	 * @throws NullPointerException when content is null
	 * @throws IllegalArgumentException when content type is unknown
	 */
	public InMemoryLittleArray(final int contentType, final Object... content) throws SQLException, NullPointerException, IllegalArgumentException {
		if (SQLUtils.typeNameByTypeId(contentType) == null) {
			throw new IllegalArgumentException("Unknown content type ["+contentType+"]. Valid type can be any field from java.sql.Types class only"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else { 
			this.contentType = contentType;
			this.content = content;
			if (SQLUtils.DEFAULT_CONVERTOR.containsKey(SQLUtils.typeNameByTypeId(contentType))) {
				this.returnedClass = SQLUtils.DEFAULT_CONVERTOR.get(SQLUtils.typeNameByTypeId(contentType));
				this.wrappedClass = CompilerUtils.toWrappedClass(this.returnedClass);
//				switch (CompilerUtils.defineClassType(this.returnedClass)) {
//					case CompilerUtils.CLASSTYPE_REFERENCE	:	this.wrappedClass = this.returnedClass; break;
//					case CompilerUtils.CLASSTYPE_BYTE		:	this.wrappedClass = Byte.class; break;
//					case CompilerUtils.CLASSTYPE_SHORT		:	this.wrappedClass = Short.class; break;
//					case CompilerUtils.CLASSTYPE_INT		:	this.wrappedClass = Integer.class; break;
//					case CompilerUtils.CLASSTYPE_LONG		:	this.wrappedClass = Long.class; break;
//					case CompilerUtils.CLASSTYPE_FLOAT 		:	this.wrappedClass = Float.class; break;
//					case CompilerUtils.CLASSTYPE_DOUBLE		:	this.wrappedClass = Double.class; break;
//					case CompilerUtils.CLASSTYPE_CHAR		:	this.wrappedClass = Character.class; break;
//					case CompilerUtils.CLASSTYPE_BOOLEAN	:	this.wrappedClass = Boolean.class; break;
//					default : throw new UnsupportedOperationException("Primitive type ["+this.returnedClass.getSimpleName()+"] is not supported yet"); 
//				}
			}
			else {
				throw new SQLException("Content type ["+contentType+"] is not supported for array"); 
			}
		}
	}
	
	@Override
	public void free() throws SQLException {
	}

	@Override
	public Object getArray() throws SQLException {
		return getArray(1,content.length);
	}

	@Override
	public Object getArray(final Map<String, Class<?>> map) throws SQLException {
		return getArray(1,content.length,map);
	}

	@Override
	public Object getArray(final long index, final int count) throws SQLException {
		return getArray(index,count,SQLUtils.DEFAULT_CONVERTOR);
	}

	@Override
	public Object getArray(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
		if (index < 1 || index > content.length) {
			throw new ArrayIndexOutOfBoundsException("Array index ["+index+"] out of range 1.."+content.length); 
		}
		else if (map == null) {
			throw new NullPointerException("Map to convert can't be null"); 
		}
		else {
			final int		end = (int) Math.min(count+index-1,content.length);
			final Object[]	result = (Object[])java.lang.reflect.Array.newInstance(wrappedClass,(int)(end-(index-1)));

			for (int curs = 0; curs < end; curs++) {
				result[curs] = SQLUtils.convert((int)(curs+(index-1)),1,wrappedClass,content[(int)(curs + (index-1))]);
			}
			switch (CompilerUtils.defineClassType(this.returnedClass)) {
				case CompilerUtils.CLASSTYPE_REFERENCE	:	return result;
				case CompilerUtils.CLASSTYPE_BYTE		:	return Utils.unwrapArray((Byte[])result);
				case CompilerUtils.CLASSTYPE_SHORT		:	return Utils.unwrapArray((Short[])result);
				case CompilerUtils.CLASSTYPE_INT		:	return Utils.unwrapArray((Integer[])result);
				case CompilerUtils.CLASSTYPE_LONG		:	return Utils.unwrapArray((Long[])result);
				case CompilerUtils.CLASSTYPE_FLOAT	 	:	return Utils.unwrapArray((Float[])result);
				case CompilerUtils.CLASSTYPE_DOUBLE		:	return Utils.unwrapArray((Double[])result);
				case CompilerUtils.CLASSTYPE_CHAR		:	return Utils.unwrapArray((Character[])result);
				case CompilerUtils.CLASSTYPE_BOOLEAN	:	return Utils.unwrapArray((Boolean[])result);
				default : throw new UnsupportedOperationException("Primitive type ["+this.returnedClass.getSimpleName()+"] is not supported yet"); 
			}
		}
	}

	@Override
	public int getBaseType() throws SQLException {
		return contentType;
	}

	@Override
	public String getBaseTypeName() throws SQLException {
		return SQLUtils.typeNameByTypeId(getBaseType());
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return getResultSet(1,content.length);
	}

	@Override
	public ResultSet getResultSet(final Map<String, Class<?>> map) throws SQLException {
		return getResultSet(1,content.length,map);
	}

	@Override
	public ResultSet getResultSet(final long index, final int count) throws SQLException {
		return getResultSet(index,count,SQLUtils.DEFAULT_CONVERTOR);
	}

	@Override
	public ResultSet getResultSet(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
		if (index < 1 || index > content.length) {
			throw new ArrayIndexOutOfBoundsException("Array index ["+index+"] out of range 1.."+content.length); 
		}
		else if (map == null) {
			throw new NullPointerException("Map to convert can't be null"); 
		}
		else {
			final int			end = (int) Math.min(count+index-1,content.length);
			final Object[][]	result = new Object[(int)(end-(index-1))][];
	
			for (int curs = 0; curs < end; curs++) {
				result[curs] = new Object[]{curs+index,content[(int)(curs + (index-1))]};
			}
			try{return new InMemoryReadOnlyResultSet(new ArrayResultSetMetaData("INDEX:INTEGER","VALUE:JAVA_OBJECT"),ResultSet.TYPE_FORWARD_ONLY,new ArrayContent(result),map);
			} catch (SyntaxException e) {
				throw new SQLException(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public String toString() {
		return "InMemoryLitteArray [contentType=" + contentType + ", content=" + Arrays.toString(content) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(content);
		result = prime * result + contentType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		InMemoryLittleArray other = (InMemoryLittleArray) obj;
		if (!Arrays.equals(content, other.content)) return false;
		if (contentType != other.contentType) return false;
		return true;
	}
	
	private static class ArrayResultSetMetaData extends AbstractResultSetMetaData {
		ArrayResultSetMetaData(final String... columns) throws SyntaxException {
			super(SQLUtils.prepareMetadata(columns),true);
		}

		@Override
		public String getSchemaName(int column) throws SQLException {
			return null;
		}

		@Override
		public String getTableName(int column) throws SQLException {
			return null;
		}

		@Override
		public String getCatalogName(int column) throws SQLException {
			return null;
		}
	}
}
