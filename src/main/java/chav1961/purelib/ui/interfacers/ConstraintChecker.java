package chav1961.purelib.ui.interfacers;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public interface ConstraintChecker<T> {
	String	MSG_TEXT = "text";
	
	boolean check(T instance) throws ContentException;
	String getConstraintExpression();
	String getMessageId();
	Severity getSeverity();
}
