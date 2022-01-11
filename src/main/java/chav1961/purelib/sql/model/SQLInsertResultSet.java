package chav1961.purelib.sql.model;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.DomainType;
import chav1961.purelib.sql.interfaces.SQLErrorType;

class SQLInsertResultSet<T> implements AutoCloseable {
	private final FieldAccessor[]	fields; 
	
	SQLInsertResultSet(final ContentNodeMetadata databaseMeta, final ContentNodeMetadata classMeta, final SimpleURLClassLoader loader) throws ContentException {
		this.fields = InternalUtils.buildFieldAccessorList(databaseMeta, classMeta, loader);
	}
	
	@Override
	public void close() throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	SQLErrorType insert(final ResultSet rs, T content) throws SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content to insert can't be null can't be null"); 
		}
		else if (rs.getFetchDirection() == ResultSet.FETCH_FORWARD) {
			throw new IllegalArgumentException("Result set cursor is fetch-forward and doesn't support insertion"); 
		}
		else if (rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY) {
			throw new IllegalArgumentException("Result set cursor is read-only and doesn't support insertion"); 
		}
		else {
			try{rs.moveToInsertRow();
				for (FieldAccessor item : fields) {
					final Object	value = ((ObjectGetterAndSetter<?>)item.gas).get(content);
					
					item.domain.bindResultSet(rs, item.fieldName, item.isMandatory && value == null ? item.domain.getEmptyValue() : value);
				}
				rs.insertRow();
				return SQLErrorType.SUCCESS;
			} catch (ContentException e) {
				return SQLErrorType.OTHER;
			} catch (SQLException e) {
				final SQLErrorType	err = SQLErrorType.valueOf(rs.getStatement().getConnection(), e);
				
				if (err == SQLErrorType.OTHER) {
					throw e;
				}
				else {
					return err;
				}
			} finally {
				rs.moveToCurrentRow();
			}
		}
	}
}
