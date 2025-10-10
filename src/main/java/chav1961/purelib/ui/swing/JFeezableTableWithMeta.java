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
	
	public JFeezableTableWithMeta(final TableModel model, final ContentNodeMetadata metadata) throws NullPointerException, IllegalArgumentException {
		super(model, extractFreezedColumns(metadata));
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
		updateUI();
	}

	private static String[] extractFreezedColumns(final ContentNodeMetadata metadata) {
		final List<String>	names = new ArrayList<>();
		
		for (ContentNodeMetadata item : metadata) {
			if (item.getFormatAssociated() != null && item.getFormatAssociated().isAnchored()) {
				names.add(item.getName());
			}
		}
		if (names.isEmpty()) {
			throw new IllegalArgumentException("No any freezable commns found in the model. Don't use this class with this model"); 
		}
		else {
			return names.toArray(new String[names.size()]);
		}
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
}
