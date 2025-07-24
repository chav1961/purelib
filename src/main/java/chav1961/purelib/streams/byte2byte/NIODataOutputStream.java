package chav1961.purelib.streams.byte2byte;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class NIODataOutputStream implements DataOutput, Closeable, Flushable {
	private static final int	MINIMUM_SEGMENT_SIZE = 8;
	private static final int	DEFAULT_SEGMENT_SIZE = 1 << 20;
	
	private final FileChannel	raf;
	private final boolean		bigEndian;
	private final byte[]		content;
	private int					where = 0;
	private boolean				closed = false;

	public NIODataOutputStream(final File file) throws IOException  {
		this(file, DEFAULT_SEGMENT_SIZE, ByteOrder.BIG_ENDIAN);
	}
	
	public NIODataOutputStream(final File file, final int segmentSize) throws IOException  {
		this(file, DEFAULT_SEGMENT_SIZE, ByteOrder.BIG_ENDIAN);
	}
	
	public NIODataOutputStream(final File file, final int segmentSize, final ByteOrder order) throws IOException  {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		else if (segmentSize < MINIMUM_SEGMENT_SIZE) {
			throw new IllegalArgumentException("Segment size ["+segmentSize+"] must be at lest "+MINIMUM_SEGMENT_SIZE+" bytes");
		}
		else if (order == null) {
			throw new NullPointerException("Byte order can't be null");
		}
		else {
			this.raf = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			this.content = new byte[segmentSize];
			this.bigEndian = order == ByteOrder.BIG_ENDIAN;
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (where + 1 >= content.length) {
			force();
			write(b);
		}
		else {
			content[where++] = (byte)b;
		}
	}

	@Override
	public void write(final byte[] b) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to write can't be null");
		}
		else if (b.length > 0) {
			write(b, 0, b.length);
		}
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to write can't be null");
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(b.length-1));
		}
		else if (len < 0 || off + len > b.length) {
			throw new IllegalArgumentException("Length [] nust be greater than 0 and offset + length ["+(off+len)+"] must be in range 1.."+b.length);
		}
		else if (len > 0){
			if (where + len >= content.length) {
				final int	tail = content.length - len, newOff = off + tail, newLen = len - tail;
				
				System.arraycopy(b, off, content, where, tail);
				force();
				write(b, newOff, newLen);
			}
			else {
				System.arraycopy(b, off, content, where, len);
				where += len;
			}
		}
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		if (where + 1 >= content.length) {
			force();
			writeBoolean(v);
		}
		else {
			content[where++] = (byte) (v ? 1 : 0);
		}
	}

	@Override
	public void writeByte(final int v) throws IOException {
		if (where + 1 >= content.length) {
			force();
			writeByte(v);
		}
		else {
			content[where++] = (byte) v;
		}
	}

	@Override
	public void writeShort(final int v) throws IOException {
		final byte[]	temp = content;
		
		if (where + 2 >= temp.length) {
			force();
			writeShort(v);
		}
		else if (bigEndian) {
			temp[where++] = (byte) ((v >>> 8) & 0xFF);
			temp[where++] = (byte) ((v >>> 0) & 0xFF);
    	}
    	else {
    		temp[where++] = (byte) ((v >>> 0) & 0xFF);
    		temp[where++] = (byte) ((v >>> 8) & 0xFF);
		}
	}

	@Override
	public void writeChar(final int v) throws IOException {
		final byte[]	temp = content;
		
		if (where + 2 >= temp.length) {
			force();
			writeChar(v);
		}
		else if (bigEndian) {
			temp[where++] = (byte) ((v >>> 8) & 0xFF);
			temp[where++] = (byte) ((v >>> 0) & 0xFF);
    	}
    	else {
    		temp[where++] = (byte) ((v >>> 0) & 0xFF);
    		temp[where++] = (byte) ((v >>> 8) & 0xFF);
		}
	}

	@Override
	public void writeInt(final int v) throws IOException {
		final byte[]	temp = content;
		
		if (where + 4 >= temp.length) {
			force();
			writeInt(v);
		}
		else if (bigEndian) {
			temp[where++] = (byte) ((v >>> 24) & 0xFF);
			temp[where++] = (byte) ((v >>> 16) & 0xFF);
			temp[where++] = (byte) ((v >>> 8) & 0xFF);
			temp[where++] = (byte) ((v >>> 0) & 0xFF);
    	}
    	else {
    		temp[where++] = (byte) ((v >>> 0) & 0xFF);
    		temp[where++] = (byte) ((v >>> 8) & 0xFF);
    		temp[where++] = (byte) ((v >>> 16) & 0xFF);
    		temp[where++] = (byte) ((v >>> 24) & 0xFF);
		}
	}

	@Override
	public void writeLong(final long v) throws IOException {
		final byte[]	temp = content;
		
		if (where + 8 >= temp.length) {
			force();
			writeLong(v);
		}
		else if (bigEndian) {
			temp[where++] = (byte) ((v >>> 56) & 0xFF);
			temp[where++] = (byte) ((v >>> 48) & 0xFF);
			temp[where++] = (byte) ((v >>> 40) & 0xFF);
			temp[where++] = (byte) ((v >>> 32) & 0xFF);
			temp[where++] = (byte) ((v >>> 24) & 0xFF);
			temp[where++] = (byte) ((v >>> 16) & 0xFF);
			temp[where++] = (byte) ((v >>> 8) & 0xFF);
			temp[where++] = (byte) ((v >>> 0) & 0xFF);
    	}
    	else {
    		temp[where++] = (byte) ((v >>> 0) & 0xFF);
    		temp[where++] = (byte) ((v >>> 8) & 0xFF);
    		temp[where++] = (byte) ((v >>> 16) & 0xFF);
    		temp[where++] = (byte) ((v >>> 24) & 0xFF);
    		temp[where++] = (byte) ((v >>> 32) & 0xFF);
    		temp[where++] = (byte) ((v >>> 40) & 0xFF);
    		temp[where++] = (byte) ((v >>> 48) & 0xFF);
    		temp[where++] = (byte) ((v >>> 56) & 0xFF);
		}
	}

	@Override
	public void writeFloat(final float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(final double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null");
		}
		else {
	        for (int i = 0, len = s.length(); i < len ; i++) {
	            write((byte)s.charAt(i));
	        }
		}
	}

	@Override
	public void writeChars(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null");
		}
		else {
	        for (int i = 0, len = s.length(); i < len ; i++) {
	            writeChar(s.charAt(i));
	        }
		}
	}

	@Override
	public void writeUTF(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null");
		}
		else {
			final int	len = calculateLength(s);
			
			if (len > Character.MAX_VALUE) {
	            throw new UTFDataFormatException("Encoded string too long: " + len + " bytes");
			}
			else {
				final byte[]	temp = content;

				writeShort(len);
				for(int index = 0; index < len; index++) {
		            final char c = s.charAt(index);
		            
		            if ((c >= 0x0001) && (c <= 0x007F)) {
		        		if (where + 1 >= temp.length) {
		        			force();
		        		}
		                temp[where++] = (byte) c;
		            } 
		            else if (c > 0x07FF) {
		        		if (where + 3 >= temp.length) {
		        			force();
		        		}
		        		temp[where++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
		        		temp[where++] = (byte) (0x80 | ((c >>  6) & 0x3F));
		        		temp[where++] = (byte) (0x80 | ((c >>  0) & 0x3F));
		            } 
		            else {
		        		if (where + 2 >= temp.length) {
		        			force();
		        		}
		        		temp[where++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
		        		temp[where++] = (byte) (0x80 | ((c >>  0) & 0x3F));
		            }
				}
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
			flush();
			raf.close();
			closed = true;
		}
	}

	private void force() throws IOException {
		raf.write(ByteBuffer.wrap(content, 0, where));
		where = 0;
	}

	private static int calculateLength(final String s) {
		int	count = 0;
		
        for (int i = 0, max = s.length(); i < max; i++) {
            final char c = s.charAt(i);
            
            if ((c >= 0x0001) && (c <= 0x007F)) {
            	count++;
            } 
            else if (c > 0x07FF) {
            	count += 3;
            } 
            else {
            	count += 2;
            }
        }
		return count;
	}
}
