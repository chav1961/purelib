package chav1961.purelib.streams.byte2char;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import chav1961.purelib.streams.char2byte.RawWriter;

/**
 * <p>This class implements simple converter from type <b>byte</b> to type <b>char</p> without using any encoders/decoders. 
 * It simply build one char item from two sequential bytes. This class must be used with {@linkplain RawWriter} class.</p>
 * <p>This class is not a thread-safe.</p>
 * @see RawWriter
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class RawReader extends Reader {
	/**
	 * <p>Default buffer size for reader</p>
	 */
	public static final int		DEFAULT_BUFFER_SIZE = 8192;
	
	private final InputStream	is;
	private final byte[]		buffer;
	private int					from = 0, to = 0;

	/**
	 * <p>Constructor of the class</p>
	 * @param is input stream to read bytes from. Can't be null
	 * @throws NullPointerException when input stream is null
	 */
	public RawReader(final InputStream is) throws NullPointerException {
		this(is, DEFAULT_BUFFER_SIZE);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param is input stream to read bytes from. Can't be null
	 * @param bufferSize internal buffer size, in bytes. Must b positive
	 * @throws NullPointerException when input stream is null
	 * @throws IllegalArgumentException when buffer size is not positive
	 */
	public RawReader(final InputStream is, final int bufferSize) throws NullPointerException, IllegalArgumentException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null"); 
		}
		else if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] must be greater than 0"); 
		}
		else {
			this.is = is;
			this.buffer = new byte[bufferSize];
		}
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		if (cbuf == null) {
			throw new NullPointerException("Buffer can't be null");
		}
		else if (off < 0 || off >= cbuf.length) {
			throw new IllegalArgumentException("Buffer offset ["+off+"] out of range 0.."+(cbuf.length-1));
		}
		else if ((off+len) < 0 || (off+len) > cbuf.length) {
			throw new IllegalArgumentException("Buffer offset + length ["+(off+len)+"] out of range 0.."+cbuf.length);
		}
		else if (to < 0) {
			return -1;
		}
		else {
			int		where = off, tail = len, currentPiece, total = 0;
			
			if (to == 0 && from == 0 && !preload()) {
				return -1;
			}
			
			while (tail > (currentPiece = (to - from) / 2)) {
				convert(buffer, from, cbuf, where, currentPiece);
				if (!preload()) {
					return total == 0 ? -1 : total;
				}
				else {
					where += currentPiece;
					tail -= currentPiece;
					total += currentPiece;
				}
			}
			convert(buffer, from, cbuf, where, tail);
			from += 2 * tail;
			total += tail;
			return total;
		}
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	private void convert(final byte[] buffer, final int from, final char[] cbuf, final int to, final int len) {
		for (int index = 0, maxIndex = len; index < maxIndex; index++) {
			cbuf[to+index] = (char) (((buffer[from+2*index] << 8) & 0xFF00) | ((buffer[from+2*index+1] << 0) & 0x00FF)); 
		}
	}
	
	private boolean preload() throws IOException {
		to = is.read(buffer);
		
		if (to == 0 || to < 0) {
			to = -1;
			return false;
		}
		else {
			from = 0;
			return true;
		}
	}
}
