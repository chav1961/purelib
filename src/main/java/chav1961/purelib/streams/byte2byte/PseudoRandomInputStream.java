package chav1961.purelib.streams.byte2byte;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class PseudoRandomInputStream extends InputStream {
	public static final long		UNKNOWN = -1;
	
	private final InputStream		nested;
	private final long				length;
	private final File				temp;
	private final RandomAccessFile	tempRaf;
	private final byte[]			buffer = new byte[1];
	private boolean					fullyRead = false;
	private long					currentLen = 0, currentPos = 0;
	
	public PseudoRandomInputStream(final InputStream nested, final long length) throws NullPointerException, IOException {
		if (nested == null) {
			throw new NullPointerException("Nested strean can't be null"); 
		}
		else {
			this.nested	= nested;
			this.length = length;
			this.temp = File.createTempFile("pris", ".tmp");
			this.tempRaf = new RandomAccessFile(temp, "rw");
		}
	}

	@Override
	public void close() throws IOException {
		this.tempRaf.close();
		this.temp.delete();
		super.close();
	}

	public long getFilePointer() throws IOException {
		return currentPos;
	}

	public long getFileLength() throws IOException {
		if (length >= 0) {
			return length;
		}
		else {
			if (!fullyRead) {
				fullyRead = true;
				return readRest();
			}
			else {
				return currentLen;
			}
		}
	}
	
	public void setFilePointer(final long fp) throws IOException {
		if (fp >= getFileLength()) {
			throw new IOException("File pointer ["+fp+"] out ofrange 0.."+(getFileLength()-1));
		}
		else {
			ensurePosInside(fp);
			tempRaf.seek(currentPos = fp);
		}
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		ensurePosInside(getFilePointer() + len);
		final int	rc = tempRaf.read(b, off, len);
		
		if (rc > 0) {
			currentPos += len;
		}
		return rc;
	}
	
	@Override
	public int read() throws IOException {
		final int	rc = read(buffer);
		
		return rc < 0 ? rc : buffer[0];
	}

	private long readRest() throws IOException {
		final byte[]	temp = new byte[8192];
		int				len;
		
		while ((len = nested.read(temp)) > 0) {
			write2ShadowFile(temp, 0, len);
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
			final byte[]	temp = new byte[8192];
			long			len;
			int				readed = 0;
			
			for (long displ = currentLen; displ < fp; displ += readed) {
				len = Math.min(fp-displ, temp.length);
						
				if ((readed = nested.read(temp,0,(int)len)) > 0) {
					write2ShadowFile(temp, 0, readed);
				}
			}
			
		}
	}
}
