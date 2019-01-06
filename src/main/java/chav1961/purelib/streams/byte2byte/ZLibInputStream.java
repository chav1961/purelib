package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * <p>This class is a non-thread-safe implementation of the ZLIB compression input stream.</p>
 * 
 * @see Inflater
 * @see InputStream
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class ZLibInputStream extends InputStream {
	private static final int		DEFAULT_INNER_BUFFER = 8192;
	
	protected final InputStream		nested;
	private final Inflater			inflater = new Inflater(true);
	private final byte[]			buffer = new byte[DEFAULT_INNER_BUFFER];
	
	/**
	 * <p>Constructor of the class</p>
	 * @param nested stream to load bytes from
	 * @throws NullPointerException if parameter is null
	 */
	public ZLibInputStream(final InputStream nested) throws NullPointerException {
		if (nested == null) {
			throw new NullPointerException("Nested output stream can't be null");
		}
		else {
			this.nested = nested;
		}
	}

	@Override
	public int read() throws IOException {
		final byte[]	buffer = new byte[1];
		final int 		rc = read(buffer);
		
		return rc != 1 ? rc : (buffer[0] & 0xFF);
	}

	@Override
    public int read(final byte b[]) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to read to can't be null"); 
		}
		else {
	        return read(b, 0, b.length);
		}
    }

	@Override
    public int read(final byte b[], final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to read to can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(b.length-1));
		}
		else if (off + len < 0 || off + len > b.length) {
			throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range 0.."+(b.length-1));
		}
		else {
			try {final int 	readed = inflater.inflate(b,off,len);
			
				if (readed == 0) {
					if (preload()) {
						return read(b,off,len);
					}
					else {
						return -1;
					}
				}
				else {
					return readed;
				}
			} catch (DataFormatException e) {
				throw new IOException("Error inflating Zlib content: illegal Zlib data format ("+e.getMessage()+")");
			}
		}
    }

	private boolean preload() throws IOException {
		final int	readed = nested.read(buffer);
		
		if (readed <= 0) {
			return false;
		}
		else {
			inflater.setInput(buffer, 0, readed);
			return true;
		}
	}
}
