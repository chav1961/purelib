package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.JToolBarWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileSystemNavigator.NavigatorModel.NavigatorRecord;

public class JFileSystemNavigator extends JSplitPane implements LocaleChangeListener {
	private static final long 	serialVersionUID = -1220218092405433021L;

	public static final String	PROP_ACCESS = "access";
	public static final String	PROP_ACCESS_READ_ONLY = "readOnly";
	public static final String	PROP_ACCESS_READ_WRITE = "readWrite";
	public static final String	PROP_SELECTION_TYPE = "selectionType";
	public static final String	PROP_NONE_SELECTION = "none";
	public static final String	PROP_SINGLE_SELECTION = "singleSelection";
	public static final String	PROP_MULTIPLE_SELECTION = "multipleSelection";
	public static final String	PROP_SELECTED_OBJECTS = "selectedObjects";
	public static final String	PROP_NONE_OBJECTS = "none";
	public static final String	PROP_FILES_ONLY = "filesOnly";
	public static final String	PROP_DIRECTORIES_ONLY = "directoriesOnly";
	public static final String	PROP_FILES_AND_DIRECTORIES = "all";
	
	private static final Icon	DIR_ICON = new ImageIcon(JFileSystemNavigator.class.getResource("directory.png"));
	private static final Icon	FILE_ICON = new ImageIcon(JFileSystemNavigator.class.getResource("file.png"));
	private static final long	REFRESH_DELAY = 500;
	private static final String	CARD_ICONS = "icons";
	private static final String	CARD_TABLE = "table";
	private static final String	DEFAULT_URI_ENCODING = "UTF-8";
	
	private static enum ContentViewType {
		AS_TABLE, 
		AS_ICONS,
		AS_LARGE_ICONS;
	}
	
	private static enum SelectionType {
		NONE(PROP_NONE_SELECTION),
		SINGLE(PROP_SINGLE_SELECTION),
		MULTIPLE(PROP_MULTIPLE_SELECTION);
		
		private final String	value;
		
		private SelectionType(final String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}

	private static enum SelectedObjects {
		NONE(PROP_NONE_SELECTION),
		FILES(PROP_SINGLE_SELECTION),
		DIRECTORIES(PROP_MULTIPLE_SELECTION),
		ALL(PROP_MULTIPLE_SELECTION);
		
		private final String	value;
		
