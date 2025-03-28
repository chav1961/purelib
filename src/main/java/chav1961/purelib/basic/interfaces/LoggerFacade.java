package chav1961.purelib.basic.interfaces;

import java.io.Closeable;
import java.net.URI;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.Set;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;

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
 * @last.update 0.0.7
 */

public interface LoggerFacade extends Closeable, SpiService<LoggerFacade> {
	/**
	 * <p>Logger URI scheme</p>
	 * @since 0.0.6
	 */
	public static final String	LOGGER_SCHEME = "logger";
	
	static final String			LOGGER_INSTANCE_KEY = "loggerInstance";  
	
	/**
	 * <p>This enumeration describes message severity. Order of the enumeration constants is important!</p>
	 *
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 * @last.update 0.0.7
	 */
	public enum Severity {
		tooltip(false, false), 
		trace(true, false), 
		debug(true, false),
		info(true, false),
		note(true, false),
		warning(true, false),
		error(true, true),
		severe(true, true);
		
		private final boolean	mandatory;
		private final boolean	blinkingRequired;
		
		private Severity(final boolean mandatory, final boolean blinkingRequired) {
			this.mandatory = mandatory;
			this.blinkingRequired = blinkingRequired; 
		}
		
		/**
		 * <p>Is the severity level mandatory. Non-mandatory severity can be ignored for any purposes</p>
		 * @return true if mandatory, false otherwise
		 */
		public boolean isMandatory() {
			return mandatory;
		}

		/**
		 * <p>Is attention required for the given severity (for example, blinking)</p>
		 * @return true if attention required, false otherwise
		 */
		public boolean isAttentionRequired() {
			return blinkingRequired;
		}
	}

