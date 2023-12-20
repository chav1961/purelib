package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Reader;

import chav1961.purelib.basic.Utils;

/**
 * <p>This class is used to concatenate content of the readers. On any read operations, it reads content from the same first reader. If the same first
 * reader is exhausted, it automatically switches to next reader until all the readers will be iterated.</p>  
 *
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class SequenceReader extends Reader {
	private final Reader[]	content;
	private boolean			closed = false;
	private int				index = 0;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param source list of readers to sequentially read. Can't be null, but can be empty. 
	 * @throws IllegalArgumentException parameter is null or contains nulls inside
	 */
	public SequenceReader(final Reader... source) throws IllegalArgumentException {
		if (source == null || Utils.checkArrayContent4Nulls(null) >= 0) {
			throw new IllegalArgumentException("Source reader's list is null or contains nulls inside");
		}
		else {
			this.content = source.clone();
		}
	}
	
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Attempt to read data after calling close()");
		}
		else if (index >= content.length) {
			return -1;
		}
		else {
			final int	result = content[index].read(cbuf, off, len);
			
			if (result == -1) {
				content[index].close();
				content[index++] = null;
				return read(cbuf, off, len);
			}
			else {
				return result;
			}
		}
	}

	@Override
	public void close() throws IOException {
		for(Reader item : content) {
			if (item != null) {
				item.close();
			}
		}
		closed = true;
	}
}
