package chav1961.purelib.model.interfaces;

import java.io.Closeable;
import java.sql.SQLException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;

public interface ORMModelMapper<Key,Data> extends ModelMapper, Closeable {
	@FunctionalInterface
	public interface ForEachCallback<Data> {
		ContinueMode process(Data data) throws ContentException, SQLException;
	}
	
	void create(Data content) throws SQLException;
	void read(Data content) throws SQLException;
	void read(Key key, Data content) throws SQLException;
	void update(Data content) throws SQLException;
	void delete(Data content) throws SQLException;
	long forEach(Data content, ForEachCallback<Data> callback, String where, String order, int offset, int limit) throws ContentException, SQLException;
}
