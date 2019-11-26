package chav1961.purelib.basic;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class is an implementation of {@link LoggerFacade} interface for the java.util.logging target log file.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see LoggerFacade
 * @see AbstractLoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.3
 */

public class StandardJRELoggerFacade extends AbstractLoggerFacade {
	private final Logger	actualLogger;
	
	public StandardJRELoggerFacade() {
		super();
		this.actualLogger = Logger.getGlobal();
	}

	public StandardJRELoggerFacade(final String mark, final Class<?> root, final Set<Reducing> reducing) {
		super(mark, root, reducing);
		this.actualLogger = Logger.getLogger(root.getCanonicalName());
	}

	StandardJRELoggerFacade(final Logger logger) {
		super();
		this.actualLogger = logger;
	}
	
	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new StandardJRELoggerFacade(mark,root,getReducing());
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		if (throwable == null) {
			switch (level) {
				case debug 		: actualLogger.log(Level.FINE,text);	break;
				case error 		: actualLogger.log(Level.SEVERE,text);	break;
				case info 		: actualLogger.log(Level.INFO,text);	break;
				case severe 	: actualLogger.log(Level.SEVERE,text);	break;
				case trace 		: actualLogger.log(Level.FINEST,text);	break;
				case warning	: actualLogger.log(Level.WARNING,text);	break;
			}
		}
		else {
			switch (level) {
				case debug 		: actualLogger.log(Level.FINE,text,throwable);		break;
				case error 		: actualLogger.log(Level.SEVERE,text,throwable);	break;
				case info 		: actualLogger.log(Level.INFO,text,throwable);		break;
				case severe 	: actualLogger.log(Level.SEVERE,text,throwable);	break;
				case trace 		: actualLogger.log(Level.FINEST,text,throwable);	break;
				case warning	: actualLogger.log(Level.WARNING,text,throwable);	break;
			}
		}
	}
}
