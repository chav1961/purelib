package chav1961.purelib.streams.char2char.intern;

import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.streams.interfaces.CsvSaxHandler;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;
import chav1961.purelib.streams.interfaces.SaxHandler;

public class SaxProcessorUtils {
	public static LineByLineProcessor createSaxProcessor(final SaxHandler handler) {
		if (handler == null) {
			throw new NullPointerException("Sax handler can' be null"); 
		}
		else if (handler instanceof CsvSaxHandler) {
			return createSaxProcessor((CsvSaxHandler)handler);
		}
		else if (handler instanceof JsonSaxHandler) {
			return createSaxProcessor((JsonSaxHandler)handler);
		}
		else {
			throw new UnsupportedOperationException("Sax handler type ["+handler.getClass().getCanonicalName()+"] is not supported yet");
		}
	}

	private static LineByLineProcessor createSaxProcessor(final CsvSaxHandler handler) {
		return null;
	}

	private static LineByLineProcessor createSaxProcessor(final JsonSaxHandler handler) {
		return null;
	}
}
