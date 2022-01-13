package chav1961.purelib.sql.interfaces;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface InstanceManager<Key, Inst> extends AutoCloseable {
	Class<?> getInstanceType();
	Class<?> getKeyType();
	boolean isReadOnly();
	Inst newInstance() throws SQLException;
	Key newKey() throws SQLException;
	default Key newKey(Inst inst) throws SQLException {return newKey();};
	Key getKey(Inst inst) throws SQLException;
	Inst clone(Inst inst) throws SQLException;
	void loadInstance(ResultSet rs, Inst inst) throws SQLException;
	void storeInstance(ResultSet rs, Inst inst) throws SQLException;
	@Override
	void close() throws SQLException; 
}
