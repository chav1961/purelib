package chav1961.purelib.ui.swing.interfaces;

import java.net.URI;

import chav1961.purelib.basic.exceptions.TestException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This interface supports testing management for all UI classes. Use it in JUnit test only!</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see ContentNodeMetadata
 * @since 0.0.3
 */
public interface UITestInterface {
	/**
	 * <p>Find control by it's URI (relative or absolute)</p>
	 * @param control URI control to search (see {@linkplain ContentNodeMetadata#getUIPath()} and {@linkplain ContentNodeMetadata#getRelativeUIPath()}) 
	 * @return self, positioned to control found 
	 * @throws TestException if control not found or not exists
	 */
	UITestInterface find(final URI control) throws TestException;
	
	/**
	 * <p>Find control by it's metadata associated</p> 
	 * @param metadata metadata to search
	 * @return self, positioned to control found 
	 * @throws TestException if control not found or not exists
	 */
	UITestInterface find(final ContentNodeMetadata metadata) throws TestException;
	
	/**
	 * <p>Get type of UI presentation for the control found</p>
	 * @return UI presentation type 
	 * @throws TestException if control not found or not exists
	 */
	Class<?> getControlType() throws TestException;
	
	/**
	 * <p>Get metadata associated with the control found</p>
	 * @return metadata associated
	 * @throws TestException if control not found or not exists
	 */
	ContentNodeMetadata getMetadata() throws TestException;
	
	/**
	 * <p>Get value stored in the control</p>
	 * @return value stored
	 * @throws TestException if control not found or not exists
	 */
	Object getValue() throws TestException;
	
	/**
	 * <p>Set value of the control</p>
	 * @param value value to set
	 * @return self
	 * @throws TestException if control not found or not exists
	 */
	UITestInterface setValue(final Object value) throws TestException;
	
	/**
	 * <pValidate value by the control without storing it</p>
	 * @param value value to validate
	 * @return true if value is valid
	 * @throws TestException if control not found or not exists
	 */
	boolean validateValue(final Object value) throws TestException;
	
	/**
	 * <p>Commit current value of the control</p>
	 * @return self
	 * @throws TestException if control not found, not exists or control valie is invalid
	 */
	UITestInterface commit() throws TestException;
	
	/**
	 * <p>Set and commit value for the control</p>
	 * @param value value to set
	 * @return self
	 * @throws TestException if control not found, not exists or control valie is invalid
	 */
	UITestInterface acceptValue(final Object value) throws TestException;
	
	/**
	 * <p>Click action source of the control</p>
	 * @return either self or new interface when action submits anything
	 * @throws TestException if control not found, not exists or doesn't support actions
	 */
	UITestInterface click() throws TestException;	
	
	/**
	 * <p>Go to control link</p>
	 * @return new interface for branch target
	 * @throws TestException if control not found, not exists or doesn't support linking
	 */
	UITestInterface go() throws TestException;	
}
