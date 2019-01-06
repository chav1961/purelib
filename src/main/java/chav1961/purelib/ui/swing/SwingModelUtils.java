package chav1961.purelib.ui.swing;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class SwingModelUtils {
	public static <T extends JComponent> T toMenuEntity(final ContentNodeMetadata node, final Class<T> awaited) throws NullPointerException, IllegalArgumentException{
		if (node == null) {
			throw new NullPointerException(); 
		}
		else if (awaited == null) {
			throw new NullPointerException(); 
		}
		else if (awaited.isAssignableFrom(JMenuBar.class)) {
			
		}
		else if (awaited.isAssignableFrom(JPopupMenu.class)) {
			
		}
		else if (awaited.isAssignableFrom(JMenu.class)) {
			
		}
		else if (awaited.isAssignableFrom(JMenuItem.class)) {
			
		}
		else {
			throw new IllegalArgumentException(); 
		}
		return null;
	}
	
}
