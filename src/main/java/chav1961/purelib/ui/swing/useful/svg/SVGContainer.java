package chav1961.purelib.ui.swing.useful.svg;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import chav1961.purelib.concurrent.LightWeightListenerList;

public class SVGContainer implements Iterable<SVGItem>{
	private final List<SVGItem>	content = new ArrayList<>();
	private final LightWeightListenerList<SVGContainerListener>	listeners = new LightWeightListenerList<>(SVGContainerListener.class);

	public static enum SVGContainerEventType {
		ADDED,
		MODIFIED,
		REMOVED,
		CLEARED
	}
	
	public static class SVGContainerEvent extends AWTEvent {
		private static final long serialVersionUID = -409993615945084051L;

		private final SVGContainerEventType 	eventType;
		private final int		position;
		private final SVGItem 	oldItem;
		private final SVGItem 	newItem;

		SVGContainerEvent(final Object source, final int id, final SVGContainerEventType eventType, final int position, final SVGItem oldItem, final SVGItem newItem) {
			super(source, id);
			this.eventType = eventType;
			this.position = position;
			this.oldItem = oldItem;
			this.newItem = newItem;
		}

		public SVGContainerEventType getEventType() {
			return eventType;
		}

		public int getPosition() {
			return position;
		}

		public SVGItem getOldItem() {
			return oldItem;
		}

		public SVGItem getNewItem() {
			return newItem;
		}
	}	
	
	@FunctionalInterface
	public static interface SVGContainerListener {
		void contentChanged(SVGContainerEvent event);
	}
	
	public SVGContainer() {
	}
	
	public boolean isEmpty() {
		return content.isEmpty();
	}
	
	public int size() {
		return content.size();
	}

	public void clear() {
		content.clear();
		final SVGContainerEvent	event = new SVGContainerEvent(this, 0, SVGContainerEventType.CLEARED, -1, null, null);
				
		listeners.fireEvent((e)->e.contentChanged(event));
	}
	
	public void add(final SVGItem value) {
		if (value == null) {
			throw new NullPointerException("Value to add can't be null");
		}
		else {
			content.add(value);
			final SVGContainerEvent	event = new SVGContainerEvent(this, 0, SVGContainerEventType.ADDED, content.size()-1, null, value);
			
			listeners.fireEvent((e)->e.contentChanged(event));
		}
	} 
	
	public SVGItem get(final int index) {
		if (index < 0 || index >= size()) {
			throw new IllegalArgumentException("Element index ["+index+"] out of range 0.."+(size()-1));
		}
		else {
			return content.get(index);
		}
	}

	public void set(final int index, final SVGItem value) {
		if (index < 0 || index >= size()) {
			throw new IllegalArgumentException("Element index ["+index+"] out of range 0.."+(size()-1));
		}
		else if (value == null) {
			throw new NullPointerException("Value to set can't be null");
		}
		else {
			final SVGItem	oldValue = content.set(index, value);
			final SVGContainerEvent	event = new SVGContainerEvent(this, 0, SVGContainerEventType.MODIFIED, index, oldValue, value);
			
			listeners.fireEvent((e)->e.contentChanged(event));
		}
	}
	
	public SVGItem remove(final int index) {
		if (index < 0 || index >= size()) {
			throw new IllegalArgumentException("Element index ["+index+"] out of range 0.."+(size()-1));
		}
		else {
			final SVGItem	oldValue = content.remove(index);
			final SVGContainerEvent	event = new SVGContainerEvent(this, 0, SVGContainerEventType.REMOVED, index, oldValue, null);
			
			listeners.fireEvent((e)->e.contentChanged(event));
			return oldValue;
		}
	}
	
	public Iterable<SVGItem> filter(final Predicate<SVGItem> pred) {
		if (pred == null) {
			throw new NullPointerException("Filter can't be null");
		}
		else {
			final List<SVGItem>	result = new ArrayList<>();
			
			for(SVGItem item : this) {
				if (pred.test(item)) {
					result.add(item);
				}
			}
			return result;
		}
	}
	
	@Override
	public Iterator<SVGItem> iterator() {
		return content.iterator();
	}
	
	public void addSVGContainerListener(final SVGContainerListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}

	public void removeSVGContainerListener(final SVGContainerListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(listener);
		}
	}
}
