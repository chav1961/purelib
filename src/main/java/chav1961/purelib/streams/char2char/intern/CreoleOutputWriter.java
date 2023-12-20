package chav1961.purelib.streams.char2char.intern;


import java.io.IOException;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.intern.CreoleFontActions;
import chav1961.purelib.streams.interfaces.intern.CreoleFontState;
import chav1961.purelib.streams.interfaces.intern.CreoleMarkUpOutputWriter;
import chav1961.purelib.streams.interfaces.intern.CreoleSectionActions;
import chav1961.purelib.streams.interfaces.intern.CreoleSectionState;
import chav1961.purelib.streams.interfaces.intern.CreoleTerminals;

abstract class CreoleOutputWriter implements CreoleMarkUpOutputWriter<Long> {
	@SuppressWarnings("unchecked")
	protected static final FSM.FSMLine<CreoleTerminals,CreoleSectionState,CreoleSectionActions>[]	SECTION_TABLE = new FSM.FSMLine[]{
								new FSM.FSMLine<>(CreoleSectionState.INITIAL,CreoleTerminals.TERM_SOD,CreoleSectionState.ORDINAL,CreoleSectionActions.DIV_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_H_START,CreoleSectionState.PRINT_CAPTION,CreoleSectionActions.H_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_EOD,CreoleSectionState.TERMINAL,CreoleSectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_P_START,CreoleSectionState.PRINT_PARAGRAPH,CreoleSectionActions.P_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_UL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.UL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_OL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.OL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_HL,CreoleSectionState.ORDINAL,CreoleSectionActions.HR),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_UL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.UL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.ORDINAL,CreoleTerminals.TERM_TH,CreoleSectionState.PRINT_TABLE_HEADER,CreoleSectionActions.TABLE_OPEN,CreoleSectionActions.THEAD_OPEN,CreoleSectionActions.TR_OPEN,CreoleSectionActions.TH_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_CAPTION,CreoleTerminals.TERM_H_END,CreoleSectionState.ORDINAL,CreoleSectionActions.H_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_CAPTION,CreoleTerminals.TERM_EOD,CreoleSectionState.TERMINAL,CreoleSectionActions.H_CLOSE,CreoleSectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_PARAGRAPH,CreoleTerminals.TERM_P_END,CreoleSectionState.ORDINAL,CreoleSectionActions.P_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_PARAGRAPH,CreoleTerminals.TERM_EOD,CreoleSectionState.TERMINAL,CreoleSectionActions.P_CLOSE,CreoleSectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_UL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.UL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_UL_END,CreoleSectionState.PRINT_LIST,CreoleSectionActions.UL_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_OL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.OL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_OL_END,CreoleSectionState.PRINT_LIST,CreoleSectionActions.OL_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_LI,CreoleSectionState.PRINT_LIST_ITEM,CreoleSectionActions.LI_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_P_START,CreoleSectionState.PRINT_PARAGRAPH,CreoleSectionActions.P_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_H_START,CreoleSectionState.PRINT_CAPTION,CreoleSectionActions.H_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_TH,CreoleSectionState.PRINT_TABLE_HEADER,CreoleSectionActions.TABLE_OPEN,CreoleSectionActions.THEAD_OPEN,CreoleSectionActions.TR_OPEN,CreoleSectionActions.TH_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST,CreoleTerminals.TERM_HL,CreoleSectionState.ORDINAL,CreoleSectionActions.HR),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_UL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.LI_CLOSE,CreoleSectionActions.UL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_UL_END,CreoleSectionState.PRINT_LIST,CreoleSectionActions.LI_CLOSE,CreoleSectionActions.UL_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_OL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.LI_CLOSE,CreoleSectionActions.OL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_OL_END,CreoleSectionState.PRINT_LIST,CreoleSectionActions.LI_CLOSE,CreoleSectionActions.OL_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_LI,CreoleSectionState.PRINT_LIST_ITEM,CreoleSectionActions.LI_CLOSE,CreoleSectionActions.LI_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_P_START,CreoleSectionState.PRINT_PARAGRAPH,CreoleSectionActions.P_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_H_START,CreoleSectionState.PRINT_CAPTION,CreoleSectionActions.H_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_LIST_ITEM,CreoleTerminals.TERM_EOD,CreoleSectionState.TERMINAL,CreoleSectionActions.LI_CLOSE,CreoleSectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_P_START,CreoleSectionState.PRINT_PARAGRAPH,CreoleSectionActions.TH_CLOSE,CreoleSectionActions.TR_CLOSE,CreoleSectionActions.THEAD_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.P_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_H_START,CreoleSectionState.PRINT_CAPTION,CreoleSectionActions.TH_CLOSE,CreoleSectionActions.TR_CLOSE,CreoleSectionActions.THEAD_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.H_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_UL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.TH_CLOSE,CreoleSectionActions.TR_CLOSE,CreoleSectionActions.THEAD_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.UL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_OL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.TH_CLOSE,CreoleSectionActions.TR_CLOSE,CreoleSectionActions.THEAD_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.OL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_TH,CreoleSectionState.PRINT_TABLE_HEADER,CreoleSectionActions.TH_CLOSE,CreoleSectionActions.TH_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_TD,CreoleSectionState.PRINT_TABLE_DATA,CreoleSectionActions.TH_CLOSE,CreoleSectionActions.TR_CLOSE,CreoleSectionActions.THEAD_CLOSE,CreoleSectionActions.TBODY_OPEN,CreoleSectionActions.TR_OPEN,CreoleSectionActions.TD_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_HEADER,CreoleTerminals.TERM_EOD,CreoleSectionState.TERMINAL,CreoleSectionActions.TH_CLOSE,CreoleSectionActions.TR_CLOSE,CreoleSectionActions.THEAD_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.DIV_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA,CreoleTerminals.TERM_TD,CreoleSectionState.PRINT_TABLE_DATA,CreoleSectionActions.TD_CLOSE,CreoleSectionActions.TD_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA,CreoleTerminals.TERM_TABLE_END,CreoleSectionState.PRINT_TABLE_DATA_END,CreoleSectionActions.TD_CLOSE,CreoleSectionActions.TR_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA,CreoleTerminals.TERM_EOL,CreoleSectionState.PRINT_TABLE_DATA_END,CreoleSectionActions.TD_CLOSE,CreoleSectionActions.TR_CLOSE),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_P_START,CreoleSectionState.PRINT_PARAGRAPH,CreoleSectionActions.TBODY_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.P_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_H_START,CreoleSectionState.PRINT_CAPTION,CreoleSectionActions.TBODY_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.H_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_UL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.TBODY_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.UL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_OL_START,CreoleSectionState.PRINT_LIST,CreoleSectionActions.TBODY_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.OL_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_TD,CreoleSectionState.PRINT_TABLE_DATA,CreoleSectionActions.TR_OPEN,CreoleSectionActions.TD_OPEN),
								new FSM.FSMLine<>(CreoleSectionState.PRINT_TABLE_DATA_END,CreoleTerminals.TERM_EOD,CreoleSectionState.TERMINAL,CreoleSectionActions.TD_CLOSE,CreoleSectionActions.TR_CLOSE,CreoleSectionActions.TBODY_CLOSE,CreoleSectionActions.TABLE_CLOSE,CreoleSectionActions.DIV_CLOSE),
							};
	
	@SuppressWarnings("unchecked")
	protected static final FSM.FSMLine<CreoleTerminals,CreoleFontState,CreoleFontActions>[]	FONT_TABLE = new FSM.FSMLine[]{
								new FSM.FSMLine<>(CreoleFontState.INITIAL,CreoleTerminals.TERM_P_START,CreoleFontState.ORDINAL),
								new FSM.FSMLine<>(CreoleFontState.INITIAL,CreoleTerminals.TERM_LI,CreoleFontState.ORDINAL),
								new FSM.FSMLine<>(CreoleFontState.INITIAL,CreoleTerminals.TERM_TD,CreoleFontState.ORDINAL),
								new FSM.FSMLine<>(CreoleFontState.ORDINAL,CreoleTerminals.TERM_P_END,CreoleFontState.INITIAL),
								new FSM.FSMLine<>(CreoleFontState.ORDINAL,CreoleTerminals.TERM_BOLD,CreoleFontState.INSIDE_BOLD,CreoleFontActions.BOLD_OPEN),
								new FSM.FSMLine<>(CreoleFontState.ORDINAL,CreoleTerminals.TERM_ITALIC,CreoleFontState.INSIDE_ITALIC,CreoleFontActions.ITALIC_OPEN),
								new FSM.FSMLine<>(CreoleFontState.ORDINAL,CreoleTerminals.TERM_BR,CreoleFontState.ORDINAL,CreoleFontActions.BR),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_BOLD,CreoleFontState.ORDINAL,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_P_END,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_UL_START,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_UL_END,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_OL_START,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_OL_END,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_ITALIC,CreoleFontState.INSIDE_BOLD_ITALIC,CreoleFontActions.ITALIC_OPEN),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_BR,CreoleFontState.INSIDE_BOLD,CreoleFontActions.BR),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD,CreoleTerminals.TERM_TD,CreoleFontState.ORDINAL,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_ITALIC,CreoleFontState.ORDINAL,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_P_END,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_UL_START,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_UL_END,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_OL_START,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_OL_END,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_BOLD,CreoleFontState.INSIDE_ITALIC_BOLD,CreoleFontActions.BOLD_OPEN),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_BR,CreoleFontState.INSIDE_ITALIC,CreoleFontActions.BR),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC,CreoleTerminals.TERM_TD,CreoleFontState.ORDINAL,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_ITALIC,CreoleFontState.INSIDE_BOLD,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_P_END,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_UL_START,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_UL_END,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_OL_START,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_OL_END,CreoleFontState.INITIAL,CreoleFontActions.ITALIC_CLOSE,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_BR,CreoleFontState.INSIDE_BOLD_ITALIC,CreoleFontActions.BR),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_BOLD_ITALIC,CreoleTerminals.TERM_TD,CreoleFontState.ORDINAL,CreoleFontActions.ITALIC_CLOSE,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_BOLD,CreoleFontState.INSIDE_ITALIC,CreoleFontActions.BOLD_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_P_END,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_UL_START,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_UL_END,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_OL_START,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_OL_END,CreoleFontState.INITIAL,CreoleFontActions.BOLD_CLOSE,CreoleFontActions.ITALIC_CLOSE),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_BR,CreoleFontState.INSIDE_ITALIC_BOLD,CreoleFontActions.BR),
								new FSM.FSMLine<>(CreoleFontState.INSIDE_ITALIC_BOLD,CreoleTerminals.TERM_TD,CreoleFontState.ORDINAL,CreoleFontActions.BOLD_CLOSE,CreoleFontActions.ITALIC_CLOSE),
							};

	protected	long	currentDispl;
	
	private final FSM<CreoleTerminals,CreoleSectionState,CreoleSectionActions,Long>	sectionFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processSection(fsm,terminal,fromState,toState,action,parameter);},CreoleSectionState.INITIAL,SECTION_TABLE);
	private final FSM<CreoleTerminals,CreoleFontState,CreoleFontActions,Long>		fontFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processFont(fsm,terminal,fromState,toState,action,parameter);},CreoleFontState.INITIAL,FONT_TABLE);

	protected CreoleOutputWriter() {
	}
	
	@Override
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
		write(displacement,content,0,content.length,keepNewLines);
	}

	public void writeNonCreole(long displacement, int lineNo, int colNo, char[] content, int from, int to, boolean keepNewLines) throws SyntaxException, IOException {
		write(displacement,content,from,to,keepNewLines);
	}		
}