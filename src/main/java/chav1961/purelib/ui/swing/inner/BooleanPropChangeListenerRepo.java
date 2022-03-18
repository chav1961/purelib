package chav1961.purelib.ui.swing.inner;

import javax.swing.JComponent;

import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;

public class BooleanPropChangeListenerRepo implements BooleanPropChangeListenerSource {
	private final LightWeightListenerList<BooleanPropChangeListener>	listeners = new LightWeightListenerList<>(BooleanPropChangeListener.class);

	@Override
	public void addBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			listeners.addListener(listener);
		}
	}

	@Override
	public void removeBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(listener);
		}
	}
	
	public void fireBooleanPropChange(final JComponent source, final EventChangeType changeType, final boolean newValue) {
		final BooleanPropChangeEvent	e = new BooleanPropChangeEvent(source, 0, changeType, newValue);
		
		listeners.fireEvent((l)->l.booleanPropChange(e));
	}
}
