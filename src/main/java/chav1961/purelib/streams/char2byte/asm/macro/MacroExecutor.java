package chav1961.purelib.streams.char2byte.asm.macro;

import java.util.Arrays;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.streams.char2byte.asm.AssignableExpressionNodeInterface;

public abstract class MacroExecutor implements MacroExecutorInterface {
	static final char[]				TRUE_CONTENT = "true".toCharArray();
	static final char[]				EMPTY_CONTENT = new char[0];
	
	public final char[]				stringResource;
	protected final long[]			longResult = new long[1]; 
	protected final double[]		doubleResult = new double[1]; 
	
	protected MacroExecutor(final char[] stringResource) {
		this.stringResource = stringResource;
	}
	
	@Override
	public abstract void exec(final AssignableExpressionNodeInterface[] memory, final GrowableCharArray<?> target) throws CalculationException;
	
	public char[] extractString(final int from, final int len) throws CalculationException {
		return Arrays.copyOfRange(stringResource,from,from+len);
	}
	
	public static boolean exists(@SuppressWarnings("exports") final AssignableExpressionNode[] memory, final int variableIndex) throws CalculationException {
		return memory[variableIndex].getValueType() != null;
	}
	
	public static char[] toString(final long value) throws CalculationException {
		return String.valueOf(value).toCharArray();
	}

	public static char[] toString(final long[] value) throws CalculationException {
		final StringBuilder	sb = new StringBuilder();
		char				prefix = '{';
		
		for (long item : value) {
			sb.append(prefix).append(item);
			prefix=',';
		}
		if (sb.length() == 0) {
			sb.append(prefix);
		}
		sb.append('}');
		
		final char[]		result = new char[sb.length()];
		
		sb.getChars(0, result.length, result, 0);
		return result;
	}
	
	public static char[] toString(final double value) throws CalculationException {
		return String.valueOf(value).toCharArray();
	}

	public static char[] toString(final double[] value) throws CalculationException {
		final StringBuilder	sb = new StringBuilder();
		char				prefix = '{';
		
		for (double item : value) {
			sb.append(prefix).append(item);
			prefix=',';
		}
		if (sb.length() == 0) {
			sb.append(prefix);
		}
		sb.append('}');
		
		final char[]		result = new char[sb.length()];
		
		sb.getChars(0, result.length, result, 0);
		return result;
	}

	public static char[] toString(final char[][] value) throws CalculationException {
		final StringBuilder	sb = new StringBuilder();
		char				prefix = '{';
		
		for (char[] item : value) {
			sb.append(prefix).append('\"').append(item != null ? item : EMPTY_CONTENT).append('\"');
			prefix=',';
		}
		if (sb.length() == 0) {
			sb.append(prefix);
		}
		sb.append('}');
		
		final char[]		result = new char[sb.length()];
		
		sb.getChars(0, result.length, result, 0);
		return result;
	}
	
	public static char[] toString(final boolean value) throws CalculationException {
		return String.valueOf(value).toCharArray();
	}

	public static char[] toString(final boolean[] value) throws CalculationException {
		final StringBuilder	sb = new StringBuilder();
		char				prefix = '{';
		
		for (boolean item : value) {
			sb.append(prefix).append(item);
			prefix=',';
		}
		if (sb.length() == 0) {
			sb.append(prefix);
		}
		sb.append('}');
		
		final char[]		result = new char[sb.length()];
		
		sb.getChars(0, result.length, result, 0);
		return result;
	}
	
	public static int compareStrings(final char[] left, final char[] right) {
		final int	leftLen = left.length, rightLen = right.length, lim = Math.min(leftLen,rightLen);
		int			result;
        
		for (int index = 0; index < lim; index++) {
			if ((result = left[index] - right[index]) != 0) {
				return result;
			}
		}
        return leftLen - rightLen;
	}
	
	public static void testInitialized(final AssignableExpressionNodeInterface node) throws CalculationException {
		if (!node.hasValue()) {
			throw new CalculationException("Variable/parameter ["+new String(node.getName())+"] is not initialized. Assign some value to it");
		}
	}

	public static boolean valueExists(final AssignableExpressionNodeInterface node) throws CalculationException {
		return node.hasValue();
	}
	
	public static boolean toBoolean(final char[] content) throws CalculationException {
		if (content == null) {
			throw new CalculationException("Attempt to convert null string to boolean");
		}
		else {
			return UnsafedCharUtils.uncheckedCompare(content,0,TRUE_CONTENT,0,TRUE_CONTENT.length);
		}
	}
	
	public static char[][] split(final char[] source, final char splitter[]) throws CalculationException {
		if (source == null) {
			throw new CalculationException("Attempt to split null string");
		}
		else if (splitter == null) {
			throw new CalculationException("Attempt to use null splitter");
		}
		else if (splitter.length == 0) {
			final char[][]	result = new char[source.length][];
			
			for(int index = 0; index < result.length; index++) {
				result[index] = new char[] {source[index]};
			}
			return result;
		}
		else {
			final int	symbol = splitter[0];
			int			counter = 1;
			
			for (int index = 0, maxIndex = source.length; index < maxIndex; index++) {
				if (source[index] == symbol && UnsafedCharUtils.uncheckedCompare(source,index,splitter,0,splitter.length)) {
					counter++;
					index += splitter.length;
				}
			}
			
			final char[][]	result = new char[counter][];
			int				start = 0;
			
			counter = 0;
			for (int index = 0, maxIndex = source.length; index < maxIndex; index++) {
				if (source[index] == symbol && UnsafedCharUtils.uncheckedCompare(source,index,splitter,0,splitter.length)) {
					result[counter++] = Arrays.copyOfRange(source,start,index);
					start = index += splitter.length;
				}
			}
			result[counter] = Arrays.copyOfRange(source,start,source.length);
			return result;
		}
	}

	public static void throwException(final Throwable exc) throws Throwable {
		throw exc;
	}
	
	public static char[] environment(final char[] varName) {
		final String	env = new String(varName);
		final String	val = System.getProperties().containsKey(env) ? System.getProperty(env) : System.getenv().containsKey(env) ? System.getenv(env) : "";

		return val.toCharArray();
	}
}
