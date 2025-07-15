package chav1961.purelib.streams.byte2byte;

import java.io.Closeable;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.UTFDataFormatException;
import java.io.EOFException;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;


public class MappedDataInputStream implements DataInput, Closeable {
	private static final int	INITIAL_LINE_BUDDER_SIZE = 128;
	private static final byte[]	EMPTY_BYTE_ARRAY = new byte[0];
	private static final char[]	EMPTY_CHAR_ARRAY = new char[0];
	
	private final FileChannel		fc;
	private final MappedByteBuffer	mbb;
	private final ByteOrder			order;
	private byte[] bytearr = EMPTY_BYTE_ARRAY;
	private char[] chararr = EMPTY_CHAR_ARRAY;
    private char[] lineBuffer = EMPTY_CHAR_ARRAY;
	
	public MappedDataInputStream(final File file, final ByteOrder order) throws IOException {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		else if (!file.exists() || !file.isFile() || !file.canRead()) {
			throw new IllegalArgumentException("File ["+file.getAbsolutePath()+"] not exists, not a file or not grants access for you");
		}
		else if (file.length() >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException("File ["+file.getAbsolutePath()+"] is too long to use with the given class");
		}
		else if (order == null) {
			throw new NullPointerException("Byte order can't be null");
		}
		else {
			this.fc = FileChannel.open(file.toPath(), StandardOpenOption.READ);
			this.mbb = this.fc.map(MapMode.READ_ONLY, 0, file.length());
			this.order = order;
			this.mbb.order(order);
		}
	}
	
	public MappedDataInputStream(final MappedByteBuffer mbb, final ByteOrder order) throws IOException {
		if (mbb == null) {
			throw new NullPointerException("Mapped byte buffer can't be null");
		}
		else if (order == null) {
			throw new NullPointerException("Byte order can't be null");
		}
		else {
			this.fc = null;
			this.mbb = mbb; 
			this.order = order;
			this.mbb.order(order);
		}
	}

	public ByteOrder getByteOrder() {
		return order;
	}
	
	public long getPosition() {
		return mbb.position();
	}
	
	@Override
	public void close() throws IOException {
		if (fc != null) {
			fc.close();
		}
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to read into can't be null");
		}
		else {
			readFully(b, 0, b.length);
		}
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer to read into can't be null");
		}
		else if (off < 0 || off >= b.length) {
            throw new IndexOutOfBoundsException("Off position ["+off+"] out of range 0.."+(b.length-1));
		}
		else if (off+len <= 0 || off+len > b.length) {
            throw new IndexOutOfBoundsException("Off + len position ["+(off+len)+"] out of range 1.."+b.length);
		}
		else {
			try {
				mbb.get(b, off, len);
			} catch (BufferUnderflowException exc) {
				throw new EOFException();
			}
		}
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		if (n < 0) {
			throw new IllegalArgumentException("Number of bytes to skip ["+n+"] can't be less than 0");
		}
		else {
			final int	newPos = Math.min(mbb.limit(), mbb.position() + n);
			
			mbb.position(newPos);
			return newPos;
		}
	}

	@Override
	public boolean readBoolean() throws IOException {
		try {
			return mbb.get() != 0;
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public byte readByte() throws IOException {
		try {
			return mbb.get();
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public int readUnsignedByte() throws IOException {
		try {
			return mbb.get() & 0xFF;
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public short readShort() throws IOException {
		try {
			return mbb.getShort();
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public int readUnsignedShort() throws IOException {
		try {
			return mbb.getShort() & 0xFFFF;
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public char readChar() throws IOException {
		return mbb.getChar();
	}

	@Override
	public int readInt() throws IOException {
		try {
			return mbb.getInt();
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public long readLong() throws IOException {
		try {
			return mbb.getLong();
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public float readFloat() throws IOException {
		try {
			return mbb.getFloat();
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public double readDouble() throws IOException {
		try {
			return mbb.getDouble();
		} catch (BufferUnderflowException exc) {
			throw new EOFException();
		}
	}

	@Override
	public String readLine() throws IOException {
        char buf[] = lineBuffer;
        int room = buf.length;
        int offset = 0;
        int c;

loop:   while (true) {
            switch (c = readUnsignedByte()) {
              case -1:
              case '\n':
                break loop;

              case '\r':
                int c2 = readUnsignedByte();
                if ((c2 != '\n') && (c2 != -1)) {
                	mbb.position(mbb.position()-1);
                }
                break loop;

              default:
                if (--room < 0) {
                    buf = new char[offset + INITIAL_LINE_BUDDER_SIZE];
                    room = buf.length - offset - 1;
                    System.arraycopy(lineBuffer, 0, buf, 0, offset);
                    lineBuffer = buf;
                }
                buf[offset++] = (char) c;
                break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
	}

	@Override
	public String readUTF() throws IOException {
        final int utflen = readUnsignedShort();
        
        if (bytearr.length < utflen) {
            bytearr = new byte[utflen];
            chararr = new char[utflen];
        }
        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        if (utflen > 0) {
	        readFully(bytearr, 0, utflen);
	
	        while (count < utflen) {
	            c = (int) bytearr[count] & 0xFF;
	            if (c > 127) break;
	            count++;
	            chararr[chararr_count++]=(char)c;
	        }
	
	        while (count < utflen) {
	            c = (int) bytearr[count] & 0xFF;
	            switch (c >> 4) {
	                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
	                    /* 0xxxxxxx*/
	                    count++;
	                    chararr[chararr_count++]=(char)c;
	                    break;
	                case 12: case 13:
	                    /* 110x xxxx   10xx xxxx*/
	                    count += 2;
	                    if (count > utflen)
	                        throw new UTFDataFormatException("malformed input: partial character at end");
	                    char2 = (int) bytearr[count-1];
	                    if ((char2 & 0xC0) != 0x80)
	                        throw new UTFDataFormatException("malformed input around byte " + count);
	                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
	                                                    (char2 & 0x3F));
	                    break;
	                case 14:
	                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
	                    count += 3;
	                    if (count > utflen)
	                        throw new UTFDataFormatException("malformed input: partial character at end");
	                    char2 = (int) bytearr[count-2];
	                    char3 = (int) bytearr[count-1];
	                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
	                        throw new UTFDataFormatException("malformed input around byte " + (count-1));
	                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
	                                                    ((char2 & 0x3F) << 6)  |
	                                                    ((char3 & 0x3F) << 0));
	                    break;
	                default:
	                    /* 10xx xxxx,  1111 xxxx */
	                    throw new UTFDataFormatException("malformed input around byte " + count);
	            }
	        }
        }
        // The number of chars produced may be less than utflen
        return String.copyValueOf(chararr, 0, chararr_count);
	}
}
