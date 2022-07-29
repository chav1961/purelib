package chav1961.purelib.basic;


import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.intern.UnsafedCharUtils;

/**
 * <p>This class contains implementation of the most-commonly-used char data parsing/printing functions in the system. It can be used to parse character 
 * representation of various content, convert the content to it's native form and make reversal converting to it's character representation. 
 * All the parsing methods in the class are oriented to direct parsing of the char arrays, not strings. It allow to avoid non-productive conversions from 
 * char array to {@linkplain String} instance and revert string content to it's character array representation. This ability increases performance of character
 * data processing, especially for Java 9 and newer. All methods in the class are thread-safe.</p>
 * <p>The class supports:</p>
 * <ul>
 * <li>parsing of <a href="#numerical">numerical data</a> (integer, long, float, double) to convert it to their native representation and reversal converting to character arrays</li>
 * <li><a href="#escaping">escaping and un-escaping</a> character array and string content (frequently used on XML, JSON, CSV format processing)</li>
 * <li>support <a href="#enums">Java enum and field</a> representation parsing</li>
 * <li>support <a href="#substitutions">substitutions</a> in the character and string content</li>
 * <li>a set of <a href="#useful">useful</a> methods for the char content processing</li>
 * </ul> 
 * 
 * <h2><a id="numerical">Parsing and printing numerical content</a></h2>
 * 
 * <p>Methods for parsing numerical content are:</p>
 * <ul>
 * <li>{@linkplain #parseInt(char[], int, int[], boolean)}, {@linkplain #parseSignedInt(char[], int, int[], boolean)}, {@linkplain #parseIntExtended(char[], int, int[], boolean)} - parse character representation of integers and convert it to native integers</li>
 * <li>{@linkplain #parseLong(char[], int, long[], boolean)}, {@linkplain #parseSignedLong(char[], int, long[], boolean)}, {@linkplain #parseLongExtended(char[], int, long[], boolean)} - parse character representation of longs and convert it to native longs</li>
 * <li>{@linkplain #parseFloat(char[], int, float[], boolean)}, {@linkplain #parseSignedFloat(char[], int, float[], boolean)} - parse character representation of floats and convert it to native floats</li>
 * <li>{@linkplain #parseDouble(char[], int, double[], boolean)}, {@linkplain #parseSignedDouble(char[], int, double[], boolean)} - parse character representation of doubles and convert it to native doubles</li>
 * <li>{@linkplain #parseNumber(char[], int, long[], int, boolean)} - parse character representation of numbers, auto-detected minimal type to store parsed value, and convert parsed value to the type detected</li>
 * </ul> 
 * <p>All these methods accept source data array and starting position from it to parse, and returns the position in the source array after parsing. This returned value can then be used
 * in the subsequent calls as starting position. Any parsed object are returned from the methods thru the arrays of appropriative data. This 
 * technique emulates call-by-reference mode for the method parameters, for example:</p>
 * <code>
 * 		final char[] source = "1234 1234".toCharArray(); <br>
 * 		final int[]	value1 = new int[1], value2 = new int[1]; <br>
 *  	final int	endPos1 = CharsUtil.parseInt(source,0,value1,false);				// endPos1 = 4, value1[0] = 1234 <br>
 *  	final int	endPos2 = CharsUtil.parseInt(source,endPos1+1,value2,false);		// endPos2 = 9, value2[0] = 5678 <br>
 * </code>
 * <p>Class {@linkplain CharUtils} also contains method {@linkplain #validateNumber(char[], int, int, boolean)} to check character content is a valid number representation.</p>
 * <p>Methods for printing numerical content are:</p>
 * <ul>
 * <li>{@linkplain #printLong(char[], int, long, boolean)} - convert native long to it's character representation</li>
 * <li>{@linkplain #printDouble(char[], int, double, boolean)} - convert native double to it's character representation</li>
 * </ul> 
 * <p>All the printing methods in the class are oriented to direct filling of the char arrays. Every method accepts target data array and the 
 * free starting position to fill result, and returns the new free position in the array. Negative returned value means that the target array
 * is too small to keep result. It <i>absolute</i> value exactly reflect new free position in the target array and can be used to expand target
 * array to required size</p>
 * <p>All the methods can throw {@linkplain SyntaxException} on parsing errors, and {@linkplain PrintingException} on printing.</p>
 *
 * <h2><a id="escaping">Escaping and un-escaping content</a></h2>
 * 
 * <p>Methods to escaping and un-escaping are used to convert internal representation of chars, char arrays and strings to it's external representation form
 * (for example, to use inside JSON double-quoted values). Methods to escape are:</p>
 * <ul>
 * <li>{@linkplain CharUtils#symbolNeedsEscaping(char, boolean)}, {@linkplain #isSymbolPrintable(char)} and {@linkplain #howManyEscapedCharsOccupies(char, boolean)} - methods to check weather symbol
 * requires escaping for external representation and how many chars it's external representation occupies</li>
 * <li>{@linkplain #parseEscapedChar(char[], int, char[])}, {@linkplain #parseStringExtended(char[], int, char, Appendable)} and {@linkplain #parseStringExtended(char[], int, char, Appendable)} - 
 * methods to parse escaped representation of the char content to it's native form</li>
 * <li>{@linkplain #printEscapedChar(char[], int, char, boolean, boolean)}, {@linkplain #printEscapedCharArray(char[], int, char[], boolean, boolean)}, {@linkplain #printEscapedCharArray(char[], int, char[], int, int, boolean, boolean)} 
 * and {@linkplain #printEscapedString(char[], int, String, boolean, boolean)} - methods to convert internal representation of character or string content to it's external escaped form</li>
 * <li>{@linkplain #escapeStringContent(String)} and {@linkplain #unescapeStringContent(String)} - methods to convert string content to and from it's internal representation to escaped external one</li>
 * </ul>
 * <p>The class {@linkplain CharUtils} also contains {@linkplain #parseUnescapedString(char[], int, char, boolean, int[])} method to parse ordinal string content.</p>
 * <p>All the methods can throw {@linkplain SyntaxException} on parsing errors, and {@linkplain PrintingException} on printing.</p>
 *  
 * <h2><a id="enums">Java enum and fields processing</a></h2>
 * 
 * <p>Enum and fields processing can be used to parse and print enumeration values and field names in the char content. Methods to parse and print enumerations and fields are:</p>
 * <ul>
 * <li>{@linkplain #parseEnum(char[], int, Class, Enum[])} - method to parse enumeration constants</li>
 * <li>{@linkplain #parseName(char[], int, int[])} and {@linkplain #parseNameExtended(char[], int, int[], char...)}- method to parse field names</li>
 * </ul>
 * <p>All the methods can throw {@linkplain SyntaxException} on parsing errors.</p>
 *  
 * <h2><a id="substitutions">Substitutions support</a></h2>
 * 
 * <p>Substitutions is a well-known mechanism for automatic replacement of <b>keys</b> in the character/string content with their current <b>values</b>. Keys in the {@linkplain CharUtils} class are any 
 * sequences in the string content similar "...${key}...". To make substitution for it, one of the substitution interfaces can be used. The complete list of substitution interfaces in the class is:</p>
 * 
 * <ul>
 * <li>{@linkplain SubstitutionSource} - interface to support String-&gt;String replacement</li>
 * <li>{@linkplain CharSubstitutionSource} -  - interface to support char[]-&gt;char[] replacement</li>
 * </ul>
 * 
 * <p>Methods to make substitutions are:</p>
 * <ul>
 * <li>{@linkplain #substitute(String, String, SubstitutionSource)} - method to process string substitutions</li>
 * <li>{@linkplain #substitute(String, char[], int, int, CharSubstitutionSource)} - method to process char array substitutions</li>
 * </ul>
 * <p>All the methods can throw {@linkplain SyntaxException} on parsing errors.</p>
 *  
 * <h2><a id="useful">Useful methods</a></h2>
 * 
 * <p>Useful methods are:</p>
 * <ul>
 * <li> a set of {@linkplain #split(String, char)} methods to split char/string content by divizors. Differ to {@linkplain String#split(String)} method, they don't use regular expressions</li>  
 * <li> a set of {@linkplain #join(char[], char[]...)} methods to join char/string content by divizors. Differ to {@linkplain String#join(CharSequence, CharSequence...)} method, they use more effective implementation</li>  
 * <li> a set of {@linkplain #like(char[], char[], int)} methods to implement SQL LIKE clause functionality. They don't use regular expressions</li>  
 * <li> a set of {@linkplain #compare(char[], int, char[], int, int)} methods for character array comparison</li>  
 * <li> {@linkplain #extract(char[], int, Object[], Object...)} and {@linkplain #tryExtract(char[], int, Object...)} methods for simplest parsers. They don't use regular expressions</li>  
 * <li> {@linkplain #terminateAndConvert2CharArray(String, char)} method for a special functionality - terminate string representation with the given char and convert result to char array (frequently uses in parsers)</li>  
 * </ul>
 * <p>Many of the methods can throw {@linkplain SyntaxException} on parsing errors.</p>
 *  
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.6
 */

public class CharUtils {
	public static final int			PREF_INT = 1;
	public static final int			PREF_LONG = 2;
	public static final int			PREF_FLOAT = 4;
	public static final int			PREF_DOUBLE = 8;
	public static final int			PREF_ANY = PREF_INT | PREF_LONG | PREF_FLOAT | PREF_DOUBLE;
	public static final int			MAX_SUBST_DEPTH = 16;
	public static final Appendable	NULL_APPENDABLE = new Appendable() {
										@Override public Appendable append(CharSequence csq, int start, int end) throws IOException {return this;}
										@Override public Appendable append(char c) throws IOException {return this;}
										@Override public Appendable append(CharSequence csq) throws IOException {return this;}
									};
									
	private static final Comparator<Object>	EQUALS_OBJECT_COMPARATOR = (o1,o2)->{
										return Objects.equals(o1, o2) ? 0 : 1;
									};
	private static final Comparator<String>	EQUALS_STRING_COMPARATOR = (o1,o2)->{
										if (o1 == o2) {
											return 0;
										}
										else if (o1 == null) {
											return 1;
										}
										else if (o2 == null) {
											return -1;
										}
										else if (o1.hashCode() == o2.hashCode()) {
											return 0;
										}
										else {
											return o1.compareTo(o2);
										}
									};
	private static final char[]		EMPTY_CHAR_ARRAY = new char[0];
	private static final String		EMPTY_STRING = "";
	private static final char[]		HYPHEN_NAME = "-".toCharArray();
	private static final char		WILDCARD_ANY_SEQ = '*';
	private static final char		WILDCARD_ANY_CHAR = '?';
	private static final SyntaxTreeInterface<Object>	CONSTANTS = new AndOrTree<>();
	private static final SyntaxTreeInterface<String>	VOCABULARY = new AndOrTree<>();

	static {
		CONSTANTS.placeName("true",true);
		CONSTANTS.placeName("on",true);
		CONSTANTS.placeName("y",true);
		
		CONSTANTS.placeName("false",false);
		CONSTANTS.placeName("off",false);
		CONSTANTS.placeName("n",false);
	}
	
