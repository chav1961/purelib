package chav1961.purelib.basic;

import java.util.Arrays;

import chav1961.purelib.basic.growablearrays.GrowableCharArray;

/**
 * <p>This class contains implementation of the most-commonly-used char data parsing/printing functions in the system.</p> 
 * 
 * <p>All the parsing methods in the class are oriented to direct parsing of the char arrays. Every method accepts source data array
 * and starting position from it to parse, and returns the position in the source array after parsing. This returned value can then be used
 * in the subsequent calls as starting position. Any parsed object are returned from the methods thru the arrays of appropriative data. This 
 * technique emulates call-by-reference mode for the method parameters, for example:</p>
 * <code>
 * 		final char[] source = "1234 1234".toCharArray(); <br>
 * 		final int[]	value1 = new int[1], value2 = new int[1]; <br>
 *  	final int	endPos1 = CharsUtil.parseInt(source,0,value1,false);				// endPos1 = 4, value1[0] = 1234 <br>
 *  	final int	endPos2 = CharsUtil.parseInt(source,endPos1+1,value2,false);		// endPos2 = 9, value2[0] = 5678 <br>
 * </code> 
 * <p>All the printing methods in the class are oriented to direct filling of the char arrays. Every method accepts target data array and the 
 * free starting position to fill result, and returns the new free position in the array. Negative returned value means that the target array
 * is too short to keep result. It <i>absolute</i> value exactly reflect new free position in the target array and can be used to expand target
 * array to required size</p>
 * 
 * <p>All the methods in the class are thread-safe</p>
 *  
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */

public class CharUtils {
	public static final int			PREF_INT = 1;
	public static final int			PREF_LONG = 2;
	public static final int			PREF_FLOAT = 4;
	public static final int			PREF_DOUBLE = 8;
	public static final int			PREF_ANY = PREF_INT | PREF_LONG | PREF_FLOAT | PREF_DOUBLE;
	public static final int			MAX_SUBST_DEPTH = 16;
	
	private static final int		INTMAX_2 = Integer.MAX_VALUE / 2;
	private static final int		INTMAX_8 = Integer.MAX_VALUE / 8;
	private static final int		INTMAX_10 = Integer.MAX_VALUE / 10;
	private static final int		INTMAX_16 = Integer.MAX_VALUE / 16;
	private static final long		LONGMAX_2 = Long.MAX_VALUE / 2;
	private static final long		LONGMAX_8 = Long.MAX_VALUE / 8;
	private static final long		LONGMAX_10 = Long.MAX_VALUE / 10;
	private static final long		LONGMAX_16 = Long.MAX_VALUE / 16;
	private static final int		EXP_BOUND = Double.MAX_EXPONENT;
	private static final int		OCT_ESCAPE_SIZE = 3;
	private static final int		U_ESCAPE_SIZE = 4;
	private static final long[]		LONG_EXPS = new long[]{1000000000000000000L, 100000000000000000L, 10000000000000000L,
										1000000000000000L, 100000000000000L, 10000000000000L,
										1000000000000L, 100000000000L, 10000000000L,
										1000000000L, 100000000L, 10000000L,
										1000000L, 100000L, 10000L, 
										1000L, 	100L, 10L,
										1L
									};
	private static final double		DOUBLE_EXPS[];
	private static final char[]		DOUBLE_NAN = "NaN".toCharArray();
	private static final char[]		DOUBLE_NEGATIVE_INFINITY = "-Infinity".toCharArray();
	private static final char[]		DOUBLE_POSITIVE_INFINITY = "Infinity".toCharArray();
	private static final char[]		HEX_DIGITS = "0123456789ABCDEF".toCharArray();
	private static final char		WILDCARD_ANY_SEQ = '*';
	private static final char		WILDCARD_ANY_CHAR = '?';
	
	static {
		DOUBLE_EXPS = new double[EXP_BOUND * 2 + 1];
		for (int index = -EXP_BOUND; index <= EXP_BOUND; index++) {
			DOUBLE_EXPS[index+EXP_BOUND] = Double.parseDouble("1E"+index);
		}
	}
	
