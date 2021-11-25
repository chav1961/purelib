package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.ui.interfaces.ItemAndSelection;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class JSelectableListWithMeta<T extends ItemAndSelection<T>> extends JPanel implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long serialVersionUID = 8688598119389158690L;

	public static enum WizardType {
		SELECTED_LIST,
		TWO_SIDED_PANEL;
	}
	
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private final InnerTableModel<T>	model = new InnerTableModel<>();
	private final WizardType			wizardType;
	private boolean						invalid = false;
	
	public JSelectableListWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws NullPointerException, IllegalArgumentException, LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			this.metadata = metadata;
			this.localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated());
			if (metadata.getFormatAssociated() != null && metadata.getFormatAssociated().getWizardType() != null && !metadata.getFormatAssociated().getWizardType().isEmpty()) {
				this.wizardType = WizardType.valueOf(metadata.getFormatAssociated().getWizardType());
			}
			else {
				this.wizardType = WizardType.SELECTED_LIST; 
			}
			setFocusable(true);
			switch (wizardType) {
				case SELECTED_LIST		:
					prepareSelectedList(metadata);
					break;
				case TWO_SIDED_PANEL	:
					prepareTwoSidedPanel();
					break;
				default :
					break;
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
		return metadata;
	}

	@Override
	public String getRawDataFromComponent() {
		try(final Writer			wr = new StringWriter();
			final JsonStaxPrinter	prn = new JsonStaxPrinter(wr)) {
			boolean		isEmpty = true;

			for (T item : model.content) {
				if (isEmpty) {
					isEmpty = false;
					prn.startArray();
				}
				else {
					prn.splitter();
				}
				prn.startObject()
						.name("selected").value(item.isSelected())
						.name("value").value(SQLUtils.convert(String.class, item.getItem()))
				   .endObject();
			}
			if (isEmpty) {
				return "[]";
			}
			else {
				prn.endArray();
				prn.flush();
			}
			return wr.toString();
		} catch (IOException | ContentException e) {
			return "[]";
		}
	}

	@Override
	public Object getValueFromComponent() {
		return model.oldContent;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return model.content;
	}

	@Override
	public void assignValueToComponent(final Object value) throws ContentException {
		if (value == null) {
			throw new ContentException("Value to assign can't be null"); 
		}
		else if (value instanceof ItemAndSelection[]) {
			model.setValue((T[]) value);
		}
		else if (value instanceof String) {
			try(final Reader			rdr = new StringReader(value.toString());
				final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
				final JsonNode			root = JsonUtils.loadJsonTree(parser);
				final List<T>			result = new ArrayList<>();
				
				if (root.getType() == JsonNodeType.JsonArray) {
					for (JsonNode item : root.children()) {
						if (item.getType() == JsonNodeType.JsonObject) {
							if (JsonUtils.checkJsonMandatories(item, "selected", "value")) {
								if (JsonUtils.checkJsonFieldTypes(item, "selected/bool not null", "value/str not null")) {
//									result.add(new ItemAndSelection<T>(item.getChild("value"));
								}
								else {
									throw new ContentException("Illegal field types for 'selected' (must be boolean) and/or 'value' (must be string) inside some json objects");
								}
							}
							else {
								throw new ContentException("Missing mandatory fields 'selected', 'value' inside some json objects");
							}
						}
						else {
							throw new ContentException("JSON items inside array are not an objects");
						}
					}
				}
				else {
					throw new ContentException("Top JSON element is not an array");
				}
				assignValueToComponent(result.toArray(new ItemAndSelection[result.size()]));
			} catch (IOException e) {
				throw new ContentException(e);
			}
		}
		else {
			throw new ContentException("Value to assign can be String or ItemAndSelection array only"); 
		}
	}

	@Override
	public Class<?> getValueType() {
		return getNodeMetadata().getType();
	}

	@Override
	public String standardValidation(final Object value) {
		return null;
	}

	@Override
	public void setInvalid(final boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	private void prepareSelectedList(final ContentNodeMetadata meta) {
		final JList<T>		list = new JList<>();
		final FieldFormat	format = meta.getFormatAssociated();
		
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		setLayout(new BorderLayout());
		if (format == null && format.getHeight() == 1) {
			add(list, BorderLayout.CENTER);
		}
		else {
			add(new JScrollPane(list));
		}
		final InnerListModel<T>	listModel = new InnerListModel<T>();
		
		list.setModel(listModel);
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(final TableModelEvent e) {
				switch (e.getType()) {
					case TableModelEvent.DELETE :
						if (e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE && e.getColumn() == TableModelEvent.ALL_COLUMNS) {
							listModel.removeAllElements();
						}
						else {
							for (int index = e.getLastRow(), maxIndex = e.getFirstRow(); index >= maxIndex; index--) {
								listModel.remove(index);
							}
						}
						listModel.fireIntervalRemoved(list, e.getFirstRow(), e.getLastRow());
						break;
					case TableModelEvent.INSERT :
						for (int index = e.getFirstRow(), maxIndex = e.getLastRow(); index < maxIndex; index++) {
							listModel.add(index, model.content[index]);
						}
						listModel.fireIntervalAdded(list, e.getFirstRow(), e.getLastRow());
						break;
					case TableModelEvent.UPDATE :
						if (e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE && e.getColumn() == TableModelEvent.ALL_COLUMNS) {
							listModel.removeAllElements();
							for (int index = 0; index < model.getRowCount(); index++) {
								listModel.add(index, model.content[index]);
							}
						}
						else {
							for (int index = e.getFirstRow(), maxIndex = Math.min(e.getLastRow(), listModel.getSize()); index < maxIndex; index++) {
								listModel.set(index, model.content[index]);
							}
						}
						listModel.fireContentsChanged(list, e.getFirstRow(), e.getLastRow());
						break;
					default : throw new UnsupportedOperationException(); 
				}
			}
		});
		SwingUtils.assignActionKey(list, SwingUtils.KS_CLICK, (e)->toggleSelected(list), SwingUtils.ACTION_CLICK);
		list.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
					toggle(list, list.locationToIndex(e.getPoint()));
				}
			}
		});
		try {
			list.setCellRenderer(SwingUtils.getCellRenderer(meta, ListCellRenderer.class));
		} catch (EnvironmentException e) {
			throw new IllegalArgumentException("No rendered found for ["+meta.getType()+"] in the list");
		}
	}

	private void toggleSelected(final JList<T> list) {
		for (int index : list.getSelectedIndices()) {
			toggle(list, index);
		}
	}

	private void toggle(final JList<T> list, int rowIndex) {
		model.setValueAt(!(Boolean)model.getValueAt(rowIndex, 0), rowIndex, 0);
	}
	
	private void prepareTwoSidedPanel() {
		// TODO Auto-generated method stub
		
	}

	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	
	private static class InnerTableModel<T extends ItemAndSelection<T>> extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		
		private T[]	content = null;
		private T[]	oldContent = null;

		@Override
		public int getRowCount() {
			return content == null ? 0 : content.length;
		}
	
		@Override
		public int getColumnCount() {
			return 2;
		}
	
		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0 : return "JSelectableListWithMeta.selected";
				case 1 : return "JSelectableListWithMeta.value";
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0 : return Boolean.class;
				case 1 : return ItemAndSelection.extract(true, content).getClass().getComponentType();
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : return true;
				case 1 : return true;
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : return content[rowIndex].isSelected();
				case 1 : return content[rowIndex].getItem();
				default : throw new UnsupportedOperationException(); 
			}
		}
	
		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : 
					content[rowIndex].setSelected((Boolean)aValue);
					break;
				case 1 : 
					content[rowIndex].setItem((T)aValue);
					break;
				default : throw new UnsupportedOperationException(); 
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
		
		public void setValue(final T[] content) {
			this.oldContent = this.content;
			this.content = content;
			fireTableDataChanged();
		}
	};
	
	
	private static class InnerListModel<T> extends DefaultListModel<T> {
		private static final long 	serialVersionUID = 1L;
		
		@Override
		public void fireIntervalAdded(Object source, int index0, int index1) {
			super.fireIntervalAdded(source, index0, index1);
		}
		
		@Override
		public void fireIntervalRemoved(Object source, int index0, int index1) {
			super.fireIntervalRemoved(source, index0, index1);
		}
		
		@Override
		public void fireContentsChanged(Object source, int index0, int index1) {
			super.fireContentsChanged(source, index0, index1);
		}
	};
}