	/**
	 * <p>This enumeration describes reducing algorithms used in the logger</p>
	 *
	 * @see LoggerFacade LoggerFacade 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	public enum Reducing {
		/**
		 * <p>Exclude standard JRE classes and methods from any stack trace</p>
		 */
		reduceJREPath, 
		/**
		 * <p>Exclude all stack trace elements in transaction loggers from root to logger creation point</p>  
		 */
		reduceOverSubtree,
		/**
		 * <p>Exclude any stack trace for {@linkplain RuntimeException} and it's children from the logging</p> 
		 */
		reduceRuntimeExceptions,
		/**
		 * <p>Don't print <b>cause</b> stack trace for any exceptions</p>
		 */
		reduceCause 
	}

	/**
	 * <p>This functional interface allow use conditional tracing</p>
	 *
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 * @last.update 0.0.5
	 */
	@FunctionalInterface
	public interface LoggerCallbackInterface {
		String process() throws LocalizationException;
	}
	
	/**
	 * <p>Log message to logger file</p>
	 * @param level message level
	 * @param format message format
	 * @param parameters message parameters
	 * @return self
	 */
	LoggerFacade message(Severity level, String format, Object... parameters) throws NullPointerException;

	/**
	 * <p>Log message to logger file if the severity level is available now</p>
	 * @param level message level
	 * @param callback callback to create message
	 * @return self
	 */
	LoggerFacade message(Severity level, LoggerCallbackInterface callback) throws NullPointerException;
	
	/**
	 * <p>Log message and exception to logger file</p>
	 * @param level message level
	 * @param exception exception to log to logger file
	 * @param format message format
	 * @param parameters message parameters
	 * @return self
	 */
	LoggerFacade message(Severity level, Throwable exception, String format, Object... parameters) throws NullPointerException;

	/**
	 * <p>Log message and exception to logger file if the severity level is available now</p>
	 * @param level message level
	 * @param exception exception to log to logger file
	 * @param callback callback to create message
	 * @return self
	 */
	LoggerFacade message(Severity level, Throwable exception, LoggerCallbackInterface callback) throws NullPointerException;
	
	/**
	 * <p>Are the severity level message logged now for the given logger. Use this method to reduce logger calls</p> 
	 * @param level severity level to test
	 * @return true if yes
	 * @throws NullPointerException when redicing set is null
	 */
	boolean isLoggedNow(Severity level) throws NullPointerException;
	
	/**
	 * <p>Get current reducing algorithms for the given logger</p>
	 * @return current reducing argorithms
	 */
	Set<Reducing> getReducing();
	
	/**
	 * <p>Set current reducing algorithms for the given logger</p>
	 * @param reducing reducing algorithm to set
	 * @return self
	 * @throws NullPointerException when redicing set is null
	 */
	LoggerFacade setReducing(Set<Reducing> reducing) throws NullPointerException;

	/**
	 * <p>Set current reducing algorithms for the given logger</p>
	 * @param reducing reducing algorithm to set
	 * @return self
	 * @throws NullPointerException when redicing set is null
	 * @last.update 0.0.6
	 */
	default LoggerFacade setReducing(Reducing... reducing) throws NullPointerException {
		return setReducing(Set.of(reducing));
	}
	
	/**
	 * <p>Push current reducing algorithms and set them to new values. Uses as pair to {@link #popReducing()}</p>
	 * @param reducing reducing algorithm to set
	 * @return self
	 * @throws NullPointerException when redicing set is null
	 * @see #popReducing()
	 */
	LoggerFacade pushReducing(Set<Reducing> reducing) throws NullPointerException;

	/**
	 * <p>Push current reducing algorithms and set them to new values. Uses as pair to {@link #popReducing()}</p>
	 * @param reducing reducing algorithm to set
	 * @return self
	 * @throws NullPointerException when any redicing is null
	 * @see #popReducing()
	 * @last.update 0.0.6
	 */
	default LoggerFacade pushReducing(Reducing... reducing) throws NullPointerException {
		return pushReducing(Set.of(reducing));
	}
	
	/**
	 * <p>Restore current reducing algorithms from the stack. Uses as pair to {@link #pushReducing(Set)}</p>
	 * @return self
	 * @see #pushReducing(Set)
	 */
	LoggerFacade popReducing();
	
	/**
	 * <p>Create <b>transaction</b> logger instance. Use this call in the <b>try-with-resource</b> statement! Any messages to transaction logger will not be send anywhere before
	 * transaction end. Real transferring will be initiated when {@linkplain #close()} method would be called. Calling {@linkplain #rollback()} method immediately before calling
	 * {@linkplain #close()} scratches all messages from the transaction logger. This functionality is designed especially for reducing logger output:
	 * successful ending of the transaction doesn't produce any messages, but fatal ending keeps detailed trace for the longer analysis:</p>
	 * <code>
	 * try(LoggerFacade logger = ...transaction("MyTransaction")) {<br>
	 * . . .<br>
	 * &nbsp;&nbsp;&nbsp;logger.message(...);<br>
	 * . . .<br>
	 * &nbsp;&nbsp;&nbsp;logger.rollback(); // scratch all logger content on successful ending<br>
	 * }<br>
	 * </code>
	 * For performance reason, method {@linkplain #transaction(String, Class)} is more preferred to use.
	 * @param mark any string to mark transaction logger trace for identification purposes. Can't be null or empty
	 * @return new logger instance. It must be closed mandatory
	 * @throws IllegalArgumentException when mark string is null or empty
	 */
	LoggerFacade transaction(final String mark) throws IllegalArgumentException;

	/**
	 * <p>Create <b>transaction</b> logger instance. Use this call in the <b>try-with-resource</b> statement! Any messages to transaction logger will not be send anywhere before
	 * transaction end. Real transferring will be initiated when {@linkplain #close()} method would be called. Calling {@linkplain #rollback()} method immediately before calling
	 * {@linkplain #close()} scratches all messages from the transaction logger. This functionality is designed especially for reducing logger output:
	 * successful ending of the transaction doesn't produce any messages, but fatal ending keeps detailed trace for the longer analysis:</p>
	 * <code>
	 * try(LoggerFacade logger = ...transaction("MyTransaction")) {<br>
	 * . . .<br>
	 * &nbsp;&nbsp;&nbsp;logger.message(...);<br>
	 * . . .<br>
	 * &nbsp;&nbsp;&nbsp;logger.rollback(); // scratch all logger content on successful ending<br>
	 * }<br>
	 * </code>
	 * @param mark any string to mark transaction logger trace for identification purposes
	 * @param root root class where transaction logger was created
	 * @return new logger instance. It must be closed mandatory
	 * @throws IllegalArgumentException when mark string is null or empty
	 * @throws NullPointerException when root class is null
	 */
	LoggerFacade transaction(final String mark, final Class<?> root) throws NullPointerException, IllegalArgumentException;
	
	/**
	 * <p>Rollback transaction trace. If the logger instance is not a transaction logger, this call has no effect. Use this method as the last call in the <b>try-with-resource</b> 
	 * block body!</p>
	 * @see #transaction(String)
	 */
	void rollback();
	
	/**
	 * <p>Close logger. If this logger is transaction logger and it has any trace, the trace will be sent to  the log file. To avoid this, use {@link #rollback()} call in the 
	 * <b>try-with-resource</b> block body.</p>
	 * @see #transaction(String)
	 */
	void close();
	
	/**
	 * <p>This class is a factory to get logger facade by it's URI. It implements a 'Factory' template and wraps call to {@linkplain LocalizerFactory#getLocalizer(URI)}</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 * @last.update 0.0.7
	 */
	public final static class Factory {
		private Factory() {}

		/**
		 * <p>Can serve given logger URI ?</p>
		 * @param loggerUri logger URI to test. Can't be null and must contain {@value #LOGGER_SCHEME} scheme
		 * @return true if can, false otherwise
		 */
		public static boolean canServe(final URI loggerUri) {
			if (loggerUri == null || !LOGGER_SCHEME.equals(loggerUri.getScheme())) {
				throw new IllegalArgumentException("Logger facade URI can't be null and must have scheme ["+LOGGER_SCHEME+"]"); 
			}
			else {
				for (LoggerFacade item : ServiceLoader.load(LoggerFacade.class)) {
					if (item.canServe(loggerUri)) {
						return true;
					}
				}
				return false; 
			}
		}
		
		/**
		 * <p>Get logger facade by URI.</p> 
		 * @param loggerUri logger facade URI to get logger for. Can't be null and must have scheme {@value LoggerFacade#LOGGER_SCHEME}
		 * @return logger facade created
		 * @throws IllegalArgumentException when logger facade URI is null or doesn't have {@value LoggerFacade#LOGGER_SCHEME} scheme
		 */
		public static LoggerFacade newInstance(final URI loggerUri) throws IllegalArgumentException{
			if (loggerUri == null || !LOGGER_SCHEME.equals(loggerUri.getScheme())) {
				throw new IllegalArgumentException("Logger facade URI can't be null and must have scheme ["+LOGGER_SCHEME+"]"); 
			}
			else {
				for (LoggerFacade item : ServiceLoader.load(LoggerFacade.class)) {
					if (item.canServe(loggerUri)) {
						return item.newInstance(loggerUri);
					}
				}
				throw new IllegalArgumentException("Logger facade for URI ["+loggerUri+"] not found anywhere"); 
			}
		}

		/**
		 * <p>Get logger facade by any of the URI list.</p> 
		 * @param loggerUris logger facade URIs to get logger for. Can't be null, empty and all the items must have scheme {@value LoggerFacade#LOGGER_SCHEME}
		 * @return logger facade created
		 * @throws IllegalArgumentException when logger facade URIs is null, empty or doesn't have {@value LoggerFacade#LOGGER_SCHEME} scheme
		 */
		public static LoggerFacade newInstance(final URI... loggerUris) throws IllegalArgumentException{
			if (loggerUris == null || loggerUris.length == 0 || Utils.checkArrayContent4Nulls(loggerUris) >= 0) {
				throw new IllegalArgumentException("Logger facade URI list is null, empty or contains nulls inside"); 
			}
			else {
				for (URI entity : loggerUris) {
					if (!LOGGER_SCHEME.equals(entity)) {
						throw new IllegalArgumentException("Logger facade URIs must have scheme ["+LOGGER_SCHEME+"]"); 
					}
					for (LoggerFacade item : ServiceLoader.load(LoggerFacade.class)) {
						if (item.canServe(entity)) {
							return item.newInstance(entity);
						}
					}
				}
				throw new IllegalArgumentException("Logger facade for URIs ["+Arrays.toString(loggerUris)+"] not found anywhere"); 
			}
		}
	}
}
