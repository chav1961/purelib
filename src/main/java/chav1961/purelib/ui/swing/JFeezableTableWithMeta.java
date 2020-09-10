package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.useful.JFreezableTable;

public class JFeezableTableWithMeta extends JFreezableTable implements NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = 8688598119389158690L;
	
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	
	public JFeezableTableWithMeta(final TableModel model, final ContentNodeMetadata metadata) throws NullPointerException, IllegalArgumentException, LocalizationException {
		super(new FilteredTableModel(model,metadata),extractFreezedColumns(metadata));
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			this.metadata = metadata;
			this.localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
			getTableHeader().setDefaultRenderer((table,value,isSelected,hasFocus,row,column)->renderHeaderCell(table,value,isSelected,hasFocus,row,column));
			getLeftBar().getTableHeader().setDefaultRenderer((table,value,isSelected,hasFocus,row,column)->renderHeaderCell(table,value,isSelected,hasFocus,row,column));
		}
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		listener.tableChanged(new TableModelEvent(getModel(),TableModelEvent.HEADER_ROW));
		listener.tableChanged(new TableModelEvent(getModel()));
	}

	
	@Override
	public void setModel(TableModel dataModel) {
		super.setModel(new FilteredTableModel(dataModel,metadata));
	}
	
	private static String[] extractFreezedColumns(final ContentNodeMetadata metadata) {
		final List<String>	names = new ArrayList<>();
		
		for (ContentNodeMetadata item : metadata) {
			if (item.getFormatAssociated() != null && item.getFormatAssociated().isAnchored()) {
				names.add(item.getName());
			}
		}
		return names.toArray(new String[names.size()]);
	}
	
	private Component renderHeaderCell(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final DefaultTableCellRenderer	renderer = new DefaultTableCellRenderer();
		final JLabel					result = (JLabel)renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		try{result.setText(localizer.getValue(itemByName(table.getModel().getColumnName(column)).getLabelId()));
			result.setToolTipText(localizer.getValue(itemByName(table.getModel().getColumnName(column)).getTooltipId()));
		} catch (LocalizationException e) {
		}
		return result;
	}

	private ContentNodeMetadata itemByName(final String name) throws LocalizationException {
		for (ContentNodeMetadata item : metadata) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		throw new LocalizationException("Name ["+name+"] is missing in the metadata");
	}
	
	private static class FilteredTableModel implements TableModel {
		private final TableModel			nested;
		private final ContentNodeMetadata	metadata;
		
		private int							trueColumnCount;
		private int[]						trueColumnIndices;
		
		FilteredTableModel(final TableModel nested, final ContentNodeMetadata metadata) throws IllegalArgumentException {
			this.nested = nested;
			this.metadata = metadata;
			prepareIndices();
		}

		@Override
		public int getRowCount() {
			return nested.getRowCount();
		}

		@Override
		public int getColumnCount() {
			if (nested.getRowCount() != trueColumnCount) {
				prepareIndices();
			}
			return trueColumnCount;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return nested.getColumnName(trueColumnIndices[columnIndex]);
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return nested.getColumnClass(trueColumnIndices[columnIndex]);
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return nested.isCellEditable(rowIndex,trueColumnIndices[columnIndex]);
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return nested.getValueAt(rowIndex,trueColumnIndices[columnIndex]);
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			nested.setValueAt(aValue,rowIndex,trueColumnIndices[columnIndex]);
		}

		@Override
		public void addTableModelListener(final TableModelListener l) {
			nested.addTableModelListener(l);
		}

		@Override
		public void removeTableModelListener(final TableModelListener l) {
			nested.removeTableModelListener(l);
		}
		
		private void prepareIndices() {
			trueColumnIndices = new int[nested.getColumnCount()];
			
			int count = 0;
loop:		for (int index = 0, maxIndex = this.trueColumnIndices.length; index < maxIndex; index++) {
				final String	name = nested.getColumnName(index);
				
				for (ContentNodeMetadata item : metadata) {
					if (name.equals(item.getName())) {
						if (item.getFormatAssociated() != null && item.getFormatAssociated().isUsedInList()) {
							trueColumnIndices[count++] = index;
						}
						continue loop;
					}
				}
				throw new IllegalArgumentException("Table model contains column ["+name+"], that is missing in the content metadata"); 
			}
			this.trueColumnCount = count;
		}
	}
}
