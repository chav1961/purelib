package chav1961.purelib.basic.subscribable;

import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.LightWeightListenerList.LightWeightListenerCallback;

public abstract class Subscribable<Listener> {
	private final LightWeightListenerList<Listener>	listenerList;
	
	protected Subscribable(final Class<Listener> listenerClass) {
		this.listenerList = new LightWeightListenerList<>(listenerClass);
	}
	
	public void addListener(final Listener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listenerList.addListener(listener);
		}
	}

	public void removeListener(final Listener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listenerList.removeListener(listener);
		}
	}
	
	public abstract void refresh();
	
	protected void fireChange(LightWeightListenerCallback<Listener> callback) {
		listenerList.fireEvent(callback);
	}
}
