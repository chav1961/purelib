package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleSpecificTextGetter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfacers.ControllerAction;
import chav1961.purelib.ui.swing.MicroTableEditor.ContentGetter;

class MicroTableEditorMarkedList<T> extends JPanel implements ActionListener, LocaleChangeListener, ContentGetter<T> {
	private static final long 		serialVersionUID = -1396058311479685945L;
	private static final KeyStroke	KS_SELECT = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0);
	private static final KeyStroke	KS_UNSELECT = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0);
	private static final KeyStroke	KS_INVERT = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,0);
	private static final int		MARKER_WIDTH = 25;
	
	private final Localizer		localizer;
	private final TableModel	model;
	private final JTable		table;
	private final JScrollPane	scroll;

	MicroTableEditorMarkedList(final Localizer localizer, final MicroTableEditorMarkableContent<T> model) throws SyntaxException {
		setLayout(new BorderLayout());
		final TableColumnModel	tcm = new DefaultTableColumnModel();
		
		TableColumn tc;
		
		tcm.addColumn(tc = new TableColumn(0,10));
//		tc.setPreferredWidth(10);
		tcm.addColumn(tc = new TableColumn(1,100));
//		tc.setPreferredWidth(100);
		
		this.localizer = localizer;
		this.model = model;
		this.table = new JTable(model,tcm);
		this.scroll = new JScrollPane(table);

		scroll.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		add(scroll,BorderLayout.CENTER);
		
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setColumnSelectionAllowed(false);
		setTableHeader(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		SwingUtils.assignActionKey(table,KS_SELECT,this,ControllerAction.ACTION_SELECT.toString());
		SwingUtils.assignActionKey(table,KS_UNSELECT,this,ControllerAction.ACTION_UNSELECT.toString());
		SwingUtils.assignActionKey(table,KS_INVERT,this,ControllerAction.ACTION_INVERT_SELECTION.toString());
		
		fillLocalizedContent();
	}
	
	public T getResult() {
		return ((ContentGetter<T>)model).getContent();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedContent();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (ControllerAction.valueOf(e.getActionCommand())) {
			case ACTION_SELECT				:
				select(table.getSelectedRows());
				break;
			case ACTION_UNSELECT			:
				unselect(table.getSelectedRows());
				break;
			case ACTION_INVERT_SELECTION	:
				int		selectedCount = 0, unselectedCount = 0;
				
				for (int index : table.getSelectedRows()) {
					if ((Boolean)model.getValueAt(index,0)) {
						unselectedCount++;
					}
					else {
						selectedCount++;
					}
				}
				int[]	selectedList = new int[selectedCount], unselectedList = new int[unselectedCount];

				selectedCount =  0;
				unselectedCount = 0;
				for (int index : table.getSelectedRows()) {
					if ((Boolean)model.getValueAt(index,0)) {
						unselectedList[unselectedCount++] = index;
					}
					else {
						selectedList[selectedCount++] = index;
					}
				}
				select(selectedList);
				unselect(unselectedList);				
				break;
			default : throw new UnsupportedOperationException("Controller action ["+e.getActionCommand()+"] is not supported yet");
		}
	}

	@Override
	public T getContent() {
		// TODO Auto-generated method stub
		return null;
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
					if (currentIndex == 0) {
						try{return localizer.getValue(PureLibLocalizer.TITLE_STANDARD_SELECTION_MARK_TOOLTIP);
						} catch (LocalizationException | IllegalArgumentException e) {
							return "...";
						}
					}
					else {
						return getLocaleSpecificText();
					}
				}
			}));
			if (index == 0) {
				col.setPreferredWidth(25);
			}
			else {
				col.setPreferredWidth(200);
			}
		}
	}
	
	private void select(final int[] select) {
		for (int item : select) {
			table.getModel().setValueAt(true,item,0);
		}
	}

	private void unselect(final int[] unselect) {
		for (int item : unselect) {
			table.getModel().setValueAt(false,item,0);
		}
	}
	
	private void fillLocalizedContent() {
		((AbstractTableModel)table.getModel()).fireTableStructureChanged();
	}

}