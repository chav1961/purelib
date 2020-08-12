package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked {@linkplain Object} variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @param <T> type of parameter kept inside the interface
 */
@FunctionalInterface
public interface OnlineObjectGetter<T> extends OnlineGetter {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	T get();

	/**
	 * <p>Build {@linkplain OnlineObjectGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineObjectGetter} for
	 * @param <T> type of parameter kept inside the interface
	 * @return {@linkplain OnlineObjectGetter} built
	 */
	static <T> OnlineObjectGetter<T> forValue(final T value) {
		return ()->value;
	}
}
