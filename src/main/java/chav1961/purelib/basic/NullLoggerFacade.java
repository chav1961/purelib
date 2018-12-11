package chav1961.purelib.basic;

import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class is an implementation of {@link LoggerFacade} interface for the empty target log file.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see LoggerFacade
 * @see AbstractLoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class NullLoggerFacade extends AbstractLoggerFacade {
	public NullLoggerFacade() {
		super();
	}

	public NullLoggerFacade(final String mark, final Class<?> root) {
		super(mark, root);
	}

	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new NullLoggerFacade(mark,root);
	}

	@Override protected void toLogger(final Severity level, final String text, final Throwable throwable) {}
}
