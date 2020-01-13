package chav1961.purelib.json;



import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.ReusableInstances;
import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableBooleanArray;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.growablearrays.GrowableDoubleArray;
import chav1961.purelib.basic.growablearrays.GrowableFloatArray;
import chav1961.purelib.basic.growablearrays.GrowableIntArray;
import chav1961.purelib.basic.growablearrays.GrowableLongArray;
import chav1961.purelib.basic.growablearrays.GrowableShortArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface.Walker;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.char2byte.asm.CompilerUtils;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

/**
 * <p>This class is a JSON serializer/deserializer for different classes. It supports both primitive and referenced Java classes. Sources and targets
 * for JSON serialization can be:</p>
 * <ul>
 * <li>character arrays</li>
 * <li>{@linkplain CharacterSource} and {@linkplain CharacterTarget} implementations</li>
 * <li>{@linkplain JsonStaxParser} and {@linkplain JsonStaxPrinter} streams</li>
 * </ul>
 * <p>To speed up serialization/deserialization, class can generate byte code on-the-fly for requested classes (see {@linkplain GettersAndSettersFactory}). Reading/writing to char arrays is limited by it's 
 * current size. Reading/writing to other source/targets has no any limitations.</p>
 * <p>Rules for serialization/deserialization are:</p>
 * <ul>
 * <li>any primitive types and {@linkplain String} entities can be serialized</li>
 * <li>any arrays of previously described types can be serialized</li>
 * <li>any public instance fields of previously described types from the class instance can be serialized</li> 
 * <li>any non-public instance fields of previously described types from the class instance can be serialized if the class implements {@link Serializable} or {@link Externalizable} interface</li> 
 * </ul>
 *  
 * @see CharacterSource
 * @see CharacterTarget
 * @see JsonStaxParser
 * @see JsonStaxPrinter
 * @see GettersAndSettersFactory
 * @see chav1961.purelib.streams
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @param <T> T - class to serialize.
 */
public abstract class JsonSerializer<T> {
	private static final char[]			TRUE_VALUE = "true".toCharArray();
	private static final char[]			FALSE_VALUE = "false".toCharArray();
	
	private static final Map<Class<?>,JsonSerializer<?>>	STANDARD_WRAPPERS = new HashMap<>();
	private static final Map<Class<?>,JsonSerializer<?>>	PRIMITIVE_ARRAY_WRAPPERS = new HashMap<>();
	private static final Map<Class<? extends Enum<?>>,JsonSerializer<?>>	ENUM_WRAPPERS = new HashMap<>();

	private static final int			NOT_PRIMITIVE = 0;
	private static final int			PRIMITIVE_BOOLEAN = 1;
	private static final int			PRIMITIVE_BYTE = 2;
	private static final int			PRIMITIVE_CHAR = 3;
	private static final int			PRIMITIVE_DOUBLE = 4;
	private static final int			PRIMITIVE_FLOAT = 5;
	private static final int			PRIMITIVE_INT = 6;
	private static final int			PRIMITIVE_LONG = 7;
	private static final int			PRIMITIVE_SHORT = 8;
	
	private static final char			STRING_TERMINATOR = '\"';
	private static final char			ARRAY_STARTER = '[';
	private static final char			ARRAY_TERMINATOR = ']';
	private static final char			OBJECT_STARTER = '{';
	private static final char			OBJECT_TERMINATOR = '}';
	private static final char			LIST_SPLITTER = ',';
	private static final char			NAME_SPLITTER = ':';
	
	private static final char[]			FOR_NULL = "null".toCharArray();
	
	static {
		STANDARD_WRAPPERS.put(Boolean.class,new BooleanSerializer());
		STANDARD_WRAPPERS.put(Byte.class,new ByteSerializer());
		STANDARD_WRAPPERS.put(Character.class,new CharacterSerializer());
		STANDARD_WRAPPERS.put(Double.class,new DoubleSerializer());
		STANDARD_WRAPPERS.put(Float.class,new FloatSerializer());
		STANDARD_WRAPPERS.put(Integer.class,new IntegerSerializer());
		STANDARD_WRAPPERS.put(Long.class,new LongSerializer());
		STANDARD_WRAPPERS.put(Short.class,new ShortSerializer());
		STANDARD_WRAPPERS.put(String.class,new StringSerializer());
		STANDARD_WRAPPERS.put(boolean.class,new BooleanSerializer());
		STANDARD_WRAPPERS.put(byte.class,new ByteSerializer());
		STANDARD_WRAPPERS.put(char.class,new CharacterSerializer());
		STANDARD_WRAPPERS.put(double.class,new DoubleSerializer());
		STANDARD_WRAPPERS.put(float.class,new FloatSerializer());
		STANDARD_WRAPPERS.put(int.class,new IntegerSerializer());
		STANDARD_WRAPPERS.put(long.class,new LongSerializer());
		STANDARD_WRAPPERS.put(short.class,new ShortSerializer());
		
		PRIMITIVE_ARRAY_WRAPPERS.put(boolean.class,new BooleanArraySerializer());
		PRIMITIVE_ARRAY_WRAPPERS.put(byte.class,new ByteArraySerializer());
		PRIMITIVE_ARRAY_WRAPPERS.put(char.class,new CharArraySerializer());
		PRIMITIVE_ARRAY_WRAPPERS.put(double.class,new DoubleArraySerializer());
		PRIMITIVE_ARRAY_WRAPPERS.put(float.class,new FloatArraySerializer());
		PRIMITIVE_ARRAY_WRAPPERS.put(int.class,new IntArraySerializer());
		PRIMITIVE_ARRAY_WRAPPERS.put(long.class,new LongArraySerializer());
		PRIMITIVE_ARRAY_WRAPPERS.put(short.class,new ShortArraySerializer());
	}
	
