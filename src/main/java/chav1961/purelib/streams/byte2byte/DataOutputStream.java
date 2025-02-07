package chav1961.purelib.streams.byte2byte;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

import chav1961.purelib.streams.interfaces.ByteOrder;

/**
 * <p>This class is copy of standard {@linkplain java.io.DataOutputStream} class with different
 * byte orders support. Byte order is a {@linkplain ByteOrder} enumeration, that passes into class
 * constructor during instance creation.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class DataOutputStream extends FilterOutputStream implements DataOutput {
    protected int 		written = 0;
    
    private final 		ByteOrder	order;
    private final byte 	writeBuffer[] = new byte[8];
    private byte[] 		bytearr = null;
    
    /**
     * <p>Constructor of the class</p>
     * @param out nested output stream to use. Can't be null.</p>
     * @param order byte order to use. Can't be null.
     * @throws NullPointerException any of the parameters is null.
     */
    public DataOutputStream(final OutputStream out, final ByteOrder order) throws NullPointerException {
        super(out);
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
    public final ByteOrder getByteOrder() {
    	return order;
    }
    
    @Override
    public synchronized void write(int b) throws IOException {
        out.write(b);
        incCount(1);
    }

    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        incCount(len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public final void writeBoolean(boolean v) throws IOException {
        out.write(v ? 1 : 0);
        incCount(1);
    }

    @Override
    public final void writeByte(int v) throws IOException {
        out.write(v);
        incCount(1);
    }

    @Override
    public final void writeShort(int v) throws IOException {
    	switch (order) {
			case BIG_ENDIAN		:
		        out.write((v >>> 8) & 0xFF);
		        out.write((v >>> 0) & 0xFF);
				break;
			case LITTLE_ENDIAN	:
		        out.write((v >>> 0) & 0xFF);
		        out.write((v >>> 8) & 0xFF);
				break;
			default:
				throw new UnsupportedOperationException("Byte order ["+order+"] ia not supported yet");
    	}
        incCount(2);
    }

    @Override
    public final void writeChar(int v) throws IOException {
    	switch (order) {
			case BIG_ENDIAN		:
		        out.write((v >>> 8) & 0xFF);
		        out.write((v >>> 0) & 0xFF);
				break;
			case LITTLE_ENDIAN	:
		        out.write((v >>> 0) & 0xFF);
		        out.write((v >>> 8) & 0xFF);
				break;
			default:
				throw new UnsupportedOperationException("Byte order ["+order+"] ia not supported yet");
    	}
        incCount(2);
    }

    @Override
    public final void writeInt(int v) throws IOException {
    	switch (order) {
			case BIG_ENDIAN		:
		        out.write((v >>> 24) & 0xFF);
		        out.write((v >>> 16) & 0xFF);
		        out.write((v >>>  8) & 0xFF);
		        out.write((v >>>  0) & 0xFF);
				break;
			case LITTLE_ENDIAN	:
		        out.write((v >>>  0) & 0xFF);
		        out.write((v >>>  8) & 0xFF);
		        out.write((v >>> 16) & 0xFF);
		        out.write((v >>> 24) & 0xFF);
				break;
			default:
				throw new UnsupportedOperationException("Byte order ["+order+"] ia not supported yet");
    	}
        incCount(4);
    }

    @Override
    public final void writeLong(long v) throws IOException {
    	switch (order) {
			case BIG_ENDIAN		:
		        writeBuffer[0] = (byte)(v >>> 56);
		        writeBuffer[1] = (byte)(v >>> 48);
		        writeBuffer[2] = (byte)(v >>> 40);
		        writeBuffer[3] = (byte)(v >>> 32);
		        writeBuffer[4] = (byte)(v >>> 24);
		        writeBuffer[5] = (byte)(v >>> 16);
		        writeBuffer[6] = (byte)(v >>>  8);
		        writeBuffer[7] = (byte)(v >>>  0);
				break;
			case LITTLE_ENDIAN	:
		        writeBuffer[0] = (byte)(v >>>  0);
		        writeBuffer[1] = (byte)(v >>>  8);
		        writeBuffer[2] = (byte)(v >>> 16);
		        writeBuffer[3] = (byte)(v >>> 24);
		        writeBuffer[4] = (byte)(v >>> 32);
		        writeBuffer[5] = (byte)(v >>> 40);
		        writeBuffer[6] = (byte)(v >>> 48);
		        writeBuffer[7] = (byte)(v >>> 56);
				break;
			default:
				throw new UnsupportedOperationException("Byte order ["+order+"] ia not supported yet");
    	}
        out.write(writeBuffer, 0, 8);
        incCount(8);
    }

    @Override
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public final void writeBytes(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            out.write((byte)s.charAt(i));
        }
        incCount(len);
    }

    @Override
    public final void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
        	switch (order) {
    			case BIG_ENDIAN		:
    	            out.write((v >>> 8) & 0xFF);
    	            out.write((v >>> 0) & 0xFF);
    				break;
    			case LITTLE_ENDIAN	:
    	            out.write((v >>> 0) & 0xFF);
    	            out.write((v >>> 8) & 0xFF);
    				break;
    			default:
    				throw new UnsupportedOperationException("Byte order ["+order+"] ia not supported yet");
        	}
        }
        incCount(len * 2);
    }

    @Override
    public final void writeUTF(String str) throws IOException {
        writeUTF(str, this);
    }

    static int writeUTF(String str, DataOutput out) throws IOException {
        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535) {
            throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
        }

        byte[] bytearr = null;
        if (out instanceof DataOutputStream) {
            final DataOutputStream dos = (DataOutputStream)out;
            
            if(dos.bytearr == null || (dos.bytearr.length < (utflen+2))) {
                dos.bytearr = new byte[(utflen*2) + 2];
            }
            bytearr = dos.bytearr;
        } else {
            bytearr = new byte[utflen+2];
        }

        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i=0;
        for (i=0; i<strlen; i++) {
           c = str.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F))) break;
           bytearr[count++] = (byte) c;
        }

        for (;i < strlen; i++){
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        out.write(bytearr, 0, utflen+2);
        return utflen + 2;
    }

    /**
     * <p>Get size written.</p>
     * @return size written. On large amount will be truncated to {@linkplain Integer#MAX_VALUE}. 
     */
    public final int size() {
        return written;
    }

    private void incCount(int value) {
        int temp = written + value;
        if (temp < 0) {
            temp = Integer.MAX_VALUE;
        }
        written = temp;
    }
}
