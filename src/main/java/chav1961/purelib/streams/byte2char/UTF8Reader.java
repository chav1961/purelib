package chav1961.purelib.streams.byte2char;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class UTF8Reader extends Reader {
	private static final int	DEFAULT_BUFFER_SIZE = 65536;
	
	private final FileChannel	ch;
	private final ByteBuffer	bb;
	private final byte[]		content;
	private final char[]		tempBuffer = new char[1];
	private int		length = 0;
	private int		cursor = 0;
	private long	position = 0;

	public UTF8Reader(final File f) throws IOException {
		this(f, DEFAULT_BUFFER_SIZE);
	}	
	
	public UTF8Reader(final File f, final int bufferSize) throws IOException {
		if (f == null) {
			throw new NullPointerException("File to read can't be null");
		}
		else if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] can't be less than or equals 0");
		}
		else {
			this.ch = FileChannel.open(f.toPath(), StandardOpenOption.READ);
			this.bb = ByteBuffer.allocate(bufferSize);
			this.content = bb.array();
		}
	}

	@Override
	public int read() throws IOException {
		return readInternal(tempBuffer, 0, 1) > 0 ? tempBuffer[0] : -1;
	}
	
	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		if (cbuf == null) {
			throw new NullPointerException("Buffer to read content to can't be null");
		}
		else if (off < 0 || off >= cbuf.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length-1));
		}
		else if (len <= 0 || off + len > cbuf.length) {
			throw new IllegalArgumentException("Length ["+len+"] is not greater than 0 or offset+len ["+(off+len)+"] out of range 1.."+cbuf.length);
		}
		else {
			return readInternal(cbuf, off, len);
		}
	}

	@Override
	public void close() throws IOException {
		ch.close();
	}

	private int readInternal(final char[] cbuf, final int off, final int len) throws IOException {
		final byte[]	temp = content;
		int				pos = cursor;
		int				maxPos = length; 
		long			from = position;
		int				c, char2, char3, where = off;
		
		for(int index = 0; index < len; index++, where++) {
			if (pos >= maxPos) {
				if (!load()) {
					if (length < 0) {
						if (where > off) {
							cursor = pos;
							return where - off;
						}
						else {
							return -1;
						}
					}
				}
				else {
					pos = cursor;
					maxPos = length;
				}
			}
            c = (int) temp[pos++] & 0xff;
            
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    cbuf[where]=(char)c;
                    from++;
                    break;
                case 12: case 13:
    				if (pos >= maxPos) {
    					if (!load()) {
	                		throw new UTFDataFormatException("Malformed input: partial character at end");
    					}
    					else {
    						pos = cursor;
    						maxPos = length;
    					}
    				}
                    char2 = (int) temp[pos++];
                    
                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException("Malformed input around byte " + from);
                    }
                    else {
	                    cbuf[where] = (char)(((c & 0x1F) << 6) | (char2 & 0x3F));
	                    from += 2;
                    }
                    break;
                case 14:
    				if (pos >= maxPos) {
    					if (!load()) {
	                		throw new UTFDataFormatException("Malformed input: partial character at end");
    					}
    					else {
    						pos = cursor;
    						maxPos = length;
    					}
    				}
                    char2 = (int) temp[pos++];
    				if (pos >= maxPos) {
    					if (!load()) {
	                		throw new UTFDataFormatException("Malformed input: partial character at end");
    					}
    					else {
    						pos = cursor;
    						maxPos = length;
    					}
    				}
                    char3 = (int) temp[pos++];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException("malformed input around byte " + from);
                    }
                    else {
	                    cbuf[where]=(char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
	                    from += 3;
                    }
                    break;
                default:
                    throw new UTFDataFormatException("malformed input around byte " + from);
            }
		}
		position = from;
		cursor = pos;
		return where-off;
	}	
	
	private boolean load() throws IOException {
		bb.clear();
		length = ch.read(bb);
		cursor = 0;
		return length > 0;
	}

	
}
