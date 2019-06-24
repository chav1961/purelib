package chav1961.purelib.basic;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;

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
 * @see chav1961.purelib.basic.interfaces.LineByLineProcessorCallback ILineByLineProcessorCallback
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.2
 */

public class LineByLineProcessor implements Closeable {
	private final List<DataStack>				pushes = new ArrayList<>();
	private final StringBuilder					sb = new StringBuilder();
	private final LineByLineProcessorCallback	callback;
	private boolean								interruptProcessing = false, insideReaderProcessing = false;
	private int									lineNo = 1;
	private int									displacement = 0;
	
	/**
	 * <p>Create line-by-line processor instance</p>
	 * @param callback callback to process
	 * @throws IllegalArgumentException any parameter's problems
	 */
	public LineByLineProcessor(final LineByLineProcessorCallback callback) {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			this.callback = callback;
		}		
	}
	
	@Override
	public void close() throws IOException {
		closeWriting();
	}
	
	/**
	 * <p>Process the next piece of source data. This method is similar to {@link java.io.Writer#write(char[],int,int) Writer.write(char[],int,int)} method.</p> 
	 * @param cbuf char arrays containing source data
	 * @param off offset inside the source array to use as data beginning
	 * @param len length of data need to process
	 * @throws IOException if any I/O exceptions were detected
	 * @throws SyntaxException if any syntax problems was detected 
	 * @throws IllegalArgumentException any parameter's problems
	 */
	public void write(final char[] cbuf, final int off, final int len) throws IOException, SyntaxException {
		if (cbuf == null || cbuf.length == 0) {
			throw new IllegalArgumentException("Char array can't be null or empty");
		}
		else if (off < 0 || off >= cbuf.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length));
		}
		else if (len <= 0 || off+len > cbuf.length) {
			throw new IllegalArgumentException("Len ["+len+"] out of range 1.."+(cbuf.length-off));
		}
		else {
			uncheckedWrite(cbuf,off,len);
		}
	}

	protected void uncheckedWrite(final char[] cbuf, final int off, final int len) throws IOException, SyntaxException {
		if (sb.length() > 0) {
			for (int index = off, maxIndex = Math.min(cbuf.length,off+len); index < maxIndex; index++) {
				if (cbuf[index] == '\n') {
					sb.append(cbuf,off,index-off+1);
					processFromBuilder();
					if (interruptProcessing) {
						interruptProcessing = false;
						pushes.add(0,new DataStack(lineNo,cbuf,index+1,off+len-index-1));
						lineNo = 1;
						return;
					}
					if (off+len-index-1 > 0) {
						uncheckedWrite(cbuf,index+1,off+len-index-1);
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
					callback.processLine(displacement,lineNo++,cbuf,start,index-start+1);
					displacement += index-start+1;
					if (interruptProcessing) {
						interruptProcessing = false;
						pushes.add(0,new DataStack(lineNo,cbuf,index+1,off+len-index-1));
						lineNo = 1;
						return;
					}
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
	 * @throws SyntaxException if any syntax problems were detected
	 * @throws IllegalArgumentException any parameter's problems
	 */
	public void write(final Reader rdr) throws IOException, SyntaxException {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else if (insideReaderProcessing) {
			throw new IllegalStateException("This call can't be used when pushProcessing() was called! Pop all nested content before");
		}
		else {
			final char[]	buffer = new char[8192];
			int 			len;

			insideReaderProcessing = true;
			try{while ((len = rdr.read(buffer)) > 0) {
					uncheckedWrite(buffer,0,len);
				}
			} finally {
				insideReaderProcessing = false;
			}
		}
	}
	
	/**
	 * <p>Pushes processing on any state and save tail content for longer using. Can be used inside callback to switch source for this class</p> 
	 * @throws IOException is any I/O errors were detected on this call
	 * @throws SyntaxException if any syntax errors were detected in this call
	 * @since 0.0.2
	 */
	public void pushProcessing() throws IOException, SyntaxException {
		interruptProcessing = true;
	}

	/**
	 * <p>Pops processing and return to the tail of previously saved content</p>
	 * @throws IOException is any I/O errors were detected on this call
	 * @throws SyntaxException if any syntax errors were detected in this call
	 * @since 0.0.2
	 */
	public void popProcessing() throws IOException, SyntaxException {
		if (pushes.size() <= 0) {
			throw new IllegalStateException("Attempt to pop empty processing stack"); 
		}
		else {
			final DataStack		item = pushes.remove(0);
			
			closeWriting();			
			lineNo = item.lineNo; 
			write(item.content,0,item.content.length);
		}
	}

	private void closeWriting() throws IOException {
		if (sb.length() > 0) {
			if (sb.charAt(sb.length()-1) != '\n') {
				sb.append('\n');
			}
			try{processFromBuilder();
			} catch (SyntaxException e) {
				throw new IOException(e);
			}
		}
	}
	
	private void processFromBuilder() throws IOException, SyntaxException {
		final char[]	data = new char[sb.length()];
		
		sb.getChars(0,data.length,data,0);
		callback.processLine(displacement,lineNo++,data,0,data.length);
		displacement += data.length;
		sb.setLength(0);
	}
	
	private static class DataStack {
		final int		lineNo;
		final char[]	content;
		
		DataStack(final int lineNo, final char[] content, final int off, final int len) {
			this.lineNo = lineNo;
			this.content = new char[len];
			System.arraycopy(content,off,this.content,0,len);
		}

		@Override
		public String toString() {
			return "DataStack [lineNo=" + lineNo + ", content=" + Arrays.toString(content) + "]";
		}
	}
}
