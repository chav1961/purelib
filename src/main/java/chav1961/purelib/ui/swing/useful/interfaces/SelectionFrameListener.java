package chav1961.purelib.ui.swing.useful.interfaces;

import java.awt.Point;

@FunctionalInterface
public interface SelectionFrameListener {
	public enum SelectionStyle {
		NONE,
		PATH,
		POINT,
		LINE,
		RECTANGLE
	}
	
	void selectionCompleted(SelectionStyle style, Point start, Point end, Object... parameters);
	default void selectionChanging(SelectionStyle style, Point start, Point current, Object... parameters) {}
	default void selectionStarted(SelectionStyle style, Point pt) {}
	default void selectionCancelled(SelectionStyle style, Point start, Point end, Object... parameters) {}
}
