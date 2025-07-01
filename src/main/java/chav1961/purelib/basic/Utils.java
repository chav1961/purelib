package chav1961.purelib.basic;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.Utils.EverywhereWalkerCollector.ReferenceType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class contains implementation of the useful actions in the system. All methods in the class are static and contain:</p>
 * <ul>
 * <li>group of <a href="#copyStream">copy stream</a> methods to move I/O content</li> 
 * <li>group of <a href="#makeCollection">making collections</a> methods to simplify creation of some kinds of collections</li> 
 * <li>group of <a href="#loadResource">resource loading</a> methods to simplify loading any resources into application</li> 
 * <li>group of <a href="#wrapping">wrapping and unwrapping</a> arrays of primitive types</li> 
 * <li>group of <a href="#walking">walking</a> methods to unify walking algorithms in applications</li> 
 * <li>group of <a href="#files">file and directory manipulation</a> methods</li> 
 * <li>group of <a href="#useful">useful</a> methods for checking purposes</li> 
 * </ul> 
 * <p>All the methods in the class are thread-safe</p>
 * <h2><a id="copyStream">Copying streams</a></h2>
 * <p>Copying streams methods allow to automate popular process of moving content from input streams to output streams. This group of 
 * methods consists of:</p>
 * <ul>
 * <li>{@linkplain #copyStream(InputStream, OutputStream)} and  {@linkplain #copyStream(InputStream, OutputStream, ProgressIndicator)} methods 
 * to copy byte content</li>  
 * <li>{@linkplain #copyStream(Reader, Writer)} and  {@linkplain #copyStream(Reader, Writer, ProgressIndicator)} methods 
 * to copy character content</li>  
 * <li>{@linkplain #copyStream(CharacterSource, CharacterTarget)} method to copy Pure Library specific character sources</li>  
 * </ul> 
 * <p>Methods to copy can contain a {@linkplain ProgressIndicator} for visualization purposes.</p>
 * <h2><a id="makeCOllection">Making collection</a> methods</h2>
 * <p>These methods are used to simplify making collections. These methods were appeared because of historical reasons. The latest JRE versions
 * contains come intersected methods now, and we strongly recommend to use them instead of appropriative deprecated methods. This group of 
 * methods contains:</p>
 * <ul>
 * <li>{@linkplain #mkSet(Class, Object...)} method to create set of content</li>
 * <li>{@linkplain #mkMap(Object...)} method to create map of content</li>
 * <li>{@linkplain #mkProps(String...)} method to create props of content</li>
 * </ul>
 * <h2><a id="loadResource">Loading resources</a> into application</h2>
 * <p>Standard methods to load resources into application are {@linkplain Class#getResource(String)} and {@linkplain Class#getResourceAsStream(String)},
 * but they load only byte-oriented content. To simplify loading for other resource types, group of loading resource methods can be used. This group contains
 * {@linkplain #fromResource(Reader)}, {@linkplain #fromResource(URL)} and {@linkplain #fromResource(URL, String)} methods to load char-oriented content into
 * application</p>
 * <h2><a id="wrapping">Wrapping and unwrapping</a> primitive arrays</h2>
 * <p>Sometimes you need to upload primitive arrays content to arrays of appropriative wrapped types (for example, byte-&gt;lava.lang.Byte}. Group of methods allow
 * you to make both wrapping and unwrapping for there arrays. It contains:</p>
 * <ul>
 * <li>{@linkplain #wrapArray(boolean[])} methods to upload primitive arrays to appropriative wrapped arrays.</li> 
 * <li>{@linkplain #unwrapArray(Boolean[])} methods to upload arrays of wrapped values to appropriative primitive arrays.</li> 
 * </ul>
 * <p>This group contains methods for all the primitive types</p>
 * <h2><a id="walking">Walking</a> on linked structures</h2>
 * <p>Walking on linked structures is a popular functionality. Group of the methods contains:</p>
 * <ul>
 * <li>{@linkplain #walkDownEverywhere(Object, EverywhereWalkerCollector, EverywhereWalkerCallback)} method to walk from node into depth</li>
 * <li>{@linkplain #walkUpEverywhere(Object, EverywhereWalkerCollector, EverywhereWalkerCallback)} method to walk from node to root</li>
 * </ul>
 * <p>All the walking methods are controlled by {@linkplain EverywhereWalkerCallback} interface. Every node in the walking trace processed twice:</p>
 * <ul>
 * <li>when entered into given node ({@linkplain NodeEnterMode} value = NodeEnterMode.ENTER)</li> 
 * <li>when exited from given node ({@linkplain NodeEnterMode} value = NodeEnterMode.EXIT)</li> 
 * </ul> 
 * <p>It's guaranteed, that <b>every</b> node entered will be called as node exited. Code returned (it's type is {@linkplain ContinueMode}) notifies walker for 
 * controlling of walking process. Usual value is ContinueMode.CONTINUE, but you can truncate some walking subgraphs by it.</p>  
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 
 * @last.update 0.0.7
 */

public class Utils {
	private static final AtomicInteger		AI = new AtomicInteger();
	private static final AsmWriter			ASM_WRITER = null;
	private static final Map<String,String>	HARDCODED_MIMES = new HashMap<>();
	private static final int				FILL_ARRAY_BOUND = 32; 
	
	static {
//		ASM_WRITER = prepareStatic();
		HARDCODED_MIMES.put("djvu","image/vnd.djvu");
	}

	private static AsmWriter prepareStatic() {
		try{final AsmWriter			tempWriter = new AsmWriter(new ByteArrayOutputStream(),new OutputStreamWriter(System.err));
		
			try(final InputStream	is = GettersAndSettersFactory.class.getResourceAsStream("utilsmacros.txt");
				final Reader		rdr = new InputStreamReader(is)) {
				
				Utils.copyStream(rdr,tempWriter);
			}
			return tempWriter;
		} catch (NullPointerException | IOException e) {
			PureLibSettings.CURRENT_LOGGER.message(Severity.error,"Utils class initialization failure: "+e.getLocalizedMessage(), e);
			throw new PreparationException(e);
		}
	}
	
	private Utils() {
	}

	/**
	 * <p>Copy one file stream to another</p>
	 * @param fis file to copy from. Can't be null
	 * @param fos file to copy to. Can't be null
	 * @return length transferred (in bytes)
	 * @throws IOException if any I/O exception was thrown
	 * @throws NullPointerException when any problems with parameters
	 * @since 0.0.6
	 */
	public static long copyStream(final File fis, final File fos) throws IOException, NullPointerException {
		return Math.abs(copyStream(fis, fos, ProgressIndicator.DUMMY));
	}
	
	/**
	 * <p>Copy one file stream to another</p>
	 * @param fis file to copy from. Can't be null
	 * @param fos file to copy to. Can't be null
	 * @param progress progress indicator to use. Can't be null
	 * @return length transferred (in bytes)
	 * @throws IOException if any I/O exception was thrown
	 * @throws NullPointerException when any problems with parameters
	 * @since 0.0.6
	 */
	public static long copyStream(final File fis, final File fos, final ProgressIndicator progress) throws IOException, NullPointerException {
		if (fis == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else if (fos == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null");
		}
		else {
			try(final InputStream	is = new FileInputStream(fis);
				final OutputStream	os = new FileOutputStream(fos)) {
				
				return copyStream(is, os, progress);
			}
		}
	}
	
	/**
	 * <p>Copy one byte stream to another</p>
	 * @param is input stream to copy from
	 * @param os output stream to copy to
	 * @return length transferred (in bytes)
	 * @throws IOException if any I/O exception was thrown
	 * @throws NullPointerException when any problems with parameters
	 * @last.update 0.0.5
	 */
	public static long copyStream(final InputStream is, final OutputStream os) throws IOException, NullPointerException {
		return Math.abs(copyStream(is, os, ProgressIndicator.DUMMY));
	}

	/**
	 * <p>Copy one byte stream to another with progress indicator</p>
	 * @param is input stream to copy from
	 * @param os output stream to copy to
	 * @param progress progress indicator to use
	 * @return length transferred (in bytes). If copying was interrupted, returns negative value of partially transferred data
	 * @throws IOException if any I/O exception was thrown
	 * @throws NullPointerException when any problems with parameters
	 * @since 0.0.3
	 */
	public static long copyStream(final InputStream is, final OutputStream os, final ProgressIndicator progress) throws IOException, NullPointerException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null");
		}
		else {
			final byte[]	buffer = new byte[8192];
			long			common = 0;
			
			for (int len = is.read(buffer); len > 0; len = is.read(buffer)) {
				os.write(buffer,0,len);
				common += len;
				if (!progress.processed(common)) {
					os.flush();
					return -common;
				}
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
	 * @last.update 0.0.5
	 */
	public static int copyStream(final Reader is, final Writer os) throws IOException {
		return Math.abs(copyStream(is, os, ProgressIndicator.DUMMY));
	}

	/**
	 * <p>Copy one character stream to another</p>
	 * @param is input stream to copy from
	 * @param os output stream to copy to
	 * @param progress progress bar to indicate progress
	 * @return length transferred (in chars). If copying was interrupted, returns negative value of partially transferred data
	 * @throws IOException if any I/O exception was thrown
	 * @throws NullPointerException when any problems with parameters
	 * @since 0.0.3
	 */
	public static int copyStream(final Reader is, final Writer os, final ProgressIndicator progress) throws IOException, NullPointerException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else if (progress == null) {
			throw new NullPointerException("Progress indicator can't be null");
		}
		else {
			final char[]	buffer = new char[8192];
			int 			len, common = 0;
			
			while ((len = is.read(buffer)) > 0) {
				os.write(buffer,0,len);
				common += len;
				if (!progress.processed(common)) {
					os.flush();
					return -common;
				}
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
	 * @last.update 0.0.5
	 */
	public static <T> Map<String,T> mkMap(final Object... parameters) {
		if (parameters == null) {
			throw new NullPointerException("Parameters can't be null");
		}
		else if (parameters.length % 2 != 0) {
			throw new IllegalArgumentException("Odd parameters amount in the list! Parameters need be key/value pairs!");
		}
		else {
			final Map<String,T>	result = new HashMap<>();
			
			for (int index = 0; index < parameters.length; index += 2) {
				if (parameters[index] == null || parameters[index].toString().isEmpty()) {
					throw new IllegalArgumentException("Parameter #=["+index+"] is a key, but it is null or empty!");
				}
				else {
					result.put(parameters[index].toString(),(T)parameters[index+1]);
				}
			}
			return result;
		}
	}

	/**
	 * <p>Build the Map&lt;String,Object&gt; map from the variable arguments list.</p>
	 * @param parameters key/value pairs to parse. Can't be null.
	 * @return map built. Can be empty but not null.
	 * @since 0.0.7
	 * @throws IllegalArgumentException parameters list is null or contains nulls inside
	 */
	public static <T> Map<String,T> mkMap(@SuppressWarnings("unchecked") final NamedValue<T>... parameters) throws IllegalArgumentException {
		if (parameters == null || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Parameters are null or contain nulls inside can't be null");
		}
		else {
			final Map<String,T>	result = new HashMap<>();
			
			for (int index = 0; index < parameters.length; index++) {
				result.put(parameters[index].getName(),(T)parameters[index].getValue());
			}
			return result;
		}
	}
	
	/**
	 * <p>Build a set of the given type from the parameter's list.</p>
	 * @param <T> any class to make set instances from
	 * @param content class of the set content
	 * @param parameters parameters to add to set
	 * @return set created with the given parameters
	 * @deprecated It's strongly recommended to use {@linkplain Set#of()} method instead
	 */
	@SafeVarargs
	@Deprecated(since="0.0.4")
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
	 * <p>Build a Properties instance from the configuration file content</p>
	 * @param content configuration file. Can't be null
	 * @return properties built
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException when content file is null 
	 * @since 0.0.4
	 */
	public static SubstitutableProperties mkProps(final File content) throws IOException, NullPointerException {
		if (content == null) {
			throw new NullPointerException("properties content file can't be null");
		}
		else {
			final SubstitutableProperties		props = new SubstitutableProperties();
			
			try(final InputStream	is = new FileInputStream(content)) {
				props.load(is);
			}
			return props;
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
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(false);
			
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
		return fromResource(resourceURL, PureLibSettings.DEFAULT_CONTENT_ENCODING);
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
				final Reader		rdr = new InputStreamReader(is, encoding)) {
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
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final byte[] content, final byte value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}

	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final short[] content, final short value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}

	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final int[] content, final int value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}

	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final long[] content, final long value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}
	
	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final float[] content, final float value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}

	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final double[] content, final double value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}

	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final char[] content, final char value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}

	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static void fillArray(final boolean[] content, final boolean value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}

	/**
	 * <p>Quick fill array content with the given value.</p>
	 * @param <T> any referenced type
	 * @param content content to fill
	 * @param value value to fill
	 * @since 0.0.6
	 */
	public static <T> void fillArray(final T[] content, final T value) {
		final int 	len;
		
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if ((len = content.length) > 0) {
			for(int index = 0, maxIndex = Math.min(FILL_ARRAY_BOUND, len); index < maxIndex; index++) {
				content[index] = value;
			}
			
			if (len > FILL_ARRAY_BOUND) {	// Copy 2N content on each step 
				int	step = FILL_ARRAY_BOUND, loopCount = 0;
				
				for(int index = 0; index < 31/*size of integer*/; index++, loopCount++, step <<= 1) {
					if (step > len) {
						break;
					}
				}
				
				step = FILL_ARRAY_BOUND;
				for(int index = 0; index < loopCount - 1; index++, step <<= 1) {
					System.arraycopy(content, 0, content, step, step);
				}
				if (len > step) {
					System.arraycopy(content, 0, content, step, len-step);
				}
			}
		}
	}
	
	/**
	 * <p>Define wrapper class for the primitive type</p>
	 * @param clazz class to define wrapper for
	 * @return wrapper for the primitive type
	 * @throws NullPointerException when class is null
	 * @throws IllegalArgumentException when class is not a primitive class
	 * @since 0.0.2
	 * @deprecated use CompilerUtils.toWrappedClass(Class) instead
	 */
	@Deprecated(since="0.0.5")
	public static Class<?> primitive2Wrapper(final Class<?> clazz) throws NullPointerException, IllegalArgumentException {
		return CompilerUtils.toWrappedClass(clazz);
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

	/**
	 * <p>Extract long value from Number object</p>
	 * @param obj object to extract value from
	 * @return value extracted
	 * @throws NullPointerException object to extract value from is null
	 * @throws IllegalArgumentException object is not a Number instance
	 * @since 0.0.3
	 */
	public static long extractLongValue(final Object obj) throws NullPointerException, IllegalArgumentException {
		if (obj == null) {
			throw new NullPointerException("Object to extract value can't be null");
		}
		else if (obj instanceof Number) {
			return ((Number)obj).longValue();
		}
		else {
			throw new IllegalArgumentException("Object to extract long value has invalid type ["+obj.getClass().getCanonicalName()+"]");
		}
	}

	/**
	 * <p>Extract double value from Number object</p>
	 * @param obj object to extract value from
	 * @return value extracted
	 * @throws NullPointerException object to extract value from is null
	 * @throws IllegalArgumentException object is not a Number instance
	 * @since 0.0.3
	 */
	public static double extractDoubleValue(final Object obj) throws NullPointerException, IllegalArgumentException {
		if (obj == null) {
			throw new NullPointerException("Object to extract value can't be null");
		}
		else if (obj instanceof Number) {
			return ((Number)obj).doubleValue(); 
		}
		else {
			throw new IllegalArgumentException("Object to extract double value has invalid type ["+obj.getClass().getCanonicalName()+"]");
		}
	}
	
	/**
	 * <p>This interface id a part of object graph walking support. It supports collecting of the graph node references with the given type</p>  
	 * @param <T> object graph node type
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7 
	 */
	@FunctionalInterface
	public static interface EverywhereWalkerCollector<T> {
		/**
		 * <p>This enumeration describes reference types in the object graph.</p> 
		 * @author Alexander Chernomyrdin aka chav1961
		 * @since 0.0.7 
		 */
		public static enum ReferenceType {
			/**
			 * <p>Reference type - children</p>
			 */
			CHILDREN, 
			/**
			 * <p>Reference type - siblings</p>
			 */
			SIBLINGS, 
			/**
			 * <p>Reference type - parents</p>
			 */
			PARENT
		}
		
		/**
		 * <p>Get references with the given types</p>
		 * @param refs reference type to collect. Can't be null
		 * @param node current object graph node to get references for. Can't be null.
		 * @return references collected. Can't be null but can be empty.
		 */
		T[] getReferences(ReferenceType refs, T node);
	}

	/**
	 * <p>This interface id a part of object graph walking support. It supports callback on every object graph node currently walking</p>  
	 * @param <T> object graph node type.
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7 
	 */
	@FunctionalInterface
	public interface EverywhereWalkerCallback<T> {
		/**
		 * <p>Process current node. This method is always calling twice: on entering node and on exiting node.</p>
		 * @param mode walking mode. Can't be null.
		 * @param node current node to process. Can't be null.
		 * @return continue mode. Can't be null.
		 * @throws ContentException on any node processing errors.
		 */
		ContinueMode process(NodeEnterMode mode, T node) throws ContentException;
	}
	
	public static <T> ContinueMode walkDownEverywhere(final T node, final EverywhereWalkerCollector<T> collector, final EverywhereWalkerCallback<T> callback) throws ContentException {
		if (node == null) {
			throw new NullPointerException("Node to walk from can't be null");
		}
		else if (collector == null) {
			throw new NullPointerException("Walker collector can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback to process walking can't be null");
		}
		else {
			return walkDownEverywhereInternal(node,collector,callback);
		}
	}

	private static <T> ContinueMode walkDownEverywhereInternal(final T node, final EverywhereWalkerCollector<T> collector, final EverywhereWalkerCallback<T> callback) throws ContentException {
		ContinueMode	rcEnter = null, rcExit = null;
		
		if (node != null) {
			try{rcEnter = callback.process(NodeEnterMode.ENTER,node);
				switch(rcEnter) {
					case CONTINUE:
						ContinueMode	rcChildren = null;
						
loop:					for (T item : collector.getReferences(ReferenceType.CHILDREN,node)) {
							switch (rcChildren = walkDownEverywhereInternal(item,collector,callback)) {
								case CONTINUE		: continue;
								case STOP			: rcEnter = ContinueMode.STOP;
								case SKIP_CHILDREN	: break loop;
								default:
									throw new IllegalArgumentException("Returned continue mode ["+rcChildren+"] is not supported for walking down");
							}
						}
					case STOP: 
						break;
					case SKIP_CHILDREN:
						rcEnter = ContinueMode.CONTINUE;
						break;
					default:
						throw new IllegalArgumentException("Returned continue mode ["+rcEnter+"] is not supported for walking down");
				}
			} finally {
				if (rcEnter != null) {
					rcExit = resolveContinueModeInternal(rcEnter,callback.process(NodeEnterMode.EXIT, node));
				}
				else {
					rcExit = ContinueMode.STOP;
				}
			}
		}
		else {
			rcExit = ContinueMode.CONTINUE;
		}
		return rcExit;
	}
	
	public static <T> ContinueMode walkUpEverywhere(final T node, final EverywhereWalkerCollector<T> collector, final EverywhereWalkerCallback<T> callback) throws ContentException {
		if (node == null) {
			throw new NullPointerException("Node to walk from can't be null");
		}
		else if (collector == null) {
			throw new NullPointerException("Walker collector can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback to process walking can't be null");
		}
		else {
			return walkUpEverywhereInternal(node,collector,callback);
		}
	}

	private static <T> ContinueMode walkUpEverywhereInternal(final T node, final EverywhereWalkerCollector<T> collector, final EverywhereWalkerCallback<T> callback) throws ContentException {
		ContinueMode	rcEnter = null, rcExit = null;
		
		if (node != null) {
			try{rcEnter = callback.process(NodeEnterMode.ENTER,node);
				if (rcEnter == ContinueMode.CONTINUE || rcEnter == ContinueMode.SIBLINGS_ONLY) {
					ContinueMode	rcSibling = null;
					
loop:				for (T item : collector.getReferences(ReferenceType.SIBLINGS,node)) {
						switch (rcSibling = walkUpEverywhereInternal(item,collector,callback)) {
							case CONTINUE		: continue;
							case STOP			: rcEnter = ContinueMode.STOP;
							case SKIP_SIBLINGS	: break loop;
							default:
								throw new IllegalArgumentException("Returned continue mode ["+rcSibling+"] is not supported for walking up");
						}
					}
				}
				if (rcEnter == ContinueMode.CONTINUE || rcEnter == ContinueMode.PARENT_ONLY) {
					ContinueMode	rcParent = null;
					
loop:				for (T item : collector.getReferences(ReferenceType.PARENT,node)) {
						switch (rcParent = walkUpEverywhereInternal(item,collector,callback)) {
							case CONTINUE		: continue;
							case STOP			: rcEnter = ContinueMode.STOP;
							case SKIP_PARENT	: break loop;
							default:
								throw new IllegalArgumentException("Returned continue mode ["+rcParent+"] is not supported for walking up");
						}
					}
				}
			} finally {
				if (rcEnter != null) {
					rcExit = resolveContinueModeInternal(rcEnter,callback.process(NodeEnterMode.EXIT, node));
				}
				else {
					rcExit = ContinueMode.STOP;
				}
			}
		}
		else {
			rcExit = ContinueMode.CONTINUE;
		}
		return rcExit;
	}
	
	public static ContinueMode resolveContinueMode(final ContinueMode before, final ContinueMode after) {
		if (before == null) {
			throw new NullPointerException("Before mode can't be null");
		}
		else if (after == null) {
			throw new NullPointerException("After mode can't be null");
		}
		else {
			return resolveContinueModeInternal(before, after);
		}
	}
	
	static ContinueMode resolveContinueModeInternal(final ContinueMode before, final ContinueMode after) {
		if (before == null) {
			throw new NullPointerException("Before mode can't be null");
		}
		else if (after == null) {
			throw new NullPointerException("After mode can't be null");
		}
		else {
			switch (before) {
				case CONTINUE		:
					return after;
				case PARENT_ONLY	:
					if (after == ContinueMode.CONTINUE || after == ContinueMode.SKIP_CHILDREN || after == ContinueMode.SKIP_SIBLINGS) {
						return ContinueMode.PARENT_ONLY;
					}
					else if (after == ContinueMode.SIBLINGS_ONLY) {
						return ContinueMode.PARENT_ONLY;
					}
					else {
						return after;
					}
				case SIBLINGS_ONLY	:
					if (after == ContinueMode.CONTINUE || after == ContinueMode.SKIP_CHILDREN) {
						return ContinueMode.SIBLINGS_ONLY;
					}
					else {
						return after;
					}
				case SKIP_CHILDREN	:
					if (after == ContinueMode.CONTINUE) {
						return ContinueMode.SKIP_CHILDREN;
					}
					else {
						return after;
					}
				case SKIP_PARENT	:
					if (after == ContinueMode.CONTINUE || after == ContinueMode.SKIP_CHILDREN || after == ContinueMode.SKIP_SIBLINGS || after == ContinueMode.SIBLINGS_ONLY || after == ContinueMode.PARENT_ONLY) {
						return ContinueMode.SKIP_PARENT;
					}
					else {
						return after;
					}
				case SKIP_SIBLINGS	:
					if (after == ContinueMode.CONTINUE || after == ContinueMode.SKIP_CHILDREN || after == ContinueMode.SIBLINGS_ONLY) {
						return ContinueMode.SKIP_SIBLINGS;
					}
					else {
						return after;
					}
				case STOP			:
					return ContinueMode.STOP;
				default : throw new UnsupportedOperationException("Continue mode ["+before+"] is not supported yet");
					
			}
		}
	}
	
	
	/**
	 * <p>Delete directory content and directory self.</p>
	 * @param dir directory to delete
	 * @return true if deletion is successful
	 * @since 0.0.2
	 */
	public static boolean deleteDir(final File dir) {
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
				else if (item == '.') {
					result.append('\\').append('.');
				}
				else {
					result.append('\\').append(item);
				}
			}
			return result.toString();
		}
	}

	/**
	 * <p>Start available browser with the given query address</p>
	 * @param query query URI to start for
	 * @since 0.0.4
	 */
	public static void startBrowser(final URL query) throws NullPointerException {
		if (query == null) {
			throw new NullPointerException("Query string can't be null");
		}
		else if (Desktop.isDesktopSupported()) {
			try{
				Desktop.getDesktop().browse(query.toURI());
			} catch (URISyntaxException | IOException exc) {
				PureLibSettings.CURRENT_LOGGER.message(Severity.error,exc,exc.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * <p>Check array content for nulls</p>
	 * @param array referenced type array to check 
	 * @return index of the same first null in the array, otherwise -1
	 * @throws NullPointerException when object to test is null
	 * @throws IllegalArgumentException when object to test is not a referenced array
	 * @since 0.0.3
	 * @last.update 0.0.7
	 */
	public static <T> int checkArrayContent4Nulls(final T[] array) throws NullPointerException, IllegalArgumentException {
		return checkArrayContent4Nulls(array,false);
	}
	
	/**
	 * <p>Check array content for nulls</p>
	 * @param array referenced type array to check
	 * @param checkStrings4Empty check all {@linkplain CharSequence} instances in the array are not empty (if presents)
	 * @return index of the same first null in the array, otherwise -1
	 * @throws NullPointerException when object to test is null
	 * @throws IllegalArgumentException when object to test is not a referenced array
	 * @since 0.0.4
	 * @last.update 0.0.7
	 */
	public static <T> int checkArrayContent4Nulls(final T[] array, final boolean checkStrings4Empty) throws NullPointerException, IllegalArgumentException {
		if (array == null) {
			throw new NullPointerException("Array object to check can't be null"); 
		}
		else {
			final boolean	checkEmpties = checkStrings4Empty && CharSequence.class.isAssignableFrom(array.getClass().getComponentType()); 
			
			for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
				T 	item = array[index];
				
				if (item == null || checkEmpties && (item instanceof CharSequence) && ((CharSequence)item).isEmpty()) {
					return index;
				}
			}
			return -1;
		}
	}

	/**
	 * <p>Check collection content for nulls</p>
	 * @param <T> collection item type
	 * @param collection collection to check. Can't be null
	 * @return index of the same first null in the collection, otherwise -1
	 * @throws NullPointerException when object to test is null
	 * @throws IllegalArgumentException when object to test is not a referenced array
	 */
	public static <T> int checkCollectionContent4Nulls(final Collection<T> collection) throws NullPointerException, IllegalArgumentException {
		return checkCollectionContent4Nulls(collection,false);
	}

	/**
	 * <p>Check collection content for nulls</p>
	 * @param <T> collection item type
	 * @param collection collection to check. Can't be null
	 * @param checkStrings4Empty check all {@linkplain CharSequence} instances in the array are not empty (if presents)
	 * @return index of the same first null in the collection, otherwise -1
	 * @throws NullPointerException when object to test is null
	 * @throws IllegalArgumentException when object to test is not a referenced array
	 */
	public static <T> int checkCollectionContent4Nulls(final Collection<T> collection, final boolean checkStrings4Empty) throws NullPointerException, IllegalArgumentException {
		if (collection == null) {
			throw new NullPointerException("Array object to check can't be null"); 
		}
		else {
			final boolean	checkEmpties = checkStrings4Empty && String.class.isAssignableFrom(collection.getClass().getComponentType());
			int				index = 0;
			
			for(T item : collection) {
				if (item == null || checkEmpties && (item instanceof CharSequence) && ((CharSequence)item).isEmpty()) {
					return index;
				}
				else {
					index++;
				}
			}
			return -1;
		}
	}
	
	/**
	 * <p>Check file existence in the given path</p>
	 * @param path2check path list to seek file in
	 * @param file2check file to seek
	 * @return true if file exists on any path, false othervise
	 * @throws IllegalArgumentException when any argument is null or empty
	 * @since 0.0.4
	 */
	public static boolean checkFileExistence(final String path2check, final String file2check) throws IllegalArgumentException {
		return checkFileExistence(path2check, file2check,File.pathSeparatorChar,File.separatorChar);
	}

	public static <T> T nvl(final T value, final T defaultValue) {
		return value == null ? defaultValue : value; 
	}
	
	/**
	 * <p>Check file existence in the given path</p>
	 * @param path2check path list to seek file in
	 * @param file2check file to seek
	 * @param pathSeparator path separator in the list
	 * @param fileSeparator file separator in the list
	 * @return true if file exists on any path, false othervise
	 * @throws IllegalArgumentException when any argument is null or empty
	 * @since 0.0.4
	 */
	public static boolean checkFileExistence(final String path2check, final String file2check, final char pathSeparator, final char fileSeparator) throws IllegalArgumentException {
		if (path2check == null || path2check.isEmpty()) {
			throw new NullPointerException("Path to check can't be null or empty string");
		} 
		else if (file2check == null || file2check.isEmpty()) {
			throw new NullPointerException("File to check can't be null or empty string");
		} 
		else {
			for (String item : CharUtils.split(path2check,pathSeparator)) {
				if (!item.isEmpty()) {
					final File		f = new File(item.charAt(item.length()-1) == fileSeparator ? item + file2check : item + fileSeparator + file2check);
					
					if (f.exists() && f.isFile()) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	/**
	 * <p>Check string is null or empty</p>
	 * @param str string to check
	 * @return true if string is null or empty
	 * @since 0.0.6
	 * @last.update 0.0.7
	 */
	public static boolean checkEmptyOrNullString(final CharSequence str) {
		return str == null || str.length() == 0;
	}

	/**
	 * <p>Throw exception (used in asm code to avoid stack manipulations for athrow)</p>
	 * @param t throwable to throw
	 * @throws Throwable throwable passed
	 * @since 0.0.4
	 */
	public static void throwException(final Throwable t) throws Throwable {
		throw t;
	}

	/**
	 * <p>Convert {@linkplain Enumeration} to {@linkplain Iterable} to use it inside for-each</p>
	 * @param <T> enumeration content
	 * @param source enumeration to convert. Can't be null
	 * @return iterable converted. Can't be null
	 * @throws NullPointerException when source is null
	 * @since 0.0.7 
	 */
	public static <T> Iterable<T> enumeration2Iterable(final Enumeration<T> source) throws NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source enumeration can't be null"); 
		}
		else {
			return new Iterable<T>() {
				@Override
				public Iterator<T> iterator() {
					return new Iterator<T>() {

						@Override
						public boolean hasNext() {
							return source.hasMoreElements();
						}

						@Override
						public T next() {
							return source.nextElement();
						}
					};
				}
			};
		}
	}
	
	public static <T> T preventRecursiveCall(final Throwable t) throws Throwable {
		throw t;
	}

	/**
	 * <p>Define MIME type for file content by it's extension</p>
	 * @param file file to define MIME type for. Can't be null
	 * @return MIME type, or "application/octet-stream" when can't be defined
	 * @throws NullPointerException when file is null
	 * @since 0.0.6
	 */
	public static MimeType mimeByFile(final File file) throws NullPointerException{
		if (file == null) {
			throw new NullPointerException("File to get MIME for can't be null");
		}
		else {
			try{final String	mime = Files.probeContentType(file.toPath());
			
				if (mime != null) {
					return MimeType.parseMimeList(mime)[0];
				}
				else{
					final String	name = file.getName();
					final int		index = name.lastIndexOf('.');
					
					if (index > 0) {
						final String	ext = name.substring(index + 1);
						
						if (HARDCODED_MIMES.containsKey(ext)) {
							return MimeType.parseMimeList(HARDCODED_MIMES.get(ext))[0];
						}
						else {
							return MimeType.MIME_OCTET_STREAM;
						}
					}
					else {
						return MimeType.MIME_OCTET_STREAM;
					}
				}
			} catch (IOException | MimeParseException e) {
				return MimeType.MIME_OCTET_STREAM;
			}
		}
	}

	/**
	 * <p>Get minimal size to represent value passes exactly</p>
	 * @param value value to define size for.
	 * @return 0 - byte enough, 1 - short enough, 2 - int enough, otherwise 3.
	 * @since 0.0.7
	 */
	public static byte getSignificantSize(final long value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			return 0;
		}
		else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			return 1;
		}
		else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
			return 2;
		}
		else {
			return 3;
		}
	}

	/**
	 * <p>This interface describer direct proxy executor.</p>
	 * @see Proxy 
	 */
	@FunctionalInterface
	public static interface DirectProxyExecutor {
		/**
		 * <p>Execute method</p>
		 * @param owner method owner (will be used as 'this' in the method). Can be null for static methods. 
		 * @param parameters method parameters. Can't be null but can be empty.
		 * @return method execution result. Can be null.
		 * @throws Exception any exception detected
		 */
		Object exec(Object owner, Object... parameters) throws Exception;
	}
	
	/**
	 * <p>This interface describer wrapped proxy executor.</p>
	 * @param <T> class type to execute method in
	 */
	@FunctionalInterface
	public static interface ProxyCallback<T> {
		/**
		 * <p>Execute method.</p>
		 * @param delegate method owner (will be used as 'this' in the method). Can be null for static methods. 
		 * @param method method to invoke. Can't be null.
		 * @param parameters method parameters. Can't be null but can be empty.
		 * @param executor method executor. Can't be null.
		 * @return method execution result. Can be null.
		 * @throws Exception any exception detected
		 * @see DirectProxyExecutor
		 */
		Object process(T delegate, Method method, Object[] parameters, DirectProxyExecutor executor) throws Exception;
	}

	public static <T> T buildProxy(final Class<T> interf, final T inst, final Set<Method> wrappedMethods, final ProxyCallback<T> callback) throws NullPointerException, IllegalArgumentException, ContentException {
		return buildProxy(PureLibSettings.INTERNAL_LOADER, interf, inst, wrappedMethods, callback);
	}
	
	public static <T> T buildProxy(final SimpleURLClassLoader loader, final Class<T> interf, final T inst, final Set<Method> wrappedMethods, final ProxyCallback<T> callback) throws NullPointerException, IllegalArgumentException, ContentException {
		if (loader == null) {
			throw new NullPointerException("Class loader can't be null");
		}
		else if (interf == null) {
			throw new NullPointerException("Interface class can't be null");
		}
		else if (!interf.isInterface()) {
			throw new IllegalArgumentException("Interface class must describe interface, not class!");
		}
		else if (inst == null) {
			throw new NullPointerException("Object instance can't be null");
		}
		else if (wrappedMethods == null) {
			throw new NullPointerException("Wrapped method list can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Proxy callback can't be null");
		}
		else if (wrappedMethods.isEmpty()) {
			return inst;
		}
		else {
			final Set<Method>			fullSet = new HashSet<>();
			final GrowableCharArray<?>	gca = new GrowableCharArray<>(false);
			
			CompilerUtils.walkMethods(interf, (cl,m)->fullSet.add(m));
			final Method[]				methods = fullSet.toArray(new Method[fullSet.size()]);
			final Set<Class<?>>			classes = new HashSet<>();
			final Map<Method,Class<DirectProxyExecutor>>	dpeList = new HashMap<>();
			final String				className = "proxy"+AI.incrementAndGet();
			
			CompilerUtils.collectTypes(classes, interf);
			classes.add(ProxyCallback.class);
			classes.add(MethodHandle.class);
			classes.add(interf);
			
			for (Method item : wrappedMethods) {
				final Class<DirectProxyExecutor>	clazz = buildDirectProxyExecutor(loader,interf, item);
				
				dpeList.put(item,clazz);
				classes.add(clazz);
			}
			
			gca.append(" printProxyImports\n");
			for (Class<?> item : classes) {
				if (!item.isPrimitive() && !item.getCanonicalName().startsWith("java.lang.")) {
					gca.append(" printProxyImportClass \"").append(item.getName()).append("\"\n");
				}
			}
			gca.append(" printProxyClass \"").append(className).append("\",\"").append(interf.getCanonicalName()).append("\"\n");
			for (Method m : methods) {
				if (wrappedMethods.contains(m)) {
					gca.append(" printProxyDeclareLink \"").append(m.getName()).append("\",\"").append(m.getReturnType().getCanonicalName()).append("\",").append(buildClassSignaturesList(m.getParameterTypes())).append('\n');
				}
			}
			gca.append(" printProxyConstructor \"").append(className).append("\",\"").append(interf.getCanonicalName()).append("\"\n");
			for (Method m : methods) {
				if (wrappedMethods.contains(m)) {
					gca.append(" printProxyInitLink \"").append(m.getName()).append("\",\"").append(CompilerUtils.buildClassSignature(m.getReturnType())).append("\",")
							.append(buildClassSignaturesList(m.getParameterTypes())).append('\n');
				}
			}
			gca.append(" printProxyConstructorEnd \"").append(className).append("\"\n");
			for (Method m : methods) {
				final String	parmList = buildClassSignaturesList(m.getParameterTypes());
				
				gca.append(" printProxyMethod \"").append(m.getName()).append("\",\"").append(m.getReturnType().getCanonicalName()).append("\",").append(parmList).append('\n');
				if (wrappedMethods.contains(m)) {
					gca.append(" printProxyWrapperCall \"").append(interf.getCanonicalName()).append("\",\"").append(m.getName()).append("\",\"").append(m.getReturnType().getCanonicalName()).append("\",").append(parmList).append('\n');
				}
				else {
					gca.append(" printProxyDirectMethodCall \"").append(interf.getCanonicalName()).append("\",\"").append(m.getName()).append("\",\"").append(m.getReturnType().getCanonicalName()).append("\",").append(parmList).append('\n');
				}
				gca.append(" printProxyMethodEnd \"").append(m.getName()).append("\"\n");
				if (wrappedMethods.contains(m)) {
					gca.append(" printProxyCallbackMethod \"").append(m.getName()).append("\",\"").append(m.getReturnType().getCanonicalName()).append("\",").append(parmList).append('\n');
					gca.append(" printProxyCallbackDirectMethodCall \"").append(interf.getCanonicalName()).append("\",\"").append(m.getName()).append("\",\"").append(m.getReturnType().getCanonicalName()).append("\",").append(parmList).append('\n');
					gca.append(" printProxyCallbackMethodEnd \"").append(m.getName()).append("\"\n");
				}
			}
			gca.append(" printProxyClassEnd \"").append(className).append("\"\n");
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final AsmWriter			wr = ASM_WRITER.clone(loader,baos)) {
					wr.write(gca.extract());
					wr.flush();
				}
				final Class<?>	proxyClass = loader.createClass(className,baos.toByteArray());
				return interf.cast(proxyClass.getDeclaredConstructor(Method[].class).newInstance((Object)methods));
			} catch (IOException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ContentException(e.getLocalizedMessage(),e); 
			}
		}
	}

	/**
	 * <p>This interface in used in {@linkplain Utils#parallelArraysBinarySearch(int, int, IndicesComparable)} method to compare parallel
	 * array "item" and value to found by array index.  
	 * @see <a href="https://en.wikipedia.org/wiki/Parallel_array">Parallel arrays</a>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8 
	 */
	@FunctionalInterface
	public static interface IndicesComparable {
		/**
		 * <p>Compare "array" item with index passed and value to found.</p>
		 * @param index "array" index to compare with value.
		 * @return &lt; 0 - "array" item is less than value, == 0 - "array" item is equals with value, &gt; 0 - "array" item is greater than value. 
		 */
		int compareTo(int index);
	}
	
	/**
	 * <p>Search parallel arrays content in sorted "array". Because parallel arrays don't contain data as the only item, but a number of parts inside separated
	 * arrays, it's sorting can't be implemented as general case. Instead of direct search, the method uses {@linkplain IndicesComparable} interface to compare
	 * content to search with the "element" of parallel array by it's index.</p> 
	 * <p>Example to use this method:</p>	
	 * <pre>		
	 * final int[] toSearch1 = ..., toSearch2 = ...;
	 * final int search1 = 10, search2 = 20;
	 * final IndicesComparable	ic = new IndicesComparable() {
	 * 							&#64;Override
	 * 							public int compareTo(int index) {
	 * 								int	delta = toSearch1[index] - search1;
	 *
	 * 								return delta == 0 ? toSearch2[index] - search2 : delta;
	 * 							}
	 * 						};
	 * final int found = parallelArraysBinarySearch(0, toSearch1.length-1, ic);
	 * </pre>		
	 * @param from start piece of parallel array to search. Must be equals or greater than 0.
	 * @param to start piece of parallel array to search. Must be greater than from variable.
	 * @param comparator comparator to search content. Can't be null.
	 * @return index &gt;= 0 - "array" item found, index &lt; 0 - =(-1-potential place for item found). 
	 * @see <a href="https://en.wikipedia.org/wiki/Parallel_array">Parallel arrays</a>
 	 * @since 0.0.8 
	 */
	public static int parallelArraysBinarySearch(final int from, final int to, final IndicesComparable comparator) {
		if (from < 0) {
			throw new IllegalArgumentException("From index ["+from+"] must be equals or greater than 0"); 
		}
		else if (to < from) {
			throw new IllegalArgumentException("To index ["+to+"] must be greater or equals than from index ["+from+"]"); 
		}
		else if (comparator == null) {
			throw new NullPointerException("Comparator can't be null");
		}
		else {
	        int low = from, high = to - 1;

	        while (low <= high) {
	            final int	mid = (low + high) >>> 1;
				final int	delta = comparator.compareTo(mid);

	            if (delta > 0) {
	                low = mid + 1;
	            }
	            else if (delta < 0) {
	                high = mid - 1;
	            }
	            else {
	                return mid;
	            }
	        }
	        return -(low + 1);
		}
	}	

	/**
	 * <p>This interface is used in {@linkplain #parallelArraysQSort(int, int, IndicesComparator, IndicesMover, int)} method to compare two
	 * items of parallel "array".</p>
	 * @see <a href="https://en.wikipedia.org/wiki/Parallel_array">Parallel arrays</a>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8 
	 */
	@FunctionalInterface
	public static interface IndicesComparator {
		/**
		 * <p>Compare two items of the "array".
		 * @param index1 first "array" item to compare.
		 * @param index2 second "array" item to compare.
		 * @return &lt; 0 - first "array" item is less than second one, == 0 - "array" items are equals, &gt; 0 - first "array" item is greater than second one. 
		 */
		int compareTo(int index1, int index2);
	}
	
	/**
	 * <p>This interface is used in {@linkplain #parallelArraysQSort(int, int, IndicesComparator, IndicesMover, int)} method to move partially
	 * sorted piece inside array(s) to sort.</p>
	 * @see <a href="https://en.wikipedia.org/wiki/Parallel_array">Parallel arrays</a>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8 
	 */
	public static interface IndicesMover {
		/**
		 * <p>Move partial content inside arrays to sort. </p>
		 * @param from start index to move data from. Negative one-based value means not source array but temporal memory to sort 
		 * @param to start index to move data to. Negative one-based value means not target array but temporal memory to sort. Both indices never can't be negative simultaneously. 
		 * @param length partial content size to move. Always is greater than 0
		 * @see Utils#parallelArraysQSort(int, int, IndicesComparator, IndicesMover, int)
		 */
		void move(int from, int to, int length);
	}
	
	/**
	 * <p>Sort parallel arrays content. Because parallel arrays don't contain data as the only item, but a number of parts inside separated
	 * arrays, it's sorting can't be implemented as general case. Instead of direct sorting, this method uses two lambda-styled interfaces:</p>
	 * <ul>
	 * <li> {@linkplain IndicesComparator} interface to compare two parallel arrays elements. Differ from {@linkplain Comparator} interface,
	 * this one uses two array indices instead of tow objects to compare</li>
	 * <li> {@linkplain IndicesMover} interface to move piece if parallel arrays during sorting operations</li>  
	 * </ul>
	 * <p> To exclude large extra memory requirements for sorting, implementation of IndicesMover interface must support a few temporal memory to
	 * support swap operation on sorting. Example to use this method:</p>	
	 * <pre>		
	 * final int[] toSort1 = ..., toSort2 = ...;
	 * final int N = 5; // Temporal memory size
	 * final int[] temp1 = new int[N], temp2 = new int[N];
	 * final IndicesMover	im = new IndicesMover() {
	 * 							&#64;Override
	 * 							public void move(int from, int to, int length) {
	 * 								if (from &lt; 0) {
	 * 									System.arraycopy(temp1, -1-from, toSort1, to, length);
	 * 									System.arraycopy(temp2, -1-from, toSort2, to, length);
	 * 								}
	 * 								else if (to &lt; 0) {
	 * 									System.arraycopy(toSort1, from, temp1, -1-to, length);
	 * 									System.arraycopy(toSort2, from, temp2, -1-to, length);
	 * 								}
	 * 								else {
	 * 									System.arraycopy(toSort1, from, toSort1, to, length);
	 * 									System.arraycopy(toSort2, from, toSort2, to, length);
	 * 								}
	 * 							}
	 * 						};
	 * final IndicesComparator	ic = new IndicesComparator() {
	 * 							&#64;Override
	 * 							public int compareTo(int index1, int index2) {
	 * 								int	delta = toSort1[index2] - toSort1[index1];
	 *
	 * 								return delta == 0 ? toSort2[index2] - toSort2[index1] : delta;
	 * 							}
	 * 						};
	 * parallelArraysSort(0, toSort1.length-1, ic, im, N);
	 * </pre>		
	 *
	 * @param from start piece of parallel array to sort. Must be equals or greater than 0.
	 * @param to start piece of parallel array to sort. Must be greater than from variable.
	 * @param comparator comparator to compare parallel arrays content. Can't be null.
	 * @param mover mover of parallel array pieces. Can't be null. 
	 * @param tempSize size of temporary memory to use in sort. Must be at least 1. Negative values in the {@linkplain IndicesMover#move(int, int, int)} method
	 * will be in the range -1..-tempSize.
	 * @see <a href="https://en.wikipedia.org/wiki/Parallel_array">Parallel arrays</a>
	 * @see <a href="https://en.wikipedia.org/wiki/Quicksort">Quick sort</a>
	 * @since 0.0.8
	 */
	public static void parallelArraysQSort(final int from, final int to, final IndicesComparator comparator, final IndicesMover mover, final int tempSize) {
		if (from < 0) {
			throw new IllegalArgumentException("From index ["+from+"] must be equals or greater than 0"); 
		}
		else if (to < from) {
			throw new IllegalArgumentException("To index ["+to+"] must be greater or equals than from index ["+from+"]"); 
		}
		else if (comparator == null) {
			throw new NullPointerException("Comparator can't be null");
		}
		else if (mover == null) {
			throw new NullPointerException("Mover can't be null");
		}
		else if (tempSize <= 0) {
			throw new IllegalArgumentException("Temporary size ["+tempSize+"] must be at least 1"); 
		}
		else if (to - from > 1) {
			quickSortPAQS(comparator, mover, from, to);
		}
	}
	
	/**
	 * <p>Split array content on two pieces by position typed</p>
	 * @param <T> any array item type
	 * @param array array to split. Can be neither null nor empty.
	 * @param index position index inside array to split. Must be inside the array bounds.
	 * @return array splitted. Always has two elements. Either first or second (but not both) element can be empty array. 
	 * @throws IllegalArgumentException
	 * @since 0.0.8
	 */
	public static <T> T[][] splitArray(final T[] array, final int index) {
		if (array == null || array.length == 0) {
			throw new IllegalArgumentException("Array to split is null or empty");
		}
		else if (index < 0 || index >= array.length) {
			throw new IllegalArgumentException("Index to split array ["+index+"] out of range 0.."+(array.length-1));
		}
		else {
			final T[][]	result = (T[][]) Array.newInstance(array.getClass(), 2);
			final T[]	left = (T[]) Array.newInstance(array.getClass().getComponentType(), index);
			final T[]	right = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - index);
			
			System.arraycopy(array, 0, left, 0, left.length);
			System.arraycopy(array, index, right, 0, right.length);
			
			result[0] = left;
			result[1] = right;
			return result;
		}
	}
	
	private static void quickSortPAQS(final IndicesComparator comparator, final IndicesMover mover, final int low, final int high) {
	    if (low < high) {
            int pi = partitionPAQS(comparator, mover, low, high);
            
            quickSortPAQS(comparator, mover, low, pi - 1);
            quickSortPAQS(comparator, mover,  pi + 1, high);
	    }
	}
	
	private static int partitionPAQS(IndicesComparator comparator, IndicesMover mover, int low, int high) {
		int pivotIndex = high;
		int i = (low - 1);
		
		for (int j = low; j < high; j++) {
           if (comparator.compareTo(pivotIndex, j) < 0) {
               i++;
               mover.move(i, -1, 1);
               mover.move(j, i, 1);
               mover.move(-1, j, 1);
           }
		}
        mover.move(i+1, -1, 1);
        mover.move(high, i+1, 1);
        mover.move(-1, high, 1);
		return i + 1;	
	}

	private static Class<DirectProxyExecutor> buildDirectProxyExecutor(final SimpleURLClassLoader loader, Class<?> interf, final Method methodCall) throws ContentException {
		final Set<Class<?>>			classes = new HashSet<>();
		final GrowableCharArray<?>	gca = new GrowableCharArray<>(false);
		final String				className = "DirectProxyExecutor$"+methodCall.getName()+"$"+AI.incrementAndGet();

		CompilerUtils.collectTypes(classes, methodCall);
		classes.add(interf);
		
		gca.append(" printDPEImports\n");
		for (Class<?> item : classes) {
			if (!item.isPrimitive() && !item.getCanonicalName().startsWith("java.lang.")) {
				gca.append(" printDPEImportClass \"").append(item.getName()).append("\"\n");
			}
		}
		gca.append(" printDPEClass \"").append(className).append("\",\"").append(interf.getCanonicalName()).append("\"\n");
		gca.append(" printDPEConstructor \"").append(className).append("\",\"").append(interf.getCanonicalName()).append("\"\n");
		gca.append(" printDPEDirectMethodCall \"").append(interf.getCanonicalName()).append("\",\"")
					.append(CompilerUtils.buildMethodPath(methodCall)+CompilerUtils.buildMethodSignature(methodCall))
					.append("\",\"").append(CompilerUtils.buildClassSignature(methodCall.getReturnType())).append("\",")
					.append(buildClassSignaturesList(methodCall.getParameterTypes())).append('\n');
		gca.append(" printDPEClassEnd \"").append(className).append("\"\n");
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final AsmWriter			wr = ASM_WRITER.clone(loader,baos)) {
				wr.write(gca.extract());
				wr.flush();
			}
			return (Class<DirectProxyExecutor>)loader.createClass(className,baos.toByteArray());
		} catch (IOException e) {
			throw new ContentException(e.getLocalizedMessage(),e); 
		}
	}

	private static String buildClassSignaturesList(final Class<?>[] list) {
		if (list.length == 0) {
			return "{}";
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			char				prefix = '{';
			
			for (Class<?> item : list) {
				sb.append(prefix).append('\"').append(CompilerUtils.buildClassSignature(item)).append('\"');
				prefix = ',';
			}
			return sb.append('}').toString();
		}
	}
	
}
