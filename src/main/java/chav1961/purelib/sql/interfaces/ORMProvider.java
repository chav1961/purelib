package chav1961.purelib.sql.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.enumerations.ContinueMode;

public interface ORMProvider<Key,Record> extends AutoCloseable {
	ORMProvider<Key,Record> associate(Connection conn) throws SQLException;
	void close() throws SQLException;
	
	@FunctionalInterface
	public interface ContentIteratorCallback<Record> {
		ContinueMode process(long seq, long offset, Record record) throws SQLException;
	}
	
	long contentSize() throws SQLException;
	long contentSize(String filter) throws SQLException;
	void content(Record item, ContentIteratorCallback<Record> callback) throws SQLException;
	void content(Record item, ContentIteratorCallback<Record> callback, long from, long count) throws SQLException;
	void content(Record item, ContentIteratorCallback<Record> callback, String filter) throws SQLException;
	void content(Record item, ContentIteratorCallback<Record> callback, String filter, long from, long count) throws SQLException;
	void content(Record item, ContentIteratorCallback<Record> callback, String filter, String ordering) throws SQLException;
	void content(Record item, ContentIteratorCallback<Record> callback, String filter, String ordering, long from, long count) throws SQLException;
	
	void create(Key key, Record record) throws SQLException;
	void read(Key key, Record record) throws SQLException;
	void update(Key key, Record record) throws SQLException;
	void delete(Key key) throws SQLException;
}
