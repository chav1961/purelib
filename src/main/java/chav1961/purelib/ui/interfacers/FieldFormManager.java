package chav1961.purelib.ui.interfacers;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;

public interface FieldFormManager<Id,Instance> {
	/**
	 * <p>Processing field-scope actions on the UI screen</p>
	 * @param inst current record instance 
	 * @param id primary key of the current record instance
	 * @param fieldName field name to change
	 * @param oldValue old value of the changing field 
	 * @return refresh mode after processing
	 * @throws FlowException on any errors or on action cancelling  
	 * @throws LocalizationException on any errors in the localization  
	 */
	RefreshMode onField(final Instance inst, final Id id, final String fieldName, final Object oldValue) throws FlowException, LocalizationException;
}
