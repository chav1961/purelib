package chav1961.purelib.basic.growablearrays;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;

/**
 * <p>This class extends functionality of {@linkplain GrowableByteArray} with different I/O-styled operations. To support this feature, the class
 * implements {@linkplain DataOutput} and {@linkplain DataInput} interfaces. There is an internal <i>cursor</i> inside the class. It moves from the beginning
 * of the array content to the end of array content during all input and output operations (it's similar to file pointer in the {@linkplain RandomAccessFile}).</p>
 * <p>To start writing data to the array, simply use {@linkplain DataOutput} interface methods. To start reading data from the array, simply use {@linkplain DataInput}
 * interface methods. You shouldn't mix calling methods from these interfaces, but you can <i>switch</i> I/O mode for the class by calling {@linkplain #reset()}
 * method in the class. I/O mode for the class can be:</p>
 * <ul>
 * <li><b>write</b> mode - you can use {@linkplain DataOutput} interface methods only</li>
 * <li><b>read</b> mode - you can use {@linkplain DataInput} interface methods only</li>
 * </ul>
 * <p>After calling {@linkplain #reset()} both of the modes are undefined. The first call to any methods from {@linkplain DataOutput} interface after calling {@linkplain #reset()}
 * sets the <b>write</b> mode implicitly. The first call to any methods from {@linkplain DataInput} interface after calling {@linkplain #reset()} sets the <b>read</b> mode implicitly.</p>
 * <p>Reading array content starts from the beginning of the array content and stops when internal cursor will be positioned after the last byte in the array. Any subsequential calls for 
 * {@linkplain DataInput} interface methods will fire {@linkplain EOFException}. Writing array content starts from the beginning of the array content until the last byte in the array. 
 * Any subsequential calls for {@linkplain DataOutput} interface methods produce automatic array growing.</p>
 * <p>The most of code for this class was copied from standard JRE {@linkplain DataInputStream} and {@linkplain DataOutputStream} classes and adapted for the specific requirements and
 * abilities of the {@linkplain GrowableByteArray} class.</p>
 * <p>Calling {@linkplain #reset()} always frees specific internal resources of the class. Especially for this case, the class implements {@linkplain Closeable} interface to support using
 * it in the <b>try-with-resource</b> statements. The only action of the {@linkplain #close()} method is calling {@linkplain #reset()} method.</p> 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.basic.growablearrays JUnit tests
 * @author Alexander Chernomyrdin aka chav1961 
 * @since 0.0.2
 * @last.update 0.0.8
 */

public class InOutGrowableByteArray extends GrowableByteArray implements DataOutput, DataInput, Flushable, Closeable {
	private final StringBuilder	sb = new StringBuilder();
	private int					displ = 0;
	private byte[]				forLong = new byte[8];
	private byte[]				buffer = new byte[0];
	private char[]				charBuffer = new char[0];
	private boolean				wasInput = false, wasOutput = false;

	/**
	 * <p>Create growable byte array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 */
	public InOutGrowableByteArray(boolean usePlain) {
		super(usePlain);
	}

	/**
	 * <p>Create growable byte array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 * @param initialPow 2^^initialPow is a piece size of the growable array.
	 */
	public InOutGrowableByteArray(boolean usePlain, int initialPow) {
		super(usePlain, initialPow);
	}

	@Override
	public void flush() throws IOException {
	}
	
	@Override
	public void close() throws IOException {
		reset();
	}
	
	/**
	 * <p>Reset I/O operations and read/write mode on the class. Frees any specific internal resources for I/O</p> 
	 * @throws IOException on any exception during resetting content 
	 */
	public void reset() throws IOException {
		displ = 0;
		buffer = new byte[0];
		charBuffer = new char[0];
		sb.setLength(0);
		wasInput = wasOutput = false;
		length(0);
	}

	/**
	 * <p>Rewinds buffer to the start point.</p>
	 * @throws IOException on any exception during rewind
	 * @since 0.0.8
	 */
	public void rewind() throws IOException {
		displ = 0;
		sb.setLength(0);
		wasInput = wasOutput = false;
	}
	
