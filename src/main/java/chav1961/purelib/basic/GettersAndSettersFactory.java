package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.char2byte.CompilerUtils;

/**
 * <p>This class contains a factory method to build getters and setters for primitive and referenced items in the class and/or it's instance. All the 
 * getters an setters implements marker interface {@linkplain GetterAndSetter} that can be used as the type of variable to store getters and setters built. 
 * All the getters and setters are not extends any common parent class, so can't be converted each other. Getters and setters support either primitive data 
 * type or referenced data type. Each primitive type has own specific getter and setter to avoid wrapping/unwrapping during data access. Total list of the 
 * getters and setters is:</p>
 * <ul>
 * <li>{@link BooleanGetterAndSetter} - allow access to <b>boolean</b> fields in the class or instance</li>
 * <li>{@link ByteGetterAndSetter} - allow access to <b>byte</b> fields in the class or instance</li>
 * <li>{@link CharGetterAndSetter} - allow access to <b>char</b> fields in the class or instance</li>
 * <li>{@link DoubleGetterAndSetter} - allow access to <b>double</b> fields in the class or instance</li>
 * <li>{@link FloatGetterAndSetter} - allow access to <b>float</b> fields in the class or instance</li>
 * <li>{@link IntGetterAndSetter} - allow access to <b>int</b> fields in the class or instance</li>
 * <li>{@link LongGetterAndSetter} - allow access to <b>long</b> fields in the class or instance</li>
 * <li>{@link ShortGetterAndSetter} - allow access to <b>short</b> fields in the class or instance</li>
 * <li>{@link ObjectGetterAndSetter} - allow access to referenced fields in the class or instance</li>
 * </ul>
 * <p>Every getter and setter contains two methods:</p>
 * <ul>
 * <li>get(T instance) - get field value from the class or instance</li>
 * <li>set(T instance, V value) - set field value in the class or instance</li>
 * </ul>
 * <p>According to configuration parameters and field properties, getters and setters can be implemented as:</p>
 * <ul>
 * <li>hard-coded on-the-fly built inner class (use {@linkplain AsmWriter}) functionality</li>
 * <li>wrapper to the {@linkplain MethodHandle} functionality</li>
 * </ul>
 * <p>The first one is implemented for the public fields only. Other cases produce the second variant.</p>
 * <p>Since Java 1.9, PureLibSettings.ALLOW_UNSAFE flag in the Pure Library configuration is deprecated, so any references to
 * sun.misc.Unsafe class in this class are invalid now.</p>
 * 
 * @see sun.misc.Unsafe
 * @see chav1961.purelib.basic.PureLibSettings
 * @see chav1961.purelib.streams.char2byte.AsmWriter
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.4
 */

public class GettersAndSettersFactory {
//	private static sun.misc.Unsafe		unsafe;
	private static AsmWriter			writer;
	private static Map<String,Map<String,GetterAndSetter>>	gettersAndSettersCache = new HashMap<>();
	
	static {
		prepareStatic();
	}

	private static void prepareStatic() {
//		if (PureLibSettings.instance().getProperty(PureLibSettings.ALLOW_UNSAFE,boolean.class,"false")) {
//			try{final Field	f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
//			
//				f.setAccessible(true);
//				unsafe = (Unsafe) f.get(null);
//			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//				PureLibSettings.logger.log(Level.WARNING,"["+PureLibSettings.ALLOW_UNSAFE+"] property was typed for the Pure Library, but attempt to get access to Unsafe functionality failed: "+e.getMessage()+". This ability will be ignored", e);
//				unsafe = null;
//			}
//		} 
//		else {
//			unsafe = null;
//		}
		
		try{final AsmWriter			tempWriter = new AsmWriter(new ByteArrayOutputStream(),new OutputStreamWriter(System.err));
		
			try(final InputStream	is = GettersAndSettersFactory.class.getResourceAsStream("gettersandsettersmacros.txt");
				final Reader		rdr = new InputStreamReader(is)) {
				
				Utils.copyStream(rdr,tempWriter);
			}
			writer = tempWriter;
		} catch (NullPointerException | IOException e) {
			PureLibSettings.CURRENT_LOGGER.message(Severity.warning,"Attempt to get AsmWriter functionality failed: "+e.getMessage()+". This problem will reduce performance for getters and setters functionality", e);
			writer = null;
		}
	}
	
	public static void clearCache() {
		synchronized(gettersAndSettersCache) {
			gettersAndSettersCache.clear();
		}
	}

	/**
	 * <p>This interface can be used as type of variable to store any getter and setter instance. All getters and setters implement 
	 * this interface, so all of them can be casted to it</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @lastUpdate 0.0.4
	 */
	public interface GetterAndSetter {
		int getClassType();
	}

	/**
	 * <p>This interface can be used as a type of variable to store any instantiator.</p> 
	 * @author achernomyrdin
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 * @param <T> any class to allocate memory for
	 */
	public interface Instantiator<T> {
		/**
		 * <p>Get managed class fpr the given interface</p>
		 * @return managed class. Can't be null
		 */
		Class<T> getType();
		
