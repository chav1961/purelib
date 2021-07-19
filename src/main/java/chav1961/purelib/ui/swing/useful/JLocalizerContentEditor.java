package chav1961.purelib.ui.swing.useful;


import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.JToolBarWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;

public class JLocalizerContentEditor extends JSplitPane implements LocaleChangeListener {
	private static final long 		serialVersionUID = 3237259445965828668L;
	private static final long		TT_DELAY = 300;
	private static final String		KEY_SELECT_FIELDS_TITLE = "";

	public static enum ContentType {
		XML
	}

	private static enum FocusedComponent {
		TREE, TABLE, OTHER
	}
	
	
	private final Localizer			localizer;
	private final LoggerFacade		logger;
	private final JToolBarWithMeta	tbm;
	private final JTree				tree = new JTree();
	private final InnerTableModel	model = new InnerTableModel();
	private final JFreezableTable	table;
	private Node					root = null;
	private TimerTask				tt = null;
	private FocusedComponent		focused;
	
	public JLocalizerContentEditor(final Localizer localizer, final LoggerFacade logger) throws EnvironmentException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else {
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("useful.xml"));
			
			this.localizer = localizer;
			this.logger = logger;
			this.tbm = new JToolBarWithMeta(mdi.byUIPath(URI.create("ui:/model/navigation.top.localizerContentEditor")));
			this.table = new JFreezableTable(model, "key");
			SwingUtils.assignActionListeners(this.tbm, this);
			SwingUtils.assignActionKeys(this, JSplitPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, (e)->processAction(e.getSource(),e.getActionCommand()), SwingUtils.EditorKeys.values());
			
