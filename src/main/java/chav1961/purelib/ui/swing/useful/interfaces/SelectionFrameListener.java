package chav1961.purelib.ui.swing.useful.interfaces;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;

import chav1961.purelib.ui.swing.useful.SelectionFrameManager;

/**
 * <p>This interface describes listener for {@linkplain SelectionFrameManager} class.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @last.update 0.0.7
 */
@FunctionalInterface
public interface SelectionFrameListener {
	/**
	 * <p>Selection frame style. Every style supports a predefined set of advanced parameters in the callback</p>
	 */
	public enum SelectionStyle {
		/**
		 * <p>No any selection. No advanced parameters will be supported in the callbacks</p>
		 */
		NONE,
		/**
		 * <p>Path selection. Advanced parameter in the {@linkplain SelectionFrameListener#selectionCompleted(SelectionStyle, Point, Point, Object...)} is
		 * {@linkplain GeneralPath} path selected. Advanced parameter in other callbacks is {@linkplain Rectangle} constructed from start and end selection points</p>  
		 */
		PATH,
		/**
		 * <p>Point selection. No advanced parameters will be supported in the callbacks</p>
		 */
		POINT,
		/**
		 * <p>Line selection. Advanced parameter in all callbacks is {@linkplain Rectangle} constructed from start and end selection points</p>
		 */
		LINE,
		/**
		 * <p>Rectangle selection. Advanced parameter in all callbacks is {@linkplain Rectangle} constructed from start and end selection points</p>
		 */
		RECTANGLE
	}
	
	/**
	 * <p>Selection complete callback.</p>
	 * @param style currently used selection style. Can't be null
	 * @param start starting selection point. Can't be null
	 * @param end ending selection point.  Can't be null
	 * @param parameters advanced parameters.  Can't be null but can be empty (see {@linkplain SelectionStyle} for details)
	 */
	void selectionCompleted(SelectionStyle style, Point start, Point end, Object... parameters);
	
	/**
	 * <p>Selection changing callback. Can be used for animation (for example)</p>
	 * @param style currently used selection style. Can't be null
	 * @param start starting selection point. Can't be null
	 * @param current ending selection point.  Can't be null
	 * @param parameters advanced parameters.  Can't be null but can be empty (see {@linkplain SelectionStyle} for details)
	 */
	default void selectionChanging(SelectionStyle style, Point start, Point current, Object... parameters) {}
	
	/**
	 * <p>Selection started callback.</p>
	 * @param style currently used selection style. Can't be null
	 * @param start starting selection point. Can't be null
	 */
	default void selectionStarted(SelectionStyle style, Point start) {}
	
	/**
	 * <p>Selection cancelled callback.</p>
	 * @param style currently used selection style. Can't be null
	 * @param start starting selection point. Can't be null
	 * @param end ending selection point.  Can't be null
	 * @param parameters advanced parameters.  Can't be null but can be empty (see {@linkplain SelectionStyle} for details)
	 */
	default void selectionCancelled(SelectionStyle style, Point start, Point end, Object... parameters) {}
}
