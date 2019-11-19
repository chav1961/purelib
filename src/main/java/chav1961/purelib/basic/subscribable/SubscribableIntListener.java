package chav1961.purelib.basic.subscribable;

@FunctionalInterface
public interface SubscribableIntListener {
	void process(final int oldValue, final int newValue);
}