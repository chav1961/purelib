package chav1961.purelib.basic;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Set;

import chav1961.purelib.basic.exceptions.EnvironmentException;
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
 * @last.update 0.0.6
 */

public class SystemErrLoggerFacade extends AbstractLoggerFacade {
	public static final URI		LOGGER_URI = URI.create(LoggerFacade.LOGGER_SCHEME+":err:/");
	
	private final PrintStream	ps;
	private final PrintWriter	pw;

	public SystemErrLoggerFacade() {
		this(System.err);
	}
	
	public SystemErrLoggerFacade(final PrintStream ps) {
		super();
		this.ps = ps;
		this.pw = null;
	}

	public SystemErrLoggerFacade(final PrintWriter pw) {
		super();
		this.ps = null;
		this.pw = pw;
	}
	
	public SystemErrLoggerFacade(final String mark, final Class<?> root, final Set<Reducing> reducing) {
		this(System.err,mark, root, reducing);
	}

	public SystemErrLoggerFacade(final PrintStream ps, final String mark, final Class<?> root, final Set<Reducing> reducing) {
		super(mark, root, reducing);
		this.ps = ps;
		this.pw = null;
	}

	public SystemErrLoggerFacade(final PrintWriter pw, final String mark, final Class<?> root, final Set<Reducing> reducing) {
		super(mark, root, reducing);
		this.ps = null;
		this.pw = pw;
	}
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null"); 
		}
		else {
			return URIUtils.canServeURI(resource, LOGGER_URI);
		}
	}

	@Override
	public LoggerFacade newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null"); 
		}
		else {
			return this;
		}
	}
	
	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new SystemErrLoggerFacade(ps,mark,root,getReducing());
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		if (level != Severity.tooltip) {
			final String	msg = "System.err.logger["+level+"]: "+text;
			
			if (ps != null) {
				ps.println(msg);
				if (throwable != null) {
					throwable.printStackTrace(ps);
				}
				ps.flush();
			}
			else if (pw != null) {
				pw.println(msg);
				if (throwable != null) {
					throwable.printStackTrace(pw);
				}
				pw.flush();
			}
		}
	}
}
