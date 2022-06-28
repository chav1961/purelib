package chav1961.purelib.ui.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public interface SpecialUIActionManager {
	boolean canProcessSpecialAction(ContentNodeMetadata metadata, JComponentInterface component, String action);
	RefreshMode processSpecialAction(ContentNodeMetadata metadata, JComponentInterface component, String action, JComponentMonitor monitor); 
}
