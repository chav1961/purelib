package chav1961.purelib.ui;

public class HighlightItem<LexemaType> {
	public final int 		from;
	public final int		length;
	public final LexemaType	type;
	
	public HighlightItem(final int from, final int length, final LexemaType type) {
		this.from = from;
		this.length = length;
		this.type = type;
	}

	@Override
	public String toString() {
		return "HighlightItem [from=" + from + ", length=" + length + ", type=" + type + "]";
	}
}