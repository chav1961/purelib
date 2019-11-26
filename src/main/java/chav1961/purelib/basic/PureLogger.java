package chav1961.purelib.basic;

import java.util.Set;
import java.util.logging.Logger;

import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class is a wrapper for {@linkplain LoggerFacade} to the standard JRE {@linkplain Logger} functionality.</p>
 *  
 * @see LoggerFacade
 * @see AbstractLoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */

public class PureLogger extends Logger implements LoggerFacade {
	private final StandardJRELoggerFacade		stdLogger = new StandardJRELoggerFacade(this);

	public PureLogger(final String name, final String resourceBundleName) {
		super(name, resourceBundleName);
	}

	@Override
	public LoggerFacade message(final Severity level, final String format, final Object... parameters) {
		stdLogger.message(level, format, parameters);
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final LoggerCallbackInterface callback) {
		stdLogger.message(level, callback);
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final String format, final Object... parameters) {
		stdLogger.message(level, exception, format, parameters);
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final LoggerCallbackInterface callback) {
		stdLogger.message(level, exception, callback);
		return this;
	}

	@Override
	public boolean isLoggedNow(final Severity level) {
		return stdLogger.isLoggedNow(level);
	}

	@Override
	public Set<Reducing> getReducing() {
		return stdLogger.getReducing();
	}

	@Override
	public LoggerFacade setReducing(final Set<Reducing> reducing) {
		stdLogger.setReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade setReducing(final Reducing... reducing) {
		stdLogger.setReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade pushReducing(final Set<Reducing> reducing) {
		stdLogger.pushReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade pushReducing(final Reducing... reducing) {
		stdLogger.pushReducing(reducing);
		return this;
	}

	@Override
	public LoggerFacade popReducing() {
		stdLogger.popReducing();
		return this;
	}

	@Override
	public LoggerFacade transaction(final String mark) {
		return stdLogger.transaction(mark);
	}

	@Override
	public void rollback() {
	}

	@Override
	public LoggerFacade transaction(String mark, Class<?> root) throws NullPointerException, IllegalArgumentException {
		return stdLogger.transaction(mark,root);
	}
	
	@Override
	public void close() {
	}
}