			setLeftComponent(new JScrollPane(tree));
			tree.addFocusListener(new FocusListener() {
				@Override public void focusLost(FocusEvent e) {focused = FocusedComponent.OTHER;}
				@Override public void focusGained(FocusEvent e) {focused = FocusedComponent.TREE;}
			});
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override 
				public void valueChanged(final TreeSelectionEvent e) {
					tt = new TimerTask() {
						@Override
						public void run() {
							refreshTable(e.getNewLeadSelectionPath());
							refreshState();
						}
					};
					PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, TT_DELAY);
					refreshState();
				}
			});
			
			setRightComponent(new JScrollPane(table));
			table.addFocusListener(new FocusListener() {
				@Override public void focusLost(FocusEvent e) {focused = FocusedComponent.OTHER;}
				@Override public void focusGained(FocusEvent e) {focused = FocusedComponent.TABLE;}
			});
			table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(final ListSelectionEvent e) {
					refreshState();
				}
			});
			
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	protected void loadContent(final InputStream is, final ContentType type) throws IOException {
		
	}

	protected void loadContent(final Localizer localizer) throws IOException {
		
	}
	
	protected void storeContent(final InputStream is, final ContentType type) throws IOException {
		
	}
	
	protected void loadClassContent(final InputStream is) throws IOException, LocalizationException, IllegalArgumentException, ContentException {
		final List<Field>	list = new ArrayList<>(), selected = new ArrayList<>(); 
		
		try(final SimpleURLClassLoader		sucl = new SimpleURLClassLoader(new URL[0]);
			final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is, baos);
			
			CompilerUtils.walkFields(sucl.createClass(baos.toByteArray()), (clazz, field) ->{
				if (!Modifier.isStatic(field.getModifiers())) {
					list.add(field);
				}
			});
		}
		
		if (selectContent2Load(list, selected)) {
			for (Field item : selected) {
				insertKey(field2Label(item));
				insertKey(field2Tooltip(item));
			}
		}
	}

	private boolean selectContent2Load(final List<Field> list, final List<Field> selected) throws LocalizationException {
		final JSelectTableModel	model = new JSelectTableModel(localizer, list);
		final JSelectTable		table = new JSelectTable(model);

		table.setPreferredSize(new Dimension(250,400));
		if (new JLocalizedOptionPane(localizer).confirm(this, new JScrollPane(table), KEY_SELECT_FIELDS_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			model.fillSelected(selected);
			return true;
		}
		else {
			return false;
		}
	}

	@OnAction("action:/loadContent")
	private void loadContent() {
	}

	@OnAction("action:/storeContent")
	private void storeContent() {
	}

	@OnAction("action:/storeContent")
	private void loadClassContent() {
	}
	
	@OnAction("action:/insertKey")
	private void insertKey() {
		insertKey("newKey");
	}

	@OnAction("action:/duplicateKey")
	private void duplicateKey() {
	}

	@OnAction("action:/removeKey")
	private void removeKey() {
		
	}

	@OnAction("action:/insertSubtree")
	private void insertSubtree() {
		
	}

	@OnAction("action:/insertSubtree")
	private void duplicateSubtree() {
		
	}
	
	@OnAction("action:/removeSubtree")
	private void removeSubtree() {
		
	}

	@OnAction("action:/editSubtree")
	private void editSubtree() {
		
	}
	
	private void insertKey(String key) {
		
	}

	private static String field2Label(final Field item) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static String field2Tooltip(final Field item) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void processAction(final Object source, final String action) {
		switch (action) {
			case SwingUtils.ACTION_INSERT 		:
				switch (focused) {
					case TREE	: insertSubtree(); break;
					case TABLE	: insertKey(); break;
					default :
				}
				break;
			case SwingUtils.ACTION_DUPLICATE	:
				switch (focused) {
					case TREE	: duplicateSubtree(); break;
					case TABLE	: duplicateKey(); break;
					default :
				}
				break;
			case SwingUtils.ACTION_DELETE		:
				switch (focused) {
					case TREE	: removeSubtree(); break;
					case TABLE	: removeKey(); break;
					default :
				}
				break;
			case SwingUtils.ACTION_ACCEPT		:
				break;
			case SwingUtils.ACTION_EXIT			:
				break;
			case SwingUtils.ACTION_HELP			:
				break;
			default :
		}
	}
	
	private void refreshTable(final TreePath path) {
		// TODO Auto-generated method stub
		
	}
	
	private void refreshState() {
		// TODO Auto-generated method stub
		
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	private class InnerTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 6525386077011559488L;

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getColumnName(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			
		}
		
		public void refreshContent(final String prefix) {
			
		}
	}
	
	private static class Node {
		private String		key;
		private String		comment;
		private String[]	values;
		private Node[]		children;
		
		@Override
		public String toString() {
			return "Node [key=" + key + ", comment=" + comment + ", values=" + Arrays.toString(values) + ", children=" + Arrays.toString(children) + "]";
		}
	}

	private static class JSelectTable extends JTable {
		private static final long serialVersionUID = 1L;

		private JSelectTable(final TableModel model) {
			super(model);
		}
	}
	
	private  static class JSelectTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		private final Localizer		localizer;
		private final List<Field>	fields;
		private final boolean[]		selection;
		
		JSelectTableModel(final Localizer localizer, final List<Field> fields) {
			this.localizer = localizer;
			this.fields = fields;
			this.selection = new boolean[fields.size()];
			
			Arrays.fill(this.selection, true);
		}
		
		public void fillSelected(final List<Field> selected) {
			for (int index = 0; index < selection.length; index++) {
				if (selection[index]) {
					selected.add(fields.get(index));
				}
			}
		}
		
		@Override
		public int getRowCount() {
			if (fields == null) {
				return 0;
			}
			else {
				return fields.size();
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0	: return "*"; 
				case 1  : return "field";
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0	: return Boolean.class; 
				case 1  : return String.class;
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0	: return true; 
				case 1  : return false;
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0	: return selection[rowIndex]; 
				case 1  : return fields.get(rowIndex).getName();
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0	: selection[rowIndex] = (Boolean)aValue; 
				default : throw new UnsupportedOperationException();
			}
		}
	}
}