	@Override
	public void readFully(final byte[] b) throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (b == null) {
			throw new NullPointerException("Buffer to read data to can't be null"); 
		}
		else {
			wasInput = true;
			readFully(b,0,b.length);
		}
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len) throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (b == null) {
			throw new NullPointerException("Buffer to read data to can't be null");
		}
		else if (off < 0 || off >= b.length) {
			throw new ArrayIndexOutOfBoundsException("Offset ["+off+"] out of bounds. Valid range is 0.."+(b.length-1));
		}
		else if (len < 0 || len > b.length) {
			throw new ArrayIndexOutOfBoundsException("Length ["+len+"] out of bounds. Valid range is 0.."+(b.length));
		}
		else if (off + len < 0 || off + len > b.length) {
			throw new ArrayIndexOutOfBoundsException("Offset + length ["+(off + len)+"] out of bounds. Valid range is 0.."+(b.length));
		}
		else if (displ + len >= length()) {
			throw new EOFException("EOF detected while reading ["+len+"] bytes"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			displ += uncheckedRead(displ,b,off,off+len);
		}
	}

	@Override
	public int skipBytes(final int n) throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (n < 0) {
			throw new IllegalArgumentException("Number of bytes to skip ["+n+"] is negative!");
		}
		else if (wasOutput) {
			throw new IllegalStateException("Don't use skip() on output"); 
		}
		else {
			final int	oldDispl = displ;
			
			wasInput = true;
			displ = Math.min(length(),displ+n);
			return displ - oldDispl; 
		}
	}

	@Override
	public boolean readBoolean() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length()) {
			throw new EOFException("EOF detected while reading byte"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			return uncheckedRead(displ++) != 0;
		}
	}

	@Override
	public byte readByte() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length()) {
			throw new EOFException("EOF detected while reading byte"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			return uncheckedRead(displ++);
		}
	}

	@Override
	public int readUnsignedByte() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length()) {
			throw new EOFException("EOF detected while reading unsigned byte"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			return uncheckedRead(displ++) & 0xFF;
		}
	}

	@Override
	public short readShort() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length() - 1) {
			throw new EOFException("EOF detected while reading short"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			return (short)((uncheckedRead(displ++) << 8) | (uncheckedRead(displ++) << 0) & 0xFF); 
		}
	}

	@Override
	public int readUnsignedShort() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length() - 1) {
			throw new EOFException("EOF detected while reading short"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			return ((uncheckedRead(displ++) << 8) | (uncheckedRead(displ++) << 0) & 0xFF) & 0xFFFF; 
		}
	}

	@Override
	public char readChar() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length() - 1) {
			throw new EOFException("EOF detected while reading char"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			return (char)((uncheckedRead(displ++) << 8) | (uncheckedRead(displ++) << 0) & 0xFF); 
		}
	}

	@Override
	public int readInt() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length() - 3) {
			throw new EOFException("EOF detected while reading int"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			uncheckedRead(displ,forLong,0,4);
			displ += 4;
	        return  ((forLong[0] & 0xFF) << 24) |
	                ((forLong[1] & 0xFF) << 16) |
	                ((forLong[2] & 0xFF) <<  8) |
	                ((forLong[3] & 0xFF) <<  0);
		}
	}

	@Override
	public long readLong() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (displ >= length() - 7) {
			throw new EOFException("EOF detected while reading int"); 
		}
		else if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			uncheckedRead(displ,forLong,0,8);
			displ += 8;
	        return (((long)forLong[0] << 56) | ((long)(forLong[1] & 0xFF) << 48) | ((long)(forLong[2] & 0xFF) << 40) | ((long)(forLong[3] & 0xFF) << 32) |
	                ((long)(forLong[4] & 0xFF) << 24) | ((forLong[5] & 0xFF) << 16) | ((forLong[6] & 0xFF) <<  8) | ((forLong[7] & 0xFF) <<  0));
		}
	}

	@Override
	public float readFloat() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
        return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
        return Double.longBitsToDouble(readLong());
	}

	@Override
	public String readLine() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasInput = true;
			sb.setLength(0);
			
			for (int index = 0, maxIndex = length() - displ; index < maxIndex; index++) {
				final char	symbol = (char)uncheckedRead(displ + index);
				
				if (symbol == '\n') {
					displ += index + 1; 
					return sb.toString();
				}
				else if (symbol == '\r') {
					displ += index + (displ + index < maxIndex - 1 && uncheckedRead(displ + index + 1) == '\n' ? 2 : 1);
					return sb.toString();
				}
				else {
					sb.append(symbol);
				}
			}
			return sb.toString();
		}
	}

	@Override
	public String readUTF() throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, IllegalStateException {
		if (wasOutput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
	        final int 		utflen = readUnsignedShort();
	        
	        if (utflen > buffer.length) {
	        	buffer = new byte[utflen * 2];
	        	charBuffer = new char[utflen * 2];
	        }
	        final byte[]	temp = buffer;
	        final char[]	charTemp = charBuffer;
	        int 			c, char2, char3;
	        int 			count = 0;
	        int 			chararr_count=0;
	
	        readFully(temp,0,utflen);
			wasInput = true;
	
	        while (count < utflen) {
	            c = (int) temp[count] & 0xff;
	            if (c > 127) break;
	            count++;
	            charTemp[chararr_count++] = (char)c;
	        }
	
	        while (count < utflen) {
	            c = (int) temp[count] & 0xff;
	            switch (c >> 4) {
	                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
	                    /* 0xxxxxxx*/
	                    count++;
	                    charTemp[chararr_count++]=(char)c;
	                    break;
	                case 12: case 13:
	                    /* 110x xxxx   10xx xxxx*/
	                    count += 2;
	                    if (count > utflen) {
	                        throw new UTFDataFormatException("malformed input: partial character at end");
	                    }
	                    char2 = (int) temp[count-1];
	                    if ((char2 & 0xC0) != 0x80) {
	                        throw new UTFDataFormatException("malformed input around byte " + count);
						}                            
	                    charTemp[chararr_count++]=(char)(((c & 0x1F) << 6) | (char2 & 0x3F));
	                    break;
	                case 14:
	                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
	                    count += 3;
	                    if (count > utflen) {
	                        throw new UTFDataFormatException("malformed input: partial character at end");
	                    }
	                    char2 = (int) temp[count-2];
	                    char3 = (int) temp[count-1];
	                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
	                        throw new UTFDataFormatException("malformed input around byte " + (count-1));
	                    }
	                    charTemp[chararr_count++]=(char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
	                    break;
	                default:
	                    /* 10xx xxxx,  1111 xxxx */
	                    throw new UTFDataFormatException("malformed input around byte " + count);
	            }
	        }
	        return new String(charTemp,0,chararr_count);
        }
	}

	@Override
	public void write(final int b) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			displ += unckeckedWrite(displ, (byte)b);
		}
	}

	@Override
	public void write(final byte[] b) throws IOException {
		if (b == null) {
			throw new NullPointerException("Byte array to write can't be null");
		}
		else if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			write(b, 0, b.length);
		}
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else if (b == null) {
			throw new NullPointerException("Byte array to write can't be null");
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range. Valig range is 0.."+(b.length));
		}
		else if (len < 0 || len > b.length) {
			throw new IllegalArgumentException("Length ["+len+"] out of range. Valig range is 0.."+(b.length));
		}
		else if (off + len < 0 || off + len > b.length) {
			throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range. Valig range is 0.."+(b.length-1));
		}
		else {
			wasOutput = true;
			displ += unckeckedWrite(displ, b, off, len);
		}
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			displ += unckeckedWrite(displ,v ? (byte)1 : (byte)0);
		}
	}

	@Override
	public void writeByte(final int v) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			displ += unckeckedWrite(displ, (byte)v);
		}
	}

	@Override
	public void writeShort(int v) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			displ += unckeckedWrite(displ, (byte)((v >>> 8) & 0xFF)); 
			displ += unckeckedWrite(displ, (byte)((v >>> 0) & 0xFF));
		}
	}

	@Override
	public void writeChar(final int v) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			displ += unckeckedWrite(displ, (byte)((v >>> 8) & 0xFF)); 
			displ += unckeckedWrite(displ, (byte)((v >>> 0) & 0xFF));
		}
	}

	@Override
	public void writeInt(final int v) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			intoLong(0, ((v >>> 24) & 0xFF));
			intoLong(1, ((v >>> 16) & 0xFF));
			intoLong(2, ((v >>>  8) & 0xFF));
			intoLong(3, ((v >>>  0) & 0xFF));
			flushLong(4);
