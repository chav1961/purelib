package chav1961.purelib.streams.chartarget;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class implements {@link CharacterTarget} interface by string builder.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.2
 *
 */
public class StringBuilderCharTarget implements CharacterTarget {
	private final StringBuilder target;
	private final int			len;
	private int					written = 0;

	/**
	 * <p>Constructor of the object</p>
	 * @param target string builder to collect data
	 */
	public StringBuilderCharTarget(final StringBuilder target) {
		if (target == null) {
			throw new NullPointerException("Target builder can't be null"); 
		}
		else {
			this.target = target;
			this.len = target.length();
			internalReset();
		}
	}
	
	@Override
	public CharacterTarget put(final char symbol) throws PrintingException {
		target.append(symbol);
		written++;
		return this;
	}

	@Override
	public CharacterTarget put(final char[] symbols) throws PrintingException {
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
		else {
			target.append(symbols,from,to);
			written += to - from;
			return this;
		}
	}

	@Override
	public CharacterTarget put(final String source) throws PrintingException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null"); 
		}
		else {
			target.append(source);
			written += source.length();
			return this;
		}
	}

	@Override
	public CharacterTarget put(final String source, final int from, final int to) throws PrintingException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null"); 
		}
		else {
			target.append(source,from, to);
			written += to - from;
			return this;
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
		this.target.setLength(this.len);
		this.written = 0;
	}
}
