/*package chav1961.purelib.sql.util;

import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.XMLBasedParser;
import chav1961.purelib.basic.XMLBasedParserLex;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.sql.interfaces.QueryExecutor;

class SimpleQueryExecutor implements QueryExecutor {
	static final SyntaxTreeInterface<LexDesc>	RESERVED_WORDS = new AndOrTree<>();
	static SimpleQueryParser					PARSER = null;
	
	enum LexType {
		LEX_OPEN_BRACKET, LEX_CLOSE_BRACKET, LEX_OPEN_A_BRACKET, LEX_CLOSE_A_BRACKET,
		LEX_DOT, LEX_COLON, LEX_SEMICOLON, LEX_DIV, 
		LEX_OPERATOR, LEX_STRING_VALUE, LEX_NUMBER_VALUE, LEX_BOOLEAN_VALUE, LEX_FIELD_NAME, LEX_TYPE_NAME, LEX_FUNCTION_NAME, LEX_GROUP_FUNCTION_NAME,
		LEX_EOD,

		LEX_ADD, LEX_ALL, LEX_ALLOCATE, LEX_ALTER, LEX_ANY, LEX_ARE, LEX_AS, LEX_ASC, LEX_ASSERTION, LEX_AT, LEX_AUTHORIZATION,
		LEX_BEGIN, LEX_BOTH, LEX_BY,
		LEX_CALL, LEX_CASCADE, LEX_CASCADED, LEX_CASE, LEX_CHECK, LEX_CLOSE, LEX_COALESCE, LEX_COLLATE, LEX_COLLATION, LEX_COLUMN, LEX_COMMIT,
		LEX_CONNECT, LEX_CONNECTION, LEX_CONSTRAINT, LEX_CONSTRAINTS, LEX_CONTINUE, LEX_CORRESPONDING, LEX_CREATE, LEX_CURRENT, LEX_CURSOR,
		LEX_DEALLOCATE, LEX_DECLARE, LEX_DEFAULT, LEX_DEFERRABLE, LEX_DEFERRED, LEX_DELETE, LEX_DESC, LEX_DESCRIBE, LEX_DIAGNOSTICS,
		LEX_DISCONNECT, LEX_DISTINCT, LEX_DROP,
		LEX_ELSE, LEX_END, LEX_END_EXEC, LEX_ESCAPE, LEX_EXCEPT, LEX_EXCEPTION, LEX_EXEC, LEX_EXISTS, LEX_EXPLAIN, LEX_EXTERNAL,
		LEX_FALSE, LEX_FETCH, LEX_FIRST, LEX_FOR, LEX_FOREIGN, LEX_FOUND, LEX_FROM, LEX_FULL, LEX_FUNCTION,
		LEX_GET, LEX_GLOBAL, LEX_GO, LEX_GOTO, LEX_GRANT, LEX_GROUP,
		LEX_HAVING,
		LEX_IDENTITY, LEX_IMMEDIATE, LEX_INDICATOR, LEX_INITIALLY, LEX_INNER, LEX_INOUT, LEX_INPUT, LEX_INSENSITIVE, LEX_INSERT,
		LEX_INTERSECT, LEX_INTO, LEX_IS, LEX_ISOLATION,
		LEX_JOIN,
		LEX_KEY, LEX_LAST, LEX_LEFT,
		LEX_NATIONAL, LEX_NATURAL, LEX_NEXT, LEX_NO, LEX_NULL, 
		LEX_OF, LEX_ON, LEX_ONLY, LEX_OPEN, LEX_OPTION, LEX_ORDER, LEX_OUTER, LEX_OUTPUT, LEX_OVERLAPS, 
		LEX_PARTIAL, LEX_PREPARE, LEX_PRESERVE, LEX_PRIMARY, LEX_PRIOR, LEX_PRIVILEGES, LEX_PROCEDURE, LEX_PUBLIC, 
		LEX_READ, LEX_REFERENCES, LEX_RELATIVE, LEX_RESTRICT, LEX_REVOKE, LEX_RIGHT, LEX_ROLLBACK, LEX_ROWS, 
		LEX_SCHEMA, LEX_SCROLL, LEX_SELECT, LEX_SET, LEX_SOME, 
		LEX_TABLE, LEX_TEMPORARY, LEX_TO, LEX_TRANSACTION, LEX_TRANSLATE, LEX_TRANSLATION,
		LEX_UNION, LEX_UNIQUE, LEX_UNKNOWN, LEX_UPDATE, LEX_USER, LEX_USING, 
		LEX_VALUES, LEX_VARYING, LEX_VIEW,
		LEX_WHENEVER, LEX_WHERE, LEX_WITH, LEX_WORK, LEX_WRITE, 
		LEX_XML, LEX_XMLEXISTS, LEX_XMLPARSE, LEX_XMLQUERY, LEX_XMLSERIALIZE,
		LEX_YEAR
	}

	enum LexSubtype {
		OPER_ADD, OPER_SUB, OPER_MUL, OPER_DIV, OPER_MOD, OPER_CAT,
		OPER_LT, OPER_LE, OPER_GT, OPER_GE, OPER_EQ, OPER_NE, OPER_IN, OPER_LIKE,
		OPER_NOT, OPER_AND, OPER_OR,
		BOOLEAN_FALSE, BOOLEAN_TRUE,  
		OPER_BETWEEN,
		GROUP_FUNC_AVG, GROUP_FUNC_COUNT, GROUP_FUNC_MAX, GROUP_FUNC_MIN, GROUP_FUNC_SUM,
		FUNC_CAST, FUNC_CONVERT, FUNC_CURRENT_DATE, FUNC_CURRENT_TIME, FUNC_CURRENT_TIMESTAMP, FUNC_CURRENT_USER,
		FUNC_GETCURRENTCONNECTION, FUNC_HOUR, FUNC_LOWER, FUNC_LTRIM, FUNC_MATCH, FUNC_MINUTE,
		FUNC_NULLIF, FUNC_PAD, FUNC_RTRIM, FUNC_SECOND, FUNC_SESSION_USER, FUNC_SPACE,
		FUNC_SQL, FUNC_SQLCODE, FUNC_SQLERROR, FUNC_SQLSTATE, FUNC_SUBSTR, FUNC_SYSTEM_USER,
		FUNC_TIMEZONE_HOUR, FUNC_TIMEZONE_MINUTE, FUNC_TRIM, FUNC_UPPER, FUNC_YEAR, 
 		TYPE_BIGINT, TYPE_BIT, TYPE_BOOLEAN, TYPE_CHAR, TYPE_DECIMAL, TYPE_DOUBLE, TYPE_FLOAT, TYPE_INT,
 		TYPE_NCHAR, TYPE_NVARCHAR, TYPE_NUMERIC, TYPE_REAL, TYPE_SMALLINT, TYPE_VARCHAR, 
	}
	
	enum Action {
		
	}
	
	static {
		try{PARSER = new SimpleQueryParser(SimpleQueryExecutor.class.getResourceAsStream("sqlsubset.xml"));
		} catch (SyntaxException e) {
			e.printStackTrace();
		}
		
		RESERVED_WORDS.placeName("ADD",new LexDesc(LexType.LEX_ADD));
		RESERVED_WORDS.placeName("ALL",new LexDesc(LexType.LEX_ALL));
		RESERVED_WORDS.placeName("ALLOCATE",new LexDesc(LexType.LEX_ALLOCATE));
		RESERVED_WORDS.placeName("ALTER",new LexDesc(LexType.LEX_ALTER));
		RESERVED_WORDS.placeName("AND",new LexDesc(LexType.LEX_OPERATOR,LexSubtype.OPER_AND));
		RESERVED_WORDS.placeName("ANY",new LexDesc(LexType.LEX_ANY));
		RESERVED_WORDS.placeName("ARE",new LexDesc(LexType.LEX_ARE));
		RESERVED_WORDS.placeName("AS",new LexDesc(LexType.LEX_AS));
		RESERVED_WORDS.placeName("ASC",new LexDesc(LexType.LEX_ASC));
		RESERVED_WORDS.placeName("ASSERTION",new LexDesc(LexType.LEX_ASSERTION));
		RESERVED_WORDS.placeName("AT",new LexDesc(LexType.LEX_AT));
		RESERVED_WORDS.placeName("AUTHORIZATION",new LexDesc(LexType.LEX_AUTHORIZATION));
		RESERVED_WORDS.placeName("AVG",new LexDesc(LexType.LEX_GROUP_FUNCTION_NAME,LexSubtype.GROUP_FUNC_AVG));
		RESERVED_WORDS.placeName("BEGIN",new LexDesc(LexType.LEX_BEGIN));
		RESERVED_WORDS.placeName("BETWEEN",new LexDesc(LexType.LEX_OPERATOR,LexSubtype.OPER_BETWEEN));
		RESERVED_WORDS.placeName("BIGINT",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_BIGINT));
		RESERVED_WORDS.placeName("BIT",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_BIT));
		RESERVED_WORDS.placeName("BOOLEAN",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_BOOLEAN));
		RESERVED_WORDS.placeName("BOTH",new LexDesc(LexType.LEX_BOTH));
		RESERVED_WORDS.placeName("BY",new LexDesc(LexType.LEX_BY));
		RESERVED_WORDS.placeName("CALL",new LexDesc(LexType.LEX_CALL));
		RESERVED_WORDS.placeName("CASCADE",new LexDesc(LexType.LEX_CASCADE));
		RESERVED_WORDS.placeName("CASCADED",new LexDesc(LexType.LEX_CASCADED));
		RESERVED_WORDS.placeName("CASE",new LexDesc(LexType.LEX_CASE));
		RESERVED_WORDS.placeName("CAST",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_CAST));
		RESERVED_WORDS.placeName("CHAR",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_CHAR));
		RESERVED_WORDS.placeName("CHARACTER",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_CHAR));
		RESERVED_WORDS.placeName("CHECK",new LexDesc(LexType.LEX_CHECK));
		RESERVED_WORDS.placeName("CLOSE",new LexDesc(LexType.LEX_CLOSE));
		RESERVED_WORDS.placeName("COALESCE",new LexDesc(LexType.LEX_COALESCE));
		RESERVED_WORDS.placeName("COLLATE",new LexDesc(LexType.LEX_COLLATE));
		RESERVED_WORDS.placeName("COLLATION",new LexDesc(LexType.LEX_COLLATION));
		RESERVED_WORDS.placeName("COLUMN",new LexDesc(LexType.LEX_COLUMN));
		RESERVED_WORDS.placeName("COMMIT",new LexDesc(LexType.LEX_COMMIT));
		RESERVED_WORDS.placeName("CONNECT",new LexDesc(LexType.LEX_CONNECT));
		RESERVED_WORDS.placeName("CONNECTION",new LexDesc(LexType.LEX_CONNECTION));
		RESERVED_WORDS.placeName("CONSTRAINT",new LexDesc(LexType.LEX_CONSTRAINT));
		RESERVED_WORDS.placeName("CONSTRAINTS",new LexDesc(LexType.LEX_CONSTRAINTS));
		RESERVED_WORDS.placeName("CONTINUE",new LexDesc(LexType.LEX_CONTINUE));
		RESERVED_WORDS.placeName("CONVERT",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_CONVERT));
		RESERVED_WORDS.placeName("CORRESPONDING",new LexDesc(LexType.LEX_CORRESPONDING));
		RESERVED_WORDS.placeName("COUNT",new LexDesc(LexType.LEX_GROUP_FUNCTION_NAME,LexSubtype.GROUP_FUNC_COUNT));
		RESERVED_WORDS.placeName("CREATE",new LexDesc(LexType.LEX_CREATE));
		RESERVED_WORDS.placeName("CURRENT",new LexDesc(LexType.LEX_CURRENT));
		RESERVED_WORDS.placeName("CURRENT_DATE",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_CURRENT_DATE));
		RESERVED_WORDS.placeName("CURRENT_TIME",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_CURRENT_TIME));
		RESERVED_WORDS.placeName("CURRENT_TIMESTAMP",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_CURRENT_TIMESTAMP));
		RESERVED_WORDS.placeName("CURRENT_USER",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_CURRENT_USER));
		RESERVED_WORDS.placeName("CURSOR",new LexDesc(LexType.LEX_CURSOR));
		RESERVED_WORDS.placeName("DEALLOCATE",new LexDesc(LexType.LEX_DEALLOCATE));
		RESERVED_WORDS.placeName("DEC",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_DECIMAL));
		RESERVED_WORDS.placeName("DECIMAL",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_DECIMAL));
		RESERVED_WORDS.placeName("DECLARE",new LexDesc(LexType.LEX_DECLARE));
		RESERVED_WORDS.placeName("DEFAULT",new LexDesc(LexType.LEX_DEFAULT));
		RESERVED_WORDS.placeName("DEFERRABLE",new LexDesc(LexType.LEX_DEFERRABLE));
		RESERVED_WORDS.placeName("DEFERRED",new LexDesc(LexType.LEX_DEFERRED));
		RESERVED_WORDS.placeName("DELETE",new LexDesc(LexType.LEX_DELETE));
		RESERVED_WORDS.placeName("DESC",new LexDesc(LexType.LEX_DESC));
		RESERVED_WORDS.placeName("DESCRIBE",new LexDesc(LexType.LEX_DESCRIBE));
		RESERVED_WORDS.placeName("DIAGNOSTICS",new LexDesc(LexType.LEX_DIAGNOSTICS));
		RESERVED_WORDS.placeName("DISCONNECT",new LexDesc(LexType.LEX_DISCONNECT));
		RESERVED_WORDS.placeName("DISTINCT",new LexDesc(LexType.LEX_DISTINCT));
		RESERVED_WORDS.placeName("DOUBLE",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_DOUBLE));
		RESERVED_WORDS.placeName("DROP",new LexDesc(LexType.LEX_DROP));
		RESERVED_WORDS.placeName("ELSE",new LexDesc(LexType.LEX_ELSE));
		RESERVED_WORDS.placeName("END",new LexDesc(LexType.LEX_END));
		RESERVED_WORDS.placeName("END-EXEC",new LexDesc(LexType.LEX_END_EXEC));
		RESERVED_WORDS.placeName("ESCAPE",new LexDesc(LexType.LEX_ESCAPE));
		RESERVED_WORDS.placeName("EXCEPT",new LexDesc(LexType.LEX_EXCEPT));
		RESERVED_WORDS.placeName("EXCEPTION",new LexDesc(LexType.LEX_EXCEPTION));
		RESERVED_WORDS.placeName("EXEC",new LexDesc(LexType.LEX_EXEC));
		RESERVED_WORDS.placeName("EXECUTE",new LexDesc(LexType.LEX_EXEC));
		RESERVED_WORDS.placeName("EXISTS",new LexDesc(LexType.LEX_EXISTS));
		RESERVED_WORDS.placeName("EXPLAIN",new LexDesc(LexType.LEX_EXPLAIN));
		RESERVED_WORDS.placeName("EXTERNAL",new LexDesc(LexType.LEX_EXTERNAL));
		RESERVED_WORDS.placeName("FALSE",new LexDesc(LexType.LEX_BOOLEAN_VALUE,LexSubtype.BOOLEAN_FALSE));
		RESERVED_WORDS.placeName("FETCH",new LexDesc(LexType.LEX_FETCH));
		RESERVED_WORDS.placeName("FIRST",new LexDesc(LexType.LEX_FIRST));
		RESERVED_WORDS.placeName("FLOAT",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_FLOAT));
		RESERVED_WORDS.placeName("FOR",new LexDesc(LexType.LEX_FOR));
		RESERVED_WORDS.placeName("FOREIGN",new LexDesc(LexType.LEX_FOREIGN));
		RESERVED_WORDS.placeName("FOUND",new LexDesc(LexType.LEX_FOUND));
		RESERVED_WORDS.placeName("FROM",new LexDesc(LexType.LEX_FROM));
		RESERVED_WORDS.placeName("FULL",new LexDesc(LexType.LEX_FULL));
		RESERVED_WORDS.placeName("FUNCTION",new LexDesc(LexType.LEX_FUNCTION));
		RESERVED_WORDS.placeName("GET",new LexDesc(LexType.LEX_GET));
		RESERVED_WORDS.placeName("GETCURRENTCONNECTION",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_GETCURRENTCONNECTION));
		RESERVED_WORDS.placeName("GLOBAL",new LexDesc(LexType.LEX_GLOBAL));
		RESERVED_WORDS.placeName("GO",new LexDesc(LexType.LEX_GO));
		RESERVED_WORDS.placeName("GOTO",new LexDesc(LexType.LEX_GOTO));
		RESERVED_WORDS.placeName("GRANT",new LexDesc(LexType.LEX_GRANT));
		RESERVED_WORDS.placeName("GROUP",new LexDesc(LexType.LEX_GROUP));
		RESERVED_WORDS.placeName("HAVING",new LexDesc(LexType.LEX_HAVING));
		RESERVED_WORDS.placeName("HOUR",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_HOUR));
		RESERVED_WORDS.placeName("IDENTITY",new LexDesc(LexType.LEX_IDENTITY));
		RESERVED_WORDS.placeName("IMMEDIATE",new LexDesc(LexType.LEX_IMMEDIATE));
		RESERVED_WORDS.placeName("IN",new LexDesc(LexType.LEX_OPERATOR,LexSubtype.OPER_IN));
		RESERVED_WORDS.placeName("INDICATOR",new LexDesc(LexType.LEX_INDICATOR));
		RESERVED_WORDS.placeName("INITIALLY",new LexDesc(LexType.LEX_INITIALLY));
		RESERVED_WORDS.placeName("INNER",new LexDesc(LexType.LEX_INNER));
		RESERVED_WORDS.placeName("INOUT",new LexDesc(LexType.LEX_INOUT));
		RESERVED_WORDS.placeName("INPUT",new LexDesc(LexType.LEX_INPUT));
		RESERVED_WORDS.placeName("INSENSITIVE",new LexDesc(LexType.LEX_INSENSITIVE));
		RESERVED_WORDS.placeName("INSERT",new LexDesc(LexType.LEX_INSERT));
		RESERVED_WORDS.placeName("INT",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_INT));
		RESERVED_WORDS.placeName("INTEGER",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_INT));
		RESERVED_WORDS.placeName("INTERSECT",new LexDesc(LexType.LEX_INTERSECT));
		RESERVED_WORDS.placeName("INTO",new LexDesc(LexType.LEX_INTO));
		RESERVED_WORDS.placeName("IS",new LexDesc(LexType.LEX_IS));
		RESERVED_WORDS.placeName("ISOLATION",new LexDesc(LexType.LEX_ISOLATION));
		RESERVED_WORDS.placeName("JOIN",new LexDesc(LexType.LEX_JOIN));
		RESERVED_WORDS.placeName("KEY",new LexDesc(LexType.LEX_KEY));
		RESERVED_WORDS.placeName("LAST",new LexDesc(LexType.LEX_LAST));
		RESERVED_WORDS.placeName("LEFT",new LexDesc(LexType.LEX_LEFT));
		RESERVED_WORDS.placeName("LIKE",new LexDesc(LexType.LEX_OPERATOR,LexSubtype.OPER_LIKE));
		RESERVED_WORDS.placeName("LOWER",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_LOWER));
		RESERVED_WORDS.placeName("LTRIM",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_LTRIM));
		RESERVED_WORDS.placeName("MATCH",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_MATCH));
		RESERVED_WORDS.placeName("MAX",new LexDesc(LexType.LEX_GROUP_FUNCTION_NAME,LexSubtype.GROUP_FUNC_MAX));
		RESERVED_WORDS.placeName("MIN",new LexDesc(LexType.LEX_GROUP_FUNCTION_NAME,LexSubtype.GROUP_FUNC_MIN));
		RESERVED_WORDS.placeName("MINUTE",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_MINUTE));
		RESERVED_WORDS.placeName("NATIONAL",new LexDesc(LexType.LEX_NATIONAL));
		RESERVED_WORDS.placeName("NATURAL",new LexDesc(LexType.LEX_NATURAL));
		RESERVED_WORDS.placeName("NCHAR",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_NCHAR));
		RESERVED_WORDS.placeName("NVARCHAR",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_NVARCHAR));
		RESERVED_WORDS.placeName("NEXT",new LexDesc(LexType.LEX_NEXT));
		RESERVED_WORDS.placeName("NO",new LexDesc(LexType.LEX_NO));
		RESERVED_WORDS.placeName("NOT",new LexDesc(LexType.LEX_OPERATOR,LexSubtype.OPER_NOT));
		RESERVED_WORDS.placeName("NULL",new LexDesc(LexType.LEX_NULL));
		RESERVED_WORDS.placeName("NULLIF",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_NULLIF));
		RESERVED_WORDS.placeName("NUMERIC",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_NUMERIC));
		RESERVED_WORDS.placeName("OF",new LexDesc(LexType.LEX_OF));
		RESERVED_WORDS.placeName("ON",new LexDesc(LexType.LEX_ON));
		RESERVED_WORDS.placeName("ONLY",new LexDesc(LexType.LEX_ONLY));
		RESERVED_WORDS.placeName("OPEN",new LexDesc(LexType.LEX_OPEN));
		RESERVED_WORDS.placeName("OPTION",new LexDesc(LexType.LEX_OPTION));
		RESERVED_WORDS.placeName("OR",new LexDesc(LexType.LEX_OPERATOR,LexSubtype.OPER_OR));
		RESERVED_WORDS.placeName("ORDER",new LexDesc(LexType.LEX_ORDER));
		RESERVED_WORDS.placeName("OUTER",new LexDesc(LexType.LEX_OUTER));
		RESERVED_WORDS.placeName("OUTPUT",new LexDesc(LexType.LEX_OUTPUT));
		RESERVED_WORDS.placeName("OVERLAPS",new LexDesc(LexType.LEX_OVERLAPS));
		RESERVED_WORDS.placeName("PAD",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_PAD));
		RESERVED_WORDS.placeName("PARTIAL",new LexDesc(LexType.LEX_PARTIAL));
		RESERVED_WORDS.placeName("PREPARE",new LexDesc(LexType.LEX_PREPARE));
		RESERVED_WORDS.placeName("PRESERVE",new LexDesc(LexType.LEX_PRESERVE));
		RESERVED_WORDS.placeName("PRIMARY",new LexDesc(LexType.LEX_PRIMARY));
		RESERVED_WORDS.placeName("PRIOR",new LexDesc(LexType.LEX_PRIOR));
		RESERVED_WORDS.placeName("PRIVILEGES",new LexDesc(LexType.LEX_PRIVILEGES));
		RESERVED_WORDS.placeName("PROCEDURE",new LexDesc(LexType.LEX_PROCEDURE));
		RESERVED_WORDS.placeName("PUBLIC",new LexDesc(LexType.LEX_PUBLIC));
		RESERVED_WORDS.placeName("READ",new LexDesc(LexType.LEX_READ));
		RESERVED_WORDS.placeName("REAL",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_REAL));
		RESERVED_WORDS.placeName("REFERENCES",new LexDesc(LexType.LEX_REFERENCES));
		RESERVED_WORDS.placeName("RELATIVE",new LexDesc(LexType.LEX_RELATIVE));
		RESERVED_WORDS.placeName("RESTRICT",new LexDesc(LexType.LEX_RESTRICT));
		RESERVED_WORDS.placeName("REVOKE",new LexDesc(LexType.LEX_REVOKE));
		RESERVED_WORDS.placeName("RIGHT",new LexDesc(LexType.LEX_RIGHT));
		RESERVED_WORDS.placeName("ROLLBACK",new LexDesc(LexType.LEX_ROLLBACK));
		RESERVED_WORDS.placeName("ROWS",new LexDesc(LexType.LEX_ROWS));
		RESERVED_WORDS.placeName("RTRIM",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_RTRIM));
		RESERVED_WORDS.placeName("SCHEMA",new LexDesc(LexType.LEX_SCHEMA));
		RESERVED_WORDS.placeName("SCROLL",new LexDesc(LexType.LEX_SCROLL));
		RESERVED_WORDS.placeName("SECOND",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SECOND));
		RESERVED_WORDS.placeName("SELECT",new LexDesc(LexType.LEX_SELECT));
		RESERVED_WORDS.placeName("SESSION_USER",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SESSION_USER));
		RESERVED_WORDS.placeName("SET",new LexDesc(LexType.LEX_SET));
		RESERVED_WORDS.placeName("SMALLINT",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_SMALLINT));
		RESERVED_WORDS.placeName("SOME",new LexDesc(LexType.LEX_SOME));
		RESERVED_WORDS.placeName("SPACE",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SPACE));
		RESERVED_WORDS.placeName("SQL",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SQL));
		RESERVED_WORDS.placeName("SQLCODE",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SQLCODE));
		RESERVED_WORDS.placeName("SQLERROR",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SQLERROR));
		RESERVED_WORDS.placeName("SQLSTATE",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SQLSTATE));
		RESERVED_WORDS.placeName("SUBSTR",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SUBSTR));
		RESERVED_WORDS.placeName("SUBSTRING",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SUBSTR));
		RESERVED_WORDS.placeName("SUM",new LexDesc(LexType.LEX_GROUP_FUNCTION_NAME,LexSubtype.GROUP_FUNC_SUM));
		RESERVED_WORDS.placeName("SYSTEM_USER",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_SYSTEM_USER));
		RESERVED_WORDS.placeName("TABLE",new LexDesc(LexType.LEX_TABLE));
		RESERVED_WORDS.placeName("TEMPORARY",new LexDesc(LexType.LEX_TEMPORARY));
		RESERVED_WORDS.placeName("TIMEZONE_HOUR",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_TIMEZONE_HOUR));
		RESERVED_WORDS.placeName("TIMEZONE_MINUTE",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_TIMEZONE_MINUTE));
		RESERVED_WORDS.placeName("TO",new LexDesc(LexType.LEX_TO));
		RESERVED_WORDS.placeName("TRANSACTION",new LexDesc(LexType.LEX_TRANSACTION));
		RESERVED_WORDS.placeName("TRANSLATE",new LexDesc(LexType.LEX_TRANSLATE));
		RESERVED_WORDS.placeName("TRANSLATION",new LexDesc(LexType.LEX_TRANSLATION));
		RESERVED_WORDS.placeName("TRIM",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_TRIM));
		RESERVED_WORDS.placeName("TRUE",new LexDesc(LexType.LEX_BOOLEAN_VALUE,LexSubtype.BOOLEAN_TRUE));
		RESERVED_WORDS.placeName("UNION",new LexDesc(LexType.LEX_UNION));
		RESERVED_WORDS.placeName("UNIQUE",new LexDesc(LexType.LEX_UNIQUE));
		RESERVED_WORDS.placeName("UNKNOWN",new LexDesc(LexType.LEX_UNKNOWN));
		RESERVED_WORDS.placeName("UPDATE",new LexDesc(LexType.LEX_UPDATE));
		RESERVED_WORDS.placeName("UPPER",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_UPPER));
		RESERVED_WORDS.placeName("USER",new LexDesc(LexType.LEX_USER));
		RESERVED_WORDS.placeName("USING",new LexDesc(LexType.LEX_USING));
		RESERVED_WORDS.placeName("VALUES",new LexDesc(LexType.LEX_VALUES));
		RESERVED_WORDS.placeName("VARCHAR",new LexDesc(LexType.LEX_TYPE_NAME,LexSubtype.TYPE_VARCHAR));
		RESERVED_WORDS.placeName("VARYING",new LexDesc(LexType.LEX_VARYING));
		RESERVED_WORDS.placeName("VIEW",new LexDesc(LexType.LEX_VIEW));
		RESERVED_WORDS.placeName("WHENEVER",new LexDesc(LexType.LEX_WHENEVER));
		RESERVED_WORDS.placeName("WHERE",new LexDesc(LexType.LEX_WHERE));
		RESERVED_WORDS.placeName("WITH",new LexDesc(LexType.LEX_WITH));
		RESERVED_WORDS.placeName("WORK",new LexDesc(LexType.LEX_WORK));
		RESERVED_WORDS.placeName("WRITE",new LexDesc(LexType.LEX_WRITE));
		RESERVED_WORDS.placeName("XML",new LexDesc(LexType.LEX_XML));
		RESERVED_WORDS.placeName("XMLEXISTS",new LexDesc(LexType.LEX_XMLEXISTS));
		RESERVED_WORDS.placeName("XMLPARSE",new LexDesc(LexType.LEX_XMLPARSE));
		RESERVED_WORDS.placeName("XMLQUERY",new LexDesc(LexType.LEX_XMLQUERY));
		RESERVED_WORDS.placeName("XMLSERIALIZE",new LexDesc(LexType.LEX_XMLSERIALIZE));
		RESERVED_WORDS.placeName("YEAR",new LexDesc(LexType.LEX_FUNCTION_NAME,LexSubtype.FUNC_YEAR));
	}
	
	
	private SimpleQueryExecutor(final Object parsed) {
	}
	
	public static SimpleQueryExecutor parse(final char[] content, final FileSystemInterface fsi, final boolean hasParameters, final boolean isCallable) throws SQLException{
		final List<LexDesc>	list = new ArrayList<LexDesc>();
		final long[]		forNumber = new long[2];
		final int			maxLen = content.length, forBounds[] = new int[2];
		long				keywordId;
		int					pos = 0, line = 0, col = 0, oldPos;

		if (PARSER == null) {
			throw new SQLException("No parser detected");
		}
		
		try{while (pos < maxLen) {
				while (pos < maxLen && content[pos] <= ' ' && content[pos] != '\n') {
					pos++;	col++;
				}
				switch (content[pos]) {
					case '\n'	: 
						line++; 	col = 0;
						break;
					case '.' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_DOT));
						break;
					case ',' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_COLON));
						break;
					case ';' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_SEMICOLON));
						break;
					case ':' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_DIV));
						break;
					case '(' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPEN_BRACKET));
						break;
					case ')' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_CLOSE_BRACKET));
						break;
					case '[' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPEN_A_BRACKET));
						break;
					case ']' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_CLOSE_A_BRACKET));
						break;
					case '<' 	:
						if (pos < maxLen - 1 && content[pos+1] == '=') {
							list.add(new LexDesc(line,col,LexType.LEX_OPERATOR,LexSubtype.OPER_LE));
							col += 2;
						}
						else if (pos < maxLen - 1 && content[pos+1] == '>') {
							list.add(new LexDesc(line,col,LexType.LEX_OPERATOR,LexSubtype.OPER_NE));
							col += 2;
						}
						else {
							list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_LT));
						}
						break;
					case '>' 	:
						if (pos < maxLen - 1 && content[pos+1] == '=') {
							list.add(new LexDesc(line,col,LexType.LEX_OPERATOR,LexSubtype.OPER_GE));
							col += 2;
						}
						else {
							list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_GT));
						}
						break;
					case '=' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_EQ));
						break;
					case '!' 	:
						if (pos < maxLen - 1 && content[pos+1] == '=') {
							list.add(new LexDesc(line,col,LexType.LEX_OPERATOR,LexSubtype.OPER_NE));
							col += 2;
						}
						else {
							throw new SyntaxException(line,col,"Illegal lexema");
						}
						break;
					case '+' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_ADD));
						break;
					case '-' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_SUB));
						break;
					case '*' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_MUL));
						break;
					case '/' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_DIV));
						break;
					case '%' 	:
						list.add(new LexDesc(line,col++,LexType.LEX_OPERATOR,LexSubtype.OPER_MOD));
						break;
					case '|' 	:
						if (pos < maxLen - 1 && content[pos+1] == '|') {
							list.add(new LexDesc(line,col,LexType.LEX_OPERATOR,LexSubtype.OPER_CAT));
							col += 2;
						}
						else {
							throw new SyntaxException(line,col,"Illegal lexema");
						}
						break;
					case '\'' 	:
						pos = CharUtils.parseUnescapedString(content,(oldPos = pos) + 1,'\'',false,forBounds); 
						col += (pos-oldPos);
						break;
					case '\"' 	:
						pos = CharUtils.parseUnescapedString(content,(oldPos = pos) + 1,'\"',false,forBounds); 
						list.add(new LexDesc(line,col,LexType.LEX_FIELD_NAME,forBounds[0],forBounds[1]));
						col += (pos-oldPos);
						break;
					case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
						pos = CharUtils.parseNumber(content,oldPos = pos,forNumber,CharUtils.PREF_ANY,true);
						switch ((int)forNumber[0]) {
							case CharUtils.PREF_INT : case CharUtils.PREF_LONG :
								list.add(new LexDesc(line,col,LexType.LEX_NUMBER_VALUE,forNumber[1]));
								break;
							case CharUtils.PREF_FLOAT : case CharUtils.PREF_DOUBLE :
								list.add(new LexDesc(line,col,LexType.LEX_NUMBER_VALUE,Double.longBitsToDouble(forNumber[1])));
								break;
						}
						col += (pos-oldPos);
						break;
					default 	:
						if (Character.isJavaIdentifierStart(content[pos])) {
							pos = CharUtils.parseName(content,(oldPos = pos),forBounds);
							if ((keywordId = RESERVED_WORDS.seekName(content,forBounds[0],forBounds[1])) >= 0) {
								final LexDesc	template = RESERVED_WORDS.getCargo(keywordId); 
								
								list.add(new LexDesc(line,col,template.type,template.subtype));
							}
							else {
								list.add(new LexDesc(line,col,LexType.LEX_FIELD_NAME,forBounds[0],forBounds[1]));
							}
							col += (pos-oldPos);
						}
						else {
							throw new SyntaxException(line,col,"Illegal lexema");
						}
				}
			}
			list.add(new LexDesc(line,col,LexType.LEX_EOD));
			
			return new SimpleQueryExecutor(PARSER.parse(list.toArray(new LexDesc[list.size()])));
		} catch (SyntaxException exc) {
			throw new SQLException(exc.getLocalizedMessage(),exc);
		} finally {
			list.clear();
		}
	}

	@Override
	public int executeUpdate(final Object... parameters) throws SQLException {
		// TODO:
		return 0;
	}

	@Override
	public Object[][] executeQuery(final Object... parameters) throws SQLException {
		// TODO:
		return null;
	}
	
	@Override
	public ParameterMetaData getParmMetaData() throws SQLException {
		// TODO:
		return null;
	}
	
	@Override
	public ResultSetMetaData getRsMetaData() throws SQLException {
		// TODO:
		return null;
	}

	static class LexDesc extends XMLBasedParserLex<LexType,LexSubtype>{
		private int 	from, to;
		private long	longValue;
		private double	doubleValue;
		
		private LexDesc(final LexType type) {
			this(type,null);
		}
		
		private LexDesc(final LexType type, final LexSubtype subtype) {
			this(0,0,type,subtype);
		}
		
		public LexDesc(final int row, final int col, final LexType type) {
			this(row,col,type,null);
		}

		public LexDesc(final int row, final int col, final LexType type, final LexSubtype subtype) {
			super(row,col,type,subtype);
		}

		public LexDesc(final int row, final int col, final LexType type, final int from, final int to) {
			super(row,col,type,null);
			this.from = from;
			this.to = to;
		}

		public LexDesc(final int row, final int col, final LexType type, final long longValue) {
			super(row,col,type,LexSubtype.TYPE_BIGINT);
			this.longValue = longValue;
		}

		public LexDesc(final int row, final int col, final LexType type, final double doubleValue) {
			super(row,col,type,LexSubtype.TYPE_DOUBLE);
			this.doubleValue = doubleValue;
		}
	}
	
}
*/