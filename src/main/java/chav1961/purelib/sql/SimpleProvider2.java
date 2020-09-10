package chav1961.purelib.sql;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.table.TableModel;

import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.model.ContentMetadataFilter;
import chav1961.purelib.model.ContentNodeFilter;
import chav1961.purelib.model.SimpleContentMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.ORMProvider2;
import chav1961.purelib.streams.char2byte.CompilerUtils;

public class SimpleProvider2<Record> implements ORMProvider2<Record> {
	private static final char				BEFORE_CHAR = '\"';	
	private static final char				AFTER_CHAR = '\"';	
	
	
	private final ContentNodeMetadata		clazzMeta, tableMeta;
	private final ContentNodeMetadata		filteredTableMeta;
	private final Class<Record>				clazz;
	private final SimpleProvider2<Record>	parent;
	private final String					insertSQL;
	private final String					updateSQL;
	private final String					deleteSQL;
	private final String					selectSQL;
	private final String					contentSizeSQL;
	private final String					contentSQL;
	private final GetterAndSetter[]			fieldGAS;				
	private Connection						conn = null;
	private boolean							modified = false;
	private String							filter = null, ordering = null;
	private long[]							range = null;
	
	public SimpleProvider2(final ContentNodeMetadata clazzMeta, final ContentNodeMetadata tableMeta, final boolean useIntersectsOnly) throws NullPointerException {
		if (clazzMeta == null) {
			throw new NullPointerException("Class metadata to build provider for can't be null");
		}
		else if (tableMeta == null) {
			throw new NullPointerException("Table metadata to build provider for can't be null");
		}
		else {
			final Set<String>	clazzNames = new HashSet<>(), tableNames = new HashSet<>(), names2Use = new HashSet<>();
			
			for (ContentNodeMetadata item : clazzMeta) {
				clazzNames.add(item.getName().toUpperCase());
			}
			for (ContentNodeMetadata item : tableMeta) {
				tableNames.add(item.getName().toUpperCase());
			}
			if (!clazzNames.equals(tableNames)) {
				if (useIntersectsOnly) {
					names2Use.addAll(tableNames);
					names2Use.retainAll(clazzNames);
				}
				else {
					final Set<String>			temp1 = new HashSet<>(), temp2 = new HashSet<>();
					
					temp1.addAll(tableNames);	temp1.removeAll(clazzNames);
					temp2.addAll(clazzNames);	temp2.removeAll(tableNames);
					throw new IllegalArgumentException("List of table names doesn't equal to list of class field names. Unpaired table fields are "+temp1+", unpaired class fields are "+temp2);
				}
			}
			else {
				names2Use.addAll(tableNames);
			}
			
			this.clazzMeta = clazzMeta;
			this.tableMeta = tableMeta;
			this.filteredTableMeta = new ContentNodeFilter(tableMeta,(n)->names2Use.contains(n.getName().toUpperCase()));
			this.clazz = (Class<Record>) clazzMeta.getType();
			this.parent = null;
			this.insertSQL = SQLUtils.buildInsertOperatorTemplate(filteredTableMeta,BEFORE_CHAR,AFTER_CHAR);
			this.updateSQL = SQLUtils.buildUpdateOperatorTemplate(filteredTableMeta,BEFORE_CHAR,AFTER_CHAR)+" where "+SQLUtils.buildWhereClause4PrimaryKey(filteredTableMeta,BEFORE_CHAR,AFTER_CHAR);
			this.deleteSQL = SQLUtils.buildDeleteOperatorTemplate(filteredTableMeta,BEFORE_CHAR,AFTER_CHAR);
			this.selectSQL = SQLUtils.buildSelectOperatorTemplate(filteredTableMeta,BEFORE_CHAR,AFTER_CHAR)+" where "+SQLUtils.buildWhereClause4PrimaryKey(filteredTableMeta,BEFORE_CHAR,AFTER_CHAR);
			this.contentSizeSQL = "select count(*) from "+BEFORE_CHAR+filteredTableMeta.getName()+AFTER_CHAR;
			this.contentSQL = SQLUtils.buildSelectOperatorTemplate(filteredTableMeta,BEFORE_CHAR,AFTER_CHAR);
			this.fieldGAS = buildFieldGettersAndSetters(clazzMeta,tableMeta,names2Use);
		}
	}

