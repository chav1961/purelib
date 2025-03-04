package chav1961.purelib.ui.swing.useful;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.AbstractLocalizer;
import chav1961.purelib.i18n.MutableJsonLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleDescriptor;
import chav1961.purelib.i18n.interfaces.MutableLocalizedString;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.swing.JToolBarWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.SwingUtils.EditorKeys;
import chav1961.purelib.ui.swing.interfaces.OnAction;

/**
 * <p>This class implements simple localization strings editor. It contains two panels (see {@linkplain JSplitPane}):
 * <ul>
 * <li>left panel contains table with localization key (the same first column) and it's representation in supported languages</li>
 * <li>right panel contains {@linkplain JCreoleEditor Creole editor} for referenced values</li>
 * </ul>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 * @last.update 0.0.8
 */
public class JLocalizerContentEditor extends JSplitPane implements LocaleChangeListener, LoggerFacadeOwner {
	private static final long 		serialVersionUID = 3237259445965828668L;
	private static final String		KEY_SELECT_FIELDS_TITLE = "JLocalizerContentEditor.selectFieldsTitle";
	private static final String		KEY_SELECT_FIELDS_FIELD = "JLocalizerContentEditor.selectFields.field";
	private static final String		KEY_SELECT_FILTER_STATIC = "JLocalizerContentEditor.selectFields.static";
	private static final String		KEY_SELECT_FILTER_NON_PUBLIC = "JLocalizerContentEditor.selectFields.nonPublic";
	private static final String		KEY_SELECT_FILTER_STATIC_TT = "JLocalizerContentEditor.selectFields.static.tt";
	private static final String		KEY_SELECT_FILTER_NON_PUBLIC_TT = "JLocalizerContentEditor.selectFields.nonPublic.tt";
	private static final String		KEY_SELECT_FIELDS_HELP = "JLocalizerContentEditor.selectFields.help";

	private static final String		MSG_DUPLICATE_KEY = "JLocalizerContentEditor.message.duplicate.key";
	private static final String		MSG_MISSING_KEY = "JLocalizerContentEditor.message.missing.key";
	
	private static final Icon		GREEN_ICON = PureLibStandardIcons.SUCCESS.getIcon();
	private static final Icon		RED_ICON = PureLibStandardIcons.FAIL.getIcon();
	
	/**
	 * <p>This enumeration describes content type for localized content sditor</p>
	 * @since 0.0.5
	 * @last.update 0.0.7
	 */
	public static enum ContentType {
		XML, JSON
	}

	/**
	 * <p>This interface describes any class to store changed localizer content</p>
	 * @since 0.0.5
	 * @last.update 0.0.7
	 */
	@FunctionalInterface
	public interface StoreContentInterface {
		/**
		 * <p>Upload localizer content.</p>
		 * @param localizer localizer to upload content from. Can't be null.
		 * @param type source content type. Can't be null.
		 * @throws LocalizationException on any localization errors
		 * @throws IOException on any I/O errors
		 */
		void process(final Localizer localizer, final ContentType type) throws LocalizationException, IOException;
	}
	
	private static enum FocusedComponent {
		TREE, TABLE, OTHER
	}
	
	private final Localizer				localizer;
	private final MutableJsonLocalizer	tempLocalizer = new MutableJsonLocalizer();
	private final LoggerFacade			logger;
	private final StoreContentInterface	sci;
	private final SupportedLanguages[]	languages;
	private final List<LocalizerRecord>	content = new ArrayList<>();
	private final Set<String>			contentKeys = new HashSet<>();
	private final JToolBarWithMeta		tbmLeft, tbmRight;
	private final JCreoleEditor			editor;
	private final InnerTableModel		model;
	private final JFreezableTable		table;
	private Node						root = null;
	private TimerTask					tt = null;
	private FocusedComponent			focused;

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to manipulate content. Can't be null.
	 * @param logger logger to print any messages at processing. Can't be null.
	 * @param store store interface to save changer logger content. Can't be null.
	 * @throws EnvironmentException on any internal errors
	 * @throws ContentException on any localizer content errors 
	 */
	public JLocalizerContentEditor(final Localizer localizer, final LoggerFacade logger, final StoreContentInterface store) throws EnvironmentException, ContentException {
		this(localizer, logger, store, false, true);
	}	
	
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to manipulate content. Can't be null.
	 * @param logger logger to print any messages at processing. Can't be null.
	 * @param store store interface to save changer logger content. Can't be null.
	 * @param showLeftToolBar show table tool bar (insert, duplicate and so on)
	 * @param showRightToolBar show Creole editor tool bar (format, insert and so on)
	 * @throws EnvironmentException on any internal errors
	 * @throws ContentException on any localizer content errors 
	 * @since 0.0.7
	 */
	public JLocalizerContentEditor(final Localizer localizer, final LoggerFacade logger, final StoreContentInterface store, final boolean showLeftToolBar, final boolean showRightToolBar) throws EnvironmentException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (store == null) {
			throw new NullPointerException("Store content interfacle can't be null"); 
		}
		else {
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("useful.xml"));
			final List<SupportedLanguages>	langsList = new ArrayList<>(); 
			
