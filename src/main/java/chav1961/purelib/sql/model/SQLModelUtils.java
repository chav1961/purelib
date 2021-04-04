package chav1961.purelib.sql.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.model.interfaces.ORMModelMapper;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class SQLModelUtils {
	public static <Key,Data> ORMModelMapper<Key, Data> buildORMModelMapper(final ContentNodeMetadata root, final ContentNodeMetadata dbRoot, final char[] nameTerminals, final String classPath, final SimpleURLClassLoader loader) throws ContentException, NullPointerException, IllegalArgumentException {
		if (root == null) {
			throw new NullPointerException("Class content node metadata can't be null");
		}
		else if (dbRoot == null) {
			throw new NullPointerException("Database content node metadata can't be null");
		}
		else if (nameTerminals == null || nameTerminals.length != 2) {
			throw new IllegalArgumentException("Name terminals can't be empty array and must contain exactly two chars");
		}
		else if (classPath == null || classPath.isEmpty()) {
			throw new IllegalArgumentException("Class path can't be null or empty");
		}
		else if (loader == null) {
			throw new NullPointerException("Class path can't be null or empty");
		}
		else {
			final Set<String>			classNames = new HashSet<>(), dbNames = new HashSet<>(), primaryKeys = new HashSet<>(), bugs = new HashSet<>();
			
			for (ContentNodeMetadata item : root) {
				classNames.add(item.getName().toUpperCase());
			}
			for (ContentNodeMetadata item : dbRoot) {
				if (item.getName().endsWith("/primaryKey")) {
					primaryKeys.add(item.getName().substring(0,item.getName().lastIndexOf("/primaryKey")).toUpperCase());
				}
				else {
					dbNames.add(item.getName().toUpperCase());
				}
			}
			classNames.retainAll(dbNames);
			
			for (String item : primaryKeys) {
				if  (!classNames.contains(item)) {
					bugs.add(item);
				}
			}
			if (!bugs.isEmpty()) {
				throw new ContentException("Database ["+dbRoot.getName()+"] model contains primary keys "+bugs+", that have no corresponding fields in the class ["+root.getName()+"] model");
			}
			
			final String	insert = buildInsertStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			final String	read = buildReadStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			final String	update = buildUpdateStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			final String	delete = buildDeleteStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			return null;
		}
	}

	static String buildInsertStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		final StringBuilder	sb = new StringBuilder("insert into ").append(nameTerminals[0]).append(root.getName()).append(nameTerminals[0]);
		char				prefix = '('; 
		
		for (String item : fields) {
			sb.append(prefix).append(nameTerminals[0]).append(item).append(nameTerminals[0]);
			prefix = ',';
		}
		
		sb.append(") values ");
		prefix = '('; 
		for (int index = 0, maxIndex = fields.size(); index < maxIndex; index++) {
			sb.append(prefix).append('?');
			prefix = ',';
		}
		return sb.append(')').toString();
	}
	
	static String buildReadStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	static String buildUpdateStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	static String buildDeleteStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	static class ORMModelMapperImpl<Key, Data> implements ORMModelMapper<Key, Data> {
		private final LightWeightRWLockerWrapper	wrapper = new LightWeightRWLockerWrapper();
		private final String						insertStmt, readStmt, updateStmt, deleteStmt;
		
		ORMModelMapperImpl(final Connection conn, final String insertStmt, final String readStmt, final String updateStmt, final String deleteStmt) {
			this.insertStmt = insertStmt;
			this.readStmt = readStmt;
			this.updateStmt = updateStmt;
			this.deleteStmt = deleteStmt;
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void create(final Data content) throws SQLException {
			// TODO Auto-generated method stub
			if (content == null) {
				throw new NullPointerException("Content to insert can't be null");
			}
			else {
				PreparedStatement	ps = getStatement(null, insertStmt);
				
				if (ps == null) {
					ps = createStatement(null, insertStmt);
				}
			}
		}

		@Override
		public void read(Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void read(Key key, Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void update(Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public long forEach(Data content, ForEachCallback<Data> callback, String where, String order, int offset, int limit) throws ContentException, SQLException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		protected PreparedStatement getStatement(final Connection conn, final String statement) {
			return null;
		}

		protected PreparedStatement createStatement(final Connection conn, final String statement) {
			return null;
		}
	}
}
