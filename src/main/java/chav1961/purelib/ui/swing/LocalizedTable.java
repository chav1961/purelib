package chav1961.purelib.ui.swing;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;

public class LocalizedTable extends JTable implements LocaleChangeListener {
	private static final long 	serialVersionUID = 4329316422119902993L;

	
	
	public interface RowSorterInfo {
		public enum OrderingState {
			A2Z, Z2A, NONE
		}
		
		OrderingState getColumnOrderingState(final String columnName);
	}
	
	private final Localizer		localizer;
	private boolean				orderingOn = false;
	
	public LocalizedTable(final Localizer localizer, final TableModel model, final FieldDescriptor[] columns, final boolean readOnly, final boolean canEditCell) {
		super(model);
		this.localizer = localizer;
		this.setTableHeader(new CreoleBasedTableHeader(localizer,this.getColumnModel()));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnSelectionAllowed(false);

		final TableColumnModel	columnModel = getColumnModel(); 
		
		for (int index = 0; index < columns.length; index++) {
			final TableColumn	col = columnModel.getColumn(index); 
			
			col.setHeaderRenderer(new LocalizedHeaderCellRenderer(localizer,columns[index]));
			col.setCellRenderer(new LocalizedBodyCellRenderer(localizer,columns[index]));
			if (!readOnly && canEditCell) {
				col.setCellEditor(new LocalizedCellEditor(columns[index]));
			}
		}
	}

	@Override
	public void setRowSorter(final RowSorter<? extends TableModel> sorter) {
		super.setRowSorter(sorter);
		orderingOn = sorter != null;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}
	
	private class LocalizedCellEditor implements  TableCellEditor {
		private static final long 				serialVersionUID = 4562762993879161315L;
		
		private final FieldDescriptor			fieldDescriptor;
		private final List<CellEditorListener>	listeners = new ArrayList<>();
		
		LocalizedCellEditor(final FieldDescriptor fieldDescriptor) {
			this.fieldDescriptor = fieldDescriptor;
		}

		@Override
		public Object getCellEditorValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isCellEditable(final EventObject anEvent) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean shouldSelectCell(final EventObject anEvent) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean stopCellEditing() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void cancelCellEditing() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addCellEditorListener(final CellEditorListener l) {
			synchronized (listeners) {
				listeners.add(l);
			}
		}

		@Override
		public void removeCellEditorListener(final CellEditorListener l) {
			synchronized (listeners) {
				listeners.remove(l);
			}
		}

		@Override
		public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
			// TODO Auto-generated method stub
			return new JLabel();
		}
	}
}
