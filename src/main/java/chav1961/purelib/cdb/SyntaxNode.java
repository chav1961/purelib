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
			if (children != null) {
				for (Clazz item : children) {
					item.parent = (Clazz)this;
				}
			}
		}
	}

	public SyntaxNode(final SyntaxNode<Type,Clazz> another) {
		assign(another);
	}
	
	public void assign(final SyntaxNode<Type,Clazz> another) {
		if (another == null) {
			throw new NullPointerException("Another node can't be null");
		}
		else {
			this.row = another.row;
			this.col = another.col;
			this.type = another.type;
			this.value = another.value;
			this.cargo = another.cargo;
			this.children = another.children;
			if (children != null) {
				for (Clazz item : children) {
					item.parent = (Clazz)this;
				}
			}
		}
	}
	
	public Type getType() {
		return type;
	}
}
