package chav1961.purelib.ui.nanoservice.interfaces;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Path {
	String value();
	QueryType[] type() default {QueryType.GET};
}
