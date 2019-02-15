package chav1961.purelib.cdb;

public class SyntaxNode<Type extends Enum<?>> {
	public int					row, col;
	protected Type				type;
	public long					value;
	public Object				cargo;
	public SyntaxNode<Type>		parent = null;
	public SyntaxNode<Type>[]	children;
	
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
			for (SyntaxNode<Type> item : children) {
				item.parent = this;
			}
		}
	}
	
	public Type getType() {
		return type;
	}
}
