package chav1961.purelib.basic.interfaces;

import java.util.Comparator;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.OrdinalSyntaxTree;

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
 * @see OrdinalSyntaxTree
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.8
 * @param <T> any content associated with the name inserted
 */

public interface SyntaxTreeInterface<T> {
	/**
	 * <p>Dummy interface implementation.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public static final SyntaxTreeInterface DUMMY = new SyntaxTreeInterface<>() {
														@Override public long placeName(char[] value, int from, int to, Object cargo) {return -1;}
														@Override public long placeOrChangeName(char[] value, int from, int to, Object cargo) {return -1;}
														@Override public long placeName(String name, Object cargo) {return -1;}
														@Override public long placeOrChangeName(String name, Object cargo) {return -1;}
														@Override public long placeName(char[] value, int from, int to, long id, Object cargo) {return -1;}
														@Override public long placeOrChangeName(char[] source, int from, int to, long id, Object cargo) {return -1;}
														@Override public long placeName(String name, long id, Object cargo) {return -1;}
														@Override public long placeOrChangeName(String name, long id, Object cargo) {return -1;}
														@Override public long seekName(char[] value, int from, int to) {return -1;}
														@Override public long seekName(String name) {return -1;}
														@Override public boolean removeName(long id) {return false;}
														@Override public Object getCargo(long id) {return null;}
														@Override public void setCargo(long id, Object cargo) {}
														@Override public boolean contains(long id) {return false;}
														@Override public int getNameLength(long id) {return 0;}
														@Override public String getName(long id) {return "";}
														@Override public int getName(long id, char[] target, int from) {return 0;}
														@Override public int compareNames(long first, long second) {return 0;}
														@Override public void walk(Walker walker) {}
														@Override public long longSize() {return 0;}
														@Override public void clear() {}
														@Override public void placeAll(SyntaxTreeInterface<Object> another) {}
														@Override public void placeOrChangeAll(SyntaxTreeInterface<Object> another) {}
													}; 
	
	
	/**
	 * <p>This interface describes processing all terminal nodes in the tree.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 * @param <T> any content associated with the name inserted
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
	 * @deprecated since 0.0.7, use {@linkplain #placeName(CharSequence, Object)}
	 */
	long placeName(final String name, final T cargo);

	/**
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param name name to place into tree
	 * @param cargo cargo associated
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.2
	 * @deprecated since 0.0.7, use {@linkplain #placeName(CharSequence, Object)}
	 */
	long placeOrChangeName(final String name, final T cargo);

	/**
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param name name to place into tree
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If name exists, replaces cargo for the name.
	 * @since 0.0.6
	 */
	default long placeName(final CharSequence name, final T cargo) {
		return placeName(CharUtils.toCharArray(name), 0, name.length(), cargo);
	}
	
