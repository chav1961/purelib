package chav1961.purelib.streams.byte2byte;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Types;
import java.util.Date;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.streams.byte2byte.SQLDataOutputStream.Convertor;


public class SQLDataInputStream extends InputStream implements DataInput {
	private final InputStream	nested;
	
	public SQLDataInputStream(final InputStream nested) {
		if (nested == null) {
			throw new NullPointerException("Nested input streamcan't be null");
		}
		else {
			this.nested = nested;
		}
	}
	
	public Iterable<Short> content(final Object[] forValue) {
		return null;
	}
	
	public int read(final Object[] forValue) throws IOException {
		if (forValue == null || forValue.length == 0) {
			throw new IllegalArgumentException("Array for value can't be null and it's size must be at least 1"); 
		}
		else {
			final int	contentType = (int) readBytes(2);
	
			switch (contentType) {
				case Types.TINYINT	:
					forValue[0] = (byte)readBytes(1);
					break;
				case Types.SMALLINT	:
					forValue[0] = (short)readBytes(2);
					break;
				case Types.INTEGER	:
					forValue[0] = (int)readBytes(4);
					break;
				case Types.BIGINT	:
					forValue[0] = readBytes(8);
					break;
				case Types.FLOAT	:
					forValue[0] = Float.intBitsToFloat((int)readBytes(4));
					break;
				case Types.REAL : case Types.DOUBLE :
					forValue[0] = Double.longBitsToDouble(readBytes(8));
					break;
				case Types.BIT : case Types.BOOLEAN :
					forValue[0] = readBytes(1) == 1;
					break;
				case Types.NULL		:
					forValue[0] = null;
					return (int) readBytes(2);
				case Short.MAX_VALUE:
					forValue[0] = null;
					break;
				default : 
					final Convertor	conv = SQLDataOutputStream.getConvertor(contentType);
					
					if (conv != null) {
						forValue[0] = conv.convIn.process(contentType, this);
					}
					else {
						throw new UnsupportedOperationException("Data type ["+contentType+"] is not supported");
					}
					break;
			}
			return contentType;
		}
	}
	
	@Override
	public int read() throws IOException {
		return nested.read();
	}

	@Override
    public int read(byte b[], int off, int len) throws IOException {
    	return nested.read(b, off, len);
    }

	@Override
    public long skip(long n) throws IOException {
		throw new IOException("This method call is not applicable for the given class");
    }
	
	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b,0,b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		final int	contentType = (int) readBytes(2);
		
		if (contentType == Types.BLOB) {
			
		}
	}

	@Override
	public int skipBytes(int n) throws IOException {
		throw new IOException("Method call is not applicable for the given class"); 
	}

	@Override
	public boolean readBoolean() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.BIT || contentType == Types.BOOLEAN) {
			return readBytes(1) == 1;
		}
		else {
			throw new IOException("Attempt to read boolean, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public byte readByte() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.TINYINT) {
			return (byte) readBytes(1);
		}
		else {
			throw new IOException("Attempt to read byte, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public int readUnsignedByte() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.TINYINT) {
			return (int) readBytes(1);
		}
		else {
			throw new IOException("Attempt to read byte, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public short readShort() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.SMALLINT) {
			return (short) readBytes(2);
		}
		else {
			throw new IOException("Attempt to read short, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public int readUnsignedShort() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.SMALLINT) {
			return (int) readBytes(2);
		}
		else {
			throw new IOException("Attempt to read short, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public char readChar() throws IOException {
		final int	contentType = (int) readBytes(2);
		
		if (contentType == Types.CHAR) {
			if (((int) readBytes(4)) > 1) {
				throw new IOException("Truncated input"); 
			}
			else {
				return (char) readBytes(2);
			}
		}
		else {
			throw new IOException("Attempt to read int, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public int readInt() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.INTEGER) {
			return (int) readBytes(4);
		}
		else {
			throw new IOException("Attempt to read int, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public long readLong() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.BIGINT) {
			return readBytes(8);
		}
		else {
			throw new IOException("Attempt to read long, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public float readFloat() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.FLOAT) {
			return Float.intBitsToFloat((int)readBytes(4));
		}
		else {
			throw new IOException("Attempt to read float, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public double readDouble() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.REAL || contentType == Types.DOUBLE) {
			return Double.longBitsToDouble(readBytes(8));
		}
		else {
			throw new IOException("Attempt to read double, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public String readLine() throws IOException {
		return readUTF();
	}

	@Override
	public String readUTF() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.CHAR || contentType == Types.VARCHAR || contentType == Types.LONGVARCHAR || contentType == Types.NVARCHAR || contentType == Types.LONGVARCHAR) {
			final char[]	buffer = new char[(int) readBytes(4)];
			
			final Reader	rdr = new InputStreamReader(nested, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			
			if (rdr.read(buffer) != buffer.length) {
				throw new EOFException("EOF while reading string content"); 
			}
			else {
				return new String(buffer);
			}
		}
		else {
			throw new IOException("Attempt to read character strings, but input stream content is ["+contentType+"]"); 
		}
	}

	private long readBytes(final int contentSize) throws IOException {
		return 0L;
	}
	
	
	Date readDate(final int sqlType) {
		return null;
	}
	
}
