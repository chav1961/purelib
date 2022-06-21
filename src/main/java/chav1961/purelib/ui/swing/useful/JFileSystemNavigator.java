package chav1961.purelib.ui.swing.useful;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
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

	private static final String	URI_READONLY_NAVI = "ui:/model/navigation.top.fileSystemNavigator";
	private static final String	URI_WRITABLE_NAVI = "ui:/model/navigation.top.writableFileSystemNavigator";
	
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
	private final JFileTree					tree;
	private final JFileList					list;
	private final JPanel					right = new JPanel(new BorderLayout());
	private final JToolBarWithMeta			toolbar;
	
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
			this.tree = new JFileTree(logger, fsi, false) {
							private static final long serialVersionUID = 1L;

							@Override
							public void refreshLinkedContent(final FileSystemInterface content) {
								try{
									refreshRightPanel(content);
								} catch (IOException e) {
									logger.message(Severity.error, "I/O error refreshing right panel: "+e.getLocalizedMessage(),e);
								}
							}

							@Override
							public void placeFileContent(final Iterable<File> content) {
								logger.message(Severity.error, "Not implemented yet");
							}
						};
			this.list = new JFileList(localizer, logger, fsi, false, false);
			this.readOnly = checkParameter(props, PROP_ACCESS, PROP_ACCESS_READ_ONLY, PROP_ACCESS_READ_ONLY, PROP_ACCESS_READ_WRITE) == 0;
			this.selectionType = SelectionType.values()[checkParameter(props, PROP_SELECTION_TYPE, SelectionType.NONE.getValue(), SelectionType.NONE.getValue(),SelectionType.SINGLE.getValue(), SelectionType.MULTIPLE.getValue())];
			this.selectedObjects = SelectedObjects.values()[checkParameter(props, PROP_SELECTED_OBJECTS, SelectedObjects.NONE.getValue(), SelectedObjects.NONE.getValue(), SelectedObjects.FILES.getValue(), SelectedObjects.DIRECTORIES.getValue(), SelectedObjects.ALL.getValue())];
			
			this.toolbar = new JToolBarWithMeta(mdi.byUIPath(URI.create(this.readOnly ? URI_READONLY_NAVI : URI_WRITABLE_NAVI)));
			this.toolbar.setFloatable(false);
			SwingUtils.assignActionListeners(this.toolbar, this);
			
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
							clickInRightPanel(list.getModel().getElementAt(index));
						}
					}
				}
			});
			list.setFocusable(true);
			
			right.add(toolbar, BorderLayout.NORTH);
			right.add(list, BorderLayout.CENTER);
			
			setLeftComponent(new JScrollPane(tree));
			setRightComponent(right);
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(toolbar, oldLocale, newLocale);
		SwingUtils.refreshLocale(tree, oldLocale, newLocale);
		SwingUtils.refreshLocale(list, oldLocale, newLocale);
	}

	public String getSelectedObject() {
		if (selectionType == SelectionType.SINGLE) {
			return null;
		}
		else {
			throw new IllegalStateException("Calling this method is available for ["+PROP_SELECTION_TYPE+"] = ["+PROP_SINGLE_SELECTION+"] only"); 
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
	private void asIcons() throws IOException {
		list.setContentViewType(JFileList.ContentViewType.AS_ICONS);
		refreshRightPanel();
	}

	@OnAction("action:/asLargeIcons")
	private void asLagreIcons() throws IOException {
		list.setContentViewType(JFileList.ContentViewType.AS_LARGE_ICONS);
		refreshRightPanel();
	}
	
	@OnAction("action:/asTable")
	private void asTable() throws IOException {
		list.setContentViewType(JFileList.ContentViewType.AS_TABLE);
		refreshRightPanel();
	}
	
	private void refreshRightPanel() {
		final TreePath	selected = tree.getSelectionPath();
		
		if (selected != null) {
			final String	path = ((JFileItemDescriptor)((DefaultMutableTreeNode)selected.getLastPathComponent()).getUserObject()).getPath(); 
					
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
		list.refreshContent(current.getPath());
	}

	private void clickInRightPanel(final JFileItemDescriptor path) {
		tree.expandPath(tree.getSelectionPath());
		SwingUtilities.invokeLater(()->tree.setSelection(path.getPath()));
	}

	private void fillLocalizedStrings() {
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
	
	public static void main(final String[] args) throws IOException, NullPointerException, EnvironmentException, ContentException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.setProperty(PROP_ACCESS, PROP_ACCESS_READ_WRITE);
		try(final FileSystemInterface	fsi = new FileSystemOnFile(URI.create("file:/c:/"))) {
			final JFileSystemNavigator	navi = new JFileSystemNavigator(PureLibSettings.PURELIB_LOCALIZER, PureLibSettings.CURRENT_LOGGER, fsi, props);
			
			navi.setPreferredSize(new Dimension(800,600));
			JOptionPane.showMessageDialog(null, navi);
		}
		
	}
}