package chav1961.purelib.ui.swing.useful.interfaces;

import java.awt.Point;
import java.awt.Rectangle;

@FunctionalInterface
public interface SelectionFrameListener {
	void selectionCompleted(Point start, Point end, Rectangle rect);
	default void selectionChanging(Point start, Point end, Rectangle rect) {}
	default void selectionStarted(Point pt) {}
	default void selectionCancelled(Point start, Point end, Rectangle rect) {}
}
