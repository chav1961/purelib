package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.concurrent.LightWeightListenerList;

/**
 * <p>This class represents a {@linkplain JTable} child, that can <b>freeze</b> some of it's columns. Frozen columns don't scroll horizontally, and lefts
 * on the screen (similar to column headers) to simplify navigation on the table content. It implements as a pair of JTable instances, 
 * left of these is located at row header view in the scroll pane.</p>   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class JFreezableTable extends JTable {
	private static final long 			serialVersionUID = 5593084316211899679L;

	protected final TableModelListener	listener = (e)->processTableChange(e);
	
	private volatile TableModel			model = null;
	private volatile boolean			leftBarExists = false;
	private volatile boolean			transferFocus = true;
	private volatile JTable				leftBar = null;
	
	private final String[]				columns2Freeze;
	private final ListSelectionListener	rightLSL = (e) -> {
											for(int index : getSelectionModel().getSelectedIndices()) {
												leftBar.getSelectionModel().setSelectionInterval(index,index);
											}
										};
	private final ListSelectionListener	leftLSL = (e) -> {
												for(int index : leftBar.getSelectionModel().getSelectedIndices()) {
													getSelectionModel().setSelectionInterval(index,index);
												}
										};
	private final KeyListener			leftKL = new KeyListener() {
											@Override public void keyTyped(KeyEvent e) {}
											@Override public void keyReleased(KeyEvent e) {}
											
											@Override
											public void keyPressed(KeyEvent e) {
												if (e.getKeyCode() == KeyEvent.VK_RIGHT && leftBar.getColumnModel().getSelectedColumns()[0] == leftBar.getColumnModel().getColumnCount()-1) {
													getColumnModel().getSelectionModel().setSelectionInterval(0,0);
													transferFocus = false;
													requestFocusInWindow();
												}
											}
										};
	private final KeyListener			rightKL = new KeyListener() {
											@Override public void keyTyped(KeyEvent e) {}
											@Override public void keyReleased(KeyEvent e) {}
											
											@Override
											public void keyPressed(KeyEvent e) {
												if (e.getKeyCode() == KeyEvent.VK_LEFT && getColumnModel().getSelectedColumns()[0] == 0) {
													leftBar.getColumnModel().getSelectionModel().setSelectionInterval(leftBar.getColumnModel().getColumnCount()-1,leftBar.getColumnModel().getColumnCount()-1);
													transferFocus = false;
													leftBar.requestFocusInWindow();
												}
											}
										};
	
	/**
	 * <p>Constructor of the class</p> 
	 * @param model table model to use
	 * @param columns2freeze list of column names to freeze
	 * @throws NullPointerException when any parameters are null
	 * @throws IllegalArgumentException when some column name 
	 */
	public JFreezableTable(final TableModel model, final String... columns2freeze) throws NullPointerException, IllegalArgumentException {
		if (model == null) {
			throw new NullPointerException("Table model can't be null");
		}
		else if (columns2freeze == null) {
			throw new NullPointerException("Freezed columns list can't be null");
		}
		else if (Utils.checkArrayContent4Nulls(columns2freeze) >= 0) {
			throw new NullPointerException("Some items in the freezed columns list are null");
		}
		else {
			this.columns2Freeze = columns2freeze;
			
			final StringBuilder	sb = new StringBuilder();
			
loop:			for (String item : columns2freeze) {
				for (int index = 0, maxIndex = model.getColumnCount(); index < maxIndex; index++) {
					if (item.equals(model.getColumnName(index))) {
						continue loop; 
					}
				}
				sb.append(',').append(item);
			}
			if (sb.length() > 0) {
				throw new IllegalArgumentException("Freezed coulmns ["+sb.substring(1)+"] are not known in the table model");
			}
			else {
				setModel(model);
				addHierarchyListener((e)->{
					if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
						Component parent = getParent();
						
						while (parent != null && !(parent instanceof JScrollPane)) {
							parent = parent.getParent();
						}
						
						if (parent instanceof JScrollPane) {
							createLeftBar((JScrollPane)parent);
						}
						else {
							removeLeftBar();
						}
					}
				});
				addFocusListener(new FocusListener() {
					@Override public void focusLost(FocusEvent e) {processFocus(e,false,true);}
					@Override public void focusGained(FocusEvent e) {processFocus(e,true,true);}
				});
			}
		}
	}

	@Override
	public void setModel(final TableModel dataModel) {
		if (this.model != null) {
			this.model.removeTableModelListener(listener);
		}
		dataModel.addTableModelListener(listener);
		this.model = dataModel;
		super.setModel(new RightTableModel(dataModel,columns2Freeze));
		if (leftBarExists()) {
			leftBar.setModel(new LeftTableModel(dataModel,columns2Freeze));
		}
	}
	
	protected JTable getLeftBar() {
		return leftBar;
	}
	
	protected boolean leftBarExists() {
		return leftBarExists;
	}

	protected void createLeftBar(final JScrollPane scroll) {
		leftBar = new JTable(new LeftTableModel(model,columns2Freeze));
		scroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER,leftBar.getTableHeader());
		scroll.setRowHeaderView(leftBar);
		leftBarExists = true;
		leftBar.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {processFocus(e,false,false);}
			@Override public void focusGained(FocusEvent e) {processFocus(e,true,false);}
		});
		leftBar.getSelectionModel().addListSelectionListener(leftLSL);
		this.getSelectionModel().addListSelectionListener(rightLSL);
		leftBar.addKeyListener(leftKL);
		this.addKeyListener(rightKL);
	}

	protected void removeLeftBar() {
		if (leftBar != null) {
			leftBar.removeKeyListener(leftKL);
			this.removeKeyListener(rightKL);
			leftBar.getSelectionModel().removeListSelectionListener(leftLSL);
			this.getSelectionModel().removeListSelectionListener(rightLSL);
			leftBar = null;
		}
		leftBarExists = false;
	}

	protected void processFocus(final FocusEvent e, final boolean gained, final boolean right) {
		if (leftBarExists() && gained && getParent() != null) {
			if (!transferFocus) {
				transferFocus = true;
			}
			else {
				final Component 			c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				final Container 			root = c.getFocusCycleRootAncestor();
				final FocusTraversalPolicy 	policy = root.getFocusTraversalPolicy();
				final Component				before = policy.getComponentBefore(root,e.getComponent());
				final Component				after = policy.getComponentAfter(root,e.getComponent());
				final Component				from = e.getOppositeComponent();
				    
				if (from == this || from == leftBar) {
					if (right) {
						if (before == leftBar) {
							after.requestFocusInWindow();
						}
						else if (after == leftBar) {
							before.requestFocusInWindow();
						}
					}
					else {
						if (before == this) {
							after.requestFocusInWindow();
						}
						else if (after == this) {
							before.requestFocusInWindow();
						}
					}
				}
			}
		}
	}

	private void processTableChange(final TableModelEvent event) {
		tableChanged(event);
		if (leftBarExists()) {
			leftBar.tableChanged(event);
		}
	}

	private static abstract class SplittedTableModel implements TableModel {
		private final LightWeightListenerList<TableModelListener>	ll = new LightWeightListenerList<>(TableModelListener.class);
		
		protected final TableModel	nested;
		protected volatile int[]	columnIndices;
		
		protected SplittedTableModel(final TableModel nested, final String... freezedColumns) {
			this.nested = nested;
			buildExcludes(freezedColumns);
			nested.addTableModelListener((e) -> {
				buildExcludes(freezedColumns);
				ll.fireEvent((listener)->listener.tableChanged(e));
			});
		}

		protected abstract void buildExcludes(final String[] freezedColumns);
		
		@Override
		public int getRowCount() {
			return nested.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return columnIndices.length;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return nested.getColumnName(columnIndices[columnIndex]);
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return nested.getColumnClass(columnIndices[columnIndex]);
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return nested.isCellEditable(rowIndex,columnIndices[columnIndex]);
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return nested.getValueAt(rowIndex,columnIndices[columnIndex]);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			nested.setValueAt(aValue,rowIndex,columnIndices[columnIndex]);
		}

		@Override
		public void addTableModelListener(final TableModelListener l) {
			if (l == null) {
				throw new NullPointerException("Table model listener can't be null");
			}
			else {
				ll.addListener(l);
			}
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			if (l == null) {
				throw new NullPointerException("Table model listener can't be null");
			}
			else {
				ll.addListener(l);
			}
		}

	}

	private static class LeftTableModel extends SplittedTableModel {
		private LeftTableModel(final TableModel nested, final String... freezedColumns) {
			super(nested,freezedColumns);
		}

		@Override
		protected void buildExcludes(final String[] freezedColumns) {
			if (nested == null || freezedColumns == null) {
				columnIndices = new int[0];
			}
			else {
				final int		count = nested.getColumnCount();
				final int[]		columns = new int[freezedColumns.length];
				
	loop:			for (int index = 0, cursor = 0; index < count; index++) {
					final String	colName = nested.getColumnName(index);
					
					for (String item : freezedColumns) {
						if (colName.equals(item)) {
							columns[cursor++] = index;
							continue loop;
						}
					}
				}
				columnIndices = columns;
			}
		}
	}

	private static class RightTableModel extends SplittedTableModel {
		private RightTableModel(final TableModel nested, final String... freezedColumns) {
			super(nested,freezedColumns);
		}

		@Override
		protected void buildExcludes(final String[] freezedColumns) {
			if (nested == null || freezedColumns == null) {
				columnIndices = new int[0];
			}
			else {
				final int		count = nested.getColumnCount();
				final int[]		columns = new int[count - freezedColumns.length];
				
	loop:			for (int index = 0, cursor = 0; index < count; index++) {
					final String	colName = nested.getColumnName(index);
					
					for (String item : freezedColumns) {
						if (colName.equals(item)) {
							continue loop;
						}
					}
					columns[cursor++] = index;
				}
				columnIndices = columns;
			}
		}
	}
}

