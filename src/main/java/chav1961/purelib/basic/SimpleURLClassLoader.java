package chav1961.purelib.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import chav1961.purelib.streams.char2byte.AsmWriter;

/**
 * <p>This class is a replacement to early existed class ClassLoaderWrapper, because Java 9 doesn't allow to support it's functionality.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class SimpleURLClassLoader extends URLClassLoader {
	public SimpleURLClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(name, urls, parent, factory);
	}

	public SimpleURLClassLoader(String name, URL[] urls, ClassLoader parent) {
		super(name, urls, parent);
	}

	public SimpleURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	public SimpleURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public SimpleURLClassLoader(URL[] urls) {
		super(urls);
	}

	/**
	 * <p>Create class by it's name and content</p>
	 * @param className class name to create
	 * @param content class file content to create class from
	 * @return class created
	 * @throws IllegalArgumentException class name is null or empty or content is null or too short
	 */

	public Class<?> createClass(final String className, final byte[] content) throws IllegalArgumentException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Class name can't be null or empty");
		}
		else if (content == null || content.length < className.length()) {
			throw new IllegalArgumentException("Reader can't be null");
		}
		else {
			try{final Class<?> 	cl = (Class<?>) defineClass(className,content,0,content.length);
			
				resolveClass(cl);			
				return cl;
			} catch (Throwable e) {
				throw new IllegalArgumentException(e);
			}
			
		}
	}

	@Deprecated
	public Class<?> createClass(final byte[] content) throws IllegalArgumentException {
		if (content == null || content.length== 0) {
			throw new IllegalArgumentException("COntent can't be null");
		}
		else {
			try{final Class<?> 	cl = (Class<?>) defineClass(content,0,content.length);
			
				resolveClass(cl);			
				return cl;
			} catch (Throwable e) {
				throw new IllegalArgumentException(e);
			}
			
		}
	}
	
	/**
	 * <p>Create class by it's name and bytecode assembler content</p>
	 * @param className class name to create
	 * @param content reader containing bytecode assembler
	 * @return class created
	 * @throws NullPointerException reader is null
	 * @throws IllegalArgumentException class name is null or empty
	 * @throws IOException when any problems during class loading and definition were detected
	 */
	public Class<?> createClass(final String className, final Reader content) throws NullPointerException, IllegalArgumentException, IOException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Class name can't be null or empty");
		}
		else if (content == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final Writer			wr = new AsmWriter(baos)) {
					
					Utils.copyStream(content,wr);
					wr.flush();
				}
				return createClass(className,baos.toByteArray());
			}
		}
	}
}
