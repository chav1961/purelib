package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked character variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OnlineCharGetter  extends OnlineGetter {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	char get();

	/**
	 * <p>Build {@linkplain OnlineCharGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineCharGetter} for
	 * @return {@linkplain OnlineCharGetter} built
	 */
	static OnlineCharGetter forValue(final char value) {
		return ()->value;
	}
}
