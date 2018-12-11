package chav1961.purelib.ui.swing;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleSpecificTextGetter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;
import chav1961.purelib.ui.FormFieldFormat;
import chav1961.purelib.ui.interfacers.ControllerAction;

class MicroTableEditorTwoPanelList<T> extends JPanel implements ActionListener, LocaleChangeListener {
	private static final long 		serialVersionUID = -8610685011692281087L;
	private static final int		TOOLBAR_WIDTH = 32; 
	private static final KeyStroke	KS_SELECT = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,InputEvent.CTRL_DOWN_MASK);
	private static final KeyStroke	KS_SELECT_ALL = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK);
	private static final KeyStroke	KS_UNSELECT = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.CTRL_DOWN_MASK);
	private static final KeyStroke	KS_UNSELECT_ALL = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK);
	
	private final Localizer			localizer;
	private final Class<T>			clazz;
	private final TableModel		leftModel, rightModel; 
	private final LocalizedTable	leftTable, rightTable;
	private final JScrollPane		leftScroll, rightScroll;
	private final MicroTableEditorTwoPanelToolBar	toolBar;
	
	MicroTableEditorTwoPanelList(final Localizer localizer, final Class<T> clazz, final TableModel leftModel, final TableModel rightModel) throws LocalizationException, IllegalArgumentException, NullPointerException, SyntaxException {
		this.localizer = localizer;
		this.clazz = clazz;
		this.leftModel = leftModel;
		this.rightModel = rightModel;
		
		final FieldDescriptor[]		fields = buildFields(clazz);
		
		leftTable = new LocalizedTable(localizer,leftModel,fields,false,false);
		rightTable = new LocalizedTable(localizer,rightModel,fields,false,false);
		toolBar = new MicroTableEditorTwoPanelToolBar(localizer,this); 
		leftScroll = new JScrollPane(leftTable);
		rightScroll = new JScrollPane(rightTable);
		
		final SpringLayout			contentLayout = new SpringLayout();
		
		setLayout(contentLayout);
		add(leftScroll);
		add(toolBar);
		add(rightScroll);
		
		contentLayout.putConstraint(SpringLayout.NORTH,toolBar,0,SpringLayout.NORTH,this);
		contentLayout.putConstraint(SpringLayout.SOUTH,toolBar,0,SpringLayout.SOUTH,this);
		contentLayout.putConstraint(SpringLayout.NORTH,leftScroll,0,SpringLayout.NORTH,this);
		contentLayout.putConstraint(SpringLayout.SOUTH,leftScroll,0,SpringLayout.SOUTH,this);
		contentLayout.putConstraint(SpringLayout.NORTH,rightScroll,0,SpringLayout.NORTH,this);
		contentLayout.putConstraint(SpringLayout.SOUTH,rightScroll,0,SpringLayout.SOUTH,this);

		contentLayout.putConstraint(SpringLayout.WEST,toolBar,0,SpringLayout.EAST,leftScroll);
		contentLayout.putConstraint(SpringLayout.EAST,toolBar,0,SpringLayout.WEST,rightScroll);
		
		contentLayout.putConstraint(SpringLayout.WEST,leftScroll,0,SpringLayout.WEST,this);
		contentLayout.putConstraint(SpringLayout.EAST,rightScroll,0,SpringLayout.EAST,this);

		toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.SELECT,false);
		toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.SELECT_ALL,leftTable.getRowCount() != 0);
		toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.UNSELECT,false);
		toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.UNSELECT_ALL,rightTable.getRowCount() != 0);
		
		leftTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		leftTable.setColumnSelectionAllowed(false);
		setTableHeader(leftTable);
		leftTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.UNSELECT,leftTable.getSelectedRows().length != 0);
				toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.UNSELECT_ALL,leftTable.getRowCount() != 0);
			}
		});
		SwingUtils.assignActionKey(leftTable,KS_UNSELECT,this,ControllerAction.ACTION_UNSELECT.toString());
		SwingUtils.assignActionKey(leftTable,KS_UNSELECT_ALL,this,ControllerAction.ACTION_UNSELECT_ALL.toString());
		
		rightTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		rightTable.setColumnSelectionAllowed(false);		
		setTableHeader(rightTable);
		rightTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.SELECT,rightTable.getSelectedRows().length != 0);
				toolBar.setEnabled(MicroTableEditorTwoPanelToolBar.SELECT_ALL,rightTable.getRowCount() != 0);
			}
		});
		SwingUtils.assignActionKey(rightTable,KS_SELECT,this,ControllerAction.ACTION_SELECT.toString());
		SwingUtils.assignActionKey(rightTable,KS_SELECT_ALL,this,ControllerAction.ACTION_SELECT_ALL.toString());
		
		addComponentListener(new ComponentListener() {
			@Override public void componentMoved(ComponentEvent e) {}
			@Override public void componentHidden(ComponentEvent e) {}
			
			@Override 
			public void componentShown(ComponentEvent e) {
				resizeContent();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				resizeContent();
			}
		});
		
		fillLocalizedString(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedString(oldLocale, newLocale);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (ControllerAction.valueOf(e.getActionCommand())) {
			case ACTION_SELECT			:
				unselect(rightTable.getSelectedRows());
				break;
			case ACTION_SELECT_ALL		:
				unselect(buildRange(0,rightTable.getRowCount()));
				break;
			case ACTION_UNSELECT		:
				select(leftTable.getSelectedRows());
				break;
			case ACTION_UNSELECT_ALL	:
				select(buildRange(0,leftTable.getRowCount()));
				break;
			default : throw new UnsupportedOperationException("Controller action ["+e.getActionCommand()+"] is not supported yet");
		}
	}

	private FieldDescriptor[] buildFields(final Class<T> clazz) throws IllegalArgumentException, NullPointerException, SyntaxException {
		final List<FieldDescriptor>	fields = new ArrayList<>();
		
		collectFields(clazz,fields);
		return fields.toArray(new FieldDescriptor[fields.size()]);
	}

	private void collectFields(final Class<?> clazz, final List<FieldDescriptor> fields) throws IllegalArgumentException, NullPointerException, SyntaxException {
		if (clazz != null) {
			for (Field f : clazz.getDeclaredFields()) {
				fields.add(FieldDescriptor.newInstance(f.getName(),new FormFieldFormat(),f.getType()));
			}
			collectFields(clazz.getSuperclass(),fields);
		}
	}

	private void setTableHeader(final JTable table) {
		final TableColumnModel	columnModel = table.getColumnModel();
		
		table.setTableHeader(new CreoleBasedTableHeader(localizer,columnModel));
		for (int index = 0, maxIndex = columnModel.getColumnCount(); index < maxIndex; index++) {
			final int			currentIndex = index;
			final TableColumn	col = columnModel.getColumn(index); 
			
			col.setHeaderRenderer(new LocalizedHeaderCellRenderer(localizer,new LocaleSpecificTextGetter() {
				@Override
				public String getLocaleSpecificText() {
					final String	name = table.getModel().getColumnName(currentIndex); 
					
					try{return localizer.getValue(name);
					} catch (LocalizationException | IllegalArgumentException e) {
						return name;
					}
				}
				
				@Override
				public String getLocaleSpecificToolTipText() {
					return getLocaleSpecificText();
				}
			}));
		}
	}
	
	private void resizeContent() {
		final Dimension	currentSize = getSize();
		final int		preferredX = (currentSize.width - TOOLBAR_WIDTH)/2;
		
		leftScroll.setPreferredSize(new Dimension(preferredX,currentSize.height));
		toolBar.setPreferredSize(new Dimension(currentSize.width-2*preferredX,currentSize.height));
		rightScroll.setPreferredSize(new Dimension(preferredX,currentSize.height));
	}

	private int[] buildRange(final int from, final int to) {
		final int[]	range = new int[to-from];
		
		for (int index = 0; index < range.length; index++) {
			range[index] = from + index;
		}
		return range;
	}

	private void select(final int[] selected) {
		System.err.println("Select: "+Arrays.toString(selected));
	}

	private void unselect(final int[] unselected) {
		System.err.println("Unselect: "+Arrays.toString(unselected));
	}
	
	private void fillLocalizedString(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		leftScroll.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),localizer.getValue(PureLibLocalizer.TITLE_STANDARD_SELECTED)));
		rightScroll.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),localizer.getValue(PureLibLocalizer.TITLE_STANDARD_AVAILABLE)));
		SwingUtils.refreshLocale(this,oldLocale,newLocale);
	}
}