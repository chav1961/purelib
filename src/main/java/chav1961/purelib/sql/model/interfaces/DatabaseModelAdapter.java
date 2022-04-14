package chav1961.purelib.sql.model.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface DatabaseModelAdapter extends SpiService<DatabaseModelAdapter> {
	String MODEL_ADAPTER_SCHEMA = "jdbc";
	
	String describeColumn(ContentNodeMetadata meta) throws ContentException;
	String getColumnName(ContentNodeMetadata meta) throws ContentException;
	String createTable(ContentNodeMetadata meta) throws ContentException;
	String dropTable(ContentNodeMetadata meta) throws ContentException;
	String getTableName(ContentNodeMetadata meta) throws ContentException;
	String createSequence(ContentNodeMetadata meta) throws ContentException;
	String dropSequence(ContentNodeMetadata meta) throws ContentException;
	String getSequenceName(ContentNodeMetadata meta) throws ContentException;
	boolean isSequenceSupported() throws ContentException;
	boolean isDataTypeSupported(int dataType) throws ContentException;
}
