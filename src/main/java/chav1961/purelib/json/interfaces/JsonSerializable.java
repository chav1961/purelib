package chav1961.purelib.json.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

/**
 * <p>This interface decsribes any object can load and store it's state from JSON stream</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public interface JsonSerializable<Any> {
	/**
	 * <p>Load object state from JSON</p>
	 * @param parser parser to load state from. Can't be null
	 * @throws SyntaxException on any JSON errors
	 * @throws IOException on any I/O errors
	 */
	void fromJson(JsonStaxParser parser) throws SyntaxException, IOException;
	
	/**
	 * <p>Store object state into JSON</p>
	 * @param printerprinter to store state to. Can't be null
	 * @throws PrintingException on any JSON errors
	 * @throws IOException on any I/O errors
	 */
	void toJson(JsonStaxPrinter printer) throws PrintingException, IOException;
	
	/**
	 * <p>Assign inner content from other entity</p>
	 * @param other other entity. Can't be null.
	 */
	default void assignFrom(Any other) {}
}
