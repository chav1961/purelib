package chav1961.purelib.json.intern;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public interface MethodCaller<T> extends AutoCloseable {
	void parsePositionalParameters(JsonStaxParser parser) throws ContentException;
	void parseNamedParameters(JsonStaxParser parser) throws ContentException;
	T callMethod() throws ContentException;
	void printResult(T result,Object id,JsonStaxPrinter printer) throws ContentException;
	void close() throws RuntimeException;
}
