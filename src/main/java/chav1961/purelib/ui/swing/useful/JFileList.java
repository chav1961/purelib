package chav1961.purelib.ui.swing.useful;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentKeeper;

public abstract class JFileList extends JPanel implements LocaleChangeListener, FileContentKeeper {
	private static final long 				serialVersionUID = 1L;
	private static final String				CARD_ICONS = "icons";
	private static final String				CARD_TABLE = "table";
	private static final String				TABLE_COL_FILE = "chav1961.purelib.ui.swing.useful.JFileList.column1";
	private static final String				TABLE_COL_FILE_TT = "chav1961.purelib.ui.swing.useful.JFileList.column1.tt";
	private static final String				TABLE_COL_SIZE = "chav1961.purelib.ui.swing.useful.JFileList.column2";
	private static final String				TABLE_COL_SIZE_TT = "chav1961.purelib.ui.swing.useful.JFileList.column2.tt";
	private static final String				TABLE_COL_CREATED = "chav1961.purelib.ui.swing.useful.JFileList.column3";
	private static final String				TABLE_COL_CREATED_TT = "chav1961.purelib.ui.swing.useful.JFileList.column3.tt";
	private static final String				TABLE_TT_FILE = "chav1961.purelib.ui.swing.useful.JFileList.tooltip.file";
	private static final String				TABLE_TT_DIRECTORY = "chav1961.purelib.ui.swing.useful.JFileList.tooltip.directory";
	
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
	private final LightWeightListenerList<ListDataListener>			listeners = new LightWeightListenerList<>(ListDataListener.class);
	private final LightWeightListenerList<ListSelectionListener>	selectionListeners = new LightWeightListenerList<>(ListSelectionListener.class);
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
	private final ListModel<JFileItemDescriptor> totalModel = new ListModel<JFileItemDescriptor>() {
													@Override public int getSize() {return getModelSize();}
													@Override public JFileItemDescriptor getElementAt(int index) {return getModelItem(index);}
													@Override public void addListDataListener(final ListDataListener l) {listeners.addListener(l);}
													@Override public void removeListDataListener(final ListDataListener l) {listeners.removeListener(l);}
												};
												
	private ContentViewType				viewType;
	private String						currentPath = "/";
	
	public JFileList(final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final boolean insertParent, final boolean useMultiselection) throws NullPointerException, LocalizationException, IOException {
		this(localizer, logger, fsi, insertParent, useMultiselection ? SelectionType.MULTIPLE : SelectionType.SINGLE, SelectedObjects.ALL, ContentViewType.AS_ICONS);
	}

