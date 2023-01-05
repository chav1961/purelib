package chav1961.purelib.streams.interfaces;

/**
 * <p>This enumeration describes types of JSON lexemas in the input stream</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 
 * @last.update 0.0.3
 */
public enum JsonStaxParserLexType {
	START_OBJECT,	// { 
	END_OBJECT,		// }
	START_ARRAY, 	// [
	END_ARRAY,		// ]
	LIST_SPLITTER, 	// ,
	NAME_SPLITTER,	// :
	BOOLEAN_VALUE, 	// true/false
	INTEGER_VALUE, 	// 123
	REAL_VALUE, 	// 123.456e-7
	STRING_VALUE, 	// "text"
	NULL_VALUE,		// null
	NAME,			// "fieldName"
	ERROR			// any unclassified content
}