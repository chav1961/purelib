package chav1961.purelib.ui.interfacers;

import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public interface ConstraintChecker {
	boolean check(Object instance);
	String getConstraintExpression();
	String getMessageId();
	Severity getSeverity();
}