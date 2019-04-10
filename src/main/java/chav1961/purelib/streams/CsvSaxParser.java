package chav1961.purelib.streams;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;


import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.streams.interfaces.CsvSaxHandler;

public class CsvSaxParser {
	private final CsvSaxHandler	handler;
	private final char			splitter;
	private final boolean		firstLineIsNames;
	private final boolean		asStrings;
	private final boolean		asNumbers;
	private final int[]			location = new int[2];
	private final long[]		valueAndType = new long[2];
	private final double[]		value = new double[1];
	private boolean				toBeContinued = false;
	private int					totalFields = Integer.MAX_VALUE, lastFieldIndex = 0;
	
	
	public CsvSaxParser(final CsvSaxHandler handler, final boolean firstLineIsNames) {
		this(handler,',',firstLineIsNames,true,true);
	}
	
	public CsvSaxParser(final CsvSaxHandler handler, final char splitter, final boolean firstLineIsNames, final boolean asStrings, final boolean asNumbers) {
		if (handler == null) {
			throw new NullPointerException("Handler can't be null");
		}
		else {
			this.handler = handler;
			this.splitter = splitter;
			this.firstLineIsNames = firstLineIsNames;
			this.asStrings = asStrings;
			this.asNumbers = asNumbers;
		}
	}
	
