package chav1961.purelib.ui.swing.useful;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;

public class JFileList2 extends JPanel implements LocaleChangeListener {
	private static final long 				serialVersionUID = 1L;
	private static final String				CARD_ICONS = "icons";
	private static final String				CARD_TABLE = "table";
	private static final String				TABLE_COL_FILE = "table";
	private static final String				TABLE_COL_SIZE = "table";
	private static final String				TABLE_COL_CREATED = "table";
	private static final NullSelectionModel	NULL_SELECTION = new NullSelectionModel();

	public static enum SelectionType {
		NONE,
		SINGLE,
		MULTIPLE;
	}

	public static enum SelectedObjects {
		NONE,
		FILES,
		DIRECTORIES,
		ALL;
	}
	
	public static enum ContentViewType {
		AS_TABLE(CARD_TABLE), 
		AS_ICONS(CARD_ICONS),
		AS_LARGE_ICONS(CARD_ICONS);
		
		private final String	cardName;
		
		private ContentViewType(final String cardName) {
			this.cardName = cardName;
		}
		
		String getCardName() {
			return cardName;
		}
	}

	private final Localizer						localizer;
	private final LoggerFacade					logger;
	private final FileSystemInterface			fsi;
	private final boolean						insertParent;
	private final SelectionType					selType;
	private final SelectedObjects				selObjects;
	private final CardLayout					layout = new CardLayout();
	private final InnerTableModel				model;
	private final JTable						table = new JTable() {
													private static final long serialVersionUID = 1L;

													@Override
													public String getToolTipText(final MouseEvent event) {
														final int	row = table.rowAtPoint(event.getPoint());
														
														if (row >= 0) {
															return getTooltip4Path(((InnerTableModel)table.getModel()).getValueAt(row).getPath()); 
														}
														else {
															return null;
														}
													}
												};
	private final JList<JFileItemDescriptor>	list = new JList<>(){
													private static final long serialVersionUID = 1L;

													@Override
													public String getToolTipText(final MouseEvent event) {
														final int	row = list.locationToIndex(event.getPoint());
														
														if (row >= 0) {
															return getTooltip4Path(list.getModel().getElementAt(row).getPath()); 
														}
														else {
															return null;
														}
													}
												};

	private ContentViewType				viewType;
	private String						currentPath = "/";
	
	public JFileList2(final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final boolean insertParent, final boolean useMultiselection) throws NullPointerException, LocalizationException {
		this(localizer, logger, fsi, insertParent, useMultiselection ? SelectionType.MULTIPLE : SelectionType.SINGLE, SelectedObjects.ALL, ContentViewType.AS_ICONS);
	}

