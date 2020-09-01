package chav1961.purelib.nanoservice.interfaces;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

/**
 * <p>This annotation marks method parameter to keep response header. Type of the parameters must be {@linkplain StringBuilder} or {@linkplain List} of strings</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ToHeader {
	/**
	 * @return name of response header parameter
	 */
	String value();
}
