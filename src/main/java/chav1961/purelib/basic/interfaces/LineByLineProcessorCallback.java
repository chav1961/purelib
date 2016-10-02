package chav1961.purelib.basic.interfaces;

import java.io.IOException;

/**
 * <p>This interface uses with the {@link chav1961.purelib.basic.LineByLineProcessor LineByLineProcessor} class to support lambda-styled callback for it</p>
 * 
 * @see chav1961.purelib.basic.LineByLineProcessor LineByLineProcessor
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

@FunctionalInterface
public interface LineByLineProcessorCallback {
	/**
	 * <p>Process line from the source character stream</p>
	 * @param lineNo 1-based sequential line number in the data stream
	 * @param data data stream to process. This data is in almost all cases a true source data, so you must not modify it to avoid unpredictable side effects in the caller methods
	 * @param from starting position of the actual line in the data stream
	 * @param length length of the actual line in the data stream
	 * @throws IOException is any I/O errors were detected
	 */
	void processLine(int lineNo, char[] data, int from, int length) throws IOException;
}
