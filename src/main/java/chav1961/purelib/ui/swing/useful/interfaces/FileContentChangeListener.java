package chav1961.purelib.ui.swing.useful.interfaces;

import chav1961.purelib.ui.swing.useful.JFileContentManipulator;

/**
 * <p>This interface describes listener for content changes</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @lastUpdate 0.0.5
 */
@FunctionalInterface
public interface FileContentChangeListener {
	void actionPerformed(FileContentChangedEvent event);
}