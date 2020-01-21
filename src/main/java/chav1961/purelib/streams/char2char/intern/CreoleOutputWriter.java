package chav1961.purelib.streams.char2char.intern;


import java.io.Closeable;
import java.io.IOException;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public abstract class CreoleOutputWriter implements Closeable {
	protected enum SectionState {
		INITIAL, ORDINAL, TERMINAL, PRINT_CAPTION, PRINT_PARAGRAPH, PRINT_LIST, PRINT_LIST_ITEM, PRINT_TABLE_HEADER, PRINT_TABLE_DATA, PRINT_TABLE_DATA_END
	}
	
	protected enum SectionActions {
		DIV_OPEN, DIV_CLOSE,
		P_OPEN, P_CLOSE,
		H_OPEN, H_CLOSE,
		HR,
		UL_OPEN, UL_CLOSE,
		OL_OPEN, OL_CLOSE,
		LI_OPEN, LI_CLOSE,
		TABLE_OPEN, TABLE_CLOSE,
		THEAD_OPEN, THEAD_CLOSE,
		TBODY_OPEN, TBODY_CLOSE,
		TR_OPEN, TR_CLOSE,
		TH_OPEN, TH_CLOSE,
		TD_OPEN, TD_CLOSE,
		BR,
	}

	protected enum FontState {
		INITIAL, ORDINAL, TERMINAL, INSIDE_BOLD, INSIDE_ITALIC, INSIDE_ITALIC_BOLD, INSIDE_BOLD_ITALIC
	}
	
	protected enum FontActions {
		BOLD_OPEN, BOLD_CLOSE,
		ITALIC_OPEN, ITALIC_CLOSE,
		BR,
	}
	
	@SuppressWarnings("unchecked")
	protected static final FSM.FSMLine<CreoleTerminals,SectionState,SectionActions>[]	SECTION_TABLE = new FSM.FSMLine[]{
								new FSM.FSMLine<>(SectionState.INITIAL,CreoleTerminals.TERM_SOD,SectionState.ORDINAL,SectionActions.DIV_OPEN),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_H_START,SectionState.PRINT_CAPTION,SectionActions.H_OPEN),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_EOD,SectionState.TERMINAL,SectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_P_START,SectionState.PRINT_PARAGRAPH,SectionActions.P_OPEN),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_UL_START,SectionState.PRINT_LIST,SectionActions.UL_OPEN),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_OL_START,SectionState.PRINT_LIST,SectionActions.OL_OPEN),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_HL,SectionState.ORDINAL,SectionActions.HR),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_UL_START,SectionState.PRINT_LIST,SectionActions.UL_OPEN),
								new FSM.FSMLine<>(SectionState.ORDINAL,CreoleTerminals.TERM_TH,SectionState.PRINT_TABLE_HEADER,SectionActions.TABLE_OPEN,SectionActions.THEAD_OPEN,SectionActions.TR_OPEN,SectionActions.TH_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_CAPTION,CreoleTerminals.TERM_H_END,SectionState.ORDINAL,SectionActions.H_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_CAPTION,CreoleTerminals.TERM_EOD,SectionState.TERMINAL,SectionActions.H_CLOSE,SectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_PARAGRAPH,CreoleTerminals.TERM_P_END,SectionState.ORDINAL,SectionActions.P_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_PARAGRAPH,CreoleTerminals.TERM_EOD,SectionState.TERMINAL,SectionActions.P_CLOSE,SectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_LIST,CreoleTerminals.TERM_UL_START,SectionState.PRINT_LIST,SectionActions.UL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST,CreoleTerminals.TERM_UL_END,SectionState.PRINT_LIST,SectionActions.UL_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_LIST,CreoleTerminals.TERM_OL_START,SectionState.PRINT_LIST,SectionActions.OL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST,CreoleTerminals.TERM_OL_END,SectionState.PRINT_LIST,SectionActions.OL_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_LIST,CreoleTerminals.TERM_LI,SectionState.PRINT_LIST_ITEM,SectionActions.LI_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST,CreoleTerminals.TERM_P_START,SectionState.PRINT_PARAGRAPH,SectionActions.P_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST,CreoleTerminals.TERM_H_START,SectionState.PRINT_CAPTION,SectionActions.H_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_UL_START,SectionState.PRINT_LIST,SectionActions.LI_CLOSE,SectionActions.UL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_UL_END,SectionState.PRINT_LIST,SectionActions.LI_CLOSE,SectionActions.UL_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_OL_START,SectionState.PRINT_LIST,SectionActions.LI_CLOSE,SectionActions.OL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_OL_END,SectionState.PRINT_LIST,SectionActions.LI_CLOSE,SectionActions.OL_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_LI,SectionState.PRINT_LIST_ITEM,SectionActions.LI_CLOSE,SectionActions.LI_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_P_START,SectionState.PRINT_PARAGRAPH,SectionActions.P_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_H_START,SectionState.PRINT_CAPTION,SectionActions.H_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_EOD,SectionState.TERMINAL,SectionActions.LI_CLOSE,SectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_P_START,SectionState.PRINT_PARAGRAPH,SectionActions.TH_CLOSE,SectionActions.TR_CLOSE,SectionActions.THEAD_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.P_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_H_START,SectionState.PRINT_CAPTION,SectionActions.TH_CLOSE,SectionActions.TR_CLOSE,SectionActions.THEAD_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.H_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_UL_START,SectionState.PRINT_LIST,SectionActions.TH_CLOSE,SectionActions.TR_CLOSE,SectionActions.THEAD_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.UL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_OL_START,SectionState.PRINT_LIST,SectionActions.TH_CLOSE,SectionActions.TR_CLOSE,SectionActions.THEAD_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.OL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_TH,SectionState.PRINT_TABLE_HEADER,SectionActions.TH_CLOSE,SectionActions.TH_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_TD,SectionState.PRINT_TABLE_DATA,SectionActions.TH_CLOSE,SectionActions.TR_CLOSE,SectionActions.THEAD_CLOSE,SectionActions.TBODY_OPEN,SectionActions.TR_OPEN,SectionActions.TD_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_EOD,SectionState.TERMINAL,SectionActions.TH_CLOSE,SectionActions.TR_CLOSE,SectionActions.THEAD_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA,CreoleTerminals.TERM_TD,SectionState.PRINT_TABLE_DATA,SectionActions.TD_CLOSE,SectionActions.TD_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA,CreoleTerminals.TERM_TABLE_END,SectionState.PRINT_TABLE_DATA_END,SectionActions.TD_CLOSE,SectionActions.TR_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA,CreoleTerminals.TERM_EOL,SectionState.PRINT_TABLE_DATA_END,SectionActions.TD_CLOSE,SectionActions.TR_CLOSE),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_P_START,SectionState.PRINT_PARAGRAPH,SectionActions.TBODY_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.P_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_H_START,SectionState.PRINT_CAPTION,SectionActions.TBODY_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.H_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_UL_START,SectionState.PRINT_LIST,SectionActions.TBODY_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.UL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_OL_START,SectionState.PRINT_LIST,SectionActions.TBODY_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.OL_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_TD,SectionState.PRINT_TABLE_DATA,SectionActions.TR_OPEN,SectionActions.TD_OPEN),
								new FSM.FSMLine<>(SectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_EOD,SectionState.TERMINAL,SectionActions.TD_CLOSE,SectionActions.TR_CLOSE,SectionActions.TBODY_CLOSE,SectionActions.TABLE_CLOSE,SectionActions.DIV_CLOSE),
							};
	
	@SuppressWarnings("unchecked")
	protected static final FSM.FSMLine<CreoleTerminals,FontState,FontActions>[]	FONT_TABLE = new FSM.FSMLine[]{
								new FSM.FSMLine<>(FontState.INITIAL,CreoleTerminals.TERM_P_START,FontState.ORDINAL),
								new FSM.FSMLine<>(FontState.INITIAL,CreoleTerminals.TERM_LI,FontState.ORDINAL),
								new FSM.FSMLine<>(FontState.INITIAL,CreoleTerminals.TERM_TD,FontState.ORDINAL),
								new FSM.FSMLine<>(FontState.ORDINAL,CreoleTerminals.TERM_P_END,FontState.INITIAL),
								new FSM.FSMLine<>(FontState.ORDINAL,CreoleTerminals.TERM_BOLD,FontState.INSIDE_BOLD,FontActions.BOLD_OPEN),
								new FSM.FSMLine<>(FontState.ORDINAL,CreoleTerminals.TERM_ITALIC,FontState.INSIDE_ITALIC,FontActions.ITALIC_OPEN),
								new FSM.FSMLine<>(FontState.ORDINAL,CreoleTerminals.TERM_BR,FontState.ORDINAL,FontActions.BR),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_BOLD,FontState.ORDINAL,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_P_END,FontState.INITIAL,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_UL_START,FontState.INITIAL,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_UL_END,FontState.INITIAL,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_OL_START,FontState.INITIAL,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_OL_END,FontState.INITIAL,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_ITALIC,FontState.INSIDE_BOLD_ITALIC,FontActions.ITALIC_OPEN),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_BR,FontState.INSIDE_BOLD,FontActions.BR),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD,CreoleTerminals.TERM_TD,FontState.ORDINAL,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_ITALIC,FontState.ORDINAL,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_P_END,FontState.INITIAL,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_UL_START,FontState.INITIAL,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_UL_END,FontState.INITIAL,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_OL_START,FontState.INITIAL,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_OL_END,FontState.INITIAL,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_BOLD,FontState.INSIDE_ITALIC_BOLD,FontActions.BOLD_OPEN),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_BR,FontState.INSIDE_ITALIC,FontActions.BR),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC,CreoleTerminals.TERM_TD,FontState.ORDINAL,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_ITALIC,FontState.INSIDE_BOLD,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_P_END,FontState.INITIAL,FontActions.ITALIC_CLOSE,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_UL_START,FontState.INITIAL,FontActions.ITALIC_CLOSE,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_UL_END,FontState.INITIAL,FontActions.ITALIC_CLOSE,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_OL_START,FontState.INITIAL,FontActions.ITALIC_CLOSE,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_OL_END,FontState.INITIAL,FontActions.ITALIC_CLOSE,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_BR,FontState.INSIDE_BOLD_ITALIC,FontActions.BR),
								new FSM.FSMLine<>(FontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_TD,FontState.ORDINAL,FontActions.ITALIC_CLOSE,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_BOLD,FontState.INSIDE_ITALIC,FontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_P_END,FontState.INITIAL,FontActions.BOLD_CLOSE,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_UL_START,FontState.INITIAL,FontActions.BOLD_CLOSE,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_UL_END,FontState.INITIAL,FontActions.BOLD_CLOSE,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_OL_START,FontState.INITIAL,FontActions.BOLD_CLOSE,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_OL_END,FontState.INITIAL,FontActions.BOLD_CLOSE,FontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_BR,FontState.INSIDE_ITALIC_BOLD,FontActions.BR),
								new FSM.FSMLine<>(FontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_TD,FontState.ORDINAL,FontActions.BOLD_CLOSE,FontActions.ITALIC_CLOSE),
							};

	protected	long	currentDispl;
	
	private final FSM<CreoleTerminals,SectionState,SectionActions,Long>	sectionFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processSection(fsm,terminal,fromState,toState,action,parameter);},SectionState.INITIAL,SECTION_TABLE);
	private final FSM<CreoleTerminals,FontState,FontActions,Long>		fontFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processFont(fsm,terminal,fromState,toState,action,parameter);},FontState.INITIAL,FONT_TABLE);

	protected CreoleOutputWriter() {
	}
	
	public abstract void internalWrite(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException;
	public abstract void internalWriteEscaped(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException;
	public abstract void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException;
	public abstract void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException;
	public abstract void processSection(final FSM<CreoleTerminals,SectionState,SectionActions,Long> fsm,final CreoleTerminals terminal,final SectionState fromState,final SectionState toState,final SectionActions[] action,final Long parameter) throws FlowException;
	public abstract void processFont(final FSM<CreoleTerminals,FontState,FontActions,Long> fsm,final CreoleTerminals terminal,final FontState fromState,final FontState toState,final FontActions[] action,final Long parameter) throws FlowException;
	
	public void automat(final long displacement, final int lineNo, final int colNo, final CreoleTerminals terminal, final long parameter) throws IOException, SyntaxException {
		currentDispl = displacement + colNo;
		try{fontFsm.processTerminal(terminal, parameter);
			sectionFsm.processTerminal(terminal, parameter);
		} catch (FlowException e) {
			throw new SyntaxException(lineNo, colNo, e.getLocalizedMessage());
		}
	}

	protected void internalWrite(final long displacement, final char[] content) throws IOException, SyntaxException {
		internalWrite(displacement, content,false);
	}		
	
	protected void internalWrite(final long displacement, final char[] content, final boolean keepNewLines) throws IOException, SyntaxException {
		internalWrite(displacement,content,0,content.length,keepNewLines);
	}

	public void internalWriteNonCreole(long displacement, int lineNo, int colNo, char[] content, int from, int to, boolean keepNewLines) throws SyntaxException, IOException {
		internalWrite(displacement,content,0,content.length,keepNewLines);
	}		
}