			AbstractLocalizer.enumerateLocales((lang, langName, icon) ->{
					langsList.add(lang);
				}
			);
			
			this.localizer = localizer;
			this.logger = logger;
			this.sci = store;
			this.languages = langsList.toArray(new SupportedLanguages[langsList.size()]);
			this.tbmLeft = new JToolBarWithMeta(mdi.byUIPath(URI.create("ui:/model/navigation.top.localizerContentEditor.left")));
			this.tbmRight = new JToolBarWithMeta(mdi.byUIPath(URI.create("ui:/model/navigation.top.localizerContentEditor.right")));
			this.model = new InnerTableModel(content, this.languages);
			this.table = new JFreezableTable(model, "key");
			this.editor = new JCreoleEditor();
			
			SwingUtils.assignActionListeners(this.tbmLeft, this);
			SwingUtils.assignActionKeys(this, JSplitPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, (e)->processAction(e.getSource(),e.getActionCommand()), SwingUtils.EditorKeys.values());
			SwingUtils.assignActionKeys(table, JTable.WHEN_FOCUSED, (e)->processAction(e.getSource(),e.getActionCommand()), SwingUtils.EditorKeys.EK_DELETE, SwingUtils.EditorKeys.EK_DUPLICATE, SwingUtils.EditorKeys.EK_INSERT);
			SwingUtils.assignActionKeys(table.getLeftBar(), JTable.WHEN_FOCUSED, (e)->processAction(e.getSource(),e.getActionCommand()), SwingUtils.EditorKeys.EK_DELETE, SwingUtils.EditorKeys.EK_DUPLICATE, SwingUtils.EditorKeys.EK_INSERT);
			
			table.addFocusListener(new FocusListener() {
				@Override public void focusLost(FocusEvent e) {focused = FocusedComponent.OTHER;}
				@Override public void focusGained(FocusEvent e) {focused = FocusedComponent.TABLE;}
			});
			table.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			        final String				name = table.getModel().getColumnName(column);
		        	final SupportedLanguages	lang = SupportedLanguages.valueOf(name);
			        final JLabel				label = new JLabel(lang.name(), lang.getIcon(), JLabel.LEFT);
			        
