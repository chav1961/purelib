package chav1961.purelib.sql.interfaces;

import java.sql.SQLException;

/**
 * <p>This interface describes unique id generator to use with the databases.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
@FunctionalInterface
public interface UniqueIdGenerator {
	/**
	 * <p>Get unique id</p>
	 * @return unique id
	 * @throws SQLException on any database errors
	 */
	long getId() throws SQLException;
	
	/**
	 * <p>Get unique id for the given entity</p>
	 * @param entity entity to get unique id for. Can't be null or empty
	 * @return unique id for the entity
	 * @throws SQLException on any database errors
	 */
	default long getId(final String entity) throws SQLException{
		return getId();
	}

	/**
	 * <p>Get unique id for the given entity</p>
	 * @param <T> entity sort. Must be {@linkplain Enum<?>} child
	 * @param entity entity to get unique id for. Can't be null or empty
	 * @return unique id for the entity
	 * @throws SQLException on any database errors
	 */
	default <T extends Enum<?>> long getId(final T entity) throws SQLException {
		return getId();
	}
}
