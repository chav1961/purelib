package chav1961.purelib.concurrent;

import java.io.Closeable;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import chav1961.purelib.basic.growablearrays.GrowableCharArray;

/**
 * <p>This class is used to support I/O channel between two threads. It's functionality is similar to {@linkplain PipedReader}/{@linkplain PipedWriter} pair.
 * This class implements {@linkplain Closeable} interface and can be used in the <b>try-with-resource</b> statements.</p>
 * <p>This class is not reusable</p>
 * @see XStream
 * @see XByteStream
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class XCharStream implements Closeable {
	private static final int			EXCHANGE_TIMEOUT_SECONDS = 1;
	
	private final Object				sync = new Object();
	private final Exchanger<GrowableCharArray<?>>	ex = new Exchanger<>();
	private volatile Thread				sender = null, receiver = null;
	private volatile boolean			senderClosed = false, receiverClosed = false;
	
	/**
	 * <p>Constructor of the class</p>
	 */
	public XCharStream() {
	}

	@Override
	public void close() throws IOException {
		synchronized(sync) {
			senderClosed = true;
			receiverClosed = true;
		}		
	}
	
	/**
	 * <p>Create receiving corner of the channel. Must be called only once</p> 
	 * @return receiving corner of the channel. Can't be null. Must be closed by application
	 * @throws IOException on any I/O errors
	 */
	public Reader createReader() throws IOException {
		synchronized(sync) {
			if (receiver != null) {
				throw new IOException("Attempt to call createReader twice");
			}
			else if (sender != null && sender == Thread.currentThread()) {
				throw new IOException("Attempt to get reader and writer with the same thread!");
			}
			else {
				receiver = Thread.currentThread();
				return new InternalReader();
			}
		}
	}

	/**
	 * <p>Create transmitting corner of the channel. Must be called only once</p> 
	 * @return transmitting corner of the channel. Can't be null. Must be closed by application
	 * @throws IOException on any I/O errors
	 */
	public Writer createWriter() throws IOException {
		synchronized(sync) {
			if (sender != null) {
				throw new IOException("Attempt to call createWriter twice");
			}
			else if (receiver != null && receiver == Thread.currentThread()) {
				throw new IOException("Attempt to get reader and writer with the same thread!");
			}
			else {
				sender = Thread.currentThread();
				return new InternalWriter();
			}
		}
	}
	
	private class InternalReader extends Reader {
		private GrowableCharArray<?>	gca = null;
		private int						cursor = 0;
		
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			if (gca == null || cursor >= gca.length()) {
				for (;;) {
					try{final GrowableCharArray<?>	gcaNew = ex.exchange(gca,EXCHANGE_TIMEOUT_SECONDS,TimeUnit.SECONDS);
					
						gca = gcaNew;
						cursor = 0;
						break;
					} catch (InterruptedException e) {
						receiverClosed = true;
						throw new IOException("Pipe transfer is interrupted");
					} catch (TimeoutException e) {
						if (senderClosed) {
							return -1;
						}
						else if (sender != null && !sender.isAlive()) {
							throw new IOException("Pipe is broken");
						}
					}
				}
			}
			final int	currentLen = Math.min(len,gca.length() - cursor);

			gca.read(cursor,cbuf,off,currentLen);
			cursor += currentLen;
			return currentLen;
		}

		@Override
		public void close() throws IOException {
			synchronized (sync) {
				receiverClosed = true;
				if (gca != null) {
					gca.clear();
					gca = null;
				}
			}
		}
	}
	
	private class InternalWriter extends Writer {
		@SuppressWarnings("rawtypes")
		private GrowableCharArray<?>	gca = new GrowableCharArray(false);

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			gca.append(cbuf,off,off+len);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void flush() throws IOException {
			for (;;) {
				try{final GrowableCharArray<?>	gcaOld = ex.exchange(gca,EXCHANGE_TIMEOUT_SECONDS,TimeUnit.SECONDS);

					if (gcaOld == null) {
						gca = new GrowableCharArray(false);
					}
					else {
						gca = gcaOld;
					}
					break;
				} catch (InterruptedException e) {
					senderClosed = true;
					throw new IOException("Pipe transfer is interrupted");
				} catch (TimeoutException e) {
					if (receiver != null && !receiver.isAlive()) {
						throw new IOException("Pipe is broken");
					}
					else if (receiverClosed) {
						throw new IOException("Pipe is closed by receiver");
					}
				}
			}
		}

		@Override
		public void close() throws IOException {
			synchronized (sync) {
				senderClosed = true;
				if (gca != null) {
					gca.clear();
					gca = null;
				}
			}
		}
	}
}
