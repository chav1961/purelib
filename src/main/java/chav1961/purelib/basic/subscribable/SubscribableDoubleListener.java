package chav1961.purelib.basic.subscribable;

@FunctionalInterface
public interface SubscribableDoubleListener {
	void process(final double oldValue, final double newValue);
}