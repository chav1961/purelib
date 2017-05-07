package chav1961.purelib.basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * <p>This class contains implementation of the useful actions in the system</p> 
 * 
 * @see chav1961.purelib.basic JUnit tests
 * @author chav1961@mail.ru
 * @since 1.0
 */

public class Utils {
	/**
	 * <p>This class is a reference class</p>
	 */
	public static final int		CLASSTYPE_REFERENCE = 0;
	
	/**
	 * <p>This class is a primitive byte</p>
	 */
	public static final int		CLASSTYPE_BYTE = 1;
	
	/**
	 * <p>This class is a primitive short</p>
	 */
	public static final int		CLASSTYPE_SHORT = 2;
	
	/**
	 * <p>This class is a primitive char</p>
	 */
	public static final int		CLASSTYPE_CHAR = 3;	
	
	/**
	 * <p>This class is a primitive int</p>
	 */
	public static final int		CLASSTYPE_INT = 4;	
	
	/**
	 * <p>This class is a primitive long</p>
	 */
	public static final int		CLASSTYPE_LONG = 5;	
	
	/**
	 * <p>This class is a primitive float</p>
	 */
	public static final int		CLASSTYPE_FLOAT = 6;	
	
	/**
	 * <p>This class is a primitive double</p>
	 */
	public static final int		CLASSTYPE_DOUBLE = 7;	
	
	/**
	 * <p>This class is a primitive boolean</p>
	 */
	public static final int		CLASSTYPE_BOOLEAN = 8;	
	
	/**
	 * <p>Copy one byte stream to another</p>
	 * @param is input stream to copy from
	 * @param os output stream to copy to
	 * @return length transferred (in bytes)
	 * @throws IOException if any I/O exception was thrown
	 * @throws IllegalArgumentException when any problems with parameters
	 */
	public static long copyStream(final InputStream is, final OutputStream os) throws IOException {
		if (is == null) {
			throw new IllegalArgumentException("Input stream can't be null");
		}
		else if (os == null) {
			throw new IllegalArgumentException("Output stream can't be null");
		}
		else {
			final byte[]	buffer = new byte[8192];
			long			common = 0;
			
			for (int len = is.read(buffer); len > 0; len = is.read(buffer)) {
				os.write(buffer,0,len);
				common += len;
			}
			os.flush();
			return common;
		}
	}
	
	/**
	 * <p>Copy one character stream to another</p>
	 * @param is input stream to copy from
	 * @param os output stream to copy to
	 * @return length transferred (in chars)
	 * @throws IOException if any I/O exception was thrown
	 * @throws IllegalArgumentException when any problems with parameters
	 */
	public static int copyStream(final Reader is, final Writer os) throws IOException {
		if (is == null) {
			throw new IllegalArgumentException("Input stream can't be null");
		}
		else if (os == null) {
			throw new IllegalArgumentException("Output stream can't be null");
		}
		else {
			final char[]	buffer = new char[8192];
			int 			len, common = 0;
			
			while ((len = is.read(buffer)) > 0) {
				os.write(buffer,0,len);
				common += len;
			}
			os.flush();
			return common;
		}
	}
	
	/**
	 * <p>Build the Map&lt;String,Object&gt; map from the variable arguments list. Argument list need be a set of key/value pairs. Type of the 'key'
	 * parameter is always {@link java.lang.String String}, type of the 'value' parameter can be any (including null as value)</p>
	 * @param parameters key/value pairs to parse.
	 * @return map built. Can be empty but not null. 
	 */
	public static Map<String,Object> mkMap(final Object... parameters) {
		if (parameters == null) {
			throw new IllegalArgumentException("Parameters can't be null");
		}
		else if (parameters.length % 2 != 0) {
			throw new IllegalArgumentException("Odd parameters amount in the list! Parameters need be key/value pairs!");
		}
		else {
			final Map<String,Object>	result = new HashMap<>();
			
			for (int index = 0; index < parameters.length; index += 2) {
				if (parameters[index] == null || parameters[index].toString().isEmpty()) {
					throw new IllegalArgumentException("Parameter #=["+index+"] is a key, but it is null or empty!");
				}
				else {
					result.put(parameters[index].toString(),parameters[index+1]);
				}
			}
			return result;
		}
	}
	
	/**
	 * <p>Build a set of the given type from the parameter's list</p>
	 * @param <T> any class to make set instances from
	 * @param content class of the set content
	 * @param parameters parameters to add to set
	 * @return set created with the given parameters
	 */
	@SafeVarargs
	public static <T> Set<T> mkSet(final Class<T> content, final T... parameters) {
		if (content == null) {
			throw new IllegalArgumentException("Content class cant' be null");
		}
		else if (parameters == null) {
			throw new IllegalArgumentException("Parameters can't be null");
		}
		else {
			final Set<T>	result = new HashSet<T>();
			
			for (T item : parameters) {
				if (item != null) {
					result.add(item);
				}
			}
			return result;
		}
	}
	
	/**
	 * <p>Load resource content to string.</p>
	 * @param resourceURL resource URL
	 * @return string loaded
	 * @throws IOException any I/O exceptions 
	 */
	public static String fromResource(final URL resourceURL) throws IOException {
		return fromResource(resourceURL,"UTF-8");
	}
	
	/**
	 * <p>Load resource content to string.</p>
	 * @param resourceURL resource URL
	 * @param encoding resource content encoding
	 * @return string loaded
	 * @throws IOException any I/O exceptions 
	 */
	public static String fromResource(final URL resourceURL, final String encoding) throws IOException {
		if (resourceURL == null) {
			throw new IllegalArgumentException("Resource URL can't be null");
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Resource content encodin can't be null or empty");
		}
		else {
			final StringBuilder		sb = new StringBuilder();
			
			try(final InputStream	is = resourceURL.openStream();
				final Reader		rdr = new InputStreamReader(is,encoding)) {
				final char[]		buffer = new char[8192];
				int		len;
				
				while ((len = rdr.read(buffer)) > 0) {
					sb.append(buffer,0,len);					
				}
			}
			return sb.toString();
		}
	}
	
	/**
	 * <p>Classify the given class by it's primitive type</p>
	 * @param clazz class to classify
	 * @return one of the CLASSTYPE_ZZZ constants (see description) 
	 */
	public static int defineClassType(final Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("Class to define can't be null"); 
		}
		else if (!clazz.isPrimitive()) {
			return CLASSTYPE_REFERENCE;
		}
		else if (clazz == byte.class) {
			return CLASSTYPE_BYTE;
		}
		else if (clazz == short.class) {
			return CLASSTYPE_SHORT;
		}
		else if (clazz == char.class) {
			return CLASSTYPE_CHAR;
		}
		else if (clazz == int.class) {
			return CLASSTYPE_INT;
		}
		else if (clazz == long.class) {
			return CLASSTYPE_LONG;
		}
		else if (clazz == float.class) {
			return CLASSTYPE_FLOAT;
		}
		else if (clazz == double.class) {
			return CLASSTYPE_DOUBLE;
		}
		else {
			return CLASSTYPE_BOOLEAN;
		}
	}
}
