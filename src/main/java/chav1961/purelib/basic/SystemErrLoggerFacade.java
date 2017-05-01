package chav1961.purelib.basic;

import java.io.PrintStream;

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
	private final PrintStream	ps;

	public SystemErrLoggerFacade() {
		this(System.err);
	}
	
	public SystemErrLoggerFacade(final PrintStream ps) {
		super();
		this.ps = ps;
	}
	
	public SystemErrLoggerFacade(final String mark, final Class<?> root) {
		this(System.err,mark, root);
	}

	public SystemErrLoggerFacade(final PrintStream ps,final String mark, final Class<?> root) {
		super(mark, root);
		this.ps = ps;
	}
	
	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new SystemErrLoggerFacade(ps,mark,root);
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		ps.println(level+": "+text);
		if (throwable != null) {
			throwable.printStackTrace(ps);
		}
	}
}
