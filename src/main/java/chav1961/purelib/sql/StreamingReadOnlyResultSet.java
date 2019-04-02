package chav1961.purelib.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class StreamingReadOnlyResultSet extends AbstractReadOnlyResultSet {

	protected StreamingReadOnlyResultSet(final ResultSetMetaData rsmd, final int resultSetType) {
		super(rsmd, resultSetType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Statement getStatement() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AbstractContent getContent() {
		// TODO Auto-generated method stub
		return null;
	}
}
