package chav1961.purelib.ui.interfaces;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class is an abstract class for implementing low-level UI form management. This class supports:</p> 
 * <ul>
 * <li>Creole-styled multi-paged UI form description</li>
 * <li>UI form inheritance</li>
 * <li>a lot of form representation on the UI screen</li>
 * <li>a lot of field representation on the UI screen</li>
 * <li>supporting callback (see {@linkplain FormManager}) for the form management</li>
 * </ul>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.5
 */

public interface FormManager<Id,Instance> extends RecordFormManager<Id,Instance>, FieldFormManager<Id,Instance>, ActionFormManager<Id,Instance> {
	/**
	 * <p>Get logger to print messages and errors</p>
	 * @return logger to print messages. Can't be null
	 */
	LoggerFacade getLogger();
	
	/**
	 * <p>Get form manager to manipulate with local editors (for example, lists, tables etc)</p>
	 * @param inst current record instance 
	 * @param id primary key of the current record instance
	 * @param fieldName field name to get manager for
	 * @param parameters additional parameters
	 * @return form manager to manipulate or null if missing
	 * @throws FlowException on any errors or on action canceling  
	 */
	default FormManager<?,?> getForEditor(final Instance inst, final Id id, final String fieldName, final Object... parameters) throws FlowException {return null;} 

	/**
	 * <p>Get content for local editors (for example, drop-down lists)</p>
	 * @param <T> content type
	 * @param inst current record instance 
	 * @param id primary key of the current record instance
	 * @param fieldName field name to get manager for
	 * @param parameters additional parameters
	 * @return content. Can be null
	 * @throws FlowException on any errors or on action canceling  
	 */
	default <T> T[] getForEditorContent(final Instance inst, final Id id, final String fieldName, final Object... parameters) throws FlowException {return null;}
	
	/**
	 * <p>This enumeration describes state of the appropriative UI control for the given field</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.5
	 */
	public static enum AvailableAndVisible {
		/**
		 * <p>No changes in the appropriative UI control required</p>
		 */
		DEFAULT,
		
		/**
		 * <p>Hide appropriative UI control</p>
		 */
		NOTVISIBLE,
		
		/**
		 * <p>Show appropriative UI control as not available</p>
		 */
		NOTAVAILABLE,
		
		/**
		 * <p>Show appropriative UI control as available, but not modifiable</p>
		 */
		READONLY,
		
		/**
		 * <p>Show appropriative UI control as fully accessed</p>
		 */
		MODIFIABLE
	};
	
	/**
	 * <p>Get state for the given item</p>
	 * @param fieldName field name to get state for
	 * @return UI item state. Can't be null. {@linkplain AvailableAndVisible#DEFAULT} means 'no changes required'
	 */
	default AvailableAndVisible getItemState(final String fieldName) {
		return AvailableAndVisible.DEFAULT;
	}
}
