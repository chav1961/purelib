package chav1961.purelib.basic;

class XMLBasedParserLex<Type extends Enum<?>,Subtype extends Enum<?>> {
	public final Type		type;
	public final Subtype	subtype;
	public final int		row;
	public final int		col;
	
	public XMLBasedParserLex(final int row, final int col, final Type type, final Subtype subtype) {
		this.row = row;
		this.col = col;
		this.type = type;
		this.subtype = subtype;
	}

	@Override
	public String toString() {
		return "XMLBasedParserLex [type=" + type + ", subtype=" + subtype + ", row=" + row + ", col=" + col + "]";
	}
}
