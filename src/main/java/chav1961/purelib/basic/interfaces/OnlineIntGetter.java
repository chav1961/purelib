package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes dynamic access to linked int variable</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface OnlineIntGetter extends OnlineGetter {
	/**
	 * <p>Get current variable value</p>
	 * @return current value
	 */
	int get();
	
	/**
	 * <p>Build {@linkplain OnlineIntGetter} implementation to immutable value</p>
	 * @param value value to build {@linkplain OnlineIntGetter} for
	 * @return {@linkplain OnlineIntGetter} built
	 */
	static OnlineIntGetter forValue(final int value) {
		return ()->value;
	}
}
