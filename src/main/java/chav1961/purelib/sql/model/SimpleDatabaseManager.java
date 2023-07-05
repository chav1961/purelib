package chav1961.purelib.sql.model;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.SimpleContentMetadata;
import chav1961.purelib.model.TableContainer;
import chav1961.purelib.model.UniqueIdContainer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.sql.interfaces.InstanceManager;
import chav1961.purelib.sql.model.SQLModelUtils.ConnectionGetter;
import chav1961.purelib.sql.model.interfaces.DatabaseManagement;
import chav1961.purelib.sql.model.interfaces.DatabaseModelManagement;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.byte2byte.SQLDataOutputStream;

public class SimpleDatabaseManager<T extends Comparable<T>> implements AutoCloseable, NodeMetadataOwner {
	private static final String						KEY_VERSIONING_SCHEMA = "purelib.db.versioning.schema";

	private static final String						MSG_BACKUP_DATABASE_SCHEMA = "chav1961.purelib.sql.model.SimpleDatabaseManager.backupDatabaseSchema";
	private static final String						MSG_BACKUP_MODEL = "chav1961.purelib.sql.model.SimpleDatabaseManager.backupModel";
	private static final String						MSG_BACKUP_ENTITY = "chav1961.purelib.sql.model.SimpleDatabaseManager.backupEntity";
	
	private static final String						VERSION_MODEL_URI = "model.json";
	private static final String						VERSION_TABLE = "dbversion";
	private static final String						VERSION_ID_FIELD = "dbv_Id";
	private static final String						VERSION_GUID_FIELD = "dbv_GUID";
	private static final String						VERSION_VERSION_FIELD = "dbv_Version";
	private static final String						VERSION_MODEL_FIELD = "dbv_Model";
	private static final String						VERSION_CREATED_FIELD = "dbv_Created";
	private static final String						PART_MODEL = "model";
	private static final ContentNodeMetadata		VERSION_MODEL;
	private static final ContentMetadataInterface	VERSION_MODEL_CONTAINER;

	static {
		try(final InputStream		is = SimpleDatabaseManager.class.getResourceAsStream(VERSION_MODEL_URI);
			final Reader			rdr = new InputStreamReader(is);
			final JsonStaxParser	parser = new JsonStaxParser(rdr)) {

			parser.next();
			VERSION_MODEL = ModelUtils.deserializeFromJson(parser);
			VERSION_MODEL_CONTAINER = new SimpleContentMetadata(VERSION_MODEL);
		} catch (IOException e) {
			throw new PreparationException("Initialization of SimpleDatabaseManager class failed: "+e.getLocalizedMessage(), e);
		}
	}
	
	@FunctionalInterface
	public interface DatabaseManagementGetter<T extends Comparable<T>> {
		DatabaseManagement<T> getManagementInterface(final Connection conn) throws SQLException;
	}

	public static enum DatabaseVersioningSchema {
		SHARED, PRIVATE
	}
	
	private final LoggerFacade					logger;
	private final UUID							applicationId;
	private final DatabaseModelManagement<T>	modelMgmt;
	private final ConnectionGetter				connGetter;
	private final DatabaseManagementGetter<T>	mgmtGetter;

