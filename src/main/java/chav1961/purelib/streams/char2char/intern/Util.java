package chav1961.purelib.streams.char2char.intern;

import java.io.IOException;
import java.util.List;

import chav1961.purelib.basic.exceptions.SyntaxException;

class Util {
	 static char[] join(final char[] source, final int from, final String substitution) {
		final char[]	result = new char[source.length-from+substitution.length()];
		
		substitution.getChars(0,substitution.length(),result,0);
		System.arraycopy(source,from,result,substitution.length(),result.length-substitution.length());
		return result;
	}

	 static int checkAndAdd(final char[] data, final int from, final int to, final int row, final int col, final List<String> names) throws SyntaxException {
		final String	name = new String(data,from,to-from);
		
		for (String item : names) {
			if (item.equals(name)) {
				throw new SyntaxException(row, col, "Duplicate name ["+name+"]");
			}
		}
		names.add(name);
		return names.size()-1; 
	}

	 static int checkAndGet(final char[] data, final int from, final int to, final int row, final int col, final List<String> names) throws SyntaxException {
		final String	name = new String(data,from,to-from);
		
		for (int index = 0; index < names.size(); index++) {
			if (names.get(index).equals(name)) {
				return index;
			}
		}
		throw new SyntaxException(row, col, "Missing name ["+name+"]");
	}

	 static int compare(final char[] data, int from, final char[] template, final boolean useX) {
		final int	endData = data.length;
		
		for (int index = 0, maxIndex = template.length; index < maxIndex; index++) {
			if (from+index >= endData || data[from+index] != template[index]) {
				if (useX){
					if (index < 4) {
						return -1;
					}
					else {
						return from+index;
					}
				}
				else {
					return -1;
				}
			}
		}
		return from+template.length;
	}
	
	 static int skipBlank(final char[] data, int from) {
		if (from >= data.length) {
			return from;
		}
		else {
			while (from < data.length && Character.isSpaceChar(data[from])) from++;
			return from;
		}
	 }

	 static int skipNonBlank(final char[] data, int from) {
		if (from >= data.length) {
			return from;
		}
		else {
			while (from < data.length && !Character.isSpaceChar(data[from])) from++;
			return from;
		}
	}

	 static int skipName(final char[] data, int from) {
		if (from >= data.length) {
			return from;
		}
		else {
			while (from < data.length && Character.isJavaIdentifierPart(data[from])) from++;
			return from;
		}
	}

	 static int skipNameAndAmpersand(final char[] data, int from) {
		if (from >= data.length) {
			return from;
		}
		else {
			while (from < data.length && (Character.isJavaIdentifierPart(data[from]) || data[from] == '&')) from++;
			return from;
		}
	}

	 static int skipColon(final char[] data, int from) {
		int		level = 0;
		boolean inString = false;
		
loop:	while (from < data.length) {
			switch (data[from]) {
				case '\"' 	:
					inString = !inString;
					break;
				case '\\' 	:
					if (inString && from < data.length-1 && data[from+1] == '\"') {
						from++;
					}
					break;
				case '[' 	: case '{' 	: case '(' 	: level++; break;
				case ')' 	: case ']' 	: case '}' 	: 
					if (level > 0) {
						level--;
						break;
					}
					else {
						break loop;
					}
				case ','	:
					if (!inString && level == 0) {
						return from;
					}
					break;
			}
			from++;
		}
		return from;
	}

	 static int skipExpression(final char[] data, int from) {
		int		level = 0;
		boolean inString = false;
		
		from = skipBlank(data,from);
		while (from < data.length) {
			switch (data[from]) {
				case '\"' 	:
					inString = !inString;
					break;
				case '\\' 	:
					if (inString && from < data.length-1 && data[from+1] == '\"') {
						from++;
					}
					break;
				case '[' 	: case '{' 	: case '(' 	: level++; break;
				case ')' 	: case ']' 	: case '}' 	: level--; break;
				case ' '	:
					if (!inString && level == 0) {
						return from;
					}
					break;
			}
			from++;
		}
		return from;
	}
	 
	 static int findChar(final char[] data, final int from, final int to, final char symbol) {
		 for(int index = from; index <= to; index++) {
			 if (data[index] == symbol) {
				 return index;
			 }
		 }
		 return -1;
	 }
	 
	 @FunctionalInterface
	 interface KeyValueCallback {
		 void process(char[] data, int keyFrom, final int keyTo, final int valueFrom, final int valueTo) throws IOException, SyntaxException;
	 }
	 
	 static void parseQueryPart(final char[] data, int from, final int to, final KeyValueCallback callback) throws IOException, SyntaxException {
		 int		keyFrom, keyTo, valueFrom, valueTo;
		 
		 while (from < to) {
			 keyFrom = from;
			 keyTo = from = skipName(data,from);
			 
			 if (data[from] == '=') {
				 valueFrom = from + 1;
				 while(from < to && data[from] != '&') {
					 from++;
				 }
				 if (from < to) {
					 valueTo = from - 1;
					 callback.process(data, keyFrom, keyTo, valueFrom, valueTo);
					 from++;
				 }
				 else {
					 valueTo = from;
					 callback.process(data, keyFrom, keyTo, valueFrom, valueTo);
					 break;
				 }
			 }
			 else {
				 break;
			 }
		 }
	 }
}
