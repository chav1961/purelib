package chav1961.purelib.streams.byte2byte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class MappedOutputStream extends OutputStream {
	private static final int	DEFAULT_SEGMENT_SIZE = 1 << 20;
	
	private final RandomAccessFile	raf;
	private final int				segmentSize;
	private long					currentLength = 0;
	private long					cursor = 0;
	private MappedByteBuffer 		map = null;
	private boolean					closed = false;

	public MappedOutputStream(final File file) throws FileNotFoundException, IOException, NullPointerException, IllegalArgumentException {
		this(file, DEFAULT_SEGMENT_SIZE);
	}	
	
	public MappedOutputStream(final File file, final int segmentSize) throws FileNotFoundException, IOException, NullPointerException, IllegalArgumentException {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		else if (segmentSize <= 0) {
			throw new IllegalArgumentException("Segment size must be greater than 0");
		}
		else {
			this.segmentSize = segmentSize;
			this.raf = new RandomAccessFile(file, "rw");
			expand();
		}
	}

	@Override
	public void write(final int b) throws IOException {
		if (map.remaining() < 1) {
			map.force();
			expand();
			write(b);
		}
		else {
			map.put((byte)b);
			currentLength++;
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
			if (map.remaining() >= len) {
				map.put(b, off, len);
				currentLength += len;
			}
			else {
				final int	remaining = map.remaining();
				
				map.put(b, off, remaining);
				currentLength += remaining;
				map.force();
				expand();
				write(b, off+remaining, len - remaining);
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			if (map != null) {
				map.force();
				map = null;
			}
			raf.setLength(currentLength);
			raf.close();
		}
	}
	
	private void expand() throws IOException {
		this.raf.seek(cursor += segmentSize);
		this.map = raf.getChannel().map(MapMode.READ_WRITE, cursor - segmentSize, segmentSize);
	}
}
