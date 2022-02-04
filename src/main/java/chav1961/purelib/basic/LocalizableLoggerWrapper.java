package chav1961.purelib.basic;

import java.net.URI;
import java.util.Set;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class is a localizable implementation of {@linkplain LoggerFacade} interface. This class is used as a wrapper to any other {@linkplain LoggerFacade}
 * instance. It has a {@linkplain Localizer} associated with it, and always tries to transalate all format strings in the messages with the localizer.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.6
 */
public class LocalizableLoggerWrapper implements LoggerFacade {
	public static final URI		LOGGER_URI = URI.create(LoggerFacade.LOGGER_SCHEME+":i18n:/");
	
	private final Localizer		localizer;
	private final LoggerFacade	nested;

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer associated with the logger
	 * @param nested nested logger to print messages to
	 * @throws NullPointerException when any parameter is null
	 */
	public LocalizableLoggerWrapper(final Localizer localizer, final LoggerFacade nested) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (nested == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.nested = nested;
		}
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
			return null;
		}
	}
	
	@Override
	public LoggerFacade message(final Severity level, final String format, final Object... parameters) throws NullPointerException {
		nested.message(level,localize(format),parameters);
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final LoggerCallbackInterface callback) throws NullPointerException {
		nested.message(level,()->localize(callback.process()));
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final String format, final Object... parameters) throws NullPointerException {
		nested.message(level,exception,localize(format),parameters);
		return this;
	}

	@Override
	public LoggerFacade message(final Severity level, final Throwable exception, final LoggerCallbackInterface callback) throws NullPointerException {
		nested.message(level,exception,()->localize(callback.process()));
		return this;
	}

	@Override
	public boolean isLoggedNow(final Severity level) throws NullPointerException {
		return nested.isLoggedNow(level);
	}

	@Override
	public Set<Reducing> getReducing() {
		return nested.getReducing();
	}

	@Override
	public LoggerFacade setReducing(final Set<Reducing> reducing) throws NullPointerException {
		return nested.setReducing(reducing);
	}

	@Override
	public LoggerFacade setReducing(final Reducing... reducing) throws NullPointerException {
		return nested.setReducing(reducing);
	}

	@Override
	public LoggerFacade pushReducing(final Set<Reducing> reducing) throws NullPointerException {
		return nested.pushReducing(reducing);
	}

	@Override
	public LoggerFacade pushReducing(final Reducing... reducing) throws NullPointerException {
		return nested.pushReducing(reducing);
	}

	@Override
	public LoggerFacade popReducing() {
		nested.popReducing();
		return this;
	}

	@Override
	public LoggerFacade transaction(final String mark) throws IllegalArgumentException {
		return new LocalizableLoggerWrapper(localizer,nested.transaction(mark));
	}

	@Override
	public LoggerFacade transaction(final String mark, Class<?> root) throws NullPointerException, IllegalArgumentException {
		return new LocalizableLoggerWrapper(localizer,nested.transaction(mark,root));
	}

	@Override
	public void rollback() {
		nested.rollback();
	}

	@Override
	public void close() {
		nested.close();
	}
	
	private String localize(final String source) {
		if (!localizer.containsKey(source)) {
			return source;
		}
		else {
			try{return localizer.getValue(source);
			} catch (LocalizationException e) {
				return source;
			}
		}
	}
}
