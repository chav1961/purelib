package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class represents a {@linkplain JTable} child, that can <b>freeze</b> some of it's columns. Frozen columns don't scroll horizontally, and lefts
 * on the screen (similar to column headers) to simplify navigation on the table content. It implements as a pair of JTable instances, 
 * left of these is located at row header view in the scroll pane.</p>   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @lastUpdate 0.0.6
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
												if (e.getKeyCode() == KeyEvent.VK_LEFT && getColumnModel().getSelectedColumnCount() > 0 && getColumnModel().getSelectedColumns()[0] == 0) {
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
	 * @throws NullPointerException when table model is null
	 * @throws IllegalArgumentException when columns are null or contains null or empty columns inside 
	 */
	public JFreezableTable(final TableModel model, final String... columns2freeze) throws NullPointerException, IllegalArgumentException {
		if (model == null) {
			throw new NullPointerException("Table model can't be null");
		}
		else if (columns2freeze == null || Utils.checkArrayContent4Nulls(columns2freeze, true) >= 0) {
			throw new IllegalArgumentException("Freezed columns are null or contains nulls or empties inside");
		}
		else {
			this.columns2Freeze = columns2freeze;
			
			final StringBuilder	sb = new StringBuilder();
			
			if (model instanceof NodeMetadataOwner) {
				for (String item : columns2freeze) {
					if (!((NodeMetadataOwner)model).hasNodeMetadata(item)) {
						sb.append(',').append(item);
					}
				}
			}
			if (sb.length() > 0) {
				throw new IllegalArgumentException("Freezed coulmns ["+sb.substring(1)+"] are not known in the table model");
			}
			else {
				prepareRenderers(this, model);
				setModel(model);
				setCellSelectionEnabled(false);
				setRowSelectionAllowed(true);
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				addHierarchyListener((e)->{
					if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
						Component parent = getParent();
						
						while (parent != null && !(parent instanceof JScrollPane)) {
							parent = parent.getParent();
						}
						
						if ((parent instanceof JScrollPane) && columns2freeze.length > 0) {
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
		if (this.model instanceof NodeMetadataOwner) {
			this.model.removeTableModelListener(listener);
		}
		this.model = dataModel;
		if (model instanceof NodeMetadataOwner) {
			final ContentNodeMetadata[] children = extractChildren((NodeMetadataOwner)model);
			
			dataModel.addTableModelListener(listener);
			super.setModel(new RightTableModel(dataModel, children, columns2Freeze));
			if (leftBarExists()) {
				leftBar.setModel(new LeftTableModel(dataModel, children, columns2Freeze));
			}
		}
		else {
			super.setModel(dataModel);
		}
	}
	
	/**
	 * <p>Get source table model passed into {@linkplain #setModel(TableModel)} method</p>
	 * @return source table model. Can't be null
	 * @since 0.0.6
	 */
	public TableModel getSourceModel() {
		return ((RightTableModel)getModel()).nested;
	}

	protected JTable getLeftBar() {
		return leftBar;
	}
	
	protected boolean leftBarExists() {
		return leftBarExists;
	}

	protected void createLeftBar(final JScrollPane scroll) {
		final ContentNodeMetadata[] children = extractChildren((NodeMetadataOwner)model);
		final TableModel			leftModel = new LeftTableModel(model, children, columns2Freeze);
		
		leftBar = new JTable(leftModel);
		
		for (KeyStroke item : getInputMap().keys()) {
			leftBar.getInputMap().put(item, getInputMap().get(item));
		}
		for (Object item : getActionMap().keys()) {
			leftBar.getActionMap().put(item, getActionMap().get(item));
		}
		prepareRenderers(leftBar, leftModel);
		
        leftBar.setPreferredScrollableViewportSize(new Dimension(200,0));
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

	private static void prepareRenderers(final JTable table, final TableModel model) {
		final Set<Class<?>>	processed = new HashSet<>();
		
		for (int index =  0, maxIndex = model.getColumnCount(); index < maxIndex; index++) {
			final Class<?>	cl = CompilerUtils.toWrappedClass(model.getColumnClass(index));
			
			if (!processed.contains(cl)) {
				try{table.setDefaultRenderer(cl, SwingUtils.getCellRenderer(cl, new FieldFormat(cl), TableCellRenderer.class));
				} catch (EnvironmentException e) {
					throw new IllegalArgumentException("No appropriative cell renderer for field ["+model.getColumnName(index)+"] with type ["+cl.getCanonicalName()+"]: "+e.getLocalizedMessage());
				}
			}
		}
	}

	private static ContentNodeMetadata[] extractChildren(final NodeMetadataOwner model) {
		final String[]				names = model.getMetadataChildrenNames();
		final ContentNodeMetadata[]	result = new ContentNodeMetadata[names.length];
		int		index = 0;
		
		for (String item : names) {
			result[index++] = model.getNodeMetadata(item);
		}
		return result;
	}
	
	private static abstract class SplittedTableModel extends DefaultTableModel{
		private static final long serialVersionUID = 1L;

		private final LightWeightListenerList<TableModelListener>	ll = new LightWeightListenerList<>(TableModelListener.class);
		
		protected final TableModel				nested;
		protected final ContentNodeMetadata[]	children;
		protected volatile int[]				columnIndices;
		
		protected SplittedTableModel(final TableModel nested, final ContentNodeMetadata[] children, final String... freezedColumns) {
			this.nested = nested;
			this.children = children;
			buildExcludes(freezedColumns);
			nested.addTableModelListener((e) -> {
				buildExcludes(freezedColumns);
				ll.fireEvent((listener)->listener.tableChanged(e));
			});
		}

		protected abstract void buildExcludes(final String[] freezedColumns);
		
		@Override
		public int getRowCount() {
			if (nested == null) {
				return 0;
			}
			else {
				return nested.getRowCount();
			}
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
		public void removeTableModelListener(final TableModelListener l) {
			if (l == null) {
				throw new NullPointerException("Table model listener can't be null");
			}
			else {
				ll.addListener(l);
			}
		}

		protected ContentNodeMetadata getColumnModel(final int columnIndex) {
			return children[columnIndex];
		}
	}

	private static class LeftTableModel extends SplittedTableModel {
		private LeftTableModel(final TableModel nested, final ContentNodeMetadata[] children, final String... freezedColumns) {
			super(nested, children, freezedColumns);
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
					final String	colName = getColumnModel(index).getName();
					
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
		private RightTableModel(final TableModel nested, final ContentNodeMetadata[] children, final String... freezedColumns) {
			super(nested, children, freezedColumns);
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
					final String	colName = getColumnModel(index).getName();
					
					if (!colName.isEmpty()) {
						for (String item : freezedColumns) {
							if (colName.equals(item)) {
								continue loop;
							}
						}
						columns[cursor++] = index;
					}
				}
				columnIndices = columns;
			}
		}
	}
}

