package chav1961.purelib.monitoring.interfaces;

import java.util.EventListener;

import chav1961.purelib.basic.exceptions.ContentException;

/**
 * <p>This interface describes listener for MBean object to monitor it's state</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
@FunctionalInterface
public interface MBeanListener extends EventListener {
	/**
	 * <p>Process event "value changed"</p>
	 * @param name name of changed attribute
	 * @param oldValue old value of the attribute
	 * @param newValue new value of the attribute
	 * @throws ContentException on any exceptions inside the listener. Exception does't break processing tail of listeners for the given object
	 */
	void valueChanged(String name, Object oldValue, Object newValue) throws ContentException;

	/**
	 * <p>Process event "operation called"</p>
	 * @param nameAndSignature qualified name and signature of method called 
	 * @param parameters parameters to call the method
	 * @param returnedValue returned value of the method
	 * @throws ContentException on any exceptions inside the listener. Exception does't break processing tail of listeners for the given object
	 */
	default void actionCalled(String nameAndSignature, Object[] parameters, Object returnedValue) throws ContentException {
	}

	/**
	 * <p>Process event "operation called" when operation threw exception</p>
	 * @param nameAndSignature qualified name and signature of method called 
	 * @param parameters parameters to call the method
	 * @param exception exception thrown
	 * @throws ContentException on any exceptions inside the listener. Exception does't break processing tail of listeners for the given object
	 */
	default void actionCalled(String nameAndSignature, Object[] parameters, Throwable exception) throws ContentException {
	}
	
	/**
	 * <p>Process event "instance created"</p>
	 * @param parameters parameters to call constructor
	 * @throws ContentException on any exceptions inside the listener. Exception does't break processing tail of listeners for the given object
	 */
	default void instanceCreated(Object[] parameters) throws ContentException {
	}

	/**
	 * <p>Process event "instance created" when operation threw exception</p>
	 * @param parameters parameters to call constructor
	 * @param exception exception thrown
	 * @throws ContentException on any exceptions inside the listener. Exception does't break processing tail of listeners for the given object
	 */
	default void instanceCreated(Object[] parameters, Throwable exception) throws ContentException {
	}
}
