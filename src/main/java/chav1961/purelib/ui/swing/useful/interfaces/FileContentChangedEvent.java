package chav1961.purelib.ui.swing.useful.interfaces;

import chav1961.purelib.ui.LRUManager;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;

/**
 * <p>This interface describes changes in the listener content. It usually used by {@linkplain JFileContentManipulator} 
 * and {@linkplain LRUManager} classes</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.7
 * @param <T> owner of the file content.
 */
public interface FileContentChangedEvent<T> {
	FileContentChangeType getChangeType();
	int getFileSupportId();
	T getOwner();
}