	public JFileList2(final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final boolean insertParent, final SelectionType selectionType, final SelectedObjects selectedObjects, final ContentViewType viewType) throws NullPointerException, LocalizationException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else if (selectionType == null) {
			throw new NullPointerException("Selection type can't be null"); 
		}
		else if (selectedObjects == null) {
			throw new NullPointerException("Selected objects can't be null"); 
		}
		else if (viewType == null) {
			throw new NullPointerException("View type can't be null"); 
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.fsi = fsi;
			this.insertParent = insertParent;
			this.selType = selectionType;
			this.selObjects = selectedObjects;
			this.viewType = viewType;
			this.model = new InnerTableModel(localizer);
			
			setLayout(layout);
			table.setModel(model);

			table.setDefaultRenderer(JFileItemDescriptor.class, new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
					// TODO Auto-generated method stub
					return null;
				}
			});
			table.setDefaultRenderer(Long.class, new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
					// TODO Auto-generated method stub
					return null;
				}
			});
			table.setDefaultRenderer(Date.class, new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
					// TODO Auto-generated method stub
					return null;
				}
			});
			list.setCellRenderer(new ListCellRenderer<JFileItemDescriptor>() {
				@Override
				public Component getListCellRendererComponent(final JList<? extends JFileItemDescriptor> list, final JFileItemDescriptor value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					JLabel	label = null;
					
					switch (viewType) {
						case AS_ICONS		:
							label = new JLabel(value.getName(), value.isDirectory() ? PureLibStandardIcons.LARGE_DIRECTORY.getIcon() : PureLibStandardIcons.LARGE_FILE.getIcon(), JLabel.LEFT);
							break;
						case AS_LARGE_ICONS	:
							label = new JLabel(value.getName(), value.isDirectory() ? PureLibStandardIcons.LARGE_DIRECTORY.getIcon() : PureLibStandardIcons.LARGE_FILE.getIcon(), JLabel.LEFT);
							break;
						case AS_TABLE		:
							return null;
						default	: throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet");
					}
					label.setOpaque(true);
					if (isSelected) {
						label.setForeground(list.getSelectionForeground());
						label.setBackground(list.getSelectionBackground());
					}
					else {
						label.setForeground(list.getForeground());
						label.setBackground(list.getBackground());
					}
					if (cellHasFocus) {
						label.setBorder(new LineBorder(Color.BLUE));
					}
					return label; 
				}
			});
			
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					final ToolTipManager 	toolTipManager = ToolTipManager.sharedInstance();
			        
			        toolTipManager.registerComponent(table);
			        toolTipManager.registerComponent(list);
				}
				
				@Override
				public void componentHidden(ComponentEvent e) {
					final ToolTipManager 	toolTipManager = ToolTipManager.sharedInstance();
			        
			        toolTipManager.unregisterComponent(table);
			        toolTipManager.unregisterComponent(list);
				}
			});
			switch (selType) {
				case NONE		:
					list.setSelectionModel(NULL_SELECTION);
					table.setSelectionModel(NULL_SELECTION);
					break;
				case SINGLE		:
					list.setSelectionModel(new SelectionModelWrapper(()->upload(list), new DefaultListSelectionModel(), selObjects));
					table.setSelectionModel(new SelectionModelWrapper(()->upload(table), new DefaultListSelectionModel(), selObjects));
					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					break;
				case MULTIPLE	:
					list.setSelectionModel(new SelectionModelWrapper(()->upload(list), new DefaultListSelectionModel(), selObjects));
					table.setSelectionModel(new SelectionModelWrapper(()->upload(table), new DefaultListSelectionModel(), selObjects));
					list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					break;
				default:
					break;
			}
			
			add(new JScrollPane(table), CARD_TABLE);
			add(new JScrollPane(list), CARD_ICONS);
			layout.show(this, viewType.getCardName());
			fillLocalizationStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizationStrings();
	}
	
	public ContentViewType getContentViewType() {
		return viewType;
	}
	
	public void setContentViewType(final ContentViewType type) throws NullPointerException, IOException {
		if (viewType ==  null) {
			throw new NullPointerException("View type can't be null");
		}
		else {
			viewType = type;
			layout.show(this, viewType.getCardName());
			switch (viewType) {
				case AS_ICONS		:
					break;
				case AS_LARGE_ICONS	:
					break;
				case AS_TABLE		:
					break;
				default	: throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet");
			}
			refreshContent(currentPath);
		}
	}

	public void refreshContent(final String path) throws IllegalArgumentException, IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path  to refresh can't be null or empty");
		}
		else {
			final List<JFileItemDescriptor>	items = new ArrayList<>();
			
			currentPath = path;
			try(final FileSystemInterface	temp = fsi.clone().open(path)) {
				if (insertParent && !"/".equals(temp.getPath())) {
					temp.push("..");
					items.add(new JFileItemDescriptor("..", temp.getPath(), true, 0, new Date(0)));
					temp.pop();
				}
				temp.list((i)->{
					items.add(new JFileItemDescriptor(i.getName(), i.getPath(), i.isDirectory(), i.size(), new Date(i.lastModified())));
					return ContinueMode.CONTINUE;
				});
			}
			switch (viewType) {
				case AS_ICONS : case AS_LARGE_ICONS :
					list.removeAll();
					list.setListData(items.toArray(new JFileItemDescriptor[items.size()]));
					break;
				case AS_TABLE		:
					((InnerTableModel)table.getModel()).refreshContent(items);
					break;
				default	: throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet");
			}
		}
	}

	private String getTooltip4Path(final String path) {
		try(final FileSystemInterface	temp = fsi.clone().open(path)) {

			if (temp.isDirectory()) {
				return String.format(path, temp.getPath(), new Date(temp.lastModified()));
			}
			else {
				return String.format(path, temp.getPath(), temp.size(), new Date(temp.lastModified()));
			}
		} catch (IOException e) {
			return path;
		}
	}
	
	private void fillLocalizationStrings() throws LocalizationException {
		model.fireTableStructureChanged();
	}

	private static List<JFileItemDescriptor> upload(final JList<JFileItemDescriptor> from) {
		final List<JFileItemDescriptor>			result = new ArrayList<>();
		final ListModel<JFileItemDescriptor>	model = from.getModel(); 
		
		for (int index = 0, maxIndex = model.getSize(); index < maxIndex; index++) {
			result.add(model.getElementAt(index));
		}
 		return result;
	}

	private static List<JFileItemDescriptor> upload(final JTable from) {
		final List<JFileItemDescriptor>	result = new ArrayList<>();
		final InnerTableModel			model = (InnerTableModel)from.getModel(); 
		
		for (int index = 0, maxIndex = model.getRowCount(); index < maxIndex; index++) {
			result.add((JFileItemDescriptor)model.getValueAt(index));
		}
 		return result;
	}
	

	private static class InnerTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		private final Localizer					localizer;
		private final List<JFileItemDescriptor>	content = new ArrayList<>();
		
		InnerTableModel(final Localizer localizer) {
			this.localizer = localizer;
		}

		@Override public boolean isCellEditable(final int rowIndex, final int columnIndex) {return false;}
		@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

		@Override
		public int getRowCount() {
			if (content == null) {
				return 0;
			}
			else {
				return content.size();
			}
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0 :
					try {
						return localizer.getValue(TABLE_COL_FILE);
					} catch (LocalizationException e) {
						return TABLE_COL_FILE;
					}
				case 1 :
					try {
						return localizer.getValue(TABLE_COL_SIZE);
					} catch (LocalizationException e) {
						return TABLE_COL_FILE;
					}
				case 2 :
					try {
						return localizer.getValue(TABLE_COL_CREATED);
					} catch (LocalizationException e) {
						return TABLE_COL_FILE;
					}
				default : throw new IllegalArgumentException();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0 : return JFileItemDescriptor.class;
				case 1 : return Long.class;
				case 2 : return Date.class;
				default : throw new IllegalArgumentException();
			}
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			final JFileItemDescriptor	desc = getValueAt(rowIndex);
			
			switch (columnIndex) {
				case 0 : return desc;
				case 1 : return desc.getSize();
				case 2 : return desc.getLastModified();
				default : throw new IllegalArgumentException();
			}
		}

		JFileItemDescriptor getValueAt(final int rowIndex) {
			return content.get(rowIndex);
		}
		
		void refreshContent(final List<JFileItemDescriptor> content) {
			this.content.clear();
			this.content.addAll(content);
			fireTableDataChanged();
		}
	}
	
	
	private static class SelectionModelWrapper implements ListSelectionModel {
		private final ModelContent			contentGetter;
		private final ListSelectionModel	nested;
		private final SelectedObjects		objects;
		
		@FunctionalInterface
		private interface ModelContent {
			List<JFileItemDescriptor> extract();
		}
		
		SelectionModelWrapper(final ModelContent contentGetter, final ListSelectionModel nested, final SelectedObjects objects) {
			this.contentGetter = contentGetter;
			this.nested = nested;
			this.objects = objects;
		}

		@Override
		public void setSelectionInterval(final int index0, final int index1) {
			for (int[] item : collectAllowedIntervals(index0, index1)) {
				nested.setSelectionInterval(item[0], item[1]);
			}
		}

		@Override
		public void addSelectionInterval(int index0, int index1) {
			for (int[] item : collectAllowedIntervals(index0, index1)) {
				nested.addSelectionInterval(item[0], item[1]);
			}
		}

		@Override
		public void removeSelectionInterval(final int index0, final int index1) {
			nested.removeSelectionInterval(index0, index1);
		}

		@Override
		public int getMinSelectionIndex() {
			return nested.getMinSelectionIndex();
		}

		@Override
		public int getMaxSelectionIndex() {
			return nested.getMaxSelectionIndex();
		}

		@Override
		public boolean isSelectedIndex(final int index) {
			final boolean	result = nested.isSelectedIndex(index);
			
			if (result) {
				return allowSelection(index);
			}
			else {
				return false;
			}
		}

		@Override
		public int getAnchorSelectionIndex() {
			return nested.getAnchorSelectionIndex();
		}

		@Override
		public void setAnchorSelectionIndex(final int index) {
			// TODO Auto-generated method stub
		}

		@Override
		public int getLeadSelectionIndex() {
			return nested.getLeadSelectionIndex();
		}

		@Override
		public void setLeadSelectionIndex(final int index) {
			// TODO Auto-generated method stub
		}

		@Override
		public void clearSelection() {
			nested.clearSelection();
		}

		@Override
		public boolean isSelectionEmpty() {
			return nested.isSelectionEmpty();
		}

		@Override
		public void insertIndexInterval(final int index, final int length, final boolean before) {
			nested.insertIndexInterval(index, length, before);
		}

		@Override
		public void removeIndexInterval(final int index0, final int index1) {
			nested.removeIndexInterval(index0, index1);
		}

		@Override
		public void setValueIsAdjusting(final boolean valueIsAdjusting) {
			nested.setValueIsAdjusting(valueIsAdjusting);
		}

		@Override
		public boolean getValueIsAdjusting() {
			return nested.getValueIsAdjusting();
		}

		@Override
		public void setSelectionMode(final int selectionMode) {
			nested.setSelectionMode(selectionMode);
		}

		@Override
		public int getSelectionMode() {
			return nested.getSelectionMode();
		}

		@Override
		public void addListSelectionListener(final ListSelectionListener x) {
			nested.addListSelectionListener(x);
		}

		@Override
		public void removeListSelectionListener(final ListSelectionListener x) {
			nested.removeListSelectionListener(x);
		}

		private boolean allowSelection(final int index) {
			final JFileItemDescriptor	desc = contentGetter.extract().get(index);
			
			switch (objects) {
				case ALL			: return true;
				case DIRECTORIES	: return desc.isDirectory();
				case FILES			: return !desc.isDirectory();
				case NONE			: return false;
				default : throw new UnsupportedOperationException("Selected objects ["+objects+"] is not supported yet");
			}
		}
		
		private List<int[]> collectAllowedIntervals(final int index0, final int index1) {
			final List<int[]>	intervals = new ArrayList<>();
			int					start = -1;
			
			for (int index = index0; index <= index1; index++) {
				if (allowSelection(index)) {
					if (start == -1) {
						start = index;
					}
				}
				else {
					if (start != -1) {
						intervals.add(new int[] {start, index-1});
						start = -1;
					}
				}
			}
			if (start != -1) {
				intervals.add(new int[] {start, index1});
			}
			return null;
		}
	}
	
	private static class NullSelectionModel implements ListSelectionModel {
		@Override public void setSelectionInterval(int index0, int index1) {}
		@Override public void addSelectionInterval(int index0, int index1) {}
		@Override public void removeSelectionInterval(int index0, int index1) {}
		@Override public int getMinSelectionIndex() {return -1;}
		@Override public int getMaxSelectionIndex() {return -1;}
		@Override public boolean isSelectedIndex(int index) {return false;}
		@Override public int getAnchorSelectionIndex() {return -1;}
		@Override public void setAnchorSelectionIndex(int index) {}
		@Override public int getLeadSelectionIndex() {return -1;}
		@Override public void setLeadSelectionIndex(int index) {}
		@Override public void clearSelection() {}
		@Override public boolean isSelectionEmpty() {return true;}
		@Override public void insertIndexInterval(int index, int length, boolean before) {}
		@Override public void removeIndexInterval(int index0, int index1) {}
		@Override public void setValueIsAdjusting(boolean valueIsAdjusting) {}
		@Override public boolean getValueIsAdjusting() {return false;}
		@Override public void setSelectionMode(int selectionMode) {}
		@Override public int getSelectionMode() {return SINGLE_SELECTION;}
		@Override public void addListSelectionListener(ListSelectionListener x) {}
		@Override public void removeListSelectionListener(ListSelectionListener x) {}
	}
}
