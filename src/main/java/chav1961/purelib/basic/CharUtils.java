package chav1961.purelib.basic;

import java.util.Properties;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
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
 * <li><a href="#numerical">escaping and un-escaping</a> character array and string content (frequently used on XML, JSON, CSV format processing)</li>
 * <li>support <a href="#enums">Java enum and field</a> representation parsing</li>
 * <li>support <a href="#substitutions">substitutions</a> in the character and string content</li>
 * <li>a set of <a href="#useful">useful</a> methods for the char content processing</li>
 * </ul> 
 * 
 * <h2><a name="numerical">Parsing and printing numerical content</a></h2>
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
 * <h2><a name="escaping">Escaping and un-escaping content</a></h2>
 * 
 * <p>Methods to escaping and un-escaping are used to convert internal representation of chars, char arrays and strings to it's external representation form
 * (for example, to use inside JSON double-quoted values). Methods to escape are:</p>
 * <ul>
 * <li>{@linkplain CharUtils#symbolNeedsEscaping(char, boolean)}, {@linkplain #isSymbolPrintable(char)} and {@linkplain #howManyEscapedCharsOccupies(char)} - methods to check weather symbol
 * requires escaping for external representation and how many chars it's external representation occupies</li>
 * <li>{@linkplain #parseEscapedChar(char[], int, char[])}, {@linkplain #parseString(char[], int, char, StringBuilder)} and {@linkplain #parseStringExtended(char[], int, char, StringBuilder)} - 
 * methods to parse escaped representation of the char content to it's native form</li>
 * <li>{@linkplain #printEscapedChar(char[], int, char, boolean, boolean)}, {@linkplain #printEscapedCharArray(char[], int, char[], boolean, boolean)}, {@linkplain #printEscapedCharArray(char[], int, char[], int, int, boolean, boolean)} 
 * and {@linkplain #printEscapedString(char[], int, String, boolean, boolean)} - methods to convert internal representation of character or string content to it's external escaped form</li>
 * <li>{@linkplain #escapeStringContent(String)} and {@linkplain #unescapeStringContent(String)} - methods to convert string content to and from it's internal representation to escaped external one</li>
 * </ul>
 * <p>The class {@linkplain CharUtils} also contains {@linkplain #parseUnescapedString(char[], int, char, boolean, int[])} method to parse ordinal string content.</p>
 * <p>All the methods can throw {@linkplain SyntaxException} on parsing errors, and {@linkplain PrintingException} on printing.</p>
 *  
 * <h2><a name="escaping">Java enum and fields processing</a></h2>
 * 
 * <p>Enum and fields processing can be used to parse and print enumeration values and field names in the char content. Methods to parse and print enumerations and fields are:</p>
 * <ul>
 * <li>{@linkplain #parseEnum(char[], int, Class, Enum[])} - method to parse enumeration constants</li>
 * <li>{@linkplain #parseName(char[], int, int[])} and {@linkplain #parseNameExtended(char[], int, int[], char...)}- method to parse field names</li>
 * </ul>
 * <p>All the methods can throw {@linkplain SyntaxException} on parsing errors.</p>
 *  
 * <h2><a name="substitutions">Substitutions support</a></h2>
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
 * <h2><a name="useful">Useful methods</a></h2>
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
 * @lastUpdate 0.0.4
 */

public class CharUtils {
	public static final int			PREF_INT = 1;
	public static final int			PREF_LONG = 2;
	public static final int			PREF_FLOAT = 4;
	public static final int			PREF_DOUBLE = 8;
	public static final int			PREF_ANY = PREF_INT | PREF_LONG | PREF_FLOAT | PREF_DOUBLE;
	public static final int			MAX_SUBST_DEPTH = 16;

	private static final char[]		HYPHEN_NAME = "-".toCharArray();
	private static final char		WILDCARD_ANY_SEQ = '*';
	private static final char		WILDCARD_ANY_CHAR = '?';
	
	private static final char[]		EMPTY_CHAR_ARRAY = new char[0];
	
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
	
	public static int howManyEscapedCharsOccupies(final char symbol) {
		if (isSymbolPrintable(symbol)) {
			return 1;
		}
		else if (symbol < ' ') {
			switch (symbol) {
				case '\b' : case '\f' : case '\n' : case '\r' : case '\t' : case '\'' : case '\"' : case '\\' :
					return 2;
				default : 
					return 4;
			}
		}
		else {
			return 6;
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
	public static int parseString(final char[] source, final int from, final char terminal, final StringBuilder result) {
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
			return UnsafedCharUtils.uncheckedParseString(source, from, terminal, result);
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
	public static int parseStringExtended(final char[] source, final int from, final char terminal, final StringBuilder result) {
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
			return 	UnsafedCharUtils.uncheckedParseStringExtended(source, from, terminal, result);
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
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(source.length-1));
		}
		else {
			return UnsafedCharUtils.uncheckedSkipBlank(source,from,stopOnEOL);
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
	 * <p>This enumeration is used to describe template for extracting content from character array with lexemas.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 */
	public enum ArgumentType {
		ordinalInt, signedInt, hexInt, ordinalLong, signedLong, hexLong, ordinalFloat, signedFloat,
		name, hyphenedName, simpleTerminatedString, specialTerminatedString
	}

	/**
	 * <p>Try to extract content from input character array with lexemas. Returns non-negative number if extraction was successful. Lexemas can be:</p>
	 * <ul>
	 * <li>single character - marks character 'as-is' in the input array</li>
	 * <li>string - marks sequence of characters in the input array</li>
	 * <li>{@linkplain ArgumentType} - marks kind of predefined lexema in the input array</li>
	 * </ul>
	 * @param source source array to try extraction from
	 * @param from start position to extract content from
	 * @param lexemas lexemas list to try extract
	 * @return non-negative first position after parsed piece of characters in the input array or (negative position-1) in the input array where parsing failed 
	 * @throws IllegalArgumentException on any invalid parameters
	 * @throws SyntaxException on any syntax error in the content
	 * @since 0.0.3
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
						return -start; 
					}
				}
				else if (lexema instanceof Character) {
					if (source[start] == ((Character)lexema).charValue()) {
						start++;
					}
					else {
						return -start; 
					}
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
							case ordinalLong	:
								start = UnsafedCharUtils.uncheckedParseLong(source,start,longResult,true);
								break;
							case ordinalFloat	:
								start = UnsafedCharUtils.uncheckedParseFloat(source,start,floatResult,true);
								break;
							case name			:
								start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
								break;
							case hyphenedName	:
								start = UnsafedCharUtils.uncheckedParseNameExtended(source,start,intResult,HYPHEN_NAME);
								break;
							case simpleTerminatedString	:
							case specialTerminatedString	:
								break;
							default				:
								throw new UnsupportedOperationException("Argument type ["+lexema+"] is not supported yet"); 
						}
					} catch (IllegalArgumentException exc) {
						return -start; 
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
	 */
	public static int extract(final char[] source, final int from, final Object[] result, Object... lexemas) throws SyntaxException {
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
			int  resultCount = 0;
			
			for (Object item : lexemas) {
				if (item instanceof ArgumentType) {
					resultCount++;
				}
			}
			if (resultCount > result.length) {
				throw new IllegalArgumentException("Result array size ["+result.length+"] is less than number of ArgumetType lexemas in the list ["+resultCount+"]");
			}
			
			final int[]		intResult = new int[2];
			final long[]	longResult = new long[2];
			final float[]	floatResult = new float[2];
			int				resultIndex = 0;
			
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
				else if (lexema instanceof ArgumentType) {
					try {
						switch ((ArgumentType)lexema) {
							case hexInt			:
								start = UnsafedCharUtils.uncheckedParseHexInt(source,start,intResult,true);
								result[resultIndex++] = intResult[0];
								break;
							case hexLong		:
								start = UnsafedCharUtils.uncheckedParseHexLong(source,start,longResult,true);
								result[resultIndex++] = longResult[0];
								break;
							case ordinalInt		:
								start = UnsafedCharUtils.uncheckedParseInt(source,start,intResult,true);
								result[resultIndex++] = intResult[0];
								break;
							case ordinalLong	:
								start = UnsafedCharUtils.uncheckedParseLong(source,start,longResult,true);
								result[resultIndex++] = longResult[0];
								break;
							case ordinalFloat	:
								start = UnsafedCharUtils.uncheckedParseFloat(source,start,floatResult,true);
								result[resultIndex++] = floatResult[0];
								break;
							case name			:
								start = UnsafedCharUtils.uncheckedParseName(source,start,intResult);
								result[resultIndex++] = new String(source,intResult[0],intResult[1]-intResult[0]+1);
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
								result[resultIndex++] = new String(source,intResult[0],intResult[1]-intResult[0]+1);
								break;
							case hyphenedName	:
								final int	startName = start--;
								
								do {start = UnsafedCharUtils.uncheckedParseName(source,++start,intResult);
								} while (start < source.length && source[start] == '-');
								result[resultIndex++] = new String(source,startName,intResult[1]-startName+1);
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
	 */
	@FunctionalInterface
	public interface SubstitutionSource {
		/**
		 * <p>Get value for the given key</p>
		 * @param key key to get value for
		 * @return value got. Can be null
		 */
		String getValue(String key);
	}
	
	/**
	 * <p>This interface describes lambda-styled callback for {@linkplain CharUtils#substitute(String, String, SubstitutionSource)} call
	 * The only functionality it must support is get appropriative char array value for char array key requested.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
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
			final char[]	content = source.toCharArray();
			final int		amount = calculateSplitters(content,splitter);
			final String[]	result = new String[amount + 1];
			
			split(content,splitter,result);
			return result;
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
			final char[]	content = source.toCharArray();
			final int		amount = calculateSplitters(content,splitter);

			if (amount + 1 > target.length) {
				return -(amount + 1);
			}
			else {
				split(content,splitter,target);
				return amount + 1;
			}
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
			final char[]	content = source.toCharArray();
			final char[]	toSplit = splitter.toCharArray();
			final int		amount = calculateSplitters(content,toSplit);
			final String[]	result = new String[amount + 1];
			
			split(content,toSplit,result);
			return result;
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
			final char[]	content = source.toCharArray();
			final char[]	toSplit = splitter.toCharArray();
			final int		amount = calculateSplitters(content,toSplit);

			if (amount + 1 > target.length) {
				return -(amount + 1);
			}
			else {
				split(content,toSplit,target);
				return amount + 1;
			}
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
		else if (content.length == 0) {
			return EMPTY_CHAR_ARRAY;
		}
		else {
			int		dlen = delimiter.length, clen = content.length - 1, counter = dlen * clen;
			
			for (char[] item : content) {
				counter += item.length;
			}
			
			final char[]	result = new char[counter];
			int				to = 0, len;
			
			for (int index = 0; index < clen; index++) {
				System.arraycopy(content[index],0,result,to,len = content[index].length);
				to += len;
				System.arraycopy(delimiter,0,result,to,dlen);
				to += dlen;
			}
			System.arraycopy(content[clen],0,result,to,len = content[clen].length);
			
			return result;
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
	 */
	public static String join(final String delimiter, final String... content) throws IllegalArgumentException, NullPointerException {
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
			int	total = 0, count = 0;
			
			for (String item : content) {
				total += item.length();
				count++;
			}

			final char[]	result = new char[total + (count-1) * delimiter.length()], delim = delimiter.toCharArray();
			int				displ = 0, delimLength = delim.length;

			for (int index = 0, maxIndex = content.length - 1; index < maxIndex; index++) {
				final int	len = content[index].length();
				
				content[index].getChars(0,len,result,displ);
				displ += len;
				System.arraycopy(delim,0,result,displ,delimLength);
				displ += delimLength;
			}
			content[content.length - 1].getChars(0,content[content.length - 1].length(),result,displ);
			return new String(result);
		}
	}

	/**
	 * <p>Terminate string content with the givem terminal and convert result to char array</p>
	 * @param content content to terminate and convert
	 * @param terminal terminal to terminate content
	 * @return content converted
	 * @throws NullPointerException when content string is null
	 * @since 0.0.4
	 */
	public static char[] terminateAndConvert2CharArray(final String content, final char terminal) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("COntent string can't be null");
		}
		else {
			final char[]	result = new char[content.length()+1];
			
			content.getChars(0,result.length-1,result,0);
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
				additionalLength += howManyEscapedCharsOccupies(content.charAt(index))-1;
			}
			if (additionalLength > 0) {
				final char[]	temp = new char[length+additionalLength];
				char			currentChar;
				
				for (int index = 0, where = 0; index < length; index++) {
					currentChar = content.charAt(index);
					
					if (isSymbolPrintable(currentChar)) {
						temp[where++] = currentChar;
					}
					else {
						where = printEscapedChar(temp,where,currentChar,true,false);
					}
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
							break;
						case 'f' 	:
							temp[start++] = '\f';
							break;
						case 'n' 	:
							temp[start++] = '\n';
							break;
						case 'r' 	:
							temp[start++] = '\r';
							break;
						case 't' 	:
							temp[start++] = '\t';
							break;
						case '\'' 	:
							temp[start++] = '\'';
							break;
						case '\"' 	:
							temp[start++] = '\"';
							break;
						case '\\' 	:
							temp[start++] = '\\';
							break;
						case '0' 	:
							int	octal = 0;
							
							index++;
							while (index < maxIndex && (currentChar = content.charAt(index)) >= '0' && currentChar <= '7') {
								octal = 8 * octal + currentChar - '0';
								index++;
							}
							temp[start++] = (char)octal;
							break;
						case 'u' 	:
							int	hex = 0;
							
							index++;
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
							temp[start++] = (char)hex;
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
	
	private static int calculateSplitters(final char[] source, final char splitter) {
		int	amount = 0;
		
		for (char item : source) {
			if (item == splitter) {
				amount++;
			}
		}
		return amount;
	}

	private static int calculateSplitters(final char[] source, final char[] splitter) {
		final char	first = splitter[0];
		final int	splitterLen = splitter.length;
		int	amount = 0;
		
		for (int index = 0, maxIndex = source.length; index < maxIndex; index++) {
			if (source[index] == first && UnsafedCharUtils.uncheckedCompare(source,index,splitter,0,splitter.length)) {
				index += splitterLen - 1;
				amount++;
			}
		}
		return amount;
	}
	
	private static void split(final char[] source, final char splitter, final String[] target) {
		int	start = 0, maxIndex = source.length, targetIndex = 0;
		
		for (int index = 0; index < maxIndex; index++) {
			if (source[index] == splitter) {
				target[targetIndex++] = new String(source,start,index-start);
				start = index + 1;
			}
		}
		target[targetIndex++] = new String(source,start,maxIndex-start);
	}

	private static void split(final char[] source, final char[] splitter, final String[] target) {
		final char	first = splitter[0];
		int	start = 0, splitterLen = splitter.length, maxIndex = source.length, targetIndex = 0;
		
		for (int index = 0; index < maxIndex; index++) {
			if (source[index] == first && UnsafedCharUtils.uncheckedCompare(source,index,splitter,0,splitter.length)) {
				target[targetIndex++] = new String(source,start,index-start);
				index += splitterLen - 1;
				start = index + 1;
			}
		}
		target[targetIndex++] = new String(source,start,maxIndex-start);
	}
}
