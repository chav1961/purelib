package chav1961.purelib.streams.byte2char;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class Byte2HexOutputStream extends OutputStream {
	private static final char[]	DIGITS = "0123456789ABCDEF".toCharArray();
	
	private final Writer		wr;
	private final char[]		buffer = new char[2];
	
	public Byte2HexOutputStream(final Writer wr) {
		if (wr == null) {
			throw new NullPointerException("Writer can't be null"); 
		}
		else {
			this.wr = wr;
		}
	}
	
	
	@Override
	public void write(int b) throws IOException {
		buffer[0] = DIGITS[(b & 0xF0) >> 4];
		buffer[1] = DIGITS[(b & 0x0F) >> 0];
		wr.write(buffer);
	}

	@Override
	public void close() throws IOException {
		wr.close();
		super.close();
	}
}
