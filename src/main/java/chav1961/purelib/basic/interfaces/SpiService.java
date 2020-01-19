package chav1961.purelib.basic.interfaces;

import java.net.URI;

import chav1961.purelib.basic.exceptions.EnvironmentException;

/**
 * <p>This interface describes any SPI member. Every purelib SPI implementation <i>must</i> implements this interface (see META-INF/services for details)</p>
 * @param Type any type of the SPI service
 *
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public interface SpiService<Type> {
	/**
	 * <p>Does the SPI support the given resource URI</p>
	 * @param resource resource URI to check
	 * @return true if supports
	 * @throws NullPointerException on null resource URI
	 */
	boolean canServe(URI resource) throws NullPointerException;
	
	/**
	 * <p>Get SPI instance to serve the given resource URI</p>
	 * @param resource resource URI to get instance for
	 * @return instance to serve the URI. Can't be null
	 * @throws EnvironmentException when instance can't be created for the given URI for any reason
	 * @throws NullPointerException when requested URI is null
	 * @throws IllegalArgumentException when requested URI has wrong format or is not supported by the given SPI service
	 */
	Type newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException;
}
