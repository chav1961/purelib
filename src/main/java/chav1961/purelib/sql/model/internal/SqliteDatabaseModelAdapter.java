package chav1961.purelib.sql.model.internal;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.sql.SQLUtils.DbTypeDescriptor;
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
	public String createSchema(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return null;
	}

	@Override
	public String dropSchema(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return null;
	}

	@Override
	public String getSchemaName(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return null;
	}

	@Override
	public String describeColumn(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnName(final ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return DefaultDatabaseModelAdapter.escape(getStartQuote(), getEndQuote(), meta.getName());
	}

	@Override
	public String createTable(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		final DbTypeDescriptor[]	dtd =new DbTypeDescriptor[0];// DbTypeDescriptor.load(conn);
		final StringBuilder			sb = new StringBuilder();
		final List<String>			primaryKeys = new ArrayList<>();
		char						prefix = '(';
		
		sb.append("create table ").append(getTableName(meta, schema));
		
		for (ContentNodeMetadata item : meta) {
			final Hashtable<String, String[]> 	query = URIUtils.parseQuery(URIUtils.extractQueryFromURI(item.getApplicationPath()));
			
			sb.append(prefix).append(DefaultDatabaseModelAdapter.escape(getStartQuote(), getEndQuote(), item.getName()))
				.append(' ').append(query.containsKey("type") ? SQLUtils.specificTypeNameByCommonTypeName(query.get("type")[0],dtd) : "varchar(100)");
			if (query.containsKey("pkSeq")) {
				primaryKeys.add(item.getName());
			}
			else if (item.getFormatAssociated().isMandatory()) {
				sb.append(" not null ");
			}
			prefix = ',';
		}
		
		if (!primaryKeys.isEmpty()) {
			prefix = '(';
			sb.append(", primary key ");
			for (String item : primaryKeys) {
				sb.append(prefix).append(DefaultDatabaseModelAdapter.escape(getStartQuote(), getEndQuote(), item));
				prefix = ',';
			}
			sb.append(')');
		}
		
		sb.append(')');
		return sb.toString();
	}

	@Override
	public String dropTable(final ContentNodeMetadata meta, final String schema) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append("drop table ").append(getTableName(meta, schema));
		return sb.toString();
	}

	@Override
	public String getTableName(final ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return DefaultDatabaseModelAdapter.escape(getStartQuote(), getEndQuote(), meta.getName());
	}

	@Override
	public String createSequence(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return null;
	}

	@Override
	public String dropSequence(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return null;
	}

	@Override
	public String getSequenceName(ContentNodeMetadata meta, final String schema) throws SyntaxException {
		return null;
	}

	@Override
	public boolean isSchemaSupported() {
		return false;
	}

	@Override
	public boolean isSequenceSupported() {
		return false;
	}

	@Override
	public StandardExceptions getExceptionType(final SQLException exc) {
		if (exc.getMessage().contains("(no such table:")) {
			return StandardExceptions.NO_OBJECT_FOUND;
		}
		else {
			return StandardExceptions.UNKNOWN;
		}
	}
	
	
	@Override
	public boolean isDataTypeSupported(int dataType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDataTypeSupported(int dataType, int length, int precision) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNearestDataType(int dataType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNearestDataType(int dataType, int length, int precision) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getDataTypeName(int dataType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDataTypeName(int dataType, int length, int precision) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public char getStartQuote() {
		return '\"';
	}

	@Override
	public char getEndQuote() {
		return '\"';
	}
}
