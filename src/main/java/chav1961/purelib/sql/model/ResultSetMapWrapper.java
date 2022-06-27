package chav1961.purelib.sql.model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import chav1961.purelib.model.MappedAdamClass;

public class ResultSetMapWrapper extends MappedAdamClass<String, Object> {
	private final ResultSet	rs;
	private final String[]	keys;
	
	public ResultSetMapWrapper(final ResultSet rs) throws SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else {
			final ResultSetMetaData	md = rs.getMetaData();	
			
			this.rs = rs;
			this.keys = new String[md.getColumnCount()];
			
			for(int index = 0; index < keys.length; index++) {
				keys[index] = md.getColumnName(index + 1);
			}
		}
	}

	@Override
	protected String[] getKeys() {
		return keys;
	}

	@Override
	protected Object[] getValues() {
		final Object[]	result = new Object[keys.length];
		
		try{for(int index = 0; index < result.length; index++) {
				result[index] = rs.getObject(index + 1);
			}
			return result;
		} catch (SQLException e) {
			throw new IllegalStateException(e.getLocalizedMessage());
		}
	}

	@Override
	protected Object setValue(final int index, final Object value) {
		try{if (rs.getConcurrency() == ResultSet.CONCUR_UPDATABLE) {
				final Object	oldValue = rs.getObject(index + 1); 
				
				rs.updateObject(index + 1, value);
				return oldValue;
			}
			else {
				throw new SQLException("Attempt to set value on non-updatable result set");
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e.getLocalizedMessage());
		}
	}
}
