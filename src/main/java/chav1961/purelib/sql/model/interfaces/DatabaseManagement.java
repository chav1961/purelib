package chav1961.purelib.sql.model.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface DatabaseManagement<Version extends Comparable<Version>> {
	Version getVersion(ContentNodeMetadata model) throws SQLException;
	void onCreate(Connection conn, ContentNodeMetadata model) throws SQLException;
	void onUpgrade(Connection conn, Version version, ContentNodeMetadata model, Version oldVersion, ContentNodeMetadata oldModel) throws SQLException;
	void onDowngrade(Connection conn, Version version, ContentNodeMetadata model, Version oldVersion, ContentNodeMetadata oldModel) throws SQLException;
	void onOpen(Connection conn, ContentNodeMetadata model) throws SQLException;
	void onClose(Connection conn, ContentNodeMetadata model) throws SQLException;
}
