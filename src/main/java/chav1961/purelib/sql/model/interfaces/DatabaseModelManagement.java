package chav1961.purelib.sql.model.interfaces;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface DatabaseModelManagement<Version extends Comparable<Version>> {
	public interface DatabaseModelContent<Version extends Comparable<Version>> {
		Version getVersion();
		ContentNodeMetadata getModel();
	}
	
	int size();
	Version getVersion(int versionNumber);
	ContentNodeMetadata getModel(int versionNumber);
	
	default Version getTheLastVersion() {
		return getVersion(size() - 1);
	}
	
	default ContentNodeMetadata getTheLastModel() {
		return getModel(size() - 1);
	}
	
	Iterable<DatabaseModelContent<Version>> allAscending();
	Iterable<DatabaseModelContent<Version>> allDescending();
}
