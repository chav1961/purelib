package chav1961.purelib.ui.swing.interfaces;

import chav1961.purelib.ui.swing.BooleanPropChangeEvent;

@FunctionalInterface
public interface BooleanPropChangeListener {
	void booleanPropChange(final BooleanPropChangeEvent event);
}
