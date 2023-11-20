package chav1961.purelib.ui.interfaces;

import java.util.Set;

import chav1961.purelib.basic.exceptions.FlowException;

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
 * @since 0.0.7
 * @param <Id> primary key Id type for the form manager instance
 * @param <Instance> content for the form manager instance
 */

public interface UIFormManager<Id,Instance> extends UIItemState {
	/**
	 * <p>Get form manager to manipulate with local editors (for example, lists, tables etc)</p>
	 * @param inst current record instance 
	 * @param id primary key of the current record instance
	 * @param fieldName field name to get manager for
	 * @param parameters additional parameters
	 * @return form manager to manipulate or null if missing
	 * @throws FlowException on any errors or on action canceling  
	 */
	FormManager<?,?> getForEditor(final Instance inst, final Id id, final String fieldName, final Object... parameters) throws FlowException; 

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
	<T> T[] getForEditorContent(final Instance inst, final Id id, final String fieldName, final Object... parameters) throws FlowException;
}
