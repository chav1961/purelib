package chav1961.purelib.concurrent;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Array;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>This class is used for inter-thread communications. Differ to {@linkplain PipedInputStream}/{@linkplain PipedOutputStream} pair,
 * it doesn't support bytes for exchange, but only one of predefined content (either primitive types or {@linkplain String} class).
 * To support predefined content, class implements {@linkplain DataInput} and {@linkplain DataOutput} interfaces.
 * Type of the predefined content passes as parameter in the class constructor. Attempt to send/receive content differ to passed type
 * will throw {@linkplain IOException}. Use of predefined types excludes packing/unpacking content during transmission and increase 
 * exchanging performance.</p>
 * <p>To start communications, sender thread must use <b>try-with-resource</b> block to create class instance. Receiver thread must use
 * this instance to receive content. Both sending and receiving threads must remain the same during all the communication, attempt to send
 * or receive data with any third thread will crush class functionality.</p>
 * <p>Example to use the class is:</p>
 * <pre>
 * // Sender thread:
 * try(final DataPipe dp = new DataPipe(float.class)) {
 *    ...
 *    dp.writeFloat(100);
 *    ...
 *    dp.flush();
 * }
 * // Receiver thread:
 *    try {
 *       ...
 *       float x = dp.readFloat();
 *       ...
 *    } catch (EOFException exc) {
 *       ...
 *    }
 * </pre>
 * <p>Class if not thread-safe. Neither sending thread can use reading methods nor receiving thread can use writing methods of the class.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class DataPipe implements DataOutput, DataInput, Flushable, Closeable {
	private static final int		DEFAULT_BUFFER_SIZE = 8192;
	private static final int		EXCHANGE_TIMEOUT = 100;
	
	private final Exchanger<Object>	ex = new Exchanger<>();
	private volatile AtomicBoolean	closed = new AtomicBoolean();
	private volatile AtomicBoolean	sendError = new AtomicBoolean();
	private volatile AtomicBoolean	recvError = new AtomicBoolean();
	private final Class<?>			dataType;
	private final int				bufferSize;
	private volatile Content		toTransmit = null; 
	private volatile Content		received = null;
	private volatile Notifier		notifierSent = null;
	private volatile Notifier		notifierReceived = null;

	public DataPipe(final Class<?> dataType) {
		this(dataType, DEFAULT_BUFFER_SIZE);
	}
	
	public DataPipe(final Class<?> dataType, final int bufferSize) {
		if (dataType == null) {
			throw new NullPointerException("Data type can't be null"); 
		}
		else if (!(dataType.isPrimitive() || dataType.isAssignableFrom(String.class))){
			throw new IllegalArgumentException("Data type must describe primitive type or String.class only, but it describes ["+dataType.getName()+"]"); 
		}
		else if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] must be greater than 0");
		}
		else {
			this.dataType = dataType;
			this.bufferSize = bufferSize;
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed.getAndSet(true)) {
			sendError.set(true);
			recvError.set(true);
		}
	}

	@Override
	public void flush() throws IOException {
		ensureNotClosed();
		sendAndWait();
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Buffer offset ["+off+"] out of range 0.."+(b.length-1)); 
		}
		else if (off + len <= 0 || off + len > b.length) {
			throw new IllegalArgumentException("Buffer offset + length ["+(off+len)+"] out of range 1.."+b.length); 
		}
		else {
			ensureNotClosed();
			ensureOperationIsValid(byte.class);
			ensureContentIsAvailable();
			for(int index = 0; index < len; index++) {
				b[off+index] = readByte();
			}
		}
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		throw new IOException("Skipping bytes is not supported"); 
	}

	@Override
	public boolean readBoolean() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(boolean.class);
		ensureContentIsAvailable();
		final boolean 	result = Array.getBoolean(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public byte readByte() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(byte.class);
		ensureContentIsAvailable();
		final byte 	result = Array.getByte(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public int readUnsignedByte() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(byte.class);
		ensureContentIsAvailable();
		final byte 	result = (byte) (Array.getByte(received.content, received.contentIndex++) & 0xFF);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public short readShort() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(short.class);
		ensureContentIsAvailable();
		final short 	result = Array.getShort(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(short.class);
		ensureContentIsAvailable();
		final short 	result = (short) (Array.getShort(received.content, received.contentIndex++) & 0xFFFF);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public char readChar() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(char.class);
		ensureContentIsAvailable();
		final char 	result = Array.getChar(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public int readInt() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(int.class);
		ensureContentIsAvailable();
		final int 	result = Array.getInt(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public long readLong() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(long.class);
		ensureContentIsAvailable();
		final long 	result = Array.getLong(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public float readFloat() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(float.class);
		ensureContentIsAvailable();
		final float 	result = Array.getFloat(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public double readDouble() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(double.class);
		ensureContentIsAvailable();
		final double 	result = Array.getDouble(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public String readLine() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(char.class);
		ensureContentIsAvailable();
		final StringBuilder sb = new StringBuilder();
		char	symbol;
		
		while ((symbol = readChar()) != '\n') {
			sb.append(symbol);
		}
		ensureReadCompleted();
		return sb.toString();
	}

	@Override
	public String readUTF() throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(String.class);
		ensureContentIsAvailable();
		final String 	result = (String)Array.get(received.content, received.contentIndex++);
		
		ensureReadCompleted();
		return result;
	}

	@Override
	public void write(final int b) throws IOException {
		writeByte(b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Buffer offset ["+off+"] out of range 0.."+(b.length-1)); 
		}
		else if (off + len <= 0 || off + len > b.length) {
			throw new IllegalArgumentException("Buffer offset + length ["+(off+len)+"] out of range 1.."+b.length); 
		}
		else {
			ensureNotClosed();
			ensureOperationIsValid(byte.class);
			for(int index = 0; index < len; index++) {
				writeByte(b[off+index]);
			}
		}
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(boolean.class);
		ensureEnoughSpace();
		Array.setBoolean(toTransmit.content, toTransmit.contentIndex++, v);
	}

	@Override
	public void writeByte(final int v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(byte.class);
		ensureEnoughSpace();
		Array.setByte(toTransmit.content, toTransmit.contentIndex++, (byte)v);
	}

	@Override
	public void writeShort(final int v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(short.class);
		ensureEnoughSpace();
		Array.setShort(toTransmit.content, toTransmit.contentIndex++, (short)v);
	}

	@Override
	public void writeChar(final int v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(char.class);
		ensureEnoughSpace();
		Array.setChar(toTransmit.content, toTransmit.contentIndex++, (char)v);
	}

	@Override
	public void writeInt(final int v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(int.class);
		ensureEnoughSpace();
		Array.setInt(toTransmit.content, toTransmit.contentIndex++, v);
	}

	@Override
	public void writeLong(final long v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(long.class);
		ensureEnoughSpace();
		Array.setLong(toTransmit.content, toTransmit.contentIndex++, v);
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(float.class);
		ensureEnoughSpace();
		Array.setFloat(toTransmit.content, toTransmit.contentIndex++, v);
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		ensureNotClosed();
		ensureOperationIsValid(double.class);
		ensureEnoughSpace();
		Array.setDouble(toTransmit.content, toTransmit.contentIndex++, v);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to wrie can't be null");
		}
		else {
			ensureNotClosed();
			ensureOperationIsValid(byte.class);
			for(byte item : s.getBytes()) {
				writeByte(item);
			}
		}
	}

	@Override
	public void writeChars(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to write can't be null");
		}
		else {
			ensureNotClosed();
			ensureOperationIsValid(char.class);
			for(char item : s.toCharArray()) {
				writeByte(item);
			}
		}
		
	}

	@Override
	public void writeUTF(final String s) throws IOException {
		if (s == null) {
			throw new NullPointerException("String to wrie can't be null");
		}
		else {
			ensureNotClosed();
			ensureOperationIsValid(String.class);
			ensureEnoughSpace();
			Array.set(toTransmit.content, toTransmit.contentIndex++, s);
		}
	}
	
	public Class<?> getTypeSupported() {
		return dataType;
	}

	private void ensureOperationIsValid(final Class<?> operation) {
		if (operation != dataType) {
			throw new IllegalStateException("Attempt to use ["+operation.getName()+"] class, but only ["+dataType.getName()+"] is avaiable for the instance");
		}
	}
	
	private void sendAndWait() throws IOException {
		toTransmit.contentSize = toTransmit.contentIndex;
		toTransmit.contentIndex = 0;
		for(;;) {
			try {
				notifierReceived = (Notifier) ex.exchange(toTransmit, EXCHANGE_TIMEOUT, TimeUnit.MILLISECONDS);
				System.err.println("Transmit end");
				break;
			} catch (InterruptedException e) {
				close();
				Thread.currentThread().interrupt();
				throw new IOException("Sending thread was interrupted");
			} catch (TimeoutException e) {
				if (!notifierReceived.thread.isAlive()) {
					close();
					throw new IOException("Sending thread was terminated");
				}
			}
		}
	}

	private void waitAndReceive() throws IOException {
		if (closed.get()) {
			throw new EOFException();
		}
		else {
			notifierSent = new Notifier(Thread.currentThread(), received);
			
			for(;;) {
				try {
					received = (Content) ex.exchange(notifierSent, EXCHANGE_TIMEOUT, TimeUnit.MILLISECONDS);
					System.err.println("Receive end");
					break;
				} catch (InterruptedException e) {
					close();
					Thread.currentThread().interrupt();
					throw new IOException("Receiver thread was interrupted");
				} catch (TimeoutException e) {
					ensureNotClosed();
					if (received != null && !received.thread.isAlive()) {
						close();
						throw new EOFException();
					}
				}
			}
		}
	}

	private void ensureNotClosed() throws IOException {
		if (closed.get()) {
			throw new IOException("Pipe was closed, all sequential operations will be failed");
		}
	}
	
	private void ensureContentIsAvailable() throws IOException {
		if (recvError.get()) {
			throw new IOException("There was I/O error in the pipe or pipe was closed, all sequential operations will be failed");
		}
		else {
			if (received == null || received.contentIndex >= received.contentSize) {
				waitAndReceive();
			}
		}
	}

	private void ensureEnoughSpace() throws IOException { 
		if (sendError.get()) {
			throw new IOException("There was I/O error in the pipe or pipe was closed, all sequential operations will be failed");
		}
		else {
			if (toTransmit == null) {
				toTransmit = new Content(Thread.currentThread(), Array.newInstance(dataType, bufferSize));
			}
			if (toTransmit.contentIndex >= toTransmit.contentSize) {
				sendAndWait();
			}
		}
	}
	
	private void ensureReadCompleted() throws IOException {
		if (recvError.get()) {
			throw new IOException("There was I/O error in the pipe or pipe was closed, all sequential operations will be failed");
		}
		else if (notifierSent != null && received.contentIndex >= received.contentSize) {
			notifierSent.latch.countDown();
		}
	}
	
	private static class Content {
		private final Thread	thread;
		private final Object	content;
		private volatile int	contentIndex;
		private volatile int	contentSize;

		public Content(final Thread thread, final Object content) {
			this.thread = thread;
			this.content = content;
			this.contentIndex = 0;
			this.contentSize = Array.getLength(content);
		}
	}
	
	private static class Notifier {
		private final Thread			thread;
		private final CountDownLatch	latch = new CountDownLatch(1);
		private volatile Content		lastContent;

		public Notifier(final Thread thread, final Content lastContent) {
			this.thread = thread;
			this.lastContent = lastContent;
		}
	}
}
