package chav1961.purelib.sql.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.sql.interfaces.InstanceManager;
import chav1961.purelib.sql.model.SQLModelUtils.ConnectionGetter;
import chav1961.purelib.sql.model.interfaces.DatabaseManagement;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class SimpleDatabaseManager<T extends Comparable<T>> implements AutoCloseable, NodeMetadataOwner {
	private static final String					VERSION_MODEL_URI = "model.json";
	private static final ContentNodeMetadata	VERSION_MODEL;
	private static final String					VERSION_TABLE = "dbversion";
	private static final String					PART_MODEL = "model";

	static {
		try(final InputStream		is = SimpleDatabaseManager.class.getResourceAsStream(VERSION_MODEL_URI);
			final Reader			rdr = new InputStreamReader(is);
			final JsonStaxParser	parser = new JsonStaxParser(null)) {

			parser.next();
			VERSION_MODEL = ModelUtils.deserializeFromJson(null);
		} catch (IOException e) {
			throw new PreparationException("Initialization of SimpleDatabaseManager class failed: "+e.getLocalizedMessage(), e);
		}
	}
	
	@FunctionalInterface
	public interface DatabaseManagementGetter<T extends Comparable<T>> {
		DatabaseManagement<T> getManagementInterface(final Connection conn) throws SQLException;
	}

	private final LoggerFacade					logger;
	private final ContentNodeMetadata 			model;
	private final ConnectionGetter				connGetter;
	private final DatabaseManagementGetter<T>	mgmtGetter;
	
	public SimpleDatabaseManager(final LoggerFacade logger, final ContentNodeMetadata model, final ConnectionGetter connGetter, final DatabaseManagementGetter<T> mgmtGetter) throws SQLException, ContentException {
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
			this.model = model;
			this.connGetter = connGetter;
			this.mgmtGetter = mgmtGetter;
			
			try(final Connection	conn = connGetter.getConnection();
				final LoggerFacade	trans = logger.transaction(this.getClass().getCanonicalName())) {

				final DatabaseManagement<T>	mgmt = mgmtGetter.getManagementInterface(conn);
				try(final ResultSet			rs = conn.getMetaData().getTables(null, conn.getSchema(), VERSION_TABLE, new String[]{"TABLE"})) {
					
					if (rs.next()) {
						final ContentNodeMetadata	oldModel = loadLastDbVersion(conn, VERSION_MODEL, model.getName());
						final int					result = mgmt.getVersion(model).compareTo(mgmt.getVersion(oldModel));
						
						if (result > 0) {
							mgmt.onUpgrade(mgmt.getVersion(model), model, mgmt.getVersion(oldModel), oldModel);
							storeCurrentDbVersion(conn, VERSION_MODEL, model, model.getName(), mgmt.getVersion(model));
						}
						else if (result < 0) {
							mgmt.onDowngrade(mgmt.getVersion(model), model, mgmt.getVersion(oldModel), oldModel);
							storeCurrentDbVersion(conn, VERSION_MODEL, model, model.getName(), mgmt.getVersion(model));
						}
					}
					else {
						try{createDbVersionTable(conn, VERSION_MODEL, model.getName());
							mgmt.onCreate(model);
							storeCurrentDbVersion(conn, VERSION_MODEL, model, model.getName(), mgmt.getVersion(model));
						} catch (SQLException exc) {
							removeDbVersionTable(conn, model, model.getName());
						}
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
		return model;
	}
	
	public Connection getConnection() throws SQLException {
		return null;
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
		ZipEntry						ze;
		
		for (ContentNodeMetadata item : meta) {
			if (pred.test(item)) {
				toBackup.add(item);
			}
		}
		try {
			progress.start("", toBackup.size() + 1);
			ze  = new ZipEntry(PART_MODEL);
			ze.setMethod(ZipEntry.DEFLATED);
			zos.putNextEntry(ze);
			
			final Writer	wr = new OutputStreamWriter(zos, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			
			try {
				progress.stage("", 1, 3);
				Utils.copyStream(null, zos, progress);
				wr.flush();
			} finally {
				progress.endStage();
			}
		} finally {
			progress.end();
		}
//		
//		ze.setMethod(ZipEntry.DEFLATED);
//		zos.putNextEntry(ze);
//		Utils.copyStream(null, zos, progress);
	}
	
	public void restore(final ZipInputStream zis) {
		restore(zis, (e)->true);
	}

	public void restore(final ZipInputStream zis, final Predicate<ContentNodeMetadata> pred) {
		restore(zis, pred, ProgressIndicator.DUMMY);
	}

	public void restore(final ZipInputStream zis, final Predicate<ContentNodeMetadata> pred, final ProgressIndicator progress) {
		
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
	
	private static void createDbVersionTable(final Connection conn, final ContentNodeMetadata model, final String schema) throws SQLException {
		SQLModelUtils.createDatabaseByModel(conn, model, schema);
	}

	private static ContentNodeMetadata loadLastDbVersion(final Connection conn, final ContentNodeMetadata versionModel, final String schema) throws SQLException {
		final String	select = SQLModelUtils.buildSelectAllStatementByModel(conn, versionModel, schema);
		
		try(final Statement					stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			final ResultSet					rs = stmt.executeQuery(select);
			final VersionInstanceManager	vim = new VersionInstanceManager()) {
		
			if (rs.last()) {
				final VersionRecord			vr = vim.newInstance();
				
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
				throw new SQLException("Operator ["+select+"]: version table is unexpectable empty"); 
			}
		}
	}

	
	private static <T extends Comparable<T>> void storeCurrentDbVersion(final Connection conn, final ContentNodeMetadata versionModel, final ContentNodeMetadata model, final String schema, final T modelVersion) throws SQLException {
		final String	select = SQLModelUtils.buildSelectAllStatementByModel(conn, versionModel, schema);
		
		try(final Statement					stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			final ResultSet					rs = stmt.executeQuery(select);
			final VersionInstanceManager	vim = new VersionInstanceManager()) {
			final VersionRecord				vr = vim.newInstance();
			
			vr.dbv_Id = vim.newKey();
			vr.dbv_Version = modelVersion.toString();
			vr.dbv_Model = modelToString(model);
			vr.dbv_Created = new Date(System.currentTimeMillis());

			rs.moveToInsertRow();
			vim.storeInstance(rs, vr, false);
			rs.insertRow();
			conn.commit();
		}
	}

	private static void removeDbVersionTable(final Connection conn, final ContentNodeMetadata model, final String schema) throws SQLException {
		SQLModelUtils.removeDatabaseByModel(conn, model, schema, true);
	}
	
	
	private static class VersionRecord implements Cloneable {
		private long	dbv_Id;
		private String	dbv_Version;
		private String	dbv_Model;
		private Date	dbv_Created;
		
		@Override
		public VersionRecord clone() throws CloneNotSupportedException {
			return (VersionRecord)super.clone();
		}
		
		@Override
		public String toString() {
			return "VersionRecord [dbv_Id=" + dbv_Id + ", dbv_Version=" + dbv_Version + ", dbv_Model=" + dbv_Model + ", dbv_Created=" + dbv_Created + "]";
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
			inst.dbv_Version = rs.getString(2);
			inst.dbv_Model = rs.getString(3);
			inst.dbv_Created = rs.getDate(4);
		}

		@Override
		public void storeInstance(ResultSet rs, VersionRecord inst, boolean update) throws SQLException {
			if (!update) {
				rs.updateLong(1, inst.dbv_Id);
			}
			rs.updateString(2, inst.dbv_Version);
			rs.updateString(3, inst.dbv_Model);
			rs.updateDate(4, new java.sql.Date(inst.dbv_Created.getTime()));
		}

		@Override
		public <T> T get(final VersionRecord inst, final String name) throws SQLException {
			switch (name) {
				case "dbv_Id"		: return (T)Long.valueOf(inst.dbv_Id);
				case "dbv_Version"	: return (T)inst.dbv_Version;
				case "dbv_Model"	: return (T)inst.dbv_Model;
				case "dbv_Created"	: return (T)inst.dbv_Created;
				default : throw new SQLException("Field name ["+name+"] doesn't exist");
			}
		}

		@Override
		public <T> InstanceManager<Long, VersionRecord> set(final VersionRecord inst, final String name, final T value) throws SQLException {
			switch (name) {
				case "dbv_Id"		: 
					inst.dbv_Id = (Long)value;
					break;
				case "dbv_Version"	:
					inst.dbv_Version = (String)value;
					break;
				case "dbv_Model"	: 
					inst.dbv_Model = (String)value;
					break;
				case "dbv_Created"	: 
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
	}
}