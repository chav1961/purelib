package chav1961.purelib.streams.chartarget;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class implements {@link CharacterTarget} interface by character streams.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.6
 */
public class WriterCharTarget implements CharacterTarget {
	private final Writer		targetWr;
	private final PrintStream	targetPs;
	private final boolean		charByChar;
	private int					written = 0;

	/**
	 * <p>Constructor of the object</p>
	 * @param target writer to store data to
	 * @param charByChar write data char-by-char (prevent to use buffers)
	 */
	public WriterCharTarget(final Writer target, final boolean charByChar) {
		if (target == null) {
			throw new NullPointerException("Target can't be null"); 
		}
		else {
			this.targetWr = target;
			this.targetPs = null;
			this.charByChar = charByChar;
		}
	}

	/**
	 * <p>Constructor of the object</p>
	 * @param target print stream to store data to
	 * @param charByChar write data char-by-char (prevent to use buffers)
	 */
	public WriterCharTarget(final PrintStream target, final boolean charByChar) {
		if (target == null) {
			throw new NullPointerException("Target can't be null"); 
		}
		else {
			this.targetPs = target;
			this.targetWr = null;
			this.charByChar = charByChar;
		}
	}
	
	@Override
	public CharacterTarget put(final char symbol) throws PrintingException {
		if (charByChar) {
			return put(new char[]{symbol});
		}
		else {
			return put(new char[]{symbol});
		}
	}

	@Override
	public CharacterTarget put(final char[] symbols) throws PrintingException {
		if (symbols == null) {
			throw new NullPointerException("Symbols to write can't be null"); 
		}
		else if (symbols.length > 0) {
			return put(symbols,0,symbols.length);
		}
		else {
			return this;
		}
	}

	@Override
	public CharacterTarget put(final char[] symbols, final int from, final int to) throws PrintingException {
		if (symbols == null) {
			throw new NullPointerException("Symbols can't be null"); 
		}
		else if (charByChar) {
			try{if (targetWr != null) {
					targetWr.write(symbols,from,to);
				}
				else {
					targetPs.print(new String(symbols,from,to));
				}
				written += to - from;
				return this;
			} catch (IOException e) {
				throw new PrintingException(e.getMessage());
			}
		}
		else {
			try{if (targetWr != null) {
					targetWr.write(symbols,from,to);
				}
				else {
					targetPs.print(new String(symbols,from,to));
				}
				written += to - from;
				return this;
			} catch (IOException e) {
				throw new PrintingException(e.getMessage());
			}
		}
	}

	@Override
	public CharacterTarget put(final String source) throws PrintingException {
		if (source == null) {
			throw new NullPointerException("Source can't be null"); 
		}
		else if (charByChar) {
			try{if (targetWr != null) {
					targetWr.write(source);
				}
				else {
					targetPs.print(source);
				}
				written += source.length();
				return this;
			} catch (IOException e) {
				throw new PrintingException(e.getMessage());
			}
		}
		else {
			try{if (targetWr != null) {
					targetWr.write(source);
				}
				else {
					targetPs.print(source);
				}
				written += source.length();
				return this;
			} catch (IOException e) {
				throw new PrintingException(e.getMessage());
			}
		}
	}

	@Override
	public CharacterTarget put(String source, int from, int to) throws PrintingException {
		if (source == null) {
			throw new NullPointerException("Source can't be null"); 
		}
		else if (charByChar) {
			try{if (targetWr != null) {
					targetWr.write(source.substring(from, to));
				}
				else {
					targetPs.print(source.substring(from, to));
				}
				written += to - from;
				return this;
			} catch (IOException e) {
				throw new PrintingException(e.getMessage());
			}
		}
		else {
			try{if (targetWr != null) {
					targetWr.write(source.substring(from, to));
				}
				else {
					targetPs.print(source.substring(from, to));
				}
				written += to - from;
				return this;
			} catch (IOException e) {
				throw new PrintingException(e.getMessage());
			}
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
		throw new EnvironmentException("This class not supports this method");
	}
	
	@Override public int totalWritten() {return written;}
	@Override public int atRow() {return 0;}
	@Override public int atColumn() {return 0;}
}
