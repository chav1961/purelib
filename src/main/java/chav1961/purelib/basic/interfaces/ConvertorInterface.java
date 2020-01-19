package chav1961.purelib.basic.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

@FunctionalInterface
/**
 * <p>This interface describes any data type converters.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
public interface ConvertorInterface {
	/**
	 * <p>Convert content to instance of awaited class.</p>
	 * @param <T> instance type to convert data to
	 * @param awaited awaited instance class
	 * @param source instance to convert (can be null)
	 * @return instance converted (can be null if source was null)
	 * @throws NullPointerException when awaited class is null
	 * @throws ContentException errors on conversion stage
	 */
	<T> T convertTo(Class<T> awaited, Object source) throws NullPointerException, ContentException;
}
