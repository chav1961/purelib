package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked boolean variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OnlineBooleanGetter extends OnlineGetter {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	boolean get();

	/**
	 * <p>Build {@linkplain OnlineBooleanGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineBooleanGetter} for
	 * @return {@linkplain OnlineBooleanGetter} built
	 */
	static OnlineBooleanGetter forValue(final boolean value) {
		return ()->value;
	}
}
