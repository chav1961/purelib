package chav1961.purelib.sql.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.table.TableModel;

import chav1961.purelib.basic.exceptions.ContentException;

public interface ORMProvider2<Record> extends AutoCloseable {
	public enum Action {
		STOP, CONTINUE, UPDATE_AND_CONTINUE, DELETE_AND_CONTINUE 
	}
	
	@FunctionalInterface
	public interface ContentIteratorCallback<Record> {
		Action process(long seq, Connection conn, Record record) throws SQLException, ContentException;
	}

	@FunctionalInterface
	public interface RecordCreationCallback<Record> {
		Record create(Connection conn) throws SQLException, ContentException;
		default Record duplicate(Connection conn, Record source) throws SQLException, ContentException {
			return create(conn);
		}
	}
	
	Connection getConnection(); 
	ORMProvider2<Record> setConnection(Connection newConnection) throws SQLException;
	
	@Override
	void close() throws SQLException;
	
	String getFilter();
	ORMProvider2<Record> setFilter(String filter) throws SQLException;
	String getOrdering();
	ORMProvider2<Record> setOrdering(String ordering) throws SQLException;
	long[] getRange();
	ORMProvider2<Record> setRange(long[] range) throws SQLException;
	
	ORMProvider2<Record> push();
	ORMProvider2<Record> pop();
	
	TableModel getTableModel() throws SQLException;
	
	long contentSize() throws SQLException;
	default ORMProvider2<Record> content(Record rec, ContentIteratorCallback<Record> callback) throws SQLException {
		return content(rec,true,callback);
	}
	ORMProvider2<Record> content(Record rec, boolean keysOnly, ContentIteratorCallback<Record> callback) throws SQLException;
	ORMProvider2<Record> refresh();

	ORMProvider2<Record> insert(Record record) throws SQLException;
	ORMProvider2<Record> read(Record record) throws SQLException;
	ORMProvider2<Record> update(Record record) throws SQLException;
	ORMProvider2<Record> delete(Record record) throws SQLException;
}