//			forLong[0] = (byte)((v >>> 24) & 0xFF);
//			forLong[1] = (byte)((v >>> 16) & 0xFF);
//			forLong[2] = (byte)((v >>>  8) & 0xFF);
//			forLong[3] = (byte)((v >>>  0) & 0xFF);
//			displ += unckeckedWrite(displ,forLong,0,4);
		}
	}

	@Override
	public void writeLong(final long v) throws IOException {
		if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			intoLong(0, ((v >>> 56) & 0xFF));
			intoLong(1, ((v >>> 48) & 0xFF));
			intoLong(2, ((v >>> 40) & 0xFF));
			intoLong(3, ((v >>> 32) & 0xFF));
			intoLong(4, ((v >>> 24) & 0xFF));
			intoLong(5, ((v >>> 16) & 0xFF));
			intoLong(6, ((v >>>  8) & 0xFF));
			intoLong(7, ((v >>>  0) & 0xFF));
			flushLong(8);
//			forLong[0] = (byte)((v >>> 56) & 0xFF);
//			forLong[1] = (byte)((v >>> 48) & 0xFF);
//			forLong[2] = (byte)((v >>> 40) & 0xFF);
//			forLong[3] = (byte)((v >>> 32) & 0xFF);
//			forLong[4] = (byte)((v >>> 24) & 0xFF);
//			forLong[5] = (byte)((v >>> 16) & 0xFF);
//			forLong[6] = (byte)((v >>>  8) & 0xFF);
//			forLong[7] = (byte)((v >>>  0) & 0xFF);
//			displ += unckeckedWrite(displ,forLong,0,8);
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
		else if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;

			int		where = 0;
//			int	len = s.length(), tail = len, index = 0;
			
			for (int index = 0, maxIndex = s.length(); index < maxIndex; index++) {
				where = intoLong(where, s.charAt(index));
			}
			flushLong(where);
//			if (len >= 8) {
//				for (index = 0; index <= len - 8; index += 8, tail -= 8) {
//					forLong[0] = (byte)s.charAt(index);
//					forLong[1] = (byte)s.charAt(index+1);
//					forLong[2] = (byte)s.charAt(index+2);
//					forLong[3] = (byte)s.charAt(index+3);
//					forLong[4] = (byte)s.charAt(index+4);
//					forLong[5] = (byte)s.charAt(index+5);
//					forLong[6] = (byte)s.charAt(index+6);
//					forLong[7] = (byte)s.charAt(index+7);
//					displ += unckeckedWrite(displ,forLong,0,8);
//				}
//			}
//			for (int delta = 0; delta < tail; index++, delta++) {
//				forLong[delta] = (byte)s.charAt(index);
//			}
//			displ += unckeckedWrite(displ,forLong,0,tail);
		}
	}

	@Override
	public void writeChars(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null"); 
		}
		else if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;

			int		where = 0;
			char	value;
