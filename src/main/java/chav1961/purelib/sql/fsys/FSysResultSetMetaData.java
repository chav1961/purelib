package chav1961.purelib.sql.fsys;

import java.sql.SQLException;

import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.InternalUtils;

final class FSysResultSetMetaData extends AbstractResultSetMetaData {
	protected final String	catalog, schema, table;

	FSysResultSetMetaData(final String catalog, final String schema, final String table, final boolean readOnly, final String... columns) {
		super(InternalUtils.prepareMetadata(columns),readOnly);
		this.catalog = catalog;
		this.schema = schema;
		this.table = table;
	}

	@Override
	public String getSchemaName(int column) throws SQLException {
		checkColumnNumber(column);
		return schema;
	}

	@Override
	public String getTableName(int column) throws SQLException {
		checkColumnNumber(column);
		return table;
	}

	@Override
	public String getCatalogName(int column) throws SQLException {
		checkColumnNumber(column);
		return catalog;
	}
}