			        label.setBorder(BorderFactory.createLineBorder(table.getGridColor()));
			        return label;
				}
			});
			table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(final ListSelectionEvent e) {
					refreshState();
				}
			});
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			
			final JPanel	leftPanel = new JPanel(new BorderLayout());
			final JPanel	rightPanel = new JPanel(new BorderLayout());

			if (showLeftToolBar) {
				tbmLeft.setFloatable(false);
				leftPanel.add(tbmLeft, BorderLayout.NORTH);
			}
			leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);
			
			if (showRightToolBar) {
				tbmRight.setFloatable(false);
				rightPanel.add(tbmRight, BorderLayout.NORTH);
			}
			rightPanel.add(new JScrollPane(editor), BorderLayout.CENTER);
			
			setLeftComponent(leftPanel);
			setRightComponent(rightPanel);
			fillLocalizedStrings();
		}
	}
	
	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	protected void loadContent(final Localizer load, final boolean replaceExistent) throws IOException, LocalizationException, IllegalArgumentException {
		final List<LocaleDescriptor>	temp = new ArrayList<>();
		
		for (LocaleDescriptor item : localizer.supportedLocales()) {
			temp.add(item);
		}
		final SupportedLanguages[]		langs = new SupportedLanguages[temp.size()];
		
		for (int index = 0; index < langs.length; index++) {
			langs[index] = SupportedLanguages.valueOf(temp.get(index).getLanguage());
		}
		
		for (String item : load.localKeys()) {
			if (contentKeys.contains(item)) {
				if (replaceExistent) {
					final LocalizerRecord rec = content.get(keyIndex(item));
					
					for (int index = 0; index < langs.length; index++) {
						rec.values[index] = load.getLocalValue(item, temp.get(index).getLocale());
					}
				}
			}
			else {
				final LocalizerRecord	newRec = new LocalizerRecord(item, langs, new String[langs.length]); 
				
				for (int index = 0; index < langs.length; index++) {
					newRec.values[index] = load.getLocalValue(item, temp.get(index).getLocale());
				}
				content.add(newRec);
				contentKeys.add(item);
			}
		}
	}
	
	protected void storeContent(final Localizer content, final ContentType type) throws IOException, LocalizationException {
		sci.process(content, type);
	}
	
	protected void loadClassContent(final InputStream is) throws IOException, LocalizationException, IllegalArgumentException, ContentException {
		try(final SimpleURLClassLoader		sucl = new SimpleURLClassLoader(new URL[0]);
			final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is, baos);
			loadClassContent(sucl.createClass(baos.toByteArray()));
		}
	}

	protected void loadClassContent(final Class<?> cl) throws IOException, LocalizationException, IllegalArgumentException, ContentException {
		final List<Field>	list = new ArrayList<>(), selected = new ArrayList<>(); 
		
		CompilerUtils.walkFields(cl, (clazz, field) ->{
			list.add(field);
		});
		
		if (selectContent2Load(list, selected)) {
			for (Field item : selected) {
				insertKey(field2Label(item));
				insertKey(field2Tooltip(item));
			}
			model.fireTableDataChanged();
		}
	}
	
	private boolean selectContent2Load(final List<Field> list, final List<Field> selected) throws LocalizationException {
		final JSelectTableModel	model = new JSelectTableModel(localizer, list);
		final JTable			table = new JTable(model);
		final JCheckBox			includeStatic = new JCheckBox(localizer.getValue(KEY_SELECT_FILTER_STATIC)); 
		final JCheckBox			includeNonPublic = new JCheckBox(localizer.getValue(KEY_SELECT_FILTER_NON_PUBLIC)); 
		final JPanel			panel = new JPanel(new BorderLayout());
		final JPanel			bottomPanel = new JPanel(new GridLayout(2,1));

		table.setDefaultRenderer(Field.class, new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				final Field		f = (Field)value;
				final String	name = f.getName();
				final JLabel	label = new JLabel(name, f.isAnnotationPresent(LocaleResource.class) ? GREEN_ICON : RED_ICON, JLabel.LEFT);
		
				label.setOpaque(true);
				if (isSelected) {
					if (table.isCellEditable(row, 0)) {
						label.setForeground(table.getSelectionForeground());
					}
					else {
						label.setForeground(Color.LIGHT_GRAY);
					}
					label.setBackground(table.getSelectionBackground());
				}
				else {
					if (table.isCellEditable(row, 0)) {
						label.setForeground(table.getForeground());
					}
					else {
						label.setForeground(Color.LIGHT_GRAY);
					}
					label.setBackground(table.getBackground());
				}
				if (hasFocus) {
					label.setBorder(new LineBorder(table.getGridColor()));
				}
				return label;
			}
		});
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.getColumnModel().getColumn(0).setMaxWidth(table.getRowHeight());
		
		bottomPanel.add(includeStatic);
		includeStatic.setToolTipText(localizer.getValue(KEY_SELECT_FILTER_STATIC_TT));
		includeStatic.setSelected(true);
		includeStatic.addActionListener((e)->model.processStatic(includeStatic.isSelected()));
		bottomPanel.add(includeNonPublic);
		includeNonPublic.setToolTipText(localizer.getValue(KEY_SELECT_FILTER_NON_PUBLIC_TT));
		includeNonPublic.setSelected(true);
		includeNonPublic.addActionListener((e)->model.processNonPublic(includeNonPublic.isSelected()));
		
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		panel.setPreferredSize(new Dimension(250,400));

		final JLocalizedOptionPane	op = new JLocalizedOptionPane(localizer);
		
		SwingUtils.assignActionKey(panel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_HELP, (e) -> {
			try {
				SwingUtils.showCreoleHelpWindow(panel, localizer, KEY_SELECT_FIELDS_HELP);
			} catch (IOException exc) {
				logger.message(Severity.error, "I/O error on help: "+exc.getLocalizedMessage(), exc);
			}
		},SwingUtils.ACTION_HELP);
		
		if (op.confirm(this, panel, KEY_SELECT_FIELDS_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			model.fillSelected(selected);
			return true;
		}
		else {
			return false;
		}
	}

	@OnAction("action:/loadContent")
	private void loadContent() {
		try {
			loadContent(tempLocalizer, true);
			
		} catch (LocalizationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnAction("action:/loadClassContent")
	private void loadClassContent() {
	}

	@OnAction("action:/storeContent")
	private void storeContent() {
		final InnerTableModel	model = (InnerTableModel) table.getSourceModel();
	
		tempLocalizer.clear();
		for(int index = 0, maxIndex = model.getRowCount(); index < maxIndex; index++) {
			final MutableLocalizedString	mls = tempLocalizer.createLocalValue(model.getValueAt(index, 0).toString());
			
			for(int col = 1, maxCol = model.getColumnCount(); col < maxCol; col++) {
			}
		}
		try {
			storeContent(localizer, ContentType.JSON);
		} catch (LocalizationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnAction("action:/insertKey")
	private void insertKey() {
		insertKey("newKey");
	}

	@OnAction("action:/duplicateKey")
	private void duplicateKey() {
		if (table.getSelectionModel().getSelectedItemsCount() > 0) {
			final TableModel	model = table.getSourceModel();
			final int			selectedRow = table.getSelectionModel().getMinSelectionIndex();
			final String 		oldKey = model.getValueAt(selectedRow, 0).toString();
			final String		newKey = createUniqueKey(oldKey);
			final String[]		content = new String[SupportedLanguages.values().length];
			
			for(int index = 1; index < model.getColumnCount(); index++) {
				content[index - 1] = model.getValueAt(selectedRow, index).toString();
			}
			insertKey(newKey, content);
		}
	}

	@OnAction("action:/removeKey")
	private void removeKey() {
		if (table.getSelectionModel().getSelectedItemsCount() > 0) {
			final TableModel	model = table.getSourceModel();
			final int			selectedRow = table.getSelectionModel().getMinSelectionIndex();
			final String 		oldKey = model.getValueAt(selectedRow, 0).toString();
			final int			oldRec = keyIndex(oldKey);
			
			if (oldRec >= 0) {
				content.remove(oldRec);
				contentKeys.remove(oldKey);
				((InnerTableModel)table.getSourceModel()).refreshContent(oldKey);
			}
			else {
				getLogger().message(Severity.warning, MSG_MISSING_KEY, oldKey);
			}
		}
	}

	private String createUniqueKey(final String key) {
		if (!Character.isDigit(key.charAt(key.length()-1))) {
			return key + "1";
		}
		else {
			for(int index = key.length()-1; index >= 0; index--) {
				if (!Character.isDigit(key.charAt(index))) {
					return key.substring(0, index) + (Integer.valueOf(key.substring(index)).intValue() + 1);
				}
			}
			return "key"+(int)(10000*Math.random());
		}
	}
	
	private void insertKey(final String key, final String... initialValues) {
		final int	id = keyIndex(key);
		
		if (id < 0) {
			final String[]			values = new String[languages.length];
			final LocalizerRecord	rec = new LocalizerRecord(key, languages, values);
			
			for(int index = 0; index < values.length; index++) {
				values[index] = index < initialValues.length ? initialValues[index] : key;
			}
			content.add(rec);
			((InnerTableModel)table.getSourceModel()).refreshContent(key);
		}
		else {
			getLogger().message(Severity.warning, MSG_DUPLICATE_KEY, key);
		}
	}

	private int keyIndex(final String key) {
		for(int index = 0, maxIndex = content.size(); index < maxIndex; index++) {
			if (content.get(index).key.equals(key)) {
				return index;
			}
		}
		return -1;
	}
	
	private static String field2Label(final Field item) {
		if (item.isAnnotationPresent(LocaleResource.class)) {
			return item.getAnnotation(LocaleResource.class).value();
		}
		else {
			return CompilerUtils.buildFieldPath(item);
		}
	}
	
	private static String field2Tooltip(final Field item) {
		if (item.isAnnotationPresent(LocaleResource.class)) {
			return item.getAnnotation(LocaleResource.class).tooltip();
		}
		else {
			return CompilerUtils.buildFieldPath(item)+".tt";
		}
	}
	
	private void processAction(final Object source, final String action) {
		switch (EditorKeys.byAction(action)) {
			case EK_DELETE		:
				removeKey();
				break;
			case EK_DUPLICATE	:
				duplicateKey();
				break;
			case EK_INSERT		:
				insertKey();
				break;
			default:
				break;
		}
	}
	
	private void refreshState() {
		// TODO Auto-generated method stub
		
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	private static class LocalizerRecord {
		String						key;
		final SupportedLanguages[]	langs;
		final String[]				values;

		LocalizerRecord(final String key, final SupportedLanguages[] langs, final String[] values) {
			this.key = key;
			this.langs = langs;
			this.values = values;
		}

		@Override
		public String toString() {
			return "LocalizerRecord [key=" + key + ", langs=" + Arrays.toString(langs) + ", values=" + Arrays.toString(values) + "]";
		}
	}
	
	
	private class InnerTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 6525386077011559488L;

		private final List<LocalizerRecord>	list;
		private final SupportedLanguages[]	langs;
		
		InnerTableModel(final List<LocalizerRecord> list, final SupportedLanguages[] langs) {
			this.list = list;
			this.langs = langs;
		}

		@Override
		public int getRowCount() {
			return (int) (list == null ? 0 : list.size());
		}

		@Override
		public int getColumnCount() {
			return langs.length + 1;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			if (columnIndex == 0) {
				return "key";
			}
			else {
				return langs[columnIndex-1].name();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return true;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			final LocalizerRecord	value = list.get(rowIndex);
			
			switch (columnIndex) {
				case 0 : return value.key;
				default : return value.values[columnIndex-1];
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			final LocalizerRecord	value = list.get(rowIndex);

			switch (columnIndex) {
				case 0 : 
					contentKeys.remove(value.key);
					value.key = aValue.toString();
					contentKeys.add(value.key);
					break;
				default : 
					value.values[columnIndex-1] = aValue.toString(); 
					break;
			}
			refreshContent(value.key);
		}
		
		public void refreshContent(final String prefix) {
			fireTableStructureChanged();
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

	private  static class JSelectTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		private final Localizer		localizer;
		private final List<Field>	fields;
		private final boolean[]		selection;
		private boolean				allowStatic = true;
		private boolean				allowNonPublic = true;
		
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
		
		public void processStatic(final boolean select) {
			allowStatic = select;
			for (int index = 0; index < selection.length; index++) {
				if (Modifier.isStatic(fields.get(index).getModifiers())) {
					selection[index] = select;
				}
			}
			fireTableDataChanged();
		}

		public void processNonPublic(final boolean select) {
			allowNonPublic = select;
			for (int index = 0; index < selection.length; index++) {
				if (!Modifier.isPublic(fields.get(index).getModifiers())) {
					selection[index] = select;
				}
			}
			fireTableDataChanged();
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
				case 0	: 
					return "*"; 
				case 1  : 
					try{
						return localizer.getValue(KEY_SELECT_FIELDS_FIELD);
					} catch (LocalizationException | IllegalArgumentException e) {
						return KEY_SELECT_FIELDS_FIELD;
					}
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0	: return Boolean.class; 
				case 1  : return Field.class;
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0	: 
					return (Modifier.isStatic(fields.get(rowIndex).getModifiers()) && allowStatic || !Modifier.isStatic(fields.get(rowIndex).getModifiers())) 
							&& (!Modifier.isPublic(fields.get(rowIndex).getModifiers()) && allowNonPublic || Modifier.isPublic(fields.get(rowIndex).getModifiers()));
				case 1  : 
					return false;
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0	: return selection[rowIndex]; 
				case 1  : return fields.get(rowIndex);
				default : throw new UnsupportedOperationException();
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0	: selection[rowIndex] = (Boolean)aValue; break; 
				default : throw new UnsupportedOperationException();
			}
		}
	}
	
	public static void main(final String[] args) throws IOException, NullPointerException, EnvironmentException, ContentException {
		try(final FileSystemInterface		fsi = new FileSystemOnFile(URI.create("file:/c:/"))) {
			final JLocalizerContentEditor	lce = new JLocalizerContentEditor(PureLibSettings.PURELIB_LOCALIZER, PureLibSettings.CURRENT_LOGGER, (l,t)->{});
			
			lce.setPreferredSize(new Dimension(800,600));
			lce.loadContent(PureLibSettings.PURELIB_LOCALIZER, false);
			lce.loadClassContent(lce.getClass());
			JOptionPane.showMessageDialog(null, lce);
		}
		
	}	
}
