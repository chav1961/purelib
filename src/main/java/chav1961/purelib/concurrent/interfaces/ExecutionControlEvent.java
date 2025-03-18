package chav1961.purelib.concurrent.interfaces;

import java.util.EventObject;

/**
 * <p>This interface describes execution control event. The class is a child of standard {@linkplain EventObject} 
 * class and can be used in the classes, that implement {@linkplain ListenableExecutionControl} interface.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class ExecutionControlEvent extends EventObject {
	private static final long serialVersionUID = -8100013165674297276L;

	/**
	 * <p>This enumeration describes types of execution control events.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum ExecutionControlEventType {
		/**
		 * <p>Execution control event - started</p>  
		 */
		STARTED,
		/**
		 * <p>Execution control event - suspended</p>  
		 */
		SUSPENDED,
		/**
		 * <p>Execution control event - resumed</p>  
		 */
		RESUMED,
		/**
		 * <p>Execution control event - sopped</p>  
		 */
		STOPPED
	}
	
	private final ExecutionControlEventType	type;

	/**
	 * <p>Constructor of the class instance</p>
	 * @param source event object source. Can't be null.
	 * @param type execution control event type. Can't be null.
	 * @throws NullPointerException any parameter is null
	 */
	public ExecutionControlEvent(final Object source, final ExecutionControlEventType type) throws NullPointerException {
		super(source);
		if (source == null) {
			throw new NullPointerException("Event source can't be null");
		}
		else if (type == null) {
			throw new NullPointerException("Event type can't be null");
		}
		else {
			this.type = type;
		}
	}
	
	/**
	 * <p>Get execution control event type.</p>
	 * @return execution control event type. Can't be null.
	 */
	public ExecutionControlEventType getExecutionControlEventType() {
		return type;
	}

	@Override
	public String toString() {
		return "ExecutionControlEvent [type=" + type + ", source=" + source + "]";
	}
}
