package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface is used to close some objects by external environment.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 * @param T - object to close 
 */

@FunctionalInterface
public interface CloseCallback<T> {
	/**
	 * <p>Make close of the object.</p>
	 * @param instance object instance to close (usually 'this')
	 */
	void close(T instance);
}
