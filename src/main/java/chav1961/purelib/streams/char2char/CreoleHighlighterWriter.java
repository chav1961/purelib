package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleLexema;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

public class CreoleHighlighterWriter extends CreoleOutputWriter {
	private static final char[][]	UL = {"*".toCharArray(),"**".toCharArray(),"***".toCharArray(),"****".toCharArray(),"*****".toCharArray()};
//	private static final char[][]	OL = {"#".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray()};
//	private static final char[][]	H = {"=".toCharArray(),"==".toCharArray(),"===".toCharArray(),"====".toCharArray(),"=====".toCharArray(),"======".toCharArray()};
	private static final char[]		HR = "----".toCharArray();
	private static final char[]		B = "**".toCharArray();
	private static final char[]		I = "//".toCharArray();
	private static final char[]		BR = "\\\\".toCharArray();
	private static final char[]		LINK_START = "[[".toCharArray();
	private static final char[]		LINK_PART = "|".toCharArray();
	private static final char[]		LINK_END = "]]".toCharArray();
	private static final char[]		IMAGE_START = "{{".toCharArray();
	private static final char[]		IMAGE_PART = "|".toCharArray();
	private static final char[]		IMAGE_END = "}}".toCharArray();
	private static final char[]		TH = "|=".toCharArray();
	private static final char[]		TD = "|".toCharArray();
	
	private static final CreoleLexema[]	H_LEX = {CreoleLexema.Header1, CreoleLexema.Header2, CreoleLexema.Header3, CreoleLexema.Header4, CreoleLexema.Header5, CreoleLexema.Header6};
	private static final CreoleLexema[]	UL_LEX = {CreoleLexema.UnorderedList1, CreoleLexema.UnorderedList2, CreoleLexema.UnorderedList3, CreoleLexema.UnorderedList4, CreoleLexema.UnorderedList5};
	private static final CreoleLexema[]	OL_LEX = {CreoleLexema.OrderedList1, CreoleLexema.OrderedList2, CreoleLexema.OrderedList3, CreoleLexema.OrderedList4, CreoleLexema.OrderedList5};

	private final Writer			nested;
	private final PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>	epilogue;
	private final List<int[]>		sectionStack = new ArrayList<>();
	private final List<CreoleLexema>	listStack = new ArrayList<>();
	private final List<int[]>		fontStack = new ArrayList<>();
	private int						currentRow = 0, currentCol = 0, totalLen = 0;
	
	
	CreoleHighlighterWriter(final Writer nested, final PrologueEpilogueMaster<Writer,CreoleHighlighterWriter> prologue, final PrologueEpilogueMaster<Writer,CreoleHighlighterWriter> epilogue) throws IOException {
		this.nested = nested;
		this.epilogue = epilogue;
		prologue.writeContent(nested,this);
	}
	
	@Override
	public void close() throws IOException {
		epilogue.writeContent(nested,this);
		nested.flush();
	}

	@Override
	void internalWrite(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		totalLen = (int)currentDispl;
	}

