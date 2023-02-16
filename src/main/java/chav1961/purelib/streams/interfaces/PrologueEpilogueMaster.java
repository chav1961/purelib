package chav1961.purelib.streams.interfaces;

import java.io.IOException;

import chav1961.purelib.streams.char2char.CreoleWriter;


/**
 * <p>This interface prepares prologue/epilogue for Creole writer. You can print any html tags here for example</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @param <Wr> writer to print content required to
 * @param <T> writer instance associated with the given content
 * @see CreoleWriter  
 */
@FunctionalInterface
public interface PrologueEpilogueMaster<Wr,T> {
	boolean writeContent(Wr writer, T instance) throws IOException;
}
