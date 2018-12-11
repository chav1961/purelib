package chav1961.purelib.sql;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class InternalUtils {
	public static final int							UNKNOWN_TYPE = 666;
	static final Map<String,Class<?>>				DEFAULT_CONVERTOR = new HashMap<>();
		
	private static final JDBCTypeDescriptor[]		TYPE_DECODER;
	private static final ConversionDescriptor[]		CONVERSION_PAIRS;
	private static final char[]						ESCAPES = {'%', '_'};
	private static final char[]						RUE = {'r', 'u', 'e'};
	private static final char[]						ALSE = {'a', 'l', 's', 'e'};
	
	
	@FunctionalInterface
	private interface ConversionCall {
		Object convert(Object source) throws ContentException;
	}
	
	
	static {
		DEFAULT_CONVERTOR.put("ARRAY",Array.class);
		DEFAULT_CONVERTOR.put("BIGINT",long.class);
		DEFAULT_CONVERTOR.put("BINARY",byte[].class);
		DEFAULT_CONVERTOR.put("BIT",boolean.class);
		DEFAULT_CONVERTOR.put("BLOB",Blob.class);
		DEFAULT_CONVERTOR.put("BOOLEAN",boolean.class);
		DEFAULT_CONVERTOR.put("CHAR",String.class);
		DEFAULT_CONVERTOR.put("CLOB",Clob.class);
		DEFAULT_CONVERTOR.put("DATALINK",Object.class);
		DEFAULT_CONVERTOR.put("DATE",Date.class);
		DEFAULT_CONVERTOR.put("DECIMAL",BigDecimal.class);
		DEFAULT_CONVERTOR.put("DISTINCT",Object.class);
		DEFAULT_CONVERTOR.put("DOUBLE",double.class);
		DEFAULT_CONVERTOR.put("FLOAT",float.class);
		DEFAULT_CONVERTOR.put("INTEGER",int.class);
		DEFAULT_CONVERTOR.put("JAVA_OBJECT",Object.class);
		DEFAULT_CONVERTOR.put("LONGNVARCHAR",String.class);
		DEFAULT_CONVERTOR.put("LONGVARBINARY",byte[].class);
		DEFAULT_CONVERTOR.put("LONGVARCHAR",String.class);
		DEFAULT_CONVERTOR.put("NCHAR",String.class);
		DEFAULT_CONVERTOR.put("NCLOB",NClob.class);
		DEFAULT_CONVERTOR.put("NULL",Object.class);
		DEFAULT_CONVERTOR.put("NUMERIC",BigDecimal.class);
		DEFAULT_CONVERTOR.put("NVARCHAR",String.class);
		DEFAULT_CONVERTOR.put("OTHER",Object.class);
		DEFAULT_CONVERTOR.put("REAL",BigDecimal.class);
		DEFAULT_CONVERTOR.put("REF",Object.class);
		DEFAULT_CONVERTOR.put("REF_CURSOR",Object.class);
		DEFAULT_CONVERTOR.put("ROWID",RowId.class);
		DEFAULT_CONVERTOR.put("SMALLINT",short.class);
		DEFAULT_CONVERTOR.put("SQLXML",SQLXML.class);
		DEFAULT_CONVERTOR.put("STRUCT",Struct.class);
		DEFAULT_CONVERTOR.put("TIME",Object.class);
		DEFAULT_CONVERTOR.put("TIME_WITH_TIMEZONE",Time.class);
		DEFAULT_CONVERTOR.put("TIMESTAMP",Timestamp.class);
		DEFAULT_CONVERTOR.put("TIMESTAMP_WITH_TIMEZONE",Timestamp.class);
		DEFAULT_CONVERTOR.put("TINYINT",byte.class);
		DEFAULT_CONVERTOR.put("VARBINARY",byte[].class);
		DEFAULT_CONVERTOR.put("VARCHAR",String.class);
		
		int	count = 0;
		
		TYPE_DECODER = new JDBCTypeDescriptor[Types.class.getFields().length];
		
		for (Field f : Types.class.getFields()) {
			try{TYPE_DECODER[count++] = new JDBCTypeDescriptor(f.getInt(null),f.getName());
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
		
		CONVERSION_PAIRS = new ConversionDescriptor[]{
				new ConversionDescriptor(String.class,prepareConversion4String())
				, new ConversionDescriptor(Boolean.class,prepareConversion4Boolean())
				, new ConversionDescriptor(Byte.class,prepareConversion4Byte())
				, new ConversionDescriptor(Short.class,prepareConversion4Short())
				, new ConversionDescriptor(Integer.class,prepareConversion4Integer())
				, new ConversionDescriptor(Long.class,prepareConversion4Long())
				, new ConversionDescriptor(Float.class,prepareConversion4Float())
				, new ConversionDescriptor(Double.class,prepareConversion4Double())
				, new ConversionDescriptor(BigDecimal.class,prepareConversion4BigDecimal())
				, new ConversionDescriptor(BigInteger.class,prepareConversion4BigInteger())
				, new ConversionDescriptor(byte[].class,prepareConversion4ByteArray())
				, new ConversionDescriptor(Blob.class,prepareConversion4Blob())
				, new ConversionDescriptor(Clob.class,prepareConversion4Clob())
				, new ConversionDescriptor(NClob.class,prepareConversion4NClob())
				, new ConversionDescriptor(Date.class,prepareConversion4Date())
				, new ConversionDescriptor(Time.class,prepareConversion4Time())
				, new ConversionDescriptor(Timestamp.class,prepareConversion4Timestamp())
				, new ConversionDescriptor(InputStream.class,prepareConversion4InputStream())
				, new ConversionDescriptor(Reader.class,prepareConversion4Reader())
				, new ConversionDescriptor(Array.class,prepareConversion4Array())
		};
	}
	
	public static RsMetaDataElement[] prepareMetadata(final String... description) {
		if (description == null || description.length == 0) {
			throw new IllegalArgumentException("Description can't be null or empty array");
		}
		else {
			final RsMetaDataElement		answer[] = new RsMetaDataElement[description.length]; 
			
			for (int index = 0; index < description.length; index++) {
				if (description[index] == null || description[index].isEmpty()) {
					throw new IllegalArgumentException("Element ["+index+"] in the description list contains null or empty string");
				}
				else {
					answer[index] = prepareMetaDataElement(index,description[index]);
				}
			}
			return answer;
		}
	}
	
	public static String typeNameByTypeId(final int typeId) {
		for (JDBCTypeDescriptor item : TYPE_DECODER) {
			if (item.getType() == typeId) {
				return item.getTypeName();
			}
		}
		return null;
	}

	public static int typeIdByTypeName(final String typeName) {
		if (typeName == null || typeName.isEmpty()) {
			throw new IllegalArgumentException("Type name can't be null or empty");
		}
		else {
			for (JDBCTypeDescriptor item : TYPE_DECODER) {
				if (item.getTypeName().equalsIgnoreCase(typeName)) {
					return item.getType();
				}
			}
			return UNKNOWN_TYPE;
		}
	}
	
	public static <T> T convert(final int rowIndex, final int columnIndex, final Class<T> awaited, final Object value) throws SQLException {
		try{return convert(awaited,value);
		} catch (ContentException exc) {
			throw new SQLException("Row ["+rowIndex+"], col ["+columnIndex+"]: "+exc.getLocalizedMessage(),exc); 
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T convert(final Class<T> awaited, final Object value) throws ContentException {
		if (value == null) {
			switch (Utils.defineClassType(awaited)) {
				case Utils.CLASSTYPE_REFERENCE	:	return null;
				case Utils.CLASSTYPE_BYTE		:	return (T)Byte.valueOf((byte)0);
				case Utils.CLASSTYPE_SHORT		:	return (T)Short.valueOf((short)0);
				case Utils.CLASSTYPE_INT		:	return (T)Integer.valueOf((int)0);
				case Utils.CLASSTYPE_LONG		:	return (T)Long.valueOf((long)0);
				case Utils.CLASSTYPE_FLOAT	 	:	return (T)Float.valueOf(0.0f);
				case Utils.CLASSTYPE_DOUBLE		:	return (T)Double.valueOf(0.0);
				case Utils.CLASSTYPE_CHAR		:	return (T)Character.valueOf(' ');
				case Utils.CLASSTYPE_BOOLEAN	:	return (T)Boolean.valueOf(false);
				default : throw new UnsupportedOperationException("Primitive type ["+awaited.getSimpleName()+"] is not supported yet"); 
			}
		}
		else if (awaited.isAssignableFrom(value.getClass())) {
			return (T)value;
		}
		else if (awaited.isPrimitive()) {
			switch (Utils.defineClassType(awaited)) {
				case Utils.CLASSTYPE_BYTE		:	return (T)convert(Byte.class,value);
				case Utils.CLASSTYPE_SHORT		:	return (T)convert(Short.class,value);
				case Utils.CLASSTYPE_INT		:	return (T)convert(Integer.class,value);
				case Utils.CLASSTYPE_LONG		:	return (T)convert(Long.class,value);
				case Utils.CLASSTYPE_FLOAT	 	:	return (T)convert(Float.class,value);
				case Utils.CLASSTYPE_DOUBLE		:	return (T)convert(Double.class,value);
				case Utils.CLASSTYPE_CHAR		:	return (T)convert(Character.class,value);
				case Utils.CLASSTYPE_BOOLEAN	:	return (T)convert(Boolean.class,value);
				default : throw new UnsupportedOperationException("Primitive type ["+awaited.getSimpleName()+"] is not supported yet"); 
			}
		}
		else {
			return 	convertInternal(awaited,value);		
		}
	}
	
	public static Class<?> classBySqlTypeName(final String sqlType) {
		if (sqlType == null || sqlType.isEmpty()) {
			throw new IllegalArgumentException("SQL type can't be null or empty"); 
		}
		else {
			return DEFAULT_CONVERTOR.get(sqlType);
		}
	}

	public static boolean matchLikeStyledTemplate(final String testString, final String template) {
		if (template == null || template.isEmpty()) {
			throw new IllegalArgumentException("String template can't be null or empty");
		}
		else if (testString == null) {
			throw new NullPointerException("String to test can't be null");
		}
		else {
			return buildPattern4LikeStyledTemplate(template).matcher(testString).matches();
		}
	}

	public static Pattern buildPattern4LikeStyledTemplate(final String template) {
		return buildPattern4LikeStyledTemplate(template,ESCAPES);
	}
	
	public static Pattern buildPattern4LikeStyledTemplate(final String template, final char[] escaping) {
		if (template == null || template.isEmpty()) {
			throw new IllegalArgumentException("String template can't be null or empty");
		}
		else {
			return null;
		}
	}
	
	public static boolean canConvert(final int fromType, final int toType) {
		return true;
	}

	private static <T> T convertInternal(final Class<T> awaited, final Object value) throws ContentException {
		final Class<?>	sourceClass = value.getClass();
		
		for (ConversionDescriptor item : CONVERSION_PAIRS) {
			if (item.sourceClass.isAssignableFrom(sourceClass)) {
				try{final ConversionCall	cc = item.target.get(awaited);
					
					if (cc != null) {
						return (T)cc.convert(value);
					}
				} catch (RuntimeException exc) {
					exc.printStackTrace();
					throw new ContentException("Error converting ["+value+"] to ["+awaited.getName()+"]: "+exc.getLocalizedMessage());
				}
			}
		}
		throw new ContentException("Conversion from ["+value.getClass().getName()+"] to ["+awaited.getName()+"] is not supported");
	}
	
	
	private static RsMetaDataElement prepareMetaDataElement(final int element, final String content) {
		final char[]		source = (content+";").toCharArray();
		final StringBuilder	sb = new StringBuilder();
		int					pos = 0;

		if (!Character.isJavaIdentifierStart(source[pos])) {
			throw new IllegalArgumentException("Syntax error in the ["+element+"] element of metadata description ("+content+"): invalid start of the name at position "+pos); 
		}
		else {
			while (Character.isJavaIdentifierPart(source[pos])) {
				sb.append(source[pos++]);
			}
			if (source[pos] != ':') {
				throw new IllegalArgumentException("Syntax error in the ["+element+"] element of metadata description ("+content+"): missing (:) at position "+pos); 
			}
			else {
				final String		name = sb.toString(), typeName;
				final int			length, frac;
				
				sb.setLength(0);	pos++;
				
				while (Character.isJavaIdentifierPart(source[pos])) {
					sb.append(source[pos++]);
				}
				typeName = sb.toString().toUpperCase();
				
				if (source[pos] == '(') {
					int				sum;
					
					sum = 0;		pos++;
					while (Character.isDigit(source[pos])) {
						sum = 10 * sum + source[pos] - '0';
						pos++;
					}
					length = sum;
					if (source[pos] == ',') {
						sum = 0;		pos++;
						while (Character.isDigit(source[pos])) {
							sum = 10 * sum + source[pos] - '0';
							pos++;
						}
						frac = sum;
					}
					else {
						frac = 0;
					}
					if (source[pos] != ')') {
						throw new IllegalArgumentException("Syntax error in the ["+element+"] element of metadata description ("+content+"): missing close bracket at position "+pos); 
					}
					else {
						if (length < frac) {
							throw new IllegalArgumentException("Syntax error in the ["+element+"] element of metadata description ("+content+"): field length less than fractional at position "+pos); 
						}
						final int	numericType = typeIdByTypeName(typeName);
						
						if (numericType != 0 && numericType != InternalUtils.UNKNOWN_TYPE) {
							return new RsMetaDataElement(name,name,typeName,numericType,length,frac);
						}
						else {
							throw new IllegalArgumentException("Syntax error in the ["+element+"] element of metadata description ("+content+"): type name ["+typeName+"] is missing in the java.sql.Types class fields"); 
						}
					}
				}
				else {
					final int	numericType = typeIdByTypeName(typeName);
					
					if (numericType != 0 && numericType != InternalUtils.UNKNOWN_TYPE) {
						return new RsMetaDataElement(name,name,typeName,numericType,0,0);
					}
					else {
						throw new IllegalArgumentException("Syntax error in the ["+element+"] element of metadata description ("+content+"): type name ["+typeName+"] is missing in the java.sql.Types class fields"); 
					}
				}				
			}
		}
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4String() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{return Boolean.valueOf(source.toString());});
		toMap.put(Byte.class,(source)->{return Byte.valueOf(source.toString());});
		toMap.put(Short.class,(source)->{return Short.valueOf(source.toString());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf(source.toString());});
		toMap.put(Long.class,(source)->{return Long.valueOf(source.toString());});
		toMap.put(Float.class,(source)->{return Float.valueOf(source.toString());});
		toMap.put(Double.class,(source)->{return Double.valueOf(source.toString());});
		toMap.put(BigInteger.class,(source)->{return new BigInteger(source.toString());});
		toMap.put(BigDecimal.class,(source)->{return new BigDecimal(source.toString());});
		toMap.put(byte[].class,(source)->{return ((String)source).getBytes();});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(source.toString().getBytes());});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(source.toString());});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(source.toString());});
		toMap.put(Date.class,(source)->{return new Date(Long.valueOf((source.toString())));});
		toMap.put(Time.class,(source)->{return new Time(Long.valueOf((source.toString())));});
		toMap.put(Timestamp.class,(source)->{return new Timestamp(Long.valueOf((source.toString())));});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(source.toString().getBytes());});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(source.toString());});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Boolean() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Byte.class,(source)->{return Byte.valueOf((Boolean)source ? (byte)1 : (byte)0);});
		toMap.put(Short.class,(source)->{return Short.valueOf((Boolean)source ? (short)1 : (short)0);});
		toMap.put(Integer.class,(source)->{return Integer.valueOf((Boolean)source ? 1 : 0);});
		toMap.put(Long.class,(source)->{return Long.valueOf((Boolean)source ? 1L : 0L);});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf((Boolean)source ? 1L : 0L);});
		toMap.put(byte[].class,(source)->{return new byte[]{(Boolean)source ? (byte)1 : (byte)0};});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(new byte[]{(Boolean)source ? (byte)1 : (byte)0});});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals((Boolean)source ? "true" : "false");});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(new byte[]{(Boolean)source ? (byte)1 : (byte)0});});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob((Boolean)source ? "true" : "false");});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob((Boolean)source ? "true" : "false");});
		toMap.put(String.class,(source)->{return (Boolean)source ? "true" : "false";});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("BIT"),source instanceof Boolean ? source : Boolean.valueOf((boolean)source));
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Byte() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{return ((Byte)source).byteValue() != 0;});
		toMap.put(Short.class,(source)->{return Short.valueOf(((Byte)source).shortValue());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf(((Byte)source).intValue());});
		toMap.put(Long.class,(source)->{return Long.valueOf(((Byte)source).longValue());});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf(((Byte)source).longValue());});
		toMap.put(byte[].class,(source)->{return new byte[]{((Byte)source).byteValue()};});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(new byte[]{((Byte)source).byteValue()});});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(String.valueOf(((Byte)source).byteValue()));});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(new byte[]{((Byte)source).byteValue()});});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Byte)source).byteValue()));});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(String.valueOf(((Byte)source).byteValue()));});
		toMap.put(String.class,(source)->{return String.valueOf(((Byte)source).byteValue());});
		toMap.put(Date.class,(source)->{return new Date(((Byte)source).longValue());});
		toMap.put(Time.class,(source)->{return new Time(((Byte)source).longValue());});
		toMap.put(Timestamp.class,(source)->{return new Timestamp(((Byte)source).longValue());});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("TINYINT"),((Byte)source).byteValue());
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Short() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{return ((Short)source).shortValue() != 0;});
		toMap.put(Byte.class,(source)->{return Byte.valueOf(((Short)source).byteValue());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf(((Short)source).intValue());});
		toMap.put(Long.class,(source)->{return Long.valueOf(((Short)source).longValue());});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf(((Short)source).longValue());});
		toMap.put(byte[].class,(source)->{return new byte[]{(byte)(((Short)source).shortValue() >> 8), (byte)(((Short)source).shortValue() >> 0)};});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(new byte[]{(byte)(((Short)source).shortValue() >> 8), (byte)(((Short)source).shortValue() >> 0)});});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(String.valueOf(((Short)source).shortValue()));});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(new byte[]{(byte)(((Short)source).shortValue() >> 8), (byte)(((Short)source).shortValue() >> 0)});});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Short)source).shortValue()));});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(String.valueOf(((Short)source).shortValue()));});
		toMap.put(String.class,(source)->{return String.valueOf(((Short)source).shortValue());});
		toMap.put(Date.class,(source)->{return new Date(((Short)source).longValue());});
		toMap.put(Time.class,(source)->{return new Time(((Short)source).longValue());});
		toMap.put(Timestamp.class,(source)->{return new Timestamp(((Short)source).longValue());});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("SMALLINT"),((Short)source).shortValue());
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Integer() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{return ((Integer)source).intValue() != 0;});
		toMap.put(Byte.class,(source)->{return Byte.valueOf(((Integer)source).byteValue());});
		toMap.put(Short.class,(source)->{return Short.valueOf(((Integer)source).shortValue());});
		toMap.put(Long.class,(source)->{return Long.valueOf(((Integer)source).longValue());});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf(((Integer)source).longValue());});
		toMap.put(byte[].class,(source)->{return new byte[]{(byte)(((Integer)source).intValue() >> 24), (byte)(((Integer)source).intValue() >> 16), (byte)(((Integer)source).intValue() >> 8), (byte)(((Integer)source).intValue() >> 0)};});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(new byte[]{(byte)(((Integer)source).intValue() >> 24), (byte)(((Integer)source).intValue() >> 16), (byte)(((Integer)source).intValue() >> 8), (byte)(((Integer)source).intValue() >> 0)});});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(String.valueOf(((Integer)source).intValue()));});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(new byte[]{(byte)(((Integer)source).intValue() >> 24), (byte)(((Integer)source).intValue() >> 16), (byte)(((Integer)source).intValue() >> 8), (byte)(((Integer)source).intValue() >> 0)});});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Integer)source).intValue()));});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(String.valueOf(((Integer)source).intValue()));});
		toMap.put(String.class,(source)->{return String.valueOf(((Integer)source).intValue());});
		toMap.put(Date.class,(source)->{return new Date(((Integer)source).longValue());});
		toMap.put(Time.class,(source)->{return new Time(((Integer)source).longValue());});
		toMap.put(Timestamp.class,(source)->{return new Timestamp(((Integer)source).longValue());});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("INTEGER"),((Integer)source).intValue());
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Long() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{return Boolean.valueOf(((Long)source).longValue() != 0L);});
		toMap.put(Byte.class,(source)->{return Byte.valueOf(((Long)source).byteValue());});
		toMap.put(Short.class,(source)->{return Short.valueOf(((Long)source).shortValue());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf(((Long)source).intValue());});
		toMap.put(Long.class,(source)->{return Long.valueOf(((Long)source).longValue());});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf((((Long)source).longValue()));});
		toMap.put(byte[].class,(source)->{
			final long	temp = ((Long)source).longValue();
			
			return new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)};
		});
		toMap.put(InputStream.class,(source)->{
			final long	temp = ((Long)source).longValue();
			
			return new ByteArrayInputStreamWithEquals(new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)});
		});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(String.valueOf(((Long)source).longValue()));});
		toMap.put(Blob.class,(source)->{
			final long	temp = ((Long)source).longValue();
			
			return new InMemoryLittleBlob(new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)});
		});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Long)source).longValue()));});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(String.valueOf(((Long)source).longValue()));});
		toMap.put(String.class,(source)->{return String.valueOf(((Long)source).longValue());});
		toMap.put(Date.class,(source)->{return new Date(((Long)source).longValue());});
		toMap.put(Time.class,(source)->{return new Time(((Long)source).longValue());});
		toMap.put(Timestamp.class,(source)->{return new Timestamp(((Long)source).longValue());});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("BIGINT"),((Long)source).longValue());
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Float() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Double.class,(source)->{return ((Float)source).doubleValue();});
		toMap.put(BigDecimal.class,(source)->{return new BigDecimal(source.toString());});
		toMap.put(Blob.class,(source)->{
			final int	temp = Float.floatToIntBits(((Float)source).floatValue());
			
			return new InMemoryLittleBlob(new byte[]{(byte)(temp >>> 24),(byte)(temp >>> 16),(byte)(temp >>> 8),(byte)(temp >>> 0)});
		});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Float)source).floatValue()));});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Float)source).floatValue()));});
		toMap.put(byte[].class,(source)->{
			final int	temp = Float.floatToIntBits(((Float)source).floatValue());
			
			return new byte[]{(byte)(temp >>> 24),(byte)(temp >>> 16),(byte)(temp >>> 8),(byte)(temp >>> 0)};
		});
		toMap.put(String.class,(source)->{return String.valueOf(((Float)source).floatValue());});
		toMap.put(InputStream.class,(source)->{
			final int	temp = Float.floatToIntBits(((Float)source).floatValue());
			
			return new ByteArrayInputStreamWithEquals(new byte[]{(byte)(temp >>> 24),(byte)(temp >>> 16),(byte)(temp >>> 8),(byte)(temp >>> 0)});
		});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(String.valueOf(((Float)source).floatValue()));});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("FLOAT"),((Float)source).floatValue());
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}
	
	private static Map<Class<?>,ConversionCall> prepareConversion4Double() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Float.class,(source)->{return ((Double)source).floatValue();});
		toMap.put(BigDecimal.class,(source)->{return new BigDecimal(source.toString());});
		toMap.put(Blob.class,(source)->{
			final long	temp = Double.doubleToLongBits(((Double)source).doubleValue());
			
			return new InMemoryLittleBlob(new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)});
		});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Double)source).doubleValue()));});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleClob(String.valueOf(((Double)source).doubleValue()));});
		toMap.put(byte[].class,(source)->{
			final long 	temp = Double.doubleToLongBits(((Double)source).doubleValue());
			
			return new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)};
		});
		toMap.put(String.class,(source)->{return String.valueOf(((Double)source).doubleValue());});
		toMap.put(InputStream.class,(source)->{
			final long	temp = Double.doubleToLongBits(((Double)source).doubleValue());
			
			return new ByteArrayInputStreamWithEquals(new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)});
		});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(String.valueOf(((Double)source).doubleValue()));});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("DOUBLE"),((Double)source).doubleValue());
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	
	
	private static Map<Class<?>,ConversionCall> prepareConversion4ByteArray() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{return ((byte[])source).length > 0 && ((byte[])source)[0] != 0;});
		toMap.put(Byte.class,(source)->{return ((byte[])source).length > 0 ? ((byte[])source)[0] : 0;});
		toMap.put(Short.class,(source)->{return ((byte[])source).length > 1 ? (short)(((((byte[])source)[0] << 8) & 0xFF00) | ((((byte[])source)[1] << 0) & 0xFF)) : 0;});
		toMap.put(Integer.class,(source)->{return ((byte[])source).length > 3 ? (int)(((((byte[])source)[0] << 24) & 0xFF000000) | ((((byte[])source)[1] << 16) & 0xFF0000) | ((((byte[])source)[2] << 8) & 0xFF00) | ((((byte[])source)[3] << 0) & 0xFF)) : 0;});
		toMap.put(Long.class,(source)->{
			final byte[]	temp = (byte[])source;
			
			if (temp.length > 7) {
				final long	result = (((((long)temp[0]) << 56) & 0xFF00000000000000L) | ((((long)temp[1]) << 48) & 0xFF000000000000L) | ((((long)temp[2]) << 40) & 0xFF0000000000L) | ((((long)temp[3]) << 32) & 0xFF00000000L) | ((((long)temp[4]) << 24) & 0xFF000000) | ((((long)temp[5]) << 16) & 0xFF0000) | ((((long)temp[6]) << 8) & 0xFF00) | ((((long)temp[7]) << 0) & 0xFF));
				
				return result;
			}
			else {
				return 0;
			}
		});
		toMap.put(Float.class,(source)->{return ((byte[])source).length > 3 ? Float.intBitsToFloat((int)(((((byte[])source)[0] << 24) & 0xFF000000) | ((((byte[])source)[1] << 16) & 0xFF0000) | ((((byte[])source)[2] << 8) & 0xFF00) | ((((byte[])source)[3] << 0) & 0xFF))) : 0;});
		toMap.put(Double.class,(source)->{
			final byte[]	temp = (byte[])source;
			
			if (temp.length > 7) {
				final long	result = (((((long)temp[0]) << 56) & 0xFF00000000000000L) | ((((long)temp[1]) << 48) & 0xFF000000000000L) | ((((long)temp[2]) << 40) & 0xFF0000000000L) | ((((long)temp[3]) << 32) & 0xFF00000000L) | ((((long)temp[4]) << 24) & 0xFF000000) | ((((long)temp[5]) << 16) & 0xFF0000) | ((((long)temp[6]) << 8) & 0xFF00) | ((((long)temp[7]) << 0) & 0xFF));
				
				return Double.longBitsToDouble(result);
			}
			else {
				return 0;
			}
		});
		toMap.put(BigInteger.class,(source)->{return new BigInteger(((byte[])source));});
		toMap.put(BigDecimal.class,(source)->{return new BigDecimal(new String(((byte[])source)));});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(((byte[])source));});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(new String(((byte[])source)));});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(new String(((byte[])source)));});
		toMap.put(String.class,(source)->{return new String(((byte[])source));});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(((byte[])source));});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(new String(((byte[])source)));});
		toMap.put(Date.class,(source)->{
			final byte[]	temp = (byte[])source;
			
			if (temp.length > 7) {
				final long	result = (((((long)temp[0]) << 56) & 0xFF00000000000000L) | ((((long)temp[1]) << 48) & 0xFF000000000000L) | ((((long)temp[2]) << 40) & 0xFF0000000000L) | ((((long)temp[3]) << 32) & 0xFF00000000L) | ((((long)temp[4]) << 24) & 0xFF000000) | ((((long)temp[5]) << 16) & 0xFF0000) | ((((long)temp[6]) << 8) & 0xFF00) | ((((long)temp[7]) << 0) & 0xFF));
				
				return new Date(result);
			}
			else {
				return 0;
			}
		});
		toMap.put(Time.class,(source)->{
			final byte[]	temp = (byte[])source;
			
			if (temp.length > 7) {
				final long	result = (((((long)temp[0]) << 56) & 0xFF00000000000000L) | ((((long)temp[1]) << 48) & 0xFF000000000000L) | ((((long)temp[2]) << 40) & 0xFF0000000000L) | ((((long)temp[3]) << 32) & 0xFF00000000L) | ((((long)temp[4]) << 24) & 0xFF000000) | ((((long)temp[5]) << 16) & 0xFF0000) | ((((long)temp[6]) << 8) & 0xFF00) | ((((long)temp[7]) << 0) & 0xFF));
				
				return new Time(result);
			}
			else {
				return 0;
			}
		});
		toMap.put(Timestamp.class,(source)->{
			final byte[]	temp = (byte[])source;
			
			if (temp.length > 7) {
				final long	result = (((((long)temp[0]) << 56) & 0xFF00000000000000L) | ((((long)temp[1]) << 48) & 0xFF000000000000L) | ((((long)temp[2]) << 40) & 0xFF0000000000L) | ((((long)temp[3]) << 32) & 0xFF00000000L) | ((((long)temp[4]) << 24) & 0xFF000000) | ((((long)temp[5]) << 16) & 0xFF0000) | ((((long)temp[6]) << 8) & 0xFF00) | ((((long)temp[7]) << 0) & 0xFF));
				
				return new Timestamp(result);
			}
			else {
				return 0;
			}
		});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("VARBINARY"),(byte[])source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Blob() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{
			try {return ((Blob)source).getBytes(1,1)[0] != 0;
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Byte.class,(source)->{
			try{if (((Blob)source).length() < 1) {
					throw new ContentException("Blob content length ["+((Blob)source).length()+"] is too small to convert it to Byte"); 
				}
				else {
					return ((Blob)source).getBytes(1,1)[0];
				}
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Short.class,(source)->{
			try{if (((Blob)source).length() < 2) {
					throw new ContentException("Blob content length ["+((Blob)source).length()+"] is too small to convert it to Short"); 
				}
				else {
					final byte[]	content = (byte[])((Blob)source).getBytes(1,2);
					
					return (short)(((content[0] << 8) & 0xFF00) | ((content[1] << 0) & 0xFF));
				}
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Integer.class,(source)->{
			try{if (((Blob)source).length() < 4) {
					throw new ContentException("Blob content length ["+((Blob)source).length()+"] is too small to convert it to Integer"); 
				}
				else {final byte[]	content = (byte[])((Blob)source).getBytes(1,4);
				
					return ((content[0] << 24) & 0xFF000000) | ((content[1] << 16) & 0xFF0000) | ((content[2] << 8) & 0xFF00) | ((content[3] << 0) & 0xFF);
				}				
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Long.class,(source)->{
			try{if (((Blob)source).length() < 8) {
					throw new ContentException("Blob content length ["+((Blob)source).length()+"] is too small to convert it to Long"); 
				}
				else {
					final byte[]	temp = (byte[])((Blob)source).getBytes(1,8);
					final long		result = (((((long)temp[0]) << 56) & 0xFF00000000000000L) | ((((long)temp[1]) << 48) & 0xFF000000000000L) | ((((long)temp[2]) << 40) & 0xFF0000000000L) | ((((long)temp[3]) << 32) & 0xFF00000000L) | ((((long)temp[4]) << 24) & 0xFF000000) | ((((long)temp[5]) << 16) & 0xFF0000) | ((((long)temp[6]) << 8) & 0xFF00) | ((((long)temp[7]) << 0) & 0xFF));
				
					return result;
				}
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Float.class,(source)->{
			try{if (((Blob)source).length() < 2) {
					throw new ContentException("Blob content length ["+((Blob)source).length()+"] is too small to convert it to Float"); 
				}
				else {final byte[]	content = (byte[])((Blob)source).getBytes(1,4);
					final int		temp = ((content[0] << 24) & 0xFF000000) | ((content[1] << 16) & 0xFF0000) | ((content[2] << 8) & 0xFF00) | ((content[3] << 0) & 0xFF);
				
					return Float.intBitsToFloat(temp);
				}
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Double.class,(source)->{
			try{if (((Blob)source).length() < 8) {
					throw new ContentException("Blob content length ["+((Blob)source).length()+"] is too small to convert it to Short"); 
				}
				else {
					final byte[]	temp = (byte[])((Blob)source).getBytes(1,8);
					final long		result = (((((long)temp[0]) << 56) & 0xFF00000000000000L) | ((((long)temp[1]) << 48) & 0xFF000000000000L) | ((((long)temp[2]) << 40) & 0xFF0000000000L) | ((((long)temp[3]) << 32) & 0xFF00000000L) | ((((long)temp[4]) << 24) & 0xFF000000) | ((((long)temp[5]) << 16) & 0xFF0000) | ((((long)temp[6]) << 8) & 0xFF00) | ((((long)temp[7]) << 0) & 0xFF));
				
					return Double.longBitsToDouble(result);
				}
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigInteger.class,(source)->{
			try {return new BigInteger(new String(((Blob)source).getBytes(1,(int)((Blob)source).length())));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigDecimal.class,(source)->{
			try {return new BigDecimal(new String(((Blob)source).getBytes(1,(int)((Blob)source).length())));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(byte[].class,(source)->{
			try {return ((Blob)source).getBytes(1,(int)((Blob)source).length());
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Clob.class,(source)->{
			try {return new InMemoryLittleClob(new String(((Blob)source).getBytes(1,(int)((Blob)source).length())));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(NClob.class,(source)->{
			try {return new InMemoryLittleNClob(new String(((Blob)source).getBytes(1,(int)((Blob)source).length())));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(String.class,(source)->{
			try {return new String(((Blob)source).getBytes(1,(int)((Blob)source).length()));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(InputStream.class,(source)->{
			try {return new ByteArrayInputStreamWithEquals(((Blob)source).getBytes(1,(int)((Blob)source).length()));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Reader.class,(source)->{
			try {return new StringReaderWithEquals(new String(((Blob)source).getBytes(1,(int)((Blob)source).length())));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("BLOB"),(Blob)source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Clob() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{
			try {return "true".equalsIgnoreCase(((Clob)source).getSubString(1,4));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Byte.class,(source)->{
			try {return Byte.valueOf(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Short.class,(source)->{
			try {return Short.valueOf(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Integer.class,(source)->{
			try {return Integer.valueOf(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Long.class,(source)->{
			try {return Long.valueOf(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Float.class,(source)->{
			try {return Float.valueOf(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Double.class,(source)->{
			try {return Double.valueOf(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigInteger.class,(source)->{
			try {return new BigInteger(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigDecimal.class,(source)->{
			try {return new BigDecimal(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(byte[].class,(source)->{
			try {return ((Clob)source).getSubString(1,(int)((Clob)source).length()).getBytes();
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Blob.class,(source)->{
			try {return new InMemoryLittleBlob(((Clob)source).getSubString(1,(int)((Clob)source).length()).getBytes());
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(NClob.class,(source)->{
			try {return new InMemoryLittleNClob(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(String.class,(source)->{
			try {return ((Clob)source).getSubString(1,(int)((Clob)source).length());
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(InputStream.class,(source)->{
			try {return new ByteArrayInputStreamWithEquals(((Clob)source).getSubString(1,(int)((Clob)source).length()).getBytes());
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Reader.class,(source)->{
			try {return new StringReaderWithEquals(((Clob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("CLOB"),(Clob)source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4NClob() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{
			try {return "true".equalsIgnoreCase(((NClob)source).getSubString(1,4));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Byte.class,(source)->{
			try {return Byte.valueOf(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Short.class,(source)->{
			try {return Short.valueOf(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Integer.class,(source)->{
			try {return Integer.valueOf(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Long.class,(source)->{
			try {return Long.valueOf(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Float.class,(source)->{
			try {return Float.valueOf(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Double.class,(source)->{
			try {return Double.valueOf(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigInteger.class,(source)->{
			try {return new BigInteger(((NClob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigDecimal.class,(source)->{
			try {return new BigDecimal(((NClob)source).getSubString(1,(int)((Clob)source).length()));
			} catch (NumberFormatException | SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(byte[].class,(source)->{
			try {return ((NClob)source).getSubString(1,(int)((NClob)source).length()).getBytes();
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Blob.class,(source)->{
			try {return new InMemoryLittleBlob(((NClob)source).getSubString(1,(int)((NClob)source).length()).getBytes());
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Clob.class,(source)->{
			try {return new InMemoryLittleClob(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(String.class,(source)->{
			try {return ((NClob)source).getSubString(1,(int)((NClob)source).length());
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(InputStream.class,(source)->{
			try {return new ByteArrayInputStreamWithEquals(((NClob)source).getSubString(1,(int)((NClob)source).length()).getBytes());
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Reader.class,(source)->{
			try {return new StringReaderWithEquals(((NClob)source).getSubString(1,(int)((NClob)source).length()));
			} catch (SQLException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("NCLOB"),(NClob)source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}
	
	private static Map<Class<?>,ConversionCall> prepareConversion4BigInteger() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{return Boolean.valueOf(((BigInteger)source).compareTo(BigInteger.ZERO) != 0);});
		toMap.put(Byte.class,(source)->{return Byte.valueOf(source.toString());});
		toMap.put(Short.class,(source)->{return Short.valueOf(source.toString());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf(source.toString());});
		toMap.put(Long.class,(source)->{return Long.valueOf(source.toString());});
		toMap.put(BigDecimal.class,(source)->{return new BigDecimal(source.toString());});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(source.toString().getBytes());});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(source.toString());});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(source.toString());});
		toMap.put(byte[].class,(source)->{return ((BigInteger)source).toByteArray();});
		toMap.put(String.class,(source)->{return ((BigInteger)source).toString();});
		toMap.put(Date.class,(source)->{return new Date(((BigInteger)source).longValue());});
		toMap.put(Time.class,(source)->{return new Time(((BigInteger)source).longValue());});
		toMap.put(Timestamp.class,(source)->{return new Timestamp(((BigInteger)source).longValue());});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(((BigInteger)source).toByteArray());});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(source.toString());});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4BigDecimal() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Float.class,(source)->{return Float.valueOf(source.toString());});
		toMap.put(Double.class,(source)->{return Double.valueOf(source.toString());});
		toMap.put(BigInteger.class,(source)->{return ((BigDecimal)source).toBigInteger();});
		toMap.put(Blob.class,(source)->{return new InMemoryLittleBlob(source.toString().getBytes());});
		toMap.put(Clob.class,(source)->{return new InMemoryLittleClob(source.toString());});
		toMap.put(NClob.class,(source)->{return new InMemoryLittleNClob(source.toString());});
		toMap.put(byte[].class,(source)->{return ((BigDecimal)source).toString().getBytes();});
		toMap.put(String.class,(source)->{return ((BigDecimal)source).toString();});
		toMap.put(InputStream.class,(source)->{return new ByteArrayInputStreamWithEquals(((BigDecimal)source).toString().getBytes());});
		toMap.put(Reader.class,(source)->{return new StringReaderWithEquals(source.toString());});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("DECIMAL"),(BigDecimal)source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}
		
	
	private static Map<Class<?>,ConversionCall> prepareConversion4Date() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Byte.class,(source)->{return Byte.valueOf((byte)((Date)source).getTime());});
		toMap.put(Short.class,(source)->{return Short.valueOf((short)((Date)source).getTime());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf((int)((Date)source).getTime());});
		toMap.put(Long.class,(source)->{return Long.valueOf(((Date)source).getTime());});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf(((Date)source).getTime());});
		toMap.put(byte[].class,(source)->{
			final long	temp = ((Date)source).getTime();
			
			return new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)};
		});
		toMap.put(String.class,(source)->{return Long.valueOf((((Date)source).getTime()));});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("DATE"),(Date)source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}
	
	private static Map<Class<?>,ConversionCall> prepareConversion4Time() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Byte.class,(source)->{return Byte.valueOf((byte)((Time)source).getTime());});
		toMap.put(Short.class,(source)->{return Short.valueOf((short)((Time)source).getTime());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf((int)((Time)source).getTime());});
		toMap.put(Long.class,(source)->{return Long.valueOf(((Time)source).getTime());});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf(((Time)source).getTime());});
		toMap.put(byte[].class,(source)->{
			final long	temp = ((Time)source).getTime();
			
			return new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)};
		});
		toMap.put(String.class,(source)->{return Long.valueOf((((Time)source).getTime()));});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("TIME_WITH_TIMEZONE"),(Time)source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}
	
	private static Map<Class<?>,ConversionCall> prepareConversion4Timestamp() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Byte.class,(source)->{return Byte.valueOf((byte)((Timestamp)source).getTime());});
		toMap.put(Short.class,(source)->{return Short.valueOf((short)((Timestamp)source).getTime());});
		toMap.put(Integer.class,(source)->{return Integer.valueOf((int)((Timestamp)source).getTime());});
		toMap.put(Long.class,(source)->{return Long.valueOf(((Timestamp)source).getTime());});
		toMap.put(BigInteger.class,(source)->{return BigInteger.valueOf(((Timestamp)source).getTime());});
		toMap.put(byte[].class,(source)->{
			final long	temp = ((Timestamp)source).getTime();
			
			return new byte[]{(byte)(temp >> 56),(byte)(temp >> 48),(byte)(temp >> 40),(byte)(temp >> 32),(byte)(temp >> 24),(byte)(temp >> 16),(byte)(temp >> 8),(byte)(temp >> 0)};
		});
		toMap.put(String.class,(source)->{return Long.valueOf((((Timestamp)source).getTime()));});
		toMap.put(Array.class,(source)->{
			try {return new InMemoryLitteArray(InternalUtils.typeIdByTypeName("TIMESTAMP"),(Timestamp)source);
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4InputStream() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{
			try {return Boolean.valueOf(((InputStream)source).read() > 0);
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Byte.class,(source)->{
			try {final int	value = ((InputStream)source).read();
				 
				 if (value == -1) {
					 throw new EOFException();
				 }
				 else {
					 return Byte.valueOf((byte)value);
				 }
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Short.class,(source)->{
			try {final int	value1 = ((InputStream)source).read(), value2 = ((InputStream)source).read();
				 
				 if (value1 == -1 || value2 == -1) {
					 throw new EOFException();
				 }
				 else {
					 return Short.valueOf((short)(((((byte)value1) << 8) & 0xFF00) | ((((byte)value2) << 0) & 0xFF)));
				 }
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Integer.class,(source)->{
			try {final int	value1 = ((InputStream)source).read(), value2 = ((InputStream)source).read(), value3 = ((InputStream)source).read(), value4 = ((InputStream)source).read();
				 
				 if (value1 == -1 || value2 == -1 || value3 == -1 || value4 == -1) {
					 throw new EOFException();
				 }
				 else {
					 return Integer.valueOf((((((byte)value1) << 24) & 0xFF000000) | ((((byte)value2) << 16) & 0xFF0000) | ((((byte)value3) << 8) & 0xFF00)| ((((byte)value4) << 0) & 0xFF)));
				 }
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Long.class,(source)->{
			try {final long	value1 = ((InputStream)source).read(), value2 = ((InputStream)source).read(), value3 = ((InputStream)source).read(), value4 = ((InputStream)source).read();
				 final long	value5 = ((InputStream)source).read(), value6 = ((InputStream)source).read(), value7 = ((InputStream)source).read(), value8 = ((InputStream)source).read();
				 
				 if (value1 == -1 || value2 == -1 || value3 == -1 || value4 == -1 || value5 == -1 || value6 == -1 || value7 == -1 || value8 == -1) {
					 throw new EOFException();
				 }
				 else {
					 return Long.valueOf((((value1 << 56) & 0xFF00000000000000L) | ((value2 << 48) & 0xFF000000000000L) | ((value3 << 40) & 0xFF0000000000L) | ((value4 << 32) & 0xFF00000000L) | ((value5 << 24) & 0xFF000000L) | ((value6 << 16) & 0xFF0000L) | ((value7 << 8) & 0xFF00L) | ((value8 << 0) & 0xFFL)));
				 }
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Float.class,(source)->{
			try {final int	value1 = ((InputStream)source).read(), value2 = ((InputStream)source).read(), value3 = ((InputStream)source).read(), value4 = ((InputStream)source).read();
				 
				 if (value1 == -1 || value2 == -1 || value3 == -1 || value4 == -1) {
					 throw new EOFException();
				 }
				 else {
					 return Float.intBitsToFloat((((((byte)value1) << 24) & 0xFF000000) | ((((byte)value2) << 16) & 0xFF0000) | ((((byte)value3) << 8) & 0xFF00)| ((((byte)value4) << 0) & 0xFF)));
				 }
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Double.class,(source)->{
			try {final long	value1 = ((InputStream)source).read(), value2 = ((InputStream)source).read(), value3 = ((InputStream)source).read(), value4 = ((InputStream)source).read();
				 final long	value5 = ((InputStream)source).read(), value6 = ((InputStream)source).read(), value7 = ((InputStream)source).read(), value8 = ((InputStream)source).read();
				 
				 if (value1 == -1 || value2 == -1 || value3 == -1 || value4 == -1 || value5 == -1 || value6 == -1 || value7 == -1 || value8 == -1) {
					 throw new EOFException();
				 }
				 else {
					 final long		result = (((value1 << 56) & 0xFF00000000000000L) | ((value2 << 48) & 0xFF000000000000L) | ((value3 << 40) & 0xFF0000000000L) | ((value4 << 32) & 0xFF00000000L) | ((value5 << 24) & 0xFF000000) | ((value6 << 16) & 0xFF0000) | ((value7 << 8) & 0xFF00) | ((value8 << 0) & 0xFF));
					 
					 return Double.longBitsToDouble(result);
				 }
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigInteger.class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return new BigInteger(gba.append((InputStream)source).extract());
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigDecimal.class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return new BigDecimal(new String(gba.append((InputStream)source).extract()));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(byte[].class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return gba.append((InputStream)source).extract();
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Blob.class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return new InMemoryLittleBlob(gba.append((InputStream)source).extract());
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Clob.class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return new InMemoryLittleClob(new String(gba.append((InputStream)source).extract()));
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(NClob.class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return new InMemoryLittleNClob(new String(gba.append((InputStream)source).extract()));
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(String.class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return new String(gba.append((InputStream)source).extract());
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Reader.class,(source)->{
			try {final GrowableByteArray	gba = new GrowableByteArray(true);
				 
				 return new StringReaderWithEquals(new String(gba.append((InputStream)source).extract()));
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Reader() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{
			try {return Boolean.valueOf(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Byte.class,(source)->{
			try {return Byte.valueOf(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Short.class,(source)->{
			try {return Short.valueOf(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Integer.class,(source)->{
			try {return Integer.valueOf(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Long.class,(source)->{
			try {return Long.valueOf(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Float.class,(source)->{
			try {return Float.valueOf(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Double.class,(source)->{
			try {return Double.valueOf(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigInteger.class,(source)->{
			try {return new BigInteger(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(BigDecimal.class,(source)->{
			try {return new BigDecimal(Utils.fromResource(((Reader)source)));
			} catch (NumberFormatException | IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(byte[].class,(source)->{
			try {return Utils.fromResource(((Reader)source)).getBytes();
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Blob.class,(source)->{
			try {return new InMemoryLittleBlob(Utils.fromResource(((Reader)source)).getBytes());
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(Clob.class,(source)->{
			try {return new InMemoryLittleClob(Utils.fromResource(((Reader)source)));
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(NClob.class,(source)->{
			try {return new InMemoryLittleNClob(Utils.fromResource(((Reader)source)));
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(InputStream.class,(source)->{
			try {return new ByteArrayInputStreamWithEquals(Utils.fromResource(((Reader)source)).getBytes());
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		toMap.put(String.class,(source)->{
			try {return Utils.fromResource(((Reader)source));
			} catch (IOException e) {
				throw new ContentException(e); 
			}
		});
		
		return toMap;
	}

	private static Map<Class<?>,ConversionCall> prepareConversion4Array() {
		final Map<Class<?>,ConversionCall>	toMap = new HashMap<>();
		
		toMap.put(Boolean.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof boolean[]) {
					return ((boolean[])content)[0];
				}
				else if (content instanceof Boolean[]) {
					return ((Boolean[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Boolean, but ["+content.getClass()+"] at position 1");
				}
				else {
					return false;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Byte.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof byte[]) {
					return ((byte[])content)[0];
				}
				else if (content instanceof Byte[]) {
					return ((Byte[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Byte, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Short.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof short[]) {
					return ((short[])content)[0];
				}
				else if (content instanceof Short[]) {
					return ((Short[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Short, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Integer.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof int[]) {
					return ((int[])content)[0];
				}
				else if (content instanceof Integer[]) {
					return ((Integer[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Integer, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Long.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof long[]) {
					return ((long[])content)[0];
				}
				else if (content instanceof Long[]) {
					return ((Long[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Long, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Float.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof float[]) {
					return ((float[])content)[0];
				}
				else if (content instanceof Float[]) {
					return ((Float[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Float, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Double.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof double[]) {
					return ((double[])content)[0];
				}
				else if (content instanceof Double[]) {
					return ((Double[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Double, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(BigInteger.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof BigInteger[]) {
					return ((BigInteger[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not BigInteger, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(BigDecimal.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof BigDecimal[]) {
					return ((BigDecimal[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not BigDecimal, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(byte[].class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof byte[][]) {
					return ((byte[][])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not byte[], but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Blob.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof Blob[]) {
					return ((Blob[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Blob, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Clob.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof Clob[]) {
					return ((Clob[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Clob, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(NClob.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof NClob[]) {
					return ((NClob[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not NClob, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Date.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof Date[]) {
					return ((Date[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Date, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Time.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof Time[]) {
					return ((Time[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Time, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		toMap.put(Timestamp.class,(source)->{
			try {final Object 	content = ((Array)source).getArray(1,1);
			
				if (content instanceof Timestamp[]) {
					return ((Timestamp[])content)[0];
				}
				else if (content != null) {
					throw new ContentException("Array content is not Timestamp, but ["+content.getClass()+"] at position 1");
				}
				else {
					return 0;
				}
			} catch (SQLException e) {
				throw new ContentException(e);
			}
		});
		
		return toMap;
	}
	
	private static class JDBCTypeDescriptor {
		final int		type;
		final String	typeName;
		
		JDBCTypeDescriptor(int type, String typeName) {
			this.type = type;
			this.typeName = typeName;
		}

		private int getType() {
			return type;
		}

		private String getTypeName() {
			return typeName;
		}

		@Override
		public String toString() {
			return "JDBCTypeDescriptor [type=" + type + ", typeName=" + typeName + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + type;
			result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			JDBCTypeDescriptor other = (JDBCTypeDescriptor) obj;
			if (type != other.type) return false;
			if (typeName == null) {
				if (other.typeName != null) return false;
			} else if (!typeName.equals(other.typeName)) return false;
			return true;
		}
	}

	private static class ConversionDescriptor {
		final Class<?>						sourceClass;
		final Map<Class<?>,ConversionCall>	target;
		
		public ConversionDescriptor(Class<?> sourceClass, Map<Class<?>, ConversionCall> target) {
			super();
			this.sourceClass = sourceClass;
			this.target = target;
		}

		@SuppressWarnings("unused")
		public Class<?> getSourceClass() {
			return sourceClass;
		}

		@SuppressWarnings("unused")
		public Map<Class<?>, ConversionCall> getTarget() {
			return target;
		}

		@Override
		public String toString() {
			return "ConversionDescriptor [sourceClass=" + sourceClass + ", target=" + target + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sourceClass == null) ? 0 : sourceClass.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ConversionDescriptor other = (ConversionDescriptor) obj;
			if (sourceClass == null) {
				if (other.sourceClass != null) return false;
			} else if (!sourceClass.equals(other.sourceClass)) return false;
			if (target == null) {
				if (other.target != null) return false;
			} else if (!target.equals(other.target)) return false;
			return true;
		}
	}

	public static class ByteArrayInputStreamWithEquals extends ByteArrayInputStream {
		public ByteArrayInputStreamWithEquals(final byte[] buf) {
			super(buf);
		}

		public ByteArrayInputStreamWithEquals(final byte[] buf, final int from, final int len) {
			super(buf,from,len);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			
			result = prime * result + hashCode(buf,mark,count);
			result = prime * result + count;
			result = prime * result + mark;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ByteArrayInputStreamWithEquals other = (ByteArrayInputStreamWithEquals) obj;
			if (!Arrays.equals(buf, other.buf)) return false;
			if (count != other.count) return false;
			if (mark != other.mark) return false;
			return true;
		}
		
	    public static int hashCode(byte a[], int from, int len) {
	        if (a == null) {
	        	return 0;
	        }
	        else {
		        int result = 1;
		        
		        for (int index = from, maxIndex = Math.min(from+len,a.length); index < maxIndex; index++) {
		            result = 31 * result + a[index];
		        }
		        return result;
	        }
	    }
	}

	public static class StringReaderWithEquals extends StringReader {
	    private String str;

		public StringReaderWithEquals(String s) {
			super(s);
			str = s;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((str == null) ? 0 : str.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			StringReaderWithEquals other = (StringReaderWithEquals) obj;
			if (str == null) {
				if (other.str != null) return false;
			} else if (!str.equals(other.str)) return false;
			return true;
		}
	}
}
