package chav1961.purelib.sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.ClassLoaderWrapper;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.AnyExecutorInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.char2byte.asm.CompilerUtils;

public class SimpleProvider<Key, Record> implements ORMProvider<Key, Record> {
	private static final AtomicInteger		ai = new AtomicInteger(1);
	private static final Builder[]			BUILDERS = {
												SimpleProvider::buildReadExecutor,
												SimpleProvider::buildCreateExecutor,
												SimpleProvider::buildUpdateExecutor,
												SimpleProvider::buildDeleteExecutor,
												SimpleProvider::buildBulkReadExecutor
											};
	
	private interface Builder<Record> {
		void build(Writer wr, ContentNodeMetadata tableModel, ContentNodeMetadata clazzModel, Class<?> clazz, String executorName, String[] fields, String[] primaryKeys, int psIndex) throws IOException;
	}
	
	
	protected final ContentNodeMetadata		tableModel;
	protected final ContentNodeMetadata		clazzModel;
	
	private final Class<Record>				clazz;
	private final String[]					fields, primaryKeys;
	private final ORMProvider<Key, Record>	parent;
	private final String					selectString, insertString, updateString, deleteString, bulkSelectString;
	private final PreparedStatement[]		statements;
	private final AnyExecutorInterface<Object,Record>[]	executors;
	
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
			this.executors = new AnyExecutorInterface[BUILDERS.length];
			
			try(final OutputStream	os = new ByteArrayOutputStream();
				final AsmWriter		asm = new AsmWriter(os)) {
				
				asm.write(Utils.fromResource(this.getClass().getResource("")));
				for (int index = 0; index < executors.length; index++) {
					try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
						final AsmWriter				wr = asm.clone(baos)) {
						final String				className = SimpleProvider.class.getPackage().getName()+".AnyORMExecutor"+ai.incrementAndGet();

						BUILDERS[index].build(wr,tableModel,clazzModel,clazz,className,fields,primaryKeys,index);
						wr.flush();
						this.executors[index] = (AnyExecutorInterface<Object,Record>)new ClassLoaderWrapper().createClass(className,baos.toByteArray()).newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new IOException(e.getLocalizedMessage(),e); 
					}
				}
			}
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
		this.executors = null;
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
	public ORMProviderIterator<Key, Record> content() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ORMProviderIterator<Key, Record> content(long from, long count) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ORMProviderIterator<Key, Record> content(String filter) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ORMProviderIterator<Key, Record> content(String filter, long from, long count) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ORMProviderIterator<Key, Record> content(String filter, String ordering) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ORMProviderIterator<Key, Record> content(String filter, String ordering, long from, long count) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(Key key, Record record) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read(Key key, Record record) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Key key, Record record) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Key key) throws SQLException {
		// TODO Auto-generated method stub
		
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

	static <Record> void buildReadExecutor(final Writer wr, final ContentNodeMetadata tableModel, final ContentNodeMetadata clazzModel, final Class<?> clazz, final String executorName, final String[] fields, final String[] primaryKeys, final int psIndex) throws IOException {
		final IOException[]	exc = new IOException[]{null};
		
		wr.write(" buildSelectCaption name=\""+executorName+"\"\n");
		CompilerUtils.walkFields(clazz,(owner,field)->{
			for(String item : fields) {
				if (item.equalsIgnoreCase(field.getName())) {
					try{wr.write(" buildGetterAndSetter fieldName=\""+executorName+"\"\n");
					} catch (IOException e) {
						exc[0] = new IOException(e.getLocalizedMessage(),e);
					}
				}
			}
		});
		if (exc[0] != null) {
			throw exc[0];
		}
		wr.write(" buildSelectConstructor name=\""+executorName+"\"\n");
		wr.write(" buildSelectExecutor name=\""+executorName+"\",count="+fields.length+",index="+psIndex+"\n");
		wr.write(" buildSelectTail name=\""+executorName+"\"\n");
	}

	static <Record> void buildCreateExecutor(final Writer wr, final ContentNodeMetadata tableModel, final ContentNodeMetadata clazzModel, final Class<?> clazz, final String executorName, final String[] fields, final String[] primaryKeys, final int psIndex) throws IOException {
		wr.write("");
	}

	static <Record> void buildUpdateExecutor(final Writer wr, final ContentNodeMetadata tableModel, final ContentNodeMetadata clazzModel, final Class<?> clazz, final String executorName, final String[] fields, final String[] primaryKeys, final int psIndex) throws IOException {
		wr.write("");
	}

	static <Record> void buildDeleteExecutor(final Writer wr, final ContentNodeMetadata tableModel, final ContentNodeMetadata clazzModel, final Class<?> clazz, final String executorName, final String[] fields, final String[] primaryKeys, final int psIndex) throws IOException {
		wr.write("");
	}

	static <Record> void buildBulkReadExecutor(final Writer wr, final ContentNodeMetadata tableModel, final ContentNodeMetadata clazzModel, final Class<?> clazz, final String executorName, final String[] fields, final String[] primaryKeys, final int psIndex) throws IOException {
		wr.write("");
	}
}
