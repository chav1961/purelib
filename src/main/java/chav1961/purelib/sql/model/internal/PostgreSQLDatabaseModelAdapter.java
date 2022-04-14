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
import chav1961.purelib.sql.model.interfaces.DatabaseModelAdapter.StandardExceptions;

public class PostgreSQLDatabaseModelAdapter implements DatabaseModelAdapter {
	public static final String	POSTGRES_SUBSCHEMA = "postgres";
	private static final URI	POSTGRES_URI = URI.create(MODEL_ADAPTER_SCHEMA+':'+POSTGRES_SUBSCHEMA+":/");
	
	public PostgreSQLDatabaseModelAdapter() {
	}
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("URI resource can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, POSTGRES_URI);
		}
	}

	@Override
	public DatabaseModelAdapter newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		return this;
	}

	@Override
	public String createSchema(ContentNodeMetadata meta) throws SyntaxException {
		return "create schema "+DefaultDatabaseModelAdapter.escape(getStartQuote(), getEndQuote(),meta.getName());
	}

	@Override
	public String dropSchema(ContentNodeMetadata meta) throws SyntaxException {
		return "drop schema "+DefaultDatabaseModelAdapter.escape(getStartQuote(), getEndQuote(), meta.getName())+" cascade";
	}

	@Override
	public String getSchemaName(ContentNodeMetadata meta) throws SyntaxException {
		return null;
	}

	@Override
	public String describeColumn(ContentNodeMetadata meta) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnName(ContentNodeMetadata meta) throws SyntaxException {
		return DefaultDatabaseModelAdapter.escape(getStartQuote(), getEndQuote(), meta.getName());
	}

	@Override
	public String createTable(ContentNodeMetadata meta) throws SyntaxException {
		final DbTypeDescriptor[]	dtd =new DbTypeDescriptor[0];// DbTypeDescriptor.load(conn);
		final StringBuilder			sb = new StringBuilder();
		final List<String>			primaryKeys = new ArrayList<>();
		char						prefix = '(';
		
		sb.append("create table ").append(getTableName(meta));
		
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
	public String dropTable(final ContentNodeMetadata meta) throws SyntaxException {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append("drop table ").append(getTableName(meta)).append(" cascade");
		return sb.toString();
	}

	@Override
	public String getTableName(ContentNodeMetadata meta) throws SyntaxException {
		return DefaultDatabaseModelAdapter.replaceSchemaAndEscape(getStartQuote(), getEndQuote(), meta.getName(), meta.getParent().getName());
	}

	@Override
	public String createSequence(ContentNodeMetadata meta) throws SyntaxException {
		final Hashtable<String, String[]> 	query = URIUtils.parseQuery(URIUtils.extractQueryFromURI(meta.getApplicationPath()));
		final StringBuilder	sb = new StringBuilder();
		long				from, to;	
		
		sb.append("create sequence ").append(DefaultDatabaseModelAdapter.replaceSchemaAndEscape(getStartQuote(), getEndQuote(), meta.getName(), meta.getParent().getName()));
		if (query.containsKey("step")) {
			sb.append(" increment by ").append(query.get("step")[0]);
		}
		if (query.containsKey("type")) {
			from = 1;
			to = Long.MAX_VALUE;
		}
		else {
			from = 1;
			to = Long.MAX_VALUE;
		}
		sb.append(" minvalue ").append(from).append(" maxvalue ").append(to);
		
		return sb.toString();
	}

	@Override
	public String dropSequence(ContentNodeMetadata meta) throws SyntaxException {
		return "drop sequence "+DefaultDatabaseModelAdapter.replaceSchemaAndEscape(getStartQuote(), getEndQuote(), meta.getName(), meta.getParent().getName());
	}

	@Override
	public String getSequenceName(ContentNodeMetadata meta) throws SyntaxException {
		return meta.getName();
	}

	@Override
	public boolean isSchemaSupported() {
		return true;
	}

	@Override
	public boolean isSequenceSupported() {
		return true;
	}

	@Override
	public StandardExceptions getExceptionType(final SQLException exc) {
		return StandardExceptions.UNKNOWN;
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
