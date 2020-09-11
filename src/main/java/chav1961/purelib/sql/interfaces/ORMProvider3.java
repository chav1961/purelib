package chav1961.purelib.sql.interfaces;

import java.sql.SQLException;

public interface ORMProvider3 extends AutoCloseable {
	void close() throws SQLException;
	void insert() throws SQLException;
	void update() throws SQLException;
	void delete() throws SQLException;
	void refresh() throws SQLException;
	boolean next() throws SQLException;
}
