package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import javax.xml.stream.XMLEventWriter;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LineByLineProcessorCallback;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.streams.char2char.ListManipulationStack.ListType;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

/**
 * <p>This class converts CREOLE 1.0 content to the predefined content (see {@linkplain MarkupOutputFormat} description). 
 * This class supports CREOLE syntax according to <a href="http://dirkriehle.com/uploads/2008/01/a4-junghans.pdf">junghans.pdf</a> description.
 *  XSLT can convert CREOLE to:</p>
 * <ul>
 * <li>raw XML content</li>
 * <li>text content</li>
 * <li>HTML content</li>
 * <li>XML PDF content (according to Apache FOP, see https://xmlgraphics.apache.org/fop/ )</li>
 * </ul>
 * <p>Content of the output XML is described by XSD schema creoleXML.xsd (see {@linkplain Utils#getPurelibXSD(chav1961.purelib.enumerations.XSDCollection)}). You can use
 * this description to validate XML output</p>
 * <p>The class is not thread-safe.</p>
 * 
 * @see <a href="http://www.wikicreole.org/">Creole Wiki</a>  
 * @see java.io.Writer Writer
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class CreoleWriter extends Writer {
	enum CreoleTerminals {
		TERM_CONTENT,
		TERM_SOD,
		TERM_EOD,
		TERM_SOL,
		TERM_EOL,
		TERM_P_START,
		TERM_P_END,
		TERM_UL_START,
		TERM_UL_END,
		TERM_OL_START,
		TERM_OL_END,
		TERM_LI,
		TERM_H_START,
		TERM_H_END,
		TERM_HL,
		TERM_BOLD,
		TERM_ITALIC,
		TERM_TH,
		TERM_TD,
		TERM_TABLE_END,
		TERM_BR
	}
	
	public enum CreoleLexema {
		Plain, Bold, Italic, BoldItalic,
		Paragraph, Header1, Header2, Header3, Header4, Header5, Header6,
		OrderedList1, OrderedList2, OrderedList3, OrderedList4, OrderedList5,
		UnorderedList1, UnorderedList2, UnorderedList3, UnorderedList4, UnorderedList5,
		ListMark,
		LinkRef, ImageRef,
		HorizontalLine,
		TableHeader, TableBody, 
		NonCreoleContent,
	}

	private static final char[]		LF = {'\n'};
	private static final char[]		CRLF = {'\r','\n'};
	private static final char[]		HTTP = "http://".toCharArray();
	private static final char[]		HTTPS = "https://".toCharArray();
	private static final char[]		FTP = "ftp://".toCharArray();
	private static final char[]		FTPS = "ftps://".toCharArray();
	private static final String		VALID_URL = "abcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&'()*+,;=`ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String		VALID_HEX = "0123456789abcdefABDCEF";
	
	private final LineByLineProcessorCallback	callback = new LineByLineProcessorCallback(){
													@Override
													public void processLine(final long displacement, final int lineNo, final char[] data, final int from, final int length) throws IOException, SyntaxException {
														internalProcessLine(displacement,lineNo,data,from,length);
													}
												}; 
	private final LineByLineProcessor	lblp = new LineByLineProcessor(callback);
	private final ListManipulationStack	lms = new ListManipulationStack();
	private final Writer				nested;
	private final CreoleOutputWriter	writer;
	private boolean						needParse = true, skipTheFirst = false, wasParagraph = false;

	public CreoleWriter(final Writer nested) throws IOException {
		this(nested,MarkupOutputFormat.XML2HTML);
	}

	public CreoleWriter(final Writer nested, final MarkupOutputFormat format) throws IOException {
		this(nested,format,getPrologue(format),getEpilogue(format));
	}
	
	@SuppressWarnings("unchecked")
	public <Wr,Inst> CreoleWriter(final Writer nested, final MarkupOutputFormat format, final PrologueEpilogueMaster<Wr,Inst> prologue, final PrologueEpilogueMaster<Wr,Inst> epilogue) throws IOException {
		if (nested == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else if (format == null) {
			throw new NullPointerException("CREOLE output format can't be null");
		}
		else if (prologue == null) {
			throw new NullPointerException("Prologue callback can't be null");
		}
		else if (epilogue == null) {
			throw new NullPointerException("Epilogue callback can't be null");
		}
		else {
			this.nested = nested;
			switch (format) {
				case XML 		: writer = new CreoleXMLOutputWriter(nested,(PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>)prologue,(PrologueEpilogueMaster<XMLEventWriter,CreoleXMLOutputWriter>)epilogue); break;
				case XML2TEXT	: writer = new CreoleTextOutputWriter(nested,(PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>)prologue,(PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>)epilogue); break;
				case XML2HTML	: writer = new CreoleHTMLOutputWriter(nested,(PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>)prologue,(PrologueEpilogueMaster<Writer,CreoleHTMLOutputWriter>)epilogue); break;
				case XML2PDF	: writer = new CreoleFOPOutputWriter(nested,(PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter>)prologue,(PrologueEpilogueMaster<XMLEventWriter,CreoleFOPOutputWriter>)epilogue); break;
				case PARSEDCSV	: writer = new CreoleHighlighterWriter(nested,(PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>)prologue,(PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>)epilogue); break;
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
			try{automat(0,0,0,CreoleTerminals.TERM_SOD,0);
			} catch (SyntaxException exc) {
				throw new IOException(exc.getLocalizedMessage(),exc); 
			}
		}
	}

	public <Wr,Inst> CreoleWriter(final CreoleOutputWriter writer) throws IOException {
		if (writer == null) {
			throw new NullPointerException("Output writer can't be null");
		}
		else {
			this.nested = null;
			this.writer = writer;
			try{automat(0,0,0,CreoleTerminals.TERM_SOD,0);
			} catch (SyntaxException exc) {
				throw new IOException(exc.getLocalizedMessage(),exc); 
			}
		}
	}
	
	
	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		try{lblp.write(cbuf, off, len);
		} catch (SyntaxException e) {
			throw new IOException(e.getMessage(),e);
		}
	}
 
	@Override
	public void flush() throws IOException {
		if (nested != null) {
			nested.flush();
		}
	}

	@Override
	public void close() throws IOException {
		lblp.close();
		try{automat(0,0,0,CreoleTerminals.TERM_EOD,0);
		} catch (SyntaxException exc) {
			throw new IOException(exc.getLocalizedMessage(),exc);
		}
		writer.close();
		flush();
	}

	protected void internalProcessLine(final long displacement, final int lineNo, final char[] data, int from, final int length) throws IOException, SyntaxException {
		final int	begin = from;
		int			count, start = from, to = from + length, captionCount = 0;
		boolean		wasCaption = false, escape = false;

		if (needParse) {
			if (!skipTheFirst) {
				if (data[from] == '\r' || data[from] == '\n') {
					if (wasParagraph) {
						automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_END,(data[from] == '\r' ? 2L : 1L) << 32);
						wasParagraph = false;
					}
					for (int index = lms.size(); index > 0; index--) {
						switch (lms.pop()) {
							case TYPE_UL :
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_END,index-1);
								break;
							case TYPE_OL :
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_END,index-1);
								break;
						};
					}							
					return;
				}
				
				for (; from < to; from++) {
					if (data[from] > ' ' || data[from] == '\n') {
						break;
					}
				}
				start = from;
				if (from < to && data[from] == '~') {
					escape = true;
					start = ++from;
				}
				switch (data[from]) {
					case '*' :
						count = 0;
						while (from < to && data[from] == '*') {
							from++;	count++;
						}
						if (!escape) {
							if (wasParagraph) {
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_END,0);
								wasParagraph = false;
							}
							start = from;
							if (lms.size() < count) {
								for (int index = lms.size(); index < count; index++) {
									lms.push(ListType.TYPE_UL);
									automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_START,index);
								}							
							}
							else if (lms.size() > count) {
								for (int index = lms.size(); index > count; index--) {
									switch (lms.pop()) {
										case TYPE_UL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_END,index-1);
											break;
										case TYPE_OL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_END,index-1);
											break;
									};
								}							
							}
							automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_LI,((0L + from - begin) << 32) | count);
						}
						else {
							escape = false;
						}
						break;
					case '#' :
						count = 0;
						while (from < to && data[from] == '#') {
							from++;	count++;
						}
						if (!escape) {
							if (wasParagraph) {
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_END,0);
								wasParagraph = false;
							}
							start = from;
							if (lms.size() < count) {
								for (int index = lms.size(); index < count; index++) {
									lms.push(ListType.TYPE_OL);
									automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_START,index);
								}							
							}
							else if (lms.size() < count) {
								for (int index = lms.size(); index > count; index--) {
									switch (lms.pop()) {
										case TYPE_UL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_END,index-1);
											break;
										case TYPE_OL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_END,index-1);
											break;
									};
								}							
							}
							automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_LI,((0L + from - begin) << 32) | count);
						}
						else {
							escape = false;
						}
						break;
					case '=' :
						captionCount = 0;
						start = from;
						while (from < to && data[from] == '=') {
							from++;	captionCount++;
						}
						if (captionCount >= 1 && captionCount <= 5) {
							if (!escape) {
								if (wasParagraph) {
									automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_END,0);
									wasParagraph = false;
								}
								for (int index = lms.size(); index > 0; index--) {
									switch (lms.pop()) {
										case TYPE_UL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_END,index-1);
											break;
										case TYPE_OL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_END,index-1);
											break;
									};
								}							
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_H_START,(((long)captionCount) << 32) | (captionCount-1L));
								start = from;
								wasCaption = true;
							}
							else {
								escape = false;
							}
						}
						else {
							internalWrite(displacement,lineNo,from-begin,data,start,from,false);
							automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_SOL,0);
						}
						break;
					case '-' :
						count = 0;
						while (from < to && data[from] == '-') {
							from++;	count++;
						}
						start = from;
						if (count >= 4) {
							if (!escape) {
								if (wasParagraph) {
									automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_END,0);
									wasParagraph = false;
								}
								for (int index = lms.size(); index > 0; index--) {
									switch (lms.pop()) {
										case TYPE_UL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_END,index-1);
											break;
										case TYPE_OL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_END,index-1);
											break;
									};
								}							
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_HL,count);
							}
							else {
								escape = false;
							}
						}
						else {
							from = start;
							automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_SOL,0);
						}
						break;
					case '|' :
						if (from < to - 1 && data[from + 1] == '=') {
							if (!escape) {
								if (wasParagraph) {
									automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_END,0);
									wasParagraph = false;
								}
								for (int index = lms.size(); index > 0; index--) {
									switch (lms.pop()) {
										case TYPE_UL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_END,index-1);
											break;
										case TYPE_OL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_END,index-1);
											break;
									};
								}							
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_TH,0);
								start = from += 2;
							}
							else {
								escape = false;
							}
						}
						else {
							boolean theLast = true;
							
							for (@SuppressWarnings("unused") int index = from + 1; from < to; index++) {	// The same last table divider
								if (data[from] > ' ') {
									theLast = false;
									break;
								}
							}
							if (!escape) {
								if (wasParagraph) {
									automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_END,0);
									wasParagraph = false;
								}
								for (int index = lms.size(); index > 0; index--) {
									switch (lms.pop()) {
										case TYPE_UL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_UL_END,index-1);
											break;
										case TYPE_OL :
											automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_OL_END,index-1);
											break;
									};
								}							
								automat(displacement,lineNo,from-begin,theLast ? CreoleTerminals.TERM_TABLE_END : CreoleTerminals.TERM_TD,0);						
								start = ++from;
							}
							else {
								escape = false;
							}
						}
						break;
					default :
						if (!wasParagraph && lms.size() == 0) {
							automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_P_START,0);
							wasParagraph = true;
						}
						automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_SOL,0);
						break;
				}
			}
			skipTheFirst = false;
			
loop:		for (;from < to; from++) {
				switch (data[from]) {
					case '~' :
						if (escape) {
							internalWrite(displacement,lineNo,from-begin,data,start-1,from,false);
							escape = false;
							start = from + 1;
						}
						else {
							internalWrite(displacement,lineNo,from-begin,data,start,from,false);
							escape = true;
							start = from + 1;
						}
						break;
					case '*' :
						if (from < to - 1 && data[from + 1] == '*') {
							if (!escape) {
								internalWrite(displacement,lineNo,from-begin,data,start,from,false);
								start = from + 2;
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_BOLD,2);
							}
							else {
								escape = false;
							}
						}
						break;
					case '/' :
						if (from < to - 1 && data[from + 1] == '/') {
							int		hrefEnd;
							
							if (from > 3 && CharUtils.compare(data,from-4,FTP)) {
								if (!escape) {
									internalWrite(displacement,lineNo,from-begin,data,start,from-4,false);
									hrefEnd = parseHref(data,from-4,to);
									insertLink(false,displacement+(from-4-begin),data,from-4,hrefEnd,hrefEnd,hrefEnd);
									start = from = hrefEnd;
								}
								else {
									escape = false;
								}
							}
							else if (from > 4 && (CharUtils.compare(data,from-5,HTTP) || CharUtils.compare(data,from-5,FTPS))) {
								if (!escape) {
									internalWrite(displacement,lineNo,from-begin,data,start,from-5,false);
									hrefEnd = parseHref(data,from-5,to);
									insertLink(false,displacement+(from-5-begin),data,from-5,hrefEnd,hrefEnd,hrefEnd);
									start = from = hrefEnd;
								}
								else {
									escape = false;
								}
							}
							else if (from > 5 && CharUtils.compare(data,from-6,HTTPS)) {
								if (!escape) {
									internalWrite(displacement,lineNo,from-begin,data,start,from-6,false);
									hrefEnd = parseHref(data,from-6,to);
									insertLink(false,displacement+(from-6-begin),data,from-6,hrefEnd,hrefEnd,hrefEnd);
									start = from = hrefEnd;
								}
								else {
									escape = false;
								}
							}
							else {
								if (!escape) {
									internalWrite(displacement,lineNo,from-begin,data,start,from,false);
									start = from + 2;
									automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_ITALIC,2);
								}
								else {
									escape = false;
								}
							}
						}
						break;
					case '[' :
						if (from < to - 1 && data[from + 1] == '[') {
							int		startLink, endLink, startCaption;
							boolean	isLocalRef = true;
							
							startLink = from += 2;
							while (from <  to && data[from] != '|' && data[from] != ']') {
								if (from < to - 1 && data[from] == '/' && data[from+1] == '/') {
									isLocalRef = false;
								}
								from++;
							}
							if (data[from] == '|') {
								endLink = from;
								startCaption = from + 1;
								while (from <  to - 1 && !(data[from] == ']' && data[from + 1] == ']')) {
									from++;
								}
							}
							else {
								endLink = from;
								startCaption = endLink;
							}
							if (from <  to - 1 && data[from] == ']' && data[from + 1] == ']') {
								if (!escape) {
									internalWrite(displacement,lineNo,from-begin,data,start,startLink-2,false);
									insertLink(isLocalRef,displacement+(startLink-2-begin),data,startLink,endLink,startCaption,from);
									start = from + 2;
								}
								else {
									escape = false;
								}
							}
						}
						break;
					case '=' :
						count = 0;
						while (from < to && data[from] == '=') {
							from++;	count++;
						}
						if (wasCaption && count >= 1 && count <= 5) {
							if (!escape) {
								internalWrite(displacement,lineNo,from-begin,data,start,from-count,false);
								automat(displacement,lineNo,from-begin-count,CreoleTerminals.TERM_H_END,(((long)count) << 32) | (count-1));
								start = from;
								wasCaption = false;
							}
							else {
								escape = false;
							}
						}
						break;
					case '|' :
						if (!escape) {
							internalWrite(displacement,lineNo,from-begin,data,start,from,false);
							if (from < to - 1 && data[from + 1] == '=') {
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_TH,0);
								start = from += 2;
							}
							else {
								boolean	empty = true;
								
								for (int index = from + 1; index < to; index++) {
									if (data[index] > ' ') {
										automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_TD,0);						
										start = ++from;
										empty = false;
										break;
									}
								}
								if (empty) {
									start = from + 1; 
								}
							}
						}
						else {
							escape = false;
						}
						break;
					case '{' :
						if (from < to - 1 && data[from + 1] == '{') {
							if (from < to - 2 && data[from + 2] == '{') {
								if (!escape) {
									internalWrite(displacement,lineNo,from-begin,data,start,from,false);
									from += 3;
									needParse = false;
									internalProcessLine(displacement+(from-begin),lineNo,data,from,to-from);
									return;
								}
								else {
									escape = false;
								}
							}
							else {
								int	startLink, endLink, startCaption;
								
								startLink = from += 2;
								while (from < to && data[from] != '|') {
									from++;
								}
								if (data[from] == '|') {
									endLink = from;
									startCaption = from + 1;
									while (from < to - 1 && !(data[from] == '}' && data[from + 1] == '}')) {
										from++;
									}
								}
								else {
									endLink = from;
									startCaption = endLink;
								}
								if (from < to - 1 && data[from] == '}' && data[from + 1] == '}') {
									if (!escape) {
										internalWrite(displacement,lineNo,from-begin,data,start,startLink-2,false);
										insertImage(displacement+(startLink-2-begin),data,startLink,endLink,startCaption,from);
										start = from + 2;
									}
									else {
										escape = false;
									}
								}
							}
						}
						break;
					case '\\' :
						if (from < to - 1 && data[from + 1] == '\\') {
							if (!escape) {
								internalWrite(displacement,lineNo,from-begin,data,start,from,false);
								automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_BR,1);
								start = from + 2;
							}
							else {
								escape = false;
							}
						}
						break;
					case '\r' :
						internalWrite(displacement,lineNo,from-begin,data,escape ? start - 1 : start,from,false);
						if (wasCaption) {
							automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_H_END,(captionCount-1));
							wasCaption = false;
						}
						automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_EOL,1);
						internalWrite(displacement,lineNo,from-begin,CRLF,false);
						break loop;
					case '\n' :
						internalWrite(displacement,lineNo,from-begin,data,escape ? start - 1 : start,from,false);
						if (wasCaption) {
							automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_H_END,(captionCount-1));
							wasCaption = false;
						}
						automat(displacement,lineNo,from-begin,CreoleTerminals.TERM_EOL,1);
						internalWrite(displacement,lineNo,from-begin,LF,false);
						break loop;
					default :
						break;
				}
			}
		}
		else {
			final int	temp = from;
			
			while (from < to - 2) {
				if (data[from] == '}' && data[from + 1] == '}' && data[from + 2] == '}') {
					break;
				}
				else {
					from++;
				}
			}
			if (from < to - 2 && data[from] == '}' && data[from + 1] == '}' && data[from + 2] == '}') {
				internalProcessLine(displacement+(temp-begin),lineNo,data,temp,from-temp);
				from += 3;
				needParse = true;
				start = from;
				skipTheFirst = true;
				internalProcessLine(displacement+(from-begin),lineNo,data,from,to-from);
			}
			else {
				internalWriteNonCreole(displacement,lineNo,from-begin,data,temp,to,true);
			}
		}
	}

	protected void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		writer.insertImage(displacement, data, startLink, endLink, startCaption, endCaption);
	}

	protected void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		writer.insertLink(localRef, displacement, data, startLink, endLink, startCaption, endCaption);
	}

	protected void internalWrite(final long displacement, final int lineNo, final int colNo,final char[] content, final boolean keepNewLines) throws IOException, SyntaxException {
		internalWrite(displacement,lineNo,colNo,content,0,content.length,keepNewLines);
	}

	protected void internalWrite(final long displacement, final int lineNo, final int colNo, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		if (to - from > 0 && !(to - from == 1 && content[from] == '\n' || to - from == 2 && content[from] == '\r' && content[from+1] == '\n')) {
			automat(displacement,lineNo,colNo,CreoleTerminals.TERM_CONTENT,0);
		}
		writer.internalWrite(displacement,content,from,to,keepNewLines);
	}
	
	protected void automat(final long displacement, final int lineNo, final int colNo, final CreoleTerminals terminal, final long parameter) throws IOException, SyntaxException {
		writer.automat(displacement, lineNo, colNo, terminal, parameter);
	}

	protected void internalWriteNonCreole(final long displacement, final int lineNo, final int colNo, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		writer.internalWriteNonCreole(displacement,lineNo,colNo,content,from,to,keepNewLines);
	}	
	
	protected int parseHref(final char[] data, int from, final int to) {	//  RFC 3986
		for (;from < to; from++) {
			if (data[from] == '%') {
				if (from < to - 2 && VALID_HEX.indexOf(data[from+1]) >= 0 && VALID_HEX.indexOf(data[from+2]) >= 0) {
					from += 2;
				}
				else {
					break;
				}
			}
			else if (VALID_URL.indexOf(data[from]) < 0) {
				break;
			}
		}
		return from;
	}
	
	@SuppressWarnings("unchecked")
	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getPrologue(final MarkupOutputFormat format) throws NullPointerException {
		if (format == null) {
			throw new NullPointerException("Output format can't be null"); 
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getPrologue();  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getPrologue();
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getPrologue();
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getPrologue();
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getPrologue();
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getPrologue(final MarkupOutputFormat format, final URI source) throws NullPointerException, ContentException {
		if (format == null) {
			throw new NullPointerException("Output format can't be null"); 
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getPrologue(source);  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getPrologue(source);
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getPrologue(source);
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getPrologue(source);
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getPrologue(source);
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getEpilogue(final MarkupOutputFormat format) throws NullPointerException {
		if (format == null) {
			throw new NullPointerException("Output format can't be null"); 
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getEpilogue();  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getEpilogue();
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getEpilogue();
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getEpilogue();
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getEpilogue();
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <Wr,T> PrologueEpilogueMaster<Wr,T> getEpilogue(final MarkupOutputFormat format, final URI source) throws NullPointerException, ContentException {
		if (format == null) {
			throw new NullPointerException("Output format can't be null"); 
		}
		else {
			switch (format) {
				case XML 		: return (PrologueEpilogueMaster<Wr, T>) CreoleXMLOutputWriter.getEpilogue(source);  
				case XML2TEXT	: return (PrologueEpilogueMaster<Wr, T>) CreoleTextOutputWriter.getEpilogue(source);
				case XML2HTML	: return (PrologueEpilogueMaster<Wr, T>) CreoleHTMLOutputWriter.getEpilogue(source);
				case XML2PDF	: return (PrologueEpilogueMaster<Wr, T>) CreoleFOPOutputWriter.getEpilogue(source);
				case PARSEDCSV	: return (PrologueEpilogueMaster<Wr, T>) CreoleHighlighterWriter.getEpilogue(source);
				default : throw new UnsupportedOperationException("Output format ["+format+"] is not implemented yet"); 
			}
		}
	}
}
