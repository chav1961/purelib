package chav1961.purelib.basic.intern;

import java.util.Arrays;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;

public class UnsafedCharUtils {
	private static final int		INTMAX_2 = Integer.MAX_VALUE / 2;
	private static final int		INTMAX_8 = Integer.MAX_VALUE / 8;
	private static final int		INTMAX_10 = Integer.MAX_VALUE / 10;
	private static final int		INTMAX_16 = Integer.MAX_VALUE / 16;

	private static final long		LONGMAX_2 = Long.MAX_VALUE / 2;
	private static final long		LONGMAX_8 = Long.MAX_VALUE / 8;
	private static final long		LONGMAX_10 = Long.MAX_VALUE / 10;
	private static final long		LONGMAX_16 = Long.MAX_VALUE / 16;

	private static final int		EXP_BOUND = Double.MAX_EXPONENT;
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

	private static final int		OCT_ESCAPE_SIZE = 3;
	private static final int		U_ESCAPE_SIZE = 4;
//	private static final char[]		HYPHEN_NAME = "-".toCharArray();
//	private static final char		WILDCARD_ANY_SEQ = '*';
//	private static final char		WILDCARD_ANY_CHAR = '?';
//	

	private static final char[]		EMPTY_CHAR_ARRAY = new char[0];
	
