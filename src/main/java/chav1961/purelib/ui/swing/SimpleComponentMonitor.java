package chav1961.purelib.ui.swing;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class SimpleComponentMonitor<T> implements JComponentMonitor {
	private final T	value;
	private T		changedValue;
	private boolean	changed = false;
	
	public SimpleComponentMonitor(final T value) {
		this.changedValue = this.value = value;
		
	}

	public boolean wasChanged() {
		return changed;
	}
	
	public T getChanged() {
		return changedValue;
	}

	public void change(final T newValue) {
		changedValue = newValue;
		changed = true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
		switch (event) {
			case Loading		:
				component.assignValueToComponent(value);
				break;
			case Rollback		:
				changedValue = value;
				changed = false;
				break;
			case Saving			:
				change((T)component.getChangedValueFromComponent());
				break;
			default:
				break;
		}
		return true;
	}
}
