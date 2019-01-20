package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface can be used for indicating long time processing progress in applications. Indicating process can be single-stage and multistage. 
 * Single-stage life cycle for the interface is:</p>
 * <ul>
 * <li>calling {@linkplain #start(String, long)} method</li>
 * <li>calling {@linkplain #processed(long)} method repeatedly.</li>
 * <li>calling {@linkplain #end()} method.</li>
 * </ul>
 * MultiState life cycle for the interface is:</p>
 * <ul>
 * <li>calling {@linkplain #start(String)} method</li>
 * <li>calling {@linkplain #stage(String,int,int)} or {@linkplain #stage(String,int,int,long)} method</li>
 * <li>calling {@linkplain #processed(long)} method repeatedly.</li>
 * <li>calling {@linkplain #endStage()} method.</li>
 * <li>repeat three previous steps if needed</li>
 * <li>calling {@linkplain #end()} method.</li>
 * </ul>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public interface ProgressIndicator {
	/**
	 * <p>Start single-stage progress.</p>
	 * @param caption caption to display. Can't be null or empty. Always is not localized. You can use it as a key to get localized string
	 * @param total total data to process. If unknown, -1 will be typed.
	 */
	void start(String caption, long total);
	
	/**
	 * <p>Start multistage progress.</p>
	 * @param caption caption to display. Can't be null or empty. Always is not localized. You can use it as a key to get localized string
	 */
	void start(String caption);

	/**
	 * <p>Start new stage of multistage progress.</p>
	 * @param caption caption to display. Can't be null or empty. Always is not localized. You can use it as a key to get localized string 
	 * @param stage stage number. Always positive
	 * @param of total number of stages. Always positive. It's not guaranteed to be the same during all life cycle
	 */
	default void stage(String caption, int stage, int of) {}
	
	/**
	 * <p>Start new stage of multistage progress.</p>
	 * @param caption caption to display. Can't be null or empty. Always is not localized. You can use it as a key to get localized string 
	 * @param stage stage number. Always positive
	 * @param of total number of stages. Always positive. It's not guaranteed to be the same during all life cycle
	 * @param total total data to process
	 */
	default void stage(String caption, int stage, int of, long total) {}
	
	/**
	 * <p>Process the next piece of data.</p>
	 * @param processed piece size already processed. Always must be treated as absolute value from the beginning, not incremental. If piece amount of
	 * the work can't be defined, can be zero
	 * @return true - continue processing, false - interrupt long progress
	 */
	boolean processed(long processed);

	/**
	 * <p>Ends the current stage</p>
	 * @return -1 - continue processing, 0 - interrupt long progress, any positive - ultimate branch to the given stage of the processing  
	 */
	default int endStage() {return -1;}
	
	/**
	 * <p>End progress</p>
	 */
	void end();
}