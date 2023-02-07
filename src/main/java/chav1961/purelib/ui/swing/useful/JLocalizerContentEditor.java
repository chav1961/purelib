package chav1961.purelib.ui.swing.useful;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.AbstractLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleDescriptor;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;
import chav1961.purelib.ui.swing.JToolBarWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;

public class JLocalizerContentEditor extends JSplitPane implements LocaleChangeListener {
	private static final long 		serialVersionUID = 3237259445965828668L;
	private static final String		KEY_SELECT_FIELDS_TITLE = "JLocalizerContentEditor.selectFieldsTitle";
	private static final String		KEY_SELECT_FIELDS_FIELD = "JLocalizerContentEditor.selectFields.field";
	private static final String		KEY_SELECT_FILTER_STATIC = "JLocalizerContentEditor.selectFields.static";
	private static final String		KEY_SELECT_FILTER_NON_PUBLIC = "JLocalizerContentEditor.selectFields.nonPublic";
	private static final String		KEY_SELECT_FILTER_STATIC_TT = "JLocalizerContentEditor.selectFields.static.tt";
	private static final String		KEY_SELECT_FILTER_NON_PUBLIC_TT = "JLocalizerContentEditor.selectFields.nonPublic.tt";
	private static final String		KEY_SELECT_FIELDS_HELP = "JLocalizerContentEditor.selectFields.help";
	private static final Icon		GREEN_ICON = PureLibStandardIcons.SUCCESS.getIcon();
	private static final Icon		RED_ICON = PureLibStandardIcons.FAIL.getIcon();
	
	public static enum ContentType {
		XML, JSON
	}

	@FunctionalInterface
	public interface StoreContentInterface {
		void process(final Localizer localizer, final ContentType type) throws LocalizationException, IOException;
	}
	
	private static enum FocusedComponent {
		TREE, TABLE, OTHER
	}
	
	private final Localizer				localizer;
	private final LoggerFacade			logger;
	private final StoreContentInterface	sci;
	private final SupportedLanguages[]	languages;
	private final SyntaxTreeInterface<LocalizerRecord>	content = new AndOrTree<>();
	private final JToolBarWithMeta		tbm;
	private final JCreoleEditor			editor;
	private final InnerTableModel		model;
	private final JFreezableTable		table;
	private Node						root = null;
	private TimerTask					tt = null;
	private FocusedComponent			focused;
	
	public JLocalizerContentEditor(final Localizer localizer, final LoggerFacade logger, final StoreContentInterface store) throws EnvironmentException, ContentException {
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
			this.tbm = new JToolBarWithMeta(mdi.byUIPath(URI.create("ui:/model/navigation.top.localizerContentEditor")));
			this.model = new InnerTableModel(content, this.languages);
			this.table = new JFreezableTable(model, "key");
			this.editor = new JCreoleEditor();
			
			SwingUtils.assignActionListeners(this.tbm, this);
			SwingUtils.assignActionKeys(this, JSplitPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, (e)->processAction(e.getSource(),e.getActionCommand()), SwingUtils.EditorKeys.values());
			
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

			
			final JPanel	rightPanel = new JPanel(new BorderLayout());
			
			tbm.setFloatable(false);
			rightPanel.add(tbm, BorderLayout.NORTH);
			rightPanel.add(new JScrollPane(editor), BorderLayout.CENTER);
			
			setLeftComponent(new JScrollPane(table));
			setRightComponent(rightPanel);
			fillLocalizedStrings();
		}
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
			final long	id = content.seekName(item);
			
			if (id >= 0) {
				if (replaceExistent) {
					final LocalizerRecord	rec = content.getCargo(id);
					
					for (int index = 0; index < langs.length; index++) {
						rec.values[index] = load.getLocalValue(item, temp.get(index).getLocale());
					}
				}
			}
			else {
				final long				newId = content.placeName(item, null); 
				final LocalizerRecord	newRec = new LocalizerRecord(newId, langs, new String[langs.length]); 
				
				for (int index = 0; index < langs.length; index++) {
					newRec.values[index] = load.getLocalValue(item, temp.get(index).getLocale());
				}
				content.setCargo(newId, newRec);
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
	}

