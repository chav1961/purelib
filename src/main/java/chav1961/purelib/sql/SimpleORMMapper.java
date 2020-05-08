package chav1961.purelib.sql;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.ORMMapper;
import chav1961.purelib.streams.char2byte.CompilerUtils;

public class SimpleORMMapper<T> implements ORMMapper<T> {
	
	private final Pair[]	pairs;
	private final Class<T>	clazz;
	
	public SimpleORMMapper(final ContentNodeMetadata record, final ContentNodeMetadata resultSet) throws ContentException, IllegalArgumentException, NullPointerException {
		this(record,resultSet,(m)->{});
	}
	
	public SimpleORMMapper(final ContentNodeMetadata record, final ContentNodeMetadata resultSet, final ModuleAccessor accessor) throws ContentException, IllegalArgumentException, NullPointerException {
		if (record == null) {
			throw new NullPointerException("Record metadata can't be null");
		}
		else if (resultSet == null) {
			throw new NullPointerException("Result set metadata can't be null");
		}
		else if (accessor == null) {
			throw new NullPointerException("Module accessor can't be null");
		}
		else {
			final Set<String>	left = new HashSet<>(), right = new HashSet<>();
			final List<Pair>	content = new ArrayList<>();
			
			for (ContentNodeMetadata item : record) {
				left.add(item.getName());
			}
			for (ContentNodeMetadata item : resultSet) {
				right.add(item.getName());
			}
			left.retainAll(right);
			
			if (left.isEmpty()) {
				throw new IllegalArgumentException("Two models don't intersect (no any field names presents in both models simultaneously)");
			}
			else {
				for (ContentNodeMetadata item : record) {
					if (left.contains(item.getName())) {
						content.add(new Pair(item.getName(),GettersAndSettersFactory.buildGetterAndSetter(record.getType(),item.getName()),CompilerUtils.defineClassType(item.getType()),item.getType()));
					}
				}
				this.pairs = content.toArray(new Pair[content.size()]);
				this.clazz = (Class<T>) record.getType();
				
				left.clear();
				right.clear();
				content.clear();
			}
		}
	}

	@Override
	public void fromRecord(final T content, final ResultSet rs) throws SQLException {
		if (content == null || !clazz.isAssignableFrom(content.getClass())) {
			throw new IllegalArgumentException("Content record can't be null and need be instance of ["+clazz.getCanonicalName()+"] class");
		}
		else if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else {
			for (Pair item : pairs) {
				try{switch (item.getClassType()) {
						case CompilerUtils.CLASSTYPE_REFERENCE	:
							final Object	value = rs.getObject(item.getName());
							
							((ObjectGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? null : SQLUtils.convert(item.getValueClass(),value));
							break;
						case CompilerUtils.CLASSTYPE_BYTE		:
							final byte		byteValue = rs.getByte(item.getName());
							
							((ByteGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? 0 : byteValue);
							break;
						case CompilerUtils.CLASSTYPE_SHORT		:
							final short		shortValue = rs.getShort(item.getName());
							
							((ShortGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? 0 : shortValue);
							break;
						case CompilerUtils.CLASSTYPE_CHAR		:	
							final String	charValue = rs.getString(item.getName());
							
							((CharGetterAndSetter)item.getGas()).set(content,!rs.wasNull() && !charValue.isEmpty() ? charValue.charAt(0) : ' ');
							break;
						case CompilerUtils.CLASSTYPE_INT		:	
							final int		intValue = rs.getInt(item.getName());
							
							((IntGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? 0 : intValue);
							break;
						case CompilerUtils.CLASSTYPE_LONG		:	
							final long		longValue = rs.getLong(item.getName());
							
							((LongGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? 0 : longValue);
							break;
						case CompilerUtils.CLASSTYPE_FLOAT		:	
							final float 	floatValue = rs.getFloat(item.getName());
							
							((FloatGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? 0 : floatValue);
							break;
						case CompilerUtils.CLASSTYPE_DOUBLE		:
							final double 	doubleValue = rs.getDouble(item.getName());
							
							((DoubleGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? 0 : doubleValue);
							break;
						case CompilerUtils.CLASSTYPE_BOOLEAN	:
							final boolean 	booleanValue = rs.getBoolean(item.getName());
							
							((BooleanGetterAndSetter)item.getGas()).set(content,rs.wasNull() ? false : booleanValue);
							break;
						default :
							throw new UnsupportedOperationException("Class type ["+item.getClassType()+"] is not implemented yet"); 
					}
				} catch (ContentException  e) {
					throw new SQLException(e.getLocalizedMessage(),e);
				}
			}
		}
	}

	@Override
	public void toRecord(final T content, final PreparedStatement ps) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet"); 
	}
	
	private static class Pair {
		private final String			name;
		private final GetterAndSetter	gas;
		private final int				classType;
		private final Class<?>			clazz;
		
		public Pair(final String name, final GetterAndSetter gas, final int classType, final Class<?> clazz) {
			this.name = name;
			this.gas = gas;
			this.classType = classType;
			this.clazz = clazz;
		}
		
		public String getName() {
			return name;
		}

		public GetterAndSetter getGas() {
			return gas;
		}

		public int getClassType() {
			return classType;
		}

		public Class<?> getValueClass() {
			return clazz;
		}
		
		@Override
		public String toString() {
			return "Pair [name=" + name + ", gas=" + gas + ", classType=" + classType + "]";
		}
	}
}
