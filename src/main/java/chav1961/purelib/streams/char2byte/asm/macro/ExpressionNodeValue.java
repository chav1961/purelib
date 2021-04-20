package chav1961.purelib.streams.char2byte.asm.macro;

public enum ExpressionNodeValue {
	STRING(false),
	INTEGER(false),
	REAL(false),
	BOOLEAN(false),
	STRING_ARRAY(true),
	INTEGER_ARRAY(true),
	REAL_ARRAY(true),
	BOOLEAN_ARRAY(true);
	
	private final boolean isArray;
	
	private ExpressionNodeValue(boolean isArray) {
		this.isArray = isArray;
	}
	
	public boolean isArray() {
		return isArray;
	}
	
	public static ExpressionNodeValue arrayByType(final ExpressionNodeValue type) {
		if (type == null) {
			throw new NullPointerException("Type to get array for can't be null");
		}
		else if (type.isArray()) {
			throw new IllegalArgumentException("Type ["+type+"] is already array");
		}
		else {
			switch (type) {
				case BOOLEAN	: return BOOLEAN_ARRAY;
				case INTEGER	: return INTEGER_ARRAY;
				case REAL		: return REAL_ARRAY;
				case STRING		: return STRING_ARRAY;
				default : throw new UnsupportedOperationException("Type ["+type+"] is not supported yet"); 
			}
		}
	}

	public static ExpressionNodeValue typeByArray(final ExpressionNodeValue type) {
		if (type == null) {
			throw new NullPointerException("Array type to get type for can't be null");
		}
		else if (!type.isArray()) {
			throw new IllegalArgumentException("Type ["+type+"] is not an array");
		}
		else {
			switch (type) {
				case BOOLEAN_ARRAY	: return BOOLEAN;
				case INTEGER_ARRAY	: return INTEGER;
				case REAL_ARRAY		: return REAL;
				case STRING_ARRAY	: return STRING;
				default : throw new UnsupportedOperationException("Type ["+type+"] is not supported yet"); 
			}
		}
	}
}
