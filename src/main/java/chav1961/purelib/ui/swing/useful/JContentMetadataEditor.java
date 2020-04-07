package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.inner.RootDescriptor;
import chav1961.purelib.ui.swing.useful.inner.SingleNodeDescriptor;

public class JContentMetadataEditor extends JPanel implements LocaleChangeListener {
	private static final long 			serialVersionUID = 4413926515693981943L;
	private static final String			FIRST_TAB_CAPTION = ""; 
	private static final String			FIRST_TAB_TOOLTIP = ""; 
	private static final String			SECOND_TAB_CAPTION = ""; 
	private static final String			SECOND_TAB_TOOLTIP = ""; 

	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final SingleNodeDescriptor		snd;
	private final ContentMetadataInterface	sndModel;
	private final RootDescriptor			rd;
	private final ContentMetadataInterface	rdModel;
	private JComponent						root = null;
	private JPanel							rootPanel = null, itemPanel = null;
	private JTree							navigator = null;
	private EditorMode						currentMode = EditorMode.SINGLE_NODE;
	private boolean							readOnly = false;
	private ContentMetadataInterface		mdi = null;
	private ContentNodeMetadata				node = null;

	@FunctionalInterface
	public interface ClassEnumAssociated {
		Class<?> getAssciatedClass();
	}

	public enum EditorMode {
		SINGLE_NODE,
		NODE_AND_SUBTREE,
		FULL_TREE
	}

	public JContentMetadataEditor(final Localizer localizer) throws LocalizationException, ContentException {
		this(localizer,PureLibSettings.CURRENT_LOGGER);
	}
	
