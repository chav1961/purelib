package chav1961.purelib.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Iterator;

public class IteratorBasedReadOnlyResultSet extends AbstractReadOnlyResultSet {
	private final IteratorContent	ic;
	
	public IteratorBasedReadOnlyResultSet(final Iterator<Object[]> iterator, final ResultSetMetaData rsmd) {
		super(rsmd, ResultSet.TYPE_FORWARD_ONLY);
		this.ic = new IteratorContent(iterator);
	}
	
	@Override
	public Statement getStatement() throws SQLException {
		throw new SQLFeatureNotSupportedException("This result set doesn't have a statement");
	}

	@Override
	protected AbstractContent getContent() {
		return this.ic;
	}
}
