package chav1961.purelib.i18n.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.JComponent;

/**
 * <p>This annotation describes any locale-specific resources associated with the given field. Field can be either {@linkplain JComponent} instance or
 * any class implemented {@linkplain LocaleSpecificTextSetter} interface</p>
 * @see Localizer
 * @see LocaleSpecificTextSetter
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
public @interface LocaleResource {
	/**
	 * <p>Content value key associated with the given entity</p>
	 * @return any key registered in the localization resource described by {@linkplain LocaleResourceLocation} annotation
	 */
	String value();
	
	/**
	 * <p>Tool tip key associated with the given entity</p>
	 * @return any key registered in the localization resource described by {@linkplain LocaleResourceLocation} annotation
	 */
	String tooltip();
	
	/**
	 * <p>Help key associated with the given entity</p>
	 * @return any key registered in the localization resource described by {@linkplain LocaleResourceLocation} annotation
	 */
	String help() default "";
}
