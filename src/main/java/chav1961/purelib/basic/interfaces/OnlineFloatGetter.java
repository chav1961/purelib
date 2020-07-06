package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked float variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OnlineFloatGetter extends OnlineGetter {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	float get();

	/**
	 * <p>Build {@linkplain OnlineFloatGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineFloatGetter} for
	 * @return {@linkplain OnlineFloatGetter} built
	 */
	static OnlineFloatGetter forValue(final float value) {
		return ()->value;
	}
}

