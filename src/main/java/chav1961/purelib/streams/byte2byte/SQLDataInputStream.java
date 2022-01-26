package chav1961.purelib.streams.byte2byte;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.sql.InMemoryLittleBlob;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.streams.byte2byte.SQLDataOutputStream.Convertor;


public class SQLDataInputStream extends InputStream implements DataInput {
	public static final int		EOL = Short.MAX_VALUE;
	
	private final InputStream	nested;
	private final byte[] 		temp = new byte[8];
	
	public SQLDataInputStream(final InputStream nested) {
		if (nested == null) {
			throw new NullPointerException("Nested input streamcan't be null");
		}
		else {
			this.nested = nested;
		}
	}
	
	public Iterable<Integer> content(final Object[] forValue) {
		if (forValue == null || forValue.length != 1) {
			throw new IllegalArgumentException("Object array can't be null and must contain at least one element");
		}
		else {
			return new Iterable<Integer>() {
				@Override
				public Iterator<Integer> iterator() {
					return new Iterator<Integer>() {
						private final Object[]	currentValue = new Object[1];
						private int				currentType = 0;
	
						@Override
						public boolean hasNext() {
							try{currentType = read(currentValue);
								return true;
							} catch (IOException exc) {
								return false;
							}
						}
	
						@Override
						public Integer next() {
							forValue[0] = currentValue[0];
							return currentType;
						}
					};
				}
			};
		}
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
				case EOL			:
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
			final byte[]	buffer = new byte[(int) readBytes(4)];
			
			if (nested.read(buffer) < buffer.length) {
				throw new EOFException("End of file while reading"); 
			}
			else {
				final String	s = new String(buffer, PureLibSettings.DEFAULT_CONTENT_ENCODING);
				
				if (s.length() > 1 || s.length() == 0) {
					throw new IOException("Attempt to read char, but input stream contains string with length=["+s.length()+"]"); 
				}
				else {
					return s.charAt(0);
				}
			}
		}
		else {
			throw new IOException("Attempt to read char, but input stream content is ["+contentType+"]"); 
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
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.CHAR || contentType == Types.VARCHAR || contentType == Types.LONGVARCHAR || contentType == Types.NVARCHAR || contentType == Types.LONGVARCHAR) {
			return readString();
		}
		else {
			throw new IOException("Attempt to read character strings, but input stream content is ["+contentType+"]"); 
		}
	}

	@Override
	public String readUTF() throws IOException {
		final int	contentType = (int) readBytes(2); 
		
		if (contentType == Types.CHAR || contentType == Types.VARCHAR || contentType == Types.LONGVARCHAR || contentType == Types.NVARCHAR || contentType == Types.LONGVARCHAR) {
			final byte[]	buffer = new byte[(int) readBytes(4)];
			
			if (nested.read(buffer) != buffer.length) {
				throw new EOFException("EOF while reading string content"); 
			}
			else {
				return new String(buffer, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			}
		}
		else {
			throw new IOException("Attempt to read character strings, but input stream content is ["+contentType+"]"); 
		}
	}

	private long readBytes(final int contentSize) throws IOException {
		final byte[] buffer = this.temp;
		
		synchronized (buffer) {
			if (nested.read(buffer,0,contentSize) < contentSize) {
				throw new EOFException("End of file while reading input stream");
			}
			else {
				long	result = 0;
				
				for (int index = contentSize - 1; index >= 0; index--) {
					result = (result >>> 8) | (((long)buffer[index]) << 56);
				}
				result >>= 8 * (8 - contentSize);
					
				return result;
			}
		}
	}
	
	Date readDate(final int sqlType) throws IOException {
		final long		value = readBytes(8);
		
		try{switch (sqlType) {
				case Types.DATE 					: return SQLUtils.convert(Date.class, value);
				case Types.TIME 					: return SQLUtils.convert(Time.class, value);
				case Types.TIMESTAMP				: return SQLUtils.convert(Timestamp.class, value);
				case Types.TIME_WITH_TIMEZONE		: return SQLUtils.convert(Time.class, value);
				case Types.TIMESTAMP_WITH_TIMEZONE	: return SQLUtils.convert(Timestamp.class, value);
				default : throw new UnsupportedOperationException("Not implemented yet");
			}
		} catch (ContentException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		}
	}
	
	Array readArray() throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	String readString() throws IOException {
		final byte[]	buffer = readRaw();
		
		return new String(buffer, PureLibSettings.DEFAULT_CONTENT_ENCODING);
	}
	
	Blob readBlob() throws IOException {
		final byte[]	buffer = readRaw();

		try{return SQLUtils.convert(Blob.class, buffer);
		} catch (ContentException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		}
	}

	Clob readClob(final Class<? extends Clob> clazz) throws IOException {
		try{return SQLUtils.convert(clazz, readString());
		} catch (ContentException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		}
	}

	byte[] readRaw() throws IOException {
		final int		size = (int)readBytes(4);
		final byte[]	buffer = new byte[size];

		if (nested.read(buffer) < size) {
			throw new EOFException("End of file while reading input stream");
		}
		else {
			return buffer;
		}
	}
	
	Object readSerial() throws IOException {
		try(final InputStream	is = new ByteArrayInputStream(readRaw());
			final ObjectInput	ois = new ObjectInputStream(is)) {

			try{return ois.readObject();
			} catch (ClassNotFoundException  e) {
				throw new IOException(e.getLocalizedMessage(), e);
			}
		}
	}
	
	SQLXML readXML() throws IOException {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	BigDecimal readBigDecimal() throws IOException {
		final byte[]	buffer = readRaw();
		
		try{return SQLUtils.convert(BigDecimal.class, new String(buffer, PureLibSettings.DEFAULT_CONTENT_ENCODING));
		} catch (ContentException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		}
	}
}