	private SimpleProvider2(final SimpleProvider2<Record> parent) {
		this.clazzMeta = null;
		this.tableMeta = null;
		this.filteredTableMeta = null;
		this.clazz = null;
		this.parent = parent;
		this.insertSQL = null;
		this.updateSQL = null;
		this.deleteSQL = null;
		this.selectSQL = null;
		this.contentSizeSQL = null;
		this.contentSQL = null;
		this.fieldGAS = null;
	}

	@Override
	public Connection getConnection() {
		return conn == null ? (parent != null ? parent.getConnection() : null) : conn;
	}

	@Override
	public ORMProvider2<Record> setConnection(final Connection newConnection) throws SQLException {
		if (conn != null) {
			close(conn);
		}
		conn = newConnection;
		modified = true;
		return this;
	}

	@Override
	public void close() throws SQLException {
		if (conn != null) {
			close(conn);
		}
	}

	@Override
	public String getFilter() {
		return concat(parent != null ? parent.getFilter() : null, filter);
	}


	@Override
	public ORMProvider2<Record> setFilter(final String filter) throws SQLException {
		if (!Objects.deepEquals(filter,this.filter)) {
			this.filter = filter;
			modified = true;
		}
		return this;
	}

	@Override
	public String getOrdering() {
		return ordering == null ? (parent != null ? parent.getOrdering() : null) : null;
	}

	@Override
	public ORMProvider2<Record> setOrdering(final String ordering) throws SQLException {
		if (!Objects.deepEquals(ordering,this.ordering)) {
			this.ordering = ordering;
			modified = true;
		}
		return this;
	}

	@Override
	public long[] getRange() {
		return range == null ? (parent != null ? parent.getRange() : null) : null;
	}

	@Override
	public ORMProvider2<Record> setRange(final long[] range) throws SQLException {
		if (!Objects.deepEquals(range,this.range)) {
			this.range = range;
			modified = true;
		}
		return this;
	}

	@Override
	public ORMProvider2<Record> push() {
		return new SimpleProvider2<Record>(this);
	}

	@Override
	public ORMProvider2<Record> pop() {
		if (parent == null) {
			throw new IllegalStateException("Attempt to call pop() method witout previous push()"); 
		}
		else {
			if (conn != null) {
				try{close(conn);
				} catch (SQLException e) {
				}
			}
			return parent;
		}
	}

