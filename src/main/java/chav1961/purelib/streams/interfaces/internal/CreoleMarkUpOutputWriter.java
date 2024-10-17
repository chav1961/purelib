package chav1961.purelib.streams.interfaces.internal;

import java.io.Closeable;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.SyntaxException;

public interface CreoleMarkUpOutputWriter<Parameter> extends 
					MarkUpOutputWriter<CreoleTerminals
							,CreoleSectionState,CreoleSectionActions
							,CreoleFontState,CreoleFontActions
							,Parameter>
					, Closeable{
	default void startDoc() {}
	default void endDoc() {}
	void automat(long displacement, int lineNo, int colNo, CreoleTerminals terminal, long parameter) throws IOException, SyntaxException;
}
