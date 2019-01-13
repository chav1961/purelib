package chav1961.purelib.nanoservice.interfaces;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface RootPath {
	String value();
	int maxInstances() default 0;
}
