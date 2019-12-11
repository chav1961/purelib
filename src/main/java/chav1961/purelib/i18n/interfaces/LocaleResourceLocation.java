package chav1961.purelib.i18n.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation describes any locale-specific resources associated with the given Java class. It is used for automatic creation of specific localizer for the given class instance
 * to use it for filling all locale-specific information in it.</p>
 * @see Localizer
 * @see LocaleResource
 * @see LocaleSpecificTextSetter
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LocaleResourceLocation {
	/**
	 * <p>Describe location of the given localizer resources.</p>
	 * @return location of the given localizer resources. Must be well-formed URI with the {@link Localizer#LOCALIZER_SCHEME} scheme 
	 */
	String value();
}
