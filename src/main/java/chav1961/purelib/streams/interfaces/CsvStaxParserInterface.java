package chav1961.purelib.streams.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import chav1961.purelib.basic.exceptions.SyntaxException;

public interface CsvStaxParserInterface extends Iterable<CsvStaxParserLexType>, Iterator<CsvStaxParserLexType>, Closeable {
	CsvStaxParserLexType current();
	String name() throws IOException, SyntaxException;
	int name(final char[] content, final int from, final int to) throws IOException, SyntaxException;
	boolean booleanValue() throws IOException, SyntaxException;
	long intValue() throws IOException, SyntaxException;
	double realValue() throws IOException, SyntaxException;
	String stringValue() throws IOException, SyntaxException;
	int stringValue(final char[] content, final int from, final int to) throws IOException, SyntaxException;
	long row();
	long col();
	Exception getLastError();
}
