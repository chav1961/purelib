package chav1961.purelib.ui.swing.useful.interfaces;

import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.LRUManager;

/**
 * <p>This enumeration describes type of content change in the {@linkplain JFileContentManipulator} and {@linkplain LRUManager}</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @lastUpdate 0.0.5
 */
public enum FileContentChangeType {
	NEW_FILE_CREATED, 
	FILE_LOADED, 
	FILE_STORED, 
	FILE_STORED_AS, 
	MODIFICATION_FLAG_SET, 
	MODIFICATION_FLAG_CLEAR,
	LRU_LIST_REFRESHED
}