package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked long variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OnlineLongGetter extends OnlineGetter {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	long get();

	/**
	 * <p>Build {@linkplain OnlineLongGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineLongGetter} for
	 * @return {@linkplain OnlineLongGetter} built
	 */
	static OnlineLongGetter forValue(final long value) {
		return ()->value;
	}
}
