package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes tree with long keys. It supports an usual set of operations on the tree.</p>  
 * @param <T> Tree value type
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public interface LongIdTreeInterface<T> {
	/**
	 * <p>This interface describes callback to process walking node in the tree.</p>
	 * @param <T> Tree value type
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public static interface WalkCallback<T> {
		/**
		 * <p>Process current walking node.</p>
		 * @param id node key
		 * @param content node value. Can be null
		 * @return true - continue walking, false - cancel walking and exit.
		 */
		boolean process(long id, T content);
	}
	
	/**
	 * <p>Does tree contain key typed</p>
	 * @param id key to test.
	 * @return true if contains, false otherwise.
	 */
	boolean contains(final long id);
	
	/**
	 * <p>Get value from the tree by it's key</p>
	 * @param id key to get value for.
	 * @return return value or null when key not found
	 */
	T get(final long id);

	/**
	 * <p>Put key/value pair into the tree. If key already exists, replace old value with the new one</p>
	 * @param id key to put.
	 * @param cargo value to put. Can be null.
	 * @return self. Can be used in the chained calls.
	 */
	LongIdTreeInterface<T> put(final long id, final T cargo);
	
	/**
	 * <p>Remove key/value pair from the tree.</p>
	 * @param id key to remove.
	 * @return removed value. Can be null.
	 */
	T remove(final long id);
	
	/**
	 * <p>Clear all tree content</p>
	 */
	void clear();

	/**
	 * <p>Walk all tree nodes</p>
	 * @param callback callback to process every walking node. Can't be null.
	 */
	void walk(final WalkCallback<T> callback);
}
