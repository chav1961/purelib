package chav1961.purelib.basic.subscribable;

/**
 * <p>This interface describes listener for String values in the {@linkplain SubscribableString} container</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@FunctionalInterface
public interface SubscribableStringListener extends SubscribableObjectListener<String> {
	/**
	 * <p>Process changes of the content value in the container</p>
	 * @param oldValue old value in the container content
	 * @param newValue new value for the container content
	 */
	void process(final String oldValue, final String newValue);
}