	public SimpleDatabaseManager(final LoggerFacade logger, final DatabaseModelManagement<T> model, final ConnectionGetter connGetter, final DatabaseManagementGetter<T> mgmtGetter) throws SQLException, ContentException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (model == null) {
			throw new NullPointerException("Database model can't be null"); 
		}
		else if (connGetter == null) {
			throw new NullPointerException("Database connection getter can't be null"); 
		}
		else if (mgmtGetter == null) {
			throw new NullPointerException("Database management getter can't be null"); 
		}
		else {
			this.logger = logger;
			this.modelMgmt = model;
			this.connGetter = connGetter;
			this.mgmtGetter = mgmtGetter;
			this.applicationId = extractUUIDFromModel(model);
			
			try(final Connection	conn = connGetter.getConnection();
				final LoggerFacade	trans = logger.transaction(this.getClass().getCanonicalName())) {
				final DatabaseManagement<T>	mgmt = mgmtGetter.getManagementInterface(conn);
				final String		versioningSchema = PureLibSettings.instance().getProperty(KEY_VERSIONING_SCHEMA, DatabaseVersioningSchema.class) == DatabaseVersioningSchema.SHARED ? VERSION_MODEL.getName() : model.getTheSameLastModel().getName();
				
				conn.setSchema(versioningSchema);
				conn.setAutoCommit(false);
				
				if (isDatabasePrepared4Versioning(conn, versioningSchema)) {
					trans.message(Severity.info, "Version table ["+VERSION_TABLE+"] found in the database, schema="+conn.getSchema());
					conn.setSchema(model.getTheSameLastModel().getName());
					
					final ContentNodeMetadata	oldModel = loadLastDbVersion(conn, VERSION_MODEL.getChild(VERSION_TABLE), versioningSchema, applicationId);
					final T						oldVersion = oldModel == null ? mgmt.getInitialVersion() : mgmt.getVersion(oldModel);
					final T						currentVersion = model.getTheSameLastVersion(); 
					final int					result = currentVersion != null ? currentVersion.compareTo(oldVersion) : -1;
					
					if (result > 0) {
						if (oldModel == null) {
							trans.message(Severity.info, "No any old versions in the database, create will be called");
							mgmt.onCreate(conn, model.getTheSameLastModel());
							trans.message(Severity.info, "Creation completed, inital version is ["+currentVersion+"]");
							storeCurrentDbVersion(conn, applicationId, VERSION_MODEL.getChild(VERSION_TABLE), model.getTheSameLastModel(), versioningSchema, currentVersion);
							trans.message(Severity.info, "Database version was changed to ["+currentVersion+"]");
						}
						else {
							trans.message(Severity.info, "Current model version ["+currentVersion+"] is newer than database version ["+oldVersion+"], upgrade will be called");
							mgmt.onUpgrade(conn, currentVersion, model.getTheSameLastModel(), oldVersion, oldModel);
							trans.message(Severity.info, "Upgrade to version ["+currentVersion+"] completed");
							storeCurrentDbVersion(conn, applicationId, VERSION_MODEL.getChild(VERSION_TABLE), model.getTheSameLastModel(), versioningSchema, currentVersion);
							trans.message(Severity.info, "Database version was changed to ["+currentVersion+"]");
						}
					}
					else if (result < 0) {
						trans.message(Severity.info, "Current model version ["+currentVersion+"] is older  than database version ["+oldVersion+"], downgrade will be called");
						mgmt.onDowngrade(conn, currentVersion, model.getTheSameLastModel(), oldVersion, oldModel);
						trans.message(Severity.info, "Downgrade to version ["+currentVersion+"] completed");
						storeCurrentDbVersion(conn, applicationId, VERSION_MODEL.getChild(VERSION_TABLE), model.getTheSameLastModel(), model.getTheSameLastModel().getName(), currentVersion);
						trans.message(Severity.info, "Database version was changed to ["+currentVersion+"]");
					}
				}
				else {
					try{trans.message(Severity.info, "Version table ["+VERSION_TABLE+"] not found in the database, schema="+versioningSchema+". Version infrastructure will be prepared now");
						createDbVersionInfrastructure(conn, VERSION_MODEL_CONTAINER.getRoot(), versioningSchema);
						trans.message(Severity.info, "Version infrastructure was prepared in the database, schema="+versioningSchema+", create model infrastructure will be called now");
						conn.setSchema(model.getTheSameLastModel().getName());
						mgmt.onCreate(conn, model.getTheSameLastModel());
						trans.message(Severity.info, "Create model infrastructure completed, schema="+conn.getSchema()+", version ["+mgmt.getInitialVersion()+"]");
						storeCurrentDbVersion(conn, applicationId, VERSION_MODEL.getChild(VERSION_TABLE), model.getTheSameLastModel(), versioningSchema, model.getTheSameLastVersion());
						trans.message(Severity.info, "Database version stored, version ["+model.getTheSameLastVersion()+"]");
					} catch (SQLException exc) {
//						exc.printStackTrace(); 
						trans.message(Severity.error, exc, "Exception was detected, database will be rolled back");
						removeDbVersionInfrastructure(conn, model.getTheSameLastModel(), model.getTheSameLastModel().getName());
						trans.message(Severity.info, exc, "Database rollback completed");
					}
				}
				conn.commit();
				trans.rollback();
			}
		}
	}

	@Override
	public void close() throws SQLException {
		// TODO Auto-generated method stub
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return modelMgmt.getTheSameLastModel();
	}
	
	public Connection getConnection() throws SQLException {
		final Connection	conn = connGetter.getConnection();
		
		mgmtGetter.getManagementInterface(conn).onOpen(conn, modelMgmt.getTheSameLastModel());
		return conn;
	}
	
	public ContentNodeMetadata getCurrentDatabaseModel() throws SQLException {
		return loadLastDbVersion(getConnection(), VERSION_MODEL.getChild(VERSION_TABLE), modelMgmt.getTheSameLastModel().getName(), applicationId);
	}

	public DatabaseManagement<T> getManagement() throws SQLException {
		return mgmtGetter.getManagementInterface(getConnection());
	}
	
	public boolean validateDatabase() throws SQLException {
		return true;
	}
	
	public void backup(final ZipOutputStream zos) throws IOException, SQLException {
		backup(zos, (e)->true);
	}

	public void backup(final ZipOutputStream zos, final Predicate<ContentNodeMetadata> pred) throws IOException, SQLException {
		backup(zos, pred, ProgressIndicator.DUMMY);
	}

	public void backup(final ZipOutputStream zos, final Predicate<ContentNodeMetadata> pred, final ProgressIndicator progress) throws IOException, SQLException {
		final ContentNodeMetadata		meta = getNodeMetadata();
		final String					model = modelToString(meta);
		final Set<ContentNodeMetadata>	toBackup = new HashSet<>();
		
		for (ContentNodeMetadata item : meta) {
			if (pred.test(item)) {
				toBackup.add(item);
			}
		}
		
		if (!toBackup.isEmpty()) {
			final Connection			conn = connGetter.getConnection();
			
			progress.start(PureLibSettings.PURELIB_LOCALIZER.getValue(MSG_BACKUP_DATABASE_SCHEMA, meta.getName()), toBackup.size() + 1);
			try {
				final ZipEntry 	ze  = new ZipEntry(PART_MODEL);
				
				ze.setMethod(ZipEntry.DEFLATED);
				zos.putNextEntry(ze);
				
				final Writer	wr = new OutputStreamWriter(zos, PureLibSettings.DEFAULT_CONTENT_ENCODING);
				int				step = 1;
				
				try {
					progress.stage(PureLibSettings.PURELIB_LOCALIZER.getValue(MSG_BACKUP_MODEL), step++, meta.getChildrenCount()+1);
					Utils.copyStream(new ByteArrayInputStream(model.getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING)), zos, progress);
					wr.flush();
				} finally {
					progress.endStage();
					zos.closeEntry();
				}
				
				for (ContentNodeMetadata item : toBackup) {
					try {
						if (item.getType() == UniqueIdContainer.class) {
							progress.stage(PureLibSettings.PURELIB_LOCALIZER.getValue(MSG_BACKUP_ENTITY, item.getName()), step++, meta.getChildrenCount()+1);
							backupSequence(conn, zos, item, progress);
						}
						else if (item.getType() == TableContainer.class) {
							progress.stage(PureLibSettings.PURELIB_LOCALIZER.getValue(MSG_BACKUP_ENTITY, item.getName()), step++, meta.getChildrenCount()+1, calculateTableSize(conn, item, meta.getName()));
							backupTable(conn, zos, item, meta.getName(), progress);
						}
						else {
							throw new UnsupportedOperationException(); 
						}
					} finally {
						progress.endStage();
					}
				}
			} finally {
				progress.end();
			}
		}
	}
	
	private void backupSequence(final Connection conn, final ZipOutputStream zos, final ContentNodeMetadata item, ProgressIndicator progress) throws IOException {
		final ZipEntry	ze = new ZipEntry(item.getName());
		
		ze.setMethod(ZipEntry.DEFLATED);
		zos.putNextEntry(ze);
		zos.closeEntry();
	}

	private long calculateTableSize(final Connection conn, final ContentNodeMetadata item, final String schema) throws IOException {
		try(final Statement		stmt = conn.createStatement();
			final ResultSet		rs = stmt.executeQuery(SQLModelUtils.buildSelectCountStatementByModel(conn, item, schema))){
		
			if (rs.next()) {
				return rs.getLong(1);
			}
			else {
				return -1;
			}
		} catch (SQLException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		}
	}
	
	private void backupTable(final Connection conn, final ZipOutputStream zos, final ContentNodeMetadata item, final String schema, final ProgressIndicator progress) throws IOException {
		final ZipEntry	ze = new ZipEntry(item.getName());
		
		ze.setMethod(ZipEntry.DEFLATED);
		zos.putNextEntry(ze);
		
		try(final Statement				stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet				rs = stmt.executeQuery(SQLModelUtils.buildSelectAllStatementByModel(conn, item, schema))){
			final ResultSetMetaData		rsmd = rs.getMetaData();
			final SQLDataOutputStream	sqlo = new SQLDataOutputStream(zos);
			final int					columnCount = rsmd.getColumnCount();
					
			long				processed = 0;
		
			for (int index = 1; index <= columnCount; index++) {
				sqlo.writeChars(rsmd.getColumnName(index));
			}
			sqlo.writeLine();
			
			while (rs.next()) {
				for (int index = 1; index <= columnCount; index++) {
					sqlo.write(rsmd.getColumnType(index),rs.getObject(index));
				}
				sqlo.writeLine();
				progress.processed(processed++);
			}
			sqlo.writeLine();
			sqlo.flush();
		} catch (SQLException e) {
			throw new IOException(e.getLocalizedMessage(), e);
		} finally {
			zos.closeEntry();
		}
	}
	
	
	public void restore(final ZipInputStream zis) {
		restore(zis, (e)->true);
	}

	public void restore(final ZipInputStream zis, final Predicate<ContentNodeMetadata> pred) {
		restore(zis, pred, ProgressIndicator.DUMMY);
	}

	public void restore(final ZipInputStream zis, final Predicate<ContentNodeMetadata> pred, final ProgressIndicator progress) {
		
	}

	public static boolean isDatabasePrepared4Versioning(final Connection conn, final String schema) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (schema == null) {
			throw new NullPointerException("Schema can't be null");
		}
		else {
			final String	oldSchema = conn.getSchema();
		
			try{conn.setSchema(schema);
			
				try(final ResultSet	rs = conn.getMetaData().getTables(null, schema, VERSION_TABLE, new String[]{"TABLE"})) {
					if (rs.next()) {
						return true;
					}
				}
				return false;								
			} finally {
				conn.setSchema(oldSchema);
			}
		}
	}
	
	public static void prepareDatabase4Versioning(final Connection conn, final String schema) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null");
		}
		else if (schema == null) {
			throw new NullPointerException("Schema can't be null");
		}
		else {
			createDbVersionInfrastructure(conn, VERSION_MODEL, VERSION_MODEL.getName());
		}
	}
	
	
	private static String modelToString(final ContentNodeMetadata model) throws SQLException {
		try(final Writer			wr = new StringWriter();
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
			
			ModelUtils.serializeToJson(model, prn);
			prn.flush();
			return wr.toString();
		} catch (IOException e) {
			throw new SQLException(e); 
		}
	}
	
	private static void createDbVersionInfrastructure(final Connection conn, final ContentNodeMetadata model, final String schema) throws SQLException {
		SQLModelUtils.createDatabaseByModel(conn, model, schema);
		conn.commit();
	}

	private static ContentNodeMetadata loadLastDbVersion(final Connection conn, final ContentNodeMetadata versionModel, final String schema, final UUID applicationId) throws SQLException {
		final String	versionSchema; 
		
		try(final ResultSet	rs = conn.getMetaData().getTables(null, schema, VERSION_TABLE, null)) {
			if (rs.next()) {
				versionSchema = schema;
			}
			else {
				versionSchema = "purelib"; 
			}
		}
		
		final String	select = SQLModelUtils.buildSelectAllStatementByModel(conn, versionModel, versionSchema)+" where \"dbv_GUID\" = ? order by 1 desc";
		
		try(final PreparedStatement			stmt = conn.prepareStatement(select);
			final VersionInstanceManager	vim = new VersionInstanceManager()) {
		
			stmt.setString(1, applicationId.toString());

			try(final ResultSet				rs = stmt.executeQuery()) {
				if (rs.next()) {
					final VersionRecord		vr = vim.newInstance();
					
					vim.loadInstance(rs, vr);
					try(final Reader			rdr = new StringReader(vr.dbv_Model);
						final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
						
						parser.next();
						return ModelUtils.deserializeFromJson(parser);
					} catch (IOException e) {
						throw new SQLException(e); 
					}
				}
				else {
					return null;
				}
			}
		}
	}
	
	private static <T extends Comparable<T>> void storeCurrentDbVersion(final Connection conn, final UUID applicationId, final ContentNodeMetadata versionModel, final ContentNodeMetadata model, final String schema, final T modelVersion) throws SQLException {
		if (conn.getMetaData().supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
			final String	select = SQLModelUtils.buildSelectAllStatementByModel(conn, versionModel, schema);

			try(final Statement					stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				final ResultSet					rs = stmt.executeQuery(select);
				final VersionInstanceManager	vim = new VersionInstanceManager()) {
				final VersionRecord				vr = vim.newInstance();
				
				vr.dbv_Id = vim.newKey();
				vr.dbv_GUID = applicationId;
				vr.dbv_Version = modelVersion.toString();
				vr.dbv_Model = modelToString(model);
				vr.dbv_Created = new Date(System.currentTimeMillis());

				rs.moveToInsertRow();
				vim.storeInstance(rs, vr, false);
				rs.insertRow();
				conn.commit();
			}
		}
		else {
			final String	insert = SQLModelUtils.buildInsertValuesStatementByModel(conn, versionModel, schema);

			try(final PreparedStatement			ps = conn.prepareStatement(insert);
				final VersionInstanceManager	vim = new VersionInstanceManager()) {
				final VersionRecord				vr = vim.newInstance();
				
				vr.dbv_Id = vim.newKey();
				vr.dbv_GUID = applicationId;
				vr.dbv_Version = modelVersion != null ? modelVersion.toString() : "";
				vr.dbv_Model = modelToString(model);
				vr.dbv_Created = new Date(System.currentTimeMillis());

				vim.storeInstance(ps, vr, false);
				ps.executeUpdate();
				conn.commit();
			}
		}
	}

	private static void removeDbVersionInfrastructure(final Connection conn, final ContentNodeMetadata model, final String schema) throws SQLException {
		SQLModelUtils.removeDatabaseByModel(conn, model, schema, true);
		conn.commit();
	}

	private static <S extends Comparable<S>> UUID extractUUIDFromModel(final DatabaseModelManagement<S> model) {
		final Hashtable<String, String[]> 	keys = URIUtils.parseQuery(model.getTheSameLastModel().getApplicationPath());

		if (keys.containsKey("UUID")) {
			return UUID.fromString(keys.get("UUID")[0]);
		}
		else {
			throw new IllegalArgumentException("Model root ["+model+"] doesn't contain mandatory 'UUID' clause in the query string of the application URI");
		}
	}

	
	private static class VersionRecord implements Cloneable {
		private long	dbv_Id;
		private UUID	dbv_GUID;
		private String	dbv_Version;
		private String	dbv_Model;
		private Date	dbv_Created;
		
		@Override
		public VersionRecord clone() throws CloneNotSupportedException {
			return (VersionRecord)super.clone();
		}

		@Override
		public String toString() {
			return "VersionRecord [dbv_Id=" + dbv_Id + ", dbv_GUID=" + dbv_GUID + ", dbv_Version=" + dbv_Version + ", dbv_Model=" + dbv_Model + ", dbv_Created=" + dbv_Created + "]";
		}
	}
	
	private static class VersionInstanceManager implements InstanceManager<Long, VersionRecord> {
		private VersionInstanceManager() {
		}

		@Override
		public Class<?> getInstanceType() {
			return VersionRecord.class;
		}

		@Override
		public Class<?> getKeyType() {
			return Long.class;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public VersionRecord newInstance() throws SQLException {
			return new VersionRecord();
		}

		@Override
		public Long newKey() throws SQLException {
			return System.currentTimeMillis();
		}

		@Override
		public Long extractKey(final VersionRecord inst) throws SQLException {
			return inst.dbv_Id;
		}

		@Override
		public VersionRecord clone(final VersionRecord inst) throws SQLException {
			try{return inst.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e); 
			}
		}

		@Override
		public void loadInstance(final ResultSet rs, final VersionRecord inst) throws SQLException {
			inst.dbv_Id = rs.getLong(1);
			inst.dbv_GUID = UUID.fromString(rs.getString(2));
			inst.dbv_Version = rs.getString(3);
			inst.dbv_Model = rs.getString(4);
			inst.dbv_Created = rs.getDate(5);
		}

		@Override
		public void storeInstance(ResultSet rs, VersionRecord inst, boolean update) throws SQLException {
			if (!update) {
				rs.updateLong(1, inst.dbv_Id);
			}
			rs.updateString(2, inst.dbv_GUID.toString());
			rs.updateString(3, inst.dbv_Version);
			rs.updateString(4, inst.dbv_Model);
			rs.updateDate(5, new java.sql.Date(inst.dbv_Created.getTime()));
		}

		@Override
		public void storeInstance(PreparedStatement ps, VersionRecord inst, boolean update) throws SQLException {
			if (!update) {
				ps.setLong(1, inst.dbv_Id);
				ps.setString(2, inst.dbv_GUID.toString());
				ps.setString(3, inst.dbv_Version);
				ps.setString(4, inst.dbv_Model);
				ps.setDate(5, new java.sql.Date(inst.dbv_Created.getTime()));
			}
			else {
				ps.setString(1, inst.dbv_GUID.toString());
				ps.setString(2, inst.dbv_Version);
				ps.setString(3, inst.dbv_Model);
				ps.setDate(4, new java.sql.Date(inst.dbv_Created.getTime()));
				ps.setLong(5, inst.dbv_Id);
			}
		}
		
		@Override
		public <T> T get(final VersionRecord inst, final String name) throws SQLException {
			switch (name) {
				case VERSION_ID_FIELD		: return (T)Long.valueOf(inst.dbv_Id);
				case VERSION_GUID_FIELD		: return (T)inst.dbv_GUID;
				case VERSION_VERSION_FIELD	: return (T)inst.dbv_Version;
				case VERSION_MODEL_FIELD	: return (T)inst.dbv_Model;
				case VERSION_CREATED_FIELD	: return (T)inst.dbv_Created;
				default : throw new SQLException("Field name ["+name+"] doesn't exist");
			}
		}

		@Override
		public <T> InstanceManager<Long, VersionRecord> set(final VersionRecord inst, final String name, final T value) throws SQLException {
			switch (name) {
				case VERSION_ID_FIELD		: 
					inst.dbv_Id = (Long)value;
					break;
				case VERSION_GUID_FIELD	:
					inst.dbv_GUID = (UUID)value;
					break;
				case VERSION_VERSION_FIELD	:
					inst.dbv_Version = (String)value;
					break;
				case VERSION_MODEL_FIELD	: 
					inst.dbv_Model = (String)value;
					break;
				case VERSION_CREATED_FIELD	: 
					inst.dbv_Created = (Date)value;
					break;
				default : 
					throw new SQLException("Field name ["+name+"] doesn't exist");
			}
			return this;
		}

		@Override
		public void close() throws SQLException {
		}

		@Override
		public void assignKey(VersionRecord inst, Long key) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Long extractKey(ResultSet rs) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
