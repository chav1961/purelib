package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.JToolBarWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;

public class JFileSystemNavigator extends JSplitPane implements LocaleChangeListener {
	private static final long 	serialVersionUID = -1220218092405433021L;

	public static final String	PROP_READ_ONLY = "readOnly";
	
	private static final long	REFRESH_DELAY = 500;
	
	private static enum ContentViewType {
		AS_TABLE, 
		AS_ICONS;
	}
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final FileSystemInterface		fsi;
	private final Properties				props;
	private final JTree						tree = new JTree();
	private final JList<String>				table = new JList<>(new DefaultListModel<String>());
	private final JPanel					right = new JPanel(new BorderLayout());
	private final JToolBarWithMeta			toolbar;
	private final DefaultMutableTreeNode	root = new DefaultMutableTreeNode();
	
	private ContentViewType	contentView = ContentViewType.AS_ICONS;
	private TimerTask		tt = null; 
	
	public JFileSystemNavigator(final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final Properties props) throws IOException, NullPointerException, EnvironmentException {
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
			this.toolbar = new JToolBarWithMeta(mdi.byUIPath(URI.create("ui:/model/navigation.top.fileSystemNavigator")));
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
					}
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
			});
			tree.setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					final JLabel	label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					final String	path = label.getText();

					label.setText(path.endsWith("/") ? path : path.substring(path.lastIndexOf('/')+1));
					return label;
				}
			});

			root.setUserObject("/");
			fillChildren(root, fsi);
			((DefaultTreeModel)tree.getModel()).setRoot(root);
			
			right.add(toolbar, BorderLayout.NORTH);
			right.add(new JScrollPane(table), BorderLayout.CENTER);
			
			setLeftComponent(new JScrollPane(tree));
			setRightComponent(right);
			fillLocalizedStrings();
		}
	}
	
	@OnAction("action:/loadContent")
	private void loadContent() {
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
						final boolean	dirsInside = flag[0];
						
						@Override
						public boolean isLeaf() {
							return !dirsInside;
						}
					});
				}
			}
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		fillLocalizedStrings();
	}

	private synchronized void refreshRightPanel(final FileSystemInterface current) throws IOException {
		final DefaultListModel<String>	model = (DefaultListModel<String>)table.getModel();
		
		model.clear();
		current.list((i)->{
			model.addElement(i.getPath());
			return ContinueMode.CONTINUE;
		});
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	private static class NavigatorListModel extends DefaultListModel<String> {
		private static final long serialVersionUID = 1L;

		private final List<String>	content = new ArrayList<>();
		
		NavigatorListModel() {
		}
		
		@Override
		public int getSize() {
			return content == null ? 0 : content.size();
		}

		@Override
		public String getElementAt(final int index) {
			return content.get(index);
		}
		
		public List<String> getContent() {
			return content;
		}
	}
	
	
	public static void main(final String[] args) throws IOException, NullPointerException, EnvironmentException {
		try(final FileSystemInterface	fsi = new FileSystemOnFile(URI.create("file:/c:/"))) {
			final JFileSystemNavigator	navi = new JFileSystemNavigator(PureLibSettings.PURELIB_LOCALIZER, PureLibSettings.CURRENT_LOGGER, fsi, new SubstitutableProperties());
			
			navi.setPreferredSize(new Dimension(800,600));
			JOptionPane.showMessageDialog(null, navi);
		}
		
	}
}