package chav1961.purelib.concurrent;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Exchanger;

import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class PipedByteStream implements Closeable {
	public static final int				DEFAULT_PIPE_BUFFER_SIZE = 8192;
	
	private final int					pipeBufferSize;
	private final Exchanger<GrowableByteArray>	ex = new Exchanger<>();	
	private final InputStream			in;
	private final OutputStream			out;
	
	public PipedByteStream() {
		this(DEFAULT_PIPE_BUFFER_SIZE);
	}

	public PipedByteStream(final int pipeBufferSize) {
		if (pipeBufferSize <= 0) {
			throw new IllegalArgumentException("Pipe buffer size must be positive"); 
		}
		else {
			this.pipeBufferSize = pipeBufferSize;
			this.out = new PBOutputStream();
			this.in = new PBInputStream();
		}
	}
	
	@Override
	public void close() throws IOException {
		this.in.close();
		this.out.close();
	}

	public InputStream getInputStream() throws IOException {
		return in;
	}
	
	public OutputStream getOutputStream() throws IOException {
		return out;
	}
	
	public class PBOutputStream extends OutputStream {
		private GrowableByteArray	gba = new GrowableByteArray(false);
		
		PBOutputStream() {
		}
		
		@Override
		public void write(int b) throws IOException {
			write(new byte[]{(byte)b});
		}
		
		@Override
		public void write(byte[] buffer, int off, int len) throws IOException {
			if (buffer == null) {
				throw new NullPointerException("Buffer to write can't be null"); 
			}
			else if (off < 0 || off >= buffer.length) {
				throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(buffer.length-1)); 
			}
			else if (off+len < 0 || off+len >= buffer.length) {
				throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range 0.."+(buffer.length-1)); 
			}
			else if (gba != null) {
				gba.append(buffer,off,off+len);
				if (gba.length() >= pipeBufferSize) {
					swap();
				}
			}
			else {
				throw new IOException("I/O error: receiving corner is closed"); 
			}
		}
		
		@Override
		public void flush() throws IOException {
			if (gba != null) {
				swap();
			}
		}

		private void swap() throws IOException {
			try{gba = ex.exchange(gba);
			} catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				throw new IOException("I/O error: transmitting thread was interrupted");
			}
		}
	}
	
	public class PBInputStream extends InputStream {
		private GrowableByteArray	gba = new GrowableByteArray(false);
		private int					read = 0;
		
		PBInputStream() {
			
		}

		@Override
		public int read() throws IOException {
			final byte[]	buffer = new byte[1];
			final int		ret = read(buffer);
			
			if (ret <= 0) {
				return ret;
			}
			else {
				return buffer[0];
			}
		}

		@Override
		public int read(final byte[] buffer, final int off, final int len) throws IOException {
			if (buffer == null) {
				throw new NullPointerException("Buffer to read can't be null"); 
			}
			else if (off < 0 || off >= buffer.length) {
				throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(buffer.length-1)); 
			}
			else if (off+len < 0 || off+len >= buffer.length) {
				throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range 0.."+(buffer.length-1)); 
			}
			else if (gba != null) {
				if (read >= gba.length()) {
					swap();
					return read(buffer,off,len);
				}
				else {
					final int	minLen = Math.min(gba.length()-read,len);
					
					gba.read(read,buffer,off,off+len);
					read += minLen;
					return minLen;
				}
			}
			else {
				return -1;
			}
		}

		private void swap() throws IOException {
			try{gba = ex.exchange(gba);
				read = 0;
			} catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				throw new IOException("I/O error: receiving thread was interrupted");
			}
		}
	}
}
