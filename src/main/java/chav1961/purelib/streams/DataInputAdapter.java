package chav1961.purelib.streams;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;

/**
 * <p>This class is an empty implementation of {@linkplain DataInput} interface.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class DataInputAdapter implements DataInput {
	@Override
	public void readFully(byte[] b) throws IOException {
		throw new EOFException();
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		throw new EOFException();
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return n;
	}

	@Override
	public boolean readBoolean() throws IOException {
		throw new EOFException();
	}

	@Override
	public byte readByte() throws IOException {
		throw new EOFException();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		throw new EOFException();
	}

	@Override
	public short readShort() throws IOException {
		throw new EOFException();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		throw new EOFException();
	}

	@Override
	public char readChar() throws IOException {
		throw new EOFException();
	}

	@Override
	public int readInt() throws IOException {
		throw new EOFException();
	}

	@Override
	public long readLong() throws IOException {
		throw new EOFException();
	}

	@Override
	public float readFloat() throws IOException {
		throw new EOFException();
	}

	@Override
	public double readDouble() throws IOException {
		throw new EOFException();
	}

	@Override
	public String readLine() throws IOException {
		throw new EOFException();
	}

	@Override
	public String readUTF() throws IOException {
		throw new EOFException();
	}
}
