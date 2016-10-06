package chav1961.purelib.basic.interfaces;

import chav1961.purelib.basic.AndOrTree;

/**
 * <p>This interface describes a quick syntax tree to store strings and associate any data to them.</p> 
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
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param value name value
	 * @param from from name index
	 * @param to to name index
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If exists, replaces cargo for the name
	 */
	long placeName(char[] value, int from, int to, T cargo);

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
	 * <p>Seek name and return it's id</p>
	 * @param value name value to seek
	 * @param from from name index
	 * @param to to name index
	 * @return id of the name or -1 if missing
	 */
	long seekName(char[] value, int from, int to);

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
	 * <p>Compare two names represented by it's identifiers</p>
	 * @param first id of the first name
	 * @param second id of the second name
	 * @return code is the same as for Comparable interface
	 */
	int compareNames(long first, long second);
	
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
