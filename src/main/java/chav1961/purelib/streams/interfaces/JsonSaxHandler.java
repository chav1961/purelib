package chav1961.purelib.streams.interfaces;

/**
 * <p>This interface describes a SAX-styled handler to parse JSON input data.</p>
 * 
 * <p>Similar to SAX parsers for XML, this interface processed any <i>events</i> on parsing data for the JSON input stream. To optimize parsing performance, 
 * all string-related methods can process as raw char array data, so data converted into string. Optional ability for the interface is an implementation of 
 * automatic decoding all 'names' in the input JSON to it's predefined ids to use them directly in the switch statements</p>
 * 
 * @see chav1961.purelib.streams.JsonSaxParser
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public interface JsonSaxHandler {
	/**
	 * <p>This event is fired on the start of the document</p>
	 */
	void startDoc();
	
	/**
	 * <p>This event is fired on the end of the document</p>
	 */
	void endDoc();
	
	/**
	 * <p>This event is fired on every opened '{'</p>
	 */
	void startObj();
	
	/**
	 * <p>This event is fired on every closed '}'</p>
	 */
	void endObj();
	
	/**
	 * <p>This event is fired on every opened '['</p>
	 */
	void startArr();
	
	/**
	 * <p>This event is fired on every closed ']'</p>
	 */
	void endArr();
	
	/**
	 * <p>This event is fired on every 'name' in the object. Differ to {@link #startName(String)}, name will not contain any escapes!</p>
	 * @param data char arrays contained name
	 * @param from starting position of the name in the array
	 * @param len length of the name detected
	 */
	void startName(char[] data, int from, int len);
	
	/**
	 * <p>This event is fired on every 'name' in the object. Escapes in the name will be processed correctly</p>
	 * @param name 'name' detected.
	 */
	void startName(String name);
	
	/**
	 * <p>This event is fired on every 'name' in the object</p>
	 * @param id predefined long id of the name was detected. Use <code> switch ((int)id) {...} </code> to operate with it
	 */
	void startName(long id);
	
	/**
	 * <p>This event is fired on every 'name' termination in the object. Single values terminate name after it's value, arrays and objects terminate names after exhausting it's data</p>
	 */
	void endName();
	
	/**
	 * <p>This event is fired before any new value of the array element</p>
	 * @param index index of the new array element
	 */
	void startIndex(int index);
	
	/**
	 * <p>This event is fired after any new value of the array element</p>
	 */
	void endIndex();
	
	/**
	 * <p>This event is fired on every string value in the JSON input. Differ to {@link #value(String)}, value will not contain any escapes!</p>
	 * @param data source array containing string data
	 * @param from starting position of the string data was detected
	 * @param len length of the string was detected
	 */
	void value(char[] data, int from, int len);
	
	/**
	 * <p>This event is fired on every string value in the object. Escapes in the value will be processed correctly</p>
	 * @param data value was extracted from JSON input
	 */
	void value(String data);
	
	/**
	 * <p>This event is fired on every integer value in the JSON input.</p>
	 * @param data value was extracted from JSON input
	 */
	void value(int data);
	
	/**
	 * <p>This event is fired on every long integer value in the JSON input.</p>
	 * @param data value was extracted from JSON input
	 */
	void value(long data);

	/**
	 * <p>This event is fired on every double value in the JSON input.</p>
	 * @param data value was extracted from JSON input
	 */
	void value(double data);
	
	/**
	 * <p>This event is fired on every boolean value in the JSON input.</p>
	 * @param data value was extracted from JSON input
	 */
	void value(boolean data);
	
	/**
	 * <p>This event is fired on every null value in the JSON input.</p>
	 */
	void value();
}
