package chav1961.purelib.ui.swing.useful;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.model.interfaces.TableContainer;
import chav1961.purelib.sql.interfaces.InstanceManager;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RecordFormManager.RecordAction;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.SwingUtils;

public class JDataBaseTableWithMeta<K,Inst> extends JFreezableTable implements NodeMetadataOwner, LocaleChangeListener {
	private static final long 	serialVersionUID = -6707307489832770493L;
	private static final String	CONFIRM_DELETE_TITLE = "JDataBaseTableWithMeta.confirm.delete.title";
	private static final String	CONFIRM_DELETE_MESSAGE = "JDataBaseTableWithMeta.confirm.delete.message";
	
	private final ContentNodeMetadata		meta;
	private final Localizer 				localizer;
	private final InnerTableModel<K,Inst>	model;
	private final boolean 					enableManipulations;
	private final LightWeightListenerList<ContentChangedListener<K,Inst>>	listeners = new LightWeightListenerList(ContentChangedListener.class);
	private FormManager<K,Inst>				mgr = null;
	private InstanceManager<K,Inst>			instMgr = null;

	@FunctionalInterface
	public static interface ContentChangedListener<K,Inst> {
		public enum ChangeType {
			INSERTED, DUPLICATED, DELETED
		}
		void process(JDataBaseTableWithMeta<K,Inst> table, ChangeType ct, K keyValue, String fieldName);
	}
	
	public JDataBaseTableWithMeta(final ContentNodeMetadata meta, final Localizer localizer, final boolean enableManipulations, final boolean enableEdit) throws NullPointerException, IllegalArgumentException {
		super(buildTableModel(meta, localizer, enableEdit), buildFreezedColumns(meta));
		if (meta == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.meta = meta;
			this.localizer = localizer;
			this.model = (InnerTableModel<K, Inst>) getModel();
			this.enableManipulations = enableManipulations;

			SwingUtils.assignActionKey(this, SwingUtils.KS_INSERT, (e)->manipulate(getSelectedRow(), SwingUtils.ACTION_INSERT), SwingUtils.ACTION_INSERT);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DUPLICATE, (e)->manipulate(getSelectedRow(), SwingUtils.ACTION_DUPLICATE), SwingUtils.ACTION_DUPLICATE);
			SwingUtils.assignActionKey(this, SwingUtils.KS_DELETE, (e)->manipulate(getSelectedRow(), SwingUtils.ACTION_DELETE), SwingUtils.ACTION_DELETE);
			setAutoResizeMode(AUTO_RESIZE_OFF);
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

	public synchronized void assignResultSet(final ResultSet rs) throws NullPointerException, IllegalArgumentException, SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else if (rs.getFetchDirection() == ResultSet.TYPE_FORWARD_ONLY) {
			throw new IllegalArgumentException("Result set type is 'TYPE_FORWARD_ONLY'. This type is not supported for this call");
		}
		else {
			this.mgr = null;
			this.instMgr = null;
			
			model.assignOwner(this, rs, null, null);
		}
	}
	
	public synchronized void assignResultSetAndManagers(final ResultSet rs, final FormManager<K, Inst> mgr, final InstanceManager<K, Inst> instMgr) throws NullPointerException, IllegalArgumentException, SQLException {
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else if (mgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else if (instMgr == null) {
			throw new NullPointerException("Instance manager can't be null");
		}
		else if (rs.getFetchDirection() == ResultSet.TYPE_FORWARD_ONLY) {
			throw new IllegalArgumentException("Result set type is 'TYPE_FORWARD_ONLY'. This type is not supported for this call");
		}
		else {
			this.mgr = mgr;
			this.instMgr = instMgr;
			
			model.assignOwner(this, rs, mgr, instMgr);
		}
	}
	
	public synchronized void resetResultSetAndManagers() {
		mgr = null;
		try{model.assignOwner(null, null, null, null);
		} catch (SQLException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc.getLocalizedMessage());
		}
	}

	public void resizeColumns() {
		resizeColumns(getPreferredSize().width);
	}

