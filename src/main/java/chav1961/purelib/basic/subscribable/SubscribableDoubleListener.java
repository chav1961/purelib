package chav1961.purelib.basic.subscribable;

/**
 * <p>This interface describes listener for double values in the {@linkplain SubscribableDouble} container</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface SubscribableDoubleListener {
	/**
	 * <p>Process changes of the content value in the container</p>
	 * @param oldValue old value in the container content
	 * @param newValue new value for the container content
	 */
	void process(final double oldValue, final double newValue);
}