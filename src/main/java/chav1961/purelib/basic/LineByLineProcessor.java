package chav1961.purelib.basic;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import chav1961.purelib.basic.interfaces.ILineByLineProcessorCallback;

/**
 * <p>This utility class supports line-by-line processing for the character streams. It parses the input stream data, defines line locations in it, calculates sequential number for the lines found
 * and pass it to callback for longer processing</p> 
 * 
 * <p>The main idea of this class is to reduce data moving when character stream is parsing. <i>Line</i> is any character sequence, terminated with '\n' char. Source data for this class is a lot of of character arrays 
 * are passing to the {@link LineByLineProcessor#write(char[],int,int) write} method of the class. This class parse every character array, find all the lines in them, and sequentially send all the lines found to the callback 
 * for longer processing. If some line is located in more than one input piece of data, this class builds the line from all pieces and also send in to the callback. It's the only case when the class uses any data moving. 
 * In all other cases this class passes the source data directly to the callback.</p>
 * 
 * <p>Class logic guarantees, that all the lines are passing to the callback will be terminated with '\n' char, including the same last line in the source data. Char '\r' also can presents in the callback data as option</p>
 * <p>This class implements {@link java.io.Closeable Closeable} interface, so it can be used in the <b>try-with-resource</b> statements.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see java.io.Writer Writer
 * @see chav1961.purelib.basic.interfaces.ILineByLineProcessorCallback ILineByLineProcessorCallback
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class LineByLineProcessor implements Closeable {
	private final StringBuilder					sb = new StringBuilder();
	private final ILineByLineProcessorCallback	callback;
	private int									lineNo = 1;
	
	/**
	 * <p>Create line-by-line processor instance</p>
	 * @param callback callback to process
	 * @throws IllegalArgumentException any parameter's problems
	 */
	public LineByLineProcessor(final ILineByLineProcessorCallback callback) {
		if (callback == null) {
			throw new IllegalArgumentException("Callback can't be null");
		}
		else {
			this.callback = callback;
		}		
	}
	
	@Override
	public void close() throws IOException {
		if (sb.length() > 0) {
			sb.append('\n');
			processFromBuilder();
		}
	}
	
	/**
	 * <p>Process the next piece of source data. This method is similar to {@link java.io.Writer#write(char[],int,int) Writer.write(char[],int,int)} method.</p> 
	 * @param cbuf char arrays containing source data
	 * @param off offset inside the source array to use as data beginning
	 * @param len length of data need to process
	 * @throws IOException if any I/O exceptions were detected
	 * @throws IllegalArgumentException any parameter's problems
	 */
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		if (cbuf == null || cbuf.length == 0) {
			throw new IllegalArgumentException("Char array can't be null or empty");
		}
		else if (off < 0 || off >= cbuf.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length));
		}
		else if (len <= 0 || off+len > cbuf.length) {
			throw new IllegalArgumentException("Len ["+len+"] out of range 1.."+(cbuf.length-off));
		}
		else if (sb.length() > 0) {
			for (int index = off, maxIndex = Math.min(cbuf.length,off+len); index < maxIndex; index++) {
				if (cbuf[index] == '\n') {
					sb.append(cbuf,off,index-off+1);
					processFromBuilder();
					if (off+len-index-1 > 0) {
						write(cbuf,index+1,off+len-index-1);
					}
					return;
				}
			}
			sb.append(cbuf,off,len);
		}
		else {
			int	start = off;
			
			for (int index = off, maxIndex = Math.min(cbuf.length,off+len); index < maxIndex; index++) {
				if (cbuf[index] == '\n') {
					callback.processLine(lineNo,cbuf,start,index-start+1);
					start = index + 1;
				}
			}
			if (start < off+len) {
				sb.append(cbuf,start,off+len-start);
			}
		}
	}

	/**
	 * <p>Process data from the reader until EOF will be detected</p>
	 * @param rdr reader to process data from
	 * @throws IOException if any I/O exceptions were detected
	 * @throws IllegalArgumentException any parameter's problems
	 */
	public void write(final Reader rdr) throws IOException {
		if (rdr == null) {
			throw new IllegalArgumentException("Readed can't be null");
		}
		else {
			final char[]	buffer = new char[8192];
			int 			len;
			
			while ((len = rdr.read(buffer)) > 0) {
				write(buffer,0,len);
			}
		}
	}
	
	
	private void processFromBuilder() throws IOException {
		final char[]	data = new char[sb.length()];
		
		sb.getChars(0,data.length,data,0);
		sb.setLength(0);
		callback.processLine(lineNo,data,0,data.length);
		lineNo++;
	}
}
