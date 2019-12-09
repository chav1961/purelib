package chav1961.purelib.basic.subscribable;

import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.LightWeightListenerList.LightWeightListenerCallback;

/**
 * <p>This class describes basic functionality for listenable values. Is supports dynamic list of listeners and method to call them.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @param <Listener> any listener to associate with the given instance
 */
public abstract class Subscribable<Listener> {
	private final LightWeightListenerList<Listener>	listenerList;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param listenerClass listener class to use with the instance
	 * @throws NullPointerException if listener class is null
	 */
	protected Subscribable(final Class<Listener> listenerClass) throws NullPointerException {
		this.listenerList = new LightWeightListenerList<>(listenerClass);
	}
	
	/**
	 * <p>Add listener to the listener's list of the given instance</p>
	 * @param listener listener to add. Can't be null
	 * @throws NullPointerException if listener to add is null
	 */
	public void addListener(final Listener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listenerList.addListener(listener);
		}
	}

	/**
	 * <p>Remove listener from the listener's list of the given instance</p>
	 * @param listener listener to remove. Can't be null
	 * @throws NullPointerException if listener to remove is null
	 */
	public void removeListener(final Listener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listenerList.removeListener(listener);
		}
	}
	
	/**
	 * <p>Manually fire events on all the listeners in the container</p>
	 */
	public abstract void refresh();
	
	protected void fireChange(final LightWeightListenerCallback<Listener> callback) throws NullPointerException {
		listenerList.fireEvent(callback);
	}
}