	@Override
	public TableModel getTableModel() throws SQLException {
		if (parent != null) {
			return parent.getTableModel();
		}
		else {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public long contentSize() throws SQLException {
		final PreparedStatement	ps = getContentSizeStmt();
		
		try(final ResultSet		rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			else {
				return 0;
			}
		}
	}

	@Override
	public ORMProvider2<Record> content(final Record rec, final ContentIteratorCallback<Record> callback) throws SQLException {
		// TODO Auto-generated method stub
		if (rec == null) {
			throw new NullPointerException("Record to use as buffer can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			final PreparedStatement	ps = getContentStmt();
			final GetterAndSetter[]	gas = getSelectGAS();
			Action					rc;
			long					counter = 0;
			
			try(final ResultSet		rs = ps.executeQuery()) {
				
loop:			while (rs.next()) {
					fillRecord(rs,rec,gas);
					
					try{
						switch (rc = callback.process(counter++,conn,rec)) {
							case CONTINUE				:
								break;
							case DELETE_AND_CONTINUE	:
								delete(rec);
								break;
							case STOP					:
								break loop;
							case UPDATE_AND_CONTINUE	:
								update(rec);
								break;
							default:
								throw new UnsupportedOperationException("Action value ["+rc+"] is not supported yet"); 
						}
					} catch (ContentException e) {
						throw new SQLException(e.getLocalizedMessage(),e);
					}
				}
			}
		}
		return this;
	}

	@Override
	public ORMProvider2<Record> refresh() {
		modified = true;
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public ORMProvider2<Record> insert(final Record record) throws SQLException {
		if (record == null) {
			throw new NullPointerException("Record to insert can't be null"); 
		}
		else {
			final PreparedStatement	ps = getInsertStmt();
			final ParameterMetaData	pmd = ps.getParameterMetaData();
			final GetterAndSetter[]	gas = getInsertGAS();
			
			try {
				for (int index = 1, maxIndex = pmd.getParameterCount(); index <= maxIndex; index++) {
					switch (gas[index-1].getClassType()) {
						case CompilerUtils.CLASSTYPE_BOOLEAN	:
							ps.setBoolean(index, ((BooleanGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_BYTE		:
							ps.setByte(index, ((ByteGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_CHAR		:
							ps.setInt(index, ((CharGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_DOUBLE		:
							ps.setDouble(index, ((DoubleGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_FLOAT		:
							ps.setFloat(index, ((FloatGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_INT		:
							ps.setInt(index, ((IntGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_LONG		:
							ps.setLong(index, ((LongGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_SHORT		:
							ps.setShort(index, ((ShortGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_REFERENCE	:
							ps.setObject(index, ((ObjectGetterAndSetter<?>)gas[index-1]).get(record));
							break;
						default :
							throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet"); 
					}
				}
				ps.executeUpdate();
			} catch (ContentException e) {
				throw new SQLException(e.getLocalizedMessage(),e);
			}
			return this;
		}
	}

	@Override
	public ORMProvider2<Record> read(Record record) throws SQLException {
		if (record == null) {
			throw new NullPointerException("Record to update can't be null"); 
		}
		else {
			final PreparedStatement	ps = getSelectStmt();
			final ParameterMetaData	pmd = ps.getParameterMetaData();
			final GetterAndSetter[]	gas = getPkGAS();
			final GetterAndSetter[]	gasR = getSelectGAS();
			
			try {
				for (int index = 1, maxIndex = pmd.getParameterCount(); index <= maxIndex; index++) {
					switch (gas[index-1].getClassType()) {
						case CompilerUtils.CLASSTYPE_BOOLEAN	:
							ps.setBoolean(index, ((BooleanGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_BYTE		:
							ps.setByte(index, ((ByteGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_CHAR		:
							ps.setInt(index, ((CharGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_DOUBLE		:
							ps.setDouble(index, ((DoubleGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_FLOAT		:
							ps.setFloat(index, ((FloatGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_INT		:
							ps.setInt(index, ((IntGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_LONG		:
							ps.setLong(index, ((LongGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_SHORT		:
							ps.setShort(index, ((ShortGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_REFERENCE	:
							ps.setObject(index, ((ObjectGetterAndSetter<?>)gas[index-1]).get(record));
							break;
						default :
							throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet"); 
					}
				}
				try(final ResultSet	rs = ps.executeQuery()) {
					if (rs.next()) {
						fillRecord(rs,record,gasR);
					}
				}
			} catch (ContentException e) {
				throw new SQLException(e.getLocalizedMessage(),e);
			}
			return this;
		}
	}

	@Override
	public ORMProvider2<Record> update(Record record) throws SQLException {
		if (record == null) {
			throw new NullPointerException("Record to update can't be null"); 
		}
		else {
			final PreparedStatement	ps = getUpdateStmt();
			final ParameterMetaData	pmd = ps.getParameterMetaData();
			final GetterAndSetter[]	gas = getUpdateGAS();
			
			try {
				for (int index = 1, maxIndex = pmd.getParameterCount(); index <= maxIndex; index++) {
					switch (gas[index-1].getClassType()) {
						case CompilerUtils.CLASSTYPE_BOOLEAN	:
							ps.setBoolean(index, ((BooleanGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_BYTE		:
							ps.setByte(index, ((ByteGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_CHAR		:
							ps.setInt(index, ((CharGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_DOUBLE		:
							ps.setDouble(index, ((DoubleGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_FLOAT		:
							ps.setFloat(index, ((FloatGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_INT		:
							ps.setInt(index, ((IntGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_LONG		:
							ps.setLong(index, ((LongGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_SHORT		:
							ps.setShort(index, ((ShortGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_REFERENCE	:
							ps.setObject(index, ((ObjectGetterAndSetter<?>)gas[index-1]).get(record));
							break;
						default :
							throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet"); 
					}
				}
				ps.executeUpdate();
			} catch (ContentException e) {
				throw new SQLException(e.getLocalizedMessage(),e);
			}
			return this;
		}
	}

	@Override
	public ORMProvider2<Record> delete(final Record record) throws SQLException {
		if (record == null) {
			throw new NullPointerException("Record to update can't be null"); 
		}
		else {
			final PreparedStatement	ps = getDeleteStmt();
			final ParameterMetaData	pmd = ps.getParameterMetaData();
			final GetterAndSetter[]	gas = getDeleteGAS();
			
			try {
				for (int index = 1, maxIndex = pmd.getParameterCount(); index <= maxIndex; index++) {
					switch (gas[index-1].getClassType()) {
						case CompilerUtils.CLASSTYPE_BOOLEAN	:
							ps.setBoolean(index, ((BooleanGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_BYTE		:
							ps.setByte(index, ((ByteGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_CHAR		:
							ps.setInt(index, ((CharGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_DOUBLE		:
							ps.setDouble(index, ((DoubleGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_FLOAT		:
							ps.setFloat(index, ((FloatGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_INT		:
							ps.setInt(index, ((IntGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_LONG		:
							ps.setLong(index, ((LongGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_SHORT		:
							ps.setShort(index, ((ShortGetterAndSetter)gas[index-1]).get(record));
							break;
						case CompilerUtils.CLASSTYPE_REFERENCE	:
							ps.setObject(index, ((ObjectGetterAndSetter<?>)gas[index-1]).get(record));
							break;
						default :
							throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet"); 
					}
				}
				ps.executeUpdate();
			} catch (ContentException e) {
				throw new SQLException(e.getLocalizedMessage(),e);
			}
			return this;
		}
	}

	private boolean isModified() {
		return modified || (parent != null ? parent.isModified() : false);
	}
	
	private void close(final Connection conn) throws SQLException {
		
	}

	private String concat(final String filter1, final String filter2) {
		// TODO Auto-generated method stub
		return null;
	}

	private PreparedStatement getInsertStmt() {
		// TODO Auto-generated method stub
		return null;
	}

	private PreparedStatement getUpdateStmt() {
		// TODO Auto-generated method stub
		return null;
	}

	private PreparedStatement getDeleteStmt() {
		// TODO Auto-generated method stub
		return null;
	}

	private PreparedStatement getSelectStmt() {
		// TODO Auto-generated method stub
		return null;
	}

	private PreparedStatement getContentSizeStmt() {
		// TODO Auto-generated method stub
		return null;
	}

	private PreparedStatement getContentStmt() {
		// TODO Auto-generated method stub
		return null;
	}

	private GetterAndSetter[] getInsertGAS() {
		// TODO Auto-generated method stub
		return null;
	}

	private GetterAndSetter[] getUpdateGAS() {
		// TODO Auto-generated method stub
		return null;
	}

	private GetterAndSetter[] getDeleteGAS() {
		// TODO Auto-generated method stub
		return null;
	}

	private GetterAndSetter[] getSelectGAS() {
		// TODO Auto-generated method stub
		return null;
	}

	private GetterAndSetter[] getPkGAS() {
		// TODO Auto-generated method stub
		return null;
	}

	private void fillRecord(final ResultSet rs, final Record record, final GetterAndSetter[] gas) throws SQLException {
		for (int index = 1, maxIndex = gas.length; index <= maxIndex; index++) {
			try {
				switch (gas[index-1].getClassType()) {
					case CompilerUtils.CLASSTYPE_BOOLEAN	:
						((BooleanGetterAndSetter)gas[index-1]).set(record,rs.getBoolean(index));
						break;
					case CompilerUtils.CLASSTYPE_BYTE		:
						((ByteGetterAndSetter)gas[index-1]).set(record,rs.getByte(index));
						break;
					case CompilerUtils.CLASSTYPE_CHAR		:
						((CharGetterAndSetter)gas[index-1]).set(record,(char)rs.getInt(index));
						break;
					case CompilerUtils.CLASSTYPE_DOUBLE		:
						((DoubleGetterAndSetter)gas[index-1]).set(record,rs.getDouble(index));
						break;
					case CompilerUtils.CLASSTYPE_FLOAT		:
						((FloatGetterAndSetter)gas[index-1]).set(record,rs.getFloat(index));
						break;
					case CompilerUtils.CLASSTYPE_INT		:
						((IntGetterAndSetter)gas[index-1]).set(record,rs.getInt(index));
						break;
					case CompilerUtils.CLASSTYPE_LONG		:
						((LongGetterAndSetter)gas[index-1]).set(record,rs.getLong(index));
						break;
					case CompilerUtils.CLASSTYPE_SHORT		:
						((ShortGetterAndSetter)gas[index-1]).set(record,rs.getShort(index));
						break;
					case CompilerUtils.CLASSTYPE_REFERENCE	:
						((ObjectGetterAndSetter<Object>)gas[index-1]).set(record,rs.getObject(index));
						break;
					default :
						throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet"); 
				}
			} catch (ContentException e) {
				throw new SQLException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	public static <Record> RecordCreationCallback<Record> buildCreationCallback(final Class<Record> clazz, final ModuleAccessor accessor, final SimpleURLClassLoader loader) throws NullPointerException, IllegalArgumentException {
		if (clazz == null) {
			throw new NullPointerException("Class to build creation callback for can't be null"); 
		}
		else if (accessor == null) {
			throw new NullPointerException("Module accessor to build creation callback for can't be null"); 
		}
		else if (loader == null) {
			throw new NullPointerException("Loader to build creation callback for can't be null"); 
		}
		else {
			try{
				final Constructor<Record>	cons = getDefaultConstructor(clazz);
				final MethodHandle			consH = MethodHandles.lookup().unreflectConstructor(cons);
				
				if (Cloneable.class.isAssignableFrom(clazz)) {
					final Method			clone = getCloneMethod(clazz);
					final MethodHandle		cloneH = MethodHandles.lookup().unreflect(clone);
					
					return new RecordCreationCallback<Record>() {
						@Override
						public Record create(final Connection conn) throws SQLException, ContentException {
							try{return (Record) consH.invokeExact();
							} catch (Throwable e) {
								throw new ContentException(e.getLocalizedMessage(),e);
							}
						}
						
						@Override
						public Record duplicate(final Connection conn, final Record source) throws SQLException, ContentException {
							try{return (Record) cloneH.invokeExact(source);
							} catch (Throwable e) {
								throw new ContentException(e.getLocalizedMessage(),e);
							}
						}
					};
				}
				else {
					return new RecordCreationCallback<Record>() {
						@Override
						public Record create(final Connection conn) throws SQLException, ContentException {
							try{return (Record) consH.invokeExact();
							} catch (Throwable e) {
								throw new ContentException(e.getLocalizedMessage(),e);
							}
						}
					};
				}
			} catch (IllegalAccessException exc) {
				throw new IllegalArgumentException("Error creating instantiator for class ["+clazz.getCanonicalName()+"] "+exc.getLocalizedMessage(),exc);
			}
		}
	}


	static GetterAndSetter[] buildFieldGettersAndSetters(final ContentNodeMetadata clazzMeta, final ContentNodeMetadata tableMeta, final Set<String> names2Use) {
		final List<GetterAndSetter>	result = new ArrayList<>();
		final Class<?>				clazz = clazzMeta.getType();
		
		for (ContentNodeMetadata item : tableMeta) {
			for (ContentNodeMetadata clazzItem : clazzMeta) {
				if (clazzItem.getName().equalsIgnoreCase(item.getName())) {
					
				}
			}
			
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	private static <Record> Constructor<Record> getDefaultConstructor(final Class<Record> clazz) {
		try{final Constructor<Record>	result = clazz.getDeclaredConstructor();
			
			result.setAccessible(true);
			return result;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Class ["+clazz.getCanonicalName()+"] doesn't have default constructor accessible");  
		}
	}

	private static <Record> Method getCloneMethod(final Class<Record> clazz) {
		try{final Method	m = clazz.getDeclaredMethod("clone");
		
			m.setAccessible(true);
			return m;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Class ["+clazz.getCanonicalName()+"] doesn't have clone() method accessible");  
		}
	}	

}
