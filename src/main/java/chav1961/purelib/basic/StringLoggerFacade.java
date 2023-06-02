package chav1961.purelib.basic;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class StringLoggerFacade extends AbstractLoggerFacade {
	public static final URI		LOGGER_URI = URI.create(LoggerFacade.LOGGER_SCHEME+":string:/");
	
	private final StringBuilder	sb = new StringBuilder();
	
	public StringLoggerFacade() {
		super();
	}

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
