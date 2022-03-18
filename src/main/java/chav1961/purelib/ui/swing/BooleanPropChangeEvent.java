package chav1961.purelib.ui.swing;

import java.awt.AWTEvent;

public class BooleanPropChangeEvent extends AWTEvent {
	private static final long serialVersionUID = 4485449474211059940L;

	public static enum EventChangeType {
		VISIBILE,
		ENABLED,
		SELECTED,
		MODIFIABLE
	}
	
	private final EventChangeType	type;
	private final boolean			newValue;
	
	public BooleanPropChangeEvent(final Object source, final int id, final EventChangeType type, final boolean newValue) {
		super(source, id);
		this.type = type;
		this.newValue = newValue;
	}

	public EventChangeType getEventChangeType() {
		return type;
	}

	public boolean getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		return "BooleanPropChangeEvent [type=" + type + ", newValue=" + newValue + ", source=" + source + "]";
	}
}
