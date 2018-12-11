package chav1961.purelib.ui.interfacers;

/**
 * <p>This enumerations describes different representations of the input forms for MVC-based manipulators</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public enum FormRepresentation {
	/**
	 * <p>Form is presented as single record editor with natural page splitting</p>
	 */
	SINGLE_RECORD,
	
	/**
	 * <p>Form is presented as single record editor with tab page splitting</p>
	 */
	SINGLE_TABBED_RECORD,
	
	/**
	 * <p>Form is presented as a table of records</p>
	 */
	LIST, 
	
	/**
	 * <p>Form is presented as a table record and detailed single record editor with natural page splitting</p>
	 */
	LIST_AND_SINGLE_RECORD,
	
	/**
	 * <p>Form is presented as a table record and detailed single record editor with tab page splitting</p>
	 */
	LIST_AND_SINGLE_TABBED_RECORD,
	
	/**
	 * <p>Form is presented as single record with key-value list of parameters</p>
	 */
	TWO_COLUMNED_RECORD
}