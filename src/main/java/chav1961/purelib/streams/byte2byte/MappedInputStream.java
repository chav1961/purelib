package chav1961.purelib.streams.byte2byte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class MappedInputStream extends InputStream {
	private final RandomAccessFile	raf;
	private final MappedByteBuffer 	map;
	
	public MappedInputStream(final File file) throws FileNotFoundException, IOException, NullPointerException {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		else {
			this.raf = new RandomAccessFile(file, "r");
			this.map = raf.getChannel().map(MapMode.READ_ONLY, 0, file.length());
		}
	}
	
	@Override
	public int read() throws IOException {
		if (map.hasRemaining()) {
			return map.get();
		}
		else {
			return -1;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (map.hasRemaining()) {
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
