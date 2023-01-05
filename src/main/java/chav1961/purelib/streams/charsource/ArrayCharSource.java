package chav1961.purelib.streams.charsource;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.streams.interfaces.CharacterSource;

/**
 * <p>This class implements {@link CharacterSource} interface by any character arrays</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.2
 */
public class ArrayCharSource implements CharacterSource {
	private final char[]	source;
	private final int		from, len;
	private boolean			backed = false;
	private int				actual, actualRow, actualCol;
	private char			last = ' ';

	/**
	 * <p>Constructor of the class</p>
	 * @param source source character array
	 */
	public ArrayCharSource(final char[] source) {
		this(source,0);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param source source character array
	 * @param from start position inside the array
	 */
	public ArrayCharSource(final char[] source, final int from) {
		int		len = 0;
		
		if (source == null || (len = source.length) == 0) {
			throw new IllegalArgumentException("Source can't be null or empty array");
		}
		else if (from < 0 || from >= len) { 
			throw new IllegalArgumentException("From posititon outside the array. Need be in 0.."+(len-1));
		}
		else {
			this.source = source;	
			this.from = from;
			this.len = len;
			internalReset();
		}		
	}
	
	@Override 
	public char next() throws ContentException {
		backed = false;
		if (actual < len) {
			if (source[actual] == '\n') {
				actualRow++;
				actualCol = 1;
			}
			else {
				actualCol++;
			}
			return  last = source[actual++];
		}
		else {
			return last = CharacterSource.EOF;
		}
	}

	@Override
	public void reset() throws EnvironmentException {
		internalReset();
	}

	@Override
	public void back() throws ContentException {
		if (actual <= from) {
			throw new ContentException("Attemp to back char before any reading"); 
		}
		else if (backed) {
			throw new ContentException("Attemp to call back twice. Use next() between back() calls"); 
		}
		else {
			actual--;
		}
	}
	
	
	@Override public char last() {return last;}
	@Override public int totalReaded() {return actual-from;}
	@Override public int atRow() {return actualRow;}
	@Override public int atColumn() {return actualCol;}

	@Override public String toString() {return "ArrayCharSource [actualRow=" + actualRow + ", actualCol=" + actualCol + ", last=" + last + "]";}

	private void internalReset() {
		this.actual = from;
		this.actualRow = 1;
		this.actualCol = from + 1;
		this.last = ' ';
	}
}
