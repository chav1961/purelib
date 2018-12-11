package chav1961.purelib.sql.interfaces;

import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface QueryExecutor {
	int executeUpdate(final Object... parameters) throws SQLException;
	Object[][] executeQuery(final Object... parameters) throws SQLException;
	ParameterMetaData getParmMetaData() throws SQLException;
	ResultSetMetaData getRsMetaData() throws SQLException;
}
