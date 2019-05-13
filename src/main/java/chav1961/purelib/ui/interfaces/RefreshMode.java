package chav1961.purelib.ui.interfaces;

/**
 * <p>This enumeration describes refreshing mode for the given UI screen after callback processing</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2 last update 0.0.3
 */
public enum RefreshMode {
	/**
	 * <p>Reject all changes and restore previous value in the field</p>
	 */
	REJECT,
	/**
	 * <p>Don't refresh field</p>
	 */
	NONE,
	/**
	 * <p>Refresh current field only</p>
	 */
	FIELD_ONLY,
	/**
	 * <p>refresh current record only</p>
	 */
	RECORD_ONLY,
	/**
	 * <p>Refresh all content</p>
	 */
	TOTAL,
	/**
	 * <p>Exit show</p>
	 */
	EXIT,
	/**
	 * <p>Default action</p>
	 */
	DEFAULT
}