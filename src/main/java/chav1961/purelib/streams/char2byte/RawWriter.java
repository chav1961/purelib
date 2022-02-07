package chav1961.purelib.streams.char2byte;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class RawWriter extends Writer {
	public static final int		DEFAULT_BUFFER_SIZE = 8192;
	
	private final OutputStream	os;
	private final byte[]		buffer;
	private int					filled = 0;

	public RawWriter(final OutputStream os) {
		this(os, DEFAULT_BUFFER_SIZE);
	}	
	
	public RawWriter(final OutputStream os, final int bufferSize) {
		if (os == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] must be greater than 0"); 
		}
		else {
			this.os = os;
			this.buffer = new byte[bufferSize];
		} 
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (cbuf == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else if (off < 0 || off > cbuf.length - 1) {
			throw new IllegalArgumentException("Buffer offset ["+off+"] out of range 0.."+(cbuf.length - 1));
		}
		else if ((off+len) < 0 || (off+len) > cbuf.length) {
			throw new IllegalArgumentException("Buffer offset + length ["+(off+len)+"] out of range 0.."+cbuf.length);
		}
		else {
			int	tail = len, from = off, currentPiece;
			
			while (tail > (currentPiece = (buffer.length-filled)/2)) {
				convert(cbuf, from, buffer, filled, currentPiece);
				filled += 2 * currentPiece;
				sync();
				from += currentPiece;
				tail -= currentPiece;
			}
			convert(cbuf, from, buffer, filled, tail);
			filled += 2 * tail;
		}
	}

	private void convert(final char[] cbuf, final int from, final byte[] buffer, final int to, final int len) {
		for(int index = 0, maxIndex = len; index < maxIndex; index++) {
			buffer[to+2*index] = (byte) ((cbuf[index+from] >> 8) & 0xFF);
			buffer[to+2*index+1] = (byte) ((cbuf[index+from] >> 0) & 0xFF);
		}
	}

	@Override
	public void flush() throws IOException {
		sync();
		os.flush();
	}

	@Override
	public void close() throws IOException {
		flush();
	}

	private void sync() throws IOException {
		os.write(buffer, 0, filled);
		filled = 0;
	}
}
