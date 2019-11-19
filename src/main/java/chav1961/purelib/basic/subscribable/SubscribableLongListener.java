package chav1961.purelib.basic.subscribable;

@FunctionalInterface
public interface SubscribableLongListener {
	void process(final long oldValue, final long newValue);
}