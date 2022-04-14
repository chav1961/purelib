package chav1961.purelib.sql.model.internal;

import java.net.URI;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.model.interfaces.DatabaseModelAdapter;

public class DefaultDatabaseModelAdapter implements DatabaseModelAdapter{
	public DefaultDatabaseModelAdapter() {
	}
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else {
			return DatabaseModelAdapter.MODEL_ADAPTER_SCHEMA.equalsIgnoreCase(resource.getScheme());
		}
	}

	@Override
	public DatabaseModelAdapter newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return this;
	}

	@Override
	public String describeColumn(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnName(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createTable(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String dropTable(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableName(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createSequence(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String dropSequence(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSequenceName(ContentNodeMetadata meta) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSequenceSupported() throws ContentException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDataTypeSupported(int dataType) throws ContentException {
		// TODO Auto-generated method stub
		return false;
	}

}
