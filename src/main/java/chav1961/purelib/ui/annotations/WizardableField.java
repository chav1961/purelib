package chav1961.purelib.ui.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import chav1961.purelib.enumerations.PreferredUIType;

@Retention(RUNTIME)
@Target(FIELD)
public @interface WizardableField {
	String name();
	String caption() default "";
	PreferredUIType preferredType() default PreferredUIType.any;
	String defaultValue() default "@as-is";
	String tooltip() default "";
}
