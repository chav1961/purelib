package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.interfacers.ControllerAction;
import chav1961.purelib.ui.interfacers.FormModel.SupportedOperations;

class MicroTableEditorEditedList extends JPanel implements ActionListener, LocaleChangeListener {
	private static final long 			serialVersionUID = 4588173930310692125L;
	
	private final JLabel				state = new JLabel(" ");
	private final JTable				table;
	private final Localizer				localizer;
	private final MicroTableEditorEditedToolBar			toolBar;
	private final MicroTableEditorEditableContent<?>	model;
	
	MicroTableEditorEditedList(final Localizer localizer, final MicroTableEditorEditableContent<?> model, final Set<SupportedOperations> operations) throws LocalizationException{
		final JPanel		statePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		setLayout(new BorderLayout());
		add(toolBar = new MicroTableEditorEditedToolBar(localizer,this,operations,true),BorderLayout.NORTH);
		
		table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(table),BorderLayout.CENTER);
		
		state.setMinimumSize(new Dimension(16,16));
		state.setPreferredSize(new Dimension(100,16));
		statePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		statePanel.add(state);
		add(statePanel,BorderLayout.SOUTH);
		
		this.localizer = localizer;
		this.model = model;

		if (operations.contains(SupportedOperations.INSERT)) {
			assignKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),ControllerAction.ACTION_INSERT);
			assignKey(table,KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),ControllerAction.ACTION_INSERT);
		}
		if (operations.contains(SupportedOperations.DUPLICATE)) {
			assignKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK|InputEvent.CTRL_DOWN_MASK),ControllerAction.ACTION_DUPLICATE);
			assignKey(table,KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK|InputEvent.CTRL_DOWN_MASK),ControllerAction.ACTION_DUPLICATE);
		}
		if (operations.contains(SupportedOperations.DELETE)) {
			assignKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),ControllerAction.ACTION_DELETE);
			assignKey(table,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),ControllerAction.ACTION_DELETE);
		}
		assignKey(this,KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),ControllerAction.EXIT);
		assignKey(table,KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),ControllerAction.EXIT);
		setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				return model.checkContent();
			}
		});
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final int	selected = table.getSelectedRow();
		
		switch (ControllerAction.valueOf(e.getActionCommand())) {
			case EXIT				: 
				model.checkContent();
				break;
			case COMMIT				:
				model.checkContent();
				break;
			case ACTION_INSERT		:
				model.insertRow();
				table.setRowSelectionInterval(selected == -1 ? 0 : selected,selected == -1 ? 0 : selected);
				break;
			case ACTION_DUPLICATE	:
				model.duplicateRow(selected == -1 ? 0 : table.getSelectedRow());
				table.setRowSelectionInterval(selected == -1 ? 0 : selected,selected == -1 ? 0 : selected);
				break;
			case ACTION_DELETE		:
				if (selected != -1) {
					model.deleteRow(selected);
					if (model.getRowCount() > 0) {
						if (selected > 0) {
							table.setRowSelectionInterval(selected - 1,selected - 1);
						}
						else {
							table.setRowSelectionInterval(selected,selected);
						}
					}
				}
				break;
			default : throw new UnsupportedOperationException("Controller action ["+e.getActionCommand()+"] is not supported yet");
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		toolBar.localeChanged(oldLocale, newLocale);
		model.localeChanged(oldLocale, newLocale);
	}
	
	private void assignKey(final JComponent component, final KeyStroke keystoke, ControllerAction action) {
		component.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keystoke,action.toString());
		component.getActionMap().put(action.toString(),new AbstractAction() {private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				MicroTableEditorEditedList.this.actionPerformed(new ActionEvent(e.getSource(),e.getID(),action.toString()));
			}
		});
	}
}