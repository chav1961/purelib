/**
 * <p>This package contains a set of classes and interfaces to support stream processing of the different sorts.</p>
 * <h2>Package content</h2>
 * <h3>chav1961.purelib.streams package</h3>
 * <p>This package contains a JSON SAX-styled parser to process very large JSON streams. Current implementation defaults,
 * that JSON stream will contains line-splitted JSON, so parsing of 'anaconda-styled' JSON radically reduces it's performance.
 * This package also contains an utility class for building JSON handler for quick-and-simple deserialization JSON content into
 * Java classes.</p>   
 * <h3>chav1961.purelib.streams.charsource package</h3>
 * <p>This package contains a collection of {@link chav1961.purelib.streams.interfaces.CharacterSource} implementations. Character
 * source is a facade to different data sources (string, files and so on) for using in parsers.</p>
 * <h3>chav1961.purelib.streams.chartarget package</h3>
 * <p>This package contains a collection of {@link chav1961.purelib.streams.interfaces.CharacterTarget} implementations. Character
 * target is a facade to different data targets (string, files and so on) for using in different printing tools.</p>
 * <h3>chav1961.purelib.streams.char2byte package</h3>
 * <p>This package contains a collection of {@link java.io.Writer} implementations. The most important example of this implementations
 * is a <b>Java bytecode Assembler</b>. It gets source assembly program as text and translates it to the Java class definition according to
 * Java class file format specifications. This Assembler uses as usual Writer and not need any special case to use.</p>
 * 
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
package chav1961.purelib.streams;