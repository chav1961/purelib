package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked double variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OnlineDoubleGetter  extends OnlineGetter {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	double get();

	/**
	 * <p>Build {@linkplain OnlineDoubleGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineDoubleGetter} for
	 * @return {@linkplain OnlineDoubleGetter} built
	 */
	static OnlineDoubleGetter forValue(final double value) {
		return ()->value;
	}
}
