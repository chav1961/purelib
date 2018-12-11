package chav1961.purelib.streams;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

/**
 * <p>This class implements a StAX-styled JSON parser to use in the in-stream JSON parsing application. Usage of this class is:</p>
 * <code>
 * try(final JsonSaxParser parser = new JsonSaxParser(&lt;any reader&gt;))){<br>
 * for (LexType lex : parser) {<br>
 * // Process content
 * ...<br>
 * }<br>
 * }<br>
 * </code>
 * <p>JSON format is according to <a href="https://tools.ietf.org/html/rfc7159">RFC 7159</a></p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7159">RFC 7159</a> 
 * @see LexType
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class JsonStaxParser implements Iterable<JsonStaxParser.LexType>, Iterator<JsonStaxParser.LexType>, Closeable {
	public static final int					DEFAULT_BUFFER_SIZE = 65536;
	public static final int					MINIMAL_BUFFER_SIZE = 8192;

	private static final char[]				TRUE_VALUE = "true".toCharArray();
	private static final char[]				FALSE_VALUE = "false".toCharArray();
	private static final char[]				NULL_VALUE = "null".toCharArray();
	
	private static final int				NAME_AWAITED = 0;
	private static final int				NAME_SPLITTER_AWAITED = 1;
	private static final int				VALUE_AWAITED = 2;
	private static final int				LIST_SPLITTER_AWAITED = 3;

	/**
	 * <p>This enumeration describes types of JSON lexemas in the input stream</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public enum LexType {
		START_OBJECT,	// { 
		END_OBJECT,		// }
		START_ARRAY, 	// [
		END_ARRAY,		// ]
		LIST_SPLITTER, 	// ,
		NAME_SPLITTER,	// :
		BOOLEAN_VALUE, 	// true/false
		INTEGER_VALUE, 	// 123
		REAL_VALUE, 	// 123.456e-7
		STRING_VALUE, 	// "text"
		NULL_VALUE,		// null
		NAME,			// "fieldName"
		ERROR			// any unclassified content
	}
	
	private final Reader					rdr;
	private final int						bufferSize;
	private final SyntaxTreeInterface<?>	tree;
	private final int[]						bounds = new int[2];	
	private final long						forParsedLong[] = new long[1];
	private final double					forParsedDouble[] = new double[1];
	private final StringBuilder				sb = new StringBuilder(), sbResult = new StringBuilder(); 
	private boolean							closed = false, parsedBoolean, shortString, escapedString;
	private char[]							buffer, pseudoStack = new char[64];
	private int[]							awaitedLex = new int[64];
	private int								cursor = 0, currentLen, currentRow = 0, currentCol = 0, pseudoStackDepth = 0;
	private LexType							currentLex = null;
	private Exception						detected = null;

	/**
	 * <p>Constructor of the class</p>
	 * @param reader reader to read JSON content from
	 * @throws IOException on any errors
	 */
	public JsonStaxParser(final Reader reader) throws IOException {
		this(reader,DEFAULT_BUFFER_SIZE);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param reader reader to read JSON content from
	 * @param bufferSize size of the buffer to read piece of data from reader
	 * @throws IOException on any errors
	 */
	public JsonStaxParser(final Reader reader, final int bufferSize) throws IOException {
		this(reader,bufferSize,null);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param reader reader to read JSON content from
	 * @param tree tree to keep available field names in the JSON 
	 * @throws IOException on any errors
	 */
	public JsonStaxParser(final Reader reader, final SyntaxTreeInterface<?> tree) throws IOException {
		this(reader,DEFAULT_BUFFER_SIZE,tree);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param reader reader to read JSON content from
	 * @param bufferSize size of the buffer to read piece of data from reader
	 * @param tree tree to keep available field names in the JSON 
	 * @throws IOException on any errors
	 */
	public JsonStaxParser(final Reader reader, final int bufferSize, final SyntaxTreeInterface<?> tree) throws IOException {
		if (reader == null) {
			throw new NullPointerException("Reader can't be null"); 
		}
		else if (bufferSize < MINIMAL_BUFFER_SIZE) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] need be at least "+MINIMAL_BUFFER_SIZE); 
		}
		else {
			this.rdr = reader;
			this.bufferSize = bufferSize;
			this.tree = tree;
			this.buffer = new char[bufferSize];
			readBlock();
			pseudoStack[0] = ' ';
			awaitedLex[0] = VALUE_AWAITED;
		}
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			buffer = null;
			pseudoStack = null;
		}
		if (detected != null) {
			throw new IOException(detected.getLocalizedMessage(),detected);
		}
	}

	/**
	 * <p>Reset parser content</p> 
	 * @throws IOException on any I/O errors
	 */
	public void reset() throws IOException {
		readBlock();
		pseudoStackDepth = 0;
		pseudoStack[0] = ' ';
		awaitedLex[0] = VALUE_AWAITED;
		detected = null;
	}
	
	@Override
	public Iterator<LexType> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if (closed || currentLen <= 0 || detected != null) {
			return false;
		}
		else {
			try{return skipBlank();
			} catch (IOException e) {
				detected = e;
			}
			return false;
		}
	}

	@Override
	public LexType next() {
		if (closed) {
			throw new IllegalStateException("Attempt to call this method on closed stream");
		}
		else if (currentLen <= 0) {
			throw new NoSuchElementException("Attempt to call this method after hasNext() returned false");
		}
		else {
			final char[]	temp = buffer;
			int				multiplier = 1;
			
			try{switch (temp[cursor]) {
					case '{' 	:
						if (awaitedLex[pseudoStackDepth] != VALUE_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited object start in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							if (pseudoStackDepth >= pseudoStack.length-1) {
								pseudoStack = Arrays.copyOf(pseudoStack,2*pseudoStack.length);
								awaitedLex = Arrays.copyOf(awaitedLex,2*awaitedLex.length);
							}
							awaitedLex[pseudoStackDepth] = LIST_SPLITTER_AWAITED;
							pseudoStackDepth++;
							pseudoStack[pseudoStackDepth] = '{';
							awaitedLex[pseudoStackDepth] = NAME_AWAITED;
							cursor++; 
							return currentLex = LexType.START_OBJECT;
						}	
					case '}' 	:
						if (pseudoStackDepth < 0) {
							detected = new SyntaxException(currentRow,currentCol,"Unpaired array terminator: nesting stack exhausted");
							return currentLex = LexType.ERROR;
						}
						else if (pseudoStack[pseudoStackDepth] != '{') {
							detected = new SyntaxException(currentRow,currentCol,"Unpaired object terminator. Array terminator awaiting");
							return currentLex = LexType.ERROR;
						}
						else {
							pseudoStackDepth--;
							cursor++; 
							return currentLex = LexType.END_OBJECT;
						}
					case '[' 	: 
						if (awaitedLex[pseudoStackDepth] != VALUE_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited array start in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							if (pseudoStackDepth >= pseudoStack.length-1) {
								pseudoStack = Arrays.copyOf(pseudoStack,2*pseudoStack.length);
								awaitedLex = Arrays.copyOf(awaitedLex,2*awaitedLex.length);
							}
							awaitedLex[pseudoStackDepth] = LIST_SPLITTER_AWAITED;
							pseudoStackDepth++;
							pseudoStack[pseudoStackDepth] = '[';
							awaitedLex[pseudoStackDepth] = VALUE_AWAITED;
							cursor++; 
							return currentLex = LexType.START_ARRAY;
						}
					case ']' 	: 
						if (pseudoStackDepth < 0) {
							detected = new SyntaxException(currentRow,currentCol,"Unpaired array terminator: nesting stack exhausted");
							return currentLex = LexType.ERROR;
						}
						else if (pseudoStack[pseudoStackDepth] != '[') {
							detected = new SyntaxException(currentRow,currentCol,"Unpaired array terminator. Object terminator awaiting");
							return currentLex = LexType.ERROR;
						}
						else {
							pseudoStackDepth--;
							cursor++; 
							return currentLex = LexType.END_ARRAY;
						}
					case ',' 	:
						if (awaitedLex[pseudoStackDepth] != LIST_SPLITTER_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited list splitter in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							cursor++; 
							awaitedLex[pseudoStackDepth] = pseudoStack[pseudoStackDepth] == '{' ? NAME_AWAITED : VALUE_AWAITED;
							return currentLex = LexType.LIST_SPLITTER;
						}
					case ':' 	: 
						if (awaitedLex[pseudoStackDepth] != NAME_SPLITTER_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited name splitter in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							cursor++; 
							awaitedLex[pseudoStackDepth] = VALUE_AWAITED;
							return currentLex = LexType.NAME_SPLITTER;
						}
					case 't' 	:
						if (awaitedLex[pseudoStackDepth] != VALUE_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited value in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							if (compare(TRUE_VALUE)) {
								parsedBoolean = true;
								awaitedLex[pseudoStackDepth] = LIST_SPLITTER_AWAITED;
								return currentLex = LexType.BOOLEAN_VALUE;
							}
							else {
								detected = new SyntaxException(currentRow,currentCol,"Illegal keyword in the stream");
								return currentLex = LexType.ERROR;
							}
						}
					case 'f' 	:
						if (awaitedLex[pseudoStackDepth] != VALUE_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited value in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							if (compare(FALSE_VALUE)) {
								parsedBoolean = false;
								awaitedLex[pseudoStackDepth] = LIST_SPLITTER_AWAITED;
								return currentLex = LexType.BOOLEAN_VALUE;
							}
							else {
								detected = new SyntaxException(currentRow,currentCol,"Illegal keyword in the stream");
								return currentLex = LexType.ERROR;
							}
						}
					case 'n'	:
						if (awaitedLex[pseudoStackDepth] != VALUE_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited value in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							if (compare(NULL_VALUE)) {
								awaitedLex[pseudoStackDepth] = LIST_SPLITTER_AWAITED;
								return currentLex = LexType.NULL_VALUE;
							}
							else {
								detected = new SyntaxException(currentRow,currentCol,"Illegal keyword in the stream");
								return currentLex = LexType.ERROR;
							}
						}
					case '-' :
						multiplier = -1;
					case '+' : 
						cursor++;
						if (!skipBlank()) {
							detected = new SyntaxException(currentRow,currentCol,"Illegal sign in the stream");
							return currentLex = LexType.ERROR;
						}
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						int		from = cursor;
						
						do {while (cursor < currentLen && temp[cursor] >= '0' && temp[cursor] <= '9') {
								cursor++;							
							}
							if (cursor < currentLen) {
								break;
							}
							else {
								sb.setLength(0);
								sb.append(buffer,from,currentLen-from);
							}
						} while (readBlock());
						
						currentLex = LexType.INTEGER_VALUE;
						if (temp[cursor] == '.') {
							currentLex = LexType.REAL_VALUE;
							cursor++;
							
							do {while (cursor < currentLen && temp[cursor] >= '0' && temp[cursor] <= '9') {
									cursor++;							
								}
								if (cursor < currentLen) {
									break;
								}
								else {
									sb.setLength(0);
									sb.append(buffer,from,currentLen-from);
								}
							} while (readBlock());
						}
						
						if (temp[cursor] == 'e' || temp[cursor] == 'E') {
							currentLex = LexType.REAL_VALUE;
							cursor++;
							
							do {while (cursor < currentLen && (temp[cursor] == '+' || temp[cursor] == '-')) {
									cursor++;							
								}
								if (cursor < currentLen) {
									break;
								}
								else {
									sb.setLength(0);
									sb.append(buffer,from,currentLen-from);
								}
							} while (readBlock());
							
							do {while (cursor < currentLen && temp[cursor] >= '0' && temp[cursor] <= '9') {
									cursor++;							
								}
								if (cursor < currentLen) {
									break;
								}
								else {
									sb.setLength(0);
									sb.append(buffer,from,currentLen-from);
								}
							} while (readBlock());
						}
						
						if (awaitedLex[pseudoStackDepth] != VALUE_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited value in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							if (currentLex == LexType.INTEGER_VALUE) {
								if (from < cursor) {
									cursor = CharUtils.parseLong(buffer,from,forParsedLong,true);
								}
								else {
									CharUtils.parseLong(sb.append(buffer,0,cursor).toString().toCharArray(),0,forParsedLong,true);
								}							
								forParsedLong[0] *= multiplier;
							}
							else {
								if (from < cursor) {
									cursor = CharUtils.parseDouble(buffer,from,forParsedDouble,true);
								}
								else {
									CharUtils.parseDouble(sb.append(buffer,0,cursor).toString().toCharArray(),0,forParsedDouble,true);
								}
								forParsedDouble[0] *= multiplier;
							}
							awaitedLex[pseudoStackDepth] = LIST_SPLITTER_AWAITED;
						}
						return currentLex;
					case '\"'	:
						int		fromString = ++cursor;
						boolean	moreThanBlock = false, escaped = false;
						
						do {while (cursor < currentLen && temp[cursor] != '\"') {
								if (temp[cursor] == '\\') {
									escaped = true;
									cursor++;
								}
								cursor++;
							}
							if (cursor < currentLen) {
								break;
							}
							else {
								if (!moreThanBlock) {
									moreThanBlock = true;
									sb.setLength(0);
								}
								sb.setLength(0);
								sb.append(buffer,fromString,currentLen-fromString);
								fromString = 0;
							}
						} while (readBlock());
						
						if (awaitedLex[pseudoStackDepth] != NAME_AWAITED && awaitedLex[pseudoStackDepth] != VALUE_AWAITED) {
							detected = new SyntaxException(currentRow,currentCol,"Unwaited value in the stream");
							return currentLex = LexType.ERROR;
						}
						else {
							if (moreThanBlock) {
								if (escaped) {
									sbResult.setLength(0);
									cursor = CharUtils.parseString(sb.append(buffer,0,cursor-1).toString().toCharArray(),fromString,'\"',sbResult);
								}
								else {
									cursor = CharUtils.parseUnescapedString(sb.append(buffer,0,cursor-1).toString().toCharArray(),fromString, '\"',true,bounds);
								}
								shortString = false;
							}
							else {
								if (escaped) {
									sbResult.setLength(0);
									cursor = CharUtils.parseStringExtended(buffer,fromString,'\"',sbResult);
								}
								else {
									cursor = CharUtils.parseUnescapedString(buffer,fromString,'\"',true,bounds);
								}
								shortString = true;
							}
							escapedString = escaped;
							if (awaitedLex[pseudoStackDepth] == NAME_AWAITED) {
								awaitedLex[pseudoStackDepth] = NAME_SPLITTER_AWAITED;
								return currentLex = LexType.NAME;
							}
							else {
								awaitedLex[pseudoStackDepth] = LIST_SPLITTER_AWAITED;
								return currentLex = LexType.STRING_VALUE;
							}
						}
					default : 
						detected = new SyntaxException(currentRow,currentCol,"Illegal char in the stream");
						return currentLex = LexType.ERROR;
				}
			} catch (IOException e) {
				detected = e;
				return currentLex = LexType.ERROR;
			}
		}
	}

	/**
	 * <p>Get current lexema type</p>
	 * @return current lexema type. Can't be nu;;
	 */
	public LexType current() {
		if (currentLex == null) {
			throw new IllegalStateException("Attempt to call this method before any next() calls");
		}
		else {
			return currentLex;
		}
	}
	
	/**
	 * <p>Get Id of the field name. This method can be used only when {@linkplain SyntaxTreeInterface} instance was passed to the constructor of the class</p>
	 * @return name id
	 * @throws IOException on any I/O errors
	 * @throws IllegalStateException attempt to get Id without passing {@linkplain SyntaxTreeInterface} instance 
	 */
	public int nameId() throws IOException, IllegalStateException {
		if (tree == null) {
			throw new IllegalStateException("You can't use this method because name tree was not passed to the constructor");
		}
		else if (currentLex != LexType.NAME) {
			throw new IllegalStateException("Attempt to read name when lex type is ["+currentLex+"]");
		}
		else if (shortString) {
			return (int)tree.seekName(buffer,bounds[0],bounds[1]-bounds[0]);
		}
		else {
			return (int)tree.seekName(sbResult.toString());
		}
	}

	/**
	 * <p>Get current field name from source input</p>
	 * @return field name.
	 * @throws IOException on any I/O errors
	 * @throws IllegalStateException if current lexema is not a name 
	 */
	public String name() throws IOException, IllegalStateException {
		if (currentLex != LexType.NAME) {
			throw new IllegalStateException("Attempt to read name when lex type is ["+currentLex+"]");
		}
		else if (shortString) {
			return new String(buffer,bounds[0],bounds[1]-bounds[0]+1);
		}
		else {
			return sbResult.toString();
		}
	}

	/**
	 * <p>Fill current field name from source input</p>
	 * @param content array to fill name to
	 * @param from start position inside the array
	 * @param to end position inside the array
	 * @return really filled length.
	 * @throws IOException on any I/O errors
	 */
	public int name(final char[] content, final int from, final int to) throws IOException, IllegalStateException {
		if (currentLex != LexType.NAME) {
			throw new IllegalStateException("Attempt to read string when lex type is ["+currentLex+"]");
		}
		else if (shortString) {
			final int	minLen = Math.min(to-from,bounds[1]-bounds[0]+1);
			
			System.arraycopy(buffer,bounds[0],content,from,minLen);
			return minLen;
		}
		else {
			final int	minLen = Math.min(to-from,sb.length());
			
			sbResult.getChars(0,minLen,content,0);
			return minLen;
		}
	}

	/**
	 * <p>Get current boolean value from source input</p>
	 * @return boolean value
	 * @throws IOException on any I/O errors
	 * @throws IllegalStateException if current lexema is not a boolean value 
	 */
	public boolean booleanValue() throws IOException, IllegalStateException {
		if (currentLex != LexType.BOOLEAN_VALUE) {
			throw new IllegalStateException("Attempt to read boolean when lex type is ["+currentLex+"]");
		}
		else {
			return parsedBoolean;
		}
	}
	
	/**
	 * <p>Get current int value from source input</p>
	 * @return int value
	 * @throws IOException on any I/O errors
	 * @throws IllegalStateException if current lexema is not a int value 
	 */
	public long intValue() throws IOException, IllegalStateException {
		if (currentLex != LexType.INTEGER_VALUE) {
			throw new IllegalStateException("Attempt to read integer when lex type is ["+currentLex+"]");
		}
		else {
			return forParsedLong[0];
		}
	}

	/**
	 * <p>Get current real value from source input</p>
	 * @return real value
	 * @throws IOException on any I/O errors
	 * @throws IllegalStateException if current lexema is not a real value 
	 */
	public double realValue() throws IOException, IllegalStateException {
		if (currentLex != LexType.REAL_VALUE) {
			throw new IllegalStateException("Attempt to read real when lex type is ["+currentLex+"]");
		}
		else {
			return forParsedDouble[0];
		}
	}

	/**
	 * <p>Get current string value from source input</p>
	 * @return field name.
	 * @throws IOException on any I/O errors
	 * @throws IllegalStateException if current lexema is not a name 
	 */
	public String stringValue() throws IOException, IllegalStateException {
		if (currentLex != LexType.STRING_VALUE) {
			throw new IllegalStateException("Attempt to read string when lex type is ["+currentLex+"]");
		}
		else if (shortString && !escapedString) {
			return new String(buffer,bounds[0],bounds[1]-bounds[0]+1);
		}
		else {
			return sbResult.toString();
		}
	}

	/**
	 * <p>Fill current string value from source input</p>
	 * @param content array to fill string value to
	 * @param from start position inside the array
	 * @param to end position inside the array
	 * @return really filled length.
	 * @throws IOException on any I/O errors
	 */
	public int stringValue(final char[] content, final int from, final int to) throws IOException, IllegalStateException {
		if (currentLex != LexType.STRING_VALUE) {
			throw new IllegalStateException("Attempt to read string when lex type is ["+currentLex+"]");
		}
		else if (shortString && !escapedString) {
			final int	minLen = Math.min(to-from,bounds[1]-bounds[0]+1);
			
			System.arraycopy(buffer,bounds[0],content,from,minLen);
			return minLen;
		}
		else {
			final int	minLen = Math.min(to-from,sbResult.length());
			
			sbResult.getChars(0,minLen,content,0);
			return minLen;
		}
	}

	/**
	 * <p>Get current row inside Reader</p>
	 * @return current row
	 */
	public long row() {
		return 0;
	}

	/**
	 * <p>Get current column inside Reader</p>
	 * @return current column
	 */
	public long col() {
		return 0;
	}
	
	/**
	 * <p>Get last error description</p>
	 * @return last error description or null if missing
	 */
	public Exception getLastError() {
		return detected;
	}
	
	private boolean readBlock() throws IOException {
		cursor = 0;
		buffer[0] = 0;
		return (currentLen = rdr.read(buffer,0,bufferSize)) > 0;
	}

	private boolean compare(final char[] value) throws IOException {
		int	start = cursor;
		
		for (int index = 0, maxIndex = value.length; index < maxIndex; index++, start++) {
			if (start >= currentLen) {
				readBlock();
				start = cursor;
			}
			if (buffer[start] != value[index]) {
				return false;
			}
		}
		cursor = start;
		return true;
	}
	
	private boolean skipBlank() throws IOException {
		final char[]	temp = buffer;
		
		do {final int	tempLen = currentLen;
			int			tempCursor = cursor, fromCol = currentCol;
			
			while(tempCursor < tempLen && temp[tempCursor] <= ' ') {
				if (temp[tempCursor] == '\n') {
					currentRow++;
					currentCol = 0;
					fromCol = tempCursor; 
				}
				tempCursor++;
			}
			if (tempCursor < tempLen) {
				currentCol += (tempCursor-fromCol);
				return true;
			}
		} while (readBlock());
		
		return false;
	}
}
