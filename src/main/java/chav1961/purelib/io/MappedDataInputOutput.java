package chav1961.purelib.io;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Flushable;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import chav1961.purelib.basic.PureLibSettings;

public class MappedDataInputOutput implements DataInput, DataOutput, Flushable, Closeable {
	private final MappedByteBuffer	delegate;
	private final Charset			charset;
	private final Consumer<MappedDataInputOutput>	closeConsumer;
	
	public MappedDataInputOutput(final MappedByteBuffer delegate, final Charset charset, final Consumer<MappedDataInputOutput> closeConsumer) {
		if (delegate == null) {
			throw new NullPointerException("Buffer delegate can't be null"); 
		}
		else if (charset == null) {
			throw new NullPointerException("Char set can't be null"); 
		}
		else {
			this.delegate = delegate;
			this.charset = charset;
			this.closeConsumer = closeConsumer;
		}
	}

	public MappedByteBuffer getBuffer() {
		return delegate;
	}
	
	@Override
	public void flush() throws IOException {
		delegate.force();
	}

	@Override
	public void close() throws IOException {
		flush();
		closeConsumer.accept(this);
	}
	
	@Override
	public void write(final int b) throws IOException {
		delegate.put((byte)b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		delegate.put(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		delegate.put(b, off, len);
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		delegate.put(v ? (byte)1 : (byte)0);
	}

	@Override
	public void writeByte(final int v) throws IOException {
		delegate.put((byte)v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		delegate.putShort((short)v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		delegate.putChar((char)v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		delegate.putInt(v);
	}

	@Override
	public void writeLong(final long v) throws IOException {
		delegate.putLong(v);
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		delegate.putFloat(v);
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		delegate.putDouble(v);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null");
		}
		else {
			writeUtf(s.getBytes(charset));
		}
	}

	@Override
	public void writeChars(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null");
		}
		else {
			writeInt(s.length());
			
			for (int index = 0, maxIndex = s.length(); index < maxIndex;  index++) {
				writeChar(s.charAt(index));
			}
		}
	}

	@Override
	public void writeUTF(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null");
		}
		else {
			writeUtf(s.getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING));
		}
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int skipBytes(int n) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean readBoolean() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte readByte() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short readShort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char readChar() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readInt() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readLong() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float readFloat() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double readDouble() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String readLine() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readUTF() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	private void writeUtf(final byte[] content) throws IOException {
		if (content.length > Short.MAX_VALUE) {
			throw new IOException("UTF string can'tbe greater than ["+Short.MAX_VALUE+"] bytes"); 
		}
		else {
			writeShort(content.length);
			write(content);
		}
	}

}
