package chav1961.purelib.streams.charsource;

import java.io.IOException;
import java.io.Reader;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.interfaces.CharacterSource;

/**
 * <p>This class implements {@link CharacterSource} interface by any reader</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 *
 */
public class ReaderCharSource implements CharacterSource {
	private final Reader	source;
	private final boolean 	charByChar;
	private int				actual = 0, actualRow = 1, actualCol = 1;
	private int				displ = 0, len = -1;
	private char[]			buffer = new char[8192];
	private char[]			last = new char[]{' '};

	/**
	 * <p>Constructor of the class</p>
	 * @param source source reader
	 * @param charByChar read exactly one character from the stream (prevent to use buffers) 
	 */
	public ReaderCharSource(final Reader source, final boolean charByChar) {
		if (source == null) {
			throw new IllegalArgumentException("Source stream can't be null");
		}
		else {
			this.source = source;
			this.charByChar = charByChar;
		}
	}
	
	@Override 
	public char next() throws ContentException {
		if (charByChar) {
			try{if (source.read(last) == 1) {
					if (last[0] == '\n') {
						actualRow++;
						actualCol = 1;
					}
					else {
						actualCol++;
					}
					actual++;	return last[0];
				}
				else {
					return last[0] = CharacterSource.EOF;
				}
			} catch (IOException e) {
				return CharacterSource.EOF;
			}
		}
		else {
			if (displ >= len) {
				try{len = source.read(buffer);
					displ = 0;
				} catch (IOException e) {
					return CharacterSource.EOF;
				}
			}
			if (len > 0) {
				if (buffer[displ] == '\n') {
					actualRow++;
					actualCol = 1;
				}
				else {
					actualCol++;
				}
				
				actual++;
				return last[0] = buffer[displ++];
			}
			else {
				return last[0] = CharacterSource.EOF;
			}
		}
	}
	
	@Override public char last() {return last[0];}
	@Override public int totalReaded() {return actual;}
	@Override public int atRow() {return actualRow;}
	@Override public int atColumn() {return actualCol;}
	
	@Override public String toString() {return "ReaderCharSource [last()=" + last() + ", atRow()=" + atRow() + ", atColumn()=" + atColumn() + "]";}
}
