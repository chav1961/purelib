package chav1961.purelib.basic.logs;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class is an implementation of {@link LoggerFacade} interface for the {@linkplain StringBuilder} content.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see LoggerFacade
 * @see AbstractLoggerFacade
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
public class StringLoggerFacade extends AbstractLoggerFacade {
	public static final URI		LOGGER_URI = URI.create(LoggerFacade.LOGGER_SCHEME+":string:/");
	
	private final StringBuilder	sb = new StringBuilder();

	/**
	 * <p>Constructor of the class instance</p>
	 */
	public StringLoggerFacade() {
		super();
	}

	/**
	 * <p>Constructor of the class instance</p>
	 * @param mark mark for logger. Can be null or empty.
	 * @param root transaction root class. Can't be null.
	 * @param reducing set of reducing rules. Can't be null but can be empty
	 */
	public StringLoggerFacade(final String mark, final Class<?> root, final Set<Reducing> reducing) {
		super(mark, root, reducing);
	}

	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new StringLoggerFacade(mark, root, this.getReducing());
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
			return new StringLoggerFacade();
		}
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		sb.append('[').append(level).append("]: ").append(text).append('\n');
		if (throwable != null) {
			sb.append(Arrays.toString(throwable.getStackTrace())).append('\n');
		}
	}
}
