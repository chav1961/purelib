package chav1961.purelib.sql.model;

import java.sql.Connection;
import java.sql.SQLException;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class SQLModelManager {
	public static enum Action {
		PREPARE_BY_MODEL,
		VALIDATE_BY_MODEL,
		DROP_BY_MODEL,
		BACKUP_BY_MODEL,
		RESTORE_BY_MODEL
	}
	
	public SQLModelManager(final ContentMetadataInterface mdi, final Localizer localizer, final LoggerFacade logger) throws NullPointerException {
		
	}
	
	public void process(final Connection conn, final Action action, final Object... parameters) throws NullPointerException, SQLException {
		
	}

	public void upgrade(final Connection conn, final ContentMetadataInterface newMdi, final Object... parameters) throws NullPointerException, SQLException {
		
	}

	public String getDbVesion(final Connection conn) throws NullPointerException, SQLException {
		return null;
	}
}
