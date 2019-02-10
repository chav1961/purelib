package chav1961.purelib.ui.swing.interfaces;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used to mark a method for call thru Swing Action listeners. The only parameter of the annotation is a string associated 
 * with Action event. This annotation is used by {@linkplain}</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 */

@Retention(RUNTIME)
@Target(METHOD)
public @interface OnAction {
	String value();
	boolean async() default false;
}
