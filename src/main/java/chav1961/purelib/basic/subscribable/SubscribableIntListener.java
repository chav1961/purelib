package chav1961.purelib.basic.subscribable;

/**
 * <p>This interface describes listener for int values in the {@linkplain SubscribableInt} container</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface SubscribableIntListener {
	/**
	 * <p>Process changes of the content value in the container</p>
	 * @param oldValue old value in the container content
	 * @param newValue new value for the container content
	 */
	void process(final int oldValue, final int newValue);
}