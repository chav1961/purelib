package chav1961.purelib.ui.swing.useful;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimerTask;

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
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.JToolBarWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;

public class JLocalizerContentEditor extends JSplitPane implements LocaleChangeListener {
	private static final long 	serialVersionUID = 3237259445965828668L;
	
	private static final long	TT_DELAY = 300;

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
	
	public JLocalizerContentEditor(final Localizer localizer, final LoggerFacade logger) throws EnvironmentException {
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

	protected void storeContent(final InputStream is, final ContentType type) throws IOException {
		
	}
	
	protected void loadClassContent(final InputStream is) throws IOException {
		
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
}
