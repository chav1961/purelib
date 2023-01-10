package chav1961.purelib.ui.swing.useful.interfaces;

/**
 * <p>This interface describes listener for content changes</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @param <T> file content owner
 * @since 0.0.4
 * @last.update 0.0.7
 */
@FunctionalInterface
public interface FileContentChangeListener<T> {
	void actionPerformed(FileContentChangedEvent<T> event);
}