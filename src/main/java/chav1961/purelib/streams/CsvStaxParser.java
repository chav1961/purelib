package chav1961.purelib.streams;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import chav1961.purelib.streams.interfaces.CsvStaxParserInterface;
import chav1961.purelib.streams.interfaces.CsvStaxParserLexType;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class CsvStaxParser implements CsvStaxParserInterface {
	private static final int	DEFAULT_BUFFER_SIZE = 8192;

	private Exception	lastError = null;
	
	public CsvStaxParser(final Reader reader) {
		this(reader,DEFAULT_BUFFER_SIZE,true);
	}

	public CsvStaxParser(final Reader reader, final int bufferSize, final boolean theSameFirstIsNames) {
		
	}

	@Override
	public Iterator<JsonStaxParserLexType> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JsonStaxParserLexType next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CsvStaxParserLexType current() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int name(char[] content, int from, int to) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean booleanValue() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long intValue() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double realValue() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String stringValue() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int stringValue(char[] content, int from, int to) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long row() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long col() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Exception getLastError() {
		return lastError;
	}
}
