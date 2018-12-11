/**
 * <p>This package contains a set of classes and interfaces to support stream processing of the different sorts.</p>
 * <h2>Package content</h2>
 * <h3>{@linkplain chav1961.purelib.streams} package</h3>
 * <p>This package contains a JSON SAX-styled parser {@linkplain chav1961.purelib.streams.JsonSaxParser} to process very large JSON streams. 
 * Current implementation defaults, that JSON stream will contains line-splitted JSON, because parsing of 'anaconda-styled' JSON radically 
 * reduces it's performance. This package also contains an utility class for building JSON handler for quick-and-simple deserialization JSON
 * content into Java classes. It also contains two useful wrappers for {@link java.io.Reader} and {@link java.io.Writer}</p>
 * <p>Since 0.0.2 the package has two new classes for JSON StAX-style {@linkplain chav1961.purelib.streams.JsonStaxParser parsing} and 
 * {@linkplain chav1961.purelib.streams.JsonStaxPrinter printing} data. They implements JSON format according to 
 * <a href="https://tools.ietf.org/html/rfc7159">RFC-7159</a> notation. </p>   
 * <h3>{@link chav1961.purelib.streams.charsource} package</h3>
 * <p>This package contains a collection of {@link chav1961.purelib.streams.interfaces.CharacterSource} implementations. Character
 * source is a facade to different data sources (string, files and so on) for using in parsers.</p>
 * <h3>{@link chav1961.purelib.streams.chartarget} package</h3>
 * <p>This package contains a collection of {@link chav1961.purelib.streams.interfaces.CharacterTarget} implementations. Character
 * target is a facade to different data targets (string, files and so on) for using in different printing tools.</p>
 * <h3>{@link chav1961.purelib.streams.byte2byte} package</h3>
 * <p>This package contains a ZLib {@linkplain chav1961.purelib.streams.byte2byte.ZLibOutputStream compression} and 
 * {@linkplain chav1961.purelib.streams.byte2byte.ZLibInputStream decompression} streams.</p>
 * <h3>{@link chav1961.purelib.streams.char2byte} package</h3>
 * <p>This package contains a collection of {@link java.io.Writer} implementations. The most important example of this implementations
 * is a <b>Java bytecode Assembler</b>. It gets source assembly program as text and translates it to the Java class definition according to
 * Java class file format specifications. This Assembler uses as usual Writer and not need any special case to use.</p>
 * <h3>{@linkplain chav1961.purelib.streams.char2char} package</h3>
 * <p>This package contains a collection of {@link java.io.Reader} and {@link java.io.Writer} implementations. The most important example 
 * of this implementations is a <b>Creole Writer</b> and <b>Preprocessing reader</b>. Creole writer gets source content formatted with the 
 * Creole markup language (see <a href="http://www.wikicreole.org/">www.wikicreole.org</a>) and convert it to a set of predefined formats
 * (text, HTML, XML:FOP). Preprocessing reader gets any input with the preprocessor instructions similar to C/C++ and processes reading
 * content according them.</p>
 * 
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 last update 0.0.2
 */
package chav1961.purelib.streams;