		/**
		 * <p>Create class instance without calling any constructors</p>
		 * @return instance created
		 * @throws InstantiationException when instantiation failed
		 */
		T newInstance() throws InstantiationException;
		
		/**
		 * <p>Allocate array to store instance if the given class</p>
		 * @param size array size to allocate
		 * @return array allocated
		 */
		T[] newArray(int size);
	}
	
	/**
	 * <p>This class implements getter and setter to the primitive boolean field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class BooleanGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive boolean</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract boolean get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive boolean</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final boolean value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the primitive byte field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class ByteGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive byte</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract byte get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive byte</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final byte value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the primitive char field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class CharGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive char</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract char get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive char</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final char value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the primitive double field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class DoubleGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive double</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract double get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive double</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final double value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the primitive float field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class FloatGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive float</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract float get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive float</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final float value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the primitive int field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class IntGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive int</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract int get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive int</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final int value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the primitive long field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class LongGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive long</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract long get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive long</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final long value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the primitive short field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class ShortGetterAndSetter implements GetterAndSetter {
		/**
		 * <p>Get field value as primitive short</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract short get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as primitive short</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final short value) throws ContentException;
	}

	/**
	 * <p>This class implements getter and setter to the referenced field in the class or instance</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public static abstract class ObjectGetterAndSetter<T> implements GetterAndSetter {
		/**
		 * <p>Get field value as referenced</p>
		 * @param instance instance to get value from. Is the field is static, this parameter will be ignored 
		 * @return boolean value for the given field
		 * @throws ContentException on any access errors
		 */
		public abstract T get(final Object instance) throws ContentException;
		
