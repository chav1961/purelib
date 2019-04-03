package chav1961.purelib.sql;

import java.sql.SQLException;

public abstract class AbstractContent implements AutoCloseable {
	public abstract boolean isStreaming();
	public abstract int getRowCount() throws SQLException;
	public abstract int getCurrentRow() throws SQLException;
	public abstract boolean setCurrentRow(int row) throws SQLException;
	public abstract Object[] getRow(int rowNum) throws SQLException;
	public abstract void close() throws SQLException;
}