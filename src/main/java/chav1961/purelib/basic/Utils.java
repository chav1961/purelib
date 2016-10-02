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
	 * <p>Get class list from given packages and it's sub-packages. This method can be useful to process all classes from the given packages</p>
	 * @param packages package list to load classes from
	 * @return loaded class list. Can be empty but not null
	 * @throws IOException if some exception was thrown
	 */
	public static List<Class<?>> loadClasses(final Package... packages) throws IOException {
		if (packages == null) {
			throw new IllegalArgumentException("Package list can't be null"); 
		}
		else {
			final List<Class<?>>	result = new ArrayList<>();
			
			for (Package item : packages) {
				for (Package sub : Package.getPackages()) {	// Seek sub-packages
					if (sub.getName().startsWith(item.getName())) {
						fillClasses(sub,result);
					}
				}
			}
			return result;
		}
	}

	private static void fillClasses(final Package item, final List<Class<?>> result) throws IOException {
		final URL	resource = item.getClass().getClassLoader().getSystemClassLoader().getResource(item.getName().replace('.','/')); 
		
		if (resource != null) {
			switch (resource.getProtocol()) {
				case "file" 	:
					try(final InputStream		is = resource.openStream();
						final Reader			rdr = new InputStreamReader(is);
						final BufferedReader	brdr = new BufferedReader(rdr)) {
						String	buffer;
								
						while ((buffer = brdr.readLine()) != null) {
							if (buffer.endsWith(".class")) {
								result.add(item.getClass().getClassLoader().loadClass(item.getName()+'.'+buffer.substring(0,buffer.lastIndexOf('.'))));
							}
						}						
					} catch (ClassNotFoundException e) {
						throw new IOException(e.getMessage());
					}
					break;
				case "jar" 		:
					try{final String[]	parts = resource.toURI().getSchemeSpecificPart().split("\\!");
						try(final InputStream		is = new URL(parts[0]).openStream();
							final JarInputStream	jis = new JarInputStream(is)) {
							final StringBuilder		sb = new StringBuilder();
							JarEntry	je;
							
							while ((je = jis.getNextJarEntry()) != null) {
								final String	packageName = je.getName().substring(0,je.getName().lastIndexOf('/'));
								
								if (packageName.equals(parts[1].substring(1)) && je.getName().endsWith(".class")) {
									result.add(item.getClass().getClassLoader().loadClass(je.getName().substring(0,je.getName().lastIndexOf('.')).replace('/','.')));
								}
							}
						}
					} catch (URISyntaxException | ClassNotFoundException e) {
						throw new IOException(e.getMessage());
					} 
					break;
				default : throw new UnsupportedOperationException("Resource URL ["+resource+"] is not supported for class loading");
			}
		}
	}
}
