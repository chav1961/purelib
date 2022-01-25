package chav1961.purelib.sql.interfaces;

import java.sql.SQLException;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface DatabaseManagement<Version extends Comparable<Version>> {
	Version databaseVersion() throws SQLException;
	Version modelVersion(ContentNodeMetadata model) throws SQLException;
	void onCreate(ContentNodeMetadata model) throws SQLException;
	void onUpgrade(Version version, ContentNodeMetadata model, Version oldVersion, ContentNodeMetadata oldModel) throws SQLException;
	void onDowngrade(Version version, ContentNodeMetadata model, Version oldVersion, ContentNodeMetadata oldModel) throws SQLException;
	void onOpen(ContentNodeMetadata model) throws SQLException;
	void onClose(ContentNodeMetadata model) throws SQLException;
}
