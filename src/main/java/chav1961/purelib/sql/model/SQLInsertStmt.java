package chav1961.purelib.sql.model;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.SQLErrorType;
import chav1961.purelib.sql.model.SQLModelUtils.ConnectionGetter;

class SQLInsertStmt<T> implements AutoCloseable, SQLPreparable {
	SQLInsertStmt(final ContentNodeMetadata databaseMeta, final ContentNodeMetadata classMeta, final SimpleURLClassLoader loader) throws ContentException {
		final Set<String>	dbNames = new HashSet<>(), classNames = new HashSet<>();
		
		for (ContentNodeMetadata item : databaseMeta) {
			if (!item.getName().endsWith("/primaryKey")) {
				dbNames.add(item.getName().toUpperCase());
			}
		}
		for (ContentNodeMetadata item : classMeta) {
			classNames.add(item.getName().toUpperCase());
		}
		dbNames.retainAll(classNames);
		for (ContentNodeMetadata item : databaseMeta) {
			if (!item.getName().endsWith("/primaryKey")) {
				if (item.getFormatAssociated() != null && item.getFormatAssociated().isMandatory() && !dbNames.contains(item.getName().toUpperCase())) {
					throw new ContentException("Field name ["+item.getName()+"] is mandatory in the database table ["+databaseMeta.getName()+"], but doesn't have appropriative field in the class ["+classMeta.getType().getCanonicalName()+"]");
				}
			}
		}
	}
	
	@Override
	public void close() throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void prepare(final ConnectionGetter getter) throws SQLException {
		
	}
	
	@Override
	public boolean isPrepared() {
		return false;
	}
	
	@Override
	public void unprepare() throws SQLException {
		
	}

	SQLErrorType insert(T content) throws SQLException {
		return null;
	}
}
