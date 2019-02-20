package chav1961.purelib.cdb;

public class SyntaxNode<Type extends Enum<?>,Clazz extends SyntaxNode<Type,Clazz>> {
	public int		row, col;
	public Type		type;
	public long		value;
	public Object	cargo;
	public Clazz	parent = null;
	public Clazz[]	children;
	
	public SyntaxNode(final int row, final int col, final Type type, final long value, final Object cargo, Clazz... children) {
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
			for (Clazz item : children) {
				item.parent = (Clazz)this;
			}
		}
	}
	
	public Type getType() {
		return type;
	}
}
