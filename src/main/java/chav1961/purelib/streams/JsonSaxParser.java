package chav1961.purelib.streams;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

import chav1961.purelib.basic.CharsUtil;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;


/**
 * <p>This class implements a SAX-styled JSON parser to use in the in-stream JSON parsing application. Usage of this class is:</p>
 * <code>
 * 		JsonSaxHandler handler = ...;
 * 		new JsonSaxParser(handler).parse(new InputStreamReader(...));
 * </code>
 * <p>JSON format is according to <a href="http://www.rfc-base.org/rfc-7159.html">RFC 7159</a> 
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
 * @see <a href="http://www.rfc-base.org/rfc-7159.html">RFC 7159</a> 
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
	private StackTop[]				stack = new StackTop[16];
	private int						stackTop = -1;
	
	/**
	 * <p>COnstructor of the object</p>
	 * @param handler handler to get all Json events
	 */
	public JsonSaxParser(final JsonSaxHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("Handler can't be null");
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
		stackTop = -1;		handler.startDoc();		push('=');
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor(this)) {
			final char[]	buffer = new char[8192];
			int	len;
			
			while ((len = reader.read(buffer)) > 0) {
				lblp.write(buffer, 0, len);
			}
		}
		
		handler.endDoc(); 	pop();
		if (stackTop != -1) {
			throw new SyntaxException(0,0,"Unclosed brackets or quotas (\") in the and of input");
		}
	}

	/**
	 * <p>Parse JSON stream from string</p>
	 * @param data string with the JSON
	 * @throws IOException on any I/O problems
	 * @throws SyntaxException on syntax problems in the JSON content
	 */
	public void parse(final String data) throws IOException, SyntaxException {
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

	/**
	 * <p>Parse JSON stream from character array</p>
	 * @param data character array with the JSON
	 * @throws IOException on any I/O problems
	 * @throws SyntaxException on syntax problems in the JSON content
	 */
	public void parse(final char[] data) throws IOException, SyntaxException {
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
	public void processLine(final int lineNo, final char[] data, final int from, final int length) throws IOException {
		int				current = from;
		char			symbol;

		for (;;) {
			while ((symbol = data[current]) <= ' ' && symbol != '\n') {
				current++;
			}
			
			if (symbol != '\n') {
				switch (symbol) {
					case '\"'	:
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
								current = stringValue(lineNo,from,data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_LAST;
								break;
							case StackTop.STATE_BEFORE_NAME		:
								current = stringName(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_NAME;
								break;
							case StackTop.STATE_BEFORE_VALUE	:
								current = stringValue(lineNo,from,data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_VALUE;
								break;
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default : 
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (\")"));
						}
						break;
					case '{'	:
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
							case StackTop.STATE_BEFORE_VALUE	:
								push('{');	current++;
								handler.startObj();
								stack[stackTop].nameState = StackTop.STATE_BEFORE_NAME;
								break;
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited ({)"));
						}
						break;
					case '['	:
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
							case StackTop.STATE_BEFORE_VALUE	:
								push('[');	current++;
								handler.startArr();
								handler.startIndex(0);
								stack[stackTop].nameState = StackTop.STATE_BEFORE_VALUE;
								break;
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited ([)"));
						}
						break;
					case '}'	: 
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
								if (stack[stackTop].recordType != '{') {
									throw new IllegalArgumentException();
								}
								else {
									pop();		current++;
									stack[stackTop].nameState =  StackTop.STATE_AFTER_VALUE;
									handler.endObj();
								}
								break;
							case StackTop.STATE_BEFORE_FIRST	:
							case StackTop.STATE_BEFORE_VALUE	:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (})"));
						}
						break;
					case ']'	: 
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
							case StackTop.STATE_BEFORE_VALUE	:
							case StackTop.STATE_AFTER_VALUE		:
								if (stack[stackTop].recordType != '[') {
									throw new IOException(new SyntaxException(lineNo,current-from,"Missing ([)"));
								}
								else {
									pop();		current++;
									stack[stackTop].nameState =  StackTop.STATE_AFTER_VALUE;
									handler.endIndex();
									handler.endArr();
								}
								break;
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (])"));
						}
						break;
					case ':'	: 
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_AFTER_NAME		:
								stack[stackTop].nameState = StackTop.STATE_BEFORE_VALUE;
								current++;
								break;
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_BEFORE_FIRST	:
							case StackTop.STATE_BEFORE_VALUE	:
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (:)"));
						}
						break;
					case ','	: 
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_AFTER_VALUE		:
								if (stack[stackTop].recordType == '[') {
									stack[stackTop].nameState = StackTop.STATE_BEFORE_VALUE;
									handler.endIndex();
									handler.startIndex(++stack[stackTop].indexNo);
								}
								else {
									stack[stackTop].nameState = StackTop.STATE_BEFORE_NAME;
									handler.endName();
								}
								current++;
								break;
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_BEFORE_FIRST	:
							case StackTop.STATE_BEFORE_VALUE	:
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited (,)"));
						}
						break;
					case '-' : case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
								current = numericValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_LAST;
								break;
							case StackTop.STATE_BEFORE_VALUE	:
								current = numericValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_VALUE;
								break;
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,current-from,"Unwaited number"));
						}
						break;
					case 't' : case 'f' :
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
								current = booleanValue(lineNo,from,data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_LAST;
								break;
							case StackTop.STATE_BEFORE_VALUE	:
								current = booleanValue(lineNo,from,data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_VALUE;
								break;
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,(from-current),"Unwaited boolean"));
						}
						break;
					case 'n'	:
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
								current = nullValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_LAST;
								break;
							case StackTop.STATE_BEFORE_VALUE	:
								current = nullValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_VALUE;
								break;
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default :
								throw new IOException(new SyntaxException(lineNo,(from-current),"Unwaited null"));
						}
						break;
					default : 
				}
				symbol = data[current];
			}
			else {
				break;
			}
		}
	}

	private int stringName(final char[] data, final int from, final int length) {
		int		temp;
		
		if ((temp = CharsUtil.parseUnescapedString(data,from+1,'\"',true,locations)) < 0) {
			final StringBuilder	sb = new StringBuilder(); 
			
			temp = CharsUtil.parseString(data,from+1,'\"',sb);
			handler.startName(sb.toString().toCharArray(),0,sb.length());
		}
		else {
			handler.startName(data,locations[0],locations[1]-locations[0]+1);
		}
		return temp;
	}
	
	private int nullValue(final char[] data, final int from, final int length) {
		if (CharsUtil.compare(data,from,CONST_NULL)) {
			handler.value();
			return from + CONST_NULL.length;
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	private int booleanValue(final int lineNo, final int startLine, final char[] data, final int from, final int length) throws IOException {
		if (CharsUtil.compare(data,from,CONST_TRUE)) {
			handler.value(true);
			return from + CONST_TRUE.length;
		}
		else if (CharsUtil.compare(data,from,CONST_FALSE)) {
			handler.value(false);
			return from + CONST_FALSE.length;
		}
		else {
			throw new IOException(new SyntaxException(lineNo, from-startLine, "Unknown boolean value"));
		}
	}

	private int numericValue(final char[] data, int from, final int length) {
		final int		multiplier;
		
		if (data[from] == '-') {
			from++;		multiplier = -1;
		}
		else {
			multiplier = 1;
		}
		
		final int		result = CharsUtil.parseNumber(data,from,numbers,CharsUtil.PREF_ANY,true);
		
		switch ((int)numbers[1]) {
			case CharsUtil.PREF_INT 	:
				handler.value(multiplier * (int)numbers[0]); 
				break;
			case CharsUtil.PREF_LONG 	:
				handler.value(multiplier * numbers[0]); 
				break;
			case CharsUtil.PREF_FLOAT	: 
				handler.value(multiplier * Float.intBitsToFloat((int)numbers[0]));
				break;
			case CharsUtil.PREF_DOUBLE	: 
				handler.value(multiplier * Double.longBitsToDouble(numbers[0]));
				break;
			default : throw new UnsupportedOperationException("Unknown preferred type from CharsUtil...");
		}
		return result;
	}

	private int stringValue(final int lineNo, final int start,final char[] data, final int from, final int length) throws IOException {
		int		temp, locations[] = new int[2];
		
		try{if ((temp = CharsUtil.parseUnescapedString(data,from+1,'\"',true,locations)) < 0) {
				final StringBuilder	sb = new StringBuilder(); 
				
				temp = CharsUtil.parseString(data,from+1,'\"',sb);
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

	private StackTop pop() {
		if (stackTop < 0) {
			throw new IllegalArgumentException();
		}
		else {
			return stack[stackTop--];
		}
	}

	private void push(final char record) {
		if (stackTop >= stack.length) {
			final StackTop[]	newStack = new StackTop[stack.length + 16];
			
			System.arraycopy(stack, 0, newStack, 0, stack.length);
			stack = newStack;
		}
		if (stackTop == 0) {
			stack[stackTop].recordType = 'x';
		}
		stack[++stackTop] = new StackTop(record);
	}
	
	private static class StackTop {
		private static final int	STATE_BEFORE_FIRST = 0;
		private static final int	STATE_BEFORE_NAME = 1;
		private static final int	STATE_AFTER_NAME = 2;
		private static final int	STATE_BEFORE_VALUE = 4;
		private static final int	STATE_AFTER_VALUE = 5;
		private static final int	STATE_AFTER_LAST = 6;
		
		char	recordType;
		int		indexNo = 0;
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