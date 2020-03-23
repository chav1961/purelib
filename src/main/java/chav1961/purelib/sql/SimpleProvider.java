package chav1961.purelib.sql;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ModuleExporter;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.SimpleContentMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.ORMProvider;

public abstract class SimpleProvider<Record> implements ORMProvider<Record>, ModuleExporter {
	private static final int				SELECT_INDEX = 0;
	private static final int				INSERT_INDEX = 1;
	private static final int				UPDATE_INDEX = 2;
	private static final int				DELETE_INDEX = 3;
	
	protected final ContentNodeMetadata		tableModel;
	protected final ContentNodeMetadata		clazzModel;
	
	private final ORMProvider<Record>		parent;
	private final Connection				conn;
	private final String					selectString, insertString, updateString, deleteString, bulkSelectString, bulkCountString;
	private final GetterAndSetter[]			gas;
	private final int[][]					forSelect;		// length = 2: [0] - index in the 'gas' to call setter, [1] - SQL type of the field (see java.sql.Types)
	private final int[][]					forInsert;		// length = 3 : [0] - index in the 'gas' to call getter, [1] - parameter index in the prepared statement, [2] - SQL type of the parameter (see java.sql.Types) 
	private final int[][]					forUpdate;		// length = 3 : [0] - index in the 'gas' to call getter, [1] - parameter index in the prepared statement, [2] - SQL type of the parameter (see java.sql.Types)
	private final int[][]					forDelete;		// length = 3 : [0] - index in the 'gas' to call getter, [1] - parameter index in the prepared statement, [2] - SQL type of the parameter (see java.sql.Types)
	private final int[][]					forSelectKeys;	// length = 3 : [0] - index in the 'gas' to call getter, [1] - parameter index in the prepared statement, [2] - SQL type of the parameter (see java.sql.Types)
	private final int[][]					forUpdateKeys;	// length = 3 : [0] - index in the 'gas' to call getter, [1] - parameter index in the prepared statement, [2] - SQL type of the parameter (see java.sql.Types)
	private final PreparedStatement[]		hardCodedStatements;
	private final PreparedStatementCache	contentCache, contentCountCache;
	
