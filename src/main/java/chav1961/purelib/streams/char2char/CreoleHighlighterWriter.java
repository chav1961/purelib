package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2char.CreoleOutputWriter.FontActions;
import chav1961.purelib.streams.char2char.CreoleOutputWriter.SectionActions;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleLexema;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

public class CreoleHighlighterWriter extends CreoleOutputWriter {
	private static final char[][]	UL = {"*".toCharArray(),"**".toCharArray(),"***".toCharArray(),"****".toCharArray(),"*****".toCharArray()};
	private static final char[][]	OL = {"#".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray()};
	private static final char[][]	H = {"=".toCharArray(),"==".toCharArray(),"===".toCharArray(),"====".toCharArray(),"=====".toCharArray(),"======".toCharArray()};
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
	void internalWrite(final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException {
		for (int index = from; index < to; index++) {
			if (content[index] != '\r') {
				totalLen++;
			}
		}
	}

	@Override
	void insertImage(final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		int[]		forItem;
		
		if (startCaption < endCaption) {
			fontStack.add(new int[] {currentRow,currentCol,totalLen});
			internalWrite(IMAGE_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(IMAGE_PART);
			internalWrite(data,startCaption,endCaption,false);
			internalWrite(IMAGE_END);
			forItem = fontStack.remove(0);
			putLexema(CreoleLexema.ImageRef,forItem[2],totalLen-forItem[2]);
		}
		else {
			fontStack.add(new int[] {currentRow,currentCol,totalLen});
			internalWrite(IMAGE_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(IMAGE_END);
			forItem = fontStack.remove(0);
			putLexema(CreoleLexema.ImageRef,forItem[2],totalLen-forItem[2]);
		}
	}

	@Override
	void insertLink(final boolean localRef, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		int[]		forItem;
		
		if (startCaption < endCaption) {
			fontStack.add(new int[] {currentRow,currentCol,totalLen});
			internalWrite(LINK_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(LINK_PART);
			internalWrite(data,startCaption,endCaption,false);
			internalWrite(LINK_END);
			forItem = fontStack.remove(0);
			putLexema(CreoleLexema.LinkRef,forItem[2],totalLen-forItem[2]);
		}
		else {
			fontStack.add(new int[] {currentRow,currentCol,totalLen});
			internalWrite(LINK_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(LINK_END);
			forItem = fontStack.remove(0);
			putLexema(CreoleLexema.LinkRef,forItem[2],totalLen-forItem[2]);
		}
	}

	@Override
	protected void processSection(final FSM<CreoleTerminals, SectionState, SectionActions, Integer> fsm, final CreoleTerminals terminal, final SectionState fromState, final SectionState toState, final SectionActions[] action, final Integer parameter) throws FlowException {
		int[]		forItem;
		
		try{for (SectionActions item : action) {
				switch (item) {
					case P_OPEN		:
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						break;
					case P_CLOSE	:
						forItem = sectionStack.remove(0);
						putLexema(CreoleLexema.Paragraph,forItem[2],totalLen-forItem[2]);
						break;
					case H_OPEN	: 
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(H[parameter]); 
						break;
					case H_CLOSE		: 
						internalWrite(H[parameter]); 
						forItem = sectionStack.remove(0);
						putLexema(H_LEX[parameter],forItem[2],totalLen-forItem[2]);
						break;
					case HR				:
						sectionStack.add(new int[] {currentRow,currentCol,totalLen});
						internalWrite(HR); 
						forItem = sectionStack.remove(0);
						putLexema(CreoleLexema.HorizontalLine,forItem[2],totalLen-forItem[2]);
						break;
					case UL_OPEN : 
						listStack.add(0,UL_LEX[parameter]);
						internalWrite(UL[parameter]); 
						break;
					case UL_CLOSE	:
						internalWrite(UL[parameter]); 
						listStack.remove(0);
						break;
					case OL_OPEN : 
						listStack.add(0,OL_LEX[parameter]);
						internalWrite(OL[parameter]); 
						break;
					case OL_CLOSE	:
						internalWrite(OL[parameter]); 
						listStack.remove(0);
						break;
					case LI_OPEN	:
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						break;
					case LI_CLOSE	:
						forItem = sectionStack.remove(0);
						putLexema(listStack.get(0),forItem[2],totalLen-forItem[2]);
						break;
					case TH_OPEN : 
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(TH);
						break;
					case TH_CLOSE : 
						internalWrite(TH);
						forItem = sectionStack.remove(0);
						putLexema(CreoleLexema.TableHeader,forItem[2],totalLen-forItem[2]);
						break;
					case TD_OPEN :
						sectionStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(TD);
						break;
					case TD_CLOSE :
						internalWrite(TD);
						forItem = sectionStack.remove(0);
						putLexema(CreoleLexema.TableBody,forItem[2],totalLen-forItem[2]);
						break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void processFont(final FSM<CreoleTerminals, FontState, FontActions, Integer> fsm, final CreoleTerminals terminal, final FontState fromState, final FontState toState, final FontActions[] action, final Integer parameter) throws FlowException {
		int[]		forItem;
		
		try{for (FontActions item : action) {
				switch (item) {
					case BOLD_OPEN :
						fontStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(B); 
						break;
					case BOLD_CLOSE : 
						internalWrite(B); 
						forItem = fontStack.remove(0);
						putLexema(CreoleLexema.Bold,forItem[2],totalLen-forItem[2]);
						break;
					case ITALIC_OPEN : 
						fontStack.add(0,new int[] {currentRow,currentCol,totalLen});
						internalWrite(I);
						break;
					case ITALIC_CLOSE :
						internalWrite(I);
						forItem = fontStack.remove(0);
						putLexema(CreoleLexema.Italic,forItem[2],totalLen-forItem[2]);
						break;
					case BR	: 
						internalWrite(BR); 
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
}
