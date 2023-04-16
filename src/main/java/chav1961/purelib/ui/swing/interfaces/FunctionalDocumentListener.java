package chav1961.purelib.ui.swing.interfaces;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * <p>This interface is a collector for three {@linkplain DocumentListener} interface methods. It was created especially
 * to use with the lambdas</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6  
 */
@FunctionalInterface
public interface FunctionalDocumentListener extends DocumentListener {
	/**
	 * <p>This enumeration describes change type in the document</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6  
	 */
	public static enum ChangeType {
		INSERT, REMOVE, CHANGE
	}
	
	/**
	 * <p>Collector method for the given interface</p>
	 * @param ct change type. Can't be null
	 * @param e Document event associated with the given change type. Can't be null
	 */
	void update(ChangeType ct, DocumentEvent e);

    @Override default void insertUpdate(DocumentEvent e) {update(ChangeType.INSERT, e);}
    @Override default void removeUpdate(DocumentEvent e) {update(ChangeType.REMOVE, e);}
    @Override default void changedUpdate(DocumentEvent e) {update(ChangeType.CHANGE, e);}
};