	@SuppressWarnings("unchecked")
	public static <Type> JsonSerializer<Type> buildSerializer(final Class<Type> awaited) throws EnvironmentException {
		if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null");
		}
		else {
			if (awaited.isPrimitive()) {
				return (JsonSerializer<Type>) STANDARD_WRAPPERS.get(awaited);
			}
			else if (awaited.isEnum()) {
				return buildEnumSerialier((Class<Type>) awaited);
			}
			else if (awaited.isArray()) {
				return buildArraySerializer(awaited);
			}
			else if (STANDARD_WRAPPERS.containsKey(awaited)) {
				return (JsonSerializer<Type>) STANDARD_WRAPPERS.get(awaited);
			}
			else if (Serializable.class.isAssignableFrom(awaited) || Externalizable.class.isAssignableFrom(awaited)) {
				return buildTotalSerializer(awaited);
			}
			else {
				return buildPublicSerializer(awaited);
			}
		}
	}

	/**
	 * <p>Serialize class instance to writer</p> 
	 * @param instance instance to serialize
	 * @param writer writer to write serialized for to
	 * @throws PrintingException in any problems during writing
	 */
	public abstract void serialize(final T instance, final JsonStaxPrinter writer) throws PrintingException;

	/**
	 * <p>Serialize class instance to {@linkplain CharacterTarget}</p> 
	 * @param instance instance to serialize
	 * @param writer writer to write serialized for to
	 * @throws PrintingException in any problems during writing
	 */
	public abstract void serialize(final T instance, final CharacterTarget writer) throws PrintingException;

	/**
	 * <p>Serialize class instance to char array</p>
	 * @param instance instance to serialize
	 * @param content array to write serialized for to
	 * @param from start position inside the array
	 * @param reallyFilled true - really fill content, false - calculate size required only
	 * @return new start position in the array. Negative number means that char array length is too small to keep serialization, 
	 * and it's absolute is full array length to store serialization
	 */
	public abstract int serialize(final T instance, final char[] content, final int from, final boolean reallyFilled);

	/**
	 * <p>Deserialize class instance from reader</p>
	 * @param reader reader to deserialize instance from
	 * @return instance deserialized
	 * @throws ContentException on any instantiation problems
	 * @throws SyntaxException in any syntax errors in the JSON
	 */
	public abstract T deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException;
	
	/**
	 * <p>Deserialize class instance from {@linkplain CharacterSource}</p>
	 * @param reader reader to deserialize instance from
	 * @return instance deserialized
	 * @throws ContentException on any instantiation problems
	 * @throws SyntaxException in any syntax errors in the JSON
	 */
	public abstract T deserialize(final CharacterSource reader) throws ContentException, SyntaxException;
	
	/**
	 * <p>Deerialize class instance from char array</p>
	 * @param content array to deserialize instance from
	 * @param from start position inside the array
	 * @param result array to receive instance deserialized. Must contain at least one element
	 * @return new start position inside the array
	 * @throws SyntaxException in any syntax errors in the JSON
	 */
	public abstract int deserialize(final char[] content, final int from, final T[] result) throws SyntaxException;

	protected static int printCharArray(final char[] content, final int from, final char[] data, final boolean reallyFilled) {
		if (from + data.length >= content.length) {
			return -(from + data.length); 
		}
		else {
			System.arraycopy(data,0,content,from,data.length);
			return from + data.length; 
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <Type> JsonSerializer<Type> buildEnumSerialier(final Class<Type> awaited) throws EnvironmentException {
		synchronized(ENUM_WRAPPERS) {
			if (!ENUM_WRAPPERS.containsKey(awaited)) {
				ENUM_WRAPPERS.put((Class<? extends Enum<?>>) awaited,new EnumSerializer<Type>(awaited));
			}
			return (JsonSerializer<Type>) ENUM_WRAPPERS.get(awaited);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <Type> JsonSerializer<Type> buildArraySerializer(final Class<Type> awaited) throws EnvironmentException {
		if (awaited.getComponentType().isPrimitive()) {
			return (JsonSerializer<Type>) PRIMITIVE_ARRAY_WRAPPERS.get(awaited.getComponentType());
		}
		else {
			return new RefArraySerializer(awaited.getComponentType(),buildSerializer(awaited.getComponentType()));
		}
	}

	private static <Type> JsonSerializer<Type> buildPublicSerializer(final Class<Type> awaited) throws EnvironmentException {
		final Map<String,Field>	fields = new HashMap<>();
		
		collectFields(awaited,true,false,fields);
		if (fields.size() == 0) {
			throw new EnvironmentException("Class ["+awaited+"] has no any field to serialize/deserialize");
		}
		else {
			final SyntaxTreeInterface<GetterAndSetter>	classComponentsTree = new AndOrTree<>();
			
			for (Entry<String, Field> item : fields.entrySet()) {
				try{classComponentsTree.placeName(item.getKey(),new GetterAndSetter(item.getValue(),buildSerializer(item.getValue().getType())));
				} catch (IllegalAccessException e) {
					throw new EnvironmentException(e.getLocalizedMessage(),e);
				}
			}
			
			return new ObjectSerializer<>(awaited,classComponentsTree);
		}
	}
	
	private static <Type> JsonSerializer<Type> buildTotalSerializer(final Class<Type> awaited) throws EnvironmentException {
		final Map<String,Field>	fields = new HashMap<>();
		
		collectFields(awaited,false,true,fields);
		if (fields.size() == 0) {
			throw new EnvironmentException("Class ["+awaited+"] has no any field to serialize/deserialize");
		}
		else {
			final SyntaxTreeInterface<GetterAndSetter>	classComponentsTree = new AndOrTree<>();
			
			for (Entry<String, Field> item : fields.entrySet()) {
				try{classComponentsTree.placeName(item.getKey(),new GetterAndSetter(item.getValue(),buildSerializer(item.getValue().getType())));
				} catch (IllegalAccessException e) {
					throw new EnvironmentException(e.getLocalizedMessage(),e);
				}
			}
			
			return new ObjectSerializer<>(awaited,classComponentsTree);
		}
	}
	
	private static void collectFields(final Class<?> awaited, final boolean publicOnly, final boolean serializableOnly, final Map<String,Field> fields) {
		if (awaited != null) {
			if (!serializableOnly || (Serializable.class.isAssignableFrom(awaited) || Externalizable.class.isAssignableFrom(awaited))) {
				if (publicOnly) {
					for (Field item : awaited.getFields()) {
						if (!Modifier.isStatic(item.getModifiers()) && !Modifier.isTransient(item.getModifiers())) {
							fields.putIfAbsent(item.getName(),item);
						}
					}
				}
				else {
					for (Field item : awaited.getDeclaredFields()) {
						if (!Modifier.isStatic(item.getModifiers()) && !Modifier.isTransient(item.getModifiers())) {
							fields.putIfAbsent(item.getName(),item);
						}
					}
					collectFields(awaited.getSuperclass(),publicOnly,serializableOnly,fields);
				}
			}
		}
	}

	private static class BooleanSerializer extends JsonSerializer<Boolean> {
		public static final char[]	FOR_FALSE = "false".toCharArray();
		public static final char[]	FOR_TRUE = "true".toCharArray();
		
		@Override
		public void serialize(final Boolean instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				printBoolean(instance.booleanValue(),writer);
			}
		}

		@Override
		public int serialize(final Boolean instance, final char[] content, final int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				return printBoolean(instance.booleanValue(),content,from,reallyFilled);
			}
		}

		@Override
		public void serialize(final Boolean instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
						writer.nullValue();
				}
				else {
					printBoolean(instance.booleanValue(),writer);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}
		
		@Override
		public Boolean deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char	symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"boolean value is missing");
					}
				}
				if (symbol == 'n') {
					if (reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
						return null;
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"boolean value is missing");
					}
				}
				else if (symbol == 't') {
					if (reader.next() == 'r' && reader.next() == 'u' && reader.next() == 'e') {
						return Boolean.valueOf(true);
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"boolean value is missing");
					}
				}
				else if (symbol == 'f') {
					if (reader.next() == 'a' && reader.next() == 'l' && reader.next() == 's' && reader.next() == 'e') {
						return Boolean.valueOf(false);
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"boolean value is missing");
					}
				}
				else {
					throw new SyntaxException(reader.atRow(),reader.atColumn(),"boolean value is missing");
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final Boolean[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				if (CharUtils.compare(content,from,FOR_NULL)) {
					from += FOR_NULL.length;
					result[0] = null;
				}
				else if (CharUtils.compare(content,from,FOR_TRUE)) {
					from += FOR_TRUE.length;
					result[0] = Boolean.valueOf(true);
				}
				else if (CharUtils.compare(content,from,FOR_FALSE)) {
					from += FOR_FALSE.length;
					result[0] = Boolean.valueOf(false);
				}
				else {
					throw new SyntaxException(0,from,"Boolean value is missing");
				}
				return from;
			}
		}

		@Override
		public Boolean deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case BOOLEAN_VALUE 	:
						try{final boolean	result = reader.booleanValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal boolean value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or boolean value awaited");
				}
				
			}
		}
	}

	private static class ByteSerializer extends JsonSerializer<Byte> {
		private final ReusableInstances<char[]>		charArrayMemory = new ReusableInstances<>(()->{return new char[20];});
		private final ReusableInstances<int[]>		intArrayMemory = new ReusableInstances<>(()->{return new int[2];});
		
		@Override
		public void serialize(final Byte instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				
				try{printLong(instance.longValue(),content,writer);
				} finally {
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int serialize(final Byte instance, final char[] content, int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				return printLong(instance.longValue(),content,from,reallyFilled);
			}
		}

		@Override
		public void serialize(final Byte instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					printLong(instance.byteValue(),writer);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public Byte deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"byte value is missing");
					}
				}

				final char[]	content = charArrayMemory.allocate();
				final int[]		result = intArrayMemory.allocate();
				
				try{final int	contentLen = content.length, multiplier;				
					int			location;
					
					if (symbol == '-') {
						multiplier = -1;
						location = 0;
					}
					else if (symbol == '+') {
						multiplier = 1;
						location = 0;
					}
					else if (symbol >= '0' && symbol <= '9') {
						multiplier = 1;
						content[0] = symbol;
						location = 1;
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"byte value is missing");
					}
					
					while ((symbol = reader.next()) >= '0' && symbol <= '9') {
						if (location < contentLen) {
							content[location++] = symbol;
						}
					}
					if (location < contentLen) {
						content[location] = ' ';
					}
					reader.back();
					
					CharUtils.parseInt(content,0,result,true);
					return Byte.valueOf((byte)(result[0]*multiplier));
				} finally {
					if (result != null) {
						intArrayMemory.free(result);
					}
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final Byte[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				final int[]		parsed = intArrayMemory.allocate();
				
				try{from = CharUtils.parseSignedInt(content,from,parsed,true);
				
					result[0] = Byte.valueOf((byte)parsed[0]);
					return from;
				} finally {
					if (parsed != null) {
						intArrayMemory.free(parsed);
					}
				}
			}
		}

		@Override
		public Byte deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case INTEGER_VALUE 	:
						try{final long	result = reader.intValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return (byte)result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or integer value awaited");
				}
				
			}
		}
	}
	
	private static class CharacterSerializer extends JsonSerializer<Character> {
		private final ReusableInstances<char[]>		charArrayMemory = new ReusableInstances<>(()->{return new char[20];});
		
		@Override
		public void serialize(final Character instance, final CharacterTarget writer)  throws PrintingException {
			final char	symbol;
			
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else if (!CharUtils.symbolNeedsEscaping(symbol = instance.charValue(),true)) {
				writer.put(STRING_TERMINATOR).put(symbol).put(STRING_TERMINATOR);
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				int				from;
				
				try{content[0] = STRING_TERMINATOR;
					from = CharUtils.printEscapedChar(content,1,symbol,true,true);
					content[from++] = STRING_TERMINATOR;
					writer.put(content,0,from);
				} finally {
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int serialize(final Character instance, final char[] content, int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int	to = content.length;
				
				if (from < to) {
					if (reallyFilled) {
						content[from] = STRING_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				from++;
				if ((from = CharUtils.printEscapedChar(content,from,instance.charValue(),reallyFilled,false)) < 0) {
					reallyFilled = false;
				}
				if (from < to) {
					if (reallyFilled) {
						content[from] = STRING_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				from++;
				
				return reallyFilled ? from : -from; 
			}
		}

		@Override
		public void serialize(final Character instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
						writer.nullValue();
				}
				else {
					final char[]	buffer = charArrayMemory.allocate();
					
					try{buffer[0] = instance.charValue();
						
						writer.value(buffer,0,1);
					} finally {
						charArrayMemory.free(buffer);
					}
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public Character deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Char value is missing");
					}
				}

				if (symbol != STRING_TERMINATOR) {
					throw new SyntaxException(reader.atRow(),reader.atColumn(),"Char value is missing");
				}
				else {
					final char[]	content = charArrayMemory.allocate();
					
					try{int			location = 0;
						boolean		wasEscapeChar = false;
						
						while (location < content.length && ((symbol = reader.next()) != STRING_TERMINATOR || wasEscapeChar)) {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Char value is missing");
							}
							else if (symbol == '\\') {
								wasEscapeChar = true;
								content[location++] = symbol;
							}
							else {
								wasEscapeChar = false;
								content[location++] = symbol;
							}
						}
						if (symbol != STRING_TERMINATOR) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Char value unclosed");
						}
						else {
							CharUtils.parseEscapedChar(content,0,content);
							return Character.valueOf(content[0]);
						}
					} finally {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int deserialize(char[] content, int from, Character[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				if (from < to && content[from] == STRING_TERMINATOR) {
					from++;
				}
				else {
					throw new SyntaxException(0,from,"Char value is missing");
				}
				final char[]	buffer = charArrayMemory.allocate();
				
				try{from = CharUtils.parseEscapedChar(content,from,buffer);
				
					if (from < to && content[from] == STRING_TERMINATOR) { 
						from++;
					}
					else {
						throw new SyntaxException(0,from,"Unclosed char value constant");
					}
					result[0] = Character.valueOf(buffer[0]);
					return from;
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public Character deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case STRING_VALUE	:
						final char[]	buffer = charArrayMemory.allocate();
						
						try{reader.stringValue(buffer,0,1);
						
							if (reader.hasNext()) {
								reader.next();
							}
							return buffer[0];
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						} finally {
							charArrayMemory.free(buffer);
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or character value awaited");
				}
				
			}
		}
	}
	
	private static class DoubleSerializer extends JsonSerializer<Double> {
		private final ReusableInstances<char[]>		charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<double[]>	doubleArrayMemory = new ReusableInstances<>(()->{return new double[2];});

		@Override
		public void serialize(final Double instance, final CharacterTarget writer)  throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				
				try{printDouble(instance.doubleValue(),content,writer);
				} finally {
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int serialize(final Double instance, final char[] content, int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				return CharUtils.printDouble(content,from,instance.doubleValue(),reallyFilled);
			}
		}

		@Override
		public void serialize(final Double instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					printDouble(instance.doubleValue(),writer);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public Double deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"byte value is missing");
					}
				}

				final char[]	content = charArrayMemory.allocate();
				final double[]	result = doubleArrayMemory.allocate();
				
				try{final int	contentLen = content.length, multiplier;				
					int			location;
					
					if (symbol == '-') {
						multiplier = -1;
						location = 0;
					}
					else if (symbol == '+') {
						multiplier = 1;
						location = 0;
					}
					else if (symbol >= '0' && symbol <= '9') {
						multiplier = 1;
						content[0] = symbol;
						location = 1;
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"byte value is missing");
					}
					
					while ((symbol = reader.next()) >= '0' && symbol <= '9') {
						if (location < contentLen) {
							content[location++] = symbol;
						}
					}
					if (location < contentLen) {
						content[location] = ' ';
					}
					reader.back();
					
					CharUtils.parseDouble(content,0,result,true);
					return Double.valueOf(result[0]*multiplier);
				} finally {
					if (result != null) {
						doubleArrayMemory.free(result);
					}
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final Double[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length, multiplier;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				if (from < to) {
					if (content[from] == '-') {
						multiplier = -1;
						from++;
					}
					else if (content[from] == '+') {
						multiplier = 1;
						from++;
					}
					else {
						multiplier = 1;
					}
				}
				else {
					multiplier = 1;
				}
				
				final double[]	parsed = doubleArrayMemory.allocate();
				
				try{from = CharUtils.parseDouble(content,from,parsed,true);
				
					result[0] = Double.valueOf(parsed[0]*multiplier);
					return from;
				} finally {
					doubleArrayMemory.free(parsed);
				}
			}
		}

		@Override
		public Double deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case INTEGER_VALUE	:
						try{final double	result = reader.intValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case REAL_VALUE		:
						try{final double	result = reader.realValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or number value awaited");
				}
			}
		}
	}

	private static class FloatSerializer extends JsonSerializer<Float> {
		private final ReusableInstances<char[]>		charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<double[]>	doubleArrayMemory = new ReusableInstances<>(()->{return new double[2];});

		@Override
		public void serialize(final Float instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				
				try{printDouble(instance.doubleValue(),content,writer);
				} finally {
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int serialize(final Float instance, final char[] content, int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				return printDouble(instance.doubleValue(),content,from,reallyFilled);
			}
		}

		@Override
		public void serialize(final Float instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					printDouble(instance.doubleValue(),writer);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public Float deserialize(CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"byte value is missing");
					}
				}

				final char[]	content = charArrayMemory.allocate();
				final double[]	result = doubleArrayMemory.allocate();
				
				try{final int	contentLen = content.length, multiplier;				
					int			location;
					
					if (symbol == '-') {
						multiplier = -1;
						location = 0;
					}
					else if (symbol == '+') {
						multiplier = 1;
						location = 0;
					}
					else if (symbol >= '0' && symbol <= '9') {
						multiplier = 1;
						content[0] = symbol;
						location = 1;
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"byte value is missing");
					}
					
					while ((symbol = reader.next()) >= '0' && symbol <= '9') {
						if (location < contentLen) {
							content[location++] = symbol;
						}
					}
					if (location < contentLen) {
						content[location] = ' ';
					}
					reader.back();
					
					CharUtils.parseDouble(content,0,result,true);
					return Float.valueOf((float)result[0]*multiplier);
				} finally {
					if (result != null) {
						doubleArrayMemory.free(result);
					}
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final Float[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length, multiplier;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				if (from < to) {
					if (content[from] == '-') {
						multiplier = -1;
						from++;
					}
					else if (content[from] == '+') {
						multiplier = 1;
						from++;
					}
					else {
						multiplier = 1;
					}
				}
				else {
					multiplier = 1;
				}
				
				final double[]	parsed = doubleArrayMemory.allocate();
				
				try{from = CharUtils.parseDouble(content,from,parsed,true);
				
					result[0] = Float.valueOf((float)parsed[0]*multiplier);
					return from;
				} finally {
					if (parsed != null) {
						doubleArrayMemory.free(parsed);
					}
				}
			}
		}

		@Override
		public Float deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case INTEGER_VALUE	:
						try{final float	result = reader.intValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case REAL_VALUE		:
						try{final float	result = (float)reader.realValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or number value awaited");
				}
			}
		}
	}
	
	private static class IntegerSerializer extends JsonSerializer<Integer> {
		private final ReusableInstances<char[]>		charArrayMemory = new ReusableInstances<>(()->{return new char[20];});
		private final ReusableInstances<int[]>		intArrayMemory = new ReusableInstances<>(()->{return new int[2];});
		
		@Override
		public void serialize(final Integer instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				
				try{printLong(instance.intValue(),content,writer);
				} finally {
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int serialize(final Integer instance, final char[] content, int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				return printLong(instance.intValue(),content,from,reallyFilled);
			}
		}

		@Override
		public void serialize(final Integer instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					printLong(instance.longValue(),writer);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public Integer deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"integer value is missing");
					}
				}

				final char[]	content = charArrayMemory.allocate();
				final int[]		result = intArrayMemory.allocate();
				
				try{final int	contentLen = content.length, multiplier;
					int			location;
					
					if (symbol == '-') {
						multiplier = -1;
						location = 0;
					}
					else if (symbol == '+') {
						multiplier = 1;
						location = 0;
					}
					else if (symbol >= '0' && symbol <= '9') {
						multiplier = 1;
						content[0] = symbol;
						location = 1;
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"integer value is missing");
					}
					
					while ((symbol = reader.next()) >= '0' && symbol <= '9') {
						if (location < contentLen) {
							content[location++] = symbol;
						}
					}
					if (location < contentLen) {
						content[location] = ' ';
					}
					reader.back();
					
					CharUtils.parseInt(content,0,result,true);
					return Integer.valueOf(result[0]*multiplier);
				} finally {
					if (result != null) {
						intArrayMemory.free(result);
					}
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final Integer[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				final int[]		parsed = intArrayMemory.allocate();
				
				try{from = CharUtils.parseSignedInt(content,from,parsed,true);
				
					result[0] = Integer.valueOf(parsed[0]);
					return from;
				} finally {
					if (parsed != null) {
						intArrayMemory.free(parsed);
					}
				}
			}
		}

		@Override
		public Integer deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case INTEGER_VALUE	:
						try{final int	result = (int)reader.intValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or integer value awaited");
				}
			}
		}
	}
	
	private static class LongSerializer extends JsonSerializer<Long> {
		private final ReusableInstances<char[]>		charArrayMemory = new ReusableInstances<>(()->{return new char[20];});
		private final ReusableInstances<long[]>		intArrayMemory = new ReusableInstances<>(()->{return new long[2];});

		@Override
		public void serialize(final Long instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				
				try{printLong(instance.longValue(),content,writer);
				} finally {
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int serialize(final Long instance, final char[] content, int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				return printLong(instance.longValue(),content,from,reallyFilled);
			}
		}

		@Override
		public void serialize(final Long instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					printLong(instance.longValue(),writer);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public Long deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"integer value is missing");
					}
				}

				final char[]	content = charArrayMemory.allocate();
				final long[]	result = intArrayMemory.allocate();
				
				try{final int	contentLen = content.length, multiplier;			
					int			location;
					
					if (symbol == '-') {
						multiplier = -1;
						location = 0;
					}
					else if (symbol == '+') {
						multiplier = 1;
						location = 0;
					}
					else if (symbol >= '0' && symbol <= '9') {
						multiplier = 1;
						content[0] = symbol;
						location = 1;
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"integer value is missing");
					}
					
					while ((symbol = reader.next()) >= '0' && symbol <= '9') {
						if (location < contentLen) {
							content[location++] = symbol;
						}
					}
					if (location < contentLen) {
						content[location] = ' ';
					}
					reader.back();
					
					CharUtils.parseLong(content,0,result,true);
					
					return Long.valueOf(result[0]*multiplier);
				} finally {
					if (result != null) {
						intArrayMemory.free(result);
					}
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int deserialize(char[] content, int from, Long[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				final long[]		parsed = intArrayMemory.allocate();
				
				try{from = CharUtils.parseSignedLong(content,from,parsed,true);
				
					result[0] = Long.valueOf(parsed[0]);
					return from;
				} finally {
					if (parsed != null) {
						intArrayMemory.free(parsed);
					}
				}
			}
		}

		@Override
		public Long deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case INTEGER_VALUE	:
						try{final long	result = reader.intValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or integer value awaited");
				}
			}
		}
	}
	
	private static class ShortSerializer extends JsonSerializer<Short> {
		private final ReusableInstances<char[]>		charArrayMemory = new ReusableInstances<>(()->{return new char[20];});
		private final ReusableInstances<int[]>		intArrayMemory = new ReusableInstances<>(()->{return new int[2];});
		
		@Override
		public void serialize(final Short instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				
				try{printLong(instance.shortValue(),content,writer);
				} finally {
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int serialize(final Short instance, final char[] content, int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				return printLong(instance.shortValue(),content,from,reallyFilled);
			}
		}

		@Override
		public void serialize(final Short instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					printLong(instance.shortValue(),writer);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public Short deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"integer value is missing");
					}
				}

				final char[]	content = charArrayMemory.allocate();
				final int[]		result = intArrayMemory.allocate();
				
				try{final int	contentLen = content.length, multiplier;				
					int			location;
					
					if (symbol == '-') {
						multiplier = -1;
						content[0] = '-';
						location = 0;
					}
					else if (symbol == '+') {
						multiplier = 1;
						location = 0;
					}
					else if (symbol >= '0' && symbol <= '9') {
						multiplier = 1;
						content[0] = symbol;
						location = 1;
					}
					else {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"integer value is missing");
					}
					
					while ((symbol = reader.next()) >= '0' && symbol <= '9') {
						if (location < contentLen) {
							content[location++] = symbol;
						}
					}
					if (location < contentLen) {
						content[location] = ' ';
					}
					reader.back();
					
					CharUtils.parseInt(content,0,result,true);
					return Short.valueOf((short)(result[0]*multiplier));
				} finally {
					if (result != null) {
						intArrayMemory.free(result);
					}
					if (content != null) {
						charArrayMemory.free(content);
					}
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final Short[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}
				final int[]		parsed = intArrayMemory.allocate();
				
				try{from = CharUtils.parseSignedInt(content,from,parsed,true);
				
					result[0] = Short.valueOf((short)parsed[0]);
					return from;
				} finally {
					if (parsed != null) {
						intArrayMemory.free(parsed);
					}
				}
			}
		}

		@Override
		public Short deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case INTEGER_VALUE	:
						try{final short	result = (short)reader.intValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal short value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or integer value awaited");
				}
			}
		}
	}

	private static class StringSerializer extends JsonSerializer<String> {
		private final ReusableInstances<char[]>			charArrayMemory = new ReusableInstances<>(()->{return new char[100];});
		private final ReusableInstances<int[]>			intArrayMemory = new ReusableInstances<>(()->{return new int[2];});
		private final ReusableInstances<String[]>		stringArrayMemory = new ReusableInstances<>(()->{return new String[2];});
		private final ReusableInstances<StringBuilder>	stringBuilderMemory = new ReusableInstances<>(()->{return new StringBuilder();},(sb)->{sb.setLength(0); return sb;});

		@Override
		public void serialize(final String instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
				int				len = 0, newLen;
				
				try{writer.put(STRING_TERMINATOR);
					for (int index = 0, maxIndex = instance.length(); index < maxIndex; index++) {	// Accumulate up to 100 chars before printing
						if ((newLen = CharUtils.printEscapedChar(buffer,len,instance.charAt(index),true,false)) < 0) {
							writer.put(buffer,0,len);
							index--;
							len = 0;
						}
						else {
							len = newLen;
						}
					}
					if (len > 0) {
						writer.put(buffer,0,len);
					}
					writer.put(STRING_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final String instance, final char[] content, int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int	to = content.length;
				
				if (reallyFilled) {
					if (from < to) {
						content[from] = STRING_TERMINATOR;
					}
					else {
						reallyFilled = false;
					}
					from++;
				}
				if ((from = CharUtils.printEscapedCharArray(content,from,instance.toCharArray(),reallyFilled,false)) < 0) {
					from = -from;
					reallyFilled = false;
				}
				if (reallyFilled) {
					if (from < to) {
						content[from] = STRING_TERMINATOR;
					}
					else {
						reallyFilled = false;
					}
					from++;
				}
				return reallyFilled ? from : -from;
			}
		}

		@Override
		public void serialize(final String instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					writer.value(instance);
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public String deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				char			symbol;
				
				while ((symbol = reader.next()) <= ' ') {
					if (symbol == CharacterSource.EOF) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"String value is missing");
					}
				}

				if (symbol != STRING_TERMINATOR) {
					throw new SyntaxException(reader.atRow(),reader.atColumn(),"String value is missing");
				}
				
				final StringBuilder	buffer = stringBuilderMemory.allocate().append('\"');
				
				try{boolean		previousWasSlash = false, escapeDetected = false;
				
					while ((symbol = reader.next()) != STRING_TERMINATOR || previousWasSlash) {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Unclosed string value");
						}
						else if (symbol == '\\') {
							escapeDetected = true;
							previousWasSlash = true;
						}
						else {
							previousWasSlash = false;
						}
						buffer.append(symbol);
					}
					if (escapeDetected) {
						final String	result[] = stringArrayMemory.allocate(), toReturn;
						
						try{deserialize(buffer.append('\"').toString().toCharArray(),0,result);
							toReturn = result[0];
							
							return toReturn;	// Don't exclude toReturn because of 'finally' specific!
						} finally {
							stringArrayMemory.free(result);
						}
					}
					else {
						final String	toReturn = buffer.substring(1,buffer.length()); 
						
						return toReturn;	// Don't exclude toReturn because of 'finally' specific!
					}
				} finally {
					stringBuilderMemory.free(buffer);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final String[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				final int	newFrom;
				
				while (from < to && content[from] <= ' ') {
					from++;
				}

				if (from >= to || content[from] != STRING_TERMINATOR) {
					throw new SyntaxException(0,from,"String is missing"); 
				}
				final int[]	bounds = intArrayMemory.allocate();
				
				try{if ((newFrom = CharUtils.parseUnescapedString(content,from+1,STRING_TERMINATOR,true,bounds)) >= 0) {
						result[0] = new String(content,bounds[0],bounds[1]-bounds[0]+1);
						from = newFrom;
					}
					else {
						final StringBuilder	sb = stringBuilderMemory.allocate();
						
						try{from = CharUtils.parseString(content,from+1,STRING_TERMINATOR,sb);
							result[0] = sb.toString();
						} finally {
							stringBuilderMemory.free(sb);
						}
					}
					return from;
				} finally {
					intArrayMemory.free(bounds);
				}
			}
		}

		@Override
		public String deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case STRING_VALUE	:
						try{final String	result = reader.stringValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							return result;
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal string value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or string value awaited");
				}
			}
		}
	}

	private static class EnumSerializer<T> extends JsonSerializer<T> {
		private final int						maxNameLength;
		private final ReusableInstances<int[]>	intArrayMemory = new ReusableInstances<>(()->{return new int[2];});
		private final ReusableInstances<char[]>	charArrayMemory;
		private final MethodHandle				toEnum;
		
		private EnumSerializer(final Class<T> enumClass) throws EnvironmentException {
			int		maxLen = 0;
			
			for (T item : enumClass.getEnumConstants()) {
				maxLen = Math.max(maxLen,item.toString().length());
			}
			this.maxNameLength = maxLen + 2;
			this.charArrayMemory = new ReusableInstances<>(()->{return new char[maxNameLength];});
			
			try{final Method	m = enumClass.getMethod("valueOf",String.class);
				
				m.setAccessible(true);
				this.toEnum = MethodHandles.lookup().unreflect(m);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException e) {
				throw new EnvironmentException(e.getMessage(),e);
			}
		}

		@Override
		public void serialize(final T instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				writer.put(STRING_TERMINATOR).put(((Enum<?>)instance).toString()).put(STRING_TERMINATOR);
			}
		}

		@Override
		public int serialize(final T instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final String	item = instance.toString();
				final int		to = content.length, stringLen = item.length();
				int				newFrom = from;
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = STRING_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				if (newFrom + stringLen < to) {
					item.getChars(0,stringLen,content,newFrom);
				}
				else {
					reallyFilled = false;
				}
				newFrom += stringLen;
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = STRING_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final T instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
						writer.nullValue();
				}
				else {
					writer.value(instance.toString());
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public T deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final char[]	content = charArrayMemory.allocate();
				
				try{char		symbol;
					int			location = 0;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Enum value is missing");
						}
					}

					if (symbol != STRING_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Enum value is missing");
					}
					content[location++] = symbol;
					while ((symbol = reader.next()) != STRING_TERMINATOR) {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Enum value is missing");
						}
						else {
							content[location++] = symbol;
						}
					}
					content[location++] = symbol;
					final T	result = (T) toEnum.invoke(new String(content,1,location-2));
					
					return  result;
				} catch (Throwable e) {
					throw new ContentException(e.getLocalizedMessage(),e);
				} finally {
					charArrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final T[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (from < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to || content[newFrom] != STRING_TERMINATOR) {
					throw new SyntaxException(0,newFrom,"Enum string is missing"); 
				}
				final int[]	bounds = intArrayMemory.allocate();
				
				try{newFrom = CharUtils.parseUnescapedString(content,newFrom+1,STRING_TERMINATOR,true,bounds);
					result[0] = (T) toEnum.invoke(new String(content,bounds[0],bounds[1]));
					return newFrom;
				} catch (Throwable e) {
					throw new SyntaxException(0,newFrom,e.getLocalizedMessage(),e);
				} finally {
					intArrayMemory.free(bounds);
				}
			}
		}

		@Override
		public T deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case STRING_VALUE	:
						try{final String	value = reader.stringValue();
						
							if (reader.hasNext()) {
								reader.next();
							}
							try{return (T) toEnum.invoke(value);
							} catch (Throwable e) {
								throw new SyntaxException(reader.row(),reader.col(),e.getLocalizedMessage(),e);
							}
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal byte value in the stream: "+e.getLocalizedMessage());
						}
					case NULL_VALUE		:
						if (reader.hasNext()) {
							reader.next();
						}
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or integer value awaited");
				}
			}
		}
	}

	private static class BooleanArraySerializer extends JsonSerializer<boolean[]> {
		private static final BitCharSet		AVAILABLE = new BitCharSet('t','r','u','e','f','a','l','s');
		
		private final ReusableInstances<char[]>						charArrayMemory = new ReusableInstances<>(()->{return new char[10];});
		private final ReusableInstances<GrowableBooleanArray>		arrayMemory = new ReusableInstances<>(()->{return new GrowableBooleanArray(true);},(array)->{array.length(0); return array;});
		
		private BooleanArraySerializer() {
		}

		@Override
		public void serialize(final boolean[] instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				char	symbol = ARRAY_STARTER;
				
				for (boolean item : instance) {
					writer.put(symbol);
					printBoolean(item,writer);
					symbol = LIST_SPLITTER;
				}
				writer.put(ARRAY_TERMINATOR);
			}
		}

		@Override
		public int serialize(final boolean[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (boolean item : instance) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom = printBoolean(item,content,newFrom, reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final boolean[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (boolean item : instance) {
						if (needSplitter) {
							writer.splitter();
						}
						writer.value(item);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public boolean[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableBooleanArray	content = arrayMemory.allocate();
				final char[]				buffer = charArrayMemory.allocate();
				final int					bufferLen = buffer.length;
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {int			location = 0;
					
						while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							do {buffer[location++] = symbol;
								symbol = reader.next();
							} while (location < bufferLen && AVAILABLE.contains(symbol));
							reader.back();

							if (CharUtils.compare(buffer,0,BooleanSerializer.FOR_TRUE)) {
								content.append(true);
							}
							else if (CharUtils.compare(buffer,0,BooleanSerializer.FOR_FALSE)) {
								content.append(false);
							}
							else {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Illegal boolean value in the array");
							}
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Unclosed boolean array");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final boolean[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (from < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				final GrowableBooleanArray	temp = arrayMemory.allocate();
				
				try{do{	newFrom++;	// First loop skips '[', all next - ',' 
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						if (CharUtils.compare(content,newFrom,BooleanSerializer.FOR_TRUE)) {
							temp.append(true);
							newFrom += BooleanSerializer.FOR_TRUE.length;
						}
						else if (CharUtils.compare(content,newFrom,BooleanSerializer.FOR_FALSE)) {
							temp.append(false);
							newFrom += BooleanSerializer.FOR_FALSE.length;
						}
						else {
							throw new SyntaxException(0,newFrom,"Illegal boolean value in the array");
						}
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed boolean array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public boolean[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableBooleanArray	temp = arrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case BOOLEAN_VALUE	:
										temp.append(reader.booleanValue());
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										reader.next();
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}

	private static class ByteArraySerializer extends JsonSerializer<byte[]> {
		private final ReusableInstances<char[]>					charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<int[]>					intArrayMemory = new ReusableInstances<>(()->{return new int[1];});
		private final ReusableInstances<GrowableByteArray>		arrayMemory = new ReusableInstances<>(()->{return new GrowableByteArray(true);},(array)->{array.length(0); return array;});
		
		private ByteArraySerializer() {
		}

		@Override
		public void serialize(final byte[] instance, CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
						
				try{char	symbol = ARRAY_STARTER;
					
					for (byte item : instance) {
						writer.put(symbol);
						printLong(item,buffer,writer);
						symbol = LIST_SPLITTER;
					}
					writer.put(ARRAY_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final byte[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (byte item : instance) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom  =printLong(item,content,newFrom,reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final byte[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (byte item : instance) {
						if (needSplitter) {
							writer.splitter();
						}
						printLong(item,writer);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public byte[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableByteArray		content = arrayMemory.allocate();
				final int[]					value = intArrayMemory.allocate();
				final char[]				buffer = charArrayMemory.allocate();
				final int					bufferLen = buffer.length;
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {int			location = 0, multiplier;
					
						while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							if (symbol == '-') {
								multiplier = -1;
								symbol = reader.next();
							}
							else if (symbol == '+') {
								multiplier = 1;
								symbol = reader.next();
							}
							else {
								multiplier = 1;
							}
							
							do {buffer[location++] = symbol;
								symbol = reader.next();
							} while (location < bufferLen-1 && symbol >= '0' && symbol <= '9');
							reader.back();
							
							buffer[location] = ' ';
							CharUtils.parseInt(buffer,0,value,true);
							content.append((byte) (value[0]*multiplier));
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array unclosed");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					intArrayMemory.free(value);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final byte[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				final GrowableByteArray	temp = arrayMemory.allocate();
				final int[] 			value = intArrayMemory.allocate();
				
				try{do{	newFrom++;	// The same first loop skips '[', all next - ','

						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						
						newFrom = CharUtils.parseSignedInt(content,newFrom,value,true);
						temp.append((byte)value[0]);
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed byte array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					intArrayMemory.free(value);
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public byte[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableByteArray	temp = arrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case INTEGER_VALUE	:
										temp.append((byte)reader.intValue());
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										reader.next();
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}

	
	private static class CharArraySerializer extends JsonSerializer<char[]> {
		private final ReusableInstances<char[]>					charArrayMemory = new ReusableInstances<>(()->{return new char[10];});
		private final ReusableInstances<int[]>					intArrayMemory = new ReusableInstances<>(()->{return new int[2];});
		private final ReusableInstances<GrowableCharArray>		arrayMemory = new ReusableInstances<>(()->{return new GrowableCharArray(true);},(array)->{array.length(0); return array;});
		
		private CharArraySerializer() {
		}

		@Override
		public void serialize(final char[] instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
				
				try{char	symbol = ARRAY_STARTER;
					
					for (char item : instance) {
						final int 	len = CharUtils.printEscapedChar(buffer,0,item,true,true);
						
						writer.put(symbol).put(STRING_TERMINATOR).put(buffer,0,len).put(STRING_TERMINATOR);
						symbol = LIST_SPLITTER;
					}
					writer.put(ARRAY_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final char[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				final char[]	temp = charArrayMemory.allocate();
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;

				try{for (char item : instance) {
						if (newFrom < to) {
							if (reallyFilled) {
								content[newFrom] = symbol;
							}
						}
						else {
							reallyFilled = false;
						}
						newFrom++;
						
						if (newFrom < to) {
							if (reallyFilled) {
								content[newFrom] = STRING_TERMINATOR;
							}
						}
						else {
							reallyFilled = false;
						}
						newFrom++;
						
						if ((newFrom = CharUtils.printEscapedChar(content,newFrom,item, reallyFilled,true)) < 0) {
							newFrom = -newFrom;
							reallyFilled = false;
						}
						if (newFrom < to) {
							if (reallyFilled) {
								content[newFrom] = STRING_TERMINATOR;
							}
						}
						else {
							reallyFilled = false;
						}
						newFrom++;
						symbol = LIST_SPLITTER;
					}
					
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = ARRAY_TERMINATOR;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					return reallyFilled ? newFrom : -newFrom;
				} finally {
					charArrayMemory.free(temp);
				}
			}
		}

		@Override
		public void serialize(final char[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					final char[] 	content = charArrayMemory.allocate();
					
					try{boolean		needSplitter = false;
						writer.startArray();
						for (char item : instance) {
							if (needSplitter) {
								writer.splitter();
							}
							content[0] = item;
							writer.value(content,0,1);
							needSplitter = true;
						}
						writer.endArray();
					} finally {
						charArrayMemory.free(content);
					}
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public char[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableCharArray	content = arrayMemory.allocate();
				final char[]			buffer = charArrayMemory.allocate();
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							int			location = 0;
							boolean		wasEscapeChar = false;
							
							while (location < buffer.length && ((symbol = reader.next()) != STRING_TERMINATOR || wasEscapeChar)) {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Char value is missing");
								}
								else if (symbol == '\\') {
									wasEscapeChar = true;
									buffer[location++] = symbol;
								}
								else {
									wasEscapeChar = false;
									buffer[location++] = symbol;
								}
							}
							if (symbol != STRING_TERMINATOR) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Char value unclosed");
							}
							else {
								CharUtils.parseEscapedChar(buffer,0,buffer);
								content.append(buffer[0]);
							}
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Unclozed char array");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Unclosed char array");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final char[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				final GrowableCharArray	temp = arrayMemory.allocate();
				final char[]			value = charArrayMemory.allocate();
				final int[]				bounds = intArrayMemory.allocate();
				
				try{do{	newFrom++;	// The same first loop skips '[', all next - ','
				
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						
						if (newFrom < to && content[newFrom] == STRING_TERMINATOR) {
							newFrom = CharUtils.parseEscapedChar(content,newFrom+1,value);
							temp.append(value[0]);
						}
						else {
							throw new SyntaxException(0,newFrom,"Unquoted char");
						}
						if (newFrom < to && content[newFrom] == STRING_TERMINATOR) {
							newFrom++;
						}
						else {
							throw new SyntaxException(0,newFrom,"Unquoted char");
						}
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed char array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					intArrayMemory.free(bounds);
					charArrayMemory.free(value);
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public char[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableCharArray	temp = arrayMemory.allocate();
						final char[]			symbol = charArrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case STRING_VALUE	:
										reader.stringValue(symbol,0,1);
										temp.append(symbol[0]);
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										if (reader.hasNext()) {
											reader.next();
										}
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							charArrayMemory.free(symbol);
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}
	
	private static class DoubleArraySerializer extends JsonSerializer<double[]> {
		private final ReusableInstances<char[]>					charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<double[]>				intArrayMemory = new ReusableInstances<>(()->{return new double[1];});
		private final ReusableInstances<GrowableDoubleArray>	arrayMemory = new ReusableInstances<>(()->{return new GrowableDoubleArray(true);},(array)->{array.length(0); return array;});
		
		private DoubleArraySerializer() {
		}

		@Override
		public void serialize(final double[] instance, CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
						
				try{char	symbol = ARRAY_STARTER;
					
					for (double item : instance) {
						writer.put(symbol);
						printDouble(item,buffer,writer);
						symbol = LIST_SPLITTER;
					}
					writer.put(ARRAY_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final double[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (double item : instance) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom = printDouble(item,content,newFrom,reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final double[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (double item : instance) {
						if (needSplitter) {
							writer.splitter();
						}
						printDouble(item,writer);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public double[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableDoubleArray	content = arrayMemory.allocate();
				final double[]				value = intArrayMemory.allocate();
				final char[]				buffer = charArrayMemory.allocate();
				final int					bufferLen = buffer.length;
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {int			location = 0, multiplier;
					
						while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							if (symbol == '-') {
								multiplier = -1;
								symbol = reader.next();
							}
							else if (symbol == '+') {
								multiplier = 1;
								symbol = reader.next();
							}
							else {
								multiplier = 1;
							}
							
							do {buffer[location++] = symbol;
								symbol = reader.next();
							} while (location < bufferLen-1 && (symbol == '.' || symbol >= '0' && symbol <= '9' || symbol == 'e' || symbol == 'E' || symbol == '-'));
							reader.back();
							
							buffer[location] = ' ';
							
							CharUtils.parseDouble(buffer,0,value,true);
							content.append(value[0]*multiplier);
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Double array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Double array unclosed");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					intArrayMemory.free(value);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final double[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Double array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Double array value is missing"); 
					}
				}
				
				final GrowableDoubleArray	temp = arrayMemory.allocate();
				final double[] 				value = intArrayMemory.allocate();
				int							multiplier;
				
				try{do{	newFrom++;	// The same first loop skips '[', all next - ','
				
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						if (newFrom < to && content[newFrom] == '-') {
							multiplier = -1;
							newFrom++;
						}
						else if (newFrom < to && content[newFrom] == '+') {
							multiplier = 1;
							newFrom++;
						}
						else {
							multiplier = 1;
						}
						newFrom = CharUtils.parseDouble(content,newFrom,value,true);
						temp.append(value[0]*multiplier);
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (from < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed double array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					intArrayMemory.free(value);
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public double[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableDoubleArray	temp = arrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case INTEGER_VALUE	:
										temp.append(reader.intValue());
										break;
									case REAL_VALUE	:
										temp.append(reader.realValue());
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										reader.next();
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}

	private static class FloatArraySerializer extends JsonSerializer<float[]> {
		private final ReusableInstances<char[]>					charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<double[]>				intArrayMemory = new ReusableInstances<>(()->{return new double[1];});
		private final ReusableInstances<GrowableFloatArray>		arrayMemory = new ReusableInstances<>(()->{return new GrowableFloatArray(true);},(array)->{array.length(0); return array;});
		
		private FloatArraySerializer() {
		}

		@Override
		public void serialize(final float[] instance, CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
						
				try{char	symbol = ARRAY_STARTER;
					
					for (double item : instance) {
						writer.put(symbol);
						printDouble(item,buffer,writer);
						symbol = LIST_SPLITTER;
					}
					writer.put(ARRAY_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final float[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (double item : instance) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom = printDouble(item,content,newFrom,reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final float[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (float item : instance) {
						if (needSplitter) {
							writer.splitter();
						}
						printDouble(item,writer);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public float[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableFloatArray	content = arrayMemory.allocate();
				final double[]				value = intArrayMemory.allocate();
				final char[]				buffer = charArrayMemory.allocate();
				final int					bufferLen = buffer.length;
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {int			location = 0, multiplier;
					
						while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Float array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							if (symbol == '-') {
								multiplier = -1;
								symbol = reader.next();
							}
							else if (symbol == '+') {
								multiplier = 1;
								symbol = reader.next();
							}
							else {
								multiplier = 1;
							}
							
							do {buffer[location++] = symbol;
								symbol = reader.next();
							} while (location < bufferLen-1 && (symbol == '.' || symbol >= '0' && symbol <= '9' || symbol == 'e' || symbol == 'E' || symbol == '-'));
							reader.back();
							
							buffer[location] = ' ';
							CharUtils.parseDouble(buffer,0,value,true);
							content.append((float) (value[0]*multiplier));
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Float array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Float array unclosed");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					intArrayMemory.free(value);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final float[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				final GrowableFloatArray	temp = arrayMemory.allocate();
				final double[] 				value = intArrayMemory.allocate();
				int							multiplier;
				
				try{do{	newFrom++;	// The same first loop skips '[', all next - ','
				
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						if (newFrom < to && content[newFrom] == '-') {
							multiplier = -1;
							newFrom++;
						}
						else if (newFrom < to && content[newFrom] == '+') {
							multiplier = 1;
							newFrom++;
						}
						else {
							multiplier = 1;
						}
						newFrom = CharUtils.parseDouble(content,newFrom,value,true);
						temp.append((float) (value[0]*multiplier));
						while (from < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed byte array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					intArrayMemory.free(value);
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public float[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableFloatArray	temp = arrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case INTEGER_VALUE	:
										temp.append(reader.intValue());
										break;
									case REAL_VALUE	:
										temp.append((float)reader.realValue());
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										reader.next();
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}

	private static class IntArraySerializer extends JsonSerializer<int[]> {
		private final ReusableInstances<char[]>					charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<int[]>					intArrayMemory = new ReusableInstances<>(()->{return new int[1];});
		private final ReusableInstances<GrowableIntArray>		arrayMemory = new ReusableInstances<>(()->{return new GrowableIntArray(true);},(array)->{array.length(0); return array;});
		
		private IntArraySerializer() {
		}

		@Override
		public void serialize(final int[] instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
						
				try{char	symbol = ARRAY_STARTER;
					
					for (int item : instance) {
						writer.put(symbol);
						printLong(item,buffer,writer);
						symbol = LIST_SPLITTER;
					}
					writer.put(ARRAY_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final int[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (int item : instance) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom = printLong(item,content,newFrom,reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final int[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (int item : instance) {
						if (needSplitter) {
							writer.splitter();
						}
						printLong(item,writer);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public int[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableIntArray		content = arrayMemory.allocate();
				final int[]					value = intArrayMemory.allocate();
				final char[]				buffer = charArrayMemory.allocate();
				final int					bufferLen = buffer.length;
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {int			location = 0, multiplier;
					
						while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							if (symbol == '-') {
								multiplier = -1;
								symbol = reader.next();
							}
							else if (symbol == '+') {
								multiplier = 1;
								symbol = reader.next();
							}
							else {
								multiplier = 1;
							}
							
							do {buffer[location++] = symbol;
								symbol = reader.next();
							} while (location < bufferLen-1 && symbol >= '0' && symbol <= '9');
							reader.back();
							
							buffer[location] = ' ';
							CharUtils.parseInt(buffer,0,value,true);
							content.append(value[0]*multiplier);
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Int array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Int array unclosed");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					intArrayMemory.free(value);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final int[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				final GrowableIntArray	temp = arrayMemory.allocate();
				final int[] 			value = intArrayMemory.allocate();
				
				try{do{	newFrom++;	// The same first loop skips '[', all next -','
				
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						newFrom = CharUtils.parseSignedInt(content,newFrom,value,true);
						temp.append(value[0]);
						while (from < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed byte array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					intArrayMemory.free(value);
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public int[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableIntArray	temp = arrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case INTEGER_VALUE	:
										temp.append((int)reader.intValue());
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										reader.next();
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}

	private static class LongArraySerializer extends JsonSerializer<long[]> {
		private final ReusableInstances<char[]>					charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<long[]>					intArrayMemory = new ReusableInstances<>(()->{return new long[1];});
		private final ReusableInstances<GrowableLongArray>		arrayMemory = new ReusableInstances<>(()->{return new GrowableLongArray(true);},(array)->{array.length(0); return array;});
		
		private LongArraySerializer() {
		}

		@Override
		public void serialize(final long[] instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
						
				try{char	symbol = ARRAY_STARTER;
					
					for (long item : instance) {
						writer.put(symbol);
						printLong(item,buffer,writer);
						symbol = LIST_SPLITTER;
					}
					writer.put(ARRAY_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final long[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (long item : instance) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom = printLong(item,content,newFrom,reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public long[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableLongArray		content = arrayMemory.allocate();
				final long[]				value = intArrayMemory.allocate();
				final char[]				buffer = charArrayMemory.allocate();
				final int					bufferLen = buffer.length;
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {int			location = 0, multiplier;
					
						while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							if (symbol == '-') {
								multiplier = -1;
								symbol = reader.next();
							}
							else if (symbol == '+') {
								multiplier = 1;
								symbol = reader.next();
							}
							else {
								multiplier = 1;
							}
							
							do {buffer[location++] = symbol;
								symbol = reader.next();
							} while (location < bufferLen-1 && symbol >= '0' && symbol <= '9');
							reader.back();
							
							buffer[location] = ' ';
							CharUtils.parseLong(buffer,0,value,true);
							content.append(value[0]*multiplier);
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Long array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Long array unclosed");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					intArrayMemory.free(value);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final long[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				final GrowableLongArray	temp = arrayMemory.allocate();
				final long[] 			value = intArrayMemory.allocate();
				
				try{do{	newFrom++;	// The same first loop skips '[', all next - ','
				
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						newFrom = CharUtils.parseSignedLong(content,newFrom,value,true);
						temp.append(value[0]);
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed long array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					intArrayMemory.free(value);
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public void serialize(final long[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (long item : instance) {
						if (needSplitter) {
							writer.splitter();
						}
						printLong(item,writer);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public long[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableLongArray	temp = arrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case INTEGER_VALUE	:
										temp.append(reader.intValue());
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										reader.next();
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}

	private static class ShortArraySerializer extends JsonSerializer<short[]> {
		private final ReusableInstances<char[]>					charArrayMemory = new ReusableInstances<>(()->{return new char[64];});
		private final ReusableInstances<int[]>					intArrayMemory = new ReusableInstances<>(()->{return new int[1];});
		private final ReusableInstances<GrowableShortArray>		arrayMemory = new ReusableInstances<>(()->{return new GrowableShortArray(true);},(array)->{array.length(0); return array;});
		
		private ShortArraySerializer() {
		}

		@Override
		public void serialize(final short[] instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				final char[]	buffer = charArrayMemory.allocate();
						
				try{char	symbol = ARRAY_STARTER;
					
					for (short item : instance) {
						writer.put(symbol);
						printLong(item,buffer,writer);
						symbol = LIST_SPLITTER;
					}
					writer.put(ARRAY_TERMINATOR);
				} finally {
					charArrayMemory.free(buffer);
				}
			}
		}

		@Override
		public int serialize(final short[] instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (short item : instance) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom = printLong(item,content,newFrom,reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final short[] instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (short item : instance) {
						if (needSplitter) {
							writer.splitter();
						}
						printLong(item,writer);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public short[] deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final GrowableShortArray	content = arrayMemory.allocate();
				final int[]					value = intArrayMemory.allocate();
				final char[]				buffer = charArrayMemory.allocate();
				final int					bufferLen = buffer.length;
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Boolean array value is missing");
						}
					}
					do {int			location = 0, multiplier;
					
						while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							if (symbol == '-') {
								multiplier = -1;
								symbol = reader.next();
							}
							else if (symbol == '+') {
								multiplier = 1;
								symbol = reader.next();
							}
							else {
								multiplier = 1;
							}
							
							do {buffer[location++] = symbol;
								symbol = reader.next();
							} while (location < bufferLen-1 && (symbol == '.' || symbol >= '0' && symbol <= '9'));
							reader.back();
							
							buffer[location] = ' ';
							CharUtils.parseInt(buffer,0,value,true);
							content.append((short) (value[0]*multiplier));
						
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Short array unclosed");
					}
					else {
						return content.extract();
					}
				} finally {
					charArrayMemory.free(buffer);
					intArrayMemory.free(value);
					arrayMemory.free(content);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, final int from, final short[][] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				final GrowableShortArray	temp = arrayMemory.allocate();
				final int[] 				value = intArrayMemory.allocate();
				
				try{do{	newFrom++;	// The same first loop skips '[', all next - ','
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						newFrom = CharUtils.parseSignedInt(content,newFrom,value,true);
						temp.append((short)value[0]);
						while (from < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed byte array");
					}
					else {
						result[0] = temp.extract();
						return newFrom+1;
					}
				} finally {
					intArrayMemory.free(value);
					arrayMemory.free(temp);
				}
			}
		}

		@Override
		public short[] deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final GrowableShortArray	temp = arrayMemory.allocate();
						
						try{
loop:						for (JsonStaxParserLexType item : reader) {
								switch (item) {
									case INTEGER_VALUE	:
										temp.append((short)reader.intValue());
										break;
									case LIST_SPLITTER	:
										break;
									case END_ARRAY		:
										reader.next();
										break loop;
									default :
										throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");										
								}
							}
							return temp.extract();
						} catch (IOException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
						} finally {
							arrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}

	private static class RefArraySerializer<T,Inner> extends JsonSerializer<T> {
		private final Class<Inner>		innerClass;
		private final JsonSerializer<Inner>	innerSerializer;
		
		private final ReusableInstances<Object[]>		objectArrayMemory;
		private final ReusableInstances<List<Object>>	listArrayMemory = new ReusableInstances<>(()->{return new ArrayList<>();},(list)->{list.clear(); return list;});
		
		
		private RefArraySerializer(final Class<Inner> innerClass, final JsonSerializer<Inner> innerSerializer) {
			this.innerClass = innerClass;
			this.innerSerializer = innerSerializer;
			this.objectArrayMemory = new ReusableInstances<>(()->{return (Object[])Array.newInstance(innerClass,1);});
		}

		@Override
		public void serialize(final T instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				char	symbol = ARRAY_STARTER;
				
				for (int index = 0, maxIndex = Array.getLength(instance); index < maxIndex; index++) {
					writer.put(symbol);
					innerSerializer.serialize(innerClass.cast(Array.get(instance,index)), writer);
					symbol = LIST_SPLITTER;
				}
				writer.put(ARRAY_TERMINATOR);
			}
		}

		@Override
		public int serialize(final T instance, final char[] content, final int from, boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final int		to = content.length;
				char			symbol = ARRAY_STARTER;
				int				newFrom = from;
				
				for (int index = 0, maxIndex = Array.getLength(instance); index < maxIndex; index++) {
					if (newFrom < to) {
						if (reallyFilled) {
							content[newFrom] = symbol;
						}
					}
					else {
						reallyFilled = false;
					}
					newFrom++;
					
					if ((newFrom = innerSerializer.serialize(innerClass.cast(Array.get(instance,index)),content,newFrom,reallyFilled)) < 0) {
						newFrom = -newFrom;
						reallyFilled = false;
					}
					symbol = LIST_SPLITTER;
				}
				
				if (newFrom < to) {
					if (reallyFilled) {
						content[newFrom] = ARRAY_TERMINATOR;
					}
				}
				else {
					reallyFilled = false;
				}
				newFrom++;
				
				return reallyFilled ? newFrom : -newFrom;
			}
		}

		@Override
		public void serialize(final T instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
					writer.nullValue();
				}
				else {
					boolean		needSplitter = false;
					
					writer.startArray();
					for (int index = 0, maxIndex = Array.getLength(instance); index < maxIndex; index++) {
						if (needSplitter) {
							writer.splitter();
						}
						innerSerializer.serialize(innerClass.cast(Array.get(instance,index)), writer);
						needSplitter = true;
					}
					writer.endArray();
				}
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final List<Object>			content = new ArrayList<>();
				
				try{char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref array value is missing");
						}
					}

					if (symbol != ARRAY_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref array value is missing");
						}
					}
					do {while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref array value is missing");
							}
						}
						if (symbol == ARRAY_TERMINATOR) {
							break;
						}
						else {
							reader.back();
							content.add(innerSerializer.deserialize(reader));
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref array value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != ARRAY_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Byte boolean array");
					}
					else {
						final Object[]	result = (Object[]) Array.newInstance(innerClass,content.size());
						
						for (int index = 0, maxIndex = content.size(); index < maxIndex; index++) {
							result[index] = content.get(index);
						}
						content.clear();
						return (T)result;
					}
				} finally {
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public int deserialize(final char[] content, final int from, final T[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != ARRAY_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				final List<Object>		temp = listArrayMemory.allocate();
				final Object[]			value = objectArrayMemory.allocate();
				
				try{do{	newFrom++;		// The same first loop skips '[', all next - ','
						
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == ARRAY_TERMINATOR) {
							break;
						}
						newFrom = innerSerializer.deserialize(content,newFrom,(Inner[]) value);
						temp.add(value[0]);
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != ARRAY_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed ref array");
					}
					else {
						final Object[]	returned = (Object[]) Array.newInstance(innerClass,temp.size());
						
						for (int index = 0, maxIndex = temp.size(); index < maxIndex; index++) {
							returned[index] = temp.get(index);
						}
						result[0] = (T)returned;
						temp.clear();
						
						return newFrom+1;
					}
				} finally {
					objectArrayMemory.free(value);
					listArrayMemory.free(temp);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T deserialize(final JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_ARRAY	:
						final List<Object>		temp = listArrayMemory.allocate();
						final Object[]			value = objectArrayMemory.allocate();
						
						try{do {reader.next();	// The same first call skips '[', all next - ','
								if (reader.current() == JsonStaxParserLexType.END_ARRAY) {
									break;
								}
								temp.add(innerSerializer.deserialize(reader));
							} while (reader.current() == JsonStaxParserLexType.LIST_SPLITTER);
						
							final Object[]	returned = (Object[]) Array.newInstance(innerClass,temp.size());
							
							for (int index = 0, maxIndex = temp.size(); index < maxIndex; index++) {
								returned[index] = temp.get(index);
							}
							temp.clear();
							reader.next();
							return (T)returned;
						} finally {
							objectArrayMemory.free(value);
							listArrayMemory.free(temp);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
	}
	
	private static class GetterAndSetter {
		private final Class<?>		valueType;
		private final int			typeSort;
		private final MethodHandle	getter;
		private final MethodHandle	setter;
		private final JsonSerializer<?>	serializer;
		
		private GetterAndSetter(final Field field, final JsonSerializer<?> serializer) throws IllegalAccessException {
			field.setAccessible(true);
			if (PureLibSettings.instance().getProperty(PureLibSettings.ALLOW_UNSAFE,boolean.class,"false")) {
				this.getter = MethodHandles.lookup().unreflectGetter(field);
				this.setter = MethodHandles.lookup().unreflectSetter(field);
			}
			else {
				this.getter = MethodHandles.lookup().unreflectGetter(field);
				this.setter = MethodHandles.lookup().unreflectSetter(field);
			} 
			this.serializer = serializer;
			this.valueType = field.getType();
			if (this.valueType.isPrimitive()) {
				switch (CompilerUtils.defineClassType(this.valueType)) {
					case CompilerUtils.CLASSTYPE_BOOLEAN	: this.typeSort = PRIMITIVE_BOOLEAN; break;
					case CompilerUtils.CLASSTYPE_BYTE		: this.typeSort = PRIMITIVE_BYTE; break;
					case CompilerUtils.CLASSTYPE_CHAR		: this.typeSort = PRIMITIVE_CHAR; break;
					case CompilerUtils.CLASSTYPE_DOUBLE		: this.typeSort = PRIMITIVE_DOUBLE; break;
					case CompilerUtils.CLASSTYPE_FLOAT		: this.typeSort = PRIMITIVE_FLOAT; break;
					case CompilerUtils.CLASSTYPE_INT		: this.typeSort = PRIMITIVE_INT; break;
					case CompilerUtils.CLASSTYPE_LONG		: this.typeSort = PRIMITIVE_LONG; break;
					case CompilerUtils.CLASSTYPE_SHORT		: this.typeSort = PRIMITIVE_SHORT; break;
					default : throw new UnsupportedOperationException("Primitive type ["+this.valueType.getSimpleName()+"] is not supported yet");
				}
			}
			else {
				this.typeSort = NOT_PRIMITIVE;
			}
		}
		
		private Object getValue(final Object instance) throws ContentException {
			try{return getter.invoke(instance);
			} catch (Throwable e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
		
		private void setValue(final Object instance, final Object value) throws ContentException {
			try{setter.invoke(instance,value);
			} catch (Throwable e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
		
		@SuppressWarnings("unchecked")
		private <T> JsonSerializer<T> getSerializer() {
			return (JsonSerializer<T>) serializer;
		}
		
		private int getTypeSort() {
			return typeSort;
		}
		
		private Class<?> getValueType() {
			return valueType;
		}
	}

	private static class ObjectSerializer<T> extends JsonSerializer<T> {
		private final ReusableInstances<char[]>				forNames = new ReusableInstances<char[]>(()->{return new char[100];});  
		private final Class<T>								contentType;
		private final SyntaxTreeInterface<GetterAndSetter> 	names;
		
		private ObjectSerializer(final Class<T> contentType, final SyntaxTreeInterface<GetterAndSetter> names) {
			this.contentType = contentType;
			this.names = names;
		}

		@Override
		public void serialize(final T instance, final CharacterTarget writer) throws PrintingException {
			if (writer == null) {
				throw new NullPointerException("Writer to serialize can't be null"); 
			}
			else if (instance == null) {
				writer.put(FOR_NULL);
			}
			else {
				names.walk(new Walker<GetterAndSetter>() {
					char	symbol = OBJECT_STARTER;
					
					@Override
					public boolean process(final char[] name, int len, long id, final GetterAndSetter cargo) {
						try{writer.put(symbol).put('\"').put(name,0,len).put("\":");
							cargo.getSerializer().serialize(cargo.getValue(instance),writer);
							symbol = LIST_SPLITTER;
						} catch (ContentException e) {
							e.printStackTrace();
						}
						return true;
					}
				});
				
				writer.put(OBJECT_TERMINATOR);
			}
		}

		@Override
		public int serialize(T instance, char[] content, final int from, final boolean reallyFilled) {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to serialize can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
			}
			else if (instance == null) {
				return printCharArray(content,from,FOR_NULL,reallyFilled);
			}
			else {
				final InnerWalker	walker = new InnerWalker(instance,content,from,reallyFilled);
				
				names.walk(walker);
				walker.printChar(OBJECT_TERMINATOR);
				
				return walker.needFill ? walker.newFrom : -walker.newFrom;
			}
		}

		@Override
		public void serialize(final T instance, final JsonStaxPrinter writer) throws PrintingException {
			try{if (writer == null) {
					throw new NullPointerException("Writer to serialize can't be null"); 
				}
				else if (instance == null) {
						writer.nullValue();
				}
				else {
					writer.startObject();
					names.walk(new Walker<GetterAndSetter>() {
						boolean	needSplitter = false;
						
						@Override
						public boolean process(final char[] name, int len, long id, final GetterAndSetter cargo) {
							try{if (needSplitter) {
									writer.splitter();
								}
								writer.name(name,0,len);
								cargo.getSerializer().serialize(cargo.getValue(instance),writer);
								needSplitter = true;
								return true;
							} catch (ContentException | IOException e) {
								e.printStackTrace();
								return false;
							}
						}
					});
					writer.endObject();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new PrintingException(e.getLocalizedMessage(),e);
			}
		}

		@Override
		public T deserialize(final CharacterSource reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader to deserialize can't be null"); 
			}
			else {
				final char[]	forName = forNames.allocate();
				
				try{final T		instance = (T) contentType.newInstance();
					char		symbol;
					
					while ((symbol = reader.next()) <= ' ') {
						if (symbol == CharacterSource.EOF) {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj value is missing");
						}
					}

					if (symbol != OBJECT_STARTER) {
						if (symbol == 'n' && reader.next() == 'u' && reader.next() == 'l' && reader.next() == 'l') {
							return null;
						}
						else {
							throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj value is missing");
						}
					}
					do {while ((symbol = reader.next()) <= ' ') {
							if (symbol == CharacterSource.EOF) {
								throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj value is missing");
							}
						}
						if (symbol == OBJECT_TERMINATOR) {
							break;
						}
						else {
							int		location = 0;
							
							if (symbol == '\"') {
								while ((symbol = reader.next()) != '\"') {
									if (symbol == CharacterSource.EOF) {
										throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj value is missing");
									}
									else if (location >= forName.length) {
										throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj too long name of the field (more than ["+forName.length+"] chars)");
									}
									else {
										forName[location++] = symbol;
									}
								}
								while ((symbol = reader.next()) <= ' ') {
									if (symbol == CharacterSource.EOF) {
										throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj value is missing");
									}
								}
								if (symbol != NAME_SPLITTER) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj colon is missing");
								}
								else {
									final long	nameId = names.seekName(forName,0,location);
									
									if (nameId < 0) {
										throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj unknown field name ["+new String(forName,0,location)+"]");
									}
									else {
										final GetterAndSetter	cargo = names.getCargo(nameId);
										
										cargo.setValue(instance,cargo.getSerializer().deserialize(reader));
									}
								}
							}
							
							while ((symbol = reader.next()) <= ' ') {
								if (symbol == CharacterSource.EOF) {
									throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj value is missing");
								}
							}
						}
					} while (symbol == LIST_SPLITTER);
					
					if (symbol != OBJECT_TERMINATOR) {
						throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj unclosed");
					}
					else {
						return instance;
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new SyntaxException(reader.atRow(),reader.atColumn(),"Ref obj unknown field name");
				} finally {
					forNames.free(forName);
				}
			}
		}

		@Override
		public int deserialize(final char[] content, int from, final T[] result) throws SyntaxException {
			if (content == null || content.length == 0) {
				throw new IllegalArgumentException("Content to read from can't be null or empty array"); 
			}
			else if (from < 0) {
				throw new IllegalArgumentException("From position ["+from+"] can't be negative");
			}
			else if (result  == null || result.length == 0) {
				throw new IllegalArgumentException("Result can't be null or empty array"); 
			}
			else {
				final int	to = content.length;
				int			newFrom = from;
				
				while (newFrom < to && content[newFrom] <= ' ') {
					newFrom++;
				}

				if (newFrom >= to){
					throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
				}
				else if (content[newFrom] != OBJECT_STARTER){
					if (CharUtils.compare(content,newFrom,FOR_NULL)) {
						result[0] = null;
						return newFrom + FOR_NULL.length;
					}
					else {
						throw new SyntaxException(0,newFrom,"Boolean array value is missing"); 
					}
				}
				
				try{@SuppressWarnings("unchecked")
				final T		instance = (T) result.getClass().getComponentType().newInstance();
					
					do{	newFrom++;		// The same first loop skips '}', all next - ','
						
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
						if (newFrom < to && content[newFrom] == OBJECT_TERMINATOR) {
							break;
						}
						else if (newFrom < to && content[newFrom] == '\"') {
							final int[]	temp = new int[2];
							
							newFrom = CharUtils.parseUnescapedString(content,newFrom+1,'\"',true,temp);
							
							while (newFrom < to && content[newFrom] != NAME_SPLITTER) {
								newFrom++;
							}
							if (newFrom >= to) {
								throw new SyntaxException(0,newFrom,"Colon is missing");
							}
							else {
								newFrom++;
							}
							
							final long	nameId = names.seekName(content,temp[0],temp[1]+1);
							
							if (nameId < 0) {
								throw new SyntaxException(0,newFrom,"Ref obj unknown field name ["+new String(content,temp[0],temp[1]-temp[0])+"]");
							}
							else {
								final GetterAndSetter	cargo = names.getCargo(nameId);
								final Object[]			pseudoArray = (Object[]) Array.newInstance(cargo.getValueType().isPrimitive() ? getWrapper4(cargo.getValueType()) : cargo.getValueType(),1);
								
								newFrom = cargo.getSerializer().deserialize(content,newFrom,pseudoArray);
								cargo.setValue(instance,pseudoArray[0]);
							}							
						}
						while (newFrom < to && content[newFrom] <= ' ') {
							newFrom++;
						}
					} while (newFrom < to && content[newFrom] == LIST_SPLITTER);
				
					if (newFrom >= to || content[newFrom] != OBJECT_TERMINATOR) {
						throw new SyntaxException(0,newFrom,"Unclosed ref object");
					}
					else {
						result[0] = instance;
						
						return newFrom+1;
					}
				} catch (ContentException | InstantiationException | IllegalAccessException e) {
					throw new SyntaxException(0,newFrom,"Ref obj unknown field name");
				} finally {
				}
			}
		}

		@Override
		public T deserialize(JsonStaxParser reader) throws ContentException, SyntaxException {
			if (reader == null) {
				throw new NullPointerException("Reader can't be null");
			}
			else {
				switch (reader.current()) {
					case START_OBJECT	:
						final char[]		buffer = forNames.allocate();
						
						try{final T			instance = (T) contentType.newInstance();
							
							do{	if (reader.hasNext()) {
									reader.next();
								}
								else {
									break;
								}
								if (reader.current() == JsonStaxParserLexType.END_OBJECT) {
									break;
								}
							
								final int	nameLen = reader.name(buffer,0,buffer.length);
								final long	nameId = names.seekName(buffer,0,nameLen);
									
								if (nameId < 0) {
									throw new SyntaxException(reader.row(),reader.col(),"Ref obj unknown field name ["+new String(buffer,0,nameLen)+"]");
								}
								else {
									final GetterAndSetter	cargo = names.getCargo(nameId);
									
									if (reader.hasNext() && reader.next() == JsonStaxParserLexType.NAME_SPLITTER) {
										if (reader.hasNext()) {
											reader.next();
											cargo.setValue(instance,cargo.getSerializer().deserialize(reader));
										}
										else {
											throw new SyntaxException(reader.row(),reader.col(),"Unwaited EOF in the value");
										}
									}
									else {
										throw new SyntaxException(reader.row(),reader.col(),"Ref obj ':' is missing");
									}
								}							
							} while (reader.current() == JsonStaxParserLexType.LIST_SPLITTER);
						
							return instance;
						} catch (IOException | InstantiationException | IllegalAccessException e) {
							throw new SyntaxException(reader.row(),reader.col(),"Ref obj unknown field name",e);
						} finally {
							forNames.free(buffer);
						}
					case NULL_VALUE		:
						reader.next();
						return null;
					default : throw new SyntaxException(reader.row(),reader.col(),"Illegal lexema ["+reader.current()+"] in the stream. Null or array awaited");
				}
			}
		}
		
		private Class<?> getWrapper4(final Class<?> valueType) {
			if (valueType == boolean.class) {
				return Boolean.class;
			}
			else if (valueType == byte.class) {
				return Byte.class;
			}
			else if (valueType == char.class) {
				return Character.class;
			}
			else if (valueType == double.class) {
				return Double.class;
			}
			else if (valueType == float.class) {
				return Float.class;
			}
			else if (valueType == int.class) {
				return Integer.class;
			}
			else if (valueType == long.class) {
				return Long.class;
			}
			else if (valueType == short.class) {
				return Short.class;
			}
			else {
				throw new UnsupportedOperationException("Primitive type wrapper for ["+valueType+"] is not supported yet"); 
			}
		}

		private class InnerWalker implements Walker<GetterAndSetter> {
			final Object	instance;
			final char[]	content;
			final int		to;
			char			symbol = OBJECT_STARTER;
			int				newFrom;
			boolean			needFill;

			private InnerWalker(final Object instance, final char[] content, final int from, final boolean reallyFilled) {
				this.instance = instance;
				this.content = content;
				this.to= content.length;
				this.newFrom = from;
				this.needFill = reallyFilled;
			}
			
			@Override
			public boolean process(final char[] name, final int len, final long id, final GetterAndSetter cargo) {
				try{printChar(symbol);
					printChar(STRING_TERMINATOR);
					if ((newFrom = CharUtils.printEscapedCharArray(content,newFrom,name,0,len,needFill,false)) < 0) {
						newFrom = -newFrom;
						needFill = false;
					}
					printChar(STRING_TERMINATOR);
					printChar(NAME_SPLITTER);
					switch (cargo.getTypeSort()) {
						case NOT_PRIMITIVE		:
							if ((newFrom = cargo.getSerializer().serialize(cargo.getValue(instance),content,newFrom,needFill)) < 0) {
								newFrom = -newFrom;
								needFill = false;
							}
							break;
						case PRIMITIVE_BOOLEAN	:
						case PRIMITIVE_BYTE		:
						case PRIMITIVE_CHAR		:
						case PRIMITIVE_DOUBLE	:
						case PRIMITIVE_FLOAT	:
						case PRIMITIVE_INT		:
						case PRIMITIVE_LONG		:
						case PRIMITIVE_SHORT	:
							if ((newFrom = cargo.getSerializer().serialize(cargo.getValue(instance),content,newFrom,needFill)) < 0) {
								newFrom = -newFrom;
								needFill = false;
							}
							break;
						default : throw new UnsupportedOperationException("Type sort ["+cargo.getTypeSort()+"] is not supported yet");
					}
					symbol = LIST_SPLITTER;
				} catch (ContentException e) {
					e.printStackTrace();
				}
				return true;
			}
			
			private void printChar(final char symbol) {
				if (newFrom < to) {
					if (needFill) {
						content[newFrom] = symbol;
					}
				}
				else {
					needFill = false;
				}
				newFrom++;
			}
		}
	}

	private static void printBoolean(final boolean value, final CharacterTarget target) throws PrintingException {
		target.put(value ? TRUE_VALUE : FALSE_VALUE);
	}

	private static int printBoolean(final boolean value, final char[] content, final int from, final boolean reallyFilled) {
		return printCharArray(content,from,value ? TRUE_VALUE : FALSE_VALUE,reallyFilled);
	}
	
	private static void printBoolean(final boolean value, final JsonStaxPrinter printer) throws PrintingException, IOException {
		printer.value(value);
	}

	private static void printLong(final long value, final char[] temporaryContent, final CharacterTarget target) throws PrintingException {
		final int 	len = CharUtils.printLong(temporaryContent,0, value,true);
		
		target.put(temporaryContent,0,len);
	}
	
	private static int printLong(final long value, final char[] content, final int from, final boolean reallyFilled) {
		return CharUtils.printLong(content,from, value,true);
	}

	private static void printLong(final long value, final JsonStaxPrinter printer) throws PrintingException, IOException {
		printer.value(value);
	}

	private static void printDouble(final double value, final char[] temporaryContent, final CharacterTarget target) throws PrintingException {
		final int 	len = CharUtils.printDouble(temporaryContent,0, value,true);
		
		target.put(temporaryContent,0,len);
	}
	
	private static int printDouble(final double value, final char[] content, final int from, final boolean reallyFilled) {
		return CharUtils.printDouble(content,from, value,true);
	}

	private static void printDouble(final double value, final JsonStaxPrinter printer) throws PrintingException, IOException {
		printer.value(value);
	}
}
