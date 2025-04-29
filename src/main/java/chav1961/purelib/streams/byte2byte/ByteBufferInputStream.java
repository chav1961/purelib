package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * <p>This class implement input stream on {@linkplain ByteBuffer}. Changes in passed byte buffer outside the class
 * will be immediately reflected on this input stream content.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class ByteBufferInputStream extends InputStream {
	private final ByteBuffer	buffer;

	/**
	 * <p>Constructor of the class instance</p>
	 * @param buffer buffer to read data from. Can't be null
	 * @throws NullPointerException buffer is null
	 */
	public ByteBufferInputStream(final ByteBuffer buffer) throws NullPointerException {
		if (buffer == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else {
			this.buffer = buffer;
		}
	}

	@Override
	public int read() throws IOException {
		return getBuffer().remaining() <= 0 ? -1 : getBuffer().get();
	}
	
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(b.length-1));
		}
		else if (off + len < 1 || off + len  > b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] + length ["+len+"] out of range 1.."+b.length);
		}
		else {
			final int	remaining = getBuffer().remaining(); 
			
			if (remaining <= 0) {
				return -1;
			}
			else if (len >= remaining) {
				getBuffer().get(b, off, remaining);
				return remaining;
			}
			else {
				getBuffer().get(b, off, len);
				return len;
			}
		}
	}
	
	/**
	 * <p>Get buffer passed in the constructor</p>
	 * @return buffer passed. Can't be null.
	 */
	public ByteBuffer getBuffer() {
		return buffer;
	}
}
