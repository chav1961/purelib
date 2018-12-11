package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes any object having unsaved modifications. It is useful, for example, in UI editors to fix and store unsaved changes</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public interface ModifiableEntity {
	/**
	 * <p>Is entity modified</p>
	 * @return true if yes
	 */
	boolean isModified();
	
	/**
	 * <p>Set entity modification state</p>
	 * @param modificationFlag new modification state
	 */
	void setModified(boolean modificationFlag);
}