	public SimpleProvider(final ContentNodeMetadata tableModel, final ContentNodeMetadata clazzModel, final Class<Record> clazz, final String[] fields, final String[] primaryKeys) throws IOException, ContentException {
		if (tableModel == null) {
			throw new NullPointerException("Table model metadata can't be null");
		} 
		else if (clazzModel == null) {
			throw new NullPointerException("Class model metadata can't be null");
		}
		else if (clazz == null) {
			throw new NullPointerException("Class description can't be null");
		}
		else if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("Fields list can't be null or empty array");
		}
		else if (primaryKeys == null) {
			throw new IllegalArgumentException("Primary keys list can't be null");
		}
		else {
			final Set<String>	fieldSet = new HashSet<>(), keySet = new HashSet<>();
			
			this.tableModel = tableModel;
			this.clazzModel = clazzModel;
			this.conn = null;
			this.parent = null;
			
			for (String item : fields) {
				fieldSet.add(item.toUpperCase());
			}
			for (String item : primaryKeys) {
				keySet.add(item.toUpperCase());
			}

			this.gas = buildGettersAndSetters(clazzModel,fieldSet);
			
			final String	tableName = URIUtils.extractSubURI(tableModel.getApplicationPath(),ContentMetadataInterface.APPLICATION_SCHEME).getPath().substring(1); 
			
			this.selectString = buildSelectString(tableName,fields,primaryKeys);
			this.forSelect = buildSelectNumbers(clazzModel,tableModel,fields,fieldSet);
			this.forSelectKeys = buildModifyNumbers(clazzModel,tableModel,primaryKeys,keySet);
			
			this.insertString = buildInsertString(tableName,fields,primaryKeys);
			this.forInsert = buildModifyNumbers(clazzModel,tableModel,fields,fieldSet);
			
			this.updateString = buildUpdateString(tableName,fields,primaryKeys);
			this.forUpdate = this.forInsert;
			this.forUpdateKeys = buildModifyNumbers(clazzModel,tableModel,primaryKeys,keySet);
			for (int[] item : this.forUpdateKeys) {	// Primary key indices must be shifted abroad the 'set' clause parameters of the 'update' statement
				item[1] += forUpdate.length;
			}
			
			this.deleteString = buildDeleteString(tableName,fields,primaryKeys);
			this.forDelete = buildModifyNumbers(clazzModel,tableModel,primaryKeys,keySet);
			
			this.bulkSelectString = buildBulkSelectString(tableName,fields);
			this.bulkCountString = buildBulkCountString(tableName,fields);
			this.hardCodedStatements = null;
			this.contentCache = this.contentCountCache = null;
		}
	}
	
	private SimpleProvider(final SimpleProvider<Record> parent, final Connection conn) throws SQLException {
		this.parent = parent;
		this.conn = conn;
		this.tableModel = parent.tableModel;
		this.clazzModel = parent.clazzModel;
		
		this.gas = null;
		this.forSelect = this.forInsert = this.forUpdate = this.forDelete = null;    
		this.forSelectKeys = this.forUpdateKeys = null;    
		this.selectString = null;
		this.insertString = null;
		this.updateString = null;
		this.deleteString = null;
		this.bulkSelectString = null;
		this.bulkCountString = null;
		this.hardCodedStatements = new PreparedStatement[]{
								conn.prepareStatement(parent.selectString),
								conn.prepareStatement(parent.insertString),
								conn.prepareStatement(parent.updateString),
								conn.prepareStatement(parent.deleteString)
							};
		this.contentCache = new PreparedStatementCache(conn, parent.bulkSelectString);
		this.contentCountCache = new PreparedStatementCache(conn, parent.bulkCountString);
	}

	@Override
	public Module getUnnamedModule() {
		if (gas.length > 0) {
			return gas[0].getClass().getClassLoader().getUnnamedModule();
		}
		else {
			return null;
		}
	}
	
	@Override
	public ORMProvider<Record> associate(final Connection conn) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (parent != null) {
			throw new IllegalStateException("Attempt to associate connection with already associated instance. Chain of associations is not supported!"); 
		}
		else {
			@SuppressWarnings("resource")
			final ORMProvider<Record>	owner = this; 
					
			return new SimpleProvider<Record>(this,conn){
				@Override public Record newRecord() throws SQLException {return owner.newRecord();}
				@Override public Record duplicateRecord(Record rec) throws SQLException {return owner.duplicateRecord(rec);}
			}; 
		}
	}

	@Override
	public void close() throws SQLException {
		if (this.hardCodedStatements != null) {
			contentCache.close();
			for (PreparedStatement item : this.hardCodedStatements) {
				item.close();
			}
		}
	}

	@Override
	public long contentSize() throws SQLException {
		testAssociated();
		return calculateCount("");
	}

	@Override
	public long contentSize(final String filter) throws SQLException {
		if (filter == null || filter.isEmpty()) {
			throw new IllegalArgumentException("Filter can't be null or empty");
		}
		else {
			testAssociated();
			return calculateCount(filter);
		}
	}

	@Override
	public void content(final Record item, final ContentIteratorCallback<Record> callback) throws SQLException {
		if (item == null) {
			throw new NullPointerException("Record item can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			testAssociated();
			iterate(item,callback,"","");
		}
	}

	@Override
	public void content(final Record item, final ContentIteratorCallback<Record> callback, final long from, final long count) throws SQLException {
		if (item == null) {
			throw new NullPointerException("Record item can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else if (from < 0) {
			throw new IllegalArgumentException("From index ["+from+"] can't be negative"); 
		}
		else if (count <= 0) {
			throw new IllegalArgumentException("Count ["+count+"] must be positive"); 
		}
		else {
			testAssociated();
			iterate(item,callback,"","",from,count);
		}
	}

	@Override
	public void content(final Record item, final ContentIteratorCallback<Record> callback, final String filter) throws SQLException {
		if (item == null) {
			throw new NullPointerException("Record item can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else if (filter == null || filter.isEmpty()) {
			throw new IllegalArgumentException("Filter can't be null or empty");
		}
		else {
			testAssociated();
			iterate(item,callback,filter,"");
		}
	}

	@Override
	public void content(final Record item, ContentIteratorCallback<Record> callback, final String filter, final long from, final long count) throws SQLException {
		if (item == null) {
			throw new NullPointerException("Record item can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else if (filter == null || filter.isEmpty()) {
			throw new IllegalArgumentException("Filter can't be null or empty");
		}
		else if (from < 0) {
			throw new IllegalArgumentException("From index ["+from+"] can't be negative"); 
		}
		else if (count <= 0) {
			throw new IllegalArgumentException("Count ["+count+"] must be positive"); 
		}
		else {
			testAssociated();
			iterate(item,callback,filter,"",from,count);
		}
	}

	@Override
	public void content(final Record item, final ContentIteratorCallback<Record> callback, final String filter, final String ordering) throws SQLException {
		if (item == null) {
			throw new NullPointerException("Record item can't be null"); 
		}
		else if (filter == null || filter.isEmpty()) {
			throw new IllegalArgumentException("Filter can't be null or empty");
		}
		else if (ordering == null || ordering.isEmpty()) {
			throw new IllegalArgumentException("Ordering can't be null or empty");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			testAssociated();
			iterate(item,callback,filter,ordering);
		}
	}

	@Override
	public void content(final Record item, final ContentIteratorCallback<Record> callback, final String filter, final String ordering, final long from, final long count) throws SQLException {
		if (item == null) {
			throw new NullPointerException("Record item can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else if (filter == null || filter.isEmpty()) {
			throw new IllegalArgumentException("Filter can't be null or empty");
		}
		else if (ordering == null || ordering.isEmpty()) {
			throw new IllegalArgumentException("Ordering can't be null or empty");
		}
		else if (from < 0) {
			throw new IllegalArgumentException("From index ["+from+"] can't be negative"); 
		}
		else if (count <= 0) {
			throw new IllegalArgumentException("Count ["+count+"] must be positive"); 
		}
		else {
			testAssociated();
			iterate(item,callback,filter,ordering,from,count);
		}
	}
	
	@Override
	public void create(final Record record) throws SQLException {
		try{upload(hardCodedStatements[INSERT_INDEX],record,getGAS(),getFields2Insert());
			if (hardCodedStatements[INSERT_INDEX].executeUpdate() != 1) {
				throw new SQLException("No any record was inserted");
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e); 
		}
	}

	@Override
	public void read(final Record record) throws SQLException {
		try{upload(hardCodedStatements[SELECT_INDEX],record,getGAS(),getFields2SelectKeys());
			
			try(final ResultSet	rs = hardCodedStatements[SELECT_INDEX].executeQuery()) {
				if (rs.next()) {
					download(rs,record,getGAS(),getFields2Select());
				}
				else {
					throw new SQLException("No any record was selected");
				}
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e); 
		}
	}

	@Override
	public void update(final Record record) throws SQLException {
		try{upload(hardCodedStatements[UPDATE_INDEX],record,getGAS(),getFields2Update());
			upload(hardCodedStatements[UPDATE_INDEX],record,getGAS(),getFields2UpdateKeys());
			if (hardCodedStatements[UPDATE_INDEX].executeUpdate() != 1) {
				throw new SQLException("No any record was updated");
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e); 
		}
	}

	@Override
	public void delete(final Record record) throws SQLException {
		try{upload(hardCodedStatements[DELETE_INDEX],record,getGAS(),getFields2Delete());
			if (hardCodedStatements[DELETE_INDEX].executeUpdate() != 1) {
				throw new SQLException("No any record was deleted");
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e); 
		}
	}

	@Override
	public abstract Record newRecord() throws SQLException;
	
	@Override
	public abstract Record duplicateRecord(final Record rec) throws SQLException;
	
	static String buildSelectString(final String table, final String[] fields, final String[] primaryKeys) {
		final StringBuilder	sb = new StringBuilder("select ");
		
		String	prefix = "";
		for (String item : fields) {
			sb.append(prefix).append(item);
			prefix = ",";
		}
		sb.append(" from ").append(table);
		prefix = " where ";
		for (String item : primaryKeys) {
			sb.append(prefix).append(item).append(" = ? ");
			prefix = " and ";
		}
		return sb.toString();
	}

	static String buildInsertString(final String table, final String[] fields, final String[] primaryKeys) {
		final StringBuilder	sb = new StringBuilder("insert into ");
		
		sb.append(table);
		
		char	prefix = '(';
		for (String item : fields) {
			sb.append(prefix).append(item);
			prefix = ',';
		}
		sb.append(") values ");
		prefix = '(';
		for (String item : fields) {
			sb.append(prefix).append('?');
			prefix = ',';
		}
		return sb.append(')').toString();
	}

	static String buildUpdateString(final String table, final String[] fields, final String[] primaryKeys) {
		final StringBuilder	sb = new StringBuilder("update ");
		
		sb.append(table);
		
		String	prefix = " set ";
		for (String item : fields) {
			sb.append(prefix).append(item).append(" = ? ");
			prefix = ",";
		}
		prefix = " where ";
		for (String item : primaryKeys) {
			sb.append(prefix).append(item).append(" = ? ");
			prefix = " and ";
		}
		return sb.toString();
	}

	static String buildDeleteString(final String table, final String[] fields, final String[] primaryKeys) {
		final StringBuilder	sb = new StringBuilder("delete from ");
		
		sb.append(table);
		
		String prefix = " where ";
		for (String item : primaryKeys) {
			sb.append(prefix).append(item).append(" = ? ");
			prefix = " and ";
		}
		return sb.toString();
	}

	static String buildBulkSelectString(final String table, final String[] fields) {
		final StringBuilder	sb = new StringBuilder("select ");
		
		String	prefix = "";
		for (String item : fields) {
			sb.append(prefix).append(item);
			prefix = ",";
		}
		sb.append(" from ").append(table);
		return sb.toString();
	}

	private String buildBulkCountString(final String table, final String[] fields) {
		return new StringBuilder("select count(*) as cnt from ").append(table).toString();
	}
	
	static void download(final ResultSet rs, final Object instance, final GetterAndSetter[] gas, final int[][] toAndType) throws ContentException, SQLException, IOException {
		for (int index = 0, maxIndex = toAndType.length; index < maxIndex; index++) {
			switch (toAndType[index][2]) {
				case Types.BIT : case Types.BOOLEAN :
					((BooleanGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getBoolean(index+1));
					break;
				case Types.SMALLINT	:
					((ByteGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getByte(index+1));
					break;
				case Types.TINYINT	:
					((ShortGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getShort(index+1));
					break;
				case Types.INTEGER	:
					((IntGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getInt(index+1));
					break;
				case Types.BIGINT	:
					((LongGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getLong(index+1));
					break;
				case Types.FLOAT	:
					((FloatGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getFloat(index+1));
					break;
				case Types.DOUBLE	:
					((DoubleGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getDouble(index+1));
					break;
				case Types.REAL		:
					if (gas[toAndType[index][0]] instanceof FloatGetterAndSetter) {
						((FloatGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getFloat(index+1));
					}
					else {
						((DoubleGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getDouble(index+1));
					}
					break;
				case Types.CHAR : case Types.VARCHAR : case Types.NCHAR : case Types.NVARCHAR : case Types.LONGVARCHAR : case Types.LONGNVARCHAR :
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getString(index+1));
					break;
				case Types.SQLXML	:
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,Utils.fromResource(rs.getSQLXML(index+1).getCharacterStream()));
					break;
				case Types.DECIMAL : case Types.NUMERIC :
					if (gas[toAndType[index][0]] instanceof FloatGetterAndSetter) {
						((FloatGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getFloat(index+1));
					}
					else if (gas[toAndType[index][0]] instanceof DoubleGetterAndSetter) {
						((DoubleGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getDouble(index+1));
					}
					else {
						((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getBigDecimal(index+1));
					}
					break;
				case Types.DATE		:
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getDate(index+1));
					break;
				case Types.TIME : case Types.TIME_WITH_TIMEZONE :
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getTime(index+1));
					break;
				case Types.TIMESTAMP : case Types.TIMESTAMP_WITH_TIMEZONE :
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getTimestamp(index+1));
					break;
				case Types.BLOB		:
					try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
						
						Utils.copyStream(rs.getBinaryStream(index+1),baos);
						((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,baos.toByteArray());
					}
					break;
				case Types.CLOB :
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,Utils.fromResource(rs.getCharacterStream(index+1)));
					break;
				case Types.NCLOB :
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,Utils.fromResource(rs.getCharacterStream(index+1)));
					break;
				case Types.BINARY : case Types.VARBINARY : case Types.LONGVARBINARY :
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getBytes(index+1));
					break;
				case Types.JAVA_OBJECT	:
					((ObjectGetterAndSetter)gas[toAndType[index][0]]).set(instance,rs.getObject(index+1));
					break;
//				case Types.ARRAY	:
//				case Types.DATALINK	:
//				case Types.DISTINCT	:
//				case Types.NULL		:
//				case Types.OTHER	:
//				case Types.REF		:
//				case Types.REF_CURSOR	:
//				case Types.ROWID	:
//				case Types.STRUCT	:
				default :
					throw new UnsupportedOperationException("SQL type ["+toAndType[index][1]+"] is not supported for downloading");
			}
		}
	}

	static void upload(final PreparedStatement ps, final Object instance, final GetterAndSetter[] gas, final int[][] fromToAndType) throws ContentException, SQLException, IOException {
		for (int index = 0, maxIndex = fromToAndType.length; index < maxIndex; index++) {
			switch (fromToAndType[index][2]) {
				case Types.BIT : case Types.BOOLEAN :
					ps.setBoolean(fromToAndType[index][1],((BooleanGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.SMALLINT	:
					ps.setByte(fromToAndType[index][1],((ByteGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.TINYINT	:
					ps.setShort(fromToAndType[index][1],((ShortGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.INTEGER	:
					ps.setInt(fromToAndType[index][1],((IntGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.BIGINT	:
					ps.setLong(fromToAndType[index][1],((LongGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.FLOAT	:
					ps.setFloat(fromToAndType[index][1],((FloatGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.DOUBLE	:
					ps.setDouble(fromToAndType[index][1],((DoubleGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.REAL		:
					if (gas[fromToAndType[index][0]] instanceof FloatGetterAndSetter) {
						ps.setFloat(fromToAndType[index][1],((FloatGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					else {
						ps.setDouble(fromToAndType[index][1],((DoubleGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					break;
				case Types.CHAR : case Types.VARCHAR : case Types.NCHAR : case Types.NVARCHAR : case Types.LONGVARCHAR : case Types.LONGNVARCHAR :
					ps.setString(fromToAndType[index][1],(String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.SQLXML	:
					ps.setCharacterStream(fromToAndType[index][1],new StringReader((String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.DECIMAL : case Types.NUMERIC :
					if (gas[fromToAndType[index][0]] instanceof FloatGetterAndSetter) {
						ps.setFloat(fromToAndType[index][1],((FloatGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					else if (gas[fromToAndType[index][0]] instanceof DoubleGetterAndSetter) {
						ps.setDouble(fromToAndType[index][1],((DoubleGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					else {
						ps.setBigDecimal(fromToAndType[index][1],(BigDecimal)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					break;
				case Types.DATE		:
					ps.setDate(fromToAndType[index][1],(Date)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.TIME : case Types.TIME_WITH_TIMEZONE :
					ps.setTime(fromToAndType[index][1],(Time)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.TIMESTAMP : case Types.TIMESTAMP_WITH_TIMEZONE :
					ps.setTimestamp(fromToAndType[index][1],(Timestamp)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.BLOB		:
					ps.setBinaryStream(fromToAndType[index][1],new ByteArrayInputStream((byte[])((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.CLOB :
					ps.setCharacterStream(fromToAndType[index][1],new StringReader((String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.NCLOB :
					ps.setCharacterStream(fromToAndType[index][1],new StringReader((String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.BINARY : case Types.VARBINARY : case Types.LONGVARBINARY :
					ps.setBytes(fromToAndType[index][1],(byte[])((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.JAVA_OBJECT	:
					ps.setObject(fromToAndType[index][1],((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
//				case Types.ARRAY	:
//				case Types.DATALINK	:
//				case Types.DISTINCT	:
//				case Types.NULL		:
//				case Types.OTHER	:
//				case Types.REF		:
//				case Types.REF_CURSOR	:
//				case Types.ROWID	:
//				case Types.STRUCT	:
				default :
					throw new UnsupportedOperationException("SQL type ["+fromToAndType[index][2]+"] is not supported for downloading");
			}
		}
	}
	
	static void uploadKeys(final ResultSet rs, final Object instance, final GetterAndSetter[] gas, final int[][] fromToAndType) throws ContentException, SQLException, IOException {
		for (int index = 0, maxIndex = fromToAndType.length; index < maxIndex; index++) {
			switch (fromToAndType[index][2]) {
				case Types.BIT : case Types.BOOLEAN :
					rs.updateBoolean(index+1,((BooleanGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.SMALLINT	:
					rs.updateByte(index+1,((ByteGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.TINYINT	:
					rs.updateShort(index+1,((ShortGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.INTEGER	:
					rs.updateInt(index+1,((IntGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.BIGINT	:
					rs.updateLong(index+1,((LongGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.FLOAT	:
					rs.updateFloat(index+1,((FloatGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.DOUBLE	:
					rs.updateDouble(index+1,((DoubleGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.REAL		:
					if (gas[fromToAndType[index][0]] instanceof FloatGetterAndSetter) {
						rs.updateFloat(index+1,((FloatGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					else {
						rs.updateDouble(index+1,((DoubleGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					break;
				case Types.CHAR : case Types.VARCHAR : case Types.NCHAR : case Types.NVARCHAR : case Types.LONGVARCHAR : case Types.LONGNVARCHAR :
					rs.updateString(index+1,(String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.SQLXML	:
					rs.updateCharacterStream(fromToAndType[index][1],new StringReader((String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.DECIMAL : case Types.NUMERIC :
					if (gas[fromToAndType[index][0]] instanceof FloatGetterAndSetter) {
						rs.updateFloat(index+1,((FloatGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					else if (gas[fromToAndType[index][0]] instanceof DoubleGetterAndSetter) {
						rs.updateDouble(index+1,((DoubleGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					else {
						rs.updateBigDecimal(index+1,(BigDecimal)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					}
					break;
				case Types.DATE		:
					rs.updateDate(index+1,(Date)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.TIME : case Types.TIME_WITH_TIMEZONE :
					rs.updateTime(index+1,(Time)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.TIMESTAMP : case Types.TIMESTAMP_WITH_TIMEZONE :
					rs.updateTimestamp(index+1,(Timestamp)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.BLOB		:
					rs.updateBinaryStream(fromToAndType[index][1],new ByteArrayInputStream((byte[])((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.CLOB :
					rs.updateCharacterStream(fromToAndType[index][1],new StringReader((String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.NCLOB :
					rs.updateCharacterStream(fromToAndType[index][1],new StringReader((String)((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance)));
					break;
				case Types.BINARY : case Types.VARBINARY : case Types.LONGVARBINARY :
					rs.updateBytes(index+1,(byte[])((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
				case Types.JAVA_OBJECT	:
					rs.updateObject(index+1,((ObjectGetterAndSetter)gas[fromToAndType[index][0]]).get(instance));
					break;
//				case Types.ARRAY	:
//				case Types.DATALINK	:
//				case Types.DISTINCT	:
//				case Types.NULL		:
//				case Types.OTHER	:
//				case Types.REF		:
//				case Types.REF_CURSOR	:
//				case Types.ROWID	:
//				case Types.STRUCT	:
				default :
					throw new UnsupportedOperationException("SQL type ["+fromToAndType[index][2]+"] is not supported for downloading");
			}
		}
	}

	private GetterAndSetter[] getGAS() {
		if (gas == null) {
			if (parent != null && (parent instanceof SimpleProvider)) {
				return ((SimpleProvider<?>)parent).getGAS();
			}
			else {
				throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
			}
		}
		else {
			return gas;
		}
	}

	private int[][] getFields2Insert() {
		if (forInsert == null) {
			if (parent != null && (parent instanceof SimpleProvider)) {
				return ((SimpleProvider<?>)parent).getFields2Insert();
			}
			else {
				throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
			}
		}
		else {
			return forInsert;
		}
	}

	private int[][] getFields2Update() {
		if (forUpdate == null) {
			if (parent != null && (parent instanceof SimpleProvider)) {
				return ((SimpleProvider<?>)parent).getFields2Update();
			}
			else {
				throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
			}
		}
		else {
			return forUpdate;
		}
	}

	private int[][] getFields2UpdateKeys() {
		if (forUpdateKeys == null) {
			if (parent != null && (parent instanceof SimpleProvider)) {
				return ((SimpleProvider<?>)parent).getFields2UpdateKeys();
			}
			else {
				throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
			}
		}
		else {
			return forUpdateKeys;
		}
	}
	
	private int[][] getFields2Delete() {
		if (forDelete == null) {
			if (parent != null && (parent instanceof SimpleProvider)) {
				return ((SimpleProvider<?>)parent).getFields2Delete();
			}
			else {
				throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
			}
		}
		else {
			return forDelete;
		}
	}

	private int[][] getFields2Select() {
		if (forSelect == null) {
			if (parent != null && (parent instanceof SimpleProvider)) {
				return ((SimpleProvider<?>)parent).getFields2Select();
			}
			else {
				throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
			}
		}
		else {
			return forSelect;
		}
	}

	private int[][] getFields2SelectKeys() {
		if (forSelectKeys == null) {
			if (parent != null && (parent instanceof SimpleProvider)) {
				return ((SimpleProvider<?>)parent).getFields2SelectKeys();
			}
			else {
				throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
			}
		}
		else {
			return forSelectKeys;
		}
	}
	
	private void iterate(final Record item, final ContentIteratorCallback<Record> callback, final String filter, final String ordering) throws SQLException {
		// TODO Auto-generated method stub
		final PreparedStatement	ps = contentCache.get(filter,ordering,false);
		
		try{upload(ps,item,getGAS(),new int[0][]);
		
			try(final ResultSet	rs = ps.executeQuery()) {
				long	count = 0;
				
				while (rs.next()) {
					download(rs,item,getGAS(),getFields2Select());
					if (callback.process(count,count,item) != ContinueMode.CONTINUE) {
						break;
					}
					count++;
				}
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e);
		}
	}

	private void iterate(final Record item, final ContentIteratorCallback<Record> callback, final String filter, final String ordering, final long offset, final long limit) throws SQLException {
		final PreparedStatement	ps = contentCache.get(filter,ordering,true);
		
		try{upload(ps,item,getGAS(),new int[0][]);
			ps.setLong(1,offset);
			ps.setLong(2,limit);
			try(final ResultSet	rs = ps.executeQuery()) {
				long	count = 0, from = offset;
				
				while (rs.next()) {
					download(rs,item,getGAS(),getFields2Select());
					if (callback.process(count,from,item) != ContinueMode.CONTINUE) {
						break;
					}
					count++;
					from++;
				}
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e);
		}
	}

	private long calculateCount(final String filter) throws SQLException {
		final PreparedStatement	ps = contentCountCache.get(filter,"",false);
		
		try(final ResultSet	rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			else {
				return 0;
			}
		}
	}
	
	static GetterAndSetter[] buildGettersAndSetters(final ContentNodeMetadata clazzModel, final Set<String> fieldSet) throws ContentException {
		final List<GetterAndSetter>	result = new ArrayList<>();
		final ContentException[]	errors = new ContentException[1]; 
		
		new SimpleContentMetadata(clazzModel).walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER && fieldSet.contains(node.getName().toUpperCase())) {
				try{result.add(GettersAndSettersFactory.buildGetterAndSetter(applicationPath));
				} catch (ContentException e) {
					errors[0] = e;
				}
			}
			return ContinueMode.CONTINUE;
		}, clazzModel.getUIPath());
		
		if (errors[0] != null) {
			throw errors[0]; 
		}
		else if (result.isEmpty()) {
			throw new ContentException("There are no any intersections with table field names and class field names. Nothing can't be used in ORM");
		}
		else {
			return result.toArray(new GetterAndSetter[result.size()]);
		}
	}

	static int[][] buildSelectNumbers(final ContentNodeMetadata clazzModel, final ContentNodeMetadata tableModel, final String[] fields, final Set<String> fieldSet) throws ContentException {
		final List<int[]>			result = new ArrayList<>(); 
		final int[]					indexNumber = new int[]{0};
		final ContentException[]	errors = new ContentException[] {null};
		
		new SimpleContentMetadata(clazzModel).walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER && fieldSet.contains(node.getName().toUpperCase())) {
				for (int index = 0; index < fields.length; index++) {
					if (node.getName().equalsIgnoreCase(fields[index])) {
						final int[]	desc = new int[3];
						
						desc[0] = indexNumber[0]++;
						desc[1] = index + 1;
						new SimpleContentMetadata(tableModel).walkDown((modeT,applicationPathT,uiPathT,nodeT)->{
							if (modeT == NodeEnterMode.ENTER && nodeT.getName().equalsIgnoreCase(node.getName())) {
								final Map<String,String[]>	parameters = URIUtils.parseQuery(URIUtils.extractQueryFromURI(applicationPathT));
								
								if (parameters.containsKey("type")) {
									desc[2] = Integer.valueOf(parameters.get("type")[0].toString());
								}
								else {
									errors[0] = new ContentException("Table field ["+nodeT.getApplicationPath()+"] doesn't contain type in it's query string");
								}
								return ContinueMode.STOP;
							}
							else {
								return ContinueMode.CONTINUE;
							}
						}, tableModel.getUIPath());
						
						result.add(desc);
						return ContinueMode.CONTINUE;
					}
				}
				throw new IllegalArgumentException("field list and field set mismatch");
			}
			return ContinueMode.CONTINUE;
		}, clazzModel.getUIPath());

		if (errors[0] != null) {
			throw errors[0];
		}
		else {
			return result.toArray(new int[result.size()][]);
		}
	}

	static int[][] buildModifyNumbers(final ContentNodeMetadata clazzModel, final ContentNodeMetadata tableModel, final String[] fields, final Set<String> fieldSet) throws ContentException {
		final List<int[]>			result = new ArrayList<>(); 
		final int[]					indexNumber = new int[]{0};
		final ContentException[]	errors = new ContentException[] {null};
		
		new SimpleContentMetadata(clazzModel).walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER && fieldSet.contains(node.getName().toUpperCase())) {
				for (int index = 0; index < fields.length; index++) {
					if (node.getName().equalsIgnoreCase(fields[index])) {
						final int[]	desc = new int[3];
						
						desc[0] = indexNumber[0]++;
						desc[1] = index + 1;
						new SimpleContentMetadata(tableModel).walkDown((modeT,applicationPathT,uiPathT,nodeT)->{
							if (modeT == NodeEnterMode.ENTER && nodeT.getName().equalsIgnoreCase(node.getName())) {
								final Map<String,String[]>	parameters = URIUtils.parseQuery(URIUtils.extractQueryFromURI(applicationPathT));
								
								if (parameters.containsKey("type")) {
									desc[2] = Integer.valueOf(parameters.get("type")[0].toString());
								}
								else {
									errors[0] = new ContentException("Table field ["+nodeT.getApplicationPath()+"] doesn't contain type in it's query string");
								}
								return ContinueMode.STOP;
							}
							else {
								return ContinueMode.CONTINUE;
							}
						}, tableModel.getUIPath());
						
						result.add(desc);
						return ContinueMode.CONTINUE;
					}
				}
				throw new IllegalArgumentException("field list and field set mismatch");
			}
			return ContinueMode.CONTINUE;
		}, clazzModel.getUIPath());
		
		if (errors[0] != null) {
			throw errors[0];
		}
		else {
			return result.toArray(new int[result.size()][]);
		}
	}

	private void testAssociated() {
		if (conn == null) {
			throw new IllegalStateException("Attempt to call method without associated connection. Call associate(...) and use returned value instead of direct call of this instance"); 
		}
	}
	
	static class PreparedStatementCache implements AutoCloseable {
		private final Map<String,Map<String,PreparedStatement[]>>	cache = new HashMap<>();
		private final Connection 	conn;
		private final String		selectTemplate;
		private final char[]		quoteId;
		
		public PreparedStatementCache(final Connection conn, final String selectTemplate) throws SQLException {
			this.conn = conn;
			this.selectTemplate = selectTemplate;
			this.quoteId = conn.getMetaData().getIdentifierQuoteString().toCharArray();
		}

		public PreparedStatement get(final String filter, final String ordering, final boolean offsetAndLimit) throws SQLException {
			final String	reducedFilter = reduce(conn,filter), reducedOrdering = reduce(conn,ordering);
			final PreparedStatement[]	ps;
			
			if (!cache.containsKey(reducedFilter)) {
				cache.put(reducedFilter,new HashMap<>());
			}
			if (!cache.get(reducedFilter).containsKey(reducedOrdering)) {
				cache.get(reducedFilter).put(reducedOrdering,new PreparedStatement[2]);
			}
			
			ps = cache.get(reducedFilter).get(reducedOrdering);
			if (offsetAndLimit) {
				if (ps[1] == null) {
					ps[1] = conn.prepareStatement(selectTemplate+(filter.isEmpty() ? "" : " where "+filter)+(ordering.isEmpty() ? "" : " order by "+ordering)+" offset ? limit ?");
				}
				return ps[1];
			}
			else {
				if (ps[0] == null) {
					ps[0] = conn.prepareStatement(selectTemplate+(filter.isEmpty() ? "" : " where "+filter)+(ordering.isEmpty() ? "" : " order by "+ordering));
				}
				return ps[0];
			}
		}

		@Override
		public void close() throws SQLException {
			boolean	wasExceptions = false;
		
			for (Entry<String, Map<String, PreparedStatement[]>> item : cache.entrySet()) {
				for (Entry<String, PreparedStatement[]> subitem : item.getValue().entrySet()) {
					for (PreparedStatement ps : subitem.getValue()) {
						if (ps != null) {
							try{ps.close();
							} catch (SQLException exc) {
								wasExceptions = true;
							}
						}
					}
				}
			}
			if (wasExceptions) {
				throw new SQLException("Some prepared statements had been thrown SQLException during close");
			}
		}

		String reduce(final Connection conn, final String content) throws SQLException {
			final char[]	source = content.toCharArray(), target = new char[source.length];
			int				targetIndex = 0;
			boolean			inString = false, inId = false;
			char			lastChar = ' ';
			
			for (int index = 0, maxIndex = source.length; index < maxIndex; index++) {
				if (source[index] == '\'') {
					inString = !inString;
				}
				if (!inString) {
					if (!inId && source[index] == quoteId[0]) {
						inId = true;
					}
					else if (inId && (source.length == 1 && source[index] == quoteId[0] || source.length == 2 && source[index] == quoteId[1])) {
						inId = false;
					}
				}
				if (!inString && !inId) {
					if (source[index] > ' ') {
						target[targetIndex++] = Character.toUpperCase(source[index]);
					}
					else if (source[index] != lastChar) {
						target[targetIndex++] = ' ';
					}
				}
				else {
					target[targetIndex++] = source[index];
				}
				lastChar = source[index];
			}
			return new String(target,0,targetIndex);
		}
	}

}
