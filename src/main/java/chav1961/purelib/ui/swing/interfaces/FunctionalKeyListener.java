package chav1961.purelib.ui.swing.interfaces;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@FunctionalInterface
public interface FunctionalKeyListener extends KeyListener {
	public static enum EventType {
		PRESSED, TYPED, RELEASED
	}
	
	void processEvent(EventType et, KeyEvent e);
	
    @Override default void keyTyped(KeyEvent e) {processEvent(EventType.TYPED, e);}
    @Override default void keyPressed(KeyEvent e) {processEvent(EventType.PRESSED, e);}
    @Override default void keyReleased(KeyEvent e) {processEvent(EventType.RELEASED, e);}

}
