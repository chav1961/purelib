package chav1961.purelib.basic.interfaces;

import java.io.Closeable;
import java.util.Set;

/**
 * <p>This interface is a facade for different loggers. The great problem with loggers is a huge amount of them at now. This Zoo strongly reduces compatibility of the 
 * 3-rd party software in the large systems. This interface isolates the pure library software from depending of any loggers. All the classes in this library use the 
 * interface as the only way to get access to loggers. The next serious problem with loggers is is an extra-large amount of the logging trace in the large system logs.
 * Most of all the trace doesn't have usability in the normal modes, but make by developers for the "post-fly-analyze" purpose only. As a result, there are two typical
 * problems with the log:</p>
 * <ul>
 * <li>the log information is too small - can't make adequate analysis</li>
 * <li>the log information is too large - can't make adequate analysis too</li>
 * </ul>
 * <p>The solving of this problem, IMHO, is a <i>transaction</i> logging mode. When you start any action can be aborted or failed, you start logging in the 
 * transaction logger. If the actions ends successfully, you can rollback the trace, and the logging trace is simply rejected without storing it to log files.
 * If transaction was aborted or failed, you get all logger trace, which will be stored in the log files.</p>
 * <p>To reduce amount of the stack trace lines (when exceptions are logged), this logger supports a set of reducing algorithms. They are defined by 
 * {@link LoggerFacade.Reducing} enumeration and means:</p>
 * <ul>
 * <li>reduceJREPath - exclude any stack trace lines with java.*, javax.*, sun.* packages</li>
 * <li>reduceOverSubtree - exclude any stack trace lines, located inside the current package and it's sub-packages. Valid in the transaction mode only</li>
 * <li>reduceRuntimeExceptions - excludes any stack trace for RuntimeException and it's children, if is throws at the same program code location again</li>
 * <li>reduceCause - if the exception contains 'cause', print stack trace only for it</li> 
 * <li>reduceTrace - exclude stack trace fully, remains the exception messages only</li> 
 * </ul>
 * <p>The most of all methods of this interface return {@link LoggerFacade} type. This is exactly a 'this' reference, so you can use this interface in the chained
 * calls (for example <code>MyLogger.message(Severity.info,"message1").message(Severity.warning,"message2")</code>).</p>
 * <p>The logger facade interface implementation not needs to be thread-safe</p>
 *    
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public interface LoggerFacade extends Closeable {
	static final String		LOGGER_INSTANCE_KEY = "loggerInstance";  
	
	/**
	 * <p>This enumeration describes message severity</p>
	 *
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	public enum Severity {
		trace, debug, info, warning, error, severe
	}

	/**
	 * <p>This enumeration describes reducing algorithms used in the logger</p>
	 *
	 * @see LoggerFacade LoggerFacade 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	public enum Reducing {
		reduceJREPath, reduceOverSubtree, reduceRuntimeExceptions, reduceCause 
	}

	/**
	 * <p>This interface allow use conditional tracing. This interface is lambda-oriented</p>
	 *
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	@FunctionalInterface
	public interface LoggerCallbackInterface {
		String process();
	}
	
	/**
	 * <p>Log message to logger file</p>
	 * @param level message level
	 * @param format message format
	 * @param parameters message parameters
	 * @return self
	 */
	LoggerFacade message(Severity level, String format, Object... parameters);

	/**
	 * <p>Log message to logger file if the severity level is available now</p>
	 * @param level message level
	 * @param callback callback to create message
	 * @return self
	 */
	LoggerFacade message(Severity level, LoggerCallbackInterface callback);
	
	/**
	 * <p>Log message and exception to logger file</p>
	 * @param level message level
	 * @param exception exception to log to logger file
	 * @param format message format
	 * @param parameters message parameters
	 * @return self
	 */
	LoggerFacade message(Severity level, Throwable exception, String format, Object... parameters);

	/**
	 * <p>Log message and exception to logger file if the severity level is available now</p>
	 * @param level message level
	 * @param exception exception to log to logger file
	 * @param callback callback to create message
	 * @return self
	 */
	LoggerFacade message(Severity level, Throwable exception, LoggerCallbackInterface callback);
	
	/**
	 * <p>Are the severity level message logged now for the given logger. Use this method to reduce logger calls</p> 
	 * @param level severity level to test
	 * @return true if yes
	 */
	boolean isLoggedNow(Severity level);
	
	/**
	 * <p>Get current reducing algorithms for the given logger</p>
	 * @return current reducing argorithms
	 */
	Set<Reducing> getReducing();
	
	/**
	 * <p>Set current reducing algorithms for the given logger</p>
	 * @param reducing reducing algorithm to set
	 * @return self
	 */
	LoggerFacade setReducing(Set<Reducing> reducing);
	
	/**
	 * <p>Push current reducing algorithms and set them to new values. Uses as pair to {@link #popReducing()}</p>
	 * @param reducing reducing algorithm to set
	 * @return self
	 * @see #popReducing()
	 */
	LoggerFacade pushReducing(Set<Reducing> reducing);
	
	/**
	 * <p>Restore current reducing algorithms from the stack. Uses as pair to {@link #pushReducing(Set)}</p>
	 * @return self
	 * @see #pushReducing(Set)
	 */
	LoggerFacade popReducing();
	
	/**
	 * <p>Create transaction logger instance. Use this call in the <b>try-with-resource</b> statement!</p>
	 * @param mark any string to mark transaction logger trace
	 * @return new logger instance
	 */
	LoggerFacade transaction(final String mark);
	
	/**
	 * <p>Rollback transaction trace. If the logger instance is not a transaction logger, this call has no effect. Use this method as the last call in the <b>try-with-resource</b> block body!</p>
	 */
	void rollback();
	
	/**
	 * <p>Close logger. If this logger is transaction logger and it has any trace, the trace will be sent to  the log file. To avoid this, use {@link #rollback()} call in the <b>try-with-resource</b> block body.</p>
	 */
	void close();
}
