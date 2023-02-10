package chav1961.purelib.concurrent;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

/**
 * <p>This class is a simple implementation of listeners repository. Listener mechanism is often used in the different classes. Most of cases require thread-safe
 * implementation of the listener list, but thread-safe implementation must use synchronized sections to keep list consistency. This class reduces cost of the
 * implementation by using {@linkplain ReentrantReadWriteLock} class instead of synchronized sections. Use it for listener's lists everywhere.</p>
 * <p>This class is thread-save</p>   
 * @param <Listener> any listener type to keep in the class
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.3
 */
public class LightWeightListenerList<Listener> {
	
	/**
	 * <p>This interface describes callback for processing every listener in the listener's list.</p>
	 * @param <Listener> any listener type to keep in the class
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface LightWeightListenerCallback<Listener> {
		void fire(final Listener listener);
	}
	
	private final ReentrantReadWriteLock	lock = new ReentrantReadWriteLock();
	private final boolean					strictImplementation;
	private final Class<Listener>			listenerClass;
	private volatile Listener[]				list;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param listenerClass class of the listeners to store
	 */
	public LightWeightListenerList(final Class<Listener> listenerClass) {
		this(listenerClass,false);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param listenerClass class of the listeners to store
	 * @param strictImplementation strictly implement repository functionality. When true, implementation guarantees, that no any calls 
	 * to the given listener instance will be made after returning from {@linkplain #removeListener(Object)} method. False allows some 
	 * calls after returning from {@linkplain #removeListener(Object)}. False is good choice in most of cases.
	 */
	@SuppressWarnings("unchecked")
	public LightWeightListenerList(final Class<Listener> listenerClass, final boolean strictImplementation) {
		if (listenerClass == null) {
			throw new NullPointerException("Listener class can't be null");
		}
		else {
			this.listenerClass = listenerClass;
			this.strictImplementation = strictImplementation;
			this.list = (Listener[]) Array.newInstance(listenerClass,0);
		}
	}

	/**
	 * <p>Add new listener to the repository</p>
	 * @param listener listener to add. Can't be null
	 * @throws NullPointerException if listener to add is null
	 */
	public void addListener(final Listener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			final WriteLock	wr = lock.writeLock();
			
			try{wr.lock();
				@SuppressWarnings("unchecked")
				final Listener[]	newContent = (Listener[]) Array.newInstance(listenerClass,list.length+1);
				
				System.arraycopy(list,0,newContent,0,list.length);
				newContent[list.length] = listener;
				list = newContent; 
			} finally {
				wr.unlock();
			}
		}
	}
	
	/**
	 * <p>Remove existent listener from the repository</p>
	 * @param listener listener to remove. Can't be null
	 * @throws NullPointerException if listener to remove is null
	 */
	public void removeListener(final Listener listener) throws NullPointerException {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			final WriteLock	wr = lock.writeLock();
			
			try{wr.lock();
				for (int index = 0, maxIndex = list.length; index < maxIndex; index++) {
					if (listener.equals(list[index])) {
						@SuppressWarnings("unchecked")
						final Listener[]	newContent = (Listener[]) Array.newInstance(listenerClass,list.length-1);
						
						if (index > 0) {
							System.arraycopy(list,0,newContent,0,index);
						}
						if (index < maxIndex - 1) {
							System.arraycopy(list,index+1,newContent,index,maxIndex-index-1);
						}
						list = newContent;
						return;
					}
				}
			} finally {
				wr.unlock();
			}
		}
	}
	
	/**
	 * <p>Remove all listeners from list</p>
	 * @since 0.0.3
	 */
	public void clear() {
		final WriteLock	wr = lock.writeLock();
		
		try{wr.lock();
			Arrays.fill(list,null);
			list = (Listener[]) Array.newInstance(listenerClass,0);
		} finally {
			wr.unlock();
		}
	}
	
	/**
	 * <p>Process event for all the listeners in the repository.</p>
	 * @param callback callback to process event. This callback will be called for every listener in the repo 
	 * (order to call listeners is not predictable). Can't be null.
	 * @throws NullPointerException if callback is null
	 */
	public void fireEvent(final LightWeightListenerCallback<Listener> callback) throws NullPointerException {
		if (callback == null) {
			throw new NullPointerException("Listener's callback can't be null");
		}
		else {
			final Listener[]	items;  

			if (strictImplementation) {
				final ReadLock		rd = lock.readLock();
				
				try{rd.lock();
					items = list;
				} finally {
					rd.unlock();
				}
			}
			else {
				items = list;
			}
			for (Listener item : items) {
				try{callback.fire(item);
				} catch (Exception exc) {
					PureLibSettings.CURRENT_LOGGER.message(Severity.error, exc, exc.getLocalizedMessage());
				}
			}
		}
	}
}
