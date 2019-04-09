package chav1961.purelib.sql.content;

import java.sql.SQLException;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.RsMetaDataElement;

class FakeResultSetMetaData extends AbstractResultSetMetaData {
	private static final String	DEFAULT_TABLE = "table";
	private static final String	DEFAULT_SCHEMA = "schema";
	
	private final String	table, schema, catalog;
	
	public FakeResultSetMetaData(final RsMetaDataElement[] columns, final boolean readOnly) {
		this(columns, new SubstitutableProperties(), readOnly);
	}

	public FakeResultSetMetaData(final RsMetaDataElement[] columns, final SubstitutableProperties props, final boolean readOnly) {
		super(columns, readOnly);
		this.table = props.getProperty(SQLContentUtils.OPTION_TABLE_NAME,DEFAULT_TABLE);
		this.schema = props.getProperty(SQLContentUtils.OPTION_SCHEMA_NAME,DEFAULT_SCHEMA);
		this.catalog = props.getProperty(SQLContentUtils.OPTION_CATALOG_NAME);
	}
	
	@Override public String getSchemaName(int column) throws SQLException {return schema;}
	@Override public String getTableName(int column) throws SQLException {return table;}
	@Override public String getCatalogName(int column) throws SQLException {return catalog;}

	@Override
	public String toString() {
		return "FakeResultSetMetaData [toString()=" + super.toString() + "]";
	}
}
