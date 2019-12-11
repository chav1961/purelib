package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

/**
 * <p>This class is a non-thread-safe implementation of the ZLIB compression output stream.</p>
 * 
 * @see Deflater
 * @see OutputStream
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */
public class ZLibOutputStream extends OutputStream {
	private static final int		MINIMUM_INNER_BUFFER = 4096;
	private static final int		DEFAULT_INNER_BUFFER = 8192;
	
	protected final OutputStream	nested;
	private final Deflater			deflater = new Deflater(Deflater.BEST_COMPRESSION,true);
	private final byte[]			buffer;
	private int						received = 0;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param nested stream to save bytes to
	 * @throws NullPointerException if parameter is null
	 */
	public ZLibOutputStream(final OutputStream nested) throws NullPointerException {
		this(nested,DEFAULT_INNER_BUFFER);
	}	
	
	/**
	 * <p>Constructor of the class</p>
	 * @param nested stream to save bytes to
	 * @param bufferSize buffer size for output stream
	 * @throws NullPointerException if output stream is null
	 * @throws IllegalArgumentException if buffer size is too small
	 */
	public ZLibOutputStream(final OutputStream nested, final int bufferSize) throws NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested output stream can't be null");
		}
		else if (bufferSize < MINIMUM_INNER_BUFFER) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] is too small. Must be at least "+MINIMUM_INNER_BUFFER);
		}
		else {
			this.nested = nested;
			this.buffer = new byte[bufferSize];
		}
	}

	@Override
	public void write(final int b) throws IOException {
		write(new byte[]{(byte)b});
	}
	
	@Override
    public void write(final byte b[]) throws IOException {
		if (b == null) {
			throw new NullPointerException("Byte buffer to write can't be null");
		}
		else {
			write(b,0,b.length);
		}
    }

	@Override
    public void write(final byte b[], final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Byte buffer to write can't be null");
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(b.length-1));
		}
		else if (off + len < 0 || off + len > b.length) {
			throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range 0.."+(b.length-1));
		}
		else {
			deflater.setInput(b,off,len);
			if ((received += len) > 10 * buffer.length) {
				deflate();
			}
		}
		
    }

	@Override
    public void flush() throws IOException {
		deflate();
		nested.flush();
    }

	@Override
    public void close() throws IOException {
		deflater.finish();
		flush();
    }
	
	private void deflate() throws IOException {
		int	read, displ = 0;
		
		while ((read = deflater.deflate(buffer,displ,buffer.length-displ)) > 0) {
			if ((displ += read) >= buffer.length) {
				nested.write(buffer);
				displ = 0;
			}
		}
		if (displ > 0) {
			nested.write(buffer,0,displ);
		}
		received = 0;
	}
}
