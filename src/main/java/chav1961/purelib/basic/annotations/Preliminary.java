package chav1961.purelib.basic.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>This annotation is used to mark preliminary entity. Entity marked is subject to change without notice.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE, ElementType.MODULE })
public @interface Preliminary {

}
