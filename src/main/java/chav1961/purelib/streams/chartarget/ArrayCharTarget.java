package chav1961.purelib.streams.chartarget;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class implements {@link CharacterTarget} interface by any character arrays.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 *
 */
public class ArrayCharTarget implements CharacterTarget {
	private final char[]	target;
	private int				written = 0, actual, max;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param target target array to store data to
	 * @param from start position in the target array
	 */
	public ArrayCharTarget(final char[] target, final int from) {
		if (target == null || target.length == 0) {
			throw new IllegalArgumentException("Target can't be null or empty array"); 
		}
		else if (from < 0 || from >= target.length) {
			throw new IllegalArgumentException("From position ["+from+"] outside the array. Need be in 0.."+(target.length-1)); 
		}
		else {
			this.target = target;
			this.actual = from;
			this.max = this.target.length;
		}
	}
	
	@Override
	public CharacterTarget put(final char symbol) throws PrintingException {
		if (actual + 1 < max) {
			target[actual++] = symbol;
			written++;
			return this;
		}
		else {
			throw new PrintingException("Target array exhausted!");
		}
	}

	@Override
	public CharacterTarget put(char[] symbols) throws PrintingException {
		if (symbols == null) {
			throw new IllegalArgumentException("Symbols can't be null"); 
		}
		else {
			return put(symbols,0,symbols.length);
		}
	}

	@Override
	public CharacterTarget put(char[] symbols, int from, int to) throws PrintingException {
		if (symbols == null) {
			throw new IllegalArgumentException("Symbols can't be null"); 
		}
		else if (actual + (to - from) < max) {
			System.arraycopy(symbols,from,target,actual,to-from);
			actual += (to-from);
			written += (to-from); 
			return this;
		}
		else {
			throw new PrintingException("Target array exhausted!");
		}
	}

	@Override
	public CharacterTarget put(final String source) throws PrintingException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null"); 
		}
		else {
			return put(source,0,source.length());
		}
	}

	@Override
	public CharacterTarget put(final String source, final int from, final int to) throws PrintingException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null"); 
		}
		else if (actual + (to - from) < max) {
			source.getChars(from,to,target,actual);
			actual += (to-from);
			written += (to-from); 
			return this;
		}
		else {
			throw new PrintingException("Target array exhausted!");
		}
	}

	@Override public int totalWritten() {return written;}
	@Override public int atRow() {return 0;}
	@Override public int atColumn() {return 0;}
}
