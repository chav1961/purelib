package chav1961.purelib.ui.swing.useful.interfaces;

import chav1961.purelib.ui.LRUManager;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;

/**
 * <p>This enumeration describes type of content change in the {@linkplain JFileContentManipulator} and {@linkplain LRUManager}</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.7
 */
public enum FileContentChangeType {
	NEW_FILE_CREATED, 
	FILE_LOADED, 
	FILE_STORED, 
	FILE_STORED_AS, 
	MODIFICATION_FLAG_SET, 
	MODIFICATION_FLAG_CLEAR,
	LRU_LIST_REFRESHED,
	FILE_SUPPORT_ID_CHANGED
}