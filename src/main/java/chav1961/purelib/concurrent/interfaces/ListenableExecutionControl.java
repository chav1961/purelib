package chav1961.purelib.concurrent.interfaces;

import java.util.EventListener;

/**
 *	<p>This interface extends {@linkplain ExecutionControl} interface to support listening events for it.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface ListenableExecutionControl extends ExecutionControl {
	/**
	 * <p>Listener interface for {@linkplain ListenableExecutionControl} implementations</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@FunctionalInterface
	public static interface ExecutionControlListener extends EventListener {
		/**
		 * <p>Process event from {@linkplain ExecutionControl} implementation</p>
		 * @param event to process. Can't be null
		 */
		void processAction(ExecutionControlEvent e);
	}
	
	/**
	 * <p>Add {@linkplain ExecutionControlListener} to {@linkplain ListenableExecutionControl} implementation</p>
	 * @param l listener to add. Can't be null
	 */
	void addExecutionControlListener(ExecutionControlListener l);

	/**
	 * <p>Remove {@linkplain ExecutionControlListener} from {@linkplain ListenableExecutionControl} implementation</p>
	 * @param l listener to remove. Can't be null
	 */
	void removeExecutionControlListener(ExecutionControlListener l);
}
