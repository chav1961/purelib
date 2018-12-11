package chav1961.purelib.streams;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.JsonStaxParser.LexType;

/**
 * <p>This class implements a StAX-styled JSON printer to use in the in-stream JSON uploading application.</p>
 * <p>JSON format is according to <a href="https://tools.ietf.org/html/rfc7159">RFC 7159</a></p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7159">RFC 7159</a> 
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class JsonStaxPrinter implements Closeable, Flushable {
	public static final int					DEFAULT_BUFFER_SIZE = 65536;
	public static final int					MINIMAL_BUFFER_SIZE = 8192;

	private static final char[]				NULL_ARRAY = "null".toCharArray();
	private static final char[]				TRUE_ARRAY = "true".toCharArray();
	private static final char[]				FALSE_ARRAY = "false".toCharArray();
	private static final char				STRING_TERMINATOR = '\"';
	private static final char				ARRAY_STARTER = '[';
	private static final char				ARRAY_TERMINATOR = ']';
	private static final char				OBJ_STARTER = '{';
	private static final char				OBJ_TERMINATOR = '}';
	private static final char				LIST_SPLITTER = ',';
	private static final char				NAME_SPLITTER = ':';

	private static final byte				VALUE_AWAITING = 0;
	private static final byte				NAME_AWAITING = 1;
	private static final byte				SPLITTER_AWAITING = 2;
	
	private final Writer					writer;
	private final int						bufferSize;
	private final SyntaxTreeInterface<?>	tree;
	private boolean							closed = false;
	private char[]							tempString = new char[MINIMAL_BUFFER_SIZE];
	private char[]							buffer;
	private char[]							pseudoStack = new char[64];
	private byte[]							pseudoStackState = new byte[64];
	private int								bufferFill = 0, pseudoStackFill = 0;

	/**
	 * <p>Constructor of the class</p>
	 * @param writer writer to pass JSON content to
	 */
	public JsonStaxPrinter(final Writer writer) {
		this(writer,DEFAULT_BUFFER_SIZE);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param writer writer to pass JSON content to
	 * @param tree tree to keep field names inside the JSON
	 */
	public JsonStaxPrinter(final Writer writer, final SyntaxTreeInterface<?> tree) {
		this(writer,DEFAULT_BUFFER_SIZE,tree);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param writer writer to pass JSON content to
	 * @param bufferSize size of the buffer to write content
	 */
	public JsonStaxPrinter(final Writer writer, final int bufferSize) {
		this(writer,bufferSize,null);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param writer writer to pass JSON content to
	 * @param bufferSize size of the buffer to write content
	 * @param tree tree to keep field names inside the JSON
	 */
	public JsonStaxPrinter(final Writer writer, final int bufferSize, final SyntaxTreeInterface<?> tree) {
		if (writer == null) {
			throw new NullPointerException("Writer can't be null");
		}
		else if (bufferSize <= MINIMAL_BUFFER_SIZE) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] need be at least "+MINIMAL_BUFFER_SIZE);
		}
		else {
			this.writer = writer;
			this.buffer = new char[bufferSize];
			this.bufferSize = bufferSize;
			this.tree = tree;
			this.pseudoStack[0] = ' ';
			this.pseudoStackState[0] = VALUE_AWAITING;
		}
	}

	@Override
	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Attempt to flush closed printer");
		}
		else {
			if (bufferFill > 0) {
				flushBuffer();
			}
			writer.flush();
		}
	}
	
	@Override
	public void close() throws IOException {
		if (!closed) {
			flush();
			if (pseudoStackFill > 0) {
				throw new IOException("Unclosed object/array pairs in the stream: "+new String(pseudoStack,0,pseudoStackFill));
			}
			else {
				closed = true;
				tempString = null;
				buffer = null;
				pseudoStack = null;
			}
		}
	}

	/**
	 * <p>Reset prinet context</p>
	 * @throws IOException on any I/O errors
	 */
	public void reset() throws IOException {
		if (closed) {
			throw new IOException("Attempt to reset closed printer");
		}
		else {
			pseudoStackFill = 0;
			this.pseudoStack[0] = ' ';
			this.pseudoStackState[0] = VALUE_AWAITING;
		}
	}
	
	/**
	 * <p>Print boolean value</p>
	 * @param value value to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter value(final boolean value) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (value) {
				final int	valueLen = TRUE_ARRAY.length;
				
				if (bufferFill + valueLen >= bufferSize) {
					flushBuffer();
				}
				System.arraycopy(TRUE_ARRAY,0,buffer,bufferFill,valueLen);
				bufferFill += valueLen;
			}
			else {
				final int	valueLen = FALSE_ARRAY.length;
				
				if (bufferFill + valueLen >= bufferSize) {
					flushBuffer();
				}
				System.arraycopy(FALSE_ARRAY,0,buffer,bufferFill,valueLen);
				bufferFill += valueLen;
			}
			pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print int value</p>
	 * @param value value to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter value(final long value) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			final int	newLen = CharUtils.printLong(buffer,bufferFill,value,true);
			
			if (newLen < 0) {
				flushBuffer();
				CharUtils.printLong(buffer,bufferFill,value,true);
			}
			else {
				bufferFill = newLen;
			}
			pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print real value</p>
	 * @param value value to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter value(final double value) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			final int	newLen = CharUtils.printDouble(buffer,bufferFill,value,true);
			
			if (newLen < 0) {
				flushBuffer();
				CharUtils.printDouble(buffer,bufferFill,value,true);
			}
			else {
				bufferFill = newLen;
			}
			pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print string value</p>
	 * @param value value to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter value(final String value) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			final int	valueLen = value.length();

			if (valueLen < tempString.length) {
				value.getChars(0,valueLen,tempString,0);
				return value(tempString,0,valueLen);
			}
			else {
				return value(value.toCharArray(),0,valueLen);
			}
		}
	}

	/**
	 * <p>Print string content</p>
	 * @param content content to print
	 * @param from start position to print
	 * @param to end position to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter value(final char[] content, final int from, final int to) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			splitter(STRING_TERMINATOR);
			hugeValue(content,from,to);
			splitter(STRING_TERMINATOR);
			pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print array of boolean</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final boolean[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					value(array[index]);
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}

	/**
	 * <p>Print array of byte</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final byte[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					value(array[index]);
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}
	
	/**
	 * <p>Print array of char</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final char[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					splitter(STRING_TERMINATOR);
					splitter(array[index]);
					splitter(STRING_TERMINATOR);
					pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}
	
	/**
	 * <p>Print array of double</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final double[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					value(array[index]);
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}
	
	/**
	 * <p>Print array of float</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final float[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					value(array[index]);
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}

	/**
	 * <p>Print array of int</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final int[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					value(array[index]);
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}

	/**
	 * <p>Print array of long</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final long[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					value(array[index]);
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}

	/**
	 * <p>Print array of short</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final short[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					value(array[index]);
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}

	/**
	 * <p>Print array of String</p>
	 * @param array array to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter array(final String[] array) throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			if (array == null) {
				return nullValue();
			}
			else {
				startArray();
				for (int index = 0, maxIndex = array.length; index < maxIndex; index++) {
					if (index > 0) {
						splitter();
					}
					if (array[index] == null) {
						nullValue();
					}
					else {
						value(array[index]);
					}
				}
				endArray();
				pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
				return this;
			}
		}
	}
	
	/**
	 * <p>Print null value</p>
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter nullValue() throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: value is not awaiting here");
		}
		else {
			final int	valueLen = NULL_ARRAY.length;
			
			if (bufferFill + valueLen >= bufferSize) {
				flushBuffer();
			}
			System.arraycopy(NULL_ARRAY,0,buffer,bufferFill,valueLen);
			bufferFill += valueLen;
			pseudoStackState[pseudoStackFill] = SPLITTER_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print field name</p>
	 * @param name field name to print
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter name(final String name) throws IOException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty");
		}
		else if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackFill == 0 || pseudoStack[pseudoStackFill] == ARRAY_STARTER) {
			throw new IOException("Name outside the object");
		}
		else if (pseudoStackState[pseudoStackFill] != NAME_AWAITING) {
			throw new IOException("Output structure failure: name is not awaiting here");
		}
		else {
			final int	nameLen = name.length();
			
			if (bufferFill + nameLen + 3>= bufferSize) {
				flushBuffer();
			}
			buffer[bufferFill] = STRING_TERMINATOR;
			name.getChars(0,nameLen,buffer,bufferFill+1);
			buffer[bufferFill + 1 + nameLen] = STRING_TERMINATOR;
			buffer[bufferFill + 2 + nameLen] = NAME_SPLITTER;
			bufferFill += nameLen + 3;
			pseudoStackState[pseudoStackFill] = VALUE_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print field name</p>
	 * @param nameId field name Id to print
	 * @return self
	 * @throws IOException on any I/O errors
	 * @throws IllegalStateException attempt to print name without passing {@linkplain SyntaxTreeInterface} instance 
	 */
	public JsonStaxPrinter name(final long nameId) throws IOException, IllegalStateException {
		if (tree == null) {
			throw new IllegalStateException("You doesn't pass tree parameter into the constructor so can't use this method");
		}
		else if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackFill == 0 || pseudoStack[pseudoStackFill] == ARRAY_STARTER) {
			throw new IOException("Name outside the object");
		}
		else if (pseudoStackState[pseudoStackFill] != NAME_AWAITING) {
			throw new IOException("Output structure failure: name is not awaiting here");
		}
		else if (!tree.contains(nameId)) {
			throw new IllegalArgumentException("Name id ["+nameId+"] is missing in the name's tree");
		}
		else {
			final int	nameLen = tree.getNameLength(nameId);
			
			if (bufferFill + nameLen + 3 >= bufferSize) {
				flushBuffer();
			}
			buffer[bufferFill] = STRING_TERMINATOR;
			tree.getName(nameId,buffer,bufferFill+1);
			buffer[bufferFill + 1 + nameLen] = STRING_TERMINATOR;
			buffer[bufferFill + 2 + nameLen] = NAME_SPLITTER;
			bufferFill += nameLen + 3;
			pseudoStackState[pseudoStackFill] = VALUE_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print field name</p>
	 * @param content content containing field name
	 * @param from start position inside the content
	 * @param to end position inside the content
	 * @return elf
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter name(final char[] content, final int from, final int to) throws IOException {
		final int	nameLen;
		
		if (content == null) {
			throw new NullPointerException("Content to write can't be null");
		}
		else if (from < 0 || from > (nameLen = content.length)) {
			throw new IllegalArgumentException("From location ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (to < 0 || to > nameLen) {
			throw new IllegalArgumentException("To location ["+to+"] out of range 0.."+(nameLen-1));
		}
		else if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackFill == 0 || pseudoStack[pseudoStackFill] == ARRAY_STARTER) {
			throw new IOException("Name outside the object");
		}
		else if (pseudoStackState[pseudoStackFill] != NAME_AWAITING) {
			throw new IOException("Output structure failure: name is not awaiting here");
		}
		else {
			if (bufferFill + nameLen >= bufferSize) {
				flushBuffer();
			}
			buffer[bufferFill] = STRING_TERMINATOR;
			System.arraycopy(content,from,buffer,bufferFill+1,nameLen);
			buffer[bufferFill + 1 + nameLen] = STRING_TERMINATOR;
			buffer[bufferFill + 2 + nameLen] = NAME_SPLITTER;
			bufferFill += nameLen + 3;
			pseudoStackState[pseudoStackFill] = VALUE_AWAITING;
			return this;
		}
	}

	/**
	 * <p>Print start of object</p>
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter startObject() throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackFill > 0 && pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: object is not awaiting here");
		}
		else {
			if (pseudoStackFill >= pseudoStack.length) {
				pseudoStack = Arrays.copyOf(pseudoStack,2*pseudoStack.length);
				pseudoStackState = Arrays.copyOf(pseudoStackState,2*pseudoStackState.length);
			}
			pseudoStackFill++;
			pseudoStackState[pseudoStackFill] = NAME_AWAITING;
			splitter(pseudoStack[pseudoStackFill] = OBJ_STARTER);
			return this;
		}
	}

	/**
	 * <p>Print end of object</p>
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter endObject() throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackFill <= 0) {
			throw new IOException("Object/array stack exhausted");
		}
		else if (pseudoStack[pseudoStackFill] != OBJ_STARTER) {
			throw new IOException("Unpaired operation - endArray awaited: "+new String(pseudoStack,0,pseudoStackFill-1));
		}
		else if (pseudoStackFill > 0 && pseudoStackState[pseudoStackFill] == VALUE_AWAITING) {
			throw new IOException("Output structure failure: value awaited here");
		}
		else {
			pseudoStackFill--;
			splitter(OBJ_TERMINATOR);
			return this;
		}
	}
	
	/**
	 * <p>Print start of array</p>
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter startArray() throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackFill > 0 && pseudoStackState[pseudoStackFill] != VALUE_AWAITING) {
			throw new IOException("Output structure failure: array is not awaiting here");
		}
		else {
			if (pseudoStackFill >= pseudoStack.length) {
				pseudoStack = Arrays.copyOf(pseudoStack,2*pseudoStack.length);
				pseudoStackState = Arrays.copyOf(pseudoStackState,2*pseudoStackState.length);
			}
			pseudoStackFill++;
			pseudoStackState[pseudoStackFill] = VALUE_AWAITING;
			splitter(pseudoStack[pseudoStackFill] = ARRAY_STARTER);
			return this;
		}
	}

	/**
	 * <p>Print end of object</p>
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter endArray() throws IOException {
		if (closed) {
			throw new IOException("Attempt to write into closed printer");
		}
		else if (pseudoStackFill <= 0) {
			throw new IOException("Object/array stack exhausted");
		}
		else if (pseudoStack[pseudoStackFill] != ARRAY_STARTER) {
			throw new IOException("Unpaired operation - endObject awaited: "+new String(pseudoStack,0,pseudoStackFill-1));
		}
		else {
			pseudoStackFill--;
			splitter(ARRAY_TERMINATOR);
			return this;
		}
	}

	/**
	 * <p>Print list and/or array splitter</p>
	 * @return self
	 * @throws IOException on any I/O errors
	 */
	public JsonStaxPrinter splitter() throws IOException {
		if (pseudoStackFill == 0 || pseudoStackState[pseudoStackFill] != SPLITTER_AWAITING) {
			throw new IOException("Output structure failure: splitter is not awaiting here");
		}
		else {
			splitter(LIST_SPLITTER);
			pseudoStackState[pseudoStackFill] = pseudoStack[pseudoStackFill] == OBJ_STARTER ? NAME_AWAITING : VALUE_AWAITING; 
			return this;
		}
	}
	
	private void splitter(final char splitter) throws IOException {
		if (bufferFill + 1 >= bufferSize) {
			flushBuffer();
		}
		buffer[bufferFill++] = splitter;
	}
	
	private void flushBuffer() throws IOException {
		writer.write(buffer,0,bufferFill);
		bufferFill = 0;
	}

	private void hugeValue(final char[] content, final int from, final int to) throws IOException {
		final int 	len = to-from;
		
		if (len >= bufferSize) {
			final int	middle = (from + to) >>> 1;
			
			hugeValue(content,from,middle);
			hugeValue(content,middle+1,to);
		}
		else {
			if (bufferFill + len >= bufferSize) {
				flushBuffer();
			}
			final int	newLen = CharUtils.printEscapedCharArray(buffer,bufferFill,content,from,to,true,true);
			
			if (newLen < 0) {
				final int	middle = (from + to) >>> 1;
				
				hugeValue(content,from,middle);
				hugeValue(content,middle+1,to);
			}
			else {
				bufferFill = newLen;
			}
		}
	}
}
