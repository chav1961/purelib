package chav1961.purelib.sql.model.internal;

import java.net.URI;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.model.interfaces.DatabaseModelAdapter;

public class SqliteDatabaseModelAdapter implements DatabaseModelAdapter {
	public static final String	SQLITE_SUBSCHEMA = "sqlite";
	private static final URI	SQLITE_URI = URI.create(MODEL_ADAPTER_SCHEMA+':'+SQLITE_SUBSCHEMA+":/");
	
	public SqliteDatabaseModelAdapter() {
	}
	

	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("URI resource can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, SQLITE_URI);
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