	/**
	 * <p>Place new name into tree and assign unique id for it</p>
	 * @param name name to place into tree
	 * @param cargo cargo associated
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.6
	 */
	default long placeOrChangeName(final CharSequence name, final T cargo) {
		return placeOrChangeName(CharUtils.toCharArray(name), 0, name.length(), cargo);
	}
	
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
	 * @deprecated since 0.0.7, use {@linkplain #placeName(CharSequence, Object)}
	 */
	long placeName(final String name, long id, T cargo);

	/**
	 * <p>Place new name into tree with the given id</p>
	 * @param name name to place into tree
	 * @param id id associated with the name. If the name already exists, has no effect
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.2
	 * @deprecated since 0.0.7, use {@linkplain #placeName(CharSequence, Object)}
	 */
	long placeOrChangeName(final String name, long id, T cargo);

	/**
	 * <p>Place new name into tree with the given id</p>
	 * @param name name to place into tree
	 * @param id id associated with the name. If the name already exists, has no effect
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If exists, replaces cargo for the name
	 * @since 0.0.6
	 */
	default long placeName(final CharSequence name, long id, T cargo) {
		return placeName(CharUtils.toCharArray(name), 0, name.length(), id, cargo);
	}

	/**
	 * <p>Place new name into tree with the given id</p>
	 * @param name name to place into tree
	 * @param id id associated with the name. If the name already exists, has no effect
	 * @param cargo cargo associated (can be null)
	 * @return id of the placed name. If name exists and cargo is not assigned yet (is null), assign cargo for the name.
	 * @since 0.0.6
	 */
	default long placeOrChangeName(final CharSequence name, long id, T cargo) {
		return placeOrChangeName(CharUtils.toCharArray(name), 0, name.length(), id, cargo);
	}

	/**
	 * <p>Place content of another tree into the current one. Existent names will be skipped.</p>
	 * @param another another tree to place. Can't be null
	 * @since 0.0.8
	 */
	void placeAll(final SyntaxTreeInterface<T> another);

	/**
	 * <p>Place content of another tree into the current one. Existent names will be replaced with new cargo.</p>
	 * @param another another tree to place. Can't be null
	 * @since 0.0.8
	 */
	void placeOrChangeAll(final SyntaxTreeInterface<T> another);
	
	/**
	 * <p>Seek name and return it's id</p>
	 * @param value name value to seek
	 * @param from from name index
	 * @param to to name index
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1) 
	 */
	long seekName(char[] value, int from, int to);

	/**
	 * <p>Seek name ignoring case and return it's id</p>
	 * @param value name value to seek
	 * @param from from name index
	 * @param to to name index
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1)
	 * @since 0.0.6 
	 */
	default long seekNameI(char[] value, int from, int to) {
		return seekName(value, from, to);
	}
	
	/**
	 * <p>Seek name and return it's id</p>
	 * @param name name to seek
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1)
	 * @deprecated since 0.0.7, use {@linkplain #seekName(CharSequence)} 
	 */
	long seekName(String name);

	/**
	 * <p>Seek name ignoring case and return it's id</p>
	 * @param name name to seek
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1) 
	 * @since 0.0.6 
	 * @deprecated since 0.0.7, use {@linkplain #seekNameI(CharSequence)} 
	 */
	default long seekNameI(String name) {
		return seekName(name);
	}
	
	/**
	 * <p>Seek name and return it's id</p>
	 * @param name name to seek
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1)
	 * @since 0.0.6 
	 */
	default long seekName(CharSequence name) {
		return seekName(CharUtils.toCharArray(name), 0, name.length());
	}

	/**
	 * <p>Seek name ignoring case and return it's id</p>
	 * @param name name to seek
	 * @return id of the name or negative number if missing. Negative number value is -(position of the first different char + 1) 
	 * @since 0.0.6 
	 */
	default long seekNameI(CharSequence name) {
		return seekNameI(CharUtils.toCharArray(name), 0, name.length());
	}
	
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
	 * @return code is the same as for {@linkplain Comparator} interface. If one or both of ids are missing in the tree, comparison result is unpredictable
	 */
	int compareNames(long first, long second);

	/**
	 * <p>Walk all the ids in the tree in ascending order</p>
	 * @param walker callback to process each walked item. Can't be null
	 */
	void walk(Walker<T> walker);

	/**
	 * <p>Walk all the ids in the tree in descending order</p>
	 * @param walker callback to process each walked item. Can't be null
	 * @since 0.0.6
	 */
	default void walkBack(Walker<T> walker) {
		walk(walker);
	}

	/**
	 * <p>Walk all the ids in the tree in ascending order starting with the given prefix</p>
	 * @param prefix prefix array. Can't be null or empty
	 * @param from from position of the prefix array
	 * @param to to position of the prefix array
	 * @param walker callback to process each walked item. Can't be null
	 * @since 0.0.7
	 */
	default void walk(char[] prefix, int from, int to, Walker<T> walker) {
		
	}
	
	/**
	 * <p>Walk all the ids in the tree in descending order starting with the given prefix</p>
	 * @param prefix prefix array. Can't be null or empty
	 * @param from from position of the prefix array
	 * @param to to position of the prefix array
	 * @param walker callback to process each walked item. Can't be null
	 * @since 0.0.7
	 */
	default void walkBack(char[] prefix, int from, int to, Walker<T> walker) {
		walk(walker);
	}
	
	/**
	 * <p>Walk all the ids in the tree in descending order starting with the given prefix</p>
	 * @param prefix prefix sequence. Can't be null or empty
	 * @param walker callback to process each walked item. Can't be null
	 * @since 0.0.7
	 */
	default void walk(CharSequence prefix, Walker<T> walker) {
		walk(walker);
	}
	
	/**
	 * <p>Walk all the ids in the tree in ascending order starting with the given prefix</p>
	 * @param prefix prefix sequence. Can't be null or empty
	 * @param walker callback to process each walked item. Can't be null
	 * @since 0.0.7
	 */
	default void walkBack(CharSequence prefix, Walker<T> walker) {
		walk(walker);
	}
	
	/**
	 * <p>Return amount of items in the tree</p>
	 * @return amount of items
	 */
	default int size() {
		final long	size = longSize();
		
		if (size > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		else {
			return (int)size;
		}
	}

	/**
	 * <p>Return amount of items in the tree</p>
	 * @return amount of items
	 * @since 0.0.6
	 */
	long longSize();
	
	/**
	 * <p>Clear tree content</p>
	 */
	void clear();
	
	/**
	 * <p>Push default tree into current one. Default tree will be used for search when no data found in the search-related methods. Default tree is always read-only.</p>
	 * @param defaultTree tree to use as default. Can't be null
	 * @since 0.0.3
	 */
	default void pushDefault(SyntaxTreeInterface<T> defaultTree) {}
	
	/**
	 * <p>Pop default tree from current one.</p>
	 * @since 0.0.3
	 */
	default void popDefault() {}
}
