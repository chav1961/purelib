package chav1961.purelib.ui.interfaces;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;

public interface ActionFormManager<Id,Instance> {
	/**
	 * <p>Processing explicitly defined actions on the UI screen</p>
	 * @param inst current record instance 
	 * @param id primary key of the current record instance
	 * @param actionName action name. Any non-null and non-empty string
	 * @param parameter advanced action parameter (usually, but not exclusive, button control associated and action string)
	 * @return refresh mode after processing
	 * @throws FlowException on any errors or on action canceling  
	 * @throws LocalizationException on any errors in the localization  
	 */
	default RefreshMode onAction(final Instance inst, final Id id, final String actionName, final Object... parameter) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}
}
