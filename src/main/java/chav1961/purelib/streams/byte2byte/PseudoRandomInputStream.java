package chav1961.purelib.streams.byte2byte;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * <p>This class is a wrapper for {@linkplain InputStream} class to support random access to it's content. It produces shadow {@linkplain RandomAccessFile} instance during 
 * read of the input stream and supports cursor movement on it.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
public class PseudoRandomInputStream extends InputStream {
	public static final long				UNKNOWN = -1;
	
	private final PseudoRandomInputStream	parent;
	private final InputStream				nested;
	private final long						offset;
	private final long						length;
	private final File						temp;
	private final RandomAccessFile			tempRaf;
	private final byte[]					buffer = new byte[1];
	private final byte[]					longBuffer = new byte[8192];
	private boolean							fullyRead = false;
	private long							currentLen = 0, currentPos = 0;

	/**
	 * <p>Constructor of the class. Don't use it with "infinite" input streams!</p> 
	 * @param nested input stream to wrap. Can't be null
	 * @throws NullPointerException on null parameter
	 * @throws IOException on any I/O error
	 */
	public PseudoRandomInputStream(final InputStream nested) throws NullPointerException, IOException {
		this(nested, UNKNOWN);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param nested input stream to wrap. Can't be null
	 * @param length input stream length awaited or -1 {@linkplain #UNKNOWN}. Don't use -1 with "infinite" streams!  
	 * @throws NullPointerException on null parameter
	 * @throws IOException on any I/O error
	 */
	public PseudoRandomInputStream(final InputStream nested, final long length) throws NullPointerException, IOException {
		if (nested == null) {
			throw new NullPointerException("Nested stream can't be null"); 
		}
		else {
			this.parent	= null;
			this.nested	= nested;
			this.offset = 0;
			this.length = length;
			this.temp = File.createTempFile("pris", ".tmp");
			this.tempRaf = new RandomAccessFile(temp, "rw");
		}
	}

	/**
	 * <p>Constructor of the class. Slices existent input stream.</p>  
	 * @param parent parent stream to slice. Can't be null
	 * @param offset offset inside parent stream 
	 * @param length size of the slice
	 * @throws NullPointerException on null parameter
	 * @throws IOException on any I/O error
	 */
	public PseudoRandomInputStream(final PseudoRandomInputStream parent, final long offset, final long length) throws NullPointerException, IOException {
		if (parent == null) {
			throw new NullPointerException("Parent stream can't be null"); 
		}
		else {
			this.parent = parent;
			this.nested	= null;
			this.offset = offset;
			this.length = length;
			this.temp = null;
			this.tempRaf = null;
			seek(0);
		}
	}
	
	@Override
	public void close() throws IOException {
		if (parent == null) {
			this.tempRaf.close();
			this.temp.delete();
		}
		super.close();
	}

	/**
	 * <p>Get zero-based "file" pointer
	 * @return file pointer. 
	 * @throws IOException on any I/O error
	 */
	public long getFilePointer() throws IOException {
		return currentPos;
	}

	/**
	 * <p>Get "file" length.
	 * @return file length
	 * @throws IOException on any I/O error
	 */
	public long length() throws IOException {
		if (length >= 0) {
			return length;
		}
		else if (parent == null) {
			if (!fullyRead) {
				fullyRead = true;
				return readRest();
			}
			else {
				return currentLen;
			}
		}
		else {
			return parent.length();
		}
	}

	/**
	 * <p>Seek file pointer inside the content.</p>
	 * @param fp file pointer position
	 * @throws IOException on any I/O error
	 */
	public void seek(final long fp) throws IOException {
		if (fp > length() && parent == null) {
			throw new IOException("File pointer ["+fp+"] out ofrange 0.."+(length()-1));
		}
		else if (parent == null) {
			ensurePosInside(fp);
			tempRaf.seek(currentPos = fp);
		}
		else {
			final long	prev = parent.getFilePointer();
			
			parent.seek(offset + fp);
			parent.seek(prev);
			currentPos = fp;
		}
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (parent == null) {
			ensurePosInside(getFilePointer() + len);
			final int	rc = tempRaf.read(b, off, len);
			
			if (rc > 0) {
				currentPos += len;
			}
			return rc;
		}
		else {
			final long	prev = parent.getFilePointer();
			
			parent.seek(offset+currentPos);
			final int	rc = parent.read(b, off, len);
			
			if (rc > 0) {
				currentPos += len;
			}
			parent.seek(prev);
			return rc;
		}
	}
	
	@Override
	public int read() throws IOException {
		final int	rc = read(buffer);
			
		return rc < 0 ? rc : buffer[0] & 0xFF;
	}

	private long readRest() throws IOException {
		int	len;
		
		while ((len = nested.read(longBuffer)) > 0) {
			write2ShadowFile(longBuffer, 0, len);
		}
		return currentLen;
	}

	private void write2ShadowFile(byte[] b, int off, int len) throws IOException {
		ensurePosInside(currentLen);
		tempRaf.seek(currentLen);
		tempRaf.write(b, off, len);
		currentLen += len;
		tempRaf.seek(currentPos);
	}

	private void ensurePosInside(final long fp) throws IOException {
		if (fp > currentLen) {
			long	len;
			int		readed = 0;
			
			for (long displ = currentLen; displ < fp; displ += readed) {
				len = Math.min(fp-displ, longBuffer.length);
						
				if ((readed = nested.read(longBuffer,0,(int)len)) > 0) {
					write2ShadowFile(longBuffer, 0, readed);
				}
			}
			
		}
	}
}
