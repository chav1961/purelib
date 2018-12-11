package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

class CreoleTextOutputWriter extends CreoleOutputWriter {
	private static final char[][]	UL = {"*".toCharArray(),"**".toCharArray(),"***".toCharArray(),"****".toCharArray(),"*****".toCharArray()};
	private static final char[][]	OL = {"#".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray(),"##".toCharArray()};
	private static final char[][]	H = {"=".toCharArray(),"==".toCharArray(),"===".toCharArray(),"====".toCharArray(),"=====".toCharArray(),"======".toCharArray()};
	private static final char[]	HR = "----".toCharArray();
	private static final char[]	B = "**".toCharArray();
	private static final char[]	I = "//".toCharArray();
	private static final char[]	BR = "\\\\".toCharArray();
	private static final char[]	LINK_START = "".toCharArray();
	private static final char[]	LINK_PART = "|".toCharArray();
	private static final char[]	LINK_END = "".toCharArray();
	private static final char[]	IMAGE_START = "".toCharArray();
	private static final char[]	IMAGE_PART = "|".toCharArray();
	private static final char[]	IMAGE_END = "".toCharArray();
	private static final char[]	TH = "|=".toCharArray();
	private static final char[]	TD = "|".toCharArray();
	
	private final Writer		nested;
	private final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>	epilogue;
	
	CreoleTextOutputWriter(final Writer nested, final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> prologue, final PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> epilogue) throws IOException {
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
	void internalWrite(final char[] content, final int from, final int to, final boolean keedNewLines) throws IOException, SyntaxException {
		nested.write(content,from,to-from);
	}


	@Override
	void insertImage(char[] data, int startLink, int endLink, int startCaption, int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			internalWrite(IMAGE_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(IMAGE_PART);
			internalWrite(data,startCaption,endCaption,false);
			internalWrite(IMAGE_END);
		}
		else {
			internalWrite(IMAGE_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(IMAGE_END);
		}
	}

	@Override
	void insertLink(final boolean localRef, char[] data, int startLink, int endLink, int startCaption, int endCaption) throws IOException, SyntaxException {
		if (startCaption < endCaption) {
			internalWrite(LINK_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(LINK_PART);
			internalWrite(data,startCaption,endCaption,false);
			internalWrite(LINK_END);
		}
		else {
			internalWrite(LINK_START);
			internalWrite(data,startLink,endLink,false);
			internalWrite(LINK_END);
		}
	}

	@Override
	protected void processSection(FSM<CreoleTerminals, SectionState, SectionActions, Integer> fsm, CreoleTerminals terminal, SectionState fromState, SectionState toState, SectionActions[] action, Integer parameter) throws FlowException {
		try{for (SectionActions item : action) {
				switch (item) {
					case H_OPEN	: case H_CLOSE		: 
						internalWrite(H[parameter]); 
						break;
					case HR				:
						internalWrite(HR); 
						break;
					case UL_OPEN : case UL_CLOSE	:
						internalWrite(UL[parameter]); 
						break;
					case OL_OPEN : case OL_CLOSE	:
						internalWrite(OL[parameter]); 
						break;
					case TH_OPEN : 
						internalWrite(TH);
						break;
					case TD_OPEN :
						internalWrite(TD);
						break;
					default :
				}
			}
		} catch (IOException | SyntaxException e) {
			throw new FlowException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void processFont(FSM<CreoleTerminals, FontState, FontActions, Integer> fsm, CreoleTerminals terminal, FontState fromState, FontState toState, FontActions[] action, Integer parameter) throws FlowException {
		try{for (FontActions item : action) {
				switch (item) {
					case BOLD_OPEN : case BOLD_CLOSE : 
						internalWrite(B); 
						break;
					case ITALIC_OPEN : case ITALIC_CLOSE :
						internalWrite(I);
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
	
	static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getPrologue() {
		return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
				return false;
			}
		};
	}

	static PrologueEpilogueMaster<Writer,CreoleTextOutputWriter> getEpilogue() {
		return new PrologueEpilogueMaster<Writer,CreoleTextOutputWriter>(){
			@Override
			public boolean writeContent(Writer writer, CreoleTextOutputWriter instance) throws IOException {
				return false;
			}
		};
	}
}