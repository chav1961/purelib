package chav1961.purelib.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import chav1961.purelib.ui.annotations.WizardableClass;
import chav1961.purelib.ui.annotations.WizardableField;

public class WizardManager extends JDialog {
	private static final long 	serialVersionUID = 3649621908902703598L;

	private final Object			persistent;
	private final FSMItem[]			table;
	private final JLabel			icon = new JLabel();
	private final JButton			backward = new JButton(), forward = new JButton(), cancel = new JButton();
	private final ActionListener	buttonListener = new ActionListener(){
										@Override
										public void actionPerformed(ActionEvent e) {
											// TODO Auto-generated method stub
											
										}
									};
	private int						actualState;
	private boolean					alreadyShowed = false;
	
	
	public WizardManager(final Window owner, final String title, final Dialog.ModalityType modalityType, final Object persistent, final FSMItem[] fsmTable, final int initialState) {
		super(owner,title,modalityType);
		if (persistent == null) {
			throw new IllegalArgumentException("Persistent object can't be null");
		}
		else if (fsmTable == null || fsmTable.length == 0) {
			throw new IllegalArgumentException("FSM table descriptor can't be null or empty array");
		}
		else {
			this.persistent = persistent;
			this.table = fsmTable;
			this.actualState = initialState;
			checkParameters();
			prepareControls();
		}
	}
	
	public WizardManager(final Frame owner, final String title, final boolean modal, final Object persistent, final FSMItem[] fsmTable, final int initialState) {
		super(owner,title,modal);
		if (persistent == null) {
			throw new IllegalArgumentException("Persistent object can't be null");
		}
		else if (fsmTable == null || fsmTable.length == 0) {
			throw new IllegalArgumentException("FSM table descriptor can't be null or empty array");
		}
		else {
			this.persistent = persistent;
			this.table = fsmTable;
			this.actualState = initialState;
			checkParameters();
			prepareControls();
		}
	}

	public WizardManager(final Dialog owner, final String title, final boolean modal, final Object persistent, final FSMItem[] fsmTable, final int initialState) {
		super(owner,title,modal);
		if (persistent == null) {
			throw new IllegalArgumentException("Persistent object can't be null");
		}
		else if (fsmTable == null || fsmTable.length == 0) {
			throw new IllegalArgumentException("FSM table descriptor can't be null or empty");
		}
		else {
			this.persistent = persistent;
			this.table = fsmTable;
			this.actualState = initialState;
			checkParameters();
			prepareControls();
		}
	}

	private void checkParameters() {
		final Class<?>	cl = persistent.getClass();
		
		if (!cl.isAnnotationPresent(WizardableClass.class)) {
			throw new IllegalArgumentException("Persistence class instance is not marked by @WClass annotation");
		}
		else {
			final Set<String>	names = new HashSet<>();
			
			for (Field f : cl.getFields()) {
				if (f.isAnnotationPresent(WizardableField.class)) {
					if (names.contains(f.getAnnotation(WizardableField.class).name())) {
						throw new IllegalArgumentException("Persistence class instance field ["+f.getName()+"] has duplicate name ["+f.getAnnotation(WizardableField.class).name()+"] in the @WField annotation");
					}
					else {
						names.add(f.getAnnotation(WizardableField.class).name());
					}
				}
			}
			if (names.size() == 0) {
				throw new IllegalArgumentException("Persistence class instance has no fields marked by @WField annotation");
			}
			else {
				final Set<Integer>	states = new HashSet<>();
				boolean				wasTerminal = false;
				
				for (FSMItem item : table) {
					for (String name : item.items) {
						if (!names.contains(name)) {
							throw new IllegalArgumentException("FSM table for state ["+item.state+"] has an item reference ["+name+"] to non-existent name in the persistence class instance. Check @WField anoontations for it");
						}
					}
					if (states.contains(item.state)) {
						throw new IllegalArgumentException("FSM table has duplicated records for state ["+item.state+"]");
					}
					else {
						states.add(item.state);
					}
					if (item.terminal && item.jumps != null) {
						throw new IllegalArgumentException("FSM table has record for state ["+item.state+"] with 'terminal':true and non-empty jumps table. This is a mutually exclusive options");
					}
					wasTerminal |= item.terminal;
				}
				if (!wasTerminal) {
					throw new IllegalArgumentException("FSM table has no one record with the \"terminal\":true. At least one record need be presented");
				}
				for (FSMItem item : table) {
					if (item.jumps != null) {
						for (FSMJump jump : item.jumps) {
							if (!states.contains(jump.newState)) {
								throw new IllegalArgumentException("FSM table for state ["+item.state+"] has a jump table 'newState' reference ["+jump.newState+"] to non-existent state in the FSM table");
							}
						}
					}
				}
				if (!states.contains(actualState)) {
					throw new IllegalArgumentException("FSM table has no one record appropriates to the initial state ["+actualState+"]. Check FSM table or 'initialState' parameter");
				}
				else if (actualItem().terminal){
					throw new IllegalArgumentException("Initial state record in the FSM table is marked as terminal ('terminal':true). Use at least two records in the FSM table");
				}
			}
		}
	}

	@Override
	public void setVisible(final boolean visible) {
		if (alreadyShowed) {
			throw new IllegalStateException("Attempt to use FSMManager instance twice!"); 
		}
		else {
			if (visible) {
				alreadyShowed = true;
			}
			super.setVisible(visible);
		}
	}
	
	private FSMItem actualItem() {
		for (FSMItem item : table) {
			if (item.state == actualState) {
				return item;
			}
		}
		throw new RuntimeException("FSM table problem..."); 
	}

	private void prepareControls() {
		final JPanel	panel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
		
		panel.add(backward);	backward.setActionCommand("backward");		backward.addActionListener(buttonListener);
		panel.add(forward);		forward.setActionCommand("forward");		forward.addActionListener(buttonListener);
		panel.add(cancel);		cancel.setActionCommand("cancel");			cancel.addActionListener(buttonListener);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		icon.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		getContentPane().add(panel, BorderLayout.WEST);
	}
}
