package chav1961.purelib.cdb;

import java.util.Arrays;

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
public class SyntaxNode<Type extends Enum<?>,Clazz extends SyntaxNode> implements Cloneable {
	/**
	 * <p>Row in source content related to the given node</p>
	 */
	public int		row;
	/**
	 * <p>Column in source content related to the given node</p>
	 */
	public int		col;
	/**
	 * <p>Current node type</p>
	 */
	public Type		type;
	/**
	 * <p>Any primitive cargo associated</p>
	 */
	public long		value;
	/**
	 * <p>Any referenced cargo associated</p>
	 */
	public Object	cargo;
	/**
	 * <p>Parent node of the given one</p>
	 */
	public Clazz	parent = null;
	/**
	 * <p>Children of the given node</p>
	 */
	public Clazz[]	children;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param row row associated
	 * @param col column associated
	 * @param type node type
	 * @param value primitive cargo of the node
	 * @param cargo referenced cargo of the node
	 * @param children children list
	 * @throws NullPointerException when node type is null
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public SyntaxNode(final int row, final int col, final Type type, final long value, final Object cargo, final Clazz... children) throws NullPointerException {
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

	/**
	 * <p>Constructor of the class</p>
	 * @param another another node instance to copy content from
	 * @throws NullPointerException when another node is null
	 */
	public SyntaxNode(final SyntaxNode<Type,Clazz> another) throws NullPointerException {
		assign(another);
	}
	
	/**
	 * <p>Assign all the content of the current node from another node</p>
	 * @param another node to assign content from
	 * @throws NullPointerException when another node is null
	 */
	@SuppressWarnings("unchecked")
	public void assign(final SyntaxNode<Type,Clazz> another) throws NullPointerException {
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
	
	@Override
	public Object clone() {
		try{return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	/**
	 * <p>Get current node type</p>
	 * @return current node type
	 */
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "SyntaxNode [row=" + row + ", col=" + col + ", type=" + type + ", value=" + value + ", cargo=" + cargo + ", parent=" + parent + ", children=" + Arrays.toString(children) + "]";
	}
}
