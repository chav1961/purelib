package chav1961.purelib.concurrent.interfaces;

import java.util.EventObject;

public class ExecutionControlEvent extends EventObject {
	private static final long serialVersionUID = -8100013165674297276L;

	public static enum ExecutionControlEventType {
		STARTED,
		SUSPENDED,
		RESUMED,
		STOPPED
	}
	
	private final ExecutionControlEventType	type;
	
	public ExecutionControlEvent(final Object source, final ExecutionControlEventType type) {
		super(source);
		if (type == null) {
			throw new NullPointerException("Event type can't be null");
		}
		else {
			this.type = type;
		}
	}
	
	public ExecutionControlEventType getExecutionControlEventType() {
		return type;
	}

	@Override
	public String toString() {
		return "ExecutionControlEvent [type=" + type + ", source=" + source + "]";
	}
}
