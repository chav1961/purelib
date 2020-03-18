package chav1961.purelib.streams;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;


/**
 * <p>This class implements a SAX-styled JSON parser to use in the in-stream JSON parsing application. Usage of this class is:</p>
 * <code>
 * 		JsonSaxHandler handler = ...;<br>
 * 		new JsonSaxParser(handler).parse(new InputStreamReader(...));<br>
 * </code>
 * <p>JSON format is according to <a href="https://tools.ietf.org/html/rfc7159">RFC 7159</a> 
 * <p>This class is specialized for fast JSON parsing, so it's native format in most of cases is a character arrays.
 * To prevent performance degradation, avoid escaped strings in the input</p> 
 * <p>This class is not thread-safe.</p>
 * 
 * <p><b>Performance notes:</b></p>
 * <ul>
 * <li>environment: Intel Celeron 1.5GHz 2-core, 64, Windows 8. Windows performance index=3.2. Java SE 1.8-32.</li>
 * <li>testing set: 250 MB in-memory JSON(see test).</li>
 * </ul>
 * <p>Performance result:</p>
 * <ul>
 * <li>parsing speed ~50 MB/second</li> 
 * </ul>
 * 
 * <p>This class is not thread-safe, but all it's parsing methods are reusable</p> 
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7159">RFC 7159</a> 
 * @see chav1961.purelib.streams.interfaces.JsonSaxHandler
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class JsonSaxParser implements LineByLineProcessorCallback {
	private static final char[]		CONST_TRUE = "true".toCharArray();
	private static final char[]		CONST_FALSE = "false".toCharArray();
	private static final char[]		CONST_NULL = "null".toCharArray();

	private final JsonSaxHandler	handler;
	private final int[]				locations = new int[2];
	private final long[]			numbers = new long[2];
	private final StringBuilder		sb = new StringBuilder();
	private StackTop[]				stack = new StackTop[16];
	private int						stackTop = -1;
	
	/**
	 * <p>COnstructor of the object</p>
	 * @param handler handler to get all Json events
	 */
	public JsonSaxParser(final JsonSaxHandler handler) {
		if (handler == null) {
			throw new NullPointerException("Handler can't be null");
		}
		else {
			this.handler = handler;
		}
	}

	/**
	 * <p>Parse JSON stream from the stream reader</p>
	 * @param reader reader to get JSON content from
	 * @throws IOException on any I/O problems
	 * @throws SyntaxException on syntax problems in the JSON content
	 */
	public void parse(final Reader reader) throws IOException, SyntaxException {
		if (reader == null) {
			throw new NullPointerException("Reader to parse from can't be null");
		}
		else {
			try{stackTop = -1;		handler.startDoc();		push('=');
				
				try(final LineByLineProcessor	lblp = new LineByLineProcessor(this)) {
					lblp.write(reader);
				}
				
				handler.endDoc();
				if (stackTop != 0) {
					throw new SyntaxException(0,0,"Unclosed brackets or quotas (\") in the and of input");
				}
			} catch (ContentException exc) {
				throw new SyntaxException(0,0,exc.getMessage(),exc);
			}
		}
	}

	/**
	 * <p>Parse JSON stream from string</p>
	 * @param data string with the JSON
	 * @throws NullPointerException when string to parse is null
	 * @throws IOException on any I/O problems
	 * @throws SyntaxException on syntax problems in the JSON content
	 */
	public void parse(final String data) throws NullPointerException, IOException, SyntaxException {
		if (data == null) {
			throw new NullPointerException("String to parse can't be null"); 
		}
		else {
			try{parse(data.toCharArray());
			} catch (IOException exc) {
				if (exc.getCause() instanceof SyntaxException) {
					throw (SyntaxException)exc.getCause();
				}
				else {
					throw exc;
				}
			}
		}
	}

	/**
	 * <p>Parse JSON stream from character array</p>
	 * @param data character array with the JSON
	 * @throws NullPointerException when array to parse is null
	 * @throws IOException on any I/O problems
	 * @throws SyntaxException on syntax problems in the JSON content
	 */
	public void parse(final char[] data) throws NullPointerException, IOException, SyntaxException {
		if (data == null) {
			throw new NullPointerException("Character array to parse can't be null"); 
		}
		else {
			try{parse(data,0,data.length);
			} catch (IOException exc) {
				if (exc.getCause() instanceof SyntaxException) {
					throw (SyntaxException)exc.getCause();
				}
				else {
					throw exc;
				}
			}
		}
	}

	/**
	 * <p>Parse JSON stream from character array</p>
	 * @param data character array with the JSON
	 * @param from start position in the array to parse
	 * @param len length or the piece to parse
	 * @throws IOException on any I/O problems
	 * @throws SyntaxException on syntax problems in the JSON content
	 */
	public void parse(final char[] data, final int from, final int len) throws IOException, SyntaxException {
		try{parse(new CharArrayReader(data,from,len));
		} catch (IOException exc) {
			if (exc.getCause() instanceof SyntaxException) {
				throw (SyntaxException)exc.getCause();
			}
			else {
				throw exc;
			}
		}
	}

	@Override
	public void processLine(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException {
		final JsonSaxHandler	h = handler;	
		StackTop				topStack = stack[stackTop];
		int						current = from;
		char					symbol;

		try{for (;;) {
				while ((symbol = data[current]) <= ' ' && symbol != '\n') {
					current++;
				}
				
				if (symbol != '\n') {
					switch (symbol) {
						case '\"'	:
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_FIRST	:
									current = stringValue(lineNo,from,data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_LAST;
									break;
								case StackTop.STATE_BEFORE_NAME		:
									current = stringName(data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_NAME;
									break;
								case StackTop.STATE_BEFORE_VALUE	:
									if (topStack.recordType == '[' && topStack.indexNo == -1) {
										h.startIndex(++topStack.indexNo);
									}
									current = stringValue(lineNo,from,data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_VALUE;
									break;
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_VALUE		:
//								case StackTop.STATE_AFTER_LAST		:
								default : 
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (\")"));
							}
							break;
						case '{'	:
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_FIRST	:
								case StackTop.STATE_BEFORE_VALUE	:
									topStack = push('{');
									current++;
									h.startObj();
									topStack.nameState = StackTop.STATE_BEFORE_NAME;
									break;
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_VALUE		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited ({)"));
							}
							break;
						case '['	:
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_FIRST	:
								case StackTop.STATE_BEFORE_VALUE	:
									topStack = push('[');
									current++;
									h.startArr();
									topStack.nameState = StackTop.STATE_BEFORE_VALUE;
									break;
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_VALUE		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited ([)"));
							}
							break;
						case '}'	: 
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_NAME		:
								case StackTop.STATE_AFTER_VALUE		:
									if (topStack.recordType != '{') {
										throw new IllegalArgumentException();
									}
									else {
										topStack = pop();
										current++;
										topStack.nameState =  StackTop.STATE_AFTER_VALUE;
										h.endName();
										h.endObj();
									}
									break;
//								case StackTop.STATE_BEFORE_FIRST	:
//								case StackTop.STATE_BEFORE_VALUE	:
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (})"));
							}
							break;
						case ']'	: 
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_FIRST	:
									if (topStack.recordType != '[') {
										throw new IOException(new SyntaxException(lineNo,current-from,"Missing ([)"));
									}
									else {
										if (topStack.indexNo != -1) {
											h.endIndex();
										}
										topStack = pop();
										current++;
										topStack.nameState =  StackTop.STATE_AFTER_VALUE;
										h.endArr();
									}
									break;
								case StackTop.STATE_BEFORE_VALUE	:
								case StackTop.STATE_AFTER_VALUE		:
									if (topStack.recordType != '[') {
										throw new IOException(new SyntaxException(lineNo,current-from,"Missing ([)"));
									}
									else {
										if (topStack.indexNo != -1) {
											h.endIndex();
										}
										topStack = pop();
										current++;
										topStack.nameState =  StackTop.STATE_AFTER_VALUE;
										h.endArr();
									}
									break;
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (])"));
							}
							break;
						case ':'	: 
							switch (topStack.nameState) {
								case StackTop.STATE_AFTER_NAME		:
									topStack.nameState = StackTop.STATE_BEFORE_VALUE;
									current++;
									break;
//								case StackTop.STATE_AFTER_VALUE		:
//								case StackTop.STATE_BEFORE_FIRST	:
//								case StackTop.STATE_BEFORE_VALUE	:
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (:)"));
							}
							break;
						case ','	: 
							switch (topStack.nameState) {
								case StackTop.STATE_AFTER_VALUE		:
									if (topStack.recordType == '[') {
										topStack.nameState = StackTop.STATE_BEFORE_VALUE;
										h.endIndex();
										h.startIndex(++topStack.indexNo);
									}
									else {
										topStack.nameState = StackTop.STATE_BEFORE_NAME;
										h.endName();
									}
									current++;
									break;
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_BEFORE_FIRST	:
//								case StackTop.STATE_BEFORE_VALUE	:
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (,)"));
							}
							break;
						case '-' : case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_FIRST	:
									current = numericValue(data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_LAST;
									break;
								case StackTop.STATE_BEFORE_VALUE	:
									if (topStack.recordType == '[' && topStack.indexNo == -1) {
										h.startIndex(++topStack.indexNo);
									}
									current = numericValue(data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_VALUE;
									break;
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_VALUE		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited number"));
							}
							break;
						case 't' : case 'f' :
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_FIRST	:
									current = booleanValue(lineNo,from,data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_LAST;
									break;
								case StackTop.STATE_BEFORE_VALUE	:
									if (topStack.recordType == '[' && topStack.indexNo == -1) {
										h.startIndex(++topStack.indexNo);
									}
									current = booleanValue(lineNo,from,data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_VALUE;
									break;
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_VALUE		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,(from-current),"Unwaited boolean"));
							}
							break;
						case 'n'	:
							switch (topStack.nameState) {
								case StackTop.STATE_BEFORE_FIRST	:
									current = nullValue(data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_LAST;
									break;
								case StackTop.STATE_BEFORE_VALUE	:
									if (topStack.recordType == '[' && topStack.indexNo == -1) {
										h.startIndex(++topStack.indexNo);
									}
									current = nullValue(data,current,length);
									topStack.nameState = StackTop.STATE_AFTER_VALUE;
									break;
//								case StackTop.STATE_BEFORE_NAME		:
//								case StackTop.STATE_AFTER_NAME		:
//								case StackTop.STATE_AFTER_VALUE		:
//								case StackTop.STATE_AFTER_LAST		:
								default :
									throw new IOException(new SyntaxException(lineNo,(current-from),"Unwaited null"));
							}
							break;
						default : 
							throw new IOException(new SyntaxException(lineNo,(current-from),"Illegal char (possibly non-quoted name?)"));
					}
					symbol = data[current];
				}
				else {
					break;
				}
			}
		} catch (ContentException exc) {
//			exc.printStackTrace();
			throw new IOException(new SyntaxException(lineNo,(current-from),exc.getMessage(),exc));
		}
	}

	protected int stringName(final char[] data, final int from, final int length) throws ContentException {
		int		temp;
		
		if ((temp = UnsafedCharUtils.uncheckedParseUnescapedString(data,from+1,'\"',true,locations)) < 0) {
			sb.setLength(0);
			try{temp = UnsafedCharUtils.uncheckedParseString(data,from+1,'\"',sb);
			} catch (IOException e) {
				throw new ContentException(e.getLocalizedMessage(),e); 
			}
			handler.startName(sb.toString().toCharArray(),0,sb.length());
		}
		else {
			handler.startName(data,locations[0],locations[1]-locations[0]+1);
		}
		return temp;
	}
	
	protected int nullValue(final char[] data, final int from, final int length) throws ContentException {
		if (UnsafedCharUtils.uncheckedCompare(data,from,CONST_NULL,0,CONST_NULL.length)) {
			handler.value();
			return from + CONST_NULL.length;
		} 
		else {
			throw new IllegalArgumentException();
		}
	}

	protected int booleanValue(final int lineNo, final int startLine, final char[] data, final int from, final int length) throws IOException, ContentException {
		if (UnsafedCharUtils.uncheckedCompare(data,from,CONST_TRUE,0,CONST_TRUE.length)) {
			handler.value(true);
			return from + CONST_TRUE.length;
		}
		else if (UnsafedCharUtils.uncheckedCompare(data,from,CONST_FALSE,0,CONST_FALSE.length)) {
			handler.value(false);
			return from + CONST_FALSE.length;
		}
		else {
			throw new IOException(new SyntaxException(lineNo, from-startLine, "Unknown boolean value"));
		}
	}

	protected int numericValue(final char[] data, int from, final int length) throws ContentException {
		final int		multiplier;
		
		if (data[from] == '-') {
			from++;		multiplier = -1;
		}
		else {
			multiplier = 1;
		}
		
		final int		result = UnsafedCharUtils.uncheckedParseNumber(data,from,numbers,CharUtils.PREF_ANY,true);
		
		switch ((int)numbers[1]) {
			case CharUtils.PREF_INT 	:
				handler.value(multiplier * (int)numbers[0]); 
				break;
			case CharUtils.PREF_LONG 	:
				handler.value(multiplier * numbers[0]); 
				break;
			case CharUtils.PREF_FLOAT	: 
				handler.value(multiplier * Float.intBitsToFloat((int)numbers[0]));
				break;
			case CharUtils.PREF_DOUBLE	: 
				handler.value(multiplier * Double.longBitsToDouble(numbers[0]));
				break;
			default : throw new UnsupportedOperationException("Unknown preferred type from CharsUtil...");
		}
		return result;
	}

	protected int stringValue(final int lineNo, final int start,final char[] data, final int from, final int length) throws IOException, ContentException {
		int		temp;
		
		try{if ((temp = UnsafedCharUtils.uncheckedParseUnescapedString(data,from+1,'\"',true,locations)) < 0) {
				sb.setLength(0);
				temp = UnsafedCharUtils.uncheckedParseString(data,from+1,'\"',sb);
				handler.value(sb.toString().toCharArray(),0,sb.length());
			}
			else {
				handler.value(data,locations[0],locations[1]-locations[0]+1);
			}
		} catch (IllegalArgumentException exc) {
			throw new IOException(new SyntaxException(lineNo,from-start,exc.getMessage()));
		}
		return temp;
	}

	protected StackTop pop() {
		if (stackTop < 0) {
			throw new IllegalArgumentException();
		}
		else {
			return stack[--stackTop];
		}
	}

	protected StackTop push(final char record) {
		if (stackTop >= stack.length) {
			stack = Arrays.copyOf(stack,stack.length + 16);
		}
		if (stackTop == 0) {
			stack[stackTop].recordType = 'x';
		}
		if (stack[++stackTop] != null) {	// Reduce memory allocation
			stack[stackTop].recordType = record;
			stack[stackTop].indexNo = -1;
			stack[stackTop].nameState = StackTop.STATE_BEFORE_FIRST;
		}
		else {
			stack[stackTop] = new StackTop(record); 
		}
		return stack[stackTop];
	}
	
	protected static class StackTop {
		private static final int	STATE_BEFORE_FIRST = 0;
		private static final int	STATE_BEFORE_NAME = 1;
		private static final int	STATE_AFTER_NAME = 2;
		private static final int	STATE_BEFORE_VALUE = 4;
		private static final int	STATE_AFTER_VALUE = 5;
		private static final int	STATE_AFTER_LAST = 6;
		
		char	recordType;
		int		indexNo = -1;
		int		nameState = STATE_BEFORE_FIRST;
		
		public StackTop(final char recordType) {
			this.recordType = recordType;
		}

		@Override
		public String toString() {
			return "StackTop [recordType=" + recordType + ", indexNo=" + indexNo + ", nameState=" + nameState + "]";
		}
	}

}