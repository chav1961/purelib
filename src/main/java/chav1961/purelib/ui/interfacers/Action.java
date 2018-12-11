package chav1961.purelib.ui.interfacers;


import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import chav1961.purelib.i18n.interfaces.LocaleResource;

@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(MultiAction.class)
public @interface Action {
	LocaleResource resource();
	String actionString();
	boolean simulateCheck() default false;
}
