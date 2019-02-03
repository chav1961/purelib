package chav1961.purelib.ui.swing.interfaces;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;

/**
 * <p>This interface associated with all swing components in the application form.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 */

public interface JComponentInterface {
	/**
	 * <p>Get raw data from component</p>
	 * @return raw data from component
	 */
	String getRawDataFromComponent();
	
	/**
	 * <p>Get current value from component</p>
	 * @return value changed but not commited yet
	 */
	Object getValueFromComponent();
	
	/**
	 * <p>Get value from component before it's commit</p>
	 * @return value changed but not commited yet
	 * @throws SyntaxException when component contains invalid content
	 */
	Object getChangedValueFromComponent() throws SyntaxException;
	
	/**
	 * <p>Assing new value to component</p>
	 * @param value value to assign
	 */
	void assignValueToComponent(Object value);
	
	/**
	 * <p>Get current value type in the component</p>
	 * @return current value type. Can't be null
	 */
	Class<?> getValueType();
	
	/**
	 * <p>Perform standard validation of the content</p>  
	 * @param value String representation of the content to validate
	 * @return null on success, otherwise error description 
	 */
	String standardValidation(String value);
	
	/**
	 * <p>Mark content as valid/invalid</p>
	 * @param invalid true means invalid content
	 */
	void setInvalid(boolean invalid);
	
	/**
	 * <p>Is current content invalid</p>
	 * @return true if the content is invalid
	 */
	boolean isInvalid();
}
