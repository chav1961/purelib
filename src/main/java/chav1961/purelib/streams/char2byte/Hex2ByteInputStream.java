package chav1961.purelib.streams.char2byte;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class Hex2ByteInputStream extends InputStream {
	private final Reader	rdr;
	private final char[]	buffer = new char[2];
	
	public Hex2ByteInputStream(final Reader rdr) {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null"); 
		}
		else {
			this.rdr = rdr;
		}
	}
	
	@Override
	public int read() throws IOException {
		int	len, displ = 0;
		
		while ((len = rdr.read(buffer,displ,buffer.length-displ)) >= 0) {
			displ += len;
		}
		if (len <= 0) {
			if (displ >= 2) {
				return (toHex(buffer[0]) << 4) | (toHex(buffer[1]) << 0);
			}
			else {
				return -1;
			}
		}
		else {
			return (toHex(buffer[0]) << 4) | (toHex(buffer[1]) << 0);
		}
	}
	
	@Override
	public void close() throws IOException {
		rdr.close();
		super.close();
	}

	private int toHex(final char symbol) throws IOException {
		if (symbol >= '0' && symbol <= '9') {
			return symbol - '0';
		}
		else if (symbol >= 'a' && symbol <= 'f') {
			return symbol - 'a' + 10;
		}
		else if (symbol >= 'A' && symbol <= 'F') {
			return symbol - 'A' + 10;
		}
		else {
			throw new IOException("Illegalr character ["+symbol+"] in the input stream");
		}
	}

}
