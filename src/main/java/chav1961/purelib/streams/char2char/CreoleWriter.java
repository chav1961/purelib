package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;

/**
 * <p>This class is a simple converter from the Creole 1.0 to the HTML format. It matches to the <a href="http://www.wikicreole.org/attach/Creole1.0/wikicreole1.txt">Creole 1.0</a> description</p> 
 * 
 * <p>This class strongly simplifies using the Creole format in your application. The usual use case for this class is similar to:</p>
 * <code>
 * 		try(final OutputStream os = &lt;some output stream&gt;;
 * 			final Writer wr = new OutputSteamWriter(os);
 * 			final CreoleWriter cwr = new CreoleWriter(wr)) {
 * 
 * 			cwr.write(&lt;any Creole 1.0. data&gt;);
 * 		} catch (IOException exc) {
 * 			. . .
 * 		}
 * </code>
 * <p>This class is not thread-safe</p>
 * 
 * @see chav1961.purelib.basic.LineByLineProcessor LineByLineProcessor
 * @see <a href="http://www.wikicreole.org/attach/Creole1.0/wikicreole1.txt">Creole description</a>
 * @see chav1961.purelib.streams.char2char JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class CreoleWriter extends OutputStreamWriter {
	private static final char[]			P_START = "<p>".toCharArray();
	private static final char[]			P_END = "</p>".toCharArray();
	private static final char[]			UL_START = "<ul>".toCharArray();
	private static final char[]			UL_END = "</ul>".toCharArray();
	private static final char[]			OL_START = "<ol>".toCharArray();
	private static final char[]			OL_END = "</ol>".toCharArray();
	private static final char[]			LI_START = "<li>".toCharArray();
	private static final char[]			LI_END = "</li>".toCharArray();
	private static final char[]			H1_START = "<h1>".toCharArray();
	private static final char[]			H1_END = "</h1>".toCharArray();
	private static final char[]			H2_START = "<h2>".toCharArray();
	private static final char[]			H2_END = "</h2>".toCharArray();
	private static final char[]			H3_START = "<h3>".toCharArray();
	private static final char[]			H3_END = "</h3>".toCharArray();
	private static final char[]			H4_START = "<h4>".toCharArray();
	private static final char[]			H4_END = "</h4>".toCharArray();
	private static final char[]			H5_START = "<h5>".toCharArray();
	private static final char[]			H5_END = "</h5>".toCharArray();
	private static final char[]			H6_START = "<h6>".toCharArray();
	private static final char[]			H6_END = "</h6>".toCharArray();
	private static final char[]			BOLD_START = "<strong>".toCharArray();
	private static final char[]			BOLD_END = "</strong>".toCharArray();
	private static final char[]			ITALIC_START = "<em>".toCharArray();
	private static final char[]			ITALIC_END = "</em>".toCharArray();
	private static final char[]			BR = "<br/>".toCharArray();
	private static final char[]			HR = "<hr/>".toCharArray();
	private static final char[]			TABLE_START = "<table>".toCharArray();
	private static final char[]			TABLE_END = "</table>".toCharArray();
	private static final char[]			TR_START = "<tr>".toCharArray();
	private static final char[]			TR_END = "</tr>".toCharArray();
	private static final char[]			TH_START = "<th>".toCharArray();
	private static final char[]			TH_END = "</th>".toCharArray();
	private static final char[]			TD_START = "<td>".toCharArray();
	private static final char[]			TD_END = "</td>".toCharArray();
	
	private static final char[][]		HEADERS_START = new char[][]{H1_START, H2_START, H3_START, H4_START, H5_START, H6_START}; 
	private static final char[][]		HEADERS_END = new char[][]{H1_END, H2_END, H3_END, H4_END, H5_END, H6_END}; 

	private final LineByLineProcessor	lblp = new LineByLineProcessor((line,data,from,length)->processLine(line,data,from,length)); 
	private int							listULevel = 0, listOLevel = 0, tableColumnCount = 0;
	private boolean						inTable = false, inParagraph = false, turnOffParser = false;
	
	public CreoleWriter(final OutputStream out, final Charset cs) {
		super(out, cs);
	}

	public CreoleWriter(final OutputStream out, final CharsetEncoder enc) {
		super(out, enc);
	}

	public CreoleWriter(final OutputStream out, final String charsetName) throws UnsupportedEncodingException {
		super(out, charsetName);
	}

	public CreoleWriter(final OutputStream out) {
		super(out);
	}

	/**
	 * <p>Write Creole-based string and convert it to the HTML syntax</p>
	 * @see chav1961.purelib.basic.LineByLineProcessor#write(char[],int,int)
	 */
	
	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		try{lblp.write(cbuf, off, len);
		} catch (SyntaxException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		lblp.close();
		if (inParagraph) {
			print(P_END);
			inParagraph = false;
		}
		super.flush();
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(),off,len);
	}	
	
	@Override
    public void write(int c) throws IOException {
        write(new char[]{(char) c},0,1);
    }	
	
	private void processLine(final int lineNo, final char[] data, final int from, final int length) throws IOException {
		int			start = from, end, closeHeader = -1, calculated;
		boolean		needCloseLi = false, inBold = false, inItalic = false, wasTable = false, wasUList = false, wasOList = false;		

		if (!turnOffParser) {
			if (length == 1 || length == 2 && data[from+length-1] == '\r') {	// Two empty strings is a paragraph 
				if (inParagraph) {
					print(P_END);
				}
				print(P_START);
				inParagraph = true;
				return;
			}
			
			if (inTable) {	// Brief seek of the table markers
				if (calculateColumnCount(data, from, length) == 0) {
					print(TABLE_END);
					inTable = false;
				}
			}
			
			switch (data[start]) {	// Process anchored descriptors
				case '*' 	:
					int	uLevel = calculateCount('*',data,start,length);
					
					if (uLevel > listULevel) {
						if (inParagraph) {
							print(P_END);
							inParagraph = false;
						}
						while (listULevel < uLevel) {
							print(UL_START);
							listULevel++;
						}
					}
					else if (uLevel < listULevel) {
						if (inParagraph) {
							print(P_END);
							inParagraph = false;
						}
						while (listULevel > uLevel) {
							print(UL_END);
							listULevel--;
						}
					}
					print(LI_START);
					start += uLevel;
					needCloseLi = true;
					wasUList = true;
					break;
				case '#' 	:
					int	oLevel = calculateCount('#',data,start,length);
					
					if (oLevel > listOLevel) {
						if (inParagraph) {
							print(P_END);
							inParagraph = false;
						}
						while (listOLevel < oLevel) {
							print(OL_START);
							listOLevel++;
						}
					}
					else if (oLevel < listOLevel) {
						if (inParagraph) {
							print(P_END);
							inParagraph = false;
						}
						while (listOLevel > oLevel) {
							print(OL_END);
							listOLevel--;
						}
					}
					print(LI_START);
					start += oLevel;
					needCloseLi = true;
					wasOList = true;
					break;
				case '=' 	:
					int	hLevel = calculateCount('=',data,start,length);
					
					print(HEADERS_START[closeHeader = Math.min(hLevel-1,HEADERS_START.length-1)]);
					start += hLevel;
					break;
				default :
			}
			if (listULevel > 0 && !wasUList) {
				while (listULevel > 0) {
					print(UL_END);
					listULevel--;
				}
			}
			if (listOLevel > 0 && !wasOList) {
				while (listOLevel > 0) {
					print(OL_END);
					listOLevel--;
				}
			}		
			end = start;

			for (int index = start, maxIndex = from + length; index < maxIndex; index++) {
				switch (data[index]) {
					case '*' 	:
						if ((calculated = calculateCount('*',data,index,maxIndex-index)) == 2) {
							super.write(data,start,end-start);
							print((inBold = !inBold) ? BOLD_START : BOLD_END);
							end = start = (++index) + 1;
						}
						else {
							end = (index += calculated-1) + 1;
						}
						break;
					case '=' 	:
						if (closeHeader >= 0) {
							super.write(data,start,end-start);
							end = (start = (index += calculateCount('=',data,index,maxIndex-index))) + 1;
							print(HEADERS_END[closeHeader]);
							closeHeader = -1;
						}
						else {
							end = index + 1;
						}
						break;
					case '\\'	:
						if ((calculated = calculateCount('\\',data,index,maxIndex-index)) == 2) {
							super.write(data,start,end-start);
							print(BR);
							end = start = (++index) + 1;
						}
						else {
							end = (index += calculated - 1) + 1;
						}
						break;
					case '-'	:
						if ((calculated = calculateCount('-',data,index,maxIndex-index)) == 4) {
							super.write(data,start,end-start);
							print(HR);
							end = start = (index += 3) + 1;
						}
						else {
							end = (index += calculated - 1) + 1;
						}
						break;
					case '/' 	:
						if ((calculated = calculateCount('/',data,index,maxIndex-index)) == 2) {
							if (notURL(data,index,length-index)) {
								super.write(data,start,end-start);
								print((inItalic = !inItalic) ? ITALIC_START : ITALIC_END);
								end = start = (++index) + 1;
							}
							else {
								final String	link = extractLink(data,index,maxIndex-index);
								
								super.write(data,start,end-start - link.toString().indexOf('/'));
								print(("<a href=\""+link+"\">"+link+"</a>").toCharArray());
								end = start = (index += link.toString().length() - link.toString().indexOf('/'));
							}
						}
						else {
							end = (index += calculated - 1) + 1;
						}
						break;
					case '[' 	:
						if ((calculated = calculateCount('[',data,index,maxIndex-index)) == 2) {
							super.write(data,start,end-start);
							final String linkName = extractLink(data,index+2,maxIndex-index-2);
							
							index += linkName.length() + 2;
							if (index < maxIndex && data[index] == '|') {
								final int	startText = ++index;
								
								for (; index < maxIndex-1 && !(data[index] == ']' && data[index+1] == ']'); index++) {}
								print(("<a href=\""+linkName+"\">").toCharArray());
								processLine(lineNo,data,startText,index-startText);
								print("</a>".toCharArray());
							}
							else {
								for (; index < maxIndex-1 && !(data[index] == ']' && data[index+1] == ']'); index++) {}
								print(("<a href=\""+linkName+"\">"+linkName+"</a>").toCharArray());
							}
							end = start = (++index) + 1;
						}
						else {
							end = (index += calculated - 1) + 1;
						}
						break;
					case '{'	:
						if ((calculated = calculateCount('{',data,index,maxIndex-index)) == 2) {
							super.write(data,start,end-start);
							
							for (index += 2; index < maxIndex && isImageNamePart(data[index]); index++) {}
							final String	imageName = new String(data,end+2,index-end-2);
							
							if (index < maxIndex && data[index] == '|') {
								final int	startText = ++index;
								
								for (; index < maxIndex-1 && !(data[index] == '}' && data[index+1] == '}'); index++) {}
								print(("<img src=\"/img/"+imageName+"\" alt=\""+new String(data,startText,index-startText)+"\"/>").toCharArray());
							}
							else {
								for (; index < maxIndex-1 && !(data[index] == '}' && data[index+1] == '}'); index++) {}
								print(("<img src=\"/img/"+imageName+"\"/>").toCharArray());
							}
							end = start = (++index) + 1;
						}
						else if (calculated == 3) {
							super.write(data,start,end-start);
							end = start = index + 3;
							
							for (;index < maxIndex-3 && !(data[index] == '}' && data[index+1] == '}' && data[index+2] == '}');index++){}
							if (index < maxIndex-3) {
								super.write(data,start,index-start);
								end = (start = index + 3) + 1;
							}
							else {
								super.write(data,start,maxIndex-start);
								turnOffParser = true;
								end = start = index = maxIndex;
							}
						}
						else {
							end = (index += calculated - 1) + 1;
						}
						break;
					case '|'	:
						if (index < maxIndex - 1 && data[index+1] == '=') {
							super.write(data,start,end-start);
							print(TABLE_START);		
							inTable = true;		wasTable = true;
							tableColumnCount = calculateColumnCount(data, index+1, maxIndex-index-1);
							end = start = (index = processTableHeader(lineNo,data,index,maxIndex-index)) + 1;
						}
						else {
							super.write(data,start,end-start);
							if (!inTable) {
								print(TABLE_START);
								inTable = true;
								tableColumnCount = calculateColumnCount(data, index+1, maxIndex-index-1);
							}
							wasTable = true;
							end = start = (index = processTableRow(lineNo,data,index,maxIndex-index)) + 1;
						}
						break;
					case '~'	:
						if (index < maxIndex - 1) {
							super.write(data,start,end-start);
							end = (start = index+1) + 1;
						}
						break;
					default :
						end = index+1;
						break;
				}
			}
			super.write(data,start,end-start);
			
			if (needCloseLi) {
				print(LI_END);
			}
			if (closeHeader >= 0) {
				print(HEADERS_END[closeHeader]);
			}
			if (inBold) {
				print(BOLD_END);
			}
			if (inItalic) {
				print(ITALIC_END);
			}
			if (inTable) {
				if (!wasTable) {
					inTable = false;
					print(TABLE_END);
				}
			}
		}
		else {
			for (int index = start, maxIndex = from + length; index < maxIndex; index++) {
				if (index < maxIndex- 3 && data[index] == '}' && data[index+1] == '}' && data[index+2] == '}') {
					super.write(data,start,index-start);
					turnOffParser = false;
					processLine(lineNo,data,index+3,maxIndex-index-3);
					return;
				}
			}
			super.write(data,from,length);
		}
	}
	
	private int processTableRow(final int lineNo, final char[] data, final int from, final int length) throws IOException {
		int		index = from, columns = 0;
		
		print(TR_START);
		
		for (int maxIndex = from + length; index < maxIndex && columns < tableColumnCount; index++) {
			if (data[index] == '|') {
				int			start = ++index;
				
				for (;index < maxIndex && data[index] != '|'; index++){}
				print(TD_START);
				inTable = false;
				processLine(lineNo,data,start,index-start);
				inTable = true;
				print(TD_END);
				index--;
				columns++;
			}
		}
		print(TR_END);
		if (index < from + length && data[index] == '|') {
			return index;
		}
		else {
			return index-1;
		}
	}

	private int processTableHeader(final int lineNo, final char[] data, final int from, final int length) throws IOException {
		boolean	oldInTable = inTable;
		int		index = from;
		
		print(TR_START);
		for (int maxIndex = from + length; index < maxIndex; index++) {
			if (index < maxIndex-2 && data[index] == '|' && data[index+1] == '=') {
				int	start = (index += 2);
				
				for (;index < maxIndex && data[index] != '|'; index++){}
				print(TH_START);
				inTable = false;
				processLine(lineNo,data,start,index-start);
				inTable = oldInTable;
				print(TH_END);
				index--;
			}
			else if (data[index] == '|') {
				print(TR_END);
				return index;
			}
		}
		print(TR_END);
		return index-1;
	}

	private int calculateColumnCount(final char[] data, final int from, int len) {
		int	count = 0, level = 0;
		
		for (int index = from, maxIndex = from+len; index < maxIndex; index++) {
			switch (data[index]) {
				case '{' : case '[' 	:	level++; break;
				case '}' : case ']' 	:	level--; break;
				case '~' : index++; break;
				case '|' : if (level == 0) count++; break;
			}
		}
		return count;
	}
	
	private boolean notURL(final char[] data, final int from, final int len) {
		final String	link = extractLink(data,from,len);
		
		return !(link.startsWith("http://") || link.startsWith("https://") || link.startsWith("file://") || link.startsWith("ftp://"));  
	}

	private String extractLink(final char[] data, final int from, final int len) {
		int	start = from, end;
		
		while (start >= 0 && isLinkNamePart(data[start])) {start--;}
		
		for (end = start+1; end < from+len && isLinkNamePart(data[end]); end++) {}
		
		return new String(data,start+1,end-start-1); 
	}
	
	private boolean isLinkNamePart(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == ':' || c == '/';
	}

	private boolean isImageNamePart(final char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '.';
	}
	
	private int calculateCount(final char symbol, final char[] data, final int from, final int length) {
		for (int count = from; count < from+length; count++) {
			if (data[count] != symbol) {
				return count-from;
			}
		}
		return length;
	}

	private void print(final char[] data) throws IOException {
		super.write(data,0,data.length);
	}
}
