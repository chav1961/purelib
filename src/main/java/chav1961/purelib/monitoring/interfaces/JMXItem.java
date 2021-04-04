package chav1961.purelib.monitoring.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>This annotation marks MBean class to register in Pure Library</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.FIELD,ElementType.METHOD,ElementType.CONSTRUCTOR,ElementType.PARAMETER})
public @interface JMXItem {
	/**
	 * <p>Get description of the class</p>
	 * @return
	 */
	String value();
}
