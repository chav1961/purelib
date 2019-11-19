package chav1961.purelib.basic.subscribable;

@FunctionalInterface
public interface SubscribableFloatListener {
	void process(final float oldValue, final float newValue);
}