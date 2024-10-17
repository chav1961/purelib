package chav1961.purelib.streams.interfaces.internal;

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
public interface MarkUpOutputWriter<Terminals extends Enum<?>, SectionState extends Enum<?>, SectionActions extends Enum<?>, FontState extends Enum<?>, FontActions extends Enum<?>, Parameter> {
	void startDoc() throws IOException, SyntaxException;
	void write(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException;
	void writeEscaped(final long displacement, final char[] content, final int from, final int to, final boolean keepNewLines) throws IOException, SyntaxException;
	void writeNonCreole(long displacement, int lineNo, int colNo, char[] content, int from, int to, boolean keepNewLines) throws SyntaxException, IOException;	
	void insertImage(final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException;
	void insertLink(final boolean localRef, final long displacement, final char[] data, final int startLink, final int endLink, final int startCaption, final int endCaption) throws IOException, SyntaxException;
	void processSection(final FSM<Terminals,SectionState,SectionActions,Parameter> fsm,final Terminals terminal,final SectionState fromState,final SectionState toState,final SectionActions[] action,final Parameter parameter) throws FlowException;
	void processFont(final FSM<Terminals,FontState,FontActions,Parameter> fsm,final Terminals terminal,final FontState fromState,final FontState toState,final FontActions[] action,final Parameter parameter) throws FlowException;
	void endDoc() throws IOException, SyntaxException;
}
