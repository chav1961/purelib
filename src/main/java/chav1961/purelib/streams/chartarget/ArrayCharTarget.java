package chav1961.purelib.streams.chartarget;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class implements {@link CharacterTarget} interface by any character arrays.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.6
 */
public class ArrayCharTarget implements CharacterTarget {
	private final char[]	target;
	private final int		from, max;
	private int				written = 0, actual;
	
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
			this.max = this.target.length;
			this.from = from;
			internalReset();
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
	public CharacterTarget put(final char[] symbols) throws PrintingException {
		if (symbols == null) {
			throw new NullPointerException("Symbols can't be null"); 
		}
		else {
			return put(symbols,0,symbols.length);
		}
	}

	@Override
	public CharacterTarget put(final char[] symbols, final int from, final int to) throws PrintingException {
		final int	delta = to - from;
		
		if (symbols == null) {
			throw new NullPointerException("Symbols can't be null"); 
		}
		else if (actual + delta < max) {
			System.arraycopy(symbols,from,target,actual,delta);
			actual += delta;
			written += delta; 
			return this;
		}
		else {
			throw new PrintingException("Target array exhausted!");
		}
	}

	@Override
	public CharacterTarget put(final String source) throws PrintingException {
		if (source == null) {
			throw new NullPointerException("Source can't be null"); 
		}
		else {
			return put(source,0,source.length());
		}
	}

	@Override
	public CharacterTarget put(final String source, final int from, final int to) throws PrintingException {
		final int	delta = to - from;
		
		if (source == null) {
			throw new NullPointerException("Source can't be null"); 
		}
		else if (actual + delta < max) {
			source.getChars(from,to,target,actual);
			actual += delta;
			written += delta; 
			return this;
		}
		else {
			throw new PrintingException("Target array exhausted!");
		}
	}

	@Override
	public Appendable append(final CharSequence csq) throws IOException {
		if (csq == null) {
			throw new NullPointerException("Sequence toadd can't be null");
		}
		else {
			return append(csq, 0, csq.length());
		}
	}

	@Override
	public Appendable append(final CharSequence csq, final int start, final int end) throws IOException {
		if (csq == null) {
			throw new NullPointerException("Sequence toadd can't be null");
		}
		else {
			try{for(int index=start; index < end; index++) {
						put(csq.charAt(index));
				}
				return this;
			} catch (PrintingException e) {
				throw new IOException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public Appendable append(char c) throws IOException {
		try{put(c);
			return this;
		} catch (PrintingException e) {
			throw new IOException(e.getLocalizedMessage(), e); 
		}
	}
	
	@Override
	public void reset() throws EnvironmentException {
		internalReset();
	}
	
	@Override public int totalWritten() {return written;}
	@Override public int atRow() {return 0;}
	@Override public int atColumn() {return 0;}

	private void internalReset() {
		this.actual = from;
		this.written = 0;
	}
}
