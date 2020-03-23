package chav1961.purelib.sql.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.enumerations.ContinueMode;

public interface ORMProvider<Record> extends AutoCloseable {
	ORMProvider<Record> associate(Connection conn) throws SQLException;
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
	
	void create(Record record) throws SQLException;
	void read(Record record) throws SQLException;
	void update(Record record) throws SQLException;
	void delete(Record record) throws SQLException;
	Record newRecord() throws SQLException; 
	Record duplicateRecord(Record record) throws SQLException; 
}
