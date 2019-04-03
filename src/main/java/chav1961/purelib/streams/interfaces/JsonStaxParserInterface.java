package chav1961.purelib.streams.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface JsonStaxParserInterface extends Iterable<JsonStaxParserLexType>, Iterator<JsonStaxParserLexType>, Closeable {
	void reset() throws IOException;
	JsonStaxParserLexType current();
	JsonStaxParserInterface nested() throws IOException;
	int nameId() throws IOException;
	String name() throws IOException;
	int name(final char[] content, final int from, final int to) throws IOException;
	boolean booleanValue() throws IOException;
	long intValue() throws IOException;
	double realValue() throws IOException;
	String stringValue() throws IOException;
	int stringValue(final char[] content, final int from, final int to) throws IOException;
	long row();
	long col();
	Exception getLastError();
	
}
