package chav1961.purelib.basic;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class is an implementation of {@link LoggerFacade} interface for the java.util.logging package.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see LoggerFacade
 * @see AbstractLoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.6
 */

public class DefaultLoggerFacade extends AbstractLoggerFacade {
	public static final URI						LOGGER_URI = URI.create(LoggerFacade.LOGGER_SCHEME+":default:/");
	private static final Map<Severity,Level>	DECODE = new HashMap<Severity,Level>();  
	
	static {
		DECODE.put(Severity.trace,Level.FINE);
		DECODE.put(Severity.debug,Level.FINE);
		DECODE.put(Severity.info,Level.INFO);
		DECODE.put(Severity.warning,Level.WARNING);
		DECODE.put(Severity.error,Level.SEVERE);
		DECODE.put(Severity.severe,Level.SEVERE);
	}
																
	private final Logger	logger;
	
	public DefaultLoggerFacade() {
		super();
		this.logger = Logger.getLogger(this.getClass().getName());
	}

	public DefaultLoggerFacade(String mark, Class<?> root) {
		super(mark, root, new HashSet<>());
		this.logger = Logger.getLogger(mark);
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
	protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {
		return new DefaultLoggerFacade(mark, root);
	}

	@Override
	protected void toLogger(Severity level, String text, Throwable throwable) {
		if (throwable != null) {
			logger.log(DECODE.get(level),text,throwable);
		}
		else {
			logger.log(DECODE.get(level),text);
		}
	}

}
