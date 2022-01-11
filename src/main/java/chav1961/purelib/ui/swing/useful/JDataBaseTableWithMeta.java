package chav1961.purelib.ui.swing.useful;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.TableContainer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

public class JDataBaseTableWithMeta<K,Inst> extends JFreezableTable implements NodeMetadataOwner, LocaleChangeListener {
	private static final long 			serialVersionUID = -6707307489832770493L;
	
	private FormManager<K,Inst>			DEFAULT_FORM_MANAGER = new FormManager<>() {
												@Override
												public RefreshMode onField(final Inst inst, final K id, final String fieldName, final Object oldValue, final boolean beforeCommit) throws FlowException, LocalizationException {
													return RefreshMode.DEFAULT;
												}
										
												@Override
												public LoggerFacade getLogger() {
													return SwingUtils.getNearestLogger(JDataBaseTableWithMeta.this);
												}
											}; 
	
	
	private final ContentNodeMetadata	meta;
	private final Localizer 			localizer;
	private final InnerTableModel		model;
	private FormManager<K,Inst>			mgr = null;
	
	public JDataBaseTableWithMeta(final ContentNodeMetadata meta, final Localizer localizer) throws NullPointerException, IllegalArgumentException {
		super(buildTableModel(meta, localizer), buildFreezedColumns(meta));
		// TODO Auto-generated constructor stub
		if (meta == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.meta = meta;
			this.localizer = localizer;
			this.model = (InnerTableModel)getModel();
			
			for (ContentNodeMetadata item : model.getMetaData()) {
				try{this.setDefaultRenderer(item.getType(), SwingUtils.getCellRenderer(item, TableCellRenderer.class));
				} catch (EnvironmentException e) {
					throw new IllegalArgumentException("No appropriative cell renderer for field ["+item.getName()+"] with type ["+item.getType().getCanonicalName()+"]: "+e.getLocalizedMessage());
				}
			}
			
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return meta;
	}

	public void assignResultSetAndFormManager(final ResultSet rs) throws NullPointerException, IllegalArgumentException, SQLException {
		assignResultSetAndFormManager(rs, DEFAULT_FORM_MANAGER);
	}	
	
	public synchronized void assignResultSetAndFormManager(final ResultSet rs, final FormManager<K, Inst> mgr) throws NullPointerException, IllegalArgumentException, SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else if (mgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else if (rs.getFetchDirection() == ResultSet.FETCH_FORWARD) {
			throw new IllegalArgumentException("Result set type is 'FETCH_FORWARD'. This type is not supported for this call");
		}
		else {
			this.mgr = mgr;
			
			model.assignOwnerAndResultSet(this, rs);
		}
	}
	
	public synchronized void resetResultSetAndFormManager() {
		mgr = null;
		try{model.assignOwnerAndResultSet(null, null);
			model.fireTableStructureChanged();
		} catch (SQLException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc.getLocalizedMessage());
		}
	}
	
	private void fillLocalizedStrings() {
		
	}
	
	static TableModel buildTableModel(final ContentNodeMetadata meta, final Localizer localizer) {
		if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (meta.getType() != TableContainer.class) {
			throw new IllegalArgumentException("Metadata type ["+meta.getType().getCanonicalName()+"] doesn't declare table container. It's type must be "+TableContainer.class.getCanonicalName()); 
		}
		else {
			final List<ContentNodeMetadata>	result = new ArrayList<>();
			
			for (ContentNodeMetadata item : meta) {
				if (item.getFormatAssociated() != null && item.getFormatAssociated().isAnchored()) {
					result.add(item);
				}
			}
			return new InnerTableModel(result.toArray(new ContentNodeMetadata[result.size()]), localizer);
		}
	}
	
	static String[] buildFreezedColumns(final ContentNodeMetadata meta) {
		final List<String>	result = new ArrayList<>();
		
		for (ContentNodeMetadata item : meta) {
			if (item.getFormatAssociated() != null && item.getFormatAssociated().isAnchored()) {
				result.add(item.getName());
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	private static class InnerTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 4821572920544412802L;

		private final ContentNodeMetadata[]	metadata;
		private final Localizer				localizer;
		private ResultSet					rs = null;
		private boolean						rsIsReadOnly = false;
		private JComponent					owner = null;

		private InnerTableModel(final ContentNodeMetadata[]	metadata, final Localizer localizer) {
			this.metadata = metadata;
			this.localizer = localizer;
		}

		@Override
		public int getRowCount() {
			if (rs == null) {
				return 0;
			}
			else {
				try{if (rs.last()) {
						return rs.getRow();
					}
					else {
						return 0;
					}
				} catch (SQLException e) {
					printError(e);
					return 0;
				}
			}
		}

		@Override
		public int getColumnCount() {
			return metadata.length;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			try{return localizer.getValue(metadata[columnIndex].getLabelId());
			} catch (LocalizationException  e) {
				return metadata[columnIndex].getLabelId();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return metadata[columnIndex].getType();
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return rs != null && !rsIsReadOnly && (metadata[columnIndex].getFormatAssociated() == null || !metadata[columnIndex].getFormatAssociated().isReadOnly(true));   
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			try{rs.absolute(rowIndex+1);
				return rs.getObject(metadata[columnIndex].getName());
			} catch (SQLException e) {
				printError(e);
				return null;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			
		}

		public ContentNodeMetadata[] getMetaData() {
			return metadata;
		}

		public void assignOwnerAndResultSet(final JComponent owner, final ResultSet rs) throws SQLException {
			this.owner = owner;
			this.rs = rs;
			this.rsIsReadOnly = rs != null && rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY;
			
			fireTableStructureChanged();
		}
		
		private void printError(final SQLException exc) {
			if (owner != null) {
				SwingUtils.getNearestLogger(owner).message(Severity.error, exc.getLocalizedMessage());
			}
			else {
				PureLibSettings.CURRENT_LOGGER.message(Severity.error, exc.getLocalizedMessage());
			}
		}
	}
}
