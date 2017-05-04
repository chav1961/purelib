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
 * <p>This class implements a SAX-styled JSON parser. Usage of this class is:</p>
 * <code>
 * 		JsonSaxHandler handler = ...;
 * 		new JsonSaxParser(handler).parse(new InputStreamReader(...));
 * </code>
 * <p>JSON format is according to <a href="http://www.rfc-base.org/rfc-7159.html">RFC 7159</a> 
 * <p>This class is specialized for fast JSON parsing, so it's native format in most of cases is a character arrays.
 * To prevent performance degradation, avoid escaped strings in the input</p> 
 * <p>This class is not thread-safe.</p>
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
	private StackTop[]				stack = new StackTop[16];
	private int						stackTop = -1;
	
	public JsonSaxParser(final JsonSaxHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("Handler can't be null");
		}
		else {
			this.handler = handler;
		}
	}

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
			throw new IllegalArgumentException();
		}
	}

	public void parse(final String data) throws IOException, SyntaxException {
		parse(data.toCharArray());
	}
	
	public void parse(final char[] data) throws IOException, SyntaxException {
		parse(data,0,data.length);
	}

	public void parse(final char[] data, final int from, final int len) throws IOException, SyntaxException {
		parse(new CharArrayReader(data,from,len));
	}

	@Override
	public void processLine(int lineNo, char[] data, int from, int length) throws IOException {
		int				current = from, multiplier = 1, temp;
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
								current = stringValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_LAST;
								break;
							case StackTop.STATE_BEFORE_NAME		:
								current = stringName(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_NAME;
								break;
							case StackTop.STATE_BEFORE_VALUE	:
								current = stringValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_VALUE;
								break;
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default : throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
						}
						break;
					case ']'	: 
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
							case StackTop.STATE_BEFORE_VALUE	:
							case StackTop.STATE_AFTER_VALUE		:
								if (stack[stackTop].recordType != '[') {
									throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
						}
						break;
					case 't' : case 'f' :
						switch (stack[stackTop].nameState) {
							case StackTop.STATE_BEFORE_FIRST	:
								current = booleanValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_LAST;
								break;
							case StackTop.STATE_BEFORE_VALUE	:
								current = booleanValue(data,current,length);
								stack[stackTop].nameState = StackTop.STATE_AFTER_VALUE;
								break;
							case StackTop.STATE_BEFORE_NAME		:
							case StackTop.STATE_AFTER_NAME		:
							case StackTop.STATE_AFTER_VALUE		:
							case StackTop.STATE_AFTER_LAST		:
							default : throw new IllegalArgumentException();
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
							default : throw new IllegalArgumentException();
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
		int		temp, locations[] = new int[2];
		
		if ((temp = CharsUtil.parseUnescapedString(data,from+1,'\"',true,locations)) < 0) {
			final StringBuilder	sb = new StringBuilder(); 
			
			temp = CharsUtil.parseString(data,from+1,'\"',sb);
			handler.startName(sb.toString());
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

	private int booleanValue(final char[] data, final int from, final int length) {
		if (CharsUtil.compare(data,from,CONST_TRUE)) {
			handler.value(true);
			return from + CONST_TRUE.length;
		}
		else if (CharsUtil.compare(data,from,CONST_FALSE)) {
			handler.value(false);
			return from + CONST_FALSE.length;
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	private int numericValue(final char[] data, int from, final int length) {
		final long[]	numbers = new long[2];
		final int		multiplier;
		
		if (data[from] == '-') {
			from++;		multiplier = -1;
		}
		else {
			multiplier = 1;
		}
		
		final int		result = CharsUtil.parseNumber(data,from,numbers,CharsUtil.PREF_ANY,true);
		
		switch ((int)numbers[1]) {
			case CharsUtil.PREF_INT 	: handler.value((int)(multiplier * numbers[0])); break;
			case CharsUtil.PREF_LONG 	: handler.value(multiplier * numbers[0]); break;
			case CharsUtil.PREF_DOUBLE	: handler.value(multiplier * Double.longBitsToDouble(numbers[0])); break;
			default : throw new UnsupportedOperationException();
		}
		return result;
	}

	private int stringValue(final char[] data, final int from, final int length) {
		int		temp, locations[] = new int[2];
		
		if ((temp = CharsUtil.parseUnescapedString(data,from+1,'\"',true,locations)) < 0) {
			final StringBuilder	sb = new StringBuilder(); 
			
			temp = CharsUtil.parseString(data,from+1,'\"',sb);
			handler.value(sb.toString());
		}
		else {
			handler.value(data,locations[0],locations[1]-locations[0]+1);
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