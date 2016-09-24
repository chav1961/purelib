package chav1961.purelib.basic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class contains implementation of the useful actions in the system</p> 
 * 
 * @see chav1961.purelib.basic JUnit tests
 * @author chav1961@mail.ru
 * @since 1.0
 */

public class Utils {
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
}
