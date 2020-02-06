package chav1961.purelib.cdb;

/**
 * <p>This class is a general syntax tree node in the Pure Library. It uses almost everywhere in the Pure Library classes.</p>
 * <p>This class contains:</p>
 * <ul>
 * <li>{@link #row} and {@link #col} fields to keep row and column in source code to build this node from</li> 
 * <li>{@link #type} to store current syntax node type</li> 
 * <li>{@link #value} to store numerical constants associated with the current node</li> 
 * <li>{@link #cargo} to store any type of object associated with the current node</li> 
 * <li>{@link #children} to store list of children for the current node</li> 
 * </ul>
 * <p>It's strongly recommended to use these fields as described above</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 * @param <Type> any enumeration to mark node types
 * @param <Clazz> types of parent and child syntax tree nodes. Usually are {@linkplain SyntaxNode} or it's children reference 
 */

@SuppressWarnings("rawtypes")
public class SyntaxNode<Type extends Enum<?>,Clazz extends SyntaxNode> {
	public int		row, col;
	public Type		type;
	public long		value;
	public Object	cargo;
	public Clazz	parent = null;
	public Clazz[]	children;
	
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public SyntaxNode(final int row, final int col, final Type type, final long value, final Object cargo, final Clazz... children) {
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
	
	@SuppressWarnings("unchecked")
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
