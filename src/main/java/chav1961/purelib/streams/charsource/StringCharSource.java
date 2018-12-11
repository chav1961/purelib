package chav1961.purelib.streams.charsource;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.streams.interfaces.CharacterSource;

/**
 * <p>This class implements {@link CharacterSource} interface by single string</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 last update 0.0.2
 *
 */
public class StringCharSource implements CharacterSource {
	private final String	source;
	private final int		from, len;
	private boolean			backed = false;
	private int				actual, actualRow, actualCol;
	private char			last = ' ';

	/**
	 * <p>Constructor if the class
	 * @param source source string
	 */
	public StringCharSource(final String source) {
		this(source,0);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param source source string
	 * @param from starting position inside the string
	 */
	public StringCharSource(final String source, final int from) {
		int		len = 0;
		
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source can't be null or empty");
		}
		else if (from < 0 || from >= (len = source.length())) { 
			throw new IllegalArgumentException("From posititon outside the string. Need be in 0.."+(len-1));
		}
		else  {
			this.source = source;			this.from = this.actual = from;
			this.len = source.length();
			this.actualRow = 1;				this.actualCol = from + 1;
		}		
	}
	
	@Override 
	public char next() throws ContentException {
		backed = false;
		if (actual < len) {
			if (source.charAt(actual) == '\n') {
				actualRow++;
				actualCol = 1;
			}
			else {
				actualCol++;
			}
			return  last = source.charAt(actual++);
		}
		else {
			return  last = CharacterSource.EOF;
		}
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
	
	@Override
	public void reset() throws EnvironmentException {
		internalReset();
	}
	
	@Override public char last() {return last;}
	@Override public int totalReaded() {return actual-from;}
	@Override public int atRow() {return actualRow;}
	@Override public int atColumn() {return actualCol;}
	
	@Override public String toString() {return "StringCharSource [last()=" + last() + ", atRow()=" + atRow() + ", atColumn()=" + atColumn() + "]";}

	private void internalReset() {
		this.actual = from;
		this.actualRow = 1;
		this.actualCol = from + 1;
		this.last = ' ';
	}

}