	@OnAction("action:/storeContent")
	private void storeContent() {
	}

	@OnAction("action:/lodaClassContent")
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

	private void insertKey(final String key) {
		final long	id = content.seekName(key);
		
		if (id < 0) {
			final long				newId = content.placeName(key, null);
			final String[]			values = new String[languages.length];
			final LocalizerRecord	rec = new LocalizerRecord(newId, languages, values);
			
			Arrays.fill(values, key);
			content.setCargo(newId, rec);
		}
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
	}
	
	private void refreshState() {
		// TODO Auto-generated method stub
		
	}
	
	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	private static class LocalizerRecord {
		final long					key;
		final SupportedLanguages[]	langs;
		final String[]				values;

		LocalizerRecord(final long key, final SupportedLanguages[] langs, final String[] values) {
			this.key = key;
			this.langs = langs;
			this.values = values;
		}

		@Override
		public String toString() {
			return "LocalizerRecord [key=" + key + ", langs=" + Arrays.toString(langs) + ", values=" + Arrays.toString(values) + "]";
		}
	}
	
	private static class OutputLocalizer extends AbstractLocalizer {
		private final SyntaxTreeInterface<LocalizerRecord>	tree;
		
		protected OutputLocalizer(final SyntaxTreeInterface<LocalizerRecord> tree) throws LocalizationException, NullPointerException {
			super();
			this.tree = tree;
		}

		@Override
		public URI getLocalizerId() {
			return URI.create(LOCALIZER_SCHEME+":/internal");
		}

		@Override
		public boolean canServe(final URI resource) throws NullPointerException {
			return false;
		}

		@Override
		public Localizer newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
			return this;
		}

		@Override
		public Iterable<String> localKeys() {
			final List<String>	keys = new ArrayList<>();
			
			tree.walk((name, len, id, cargo)->{
				keys.add(new String(name, 0 , len));
				return true;
			});
			return keys;
		}

		@Override
		public String getLocalValue(final String key) throws LocalizationException, IllegalArgumentException {
			return getLocalValue(key, currentLocale().getLocale());
		}

		@Override
		public String getLocalValue(final String key, final Locale locale) throws LocalizationException, IllegalArgumentException {
			final long	id = tree.seekName(key);
			
			if (id >= 0) {
				final LocalizerRecord	rec = tree.getCargo(id);
				final String			lang = locale.getLanguage();
				
				for (int index = 0; index < rec.langs.length;  index++) {
					if (lang.equals(rec.langs[index].getLocale().getLanguage())) {
						return rec.values[index];
					}
				}
				throw new LocalizationException("Language ["+lang+"] not found"); 
			}
			else {
				throw new LocalizationException("Key ["+key+"] not found"); 
			}
		}

		@Override
		protected void loadResource(Locale newLocale) throws LocalizationException, NullPointerException {
		}

		@Override
		protected String getHelp(final String helpId, final Locale locale, final String encoding) throws LocalizationException, IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected boolean isLocaleSupported(String key, Locale locale)
				throws LocalizationException, IllegalArgumentException {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	private class InnerTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 6525386077011559488L;

		private final SyntaxTreeInterface<LocalizerRecord>	tree;
		private final SupportedLanguages[]					langs;
		
		InnerTableModel(final SyntaxTreeInterface<LocalizerRecord> tree, final SupportedLanguages[] langs) {
			this.tree = tree;
			this.langs = langs;
		}

		@Override
		public int getRowCount() {
			return (int) (tree == null ? 0 : tree.size());
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
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			final int[]	place = new int[] {rowIndex};
			final LocalizerRecord[]	result = new LocalizerRecord[1];
			
			content.walk((name, len, id, cargo) -> {
					if (--place[0] > 0) {
						return true;
					}
					else {
						result[0] = cargo;
						return false;
					}
				}
			);
			switch (columnIndex) {
				case 0 : return content.getName(result[0].key);
				default : return result[0].values[columnIndex-1];
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
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
