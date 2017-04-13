package chav1961.purelib.basic;

/**
 * <p>This class contains implementation of the most-commonly-used char data parsing functions in the system.</p> 
 * 
 * <p>All the methods in the class are oriented to direct parsing of the char sequences. Every method in the class accepts source data array
 * and starting position from it to parse, and returns the position in the source array after parsing. This returned value can then be used
 * in the subsequent calls as starting position. Any parsed object are returned from the methods thru the arrays of appropriative data. This 
 * technique emulates call-by-reference mode for the method parameters, for example:</p>
 * <code>
 * 		final char[] source = "1234 1234".toCharArray(); <br>
 * 		final int[]	value1 = new int[1], value2 = new int[1]; <br>
 *  	final int	endPos1 = CharsUtil.parseInt(source,0,value1,false);				// endPos1 = 4, value1[0] = 1234 <br>
 *  	final int	endPos2 = CharsUtil.parseInt(source,endPos1+1,value2,false);		// endPos2 = 9, value2[0] = 5678 <br>
 * </code> 
 * <p>All the methods in the class are thread-safe</p>
 *  
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class CharsUtil {
	public static final int			PREF_INT = 1;
	public static final int			PREF_LONG = 2;
	public static final int			PREF_FLOAT = 4;
	public static final int			PREF_DOUBLE = 8;
	public static final int			PREF_ANY = PREF_INT | PREF_LONG | PREF_FLOAT | PREF_DOUBLE;
	
	private static final int		INTMAX_2 = Integer.MAX_VALUE / 2;
	private static final int		INTMAX_8 = Integer.MAX_VALUE / 8;
	private static final int		INTMAX_10 = Integer.MAX_VALUE / 10;
	private static final int		INTMAX_16 = Integer.MAX_VALUE / 16;
	private static final long		LONGMAX_2 = Long.MAX_VALUE / 2;
	private static final long		LONGMAX_8 = Long.MAX_VALUE / 8;
	private static final long		LONGMAX_10 = Long.MAX_VALUE / 10;
	private static final long		LONGMAX_16 = Long.MAX_VALUE / 16;
	private static final int		EXP_BOUND = Double.MAX_EXPONENT;
	private static final int		U_ESCAPE_SIZE = 4;
	private static final double		EXPS[];
	
	static {
		EXPS = new double[EXP_BOUND * 2 + 1];
		for (int index = -EXP_BOUND; index <= EXP_BOUND; index++) {
			EXPS[index+EXP_BOUND] = Double.parseDouble("1E"+index);
		}
	}
	
	/**
	 * <p>Extract unsigned integer value from the current position of the source data</p>
	 * @param source source data contains character representation of the integer value
	 * @param from starting position in the source data
	 * @param result array (new int[1]) to store parsed value
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
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
		}
		else {
			int		temp = 0, index = from;
			char	symbol;
			
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
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
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
	 * <p>Extract unsigned integer value from the current position of the source data</p>
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
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
		}
		else {
			long	temp = 0;
			int		index = from;
			char	symbol;
			
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
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
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
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
		}
		else {
			long	temp = 0;
			int		index = from;
			char	symbol = ' ';
			boolean	continueParsing = false;
			
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
						tempInt *= EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
					}
				}
				result[0] = (float)tempInt;
			}
			else {
				result[0] = temp;
			}
			return index;
		}
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
		else if (result == null || result.length == 0) {
			throw new IllegalArgumentException("Result array can't be null or empty array"); 
		}
		else {
			long	temp = 0;
			int		index = from;
			char	symbol = ' ';
			boolean	continueParsing = false;
			
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
						tempInt *= EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
					}
				}
				result[0] = tempInt;
			}
			else {
				result[0] = temp;
			}
			return index;
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
	 * @throws IllegalArgumentException if any parsing errors ware detected 
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
					if (temp > INTMAX_10 && (preferences & (PREF_LONG | PREF_DOUBLE)) == 0) {
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
					throw new NumberFormatException("Long constant is not wated here");
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
						tempInt *= EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
					}
				}

				if (symbol == 'f' || symbol == 'f') {
					index++;
					if ((preferences | PREF_FLOAT) == 0) {
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
					if ((preferences | PREF_DOUBLE) != 0) {
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
			return index;
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
				else if (source[index] == '\\' && checkEscaping) {
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
			throw new IllegalArgumentException("Result builder can't be null"); 
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
			throw new IllegalArgumentException("Result builder can't be null"); 
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
								else if (source[index+2] == 'x' || source[index+2] == 'x') {
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
			throw new IllegalArgumentException("Result class can't be null"); 
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
			throw new IllegalArgumentException("Template data can't be null"); 
		}
		else if (template.length == 0) {
			return true;
		}
		else {
			return compare(source,from,template,0,template.length);
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
			throw new IllegalArgumentException("Template data can't be null"); 
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
			for (int index = 0; index < templateLen; index++) {
				if (source[from+index] != template[templateFrom+index]) {
					return false;
				}
			}
			return true;
		}
	}
}
