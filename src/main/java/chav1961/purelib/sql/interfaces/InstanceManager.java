package chav1961.purelib.sql.interfaces;

import java.sql.ResultSet;
import java.sql.SQLException;

import chav1961.purelib.ui.swing.useful.DnDManager.DnDInterface;

/**
 * <p>This interface describes instance manager to use in Object-relational mapping (ORM).</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 * @param <Key> java set of primary key(s) for java instance  
 * @param <Inst> java instance type to map on database table 
 */
public interface InstanceManager<Key, Inst> extends AutoCloseable {
	/**
	 * <p>Get instance type</p>
	 * @return instance type. Can't be null
	 */
	Class<?> getInstanceType();
	
	/**
	 * <p>Get primary key set type</p>
	 * @return primary key set type. Can'tbe null
	 */
	Class<?> getKeyType();
	
	/**
	 * <p>Is the instance manager read only</p>
	 * @return true if yes
	 */
	boolean isReadOnly();
	
	/**
	 * <p>Create new 'clean' instance to use in your program.</p>
	 * @return instance created. Can't be null.
	 * @throws SQLException on any errors
	 */
	Inst newInstance() throws SQLException;
	
	/**
	 * <p>Create new unique primary key set for the new record in the database table</p> 
	 * @return key set created. Can't be null
	 * @throws SQLException on any errors
	 */
	Key newKey() throws SQLException;
	
	/**
	 * <p>Create new unique primary key set for the new record in the database table</p>
	 * @param inst instance template to create new key set (for example, only change last item in the key set). Can't be null 
	 * @return key set created. Can't be null
	 * @throws SQLException on any errors
	 */
	default Key newKey(Inst inst) throws SQLException {return newKey();};
	
	/**
	 * <p>Extract primary keys from instance</p>
	 * @param inst instance to extract keys from. Can'tbe null
	 * @return key set extracted. Can't be null
	 * @throws SQLException on any errors
	 */
	Key extractKey(Inst inst) throws SQLException;
	
	/**
	 * <p>Clone existent instance with new unique primary key set</p>
	 * @param inst instance to clone. Can't be null
	 * @return instance cloned. Can't be null
	 * @throws SQLException on any errors
	 */
	Inst clone(Inst inst) throws SQLException;
	
	/**
	 * <p>Fill instance content from the result set row.</p>
	 * @param rs result set to fill content from. Can't be null
	 * @param inst instance to fill. Can't be null
	 * @throws SQLException on any errors
	 */
	void loadInstance(ResultSet rs, Inst inst) throws SQLException;
	
	/**
	 * <p>Update result set row with instance content.</p>
	 * @param rs result set to update row. Can't be null
	 * @param inst instance to update row from. Can't be null
	 * @throws SQLException on any errors
	 */
	void storeInstance(ResultSet rs, Inst inst) throws SQLException;
	
	/**
	 * <p>Get field value from instance.</p>
	 * @param <T> field type awaited
	 * @param inst instance to get field from. Can't be null
	 * @param name field name to get. Can't be null or empty and must exists in the instance
	 * @return field value (can be null)
	 * @throws SQLException on any errors
	 */
	<T> T get(Inst inst, String name) throws SQLException;
	
	/**
	 * <p>Set field value in the instance.</p> 
	 * @param <T> field type awaited
	 * @param inst instance to get field in. Can't be null
	 * @param name field name to set. Can't be null or empty and must exists in the instance
	 * @param value field value (can be null)
	 * @return self (to use in call chains)
	 * @throws SQLException on any errors
	 */
	<T> InstanceManager<Key, Inst> set(Inst inst, String name,T value) throws SQLException;

	@Override
	void close() throws SQLException; 
}
