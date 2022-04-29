package chav1961.purelib.sql.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.model.SQLModelUtils.ConnectionGetter;
import chav1961.purelib.sql.model.interfaces.DatabaseModelManagement;

public class SimpleDatabaseModelKeeper<Version extends Comparable<Version>> implements DatabaseModelManagement<Version>, AutoCloseable {
//	private static final Comparator<Comparable>		DESC_COMPARATOR = new Comparator<>() {
//														@Override
//														public int compare(Comparable o1, Comparable o2) {
//															return -o1.compareTo(o2);
//														}
//													};
	
	private final ConnectionGetter					conn;
	private final String							schema;
	private final ContentNodeMetadata				meta = null;
	private final DatabaseModelContent<Version>[]	content;
	
	public SimpleDatabaseModelKeeper(final ConnectionGetter conn, final String schema) throws NullPointerException, IllegalArgumentException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema can't be null or empty");
		}
		else {
			this.conn = conn;
			this.schema = schema;

			final List<DatabaseModelContent<Version>>	versions = new ArrayList<>();
			
			this.content = versions.toArray(new DatabaseModelContent[versions.size()]);
		}
	}
	
	
	@Override
	public void close() throws SQLException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public int size() {
		return content.length;
	}

	@Override
	public Version getVersion(final int versionNumber) {
		if (versionNumber < 0 || versionNumber >= content.length) {
			throw new ArrayIndexOutOfBoundsException("Version number ["+versionNumber+"] must be in range 0.."+(content.length-1));
		}
		else {
			return SQLModelUtils.extractVersionFromModel(getModel(versionNumber), null);
		}
	}

	@Override
	public ContentNodeMetadata getModel(final int versionNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<DatabaseModelContent<Version>> allAscending() {
		return Arrays.asList(content);
	}

	@Override
	public Iterable<DatabaseModelContent<Version>> allDescending() {
		final DatabaseModelContent<Version>[]	copy = content.clone();
		
//		Arrays.sort(copy, DESC_COMPARATOR);
		return Arrays.asList(copy);
	}

}
