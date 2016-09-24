/**
 * <p>This package contains a set of {@link chav1961.purelib.fsys.interfaces.FileSystemInterface} implementations and a factory to get access to them.</p>
 * <p>Every {@link chav1961.purelib.fsys.interfaces.FileSystemInterface} implementation is deployed into the Java runtime by the 
 * <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">Java SPI</a> protocol (the same one use all database JDBC drivers). Pure library includes 
 * File system interfaces for the usual file system and xml content files only. Extension of the library will contain much more implementations.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 *
 */
package chav1961.purelib.fsys;