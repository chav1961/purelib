package chav1961.purelib.basic;

import java.io.PrintStream;
import java.util.Set;

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
 * @lastUpdate 0.0.3
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
	
	public SystemErrLoggerFacade(final String mark, final Class<?> root, final Set<Reducing> reducing) {
		this(System.err,mark, root, reducing);
	}

	public SystemErrLoggerFacade(final PrintStream ps,final String mark, final Class<?> root, final Set<Reducing> reducing) {
		super(mark, root, reducing);
		this.ps = ps;
	}
	
	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new SystemErrLoggerFacade(ps,mark,root,getReducing());
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		if (level != Severity.tooltip) {
			ps.println("System.err.logger["+level+"]: "+text);
			if (throwable != null) {
				throwable.printStackTrace(ps);
			}
		}
	}
}
