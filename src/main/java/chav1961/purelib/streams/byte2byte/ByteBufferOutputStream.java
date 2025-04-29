package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * <p>This class implement output stream on {@linkplain ByteBuffer}. Changes in passed byte buffer outside the class
 * will be immediately reflected on this output stream behavior.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class ByteBufferOutputStream extends OutputStream {
	private final ByteBuffer	buffer;

	/**
	 * <p>Constructor of the class instance</p>
	 * @param buffer buffer to write data to. Can't be null
	 * @throws NullPointerException buffer is null
	 */
	public ByteBufferOutputStream(final ByteBuffer buffer) throws NullPointerException {
		if (buffer == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else {
			this.buffer = buffer.rewind();
		}
	}
	
	@Override
	public void write(final int b) throws IOException {
		if (getBuffer().remaining() > 0) {
			getBuffer().put((byte)b);
		}
		else {
			throw new IOException("No space in the byte buffer");
		}
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(b.length-1));
		}
		else if (off + len < 1 || off + len  > b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] + length ["+len+"] out of range 1.."+b.length);
		}
		else if (getBuffer().remaining() > len) {
			getBuffer().put(b, off, len);
		}
		else {
			throw new IOException("No space in the byte buffer");
		}
	}
	
	@Override
	public void close() throws IOException {
		getBuffer().flip();
		super.close();
	}
	
	/**
	 * <p>Get buffer passed in the constructor</p>
	 * @return buffer passed. Can't be null.
	 */
	public ByteBuffer getBuffer() {
		return buffer;
	}
}
