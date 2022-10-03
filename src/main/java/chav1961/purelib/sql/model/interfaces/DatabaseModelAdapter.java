package chav1961.purelib.sql.model.interfaces;

import java.sql.SQLException;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface DatabaseModelAdapter extends SpiService<DatabaseModelAdapter> {
	String MODEL_ADAPTER_SCHEMA = "jdbc";
	
	public static enum StandardExceptions {
		UNKNOWN,
		NO_OBJECT_FOUND;
	}

	String createSchemaOwner(ContentNodeMetadata meta, String schema, String user, char[] password) throws SyntaxException;
	String dropSchemaOwner(ContentNodeMetadata meta, String schema, String user) throws SyntaxException;
	
	String createSchema(ContentNodeMetadata meta, String schema, String schemaOwner) throws SyntaxException;
	default String createSchema(ContentNodeMetadata meta, String schema) throws SyntaxException {
		return createSchema(meta, schema, schema);
	}
	String dropSchema(ContentNodeMetadata meta, String schema) throws SyntaxException;
	String getSchemaName(ContentNodeMetadata meta, String schema) throws SyntaxException;
	
	String describeColumn(ContentNodeMetadata meta, String schema) throws SyntaxException;
	String getColumnName(ContentNodeMetadata meta, String schema) throws SyntaxException;
	
	String createTable(ContentNodeMetadata meta, String schema) throws SyntaxException;
	String dropTable(ContentNodeMetadata meta, String schema) throws SyntaxException;
	String getTableName(ContentNodeMetadata meta, String schema) throws SyntaxException;
	
	String createReference(ContentNodeMetadata metaFrom, ContentNodeMetadata metaTo, String schema) throws SyntaxException;
	String dropReference(ContentNodeMetadata metaFrom, ContentNodeMetadata metaTo, String schema) throws SyntaxException;
	
	String createSequence(ContentNodeMetadata meta, String schema) throws SyntaxException;
	String dropSequence(ContentNodeMetadata meta, String schema) throws SyntaxException;
	String getSequenceName(ContentNodeMetadata meta, String schema) throws SyntaxException;
	
	boolean isSchemaSupported();
	boolean isSequenceSupported();
	
	StandardExceptions getExceptionType(SQLException exc); 
	
	boolean isDataTypeSupported(int dataType, int length, int precision);
	default boolean isDataTypeSupported(int dataType) {
		return isDataTypeSupported(dataType, 0, 0);
	}
	
	int getNearestDataType(int dataType, int length, int precision);
	default int getNearestDataType(int dataType) {
		return getNearestDataType(dataType, 0, 0);
	}
	
	String getDataTypeName(int dataType, int length, int precision);
	default String getDataTypeName(int dataType) {
		return getDataTypeName(dataType, 0, 0);
	}
	
	char getStartQuote();
	char getEndQuote();
}