	public JFileList(final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final boolean insertParent, final SelectionType selectionType, final SelectedObjects selectedObjects, final ContentViewType viewType) throws NullPointerException, LocalizationException, IOException {
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

			table.setDefaultRenderer(JFileItemDescriptor.class, new InnerTableCellRenderer<JFileItemDescriptor>((t,v)->new JLabel(v.getName(), v.isDirectory() ? PureLibStandardIcons.DIRECTORY.getIcon() : PureLibStandardIcons.FILE.getIcon() , JLabel.LEFT)));
			table.setDefaultRenderer(Long.class, new InnerTableCellRenderer<Long>((t,v)->new JLabel("<html><body><p><b>"+v.longValue()+"</b></p></body></html>", JLabel.RIGHT))); 
			table.setDefaultRenderer(Date.class, new InnerTableCellRenderer<Date>((t,v)->new JLabel(new SimpleDateFormat("yyyy-MM-dd", localizer.currentLocale().getLocale()).format(v), JLabel.LEFT))); 
			table.setTableHeader(new NavigatorTableHeader(table.getColumnModel(), model));
			model.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(final TableModelEvent e) {
					switch (e.getType()) {
						case TableModelEvent.INSERT :
							notifyDataInserted(e.getFirstRow(), e.getLastRow());
							break;
						case TableModelEvent.DELETE :
							notifyDataRemoved(e.getFirstRow(), e.getLastRow());
							break;
						case TableModelEvent.UPDATE :
							notifyDataChanged(e.getFirstRow(), e.getLastRow());
							break;
					}
				}
			});
			table.getSelectionModel().addListSelectionListener((e)->notifySelectionChanged(e));
			table.setDragEnabled(true);
			table.setTransferHandler(new FileTransferHandler());
			
			list.setCellRenderer(new ListCellRenderer<JFileItemDescriptor>() {
				@Override
				public Component getListCellRendererComponent(final JList<? extends JFileItemDescriptor> list, final JFileItemDescriptor value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					JLabel		label = null;
					
					switch (viewType) {
						case AS_ICONS		:
							label = new JLabel(value.getName(), value.isDirectory() ? PureLibStandardIcons.DIRECTORY.getIcon() : PureLibStandardIcons.FILE.getIcon(), JLabel.LEFT);
							label.setOpaque(true);
							if (isSelected) {
								label.setForeground(list.getSelectionForeground());
								label.setBackground(list.getSelectionBackground());
							}
							else {
								label.setForeground(list.getForeground());
								label.setBackground(list.getBackground());
							}
							break;
						case AS_LARGE_ICONS	:
							final Icon	icon = value.isDirectory() ? PureLibStandardIcons.LARGE_DIRECTORY.getIcon() : PureLibStandardIcons.LARGE_FILE.getIcon();
							
							label = new JLabel(value.getName(), JLabel.CENTER);

							label.setIcon(icon);
						    label.setHorizontalTextPosition(JLabel.CENTER);
						    label.setVerticalTextPosition(JLabel.BOTTOM);
							label.setOpaque(true);
							if (isSelected) {
								label.setForeground(list.getSelectionForeground());
								label.setBackground(list.getSelectionBackground());
							}
							else {
								label.setForeground(list.getForeground());
								label.setBackground(list.getBackground());
							}
							label.revalidate();
							break;
						case AS_TABLE		:
							return null;
						default	: throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet");
					}
					if (cellHasFocus) {
						label.setBorder(new LineBorder(Color.BLUE));
					}
					return label; 
				}
			});
			list.getModel().addListDataListener(new ListDataListener() {
				@Override
				public void intervalAdded(final ListDataEvent e) {
					notifyDataInserted(e.getIndex0(), e.getIndex1());
				}

				@Override
				public void intervalRemoved(final ListDataEvent e) {
					notifyDataRemoved(e.getIndex0(), e.getIndex1());
				}

				@Override
				public void contentsChanged(final ListDataEvent e) {
					notifyDataChanged(e.getIndex0(), e.getIndex1());
				}
			});
			list.addListSelectionListener((e)->notifySelectionChanged(e));
			list.setDragEnabled(true);
			list.setTransferHandler(new FileTransferHandler());
			
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
//					list.setSelectionModel(new SelectionModelWrapper(()->upload(list), new DefaultListSelectionModel(), selObjects));
//					table.setSelectionModel(new SelectionModelWrapper(()->upload(table), new DefaultListSelectionModel(), selObjects));
//					list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//					table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					break;
				default:
					break;
			}

			SwingUtils.redirectMouseAndKeyEvents(table, this);
			SwingUtils.redirectMouseAndKeyEvents(list, this);
			
			add(new JScrollPane(table), CARD_TABLE);
			add(new JScrollPane(list), CARD_ICONS);
			FileTransferHandler.prepare4DroppingFiles(table);
			FileTransferHandler.prepare4DroppingFiles(list);

			fillLocalizationStrings();
			setContentViewType(viewType);
		}
	}

	@Override public abstract void placeFileContent(final Point location, final Iterable<JFileItemDescriptor> content);
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizationStrings();
	}
	
	public int locationToIndex(final Point point) {
		switch (viewType) {
			case AS_ICONS : case AS_LARGE_ICONS : return list.locationToIndex(point);
			case AS_TABLE : return table.rowAtPoint(point);
			default : throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet");
		}
	}
	
	public ListModel<JFileItemDescriptor> getModel() {
		return totalModel;
	}

	public void addListSelectionListener(final ListSelectionListener l) {
		selectionListeners.addListener(l);
	}

	public void removeListSelectionListener(final ListSelectionListener l) {
		selectionListeners.removeListener(l);
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
					list.setLayoutOrientation(JList.VERTICAL_WRAP);
					break;
				case AS_LARGE_ICONS	:
					list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
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
			items.sort((o1,o2)->{
				if (o1.isDirectory() == o2.isDirectory()) {
					return o1.getName().compareTo(o2.getName());
				}
				else {
					return o1.isDirectory() ? -1 : 1;
				}
			});
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

	@Override
	public boolean hasFileContentNow() {
		return getModelSize() > 0;
	}

	@Override
	public Collection<JFileItemDescriptor> getFileContent() {
		final List<JFileItemDescriptor>	result = new ArrayList<>();
		
		for(int index = 0, maxIndex = getModelSize(); index < maxIndex; index++) {
			result.add(getModelItem(index));
		}
		return result;
	}

	@Override
	public boolean hasSelectedFileContentNow() {
		return isAnythingSelected();
	}

	@Override
	public Collection<JFileItemDescriptor> getSelectedFileContent() {
		final List<JFileItemDescriptor>	result = new ArrayList<>();
		
		for(int index = 0, maxIndex = getModelSize(); index < maxIndex; index++) {
			if (isItemSelected(index)) {
				result.add(getModelItem(index));
			}
		}
		return result;
	}

	
	private String getTooltip4Path(final String path) {
		try(final FileSystemInterface	temp = fsi.clone().open(path)) {

			if (temp.isDirectory()) {
				return String.format(localizer.getValue(TABLE_TT_DIRECTORY), temp.getPath(), new Date(temp.lastModified()));
			}
			else {
				return String.format(localizer.getValue(TABLE_TT_FILE), temp.getPath(), temp.size(), new Date(temp.lastModified()));
			}
		} catch (IOException | LocalizationException e) {
			return path;
		}
	}
	
	private int getModelSize() {
		switch (viewType) {
			case AS_ICONS : case AS_LARGE_ICONS : return list.getModel().getSize();
 			case AS_TABLE: return table.getModel().getRowCount();
			default : throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet"); 
		}
	}
	
	private JFileItemDescriptor getModelItem(final int index) {
		switch (viewType) {
			case AS_ICONS : case AS_LARGE_ICONS : return list.getModel().getElementAt(index);
			case AS_TABLE: return (JFileItemDescriptor)table.getModel().getValueAt(index,0);
			default : throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet"); 
		}
	}

	private boolean isAnythingSelected() {
		switch (viewType) {
			case AS_ICONS : case AS_LARGE_ICONS : return !list.getSelectionModel().isSelectionEmpty();
			case AS_TABLE: return !table.getSelectionModel().isSelectionEmpty();
			default : throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet"); 
		}
	}
	
	private boolean isItemSelected(final int index) {
		switch (viewType) {
			case AS_ICONS : case AS_LARGE_ICONS : return list.getSelectionModel().isSelectedIndex(index);
			case AS_TABLE: return table.getSelectionModel().isSelectedIndex(index);
			default : throw new UnsupportedOperationException("View type ["+viewType+"] is not supported yet"); 
		}
	}
	
	private void notifyDataInserted(final int from, final int to) {
		final ListDataEvent	lde = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, from, to);
		
		listeners.fireEvent((l)->l.intervalAdded(lde));
	}

	private void notifyDataRemoved(final int from, final int to) {
		final ListDataEvent	lde = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, from, to);
		
		listeners.fireEvent((l)->l.intervalRemoved(lde));
	}

	private void notifyDataChanged(final int from, final int to) {
		final ListDataEvent	lde = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, from, to);
		
		listeners.fireEvent((l)->l.contentsChanged(lde));
	}

	private void notifySelectionChanged(final ListSelectionEvent event) {
		System.err.println("Changed");
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

	private static class InnerTableCellRenderer<T> implements TableCellRenderer {
		private static interface LabelBuilder<T> {
			JLabel build(JTable table, T value);
		}

		private final LabelBuilder<T>	builder;
		
		public InnerTableCellRenderer(final LabelBuilder<T> builder) {
			this.builder = builder;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			final JLabel				label = builder.build(table, (T)value);

			label.setOpaque(true);
			if (isSelected) {
				label.setForeground(table.getSelectionForeground());
				label.setBackground(table.getSelectionBackground());
			}
			else {
				label.setForeground(table.getForeground());
				label.setBackground(table.getBackground());
			}
			return label;
		}
		
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

		public String getColumnTooltip(final int columnIndex) {
			switch (columnIndex) {
				case 0 :
					try {
						return localizer.getValue(TABLE_COL_FILE_TT);
					} catch (LocalizationException e) {
						return TABLE_COL_FILE_TT;
					}
				case 1 :
					try {
						return localizer.getValue(TABLE_COL_SIZE_TT);
					} catch (LocalizationException e) {
						return TABLE_COL_FILE_TT;
					}
				case 2 :
					try {
						return localizer.getValue(TABLE_COL_CREATED_TT);
					} catch (LocalizationException e) {
						return TABLE_COL_FILE_TT;
					}
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
//			for (int[] item : collectAllowedIntervals(index0, index1)) {
//				nested.setSelectionInterval(item[0], item[1]);
//			}
			nested.setSelectionInterval(index0, index1);
		}

		@Override
		public void addSelectionInterval(int index0, int index1) {
//			for (int[] item : collectAllowedIntervals(index0, index1)) {
//				nested.addSelectionInterval(item[0], item[1]);
//			}
			nested.addSelectionInterval(index0, index1);
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
			return nested.isSelectedIndex(index);
//			final boolean	result = nested.isSelectedIndex(index);
//			
//			if (result) {
//				return allowSelection(index);
//			}
//			else {
//				return false;
//			}
		}

		@Override
		public int getAnchorSelectionIndex() {
			return nested.getAnchorSelectionIndex();
		}

		@Override
		public void setAnchorSelectionIndex(final int index) {
			// TODO Auto-generated method stub
			nested.setAnchorSelectionIndex(index);
		}

		@Override
		public int getLeadSelectionIndex() {
			return nested.getLeadSelectionIndex();
		}

		@Override
		public void setLeadSelectionIndex(final int index) {
			// TODO Auto-generated method stub
			nested.setLeadSelectionIndex(index);
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
			return true;
//			final JFileItemDescriptor	desc = contentGetter.extract().get(index);
//			
//			switch (objects) {
//				case ALL			: return true;
//				case DIRECTORIES	: return desc.isDirectory();
//				case FILES			: return !desc.isDirectory();
//				case NONE			: return false;
//				default : throw new UnsupportedOperationException("Selected objects ["+objects+"] is not supported yet");
//			}
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
			return intervals;
		}
	}

	private static class NavigatorTableHeader extends JTableHeader {
		private static final long 		serialVersionUID = 1L;

		private final InnerTableModel	tableModel;
		
		private NavigatorTableHeader(final TableColumnModel columnModel, final InnerTableModel model) {
	    	super(columnModel);
	    	this.tableModel = model;
	    }

		@Override
	    public String getToolTipText(final MouseEvent e) {
	        final Point	p = e.getPoint();
	        final int 	index = columnModel.getColumnIndexAtX(p.x);
	        final int 	realIndex = columnModel.getColumn(index).getModelIndex();
	        
	        return tableModel.getColumnTooltip(realIndex);
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
