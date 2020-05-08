package chav1961.purelib.sql.interfaces;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>This interface describes simplest ORM provider. The only two actions for it are:</p>.
 * <ul>
 * <li>move {@linkplain ResultSet} content to class instance</li>
 * <li>bound class instance content with {@linkplain PreparedStatement}</li>
 * </ul>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @param <T> any type of class instance
 */
public interface ORMMapper<T> {
	/**
	 * <p>Fill class instance content with the result set values</p>
	 * @param content class instance to fill data with
	 * @param rs content source to fill class instance
	 * @throws SQLException
	 */
	void fromRecord(T content, ResultSet rs) throws SQLException;
	
	/**
	 * <p>Bound {@linkplain PreparedStatement} with class instance content</p>
	 * @param content content source to bound {@linkplain PreparedStatement} 
	 * @param ps prepared statement to bound
	 * @throws SQLException
	 */
	void toRecord(T content, PreparedStatement ps) throws SQLException;
}
