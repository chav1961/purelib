package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.xsd.XSDConst;
import chav1961.purelib.enumerations.XSDCollection;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;


/**
 * <p>This class contains implementation of the useful actions in the system</p> 
 * 
 * @see chav1961.purelib.basic JUnit tests
 * @author chav1961@mail.ru
 * @since 0.0.1 last update 0.0.2
 */

public class Utils {
	private static final String 	W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
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
	 * <p>This class is a primitive void</p>
	 */
	public static final int		CLASSTYPE_VOID = 9;	

	private Utils() {
	}
	
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
			throw new NullPointerException("Input stream can't be null");
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null");
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
	 * @throws NullPointerException when any problems with parameters
	 */
	public static int copyStream(final Reader is, final Writer os) throws IOException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null");
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
	 * <p>Copy one character source stream to another</p>
	 * @param is input source stream to copy from
	 * @param os output target stream to copy to
	 * @return length transferred (in chars)
	 * @throws ContentException error reading source chars
	 * @throws PrintingException error writing target chars 
	 * @throws NullPointerException when any problems with parameters
	 * @since 0.0.2
	 */
	public static int copyStream(final CharacterSource is, final CharacterTarget os) throws NullPointerException, ContentException, PrintingException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else {
			int		common = 0;
			char	symbol;
			
			while ((symbol = is.next()) != CharacterSource.EOF) {
				os.put(symbol);
				common++;
			}
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
			throw new NullPointerException("Parameters can't be null");
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
			throw new NullPointerException("Content class cant' be null");
		}
		else if (parameters == null) {
			throw new NullPointerException("Parameters can't be null");
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
	 * <p>Build a Properties instance of the given type from the parameter's list</p>
	 * @param parameters parameters to add to Properties
	 * @return Properties instance created with the given parameters
	 * @since 0.0.2
	 */
	public static Properties mkProps(final String... parameters) {
		if (parameters == null) {
			throw new NullPointerException("Parameters can't be null");
		}
		else if (parameters.length % 2 != 0) {
			throw new IllegalArgumentException("Odd parameters amount in the list! Parameters need be key/value pairs!");
		}
		else {
			final Properties	result = new Properties();
			
			for (int index = 0; index < parameters.length; index += 2) {
				if (parameters[index] == null || parameters[index].isEmpty()) {
					throw new IllegalArgumentException("Parameter #=["+index+"] is a key, but it is null or empty!");
				}
				else {
					result.setProperty(parameters[index],parameters[index+1]);
				}
			}
			return result;
		}
	}
	
	/**
	 * <p>Load reader content to string</p>
	 * @param reader reader to load data from
	 * @return string loaded
	 * @throws IOException any I/O exceptions 
	 * @throws NullPointerException if any parameters are null
	 * @since 0.0.2
	 */
	public static String fromResource(final Reader reader) throws IOException {
		if (reader == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			final GrowableCharArray	gca = new GrowableCharArray(false);
			
			try{gca.append(reader);
				return new String(gca.extract());
			} finally {
				gca.clear();
			}
		}
	} 
	
	/**
	 * <p>Load resource content to string.</p>
	 * @param resourceURL resource URL
	 * @return string loaded
	 * @throws IOException any I/O exceptions 
	 * @throws NullPointerException if any parametetrs are null
	 */
	public static String fromResource(final URL resourceURL) throws IOException, NullPointerException {
		return fromResource(resourceURL,"UTF-8");
	}
	
	/**
	 * <p>Load resource content to string.</p>
	 * @param resourceURL resource URL
	 * @param encoding resource content encoding
	 * @return string loaded
	 * @throws IOException any I/O exceptions 
	 * @throws NullPointerException if any parametetrs are null
	 */
	public static String fromResource(final URL resourceURL, final String encoding) throws IOException, NullPointerException {
		if (resourceURL == null) {
			throw new NullPointerException("Resource URL can't be null");
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
			throw new NullPointerException("Class to define can't be null"); 
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
		else if (clazz == boolean.class) {
			return CLASSTYPE_BOOLEAN;
		}
		else {
			return CLASSTYPE_VOID;
		}
	}
	
	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Byte[] wrapArray(final byte[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Byte[]	result = new Byte[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Byte.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Short[] wrapArray(final short[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Short[]	result = new Short[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Short.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Integer[] wrapArray(final int[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Integer[]	result = new Integer[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Integer.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Long[] wrapArray(final long[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Long[]	result = new Long[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Long.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Float[] wrapArray(final float[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Float[]	result = new Float[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Float.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Double[] wrapArray(final double[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Double[]	result = new Double[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Double.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Character[] wrapArray(final char[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Character[]	result = new Character[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Character.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with wrapped elements for the primitive values</p>
	 * @param content primitive type array
	 * @return wrapped type array
	 */
	public static Boolean[] wrapArray(final boolean[] content) {
		if (content == null) {
			return null;
		}
		else {
			final Boolean[]	result = new Boolean[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = Boolean.valueOf(content[index]);
			}
			return result;
		}
	}

	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static byte[] unwrapArray(final Byte[] content) {
		if (content == null) {
			return null;
		}
		else {
			final byte[]	result = new byte[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].byteValue() : 0;
			}
			return result;
		}
	}

	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static short[] unwrapArray(final Short[] content) {
		if (content == null) {
			return null;
		}
		else {
			final short[]	result = new short[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].shortValue() : 0;
			}
			return result;
		}
	}

	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static int[] unwrapArray(final Integer[] content) {
		if (content == null) {
			return null;
		}
		else {
			final int[]	result = new int[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].intValue() : 0;
			}
			return result;
		}
	}

	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static long[] unwrapArray(final Long[] content) {
		if (content == null) {
			return null;
		}
		else {
			final long[]	result = new long[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].longValue() : 0;
			}
			return result;
		}
	}

	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static float[] unwrapArray(final Float[] content) {
		if (content == null) {
			return null;
		}
		else {
			final float[]	result = new float[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].floatValue() : 0;
			}
			return result;
		}
	}
	
	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static double[] unwrapArray(final Double[] content) {
		if (content == null) {
			return null;
		}
		else {
			final double[]	result = new double[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].doubleValue() : 0;
			}
			return result;
		}
	}

	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static char[] unwrapArray(final Character[] content) {
		if (content == null) {
			return null;
		}
		else {
			final char[]	result = new char[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].charValue() : 0;
			}
			return result;
		}
	}

	/**
	 * <p>Create array with primitive elements from wrapped values</p>
	 * @param content wrapped type array
	 * @return primitive type array
	 */
	public static boolean[] unwrapArray(final Boolean[] content) {
		if (content == null) {
			return null;
		}
		else {
			final boolean[]	result = new boolean[content.length];
			
			for (int index = 0; index < result.length; index++) {
				result[index] = content[index] != null ? content[index].booleanValue() : false;
			}
			return result;
		}
	}

	/**
	 * <p>Define wrapper class for the primitive type</p>
	 * @param clazz class to define wrapper for
	 * @return wrapper for the primitive type
	 * @throws NullPointerException when class is null
	 * @throws IllegalArgumentException when class is not a primitive class
	 * @since 0.0.2
	 */
	public static Class<?> primitive2Wrapper(final Class<?> clazz) throws NullPointerException, IllegalArgumentException {
		if (clazz == null) {
			throw new NullPointerException("Class to get wrapper for can't be null");
		}
		else {
			switch (defineClassType(clazz)) {
				case CLASSTYPE_REFERENCE	: throw new IllegalArgumentException("Class ["+clazz+"] must be primitive");
				case CLASSTYPE_BYTE			: return Byte.class;
				case CLASSTYPE_SHORT		: return Short.class;
				case CLASSTYPE_CHAR			: return Character.class;
				case CLASSTYPE_INT			: return Integer.class;
				case CLASSTYPE_LONG			: return Long.class;
				case CLASSTYPE_FLOAT		: return Float.class;
				case CLASSTYPE_DOUBLE		: return Double.class;
				case CLASSTYPE_BOOLEAN		: return Boolean.class;
				case CLASSTYPE_VOID			: return Void.class;
				default : throw new UnsupportedOperationException("Class type ["+defineClassType(clazz)+"] is not supported yet"); 
			}
		}
	}

	/**
	 * <p>Define primitive class for primitive wrapper class</p>
	 * @param clazz class to define wrapper for
	 * @return primitive class for wrapper
	 * @throws NullPointerException when class is null
	 * @throws IllegalArgumentException when class is not a wrapper to the primitive class
	 * @since 0.0.2
	 */
	public static Class<?> wrapper2Primitive(final Class<?> clazz) throws NullPointerException, IllegalArgumentException {
		if (clazz == null) {
			throw new NullPointerException("Class to get primitive for can't be null");
		}
		else if (clazz == Byte.class) {
			return byte.class;
		}
		else if (clazz == Short.class) {
			return short.class;
		}
		else if (clazz == Character.class) {
			return char.class;
		}
		else if (clazz == Integer.class) {
			return int.class;
		}
		else if (clazz == Long.class) {
			return long.class;
		}
		else if (clazz == Float.class) {
			return float.class;
		}
		else if (clazz == Double.class) {
			return double.class;
		}
		else if (clazz == Boolean.class) {
			return boolean.class;
		}
		else if (clazz == Void.class) {
			return void.class;
		}
		else {
			throw new IllegalArgumentException("Class ["+clazz+"] is not a wrapper to primitive type"); 
		}
	}

	public static long extractLongValue(final Object obj) {
		if (obj == null) {
			throw new NullPointerException("Object to extract value can't be null");
		}
		else if (obj instanceof Byte) {
			return ((Byte)obj).longValue();
		}
		else if (obj instanceof Short) {
			return ((Short)obj).longValue();
		}
		else if (obj instanceof Integer) {
			return ((Integer)obj).longValue();
		}
		else if (obj instanceof Long) {
			return ((Long)obj).longValue();
		}
		else if (obj instanceof Float) {
			return ((Float)obj).longValue();
		}
		else if (obj instanceof Double) {
			return ((Double)obj).longValue();
		}
		else {
			throw new IllegalArgumentException("Object to extract long value has invalid type ["+obj.getClass().getCanonicalName()+"]");
		}
	}

	public static double extractDoubleValue(final Object obj) {
		if (obj == null) {
			throw new NullPointerException("Object to extract value can't be null");
		}
		else if (obj instanceof Byte) {
			return ((Byte)obj).doubleValue();
		}
		else if (obj instanceof Short) {
			return ((Short)obj).doubleValue();
		}
		else if (obj instanceof Integer) {
			return ((Integer)obj).doubleValue();
		}
		else if (obj instanceof Long) {
			return ((Long)obj).doubleValue();
		}
		else if (obj instanceof Float) {
			return ((Float)obj).doubleValue();
		}
		else if (obj instanceof Double) {
			return ((Double)obj).doubleValue();
		}
		else {
			throw new IllegalArgumentException("Object to extract double value has invalid type ["+obj.getClass().getCanonicalName()+"]");
		}
	}
	
	/**
	 * <p>Does the resource URI can be served by the given URI resource template</p> 
	 * @param uri uri to check
	 * @param template resource template to compare with. Template must be absolute and contains valid scheme and subScheme
	 * @return true if can
	 * @throws NullPointerException if any argument ia null
	 * @throws IllegalArgumentException if template doesn't appropriates with the template constraints
	 */
	public static boolean canServeURI(final URI uri, final URI template) throws NullPointerException, IllegalArgumentException {
		if (uri == null) {
			throw new NullPointerException("URI to test can't be null");
		}
		else if (template == null) {
			throw new NullPointerException("Template URI can't be null");
		}
		else if (!template.isAbsolute() || template.getScheme() == null || template.getRawSchemeSpecificPart() == null) {
			throw new IllegalArgumentException("Template URI ["+template+"] isn't absolute or doesn't contain scheme or scheme-specific part");
		}
		else if (!uri.isAbsolute() || !template.getScheme().equalsIgnoreCase(uri.getScheme()) || uri.getRawSchemeSpecificPart() == null) {
			return false;
		}
		else {
			final URI	subUri = URI.create(uri.getRawSchemeSpecificPart()), subTemplate =  URI.create(template.getRawSchemeSpecificPart());
			
			if (!subTemplate.isAbsolute() || subTemplate.getScheme() == null) {
				throw new IllegalArgumentException("Template URI ["+template+"]: uri subScheme isn't absolute or doesn't contain subScheme or subScheme-specific part");
			}
			else {
				return subTemplate.getScheme().equalsIgnoreCase(subUri.getScheme());
			}
		}
	}
	
	/**
	 * <p>Validate XML content by it's XSD</p>
	 * @param xml XML content to validate
	 * @param xsd XSD to check validation
	 * @return true is the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 */
	public static boolean validateXMLByXSD(final InputStream xml, final InputStream xsd) throws NullPointerException {
		return validateXMLByXSD(xml,xsd, new AbstractLoggerFacade() {
			@Override protected void toLogger(Severity level, String text, Throwable throwable) {}
			@Override protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {return this;}
		});
	}

	/**
	 * <p>Validate XML content by it's XSD</p>
	 * @param xml XML content to validate
	 * @param xsd XSD to check validation
	 * @param logger logger facade to print error messages
	 * @return true is the XML content is valid
	 * @throws NullPointerException if any parameters are null
	 */
	public static boolean validateXMLByXSD(final InputStream xml, final InputStream xsd, final LoggerFacade logger) throws NullPointerException {
		if (xml == null) {
			throw new NullPointerException("XML input stream can't be null");
		}
		else if (xsd == null) {
			throw new NullPointerException("XSD input stream can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			try{final DocumentBuilderFactory 	dbf = DocumentBuilderFactory.newInstance();
			
				dbf.setNamespaceAware(true);
				dbf.setValidating(true);
				dbf.setAttribute(XSDConst.SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
				dbf.setAttribute(XSDConst.SCHEMA_SOURCE, new InputSource(xsd));
				
			    final DocumentBuilder 			db = dbf.newDocumentBuilder();
			    
			    db.setErrorHandler(new ErrorHandler() {
					@Override public void warning(SAXParseException exception) throws SAXException {logger.message(Severity.warning,exception.toString());}
					@Override public void error(SAXParseException exception) throws SAXException {logger.message(Severity.error,exception.toString()); throw exception;}
					@Override public void fatalError(SAXParseException exception) throws SAXException {logger.message(Severity.severe,exception.toString()); throw exception;}
					}
			    );
			    db.parse(new InputSource(xml));
			    
	            return true;
	        } catch (IOException | SAXException | ParserConfigurationException e) {
	            return false;
	        }			
		}
	}

	/**
	 * <p>Get XSD from purelib XSD collection.</p> 
	 * @param item xsd type to get
	 * @return content of the XSD
	 * @throws NullPointerException if item is null
	 */
	public static InputStream getPurelibXSD(final XSDCollection item) throws NullPointerException {
		if (item == null) {
			throw new NullPointerException("XSD connection item can't be null");
		}
		else {
			return Utils.class.getResourceAsStream("xsd/"+item+".xsd");
		}
	}
	
	/**
	 * <p>Load bytes from the given URI.</p>
	 * @param uri uri to load bytes from
	 * @return bytes loaded
	 * @throws NullPointerException when uri is null
	 * @throws IOException on any I/O errors
	 */
	public static byte[] loadBytesFromURI(final URI uri) throws NullPointerException, IOException {
		if (uri == null) {
			throw new NullPointerException("URI to load data from can't be null");  
		}
		else if (uri.getScheme().equals(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			final URL			url = new URL(null,uri.getRawSchemeSpecificPart(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final InputStream			is = conn.getInputStream()) {
				
				copyStream(is,baos);
				return baos.toByteArray();
			}
		}
		else {
			final URL			url = uri.toURL();
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final InputStream			is = url.openStream()) {
				
				copyStream(is,baos);
				return baos.toByteArray();
			}
		}
	}

	/**
	 * <p>Load chars from the given URI.</p>
	 * @param uri uri to load bytes from
	 * @return chars loaded
	 * @throws NullPointerException when uri is null
	 * @throws IOException on any I/O errors
	 */
	public static char[] loadCharsFromURI(final URI uri) throws NullPointerException, IOException {
		if (uri == null) {
			throw new NullPointerException("URI to load data from can't be null");  
		}
		else if (uri.getScheme().equals(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			final URL			url = new URL(null,uri.getRawSchemeSpecificPart(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = conn.getInputStream();
				final Reader		rdr = new InputStreamReader(is)) {
				
				copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
		}
		else {
			final URL				url = uri.toURL();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = url.openStream();
				final Reader		rdr = new InputStreamReader(is)) {
				
				copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
		}
	}

	/**
	 * <p>Load chars from the given URI.</p>
	 * @param uri uri to load bytes from
	 * @param encoding content encoding
	 * @return chars loaded
	 * @throws NullPointerException when uri is null
	 * @throws IOException on any I/O errors
	 */
	public static char[] loadCharsFromURI(final URI uri, final String encoding) throws NullPointerException, IllegalArgumentException, IOException {
		if (uri == null) {
			throw new NullPointerException("URI to load data from can't be null");  
		}
		else if (encoding == null || encoding.isEmpty()) {
			throw new IllegalArgumentException("Content encoding can't be null or empty string");  
		}
		else if (uri.getScheme().equals(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			final URL			url = new URL(null,uri.getRawSchemeSpecificPart(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = conn.getInputStream();
				final Reader		rdr = new InputStreamReader(is,encoding)) {
				
				copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
		}
		else {
			final URL				url = uri.toURL();
			
			try(final Writer		wr = new CharArrayWriter();
				final InputStream	is = url.openStream();
				final Reader		rdr = new InputStreamReader(is,encoding)) {
				
				copyStream(rdr,wr);
				return wr.toString().toCharArray();
			}
		}
	}

	/**
	 * <p>Test that given URI contains nested URI. NEsted URI is any URI in the scheme-specific part, terminated with exclamation mark (!)</p>
	 * @param uri uri to test
	 * @return true if the given uri contains nested URI
	 * @throws NullPointerException if uri is null
	 * @since 0.0.2
	 */
	public static boolean containsNestedURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri can't be null");
		}
		else {
			final String 		path = uri.getPath();
			
			if (path != null && !path.isEmpty()) {
				return path.lastIndexOf('!') >= 0;
			}
			else if (path == null) {
				final String	specific = uri.getSchemeSpecificPart();
				
				return specific.lastIndexOf('!') >= 0;
			}
			else {
				return false;
			}
		}
	}
	
	/**
	 * <p>Extract nested URI from the given URI</p>
	 * @param uri uri to extract nested URI from
	 * @return URI extracted or null if no nested URI inside the given uri instance
	 * @throws NullPointerException if uri is null
	 * @see #containsNestedURI(URI)
	 * @since 0.0.2
	 */
	public static URI extractNestedURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri can't be null");
		}
		else if (containsNestedURI(uri)) {
			final String 	path = uri.getPath();
			
			if (path != null && !path.isEmpty()) {
				final int 	tail = path.lastIndexOf('!');
				
				return URI.create(path.substring(0,tail));
			}
			else if (path == null) {
				final String	specific = uri.getSchemeSpecificPart();
				
				return URI.create(specific.substring(0,specific.lastIndexOf('!')));
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * <p>Extract path in the nested uri. Path in the nested URI is all path content after last exclamation mark (!)</p>
	 * @param uri uri to extract path from
	 * @return path extracted or null if no nested URI inside the given uri instance
	 * @throws NullPointerException if uri is null
	 * @see #containsNestedURI(URI)
	 * @since 0.0.2
	 */
	public static URI extractPathInNestedURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri can't be null");
		}
		else if (containsNestedURI(uri)) {
			final String 	path = uri.getPath();
			
			if (path != null && !path.isEmpty()) {
				final int 	tail = path.lastIndexOf('!');
				
				return URI.create(path.substring(tail+1));
			}
			else if (path == null) {
				final String	specific = uri.getSchemeSpecificPart();
				
				return URI.create(specific.substring(specific.lastIndexOf('!')+1));
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * <p>Append relative path to path inside URI.</p>
	 * @param uri uri to append relative path to
	 * @param relativePath path to append
	 * @return new resolved uri
	 * @throws NullPointerException if uri is null
	 * @throws IllegalArgumentException if relative path is null or empty
	 * @see https://tools.ietf.org/html/rfc3986
	 * @since 0.0.3
	 */
	public static URI appendRelativePath2URI(final URI uri, final String relativePath) throws NullPointerException, IllegalArgumentException {
		if (uri == null) {
			throw new NullPointerException("Uri to append path to can't be null");
		}
		else if (relativePath == null || relativePath.isEmpty()) {
			throw new IllegalArgumentException("Uri to append path to can't be null");
		}
		else {
			final String	newPath = URI.create(uri.getPath()+(relativePath.charAt(0) == '/' ? "" : "/")+relativePath).normalize().toString();
			String			temp = uri.resolve(newPath).toString();
			
			if (uri.getRawQuery() != null) {
				temp += '?' + uri.getRawQuery();
			}
			if (uri.getRawFragment() != null) {
				temp += '#' + uri.getRawFragment();
			}
			return URI.create(temp);
		}
	}
	
	/**
	 * <p>Remove query string from URI</p>
	 * @param uri uri to remove query from
	 * @return uri with query removed. If query is missing, returns source uri
	 * @throws NullPointerException if uri is null
	 * @see https://tools.ietf.org/html/rfc3986
	 * @since 0.0.3
	 */
	public static URI removeQueryFromURI(final URI uri) throws NullPointerException {
		if (uri == null) {
			throw new NullPointerException("Uri to remove query from can't be null");
		}
		else {
			if (uri.getQuery() != null) {
				final String		str = uri.toString();
				
				if (uri.getFragment() != null) {
					final String	frag = str.substring(str.lastIndexOf('#'));
					
					return URI.create(str.substring(0,str.lastIndexOf('?'))+frag);
				}
				else {
					return URI.create(str.substring(0,str.lastIndexOf('?')));
				}
			}
			else {
				return uri;
			}
		}
	}

	/**
	 * <p>Delete directory content and directory self.</p>
	 * @param dir directory to delete
	 * @return true if deletion is successful
	 * @since 0.0.2
	 */
	public static boolean  deleteDir(final File dir) {
		if (dir == null) {
			throw new NullPointerException("Uri can't be null");
		}
		else if (!dir.exists()) {
			throw new IllegalArgumentException("Directory/file to delete ["+dir.getAbsolutePath()+"] not exists");
		}
		else {
			if (dir.isDirectory()){
				dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(final File pathname) {
						deleteDir(pathname);
						return false;
					}
				});
			}
			return dir.delete();
		}
	}
	
	/**
	 * <p>Convert file mask to appropriative regular expression</p> 
	 * @param fileMask file mask to convert (for example *.txt)
	 * @return appropriative regular expression. Can't be null or empty
	 * @throws IllegalArgumentException when file mask to convert is null or empty
	 * @since 0.0.3
	 */
	public static String fileMask2Regex(final String fileMask) throws IllegalArgumentException {
		if (fileMask == null || fileMask.isEmpty()) {
			throw new IllegalArgumentException("File mask to convert can't be null or empty");
		}
		else {
			final StringBuilder	result = new StringBuilder();
			
			for (char item : fileMask .toCharArray()) {
				if (Character.isLetterOrDigit(item)) {
					result.append(item);
				}
				else if (item == '*') {
					result.append('.').append('*');
				}
				else if (item == '?') {
					result.append('.');
				}
				else {
					result.append('\\').append(item);
				}
			}
			return result.toString();
		}
	}
}
