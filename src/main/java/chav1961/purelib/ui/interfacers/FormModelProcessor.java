package chav1961.purelib.ui.interfacers;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public interface FormModelProcessor<Id,Instance> {
	void processAction(final FormModel<Id,Instance> model, final ControllerAction action, final Object... parameters) throws ContentException;
	void message(Severity severity, String message, Object... parameters);
	void message(int cell, Severity severity, String message, Object... parameters);
}
