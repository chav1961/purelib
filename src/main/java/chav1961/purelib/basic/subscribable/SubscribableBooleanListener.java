package chav1961.purelib.basic.subscribable;

@FunctionalInterface
public interface SubscribableBooleanListener {
	void process(final boolean oldValue, final boolean newValue);
}