package chav1961.purelib.basic.interfaces;

import java.net.URI;

import chav1961.purelib.basic.exceptions.EnvironmentException;

/**
 * <p>This interface describes any SPI member. Every PureLib SPI implementation <i>must</i> implements this interface (see META-INF/services for details)</p>
 * @param <Type> any type of the SPI service
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 * @last.update 0.0.7
 */
public interface SpiService<Type> {
	/**
	 * <p>Does the SPI support the given resource URI</p>
	 * @param resource resource URI to check. Can't be null
	 * @return true if supports
	 * @throws NullPointerException on null resource URI
	 */
	boolean canServe(URI resource) throws NullPointerException;
	
	/**
	 * <p>Explain why this service can't processing this URI.</p> 
	 * @param resource resource URI to check. Can't be null
	 * @return explanation string or null, if can't give adequate explanation.
	 * @throws NullPointerException on null resource URI
	 * @since 0.0.7
	 */
	default String explainWhy(URI resource) throws NullPointerException {
		return null;
	}
	
	/**
	 * <p>Get SPI instance to serve the given resource URI</p>
	 * @param resource resource URI to get instance for. Can't be null
	 * @return instance to serve the URI. Can't be null
	 * @throws EnvironmentException when instance can't be created for the given URI for any reason
	 * @throws NullPointerException when requested URI is null
	 * @throws IllegalArgumentException when requested URI has wrong format or is not supported by the given SPI service
	 */
	Type newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException;

	/**
	 * <p>Get SPI instance to serve the given resource URI</p>
	 * @param resource resource URI to get instance for. Can't be null
	 * @param parameters advanced parameters to create instance. Can be null
	 * @return instance to serve the URI. Can't be null
	 * @throws EnvironmentException when instance can't be created for the given URI for any reason
	 * @throws NullPointerException when requested URI is null
	 * @throws IllegalArgumentException when requested URI has wrong format or is not supported by the given SPI service
	 * @since 0.0.7
	 */
	default Type newInstance(URI resource, Object... parameters) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return newInstance(resource);
	}
}
