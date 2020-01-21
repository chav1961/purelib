package chav1961.purelib.streams.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.SyntaxException;

/**
 * <p>This interface describes output writers for different mark-up languages. Implementation of this interface calls from mark-up parsers to produce output, converted from source
 * mark-up language to another language</p>
 * @param <Terminals> terminal of {@linkplain FSM}  
 * @param <FontState> current state of font changing {@linkplain FSM} 
 * @param <SectionState> current state of section changing {@linkplain FSM}
 * @param <FontActions> font actions for given font changing {@linkplain FSM}
 * @param <SectionActions> section actions for given section changing {@linkplain FSM}
 * @param <Parameter> advanced parameter from {@linkplain FSM}
 * @since 0.0.4
 */
public interface MarkUpOutputWriter<Terminals extends Enum<?>, Parameter> {
	public enum SectionState {
		INITIAL, ORDINAL, TERMINAL, PRINT_CAPTION, PRINT_PARAGRAPH, PRINT_LIST, PRINT_LIST_ITEM, PRINT_TABLE_HEADER, PRINT_TABLE_DATA, PRINT_TABLE_DATA_END
	}
	
	public enum SectionActions {
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

	public enum FontState {
		INITIAL, ORDINAL, TERMINAL, INSIDE_BOLD, INSIDE_ITALIC, INSIDE_ITALIC_BOLD, INSIDE_BOLD_ITALIC
	}
	
	public enum FontActions {
		BOLD_OPEN, BOLD_CLOSE,
		ITALIC_OPEN, ITALIC_CLOSE,
		BR,
	}

	void startDoc() throws IOException, SyntaxException;
	void write(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException;
	void writeEscaped(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException;
	void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException;
	void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException;
	void processSection(final FSM<Terminals,SectionState,SectionActions,Parameter> fsm,final Terminals terminal,final SectionState fromState,final SectionState toState,final SectionActions[] action,final Parameter parameter) throws FlowException;
	void processFont(final FSM<Terminals,FontState,FontActions,Parameter> fsm,final Terminals terminal,final FontState fromState,final FontState toState,final FontActions[] action,final Parameter parameter) throws FlowException;
	void endDoc() throws IOException, SyntaxException;
}
