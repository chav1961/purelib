package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked {@linkplain String} variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OnlineStringGetter extends OnlineObjectGetter<String> {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	String get();

	/**
	 * <p>Build {@linkplain OnlineStringGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineStringGetter} for
	 * @return {@linkplain OnlineStringGetter} built
	 */
	static OnlineStringGetter forValue(final String value) {
		return ()->value;
	}
}
