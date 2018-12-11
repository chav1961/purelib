package chav1961.purelib.i18n.interfaces;

/**
 * <p>This interface describes any locale-specific entities (usually GUI items). It uses in conjunction with the {@linkplain LocaleResource} and {@linkplain LocaleResourceLocation} annotations.
 * You can associate any locale-specific resources with any entity, that implements this interface, and it's content and tool tip will refresh automatically on locale changes.</p>
 * @see Localizer
 * @see LocaleResource
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public interface LocaleSpecificTextSetter {
	/**
	 * <p>Set locale-specific text for the entity</p>
	 * @param text locale-specific text to set. This parameter is not a key to seek localization resource, but value found
	 */
	void setLocaleSpecificText(String text);
	
	/**
	 * <p>Set locale-specific tool tip text for the entity.</p> 
	 * @param toolTip locale-specific tool tip text to set. This parameter is not a key to seek localization resource, but value found
	 */
	void setLocaleSpecificToolTipText(String toolTip);
}
