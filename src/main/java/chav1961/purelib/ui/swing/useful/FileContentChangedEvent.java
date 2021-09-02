package chav1961.purelib.ui.swing.useful;

import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangeType;

/**
 * <p>This interface describes changes in the listener content</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public interface FileContentChangedEvent<T> {
	FileContentChangeType getChangeType();
	T getOwner();
}