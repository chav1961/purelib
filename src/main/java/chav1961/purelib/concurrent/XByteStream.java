package chav1961.purelib.concurrent;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class XByteStream implements Closeable {
	private static final int			EXCHANGE_TIMEOUT_SECONDS = 1;
	
	private final Object				sync = new Object();
	private final Exchanger<GrowableByteArray>	ex = new Exchanger<>();
	private volatile Thread				sender = null, receiver = null;
	private volatile boolean			senderClosed = false, receiverClosed = false;
	
	public XByteStream() {
	}

	@Override
	public void close() throws IOException {
		synchronized(sync) {
			senderClosed = true;
			receiverClosed = true;
		}		
	}
	
	public InputStream createInputStream() throws IOException {
		synchronized(sync) {
			if (receiver != null) {
				throw new IOException("Attempt to call createReader twice");
			}
			else if (sender != null && sender == Thread.currentThread()) {
				throw new IOException("Attempt to get reader and writer with the same thread!");
			}
			else {
				receiver = Thread.currentThread();
				return new InternalInputStream();
			}
		}
	}

	public OutputStream createOutputStream() throws IOException {
		synchronized(sync) {
			if (sender != null) {
				throw new IOException("Attempt to call createWriter twice");
			}
			else if (receiver != null && receiver == Thread.currentThread()) {
				throw new IOException("Attempt to get reader and writer with the same thread!");
			}
			else {
				sender = Thread.currentThread();
				return new InternalOutputStream();
			}
		}
	}
	
	private class InternalInputStream extends InputStream {
		private GrowableByteArray	gca = null;
		private int					cursor = 0;

		@Override
		public int read() throws IOException {
			if (!ensureDataExists()) {
				return -1;
			} 
			else {
				final int	result = gca.read(cursor);
				
				cursor++;
				return result;
			}
		}
		
		@Override
		public int read(byte[] cbuf, int off, int len) throws IOException {
			if (!ensureDataExists()) {
				return -1;
			} 
			else {
				final int	currentLen = Math.min(len,gca.length() - cursor);
	
				gca.read(cursor,cbuf,off,currentLen);
				cursor += currentLen;
				return currentLen;
			}
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
		
		private boolean ensureDataExists() throws IOException {
			if (gca == null || cursor >= gca.length()) {
				for (;;) {
					try{final GrowableByteArray	gcaNew = ex.exchange(gca,EXCHANGE_TIMEOUT_SECONDS,TimeUnit.SECONDS);
					
						gca = gcaNew;
						cursor = 0;
						break;
					} catch (InterruptedException e) {
						receiverClosed = true;
						throw new IOException("Pipe transfer is interrupted");
					} catch (TimeoutException e) {
						if (senderClosed) {
							return false;
						}
						else if (sender != null && !sender.isAlive()) {
							throw new IOException("Pipe is broken");
						}
					}
				}
			}
			return true;
		}
	}
	
	private class InternalOutputStream extends OutputStream {
		private GrowableByteArray	gca = new GrowableByteArray(false);

		@Override
		public void write(int b) throws IOException {
			gca.append((byte)b);
		}
		
		@Override
		public void write(byte[] cbuf, int off, int len) throws IOException {
			gca.append(cbuf,off,off+len);
		}

		@Override
		public void flush() throws IOException {
			for (;;) {
				try{final GrowableByteArray	gcaOld = ex.exchange(gca,EXCHANGE_TIMEOUT_SECONDS,TimeUnit.SECONDS);

					if (gcaOld == null) {
						gca = new GrowableByteArray(false);
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
