package chav1961.purelib.ui.swing;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;

class LocalizedBodyCellRenderer implements TableCellRenderer {
	
	private final Localizer			localizer;
	private final FieldDescriptor	fieldDescriptor;
	
	LocalizedBodyCellRenderer(final Localizer localizer, final FieldDescriptor fieldDescriptor) {
		this.localizer = localizer;
		this.fieldDescriptor = fieldDescriptor;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		final JComponent	result = SwingUtils.prepareCellRendererComponent(localizer.currentLocale().getLocale(),fieldDescriptor,value);
	
		result.setOpaque(true);
		result.setBackground(isSelected ? SwingUtils.SELECTED_TABLE_LINE : SwingUtils.UNSELECTED_TABLE_LINE);
		result.setForeground(!fieldDescriptor.fieldFormat.isMandatory() ? SwingUtils.MANDATORY_FOREGROUND : SwingUtils.OPTIONAL_FOREGROUND);
		result.setBorder(result.hasFocus() ? SwingUtils.FOCUSED_TABLE_CELL_BORDER : SwingUtils.TABLE_CELL_BORDER);
		return result;
	}
}