	static {
		DOUBLE_EXPS = new double[EXP_BOUND * 2 + 1];
		for (int index = -EXP_BOUND; index <= EXP_BOUND; index++) {
			DOUBLE_EXPS[index+EXP_BOUND] = Double.parseDouble("1E"+index);
		}
	}
	
	
	public static int uncheckedParseInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (checkOverflow && temp > INTMAX_10) {
				throw new SyntaxException(0,index,"Number is greater then maximal available integer");
			}
			temp = temp * 10 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}

	public static int uncheckedParseBinaryInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '1') {
			if (checkOverflow && temp > INTMAX_2) {
				throw new SyntaxException(0,index,"Number is greater then maximal available integer");
			}
			temp = temp * 2 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}
	
	public static int uncheckedParseOctalInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '7') {
			if (checkOverflow && temp > INTMAX_8) {
				throw new SyntaxException(0,index,"Number is greater then maximal available integer");
			}
			temp = temp * 8 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}

	public static int uncheckedParseHexInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && ((symbol = source[index]) >= '0' && symbol <= '9' || symbol >= 'a' && symbol <= 'f' || symbol >= 'A' && symbol <= 'F')) {
			if (checkOverflow && temp > INTMAX_16) {
				throw new SyntaxException(0,index,"Number is greater then maximal available integer");
			}
			if (symbol >= '0' && symbol <= '9') {
				temp = temp * 16 + symbol - '0';
			}
			else if (symbol >= 'a' && symbol <= 'f') {
				temp = temp * 16 + symbol - 'a' + 10;
			}
			else {
				temp = temp * 16 + symbol - 'A' + 10;
			}
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}
	
	public static int uncheckedParseIntExtended(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		int		index = from, len = source.length;
		
		if (source[index] == '0') {
			if (index < len-1) {
				switch (source[index+1]) {
					case 'b' : case 'B' :
						index = uncheckedParseBinaryInt(source, index+2, result, checkOverflow);
						break;
					case 'x' : case 'X' :
						index = uncheckedParseHexInt(source, index+2, result, checkOverflow);
						break;
					default :
						index = uncheckedParseOctalInt(source, index+1, result, checkOverflow);
						break;
				}
			}
			else {
				index = uncheckedParseOctalInt(source, index, result, checkOverflow);
			}				
		}
		else {
			index = uncheckedParseInt(source, index, result, checkOverflow);
		}
		
		if (index == from) {
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else {
			return index;
		}
	}	
	
	public static int uncheckedParseSignedInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) throws SyntaxException {
		if (source[from] == '-') {
			final int	returned = UnsafedCharUtils.uncheckedParseInt(source, from+1, result, checkOverflow);
			
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
	
	public static int uncheckedParseLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (checkOverflow && temp > LONGMAX_10) {
				throw new SyntaxException(0,index,"Number is greater then maximal available long");
			}
			temp = temp * 10 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}

	public static int uncheckedParseBinaryLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '1') {
			if (checkOverflow && temp > LONGMAX_2) {
				throw new SyntaxException(0,index,"Number is greater then maximal available long");
			}
			temp = temp * 2 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}

	public static int uncheckedParseOctalLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '7') {
			if (checkOverflow && temp > LONGMAX_8) {
				throw new SyntaxException(0,index,"Number is greater then maximal available long");
			}
			temp = temp * 8 + symbol - '0';
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}

	public static int uncheckedParseHexLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && ((symbol = source[index]) >= '0' && symbol <= '9' || symbol >= 'a' && symbol <= 'f' || symbol >= 'A' && symbol <= 'F')) {
			if (checkOverflow && temp > LONGMAX_16) {
				throw new SyntaxException(0,index,"Number is greater then maximal available integer");
			}
			if (symbol >= '0' && symbol <= '9') {
				temp = temp * 16 + symbol - '0';
			}
			else if (symbol >= 'a' && symbol <= 'f') {
				temp = temp * 16 + symbol - 'a' + 10;
			}
			else {
				temp = temp * 16 + symbol - 'A' + 10;
			}
			index++;
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}
	
	public static int uncheckedParseLongExtended(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		int		index = from, len = source.length;
		
		if (source[index] == '0') {
			if (index < len-1) {
				switch (source[index+1]) {
					case 'b' : case 'B' :
						index = uncheckedParseBinaryLong(source, index+2, result, checkOverflow);
						break;
					case 'x' : case 'X' :
						index = uncheckedParseHexLong(source, index+2, result, checkOverflow);
						break;
					default :
						index = uncheckedParseOctalLong(source, index+1, result, checkOverflow);
						break;
				}
			}
			else {
				index = uncheckedParseOctalLong(source, index, result, checkOverflow);
			}				
		}
		else {
			index = uncheckedParseLong(source, index, result, checkOverflow);
		}
		
		if (index == from) {
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else {
			return index;
		}
	}

	public static int uncheckedParseSignedLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) throws SyntaxException {
		if (source[from] == '-') {
			final int	returned = UnsafedCharUtils.uncheckedParseLong(source, from+1, result, checkOverflow);
			
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
	
	public static int uncheckedParseFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) throws SyntaxException {
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
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
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
					throw new SyntaxException(0,index,"No one digits in the fractional part of float was detected!");
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
					throw new SyntaxException(0,index,"Number is greater then maximal available float");
				}
				
				if (index == expFrom) {
					throw new SyntaxException(0,index,"No one digits in the exponent part of float was detected!");
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
	
	public static int uncheckedParseSignedFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) throws SyntaxException {
		if (source[from] == '-') {
			final int	returned = UnsafedCharUtils.uncheckedParseFloat(source, from+1, result, checkOverflow);
			
			result[0] = -result[0];
			return returned;
		}
		else if (source[from] == '+') {
			return uncheckedParseFloat(source, from+1, result, checkOverflow);
		}
		else {
			return uncheckedParseFloat(source, from, result, checkOverflow);
		}
	}

	public static int uncheckedParseDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) throws SyntaxException {
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
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
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
					throw new SyntaxException(0,index,"No one digits in the fractional part of double was detected!");
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
					throw new SyntaxException(0,index,"Number is greater then maximal available double");
				}
				
				if (index == expFrom) {
					throw new SyntaxException(0,index,"No one digits in the exponent part of double was detected!");
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

	public static int uncheckedParseSignedDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) throws SyntaxException {
		if (source[from] == '-') {
			final int	returned = UnsafedCharUtils.uncheckedParseDouble(source, from+1, result, checkOverflow);
			
			result[0] = -result[0];
			return returned;
		}
		else if (source[from] == '+') {
			return UnsafedCharUtils.uncheckedParseDouble(source, from+1, result, checkOverflow);
		}
		else {
			return UnsafedCharUtils.uncheckedParseDouble(source, from, result, checkOverflow);
		}
	}
	
	public static int uncheckedParseNumber(final char[] source, final int from, final long[] result, final int preferences, final boolean checkOverflow) throws SyntaxException {
		long	temp = 0;
		int		index = from, len = source.length;
		char	symbol = ' ';
		boolean	continueParsing = false;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (checkOverflow) {
				if (temp > INTMAX_10 && (preferences & (CharUtils.PREF_LONG | CharUtils.PREF_FLOAT)) == 0) {
					throw new SyntaxException(0,index,"Number is greater then maximal available integer");
				}
				if (temp > LONGMAX_10 && (preferences & CharUtils.PREF_DOUBLE) == 0) {
					throw new SyntaxException(0,index,"Number is greater then maximal available long");
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
			if ((preferences & CharUtils.PREF_LONG) == 0) {
				throw new SyntaxException(0,index,"Long constant is not waited here");
			}
			else {
				result[0] = temp; 
				result[1] = CharUtils.PREF_LONG;
				return index+1;
			}
		}
		
		if (index == from) {
			throw new SyntaxException(0,index,"No one digits in the number was detected!");
		}
		else if (index < len && (symbol == '.' || symbol == 'e' || symbol == 'E' || symbol == 'f' || symbol == 'F') || continueParsing) {
			if ((preferences & (CharUtils.PREF_FLOAT | CharUtils.PREF_DOUBLE)) != 0) {
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
						throw new SyntaxException(0,index,"No one digits in the fractional part of double was detected!");
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
						throw new SyntaxException(0,index,"Number is greater then maximal available double");
					}
					
					if (index == expFrom) {
						throw new SyntaxException(0,index,"No one digits in the exponent part of double was detected!");
					}
					else {
						tempInt *= DOUBLE_EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
					}
				}

				if (symbol == 'f' || symbol == 'F') {
					index++;
					if ((preferences & CharUtils.PREF_FLOAT) == 0) {
						throw new SyntaxException(0,index,"Float number is not waited here");
					}
					else if (Math.abs(tempInt) < Float.MAX_VALUE) {
						result[0] = Float.floatToIntBits((float)tempInt);
						result[1] = CharUtils.PREF_FLOAT;
					}
					else {
						throw new SyntaxException(0,index,"Float number is greater then maximal available float");
					}
				}
				else {
					if ((preferences & CharUtils.PREF_DOUBLE) != 0) {
						result[0] = Double.doubleToLongBits(tempInt);
						result[1] = CharUtils.PREF_DOUBLE;
					}
					else {
						throw new SyntaxException(0,index,"Double number is not waited here");
					}
				}
			}
			else {
				result[0] = temp; 
				result[1] = temp > Integer.MAX_VALUE ? CharUtils.PREF_LONG : CharUtils.PREF_INT;
			}
		}
		else {
			result[0] = temp; 
			result[1] = temp > Integer.MAX_VALUE ? CharUtils.PREF_LONG : CharUtils.PREF_INT;
		}
		return index;
	}

	public static int uncheckedValidateNumber(final char[] source, final int from, final int preferences, final boolean checkOverflow) {
		long	temp = 0;
		int		index = from, len = source.length;
		char	symbol = ' ';
		boolean	continueParsing = false;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (checkOverflow) {
				if (temp > INTMAX_10 && (preferences & (CharUtils.PREF_LONG | CharUtils.PREF_FLOAT)) == 0) {
					return -index-1;
				}
				if (temp > LONGMAX_10 && (preferences & CharUtils.PREF_DOUBLE) == 0) {
					return -index-1;
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
			if ((preferences & CharUtils.PREF_LONG) == 0) {
				return -index-1;
			}
			else {
				return index+1;
			}
		}

		double	tempInt = temp;
		
		if (continueParsing) {
			while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
				tempInt = tempInt * 10 + symbol - '0';
				index++;
			}
		}
		
		if (index == from) {
			return -index-1;
		}
		else if (index < len && (symbol == '.' || symbol == 'e' || symbol == 'E' || symbol == 'f' || symbol == 'F') || continueParsing) {
			if ((preferences & (CharUtils.PREF_FLOAT | CharUtils.PREF_DOUBLE)) != 0) {
				if (symbol == '.') {
					double	frac = 0, scale = 1;
					int		fracFrom = ++index;
					
					while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
						frac = frac * 10 + symbol - '0';
						scale *= 0.1;
						index++;
					}
					
					if (index == fracFrom) {
						return -index-1;
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
						return -index-1;
					}
					
					if (index == expFrom) {
						return -index-1;
					}
					else {
						tempInt *= DOUBLE_EXPS[EXP_BOUND+Math.max(-EXP_BOUND,Math.min(EXP_BOUND,exp))];
					}
				}

				if (symbol == 'f' || symbol == 'F') {
					index++;
					if ((preferences & CharUtils.PREF_FLOAT) == 0) {
						throw new NumberFormatException("Float number is not waited here");
					}
					else if (Math.abs(tempInt) < Float.MAX_VALUE) {
					}
					else {
						return -index-1;
					}
				}
				else {
					if ((preferences & CharUtils.PREF_DOUBLE) != 0) {
					}
					else {
						return -index-1;
					}
				}
			}
			else {
				return -index-1;
			}
		}
		else {
		}
		return index; 
	}
	
	public static int uncheckedParseEscapedChar(final char[] source, int from, final char[] result) {
		final int 	len = source.length;
		
		if (source[from] == '\\') {
			if (from < len - 1) {
				switch (source[from+1]) {
					case '\"' 	: result[0] = '\"'; from += 2; break;
					case '\'' 	: result[0] = '\''; from += 2; break;
					case '\\' 	: result[0] = '\\'; from += 2; break;
					case 'b' 	: result[0] = '\b'; from += 2; break;
					case 'f' 	: result[0] = '\f'; from += 2; break;
					case 'n' 	: result[0] = '\n'; from += 2; break;
					case 'r' 	: result[0] = '\r'; from += 2; break;
					case 't' 	: result[0] = '\t'; from += 2; break;
					case '0' 	:
						if (from + 1 > len - OCT_ESCAPE_SIZE) {
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
						if (from + 2 > len - U_ESCAPE_SIZE) {
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
	
	public static int uncheckedParseUnescapedString(final char[] source, final int from, final char terminal, final boolean checkEscaping, final int[] result) {
		final int		len = source.length;
		
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
	
	public static int uncheckedParseString(final char[] source, final int from, final char terminal, final StringBuilder result) {
		final int		len = source.length;
		
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
	
	public static int uncheckedParseStringExtended(final char[] source, final int from, final char terminal, final StringBuilder result) {
		final int		len = source.length;
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
	
	public static int uncheckedParseName(final char[] source, final int from, final int[] result) {
		if (!Character.isJavaIdentifierStart(source[from])) {
			throw new IllegalArgumentException("No valid beginning of the name"); 
		}
		else {
			int		index, len = source.length;
			
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

	public static int uncheckedParseNameExtended(final char[] source, final int from, final int[] result, final char... availableChars) {
		if (!Character.isJavaIdentifierStart(source[from])) {
			throw new IllegalArgumentException("No valid beginning of the name"); 
		}
		else {
			int		index, len = source.length;
			
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
	
	public static final int uncheckedSkipBlank(final char[] source, final int from, final boolean stopOnEOL) {
		if (stopOnEOL) {
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
	
	public static int uncheckedPrintLong(final char[] content, final int from, final long value, boolean reallyFill) throws IllegalArgumentException {
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

	public static int uncheckedPrintDouble(final char[] content, final int from, double value, boolean reallyFill) throws IllegalArgumentException {
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
				
				if ((newFrom = UnsafedCharUtils.uncheckedPrintLong(content,newFrom,(long)intPart,reallyFill)) < 0) {
					newFrom = -newFrom;
					reallyFill = false;
				}

				if (fracPart != 0) {	// Print fraction of non zero
					final int	dot = newFrom;	// Store dot location
					
					fracPart += 1E18; // +1E18 to strongly type left unsignificant zeroes
					if ((newFrom = UnsafedCharUtils.uncheckedPrintLong(content,newFrom,(long)fracPart,reallyFill)) < 0) {
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
					if ((newFrom = UnsafedCharUtils.uncheckedPrintLong(content,newFrom,exponent,reallyFill)) < 0) {
						newFrom = -newFrom;
						reallyFill = false;
					}
				}
			}
		}
		return reallyFill ? newFrom : -newFrom;
	}

	public static int uncheckedPrintEscapedChar(final char[] content, final int from, final char value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
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
						content[newFrom++] = (char)('0' + value / 8);
						content[newFrom++] = (char)('0' + value % 8);
					}
					else {
						return -(newFrom + 4);
					}
					break;
			}
		}
		else if (value <= 0xFF) {
			switch (value) {
				case '\\' : case '\"' : case '\'' :
					if (newFrom < to-1) {
						content[newFrom++] = '\\';
						content[newFrom++] = value;
					}
					else {
						return -(newFrom + 2);
					}
					break;
				default :
					if (newFrom < to) {
						content[newFrom++] = value;
					}
					else {
						return -(newFrom + 1);
					}
					break;
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
	
	public static int uncheckedPrintEscapedCharArray(final char[] content, int from, final char[] value, final int charFrom, final int charTo, boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
		final int	to = content.length;
		
		for (int index = charFrom; index < charTo; index++) {
			if (from < to) {
				if ((from = UnsafedCharUtils.uncheckedPrintEscapedChar(content,from,value[index],reallyFill,strongEscaping)) < 0) {
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
	
	public static boolean uncheckedCompare(final char[] source, final int from, final char[] template, final int templateFrom, final int templateLen) {
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

	public static char[] uncheckedSubstitute(final String key, final char[] value, int from, final int length, final CharSubstitutionSource source, final int substDepth) throws NullPointerException, IllegalArgumentException {
		if (substDepth >= CharUtils.MAX_SUBST_DEPTH) {
			throw new IllegalArgumentException("Too deep substitution was detected (more than "+CharUtils.MAX_SUBST_DEPTH+") for key ["+key+"]=["+new String(value,from,length-from)+"]. Possibly you have a resursion in the substitution way!"); 
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
												
										gca.append(subst != null ? uncheckedSubstitute(key,subst,0,subst.length,source,substDepth+1) : result);
									}
									else {
										gca.append(uncheckedSubstitute(key,result,0,result.length,source,substDepth+1));
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
	

	public static String[] split(final String source, final char splitter) throws NullPointerException {
		final char[]	content = source.toCharArray();
		final int		amount = calculateSplitters(content,splitter);
		final String[]	result = new String[amount + 1];
		
		split(content,splitter,result);
		return result;
	}

	public static int split(final String source, final char splitter, final String[] target) throws NullPointerException {
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

	public static String[] split(final String source, final String splitter) throws NullPointerException, IllegalArgumentException {
		final char[]	content = source.toCharArray();
		final char[]	toSplit = splitter.toCharArray();
		final int		amount = calculateSplitters(content,toSplit);
		final String[]	result = new String[amount + 1];
		
		split(content,toSplit,result);
		return result;
	}

	public static int split(final String source, final String splitter, final String[] target) throws NullPointerException, IllegalArgumentException {
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

	public static char[] join(final char[] delimiter, final char[]... content) throws IllegalArgumentException, NullPointerException {
		if (content.length == 0) {
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
	
	public static String join(final String delimiter, final String... content) throws IllegalArgumentException, NullPointerException {
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
