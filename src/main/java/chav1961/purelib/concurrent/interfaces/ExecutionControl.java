package chav1961.purelib.concurrent.interfaces;

/**
 *	<p>This interface describes any entity with start/suspend/resume/stop life cycle.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public interface ExecutionControl {
	/**
	 * <p>Start entity execution</p>
	 * @throws Exception on any errors during starting
	 */
	void start() throws Exception;
	
	/**
	 * <p>Suspend entity execution</p>
	 * @throws Exception on any errors during suspend
	 */
	void suspend() throws Exception;
	
	/**
	 * <p>Resume entity execution</p>
	 * @throws Exception on any errors during suspend
	 */
	void resume() throws Exception;
	
	/**
	 * <p>Stop entity execution</p>
	 * @throws Exception on any errors during suspend
	 */
	void stop() throws Exception;
	
	/**
	 * <p>Is entity started now</p>
	 * @return true of yes
	 */
	boolean isStarted();
	
	/**
	 * <p>Is entity suspended now</p>
	 * @return true if yes
	 */
	boolean isSuspended();
}