//			int	len = s.length(), tail = len, index = 0;
			
			for (int index = 0, maxIndex = s.length(); index < maxIndex; index++) {
				value = s.charAt(index);	
				where = intoLong(where, ((value >>> 8) & 0xFF));
				where = intoLong(where, ((value >>> 0) & 0xFF));
			}
			flushLong(where);
//			
//			int		len = s.length(), tail = len, index = 0;
//			char	value;
//			
//			if (len >= 4) {
//				for (index = 0; index <= len - 4; index += 4, tail -= 4) {
//					value = s.charAt(index);	
//					forLong[0] = (byte)((value >>> 8) & 0xFF);	
//					forLong[1] = (byte)((value >>> 0) & 0xFF);
//					value = s.charAt(index+1);	
//					forLong[2] = (byte)((value >>> 8) & 0xFF);	
//					forLong[3] = (byte)((value >>> 0) & 0xFF);
//					value = s.charAt(index+2);	
//					forLong[4] = (byte)((value >>> 8) & 0xFF);	
//					forLong[5] = (byte)((value >>> 0) & 0xFF);
//					value = s.charAt(index+3);	
//					forLong[6] = (byte)((value >>> 8) & 0xFF);	
//					forLong[7] = (byte)((value >>> 0) & 0xFF);
//					displ += unckeckedWrite(displ,forLong,0,8);
//				}
//			}
//			for (int delta = 0; delta < tail; index++, delta += 2) {
//				value = s.charAt(index);	
//				forLong[delta] = (byte)((value >>> 8) & 0xFF);	
//				forLong[delta+1] = (byte)((value >>> 0) & 0xFF);
//			}
//			displ += unckeckedWrite(displ,forLong,0,2*tail);
		}
	}

	@Override
	public void writeUTF(final String str) throws IOException {
		if (str == null) {
			throw new NullPointerException("String to write can't be null"); 
		}
		else if (wasInput) {
			throw new IllegalStateException("Mix with input and output operations! Don't mix them or call reset() between the ones"); 
		}
		else {
			wasOutput = true;
			
			final int 	strlen = str.length(); 
			int 		utflen = 0, c;
			
			for (int i = 0; i < strlen; i++) {
			    c = str.charAt(i);
			    if ((c >= 0x0001) && (c <= 0x007F)) {
			        utflen++;
			    } 
			    else if (c > 0x07FF) {
			        utflen += 3;
			    } else {
			        utflen += 2;
			    }
			}
			
			if (utflen > 65535) {
			    throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
			}
			writeShort(utflen);
			
			int i = 0, forLongIndex = 0, where = 0;
			
			for (i=0; i < strlen; i++, forLongIndex++) {
			   c = str.charAt(i);
			   if (!((c >= 0x0001) && (c <= 0x007F))) {
				   break;
			   }
			   else {
//				   if (forLongIndex >= 8) {
//					   displ += unckeckedWrite(displ,forLong,0,8);
//					   forLongIndex = 0;
//				   }
//				   forLong[forLongIndex] = (byte) c;
				   where = intoLong(where, c);
			   }
			}
			flushLong(where);
			where = 0;
//			if (forLongIndex != 0) {
//				displ += unckeckedWrite(displ,forLong,0,forLongIndex);
//				forLongIndex = 0;
//			}
			
			for (; i < strlen; i++, forLongIndex++){
			    c = str.charAt(i);
			    if ((c >= 0x0001) && (c <= 0x007F)) {
//				   if (forLongIndex >= 8) {
//					   displ += unckeckedWrite(displ,forLong,0,forLongIndex);
//					   forLongIndex = 0;
//				   }
//				   forLong[forLongIndex] = (byte) c;
				   where = intoLong(where, c);
			    } 
			    else if (c > 0x07FF) {
				   where = intoLong(where, (0xE0 | ((c >> 12) & 0x0F)));
				   where = intoLong(where, (0x80 | ((c >>  6) & 0x3F)));
				   where = intoLong(where, (0x80 | ((c >>  0) & 0x3F)));
//				   if (forLongIndex >= 6) {
//					   displ += unckeckedWrite(displ,forLong,0,forLongIndex);
//					   forLongIndex = 0;
//				   }
//				   forLong[forLongIndex] = (byte) (0xE0 | ((c >> 12) & 0x0F));
//				   forLong[forLongIndex+1] = (byte) (0x80 | ((c >>  6) & 0x3F));
//				   forLong[forLongIndex+2] = (byte) (0x80 | ((c >>  0) & 0x3F));
//				   forLongIndex += 2;
			    } 
			    else {
				   where = intoLong(where, (0xC0 | ((c >>  6) & 0x1F)));
				   where = intoLong(where, (0x80 | ((c >>  0) & 0x3F)));
//				   if (forLongIndex >= 7) {
//					   displ += unckeckedWrite(displ,forLong,0,forLongIndex);
//					   forLongIndex = 0;
//				   }
//				   forLong[forLongIndex] = (byte) (0xC0 | ((c >>  6) & 0x1F));
//				   forLong[forLongIndex+1] = (byte) (0x80 | ((c >>  0) & 0x3F));
			    }
			}
			flushLong(where);
//			if (forLongIndex != 0) {
//				displ += unckeckedWrite(displ,forLong,0,forLongIndex);
//				forLongIndex = 0;
//			}
		}
	}
	
	public void write(final InOutGrowableByteArray another) throws NullPointerException, IOException {
		if (another == null) {
			throw new NullPointerException("Another growable array can't be null"); 
		}
		else {
			write(another.toPlain().toArray(), 0, another.length());
		}
	}
	
	private int intoLong(int index, final long value) {
		if (index >= forLong.length) {
			flushLong(index);
			index = 0;
		}
		forLong[index] = (byte)value;
		return index + 1;
	}

	private void flushLong(final int size) {
		if (size != 0) {
			displ += unckeckedWrite(displ, forLong, 0, size);
		}
	}
}