		/**
		 * <p>Set field value as referenced</p>
		 * @param instance instance to set value in. Is the field is static, this parameter will be ignored
		 * @param value value to set to the field
		 * @throws ContentException on any access errors
		 */
		public abstract void set(final Object instance, final T value) throws ContentException;
	}

	protected static abstract class InstantiatorImpl<T> implements Instantiator<T> {
		protected final Class<T> 	clazz;
		
		public InstantiatorImpl(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		public Class<T> getType() {
			return clazz;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T[] newArray(int size) {
			return (T[])Array.newInstance(getType(),size);
		}
	}
	
	/**
	 * <p>Build getter and setter by it's application path in model.</p>
	 * @param applicationPath application path URI in the model
	 * @return getter and setter built
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class is null
	 * @since 0.0.3
	 */
	public static GetterAndSetter buildGetterAndSetter(final URI applicationPath) throws ContentException, IllegalArgumentException, NullPointerException {
		return buildGetterAndSetter(applicationPath,Thread.currentThread().getContextClassLoader());
	}

	/**
	 * <p>Build getter and setter by it's application path in model for specific class loader.</p>
	 * @param applicationPath application path URI in the model
	 * @param loader class loader to seek class in
	 * @return getter and setter built
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class is null
	 * @since 0.0.3
	 */
	public static GetterAndSetter buildGetterAndSetter(final URI applicationPath, final ClassLoader loader) throws ContentException, IllegalArgumentException, NullPointerException {
		if (applicationPath == null) {
			throw new NullPointerException("Application path URI can't be null");
		}
		else if (loader == null) {
			throw new NullPointerException("Class loader URI can't be null");
		}
		else if (!ContentMetadataInterface.APPLICATION_SCHEME.equals(applicationPath.getScheme())) {
			throw new IllegalArgumentException("Illegal scheme ["+applicationPath.getScheme()+"] for application path, must be ["+ContentMetadataInterface.APPLICATION_SCHEME+"]");
		}
		else {
			final URI	subScheme = URI.create(applicationPath.getSchemeSpecificPart());
			
			if (Constants.MODEL_APPLICATION_SCHEME_FIELD.equals(subScheme.getScheme())) {
				final String[]	parts = CharUtils.split(subScheme.getPath(),'/');
				
				if (parts.length != 3) {
					throw new IllegalArgumentException("Illegal path format ["+subScheme.getPath()+"] for getters/setters, must be [/<class>/<field_name>]");
				}
				
				try{return buildGetterAndSetter(loader.loadClass(parts[1]),parts[2]);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Class ["+parts[1]+"] is not known in the given class loader");
				}
			}
			else {
				throw new IllegalArgumentException("Illegal subscheme ["+subScheme.getScheme()+"] for getters/setters, must be ["+Constants.MODEL_APPLICATION_SCHEME_FIELD+"]");
			}
		}
	}
	
	/**
	 * <p>Build getter and setter for the given field in the class or instance.</p>
	 * @param <T> instance to build getter and setter for
	 * @param awaited class containing field to get access to
	 * @param fieldName field name to get access to
	 * @param assigner module assigner for correct working in Java 1.9 and later
	 * @return getter and setter built
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class is null
	 */
	public static <T> GetterAndSetter buildGetterAndSetter(final Class<T> awaited, final String fieldName) throws ContentException, IllegalArgumentException, NullPointerException {
		return buildGetterAndSetter(awaited, fieldName,(m)->{});
	}
	
	/**
	 * <p>Build getter and setter for the given field in the class or instance.</p>
	 * @param <T> instance to build getter and setter for
	 * @param awaited class containing field to get access to
	 * @param fieldName field name to get access to
	 * @param assigner module assigner to allow cross-module access in Java 1.9 and higher
	 * @return getter and setter built
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class is null
	 * @since 0.0.4
	 */
	public static <T> GetterAndSetter buildGetterAndSetter(final Class<T> awaited, final String fieldName, final ModuleAccessor assigner) throws ContentException, IllegalArgumentException, NullPointerException {
		return buildGetterAndSetter(awaited, fieldName, assigner, PureLibSettings.INTERNAL_LOADER);
	}
	
	/**
	 * <p>Build getter and setter for the given field in the class or instance.</p>
	 * @param <T> instance to build getter and setter for
	 * @param awaited class containing field to get access to
	 * @param fieldName field name to get access to
	 * @param assigner module assigner to allow cross-module access in Java 1.9 and higher
	 * @param loader loader to create on-the-fly class in
	 * @return getter and setter built
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class, assigner or loader is null
	 * @since 0.0.4
	 */
	public static <T> GetterAndSetter buildGetterAndSetter(final Class<T> awaited, final String fieldName, final ModuleAccessor assigner, final SimpleURLClassLoader loader) throws ContentException, IllegalArgumentException, NullPointerException {
		if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null");
		}
		else if (fieldName == null || fieldName.isEmpty()) {
			throw new NullPointerException("Field name can't be null or empty");
		}
		else if (assigner == null) {
			throw new NullPointerException("Assigner class can't be null");
		}
		else if (loader == null) {
			throw new NullPointerException("Loader class can't be null");
		}
		else {
			synchronized (gettersAndSettersCache) {
				Map<String,GetterAndSetter>	clazz = gettersAndSettersCache.get(awaited.getCanonicalName());
				
				if (clazz == null) {
					gettersAndSettersCache.put(awaited.getCanonicalName(),clazz = new HashMap<>());
				}
				
				final GetterAndSetter	item = clazz.get(fieldName);
					
				if (item != null) {
					return item;
				}
			}
			final GetterAndSetter	gas;
			
			final Field		f = extractFieldInfo(awaited,awaited,fieldName);
			final Class<?>	fType = f.getType();

			try{if (Modifier.isPublic(f.getModifiers())) {
					if (fType.isPrimitive()) {
//						if (unsafe != null) {
//							if (Modifier.isStatic(f.getModifiers())) {
//								gas = buildPrimitiveUnsafeStatic(unsafe,f,fType);
//							}
//							else {
//								gas = buildPrimitiveUnsafeInstance(unsafe,f,fType);
//							}
//						}
//						else 
						if (writer != null) {
							if (Modifier.isStatic(f.getModifiers())) {
								gas = buildPrimitiveAsmStatic(writer,awaited,f,fType,assigner,loader);
							}
							else {
								gas = buildPrimitiveAsmInstance(writer,awaited,f,fType,assigner,loader);
							}
						}
						else {
							if (Modifier.isStatic(f.getModifiers())) {
								gas = buildPrimitiveHandleStatic(f,fType);
							}
							else {
								gas = buildPrimitiveHandleInstance(f,fType);
							}
						}
					}
					else {
//						if (unsafe != null) {
//							if (Modifier.isStatic(f.getModifiers())) {
//								gas = buildReferencedUnsafeStatic(unsafe,f,fType);
//							}
//							else {
//								gas = buildReferencedUnsafeInstance(unsafe,f,fType);
//							}
//						}
//						else 
						if (writer != null) {
							if (Modifier.isStatic(f.getModifiers())) {
								gas = buildReferencedAsmStatic(writer,awaited,f,fType,assigner,loader);
							}
							else {
								gas = buildReferencedAsmInstance(writer,awaited,f,fType,assigner,loader);
							}
						}
						else {
							if (Modifier.isStatic(f.getModifiers())) {
								gas = buildReferencedHandleStatic(f,fType);
							}
							else {
								gas = buildReferencedHandleInstance(f,fType);
							}
						}
					}
				}
				else {
					if (fType.isPrimitive()) {
//						if (unsafe != null) {
//							if (Modifier.isStatic(f.getModifiers())) {
//								gas = buildPrimitiveUnsafeStatic(unsafe,f,fType);
//							}
//							else {
//								gas = buildPrimitiveUnsafeInstance(unsafe,f,fType);
//							}
//						}
//						else 
						{
							if (Modifier.isStatic(f.getModifiers())) {
								gas = buildPrimitiveHandleStatic(f,fType);
							}
							else {
								gas = buildPrimitiveHandleInstance(f,fType);
							}
						}
					}
					else {
//						if (unsafe != null) {
//							if (Modifier.isStatic(f.getModifiers())) {
//								gas = buildReferencedUnsafeStatic(unsafe,f,fType);
//							}
//							else {
//								gas = buildReferencedUnsafeInstance(unsafe,f,fType);
//							}
//						}
//						else 
						{
							if (Modifier.isStatic(f.getModifiers())) {
								gas = buildReferencedHandleStatic(f,fType);
							}
							else {
								gas = buildReferencedHandleInstance(f,fType);
							}
						}
					}
				}
			} catch (IOException exc) {
				throw new ContentException(exc.getLocalizedMessage(),exc);
			}
			synchronized (gettersAndSettersCache) {
				gettersAndSettersCache.get(awaited.getCanonicalName()).put(fieldName,gas);
				return gas;
			}
		}
	}

	/**
	 * <p>Build instantiator of the class.</p>
	 * @param <T> class to instantiate
	 * @param clazz managed class to build instantiator to
	 * @return instantiator for managed class
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class is null
	 * @throws IllegalArgumentException awaited class is not valid
	 * @throws IllegalStateException awaited class is not public and sun.misc.Unsafe is not available
	 * @since 0.0.3
	 */
	public static <T> Instantiator<T> buildInstantiator(final Class<T> clazz) throws ContentException, IllegalArgumentException, NullPointerException, IllegalStateException {
		return buildInstantiator(clazz,(m)->{});
	}
	
	/**
	 * <p>Build instantiator of the class.</p>
	 * @param <T> class to instantiate
	 * @param clazz managed class to build instantiator to
	 * @param assigner module assigner for correct working in Java 1.9 and later
	 * @return instantiator for managed class
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class is null
	 * @throws IllegalArgumentException awaited class is not valid
	 * @throws IllegalStateException awaited class is not public and sun.misc.Unsafe is not available
	 * @since 0.0.4
	 */
	public static <T> Instantiator<T> buildInstantiator(final Class<T> clazz, final ModuleAccessor assigner) throws ContentException, IllegalArgumentException, NullPointerException, IllegalStateException {
		return buildInstantiator(clazz,assigner,PureLibSettings.INTERNAL_LOADER);
	}
	
	/**
	 * <p>Build instantiator of the class.</p>
	 * @param <T> class to instantiate
	 * @param clazz managed class to build instantiator to
	 * @param assigner module assigner for correct working in Java 1.9 and later
	 * @param loader loader to create on-the-fly class in
	 * @return instantiator for managed class
	 * @throws ContentException on any building errors
	 * @throws IllegalArgumentException field name is null, empty or is missing in the class
	 * @throws NullPointerException awaited class is null
	 * @throws IllegalArgumentException awaited class is not valid
	 * @throws IllegalStateException awaited class is not public and sun.misc.Unsafe is not available
	 * @since 0.0.4
	 */
	public static <T> Instantiator<T> buildInstantiator(final Class<T> clazz, final ModuleAccessor assigner, final SimpleURLClassLoader loader) throws ContentException, IllegalArgumentException, NullPointerException, IllegalStateException {
		if (clazz == null) {
			throw new NullPointerException("Class to build instantitor for can't be null"); 
		}
		else if (assigner == null) {
			throw new NullPointerException("Module assigner can't be null"); 
		}
		else if (loader == null) {
			throw new NullPointerException("Loader can't be null"); 
		}
		else if (clazz.isPrimitive() || clazz.isArray()) {
			throw new IllegalArgumentException("Class to build instantitor for can't be primitive or array"); 
		}
		else if (Modifier.isPublic(clazz.getModifiers())) {
			try{final String 	className = clazz.getSimpleName()+"$instantiator";
				
				return buildCode(writer,clazz,className,assigner,loader);
			} catch (IOException exc) {
				throw new ContentException(exc.getLocalizedMessage(),exc);
			}
		} 
//		else if (unsafe != null) {
//			return new InstantiatorImpl<T>(clazz){
//				@SuppressWarnings("unchecked")
//				@Override
//				public T newInstance() throws InstantiationException {
//					return (T)unsafe.allocateInstance(clazz);
//				}}; 
//		}
		else {
			throw new IllegalStateException("Class to build instantitor for is not public, but sun.misc.Unsafe functionality is not available to build memory allocation code.");
		}
	}
	
	private static Field extractFieldInfo(final Class<?> awaited, final Class<?> forError, final String fieldName) {
		if (awaited != null) {
			for (Field f : awaited.getDeclaredFields() ) {
				if (fieldName.equals(f.getName())) {
					return f;
				}
			}
			return extractFieldInfo(awaited.getSuperclass(),forError,fieldName);
		}
		else {
			throw new IllegalArgumentException("Field name ["+fieldName+"] is missing in the class ["+forError+"]");
		}
	}
	
//	private static GetterAndSetter buildPrimitiveUnsafeStatic(final Unsafe unsafe, final Field f, final Class<?> fType) {
//		final Class<?>	container = f.getDeclaringClass();
//		final long		displ = unsafe.staticFieldOffset(f);
//		
//		switch (CompilerUtils.defineClassType(fType)) {
//			case CompilerUtils.CLASSTYPE_BOOLEAN	:
//				return new BooleanGetterAndSetter() {
//					@Override public boolean get(final Object instance) throws ContentException {return unsafe.getBoolean(container,displ);}
//					@Override public void set(final Object instance, final boolean value) throws ContentException {unsafe.putBoolean(container,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_BYTE		: 
//				return new ByteGetterAndSetter() {
//					@Override public byte get(final Object instance) throws ContentException {return unsafe.getByte(container,displ);}
//					@Override public void set(final Object instance, final byte value) throws ContentException {unsafe.putByte(container,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_CHAR		:
//				return new CharGetterAndSetter() {
//					@Override public char get(final Object instance) throws ContentException {return unsafe.getChar(container,displ);}
//					@Override public void set(final Object instance, final char value) throws ContentException {unsafe.putChar(container,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_DOUBLE		:
//				return new DoubleGetterAndSetter() {
//					@Override public double get(final Object instance) throws ContentException {return unsafe.getDouble(container,displ);}
//					@Override public void set(final Object instance, final double value) throws ContentException {unsafe.putDouble(container,displ,value);}					
//				};
//			case CompilerUtils.CLASSTYPE_FLOAT		:
//				return new FloatGetterAndSetter() {
//					@Override public float get(final Object instance) throws ContentException {return unsafe.getFloat(container,displ);}
//					@Override public void set(final Object instance, final float value) throws ContentException {unsafe.putFloat(container,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_INT		:
//				return new IntGetterAndSetter() {
//					@Override public int get(final Object instance) throws ContentException {return unsafe.getInt(container,displ);}
//					@Override public void set(final Object instance, final int value) throws ContentException {unsafe.putInt(container,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_LONG		:
//				return new LongGetterAndSetter() {
//					@Override public long get(final Object instance) throws ContentException {return unsafe.getLong(container,displ);}
//					@Override public void set(final Object instance, final long value) throws ContentException {unsafe.putLong(container,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_SHORT		:
//				return new ShortGetterAndSetter() {
//					@Override public short get(final Object instance) throws ContentException {return unsafe.getShort(container,displ);}
//					@Override public void set(final Object instance, final short value) throws ContentException {unsafe.putShort(container,displ,value);}
//				};
//			default : throw new UnsupportedOperationException("Primitive type ["+fType+"] is not supported");  
//		}
//	}

//	private static GetterAndSetter buildPrimitiveUnsafeInstance(final Unsafe unsafe, final Field f, final Class<?> fType) {
//		final long	displ = unsafe.objectFieldOffset(f);
//		
//		switch (CompilerUtils.defineClassType(fType)) {
//			case CompilerUtils.CLASSTYPE_BOOLEAN	:
//				return new BooleanGetterAndSetter() {
//					@Override public boolean get(final Object instance) throws ContentException {return unsafe.getBoolean(instance,displ);}
//					@Override public void set(final Object instance, final boolean value) throws ContentException {unsafe.putBoolean(instance,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_BYTE		:
//				return new ByteGetterAndSetter() {
//					@Override public byte get(final Object instance) throws ContentException {return unsafe.getByte(instance,displ);}
//					@Override public void set(final Object instance, final byte value) throws ContentException {unsafe.putByte(instance,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_CHAR		:
//				return new CharGetterAndSetter() {
//					@Override public char get(final Object instance) throws ContentException {return unsafe.getChar(instance,displ);}
//					@Override public void set(final Object instance, final char value) throws ContentException {unsafe.putChar(instance,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_DOUBLE		:
//				return new DoubleGetterAndSetter() {
//					@Override public double get(final Object instance) throws ContentException {return unsafe.getDouble(instance,displ);}
//					@Override public void set(final Object instance, final double value) throws ContentException {unsafe.putDouble(instance,displ,value);}					
//				};
//			case CompilerUtils.CLASSTYPE_FLOAT		:
//				return new FloatGetterAndSetter() {
//					@Override public float get(final Object instance) throws ContentException {return unsafe.getFloat(instance,displ);}
//					@Override public void set(final Object instance, final float value) throws ContentException {unsafe.putFloat(instance,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_INT		:
//				return new IntGetterAndSetter() {
//					@Override public int get(final Object instance) throws ContentException {return unsafe.getInt(instance,displ);}
//					@Override public void set(final Object instance, final int value) throws ContentException {unsafe.putInt(instance,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_LONG		:
//				return new LongGetterAndSetter() {
//					@Override public long get(final Object instance) throws ContentException {return unsafe.getLong(instance,displ);}
//					@Override public void set(final Object instance, final long value) throws ContentException {unsafe.putLong(instance,displ,value);}
//				};
//			case CompilerUtils.CLASSTYPE_SHORT		:
//				return new ShortGetterAndSetter() {
//					@Override public short get(final Object instance) throws ContentException {return unsafe.getShort(instance,displ);}
//					@Override public void set(final Object instance, final short value) throws ContentException {unsafe.putShort(instance,displ,value);}
//				};
//			default : throw new UnsupportedOperationException("Primitive type ["+fType+"] is not supported");  
//		}
//	}

	private static GetterAndSetter buildPrimitiveAsmStatic(final AsmWriter writer, final Class<?> owner, final Field f, final Class<?> fType, final ModuleAccessor assigner, final SimpleURLClassLoader loader) throws IOException {
		final String 	className = owner.getSimpleName()+"$"+f.getName();
		final String	ownerName = owner.getCanonicalName() != null ? owner.getCanonicalName() : owner.getName();  

		switch (CompilerUtils.defineClassType(fType)) {
			case CompilerUtils.CLASSTYPE_BOOLEAN	:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticBoolean className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_BYTE		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticByte className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_CHAR		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticChar className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_DOUBLE		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticDouble className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_FLOAT		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticFloat className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_INT		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticInt className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_LONG		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticLong className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_SHORT		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticShort className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			default : throw new UnsupportedOperationException("Primitive type ["+fType+"] is not supported");  
		}
	}

	private static GetterAndSetter buildPrimitiveAsmInstance(final AsmWriter writer, final Class<?> owner, final Field f, final Class<?> fType, final ModuleAccessor assigner, final SimpleURLClassLoader loader) throws IOException {
		final String 	className = owner.getSimpleName()+"$"+f.getName();
		final String	ownerName = owner.getCanonicalName() != null ? owner.getCanonicalName() : owner.getName();  

		switch (CompilerUtils.defineClassType(fType)) {
			case CompilerUtils.CLASSTYPE_BOOLEAN	:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceBoolean className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_BYTE		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceByte className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_CHAR		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceChar className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_DOUBLE		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceDouble className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_FLOAT		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceFloat className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_INT		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceInt className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_LONG		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceLong className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			case CompilerUtils.CLASSTYPE_SHORT		:
				return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceShort className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
			default : throw new UnsupportedOperationException("Primitive type ["+fType+"] is not supported");  
		}
	}

	private static GetterAndSetter buildPrimitiveHandleStatic(final Field f, final Class<?> fType) {
		f.setAccessible(true);
		
		try{final MethodHandle	getter = MethodHandles.lookup().unreflectGetter(f);
			final MethodHandle	setter = MethodHandles.lookup().unreflectSetter(f);
		
			switch (CompilerUtils.defineClassType(fType)) {
				case CompilerUtils.CLASSTYPE_BOOLEAN	:
					return new BooleanGetterAndSetter() {
						@Override
						public boolean get(final Object instance) throws ContentException {
							try{return ((Boolean) getter.invoke()).booleanValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final boolean value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_BOOLEAN;
						}
					};
				case CompilerUtils.CLASSTYPE_BYTE		:
					return new ByteGetterAndSetter() {
						@Override
						public byte get(final Object instance) throws ContentException {
							try{return ((Byte) getter.invoke()).byteValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final byte value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_BYTE;
						}
					};
				case CompilerUtils.CLASSTYPE_CHAR		:
					return new CharGetterAndSetter() {
						@Override
						public char get(final Object instance) throws ContentException {
							try{return ((Character) getter.invoke()).charValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final char value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_CHAR;
						}
					};
				case CompilerUtils.CLASSTYPE_DOUBLE		:
					return new DoubleGetterAndSetter() {
						@Override
						public double get(final Object instance) throws ContentException {
							try{return ((Double) getter.invoke()).doubleValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final double value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_DOUBLE;
						}
					};
				case CompilerUtils.CLASSTYPE_FLOAT		:
					return new FloatGetterAndSetter() {
						@Override
						public float get(final Object instance) throws ContentException {
							try{return ((Float) getter.invoke()).floatValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final float value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_FLOAT;
						}
					};
				case CompilerUtils.CLASSTYPE_INT		:
					return new IntGetterAndSetter() {
						@Override
						public int get(final Object instance) throws ContentException {
							try{return ((Integer) getter.invoke()).intValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final int value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_INT;
						}
					};
				case CompilerUtils.CLASSTYPE_LONG		:
					return new LongGetterAndSetter() {
						@Override
						public long get(final Object instance) throws ContentException {
							try{return ((Long) getter.invoke()).longValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final long value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_LONG;
						}
					};
				case CompilerUtils.CLASSTYPE_SHORT		:
					return new ShortGetterAndSetter() {
						@Override
						public short get(final Object instance) throws ContentException {
							try{return ((Short) getter.invoke()).shortValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public void set(final Object instance, final short value) throws ContentException {
							try{setter.invoke(value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_SHORT;
						}
					};
				default : throw new UnsupportedOperationException("Primitive type ["+fType+"] is not supported");  
			}
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage(),e); 
		}
	}

	private static GetterAndSetter buildPrimitiveHandleInstance(final Field f, final Class<?> fType) {
		f.setAccessible(true);
		
		try{final MethodHandle	getter = MethodHandles.lookup().unreflectGetter(f);
			final MethodHandle	setter = MethodHandles.lookup().unreflectSetter(f);
		
			switch (CompilerUtils.defineClassType(fType)) {
				case CompilerUtils.CLASSTYPE_BOOLEAN	:
					return new BooleanGetterAndSetter() {
						@Override
						public boolean get(final Object instance) throws ContentException {
							try{return ((Boolean) getter.invoke(instance)).booleanValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final boolean value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_BOOLEAN;
						}
					};
				case CompilerUtils.CLASSTYPE_BYTE		:
					return new ByteGetterAndSetter() {
						@Override
						public byte get(final Object instance) throws ContentException {
							try{return ((Byte) getter.invoke(instance)).byteValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final byte value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_BYTE;
						}
					};
				case CompilerUtils.CLASSTYPE_CHAR		:
					return new CharGetterAndSetter() {
						@Override
						public char get(final Object instance) throws ContentException {
							try{return ((Character) getter.invoke(instance)).charValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final char value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_CHAR;
						}
					};
				case CompilerUtils.CLASSTYPE_DOUBLE		:
					return new DoubleGetterAndSetter() {
						@Override
						public double get(final Object instance) throws ContentException {
							try{return ((Double) getter.invoke(instance)).doubleValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final double value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_DOUBLE;
						}
					};
				case CompilerUtils.CLASSTYPE_FLOAT		:
					return new FloatGetterAndSetter() {
						@Override
						public float get(final Object instance) throws ContentException {
							try{return ((Float) getter.invoke(instance)).floatValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final float value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_FLOAT;
						}
					};
				case CompilerUtils.CLASSTYPE_INT		:
					return new IntGetterAndSetter() {
						@Override
						public int get(final Object instance) throws ContentException {
							try{return ((Integer) getter.invoke(instance)).intValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final int value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_INT;
						}
					};
				case CompilerUtils.CLASSTYPE_LONG		:
					return new LongGetterAndSetter() {
						@Override
						public long get(final Object instance) throws ContentException {
							try{return ((Long) getter.invoke(instance)).longValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final long value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_LONG;
						}
					};
				case CompilerUtils.CLASSTYPE_SHORT		:
					return new ShortGetterAndSetter() {
						@Override
						public short get(final Object instance) throws ContentException {
							try{return ((Short) getter.invoke(instance)).shortValue();
							} catch (Throwable e) {
								throw new ContentException("Exception getting read access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}
	
						@Override
						public void set(final Object instance, final short value) throws ContentException {
							try{setter.invoke(instance,value);
							} catch (Throwable e) {
								throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] type ["+fType+"]: "+e.getLocalizedMessage(),e);
							}
						}

						@Override
						public int getClassType() {
							return CompilerUtils.CLASSTYPE_SHORT;
						}
					};
				default : throw new UnsupportedOperationException("Primitive type ["+fType+"] is not supported");  
			}
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage(),e); 
		}
	}
	
//	private static <T> GetterAndSetter buildReferencedUnsafeStatic(final Unsafe unsafe, final Field f, final Class<T> fType) {
//		final Class<?>	container = f.getDeclaringClass();
//		final long		displ = unsafe.staticFieldOffset(f);
//		
//		return new ObjectGetterAndSetter<T>() {
//			@Override
//			public T get(final Object instance) throws ContentException {
//				return fType.cast(unsafe.getObject(container,displ));
//			}
//
//			@Override
//			public void set(final Object instance, final T value) throws ContentException {
//				unsafe.putObject(container,displ,value);
//			}
//		};
//	}

//	private static <T> GetterAndSetter buildReferencedUnsafeInstance(final Unsafe unsafe, final Field f, final Class<T> fType) {
//		final long	displ = unsafe.objectFieldOffset(f);
//		
//		return new ObjectGetterAndSetter<T>() {
//			@Override
//			public T get(final Object instance) throws ContentException {
//				return fType.cast(unsafe.getObject(instance,displ));
//			}
//
//			@Override
//			public void set(final Object instance, final T value) throws ContentException {
//				unsafe.putObject(instance,displ,value);
//			}
//		};
//	}

	private static GetterAndSetter buildReferencedAsmStatic(final AsmWriter writer, final Class<?> owner, final Field f, final Class<?> fType, final ModuleAccessor assigner, final SimpleURLClassLoader loader) throws IOException {
		final String 	className = owner.getSimpleName()+"$"+f.getName();
		final String	ownerName = owner.getCanonicalName() != null ? owner.getCanonicalName() : owner.getName();  

		return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveStaticRef className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
	}

	private static GetterAndSetter buildReferencedAsmInstance(final AsmWriter writer, final Class<?> owner, final Field f, final Class<?> fType, final ModuleAccessor assigner, final SimpleURLClassLoader loader) throws IOException {
		final String 	className = owner.getSimpleName()+"$"+f.getName();
		final String	ownerName = owner.getCanonicalName() != null ? owner.getCanonicalName() : owner.getName();  

		return buildCode(writer,owner,fType,assigner,loader,className," buildPrimitiveInstanceRef className=\"%1$s\",ownerClass=\"%2$s\",fieldName=\"%3$s\",valueClass=\"%4$s\"",className,ownerName,f.getName(),fType.getCanonicalName());
	}

	private static <T> GetterAndSetter buildReferencedHandleStatic(final Field f, final Class<T> fType) {
		f.setAccessible(true);
		
		try{final MethodHandle	getter = MethodHandles.lookup().unreflectGetter(f);
			final MethodHandle	setter = MethodHandles.lookup().unreflectSetter(f);

			return new ObjectGetterAndSetter<T>() {
				@Override
				public T get(final Object instance) throws ContentException {
					try{return fType.cast(getter.invoke());
					} catch (Throwable e) {
						throw new ContentException("Exception getting read access to the field ["+f.getName()+"] in the class ["+fType+"]: "+e.getLocalizedMessage(),e);
					}
				}

				@Override
				public void set(final Object instance, final T value) throws ContentException {
					try{setter.invoke(value);
					} catch (Throwable e) {
						throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] in the class ["+fType+"]: "+e.getLocalizedMessage(),e);
					}
				}

				@Override
				public int getClassType() {
					return CompilerUtils.CLASSTYPE_REFERENCE;
				}
			};
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage(),e); 
		}
	}

	private static <T> GetterAndSetter buildReferencedHandleInstance(final Field f, final Class<T> fType) {
		f.setAccessible(true);
		
		try{final MethodHandle	getter = MethodHandles.lookup().unreflectGetter(f);
			final MethodHandle	setter = MethodHandles.lookup().unreflectSetter(f);
	
			return new ObjectGetterAndSetter<T>() {
				@Override
				public T get(final Object instance) throws ContentException {
					try{return fType.cast(getter.invoke(instance));
					} catch (Throwable e) {
						throw new ContentException("Exception getting read access to the field ["+f.getName()+"] in the class ["+fType+"]: "+e.getLocalizedMessage(),e);
					}
				}
	
				@Override
				public void set(final Object instance, final T value) throws ContentException {
					try{setter.invoke(instance,value);
					} catch (Throwable e) {
						throw new ContentException("Exception getting modification access to the field ["+f.getName()+"] in the class ["+fType+"]: "+e.getLocalizedMessage(),e);
					}
				}

				@Override
				public int getClassType() {
					return CompilerUtils.CLASSTYPE_REFERENCE;
				}
			};
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage(),e); 
		}
	}

	@SuppressWarnings("unchecked")
	private static GetterAndSetter buildCode(final AsmWriter writer, final Class<?> owner, final Class<?> type, final ModuleAccessor assigner, final SimpleURLClassLoader loader, final String className, final String format, final Object... parameters) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final AsmWriter			wr = writer.clone(owner.getClassLoader(),baos)) {
				
				if (!type.isPrimitive()) {
					Class<?>	currentType = type;
					
					while (currentType.isArray()) {
						currentType = currentType.getComponentType();
					}
					if (currentType.isPrimitive()) {
						wr.write(String.format(" .import %1$s%n",owner.getCanonicalName()));
					}
					else {
						wr.write(String.format(" .import %1$s%n .import %2$s%n",owner.getCanonicalName(),currentType.getCanonicalName()));
					}
				}
				
				try{wr.importClass(owner);
				} catch (ContentException e) {
				}
				if (parameters == null || parameters.length == 0) {
					wr.write(format);
				}
				else {
					wr.write(String.format(format,parameters));
				}
				wr.write('\n');
				wr.flush();
			}
			Class<GetterAndSetter>	gas;
				
			try{gas = (Class<GetterAndSetter>) loader.createClass(className,baos.toByteArray());
			} catch (Exception exc) {
				gas = (Class<GetterAndSetter>) loader.loadClass(className);
			}
			assigner.allowUnnamedModuleAccess(loader.getUnnamedModule());
			return gas.getConstructor().newInstance();
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Can't build code for access to fields in class ["+className+"] : "+e.getLocalizedMessage(),e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Instantiator<T> buildCode(final AsmWriter writer, final Class<T> owner, final String className, final ModuleAccessor assigner, final SimpleURLClassLoader loader) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final AsmWriter			wr = writer.clone(owner.getClassLoader(),baos)) {
				
				try{wr.importClass(owner);
					wr.importClass(Instantiator.class);
					wr.importClass(InstantiatorImpl.class);
				} catch (ContentException e) {
				}
				wr.write(" buildInstantiator className=\""+className+"\",managedClass=\""+owner.getCanonicalName()+"\"\n");
				wr.flush();
			}
			Class<Instantiator<T>>	inst;
			
			try{inst = (Class<Instantiator<T>>) loader.createClass(className,baos.toByteArray());
			} catch (Exception exc) {
				inst = (Class<Instantiator<T>>) loader.loadClass(className);
			}
			assigner.allowUnnamedModuleAccess(loader.getUnnamedModule());
			return inst.getConstructor().newInstance();
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Can't build code for create instantiator of class ["+className+"] : "+e.getLocalizedMessage(),e);
		}
	}
}
