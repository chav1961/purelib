package chav1961.purelib.cdb;

public class SyntaxNode<Type extends Enum<?>> {
	public int					row, col;
	public long					value;
	public Object				cargo;
	public SyntaxNode<Type>[]	children;
	protected Type				type;
	
	public SyntaxNode(final int row, final int col, final Type type, final long value, final Object cargo, SyntaxNode<Type>... children) {
		if (type == null) {
			throw new NullPointerException("Type can't be null");
		}
		else {
			this.row = row;
			this.col = col;
			this.type = type;
			this.value = value;
			this.cargo = cargo;
			this.children = children;
		}
	}
	
	public Type getType() {
		return type;
	}
}
