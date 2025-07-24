package chav1961.purelib.streams.byte2byte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class NIOOutputStream extends OutputStream {
	private static final int	DEFAULT_SEGMENT_SIZE = 1 << 16;

	private final FileChannel	raf;
	private final byte[]		content;
	private final int			segmentSize;
	private int					where = 0;
	private boolean				closed = false;

	public NIOOutputStream(final File file) throws FileNotFoundException, IOException, NullPointerException, IllegalArgumentException {
		this(file, DEFAULT_SEGMENT_SIZE);
	}	
	
	public NIOOutputStream(final File file, final int segmentSize) throws FileNotFoundException, IOException, NullPointerException, IllegalArgumentException {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		else if (segmentSize <= 0) {
			throw new IllegalArgumentException("Segment size must be greater than 0");
		}
		else {
			this.segmentSize = segmentSize;
			this.raf = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			this.content = new byte[segmentSize];
		}
	}

	@Override
	public void write(final int b) throws IOException {
		if (where + 1 >= segmentSize) {
			force();
			write(b);
		}
		else {
			content[where++] = (byte)b;
		}
	}
	
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to write can't be null");
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Buffer offset ["+off+"] out of range 0.."+(b.length-1));
		}
		else if (len <= 0 || off + len > b.length) {
			throw new IllegalArgumentException("Buffer length ["+len+"] is not greater than 0 or offset+length out of range 1.."+b.length);
		}
		else {
			if (where + len <= segmentSize) {
				System.arraycopy(b, off, content, where, len);
				where += len;
			}
			else {
				final int	tail = segmentSize - where, newOff = off + tail, newLen = len - tail;
				
				if (tail > 0) {
					System.arraycopy(b, off, content, where, tail);
					where += tail;
				}
				force();
				write(b, newOff, newLen);
			}
		}
	}

	@Override
	public void flush() throws IOException {
		force();
	}
	
	@Override
	public void close() throws IOException {
		if (!closed) {
			force();
			closed = true;
			raf.close();
		}
	}
	
	private void force() throws IOException {
		raf.write(ByteBuffer.wrap(content, 0, where));
		where = 0;
	}
}
