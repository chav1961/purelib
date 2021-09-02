package chav1961.purelib.ui.swing.useful;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.ui.swing.useful.interfaces.LRUPersistence;

public class LRUManager implements Iterable<String>, AutoCloseable {
	@FunctionalInterface
	public interface LRUManagerListener {
		public static enum EventType {
			ITEM_ADDED,
			ITEM_REMOVED;
		}
		
		void process(LRUManager mgr, EventType type, String item);
	}

	private final LRUPersistence	persistence;
	private final int				maxSize;
	private final List<String>		list = new ArrayList<>();
	private final LightWeightListenerList<LRUManagerListener>	listeners = new LightWeightListenerList<>(LRUManagerListener.class); 

	public LRUManager(final LRUPersistence persistence) throws NullPointerException, IOException {
		this(persistence,0);
	}
	
	public LRUManager(final LRUPersistence persistence, final int maxSize) throws NullPointerException, IllegalArgumentException, IOException {
		if (persistence == null) {
			throw new NullPointerException("Persistence can't be null");
		}
		else if (maxSize < 0) {
			throw new IllegalArgumentException("Max size ["+maxSize+"] can't be negative");
		}
		else {
			this.persistence = persistence;
			this.maxSize = maxSize;
			
			persistence.loadLRU(list);
		}
	}

	public void addLRUManagerListener(final LRUManagerListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeLRUManagerListener(final LRUManagerListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(l);
		}
	}
	
	public void addItem(final String item) {
		if (item == null || item.isEmpty()) {
			throw new IllegalArgumentException("Item to add can't be null or empty");
		}
		else {
			final List<String>	removed = new ArrayList<>();
			
			synchronized(list) {
				if (!list.contains(item)) {
					list.add(0, item);
					if (maxSize > 0 && list.size() > maxSize) {
						for (int index = list.size() - 1; index > maxSize-1; index--) {
							removed.add(list.remove(index));
						}
					}
				}
				else {
					return;
				}
			}
			listeners.fireEvent((l)->l.process(LRUManager.this, LRUManagerListener.EventType.ITEM_ADDED, item));
			if (!removed.isEmpty()) {
				for (String rem : removed) {
					final String	remItem = rem;
					
					listeners.fireEvent((l)->l.process(LRUManager.this, LRUManagerListener.EventType.ITEM_REMOVED, remItem));
				}
			}
		}
	}

	public void removeItem(final String item) {
		if (item == null || item.isEmpty()) {
			throw new IllegalArgumentException("Item to remove can't be null or empty");
		}
		else {
			synchronized(list) {
				list.remove(item);
			}
			listeners.fireEvent((l)->l.process(LRUManager.this, LRUManagerListener.EventType.ITEM_REMOVED, item));
		}
	}
	
	@Override
	public Iterator<String> iterator() {
		synchronized(list) {
			return Arrays.asList(list.toArray(new String[list.size()])).iterator();
		}
	}

	@Override
	public void close() throws RuntimeException {
		try{persistence.saveLRU(list);
		} catch (IOException e) {
		}
	}

	@Override
	public String toString() {
		return "LRUManager [maxSize=" + maxSize + ", list=" + list + "]";
	}
}
