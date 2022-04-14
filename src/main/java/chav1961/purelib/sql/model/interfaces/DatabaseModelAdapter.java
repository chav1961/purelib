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

	String createSchema(ContentNodeMetadata meta) throws SyntaxException;
	String dropSchema(ContentNodeMetadata meta) throws SyntaxException;
	String getSchemaName(ContentNodeMetadata meta) throws SyntaxException;
	String describeColumn(ContentNodeMetadata meta) throws SyntaxException;
	String getColumnName(ContentNodeMetadata meta) throws SyntaxException;
	String createTable(ContentNodeMetadata meta) throws SyntaxException;
	String dropTable(ContentNodeMetadata meta) throws SyntaxException;
	String getTableName(ContentNodeMetadata meta) throws SyntaxException;
	String createSequence(ContentNodeMetadata meta) throws SyntaxException;
	String dropSequence(ContentNodeMetadata meta) throws SyntaxException;
	String getSequenceName(ContentNodeMetadata meta) throws SyntaxException;
	
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
