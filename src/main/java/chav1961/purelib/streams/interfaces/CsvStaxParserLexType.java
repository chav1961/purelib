package chav1961.purelib.streams.interfaces;

public enum CsvStaxParserLexType {
	START_NAMES,	// Line 1 start
 	NAME,			// "fieldName"
	END_NAMES,		// Line 2 end
	START_DATA, 	// Line i start
	END_DATA,		// Line i end
	BOOLEAN_VALUE, 	// true/false
	INTEGER_VALUE, 	// 123
	REAL_VALUE, 	// 123.456e-7
	STRING_VALUE, 	// "text"
	NULL_VALUE,		// null
	ERROR			// any unclassified content
}
