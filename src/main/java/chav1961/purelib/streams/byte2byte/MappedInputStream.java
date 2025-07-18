package chav1961.purelib.streams.byte2byte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

/**
 * <p>This class implement input stream by memory-mapped technique. It's performance is greater than usual input stream implementation.
 * stream has restriction for file size (must be less than 2G)</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class MappedInputStream extends InputStream {
	private final RandomAccessFile	raf;
	private final MappedByteBuffer 	map;
	
	/**
	 * <p>Constructor of the class instance</p>
	 * @param file file to open. Can't be null.
	 * @throws FileNotFoundException file not found
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException file argument is null
	 * @throws IllegalArgumentException file size is greater than 2G
	 */
	public MappedInputStream(final File file) throws FileNotFoundException, IOException, NullPointerException, IllegalArgumentException {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		else if (file.length() >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException("File size ["+file.getAbsolutePath()+"] exceeds 2G");
		}
		else {
			this.raf = new RandomAccessFile(file, "r");
			this.map = raf.getChannel().map(MapMode.READ_ONLY, 0, file.length());
		}
	}
	
	@Override
	public int read() throws IOException {
		if (map.hasRemaining()) {
			return map.get() & 0xFF;
		}
		else {
			return -1;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to put data can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Buffer offset ["+off+"] out of range 0.."+(b.length-1)); 
		}
		else if (len <= 0 || off + len > b.length) {
			throw new IllegalArgumentException("Buffer length ["+len+"] is not greater than 0 or offset+length out of range 1.."+b.length);
		}
		else if (map.hasRemaining()) {
			final int	size = Math.min(map.limit()-map.position(), len);
			
			map.get(b, off, size);
			return size;
		}
		else {
			return -1;
		}
	}
	
	@Override
	public void close() throws IOException {
		raf.close();
		super.close();
	}
}
