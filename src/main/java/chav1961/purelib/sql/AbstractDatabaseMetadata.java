package chav1961.purelib.sql;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public abstract class AbstractDatabaseMetadata implements DatabaseMetaData {
	public static final String[]				ANY_CONTENT = new String[0];
	
	public static final String					ALIAS_SORT = "ALIAS";
	public static final String					GLOBAL_TEMPORARY_SORT = "GLOBAL TEMPORARY";
	public static final String					LOCAL_TEMPORARY_SORT = "LOCAL TEMPORARY";
	public static final String					FUNCTION_SORT = "FUNCTION";
	public static final String					PROCEDURE_SORT = "PROCEDURE";
	public static final String					SYNONYM_SORT = "SYNONYM";
	public static final String					TABLE_SORT = "TABLE";
	public static final String					TYPE_SORT = "TYPE";
	public static final String					VIEW_SORT = "VIEW";

	public static final String					ASC_OR_DESC_NAME = "ASC_OR_DESC";
	
	public static final String					ATTR_DEF_NAME = "ATTR_DEF";
	public static final String					ATTR_NAME_NAME = "ATTR_NAME";
	public static final String					ATTR_SIZE_NAME = "ATTR_SIZE";
	public static final String					ATTR_TYPE_NAME_NAME = "ATTR_TYPE_NAME";
	public static final String					AUTO_INCREMENT_NAME = "AUTO_INCREMENT";

	public static final String					BASE_TYPE_NAME = "BASE_TYPE";
	
	public static final String					CARDINALITY_NAME = "CARDINALITY";
	public static final String					CASE_SENSITIVE_NAME = "CASE_SENSITIVE";

	public static final String					CHAR_OCTET_LENGTH_NAME = "CHAR_OCTET_LENGTH";

	public static final String					CLASS_NAME_NAME = "CLASS_NAME";
	
	public static final String					COLUMN_DEF_NAME = "COLUMN_DEF";
	public static final String					COLUMN_NAME_NAME = "COLUMN_NAME";
	public static final String					COLUMN_SIZE_NAME = "COLUMN_SIZE";
	public static final String					COLUMN_TYPE_NAME = "COLUMN_TYPE";
	public static final String					COLUMN_USAGE_NAME = "COLUMN_USAGE";
	
	public static final String					DATA_TYPE_NAME = "DATA_TYPE";	

	public static final String					DECIMAL_DIGITS_NAME = "DECIMAL_DIGITS";	

	public static final String					DEFAULT_VALUE_NAME = "DEFAULT_VALUE";
	
	public static final String					DEFERRABILITY_NAME = "DEFERRABILITY";
	
	public static final String					DELETE_RULE_NAME = "DELETE_RULE";

	public static final String					DESCRIPTION_NAME = "DESCRIPTION";
	
	public static final String					FILTER_CONDITION_NAME = "FILTER_CONDITION";	
	public static final String					FIXED_PREC_SCALE_NAME = "FIXED_PREC_SCALE";	

	public static final String					FK_NAME_NAME = "FK_NAME";
	
	public static final String					FKTABLE_CAT_NAME = "FKTABLE_CAT";
	public static final String					FKTABLE_SCHEM_NAME = "FKTABLE_SCHEM";
	public static final String					FKTABLE_TABLE_NAME = "FKTABLE_NAME";
	public static final String					FKTABLE_COLUMN_NAME = "FKCOLUMN_NAME";
	
	public static final String					FUNCTION_CAT_NAME = "FUNCTION_CAT";
	public static final String					FUNCTION_SCHEM_NAME = "FUNCTION_SCHEM";
	public static final String					FUNCTION_NAME_NAME = "FUNCTION_NAME";

	public static final String					GRANTEE_NAME = "GRANTEE";	
	public static final String					GRANTOR_NAME = "GRANTOR";	

	public static final String					INDEX_NAME_NAME = "INDEX_NAME";	
	public static final String					INDEX_QUALIFIER_NAME = "INDEX_QUALIFIER";	
	
	public static final String					IS_AUTOINCREMENT_NAME = "IS_AUTOINCREMENT";	
	public static final String					IS_GENERATEDCOLUMN_NAME = "IS_GENERATEDCOLUMN";	
	public static final String					IS_GRANTABLE_NAME = "IS_GRANTABLE";	
	public static final String					IS_NULLABLE_NAME = "IS_NULLABLE";	

	public static final String					KEY_SEQ_NAME = "KEY_SEQ";	
	
	public static final String					LENGTH_NAME = "LENGTH";	
	public static final String					LOCAL_TYPE_NAME_NAME = "LOCAL_TYPE_NAME";	

	public static final String					MINIMUM_SCALE_NAME = "MINIMUM_SCALE";	

	public static final String					MAX_LEN_NAME = "MAX_LEN";	
	
	public static final String					MAXIMUM_SCALE_NAME = "MAXIMUM_SCALE";	

	public static final String					NAME_NAME = "NAME";
	
	public static final String					NON_UNIQUE_NAME = "NON_UNIQUE";	
	
	public static final String					NULLABLE_NAME = "NULLABLE";	
	
	public static final String					NUM_PREC_RADIX_NAME = "NUM_PREC_RADIX";

	public static final String					ORDINAL_POSITION_NAME = "ORDINAL_POSITION";

	public static final String					PAGES_NAME = "PAGES";
	
	public static final String					PKTABLE_CAT_NAME = "PKTABLE_CAT";
	public static final String					PKTABLE_SCHEM_NAME = "PKTABLE_SCHEM";
	public static final String					PKTABLE_TABLE_NAME = "PKTABLE_NAME";
	public static final String					PKTABLE_COLUMN_NAME = "PKCOLUMN_NAME";
	
	public static final String					PK_NAME_NAME = "PK_NAME";
	
	public static final String					PROCEDURE_CAT_NAME = "PROCEDURE_CAT";
	public static final String					PROCEDURE_SCHEM_NAME = "PROCEDURE_SCHEM";
	public static final String					PROCEDURE_NAME_NAME = "PROCEDURE_NAME";

	public static final String					PRECISION_NAME = "PRECISION";

	public static final String					PRIVILEGE_NAME = "PRIVILEGE";

	public static final String					PSEUDO_COLUMN_NAME = "PSEUDO_COLUMN";
	
	public static final String					RADIX_NAME = "RADIX";
	
	public static final String					REMARKS_NAME = "REMARKS";
	
	public static final String					SEARCHABLE_NAME = "SEARCHABLE";

	public static final String					SCALE_NAME = "SCALE";

	public static final String					SCOPE_NAME = "SCOPE";
	
	public static final String					SCOPE_CATALOG_NAME = "SCOPE_CATALOG";
	public static final String					SCOPE_SCHEMA_NAME = "SCOPE_SCHEMA";
	public static final String					SCOPE_TABLE_NAME = "SCOPE_TABLE";
	
	public static final String					SOURCE_DATA_TYPE_NAME = "SOURCE_DATA_TYPE";

	public static final String					SPECIFIC_NAME_NAME = "SPECIFIC_NAME";

	public static final String					SUPERTABLE_NAME_NAME = "SUPERTABLE_NAME";
	
	public static final String					SUPERTYPE_CAT_NAME = "SUPERTYPE_CAT";
	public static final String					SUPERTYPE_SCHEM_NAME = "SUPERTYPE_SCHEM";
	public static final String					SUPERTYPE_NAME_NAME = "SUPERTYPE_NAME";
	
	public static final String					TABLE_CAT_NAME = "TABLE_CAT";
	public static final String					TABLE_SCHEM_NAME = "TABLE_SCHEM";
	public static final String					TABLE_NAME_NAME = "TABLE_NAME";
	public static final String					TABLE_TYPE_NAME = "TABLE_TYPE";
	
	public static final String					TYPE_NAME = "TYPE";

	public static final String					TYPE_CAT_NAME = "TYPE_CAT";
	public static final String					TYPE_SCHEM_NAME = "TYPE_SCHEM";
	public static final String					TYPE_NAME_NAME = "TYPE_NAME";

	public static final String					UNSIGNED_ATTRIBUTE_NAME = "UNSIGNED_ATTRIBUTE";

	public static final String					UPDATE_RULE_NAME = "UPDATE_RULE";
	
	public static final String[]				PROCEDURES_CONTENT = new String[]{PROCEDURE_SORT};
	public static final String[]				FUNCTIONS_CONTENT = new String[]{PROCEDURE_SORT};
	public static final String[]				TABLES_CONTENT = new String[]{TABLE_SORT, VIEW_SORT, GLOBAL_TEMPORARY_SORT};
	public static final String[]				TYPES_CONTENT = new String[]{TYPE_SORT};
	
	private static final RsMetaDataElement[]	ATTRIBUTES_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TYPE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(ATTR_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(ATTR_TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(ATTR_SIZE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(DECIMAL_DIGITS_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(NUM_PREC_RADIX_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(NULLABLE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(ATTR_DEF_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement("SQL_DATA_TYPE","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement("SQL_DATETIME_SUB","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(CHAR_OCTET_LENGTH_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(ORDINAL_POSITION_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(IS_NULLABLE_NAME,"","VARCHAR",Types.VARCHAR,10,0)
													,new RsMetaDataElement(SCOPE_CATALOG_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SCOPE_SCHEMA_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SCOPE_TABLE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SOURCE_DATA_TYPE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
												};
	private static final RsMetaDataElement[]	BEST_ROW_META = new RsMetaDataElement[]{
													new RsMetaDataElement(SCOPE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(COLUMN_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_SIZE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement("BUFFER_LENGTH","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(DECIMAL_DIGITS_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(PSEUDO_COLUMN_NAME,"","SMALLINT",Types.SMALLINT,5,0)
												};
	private static final RsMetaDataElement[]	CATALOG_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};	
	private static final RsMetaDataElement[]	CLIENT_INFO_META = new RsMetaDataElement[]{
													new RsMetaDataElement(NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(MAX_LEN_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(DEFAULT_VALUE_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(DESCRIPTION_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
												};
	private static final RsMetaDataElement[]	COLUMNS_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_SIZE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement("BUFFER_LENGTH","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(DECIMAL_DIGITS_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(NUM_PREC_RADIX_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(NULLABLE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(COLUMN_DEF_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement("SQL_DATA_TYPE","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement("SQL_DATETIME_SUB","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(CHAR_OCTET_LENGTH_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(ORDINAL_POSITION_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(IS_NULLABLE_NAME,"","VARCHAR",Types.VARCHAR,10,0)
													,new RsMetaDataElement(SCOPE_CATALOG_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SCOPE_SCHEMA_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SCOPE_TABLE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SOURCE_DATA_TYPE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(IS_AUTOINCREMENT_NAME,"","VARCHAR",Types.VARCHAR,10,0)
													,new RsMetaDataElement(IS_GENERATEDCOLUMN_NAME,"","VARCHAR",Types.VARCHAR,10,0)
												};
	private static final RsMetaDataElement[]	COLUMN_PRIVILEGES_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(GRANTOR_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(GRANTEE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PRIVILEGE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(IS_GRANTABLE_NAME,"","VARCHAR",Types.VARCHAR,10,0)
												};
	private static final RsMetaDataElement[]	FUNCTION_META = new RsMetaDataElement[]{
													new RsMetaDataElement(FUNCTION_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FUNCTION_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FUNCTION_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(SPECIFIC_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};
	private static final RsMetaDataElement[]	FUNCTION_COLUMNS_META = new RsMetaDataElement[]{
													new RsMetaDataElement(FUNCTION_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FUNCTION_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FUNCTION_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_TYPE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PRECISION_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(LENGTH_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(SCALE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(RADIX_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(NULLABLE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(CHAR_OCTET_LENGTH_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(ORDINAL_POSITION_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(IS_NULLABLE_NAME,"","VARCHAR",Types.VARCHAR,10,0)
													,new RsMetaDataElement(SPECIFIC_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};
	private static final RsMetaDataElement[]	KEYS_META = new RsMetaDataElement[]{
													new RsMetaDataElement(PKTABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PKTABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PKTABLE_TABLE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PKTABLE_COLUMN_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FKTABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FKTABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FKTABLE_TABLE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FKTABLE_COLUMN_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(KEY_SEQ_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(UPDATE_RULE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(DELETE_RULE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(PK_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(FK_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DEFERRABILITY_NAME,"","SMALLINT",Types.SMALLINT,5,0)
												};
	private static final RsMetaDataElement[]	INDEX_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(NON_UNIQUE_NAME,"","BOOLEAN",Types.BOOLEAN,1,0)
													,new RsMetaDataElement(INDEX_QUALIFIER_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(INDEX_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(ORDINAL_POSITION_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(COLUMN_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(ASC_OR_DESC_NAME,"","CHAR",Types.CHAR,1,0)
													,new RsMetaDataElement(CARDINALITY_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(PAGES_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(FILTER_CONDITION_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
												};
	private static final RsMetaDataElement[]	PRIMARY_KEYS_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(KEY_SEQ_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(PK_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};  
	private static final RsMetaDataElement[]	PSEUDOCOLUMNS_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(COLUMN_SIZE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(DECIMAL_DIGITS_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(NUM_PREC_RADIX_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(COLUMN_USAGE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(CHAR_OCTET_LENGTH_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(IS_NULLABLE_NAME,"","VARCHAR",Types.VARCHAR,10,0)
												};  
	private static final RsMetaDataElement[]	SCHEMA_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement("TABLE_CATALOG","","VARCHAR",Types.VARCHAR,2048,0)
												};  
	private static final RsMetaDataElement[]	SUPERTYPE_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TYPE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SUPERTYPE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SUPERTYPE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SUPERTYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};  
	private static final RsMetaDataElement[]	SUPERTABLE_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(SUPERTABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};  
	private static final RsMetaDataElement[]	PROCEDURE_META = new RsMetaDataElement[]{
													new RsMetaDataElement(PROCEDURE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PROCEDURE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PROCEDURE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement("EMPTY1","","BIT",Types.BIT,1,0)
													,new RsMetaDataElement("EMPTY2","","BIT",Types.BIT,1,0)
													,new RsMetaDataElement("EMPTY3","","BIT",Types.BIT,1,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement("PROCEDURE_TYPE","","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(SPECIFIC_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};
	private static final RsMetaDataElement[]	TABLE_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_TYPE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement("TYPE_CAT","","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement("TYPE_SCHEM","","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement("SELF_REFERENCING_COL_NAME","","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement("REF_GENERATION","","VARCHAR",Types.VARCHAR,2048,0)
												};
	private static final RsMetaDataElement[]	TABLE_PRIVILEGES_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TABLE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(GRANTOR_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(GRANTEE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(PRIVILEGE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(IS_GRANTABLE_NAME,"","VARCHAR",Types.VARCHAR,10,0)
												};
	private static final RsMetaDataElement[]	TABLE_TYPES_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TABLE_TYPE_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
												};
	private static final RsMetaDataElement[]	TYPE_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(PRECISION_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement("LITERAL_PREFIX","","VARCHAR",Types.VARCHAR,1,0)
													,new RsMetaDataElement("LITERAL_SUFFIX","","VARCHAR",Types.VARCHAR,1,0)
													,new RsMetaDataElement("CREATE_PARAMS","","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(NULLABLE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(CASE_SENSITIVE_NAME,"","BOOLEAN",Types.BOOLEAN,1,0)
													,new RsMetaDataElement(SEARCHABLE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(UNSIGNED_ATTRIBUTE_NAME,"","BOOLEAN",Types.BOOLEAN,1,0)
													,new RsMetaDataElement(FIXED_PREC_SCALE_NAME,"","BOOLEAN",Types.BOOLEAN,1,0)
													,new RsMetaDataElement(AUTO_INCREMENT_NAME,"","BOOLEAN",Types.BOOLEAN,1,0)
													,new RsMetaDataElement(LOCAL_TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(MINIMUM_SCALE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(MAXIMUM_SCALE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement("SQL_DATA_TYPE","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement("SQL_DATETIME_SUB","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(NUM_PREC_RADIX_NAME,"","INTEGER",Types.INTEGER,1,0)
												};
	private static final RsMetaDataElement[]	UDT_META = new RsMetaDataElement[]{
													new RsMetaDataElement(TYPE_CAT_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_SCHEM_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(CLASS_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(REMARKS_NAME,"","VARCHAR",Types.VARCHAR,65536,0)
													,new RsMetaDataElement(BASE_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
												};
	private static final RsMetaDataElement[]	VERSION_COLUMNS_META = new RsMetaDataElement[]{
													new RsMetaDataElement(SCOPE_NAME,"","SMALLINT",Types.SMALLINT,5,0)
													,new RsMetaDataElement(COLUMN_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(DATA_TYPE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(TYPE_NAME_NAME,"","VARCHAR",Types.VARCHAR,2048,0)
													,new RsMetaDataElement(COLUMN_SIZE_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement("BUFFER_LENGTH","","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(DECIMAL_DIGITS_NAME,"","INTEGER",Types.INTEGER,9,0)
													,new RsMetaDataElement(PSEUDO_COLUMN_NAME,"","SMALLINT",Types.SMALLINT,5,0)
												};
	
	private static final Object[][]				TABLE_TYPES_RS = new Object[][]{
													new Object[]{TABLE_SORT}, new Object[]{VIEW_SORT}, new Object[]{GLOBAL_TEMPORARY_SORT}
												};
	
	public enum CaseSensitivity {
		LOWER_CASE, UPPER_CASE, MIXED_CASE
	}

	public enum GrammarLevel {
		CORE_SQL, EXTENDED_SQL, ANSI92_ENTRYLEVEL, ANSI92_INTERMEDIATE, ANSI92_FULL	
	}
	
	private final AbstractDriver		driver;
	private final AbstractConnection	conn;
	private final CaseSensitivity		caseSens;
	private final GrammarLevel			grammarLevel;
	
	protected AbstractDatabaseMetadata(final AbstractDriver driver, final AbstractConnection conn, final CaseSensitivity caseSens
									  ,final GrammarLevel grammarLevel) {
		this.driver = driver;
		this.conn = conn;
		this.caseSens = caseSens;
		this.grammarLevel = grammarLevel;
	}

	@FunctionalInterface
	protected interface DBTreeCallback<T> {
		ContinueMode process(NodeEnterMode mode, RsMetaDataElement[] contentType, String nodeType, T node);
	}

	protected abstract boolean isSuperUser(String userName) throws SQLException;
	protected abstract boolean isCatalogsSupported() throws SQLException;
	protected abstract <T> ContinueMode collect(final String pattern, final String[] acceptedTypes, final RsMetaDataElement[] contentType, DBTreeCallback<T> callback);
	
	@Override public abstract boolean nullsAreSortedHigh() throws SQLException;
	@Override public abstract boolean nullsAreSortedLow() throws SQLException;
	@Override public abstract boolean nullsAreSortedAtStart() throws SQLException;
	@Override public abstract boolean nullsAreSortedAtEnd() throws SQLException;
	@Override public abstract String getDatabaseProductVersion() throws SQLException;
	@Override public abstract String getIdentifierQuoteString() throws SQLException;
	@Override public abstract boolean usesLocalFiles() throws SQLException;
	@Override public abstract boolean usesLocalFilePerTable() throws SQLException;
	@Override public abstract String getSQLKeywords() throws SQLException;
	@Override public abstract String getNumericFunctions() throws SQLException;
	@Override public abstract String getStringFunctions() throws SQLException;
	@Override public abstract String getSystemFunctions() throws SQLException;
	@Override public abstract String getTimeDateFunctions() throws SQLException;
	@Override public abstract String getSearchStringEscape() throws SQLException;
	@Override public abstract String getExtraNameCharacters() throws SQLException;
	@Override public abstract boolean supportsAlterTableWithAddColumn() throws SQLException;
	@Override public abstract boolean supportsAlterTableWithDropColumn() throws SQLException;
	@Override public abstract boolean supportsColumnAliasing() throws SQLException;	
	@Override public abstract boolean supportsTableCorrelationNames() throws SQLException;
	@Override public abstract boolean supportsDifferentTableCorrelationNames() throws SQLException;
	@Override public abstract boolean supportsExpressionsInOrderBy() throws SQLException;
	@Override public abstract boolean supportsOrderByUnrelated() throws SQLException;
	@Override public abstract boolean supportsGroupBy() throws SQLException;
	@Override public abstract boolean supportsGroupByUnrelated() throws SQLException;
	@Override public abstract boolean supportsGroupByBeyondSelect() throws SQLException;
	@Override public abstract boolean supportsLikeEscapeClause() throws SQLException;
	@Override public abstract boolean supportsMultipleResultSets() throws SQLException;
	@Override public abstract boolean supportsMultipleTransactions() throws SQLException;
	@Override public abstract boolean supportsNonNullableColumns() throws SQLException;
	@Override public abstract boolean supportsIntegrityEnhancementFacility() throws SQLException;
	@Override public abstract boolean supportsOuterJoins() throws SQLException;
	@Override public abstract boolean supportsFullOuterJoins() throws SQLException;
	@Override public abstract boolean supportsLimitedOuterJoins() throws SQLException;
	@Override public abstract boolean isCatalogAtStart() throws SQLException;
	@Override public abstract String getCatalogSeparator() throws SQLException;
	@Override public abstract boolean supportsSchemasInDataManipulation() throws SQLException;
	@Override public abstract boolean supportsSchemasInProcedureCalls() throws SQLException;
	@Override public abstract boolean supportsSchemasInTableDefinitions() throws SQLException;
	@Override public abstract boolean supportsSchemasInIndexDefinitions() throws SQLException;
	@Override public abstract boolean supportsSchemasInPrivilegeDefinitions() throws SQLException;
	@Override public abstract boolean supportsCatalogsInDataManipulation() throws SQLException;
	@Override public abstract boolean supportsCatalogsInProcedureCalls() throws SQLException;
	@Override public abstract boolean supportsCatalogsInTableDefinitions() throws SQLException;
	@Override public abstract boolean supportsCatalogsInIndexDefinitions() throws SQLException;
	@Override public abstract boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException;
	@Override public abstract boolean supportsPositionedDelete() throws SQLException;
	@Override public abstract boolean supportsPositionedUpdate() throws SQLException;
	@Override public abstract boolean supportsSelectForUpdate() throws SQLException;
	@Override public abstract boolean supportsStoredProcedures() throws SQLException;
	@Override public abstract boolean supportsSubqueriesInComparisons() throws SQLException;
	@Override public abstract boolean supportsSubqueriesInExists() throws SQLException;
	@Override public abstract boolean supportsSubqueriesInIns() throws SQLException;
	@Override public abstract boolean supportsSubqueriesInQuantifieds() throws SQLException;
	@Override public abstract boolean supportsCorrelatedSubqueries() throws SQLException;
	@Override public abstract boolean supportsUnion() throws SQLException;
	@Override public abstract boolean supportsUnionAll() throws SQLException;
	@Override public abstract boolean supportsOpenCursorsAcrossCommit() throws SQLException;
	@Override public abstract boolean supportsOpenCursorsAcrossRollback() throws SQLException;
	@Override public abstract boolean supportsOpenStatementsAcrossCommit() throws SQLException;
	@Override public abstract boolean supportsOpenStatementsAcrossRollback() throws SQLException;
	@Override public abstract int getDefaultTransactionIsolation() throws SQLException;
	@Override public abstract boolean supportsTransactions() throws SQLException;
	@Override public abstract boolean supportsTransactionIsolationLevel(int level) throws SQLException;
	@Override public abstract boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException;
	@Override public abstract boolean supportsDataManipulationTransactionsOnly() throws SQLException;
	@Override public abstract boolean dataDefinitionCausesTransactionCommit() throws SQLException;
	@Override public abstract boolean dataDefinitionIgnoredInTransactions() throws SQLException;
	@Override public abstract boolean supportsResultSetType(int type) throws SQLException;
	@Override public abstract boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException;
	@Override public abstract boolean ownUpdatesAreVisible(int type) throws SQLException;
	@Override public abstract boolean ownDeletesAreVisible(int type) throws SQLException;
	@Override public abstract boolean ownInsertsAreVisible(int type) throws SQLException;
	@Override public abstract boolean othersUpdatesAreVisible(int type) throws SQLException;
	@Override public abstract boolean othersDeletesAreVisible(int type) throws SQLException;
	@Override public abstract boolean othersInsertsAreVisible(int type) throws SQLException;
	@Override public abstract boolean updatesAreDetected(int type) throws SQLException;
	@Override public abstract boolean deletesAreDetected(int type) throws SQLException;
	@Override public abstract boolean insertsAreDetected(int type) throws SQLException;
	@Override public abstract boolean supportsBatchUpdates() throws SQLException;	
	@Override public abstract boolean supportsSavepoints() throws SQLException;
	@Override public abstract boolean supportsNamedParameters() throws SQLException;
	@Override public abstract boolean supportsMultipleOpenResults() throws SQLException;
	@Override public abstract boolean supportsGetGeneratedKeys() throws SQLException;
	@Override public abstract boolean supportsResultSetHoldability(int holdability) throws SQLException;
	@Override public abstract int getResultSetHoldability() throws SQLException;
	@Override public abstract int getSQLStateType() throws SQLException;
	@Override public abstract boolean locatorsUpdateCopy() throws SQLException;
	@Override public abstract boolean supportsStatementPooling() throws SQLException;
	@Override public abstract RowIdLifetime getRowIdLifetime() throws SQLException;
	@Override public abstract boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException;
	@Override public abstract boolean autoCommitFailureClosesAllResultSets() throws SQLException;
	@Override public abstract boolean generatedKeyAlwaysReturned() throws SQLException;
	
	@Override 
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Interface to convert to can't ne null");
		}
		else {
			return iface.cast(this);
		}
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		if (iface == null) {
			throw new NullPointerException("Interface to convert to can't ne null");
		}
		else {
			return iface.isAssignableFrom(this.getClass());
		}
	}

	@Override
	public boolean allProceduresAreCallable() throws SQLException {
		return isSuperUser(getUserName());
	}

	@Override
	public boolean allTablesAreSelectable() throws SQLException {
		return isSuperUser(getUserName());
	}

	@Override
	public String getURL() throws SQLException {
		return conn.connString.toString();
	}

	@Override
	public String getUserName() throws SQLException {
		return conn.user;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return conn.isReadOnly();
	}

	@Override
	public String getDatabaseProductName() throws SQLException {
		return driver.getDatabaseProductName();
	}

	@Override
	public String getDriverName() throws SQLException {
		return driver.getDriverName();
	}

	@Override
	public String getDriverVersion() throws SQLException {
		return getDriverMajorVersion()+"."+getDriverMinorVersion();
	}

	@Override
	public int getDriverMajorVersion() {
		return driver.getMajorVersion();
	}

	@Override
	public int getDriverMinorVersion() {
		return driver.getMinorVersion();
	}

	@Override
	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		return caseSens == CaseSensitivity.MIXED_CASE;
	}

	@Override
	public boolean storesUpperCaseIdentifiers() throws SQLException {
		return caseSens == CaseSensitivity.UPPER_CASE;
	}

	@Override
	public boolean storesLowerCaseIdentifiers() throws SQLException {
		return caseSens == CaseSensitivity.LOWER_CASE;
	}

	@Override
	public boolean storesMixedCaseIdentifiers() throws SQLException {
		return caseSens == CaseSensitivity.MIXED_CASE;
	}

	@Override
	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		return supportsMixedCaseIdentifiers();
	}

	@Override
	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		return storesUpperCaseIdentifiers();
	}

	@Override
	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		return storesLowerCaseIdentifiers();
	}

	@Override
	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		return storesMixedCaseIdentifiers();
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
	public boolean supportsConvert(int fromType, int toType) throws SQLException {
		return SQLUtils.canConvert(fromType, toType);
	}

	@Override
	public boolean supportsMinimumSQLGrammar() throws SQLException {
		return true;
	}

	@Override
	public boolean supportsCoreSQLGrammar() throws SQLException {
		return grammarLevel.ordinal() >= GrammarLevel.CORE_SQL.ordinal();
	}

	@Override
	public boolean supportsExtendedSQLGrammar() throws SQLException {
		return grammarLevel.ordinal() >= GrammarLevel.EXTENDED_SQL.ordinal();
	}

	@Override
	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		return grammarLevel.ordinal() >= GrammarLevel.ANSI92_ENTRYLEVEL.ordinal();
	}

	@Override
	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		return grammarLevel.ordinal() >= GrammarLevel.ANSI92_INTERMEDIATE.ordinal();
	}

	@Override
	public boolean supportsANSI92FullSQL() throws SQLException {
		return grammarLevel.ordinal() >= GrammarLevel.ANSI92_FULL.ordinal();
	}

	@Override
	public int getMaxBinaryLiteralLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxCharLiteralLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxColumnNameLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxColumnsInGroupBy() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxColumnsInIndex() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxColumnsInOrderBy() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxColumnsInSelect() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxColumnsInTable() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxConnections() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxCursorNameLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxIndexLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxSchemaNameLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxProcedureNameLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxCatalogNameLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxRowSize() throws SQLException {
		return 0;
	}

	@Override
	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		return false;
	}

	@Override
	public int getMaxStatementLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxStatements() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxTableNameLength() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxTablesInSelect() throws SQLException {
		return 0;
	}

	@Override
	public int getMaxUserNameLength() throws SQLException {
		return 0;
	}

	@Override 
	public String getSchemaTerm() throws SQLException {
		return "SCHEMA";
	}
	
	@Override 
	public String getProcedureTerm() throws SQLException {
		return "PROCEDURE";
	}
	
	@Override 
	public String getCatalogTerm() throws SQLException {
		return isCatalogsSupported() ? "CATALOG" : null;
	}
	
	@Override
	public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(procedureNamePattern == null ? "%" : procedureNamePattern)
				,PROCEDURES_CONTENT,PROCEDURE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PROCEDURE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(procedureNamePattern == null ? "%" : procedureNamePattern)
				,PROCEDURES_CONTENT,PROCEDURE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PROCEDURE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(procedureNamePattern == null ? "%" : procedureNamePattern)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,PROCEDURES_CONTENT,PROCEDURE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PROCEDURE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(procedureNamePattern == null ? "%" : procedureNamePattern)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,PROCEDURES_CONTENT,PROCEDURE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PROCEDURE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)
				,types,TABLE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(TABLE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)
				,types,TABLE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(TABLE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getSchemas() throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect("%/%",ANY_CONTENT,SCHEMA_META,(mode, contentType, nodeType, node)->{
				if (mode == NodeEnterMode.ENTER) {
					// TODO:
				}
				return ContinueMode.SKIP_CHILDREN;
			});
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SCHEMA_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect("%",ANY_CONTENT,SCHEMA_META,(mode, contentType, nodeType, node)->{
				if (mode == NodeEnterMode.ENTER) {
					// TODO:
				}
				return ContinueMode.SKIP_CHILDREN;
			});
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SCHEMA_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getCatalogs() throws SQLException {
		if (!isCatalogsSupported()) {
			return new NullReadOnlyResultSet(new AbstractResultSetMetaData(CATALOG_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY);
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect("%",ANY_CONTENT,CATALOG_META,(mode, contentType, nodeType, node)->{
				if (mode == NodeEnterMode.ENTER) {
					// TODO:
				}
				return ContinueMode.SKIP_CHILDREN;
			});
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(CATALOG_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getTableTypes() throws SQLException {
		return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(TABLE_TYPES_META,true) {
			@Override public String getTableName(int column) throws SQLException {return null;}
			@Override public String getSchemaName(int column) throws SQLException {return null;}
			@Override public String getCatalogName(int column) throws SQLException {return null;}
		},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(TABLE_TYPES_RS));
	}

	@Override
	public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,TABLES_CONTENT,COLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(COLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,TABLES_CONTENT,COLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(COLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,TABLES_CONTENT,COLUMN_PRIVILEGES_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(COLUMN_PRIVILEGES_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,TABLES_CONTENT,COLUMN_PRIVILEGES_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(COLUMN_PRIVILEGES_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)
				,TABLES_CONTENT,TABLE_PRIVILEGES_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(TABLE_PRIVILEGES_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)
				,TABLES_CONTENT,TABLE_PRIVILEGES_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(TABLE_PRIVILEGES_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/version/%"
				,TABLES_CONTENT,BEST_ROW_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(BEST_ROW_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/version/%"
				,TABLES_CONTENT,BEST_ROW_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(BEST_ROW_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/version/%"
				,TABLES_CONTENT,VERSION_COLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(VERSION_COLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/version/%"
				,TABLES_CONTENT,VERSION_COLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(VERSION_COLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/constrains/primarykey"
				,TABLES_CONTENT,PRIMARY_KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PRIMARY_KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/constrains/primarykey"
				,TABLES_CONTENT,PRIMARY_KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PRIMARY_KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/constrains/primarykey"
				,TABLES_CONTENT,KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/constrains/primarykey"
				,TABLES_CONTENT,KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/constrains/primarykey"
				,TABLES_CONTENT,KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/constrains/primarykey"
				,TABLES_CONTENT,KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((parentCatalog == null ? "%" : parentCatalog)+'/'+(parentSchema == null ? "%" : parentSchema)+'/'+(parentTable == null ? "%" : parentTable)+"/constrains/primarykey"
				,TABLES_CONTENT,KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((parentSchema == null ? "%" : parentSchema)+'/'+(parentTable == null ? "%" : parentTable)+"/constrains/primarykey"
				,TABLES_CONTENT,KEYS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(KEYS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getTypeInfo() throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect("%/%/%",TYPES_CONTENT,TYPE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(TYPE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect("%/%",TYPES_CONTENT,TYPE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(TYPE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/indices/%"
				,TABLES_CONTENT,INDEX_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(INDEX_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schema == null ? "%" : schema)+'/'+(table == null ? "%" : table)+"/indices/%"
				,TABLES_CONTENT,INDEX_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(INDEX_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(typeNamePattern == null ? "%" : typeNamePattern)
				,convertTypes(types),UDT_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(UDT_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(typeNamePattern == null ? "%" : typeNamePattern)
				,convertTypes(types),UDT_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(UDT_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return conn;
	}

	@Override
	public ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(typeNamePattern == null ? "%" : typeNamePattern)+"/indices/%"
				,TABLES_CONTENT,SUPERTYPE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SUPERTYPE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(typeNamePattern == null ? "%" : typeNamePattern)+"/indices/%"
				,TABLES_CONTENT,SUPERTYPE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SUPERTYPE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)+"/indices/%"
				,TABLES_CONTENT,SUPERTABLE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SUPERTABLE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)+"/indices/%"
				,TABLES_CONTENT,SUPERTABLE_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SUPERTABLE_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(typeNamePattern == null ? "%" : typeNamePattern)+"/indices/%"
				,TABLES_CONTENT,ATTRIBUTES_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(ATTRIBUTES_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(typeNamePattern == null ? "%" : typeNamePattern)+"/indices/%"
				,TABLES_CONTENT,ATTRIBUTES_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(ATTRIBUTES_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public int getDatabaseMajorVersion() throws SQLException {
		return driver.getMajorVersion();
	}

	@Override
	public int getDatabaseMinorVersion() throws SQLException {
		return driver.getMinorVersion();
	}

	@Override
	public int getJDBCMajorVersion() throws SQLException {
		return 4;
	}

	@Override
	public int getJDBCMinorVersion() throws SQLException {
		return 2;
	}

	@Override
	public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern),ANY_CONTENT,SCHEMA_META,(mode, contentType, nodeType, node)->{
				if (mode == NodeEnterMode.ENTER) {
					// TODO:
				}
				return ContinueMode.SKIP_CHILDREN;
			});
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SCHEMA_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern),ANY_CONTENT,SCHEMA_META,(mode, contentType, nodeType, node)->{
				if (mode == NodeEnterMode.ENTER) {
					// TODO:
				}
				return ContinueMode.SKIP_CHILDREN;
			});
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(SCHEMA_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getClientInfoProperties() throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect("%/%/%",TYPES_CONTENT,CLIENT_INFO_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(CLIENT_INFO_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect("%/%",TYPES_CONTENT,CLIENT_INFO_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(CLIENT_INFO_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(functionNamePattern == null ? "%" : functionNamePattern)
				,FUNCTIONS_CONTENT,FUNCTION_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(FUNCTION_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(functionNamePattern == null ? "%" : functionNamePattern)
				,FUNCTIONS_CONTENT,FUNCTION_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(FUNCTION_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(functionNamePattern == null ? "%" : functionNamePattern)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,PROCEDURES_CONTENT,FUNCTION_COLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(FUNCTION_COLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(functionNamePattern == null ? "%" : functionNamePattern)+'/'+(columnNamePattern == null ? "%" : columnNamePattern)
				,PROCEDURES_CONTENT,FUNCTION_COLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(FUNCTION_COLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}

	@Override
	public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
		if (!isCatalogsSupported()) {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((catalog == null ? "%" : catalog)+'/'+(schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)+"/indices/%"
				,TABLES_CONTENT,PSEUDOCOLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PSEUDOCOLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
		else {
			final List<Object[]>	result = new ArrayList<>();
			
			collect((schemaPattern == null ? "%" : schemaPattern)+'/'+(tableNamePattern == null ? "%" : tableNamePattern)+"/indices/%"
				,TABLES_CONTENT,PSEUDOCOLUMNS_META,(mode, contentType, nodeType, node)->{
					if (mode == NodeEnterMode.ENTER) {
						// TODO:
					}
					return ContinueMode.SKIP_CHILDREN;
				}
			);
			return new InMemoryReadOnlyResultSet(new AbstractResultSetMetaData(PSEUDOCOLUMNS_META,true) {
				@Override public String getTableName(int column) throws SQLException {return null;}
				@Override public String getSchemaName(int column) throws SQLException {return null;}
				@Override public String getCatalogName(int column) throws SQLException {return null;}
			},ResultSet.TYPE_FORWARD_ONLY, new ArrayContent(result.toArray()));
		}
	}


//	private static int[] convertTypes(final String[] types) {
//		return null;
//	}

	private static String[] convertTypes(final int[] types) {
		return null;
	}
}
