package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleTerminals;
import chav1961.purelib.streams.interfaces.PrologueEpilogueMaster;

public class CreoleHighlighterWriter extends CreoleOutputWriter {

	private final Writer		nested;
	private final PrologueEpilogueMaster<Writer,CreoleHighlighterWriter>	epilogue;
	
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
		// TODO Auto-generated method stub
		
	}

	@Override
	void insertImage(final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	void insertLink(final boolean localRef, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processSection(final FSM<CreoleTerminals, SectionState, SectionActions, Integer> fsm, final CreoleTerminals terminal, final SectionState fromState, final SectionState toState, final SectionActions[] action, final Integer parameter) throws FlowException {
		// TODO Auto-generated method stub
//		try{for (SectionActions item : action) {
//				switch (item) {
//					case H_OPEN	: case H_CLOSE		: 
//						internalWrite(H[parameter]); 
//						break;
//					case HR				:
//						internalWrite(HR); 
//						break;
//					case UL_OPEN : case UL_CLOSE	:
//						internalWrite(UL[parameter]); 
//						break;
//					case OL_OPEN : case OL_CLOSE	:
//						internalWrite(OL[parameter]); 
//						break;
//					case TH_OPEN : 
//						internalWrite(TH);
//						break;
//					case TD_OPEN :
//						internalWrite(TD);
//						break;
//					default :
//				}
//			}
//		} catch (IOException | SyntaxException e) {
//			throw new FlowException(e.getLocalizedMessage());
//		}
	}

	@Override
	protected void processFont(final FSM<CreoleTerminals, FontState, FontActions, Integer> fsm, final CreoleTerminals terminal, final FontState fromState, final FontState toState, final FontActions[] action, final Integer parameter) throws FlowException {
		// TODO Auto-generated method stub
//		try{for (FontActions item : action) {
//				switch (item) {
//					case BOLD_OPEN : case BOLD_CLOSE : 
//						internalWrite(B); 
//						break;
//					case ITALIC_OPEN : case ITALIC_CLOSE :
//						internalWrite(I);
//						break;
//					case BR	: 
//						internalWrite(BR); 
//						break;
//					default :
//				}
//			}
//		} catch (IOException | SyntaxException e) {
//			throw new FlowException(e.getLocalizedMessage());
//		}
	}
}
