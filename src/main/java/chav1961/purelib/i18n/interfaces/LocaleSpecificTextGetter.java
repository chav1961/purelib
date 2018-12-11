package chav1961.purelib.i18n.interfaces;

/**
 * <p>This interface describes any locale-specific entities (usually GUI items). It uses in conjunction with the {@linkplain LocaleResource} and {@linkplain LocaleResourceLocation} annotations.
 * You can associate any locale-specific resources with any entity, that implements this interface, and it's content and tool tip will refresh automatically on locale changes.</p>
 * @see Localizer
 * @see LocaleResource
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public interface LocaleSpecificTextGetter {
	/**
	 * <p>Get locale-specific text for the entity</p>
	 * @return locale-specific text to gotten. This parameter is not a key to seek localization resource, but value found
	 */
	String getLocaleSpecificText();
	
	/**
	 * <p>Get locale-specific tool tip text for the entity.</p> 
	 * @return locale-specific tool tip text gotten. This parameter is not a key to seek localization resource, but value found
	 */
	String getLocaleSpecificToolTipText();
}