	/**
	 * <p>Extract unsigned integer value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (at least new int[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static int parseInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
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
			return uncheckedParseInt(source, from, result, checkOverflow);
		}
	}

	/**
	 * <p>Extract signed integer value from the current position of the source data</p>
	 * @param source source data contains character representation of the signed integer value
	 * @param from starting position in the source data
	 * @param result array (at least new int[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 * @since 0.0.2
	 */
	public static int parseSignedInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
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
			if (source[from] == '-') {
				final int	returned = uncheckedParseInt(source, from+1, result, checkOverflow);
				
				result[0] = -result[0];
				return returned;
			}
			else if (source[from] == '+') {
				return uncheckedParseInt(source, from+1, result, checkOverflow);
			}
			else {
				return uncheckedParseInt(source, from, result, checkOverflow);
			}
		}
	}
	
	private static int uncheckedParseInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (checkOverflow && temp > INTMAX_10) {
				throw new NumberFormatException("Number is greater then maximal available integer");
			}
			temp = temp * 10 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}
	
	
	/**
	 * <p>Extract unsigned integer value from the current position of the source data. Differ to {@link #parseInt(char[], int, int[], boolean)} supports binary, octal and hexadecimal representation of the integer constants.</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new int[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static int parseIntExtended(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
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
			int		temp = 0, index = from;
			char	symbol;
			
			if ((symbol = source[index]) == '0') {
				if (index < len-1) {
					switch (source[index+1]) {
						case 'b' : case 'B' :
							index += 2;
							while (index < len && (symbol = source[index]) >= '0' && symbol <= '1') {
								if (checkOverflow && temp > INTMAX_2) {
									throw new NumberFormatException("Number is greater then maximal available integer");
								}
								temp = (temp << 1) + (symbol == '0' ? 0 : 1);
								index++;
							}
							break;
						case 'x' : case 'X' :
							index += 2;
							while (index < len && ((symbol = source[index]) >= '0' && symbol <= '9' || symbol >= 'a' && symbol <= 'f' || symbol >= 'A' && symbol <= 'F')) {
								if (checkOverflow && temp > INTMAX_16) {
									throw new NumberFormatException("Number is greater then maximal available integer");
								}
								if (symbol >= '0' && symbol <= '9') {
									temp = (temp << 4) + symbol - '0';
								}
								else if (symbol >= 'a' && symbol <= 'f') {
									temp = (temp << 4) + symbol - 'a' + 10;
								}
								else {
									temp = (temp << 4) + symbol - 'A' + 10;
								}
								index++;
							}
							break;
						default :
							while (index < len && ((symbol = source[index]) >= '0' && symbol <= '7')) {
								if (checkOverflow && temp > INTMAX_8) {
									throw new NumberFormatException("Number is greater then maximal available integer");
								}
								temp = (temp << 3) + symbol - '0';
								index++;
							}
							break;
					}
				}
				else {
					while (index < len && ((symbol = source[index]) >= '0' && symbol <= '7')) {
						if (checkOverflow && temp > INTMAX_8) {
							throw new NumberFormatException("Number is greater then maximal available integer");
						}
						temp = (temp << 3) + symbol - '0';
						index++;
					}
				}				
			}
			else {
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					if (checkOverflow && temp > INTMAX_10) {
						throw new NumberFormatException("Number is greater then maximal available integer");
					}
					temp = temp * 10 + symbol - '0';
					index++;
				}
			}
			
			if (index == from) {
				throw new NumberFormatException("No one digits in the number was detected!");
			}
			else {
				result[0] = temp;
				return index;
			}
		}
	}
	
	/**
	 * <p>Extract unsigned long value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new long[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static int parseLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
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
			return uncheckedParseLong(source, from, result, checkOverflow);
		}
	}

	/**
	 * <p>Extract signed long value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new long[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 * @since 0.0.2
	 */
	public static int parseSignedLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
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
			if (source[from] == '-') {
				final int	returned = uncheckedParseLong(source, from+1, result, checkOverflow);
				
				result[0] = -result[0];
				return returned;
			}
			else if (source[from] == '+') {
				return uncheckedParseLong(source, from+1, result, checkOverflow);
			}
			else {
				return uncheckedParseLong(source, from, result, checkOverflow);
			}
		}
	}
	
	private static int uncheckedParseLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (checkOverflow && temp > LONGMAX_10) {
				throw new NumberFormatException("Number is greater then maximal available long");
			}
			temp = temp * 10 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}
	
	
	/**
	 * <p>Extract unsigned long value from the current position of the source data. Differ to {@link #parseLong(char[], int, long[], boolean)} supports binary, octal and hexadecimal representation of the integer constants.</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new long[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static int parseLongExtended(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
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
			long	temp = 0;
			int		index = from;
			char	symbol;
			
			if ((symbol = source[index]) == '0') {
				if (index < len-1) {
					switch (source[index+1]) {
						case 'b' : case 'B' :
							index += 2;
							while (index < len && (symbol = source[index]) >= '0' && symbol <= '1') {
								if (checkOverflow && temp > LONGMAX_2) {
									throw new NumberFormatException("Number is greater then maximal available long");
								}
								temp = (temp << 1) + (symbol == '0' ? 0 : 1);
								index++;
							}
							break;
						case 'x' : case 'X' :
							index += 2;
							while (index < len && ((symbol = source[index]) >= '0' && symbol <= '9' || symbol >= 'a' && symbol <= 'f' || symbol >= 'A' && symbol <= 'F')) {
								if (checkOverflow && temp > LONGMAX_16) {
									throw new NumberFormatException("Number is greater then maximal available long");
								}
								if (symbol >= '0' && symbol <= '9') {
									temp = (temp << 4) + symbol - '0';
								}
								else if (symbol >= 'a' && symbol <= 'f') {
									temp = (temp << 4) + symbol - 'a' + 10;
								}
								else {
									temp = (temp << 4) + symbol - 'A' + 10;
								}
								index++;
							}
							break;
						default :
							while (index < len && ((symbol = source[index]) >= '0' && symbol <= '7')) {
								if (checkOverflow && temp > LONGMAX_8) {
									throw new NumberFormatException("Number is greater then maximal available long");
								}
								temp = (temp << 3) + symbol - '0';
								index++;
							}
							break;
					}
				}
				else {
					while (index < len && ((symbol = source[index]) >= '0' && symbol <= '7')) {
						if (checkOverflow && temp > INTMAX_8) {
							throw new NumberFormatException("Number is greater then maximal available integer");
						}
						temp = (temp << 3) + symbol - '0';
						index++;
					}
				}				
			}
			else {
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					if (checkOverflow && temp > LONGMAX_10) {
						throw new NumberFormatException("Number is greater then maximal available long");
					}
					temp = temp * 10 + symbol - '0';
					index++;
				}
			}
			
			if (index == from) {
				throw new NumberFormatException("No one digits in the number was detected!");
			}
			else {
				result[0] = temp;
				return index;
			}
		}
	}

	/**
	 * <p>Extract unsigned float value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static int parseFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) {
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
			return unckeckedParseFloat(source,from,result,checkOverflow);
		}
	}

	/**
	 * <p>Extract signed float value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 * @since 0.0.2
	 */
	public static int parseSignedFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) {
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
			if (source[from] == '-') {
				final int	returned = unckeckedParseFloat(source, from+1, result, checkOverflow);
				
				result[0] = -result[0];
				return returned;
			}
			else if (source[from] == '+') {
				return unckeckedParseFloat(source, from+1, result, checkOverflow);
			}
			else {
				return unckeckedParseFloat(source, from, result, checkOverflow);
			}
		}
	}
	
	private static int unckeckedParseFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol = ' ';
		boolean		continueParsing = false;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (temp > LONGMAX_10) {
				continueParsing = true;
				break;
			}
			temp = temp * 10 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else if (index < len && (symbol == '.' || symbol == 'e' || symbol == 'E') || continueParsing) {
			double	tempInt = temp;
			
			if (continueParsing) {
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					tempInt = tempInt * 10 + symbol - '0';
					index++;
				}
			}
			
			if (symbol == '.') {
				double	frac = 0, scale = 1;
				int		fracFrom = ++index;
				
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					frac = frac * 10 + symbol - '0';
					scale *= 0.1;
					index++;
				}
				
				if (index == fracFrom) {
					throw new NumberFormatException("No one digits in the fractional part of float was detected!");
				}
				else {
					tempInt += frac * scale;
				}
			}
			if (symbol == 'e' || symbol == 'E') {
				int		multiplier = 1;
				
				index++;
				if (index < len && (symbol = source[index]) == '-') {
					multiplier = -1;
					index++;
				}
				else if (symbol == '+') {
					index++;
				}
				int		exp = 0, expFrom = index;
				
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					exp = exp * 10 + symbol - '0';
					index++;
				}
				exp *= multiplier;
				
				
				if (checkOverflow && (exp > Float.MAX_EXPONENT || exp < Float.MIN_EXPONENT)) {
					throw new NumberFormatException("Number is greater then maximal available float");
				}
				
				if (index == expFrom) {
					throw new NumberFormatException("No one digits in the exponent part of float was detected!");
				}
				else {
					tempInt *= DOUBLE_EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
				}
			}
			result[0] = (float)tempInt;
		}
		else {
			result[0] = temp;
		}
		return index;
	}
	
	/**
	 * <p>Extract unsigned double value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 */
	public static int parseDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) {
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
			return uncheckedParseDouble(source, from, result, checkOverflow);
		}
	}

	/**
	 * <p>Extract signed double value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new double[1]) to store parsed value
	 * @param checkOverflow need check overflow when parsing data
	 * @return position of the first char in the source after successful parsing of the current integer 
	 * @throws IllegalArgumentException if any parsing errors ware detected 
	 * @since 0.0.2
	 */
	public static int parseSignedDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) {
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
			if (source[from] == '-') {
				final int	returned = uncheckedParseDouble(source, from+1, result, checkOverflow);
				
				result[0] = -result[0];
				return returned;
			}
			else if (source[from] == '+') {
				return uncheckedParseDouble(source, from+1, result, checkOverflow);
			}
			else {
				return uncheckedParseDouble(source, from, result, checkOverflow);
			}
		}
	}
	
	private static int uncheckedParseDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol = ' ';
		boolean		continueParsing = false;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (temp > LONGMAX_10) {
				continueParsing = true;
				break;
			}
			temp = temp * 10 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else if (index < len && (symbol == '.' || symbol == 'e' || symbol == 'E') || continueParsing) {
			double	tempInt = temp;
			
			if (continueParsing) {
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					tempInt = tempInt * 10 + symbol - '0';
					index++;
				}
			}
			
			if (symbol == '.') {
				double	frac = 0, scale = 1;
				int		fracFrom = ++index;
				
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					frac = frac * 10 + symbol - '0';
					scale *= 0.1;
					index++;
				}
				
				if (index == fracFrom) {
					throw new NumberFormatException("No one digits in the fractional part of double was detected!");
				}
				else {
					tempInt += frac * scale;
				}
			}
			if (symbol == 'e' || symbol == 'E') {
				int		multiplier = 1;
				
				index++;
				if (index < len && (symbol = source[index]) == '-') {
					multiplier = -1;
					index++;
				}
				else if (symbol == '+') {
					index++;
				}
				int		exp = 0, expFrom = index;
				
				while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
					exp = exp * 10 + symbol - '0';
					index++;
				}
				exp *= multiplier;					
				
				if (checkOverflow && (exp > Double.MAX_EXPONENT || exp < Double.MIN_EXPONENT)) {
					throw new NumberFormatException("Number is greater then maximal available double");
				}
				
				if (index == expFrom) {
					throw new NumberFormatException("No one digits in the exponent part of double was detected!");
				}
				else {
					tempInt *= DOUBLE_EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
				}
			}
			result[0] = tempInt;
		}
		else {
			result[0] = temp;
		}
		return index;
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
	 * @throws IllegalArgumentException if any parsing errors ware detected
	 * @since 0.0.1 last update 0.0.3 
	 */
	public static int parseNumber(final char[] source, final int from, final long[] result, final int preferences, final boolean checkOverflow) {
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
			long	temp = 0;
			int		index = from;
			char	symbol = ' ';
			boolean	continueParsing = false;
			
			while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
				if (checkOverflow) {
					if (temp > INTMAX_10 && (preferences & (PREF_LONG | PREF_FLOAT)) == 0) {
						throw new NumberFormatException("Number is greater then maximal available integer");
					}
					if (temp > LONGMAX_10 && (preferences & PREF_DOUBLE) == 0) {
						throw new NumberFormatException("Number is greater then maximal available long");
					}
				}
				if (temp > LONGMAX_10) {
					continueParsing = true;
					break;
				}
				temp = temp * 10 + symbol - '0';
				index++;
			}
			if (index < len && (source[index] == 'l' || source[index] == 'L')) {
				if ((preferences & PREF_LONG) == 0) {
					throw new NumberFormatException("Long constant is not waited here");
				}
				else {
					result[0] = temp; 
					result[1] = PREF_LONG;
					return index+1;
				}
			}
			
			if (index == from) {
				throw new NumberFormatException("No one digits in the number was detected!");
			}
			else if (index < len && (symbol == '.' || symbol == 'e' || symbol == 'E' || symbol == 'f' || symbol == 'F') || continueParsing) {
				if ((preferences & (PREF_FLOAT | PREF_DOUBLE)) != 0) {
					double	tempInt = temp;
					
					if (continueParsing) {
						while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
							tempInt = tempInt * 10 + symbol - '0';
							index++;
						}
					}
					
					if (symbol == '.') {
						double	frac = 0, scale = 1;
						int		fracFrom = ++index;
						
						while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
							frac = frac * 10 + symbol - '0';
							scale *= 0.1;
							index++;
						}
						
						if (index == fracFrom) {
							throw new NumberFormatException("No one digits in the fractional part of double was detected!");
						}
						else {
							tempInt += frac * scale;
						}
					}
					if (symbol == 'e' || symbol == 'E') {
						int		multiplier = 1;
						
						index++;
						if (index < len && (symbol = source[index]) == '-') {
							multiplier = -1;
							index++;
						}
						else if (symbol == '+') {
							index++;
						}
						int		exp = 0, expFrom = index;
						
						while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
							exp = exp * 10 + symbol - '0';
							index++;
						}
						exp *= multiplier;
						
						if (checkOverflow && (exp > EXP_BOUND || exp < -EXP_BOUND)) {
							throw new NumberFormatException("Number is greater then maximal available double");
						}
						
						if (index == expFrom) {
							throw new NumberFormatException("No one digits in the exponent part of double was detected!");
						}
						else {
							tempInt *= DOUBLE_EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
						}
					}
	
					if (symbol == 'f' || symbol == 'F') {
						index++;
						if ((preferences & PREF_FLOAT) == 0) {
							throw new NumberFormatException("Float number is not waited here");
						}
						else if (Math.abs(tempInt) < Float.MAX_VALUE) {
							result[0] = Float.floatToIntBits((float)tempInt);
							result[1] = PREF_FLOAT;
						}
						else {
							throw new NumberFormatException("Float number is greater then maximal available float");
						}
					}
					else {
						if ((preferences & PREF_DOUBLE) != 0) {
							result[0] = Double.doubleToLongBits(tempInt);
							result[1] = PREF_DOUBLE;
						}
						else {
							throw new NumberFormatException("Double number is not waited here");
						}
					}
				}
				else {
					result[0] = temp; 
					result[1] = temp > Integer.MAX_VALUE ? PREF_LONG : PREF_INT;
				}
			}
			else {
				result[0] = temp; 
				result[1] = temp > Integer.MAX_VALUE ? PREF_LONG : PREF_INT;
			}
			return index;
		}
	}

	/**
	 * <p>Test weather the given char need escaping in the external representation</p>
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
	 * <p>Parse possibly escaped char.</p>
	 * @param source source data contains character representation of the char
	 * @param from starting position in the source data. 
	 * @param result array (new char[1]) to store parsed char
	 * @return position of the first char in the source after successful parsing of the current char.
	 * @since 0.0.2 
	 */
	public static int parseEscapedChar(final char[] source, int from, final char[] result) {
		final int 	len;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source data can't be null or empty array"); 
		}
		else if (from < 0 || from >= len) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+len); 
		}
		else if (result == null || result.length == 1) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
		}
		else if (source[from] == '\\') {
			if (from < len - 1) {
				switch (source[from+1]) {
					case '\"' 	: result[0] = '\"'; from += 2; break;
					case '\\' 	: result[0] = '\\'; from += 2; break;
					case '/' 	: result[0] = '/';  from += 2; break;
					case 'b' 	: result[0] = '\b'; from += 2; break;
					case 'f' 	: result[0] = '\f'; from += 2; break;
					case 'n' 	: result[0] = '\n'; from += 2; break;
					case 'r' 	: result[0] = '\r'; from += 2; break;
					case 't' 	: result[0] = '\t'; from += 2; break;
					case '0' 	:
						if (from + 1 >= len - OCT_ESCAPE_SIZE) {
							throw new IllegalArgumentException("Escape \\0nn sequence at the "+from+"-th char is too short");
						}
						else {
							int		octVal = 0;
							char	symbol;
							
							for (int chars = from + 1; chars < from + 1 + OCT_ESCAPE_SIZE; chars++) {
								if ((symbol = source[chars]) >= '0' && symbol <= '7') {
									octVal = (octVal << 3) + symbol - '0';
								}
								else {
									throw new IllegalArgumentException("Escape \\0nn sequence at the "+from+"-th char has illegal octal value ("+source[chars]+")");
								}
							}
							result[0] = (char) octVal;
							from += OCT_ESCAPE_SIZE+1;
						}
						break;
					case 'u' 	:
						if (from + 2 >= len - U_ESCAPE_SIZE) {
							throw new IllegalArgumentException("Escape \\uXXXX sequence at the "+from+"-th char is too short");
						}
						else {
							int		hexVal = 0;
							char	symbol;
							
							for (int chars = from + 2; chars < from + 2 + U_ESCAPE_SIZE; chars++) {
								if ((symbol = source[chars]) >= '0' && symbol <= '9') {
									hexVal = (hexVal << 4) + symbol - '0';
								}
								else if (symbol >= 'a' && symbol <= 'f') {
									hexVal = (hexVal << 4) + symbol - 'a' + 10;
								}
								else if (symbol >= 'A' && symbol <= 'F') {
									hexVal = (hexVal << 4) + symbol - 'A' + 10;
								}
								else {
									throw new IllegalArgumentException("Escape \\uXXXX sequence at the "+from+"-th char has illegal hex value ("+source[chars]+")");
								}
							}
							result[0] = (char) hexVal;
							from += U_ESCAPE_SIZE+2;
						}
						break;
					default : throw new IllegalArgumentException("Illegal escape sequence at the "+from+"-th char of the string ("+source[from]+")");
				}				
				return from;
			}
			else {
				throw new IllegalArgumentException("Truncated char escape sequence"); 
			}
		}
		else {
			result[0] = source[from];
			
			return from + 1;
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
			int		index;
			
			for (index = from; index < len; index++) {
				if (source[index] == terminal) {
					result[0] = from;
					result[1] = index - 1;
					return index + 1;
				}
				else if ((source[index] == '\\' || source[index] == '\n')&& checkEscaping) {
					result[0] = from;
					result[1] = index - 1;
					return -index;
				}
			}
			throw new IllegalArgumentException("Unterminated string in the "+(index-from+1)+"-th char of the string");
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
			int		index;
			
			for (index = from; index < len; index++) {
				if (source[index] == terminal) {
					result.append(source,from,index-from);
					return index + 1;
				}
				else if (source[index] == '\\') {
					result.append(source,from,index-from);
					break;
				}
			}
			for (; index < len; index++) {
				if (source[index] == terminal) {
					return index + 1;
				}
				else if (source[index] == '\\') {
					if (index >= len) {
						throw new IllegalArgumentException("Illegal escape sequence at the "+(index-from+1)+"-th char of the string");
					}
					else {
						switch (source[index+1]) {
							case '\"' 	: result.append("\""); break;
							case '\\' 	: result.append("\\"); break;
							case '/' 	: result.append("/"); break;
							case 'b' 	: result.append("\b"); break;
							case 'f' 	: result.append("\f"); break;
							case 'n' 	: result.append("\n"); break;
							case 'r' 	: result.append("\r"); break;
							case 't' 	: result.append("\t"); break;
							case 'u' 	:
								if (index + 2 >= len - U_ESCAPE_SIZE) {
									throw new IllegalArgumentException("Escape \\uXXXX sequence at the "+(index-from+1)+"-th char is too short");
								}
								else {
									int		hexVal = 0;
									char	symbol;
									
									for (int chars = index + 2; chars < index + 2 + U_ESCAPE_SIZE; chars++) {
										if ((symbol = source[chars]) >= '0' && symbol <= '9') {
											hexVal = (hexVal << 4) + symbol - '0';
										}
										else if (symbol >= 'a' && symbol <= 'f') {
											hexVal = (hexVal << 4) + symbol - 'a' + 10;
										}
										else if (symbol >= 'A' && symbol <= 'F') {
											hexVal = (hexVal << 4) + symbol - 'A' + 10;
										}
										else {
											throw new IllegalArgumentException("Escape \\uXXXX sequence at the "+(index-from+1)+"-th char has illegal hex value ("+source[index]+")");
										}
									}
									result.append((char)hexVal);
									index += U_ESCAPE_SIZE;
								}
								break;
							default : throw new IllegalArgumentException("Illegal escape sequence at the "+(index-from+1)+"-th char of the string ("+source[index]+")");
						}
						index++;
					}
				}
				else {
					result.append(source[index]);
				}
			}
			throw new IllegalArgumentException("Unterminated string in the "+(index-from+1)+"-th char of the string");
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
			int		index;
			
			for (index = from; index < len; index++) {
				if (source[index] == terminal) {
					result.append(source,from,index-from);
					return index + 1;
				}
				else if (source[index] == '\\') {
					result.append(source,from,index-from);
					break;
				}
			}
			for (; index < len; index++) {
				if (source[index] == terminal) {
					return index + 1;
				}
				else if (source[index] == '\\') {
					if (index >= len) {
						throw new IllegalArgumentException("Illegal escape sequence at the "+(index-from+1)+"-th char of the string");
					}
					else {
						switch (source[index+1]) {
							case '\"' 	: result.append("\""); break;
							case '\'' 	: result.append("\'"); break;
							case '\\' 	: result.append("\\"); break;
							case '/' 	: result.append("/"); break;
							case 'b' 	: result.append("\b"); break;
							case 'f' 	: result.append("\f"); break;
							case 'n' 	: result.append("\n"); break;
							case 'r' 	: result.append("\r"); break;
							case 't' 	: result.append("\t"); break;
							case 'u' 	:
								if (index + 2 >= len - U_ESCAPE_SIZE) {
									throw new IllegalArgumentException("Escape \\uXXXX sequence at the "+(index-from+1)+"-th char is too short");
								}
								else {
									int		hexVal = 0;
									char	symbol;
									
									for (int chars = index + 2; chars < index + 2 + U_ESCAPE_SIZE; chars++) {
										if ((symbol = source[chars]) >= '0' && symbol <= '9') {
											hexVal = (hexVal << 4) + symbol - '0';
										}
										else if (symbol >= 'a' && symbol <= 'f') {
											hexVal = (hexVal << 4) + symbol - 'a' + 10;
										}
										else if (symbol >= 'A' && symbol <= 'F') {
											hexVal = (hexVal << 4) + symbol - 'A' + 10;
										}
										else {
											throw new IllegalArgumentException("Escape \\uXXXX sequence at the "+(index-from+1)+"-th char has illegal hex value ("+source[index]+")");
										}
									}
									result.append((char)hexVal);
									index += U_ESCAPE_SIZE;
								}
								break;
							case '0' 	:
								if (index >= len - 2) {
									throw new IllegalArgumentException("Illegal escape sequence at the "+(index-from+1)+"-th char of the string");
								}
								else if (source[index+2] == 'x' || source[index+2] == 'X') {
									if (index >= len - 3) {
										throw new IllegalArgumentException("Illegal escape sequence at the "+(index-from+1)+"-th char of the string");
									}
									else {
										int		hexVal = 0;
										char	symbol = source[index+=3];
										
										while (index < len && "0123456789abcdefABCDEF".indexOf(symbol) >= 0) {
											if (symbol >= '0' && symbol <= '9') {
												hexVal = (hexVal << 4) + symbol - '0';
											}
											else if (symbol >= 'a' && symbol <= 'f') {
												hexVal = (hexVal << 4) + symbol - 'a' + 10;
											}
											else {
												hexVal = (hexVal << 4) + symbol - 'A' + 10;
											}
											symbol = source[++index];
										}
										result.append((char)hexVal);
										if (index < len) {
											index -= 2;
										}
									}
								}
								else {
									int		octVal = 0;
									char	symbol = source[index+=2];
									
									while (index < len && symbol >= '0' && symbol <= '7') {
										octVal = (octVal << 3) + symbol - '0';
										symbol = source[++index];
									}
									result.append((char)octVal);
									if (index < len) {
										index -= 2;
									}
								}
								break;
							default : throw new IllegalArgumentException("Illegal escape sequence at the "+(index-from+1)+"-th char of the string");
						}
						index++;
					}
				}
				else {
					result.append(source[index]);
				}
			}
			throw new IllegalArgumentException("Unterminated string in the "+(index-from+1)+"-th char of the string");
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
			if (!Character.isJavaIdentifierStart(source[from])) {
				throw new IllegalArgumentException("No valid beginning of the name"); 
			}
			else {
				int		index;
				
				result[0] = from;
				for (index = from; index < len; index++) {
					if (!Character.isJavaIdentifierPart(source[index])) {
						break;
					}
				}
				result[1] = index-1;
				
				return index;
			}
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
			if (!Character.isJavaIdentifierStart(source[from])) {
				throw new IllegalArgumentException("No valid beginning of the name"); 
			}
			else {
				int		index;
				
				result[0] = from;
loop:			for (index = from; index < len; index++) {
					for (char item : availableChars) {
						if (item == source[index]) {
							continue loop;
						}
					}
					if (!Character.isJavaIdentifierPart(source[index])) {
						break;
					}
				}
				result[1] = index-1;
				
				return index;
			}
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
		else if (stopOnEOL) {
			for (int index = from, maxIndex = source.length; index < maxIndex; index++) {
				if (source[index] > ' ' || source[index] == '\n') {
					return index;
				}
			}
			return source.length;
		}
		else {
			for (int index = from, maxIndex = source.length; index < maxIndex; index++) {
				if (source[index] > ' ') {
					return index;
				}
			}
			return source.length;
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
			return uncheckedCompare(source,from,template,0,template.length);
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
			return uncheckedCompare(source,from,template,templateFrom,templateLen);
		}
	}

	private static boolean uncheckedCompare(final char[] source, final int from, final char[] template, final int templateFrom, final int templateLen) {
		if (source.length - from < templateLen) {
			return false;
		}
		else {
			for (int index = 0; index < templateLen; index++) {
				if (source[from+index] != template[templateFrom+index]) {
					return false;
				}
			}
			return true;
		}
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
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
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
			return uncheckedPrintLong(content,from,value,reallyFill);
		}
	}

	private static int uncheckedPrintLong(final char[] content, final int from, final long value, boolean reallyFill) throws IllegalArgumentException {
		final int	to = content.length;
		long		currentVal = value;
		int			newFrom = from,  delta = 0;
		
		if (currentVal == 0) {
			if (newFrom < to) {
				if (reallyFill) {
					content[newFrom] = '0';
				}
			}
			else {
				reallyFill = false;
			}
			newFrom++;
		}
		else {
			if (currentVal < 0) {
				if (currentVal == Long.MIN_VALUE) {
					currentVal = Long.MAX_VALUE;
					delta = 1;	// Problem with the Long.MIN_VALUE
				}
				else {
					currentVal = - currentVal;
				}
				if (newFrom < to) {
					if (reallyFill) {
						content[newFrom] = '-';
					}
				}
				else {
					reallyFill = false;
				}
				newFrom++;
			}
			final long[]	expsArray = LONG_EXPS;
			int				index, maxIndex;
			
			for (index = expsArray.length - 1; index > 0; index--) {
				if (expsArray[index] > currentVal) {
					index++;
					break;
				}
			}
			
			for (maxIndex = expsArray.length; index < maxIndex; index++) {
				final long	currentDelta = expsArray[index];
				
				if (currentVal >= currentDelta) {
					for (int digit = 1; digit <= 10; digit++) {
						if ((currentVal -= currentDelta) < 0) {
							currentVal += currentDelta;
							if (newFrom < to) {
								if (reallyFill) {
									content[newFrom] = (char) ('0'+digit-1);
								}
							}
							else {
								reallyFill = false;
							}
							newFrom++;
							break;
						}
						else if (currentVal == 0) {
							if (newFrom < to) {
								if (reallyFill) {
									content[newFrom] = (char) ('0'+digit);
								}
							}
							else {
								reallyFill = false;
							}
							newFrom++;
							break;
						}
					}
					currentVal += delta;	// Problem with the Long.MIN_VALUE 
					delta = 0;
				}
				else {
					if (newFrom < to) {
						if (reallyFill) {
							content[newFrom] = '0';
						}
					}
					else {
						reallyFill = false;
					}
					newFrom++;
				}
			}
		}
		return reallyFill ? newFrom : -newFrom;
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
			final int	to = content.length;
			int			newFrom = from;
			
			if (Double.isNaN(value)) {
				if (newFrom < to - DOUBLE_NAN.length) {
					if (reallyFill) {
						System.arraycopy(DOUBLE_NAN,0,content,newFrom,DOUBLE_NAN.length);
					}
				}
				else {
					reallyFill = false;
				}
				newFrom += DOUBLE_NAN.length;
			}
			else if (value == Double.NEGATIVE_INFINITY) {
				if (newFrom < to - DOUBLE_NEGATIVE_INFINITY.length) {
					if (reallyFill) {
						System.arraycopy(DOUBLE_NEGATIVE_INFINITY,0,content,newFrom,DOUBLE_NEGATIVE_INFINITY.length);
					}
				}
				else {
					reallyFill = false;
				}
				newFrom += DOUBLE_NEGATIVE_INFINITY.length;
			}
			else if (value == Double.POSITIVE_INFINITY) {
				if (newFrom < to - DOUBLE_POSITIVE_INFINITY.length) {
					if (reallyFill) {
						System.arraycopy(DOUBLE_POSITIVE_INFINITY,0,content,newFrom,DOUBLE_POSITIVE_INFINITY.length);
					}
				}
				else {
					reallyFill = false;
				}
				newFrom += DOUBLE_POSITIVE_INFINITY.length;
			}
			else {
				if (value == 0) {
					if (newFrom < to) {
						if (reallyFill) {
							content[newFrom] = '0';
						}
					}
					else {
						reallyFill = false;
					}
					newFrom++;
				}
				else {
					if (value < 0) {
						value = -value;
						if (newFrom < to) {
							if (reallyFill) {
								content[newFrom] = '-';
							}
						}
						else {
							reallyFill = false;
						}
						newFrom++;
					}
					int		exponent;
					
					if (value > 1e19 || value < 1e-19) {	// Minimize output for too long or too small values
						exponent = ((int)Math.log10(value));
						value *= DOUBLE_EXPS[Double.MAX_EXPONENT - exponent]; 
					}
					else {
						exponent = 0;
					}
					double	intPart = Math.floor(value), fracPart = (value - intPart) * 1E18;
					
					if ((newFrom = uncheckedPrintLong(content,newFrom,(long)intPart,reallyFill)) < 0) {
						newFrom = -newFrom;
						reallyFill = false;
					}

					if (fracPart != 0) {	// Print fraction of non zero
						final int	dot = newFrom;	// Store dot location
						
						fracPart += 1E18; // +1E18 to strongly type left unsignificant zeroes
						if ((newFrom = uncheckedPrintLong(content,newFrom,(long)fracPart,reallyFill)) < 0) {
							newFrom = -newFrom;
							reallyFill = false;
						}
						else {	// Replace the same first '1' of 1E18 to '.'
							if (dot < to) {
								if (reallyFill) {
									content[dot] = '.';
								}
							}
							while (content[newFrom-1] == '0') {	// Trunk right unsignificant zeroes in the tail of fractional part 
								newFrom--;
							}
							if (content[newFrom-1] == '.') {
								newFrom--;
							}
						}
					}
					
					if (exponent != 0) {	// Print exponent of non zero
						if (newFrom < to) {
							if (reallyFill) {
								content[newFrom] = 'E';
							}
						}
						else {
							reallyFill = false;
						}
						newFrom++;
						if ((newFrom = uncheckedPrintLong(content,newFrom,exponent,reallyFill)) < 0) {
							newFrom = -newFrom;
							reallyFill = false;
						}
					}
				}
			}
			return reallyFill ? newFrom : -newFrom;
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
		else if (from < 0) {
			throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
		}
		else {
			return printUncheckedEscapedChar(content,from,value,reallyFill,strongEscaping);
		}
	}

	private static int printUncheckedEscapedChar(final char[] content, final int from, final char value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		final int	to = content.length;
		int			newFrom = from;

		if (value < ' ') {
			switch (value) {
				case '\b' 	:
					if (newFrom < to - 2) {
						content[newFrom++] = '\\';
						content[newFrom++] = 'b';
					}
					else {
						return -(newFrom + 2);
					}
					break;
				case '\f' 	:
					if (newFrom < to - 2) {
						content[newFrom++] = '\\';
						content[newFrom++] = 'f';
					}
					else {
						return -(newFrom + 2);
					}
					break;
				case '\n' 	:
					if (newFrom < to - 2) {
						content[newFrom++] = '\\';
						content[newFrom++] = 'n';
					}
					else {
						return -(newFrom + 2);
					}
					break;
				case '\r' 	:
					if (newFrom < to - 2) {
						content[newFrom++] = '\\';
						content[newFrom++] = 'r';
					}
					else {
						return -(newFrom + 2);
					}
					break;
				case '\t' 	:
					if (newFrom < to - 2) {
						content[newFrom++] = '\\';
						content[newFrom++] = 't';
					}
					else {
						return -(newFrom + 2);
					}
					break;
				default :
					if (newFrom < to - 4) {
						content[newFrom++] = '\\';
						content[newFrom++] = '0';
						content[newFrom++] = '0';
						content[newFrom++] = '0';
					}
					else {
						return -(newFrom + 4);
					}
					break;
			}
		}
		else if (value <= 0xFF) {
			if (newFrom < to) {
				content[newFrom++] = value;
			}
			else {
				return -(newFrom + 1);
			}
		}
		else {
			if (strongEscaping) {
				if (from < to - 6) {
					content[newFrom++] = '\\';
					content[newFrom++] = 'u';
					content[newFrom++] = HEX_DIGITS[(value & 0xF000)>>12];
					content[newFrom++] = HEX_DIGITS[(value & 0x0F00)>>8];
					content[newFrom++] = HEX_DIGITS[(value & 0x00F0)>>4];
					content[newFrom++] = HEX_DIGITS[(value & 0x000F)>>0];
				}
				else {
					return -(newFrom + 6);
				}
			}
			else {
				if (from < to - 6) {
					content[newFrom++] = '\\';
					content[newFrom++] = 'u';
					content[newFrom++] = HEX_DIGITS[(value & 0xF000)>>12];
					content[newFrom++] = HEX_DIGITS[(value & 0x0F00)>>8];
					content[newFrom++] = HEX_DIGITS[(value & 0x00F0)>>4];
					content[newFrom++] = HEX_DIGITS[(value & 0x000F)>>0];
				}
				else {
					return -(newFrom + 6);
				}
			}
		}
		
		return newFrom;
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
	 */
	public static int printEscapedString(final char[] content, final int from, final String value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		return printEscapedCharArray(content,from,value.toCharArray(),0,value.length(),reallyFill,strongEscaping); 
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
	 */
	public static int printEscapedCharArray(final char[] content, final int from, final char[] value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		return printEscapedCharArray(content,from,value,0,value.length,reallyFill,strongEscaping); 
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
	 */
	public static int printEscapedCharArray(final char[] content, int from, final char[] value, final int charFrom, final int charTo, boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content buffer can't be null or empty array"); 
		}
		else if (from < 0) {
			throw new IllegalArgumentException("From position ["+from+"] can't be negative"); 
		}
		else if (value == null) {
			throw new IllegalArgumentException("Value can't be null array"); 
		}
		else if (charFrom < 0 || charFrom > value.length) {
			throw new IllegalArgumentException("CharFrom position ["+charFrom+"] out of range 0.."+(value.length-1)); 
		}
		else if (charTo < 0 || charTo > value.length) {
			throw new IllegalArgumentException("CharTo position ["+charTo+"] out of range 0.."+(value.length)); 
		}
		else {
			final int	to = content.length;
			
			for (int index = charFrom; index < charTo; index++) {
				if (from < to) {
					if ((from = printUncheckedEscapedChar(content,from,value[index],reallyFill,strongEscaping)) < 0) {
						from = -from;
						reallyFill = false;
					}
				}
				else {
					reallyFill = false;
				}
			}

			return from;
		}
	}
	
	/**
	 * <p>This interface describes lambda-styled callback for {@linkplain CharUtils#substitute(String, String, SubstitutionSource)} call</p> 
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
	 * <p>This interface describes lambda-styled callback for {@linkplain CharUtils#substitute(String, String, SubstitutionSource)} call</p> 
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
			return new String(substitute(key,value.toCharArray(),0,value.length(),new CharSubstitutionSourceWrapper(source),0));
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
			return substitute(key,value,from,length,source,0);
		}
	}
	
	private static char[] substitute(final String key, final char[] value, int from, final int length, final CharSubstitutionSource source, final int substDepth) throws NullPointerException, IllegalArgumentException {
		if (substDepth >= MAX_SUBST_DEPTH) {
			throw new IllegalArgumentException("Too deep substitution was detected (more than "+MAX_SUBST_DEPTH+") for key ["+key+"]=["+new String(value,from,length-from)+"]. Possibly you have a resursion in the substitution way!"); 
		}
		else {
			final int	to = from + length;
			
			for (int index = from; index < to; index++) {
				if (value[index] == '$') {
					final GrowableCharArray	gca = new GrowableCharArray(false);
					int						dollarPos = index, bracketCount = 0, startName = 0, endName = 0;
					boolean					wasDollar = false;

nextName:			while (dollarPos >= 0) {
						gca.append(value,from,dollarPos);
						
						if (dollarPos >= to || value[dollarPos + 1] == '{') {
end:						for (int scan = dollarPos + 1; scan < to; scan++) {
								switch (value[scan]) {
									case '{' 	:
										if (bracketCount++ == 0) {
											startName = scan + 1;
										};
										break;
									case '}' 	:
										if (--bracketCount == 0) {
											endName = scan;
											break end;
										}
										break;
									case '$'	:
										wasDollar = true;
										break;
								}
							}
							if (bracketCount != 0) {
								throw new IllegalArgumentException("Unpaired {} in the key ["+key+"]=["+new String(value,from,length-from)+"]");
							}
							else if (startName >= endName) {
								throw new IllegalArgumentException("Empty ${} in the key ["+key+"]=["+new String(value,from,length-from)+"]");
							}
							else {
								final char[]	result = source.getValue(value,startName,endName);
								
								if (result != null) {
									if (wasDollar) {
										final char[]	subst = source.getValue(result,0,result.length); 
												
										gca.append(subst != null ? substitute(key,subst,0,subst.length,source,substDepth+1) : result);
									}
									else {
										gca.append(substitute(key,result,0,result.length,source,substDepth+1));
									}
								}
								else {
									gca.append(value,startName-2,endName+1);
								}
								from = endName + 1;
							}
						}
						else {
							gca.append('$');
							from = dollarPos + 1;
						}
						for (dollarPos = from; dollarPos < to; dollarPos++) {
							if (value[dollarPos] == '$') {
								continue nextName;
							}
						}
						if (from < to) {
							gca.append(value,from,dollarPos);
						}
						dollarPos = -1;
					}
					return gca.extract();
				}
			}
			return Arrays.copyOfRange(value,from,to);
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

	public static char[] join(final char[] delimiter, final char[]... content) {
		if (delimiter == null) {
			throw new NullPointerException("String delimiter can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content to join can't be null");
		}
		else if (content.length == 0) {
			return new char[0];
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
	
	public static String join(final String delimiter, final String... content) {
		if (delimiter == null) {
			throw new NullPointerException("String delimiter can't be null");
		}
		else if (content == null) {
			throw new NullPointerException("Content to join can't be null");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			String				prefix = "";
			
			for (String item : content) {
				sb.append(prefix).append(item);
				prefix = delimiter;
			}
			return sb.toString();
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
			if (source[index] == first && compare(source,index,splitter)) {
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
			if (source[index] == first && compare(source,index,splitter)) {
				target[targetIndex++] = new String(source,start,index-start);
				index += splitterLen - 1;
				start = index + 1;
			}
		}
		target[targetIndex++] = new String(source,start,maxIndex-start);
	}
}
