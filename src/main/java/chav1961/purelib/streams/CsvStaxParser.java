package chav1961.purelib.streams;


import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.intern.UnsafedCharUtils;
import chav1961.purelib.streams.interfaces.CsvStaxParserInterface;
import chav1961.purelib.streams.interfaces.CsvStaxParserLexType;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class CsvStaxParser implements CsvStaxParserInterface {
	private static final int		DEFAULT_BUFFER_SIZE = 8192;
	private static final char[][]	DUMMY_NAMES = new char[0][];
	private static final char[]		TRUE = "true".toCharArray(); 
	private static final char[]		FALSE = "false".toCharArray(); 

	private static final int		NAME_AWAITED = 0;
	private static final int		NAME_SPLITTER_AWAITED = 1;
	private static final int		VALUE_AWAITED = 2;
	private static final int		LIST_SPLITTER_AWAITED = 3;
	
	private final Reader			reader;
	private final char[]			buffer;
	private final boolean			theSameFirstIsNames;
	private final StringBuilder		sb = new StringBuilder();
	private final long[]			parsedLong = new long[1];
	private final double[]			parsedDouble = new double[1];
	private final CsvStaxParserLexType[]	awaitedDataType; 
	
	private int						current, last;
	private long					row = 0, colBegin = 0;
	private int						awaitedIndex = 0;
	private Exception				lastError = null;
	private CsvStaxParserLexType	lexType;
	private char[]					value;
	private int						valueFrom, valueTo;
	
	public CsvStaxParser(final Reader reader, final Class<?>... awaitedTypes) throws IOException {
		this(reader,DEFAULT_BUFFER_SIZE,true,awaitedTypes);
	}

	public CsvStaxParser(final Reader reader, final int bufferSize, final boolean theSameFirstIsNames, final Class<?>... awaitedTypes) throws IOException {
		if (reader == null) {
			throw new NullPointerException("Reader can't be null"); 
		}
		else if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] must be positive"); 
		}
		else if (awaitedTypes == null || awaitedTypes.length == 0) {
			throw new IllegalArgumentException("List of awaited types can't be null or empty array"); 
		}
		else {
			awaitedDataType = new CsvStaxParserLexType[awaitedTypes.length]; 
			for (int index = 0; index < awaitedDataType.length; index++) {
				if (awaitedTypes[index] == null) {
					throw new IllegalArgumentException("Null value at index ["+index+"] in the awaited types list"); 
				}
				else if (awaitedTypes[index] == String.class || awaitedTypes[index] == char.class || awaitedTypes[index] == Character.class) {
					awaitedDataType[index] = CsvStaxParserLexType.STRING_VALUE;
				}
				else if (awaitedTypes[index] == int.class || awaitedTypes[index] == long.class || awaitedTypes[index] == Integer.class || awaitedTypes[index] == Long.class) {
					awaitedDataType[index] = CsvStaxParserLexType.INTEGER_VALUE;
				}
				else if (awaitedTypes[index] == float.class || awaitedTypes[index] == double.class || awaitedTypes[index] == Float.class || awaitedTypes[index] == Double.class) {
					awaitedDataType[index] = CsvStaxParserLexType.REAL_VALUE;
				}
				else if (awaitedTypes[index] == boolean.class || awaitedTypes[index] == Boolean.class) {
					awaitedDataType[index] = CsvStaxParserLexType.BOOLEAN_VALUE;
				}
				else {
					throw new IllegalArgumentException("Illegal class awaited ["+awaitedTypes[index].getCanonicalName()+"] at index ["+index+"] in the awaited types list. int.class, long.class, float.class, double.class, boolean.class, char.class, String.class and it's wrappers are valid only"); 
				}
			}
			
			this.reader = reader;
			this.buffer = new char[bufferSize+1];
			this.theSameFirstIsNames = theSameFirstIsNames;
			nextPiece();
		}
	}

	@Override
	public Iterator<CsvStaxParserLexType> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if (lastError != null) {
			return false;
		}
		else if (current >= last) {
			try{this.last = reader.read(this.buffer);
				this.current = 0;
			} catch (IOException e) {
				lastError = e;
				return false;
			}
		}
		return current < last;
	}

	@Override
	public CsvStaxParserLexType next() {
		CsvStaxParserLexType	result;
		int						start = current;
		boolean					splitted = false, dquoted = false;
		char					symbol;
		
		try{if (lastError != null) {
				return CsvStaxParserLexType.ERROR;
			}
			else if (buffer[current] == '\"') {
				start = ++current;
loop:			for (;;) {
					while ((symbol = buffer[current]) != 0 && symbol != ',' && symbol != '\r' && symbol != '\n' && symbol != '\"') {
						current++;
					}
					switch (symbol) {
						case 0 		:
							sb.append(buffer,start,current-start);
							if (!nextPiece()) {
								lastError = new SyntaxException(row(),col(),"Unclosed double quote");
								return lexType = CsvStaxParserLexType.ERROR;
							}
							splitted = true;
							start = 0;
							break;
						case ','	:
							break loop;
						case '\r' 	:
							nextChar();
						case '\n' 	:
							nextChar();
							break;
						case '\"'	:
							nextChar();
							if (buffer[current] == '\"') {
								dquoted = true;
								current++;
							}
							else if (buffer[current] == ',') {
								break loop;
							}
							else if (buffer[current] == '\n' || buffer[current] == '\r') {
								break loop;
							}
							if (splitted) {
								sb.append(buffer,start,current-start);
							}
							break;
					}
				}
				if (splitted) {
					if (current-start-1 > 0) {
						sb.append(buffer,start,current-start-1);
					}
					result = classify(sb.toString().toCharArray(),0,sb.length(),dquoted);
					sb.setLength(0);
				}
				else {
					result = classify(buffer,start,current-start-1,dquoted);
				}
				if (buffer[current] == ',') {
					splitter();
				}
				else {
					if (++awaitedIndex < awaitedDataType.length) {
						lastError = new SyntaxException(row(),col(),"number of fields ["+awaitedIndex+"] is less than awaited ["+awaitedDataType.length+"]");
						return lexType = CsvStaxParserLexType.ERROR;
					}
					newLine();
				}
				return lexType = result;
			}
			else {
				for (;;) {
					while ((symbol = buffer[current]) != 0 && symbol != ',' && symbol != '\r' && symbol != '\n') {
						current++;
					}
					if (symbol == 0) {
						if (current > start) {
							sb.append(buffer,start,current-start);
						}
						if (!nextPiece()) {
							result = classify(sb.toString().toCharArray(),0,sb.length(),dquoted);
							sb.setLength(0);
							return lexType = result;
						}
						splitted = true;
					}
					else {
						break;
					}
				}
				if (splitted) {
					if (current > start) {
						sb.append(buffer,start,current-start);
					}
					result = classify(sb.toString().toCharArray(),0,sb.length(),dquoted);
					sb.setLength(0);
				}
				else {
					result = classify(buffer,start,current-start,dquoted);
				}
				if (symbol == ',') {
					splitter();
				}
				else {
					if (++awaitedIndex < awaitedDataType.length) {
						lastError = new SyntaxException(row(),col(),"number of fields ["+awaitedIndex+"] is less than awaited ["+awaitedDataType.length+"]");
						return lexType = CsvStaxParserLexType.ERROR;
					}
					newLine();
				}
				return lexType = result;
			}
		} catch (IOException e) {
			lastError = e;
			return lexType = CsvStaxParserLexType.ERROR;
		}
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public CsvStaxParserLexType current() {
		return lexType;
	}

	@Override
	public String name() throws IOException, SyntaxException {
		if (current() != CsvStaxParserLexType.NAME) {
			throw new IllegalStateException("Attempt to read name from illegal lexema ["+current()+"]");
		}
		else {
			return new String(value,valueFrom,valueTo-valueFrom);
		}
	}

	@Override
	public int name(char[] content, int from, int to) throws IOException, SyntaxException {
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] outside the content array 0.."+(content.length-1)); 
		}
		else if (to < 0 || to >= content.length) {
			throw new IllegalArgumentException("To position ["+to+"] outside the content array 0.."+(content.length-1));
		}
		else if (current() != CsvStaxParserLexType.NAME) {
			throw new IllegalStateException("Attempt to read name from illegal lexema ["+current()+"]");
		}
		else {
			final 	int minLen = Math.min(to-from,valueTo-valueFrom);
			
			System.arraycopy(value,valueFrom,content,from,minLen);
			return minLen;
		}
	}

	@Override
	public boolean booleanValue() throws IOException, SyntaxException {
		if (current() != CsvStaxParserLexType.BOOLEAN_VALUE) {
			throw new IllegalStateException("Attempt to read boolean value from illegal lexema ["+current()+"]");
		}
		else if (UnsafedCharUtils.uncheckedCompare(value,valueFrom,TRUE,0,TRUE.length)) {
			return true;
		}
		else if (UnsafedCharUtils.uncheckedCompare(value,valueFrom,FALSE,0,FALSE.length)) {
			return false;
		}
		else {
			throw new SyntaxException(row(),col(),"Neither 'true' nor 'false' in the  input stream for the boolean");
		}
	}

	@Override
	public long intValue() throws IOException, SyntaxException {
		if (current() != CsvStaxParserLexType.INTEGER_VALUE) {
			throw new IllegalStateException("Attempt to read integer value from illegal lexema ["+current()+"]");
		}
		else {
			try{CharUtils.parseSignedLong(value,valueFrom,parsedLong,true);
				return parsedLong[0];
			} catch (IllegalArgumentException exc) {
				throw new SyntaxException(row(),col(),"Invalid integer in the input stream");
			}
	}
	}

	@Override
	public double realValue() throws IOException, SyntaxException {
		if (current() != CsvStaxParserLexType.REAL_VALUE) {
			throw new IllegalStateException("Attempt to read real value from illegal lexema ["+current()+"]");
		}
		else {
			try{CharUtils.parseSignedDouble(value,valueFrom,parsedDouble,true);
				return parsedDouble[0];
			} catch (IllegalArgumentException exc) {
				throw new SyntaxException(row(),col(),"Invalid real in the input stream");
			}
		}
	}

	@Override
	public String stringValue() throws IOException, SyntaxException {
		if (current() != CsvStaxParserLexType.STRING_VALUE) {
			throw new IllegalStateException("Attempt to read string value from illegal lexema ["+current()+"]");
		}
		else {
			return new String(value,valueFrom,valueTo-valueFrom);
		}
	}

	@Override
	public int stringValue(char[] content, int from, int to) throws IOException, SyntaxException {
		if (content == null) {
			throw new NullPointerException("Content array can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] outside the content array 0.."+(content.length-1)); 
		}
		else if (to < 0 || to >= content.length) {
			throw new IllegalArgumentException("To position ["+to+"] outside the content array 0.."+(content.length-1));
		}
		else if (current() != CsvStaxParserLexType.STRING_VALUE) {
			throw new IllegalStateException("Attempt to read string value from illegal lexema ["+current()+"]");
		}
		else {
			final 	int minLen = Math.min(to-from,valueTo-valueFrom);
			
			System.arraycopy(value,valueFrom,content,from,minLen);
			return minLen;
		}
	}

	@Override
	public long row() {
		return row + 1;
	}

	@Override
	public long col() {
		return current - colBegin + 1;
	}

	@Override
	public Exception getLastError() {
		return lastError;
	}


	private boolean nextPiece() throws IOException {
		this.current = 0;
		if ((this.last = reader.read(this.buffer,0,buffer.length-1)) <= 0) {
			return false;
		}
		else {
			this.buffer[this.last] = 0;
			return true;
		}
	}

	private void nextChar() throws IOException {
		if (++current >= last)  {
			colBegin = -(last-colBegin);
			nextPiece();
		}
	}
	
	private void splitter() throws IOException {
		nextChar();
		awaitedIndex++;
	}

	private void newLine() throws IOException {
		nextChar();
		if (buffer[current] == '\n') {
			nextChar();
		}
		colBegin = current;
		awaitedIndex = 0;
		row++;
	}
	
	private CsvStaxParserLexType classify(final char[] content, final int from, final int length, final boolean dquoted) {
		if (theSameFirstIsNames && row() == 1) {
			value = content;
			valueFrom = from;
			valueTo = from + length;
			return CsvStaxParserLexType.NAME; 
		}
		else if (awaitedIndex >= awaitedDataType.length) {
			lastError = new SyntaxException(row(),col(),"number of fields ["+awaitedIndex+"] is greater than awaited ["+awaitedDataType.length+"]");
			return lexType = CsvStaxParserLexType.ERROR;
		}
		else if (dquoted) {
			int	count = 0;	// Calculate double quote count
			
			for (int index = from; index < from + length; index++) {
				if (content[index] == '\"') {
					count++;
				}
			}
			final char[]	result = new char[length-count/2];
			
			for (int index = from, targetIndex = 0; index < from + length; index++, targetIndex++) {
				if ((result[targetIndex] = content[index]) == '\"') {	// Move content and exclude duplicated
					index++;
				}
			}
			value = result;
			valueFrom = 0;
			valueTo = result.length;
			return awaitedDataType[awaitedIndex]; 
		}
		else {
			value = content;
			valueFrom = from;
			valueTo = from + length;
			return awaitedDataType[awaitedIndex]; 
		}
	}
}
