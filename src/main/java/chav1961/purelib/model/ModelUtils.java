package chav1961.purelib.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.ORMSerializer;
import chav1961.purelib.streams.char2byte.AsmWriter;


/**
 * <p>This utility class supports most of useful operations with models.</p>
 * @see chav1961.purelib.model.interfaces
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class ModelUtils {
	private static final AsmWriter		writer;
	private static final IOException	initExc;
	private static final AtomicInteger	uniqueSuffix = new AtomicInteger(0);
	
	static {
		AsmWriter	temp;
		IOException	tempExc;
		
		try{temp = new AsmWriter(new OutputStream(){@Override public void write(int b) throws IOException {}},new OutputStreamWriter(System.err));
			try(final InputStream	is = ModelUtils.class.getResourceAsStream("macros.txt");
				final Reader		rdr = new InputStreamReader(is)) {
				
				Utils.copyStream(rdr, temp);
			}
			tempExc = null;
		} catch (IOException exc) {
			temp = null;
			tempExc = exc;
		}
		writer = temp;
		initExc = tempExc;
	}
	
	public static <Key,Content> ORMSerializer<Key,Content> buildORMSerializer(final ContentMetadataInterface metadata, final ClassLoader deploy) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			// TODO:
			return null;
		}
	}

	public static <Key,Content> ORMSerializer<Key,Content> buildResultsetSerializer(final ContentMetadataInterface metadata, final Class<Key> keyClass, final Class<Content> contentClass, final ClassLoader deploy) {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (keyClass == null) {
			throw new NullPointerException("Key class can't be null");
		}
		else if (contentClass == null) {
			throw new NullPointerException("Content class can't be null");
		}
		else if (deploy == null) {
			throw new NullPointerException("Loader to deploy to can't be null");
		}
		else if (initExc != null) {
			throw new PreparationException("Class ["+ModelUtils.class.getCanonicalName()+"]: error during static initialization: ("+initExc.getLocalizedMessage()+"). This class can't be used before solving problems",initExc);
		}
		else {
			// TODO:
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final Writer				wr = writer.clone(baos)) {
//				final int					suffix = uniqueSuffix.incrementAndGet();
				
				wr.write(" makeIncludes \""+keyClass.getCanonicalName()+"\",\""+keyClass.getCanonicalName()+"\",\"");
			} catch (IOException e) {
			}
			return null;
		}
	}
	
	/**
	 * <p>Get value from instance by getters and setters</p>
	 * @param instance instance to get value from
	 * @param gas getter and setter to access to instance field
	 * @param metadata mode meta data for the given getter and setter
	 * @return value extracted
	 * @throws NullPointerException if any parameters are null
	 * @throws ContentException on any errors while getting value
	 */
	public static Object getValueByGetter(final Object instance, final GetterAndSetter gas, final ContentNodeMetadata metadata) throws NullPointerException, ContentException {
		if (instance == null) {
			throw new NullPointerException("Object to get value from can't be null");
		}
		else if (gas == null) {
			throw new NullPointerException("Getter&setter can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			if (gas instanceof ObjectGetterAndSetter<?>) {
				return ((ObjectGetterAndSetter<?>)gas).get(instance);
			}
			else if (gas instanceof BooleanGetterAndSetter) {
				return Boolean.valueOf(((BooleanGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof ByteGetterAndSetter) {
				return Byte.valueOf(((ByteGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof CharGetterAndSetter) {
				return Character.valueOf(((CharGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof ShortGetterAndSetter) {
				return Short.valueOf(((ShortGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof IntGetterAndSetter) {
				return Integer.valueOf(((IntGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof LongGetterAndSetter) {
				return Long.valueOf(((LongGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof FloatGetterAndSetter) {
				return Float.valueOf(((FloatGetterAndSetter)gas).get(instance));
			}
			else if (gas instanceof DoubleGetterAndSetter) {
				return Double.valueOf(((DoubleGetterAndSetter)gas).get(instance));
			}
			else {
				throw new UnsupportedOperationException("Getter&setter type ["+gas.getClass().getSimpleName()+"] is not supported yet");
			}
		}
	}

	/**
	 * <p>Store value into instance by getters and setters</p>
	 * @param instance instance to store value to
	 * @param value value to store
	 * @param gas getter and setter to access to instance field
	 * @param metadata mode meta data for the given getter and setter
	 * @throws NullPointerException if any parameters are null
	 * @throws IllegalArgumentException if value to store is incompatible with target setter 
	 * @throws ContentException on any errors while setting value
	 */
	public static void setValueBySetter(final Object instance, final Object value, final GetterAndSetter gas, final ContentNodeMetadata metadata) throws ContentException, NullPointerException, IllegalArgumentException {
		if (instance == null) {
			throw new NullPointerException("Object to get value from can't be null");
		}
		else if (gas == null) {
			throw new NullPointerException("Getter&setter can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			if (gas instanceof ObjectGetterAndSetter) {
				((ObjectGetterAndSetter)gas).set(instance,value);
			}
			else if (gas instanceof BooleanGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Boolean)) {
					throw new IllegalArgumentException("Value to assign to primitive boolean type must be [Boolean], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((BooleanGetterAndSetter)gas).set(instance,((Boolean)value).booleanValue());
				}
			}
			else if (gas instanceof ByteGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Byte)) {
					throw new IllegalArgumentException("Value to assign to primitive byte type must be [Byte], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((ByteGetterAndSetter)gas).set(instance,((Byte)value).byteValue());
				}
			}
			else if (gas instanceof CharGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Character)) {
					throw new IllegalArgumentException("Value to assign to primitive char type must be [Character], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((CharGetterAndSetter)gas).set(instance,((Character)value).charValue());
				}
			}
			else if (gas instanceof ShortGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Short)) {
					throw new IllegalArgumentException("Value to assign to primitive short type must be [Short], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((ShortGetterAndSetter)gas).set(instance,((Short)value).shortValue());
				}
			}
			else if (gas instanceof IntGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Integer)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Integer], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((IntGetterAndSetter)gas).set(instance,((Integer)value).intValue());
				}
			}
			else if (gas instanceof LongGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Long)) {
					throw new IllegalArgumentException("Value to assign to primitive int type must be [Long], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((LongGetterAndSetter)gas).set(instance,((Long)value).longValue());
				}
			}
			else if (gas instanceof FloatGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Float)) {
					throw new IllegalArgumentException("Value to assign to primitive float type must be [Float], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((FloatGetterAndSetter)gas).set(instance,((Float)value).floatValue());
				}
			}
			else if (gas instanceof DoubleGetterAndSetter) {
				if (value == null) {
					throw new NullPointerException("Value to assign to primitive type can't be null");
				}
				else if (!(value instanceof Double)) {
					throw new IllegalArgumentException("Value to assign to primitive double type must be [Double], not ["+value.getClass().getCanonicalName()+"]");
				}
				else {
					((DoubleGetterAndSetter)gas).set(instance,((Double)value).doubleValue());
				}
			}
			else {
				throw new UnsupportedOperationException("Getter&setter type ["+gas.getClass().getSimpleName()+"] is not supported yet");
			}
		}
	}
}
