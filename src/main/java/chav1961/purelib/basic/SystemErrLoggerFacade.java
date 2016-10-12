package chav1961.purelib.basic;

import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class is an implementation of {@link LoggerFacade} interface for the System.err target log file.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see LoggerFacade
 * @see AbstractLoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class SystemErrLoggerFacade extends AbstractLoggerFacade {
	public SystemErrLoggerFacade() {
		super();
	}

	public SystemErrLoggerFacade(String mark, Class<?> root) {
		super(mark, root);
	}

	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {
		return new SystemErrLoggerFacade(mark,root);
	}

	@Override
	protected void toLogger(Severity level, String text, Throwable throwable) {
		System.err.println(level+": "+text);
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}
}
