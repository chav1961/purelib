/*package chav1961.purelib.sql.fsys;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.sql.ArrayContent;
import chav1961.purelib.sql.InMemoryReadOnlyResultSet;
import chav1961.purelib.sql.InternalUtils;
import chav1961.purelib.sql.NullReadOnlyResultSet;

class FSysMetaData implements DatabaseMetaData {
	private static final Pattern		NEVER_MATCH = Pattern.compile("Z{999}");
	private static final String[]		PRIVILEGES = new String[]{"SELECT","INSERT","UPDATE","DELETE"};
	
	private final FSysConnection 		parent;
	private final FileSystemInterface 	fsi;
	
	FSysMetaData(final FSysConnection parent, final FileSystemInterface fsi) {
		this.parent = parent;
		this.fsi = fsi;
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Interface to wrap can't be null");
		}
		else if (!isWrapperFor(iface)) {
			throw new SQLException("This instance can't be wrapped to awaited interface ["+iface+"]");
		}
		else {
			return iface.cast(this);
		}
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Interface to wrap can't be null");
		}
		else {
			return iface.isAssignableFrom(this.getClass());
		}
	}

	@Override
	public boolean allProceduresAreCallable() throws SQLException {
		return false;
	}

	@Override
	public boolean allTablesAreSelectable() throws SQLException {
		return true;
	}

	@Override
	public String getURL() throws SQLException {
		return null;
	}

	@Override
	public String getUserName() throws SQLException {
		return null;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return parent.isReadOnly();
	}

	@Override
	public boolean nullsAreSortedHigh() throws SQLException {
		return false;
	}

	@Override
	public boolean nullsAreSortedLow() throws SQLException {
		return false;
	}

	@Override
	public boolean nullsAreSortedAtStart() throws SQLException {
		return false;
	}

	@Override
	public boolean nullsAreSortedAtEnd() throws SQLException {
		return false;
	}

	@Override
	public String getDatabaseProductName() throws SQLException {
		return null;
	}

	@Override
	public String getDatabaseProductVersion() throws SQLException {
		return getDatabaseMajorVersion()+"."+getDatabaseMinorVersion();
	}

	@Override
	public String getDriverName() throws SQLException {
		return FSysDriver.class.getName();
	}

	@Override
	public String getDriverVersion() throws SQLException {
		return getDriverMajorVersion()+"."+getDriverMinorVersion();
	}

	@Override
	public int getDriverMajorVersion() {
		return FSysDriver.DRIVER_MAJOR;
	}

	@Override
	public int getDriverMinorVersion() {
		return FSysDriver.DRIVER_MINOR;
	}

	@Override
	public boolean usesLocalFiles() throws SQLException {
		return true;
	}

	@Override
	public boolean usesLocalFilePerTable() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		return true;
	}

	@Override
	public boolean storesUpperCaseIdentifiers() throws SQLException {
		return false;
	}

	@Override
	public boolean storesLowerCaseIdentifiers() throws SQLException {
		return false;
	}

	@Override
	public boolean storesMixedCaseIdentifiers() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		return true;
	}

	@Override
	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		return false;
	}

	@Override
	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		return false;
	}

	@Override
	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		return true;
	}

	@Override
	public String getIdentifierQuoteString() throws SQLException {
		return "\"";
	}

	@Override
	public String getSQLKeywords() throws SQLException {
		return "";
	}

	@Override
	public String getNumericFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getStringFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getSystemFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getTimeDateFunctions() throws SQLException {
		return "";
	}

	@Override
	public String getSearchStringEscape() throws SQLException {
		return "\\";
	}

	@Override
	public String getExtraNameCharacters() throws SQLException {
		return "";
	}

	@Override
	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsColumnAliasing() throws SQLException {
		return true;
	}

	@Override
	public boolean nullPlusNonNullIsNull() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsConvert() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsConvert(final int fromType, final int toType) throws SQLException {
		return InternalUtils.canConvert(fromType, toType);
	}

	@Override
	public boolean supportsTableCorrelationNames() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsExpressionsInOrderBy() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsOrderByUnrelated() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsGroupBy() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsGroupByUnrelated() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsGroupByBeyondSelect() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsLikeEscapeClause() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsMultipleResultSets() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsMultipleTransactions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsNonNullableColumns() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsMinimumSQLGrammar() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCoreSQLGrammar() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsExtendedSQLGrammar() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsANSI92FullSQL() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsOuterJoins() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsFullOuterJoins() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsLimitedOuterJoins() throws SQLException {
		return true;
	}

	@Override
	public String getSchemaTerm() throws SQLException {
		return "subfolder";
	}

	@Override
	public String getProcedureTerm() throws SQLException {
		return "procedure";
	}

	@Override
	public String getCatalogTerm() throws SQLException {
		return "folder";
	}

	@Override
	public boolean isCatalogAtStart() throws SQLException {
		try{return "/".equals(fsi.getPath());
		} catch (IOException e) {
			throw new SQLException("I/O error checking catalog: "+e.getLocalizedMessage(),e);
		}
	}

	@Override
	public String getCatalogSeparator() throws SQLException {
		return ".";
	}

	@Override
	public boolean supportsSchemasInDataManipulation() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsPositionedDelete() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsPositionedUpdate() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSelectForUpdate() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsStoredProcedures() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsSubqueriesInComparisons() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsSubqueriesInExists() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsSubqueriesInIns() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCorrelatedSubqueries() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsUnion() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsUnionAll() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		return false;
	}

	@Override
	public int getMaxBinaryLiteralLength() throws SQLException {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxCharLiteralLength() throws SQLException {
		return 65535;
	}

	@Override
	public int getMaxColumnNameLength() throws SQLException {
		return 100;
	}

	@Override
	public int getMaxColumnsInGroupBy() throws SQLException {
		return 8;
	}

	@Override
	public int getMaxColumnsInIndex() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxColumnsInOrderBy() throws SQLException {
		return 8;
	}

	@Override
	public int getMaxColumnsInSelect() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxColumnsInTable() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxConnections() throws SQLException {
		return 16;
	}

	@Override
	public int getMaxCursorNameLength() throws SQLException {
		return 32;
	}

	@Override
	public int getMaxIndexLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxSchemaNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxProcedureNameLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxCatalogNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxRowSize() throws SQLException {
		return 65535;
	}

	@Override
	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		return false;
	}

	@Override
	public int getMaxStatementLength() throws SQLException {
		return 65535;
	}

	@Override
	public int getMaxStatements() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxTableNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getMaxTablesInSelect() throws SQLException {
		return 16;
	}

	@Override
	public int getMaxUserNameLength() throws SQLException {
		return 256;
	}

	@Override
	public int getDefaultTransactionIsolation() throws SQLException {
		return Connection.TRANSACTION_NONE;
	}

	@Override
	public boolean supportsTransactions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
		return false;
	}

	@Override
	public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
		return false;
	}

	@Override
	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		return false;
	}

	@Override
	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		return false;
	}

	@Override
	public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"PROCEDURE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"PROCEDURE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"PROCEDURE_NAME:VARCHAR("+getMaxProcedureNameLength()+")",
											"REMARKS:VARCHAR(100)",
											"PROCEDURE_TYPE:SMALLINT",
											"SPECIFIC_NAME:VARCHAR("+getMaxProcedureNameLength()+")"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"PROCEDURE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"PROCEDURE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"PROCEDURE_NAME:VARCHAR("+getMaxProcedureNameLength()+")",
											"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"COLUMN_TYPE:SMALLINT",
											"DATA_TYPE:INTEGER",
											"TYPE_NAME:VARCHAR(100)",
											"PRECISION:INTEGER",
											"LENGTH:INTEGER",
											"SCALE:SMALLINT",
											"RADIX:SMALLINT",
											"NULLABLE:SMALLINT",
											"COLUMN_DEF:VARCHAR(1024)",
											"SQL_DATA_TYPE:INTEGER",
											"SQL_DATETIME_SUB:INTEGER",
											"CHAR_OCTET_LENGTH:INTEGER",
											"ORDINAL_POSITION:INTEGER",
											"IS_NULLABLE:VARCHAR(10)",
											"SPECIFIC_NAME:VARCHAR("+getMaxColumnNameLength()+")"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
		final String[][]	found = scanContent(InternalUtils.buildPattern4LikeStyledTemplate(catalog == null || catalog.isEmpty() ? "%" : catalog),
												InternalUtils.buildPattern4LikeStyledTemplate(schemaPattern == null || schemaPattern.isEmpty() ? "%" : schemaPattern),
												InternalUtils.buildPattern4LikeStyledTemplate(tableNamePattern == null || tableNamePattern.isEmpty() ? "%" : tableNamePattern)
												); 
		final Object[][]	content = new Object[found.length][];
		
		for (int index = 0; index < content.length; index++) {
			content[index] = new String[]{found[index][0],found[index][1],found[index][2],"TABLE","",null,null,null,null,null};
		}
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
											"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"TABLE_TYPE:VARCHAR(100)",
											"REMARKS:VARCHAR(100)",
											"TYPE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"TYPE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"TYPE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"SELF_REFERENCING_COL_NAME:VARCHAR(100)",
											"REF_GENERATION:VARCHAR(1024)"),
											ResultSet.FETCH_FORWARD,new ArrayContent(content)
										);
	}

	@Override
	public ResultSet getSchemas() throws SQLException {
		return getSchemas(null,"%");
	}

	@Override
	public ResultSet getCatalogs() throws SQLException {
		final String[][]	found = scanContent(InternalUtils.buildPattern4LikeStyledTemplate("%"),
												NEVER_MATCH,
												NEVER_MATCH
												); 
		final Object[][]	content = new Object[found.length][];
		
		for (int index = 0; index < content.length; index++) {
			content[index] = new String[]{found[index][0]};
		}
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
									"TABLE_CATALOG:VARCHAR("+getMaxCatalogNameLength()+")"),
									ResultSet.FETCH_FORWARD,new ArrayContent(content)
								);
	}

	@Override
	public ResultSet getTableTypes() throws SQLException {
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
									"TABLE_TYPE:VARCHAR(100)"),
									ResultSet.FETCH_FORWARD,new ArrayContent(new String[]{"TABLE","VIEW","SYSTEM TABLE","GLOBAL TEMPORARY","LOCAL TEMPORARY","ALIAS","SYNONYM"})
								);
	}

	@Override
	public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
		final String[][]		found = scanContent(InternalUtils.buildPattern4LikeStyledTemplate(catalog == null || catalog.isEmpty() ? "%" : catalog),
												InternalUtils.buildPattern4LikeStyledTemplate(schemaPattern == null || schemaPattern.isEmpty() ? "%" : schemaPattern),
												InternalUtils.buildPattern4LikeStyledTemplate(tableNamePattern == null || tableNamePattern.isEmpty() ? "%" : tableNamePattern)
												);
		final Pattern			colPattern = InternalUtils.buildPattern4LikeStyledTemplate(columnNamePattern == null || columnNamePattern.isEmpty() ? "%" : columnNamePattern);
		final List<Object[]>	content = new ArrayList<>();
		
		for (int index = 0; index < found.length; index++) {
			final ResultSetMetaData	rsmd = extractMetaData(found[index][0],found[index][1],found[index][2]);
			
			for (int itemIndex = 1; itemIndex <= rsmd.getColumnCount(); itemIndex++) {
				if (colPattern.matcher(rsmd.getColumnName(itemIndex)).matches()) {
					content.add(new Object[]{found[index][0],found[index][1],found[index][2],
									rsmd.getColumnName(itemIndex),
									rsmd.getColumnType(itemIndex),
									rsmd.getColumnTypeName(itemIndex),
									rsmd.getColumnDisplaySize(itemIndex),
									0,
									rsmd.getPrecision(itemIndex),
									10,
									ResultSetMetaData.columnNullableUnknown,
									null,
									0,
									0,
									rsmd.getColumnDisplaySize(itemIndex),
									itemIndex,
									"YES",
									null,
									null,
									null,
									null,
									"",
									""
								}
							);
				}
			}
		}
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
												"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
												"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
												"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
												"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
												"DATA_TYPE:INTEGER",
												"TYPE_NAME:VARCHAR(100)",
												"COLUMN_SIZE:INTEGER",
												"BUFFER_LENGTH:INTEGER",
												"DECIMAL_DIGITS:INTEGER",
												"NUM_PREC_RADIX:INTEGER",
												"NULLABLE:INTEGER",
												"COLUMN_DEF:VARCHAR(100)",
												"SQL_DATA_TYPE:INTEGER",
												"SQL_DATETIME_SUB:INTEGER",
												"CHAR_OCTET_LENGTH:INTEGER",
												"ORDINAL_POSITION:INTEGER",
												"IS_NULLABLE:VARCHAR(10)",
												"SCOPE_CATALOG:VARCHAR("+getMaxCatalogNameLength()+")",
												"SCOPE_SCHEMA:VARCHAR("+getMaxSchemaNameLength()+")",
												"SCOPE_TABLE:VARCHAR("+getMaxTableNameLength()+")",
												"REMARKS:VARCHAR(100)",
												"SOURCE_DATA_TYPE:SMALLINT",
												"IS_AUTOINCREMENT:VARCHAR(10)",
												"IS_GENERATEDCOLUMN:VARCHAR(10)"),
												ResultSet.FETCH_FORWARD,new ArrayContent(content.toArray(new Object[content.size()][]))
											);
	}

	@Override
	public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
		final String[][]		found = scanContent(InternalUtils.buildPattern4LikeStyledTemplate(catalog == null || catalog.isEmpty() ? "%" : catalog),
										InternalUtils.buildPattern4LikeStyledTemplate(schema == null || schema.isEmpty() ? "%" : schema),
										InternalUtils.buildPattern4LikeStyledTemplate(table == null || table.isEmpty() ? "%" : table)
									);
		final Pattern			colPattern = InternalUtils.buildPattern4LikeStyledTemplate(columnNamePattern == null || columnNamePattern.isEmpty() ? "%" : columnNamePattern);
		final List<Object[]>	content = new ArrayList<>();
		
		for (int index = 0; index < found.length; index++) {
			final ResultSetMetaData	rsmd = extractMetaData(found[index][0],found[index][1],found[index][2]);
			
			for (int itemIndex = 1; itemIndex <= rsmd.getColumnCount(); itemIndex++) {
				if (colPattern.matcher(rsmd.getColumnName(itemIndex)).matches()) {
					for (int subIndex = 0; subIndex < PRIVILEGES.length; subIndex++) {
						content.add(new Object[]{found[index][0],found[index][1],found[index][2],rsmd.getColumnName(itemIndex),null,"PUBLIC",PRIVILEGES[subIndex],"YES"});
					}
				}
			}
		}
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
												"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
												"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
												"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
												"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
												"GRANTOR:VARCHAR("+getMaxUserNameLength()+")",
												"GRANTEE:VARCHAR("+getMaxUserNameLength()+")",
												"PRIVILEGE:VARCHAR(100)",
												"IS_GRANTABLE:VARCHAR(10)"),
												ResultSet.FETCH_FORWARD,new ArrayContent(content.toArray(new Object[content.size()][]))
											);
	}

	@Override
	public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
		final String[][]	found = scanContent(InternalUtils.buildPattern4LikeStyledTemplate(catalog == null || catalog.isEmpty() ? "%" : catalog),
											InternalUtils.buildPattern4LikeStyledTemplate(schemaPattern == null || schemaPattern.isEmpty() ? "%" : schemaPattern),
											InternalUtils.buildPattern4LikeStyledTemplate(tableNamePattern == null || tableNamePattern.isEmpty() ? "%" : tableNamePattern)
											);
		final Object[][]	content = new Object[found.length*PRIVILEGES.length][];

		for (int index = 0; index < found.length; index++) {
			for (int subIndex = 0; subIndex < PRIVILEGES.length; subIndex++) {
				content[4*index+subIndex] = new Object[]{found[index][0],found[index][1],found[index][2],null,"PUBLIC",PRIVILEGES[subIndex],"YES"}; 
			}
		}
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
											"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"GRANTOR:VARCHAR("+getMaxUserNameLength()+")",
											"GRANTEE:VARCHAR("+getMaxUserNameLength()+")",
											"PRIVILEGE:VARCHAR(100)",
											"IS_GRANTABLE:VARCHAR(10)"),
											ResultSet.FETCH_FORWARD,new ArrayContent(content)
										);
	}

	@Override
	public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"SCOPE:SMALLINT",
											"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"DATA_TYPE:INTEGER",
											"TYPE_NAME:VARCHAR(100)",
											"COLUMN_SIZE:INTEGER",
											"BUFFER_LENGTH:INTEGER",
											"DECIMAL_DIGITS:SMALLINT",
											"PSEUDO_COLUMN:SMALLINT"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"SCOPE:SMALLINT",
											"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"DATA_TYPE:INTEGER",
											"TYPE_NAME:VARCHAR(100)",
											"COLUMN_SIZE:INTEGER",
											"BUFFER_LENGTH:INTEGER",
											"DECIMAL_DIGITS:INTEGER",
											"PSEUDO_COLUMN:SMALLINT"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"KEY_SEQ:SMALLINT"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"PKTABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"PKTABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"PKTABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"PKCOLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"FKTABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"FKTABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"FKTABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"FKCOLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"KEY_SEQ:SMALLINT",
											"UPDATE_RULE:SMALLINT",
											"DELETE_RULE:SMALLINT",
											"FK_NAME:VARCHAR(100)",
											"PK_NAME:VARCHAR(100)",
											"DEFERRABILITY:SMALLINT"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"PKTABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"PKTABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"PKTABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"PKCOLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"FKTABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"FKTABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"FKTABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"FKCOLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"KEY_SEQ:SMALLINT",
											"UPDATE_RULE:SMALLINT",
											"DELETE_RULE:SMALLINT",
											"FK_NAME:VARCHAR(100)",
											"PK_NAME:VARCHAR(100)",
											"DEFERRABILITY:SMALLINT"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getCrossReference(final String parentCatalog, final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
											"PKTABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"PKTABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"PKTABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"PKCOLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"FKTABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"FKTABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"FKTABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"FKCOLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"KEY_SEQ:SMALLINT",
											"UPDATE_RULE:SMALLINT",
											"DELETE_RULE:SMALLINT",
											"FK_NAME:VARCHAR(100)",
											"PK_NAME:VARCHAR(100)",
											"DEFERRABILITY:SMALLINT"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public ResultSet getTypeInfo() throws SQLException {
		final Map<String, Class<?>> 	map = parent.getTypeMap();
		final Object[][]				content = new Object[map.size()][];
		int								index = 0;
		
		for (Entry<String, Class<?>> item : map.entrySet()) {
			content[index++] = new Object[]{item.getKey(),
											InternalUtils.typeIdByTypeName(item.getKey()),
											0,
											null,
											null,
											null,
											DatabaseMetaData.typeNullableUnknown,
											true,
											DatabaseMetaData.typeSearchable,
											false,
											false,
											false,
											null,
											0,
											0,
											0,
											0,
											10
									};
		}

		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
											"TYPE_NAME:VARCHAR(100)",
											"DATA_TYPE:INTEGER",
											"PRECISION:INTEGER",
											"LITERAL_PREFIX:CHAR(1)",
											"LITERAL_SUFFIX:CHAR(1)",
											"CREATE_PARAMS:VARCHAR(1024)",
											"NULLABLE:SMALLINT",
											"CASE_SENSITIVE:BOOLEAN",
											"SEARCHABLE:SMALLINT",
											"UNSIGNED_ATTRIBUTE:BOOLEAN",
											"FIXED_PREC_SCALE:BOOLEAN",
											"AUTO_INCREMENT:BOOLEAN",
											"LOCAL_TYPE_NAME:VARCHAR(100)",
											"MINIMUM_SCALE:SMALLINT",
											"MAXIMUM_SCALE:SMALLINT",
											"SQL_DATA_TYPE:INTEGER",
											"SQL_DATETIME_SUB:INTEGER",
											"NUM_PREC_RADIX:INTEGER"),
											ResultSet.FETCH_FORWARD,new ArrayContent(content)
										);
	}

	@Override
	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
											"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
											"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
											"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
											"NON_UNIQUE:BOOLEAN",
											"INDEX_QUALIFIER:VARCHAR(100)",
											"INDEX_NAME:VARCHAR(100)",
											"TYPE:SMALLINT",
											"ORDINAL_POSITION:SMALLINT",
											"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
											"ASC_OR_DESC:CHAR(1)",
											"CARDINALITY:INTEGER",
											"PAGES:INTEGER",
											"FILTER_CONDITION:VARCHAR(4000)"),
											ResultSet.TYPE_FORWARD_ONLY
										);
	}

	@Override
	public boolean supportsResultSetType(final int type) throws SQLException {
		return type == ResultSet.TYPE_FORWARD_ONLY || type == ResultSet.TYPE_SCROLL_INSENSITIVE || type == ResultSet.TYPE_SCROLL_SENSITIVE;
	}

	@Override
	public boolean supportsResultSetConcurrency(final int type, final int concurrency) throws SQLException {
		return (type == ResultSet.TYPE_FORWARD_ONLY || type == ResultSet.TYPE_SCROLL_INSENSITIVE || type == ResultSet.TYPE_SCROLL_SENSITIVE)
				&&
			   (concurrency == ResultSet.CONCUR_READ_ONLY || concurrency == ResultSet.CONCUR_UPDATABLE);
	}

	@Override
	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		return true;
	}

	@Override
	public boolean ownDeletesAreVisible(int type) throws SQLException {
		return true;
	}

	@Override
	public boolean ownInsertsAreVisible(int type) throws SQLException {
		return true;
	}

	@Override
	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		return false;
	}

	@Override
	public boolean othersDeletesAreVisible(int type) throws SQLException {
		return false;
	}

	@Override
	public boolean othersInsertsAreVisible(int type) throws SQLException {
		return false;
	}

	@Override
	public boolean updatesAreDetected(int type) throws SQLException {
		return false;
	}

	@Override
	public boolean deletesAreDetected(int type) throws SQLException {
		return false;
	}

	@Override
	public boolean insertsAreDetected(int type) throws SQLException {
		return false;
	}

	@Override
	public boolean supportsBatchUpdates() throws SQLException {
		return true;
	}

	@Override
	public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
													"TYPE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
													"TYPE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
													"TYPE_NAME:VARCHAR(100)",
													"CLASS_TYPE:VARCHAR(100)",
													"DATA_TYPE:INTEGER",
													"REMARKS:VARCHAR(100)",
													"BASE_TYPE:SMALLINT"),
													ResultSet.TYPE_FORWARD_ONLY
												);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return parent;
	}

	@Override
	public boolean supportsSavepoints() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsNamedParameters() throws SQLException {
		return false;
	}

	@Override
	public boolean supportsMultipleOpenResults() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsGetGeneratedKeys() throws SQLException {
		return false;
	}

	@Override
	public ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
													"TYPE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
													"TYPE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
													"TYPE_NAME:VARCHAR(100)",
													"SUPERTYPE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
													"SUPERTYPE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
													"SUPERTYPE_NAME:VARCHAR(100)"),
													ResultSet.TYPE_FORWARD_ONLY
												);
	}

	@Override
	public ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
													"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
													"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
													"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
													"SUPERTABLE_NAME:VARCHAR("+getMaxTableNameLength()+")"),
													ResultSet.TYPE_FORWARD_ONLY
												);
	}

	@Override
	public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
												"TYPE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
												"TYPE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
												"TYPE_NAME:VARCHAR(100)",
												"ATTR_NAME:VARCHAR(100)",
												"DATA_TYPE:INTEGER",
												"ATTR_TYPE_NAME:VARCHAR(100)",
												"ATTR_SIZE:INTEGER",
												"DECIMAL_DIGITS:INTEGER",
												"NUM_PREC_RADIX:INTEGER",
												"NULLABLE:INTEGER",
												"REMARKS:VARCHAR(100)",
												"ATTR_DEF:VARCHAR(100)",
												"SQL_DATA_TYPE:INTEGER",												
												"SQL_DATETIME_SUB:INTEGER",												
												"CHAR_OCTET_LENGTH:INTEGER",												
												"ORDINAL_POSITION:INTEGER",												
												"IS_NULLABLE:VARCHAR(10)",
												"SCOPE_CATALOG:VARCHAR("+getMaxCatalogNameLength()+")",
												"SCOPE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
												"SCOPE_TABLE:VARCHAR("+getMaxTableNameLength()+")",
												"SOURCE_DATA_TYPE:SMALLINT"),
												ResultSet.TYPE_FORWARD_ONLY
											);
	}

	@Override
	public boolean supportsResultSetHoldability(int holdability) throws SQLException {
		return holdability == ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public int getDatabaseMajorVersion() throws SQLException {
		return getDriverMajorVersion();
	}

	@Override
	public int getDatabaseMinorVersion() throws SQLException {
		return getDriverMajorVersion();
	}

	@Override
	public int getJDBCMajorVersion() throws SQLException {
		return 1;
	}

	@Override
	public int getJDBCMinorVersion() throws SQLException {
		return 0;
	}

	@Override
	public int getSQLStateType() throws SQLException {
		return DatabaseMetaData.sqlStateSQL;
	}

	@Override
	public boolean locatorsUpdateCopy() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsStatementPooling() throws SQLException {
		return false;
	}

	@Override
	public RowIdLifetime getRowIdLifetime() throws SQLException {
		return RowIdLifetime.ROWID_UNSUPPORTED;
	}

	@Override
	public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
		final String[][]	found = scanContent(InternalUtils.buildPattern4LikeStyledTemplate(catalog == null || catalog.isEmpty() ? "%" : catalog),
												InternalUtils.buildPattern4LikeStyledTemplate(schemaPattern == null || schemaPattern.isEmpty() ? "%" : schemaPattern),
												NEVER_MATCH
												); 
		final Object[][]	content = new Object[found.length][];
		
		for (int index = 0; index < content.length; index++) {
			content[index] = new String[]{found[index][1],found[index][0]};
		}
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
												"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
												"TABLE_CATALOG:VARCHAR("+getMaxCatalogNameLength()+")"),
												ResultSet.FETCH_FORWARD,new ArrayContent(content)
											);
	}

	@Override
	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		return true;
	}

	@Override
	public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
		return false;
	}

	@Override
	public ResultSet getClientInfoProperties() throws SQLException {
		final Properties	props = parent.getClientInfo();
		final Object[][]	content = new Object[props.size()][];
		int					index = 0;
		
		for (Entry<Object, Object> item : props.entrySet()) {
			content[index++] = new Object[]{item.getKey().toString(),1024,item.getValue(),""};
		}	
		return new InMemoryReadOnlyResultSet(new FSysResultSetMetaData(null, null, null, true,
													"NAME:VARCHAR(100)",
													"MAX_LEN:INTEGER",
													"DEFAULT_VALUE:VARCHAR(1024)",
													"DESCRIPTION:VARCHAR(1024)"),
													ResultSet.FETCH_FORWARD,new ArrayContent(content)
												);
	}

	@Override
	public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
													"FUNCTION_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
													"FUNCTION_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
													"FUNCTION_NAME:VARCHAR(100)",
													"REMARKS:VARCHAR(100)",
													"FUNCTION_TYPE:SMALLINT",
													"SPECIFIC_NAME:VARCHAR(100)"),
													ResultSet.TYPE_FORWARD_ONLY
												);
	}

	@Override
	public ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
													"FUNCTION_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
													"FUNCTION_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
													"FUNCTION_NAME:VARCHAR(100)",
													"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
													"COLUMN_TYPE:SMALLINT",
													"DATA_TYPE:INTEGER",
													"TYPE_NAME:VARCHAR(100)",
													"PRECISION:INTEGER",
													"LENGTH:INTEGER",
													"SCALE:SMALLINT",
													"RADIX:SMALLINT",
													"NULLABLE:SMALLINT",
													"REMARKS:VARCHAR(100)",
													"CHAR_OCTET_LENGTH:INTEGER",
													"ORDINAL_POSITION:INTEGER",
													"IS_NULLABLE:VARCHAR(100)",
													"SPECIFIC_NAME:VARCHAR("+getMaxColumnNameLength()+")"),
													ResultSet.TYPE_FORWARD_ONLY
												);
	}

	@Override
	public ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
		return new NullReadOnlyResultSet(new FSysResultSetMetaData(catalog, null, null, true,
													"TABLE_CAT:VARCHAR("+getMaxCatalogNameLength()+")",
													"TABLE_SCHEM:VARCHAR("+getMaxSchemaNameLength()+")",
													"TABLE_NAME:VARCHAR("+getMaxTableNameLength()+")",
													"COLUMN_NAME:VARCHAR("+getMaxColumnNameLength()+")",
													"DATA_TYPE:INTEGER",
													"COLUMN_SIZE:INTEGER",
													"DECIMAL_DIGITS:INTEGER",
													"NUM_PREC_RADIX:INTEGER",
													"COLUMN_USAGE:VARCHAR(100)",
													"REMARKS:VARCHAR(100)",
													"CHAR_OCTET_LENGTH:INTEGER",
													"IS_NULLABLE:VARCHAR(100)"),
													ResultSet.TYPE_FORWARD_ONLY
												);
	}

	@Override
	public boolean generatedKeyAlwaysReturned() throws SQLException {
		return false;
	}

	protected String[][] scanContent(final Pattern forCatalog, final Pattern forSchema, final Pattern forTable) throws SQLException {
		final List<String[]>		result = new ArrayList<>();
		
		try(final FileSystemInterface	level1 = fsi.clone().open("/")) {
			for (String item1 : level1.list()) {
				if (forCatalog.matcher(item1).matches()) {
					try(final FileSystemInterface	level1Item = fsi.clone().open("/"+item1)) {
						if (level1Item.isFile()) {
							result.add(new String[]{null,null,item1});
						}
						else {
							result.add(new String[]{item1,null,null});
							
							for (String item2 : level1Item.list()) {
								if (forSchema.matcher(item2).matches()) {
									try(final FileSystemInterface	level2Item = fsi.clone().open("/"+item1+"/"+item2)) {
										if (level2Item.isFile()) {
											result.add(new String[]{item1,null,item2});
										}
										else {
											result.add(new String[]{item1,item2,null});
											
											for (String item3 : level2Item.list()) {
												if (forTable.matcher(item3).matches()) {
													try(final FileSystemInterface	level3Item = fsi.clone().open("/"+item1+"/"+item2+"/"+item3)) {
														if (level3Item.isFile()) {
															result.add(new String[]{item1,item2,item3});
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			return result.toArray(new String[result.size()][]);
		} catch (IOException e) {
			throw new SQLException("I/O error getting catalog content: "+e.getLocalizedMessage(),e);
		} finally {
			result.clear();
		}
	}
	
	protected ResultSetMetaData extractMetaData(final String catalog, final String schema, final String table) throws SQLException {
		final StringBuilder	sb = new StringBuilder("select * from ");
		
		if (catalog != null && catalog.isEmpty()) {
			sb.append(catalog).append('.');
		}
		if (schema != null && schema.isEmpty()) {
			sb.append(schema).append('.');
		}
		sb.append(table);
		
		try(final Statement	stmt = parent.createStatement();
			final ResultSet	rs = stmt.executeQuery(sb.toString())) {
			
			return rs.getMetaData();
		}
	}
	
}
*/