package chav1961.purelib.streams.interfaces;

import java.io.IOException;

@FunctionalInterface
public interface PrologueEpilogueMaster<Wr,T> {
	boolean writeContent(Wr writer, T instance) throws IOException;
}