	void internalWriteNonCreole(long displacement, int lineNo, int colNo, char[] content, int from, int to, boolean keepNewLines) throws SyntaxException, IOException {
		internalWrite(displacement, content, from, to, keepNewLines);
		putLexema(CreoleLexema.NonCreoleContent, (int)displacement, to-from);
	}
	
	
	@Override
	void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			putLexema(CreoleLexema.ImageRef,(int)displacement,IMAGE_START.length+(endLink-startLink)+IMAGE_PART.length+(endCaption-startCaption)+IMAGE_END.length);
		}
		else {
			if (data[endLink+1] != '}') {
				putLexema(CreoleLexema.ImageRef,(int)displacement,endLink-startLink);
			}
			else {
				putLexema(CreoleLexema.ImageRef,(int)displacement,IMAGE_START.length+(endLink-startLink)+IMAGE_END.length);
			}
		}
	}

	@Override
	void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			putLexema(CreoleLexema.LinkRef,(int)displacement,LINK_START.length+(endLink-startLink)+LINK_PART.length+(endCaption-startCaption)+LINK_END.length);
		}
		else {
			if (data[endLink+1] != ']') {
				putLexema(CreoleLexema.LinkRef,(int)displacement,endLink-startLink);
			}
			else {
				putLexema(CreoleLexema.LinkRef,(int)displacement,LINK_START.length+(endLink-startLink)+LINK_END.length);
			}
		}
	}

	@Override
	protected void processSection(final FSM<CreoleTerminals, SectionState, SectionActions, Long> fsm, final CreoleTerminals terminal, final SectionState fromState, final SectionState toState, final SectionActions[] action, final Long parameter) throws FlowException {
		int[]		forItem;
		
		try{for (SectionActions item : action) {
				switch (item) {
					case P_OPEN		:
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						break;
					case P_CLOSE	:
						forItem = sectionStack.remove(0);
						putLexema(CreoleLexema.Paragraph,forItem[2],totalLen-forItem[2]+1);
						totalLen += (int)(parameter.longValue() >> 32);
						break;
					case H_OPEN	:
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						totalLen += (int)(parameter.longValue() >> 32);
						break;
					case H_CLOSE		: 
		//				totalLen += (int)(parameter.longValue() >> 32);
						forItem = sectionStack.remove(0);
						putLexema(H_LEX[parameter.intValue()],forItem[2],totalLen-forItem[2]+1);
						break;
					case HR				:
						sectionStack.add(new int[] {currentRow,currentCol,totalLen});
						internalWrite(currentDispl,HR); 
						forItem = sectionStack.remove(0);
						putLexema(CreoleLexema.HorizontalLine,forItem[2],totalLen-forItem[2]+1);
						break;
					case UL_OPEN : 
						listStack.add(0,UL_LEX[parameter.intValue()]);
						break;
					case UL_CLOSE	:
						listStack.remove(0);
						break;
					case OL_OPEN : 
						listStack.add(0,OL_LEX[parameter.intValue()]);
						break;
					case OL_CLOSE	:
						listStack.remove(0);
						break;
					case LI_OPEN	:
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen,(int)(parameter.longValue() >> 32)});
						internalWrite(currentDispl,UL[listStack.size()-1]);
						break;
					case LI_CLOSE	:
						if (sectionStack.size() > 0) {
							forItem = sectionStack.remove(0);
							putLexema(CreoleLexema.ListMark,forItem[2],forItem[3]+1);
							putLexema(listStack.get(0),forItem[2],totalLen-forItem[2]);
						}
						break;
					case TH_OPEN : 
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(currentDispl,TH);
						break;
					case TH_CLOSE : 
						internalWrite(currentDispl,TH);
						if (sectionStack.size() > 0) {
							forItem = sectionStack.remove(0);
							putLexema(CreoleLexema.TableHeader,forItem[2],totalLen-forItem[2]);
						}
						break;
					case TD_OPEN :
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(currentDispl,TD);
						break;
					case TD_CLOSE :
						internalWrite(currentDispl,TD);
						if (sectionStack.size() > 0) {
							forItem = sectionStack.remove(0);
							putLexema(CreoleLexema.TableBody,forItem[2],totalLen-forItem[2]);
						}
						break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void processFont(final FSM<CreoleTerminals, FontState, FontActions, Long> fsm, final CreoleTerminals terminal, final FontState fromState, final FontState toState, final FontActions[] action, final Long parameter) throws FlowException {
		int[]		forItem;
		
		try{for (FontActions item : action) {
				switch (item) {
					case BOLD_OPEN :
						fontStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(currentDispl,B); 
						break;
					case BOLD_CLOSE : 
						internalWrite(currentDispl,B); 
						forItem = fontStack.remove(0);
						putLexema(fontStack.size() > 0 ? CreoleLexema.BoldItalic : CreoleLexema.Bold,forItem[2],totalLen-forItem[2]+2);
						break;
					case ITALIC_OPEN : 
						fontStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(currentDispl,I);
						break;
					case ITALIC_CLOSE :
						internalWrite(currentDispl,I);
						forItem = fontStack.remove(0);
						putLexema(fontStack.size() > 0 ? CreoleLexema.BoldItalic : CreoleLexema.Italic,forItem[2],totalLen-forItem[2]+2);
						break;
					case BR	: 
						internalWrite(currentDispl,BR); 
						break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	private void putLexema(final CreoleLexema type, final int from, final int len) throws IOException {
		nested.write(type+","+from+","+len+"\n");
	}

	static PrologueEpilogueMaster<Writer,CreoleHighlighterWriter> getPrologue() {
		return new PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleHighlighterWriter instance) throws IOException {
				return false;
			}
		};
	}

	static PrologueEpilogueMaster<Writer,CreoleHighlighterWriter> getEpilogue() {
		return new PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleHighlighterWriter instance) throws IOException {
				return false;
			}
		};
	}

	static PrologueEpilogueMaster<Writer,CreoleHighlighterWriter> getPrologue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>(){
					@Override
					public boolean writeContent(Writer writer, CreoleHighlighterWriter instance) throws IOException {
						writer.write(content);
						return true;
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}

	static PrologueEpilogueMaster<Writer,CreoleHighlighterWriter> getEpilogue(final URI source) throws NullPointerException, ContentException {
		if (source == null) {
			throw new NullPointerException("Source URI can't be null"); 
		}
		else {
			try{final String	content = Utils.fromResource(source.toURL());
			
				return new PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>(){
					@Override
					public boolean writeContent(Writer writer, CreoleHighlighterWriter instance) throws IOException {
						writer.write(content);
						return true;
					}
				};
			} catch (IOException e) {
				throw new ContentException("I/O error loading content from ["+source+"]: "+e.getLocalizedMessage());
			}
		}
	}
}
