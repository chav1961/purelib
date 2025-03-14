package chav1961.purelib.streams.byte2byte;

import java.io.DataInput;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UTFDataFormatException;

import chav1961.purelib.streams.interfaces.ByteOrder;

/**
 * <p>This class is copy of standard {@linkplain java.io.DataInputStream} class with different
 * byte orders support. Byte order is a {@linkplain ByteOrder} enumeration, that passes into class
 * constructor during instance creation.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class DataInputStream extends FilterInputStream implements DataInput {
	private final ByteOrder	order;
    private final byte readBuffer[] = new byte[8];
    private byte bytearr[] = new byte[80];
    private char chararr[] = new char[80];
    private char lineBuffer[];
	
    /**
     * <p>Constructor of the class</p>
     * @param in nested input stream to use. Can't be null.
     * @param order byte order to use. Can't be null.
     * @throws NullPointerException any of the parameters is null.
     */
    public DataInputStream(final InputStream in, final ByteOrder order) throws NullPointerException {
        super(in);
        if (order == null) {
        	throw new NullPointerException("Byte order can't be null");
        }
        else {
        	this.order = order;
        }
    }
    
    /**
     * <p>Get current byte order.</p>
     * @return current byte order. Can't be null.
     */
    public ByteOrder getByteOrder() {
    	return order;
    }

    @Override
    public final int read(byte b[]) throws IOException {
        return in.read(b, 0, b.length);
    }

    @Override
    public final int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public final void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    @Override
    public final int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) in.skip(n-total)) > 0)) {
            total += cur;
        }

        return total;
    }

    @Override
    public final boolean readBoolean() throws IOException {
        int ch = in.read();
        
        if (ch < 0) {
            throw new EOFException();
        }
        else {
            return (ch != 0);
        }
    }

    @Override
    public final byte readByte() throws IOException {
        int ch = in.read();
        
        if (ch < 0) {
            throw new EOFException();
        }
        else {
            return (byte)(ch);
        }
    }

    @Override
    public final int readUnsignedByte() throws IOException {
        int ch = in.read();
        
        if (ch < 0) {
            throw new EOFException();
        }
        else {
            return ch;
        }
    }

    @Override
    public final short readShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        else {
        	switch (order) {
				case BIG_ENDIAN		:
			        return (short)((ch1 << 8) + (ch2 << 0));
				case LITTLE_ENDIAN	:
			        return (short)((ch1 << 0) + (ch2 << 8));
				default:
					throw new UnsupportedOperationException("Byte order ["+order+"] is not supported yet");
        	}
        }
    }

    @Override
    public final int readUnsignedShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        else {
        	switch (order) {
				case BIG_ENDIAN		:
			        return (ch1 << 8) + (ch2 << 0);
				case LITTLE_ENDIAN	:
			        return (ch1 << 0) + (ch2 << 8);
				default:
					throw new UnsupportedOperationException("Byte order ["+order+"] is not supported yet");
        	}
        }
    }

    @Override
    public final char readChar() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        else {
        	switch (order) {
				case BIG_ENDIAN		:
			        return (char)((ch1 << 8) + (ch2 << 0));
				case LITTLE_ENDIAN	:
			        return (char)((ch1 << 0) + (ch2 << 8));
				default:
					throw new UnsupportedOperationException("Byte order ["+order+"] is not supported yet");
        	}
        }
    }

    @Override
    public final int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        else {
        	switch (order) {
				case BIG_ENDIAN		:
			        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
				case LITTLE_ENDIAN	:
			        return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
				default:
					throw new UnsupportedOperationException("Byte order ["+order+"] is not supported yet");
        	}
        }
    }

    @Override
    public final long readLong() throws IOException {
        readFully(readBuffer, 0, 8);
    	switch (order) {
			case BIG_ENDIAN		:
		        return (((long)readBuffer[0] << 56) +
		                ((long)(readBuffer[1] & 255) << 48) +
		                ((long)(readBuffer[2] & 255) << 40) +
		                ((long)(readBuffer[3] & 255) << 32) +
		                ((long)(readBuffer[4] & 255) << 24) +
		                ((readBuffer[5] & 255) << 16) +
		                ((readBuffer[6] & 255) <<  8) +
		                ((readBuffer[7] & 255) <<  0));
			case LITTLE_ENDIAN	:
		        return (((long)readBuffer[7] << 56) +
		                ((long)(readBuffer[6] & 255) << 48) +
		                ((long)(readBuffer[5] & 255) << 40) +
		                ((long)(readBuffer[4] & 255) << 32) +
		                ((long)(readBuffer[3] & 255) << 24) +
		                ((readBuffer[2] & 255) << 16) +
		                ((readBuffer[1] & 255) <<  8) +
		                ((readBuffer[0] & 255) <<  0));
			default:
				throw new UnsupportedOperationException("Byte order ["+order+"] is not supported yet");
    	}
        
    }

    @Override
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    @Deprecated
    public final String readLine() throws IOException {
        char buf[] = lineBuffer;

        if (buf == null) {
            buf = lineBuffer = new char[128];
        }

        int room = buf.length;
        int offset = 0;
        int c;

loop:   while (true) {
            switch (c = in.read()) {
              case -1:
              case '\n':
                break loop;

              case '\r':
                int c2 = in.read();
                if ((c2 != '\n') && (c2 != -1)) {
                    if (!(in instanceof PushbackInputStream)) {
                        this.in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream)in).unread(c2);
                }
                break loop;

              default:
                if (--room < 0) {
                    buf = new char[offset + 128];
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
    public final String readUTF() throws IOException {
        return readUTF(this);
    }

    public final static String readUTF(DataInput in) throws IOException {
        int utflen = in.readUnsignedShort();
        byte[] bytearr = null;
        char[] chararr = null;
        if (in instanceof DataInputStream) {
            DataInputStream dis = (DataInputStream)in;
            
            if (dis.bytearr.length < utflen){
                dis.bytearr = new byte[utflen*2];
                dis.chararr = new char[utflen*2];
            }
            chararr = dis.chararr;
            bytearr = dis.bytearr;
        } else {
            bytearr = new byte[utflen];
            chararr = new char[utflen];
        }

        int c, char2, char3;
        int count = 0;
        int chararr_count=0;

        in.readFully(bytearr, 0, utflen);

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++]=(char)c;
        }

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
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
                        throw new UTFDataFormatException(
                            "malformed input: partial character at end");
                    char2 = (int) bytearr[count-1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                            "malformed input around byte " + count);
                    chararr[chararr_count++]=(char)(((c & 0x1F) << 6) |
                                                    (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                            "malformed input: partial character at end");
                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                            "malformed input around byte " + (count-1));
                    chararr[chararr_count++]=(char)(((c     & 0x0F) << 12) |
                                                    ((char2 & 0x3F) << 6)  |
                                                    ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                        "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }
}
