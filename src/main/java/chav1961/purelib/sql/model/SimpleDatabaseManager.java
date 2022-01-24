package chav1961.purelib.sql.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Predicate;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.sql.model.SQLModelUtils.ConnectionGetter;
import chav1961.purelib.sql.model.interfaces.DatabaseManagement;
import chav1961.purelib.streams.JsonStaxParser;

public class SimpleDatabaseManager<T extends Comparable<T>> implements AutoCloseable, NodeMetadataOwner {
	private static final String					VERSION_MODEL_URI = "model.json";
	private static final ContentNodeMetadata	VERSION_MODEL;
	private static final String					VERSION_TABLE = "dbversion";

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
						final ContentNodeMetadata	oldModel = loadLastDbVersion(conn);
						final int					result = mgmt.getVersion(model).compareTo(mgmt.getVersion(oldModel));
						
						if (result > 0) {
							mgmt.onUpgrade(mgmt.getVersion(model), model, mgmt.getVersion(oldModel), oldModel);
							storeCurrentDbVersion(conn, model, mgmt.getVersion(model));
						}
						else if (result < 0) {
							mgmt.onDowngrade(mgmt.getVersion(model), model, mgmt.getVersion(oldModel), oldModel);
							storeCurrentDbVersion(conn, model, mgmt.getVersion(model));
						}
					}
					else {
						try{createDbVersionTable(conn, VERSION_MODEL);
							mgmt.onCreate(model);
							storeCurrentDbVersion(conn, model, mgmt.getVersion(model));
						} catch (SQLException exc) {
							removeDbVersionTable(conn);
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
	
	public void backup(final ZipOutputStream zos) {
		backup(zos, (e)->true);
	}

	public void backup(final ZipOutputStream zos, final Predicate<ContentNodeMetadata> pred) {
		backup(zos, pred, ProgressIndicator.DUMMY);
	}

	public void backup(final ZipOutputStream zos, final Predicate<ContentNodeMetadata> pred, final ProgressIndicator progress) {
		
	}
	
	public void restore(final ZipInputStream zis) {
		restore(zis, (e)->true);
	}

	public void restore(final ZipInputStream zis, final Predicate<ContentNodeMetadata> pred) {
		restore(zis, pred, ProgressIndicator.DUMMY);
	}

	public void restore(final ZipInputStream zis, final Predicate<ContentNodeMetadata> pred, final ProgressIndicator progress) {
		
	}
	
	private static void createDbVersionTable(final Connection conn, final ContentNodeMetadata model) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	private static ContentNodeMetadata loadLastDbVersion(final Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	private static <T extends Comparable<T>> void storeCurrentDbVersion(final Connection conn, final ContentNodeMetadata model, final T modelVersion) {
		// TODO Auto-generated method stub
		
	}

	private static void removeDbVersionTable(final Connection conn) throws SQLException {
		// TODO Auto-generated method stub
		
	}
}
