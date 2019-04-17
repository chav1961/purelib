package chav1961.purelib.sql;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

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
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.ORMProvider;

public class SimpleProvider<Key, Record> implements ORMProvider<Key, Record> {
	private static final AtomicInteger		ai = new AtomicInteger(1);
	private static final int				SELECT_INDEX = 0;
	private static final int				INSERT_INDEX = 1;
	private static final int				UPDATE_INDEX = 2;
	private static final int				DELETE_INDEX = 3;
	
	protected final ContentNodeMetadata		tableModel;
	protected final ContentNodeMetadata		clazzModel;
	
	private final Class<Record>				clazz;
	private final String[]					fields, primaryKeys;
	private final ORMProvider<Key, Record>	parent;
	private final String					selectString, insertString, updateString, deleteString, bulkSelectString;
	private final GetterAndSetter[]			gas = null;
	private final int[][]					forSelect = null, forInsert = null, forUpdate = null, forDelete = null;
	private final int[][]					forSelectKeys = null, forUpdateKeys = null, forDeleteKeys = null;
	private final PreparedStatement[]		statements;
	private final PreparedStatementCache	cache;
	
	private ProgressIndicator				progress = null;
	
	public SimpleProvider(final ContentNodeMetadata tableModel, final ContentNodeMetadata clazzModel, final Class<Record> clazz, final String[] fields, final String[] primaryKeys) throws IOException {
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
			this.tableModel = tableModel;
			this.clazzModel = clazzModel;
			this.clazz = clazz;
			this.fields = fields;
			this.primaryKeys = primaryKeys;
			this.parent = null;
			
			this.selectString = buildSelectString(tableModel.getName(),fields,primaryKeys);
			this.insertString = buildInsertString(tableModel.getName(),fields,primaryKeys);
			this.updateString = buildUpdateString(tableModel.getName(),fields,primaryKeys);
			this.deleteString = buildDeleteString(tableModel.getName(),fields,primaryKeys);
			this.bulkSelectString = buildBulkSelectString(tableModel.getName(),fields);
			this.statements = null;
			this.cache = null;
		}
	}
	
	private SimpleProvider(final SimpleProvider<Key, Record> parent, final Connection conn) throws SQLException {
		this.parent = parent;
		this.tableModel = parent.tableModel;
		this.clazzModel = parent.clazzModel;
		
		this.clazz = null;
		this.fields = null;
		this.primaryKeys = null;
		
		this.selectString = null;
		this.insertString = null;
		this.updateString = null;
		this.deleteString = null;
		this.bulkSelectString = null;
		this.statements = new PreparedStatement[]{
								conn.prepareStatement(parent.selectString),
								conn.prepareStatement(parent.insertString),
								conn.prepareStatement(parent.updateString),
								conn.prepareStatement(parent.deleteString),
								conn.prepareStatement(parent.bulkSelectString)
							};
		this.cache = new PreparedStatementCache(conn, parent.bulkSelectString);
	}

	@Override
	public ORMProvider<Key, Record> associate(final Connection conn) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (parent != null) {
			throw new IllegalStateException("Attempt to associate connection with already associated instance. Chain of associations is not supported!"); 
		}
		else {
			return new SimpleProvider<Key, Record>(this,conn); 
		}
	}

	@Override
	public void close() throws SQLException {
		if (this.statements != null) {
			cache.close();
			for (PreparedStatement item : this.statements) {
				item.close();
			}
		}
	}

	@Override
	public long contentSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long contentSize(String filter) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
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
		else {
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
		else if (from < 0) {
			throw new IllegalArgumentException("From index ["+from+"] can't be negative"); 
		}
		else if (count <= 0) {
			throw new IllegalArgumentException("Count ["+count+"] must be positive"); 
		}
		else {
			iterate(item,callback,filter,"",from,count);
		}
	}

	@Override
	public void content(final Record item, final ContentIteratorCallback<Record> callback, final String filter, final String ordering) throws SQLException {
		if (item == null) {
			throw new NullPointerException("Record item can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
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
		else if (from < 0) {
			throw new IllegalArgumentException("From index ["+from+"] can't be negative"); 
		}
		else if (count <= 0) {
			throw new IllegalArgumentException("Count ["+count+"] must be positive"); 
		}
		else {
			iterate(item,callback,filter,ordering,from,count);
		}
	}
	
	@Override
	public void create(final Key key, final Record record) throws SQLException {
		try{upload(statements[INSERT_INDEX],record,gas,forInsert);
			if (statements[INSERT_INDEX].executeUpdate() != 1) {
				throw new SQLException("No any record was inserted");
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e); 
		}
	}

	@Override
	public void read(final Key key, final Record record) throws SQLException {
		try{upload(statements[SELECT_INDEX],key,gas,forSelectKeys);
			try(final ResultSet	rs = statements[SELECT_INDEX].executeQuery()) {
				if (rs.next()) {
					download(rs,record,gas,forSelect);
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
	public void update(final Key key, final Record record) throws SQLException {
		try{upload(statements[UPDATE_INDEX],record,gas,forUpdate);
			upload(statements[UPDATE_INDEX],key,gas,forUpdateKeys);
			if (statements[UPDATE_INDEX].executeUpdate() != 1) {
				throw new SQLException("No any record was updated");
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e); 
		}
	}

	@Override
	public void delete(final Key key) throws SQLException {
		try{upload(statements[DELETE_INDEX],key,gas,forDeleteKeys);
			if (statements[DELETE_INDEX].executeUpdate() != 1) {
				throw new SQLException("No any record was deleted");
			}
		} catch (ContentException | IOException e) {
			throw new SQLException(e.getLocalizedMessage(),e); 
		}
	}

	public void setProgressIndicator(final ProgressIndicator indicator) {
		if (parent == null) {
			throw new IllegalStateException("Attempt to set progress indicator with non-associated instance. Call associate(...) to get appropriative instance"); 
		}
		else {
			this.progress = indicator; 
		}
	}	
	
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
		
		String	prefix = "(";
		for (String item : fields) {
			sb.append(prefix).append(item);
			prefix = ",";
		}
		sb.append(") values ");
		prefix = "(";
		for (String item : fields) {
			sb.append(prefix).append("?");
			prefix = ",";
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
		final StringBuilder	sb = new StringBuilder("delete ");
		
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
	
	static void download(final ResultSet rs, final Object instance, final GetterAndSetter[] gas, final int[][] toAndType) throws ContentException, SQLException, IOException {
		for (int index = 0, maxIndex = toAndType.length; index < maxIndex; index++) {
			switch (toAndType[index][1]) {
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
	
	static void upload(final ResultSet rs, final Object instance, final GetterAndSetter[] gas, final int[][] fromToAndType) throws ContentException, SQLException, IOException {
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

	private void iterate(final Record item, final ContentIteratorCallback<Record> callback, final String filter, final String ordering) throws SQLException {
		// TODO Auto-generated method stub
		final PreparedStatement	ps = cache.get(filter,ordering,false);
		
		try{upload(ps,item,gas,null);
			try(final ResultSet	rs = ps.executeQuery()) {
				long	count = 0;
				
				while (rs.next()) {
					download(rs,item,gas,forSelect);
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
		// TODO Auto-generated method stub
		final PreparedStatement	ps = cache.get(filter,ordering,false);
		
		try{upload(ps,item,gas,null);
			ps.setLong(0,offset);
			ps.setLong(0,limit);
			try(final ResultSet	rs = ps.executeQuery()) {
				long	count = 0, from = offset;
				
				while (rs.next()) {
					download(rs,item,gas,forSelect);
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
			if (!cache.get(reducedOrdering).containsKey(reducedOrdering)) {
				cache.get(reducedOrdering).put(reducedOrdering,new PreparedStatement[2]);
			}
			
			ps = cache.get(reducedOrdering).get(reducedOrdering);
			if (offsetAndLimit) {
				if (ps[1] == null) {
					ps[1] = conn.prepareStatement(selectTemplate+" where "+filter+(ordering.isEmpty() ? "" : " order by "+ordering)+" offset ? limit ?");
				}
				return ps[1];
			}
			else {
				if (ps[0] == null) {
					ps[0] = conn.prepareStatement(selectTemplate+" where "+filter+(ordering.isEmpty() ? "" : " order by "+ordering));
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
						try{ps.close();
						} catch (SQLException exc) {
							wasExceptions = true;
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
