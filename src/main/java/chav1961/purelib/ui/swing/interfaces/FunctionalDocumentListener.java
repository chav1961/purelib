package chav1961.purelib.ui.swing.interfaces;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 */
@FunctionalInterface
public interface FunctionalDocumentListener extends DocumentListener {
	public static enum ChangeType {
		INSERT, REMOVE, CHANGE
	}
	
	void update(ChangeType ct, DocumentEvent e);

    @Override default void insertUpdate(DocumentEvent e) {update(ChangeType.INSERT, e);}
    @Override default void removeUpdate(DocumentEvent e) {update(ChangeType.REMOVE, e);}
    @Override default void changedUpdate(DocumentEvent e) {update(ChangeType.CHANGE, e);}
};