	public void parse(final Reader reader) throws IOException, SyntaxException {
		if (reader == null) {
			throw new NullPointerException("Reader to parse from can't be null");
		}
		else {
			try{final GrowableCharArray	temporary = new GrowableCharArray(true); 
				
				handler.startDoc();
				try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement,lineNo,data,from,length)->{
														try{if (firstLineIsNames && lineNo == 1) {
																processNames(data,from,length);
															}
															else {
																processContent(lineNo,data,from,length,temporary);
															}
														} catch (SyntaxException e) {
															throw e;
														} catch (ContentException e) {
															throw new IOException(e.getLocalizedMessage(),e); 
														}
													})) {
					lblp.write(reader);
				}
				handler.endDoc();
			} catch (ContentException exc) {
				throw new SyntaxException(0,0,exc.getMessage(),exc);
			}
		}
	}

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
	
	public void parse(final char[] data) throws NullPointerException, IOException, SyntaxException {
		if (data == null) {
			throw new NullPointerException("String to parse can't be null"); 
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

	private void processNames(final char[] data, final int from, final int length) throws ContentException, SyntaxException {
		int			start = from-1, index = 0;
		
		handler.startCaption();
loop:	do {boolean	hasData = false;

			start = CharUtils.skipBlank(data,start+1,true);
			switch (data[start]) {
				case '\r' : case '\n' :
					break loop;
				case '\"' :
					final int	end = CharUtils.parseUnescapedString(data,start+1,'\"',true, location);
					
					if (end < 0) {
						throw new SyntaxException(1,start-from,"Name contains escaping or newline inside");
					}
					else if (data[end-1] == '\"') {
						if (asStrings) {
							handler.name(index++,new String(data,location[0],location[1]-location[0]+1));
						}
						else {
							handler.name(index++,data,location[0],location[1]-location[0]+1);
						}
						start = end;
						hasData = true;
					}
					else {
						throw new SyntaxException(1,start-from,"Missing double quote after field name");
					}
					break;
				default :
					if (Character.isJavaIdentifierStart(data[start])) {
						int	begin = start;
						
						while (data[start] != '\n' && data[start] != '\r' && data[start] != splitter) {
							start++;
						}
						while (start > begin && data[start-1] <= ' ') {
							start--;
						}
						if (asStrings) {
							handler.name(index++,new String(data,begin,start-begin));
						}
						else {
							handler.name(index++,data,begin,start-begin);
						}
						hasData = true;
					}
					else {
						throw new SyntaxException(1,start-from,"Name is missing or contains invalid chars");
					}
			}
			start = CharUtils.skipBlank(data,start,true);
			if (!hasData) {
				throw new SyntaxException(1,start-from,"Name is missing");
			}
		} while (data[start] == splitter);
		handler.endCaption();
		totalFields = index;
	}

	private void processContent(final int lineNo, final char[] data, final int from, final int length, final GrowableCharArray store) throws ContentException, SyntaxException {
		int		start = from-1, begin, fieldIndex = lastFieldIndex;

		if (!toBeContinued) {
			handler.startData();
		}
		else {
			start = -extractValue(data,start+1,location,store);
			if (data[start-1] == '\n') {
				lastFieldIndex = fieldIndex; 
				toBeContinued = true;
				return;
			}
			else {
				processStringValue(fieldIndex++,store.toPlain().toArray(),0,store.length());
				store.length(0);
			}
		}
		do {start++;
		
			if (fieldIndex >= totalFields) {
				throw new SyntaxException(lineNo,start,"Number of fields in line ["+fieldIndex+"] is greater due to previous line ["+totalFields+"]");
			}
			if (data[start] == '\"') {
				final int	beginValue = start + 1;
				start = extractValue(data,beginValue,location,store);
				
				if (start < 0) {
					start = -start;
					if (data[start-1] == '\n') {
						lastFieldIndex = fieldIndex; 
						toBeContinued = true;
						return;
					}
					else {
						processStringValue(fieldIndex++,store.toPlain().toArray(),0,store.length());
						store.length(0);
					}
				}
				else {
					location[0] = beginValue;
					location[1] = start-1;
					
					if (asNumbers) {
						switch (testNumber(data,location[0],location[1])) {
							case CharUtils.PREF_LONG 	:
								processLongValue(fieldIndex++,data,location[0],location[1]);
								break;
							case CharUtils.PREF_DOUBLE	:
								processDoubleValue(fieldIndex++,data,location[0],location[1]);
								break;
							default :
								processStringValue(fieldIndex++,data,location[0],location[1]);
								break;
						}
					}
					else {
						processStringValue(fieldIndex++,data,location[0],location[1]);
					}
				}
			}
			else {
				location[0] = begin = start;
				while (data[start] != '\r' && data[start] != '\n' && data[start] != splitter) {
					start++;
				}
				location[1] = start;
				
				if (start == begin) {
					handler.value(fieldIndex++);
				}
				else if (asNumbers) {
					switch (testNumber(data,location[0],location[1])) {
						case CharUtils.PREF_LONG 	:
							processLongValue(fieldIndex++,data,location[0],location[1]);
							break;
						case CharUtils.PREF_DOUBLE	:
							processDoubleValue(fieldIndex++,data,location[0],location[1]);
							break;
						default :
							processStringValue(fieldIndex++,data,location[0],location[1]);
							break;
					}
				}
				else {
					processStringValue(fieldIndex++,data,location[0],location[1]);
				}
			}
		} while (data[start] == splitter);
		
		if (fieldIndex != totalFields) {
			if (totalFields == Integer.MAX_VALUE) {
				totalFields = fieldIndex;
			}
			else {
				throw new SyntaxException(lineNo,start,"Number of fields in line ["+fieldIndex+"] is less due to previous line ["+totalFields+"]");
			}
		}
		lastFieldIndex = 0;
		toBeContinued = false;
		handler.endData();
	}
	
	private int extractValue(final char[] data, final int from, final int[] location, final GrowableCharArray store) {
		int	start = from;
		
		while (data[start] != '\r' && data[start] != '\n' && data[start] != '\"') {
			start++;
		}
		if (data[start] == '\r') {
			store.append(data,from,start+1).append('\n');
			return -(start+1);
		}
		else if (data[start] == '\n') {
			store.append(data,from,start+1);
			return -(start+1);
		}
		else if (data[start+1] == '\"') {
			store.append(data,from,start+1);
			return extractValue(data,start+2,location,store);
		}
		else if (store.length() == 0) {
			location[0] = from;
			location[1] = start-1;
			return start+1;
		}
		else {
			store.append(data,from,start);
			return -(start+1);
		}
	}
	
	private int testNumber(final char[] data, final int from, final int to) {
		int	start = CharUtils.skipBlank(data,from,true);
		
		if (data[start] == '+' || data[start] == '-') {
			start++;
		}
		if (data[start] >= '0' && data[start] <= '9') {
			while (data[start] >= '0' && data[start] <= '9') {
				start++;
			}
			if (data[start] == '.' || data[start] == 'e' || data[start] == 'E') {
				if (data[start] == '.') {
					start++;
					while (data[start] >= '0' && data[start] <= '9') {
						start++;
					}
				}
				if (data[start] == 'e' || data[start] == 'E') {
					start++;
					if (data[start] == '+' || data[start] == '-') {
						start++;
					}
					while (data[start] >= '0' && data[start] <= '9') {
						start++;
					}
				}
				if (CharUtils.skipBlank(data,start,true) == to) {
					return CharUtils.PREF_DOUBLE;
				}
				else {
					return 0;
				}
			}
			else {
				if (CharUtils.skipBlank(data,start,true) == to) {
					return CharUtils.PREF_LONG;
				}
				else {
					return 0;
				}
			}
		}
		else {
			return 0;
		}
	}
	
	private void processStringValue(final int fieldNo, final char[] data, final int from, final int to) throws ContentException {
		if (asStrings) {
			handler.value(fieldNo, new String(data,from,to-from));
		}
		else {
			handler.value(fieldNo, data,from,to-from);
		}
	}

	private void processLongValue(final int fieldNo, final char[] data, final int from, final int to) throws ContentException {
		CharUtils.parseSignedLong(data,CharUtils.skipBlank(data,from,true),valueAndType,true);
		handler.value(fieldNo,valueAndType[0]);
	}

	private void processDoubleValue(final int fieldNo, final char[] data, final int from, final int to) throws ContentException {
		CharUtils.parseSignedDouble(data,CharUtils.skipBlank(data,from,true),value,true);
		handler.value(fieldNo,value[0]);
	}
}
