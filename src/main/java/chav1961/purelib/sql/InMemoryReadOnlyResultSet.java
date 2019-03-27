package chav1961.purelib.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class InMemoryReadOnlyResultSet extends AbstractReadOnlyResultSet {
	private final Statement				stmt;
	private final AbstractContent		content;
	private final Map<String, Class<?>>	map;

	public InMemoryReadOnlyResultSet(final Statement stmt, final ResultSetMetaData rsmd, final int resultSetType, final AbstractContent content) {
		this(stmt,rsmd,resultSetType,content,SQLUtils.DEFAULT_CONVERTOR);
	}
	
	public InMemoryReadOnlyResultSet(final Statement stmt, final ResultSetMetaData rsmd, final int resultSetType, final AbstractContent content, final Map<String, Class<?>> map) {
		super(rsmd, resultSetType);
		if (stmt == null) {
			throw new NullPointerException("Statement can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			this.stmt = stmt;
			this.content = content;
			this.map = map;
		}
	}

	public InMemoryReadOnlyResultSet(final ResultSetMetaData rsmd, final int resultSetType, final AbstractContent content) {
		this(rsmd,resultSetType,content,SQLUtils.DEFAULT_CONVERTOR);
	}
	
	public InMemoryReadOnlyResultSet(final ResultSetMetaData rsmd, final int resultSetType, final AbstractContent content, final Map<String, Class<?>> map) {
		super(rsmd, resultSetType);
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			this.stmt = null;
			this.content = content;
			this.map = map;
		}
	}

	@Override
	public Statement getStatement() throws SQLException {
		return stmt;
	}

	@Override
	protected AbstractContent getContent() {
		return content;
	}
	
	@Override
	public Object getObject(final int columnIndex) throws SQLException {
		return getObject(columnIndex,map);
	}
}
