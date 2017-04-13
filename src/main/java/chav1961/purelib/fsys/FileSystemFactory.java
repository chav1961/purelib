package chav1961.purelib.fsys;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ServiceLoader;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class is a factory for creating file system instances for the given URI type.</p>
 * 
 * <p>All implementations of the {@link chav1961.purelib.fsys.interfaces.FileSystemInterface FileSystemInterface} are installed into the library by Java SPI mechanism.
 * Every file system implementation need implements two constructors:</p>
 * <ul>
 * <li>public default constructor</li>
 * <li>public constructor with the URI parameter</li>
 * </ul>
 * <p>The first constructor is a requirement of the SPI, but not uses by file system factory. File system instance was created with the given constructor, need support the
 * {@link FileSystemInterface#canServe(String) canServe(String)} method only and <b>nothing more</b>! The second constructor is the legal way to create file system instance.
 * Any URI parameter for this constructor need be absolute, contains a scheme, and need contain all required information for creating file system instance (for example, database
 * connection, user name and password string etc). The factory find all deployed classes (as described in <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">Java SPI</a>),
 * and test the class about supporting URI with the given scheme. If class was found, the factory creates the class instance and return it<p>
 * 
 * <p>This package implements a restricted set of file systems:</p> 
 * <ul>
 * <li>{@link FileSystemOnFile} - file system based on the native file system</li>
 * <li>{@link FileSystemOnRMI} - file system as a client for remote server</li>
 * <li>{@link FileSystemOnXMLReadOnly} - file system based on an XML file content</li>
 * <li>{@link FileSystemOnFileSystem} - file system based on standard file system in the Java 1.7 or later</li>
 * </ul>
 * <p>Any vendor can add it's own file system implementation to the library. To make this, simply use SPI protocol. The reference to vendor file system
 * need be described in the <b>META-INF/services/chav1961.purelib.fsys.interfaces.FileSystemInterface</b> file</p> 
 * 
 * @see <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">Java SPI description</a>
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface IFileSystem
 * @see chav1961.purelib.fsys JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */


public class FileSystemFactory {
	/**
	 * <p>Create {@link FileSystemInterface} instance for the given URI</p>
	 * @param location uri for passing it to the file system instance constructor.
	 * @return file system instance was created
	 * @throws IOException if any exceptions was thrown
	 */
	public static FileSystemInterface createFileSystem(final URI location) throws IOException {
		return createFileSystem(location,FileSystemFactory.class.getClassLoader());
	}

	/**
	 * <p>Create {@link FileSystemInterface} instance for the given URI and class loader</p>
	 * @param location uri for passing it to the file system instance constructor.
	 * @param loader class loader to seek deployed file system instance services
	 * @return file system instance was created
	 * @throws IOException if any exceptions was thrown
	 */
	public static FileSystemInterface createFileSystem(final URI location, final ClassLoader loader) throws IOException {
		if (location == null) {
			throw new IllegalArgumentException("Location can't be null");
		}
		else if (!location.isAbsolute()) {
			throw new IllegalArgumentException("Location URI ["+location+"] need be absolute and need contain a scheme");
		}
		else if (loader == null) {
			throw new IllegalArgumentException("Loader can't be null");
		}
		else {
			for (FileSystemInterface item : ServiceLoader.load(FileSystemInterface.class,loader)){
				try{if (item.canServe(location.getScheme())) {
						return item.getClass().getConstructor(URI.class).newInstance(location);
					}
				} catch (InvocationTargetException | InstantiationException e) {
					throw new IOException("Error creating file system instance for ["+location+"]: "+e.getCause().getMessage());
				} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
				}
			}
			throw new IOException("No registered filesystem are supported the ["+location.getScheme()+"] URI scheme");
		}
	}
}
