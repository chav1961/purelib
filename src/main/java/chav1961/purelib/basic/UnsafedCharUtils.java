package chav1961.purelib.basic;

class UnsafedCharUtils {
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
	
	static {
		DOUBLE_EXPS = new double[EXP_BOUND * 2 + 1];
		for (int index = -EXP_BOUND; index <= EXP_BOUND; index++) {
			DOUBLE_EXPS[index+EXP_BOUND] = Double.parseDouble("1E"+index);
		}
	}
	
	
	static int uncheckedParseInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
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

	static int uncheckedParseBinaryInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '1') {
			if (checkOverflow && temp > INTMAX_2) {
				throw new NumberFormatException("Number is greater then maximal available integer");
			}
			temp = temp * 2 + symbol - '0';
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
	
	static int uncheckedParseOctalInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '7') {
			if (checkOverflow && temp > INTMAX_8) {
				throw new NumberFormatException("Number is greater then maximal available integer");
			}
			temp = temp * 8 + symbol - '0';
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

	static int uncheckedParseHexInt(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
		final int	len = source.length;
		int			temp = 0, index = from;
		char		symbol;
		
		while (index < len && ((symbol = source[index]) >= '0' && symbol <= '9' || symbol >= 'a' && symbol <= 'f' || symbol >= 'A' && symbol <= 'F')) {
			if (checkOverflow && temp > INTMAX_16) {
				throw new NumberFormatException("Number is greater then maximal available integer");
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
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}
	
	static int uncheckedParseIntExtended(final char[] source, final int from, final int[] result, final boolean checkOverflow) {
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
	
	static int uncheckedParseLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
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

	static int uncheckedParseBinaryLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '1') {
			if (checkOverflow && temp > LONGMAX_2) {
				throw new NumberFormatException("Number is greater then maximal available long");
			}
			temp = temp * 2 + symbol - '0';
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

	static int uncheckedParseOctalLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '7') {
			if (checkOverflow && temp > LONGMAX_8) {
				throw new NumberFormatException("Number is greater then maximal available long");
			}
			temp = temp * 8 + symbol - '0';
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

	static int uncheckedParseHexLong(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
		final int	len = source.length;
		long		temp = 0;
		int			index = from;
		char		symbol;
		
		while (index < len && ((symbol = source[index]) >= '0' && symbol <= '9' || symbol >= 'a' && symbol <= 'f' || symbol >= 'A' && symbol <= 'F')) {
			if (checkOverflow && temp > LONGMAX_16) {
				throw new NumberFormatException("Number is greater then maximal available integer");
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
			throw new NumberFormatException("No one digits in the number was detected!");
		}
		else {
			result[0] = temp;
			return index;
		}
	}
	
	static int uncheckedParseLongExtended(final char[] source, final int from, final long[] result, final boolean checkOverflow) {
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

	static int uncheckedParseFloat(final char[] source, final int from, final float[] result, final boolean checkOverflow) {
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

	static int uncheckedParseDouble(final char[] source, final int from, final double[] result, final boolean checkOverflow) {
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

	static int uncheckedParseNumber(final char[] source, final int from, final long[] result, final int preferences, final boolean checkOverflow) {
		long	temp = 0;
		int		index = from, len = source.length;
		char	symbol = ' ';
		boolean	continueParsing = false;
		
		while (index < len && (symbol = source[index]) >= '0' && symbol <= '9') {
			if (checkOverflow) {
				if (temp > INTMAX_10 && (preferences & (CharUtils.PREF_LONG | CharUtils.PREF_FLOAT)) == 0) {
					throw new NumberFormatException("Number is greater then maximal available integer");
				}
				if (temp > LONGMAX_10 && (preferences & CharUtils.PREF_DOUBLE) == 0) {
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
			if ((preferences & CharUtils.PREF_LONG) == 0) {
				throw new NumberFormatException("Long constant is not waited here");
			}
			else {
				result[0] = temp; 
				result[1] = CharUtils.PREF_LONG;
				return index+1;
			}
		}
		
		if (index == from) {
			throw new NumberFormatException("No one digits in the number was detected!");
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
					if ((preferences & CharUtils.PREF_FLOAT) == 0) {
						throw new NumberFormatException("Float number is not waited here");
					}
					else if (Math.abs(tempInt) < Float.MAX_VALUE) {
						result[0] = Float.floatToIntBits((float)tempInt);
						result[1] = CharUtils.PREF_FLOAT;
					}
					else {
						throw new NumberFormatException("Float number is greater then maximal available float");
					}
				}
				else {
					if ((preferences & CharUtils.PREF_DOUBLE) != 0) {
						result[0] = Double.doubleToLongBits(tempInt);
						result[1] = CharUtils.PREF_DOUBLE;
					}
					else {
						throw new NumberFormatException("Double number is not waited here");
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

	static int uncheckedValidateNumber(final char[] source, final int from, final int preferences, final boolean checkOverflow) {
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
		
		if (index == from) {
			return -index-1;
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
			}
		}
		else {
		}
		return index;
	}
	
	static int uncheckedParseName(final char[] source, final int from, final int[] result) {
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

	static int uncheckedParseNameExtended(final char[] source, final int from, final int[] result, final char... availableChars) {
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
	
	static final int uncheckedSkipBlank(final char[] source, final int from, final boolean stopOnEOL) {
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
	
	static int uncheckedPrintLong(final char[] content, final int from, final long value, boolean reallyFill) throws IllegalArgumentException {
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

	static int unckeckedPrintDouble(final char[] content, final int from, double value, boolean reallyFill) throws IllegalArgumentException {
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

	static int printUncheckedEscapedChar(final char[] content, final int from, final char value, final boolean reallyFill, final boolean strongEscaping) throws IllegalArgumentException {
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
	
	static boolean uncheckedCompare(final char[] source, final int from, final char[] template, final int templateFrom, final int templateLen) {
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
}
