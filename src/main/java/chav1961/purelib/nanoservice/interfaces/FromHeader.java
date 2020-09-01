package chav1961.purelib.nanoservice.interfaces;


import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * <p>This annotation marks method parameter containing request header. Type of the parameters must be <b>primitive</b>, {@linkplain String}, {@linkplain UUID} or array of all the previous types</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface FromHeader {
	/**
	 * @return name of request header
	 */
	String value();
}
