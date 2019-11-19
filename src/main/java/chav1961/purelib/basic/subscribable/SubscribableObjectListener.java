package chav1961.purelib.basic.subscribable;

@FunctionalInterface
public interface SubscribableObjectListener<T> {
	void process(final T oldValue, final T newValue);
}