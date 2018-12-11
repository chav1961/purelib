package chav1961.purelib.streams.byte2char;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.UnsupportedCharsetException;

/**
 * <p>This class is a fast, not thread-safe implementation of the standard {@linkplain InputStreamReader} class that can be used everywhere instead of it. It is not
 * a child of the {@linkplain InputStreamReader} but replacement for it</p>
 * 
 * @see Reader
 * @see InputStreamReader
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public class BufferedInputStreamReader extends Reader {
	public static final int			DEFAULT_BUFFER_SIZE = 65536;
	
	private final InputStream		is;
	private final CharsetDecoder	decoder;
	private final byte[]			toRead;
	private final char[]			toConvert;
	private CharBuffer				cb = null;
	private int						displ = 0;
	private boolean 				closed = false;
	
	public BufferedInputStreamReader(final InputStream is) throws NullPointerException, IllegalArgumentException, IOException {
		this(is,Charset.defaultCharset());
	}

	public BufferedInputStreamReader(final InputStream is, final String encoding) throws UnsupportedCharsetException, NullPointerException, IllegalArgumentException, IOException {
		this(is,DEFAULT_BUFFER_SIZE,Charset.forName(encoding));
	}

	public BufferedInputStreamReader(final InputStream is, final Charset charset) throws NullPointerException, IllegalArgumentException, IOException {
		this(is,DEFAULT_BUFFER_SIZE,charset);
	}
	
	public BufferedInputStreamReader(final InputStream is, final int bufferSize, final Charset charset) throws NullPointerException, IllegalArgumentException, IOException{
		if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else if (bufferSize < 1024 || bufferSize > Integer.MAX_VALUE/10) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] out of range 1024.."+(Integer.MAX_VALUE/10));
		}
		else if (charset == null) {
			throw new NullPointerException("Charset can't be null");
		}
		else {
			this.is = is;
			this.decoder = charset.newDecoder();
			this.toRead = new byte[bufferSize];
			this.toConvert = new char[bufferSize/2];
			fillCharBuffer();
		}
	}
	
	private boolean fillCharBuffer() throws IOException {
		final int	len = is.read(this.toRead,displ,this.toRead.length-displ);
		
		if (len <= 0) {
			if (displ > 0) {
				this.cb = decoder.decode(ByteBuffer.wrap(this.toRead,0,displ));
				displ = 0;
				return true;
			}
			else {
				this.cb = CharBuffer.wrap(new char[0]);
				return false;
			}
		}
		else {
			final ByteBuffer 	bb = ByteBuffer.wrap(this.toRead,0,len+displ);
			this.cb = CharBuffer.wrap(this.toConvert,0,this.toConvert.length);
			
			final CoderResult 	result = decoder.decode(bb,this.cb,false);
			
			if (result.isOverflow()) {
				if (bb.hasRemaining()) {
					System.arraycopy(this.toRead,bb.position(),this.toRead,0,displ = bb.remaining());
				}
				else {
					displ = 0;
				}
				this.cb.flip();
				return true;
			}
			else if (result.isUnderflow()) {
				displ = 0;
				return true;
			}
			else {
				result.throwException();
				return false;
			}
		}
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		if (closed) {
			throw new IOException("Stream already closed");
		}
		else {
			if (cb.remaining() == 0 && !fillCharBuffer()) {
				return -1;
			}
			final int	result = Math.min(len,cb.limit() - cb.position());
			
			System.arraycopy(this.toConvert,cb.position(),cbuf,off,result);
			cb.position(cb.position()+result);
			return result;
		}
	}

	@Override
	public void close() throws IOException {
		is.close();
		closed = true;
	}
}
