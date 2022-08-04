package chav1961.purelib.ui.swing.useful;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;

public final class PrimitiveAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final KeyStroke										recommended;
	private final PrimitiveEditorKit.ThrowableActionListener	listener;
	
	PrimitiveAction(final String name, final KeyStroke recommended, final PrimitiveEditorKit.ThrowableActionListener listener) {
		super(name);
		this.recommended = recommended;
		this.listener = listener; 
	}
	
	public KeyStroke getKeyStrokeRecommended() {
		return recommended;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try{
			listener.actionPerformed(e);
		} catch (BadLocationException exc) {
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
	}
}