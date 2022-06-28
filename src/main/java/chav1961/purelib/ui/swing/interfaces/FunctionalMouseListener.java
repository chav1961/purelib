package chav1961.purelib.ui.swing.interfaces;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@FunctionalInterface
public interface FunctionalMouseListener extends MouseListener {
	public static enum EventType {
		CLICKED, PRESSED, RELEASED, ENTERED, EXITED
	}
	
	void processEvent(EventType et, MouseEvent e);
	
	@Override default void mouseClicked(MouseEvent e) {processEvent(EventType.CLICKED, e);}
	@Override default void mousePressed(MouseEvent e) {processEvent(EventType.PRESSED, e);}
	@Override default void mouseReleased(MouseEvent e) {processEvent(EventType.RELEASED, e);}
	@Override default void mouseEntered(MouseEvent e) {processEvent(EventType.ENTERED, e);}
	@Override default void mouseExited(MouseEvent e) {processEvent(EventType.EXITED, e);}
}
