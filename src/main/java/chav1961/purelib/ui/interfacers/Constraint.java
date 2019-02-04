package chav1961.purelib.ui.interfacers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Constraint {
	String value();
	String messageId() default "";
	Severity severity() default Severity.error;
}
