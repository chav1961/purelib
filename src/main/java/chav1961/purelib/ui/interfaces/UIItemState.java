package chav1961.purelib.ui.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface UIItemState {
	/**
	 * <p>This enumeration describes state of the appropriative UI control for the given field</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.5
	 */
	public static enum AvailableAndVisible {
		/**
		 * <p>No changes in the appropriative UI control required</p>
		 */
		DEFAULT,
		
		/**
		 * <p>Hide appropriative UI control</p>
		 */
		NOTVISIBLE,
		
		/**
		 * <p>Show appropriative UI control as not available</p>
		 */
		NOTAVAILABLE,
		
		/**
		 * <p>Show appropriative UI control as available, but not modifiable</p>
		 */
		READONLY,
		
		/**
		 * <p>Show appropriative UI control as fully accessed</p>
		 */
		AVAILABLE
	};
	
	/**
	 * <p>Get state for the given item</p>
	 * @param meta metadata to get state for
	 * @return UI item state. Can't be null. {@linkplain AvailableAndVisible#DEFAULT} means 'no changes required'
	 */
	AvailableAndVisible getItemState(final ContentNodeMetadata meta);
}
