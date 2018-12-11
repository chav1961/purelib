package chav1961.purelib.basic.interfaces;

import chav1961.purelib.basic.AndOrTree;

/**
 * <p>This interface describes a quick syntax tree to store strings and associate any data to them for use in the parsers.</p> 
 * 
 * <p>The main aim of interface is to radically reduce memory and time to manipulate with the string data. All methods of the interface are based on character arrays, not strings.
 * This solution excludes any conversions between source data (usually char arrays) and their string representations. Every 'string' in the tree is identified by it's long <i>id</i>.
 * Tree is optimized to use contiguous range of id's. Every string can also be associated with the object of any kind (called <i>cargo</i>).</p> 
 * 
 * <p>The main usages of this interface are a wide range of syntax trees.</p> 
 * 
 * @see AndOrTree
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public interface SyntaxTreeInterface<T> {
	/**
	 * <p>This interface describes processing all terminal nodes in the tree.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	@FunctionalInterface
	public interface Walker<T> {
		/**
		 * <p>Process terminal node for the tree</p>
		 * @param name source array containing node name
		 * @param len length of the source array (don't use name.length in your implementations!)
		 * @param id node id
		 * @param cargo cargo associated
		 * @return false for immediately interruption of the tree walking, true otherwise 
		 */
		boolean process(char[] name, int len, long id, T cargo);
	}
	
	/**
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param value name value
	 * @param from from name index
	 * @param to to name index
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If exists, replaces cargo for the name
	 */
	long placeName(char[] value, int from, int to, T cargo);

	/**
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param value name value
	 * @param from from name index
	 * @param to to name index
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.2
	 */
	long placeOrChangeName(char[] value, int from, int to, T cargo);
	
	/**
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param name name to place into tree
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If name exists, replaces cargo for the name.
	 */
	long placeName(final String name, final T cargo);

	/**
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param name name to place into tree
	 * @param cargo cargo associated
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.2
	 */
	long placeOrChangeName(final String name, final T cargo);
	
	/**
	 * <p>Place new name into tree with the given id</p>
	 * @param value name value
	 * @param from from name index
	 * @param to to name index
	 * @param id id associated with the name. If the name already exists, has no effect
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If exists, replaces cargo for the name
	 */
	long placeName(char[] value, int from, int to, long id, T cargo);

	/**
	 * <p>Place new name into tree with the given id</p>
	 * @param source name value
	 * @param from from name index
	 * @param to to name index
	 * @param id id associated with the name. If the name already exists, has no effect
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.2
	 */
	long placeOrChangeName(final char[] source, final int from, final int to, final long id, final T cargo);
	
	/**
	 * <p>Place new name into tree with the given id</p>
	 * @param name name to place into tree
	 * @param id id associated with the name. If the name already exists, has no effect
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If exists, replaces cargo for the name
	 * @since 0.0.2
	 */
	long placeName(final String name, long id, T cargo);

	/**
	 * <p>Place new name into tree with the given id</p>
	 * @param name name to place into tree
	 * @param id id associated with the name. If the name already exists, has no effect
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.2
	 */
	long placeOrChangeName(final String name, long id, T cargo);
	
	/**
	 * <p>Seek name and return it's id</p>
	 * @param value name value to seek
	 * @param from from name index
	 * @param to to name index
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1) 
	 */
	long seekName(char[] value, int from, int to);

	/**
	 * <p>Seek name and return it's id</p>
	 * @param name name to seek
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1) 
	 */
	long seekName(String name);
	
	/**
	 * <p>Remove name from the tree</p>
	 * @param id name id to remove
	 * @return true if successful
	 */
	boolean removeName(long id);
	
	/**
	 * <p>Get cargo associated with this name</p>
	 * @param id name id
	 * @return cargo associated (can be null)
	 */
	T getCargo(long id);
	
	/**
	 * <p>Associate cargo with the given name</p>
	 * @param id name id
	 * @param cargo cargo to associate. Can be null. Replaces previous association
	 */
	void setCargo(long id, T cargo);

	/**
	 * <p>Test existence of the name with the given id</p>
	 * @param id name id to test
	 * @return true if exists
	 */
	boolean contains(long id);
	
	/**
	 * <p>Get length of the name in chars</p>
	 * @param id name id
	 * @return name length or -1 if name is missing
	 */
	int getNameLength(long id);
	
	/**
	 * <p>Get name by it's id</p>
	 * @param id name id
	 * @return name or null if missing
	 */
	String getName(long id);
	
	/**
	 * <p>Get name by it's id</p>
	 * @param id name id
	 * @param target target to place result to. Target need to have enough space to store result. Use getNameLength to get space amount 
	 * @param from from index to fill
	 * @return end position in the target. If target less than need to store data, returned value will be negative
	 */
	int getName(long id, char[] target, int from);
	
	/**
	 * <p>Compare two <i>names</i> represented by it's <i>identifiers</i></p>
	 * @param first id of the first name
	 * @param second id of the second name
	 * @return code is the same as for Comparable interface
	 */
	int compareNames(long first, long second);

	/**
	 * <p>Walk all the ids in the tree</p>
	 * @param walker callback to process each walked item
	 */
	void walk(Walker<T> walker);
	
	/**
	 * <p>Return amount of items in the tree</p>
	 * @return amount of items
	 */
	long size();

	/**
	 * <p>Clear tree content</p>
	 */
	void clear();
}
