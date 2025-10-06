package chav1961.purelib.ui.swing.interfaces;

import java.util.EventListener;
import java.awt.AWTEvent;

/**
 * <p>This interface describes any entity that can support undo/redo actions</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 * @param <T> content to undo/redo
 */
public interface Undoable<T> {
	/**
	 * <p>This enumeration describes undo event types</p>
	 */
	public static enum UndoEventType {
		CHANGE_UNDO,
		APPEND_UNDO,
		CLEAR_UNDO;
	}
	
	/**
	 * <p>This class describes undo event</p>
	 */
	public static class UndoEvent extends AWTEvent {
		private static final long serialVersionUID = -31332343097479294L;

		private final UndoEventType	type;
		
		public UndoEvent(final Object source, final int id, final UndoEventType type) {
			super(source, id);
			this.type = type;
		}
		
		public UndoEventType getUndoEventType() {
			return type;
		}
	}
	
	/**
	 * <p>This interface describes undo event listener</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.8
	 */
	@FunctionalInterface
	public static interface UndoListener extends EventListener {
		void undoChanged(UndoEvent e);
	}
	
	/**
	 * <p>Can invoke undo() in the current state</p> 
	 * @return true if invoking undo() can be done, false otherwise
	 */
	boolean canUndo();
	
	/**
	 * <p>Invoke undo</p>
	 * @return undo content. Can not be null
	 * @throws IllegalStateException attempt to make undo when {@linkplain #canUndo()} returns false
	 */
	T undo() throws IllegalStateException;

	/**
	 * <p>Can invoke redo() in the current state</p> 
	 * @return true if invoking redo() can be done, false otherwise
	 */
	boolean canRedo();
	
	/**
	 * <p>Invoke redo</p>
	 * @return undo content. Can not be null
	 * @throws IllegalStateException attempt to make redo when {@linkplain #canRedo()} returns false
	 */
	T redo() throws IllegalStateException;
	
	/**
	 * <p>Append new content to undo</p>
	 * @param item content to append. Can't be null.
	 * @throws NullPointerException attempt to append null value
	 */
	void appendUndo(T item) throws NullPointerException;
	
	/**
	 * <p>Remove undo history</p>
	 */
	void clearUndo();
	
	/**
	 * <p>Gets current item</p>
	 * @return current item. Can't be null
	 * @throws IllegalStateException no any content to return item
	 */
	T getCurrentItem()throws IllegalStateException;
	
	/**
	 * <p>Add undo event lister</p>
	 * @param l listener to add. Can't be null
	 * @throws NullPointerException attempt to add null listener
	 */
	void addUndoListener(UndoListener l) throws NullPointerException;
	
	/**
	 * <p>Remove undo event listener</p>
	 * @param l listener to remove. Can't be null
	 * @throws NullPointerException attempt to remove null listener
	 */
	void removeUndoListener(UndoListener l) throws NullPointerException;
}
