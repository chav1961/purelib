package chav1961.purelib.ui.swing.interfaces;

import java.util.Set;

import javax.swing.JComponent;

import chav1961.purelib.ui.interfaces.FormManager;

/**
 * <p>This interface associated with all swing components in the application form, that allows complex local editing with it's content.
 * Yhis interface extends {@linkplain JComponentInterface} interface</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */


public interface JExtendedComponentInterface extends JComponentInterface {
	/**
	 * <p>This interface describes modification modes for the given control</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public enum ModificationMode {
		INSERT, DELETE, UPDATE
	}

	@FunctionalInterface
	public interface Value2ComponentConvertor {
		JComponent toComponent(Object value, boolean editable, boolean selected, boolean focused, int row, int col);
	}
	
	/**
	 * <p>Set form manager for the given control to manage editing. If the form manager is not null,
	 * all modification modes will be added automatically (see {@linkplain #addModificationMode(ModificationMode...)}).
	 * If the form manager is null, all modification midel will be removed automatically (see 
	 * {@linkplain #removeModificationMode(ModificationMode...)})</p>
	 * @param mgr manager to set. Can be null 
	 */
	void setFormManager(FormManager<?,?> mgr);
	
	/**
	 * <p>Get current form manager for the given control.</p>
	 * @return form manager for the given control. Can be null
	 */
	FormManager<?,?> getFormManager();
	
	/**
	 * <p>Allow modification modes foe the given control</p>
	 * @param modes modification modes to add for the modifications.
	 */
	void addModificationMode(ModificationMode... modes);
	
	/**
	 * <p>Reject modification modes for the given control</p>
	 * @param modes modification modes to remove for the modifications.
	 */
	void removeModificationMode(ModificationMode... modes);
	
	/**
	 * <p>Get current modification modes for the given control.</p>
	 * @return set of currently available modification model for the given control. The set will be unmodifiable
	 */
	Set<ModificationMode> getModificationModes();
	
	/**
	 * <p>Set component convertor for the given control</p>
	 * @param convertor component convertor. If null, default convertor will be used.
	 */
	void setComponentConvertor(Value2ComponentConvertor convertor);
}
