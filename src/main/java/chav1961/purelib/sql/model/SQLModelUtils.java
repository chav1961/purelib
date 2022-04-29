package chav1961.purelib.sql.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonValueType;
import chav1961.purelib.model.SchemaContainer;
import chav1961.purelib.model.TableContainer;
import chav1961.purelib.model.UniqueIdContainer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.ORMModelMapper;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.sql.interfaces.InstanceManager;
import chav1961.purelib.sql.interfaces.SQLErrorType;
import chav1961.purelib.sql.model.interfaces.DatabaseModelAdapter;
import chav1961.purelib.sql.model.interfaces.DatabaseModelAdapter.StandardExceptions;
import chav1961.purelib.sql.model.internal.DefaultDatabaseModelAdapter;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

/**
 * <p>This class supports database manipulations with model.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class SQLModelUtils {
	private static final DatabaseModelAdapter	DEFAULT_MODEL_ADAPTER = new DefaultDatabaseModelAdapter();
	private static final AtomicInteger			AI = new AtomicInteger();
	private static final AsmWriter				ASM_WRITER;
	private static final Set<String>			LOBS = Set.of("BLOB", "CLOB", "NCLOB", "BYTEA");
	private static final Pattern				PART_PATTERN = Pattern.compile("([^\\.]+)\\.([^@]+)@(.*)");
	private static final Map<String, JsonValueType>	TYPES = Utils.mkMap("VARCHAR", JsonValueType.STRING,
																		"TEXT", JsonValueType.STRING,
																		"BIGINT", JsonValueType.INTEGER);

	static {
		ASM_WRITER = null;
	}

	
	public static <K,I> InstanceManager<K,I> createClassInstanceManagerByModel(final Connection conn, final ContentNodeMetadata clazz, final ContentNodeMetadata table) throws SQLException, NullPointerException {
		return null;
	}

	public static <K,I> InstanceManager<K,I> createReadOnlyClassInstanceManagerByModel(final Connection conn, final ContentNodeMetadata clazz, final ContentNodeMetadata table) throws SQLException, NullPointerException {
		return null;
	}
	
	public static <K,I> InstanceManager<K,I> createMapInstanceManagerByModel(final Connection conn, final ContentNodeMetadata table) throws SQLException, NullPointerException {
		return null;
	}

	public static InstanceManager<List<Map.Entry<String, Object>>, Map<String, Object>> createReadOnlyMapInstanceManagerByModel(final Connection conn, final ContentNodeMetadata table) throws SQLException, NullPointerException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (table == null || !TableContainer.class.isAssignableFrom(table.getType())) {
			throw new IllegalArgumentException("Model is null or doesn't reference to TableContainer class");
		}
		else {
			final List<String>	keys = new ArrayList<>(), columns = new ArrayList<>();
			final Set<String>	names = new HashSet<>();
			
			for (ContentNodeMetadata item : table) {
				if (item.getName().endsWith("/pk")) {
					final String	temp = item.getName().substring(0,item.getName().length()-3); 
					
					keys.add(temp);
					names.add(temp);
				}
				else if (item.getFormatAssociated() != null && item.getFormatAssociated().isUsedInList()) {
					columns.add(item.getName());
					names.add(item.getName());
				}
			}
			
			return new InstanceManager<List<Map.Entry<String, Object>>, Map<String, Object>>(){
				@Override
				public Class<?> getInstanceType() {
					return Map.class;
				}

				@Override
				public Class<?> getKeyType() {
					return List.class;
				}

				@Override
				public boolean isReadOnly() {
					return true;
				}
				
				@Override
				public Map<String, Object> newInstance() throws SQLException {
					final Map<String, Object>	result = new HashMap<>();
					
					for (String item : names) {
						result.put(item, null);
					}
					return result;
				}

				@Override
				public List<Entry<String, Object>> newKey() throws SQLException {
					throw new SQLException("Attempt to create key for read-only instance manager");
				}

				@Override
				public List<Entry<String, Object>> extractKey(final Map<String, Object> inst) throws SQLException {
					if (inst == null) {
						throw new NullPointerException("Instance can't be null"); 
					}
					else {
						final List<Entry<String, Object>> result = new ArrayList<>();
						
						for (Entry<String, Object> item : inst.entrySet()) {
							if (names.contains(item.getKey())) {
								result.add(item);
							}
						}
						return result;
					}
				}

				@Override
				public Map<String, Object> clone(final Map<String, Object> inst) throws SQLException {
					throw new SQLException("Attempt to clone instance for read-only instance manager");
				}

				@Override
				public void loadInstance(final ResultSet rs, final Map<String, Object> inst) throws SQLException {
					if (rs == null) {
						throw new NullPointerException("Result set can't be null"); 
					}
					else if (inst == null) {
						throw new NullPointerException("Instance can't be null"); 
					}
					else {
						for (Entry<String, Object> item : inst.entrySet()) {
							item.setValue(rs.getObject(item.getKey()));
						}
					}
				}

				@Override
				public void storeInstance(final ResultSet rs, final Map<String, Object> inst, final boolean update) throws SQLException {
					throw new SQLException("Attempt to store content for read-only instance manager");
				}

				@Override
				public void close() throws SQLException {
				}

				@Override
				public <T> T get(final Map<String, Object> inst, final String name) throws SQLException {
					if (inst == null) {
						throw new NullPointerException("Instance can't be null"); 
					}
					else if (name == null || name.isEmpty()) {
						throw new IllegalArgumentException("Name can't be null or empty"); 
					}
					else if (!inst.containsKey(name)) {
						throw new IllegalArgumentException("Name ["+name+"] is missing in the instance content"); 
					}
					else {
						return (T) inst.get(name);
					}
				}

				@Override
				public <T> InstanceManager<List<Entry<String, Object>>, Map<String, Object>> set(final Map<String, Object> inst, final String name, final T value) throws SQLException {
					if (inst == null) {
						throw new NullPointerException("Instance can't be null"); 
					}
					else if (name == null || name.isEmpty()) {
						throw new IllegalArgumentException("Name can't be null or empty"); 
					}
					else if (!inst.containsKey(name)) {
						throw new IllegalArgumentException("Name ["+name+"] is missing in the instance content"); 
					}
					else {
						inst.put(name, value);
						return this;
					}
				}

				@Override
				public void storeInstance(PreparedStatement ps, Map<String, Object> inst, boolean update) throws SQLException {
					throw new SQLException("Attempt to store content for read-only instance manager");
				}
			};
		}
	}

	/**
	 * <p>Create database structure by model description.</p>
	 * @param conn database connection. Can't be null and must have auto commit mode off
	 * @param root model root. Can't be null 
	 * @throws SQLException on any database errors
	 * @throws NullPointerException on any parameter is null
	 */
	public static void createDatabaseByModel(final Connection conn, final ContentNodeMetadata root) throws SQLException, NullPointerException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root metadata can't be null");
		}
		else {
			final boolean	autoCommitState = conn.getAutoCommit();
			
			try{conn.setAutoCommit(false);
				createDatabaseByModel(conn, root, root.getName());
			} finally {
				conn.setAutoCommit(autoCommitState);
			}
		}
	}
	
	/**
	 * <p>Create database structure by model description.</p>
	 * @param conn database connection. Can't be null and must have auto commit mode off
	 * @param root model root. Can't be null 
	 * @param schema schema name. Replaces schema name in the model. Can't be null or empty
	 * @throws SQLException on any database errors
	 * @throws NullPointerException on any parameter is null
	 * @throws IllegalArgumentException when schema name is null or empty
	 */
	public static void createDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final String schema) throws SQLException, NullPointerException, IllegalArgumentException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root metadata can't be null");
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema name can't be null or empty");
		}
		else if (root.getType() == SchemaContainer.class) {
			final String				oldSchema = conn.getSchema();
			
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
			
				if (adapter.isSchemaSupported()) {
					createDatabaseSchemaByModel(conn, root, schema, adapter);
					conn.setSchema(schema);
				}
				
				for (ContentNodeMetadata item : root) {
					internalCreateDatabaseByModel(conn, item, schema, adapter);
				}
			} catch (EnvironmentException e) {
				throw new SQLException(e);
			} finally {
				conn.setSchema(oldSchema);
			}
		}
		else {
			throw new IllegalArgumentException("Root metadata must have [SchemaContainer] type");
		}
	}

	private static void internalCreateDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final DatabaseModelAdapter adapter) throws SQLException, NullPointerException {
		if (root.getType() == TableContainer.class) {
			createDatabaseTableByModel(conn, root, schema, adapter);
		}
		else if (root.getType() == UniqueIdContainer.class) {
			if (adapter.isSequenceSupported()) {
				createDatabaseSequenceByModel(conn, root, schema, adapter);
			}
		}
		else {
			throw new IllegalArgumentException("Database metadata must have [TableContainer] or [UniqueIdContainer] types only");
		}
	}	
	
	private static void createDatabaseSchemaByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final DatabaseModelAdapter adapter) throws SQLException {
		try(final ResultSet	rs = conn.getMetaData().getSchemas(null, schema)) {
			if (!rs.next()) {
				executeSQL(conn, "create schema "+conn.getMetaData().getIdentifierQuoteString()+schema+conn.getMetaData().getIdentifierQuoteString());
			}
		}
	}

	private static void createDatabaseTableByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final DatabaseModelAdapter adapter) throws SQLException {
		try{executeSQL(conn, adapter.createTable(root, schema));
		} catch (SyntaxException e) {
			throw new SQLException(e); 
		}
	}

	private static void createDatabaseSequenceByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final DatabaseModelAdapter adapter) throws SQLException {
		try{executeSQL(conn, adapter.createSequence(root, schema));
		} catch (SyntaxException  e) {
			throw new SQLException(e); 
		}
	}

	public static void removeDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final boolean removeSchema) throws SQLException, NullPointerException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root metadata can't be null");
		}
		else {
			removeDatabaseByModel(conn, root, root.getName(), removeSchema);
		}
	}	
	
	public static void removeDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final boolean removeSchema) throws SQLException, NullPointerException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root metadata can't be null");
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema can't be null or empty");
		}
		else if (root.getType() == SchemaContainer.class) {
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
			
				for (ContentNodeMetadata item : root) {
					internalRemoveDatabaseByModel(conn, item, schema, removeSchema, adapter);
				}
				if (removeSchema && adapter.isSchemaSupported()) {
					removeDatabaseSchemaByModel(conn, root, schema, adapter);
				}
			} catch (EnvironmentException e) {
				throw new SQLException(e);
			}
		}
		else {
			throw new IllegalArgumentException("Root metadata must have [SchemaContainer] type");
		}
	}

	public static <Version extends Comparable<Version>> Version extractVersionFromModel(final ContentNodeMetadata model, final Version defaultVersion) throws NullPointerException, IllegalArgumentException {
		if (model == null) {
			throw new NullPointerException("Model can'tbe null");
		}
		else if (model.getType() == SchemaContainer.class) {
			final URI		appUri = model.getApplicationPath();
			final String	query = URIUtils.extractQueryFromURI(appUri);
			
			if (query != null && !query.isEmpty()) {
				final Hashtable<String,String[]>	content = URIUtils.parseQuery(query);
				
				if (content.containsKey("version")) {
					return (Version) new SimpleDottedVersion(content.get("verison")[0]);
				}
				else {
					return defaultVersion;
				}
			}
			else {
				return defaultVersion;
			}
		}
		else {
			throw new IllegalArgumentException("Invalid model: root must have ["+SchemaContainer.class+"] type, not ["+model.getType()+"]"); 
		}
	}
	
	public static String buildSelectAllStatementByModel(final Connection conn, final ContentNodeMetadata meta, final String schema) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema can't be null or empty"); 
		}
		else if (meta.getType() != TableContainer.class) {
			throw new IllegalArgumentException("Metadata must have [TableContainer] type"); 
		}
		else {
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
			
				return "select * from "+adapter.getTableName(meta, schema);
			
			} catch (EnvironmentException | SyntaxException e) {
				throw new SQLException(e); 
			}
		}
	}

	public static String buildSelectCountStatementByModel(final Connection conn, final ContentNodeMetadata meta, final String schema) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema can't be null or empty"); 
		}
		else if (meta.getType() != TableContainer.class) {
			throw new IllegalArgumentException("Metadata must have [TableContainer] type"); 
		}
		else {
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
			
				return "select count(*) from "+adapter.getTableName(meta, schema);
			
			} catch (EnvironmentException | SyntaxException e) {
				throw new SQLException(e); 
			}
		}
	}
	
	public static String buildInsertValuesStatementByModel(final Connection conn, final ContentNodeMetadata meta, final String schema) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (schema == null || schema.isEmpty()) {
			throw new IllegalArgumentException("Schema can't be null or empty"); 
		}
		else if (meta.getType() != TableContainer.class) {
			throw new IllegalArgumentException("Metadata must have [TableContainer] type"); 
		}
		else {
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
				final StringBuilder			sb = new StringBuilder(), sbVal = new StringBuilder();
				String	prefix = "(";
			
				sb.append("insert into ").append(adapter.getTableName(meta, schema));
				for (ContentNodeMetadata item : meta) {
					sb.append(prefix).append(adapter.getColumnName(item, schema));
					sbVal.append(prefix).append('?');
					prefix = ",";
				}
				return sb.append(") values ").append(sbVal).append(')').toString();
			} catch (EnvironmentException | SyntaxException e) {
				throw new SQLException(e); 
			}
		}
	}
	
	private static void internalRemoveDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final boolean removeSchema, final DatabaseModelAdapter adapter) throws SQLException, NullPointerException {
		if (root.getType() == UniqueIdContainer.class) {
			if (adapter.isSequenceSupported()) {
				removeDatabaseSequenceByModel(conn, root, schema, adapter);
			}
		}
		else if (root.getType() == TableContainer.class) {
			removeDatabaseTableByModel(conn, root, schema, adapter);
		}
		else {
			throw new IllegalArgumentException("Database metadata must have [TableContainer] or [UniqueIdContainer] types only");
		}
	}
	
	private static void removeDatabaseSchemaByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final DatabaseModelAdapter adapter) throws SQLException {
		try(final Statement	stmt = conn.createStatement()) {
			stmt.executeUpdate(adapter.dropSchema(root, schema));
		} catch (SQLException exc) {
			if (adapter.getExceptionType(exc) != StandardExceptions.NO_OBJECT_FOUND) {
				throw exc;
			}
		} catch (SyntaxException e) {
			throw new SQLException(e);
		}
	}

	private static void removeDatabaseTableByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final DatabaseModelAdapter adapter) throws SQLException {
		try(final Statement	stmt = conn.createStatement()) {

			stmt.executeUpdate(adapter.dropTable(root, schema));
		} catch (SQLException exc) {
			if (adapter.getExceptionType(exc) != StandardExceptions.NO_OBJECT_FOUND) {
				throw exc;
			}
		} catch (SyntaxException e) {
			throw new SQLException(e); 
		}
	}

	private static void removeDatabaseSequenceByModel(final Connection conn, final ContentNodeMetadata root, final String schema, final DatabaseModelAdapter adapter) throws SQLException {
		try(final Statement	stmt = conn.createStatement()) {
			
			stmt.executeUpdate(adapter.dropSequence(root, schema));
		} catch (SQLException exc) {
			if (adapter.getExceptionType(exc) != StandardExceptions.NO_OBJECT_FOUND) {
				throw exc;
			}
		} catch (SyntaxException e) {
			throw new SQLException(e); 
		}
	}
	
	public static void backupDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final ZipOutputStream os) throws SQLException, NullPointerException, IOException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root metadata can't be null");
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else if (root.getType() == SchemaContainer.class) {
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
			
				backupDatabaseSchemaByModel(conn, root, os, adapter);
				for (ContentNodeMetadata item : root) {
					backupDatabaseByModel(conn, item, os);
				}
			} catch (EnvironmentException e) {
				throw new SQLException(e);
			}
		}
		else if (root.getType() == UniqueIdContainer.class) {
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
				
				if (adapter.isSequenceSupported()) {
					backupDatabaseSequenceByModel(conn, root, os, adapter);
				}
			} catch (EnvironmentException e) {
				throw new SQLException(e);
			}
		}
		else if (root.getType() == TableContainer.class) {
			try{final DatabaseModelAdapter	adapter = getModelAdapter(URI.create(conn.getMetaData().getURL()));
				
				backupDatabaseTableByModel(conn, root, os, adapter);
			} catch (EnvironmentException e) {
				throw new SQLException(e);
			}
		}
	}

	public static void restoreDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final ZipInputStream is) throws SQLException, NullPointerException, IOException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root metadata can't be null");
		}
		else if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else {
			ZipEntry	ze;
			JsonNode	schema = null;
			
			while ((ze = is.getNextEntry()) != null) {
				if (".schema".equalsIgnoreCase(ze.getName())) {
					final Reader			rdr = new InputStreamReader(is);
					final JsonStaxParser	parser = new JsonStaxParser(rdr);
					
					parser.next();
					try{schema = JsonUtils.loadJsonTree(parser);
					} catch (SyntaxException e) {
						throw new IOException(e);
					}
				}
				else if (schema != null) {
					final Matcher	m = PART_PATTERN.matcher(ze.getName());
					
					if (m.find()) {
						loadImageContent(conn, m.group(1), m.group(2), m.group(3), is);
					}
					else {
						for (ContentNodeMetadata item : root) {
							if (item.getName().equals(ze.getName())) {
								final Reader			rdr = new InputStreamReader(is);
								final JsonStaxParser	parser = new JsonStaxParser(rdr);
								
								parser.next();
								loadTableContent(parser, conn, item);
								break;
							}
						}
					}
				}
			}
		}
	}	
	
	private static void loadImageContent(final Connection conn, final String table, final String key, final String field, final InputStream is) throws UnsupportedEncodingException, SQLException {
		final byte[]	decodedKey = Base64.getDecoder().decode((key + '=').getBytes());
		final String	sql = "select * from " + conn.getSchema() + "." + table +" where ci_Id = ?";
		
		try(final PreparedStatement	ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
			ps.setLong(1, Long.valueOf(new String(decodedKey)));
			try(final ResultSet				rs = ps.executeQuery();
				final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				
				Utils.copyStream(is, baos);
				rs.next();
				rs.updateBytes(field, baos.toByteArray());
				rs.updateRow();
			} catch (IOException e) {
				throw new SQLException(e);
			}
		}
	}

	private static void loadTableContent(final JsonStaxParser parser, final Connection conn, final ContentNodeMetadata node) throws IOException, SQLException {
		final String	sql = "select * from "+node.getParent().getName()+"."+node.getName();
		final boolean	isAutocommit = conn.getAutoCommit();
		String			name = null;
		
		conn.setAutoCommit(false);
		try(final Statement		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			final ResultSet		rs = stmt.executeQuery(sql)) {
			final Set<String>	bounded = new HashSet<>();
			
			for (JsonStaxParserLexType item : parser) {
				switch (item) {
					case BOOLEAN_VALUE	:
						rs.updateBoolean(name, parser.booleanValue());
						break;
					case END_ARRAY		:
						break;
					case END_OBJECT		:
						for (ContentNodeMetadata field : node) {
							if (!bounded.contains(field.getName())) {
								if (field.getFormatAssociated() != null && field.getFormatAssociated().isMandatory()) {
									rs.updateBytes(field.getName(), new byte[0]);
								}
							}
						}
						rs.insertRow();
						bounded.clear();
						break;
					case INTEGER_VALUE	:
						rs.updateLong(name, parser.intValue());
						break;
					case LIST_SPLITTER	:
						break;
					case NAME			:
						name = parser.name();
						bounded.add(name);
						break;
					case NAME_SPLITTER	:
						break;
					case NULL_VALUE		:
						rs.updateNull(name);
						break;
					case REAL_VALUE		:
						rs.updateDouble(name, parser.realValue());
						break;
					case START_ARRAY	:
						break;
					case START_OBJECT	:
						rs.moveToInsertRow();
						break;
					case STRING_VALUE	:
						rs.updateString(name, parser.stringValue());
						break;
					default :
						break;
				}
			}
			conn.commit();
		} finally {
			conn.setAutoCommit(isAutocommit);
		}
	}

	private static void backupDatabaseSchemaByModel(final Connection conn, final ContentNodeMetadata root, final ZipOutputStream os, final DatabaseModelAdapter adapter) throws SQLException, IOException {
		final ZipEntry			ze = new ZipEntry(".schema");
		final Writer			wr = new OutputStreamWriter(os);
		final JsonStaxPrinter 	prn = new JsonStaxPrinter(wr, 65536);
		boolean					theSameFirst, theSameFirst2;
		
		os.putNextEntry(ze);
		try(final Statement	stmt = conn.createStatement()) {
			
			prn.startObject();
			prn.name("schemaName").value(conn.getSchema()).splitter();
			
			theSameFirst = true;
			prn.name("sequences").startArray();
			for (ContentNodeMetadata item : root) {
				if (item.getType() == UniqueIdContainer.class) {
					if (theSameFirst) {
						theSameFirst = false;
					}
					else {
						prn.splitter();
					}
					prn.value(item.getName());
				}
			}
			prn.endArray().splitter();
			
			theSameFirst = true;
			prn.name("tables").startArray();
			for (ContentNodeMetadata item : root) {
				if (item.getType() == TableContainer.class) {
					if (theSameFirst) {
						theSameFirst = false;
					}
					else {
						prn.splitter();
					}
					prn.startObject();
					prn.name("name").value(item.getName()).splitter();
					prn.name("lobs").startArray();
					theSameFirst2 = true;
					for (ContentNodeMetadata lob : item) {
						if (lob.getType() == byte[].class) {
							if (theSameFirst2) {
								theSameFirst2 = false;
							}
							else {
								prn.splitter();
							}
							prn.startObject()
								.name(lob.getName())
								.value(getCount(stmt, "select count(" + lob.getName() + ") from "+item.getName()))
								.endObject();
						}
					}
					prn.endArray()
						.endObject();
				}
			}
			prn.endArray();			
			prn.endObject();
		} finally {
			prn.flush();
			os.closeEntry();
		}
	}
	
	private static long getCount(final Statement stmt, final String query) throws SQLException {
		try(final ResultSet	rs = stmt.executeQuery(query)) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			else {
				return 0;
			}
		}
	}

	private static void backupDatabaseSequenceByModel(final Connection conn, final ContentNodeMetadata root, final ZipOutputStream os, final DatabaseModelAdapter adapter) throws SQLException {
		// TODO Auto-generated method stub
	}

	private static void backupDatabaseTableByModel(final Connection conn, final ContentNodeMetadata root, final ZipOutputStream os, final DatabaseModelAdapter adapter) throws SQLException, IOException {
		final ZipEntry			ze = new ZipEntry(root.getName());
		final String[]			keys = buildPrimaryKeyList(root);
		final String[]			lobs = buildLobsList(root);
		final ColumnAndType[]	colsAndTypes = buildColumnAndType(root);
		final String			order = buildPrimaryKeyOrder(keys);
		final String			sql = "select * from "+root.getName()+(!order.isEmpty() ? " order by "+order : "");
		boolean					wereLobs = false;
		
		ze.setMethod(ZipEntry.DEFLATED);
		os.putNextEntry(ze);
		
		final Writer			wr = new OutputStreamWriter(os);
		final JsonStaxPrinter 	prn = new JsonStaxPrinter(wr, 65536);
		
		try{
			try(final Statement	stmt = conn.createStatement();
				final ResultSet	rs = stmt.executeQuery(sql)) {
				boolean			previousPrinted = false;
				
				while(rs.next()) {
					if (!previousPrinted) {
						previousPrinted = true;
						prn.startArray();
					}
					else {
						prn.splitter();
					}
					prn.startObject();
					wereLobs = uploadRecord(rs, colsAndTypes, prn);
					prn.endObject();
				}
				if (!previousPrinted) {
					prn.startArray();
				}
				prn.endArray();
			}
		}
		finally {
			prn.flush();
			wr.flush();
		}
		os.closeEntry();
		
		if (lobs.length > 0 && wereLobs) {
			try(final Statement	stmt = conn.createStatement();
				final ResultSet	rs = stmt.executeQuery(sql)) {
					
				while(rs.next()) {
					final String	partPrefix = root.getName()+"."+buildPrimaryKeyValues(rs, keys);
					
					for (String item : lobs) {
						uploadLob(rs, item, partPrefix, os);
					}
				}
			}
		}
	}

	
	private static ColumnAndType[] buildColumnAndType(final ContentNodeMetadata root) {
		final List<ColumnAndType>	result = new ArrayList<>();
		
		for (ContentNodeMetadata child : root) {
			final Hashtable<String, String[]>	parm = URIUtils.parseQuery(URIUtils.extractQueryFromURI(child.getApplicationPath()));
			
			if (parm.containsKey("type") && TYPES.containsKey(parm.get("type")[0])) {
				result.add(new ColumnAndType(child.getName(), TYPES.get(parm.get("type")[0])));
			}
		}
		return result.toArray(new ColumnAndType[result.size()]);
	}

	private static String[] buildPrimaryKeyList(final ContentNodeMetadata root) {
		final Map<Integer,String>	pk = new HashMap<>();
		
		for (ContentNodeMetadata child : root) {
			final Hashtable<String, String[]>	parm = URIUtils.parseQuery(URIUtils.extractQueryFromURI(child.getApplicationPath()));
			
			if (parm.containsKey("pkSeq")) {
				pk.put(Integer.valueOf(parm.get("pkSeq")[0]), child.getName());
			}
		}
		final String[]	result = new String[pk.size()];
		
		for (Entry<Integer, String> item : pk.entrySet()) {
			result[item.getKey()-1] = item.getValue();
		}
		return result;
	}

	
	private static String[] buildLobsList(final ContentNodeMetadata root) {
		final List<String>	lobs = new ArrayList<>();
		
		for (ContentNodeMetadata child : root) {
			final Hashtable<String, String[]>	parm = URIUtils.parseQuery(URIUtils.extractQueryFromURI(child.getApplicationPath()));
			
			if (parm.containsKey("type") && LOBS.contains(parm.get("type")[0])) {
				lobs.add(child.getName());
			}
		}
		return lobs.toArray(new String[lobs.size()]);
	}

	private static String buildPrimaryKeyOrder(final String[] content) {
		return String.join(", ", content);
	}

	private static String buildPrimaryKeyValues(final ResultSet rs, final String[] keys) throws SQLException {
		final StringBuilder	sb = new StringBuilder();

		for (String item : keys) {
			sb.append('.');
			try {
				final String	val = SQLUtils.convert(String.class, rs.getObject(item));
				final String	encoded = Base64.getEncoder().encodeToString(val.getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING));
				
				if (encoded.endsWith("=")) {
					sb.append(encoded, 0, encoded.length()-1);
				}
				else {
					sb.append(encoded);
				}
			} catch (ContentException | UnsupportedEncodingException e) {
				throw new SQLException(e);
			}
		}
		return sb.substring(1);
	}

	private static boolean uploadRecord(final ResultSet rs, final ColumnAndType[] content, final JsonStaxPrinter prn) throws IOException, SQLException {
		boolean theSameFirst = true;
		
		for (ColumnAndType item : content) {
			if (theSameFirst) {
				theSameFirst = false;
			}
			else {
				prn.splitter();
			}
			prn.name(item.column);
			switch (item.type) {
				case INTEGER	:
					final long	longVal = rs.getLong(item.column);
					
					if (rs.wasNull()) {
						prn.nullValue();
					}
					else {
						prn.value(longVal);
					}
					break;
				case STRING		:
					final String	stringVal = rs.getString(item.column);
					
					if (rs.wasNull()) {
						prn.nullValue();
					}
					else {
						prn.value(stringVal);
					}
					break;
				default :
					throw new UnsupportedOperationException("Column type [" + item.type + "] is not supported yet");
			}
		}
		return true;
	}

	private static void uploadLob(final ResultSet rs, final String lobColumnName, final String namePrefix, final ZipOutputStream os) throws IOException, SQLException {
		try(final InputStream	is = rs.getBinaryStream(lobColumnName)) {
			final ZipEntry		ze = new ZipEntry(namePrefix+"@"+lobColumnName);
			
			ze.setMethod(ZipEntry.DEFLATED);
			os.putNextEntry(ze);
			Utils.copyStream(is, os);
			os.closeEntry();
		}
	}

	
	public static enum RestoreMode {
		STRUCTURE_ONLY,
		DATA_ONLY,
		STRUCTURE_AND_DATA
	}
	
	public static void restoreDatabaseByModel(final Connection conn, final ContentNodeMetadata root, final ZipInputStream is, final RestoreMode mode) throws SQLException, NullPointerException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (root == null) {
			throw new NullPointerException("Root metadata can't be null");
		}
		else if (is == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else if (mode == null) {
			throw new NullPointerException("Restore mode can't be null");
		}
		else if (root.getType() == SchemaContainer.class) {
			restoreDatabaseSchemaByModel(conn, root, is, mode);
			for (ContentNodeMetadata item : root) {
				restoreDatabaseByModel(conn, item, is, mode);
			}
		}
		else if (root.getType() == UniqueIdContainer.class) {
			restoreDatabaseSequenceByModel(conn, root, is, mode);
		}
		else if (root.getType() == TableContainer.class) {
			restoreDatabaseTableByModel(conn, root, is, mode);
		}
	}
	
	private static void restoreDatabaseSchemaByModel(final Connection conn, final ContentNodeMetadata root, final ZipInputStream is, final RestoreMode mode) throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	private static void restoreDatabaseSequenceByModel(final Connection conn, final ContentNodeMetadata root, final ZipInputStream is, final RestoreMode mode) throws SQLException {
		// TODO Auto-generated method stub
		
	}


	private static void restoreDatabaseTableByModel(final Connection conn, final ContentNodeMetadata root, final ZipInputStream is, final RestoreMode mode) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	private static String replaceSchemaAndEscape(final String quote, final String source, final String newSchema) throws SQLException {
		final int		dotIndex = source.indexOf('.'); 
		
		if (dotIndex > 0) {
			return quote + newSchema + quote + '.' + quote + source.substring(dotIndex+1) + quote; 
		}
		else {
			return quote + newSchema + quote + '.' + quote + source + quote;
		}
	}
	
	private static String escape(final String quote, final String source) throws SQLException {
		return quote + source + quote;
	}

	private static void executeSQL(final Connection conn, final String sql) throws SQLException {
		try(final Statement	stmt = conn.createStatement()) {
			
			stmt.executeUpdate(sql);
		} catch (SQLException exc) {
			throw new SQLException((exc.getCause() != null ? exc.getCause().getLocalizedMessage() : exc.getLocalizedMessage()) + "\nStatement: "+sql); 
		}
	}
	
	
	@FunctionalInterface
	public interface ConnectionGetter {
		Connection getConnection() throws SQLException;
	}
	
	public static <Key,Data> ORMModelMapper<Key, Data> buildORMModelMapper(final ContentNodeMetadata root, final ContentNodeMetadata dbRoot, final char[] nameTerminals, final ConnectionGetter connGetter, final SimpleURLClassLoader loader) throws ContentException, NullPointerException, IllegalArgumentException {
		if (root == null) {
			throw new NullPointerException("Class content node metadata can't be null");
		}
		else if (dbRoot == null) {
			throw new NullPointerException("Database content node metadata can't be null");
		}
		else if (nameTerminals == null || nameTerminals.length != 2) {
			throw new IllegalArgumentException("Name terminals can't be empty array and must contain exactly two chars");
		}
		else if (connGetter == null) {
			throw new NullPointerException("Connection getter can't be null");
		}
		else if (loader == null) {
			throw new NullPointerException("Class path can't be null");
		}
		else {
			final Set<String>			classNames = new HashSet<>(), dbNames = new HashSet<>(), primaryKeys = new HashSet<>(), bugs = new HashSet<>();
			
			for (ContentNodeMetadata item : root) {
				classNames.add(item.getName().toUpperCase());
			}
			for (ContentNodeMetadata item : dbRoot) {
				if (item.getName().endsWith("/primaryKey")) {
					primaryKeys.add(item.getName().substring(0,item.getName().lastIndexOf("/primaryKey")).toUpperCase());
				}
				else {
					dbNames.add(item.getName().toUpperCase());
				}
			}
			classNames.retainAll(dbNames);
			
			for (String item : primaryKeys) {
				if  (!classNames.contains(item)) {
					bugs.add(item);
				}
			}
			if (!bugs.isEmpty()) {
				throw new ContentException("Database ["+dbRoot.getName()+"] model contains primary keys "+bugs+", that have no corresponding fields in the class ["+root.getName()+"] model");
			}
			
			final Class<?>	clazz = root.getType();
			
			final String	insert = buildInsertStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			final String	read = buildReadStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			final String	update = buildUpdateStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			final String	delete = buildDeleteStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			final String	select = buildSelectStmt(dbRoot,classNames,primaryKeys,nameTerminals);
			
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final AsmWriter				wr = ASM_WRITER.clone(baos)) {
				final int 					unique = AI.incrementAndGet();
				final String				className = SQLModelUtils.class.getPackageName()+".ORMMapper"+unique;
				
				wr.println(" printImports parentName=\""+clazz.getCanonicalName()+"\"");
				wr.println(" printClassHeader parentName=\""+clazz.getCanonicalName()+"\",className=\""+className+"\"");
				wr.println("insertString printStaticString content=\""+insert+"\"");
				wr.println("readString printStaticString content=\""+read+"\"");
				wr.println("updateString printStaticString content=\""+update+"\"");
				wr.println("deleteString printStaticString content=\""+delete+"\"");
				wr.println("selectString printStaticString content=\""+select+"\"");

				wr.println(" printConstructor className=\""+className+"\"");

				wr.println(" printCreate className=\""+className+"\",");
				
				wr.println(" printClassEnd className=\""+className+"\"");				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	static String buildInsertStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		final StringBuilder	sb = new StringBuilder("insert into ").append(nameTerminals[0]).append(root.getName()).append(nameTerminals[0]);
		char				prefix = '('; 
		
		for (String item : fields) {
			sb.append(prefix).append(nameTerminals[0]).append(item).append(nameTerminals[0]);
			prefix = ',';
		}
		
		sb.append(") values ");
		prefix = '('; 
		for (int index = 0, maxIndex = fields.size(); index < maxIndex; index++) {
			sb.append(prefix).append('?');
			prefix = ',';
		}
		return sb.append(')').toString();
	}
	
	static String buildReadStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	static String buildUpdateStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	static String buildDeleteStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	static String buildSelectStmt(final ContentNodeMetadata root, final Set<String> fields, final Set<String> pimaryKeys, final char[] nameTerminals) throws ContentException {
		// TODO Auto-generated method stub
		return null;
	}

	static class ORMModelMapperImpl<Key, Data> implements ORMModelMapper<Key, Data> {
		private final LightWeightRWLockerWrapper	wrapper = new LightWeightRWLockerWrapper();
		private final String						insertStmt, readStmt, updateStmt, deleteStmt;
		
		ORMModelMapperImpl(final Connection conn, final String insertStmt, final String readStmt, final String updateStmt, final String deleteStmt) {
			this.insertStmt = insertStmt;
			this.readStmt = readStmt;
			this.updateStmt = updateStmt;
			this.deleteStmt = deleteStmt;
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void create(final Data content) throws SQLException {
			// TODO Auto-generated method stub
			if (content == null) {
				throw new NullPointerException("Content to insert can't be null");
			}
			else {
				PreparedStatement	ps = getStatement(null, insertStmt);
				
				if (ps == null) {
					ps = createStatement(null, insertStmt);
				}
			}
		}

		@Override
		public void read(Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void read(Key key, Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void update(Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(Data content) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public long forEach(Data content, ForEachCallback<Data> callback, String where, String order, int offset, int limit) throws ContentException, SQLException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		protected PreparedStatement getStatement(final Connection conn, final String statement) {
			return null;
		}

		protected PreparedStatement createStatement(final Connection conn, final String statement) {
			return null;
		}
	}

	static DatabaseModelAdapter getModelAdapter(final URI resource) throws EnvironmentException {
		for (DatabaseModelAdapter item : ServiceLoader.load(DatabaseModelAdapter.class)) {
			if (item.canServe(resource) && !(item instanceof DefaultDatabaseModelAdapter)) {
				return item.newInstance(resource);
			}
		}
		return DEFAULT_MODEL_ADAPTER.newInstance(resource);
	}
	
	private static class ColumnAndType {
		private final String		column;
		private final JsonValueType type;
		
		public ColumnAndType(final String column, final JsonValueType type) {
			this.column = column;
			this.type = type;
		}

		@Override
		public String toString() {
			return "ColumnAndType [column=" + column + ", type=" + type + "]";
		}
	}
}
