package chav1961.purelib.basic.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import chav1961.purelib.basic.ConsoleCommandManager;

/**
 * <p>This annotation is used to associate the method parameters and console command variable. It associates parameter of the method with the variable name in the command template.</p>
 * <p>Annotation contains two parameters:</p>
 * <ul>
 * <li>name - console command variable associated with the given parameter</li> 
 * <li>defaultValue - optional parameters with default value for ths parameter. Default value is always a string, but will be converted to the parameter type</li> 
 * </ul>
 * <p>Parameters of the methods can be any primitive types except <b>char</b>, and also:</p>
 * <ul>
 * <li>java.lang.String</li>
 * <li>java.net.URL</li>
 * <li>java.net.URI</li>
 * <li>any enumeration</li>
 * <li>wrappers for any available primitive type</li>
 * </ul>
 * <p>If console command variable located inside the <i>repeat</i> template, parameter need be an <i>array</i> of appropriative type</p>
 * 
 * @see ConsoleCommand
 * @see ConsoleCommandManager
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ConsoleCommandParameter {
	String name();
	String defaultValue() default "";
}
