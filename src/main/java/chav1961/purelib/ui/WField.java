package chav1961.purelib.ui;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface WField {
	String name();
	String caption();
	PreferredType preferredType() default PreferredType.any;
	String defaultValue() default "@as-is";
	String tooltip() default "";
}
