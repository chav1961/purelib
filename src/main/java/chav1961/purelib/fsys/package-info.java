/**
 * <p>This package contains a set of {@link chav1961.purelib.fsys.interfaces.FileSystemInterface} implementations and a factory to get access to them.
 * Since 0.0.2 it also contains an implementation of {@link java.net.URLStreamHandler} (named {@link chav1961.purelib.fsys.FileSystemURLStreamHandler})
 * to support access to the file systems thru the URL mechanism</p>
 * <p>Every {@link chav1961.purelib.fsys.interfaces.FileSystemInterface} implementation is deployed into the Java runtime by the 
 * <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">Java SPI</a> protocol (the same one is used by all JDBC drivers). Concept of 
 * the 'File System' see {@link chav1961.purelib.fsys.interfaces.FileSystemInterface} description.</p> 
 * <p>Pure library contains File system interfaces:</p>
 * <ul>
 * <li>{@link chav1961.purelib.fsys.FileSystemOnFile} - the usual file system</li>
 * <li>{@link chav1961.purelib.fsys.FileSystemOnFileSystem} - file system based on Java 1.7 and later standard file systems mechanism</li>
 * <li>{@link chav1961.purelib.fsys.FileSystemOnCsvDb} - file system provided access to database content in the CSV format via JDBC protocol</li>
 * <li>{@link chav1961.purelib.fsys.FileSystemOnXMLReadOnly} - read-only file system based on XML</li>
 * <li>{@link chav1961.purelib.fsys.FileSystemInMemory} - file system in the RAM</li>
 * </ul>
 * <p>Extension of the pure library will contain much more implementations of the File systems (SVN, Git, Amazon S3 etc).</p>
 * 
 * @see chav1961.purelib.fsys.interfaces.FileSystemInterface
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1, last change at 0.0.2
 *
 */
package chav1961.purelib.fsys;