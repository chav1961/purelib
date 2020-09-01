package chav1961.purelib.nanoservice.interfaces;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>This annotation marks request URI path prefix for all the methods of the given class.</p>
 * @see Path
 * @see FromPath
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface RootPath {
	/**
	 * @return path prefix for all the methods in the given class
	 */
	String value();
}
