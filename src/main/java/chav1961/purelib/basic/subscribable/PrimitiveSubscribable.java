package chav1961.purelib.basic.subscribable;

public abstract class PrimitiveSubscribable<Listener> extends Subscribable<Listener> {

	protected PrimitiveSubscribable(final Class<Listener> listenerClass) throws NullPointerException {
		super(listenerClass);
	}
}
