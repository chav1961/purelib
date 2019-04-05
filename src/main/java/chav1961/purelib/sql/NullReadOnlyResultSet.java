package chav1961.purelib.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class NullReadOnlyResultSet extends AbstractReadOnlyResultSet {
	private final NullContent	content = new NullContent();
	private final Statement		stmt;

	public NullReadOnlyResultSet(final ResultSetMetaData rsmd, final int resultSetType) {
		super(rsmd, resultSetType);
		this.stmt = null;
	}

	public NullReadOnlyResultSet(final Statement stmt, final ResultSetMetaData rsmd, final int resultSetType) {
		super(rsmd, resultSetType);
		if (stmt == null) {
			throw new NullPointerException("Statement can't be null"); 
		}
		else {
			this.stmt = stmt;
		}
	}
	
	@Override
	protected AbstractContent getContent() {
		return content;
	}

	@Override
	public Statement getStatement() throws SQLException {
		return stmt;
	}
}
