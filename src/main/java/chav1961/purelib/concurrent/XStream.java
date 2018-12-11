package chav1961.purelib.concurrent;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>This class implements reader/writer stream pair for thread-to-thread communications. It's a light-weight replacement for {@link java.io.PipedReader}/{@link java.io.PipedWriter} pairs. Differ
 * to these streams, methods of XStream streams are not synchronized, and <i>bulk</i> data transfer is used everywhere, so this class is optimized for bulk data transfer.</p>
 * <p>Example of using this class is:</p>
 * <code>
 * try(final XStream xs = new XStream()) {<br>
 * 		. . .<br>
 * 		Thread t1 = new Thread(new Runnable(){<br>
 * 						public void run(){<br>
 * 							try(final Writer wr = xs.getWriter()) {
 * 								. . .<br>
 * 								wr.write("test string".toCharArray());<br>
 * 								. . .<br>
 * 							}
 * 						}<br>
 * 					}<br>
 * 		);<br>
 * 		. . .<br>
 * 		Thread t1 = new Thread(new Runnable(){<br>
 * 						public void run(){<br>
 * 							try(final Reader rdr = xs.getReader()) {
 * 								. . .<br>
 * 								char [] buffer = ...<br>
 * 								int	len = rdr.read(buffer);<br>
 * 								. . .<br>
 * 							}
 * 						}<br>
 * 					}<br>
 * 		);<br>
 * 		. . .<br>
 * }<br>   
 * </code>   
 * <p>Note, that writer part of the class automatically flushes when the '\n' char is appeared in the output stream.</p>
 *
 * @see chav1961.purelib.concurrent JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class XStream implements Closeable {
	private final StringBuilder			sb = new StringBuilder(); 
	private final Exchanger<ExContent>	ex = new Exchanger<>(); 
	private final Reader				reader = new ReaderImpl();
	private final Writer				writer = new WriterImpl();
	private ExContent					contentWrite = new ExContent(), contentRead = new ExContent();

	public XStream() {
		
	}
	
	/**
	 * <p>Get reader part of the stream</p> 
	 * @return reader part of the stream. Subsequent calls return the same reader instance
	 */
	public Reader getReader() {
		return reader;
	}
	
	/**
	 * <p>Get writer part of the stream</p>
	 * @return writer part of the stream. Subsequent calls return the same writer instance 
	 */
	public Writer getWriter() {
		return writer;
	}

	@Override
	public void close() throws IOException {
		reader.close();
		writer.close();
	}

	private class ReaderImpl extends Reader {
		private boolean		closed = false;
		
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			if (cbuf == null) {
				throw new NullPointerException("Buffer to write can't be null");
			}
			else if (off < 0 || off >= cbuf.length) {
				throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length-1));
			}
			else if (off+len-1 < 0 || off+len-1 >= cbuf.length) {
				throw new IllegalArgumentException("Offset + length ["+(off+len-1)+"] out of range 0.."+(cbuf.length-1));
			}
			else if (contentRead == null) {
				return -1;
			}
			else {
				int		currentLen = Math.min(len,contentRead.currentLen-contentRead.from);
				
				System.arraycopy(contentRead.buffer,contentRead.from,cbuf,off,currentLen);
				contentRead.from += currentLen;
				
				if (currentLen == len) {
					return len;
				}
				else {
					try{if ((contentRead = ex.exchange(contentRead)) == null) {
							return currentLen;
						}
						else {
							return currentLen + read(cbuf,off+currentLen,len-currentLen);
						}
					} catch (InterruptedException e) {
						throw new IOException("Exchanger stream was interrupted");
					}
				}
			}
		}

		@Override
		public void close() throws IOException {
			if (!closed) {
				try{ex.exchange(null,1,TimeUnit.MILLISECONDS);
				} catch (InterruptedException | TimeoutException e) {
				} finally {
					closed = true;
				}
			}
		}
	}
	
	private class WriterImpl extends Writer {
		private boolean		closed = false;
		
		@Override
		public void write(final char[] cbuf, int off, int len) throws IOException {
			if (cbuf == null) {
				throw new NullPointerException("Buffer to write can't be null");
			}
			else if (off < 0 || off >= cbuf.length) {
				throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length-1));
			}
			else if (off+len < 0 || off+len >= cbuf.length) {
				throw new IllegalArgumentException("Offset + length ["+(off+len)+"] out of range 0.."+(cbuf.length-1));
			}
			else if (closed) {
				throw new IllegalStateException("Attempt to write into closed stream");
			}
			else {
				final int	maxIndex = off + len;
				boolean		theSameFirst = true;
				int			start = off;
				
				for (int index = off; index < maxIndex; index++) {
					if (cbuf[index] == '\n') {
						if (theSameFirst) {
							theSameFirst = false;
							sb.append(cbuf,start,index-start);
							flush();
						}
						else {
							send(cbuf,start,index-start);
						}
						start = index + 1;
					}
				}
				sb.append(cbuf,start,maxIndex-start);
			}
		}

		@Override
		public void flush() throws IOException {
			if (closed) {
				throw new IllegalStateException("Attempt to write into closed stream");
			}
			else {
				send(sb.toString().toCharArray(),0,sb.length());
				sb.setLength(0);
			}
		}

		@Override
		public void close() throws IOException {
			if (!closed) {
				flush();
				try{ex.exchange(null,1,TimeUnit.MILLISECONDS);
				} catch (InterruptedException | TimeoutException e) {
				} finally {
					closed = true;
				}
			}
		}
		
		private void send(final char[] data, int off, int len) throws IOException {
			if (contentWrite.buffer.length < len) {
				contentWrite.buffer = new char[((len + 1023)/1024)*1024];
			}
			System.arraycopy(data,off,contentWrite.buffer,contentWrite.from = 0,contentWrite.currentLen = len);
			try{if ((contentWrite = ex.exchange(contentWrite)) == null) {
					closed = true;
				}			
			} catch (InterruptedException e) {
				closed = true;
			}
		}
	}
	
	private static class ExContent {
		char[]	buffer = new char[1024];
		int		from = 0;
		int		currentLen = 0;
	}
}
