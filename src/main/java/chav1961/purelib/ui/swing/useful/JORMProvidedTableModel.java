package chav1961.purelib.ui.swing.useful;

import java.sql.SQLException;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.sql.interfaces.ORMProvider;

public class JORMProvidedTableModel<Record> extends DefaultTableModel implements LocaleChangeListener {
	private static final long serialVersionUID = -1667254595075642849L;

	private static final int			FETCH_SIZE = 20;
	
	private final Localizer				localizer;
	private final ORMProvider<Record> 	provider;
	private final Record				instance;
	private final boolean				readOnly;
	private final LongIdMap<Record>		records;
	
	private long						rowCount = 0;
	private boolean						needRefresh = true;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JORMProvidedTableModel(final Localizer localizer, final ORMProvider<Record> provider, final Record instance, final boolean readOnly) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null"); 
		}
		else if (instance == null) {
			throw new NullPointerException("Instance can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.provider = provider;
			this.instance = instance;
			this.records = new LongIdMap(instance.getClass());
			this.readOnly = readOnly;
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fireTableStructureChanged();
	}
	
	@Override
	public int getRowCount() {
		if (provider == null) {	// Bug in the Swing...
			return 0;
		}
		else {
			if (needRefresh) {
				needRefresh = false;
				refresh();
			}
			return (int)rowCount;
		}
	}

	@Override
	public int getColumnCount() {
		return provider.getContentMetadata().length;
	}

	@Override
	public String getColumnName(final int columnIndex) {
		try{return localizer.getValue(provider.getContentMetadata()[columnIndex].getLabelId());
		} catch (LocalizationException e) {
			return provider.getContentMetadata()[columnIndex].getLabelId();
		}
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return provider.getContentMetadata()[columnIndex].getType();
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return !readOnly;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (needRefresh) {
			needRefresh = false;
			refresh();
		}
		try{return provider.getValue(provider.getContentMetadata()[columnIndex],getRecord(rowIndex));
		} catch (ContentException e) {
			return null;
		}
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		if (!isCellEditable(rowIndex, columnIndex)) {
			throw new IllegalStateException("Table model is read-only!");
		}
		else {
			try{final Record	r = getRecord(rowIndex);
			
				provider.setValue(provider.getContentMetadata()[columnIndex],r,aValue);
				provider.update(r);
			} catch (ContentException | SQLException e) {
			}
		}
	}

	public void refresh() {
		try{rowCount = provider.contentSize();
			records.clear();
		} catch (SQLException e) {
			rowCount = 0;
		}
	}

	private Record getRecord(final int rowIndex) {
		if (records.get(rowIndex) == null) {	// Lazy loading
			final int	offset = Math.max(0,rowIndex-FETCH_SIZE), limit = (int) (Math.min(rowCount,rowIndex+FETCH_SIZE) - offset); 
			
			try{provider.content(instance,(seq,off,rec)->{
					records.put(off,provider.clone(rec));
					return ContinueMode.CONTINUE;
				},offset,limit);
			} catch (SQLException e) {
			}
		}
		return records.get(rowIndex);
	}
}