	public JContentMetadataEditor(final Localizer localizer, final LoggerFacade logger) throws LocalizationException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.snd = new SingleNodeDescriptor(logger);
			this.sndModel = ContentModelFactory.forAnnotatedClass(SingleNodeDescriptor.class);
			this.rd = new RootDescriptor(logger);
			this.rdModel = ContentModelFactory.forAnnotatedClass(RootDescriptor.class);
			rebuildContent(currentMode);
		}
	}
	
	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(root,oldLocale,newLocale);
	}
	
	public void setReadOnly(final boolean readOnly) {
		// TODO:
		setReadOnlyContent(this.readOnly = readOnly);
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	public void setEditorMode(final EditorMode mode) throws NullPointerException, LocalizationException, ContentException {
		if (mode == null) {
			throw new NullPointerException("Editor mode can't be null");
		}
		else {
			rebuildContent(currentMode = mode);
			fillLocalizedStrings();
		}
	}

	public EditorMode getEditorMode() {
		return currentMode;
	}
	
	public void setValue(final ContentMetadataInterface value) throws NullPointerException, IllegalStateException, ContentException {
		if (value == null) {
			throw new NullPointerException("Value to set can'tt be null");
		}
		else if (getEditorMode() != EditorMode.FULL_TREE) {
			throw new IllegalStateException("This call is not applicable now, because current editor mode is ["+EditorMode.FULL_TREE+"]");
		}
		else {
			this.mdi = value;
			this.node = null;
			fillContent();
		}
	}

	public void setValue(final ContentNodeMetadata value) throws NullPointerException, IllegalStateException, ContentException {
		if (value == null) {
			throw new NullPointerException("Value to set can'tt be null");
		}
		else if (getEditorMode() == EditorMode.FULL_TREE) {
			throw new IllegalStateException("This call is not applicable now, because current editor mode is not ["+EditorMode.FULL_TREE+"]");
		}
		else {
			this.mdi = null;
			this.node = value;
			fillContent();
		}
	}
	
	public ContentMetadataInterface getContentMetadataInterfaceValue() {
		if (getEditorMode() != EditorMode.FULL_TREE) {
			throw new IllegalStateException("This call is not applicable now, because current editor mode is not ["+EditorMode.FULL_TREE+"]");
		}
		else {
			return mdi;
		}
	}
	
	public ContentNodeMetadata getContentNodeMetadataValue() {
		if (getEditorMode() == EditorMode.FULL_TREE) {
			throw new IllegalStateException("This call is not applicable now, because current editor mode is ["+EditorMode.FULL_TREE+"]");
		}
		else {
			return snd.getContent();
		}
	}


	private void rebuildContent(final EditorMode buildMode) throws LocalizationException, ContentException {
		rootPanel = null;
		itemPanel = null;
		navigator = null;
		setLayout(null);
		removeAll();
		
		switch (buildMode) {
			case FULL_TREE			:
				final JTabbedPane	tabs = new JTabbedPane();
				final JSplitPane	firstPane = new JSplitPane();
				final JPanel		firstLeft = new JPanel(), firstRight = buildSingleNode();
				final JTabContent	first = new JTabContent(localizer,FIRST_TAB_CAPTION,FIRST_TAB_TOOLTIP)
									, second = new JTabContent(localizer,SECOND_TAB_CAPTION,SECOND_TAB_TOOLTIP);
				
				setLayout(new BorderLayout());
				rootPanel = second;
				itemPanel = firstRight;
				navigator = buildNavigationTree(firstLeft);
				second.add(buildRoot(),BorderLayout.CENTER);
				firstPane.setLeftComponent(firstLeft);
				firstPane.setRightComponent(firstRight);
				first.add(firstPane,BorderLayout.CENTER);
				placeTab(tabs,first);
				placeTab(tabs,second);
				add(tabs,BorderLayout.CENTER);
				root = tabs;
				break;
			case NODE_AND_SUBTREE	:
				final JSplitPane	pane = new JSplitPane();
				final JPanel		left = new JPanel(), right = buildSingleNode();
				
				setLayout(new BorderLayout());
				itemPanel = right;
				navigator = buildNavigationTree(left);
				pane.setLeftComponent(left);
				pane.setRightComponent(right);
				add(pane,BorderLayout.CENTER);
				root = pane;
				break;
			case SINGLE_NODE		:
				final JPanel		inner = buildSingleNode();
				
				setLayout(new BorderLayout());
				itemPanel = inner;
				add(inner,BorderLayout.CENTER);
				root = inner;
				break;
			default	:
				throw new UnsupportedOperationException("Build mode ["+buildMode+"] is not supported yet");
		}
	}

	private AutoBuiltForm<RootDescriptor> buildRoot() throws LocalizationException, ContentException {
		final AutoBuiltForm<RootDescriptor>	abf = new AutoBuiltForm<>(rdModel,localizer,rd,rd);
	
		abf.setPreferredSize(new Dimension(200,200));
		for (Module item : abf.getUnnamedModules()) {
			rd.getClass().getModule().addExports(rd.getClass().getPackageName(),item);
		}
		return abf;
	}
	
	private JTree buildNavigationTree(final JPanel panel) {
		final JTree			tree = new JTree();
		final JScrollPane	scroll = new JScrollPane(tree);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		panel.setLayout(new BorderLayout());
		panel.add(scroll,BorderLayout.CENTER);
		return tree;
	}

	private AutoBuiltForm<SingleNodeDescriptor> buildSingleNode() throws LocalizationException, ContentException {
		final AutoBuiltForm<SingleNodeDescriptor>	abf = new AutoBuiltForm<>(sndModel,localizer,snd,snd);
	
		abf.setPreferredSize(new Dimension(200,200));
		for (Module item : abf.getUnnamedModules()) {
			snd.getClass().getModule().addExports(snd.getClass().getPackageName(),item);
		}
		return abf;
	}

	
	private void setReadOnlyContent(final boolean readOnly) {
		switch (getEditorMode()) {
			case FULL_TREE			:
				setReadOnlyRoot(readOnly);
			case NODE_AND_SUBTREE	:
				setReadOnlyNavigator(readOnly);
			case SINGLE_NODE		:
				setReadOnlySingle(readOnly);
				break;
			default :
				throw new UnsupportedOperationException("Editor mode ["+getEditorMode()+"] is not supported yet");
		}
	}
	
	private void setReadOnlyRoot(final boolean readOnly) {
		// TODO Auto-generated method stub
		
	}


	private void setReadOnlyNavigator(final boolean readOnly) {
		// TODO Auto-generated method stub
		
	}

	private void setReadOnlySingle(final boolean readOnly) {
		// TODO Auto-generated method stub
		
	}

	private void fillContent() throws ContentException {
		switch (getEditorMode()) {
			case FULL_TREE			:
				if (mdi != null) {
					fillRoot(mdi);
					fillNavigator(mdi.getRoot());
					navigator.setSelectionRow(0);
				}
				else {
					clearRoot();
					clearNavigator();
				}
				break;
			case NODE_AND_SUBTREE	:
				if (node != null) {
					fillNavigator(node);
					navigator.setSelectionRow(0);
				}
				else { 
					clearNavigator();
				}
				break;
			case SINGLE_NODE		:
				if (node != null) {
					fillSingleNode(node);
				}
				else {
					clearSingleNode();
				}
				break;
			default :
				throw new UnsupportedOperationException("Editor mode ["+getEditorMode()+"] is not supported yet");
		}
	}

	private void clearRoot() {
		// TODO Auto-generated method stub
		
	}
	
	private void clearNavigator() {
		// TODO Auto-generated method stub
		
	}

	private void clearSingleNode() {
		// TODO Auto-generated method stub
	}

	private void fillRoot(final ContentMetadataInterface mdi) {
		// TODO Auto-generated method stub
		
	}
	
	private void fillNavigator(final ContentNodeMetadata root) {
		// TODO Auto-generated method stub
		
	}

	private void fillSingleNode(final ContentNodeMetadata node) throws ContentException {
		// TODO Auto-generated method stub
		snd.fillContent(node);
		SwingUtils.putToScreen(sndModel.getRoot(),snd,itemPanel);
	}

	private void placeTab(final JTabbedPane pane, final JTabContent tab) {
		final JCloseableTab	label = tab.getTab();
		
		label.setCloseEnable(false);
		pane.addTab("",tab);
		pane.setTabComponentAt(pane.getTabCount()-1,label);
	}

	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
	}

	
	public static boolean show(final JFrame parent, JContentMetadataEditor editor) {
		// TODO:
		return false;
	}

	public static boolean show(final JDialog parent, JContentMetadataEditor editor) {
		// TODO:
		return false;
	}

	private static class JTabContent extends JPanel implements LocaleChangeListener {
		private static final long serialVersionUID = -6102018791089268759L;

		private final Localizer		localizer;
		private final JCloseableTab	tab;
		
		public JTabContent(final Localizer localizer, final String captionId, final String tooltipId) throws LocalizationException {
			if (localizer == null) {
				throw new NullPointerException("Localizer can't be null");
			}
			else if (captionId == null || captionId.isEmpty()) {
				throw new IllegalArgumentException("Caption ID can't be null or empty");
			}
			else if (tooltipId == null || tooltipId.isEmpty()) {
				throw new IllegalArgumentException("Tooltip ID can't be null or empty");
			}
			else if (!localizer.containsKey(captionId)) {
				throw new IllegalArgumentException("Caption ID ["+captionId+"] is not defined in the localizer ["+localizer.getLocalizerId()+"]");
			}
			else if (!localizer.containsKey(tooltipId)) {
				throw new IllegalArgumentException("Tooltip ID ["+tooltipId+"] is not defined in the localizer ["+localizer.getLocalizerId()+"]");
			}
			else {
				this.localizer = localizer;
				this.tab = new JCloseableTab(localizer,captionId);
				this.tab.setToolTipText(tooltipId);
				setLayout(new BorderLayout());
				fillLocalizedStrings();
			}
		}

		public JCloseableTab getTab() {
			return null;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			// TODO Auto-generated method stub
			fillLocalizedStrings();
		}
		
		private void fillLocalizedStrings() throws LocalizationException {
			this.tab.localeChanged(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
	}
}
