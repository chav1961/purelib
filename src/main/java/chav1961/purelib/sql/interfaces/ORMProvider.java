package chav1961.purelib.sql.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface ORMProvider<Key,Record> extends AutoCloseable {
	ORMProvider<Key,Record> associate(Connection conn) throws SQLException;
	void close() throws SQLException;
	
	public interface ORMProviderIterator<Key,Record> extends Iterable<Record>{
		Key getCurrentKey() throws SQLException;
		Record getCurrentRecord() throws SQLException;
	}
	
	long contentSize() throws SQLException;
	ORMProviderIterator<Key,Record> content() throws SQLException;
	ORMProviderIterator<Key,Record> content(long from, long count) throws SQLException;
	ORMProviderIterator<Key,Record> content(String filter) throws SQLException;
	ORMProviderIterator<Key,Record> content(String filter, long from, long count) throws SQLException;
	ORMProviderIterator<Key,Record> content(String filter, String ordering) throws SQLException;
	ORMProviderIterator<Key,Record> content(String filter, String ordering, long from, long count) throws SQLException;
	
	void create(Key key, Record record) throws SQLException;
	void read(Key key, Record record) throws SQLException;
	void update(Key key, Record record) throws SQLException;
	void delete(Key key) throws SQLException;
}
