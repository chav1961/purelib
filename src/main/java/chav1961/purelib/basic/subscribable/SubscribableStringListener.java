package chav1961.purelib.basic.subscribable;

@FunctionalInterface
public interface SubscribableStringListener extends SubscribableObjectListener<String> {
	void process(final String oldValue, final String newValue);
}