		private SelectedObjects(final String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final FileSystemInterface		fsi;
	private final Properties				props;
	private final boolean					readOnly;
	private final SelectionType				selectionType;
	private final SelectedObjects			selectedObjects;
	private final JTree						tree = new JTree();
	private final JList<String>				list = new JList<>(new DefaultListModel<String>());
	private final NavigatorModel			model;  
	private final JTable					table;
	private final CardLayout				card = new CardLayout();
	private final JPanel					rightCard = new JPanel(card);
	private final JPanel					right = new JPanel(new BorderLayout());
	private final JToolBarWithMeta			toolbar;
	private final DefaultMutableTreeNode	root = new DefaultMutableTreeNode();
	
	private ContentViewType		contentView = ContentViewType.AS_ICONS;
	private TimerTask			tt = null;
	private volatile boolean	fastRefresh = false;
	
	public JFileSystemNavigator(final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final Properties props) throws IOException, NullPointerException, EnvironmentException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else {
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("useful.xml"));
			
			this.localizer = localizer;
			this.logger = logger;
			this.fsi = fsi;
			this.props = props;
			this.readOnly = checkParameter(props, PROP_ACCESS, PROP_ACCESS_READ_ONLY, PROP_ACCESS_READ_ONLY, PROP_ACCESS_READ_WRITE) == 0;
			this.selectionType = SelectionType.values()[checkParameter(props, PROP_SELECTION_TYPE, SelectionType.NONE.getValue(), SelectionType.NONE.getValue(),SelectionType.SINGLE.getValue(), SelectionType.MULTIPLE.getValue())];
			this.selectedObjects = SelectedObjects.values()[checkParameter(props, PROP_SELECTED_OBJECTS, SelectedObjects.NONE.getValue(), SelectedObjects.NONE.getValue(), SelectedObjects.FILES.getValue(), SelectedObjects.DIRECTORIES.getValue(), SelectedObjects.ALL.getValue())];
			
			this.toolbar = new JToolBarWithMeta(mdi.byUIPath(URI.create("ui:/model/navigation.top.fileSystemNavigator")));
			this.toolbar.setFloatable(false);
			this.model = new NavigatorModel(localizer);
			this.table = new JTable(this.model);
			SwingUtils.assignActionListeners(this.toolbar, this);
			
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.setRootVisible(true);
			tree.addTreeWillExpandListener(new TreeWillExpandListener() {
				@Override public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}
				
				@Override
				public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
					final DefaultMutableTreeNode	last = (DefaultMutableTreeNode)event.getPath().getLastPathComponent(); 
					final String					path = (String) last.getUserObject();
					
					try(final FileSystemInterface	partFsi = fsi.clone().open(path)) {
						fillChildren(last, partFsi);
					} catch (IOException e) {
						logger.message(Severity.error, "error expanding tree: "+e.getLocalizedMessage(),e);
					}
				}
			});
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(final TreeSelectionEvent event) {
					final DefaultMutableTreeNode	last = (DefaultMutableTreeNode)event.getPath().getLastPathComponent(); 
					final String					path = (String) last.getUserObject();
					
					if (tt != null) {
						tt.cancel();
						tt = null;
					}
					if (fastRefresh) {
						try(final FileSystemInterface	partFsi = fsi.clone().open(path)) {
							
							refreshRightPanel(partFsi);
						} catch (IOException e) {
							logger.message(Severity.error, "error refreshing right panel: "+e.getLocalizedMessage(),e);
						} finally {
							fastRefresh = false;
						}
					}
					else {
						tt = new TimerTask() {
							@Override
							public void run() {
								try(final FileSystemInterface	partFsi = fsi.clone().open(path)) {
									
									refreshRightPanel(partFsi);
								} catch (IOException e) {
									logger.message(Severity.error, "error refreshing right panel: "+e.getLocalizedMessage(),e);
								}
							}
						};
						PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, REFRESH_DELAY);
					}
				}
			});
			tree.setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					final JLabel		label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					try {final String	path = URLDecoder.decode(label.getText(), DEFAULT_URI_ENCODING);
					
						label.setText(path.endsWith("/") ? path : path.substring(path.lastIndexOf('/')+1));
						label.setIcon(DIR_ICON);
					} catch (UnsupportedEncodingException e) {
						label.setText("I/O error: "+e.getLocalizedMessage());
					}

					return label;
				}
			});
			
			list.setCellRenderer(new ListCellRenderer<String>() {
				@Override
				public Component getListCellRendererComponent(final JList<? extends String> list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					try(final FileSystemInterface	temp = fsi.clone().open(value)) {
						final String	name = URLDecoder.decode(temp.getName(), DEFAULT_URI_ENCODING);
						
						switch (contentView) {
							case AS_ICONS		:
								final JLabel	label = new JLabel(name, temp.isDirectory() ? DIR_ICON : FILE_ICON, JLabel.LEFT);
								final String	tooltip = "<html><body><p>size: <b>"+temp.size()+"</b></p><p>created: <b>"+new Date(temp.lastModified())+"</b></p></body></html>";
								
								label.setOpaque(true);
								if (isSelected) {
									label.setForeground(list.getSelectionForeground());
									label.setBackground(list.getSelectionBackground());
								}
								else {
									label.setForeground(list.getForeground());
									label.setBackground(list.getBackground());
								}
								label.setToolTipText(tooltip);
								return label;
							case AS_LARGE_ICONS	:
								final JLabel	largeLabel = new JLabel(name,temp.isDirectory() ? DIR_ICON : FILE_ICON, JLabel.CENTER);
								final String	largeTooltip = "<html><body><p>size: <b>"+temp.size()+"</b></p><p>created: <b>"+new Date(temp.lastModified())+"</b></p></body></html>";
								
								largeLabel.setVerticalTextPosition(JLabel.BOTTOM);
								largeLabel.setHorizontalTextPosition(JLabel.CENTER);								
								largeLabel.setOpaque(true);
								if (isSelected) {
									largeLabel.setForeground(list.getSelectionForeground());
									largeLabel.setBackground(list.getSelectionBackground());
								}
								else {
									largeLabel.setForeground(list.getForeground());
									largeLabel.setBackground(list.getBackground());
								}
								largeLabel.setToolTipText(largeTooltip);
								return largeLabel;
							default :
								throw new UnsupportedOperationException("Content view ["+contentView+"] is not supported yet");
						}
					} catch (IOException e) {
						return new JLabel("I/O error on ["+value+"]");
					}
				}
			});
			list.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(final MouseEvent e) {}
				@Override public void mousePressed(final MouseEvent e) {}
				@Override public void mouseExited(final MouseEvent e) {}
				@Override public void mouseEntered(final MouseEvent e) {}
				
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
						final int	index = list.locationToIndex(e.getPoint());
						
						if (index >= 0) {
							clickInRightPanel((String)list.getModel().getElementAt(index));
						}
					}
				}
			});

			table.setTableHeader(new NavigatorTableHeader(table.getColumnModel(), model));
			table.setDefaultRenderer(NavigatorModel.NavigatorRecord.class, (table, value, isSelected, hasFocus, row, column) -> {
					final NavigatorModel.NavigatorRecord	rec = (NavigatorModel.NavigatorRecord)value;
					
					try {final JLabel	label = new JLabel(URLDecoder.decode(rec.name.substring(rec.name.lastIndexOf('/')+1), DEFAULT_URI_ENCODING), rec.isDirectory ? DIR_ICON : FILE_ICON, JLabel.LEFT);
					
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
					} catch (UnsupportedEncodingException e) {
						return new JLabel("I/O error on ["+value+"]");
					}
				}
			);
			table.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(final MouseEvent e) {}
				@Override public void mousePressed(final MouseEvent e) {}
				@Override public void mouseExited(final MouseEvent e) {}
				@Override public void mouseEntered(final MouseEvent e) {}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
						final int	index = table.rowAtPoint(e.getPoint());
						
						if (index >= 0) {
							clickInRightPanel(((NavigatorRecord)table.getModel().getValueAt(index,0)).name);
						}
					}
				}
			});
			
			root.setUserObject("/");
			fillChildren(root, fsi);
			((DefaultTreeModel)tree.getModel()).setRoot(root);
			
			rightCard.add(new JScrollPane(list), CARD_ICONS);
			rightCard.add(new JScrollPane(table), CARD_TABLE);
			
			right.add(toolbar, BorderLayout.NORTH);
			right.add(rightCard, BorderLayout.CENTER);
			card.show(rightCard, CARD_ICONS);
			
			setLeftComponent(new JScrollPane(tree));
			setRightComponent(right);
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(toolbar, oldLocale, newLocale);
	}

	public String getSelectedObject() {
		if (selectionType == SelectionType.SINGLE) {
			return null;
		}
		else {
			throw new IllegalStateException("Calling this method is avaiable for ["+PROP_SELECTION_TYPE+"] = ["+PROP_SINGLE_SELECTION+"] only"); 
		}
	}

	public String[] getSelectedObjects() {
		if (selectionType == SelectionType.MULTIPLE) {
			return null;
		}
		else {
			throw new IllegalStateException("Calling this method is avaiable for ["+PROP_SELECTION_TYPE+"] = ["+PROP_MULTIPLE_SELECTION+"] only"); 
		}
	}
	
	@OnAction("action:/asIcons")
	private void asIcons() {
		contentView = ContentViewType.AS_ICONS;
		card.show(rightCard, CARD_ICONS);
		list.setLayoutOrientation(JList.VERTICAL);
		refreshRightPanel();
	}

	@OnAction("action:/asLargeIcons")
	private void asLagreIcons() {
		contentView = ContentViewType.AS_LARGE_ICONS;
		card.show(rightCard, CARD_ICONS);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		refreshRightPanel();
	}
	
	@OnAction("action:/asTable")
	private void asTable() {
		contentView = ContentViewType.AS_TABLE;
		card.show(rightCard, CARD_TABLE);
		refreshRightPanel();
	}
	
	private void fillChildren(final DefaultMutableTreeNode node, final FileSystemInterface fsi) throws IOException {
		final boolean[]	flag = new boolean[1];
		
		node.removeAllChildren();
		
		for (String item : fsi.list()) {
			try(final FileSystemInterface	child = fsi.clone().open(item)) {
				if (child.isDirectory()) {
					flag[0] = false;
					child.list((i)->{
						return (flag[0] |= i.isDirectory()) ? ContinueMode.STOP : ContinueMode.CONTINUE; 
					});
					
					node.add(new DefaultMutableTreeNode(child.getPath()) {
						private static final long	serialVersionUID = 1L;
						
						final boolean				dirsInside = flag[0];
						
						@Override
						public boolean isLeaf() {
							return !dirsInside;
						}
					});
				}
			}
		}
	}

	private void refreshRightPanel() {
		final TreePath	selected = tree.getSelectionPath();
		
		if (selected != null) {
			final String	path = (String)((DefaultMutableTreeNode)selected.getLastPathComponent()).getUserObject(); 
					
			try(final FileSystemInterface	temp = fsi.clone().open(path)) {
				refreshRightPanel(temp);
			} catch (IOException e) {
				logger.message(Severity.error, "I/O error refreshing right panel on ["+path+"]: "+e.getLocalizedMessage(),e);
			}
		}
		else {
			try{refreshRightPanel(null);
			} catch (IOException e) {
				logger.message(Severity.error, "I/O error refreshing right panel: "+e.getLocalizedMessage(),e);
			}
		}
	}
	
	private void refreshRightPanel(final FileSystemInterface current) throws IOException {
		switch (contentView) {
			case AS_ICONS : case AS_LARGE_ICONS :
				if (current != null) {
					final List<String>	content = new ArrayList<>();
					
					current.list((i)->{
						if (i.isDirectory()) {
							content.add(i.getPath());
						}
						return ContinueMode.CONTINUE;
					});
					current.list((i)->{
						if (i.isFile()) {
							content.add(i.getPath());
						}
						return ContinueMode.CONTINUE;
					});
					SwingUtilities.invokeLater(()->{
						final DefaultListModel<String>	model = (DefaultListModel<String>)list.getModel();
						model.clear();
						model.addAll(content);
					});
				}
				else {
					SwingUtilities.invokeLater(()->{
						final DefaultListModel<String>	model = (DefaultListModel<String>)list.getModel();
						model.clear();
					});
				}
				break;
			case AS_TABLE	:
				model.refillModel(current);
				break;
			default:
				throw new UnsupportedOperationException("Content type ["+contentView+"] is not supported yet");
		}
	}

	private void clickInRightPanel(final String path) {
		tree.expandPath(tree.getSelectionPath());
		SwingUtilities.invokeLater(()->findAndSelect((DefaultMutableTreeNode)tree.getModel().getRoot(),path));
	}

	private void findAndSelect(final DefaultMutableTreeNode node, final String path) {
		for (int index = 0, maxIndex = node.getChildCount(); index < maxIndex; index++) {
			final DefaultMutableTreeNode	item = (DefaultMutableTreeNode)node.getChildAt(index); 
			
			if (Objects.equals(item.getUserObject(),path)) {
				final TreeNode[]	tn = ((DefaultMutableTreeNode)node.getChildAt(index)).getPath();
				
				fastRefresh = true;
				tree.setSelectionPath(new TreePath(tn));
				break;
			}
			else {
				findAndSelect(item, path);
			}
		}
	}

	private void fillLocalizedStrings() {
		model.fireTableStructureChanged();
	}

	private static int checkParameter(final Properties props, final String key, final String defaultValue, final String... availableValues) throws IllegalArgumentException {
		final String  value = props.getProperty(key, defaultValue);
		
		for (int index = 0; index < availableValues.length; index++) {
			if (value.equals(availableValues[index])) {
				return index;
			}
		}
		throw new IllegalArgumentException("Property key ["+key+"] contains illegal value ["+value+"]. Available values are "+Arrays.toString(availableValues));
	}
	
	private static class NavigatorTableHeader extends JTableHeader {
		private static final long 	serialVersionUID = 1L;

		private final NavigatorModel	tableModel;
		
		private NavigatorTableHeader(final TableColumnModel columnModel, final NavigatorModel tableModel) {
	    	super(columnModel);
	    	this.tableModel = tableModel;
	    }

		@Override
	    public String getToolTipText(final MouseEvent e) {
	        final Point	p = e.getPoint();
	        final int 	index = columnModel.getColumnIndexAtX(p.x);
	        final int 	realIndex = columnModel.getColumn(index).getModelIndex();
	        
	        return tableModel.getColumnTooltip(realIndex);
	    }
	}
	
	static class NavigatorModel extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		private static final NavigatorRecord[]	EMPTY_LIST = new NavigatorRecord[0];
		
		private static final String	COL1_NAME = "chav1961.purelib.ui.swing.useful.JFileSystemNavigator.NavigatorModel.column1"; 
		private static final String	COL1_TT = "chav1961.purelib.ui.swing.useful.JFileSystemNavigator.NavigatorModel.column1.tt"; 
		private static final String	COL2_NAME = "chav1961.purelib.ui.swing.useful.JFileSystemNavigator.NavigatorModel.column2"; 
		private static final String	COL2_TT = "chav1961.purelib.ui.swing.useful.JFileSystemNavigator.NavigatorModel.column2.tt"; 
		private static final String	COL3_NAME = "chav1961.purelib.ui.swing.useful.JFileSystemNavigator.NavigatorModel.column3"; 
		private static final String	COL3_TT = "chav1961.purelib.ui.swing.useful.JFileSystemNavigator.NavigatorModel.column3.tt";
		
		private final Localizer		localizer;
		private NavigatorRecord[]	rec = EMPTY_LIST;
		
		NavigatorModel(final Localizer localizer) {
			this.localizer = localizer;
		}

		public void refillModel(final FileSystemInterface fsi) throws IOException {
			if (fsi != null) {
				final List<NavigatorRecord>	list = new ArrayList<>();
				
				fsi.list((i)->{
					if (i.isDirectory()) {
						list.add(new NavigatorRecord(true, i.getPath(), i.size(), i.lastModified()));
					}
					return ContinueMode.CONTINUE;
				});
				fsi.list((i)->{
					if (i.isFile()) {
						list.add(new NavigatorRecord(false, i.getPath(), i.size(), i.lastModified()));
					}
					return ContinueMode.CONTINUE;
				});
				rec = list.toArray(new NavigatorRecord[list.size()]);
			}
			else {
				rec = EMPTY_LIST;	
			}
			fireTableDataChanged();
		}
		
		@Override
		public int getRowCount() {
			if (rec == null) {
				return 0;
			}
			else {
				return rec.length;
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
						return localizer.getValue(COL1_NAME);
					} catch (LocalizationException e) {
						return COL1_NAME;
					}
				case 1 :
					try {
						return localizer.getValue(COL2_NAME);
					} catch (LocalizationException e) {
						return COL2_NAME;
					}
				case 2 :
					try {
						return localizer.getValue(COL3_NAME);
					} catch (LocalizationException e) {
						return COL3_NAME;
					}
				default :
					throw new UnsupportedOperationException();
			}
		}

		public String getColumnTooltip(final int columnIndex) {
			switch (columnIndex) {
				case 0 : 
					try {
						return localizer.getValue(COL1_TT);
					} catch (LocalizationException e) {
						return COL1_TT;
					}
				case 1 :
					try {
						return localizer.getValue(COL2_TT);
					} catch (LocalizationException e) {
						return COL2_TT;
					}
				case 2 :
					try {
						return localizer.getValue(COL3_TT);
					} catch (LocalizationException e) {
						return COL3_TT;
					}
				default :
					throw new UnsupportedOperationException();
			}
		}
		
		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0 : return NavigatorRecord.class;
				case 1 : return Long.class;
				case 2 : return Date.class;
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			if (rec == null) {
				return null;
			}
			else {
				switch (columnIndex) {
					case 0 : return rec[rowIndex];
					case 1 : return rec[rowIndex].size;
					case 2 : return new Date(rec[rowIndex].created);
					default : throw new UnsupportedOperationException();
				}
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		}
		
		static class NavigatorRecord {
			private final boolean	isDirectory;
			private final String	name;
			private final long		created;
			private final long		size;
			
			public NavigatorRecord(final boolean isDirectory, final String name, final long size, final long created) {
				this.isDirectory = isDirectory;
				this.name = name;
				this.created = created;
				this.size = size;
			}

			@Override
			public String toString() {
				return "NavigatorRecord [isDirectory=" + isDirectory + ", name=" + name + ", created=" + created + ", size=" + size + "]";
			}
		}
	}

	
	public static void main(final String[] args) throws IOException, NullPointerException, EnvironmentException, ContentException {
		try(final FileSystemInterface	fsi = new FileSystemOnFile(URI.create("file:/c:/"))) {
			final JFileSystemNavigator	navi = new JFileSystemNavigator(PureLibSettings.PURELIB_LOCALIZER, PureLibSettings.CURRENT_LOGGER, fsi, new SubstitutableProperties());
			
			navi.setPreferredSize(new Dimension(800,600));
			JOptionPane.showMessageDialog(null, navi);
		}
		
	}
}