	/**
	 * <p>Extract unsigned integer value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (at least new int[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @lastUpdate 0.0.3
	 */
	public static int parseInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length < 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain at least one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseInt(source, from, result, checkOverflow);
		}
	}

	/**
	 * <p>Extract signed integer value from the current position of the source data</p>
	 * @param source source data contains character representation of the signed integer value
	 * @param from starting position in the source data
	 * @param result array (at least new int[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @since 0.0.2
	 * @lastUpdate 0.0.4
	 */
	public static int parseSignedInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length < 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain at least one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseSignedInt(source, from, result, checkOverflow);
		}
	}
	
	/**
	 * <p>Extract unsigned integer value from the current position of the source data. Differ to {@link #parseInt(char[], int, int[], boolean)} supports binary, octal and hexadecimal representation of the integer constants.</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new int[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @lastUpdate 0.0.3
	 */
	public static int parseIntExtended(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length != 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain exactly one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseIntExtended(source,from,result,checkOverflow);
		}
	}

	/**
	 * <p>Extract unsigned long value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new long[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected 
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @lastUpdate 0.0.3
	 */
	public static int parseLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length < 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain at least one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseLong(source, from, result, checkOverflow);
		}
	}

	/**
	 * <p>Extract signed long value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new long[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @since 0.0.2
	 * @lastUpdate 0.0.4
	 */
	public static int parseSignedLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length < 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain at least one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseSignedLong(source, from, result, checkOverflow);
		}
	}
	
	/**
	 * <p>Extract unsigned long value from the current position of the source data. Differ to {@link #parseLong(char[], int, long[], boolean)} supports binary, octal and hexadecimal representation of the integer constants.</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new long[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @lastUpdate 0.0.3
	 */
	public static int parseLongExtended(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length < 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain at least one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseLongExtended(source, from, result, checkOverflow);
		}
	}

	/**
	 * <p>Extract unsigned float value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @lastUpdate 0.0.3
	 */
	public static int parseFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length != 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain exactly one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseFloat(source,from,result,checkOverflow);
		}
	}

	/**
	 * <p>Extract signed float value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @since 0.0.2
	 * @lastUpdate 0.0.4
	 */
	public static int parseSignedFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length != 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain exactly one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseSignedFloat(source, from, result, checkOverflow);
		}
	}
	
	/**
	 * <p>Extract unsigned double value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @lastUpdate 0.0.3
	 */
	public static int parseDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length < 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain at least one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseDouble(source, from, result, checkOverflow);
		}
	}

	/**
	 * <p>Extract signed double value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 * @since 0.0.2
	 * @lastUpdate 0.0.4
	 */
	public static int parseSignedDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length < 1) {
			throw new IllegalArgumentException("Result array can't be null and need contain at least one element"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseSignedDouble(source, from, result, checkOverflow);
		}
	}
	
	/**
	 * <p>Extract unsigned numeric value from the current position of the source data. This method uses a <i>preferrable</i> parameter to define data type you wish to get. Available 
	 * preferences are described by {@link #PREF_INT}, {@link #PREF_LONG}, {@link #PREF_DOUBLE} and {@link #PREF_ANY} constants. This method always attempts to 'minimize' data type 
	 * for parsed value (if value is greater than maximal integer, represents data as long, and if value is greater than maximal long, represents it as double).</p>    
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data. 
	 * @param result array (new long[2]) to store parsed value. The first element contains parsed value, the second - current data type of the value ({@link #PREF_INT}, {@link #PREF_LONG}
	 * or {@link #PREF_DOUBLE}). If the data type is {@link #PREF_DOUBLE}, you need call {@link Double#longBitsToDouble(long)} method to convert the first element of array to double value 
	 * @param preferences available data types you wish to get 
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws SyntaxException if any parsing errors ware detected
	 * @throws IllegalArgumentException if any argument errors ware detected
	 * @since 0.0.1 
	 * @lastUpdate 0.0.3
	 */
	public static int parseNumber(final char[] source, final int from, final long[] result, final int preferences, final boolean checkOverflow) throws SyntaxException {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (preferences == 0) {
			throw new IllegalArgumentException("Prefeferences bits can't be all zeroes"); 
		}
		else if (result == null || result.length != 2) {
			throw new IllegalArgumentException("Result array can't be null and need conatins exactly two elements"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseNumber(source,from,result,preferences,checkOverflow);
		}
	}


	/**
	 * <p>Validate syntax of number value</p>
	 * @param source content to validate number
	 * @param from start position to parse number
	 * @param preferences available data types you wish to get (see {@link #parseNumber(char[], int, long[], int, boolean)}) 
	 * @param checkOverflow check number overflow
	 * @return number of chars really parsed. Negative value means invalid value. It's absolute value marks the first invalid char location - 1.
	 * @since 0.0.3
	 */
	public static int validateNumber(final char[] source, final int from, final int preferences, final boolean checkOverflow) {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (preferences == 0) {
			throw new IllegalArgumentException("Prefeferences bits can't be all zeroes"); 
		}
		else {
			return UnsafedCharUtils.uncheckedValidateNumber(source,from,preferences,checkOverflow);
		}
	}
	
	
	/**
	 * <p>Test weather the given char needs escaping in the external representation</p>
	 * @param symbol symbol to test
	 * @param strongEscaping true means mandatory escaping for all chars greater than 0xFF. False requires more smart analysis. 
	 * @return true if the char need escaping
	 */
	public static boolean symbolNeedsEscaping(final char symbol, final boolean strongEscaping) {
		if (symbol < ' ') {
			return true;
		}
		else if (symbol <= 0xFF) {
			return false;
		}
		else if (strongEscaping) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * <p>Test weather the given char definitely needs escaping in the external representation</p>
	 * @param symbol symbol to test
	 * @return true if the char need escaping
	 * @since 0.0.4
	 */
	public static boolean isSymbolPrintable(final char symbol) {
		if (symbol == 0xFFFF || Character.isISOControl(symbol)) {
			return false;
		}
		else {
		    final Character.UnicodeBlock 	block = Character.UnicodeBlock.of(symbol);
			
		    return block != null && block != Character.UnicodeBlock.SPECIALS;
		}
	}
	
	public static int howManyEscapedCharsOccupies(final char symbol, final boolean strongEscaping) {
		if (symbol < ' ') {
			switch (symbol) {
				case '\b' : case '\f' : case '\n' : case '\r' : case '\t' : case '\'' : case '\"' : case '\\' :
					return 2;
				default : 
					return 4;
			}
		}
		else {
			if (strongEscaping) {
				return 6;
			}
			else if (symbol <= 0xFF) {
				switch (symbol) {
					case '\\' : case '\'' : case '\"' :
						return 2;
					default :
						return 1;
				}
			}
			else {
				return 6;
			}
		}
	}
	
	/**
	 * <p>Parse possibly escaped char.</p>
	 * @param source source data contains character representation of the char
	 * @param from starting position in the source data. 
	 * @param result array (new char[1]) to store parsed char
	 * @return position of the first char in the source after successful parsing of the current char.
	 * @since 0.0.2 
	 * @lastUpdate 0.0.4 
	 */
	public static int parseEscapedChar(final char[] source, int from, final char[] result) {
		final int 	len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseEscapedChar(source, from, result);
		}
	}
	
	
	/**
	 * <p>Parse unescaped string. This method defines location of the string was bound with the paired quotas (for example "before 'my string' after").
	 * This method <b>not</b> supports any escape sequences inside the string. To process it, use {@link #parseString(char[], int, char, StringBuilder)} method</p>
	 * @param source source data contains character representation of the string value
	 * @param from starting position in the source data. Starting position need points not to quota mark, but the first character in the string (see JUnit tests) 
	 * @param terminal quota mark to use for the string binding (usually (") or (')) 
	 * @param checkEscaping test presence of any escaping. If true, negative returned value from the method marks about it  
	 * @param result array (new int[2]) to store start and end position of the detected string
	 * @return position of the first char in the source after successful parsing of the current string. Will be negative if checkEscaping=true and any escape sequence 
	 * in the string will be detected. It can be used for optimization purposes to avoid rare escaping processing  
	 * @throws IllegalArgumentException if any parsing errors ware detected
	 * @lastUpdate 0.0.4 
	 */
	public static int parseUnescapedString(final char[] source, final int from, final char terminal, final boolean checkEscaping, final int[] result) {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length != 2) {
			throw new IllegalArgumentException("Result array can't be null and needs contain exactly two elements"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseUnescapedString(source,from,terminal,checkEscaping,result);
		}
	}

	/**
	 * <p>Parse escaped string. DIffer to {@link #parseUnescapedString(char[], int, char, boolean, int[])} correctly process the escape sequences. 
	 * Available escape sequences are oriented to the sequences used in the JSON string (see JSON description RFC)</p> 
	 * @param source source data contains character representation of the string value
	 * @param from starting position in the source data. Starting position need points not to quota mark, but the first character in the string (see JUnit tests) 
	 * @param terminal quota mark to use for the string binding (usually (") or (')) 
	 * @param result string builder to store parsed string
	 * @return position of the first char in the source after successful parsing of the current string 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 * @lastUpdate 0.0.4
	 */
	public static int parseString(final char[] source, final int from, final char terminal, final Appendable result) {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null) {
			throw new NullPointerException("Result builder can't be null"); 
		}
		else {
			try{return UnsafedCharUtils.uncheckedParseString(source, from, terminal, result);
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getLocalizedMessage()); 
			}
		}
	}

	/**
	 * <p>Parse escaped string. DIffer to {@link #parseString(char[], int, char, StringBuilder)} also correctly process advanced escape sequences (\0NNN and \0xNNN).</p> 
	 * @param source source data contains character representation of the string value
	 * @param from starting position in the source data. Starting position need points not to quota mark, but the first character in the string (see JUnit tests) 
	 * @param terminal quota mark to use for the string binding (usually (") or (')) 
	 * @param result string builder to store parsed string
	 * @return position of the first char in the source after successful parsing of the current string 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 * @lastUpdate 0.0.4
	 */
	public static int parseStringExtended(final char[] source, final int from, final char terminal, final Appendable result) {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null) {
			throw new NullPointerException("Result builder can't be null"); 
		}
		else {
			try{return 	UnsafedCharUtils.uncheckedParseStringExtended(source, from, terminal, result);
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getLocalizedMessage()); 
			}
		}
	}

	/**
	 * <p>Parse long hexadecimal string. Parses string content and converts it to byte stream.</p>
	 * @param source source data contains character representation of the string value
	 * @param from starting position in the source data. Starting position need points not to quota mark, but the first character in the string (see JUnit tests) 
	 * @param terminal quota mark to use for the string binding (usually (") or (')) 
	 * @param processLineFeed allows line feed and/or carriage return inside the string
	 * @param result byte keeper to store parsed string
	 * @return position of the first char in the source after successful parsing of the current string 
	 * @throws NullPointerException when result is null 
	 * @throws IllegalArgumentException if any parsing errors ware detected
	 * @since 0.0.5 
	 */
	public static int parseHexString(final char[] source, final int from, final char terminal, final boolean processLineFeed, final GrowableByteArray result) throws NullPointerException, IllegalArgumentException{
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null) {
			throw new NullPointerException("Result builder can't be null"); 
		}
		else {
			byte	value = 0;
			char	symbol;
			int		charCount = 0, index;
			
loop:		for (index = from; index < len; index++) {
				switch (symbol = source[index]) {
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						value = (byte) ((value << 4) | (symbol - '0'));
						charCount++;
						break;
					case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' :
						value = (byte) ((value << 4) | (symbol - 'a' + 10));
						charCount++;
						break;
					case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' :
						value = (byte) ((value << 4) | (symbol - 'A' + 10));
						charCount++;
						break;
					case '\n' : case '\r' :
						if (!processLineFeed) {
							throw new IllegalArgumentException("CR/LF inside the string. Use processLineFeed=true"); 
						}
						else {
							continue;
						}
					default :
						if (symbol == terminal) {
							break loop;
						}
						else {
							throw new IllegalArgumentException("Invalid hex symbol [] at index "+index);
						}
				}
				if ((charCount & 0x01) == 0) {
					result.append(value);
					value = 0;
				}
			}
			if (index >= len) {
				throw new IllegalArgumentException("Terminal char ["+terminal+"] not found inside string");
			}
			else {
				if ((charCount & 0x01) != 0) {
					result.append(value);
				}
				return index + 1;
			}
		}
	}	
	
	/**
	 * <p>Parse long hexadecimal string. Parses string content and converts it to byte stream.</p>
	 * @param source source data contains character representation of the string value
	 * @param from starting position in the source data. Starting position need points not to quota mark, but the first character in the string (see JUnit tests) 
	 * @param terminal quota mark to use for the string binding (usually (") or (')) 
	 * @param processLineFeed allows line feed and/or carriage return inside the string
	 * @param result byte keeper to store parsed string
	 * @return position of the first char in the source after successful parsing of the current string
	 * @throws NullPointerException when result is null 
	 * @throws IOException on any I/O errors
	 * @throws IllegalArgumentException if any parsing errors ware detected
	 * @since 0.0.5 
	 */
	public static int parseHexString(final char[] source, final int from, final char terminal, final boolean processLineFeed, final OutputStream result) throws IOException, NullPointerException, IllegalArgumentException{
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null) {
			throw new NullPointerException("Result builder can't be null"); 
		}
		else {
			byte	value = 0;
			char	symbol;
			int		charCount = 0, index;
			
loop:		for (index = from; index < len; index++) {
				switch (symbol = source[index]) {
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						value = (byte) ((value << 4) | (symbol - '0'));
						charCount++;
						break;
					case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' :
						value = (byte) ((value << 4) | (symbol - 'a' + 10));
						charCount++;
						break;
					case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' :
						value = (byte) ((value << 4) | (symbol - 'A' + 10));
						charCount++;
						break;
					case '\n' : case '\r' :
						if (!processLineFeed) {
							throw new IllegalArgumentException("CR/LF inside the string. Use processLineFeed=true"); 
						}
						else {
							continue;
						}
					default :
						if (symbol == terminal) {
							break loop;
						}
						else {
							throw new IllegalArgumentException("Invalid hex symbol [] at index "+index);
						}
				}
				if ((charCount & 0x01) == 0) {
					result.write(value);
					value = 0;
				}
			}
			if (index >= len) {
				throw new IllegalArgumentException("Terminal char ["+terminal+"] not found inside string");
			}
			else {
				if ((charCount & 0x01) != 0) {
					result.write(value);
				}
				return index + 1;
			}
		}
	}	
	
	/**
	 * <p>Parse enumeration constants from the source</p>
	 * @param <T> enumeration type
	 * @param source source data contains character representation of the string value
	 * @param from starting position in the source data. 
	 * @param clazz enumeration class awaited for the constant
	 * @param result array (new Enum[1]) to store parsed enumeration constant
	 * @return position of the first char in the source after successful parsing of the enumeration constant 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static <T extends Enum<?>> int parseEnum(final char[] source, final int from, final Class<T> clazz, final T[] result) {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (clazz == null) {
			throw new NullPointerException("Result class can't be null"); 
		}
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result can't be null or empty array"); 
		}
		else {
			if (!Character.isJavaIdentifierStart(source[from])) {
				throw new IllegalArgumentException("No valid beginning of the Enum name"); 
			}
			else {
				int		index;
				
				for (index = from; index < len; index++) {
					if (!Character.isJavaIdentifierPart(source[index])) {
						break;
					}
				}
				final String	parsed = new String(source,from,index-from);
				
				for(T item : clazz.getEnumConstants()) {
					if (item.name().equals(parsed)) {
						result[0] = item;
						return index;
					}
				}
				throw new IllegalArgumentException("No enumeration constant ["+parsed+"] found in the "+clazz.getName()+" enumeration class"); 
			}
		}		
	}

	/**
	 * <p>Parse name from the source</p>
	 * @param source source data contains character representation of the name
	 * @param from starting position in the source data. 
	 * @param result array (new int[2]) to store start and end position of the name detected
	 * @return position of the first char in the source after successful parsing of the name 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 * @lastUpdate 0.0.4
	 */
	public static int parseName(final char[] source, final int from, final int[] result) {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length != 2) {
			throw new IllegalArgumentException("Result array can't be null and needs contain exactly two elements"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseName(source,from,result);
		}		
	}
	
	/**
	 * <p>Parse name from the source</p>
	 * @param source source data contains character representation of the name
	 * @param from starting position in the source data. 
	 * @param result array (new int[2]) to store start and end position of the name detected
	 * @param availableChars additional chars that are valid in the name
	 * @return position of the first char in the source after successful parsing of the name 
	 * @throws IllegalArgumentException if any parsing errors ware detected
	 * @since 0.0.3 
	 */
	public static int parseNameExtended(final char[] source, final int from, final int[] result, final char... availableChars) {
		int		len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length != 2) {
			throw new IllegalArgumentException("Result array can't be null and needs contain exactly two elements"); 
		}
		else {
			return UnsafedCharUtils.uncheckedParseNameExtended(source,from,result,availableChars);
		}		
	}
	
	
	/**
	 * <p>Skip blank content of the line</p>
	 * @param source content to skip in
	 * @param from start position to skip
	 * @param stopOnEOL don't pass cross lines
	 * @return nearest non-blank position in the source
	 * @throws NullPointerException if source string is null
	 * @throws IllegalArgumentException if from position out of range 
	 * @since 0.0.3 
	 */
	public static final int skipBlank(final char[] source, final int from, final boolean stopOnEOL) {
		if (source == null) {
			throw new NullPointerException("Source string can't be null");
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+source.length);
		}
		else {
			return UnsafedCharUtils.uncheckedSkipBlank(source,from,stopOnEOL);
		}
	}	

	public static final int skipNested(final char[] source, final int from, final char quotas, final char[][] pairs, final boolean stopOnEOL) {
		if (source == null) {
			throw new NullPointerException("Source string can't be null");
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(source.length-1));
		}
		else {
			int index = from;
			
			for (int maxIndex = source.length, depth = 0; index < maxIndex && depth >= 0; index++) {
				char	symbol = source[index];
				
				if (stopOnEOL && symbol == '\n') {
					break;
				}
				else if (symbol == quotas) {
					index = CharUtils.parseString(source,index+1,quotas,new StringBuilder());
				}
				else {
					for (char[] pair : pairs) {
						if (symbol == pair[0]) {
							depth++;
							break;
						}
						else if (symbol == pair[1]) {
							depth--;
							break;
						}
					}
				}
			}
			return index;
		}
	}	
	
	
	/**
	 * <p>Compare char array slice with the given template</p>
	 * @param source source data contains data to compare
	 * @param from starting position of the slice to compare 
	 * @param template template to compare
	 * @return true if the source slice is equals to template
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static boolean compare(final char[] source, final int from, final char[] template) {
		if (source == null || source.length == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+source.length); 
		}
		else if (template == null) {
			throw new NullPointerException("Template data can't be null"); 
		}
		else if (template.length == 0) {
			return true;
		}
		else {
			return UnsafedCharUtils.uncheckedCompare(source,from,template,0,template.length);
		}
	}

	/**
	 * <p>Compare char array slice with the given template ignoring case register</p>
	 * @param source source data contains data to compare
	 * @param from starting position of the slice to compare 
	 * @param template template to compare
	 * @return true if the source slice is equals to template
	 * @throws IllegalArgumentException if any parsing errors ware detected
	 * @since 0.0.4 
	 */
	public static boolean compareIgnoreCase(final char[] source, final int from, final char[] template) {
		if (source == null || source.length == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+source.length); 
		}
		else if (template == null) {
			throw new NullPointerException("Template data can't be null"); 
		}
		else if (template.length == 0) {
			return true;
		}
		else {
			return UnsafedCharUtils.uncheckedCompareIgnoreCase(source,from,template,0,template.length);
		}
	}
	
	/**
	 * <p>Compare char array slice with the given part of template</p>
	 * @param source source data contains data to compare
	 * @param from starting position of the slice to compare 
	 * @param template template to compare
	 * @param templateFrom starting position on the template to compare
	 * @param templateLen legth of template piece to compare with
	 * @return true if the source slice is equals to template part
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static boolean compare(final char[] source, final int from, final char[] template, final int templateFrom, final int templateLen) {
		int	len, tempLen;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+source.length); 
		}
		else if (template == null) {
			throw new NullPointerException("Template data can't be null"); 
		}
		else if (templateFrom > (tempLen = template.length) || templateFrom < 0) {
			throw new IllegalArgumentException("From template position ["+templateFrom+"] out of range 0.."+tempLen); 
		}
		else if (templateFrom + templateLen < 0 || templateFrom + templateLen > tempLen) {
			throw new IllegalArgumentException("End template position ["+(templateFrom+templateLen)+"] out of range 0.."+tempLen); 
		}
		else if (tempLen == 0) {
			return true;
		}
		else if ((len-from) < tempLen) {
			return false;
		}
		else {
			return UnsafedCharUtils.uncheckedCompare(source,from,template,templateFrom,templateLen);
		}
	}

	/**
	 * <p>Compare char array slice with the given part of template</p>
	 * @param source source data contains data to compare
	 * @param from starting position of the slice to compare 
	 * @param template template to compare
	 * @param templateFrom starting position on the template to compare
	 * @param templateLen legth of template piece to compare with
	 * @return true if the source slice is equals to template part
	 * @throws IllegalArgumentException if any parsing errors ware detected
	 * @since 0.0.4 
	 */
	public static boolean compareIgnoreCase(final char[] source, final int from, final char[] template, final int templateFrom, final int templateLen) {
		int	len, tempLen;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+source.length); 
		}
		else if (template == null) {
			throw new NullPointerException("Template data can't be null"); 
		}
		else if (templateFrom > (tempLen = template.length) || templateFrom < 0) {
			throw new IllegalArgumentException("From template position ["+templateFrom+"] out of range 0.."+tempLen); 
		}
		else if (templateFrom + templateLen < 0 || templateFrom + templateLen > tempLen) {
			throw new IllegalArgumentException("End template position ["+(templateFrom+templateLen)+"] out of range 0.."+tempLen); 
		}
		else if (tempLen == 0) {
			return true;
		}
		else if ((len-from) < tempLen) {
			return false;
		}
		else {
			return UnsafedCharUtils.uncheckedCompareIgnoreCase(source,from,template,templateFrom,templateLen);
		}
	}
	
	/**
	 * <p>This enumeration is used to describe template for extracting content from character array with lexemas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 * @lastUpdate 0.0.4
	 */
	public enum ArgumentType {
		ordinalInt, signedInt, hexInt, ordinalLong, signedLong, hexLong, ordinalFloat, signedFloat, Boolean,
		name, hyphenedName, simpleTerminatedString, specialTerminatedString,
		colorRepresentation,
		raw
	}

	/**
	 * <p>This class is used to describe template for extracting content from character array with lexemas. It contains <i>optional</i> sequence of lexemas</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public static class Optional {
		private final Object[]	lexemas;
		
		/**
		 * <p>Constructor of the class</p>
		 * @param lexemas to keep. Can't be null or empty array and can't contain nulls inside
		 * @throws IllegalArgumentException on any argument errors
		 */
		public Optional(Object... lexemas) throws IllegalArgumentException {
			if (lexemas == null || lexemas.length == 0 || Utils.checkArrayContent4Nulls(lexemas) >= 0) {
				throw new IllegalArgumentException("Lexemas is null, empty or contain nulls inside"); 
			}
			else {
				this.lexemas = lexemas;
			}
		}
	}

	/**
	 * <p>This class is used to describe template for extracting content from character array with lexemas. It contains one or more <i>choises<i> (each with sequence of lexemas)</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 * 
	 */
	public static class Choise {
		private final Object[][]	lexemas;
		
		/**
		 * <p>Constructor of the class</p>
		 * @param lexemas groups of lexemas to choise alternatives. Can't be null, empty or contains nulls inside
		 * @throws IllegalArgumentException on any argument errors
		 */
		public Choise(Object[]... lexemas)  throws IllegalArgumentException {
			if (lexemas == null || lexemas.length == 0 || Utils.checkArrayContent4Nulls(lexemas) >= 0) {
				throw new IllegalArgumentException("Lexemas is null, empty or contain nulls inside"); 
			}
			else {
				this.lexemas = lexemas;
			}
		}
	}

	/**
	 * <p>This class is used to describe template for extracting content from character array with lexemas. It represents self and will be included into parsed parameters as-is.
	 * It is useful to mark 'trace' of parsing (for example in option clause)</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public static class Mark {
		private final int	value;

		/**
		 * <p>Constructor of the class</p>
		 * @param value value associated with this mark instance</p>
		 */
		public Mark(final int value) {
			this.value = value;
		}
		
		/**
		 * <p>Get value associated with thisinstance</p>
		 * @return value associated
		 */
		public int getMark() {
			return value;
		}
	}
	
	/**
	 * <p>Try to extract content from input character array with lexemas. Returns non-negative number if extraction was successful. Lexemas can be:</p>
	 * <ul>
	 * <li>single character - marks character 'as-is' in the input array</li>
	 * <li>string - marks sequence of characters 'as-is' in the input array</li>
	 * <li>{@linkplain ArgumentType} - marks kind of predefined lexema in the input array</li>
	 * <li>{@linkplain Class<? extends Enum>} - marks kind of any enumeration in the input array</li>
	 * <li>{@linkplain Mark} - marks self and will be included into parsed parameters list 'as-is'. It is useful to mark parsing 'trace'</li>
	 * <li>{@linkplain Optional} - marks optional sequence of the lexemas in the inpt array</li>
	 * <li>{@linkplain Choise} - marks choise (one of the sequences) in the input array</li>
	 * </ul>
	 * @param source source array to try extraction from
	 * @param from start position to extract content from
	 * @param lexemas lexemas list to try extract
	 * @return non-negative first position after parsed piece of characters in the input array or (negative position-1) in the input array where parsing failed 
	 * @throws IllegalArgumentException on any invalid parameters
	 * @throws SyntaxException on any syntax error in the content
	 * @since 0.0.3
	 * @lastUpdate 0.0.6
	 */
	public static int tryExtract(final char[] source, final int from, Object... lexemas) throws IllegalArgumentException, SyntaxException {
		int	len, start = from;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source char array can't be null or empty");
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From location ["+from+"] out of range 0.."+(len-1));
		}
		else if (lexemas == null || lexemas.length == 0) {
			throw new IllegalArgumentException("Lexemas list can't be null or empty");
		}
		else {
			final int[]			intResult = new int[2];
			final long[]		longResult = new long[2];
			final float[]		floatResult = new float[2];
			final StringBuilder	sb = new StringBuilder();
			
loop:		for (int index = 0, maxIndex = lexemas.length; index < maxIndex; index++) {
				final Object	lexema = lexemas[index];
				
				start = UnsafedCharUtils.uncheckedSkipBlank(source,start,true);
				if (lexema instanceof char[]) {
					final char[]	item = (char[])lexema; 
					
					if (UnsafedCharUtils.uncheckedCompare(source,start,item,0,item.length)) {
						start += item.length;
					}
					else {
						return -(start+1); 
					}
				}
				else if (lexema instanceof Character) {
					if (source[start] == ((Character)lexema).charValue()) {
						start++;
					}
					else {
						return -(start+1); 
					}
				}
				else if (lexema instanceof Mark) {
					// marker doesn't process on try
				}
				else if ((lexema instanceof Class<?>) && ((Class<?>)lexema).isEnum()) {
					if (Character.isJavaIdentifierStart(source[start])) {
						final int	possibleStart = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
						
						try {Enum.valueOf((Class<? extends Enum>)lexema, new String(source, start, possibleStart-start));
							start = possibleStart;
						} catch (IllegalArgumentException exc) {
							return -(start+1);
						}
					}
					else {
						return -(start+1);
					}
				}
				else if (lexema instanceof Optional) {
					final int	afterOptional = tryExtract(source, start, ((Optional)lexema).lexemas);
					
					if (afterOptional >= 0) {
						start = afterOptional;
					}
				}
				else if (lexema instanceof Choise) {
					for (Object[] item : ((Choise)lexema).lexemas) {
						final int	afterChoise;
						
						afterChoise = tryExtract(source, start, item);
						if (afterChoise >= 0) {
							start = afterChoise;
							continue loop;
						}
					}
					return -(start+1); 
				}
				else if (lexema instanceof ArgumentType) {
					try {
						switch ((ArgumentType)lexema) {
							case hexInt			:
								start = UnsafedCharUtils.uncheckedParseHexInt(source,start,intResult,true);
								break;
							case hexLong		:
								start = UnsafedCharUtils.uncheckedParseHexLong(source,start,longResult,true);
								break;
							case ordinalInt		:
								start = UnsafedCharUtils.uncheckedParseInt(source,start,intResult,true);
								break;
							case signedInt		:
								if (source[start] == '-' || source[start] == '+') {
									start++;
								}
								start = UnsafedCharUtils.uncheckedParseInt(source,UnsafedCharUtils.uncheckedSkipBlank(source,start,true),intResult,true);
								break;
							case ordinalLong	:
								start = UnsafedCharUtils.uncheckedParseLong(source,start,longResult,true);
								break;
							case ordinalFloat	:
								start = UnsafedCharUtils.uncheckedParseFloat(source,start,floatResult,true);
								break;
							case name			:
								if (Character.isJavaIdentifierStart(source[start])) {
									start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
								}
								else {
									return -(start+1);
								}
								break;
							case hyphenedName	:
								if (Character.isJavaIdentifierStart(source[start])) {
									start = UnsafedCharUtils.uncheckedParseNameExtended(source,start,intResult,HYPHEN_NAME);
								}
								else {
									return -(start+1);
								}
								break;
							case simpleTerminatedString	:
								if (source[start] == '\"') {
									start = UnsafedCharUtils.uncheckedParseUnescapedString(source,start+1,'\"',false,intResult);
								}
								else if (source[start] == '\'') {
									start = UnsafedCharUtils.uncheckedParseUnescapedString(source,start+1,'\'',false,intResult);
								}
								else if (Character.isJavaIdentifierStart(source[start])){
									start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
								}
								else {
									return -(start+1);
								}
								break;
							case specialTerminatedString	:
								try{if (source[start] == '\"') {
										start = UnsafedCharUtils.uncheckedParseString(source,start+1,'\"',sb);
									}
									else if (source[start] == '\'') {
										start = UnsafedCharUtils.uncheckedParseString(source,start+1,'\'',sb);
									}
									else if (Character.isJavaIdentifierStart(source[start])){
										start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
									}
									else {
										return -(start+1);
									}
								} catch (IOException e) {
									return -(start+1);
								}
								break;
							case colorRepresentation	:
								if (source[start] == '#') {	// Hex presentation
									if ("0123456789abcdefABCDEF".indexOf(source[start+1]) >= 0) {
										start = UnsafedCharUtils.uncheckedParseHexInt(source,start+1,intResult,true);
									}
									else {
										return -(start+1);
									}
								}
								else if (Character.isJavaIdentifierStart(source[start])){
									final int	oldStart = start;
									
									start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
									if (PureLibSettings.colorByName(new String(source,intResult[0],intResult[1]-intResult[0]+1),null) == null) {
										return - (oldStart+1);
									}
								}
								else {
									return -(start+1);
								}
								break;
							case Boolean	:
								if (Character.isJavaIdentifierStart(source[start])) {
									start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
									final long	boolId = CONSTANTS.seekName(source, intResult[0], intResult[1]+1);
										
									if (!(boolId >= 0 && (CONSTANTS.getCargo(boolId) instanceof Boolean))) {
										return -(start+1);
									}
								}
								else {
									return -(start+1);
								}
								break;
							case raw	:
								while (start < source.length && source[start] != '\r' && source[start] != '\n') {
									start++;
								}
								return start < source.length && (source[start] == '\r' || source[start] == '\n') ? start-1 : start;
							default				:
								throw new UnsupportedOperationException("Argument type ["+lexema+"] is not supported yet"); 
						}
					} catch (IllegalArgumentException exc) {
						return -(start+1); 
					}
				}
				else {
					throw new IllegalArgumentException("Unsupported Lexema type ["+lexema+"] at index ["+index+"]"); 
				}
			}
		}
		return start;
	}

	/**
	 * <p>Extract content from input character array according to lexemas list. Failed extraction produces {@linkplain SyntaxException}.</p> 
	 * @param source source array to extraction from
	 * @param from start position to extract content from
	 * @param result array to store extracted content to. It's capacity must be enough to receive all the content extracted
	 * @param lexemas lexemas list to extract. See {@linkplain #tryExtract(char[], int, Object...)} for details
	 * @return first position after parsed piece of characters in the input array 
	 * @throws IllegalArgumentException on any invalid parameters
	 * @throws SyntaxException on any syntax error in the content
	 * @since 0.0.3
	 * @lastUpate 0.0.6
	 */
	public static int extract(final char[] source, final int from, final Object[] result, Object... lexemas) throws SyntaxException {
		return extract(source, from, result, new int[] {0}, lexemas);
	}	
	
	static int extract(final char[] source, final int from, final Object[] result, final int[] resultIndex, Object... lexemas) throws SyntaxException {
		int	len, start = from;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source char array can't be null or empty");
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From location ["+from+"] out of range 0.."+(len-1));
		}
		else if (result == null) {
			throw new NullPointerException("Result object array can't be null");
		}
		else if (lexemas == null || lexemas.length == 0) {
			throw new IllegalArgumentException("Lexemas list can't be null or empty");
		}
		else {
			int  resultCount = resultIndex[0];
			
			for (Object item : lexemas) {
				if ((item instanceof ArgumentType) || (item instanceof Mark)) {
					resultCount++;
				}
			}
			if (resultCount > result.length) {
				throw new IllegalArgumentException("Result array size ["+result.length+"] is less than number of ArgumetType and/or Mark lexemas in the list ["+resultCount+"]");
			}
			
			final int[]		intResult = new int[2];
			final long[]	longResult = new long[2];
			final float[]	floatResult = new float[2];
			
			for (int index = 0, maxIndex = lexemas.length; index < maxIndex; index++) {
				final Object	lexema = lexemas[index];
				
				start = UnsafedCharUtils.uncheckedSkipBlank(source,start,true);
				if (lexema instanceof char[]) {
					final char[]	item = (char[])lexema; 
					
					if (UnsafedCharUtils.uncheckedCompare(source,start,item,0,item.length)) {
						start += item.length;
					}
					else {
						throw new SyntaxException(0,start,"Missing '"+new String(item)+"'"); 
					}
				}
				else if (lexema instanceof Character) {
					if (source[start] == ((Character)lexema).charValue()) {
						start++;
					}
					else {
						throw new SyntaxException(0,start,"Missing '"+((Character)lexema).charValue()+"'"); 
					}
				}
				else if (lexema instanceof Mark) {
					result[resultIndex[0]++] = lexema;
				}
				else if ((lexema instanceof Class<?>) && ((Class<?>)lexema).isEnum()) {
					if (Character.isJavaIdentifierStart(source[start])) {
						final int		possibleStart = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
						final String	value = new String(source, start, possibleStart-start);
						
						try{result[resultIndex[0]++] = Enum.valueOf((Class<? extends Enum>)lexema, value);
							start = possibleStart;
						} catch (IllegalArgumentException exc) {
							throw new SyntaxException(0,start,"Invalid enum value '"+value+"' for enum ["+((Class<?>)lexema).getSimpleName()+"]"); 
						}
					}
					else {
						throw new SyntaxException(0,start,"Enum value awaited"); 
					}
				}
				else if (lexema instanceof Optional) {
					final int	afterOptional = tryExtract(source, start, ((Optional)lexema).lexemas);
					
					if (afterOptional >= 0) {
						start = extract(source, start, result, resultIndex, ((Optional)lexema).lexemas);
					}
				}
				else if (lexema instanceof Choise) {
					boolean found = false;
					
					for (Object[] item : ((Choise)lexema).lexemas) {
						final int	afterChoise;
						
						afterChoise = tryExtract(source, start, item);
						if (afterChoise >= 0) {
							start = extract(source, start, result, resultIndex, item);
							found = true;
							break;
						}	
					}
					if (!found) {
						throw new SyntaxException(0,start,"Parse error: No choise detected"); 
					}
				}
				else if (lexema instanceof ArgumentType) {
					try {
						switch ((ArgumentType)lexema) {
							case hexInt			:
								start = UnsafedCharUtils.uncheckedParseHexInt(source,start,intResult,true);
								result[resultIndex[0]++] = intResult[0];
								break;
							case hexLong		:
								start = UnsafedCharUtils.uncheckedParseHexLong(source,start,longResult,true);
								result[resultIndex[0]++] = longResult[0];
								break;
							case ordinalInt		:
								start = UnsafedCharUtils.uncheckedParseInt(source,start,intResult,true);
								result[resultIndex[0]++] = intResult[0];
								break;
							case signedInt		:
								final int	intSign;
								
								if (source[start] == '-') {
									intSign = -1;
									start++;
								}
								else if (source[start] == '+') {
									intSign = 1;
									start++;
								}
								else {
									intSign = 1;
								}
								start = UnsafedCharUtils.uncheckedParseInt(source,UnsafedCharUtils.uncheckedSkipBlank(source,start,true),intResult,true);
								result[resultIndex[0]++] = intSign * intResult[0];
								break;
							case ordinalLong	:
								start = UnsafedCharUtils.uncheckedParseLong(source,start,longResult,true);
								result[resultIndex[0]++] = longResult[0];
								break;
							case ordinalFloat	:
								start = UnsafedCharUtils.uncheckedParseFloat(source,start,floatResult,true);
								result[resultIndex[0]++] = floatResult[0];
								break;
							case name			:
								start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
								result[resultIndex[0]++] = new String(source,intResult[0],intResult[1]-intResult[0]+1);
								break;
							case simpleTerminatedString	:
								if (source[start] == '\"') {
									start = UnsafedCharUtils.uncheckedParseUnescapedString(source,start+1,'\"',false,intResult);
								}
								else if (source[start] == '\'') {
									start = UnsafedCharUtils.uncheckedParseUnescapedString(source,start+1,'\'',false,intResult);
								}
								else {
									start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
								}
								result[resultIndex[0]++] = new String(source,intResult[0],intResult[1]-intResult[0]+1);
								break;
							case specialTerminatedString	:
								final StringBuilder	sb = new StringBuilder();
								
								try{if (source[start] == '\"') {
										start = UnsafedCharUtils.uncheckedParseString(source,start+1,'\"',sb);
										result[resultIndex[0]++] = sb.toString();
									}
									else if (source[start] == '\'') {
										start = UnsafedCharUtils.uncheckedParseString(source,start+1,'\'',sb);
										result[resultIndex[0]++] = sb.toString();
									}
									else {
										start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
										result[resultIndex[0]++] = new String(source,intResult[0],intResult[1]-intResult[0]+1);
									}
								} catch (IOException exc) {
									throw new SyntaxException(0,start,"Parse error: "+exc.getLocalizedMessage()); 
								}
								break;
							case hyphenedName	:
								final int	startName = start--;
								
								do {start = UnsafedCharUtils.uncheckedParseName(source,++start,intResult);
								} while (start < source.length && source[start] == '-');
								result[resultIndex[0]++] = new String(source,startName,intResult[1]-startName+1);
								break; 
							case colorRepresentation	:
								if (source[start] == '#') {	// Hex presentation
									start = UnsafedCharUtils.uncheckedParseHexInt(source,start+1,intResult,true);
									result[resultIndex[0]++] = new Color(intResult[0]);
								}
								else {
									start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
									final String	color = new String(source,intResult[0],intResult[1]-intResult[0]+1);
									
									if ((result[resultIndex[0]++] = PureLibSettings.colorByName(color,null)) == null) {
										throw new IllegalArgumentException("Unknonw color name ["+color+"]");
									}
								}
								break;
							case Boolean	:
								if (Character.isJavaIdentifierStart(source[start])) {
									start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
									final long	boolId = CONSTANTS.seekName(source, intResult[0], intResult[1]+1);
										
									if (boolId >= 0) {
										final Object	cargo = CONSTANTS.getCargo(boolId);
										
										if (cargo instanceof Boolean) {
											result[resultIndex[0]++] = cargo; 
										}
										else {
											throw new SyntaxException(0,start,"Parse error: illegal boolean value"); 
										}
									}
									else {
										throw new SyntaxException(0,start,"Parse error: illegal boolean value"); 
									}
								}
								else {
									return -(start+1);
								}
								break;
							case raw	:
								final int 	tailStart = start;
								
								while (start < source.length && source[start] != '\n' && source[start] != '\r') {	// Content to the \n
									start++;
								}
								if (start < source.length && (source[start] == '\n' || source[start] == '\r')) {
									start--;
								}
								result[resultIndex[0]++] = new String(source,tailStart,start-tailStart);
								break;
							default				:
								throw new UnsupportedOperationException("Argument type ["+lexema+"] is not supported yet"); 
						}
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(0,start,"Parse error: "+exc.getLocalizedMessage()); 
					}
				}
				else {
					throw new IllegalArgumentException("Unsupported Lexema type ["+lexema+"] at index ["+index+"]"); 
				}
			}
		}
		return start;
	}
	
	/**
	 * <p>Performs 'like' operation in char arrays (see SQL language syntax)</p>
	 * @param source source array to test
	 * @param template template to test. Wild cards of the template are (*) - any sequence, (?) - exactly one char
	 * @param from start testing like in the source
	 * @return length 'liked' if resolved, or any negative value if doesn't like
	 * @throws NullPointerException if source or template are null
	 * @throws IllegalArgumentException if from position out of range
	 * @since 0.0.3
	 */
	public static int like(final char[] source, final char[] template, final int from) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source can't be null");
		}
		else {
			return like(source, template, from, source.length-1);
		}
	}


	/**
	 * <p>Performs 'like' operation in char arrays (see SQL language syntax)</p>
	 * @param source source array to test
	 * @param template template to test. Wild cards of the template are (*) - any sequence, (?) - exactly one char
	 * @param from start testing like in the source
	 * @param to end testing like in the source
	 * @return length 'liked' if resolved, or any negative value if doesn't like
	 * @throws NullPointerException when any char array references are null
	 * @throws IllegalArgumentException when any errors in the arguments
	 * @since 0.0.3
	 */
	public static int like(final char[] source, final char[] template, final int from, final int to) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source can't be null");
		}
		else if (template == null) {
			throw new NullPointerException("Template can't be null");
		}
		else if (source.length == 0 || template.length == 0) {
			return -1;
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(source.length-1));
		}
		else if (to < 0 || to >= source.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(source.length-1));
		}
		else if (to < from) {
			throw new IllegalArgumentException("To position ["+to+"] is less than from ["+from+"]");
		}
		else {
			return likeInternal(source,template,from,to,0);
		}
	}

	private static int likeInternal(final char[] source, final char[] template, final int from, final int to, final int templateFrom) {
		int	asteriskCount =  0;
		
		for (int index = templateFrom, maxIndex = template.length; index < maxIndex; index++) {
			if (template[index] == WILDCARD_ANY_SEQ) {
				asteriskCount++;
			}
		}
		if (asteriskCount == 0) {
			if (to - from != template.length - templateFrom - 1) {
				return -template.length;
			}
			else {
				for (int index = 0, maxIndex = template.length-templateFrom; index < maxIndex; index++) {
					if (source[from+index] != template[index + templateFrom] && template[index + templateFrom] != WILDCARD_ANY_CHAR) {
						return -template.length;
					}
				}
				return from + template.length - templateFrom;
			}				
		}
		else {
			for (int start = 0, index = 0, maxIndex = template.length; index < maxIndex && start + from <= to; index++, start++) {
				if (source[start + from] != template[index + templateFrom] && template[index + templateFrom] != WILDCARD_ANY_CHAR) {
					if (template[index + templateFrom] != WILDCARD_ANY_SEQ) {
						return -template.length;
					}
					else if (index + templateFrom == template.length - 1) {
						return to+1;
					}
					else {
						for(; start + from <= to; start++) {
							int result = likeInternal(source,template,start+from,to,index+templateFrom+1);
							
							if (result > 0) {
								return result;
							}
						}
						return -template.length;
					}
				}
			}
			return from + template.length - templateFrom;
		}
	}
	
	/**
	 * <p>Print long number to the char array</p>
	 * @param content char array to fill with long
	 * @param from start position in the char array to fill
	 * @param value long value to fill to content
	 * @param reallyFill weather really fill content. When false, only total filled size will be calculated
	 * @return new from position to continue filling content. Negative value marks that content is too short to keep value. It's
	 * absolute value exactly marks new from position to continue filling 
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 */
	public static int printLong(final char[] content, final int from, final long value, boolean reallyFill) throws IllegalArgumentException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content buffer can't be null or empty array"); 
		}
		else if (from < 0) {
			throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
		}
		else {
			return UnsafedCharUtils.uncheckedPrintLong(content,from,value,reallyFill);
		}
	}

	/**
	 * <p>Print double number to the char array</p>
	 * @param content char array to fill with long
	 * @param from start position in the char array to fill
	 * @param value double value to fill to content
	 * @param reallyFill weather really fill content. When false, only total filled size will be calculated
	 * @return new from position to continue filling content. Negative value marks that content is too short to keep value. It's
	 * absolute value exactly marks new from position to continue filling 
	 * @throws IllegalArgumentException if any argument errors ware detected 
	 */
	public static int printDouble(final char[] content, final int from, double value, boolean reallyFill) throws IllegalArgumentException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content buffer can't be null or empty array"); 
		}
		else if (from < 0) {
			throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
		}
		else {
			return UnsafedCharUtils.uncheckedPrintDouble(content,from,value,reallyFill);
		}
	}

	/**
	 * <p>Print char to char array with escaping it if need</p> 
	 * @param content char array to fill with long
	 * @param from start position in the char array to fill
	 * @param value char value to fill to content
	 * @param reallyFill weather really fill content. When false, only total filled size will be calculated
	 * @param strongEscaping true strongly escapes all chars &gt; 0xFF. False makes smart analyze 
	 * @return new from position to continue filling content. Negative value marks that content is too short to keep value.
	 * @throws IllegalArgumentException on any argument errors
	 * @since 0.0.2
	 */
	public static int printEscapedChar(final char[] content, final int from, final char value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content buffer can't be null or empty array"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1)); 
		}
		else {
			return UnsafedCharUtils.uncheckedPrintEscapedChar(content,from,value,reallyFill,strongEscaping);
		}
	}

	
	/**
	 * <p>Print string to the char array with escaping if need</p>
	 * @param content char array to fill with long
	 * @param from start position in the char array to fill
	 * @param value string value to fill to content
	 * @param reallyFill weather really fill content. When false, only total filled size will be calculated
	 * @param strongEscaping true strongly escapes all chars &gt; 0xFF. False makes smart analyze 
	 * @return new from position to continue filling content. Negative value marks that content is too short to keep value.
	 * @throws IllegalArgumentException on any argument errors
	 * @since 0.0.2
	 * @lastUpdate 0.0.3
	 */
	public static int printEscapedString(final char[] content, final int from, final String value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		int		len;
		
		if (value == null) {
			throw new IllegalArgumentException("String to print can't be null");
		}
		else if ((len = value.length()) == 0) {
			return from;
		}
		else {
			return printEscapedCharArray(content,from,value.toCharArray(),0,len,reallyFill,strongEscaping); 
		}
	}

	
	/**
	 * <p>Print string to the char array with escaping if need</p>
	 * @param content char array to fill with long
	 * @param from start position in the char array to fill
	 * @param value char array value to fill to content
	 * @param reallyFill weather really fill content. When false, only total filled size will be calculated
	 * @param strongEscaping true strongly escapes all chars &gt; 0xFF. False makes smart analyze 
	 * @return new from position to continue filling content. Negative value marks that content is too short to keep value.
	 * @throws IllegalArgumentException on any argument errors
	 * @since 0.0.2
	 * @lastUpdate 0.0.3
	 */
	public static int printEscapedCharArray(final char[] content, final int from, final char[] value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		if (value == null) {
			throw new IllegalArgumentException("VLaue can't be null array"); 
		}
		else {
			return printEscapedCharArray(content,from,value,0,value.length,reallyFill,strongEscaping); 
		}
	}

	/**
	 * <p>Print string to the char array with escaping if need. String terminators will not be printed, so you must print it yourself</p>
	 * @param content char array to fill with long
	 * @param from start position in the char array to fill
	 * @param value char array value to fill to content
	 * @param charFrom start position inside char array to fill from
	 * @param charTo end position inside char array to fill from
	 * @param reallyFill weather really fill content. When false, only total filled size will be calculated
	 * @param strongEscaping true strongly escapes all chars &gt; 0xFF. False makes smart analyze 
	 * @return new from position to continue filling content. Negative value marks that content is too short to keep value.
	 * @throws IllegalArgumentException on any argument errors
	 * @since 0.0.2
	 * @lastUpdate 0.0.4
	 */
	public static int printEscapedCharArray(final char[] content, int from, final char[] value, final int charFrom, final int charTo, boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content buffer can't be null or empty array"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1)); 
		}
		else if (value == null) {
			throw new IllegalArgumentException("Value can't be null array"); 
		}
		else if (charFrom < 0 || charFrom > value.length) {
			throw new IllegalArgumentException("CharFrom position ["+charFrom+"] out of range 0.."+(value.length-1)); 
		}
		else if (charTo < 0 || charTo > value.length) {
			throw new IllegalArgumentException("CharTo position ["+charTo+"] out of range 0.."+(value.length-1)); 
		}
		else {
			return UnsafedCharUtils.uncheckedPrintEscapedCharArray(content, from, value, charFrom, charTo, reallyFill, strongEscaping);
		}
	}
	
	/**
	 * <p>This interface describes lambda-styled callback for {@linkplain CharUtils#substitute(String, String, SubstitutionSource)} call. 
	 * The only functionality it must support is get appropriative string value for string key requested.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @lastUpdate 0.0.6
	 */
	@FunctionalInterface
	public interface SubstitutionSource {
		/**
		 * <p>Get value for the given key</p>
		 * @param key key to get value for
		 * @return value got. Can be null
		 */
		String getValue(String key);
		
		/**
		 * <p>Test substitution has the given key</p>
		 * @param key key to test
		 * @return true if has
		 * @since 0.0.6
		 */
		default boolean hasKey(String key) {
			return true;
		}

		/**
		 * <p>Build total substitution source from list of sources. It substitute values from the first sequential substitution, that returns true on call
		 * {@linkplain #hasKey(String)} method. If no one returns true, return value will be empty string</p>   
		 * @param list list of substitution sources. Can't be null or empty, and can't contains nulls inside
		 * @return substitution source. Can't be null
		 * @throws IllegalArgumentException when parameter list is null, empty or contains nulls inside 
		 * @since 0.0.6
		 */
		static SubstitutionSource of(final SubstitutionSource... list) throws IllegalArgumentException {
			if (list == null || list.length == 0 || Utils.checkArrayContent4Nulls(list) >= 0) {
				throw new IllegalArgumentException("Substitution list is null, empty or contains nulls inside");
			}
			else {
				return new SubstitutionSource() {
					@Override
					public String getValue(final String data) {
						for (SubstitutionSource item : list) {
							if (item.hasKey(data)) {
								return item.getValue(data);
							}
						}
						return EMPTY_STRING;
					}
					
					@Override
					public boolean hasKey(final String data) {
						for (SubstitutionSource item : list) {
							if (item.hasKey(data)) {
								return true;
							}
						}
						return false;
					}
				};
			}
		}
	}
	
	/**
	 * <p>This interface describes lambda-styled callback for {@linkplain CharUtils#substitute(String, String, SubstitutionSource)} call
	 * The only functionality it must support is get appropriative char array value for char array key requested.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @lastUpdate 0.0.6
	 */
	@FunctionalInterface
	public interface CharSubstitutionSource {
		/**
		 * <p>Get value for the given key</p>
		 * @param data data containing key
		 * @param from key name start index inside data 
		 * @param to key name end index inside data
		 * @return value got. Can be null
		 */
		char[] getValue(char[] data, int from, int to);
		
		/**
		 * <p>Test substitution has the given key</p>
		 * @return true if has
		 * @since 0.0.6
		 */
		default boolean hasKey(char[] data, int from, int to) {
			return true;
		}

		/**
		 * <p>Build total substitution source from list of sources. It substitute values from the first sequential substitution, that returns true on call
		 * {@linkplain #hasKey(char[], int, int)} method. If no one returns true, return value will be empty char[]</p>   
		 * @param list list of substitution sources. Can't be null or empty, and can't contains nulls inside
		 * @return substitution source. Can't be null
		 * @throws IllegalArgumentException when parameter list is null, empty or contains nulls inside 
		 * @since 0.0.6
		 */
		static CharSubstitutionSource of(final CharSubstitutionSource... list) throws IllegalArgumentException {
			if (list == null || list.length == 0 || Utils.checkArrayContent4Nulls(list) >= 0) {
				throw new IllegalArgumentException("Substitution list is null, empty or contains nulls inside");
			}
			else {
				return new CharSubstitutionSource() {
					@Override
					public char[] getValue(final char[] data, final int from, final int to) {
						for (CharSubstitutionSource item : list) {
							if (item.hasKey(data, from, to)) {
								return item.getValue(data, from, to);
							}
						}
						return EMPTY_CHAR_ARRAY;
					}
					
					@Override
					public boolean hasKey(final char[] data, final int from, final int to) {
						for (CharSubstitutionSource item : list) {
							if (item.hasKey(data, from, to)) {
								return true;
							}
						}
						return false;
					}
				};
			}
		}
	}
	
	/**
	 * <p>Substitute string content. Seeks the <b>${</b>&lt;key_to_substitute&gt;<b>}</b> sequence inside string value and substitute it with the
	 * <i>key_to_substitute</i> value. This value will be got by calling {@linkplain SubstitutionSource#getValue(String)} method.</p> 
	 * @param key optional key to identify string value. Will be used in the exceptions if will be thrown
	 * @param value value for substitution. 
	 * @param source interface to get values for keys detected
	 * @return value with substituted keys
	 * @throws NullPointerException when any parameters is null 
	 * @throws IllegalArgumentException when any problems during substitution were detected 
	 * @since 0.0.2
	 */
	public static String substitute(final String key, final String value, final SubstitutionSource source) throws NullPointerException, IllegalArgumentException {
		if (value == null) {
			return null; 
		}
		else if (source == null) {
			throw new NullPointerException("Substitution source can't be null"); 
		}
		else {
			return new String(UnsafedCharUtils.uncheckedSubstitute(key,value.toCharArray(),0,value.length(),new CharSubstitutionSourceWrapper(source),0));
		}
	}

	/**
	 * <p>Substitute string content. Seeks the <b>${</b>&lt;key_to_substitute&gt;<b>}</b> sequence inside string value and substitute it with the
	 * <i>key_to_substitute</i> value. This value will be got by calling {@linkplain SubstitutionSource#getValue(String)} method.</p> 
	 * @param key optional key to identify string value. Will be used in the exceptions if will be thrown
	 * @param value char array for substitution. 
	 * @param from start position in the char array to process substitution
	 * @param length length of the content portion to process
	 * @param source interface to get values for keys detected
	 * @return value with substituted keys
	 * @throws NullPointerException when any parameters is null 
	 * @throws IllegalArgumentException when any problems during substitution were detected 
	 * @since 0.0.2
	 */
	public static char[] substitute(final String key, final char[] value, final int from, final int length, final CharSubstitutionSource source) throws NullPointerException, IllegalArgumentException {
		if (value == null) {
			return null; 
		}
		else if (from < 0 || from >= value.length) {
			throw new IllegalArgumentException("From index ["+from+"] out of range. Valid range is 0.."+(value.length-1)); 
		}
		else if (from+length < 0 || from+length > value.length) {
			throw new IllegalArgumentException("From index + length ["+(from+length)+"] out of range. Valid range is 0.."+(value.length)); 
		}
		else if (source == null) {
			throw new NullPointerException("Substitution source can't be null"); 
		}
		else {
			return UnsafedCharUtils.uncheckedSubstitute(key,value,from,length,source,0);
		}
	}
	
	private static class CharSubstitutionSourceWrapper implements CharSubstitutionSource {
		private final SubstitutionSource	nested;
		
		CharSubstitutionSourceWrapper(final SubstitutionSource nested) {
			this.nested = nested;
		}

		@Override
		public char[] getValue(char[] data, int from, int to) {
			final String	result = nested.getValue(new String(data,from,to-from));
			
			return result == null ? null : result.toCharArray();
		}
	}
	
	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>	
	 * @param source string to split
	 * @param splitter splitter char
	 * @return string array with splitter string. Always contains at least one element
	 * @throws NullPointerException when source string is null
	 * @since 0.0.2
	 */
	public static String[] split(final String source, final char splitter) throws NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else {
			return split(source, splitter, false, false);
		}
	}

	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>	
	 * @param source string to split
	 * @param splitter splitter char
	 * @param removeCornerEmpty remove corner elements from result, if they are empty 
	 * @param removeTotalEmpty remove all empty elements from result
	 * @return string array with splitter string. Always contains at least one element
	 * @throws NullPointerException when source string is null
	 * @since 0.0.4
	 */
	public static String[] split(final String source, final char splitter, final boolean removeCornerEmpty, final boolean removeTotalEmpty) throws NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else {
			return UnsafedCharUtils.split(source, splitter, removeCornerEmpty, removeTotalEmpty);
		}
	}
	
	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>
	 * @param source string to split
	 * @param splitter splitter char
	 * @param target target array to receive strings splitted
	 * @return non-negative number - number of pieces of the splitted string, negative number - -(number of pieces of the splitted string + 1).
	 * Negative number means that target array is too small to receive content (nothing will be filled in this case)
	 * @throws NullPointerException when either source or target parameter is null
	 * @since 0.0.2
	 */
	public static int split(final String source, final char splitter, final String[] target) throws NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (target == null) {
			throw new NullPointerException("Traget string array can't be null"); 
		}
		else {
			return split(source, splitter, false, false, target);
		}
	}

	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>
	 * @param source string to split
	 * @param splitter splitter char
	 * @param removeCornerEmpty remove corner elements from result, if they are empty 
	 * @param removeTotalEmpty remove all empty elements from result
	 * @param target target array to receive strings splitted
	 * @return non-negative number - number of pieces of the splitted string, negative number - -(number of pieces of the splitted string + 1).
	 * Negative number means that target array is too small to receive content (nothing will be filled in this case)
	 * @throws NullPointerException when either source or target parameter is null
	 * @since 0.0.4
	 */
	public static int split(final String source, final char splitter, final boolean removeCornerEmpty, final boolean removeTotalEmpty, final String[] target) throws NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (target == null) {
			throw new NullPointerException("Traget string array can't be null"); 
		}
		else {
			return UnsafedCharUtils.split(source, splitter, removeCornerEmpty, removeTotalEmpty, target);
		}
	}
	
	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>	
	 * @param source string to split
	 * @param splitter splitter char
	 * @return string array with splitter string. Always contains at least one element
	 * @throws NullPointerException when source string is null
	 * @throws IllegalArgumentException when splitter is null or empty
	 * @since 0.0.2
	 */
	public static String[] split(final String source, final String splitter) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (splitter == null || splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitter string can't be null or empty"); 
		}
		else {
			return split(source, splitter, false, false);
		}
	}

	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>
	 * @param source string to split
	 * @param splitter splitter char
	 * @param removeCornerEmpty remove corner elements from result, if they are empty 
	 * @param removeTotalEmpty remove all empty elements from result
	 * @return string array with splitter string. Always contains at least one element
	 * @throws NullPointerException when source string is null
	 * @throws IllegalArgumentException when splitter is null or empty
	 * @since 0.0.4
	 */
	public static String[] split(final String source, final String splitter, final boolean removeCornerEmpty, final boolean removeTotalEmpty) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (splitter == null || splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitter string can't be null or empty"); 
		}
		else {
			return UnsafedCharUtils.split(source, splitter, removeCornerEmpty, removeTotalEmpty);
		}
	}
	
	
	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>
	 * @param source string to split
	 * @param splitter splitter char
	 * @param target target array to receive strings splitted
	 * @return non-negative number - number of pieces of the splitted string, negative number - -(number of pieces of the splitted string + 1).
	 * Negative number means that target array is too small to receive content (nothing will be filled in this case)
	 * @throws NullPointerException when either source or target parameter is null
	 * @throws IllegalArgumentException when splitter is null or empty
	 * @since 0.0.2
	 */
	public static int split(final String source, final String splitter, final String[] target) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (splitter == null || splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitter string can't be null or empty"); 
		}
		else if (target == null) {
			throw new NullPointerException("Traget string array can't be null"); 
		}
		else {
			return split(source, splitter, false, false, target);
		}
	}

	/**
	 * <p>Split string into string arrays. Against {@linkplain String#split(String)} doesn't use regular expressions</p>
	 * @param source string to split
	 * @param splitter splitter char
	 * @param removeCornerEmpty remove corner elements from result, if they are empty 
	 * @param removeTotalEmpty remove all empty elements from result
	 * @param target target array to receive strings splitted
	 * @return non-negative number - number of pieces of the splitted string, negative number - -(number of pieces of the splitted string + 1).
	 * Negative number means that target array is too small to receive content (nothing will be filled in this case)
	 * @throws NullPointerException when either source or target parameter is null
	 * @throws IllegalArgumentException when splitter is null or empty
	 * @since 0.0.2
	 */
	public static int split(final String source, final String splitter, final boolean removeCornerEmpty, final boolean removeTotalEmpty, final String[] target) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (splitter == null || splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitter string can't be null or empty"); 
		}
		else if (target == null) {
			throw new NullPointerException("Traget string array can't be null"); 
		}
		else {
			return UnsafedCharUtils.split(source, splitter, removeCornerEmpty, removeTotalEmpty, target);
		}
	}
	
	/**
	 * <p>Concatenate character content to character array using given delimiters</p>
	 * @param delimiter delimiter to use
	 * @param content content to concatenate. Can't contain nulls
	 * @return char array concatenated. Can't be null
	 * @throws IllegalArgumentException any parameters errors
	 * @throws NullPointerException any parameters errors
	 * @since 0.0.3
	 */
	public static char[] join(final char[] delimiter, final char[]... content) throws IllegalArgumentException, NullPointerException {
		if (delimiter == null || delimiter.length == 0) {
			throw new IllegalArgumentException("String delimiter can't be null or empty array");
		}
		else if (content == null) {
			throw new NullPointerException("Content to join can't be null");
		}
		else {
			return UnsafedCharUtils.join(delimiter, content);
		}
	}
	
	/**
	 * <p>Concatenate string content to string array using given delimiters</p>
	 * @param delimiter delimiter to use
	 * @param content content to concatenate. Can't contain nulls
	 * @return String concatenated. Can't be null
	 * @throws IllegalArgumentException any parameters errors
	 * @throws NullPointerException any parameters errors
	 * @since 0.0.3
	 * @lastUpdate 0.0.6
	 */
	public static String join(final String delimiter, final CharSequence... content) throws IllegalArgumentException, NullPointerException {
		if (delimiter == null || delimiter.isEmpty()) {
			throw new IllegalArgumentException("String delimiter can't be null or empty");
		}
		else if (content == null) {
			throw new NullPointerException("Content to join can't be null");
		}
		else if (Utils.checkArrayContent4Nulls(content) != -1) {
			throw new NullPointerException("Nulls inside content");
		}
		else {
			return UnsafedCharUtils.join(delimiter, content);
		}
	}

	/**
	 * <p>Terminate string content with the given terminal symbol and convert result to char array. This method is useful for string parsers
	 * to exclude end-of-string checking from parser code</p>
	 * @param content content to terminate and convert
	 * @param terminal terminal to terminate content
	 * @return content converted
	 * @throws NullPointerException when content string is null
	 * @since 0.0.4
	 * @lastUpdate 0.0.6
	 */
	public static char[] terminateAndConvert2CharArray(final CharSequence content, final char terminal) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("COntent string can't be null");
		}
		else {
			final char[]	result = new char[content.length()+1];
			
			for(int index = 0, maxIndex = result.length-1; index < maxIndex; index++) {
				result[index] = content.charAt(index);
			}
			result[result.length-1] = terminal;
			return result;
		}
	}

	/**
	 * <p>Prepare string with non-printable chars inside to escaped form (for example '\n'-&gt;'\\n')</p>
	 * @param content content to prepare
	 * @return prepared string. Can't be null
	 * @throws NullPointerException string to prepare is null
	 * @since 0.0.4
	 */
	public static String escapeStringContent(final String content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to escape can't be null");
		}
		else {
			int		length = content.length(), additionalLength = 0;
			
			for (int index = 0; index < length; index++) {
				additionalLength += howManyEscapedCharsOccupies(content.charAt(index),false) - 1;
			}
			if (additionalLength > 0) {
				final char[]	temp = new char[length+additionalLength];
				char			currentChar;
				
				for (int index = 0, where = 0; index < length; index++) {
					currentChar = content.charAt(index);
					
					where = printEscapedChar(temp,where,currentChar,true,false);
				}
				return new String(temp);
			}
			else {
				return content;
			}
		}
	}

	/**
	 * <p>Prepare string with escape sequences inside to it's internal representation (for example '\\n'-&gt;'\n')</p> 
	 * @param content content to prepare
	 * @return prepared string. Can't be null
	 * @throws NullPointerException string to prepare is null
	 * @throws IllegalArgumentException wrong escape sequences inside
	 * @since 0.0.4
	 */
	public static String unescapeStringContent(final String content) throws NullPointerException, IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("Content to escape can't be null");
		}
		else if (content.indexOf('\\') < 0) {
			return content;
		}
		else {
			final char[]	temp = new char[content.length()];
			int				start = 0;
			char			currentChar;
			
			for (int index = 0, maxIndex = temp.length; index < maxIndex; index++) {
				currentChar = content.charAt(index);
				
				if (currentChar != '\\') {
					temp[start++] = currentChar;
				}
				else if (index < maxIndex - 1) {
					switch (content.charAt(index+1)) {
						case 'b' 	:
							temp[start++] = '\b';
							index++;
							break;
						case 'f' 	:
							temp[start++] = '\f';
							index++;
							break;
						case 'n' 	:
							temp[start++] = '\n';
							index++;
							break;
						case 'r' 	:
							temp[start++] = '\r';
							index++;
							break;
						case 't' 	:
							temp[start++] = '\t';
							index++;
							break;
						case '\'' 	:
							temp[start++] = '\'';
							index++;
							break;
						case '\"' 	:
							temp[start++] = '\"';
							index++;
							break;
						case '\\' 	:
							temp[start++] = '\\';
							index++;
							break;
						case '0' 	:
							int	octal = 0;
							
							index++;
							while (index < maxIndex && (currentChar = content.charAt(index)) >= '0' && currentChar <= '7') {
								octal = 8 * octal + currentChar - '0';
								index++;
							}
							temp[start++] = (char)octal;
							index--;
							break;
						case 'u' 	:
							int	hex = 0;
							
							index += 2;
							while (index < maxIndex && ((currentChar = content.charAt(index)) >= '0' && currentChar <= '9' || currentChar >= 'a' && currentChar <= 'f' || currentChar >= 'A' && currentChar <= 'F') ) {
								if (currentChar >= '0' && currentChar <= '9') {
									hex = 16 * hex + currentChar - '0';
								}
								else if (currentChar >= 'a' && currentChar <= 'f') {
									hex = 16 * hex + currentChar - 'a' + 10;
								}
								else {
									hex = 16 * hex + currentChar - 'A' + 10;
								}
								index++;
							}
							index--;
							temp[start++] = (char)hex;
							break;
						case 'U' 	:
							int	hexLong = 0;
							
							index += 2;
							while (index < maxIndex && ((currentChar = content.charAt(index)) >= '0' && currentChar <= '9' || currentChar >= 'a' && currentChar <= 'f' || currentChar >= 'A' && currentChar <= 'F') ) {
								if (currentChar >= '0' && currentChar <= '9') {
									hexLong = 16 * hexLong + currentChar - '0';
								}
								else if (currentChar >= 'a' && currentChar <= 'f') {
									hexLong = 16 * hexLong + currentChar - 'a' + 10;
								}
								else {
									hexLong = 16 * hexLong + currentChar - 'A' + 10;
								}
								index++;
							}
							index--;
							temp[start++] = Character.highSurrogate(hexLong);
							temp[start++] = Character.lowSurrogate(hexLong);
							break;
						default :
							throw new IllegalArgumentException("Wrong escape sequence at position ["+index+"]");
 					}
				}
				else {
					throw new IllegalArgumentException("Wrong escape sequence at the end of sting");
				}
			}
			return new String(temp,0,start);
		}
	}

	/**
	 * <p>Create {@linkplain CharSequence} wrapper for the given piece of char array</p>
	 * @param content char array to make wrapper for
	 * @param from start index inside char array to make sequence
	 * @param to end index inside char array to make sequence
	 * @return wrapper created. Can't be null
	 * @throws NullPointerException if char array reference is null
	 * @throws IllegalArgumentException from or to indices out of range
	 */
	public static CharSequence toCharSequence(final char[] content, final int from, final int to) throws NullPointerException, IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("Content to make sequence for can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From index ["+from+"] out of range 0.."+(content.length-1)); 
		}
		else if (to < 0 || to >= content.length) {
			throw new IllegalArgumentException("To index ["+to+"] out of range 0.."+(content.length-1)); 
		}
		else if (to < from) {
			throw new IllegalArgumentException("To index ["+to+"] less than from index ["+from+"]"); 
		}
		else {
			return new OrdinalCharSequence(content, from, to);
		}
	}
	
	/**
	 * <p>Create {@linkplain CharSequence} wrapper for the given piece of char array. Differ to {@linkplain #toCharSequence(char[], int, int)}, makes weak reference for wrapper content</p>
	 * @param content char array to make wrapper for
	 * @param from start index inside char array to make sequence
	 * @param to end index inside char array to make sequence
	 * @return wrapper created. Can't be null
	 * @throws NullPointerException if char array reference is null
	 * @throws IllegalArgumentException from or to indices out of range
	 */
	public static CharSequence toWeakCharSequence(final char[] content, final int from, final int to) throws NullPointerException, IllegalArgumentException {
		if (content == null) {
			throw new NullPointerException("Content to make sequence for can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From index ["+from+"] out of range 0.."+(content.length-1)); 
		}
		else if (to < 0 || to >= content.length) {
			throw new IllegalArgumentException("To index ["+to+"] out of range 0.."+(content.length-1)); 
		}
		else if (to < from) {
			throw new IllegalArgumentException("To index ["+to+"] less than from index ["+from+"]"); 
		}
		else {
			return new WeakCharSequence(content, from, to);
		}
	}

	/**
	 * <p>Create space-filled char array with the given length</p 
	 * @param len length of array to create
	 * @return array created. Can't be null
	 * @throws IllegalArgumentException if length < 0
	 * @since 0.0.4
	 */
	public static char[] space(final int len) throws IllegalArgumentException {
		if (len < 0) {
			throw new IllegalArgumentException("Array length ["+len+"] can't be negative");
		}
		else {
			final char[]	result = new char[len];
			
			Arrays.fill(result, ' ');
			return result;
		}
	}

	/**
	 * <p>This enumeration describes adjustment type for {@linkplain CharUtils#fillInto(String, int, boolean, FillingAdjstment)} methods
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.4
	 */
	public enum FillingAdjstment {
		LEFT, RIGHT, CENTER, JUSTIFY
	}
	
	/**
	 * <p>Build content with the given length and space placements</p>
	 * @param source source content to fill and place into target
	 * @param awaitedLen awaited length of placed content
	 * @param truncExtra truncate extra characters to fill into given length 
	 * @param adjustment how fill spaces when source content length is less than awaited
	 * @return content filled. Can be empty but not null
	 * @throws NullPointerException when any referenced argument is null
	 * @throws IllegalArgumentException on any errors in parameters
	 * @see #fillInto(char[], int, int, int, boolean, FillingAdjstment)
	 * @see #fillInto(char[], int, int, char[], int, int, boolean, FillingAdjstment)
	 * @since 0.0.4
	 */
	public static String fillInto(final String source, final int awaitedLen, final boolean truncExtra, final FillingAdjstment adjustment) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source string can't be null"); 
		}
		else if (awaitedLen < 0) {
			throw new IllegalArgumentException("Awaited length ["+awaitedLen+"] can't be negative"); 
		}
		else if (adjustment == null) {
			throw new NullPointerException("Filling adjustment can't be null"); 
		}
		else {
			return new String(fillInto(source.toCharArray(), 0, source.length(), awaitedLen, truncExtra, adjustment));
		}
	}
	
	/**
	 * <p>Build content with the given length and space placements</p>
	 * @param source source content to fill and place into target
	 * @param from from position of the source content
	 * @param to to position of the source content
	 * @param awaitedLen awaited length of placed content
	 * @param truncExtra truncate extra characters to fill into given length 
	 * @param adjustment how fill spaces when source content length is less than awaited
	 * @return content filled. Can be empty but not null
	 * @throws NullPointerException when any referenced argument is null
	 * @throws IllegalArgumentException on any errors in parameters
	 * @see #fillInto(String, int, boolean, FillingAdjstment)
	 * @see #fillInto(char[], int, int, char[], int, int, boolean, FillingAdjstment)
	 * @since 0.0.4
	 */
	public static char[] fillInto(final char[] source, final int from, final int to, final int awaitedLen, final boolean truncExtra, final FillingAdjstment adjustment) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else if (from < 0 || from > source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+source.length); 
		}
		else if (to < 0 || to > source.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+source.length); 
		}
		else if (from > to) {
			throw new IllegalArgumentException("From position ["+from+"] greater than to position ["+to+"]"); 
		}
		else if (awaitedLen < 0) {
			throw new IllegalArgumentException("Awaited length ["+awaitedLen+"] can't be negative"); 
		}
		else if (adjustment == null) {
			throw new NullPointerException("Filling adjustment can't be null"); 
		}
		else {
			final char[]	result = new char[truncExtra ? awaitedLen : Math.max(awaitedLen, to-from)];
			
			fillInto(source, from, to, result, 0, awaitedLen, truncExtra, adjustment);
			return result;
		}
	}
	
	/**
	 * <p>Fill target content with adjusted source content and return target location after filling</p>
	 * @param source source content to fill and place into target
	 * @param from from position of the source content
	 * @param to to position of the source content
	 * @param target target location to place content into
	 * @param targetFrom from position of target location
	 * @param awaitedLen awaited length of placed content
	 * @param truncExtra truncate extra characters to fill into given length 
	 * @param adjustment how fill spaces when source content length is less than awaited
	 * @return target location after placing content into
	 * @throws NullPointerException when any referenced argument is null
	 * @throws IllegalArgumentException on any errors in parameters
	 * @see #fillInto(String, int, boolean, FillingAdjstment)
	 * @see #fillInto(char[], int, int, int, boolean, FillingAdjstment)
	 * @since 0.0.4
	 */
	public static int fillInto(final char[] source, final int from, final int to, final char[] target, final int targetFrom, final int awaitedLen, final boolean truncExtra, final FillingAdjstment adjustment) throws NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source array can't be null"); 
		}
		else if (from < 0 || from > source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+source.length); 
		}
		else if (to < 0 || to > source.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+source.length); 
		}
		else if (from > to) {
			throw new IllegalArgumentException("From position ["+from+"] greater than to position ["+to+"]"); 
		}
		else if (target == null) {
			throw new NullPointerException("Target array can't be null"); 
		}
		else if (targetFrom < 0 || targetFrom > target.length) {
			throw new IllegalArgumentException("Target from position ["+targetFrom+"] out of range 0.."+target.length); 
		}
		else if (awaitedLen < 0) {
			throw new IllegalArgumentException("Awaited length ["+awaitedLen+"] can't be negative"); 
		}
		else if (adjustment == null) {
			throw new NullPointerException("Filling adjustment can't be null"); 
		}
		else {
			final int	targetTo = Math.min(targetFrom + (truncExtra ? awaitedLen : Math.max(awaitedLen, to - from)), target.length);
			int			sourceFrom = from, sourceTo = to, sourceLen;
			
			while (sourceFrom <= to && source[sourceFrom] <= ' ') {
				sourceFrom++;
			}
			while (sourceTo-1 >= from && source[sourceTo-1] <= ' ') {
				sourceTo--;
			}
			sourceLen = sourceTo - sourceFrom;
			
			if (sourceFrom == sourceTo) {
				Arrays.fill(target, targetFrom, targetTo, ' ');
			}
			else if (sourceLen >= awaitedLen) {
				System.arraycopy(source, sourceFrom, target, targetFrom, targetTo - targetFrom);
			}
			else {
				switch (adjustment) {
					case CENTER		:
						final int	sourceLeft = (awaitedLen - sourceLen) / 2, sourceRight = awaitedLen - sourceLeft;
						
						Arrays.fill(target, targetFrom, targetFrom + sourceLeft, ' ');
						System.arraycopy(source, sourceFrom, target, targetFrom + sourceLeft, sourceLen);
						Arrays.fill(target, targetFrom + sourceLeft + sourceLen, targetTo, ' ');
						break;
					case JUSTIFY	:
						int	blankCount = 0;
						
						for (int index = sourceFrom; index < sourceTo; index++) {
							if (source[index] <= ' ') {
								blankCount++;
							}
						}
						
						if (blankCount > 0) {
							final int	extraBlank = (awaitedLen - sourceLen) / blankCount;
							int			targetPos = targetFrom;
							
							for (int index = sourceFrom; index < sourceTo; index++) {
								if ((target[targetPos++] = source[index]) <= ' ') {
									for (int extra = 0; extra < extraBlank; extra++) {
										target[targetPos++] = ' ';
									}
								}
							}
							for (int extra = targetPos; extra < targetTo; extra++) {
								target[extra] = ' ';
							}
							break;
						}
						// break is NOT required here! 
					case LEFT		:
						System.arraycopy(source, sourceFrom, target, targetFrom, sourceLen);
						Arrays.fill(target, targetFrom + sourceLen, targetTo, ' ');
						break;
					case RIGHT		:
						Arrays.fill(target, targetFrom, targetTo - sourceLen, ' ');
						System.arraycopy(source, sourceFrom, target, targetTo - sourceLen, sourceLen);
						break;
					default:
						throw new UnsupportedOperationException("Adjustment ["+adjustment+"] is not supported yet");
				}
			}
			return targetTo;
		}
	}
		
	
	/**
	 * <p>Calculate number of given characters in the string sequence</p>
	 * @param seq sequence to calculate characters in
	 * @param symbol character to seek
	 * @return number of characters found. Can't bbe negative
	 * @throws NullPointerException when sequence is null
	 * @since 0.0.4
	 */
	public static int howManyChars(final CharSequence seq, final char symbol) throws NullPointerException {
		if (seq == null) {
			throw new NullPointerException("Char sequence to test number of char can't be null");
		}
		else if (seq.length() == 0) {
			return 0;
		}
		else {
			int		count = 0;
			
			for (int index = 0, maxIndex = seq.length(); index < maxIndex; index++) {
				if (seq.charAt(index) == symbol) {
					count++;
				}
			}
			return count;
		}
	}

	public static boolean isASCIIOnly(final char[] source, final int from, final int to) {
		for (int index = from; index < to; index++) {
			if (source[index] > 127) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * <p>This class describes Levenstain distance and editor prescription for two strins.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 */
    public static class Prescription {
    	public static final int 	LEV_DELETE = 1;
    	public static final int 	LEV_REPLACE = 2;
    	public static final int 	LEV_INSERT = 3;
    	public static final int 	LEV_NONE = 4;
    	
		public int[][] route;
		public int distance;
	        
		Prescription(int distance, int[][] route) {
			this.distance = distance;
			this.route = route;
		}
	
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Prescription(dist="+distance+") :");
     
            for (int index = 0; index < route.length; index++) {
                sb.append('\n').append(index).append(" : ");
                switch (route[index][0]) {
                    case LEV_DELETE : sb.append("delete "); break;
                    case LEV_REPLACE : sb.append("replace "); break;
                    case LEV_INSERT : sb.append("insert "); break;
                    case LEV_NONE : sb.append("not changed "); break;
                }
                sb.append(' ').append(route[index][1]).append(" and ").append(route[index][2]);
            }
            return sb.toString();
        }
    }
	
	/**
	 * <P>Calculate Levenstain distance and editor prescription for two strings</p> 
	 * @param str1 string to calculate difference for. Can't be null
	 * @param str2 string to use as template. Can't be null
	 * @return prescription list and Levenstain distance. Can't be null
	 * @throws NullPointerException on any argument is null
	 * @see https://ru.wikibooks.org/wiki/%D0%A0%D0%B5%D0%B0%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D0%B8_%D0%B0%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC%D0%BE%D0%B2/%D0%A0%D0%B5%D0%B4%D0%B0%D0%BA%D1%86%D0%B8%D0%BE%D0%BD%D0%BD%D0%BE%D0%B5_%D0%BF%D1%80%D0%B5%D0%B4%D0%BF%D0%B8%D1%81%D0%B0%D0%BD%D0%B8%D0%B5
	 * @since 0.0.4
	 */
    public static Prescription calcLevenstain(final char[] str1, final char[] str2) throws NullPointerException {
    	if (str1 == null) {
    		throw new NullPointerException("Str1 array can't be null");
    	}
    	else  if (str2 == null) {
    		throw new NullPointerException("Str1 array can't be null");
    	}
    	else {
			final int 		m = str1.length, n = str2.length;
			final int[][] 	D = new int[m + 1][n + 1];
			final char[][] 	P = new char[m + 1][n + 1];
		
			for (int i = 0; i <= m; i++) {
				D[i][0] = i;
				P[i][0] = 'D';
			}
			for (int i = 0; i <= n; i++) {
				D[0][i] = i;
				P[0][i] = 'I';
			}
		
			for (int i = 1; i <= m; i++) {
		            for (int j = 1; j <= n; j++) {
		                final int cost = str1[i - 1] != str2[j - 1] ? 1 : 0;
		
		                if(D[i][j - 1] < D[i - 1][j] && D[i][j - 1] < D[i - 1][j - 1] + cost) {
		                    D[i][j] = D[i][j - 1] + 1;
		                    P[i][j] = 'I';
		                }
		                else if(D[i - 1][j] < D[i - 1][j - 1] + cost) {
		                    D[i][j] = D[i - 1][j] + 1;
		                    P[i][j] = 'D';
		                }
		                else {
		                    D[i][j] = D[i - 1][j - 1] + cost;
		                    P[i][j] = (cost == 1) ? 'R' : 'M';
		                }
		            }
		        }
		
			final List<int[]> opers = new ArrayList<>();
			int i = m, j = n;
		        
			do {char c = P[i][j];
		            if(c == 'R' || c == 'M') {
		                opers.add(0,new int[]{c == 'M' ? Prescription.LEV_NONE : Prescription.LEV_REPLACE,i,j});
		                i --;
		                j --;
		            }
		            else if(c == 'D') {
		                opers.add(0,new int[]{Prescription.LEV_DELETE,i,j});
		                i --;
		            }
		            else {
		                opers.add(0,new int[]{Prescription.LEV_INSERT,i,j});
		                j --;
		            }
			} while((i != 0) || (j != 0));
		        
			return new Prescription(D[m][n], opers.toArray(new int[opers.size()][]));
    	}
    }

    /**
     * <p>Calculate Levenstain distance and editor prescription for two object arrays</p>
     * @param <T> Object nature
     * @param obj1 array to calculate difference for. Can't be null
     * @param obj2 array to use as template. Can't be null
	 * @return prescription list and Levenstain distance. Can't be null
	 * @throws NullPointerException on any argument is null
	 * @see #calcLevenstain(char[], char[])
	 * @since 0.0.6
     */
    public static <T> Prescription calcLevenstain(final T[] obj1, final T[] obj2) throws NullPointerException {
    	return calcLevenstain(obj1, obj2, (Comparator<T>)(obj1 instanceof String[] ? EQUALS_STRING_COMPARATOR : EQUALS_OBJECT_COMPARATOR));
    }

    /**
     * <p>Calculate Levenstain distance and editor prescription for two object arrays</p>
     * @param <T> Object nature
     * @param obj1 array to calculate difference for. Can't be null
     * @param obj2 array to use as template. Can't be null
     * @param comp comparator to compare objects. Can't be null
	 * @return prescription list and Levenstain distance. Can't be null
     * @throws NullPointerException
	 * @see #calcLevenstain(char[], char[])
	 * @since 0.0.6
     */
    public static <T> Prescription calcLevenstain(final T[] obj1, final T[] obj2, final Comparator<T> comp) throws NullPointerException {
    	if (obj1 == null) {
    		throw new NullPointerException("Obj1 array can't be null");
    	}
    	else if (obj2 == null) {
    		throw new NullPointerException("Obj2 array can't be null");
    	}
    	else  if (comp == null) {
    		throw new NullPointerException("Comparator can't be null");
    	}
    	else {
			final int 		m = obj1.length, n = obj2.length;
			final int[][] 	D = new int[m + 1][n + 1];
			final char[][] 	P = new char[m + 1][n + 1];
		
			for (int i = 0; i <= m; i++) {
				D[i][0] = i;
				P[i][0] = 'D';
			}
			for (int i = 0; i <= n; i++) {
				D[0][i] = i;
				P[0][i] = 'I';
			}
		
			for (int i = 1; i <= m; i++) {
		            for (int j = 1; j <= n; j++) {
		                final int cost = comp.compare(obj1[i - 1], obj2[j - 1]) != 0 ? 1 : 0;
		
		                if(D[i][j - 1] < D[i - 1][j] && D[i][j - 1] < D[i - 1][j - 1] + cost) {
		                    D[i][j] = D[i][j - 1] + 1;
		                    P[i][j] = 'I';
		                }
		                else if(D[i - 1][j] < D[i - 1][j - 1] + cost) {
		                    D[i][j] = D[i - 1][j] + 1;
		                    P[i][j] = 'D';
		                }
		                else {
		                    D[i][j] = D[i - 1][j - 1] + cost;
		                    P[i][j] = (cost == 1) ? 'R' : 'M';
		                }
		            }
		        }
		
			final List<int[]> opers = new ArrayList<>();
			int i = m, j = n;
		        
			do {char c = P[i][j];
		            if(c == 'R' || c == 'M') {
		                opers.add(0,new int[]{c == 'M' ? Prescription.LEV_NONE : Prescription.LEV_REPLACE,i,j});
		                i --;
		                j --;
		            }
		            else if(c == 'D') {
		                opers.add(0,new int[]{Prescription.LEV_DELETE,i,j});
		                i --;
		            }
		            else {
		                opers.add(0,new int[]{Prescription.LEV_INSERT,i,j});
		                j --;
		            }
			} while((i != 0) || (j != 0));
		        
			return new Prescription(D[m][n], opers.toArray(new int[opers.size()][]));
    	}
    }
    
    /**
     * <p>Replace string with the same content to the same string</p>
     * @param source string to replace. Null value will return null  
     * @return string replaced or null
     * @see String#intern()
     * @since 0.0.6
     */
    public static String buildIdenticalString(final String source) {
    	if (source == null) {
    		return null; 
    	}
    	else {
        	return buildIdenticalString(source.toCharArray(), 0, source.length());
    	}
    }
    
    /**
     * <p>Replace piece of char with the same content to the same string.</p>
     * @param source char content. Can't be null
     * @param from from position on the char content
     * @param to to position in the char content
     * @return string built from char content. Can't be null
     * @throws NullPointerException when source content is null
     * @throws IllegalArgumentException when from and to arguments out of range
     * @see String#intern()
     * @since 0.0.6
     */
    public static String buildIdenticalString(final char[] source, final int from, final int to) throws NullPointerException, IllegalArgumentException {
    	if (source == null) {
    		throw new NullPointerException("Source chars buffer can't be null"); 
    	}
    	else if (from < 0 || from >= source.length) {
    		throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(source.length-1)); 
    	}
    	else if (to < 0 || to >= source.length) {
    		throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(source.length-1)); 
    	}
    	else if (to < from) {
    		throw new IllegalArgumentException("To position ["+to+"] can't be less than from position ["+from+"]"); 
    	}
    	else {
    		synchronized (VOCABULARY) {
        		final long	id = VOCABULARY.seekName(source, from, to);
        		
        		if (id >= 0) {
        			return VOCABULARY.getCargo(id);
        		}
        		else {
        			final String	content = new String(source, from, to-from);
        			
        			VOCABULARY.placeName(source, from, to, content);
        			return content;
        		}
			}
    	}
    }
    
    /**
     * <p>Reset content of identical strings vocabulary</p>
     * @since 0.0.6
     */
    public static void resetIdenticalStringVocabulary() {
    	VOCABULARY.clear();
    }
    
    private static class OrdinalCharSequence implements CharSequence {
    	private final char[]	content;
    	private final int		from;
    	private final int 		to;

		private OrdinalCharSequence(final char[] content, final int from, final int to) {
			this.content = content;
			this.from = from;
			this.to = to;
		}

		@Override
		public int length() {
			return to-from+1;
		}

		@Override
		public char charAt(final int index) throws StringIndexOutOfBoundsException {
			if (index < 0 || index >= length()) {
				throw new StringIndexOutOfBoundsException("Char index ["+index+"] out of range 0.."+(length()-1)); 
			}
			else {
				return content[index-from];
			}
		}

		@Override
		public CharSequence subSequence(final int start, final int end) {
			if (start < 0 || start >= length()) {
				throw new StringIndexOutOfBoundsException("Start index ["+start+"] out of range 0.."+(length()-1)); 
			}
			else if (end < 0 || end >= length()) {
				throw new StringIndexOutOfBoundsException("End index ["+end+"] out of range 0.."+(length()-1)); 
			}
			else if (end < start) {
				throw new StringIndexOutOfBoundsException("End index ["+end+"] less than start index ["+start+"]"); 
			}
			else {
				return new OrdinalCharSequence(content,from+start,from+end);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(content);
			result = prime * result + from;
			result = prime * result + to;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			OrdinalCharSequence other = (OrdinalCharSequence) obj;
			if (!Arrays.equals(content, other.content)) return false;
			if (from != other.from) return false;
			if (to != other.to) return false;
			return true;
		}

		@Override
		public String toString() {
			return "OrdinalCharSequence [content=" + Arrays.toString(content) + ", from=" + from + ", to=" + to + "]";
		}
    }

    private static class WeakCharSequence implements CharSequence {
    	private final WeakReference<char[]>	content;
    	private final int		from;
    	private final int 		to;

		private WeakCharSequence(final char[] content, final int from, final int to) {
			this.content = new WeakReference<char[]>(content);
			this.from = from;
			this.to = to;
		}

		@Override
		public int length() {
			return to-from+1;
		}

		@Override
		public char charAt(final int index) throws StringIndexOutOfBoundsException {
			if (index < 0 || index >= length()) {
				throw new StringIndexOutOfBoundsException("Char index ["+index+"] out of range 0.."+(length()-1)); 
			}
			else {
				final char[]	val = content.get();
				
				if (val != null) {
					return val[index-from];
				}
				else {
					throw new IllegalStateException("Reference for inner array was garbaged");
				}
			}
		}

		@Override
		public CharSequence subSequence(final int start, final int end) {
			if (start < 0 || start >= length()) {
				throw new StringIndexOutOfBoundsException("Start index ["+start+"] out of range 0.."+(length()-1)); 
			}
			else if (end < 0 || end >= length()) {
				throw new StringIndexOutOfBoundsException("End index ["+end+"] out of range 0.."+(length()-1)); 
			}
			else if (end < start) {
				throw new StringIndexOutOfBoundsException("End index ["+end+"] less than start index ["+start+"]"); 
			}
			else {
				final char[]	val = content.get();
				
				if (val != null) {
					return new WeakCharSequence(val,from+start,from+end);
				}
				else {
					throw new IllegalStateException("Reference for inner array was garbaged");
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + from;
			result = prime * result + to;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			WeakCharSequence other = (WeakCharSequence) obj;
			if (content == null) {
				if (other.content != null) return false;
			} else if (!equals(content,other.content)) return false;
			if (from != other.from) return false;
			if (to != other.to) return false;
			return true;
		}

		@Override
		public String toString() {
			return "WeakCharSequence [content=" + content + ", from=" + from + ", to=" + to + "]";
		}
		
		private boolean equals(final WeakReference<char[]> left, final WeakReference<char[]> right) {
			final char[]	leftVal = left.get(), rightVal = right.get();
			
			return leftVal != null && rightVal != null && Arrays.equals(leftVal,rightVal);
		}
    }
}