	public void resizeColumns(final int width) {
		resizeColumns(width, model);
	}
	
	public void refresh() {
		model.fireTableDataChanged();
	}
	
	public void processAction(final String action) {
		manipulate(editingRow, action);
	}
	
	public K getSelectedKey() throws SQLException {
		if (getSelectedRow() >= 0) {
			model.desc.rs.absolute(getSelectedRow() + 1);
			return instMgr.extractKey(model.desc.rs);
		}
		else {
			return null;
		}
	}
	
	public void addContentChangedListener(final ContentChangedListener<K,Inst> listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			listeners.addListener(listener);
		}
	}
	
	public void removeContentChangedListener(final ContentChangedListener<K,Inst> listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(listener);
		}
	}
	
	protected K insertRow(final Inst content) throws SQLException, FlowException {
		if (mgr == null) {
			throw new IllegalStateException("Manager is not assigned for the control");
		}
		else { 
			final K	newKey = instMgr.extractKey(content);
			
			if (mgr.onRecord(RecordAction.INSERT, null, null, content, newKey) != RefreshMode.REJECT) {
				model.desc.rs.moveToInsertRow();
				instMgr.storeInstance(model.desc.rs, content, false);
				model.desc.rs.insertRow();
				model.desc.rs.moveToCurrentRow();
				model.fireTableDataChanged();
				return newKey;
			}
			else {
				return null;
			}
		}
	}

	protected K duplicateRow(final int row, final Inst sourceRow) throws SQLException, FlowException {
		if (mgr == null) {
			throw new IllegalStateException("Manager is not assigned for the control");
		}
		else {
			final Inst	duplicatedRow = instMgr.clone(sourceRow);
			final K		newKey = instMgr.newKey();
			
			instMgr.assignKey(duplicatedRow, newKey);
			if (mgr.onRecord(RecordAction.DUPLICATE, sourceRow, instMgr.extractKey(sourceRow), duplicatedRow, newKey) != RefreshMode.REJECT) {
				model.desc.rs.moveToInsertRow();
				instMgr.storeInstance(model.desc.rs, duplicatedRow, false);
				model.desc.rs.insertRow();
				model.desc.rs.moveToCurrentRow();
				model.fireTableDataChanged();
				return newKey;
			}
			else {
				return null;
			}
		}
	}

	protected K deleteRow(final int row) throws SQLException, FlowException {
		if (mgr == null) {
			throw new IllegalStateException("Manager is not assigned for the control");
		}
		else {
			model.desc.rs.absolute(row);
			
			final Inst	inst = loadRow(row);
		
			if (mgr.onRecord(RecordAction.DELETE, inst, instMgr.extractKey(inst), null, null) != RefreshMode.REJECT) {
				final K	key = instMgr.extractKey(inst);
				
				model.desc.rs.deleteRow();
				model.fireTableDataChanged();
				return key;
			}
			else {
				return null;
			}
		}
	}
	
	protected Inst newRow() throws SQLException {
		return instMgr.newInstance(); 
	}
	
	protected Inst loadRow(final int row) throws SQLException {
		final Inst	inst = instMgr.newInstance();
		
		instMgr.loadInstance(model.desc.rs, inst);
		return inst;
	}
	
	private void manipulate(final int row, final String action) {
		if (!model.rsIsReadOnly && instMgr != null && enableManipulations) {
			try{switch (action) {
					case SwingUtils.ACTION_INSERT 		:
						final K	keyInserted = insertRow(newRow());
						
						listeners.fireEvent((l)->l.process(this, ContentChangedListener.ChangeType.INSERTED, keyInserted, ""));
						break;
					case SwingUtils.ACTION_DUPLICATE 	:
						if (model.getRowCount() > 0) {
							model.desc.rs.absolute(row + 1);
							final K	keyDuplicated = duplicateRow(model.desc.rs.getRow(), loadRow(model.desc.rs.getRow()));
							
							listeners.fireEvent((l)->l.process(this, ContentChangedListener.ChangeType.DUPLICATED, keyDuplicated, ""));
						}
						break;
					case SwingUtils.ACTION_DELETE		:
						if (model.getRowCount() > 0) {
							if (new JLocalizedOptionPane(localizer).confirm(this, CONFIRM_DELETE_MESSAGE, CONFIRM_DELETE_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)  {
								final K	keyRemoved = deleteRow(row + 1);
								
								listeners.fireEvent((l)->l.process(this, ContentChangedListener.ChangeType.DELETED, keyRemoved, ""));
							}
						}
						break;
					default :
						throw new UnsupportedOperationException("Action ["+action+"] is not supported yet"); 
				}
			} catch (SQLException | FlowException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e,e.getLocalizedMessage());
			} 
		}
	}

	private void resizeColumns(final int width, final InnerTableModel<K, Inst> model) {
		final int[]	sizes = new int[model.getColumnCount()];
		
		int	totalSize = 0, index = 0;
		
		for (String name : model.getMetadataChildrenNames()) {
			final ContentNodeMetadata	meta = model.getNodeMetadata(name);
			
			if (meta.getFormatAssociated() != null && meta.getFormatAssociated().getLength() > 0) {
				sizes[index] = meta.getFormatAssociated().getLength(); 
				totalSize += sizes[index++];
			}
			else {
				totalSize += sizes[index++] = 10;
			}
		}
		final float	scale = 1.0f * width / totalSize;
		
		for (int col = 0, maxCol = getColumnModel().getColumnCount(); col < maxCol; col++) {
			final int	currentWidth = (int) (scale * sizes[col]);
			
			getColumnModel().getColumn(col).setPreferredWidth(currentWidth);
		}
	}

	private void fillLocalizedStrings() {
		
	}

	static <K, Inst> TableModel buildTableModel(final ContentNodeMetadata meta, final Localizer localizer, final boolean enableEdit) {
		if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (meta.getType() != TableContainer.class) {
			throw new IllegalArgumentException("Metadata type ["+meta.getType().getCanonicalName()+"] doesn't declare table container. It's type must be "+TableContainer.class.getCanonicalName()); 
		}
		else {
			final List<ContentNodeMetadata>	result = new ArrayList<>();
			final Set<String>				names = new HashSet<>();
			
			for (ContentNodeMetadata item : meta) {
				if (!names.contains(item.getName())) {
					if (item.getFormatAssociated() != null && item.getFormatAssociated().isUsedInList()) {
						result.add(item);
						names.add(item.getName());
					}
				}
			}
			return new InnerTableModel<K, Inst>(meta, result.toArray(new ContentNodeMetadata[result.size()]), localizer, enableEdit);
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
	
	private static class InnerTableModel<K,Inst> extends DefaultTableModel implements NodeMetadataOwner {
		private static final long serialVersionUID = 4821572920544412802L;

		private final ContentNodeMetadata		owner;
		private final ContentNodeMetadata[]		metadata;
		private final Localizer					localizer;
		private volatile ContentDesc<K, Inst>	desc = null; 
		private Inst						lastRow;
		private boolean							rsIsReadOnly = false;

		private InnerTableModel(final ContentNodeMetadata owner, final ContentNodeMetadata[] metadata, final Localizer localizer, final boolean enableEdit) {
			this.owner = owner;
			this.metadata = metadata;
			this.localizer = localizer;
		}

		@Override
		public ContentNodeMetadata getNodeMetadata() {
			return owner;
		}

		@Override
		public boolean hasNodeMetadata(final String childName) {
			if (childName == null || childName.isEmpty()) {
				throw new IllegalArgumentException("Child name can't be null or empty");
			}
			else {
				for (ContentNodeMetadata item : metadata) {
					if (childName.equals(item.getName())) {
						return true;
					}
				}
				return false;
			}
		}		
		
		@Override
		public ContentNodeMetadata getNodeMetadata(final String childName) {
			if (childName == null || childName.isEmpty()) {
				throw new IllegalArgumentException("Child name can't be null or empty");
			}
			else {
				for (ContentNodeMetadata item : metadata) {
					if (childName.equals(item.getName())) {
						return item;
					}
				}
				throw new IllegalArgumentException("Child name ["+childName+"] not found in he model");
			}
		}
		
		@Override
		public String[] getMetadataChildrenNames() {
			final String[]	result = new String[metadata.length];
			int	index = 0;
			
			for (ContentNodeMetadata item : metadata) {
				result[index++] = item.getName();
			}
			return result;
		}
		
		@Override
		public int getRowCount() {
			try{if (desc == null || desc.rs == null || desc.rs.isClosed()) {
					return 0;
				}
				else {
					final int	last = desc.rs.getRow();
					
					if (desc.rs.last()) {
						final int	result = desc.rs.getRow();
						
						desc.rs.absolute(last);
						return result;
					}
					else {
						desc.rs.absolute(last);
						return 0;
					}
				}
			} catch (SQLException e) {
				printError(e);
				return 0;
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
			return CompilerUtils.toWrappedClass(metadata[columnIndex].getType());
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return desc.rs != null && !rsIsReadOnly && (metadata[columnIndex].getFormatAssociated() == null || !metadata[columnIndex].getFormatAssociated().isReadOnly(true));   
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			try{desc.rs.absolute(rowIndex + 1);
				if (desc.instMgr != null) {
					desc.instMgr.loadInstance(desc.rs, lastRow);
				}
				return desc.instMgr != null ? desc.instMgr.get(lastRow, metadata[columnIndex].getName()) : desc.rs.getObject(metadata[columnIndex].getName());
			} catch (SQLException e) {
				printError(e);
				return null;
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			try{desc.rs.absolute(rowIndex + 1);
				desc.instMgr.loadInstance(desc.rs, lastRow);
				
				final String	fieldName = metadata[columnIndex].getName();
				final Object	oldValue = desc.instMgr.get(lastRow, fieldName);
				
				desc.instMgr.set(lastRow, fieldName, aValue);
				if (desc.mgr == null || desc.mgr.onField(lastRow, desc.instMgr.extractKey(lastRow), metadata[columnIndex].getName(), oldValue, true) != RefreshMode.REJECT) {
					desc.rs.absolute(rowIndex + 1);
					desc.instMgr.storeInstance(desc.rs, lastRow, true);
					desc.rs.updateRow();
					fireTableRowsUpdated(rowIndex, rowIndex);
				}
				else {
					desc.instMgr.set(lastRow, fieldName, oldValue);
				}
			} catch (SQLException | FlowException e) {
				printError(e);
			}
		}

		public ContentNodeMetadata[] getMetaData() {
			return metadata;
		}

		public void assignOwner(final JComponent owner, final ResultSet rs, final FormManager<K,Inst> mgr, final InstanceManager<K,Inst> instMgr) throws SQLException {
			this.lastRow = instMgr != null ? instMgr.newInstance() : null;
			this.rsIsReadOnly = rs != null && rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY;
			this.desc = new ContentDesc<K, Inst>(rs, mgr, instMgr, owner);
			
			fireTableStructureChanged();
			fireTableDataChanged();
		}
		
		private void printError(final Exception exc) {
			if (desc.owner != null) {
				SwingUtils.getNearestLogger(desc.owner).message(Severity.error, exc.getLocalizedMessage());
			}
			else {
				PureLibSettings.CURRENT_LOGGER.message(Severity.error, exc.getLocalizedMessage());
			}
		}
		
		static class ContentDesc<K, Inst> {
			final ResultSet					rs;
			final FormManager<K, Inst>		mgr;
			final InstanceManager<K, Inst>	instMgr;
			final JComponent				owner;
			
			ContentDesc(final ResultSet rs, final FormManager<K, Inst> mgr, final InstanceManager<K, Inst> instMgr, final JComponent owner) throws SQLException {
				this.rs = rs;
				this.mgr = mgr;
				this.instMgr = instMgr;
				this.owner = owner;
			}
		}
	}
}
