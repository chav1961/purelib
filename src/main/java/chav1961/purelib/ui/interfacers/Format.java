package chav1961.purelib.ui.interfacers;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Format {
	String value();
	String wizardType() default "";
	Class<?> contentType() default Object.class;
}
