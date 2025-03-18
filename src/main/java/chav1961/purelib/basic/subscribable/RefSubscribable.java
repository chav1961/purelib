package chav1961.purelib.basic.subscribable;

abstract class RefSubscribable<Listener> extends Subscribable<Listener> {
	protected RefSubscribable(final Class<Listener> listenerClass) throws NullPointerException {
		super(listenerClass);
	}
}
