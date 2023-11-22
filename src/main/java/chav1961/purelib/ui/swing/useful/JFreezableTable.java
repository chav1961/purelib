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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class represents a {@linkplain JTable} child, that can <b>freeze</b> some of it's columns. Frozen columns don't scroll horizontally, and lefts
 * on the screen (similar to column headers) to simplify navigation on the table content. It implements as a pair of JTable instances, 
 * left of these is located at row header view in the scroll pane.</p>   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 * @last.update 0.0.7
 */
public class JFreezableTable extends JTable {
	private static final long 			serialVersionUID = 5593084316211899679L;

	protected final TableModelListener	listener = (e)->processTableChange(e);
	
	private volatile boolean			transferFocusInside = false;
	
	private final JTable				leftBar;
	private final boolean				leftBarExists;
	private final String[]				columns2Freeze;
	private final boolean[]				reduceFocusCalls = new boolean[2];
	private final ListSelectionListener	rightLSL = (e) -> {
											for(int index : getSelectionModel().getSelectedIndices()) {
												getLeftBar().getSelectionModel().setSelectionInterval(index,index);
											}
										};
	private final ListSelectionListener	leftLSL = (e) -> {
												for(int index : getLeftBar().getSelectionModel().getSelectedIndices()) {
													getSelectionModel().setSelectionInterval(index,index);
												}
										};
	private final KeyListener			leftKL = new KeyListener() {
											@Override public void keyTyped(KeyEvent e) {}
											@Override public void keyReleased(KeyEvent e) {}
											
											@Override
											public void keyPressed(KeyEvent e) {
												if (leftBarExists()) {
													if (e.getKeyCode() == KeyEvent.VK_RIGHT && getLeftBar().getColumnModel().getSelectedColumns()[0] == getLeftBar().getColumnModel().getColumnCount()-1) {
														transferFocusInside = true;
														SwingUtilities.invokeLater(()->requestFocusInWindow());
														getColumnModel().getSelectionModel().setSelectionInterval(0,0);
													}
												}
											}
										};
	private final KeyListener			rightKL = new KeyListener() {
											@Override public void keyTyped(KeyEvent e) {}
											@Override public void keyReleased(KeyEvent e) {}
											
											@Override
											public void keyPressed(KeyEvent e) {
												if (leftBarExists()) {
													if (e.getKeyCode() == KeyEvent.VK_LEFT && getColumnModel().getSelectedColumnCount() > 0 && getColumnModel().getSelectedColumns()[0] == 0) {
														transferFocusInside = true;
														SwingUtilities.invokeLater(()->getLeftBar().requestFocusInWindow());
														getLeftBar().getColumnModel().getSelectionModel().setSelectionInterval(getLeftBar().getColumnModel().getColumnCount()-1, getLeftBar().getColumnModel().getColumnCount()-1);
													}
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
				if (columns2freeze.length > 0) {
					this.leftBar = createLeftBar();
					this.leftBarExists = true;					
				}
				else {
					this.leftBar = null;
					this.leftBarExists = false;					
				}
				setModel(model);
				
				prepareRenderers(this, getModel());
				setCellSelectionEnabled(false);
				setRowSelectionAllowed(true);
				setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				addHierarchyListener((e)->{
					if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
						Component parent = getParent();
						
						while (parent != null && !(parent instanceof JScrollPane)) {
							parent = parent.getParent();
						}
						
						if (leftBarExists()) {
							if (parent instanceof JScrollPane) {
								addLeftBar((JScrollPane)parent);
							}
							else {
								removeLeftBar((JScrollPane)parent);
							}
						}
					}
				});
				addFocusListener(new FocusListener() {
					@Override 
					public void focusLost(final FocusEvent e) {
						processFocus(e, false, true);
					}
					
					@Override 
					public void focusGained(final FocusEvent e) {
						processFocus(e, true, true);
					}
				});
			}
		}
	}

	/**
	 * <p>Get source model </p>
	 * @return source model
	 * @since 0.0.6
	 */
	public TableModel getSourceModel() {
		final TableModel	model = getModel();
		
		return model instanceof RightTableModelWrapper ? ((RightTableModelWrapper)model).nested : model;
	}
	
	@Override
	public void setModel(final TableModel dataModel) {
		if (columns2Freeze == null) {
			super.setModel(dataModel);
		}
		else {
			final LeftTableModelWrapper		lw = new LeftTableModelWrapper(dataModel, columns2Freeze.length); 
			final RightTableModelWrapper	rw = new RightTableModelWrapper(dataModel, columns2Freeze.length); 
			
			if (leftBarExists()) {
				getLeftBar().setModel(lw);
				prepareRenderers(getLeftBar(), lw);
			}
			super.setModel(rw);
		}
	}

	@Override
	public void setDefaultRenderer(final Class<?> columnClass, final TableCellRenderer renderer) {
		super.setDefaultRenderer(columnClass, renderer);
		
		if (leftBarExists()) {
			getLeftBar().setDefaultRenderer(columnClass, renderer);
		}
	}
	
	@Override
	public void setDefaultEditor(final Class<?> columnClass, final TableCellEditor editor) {
		super.setDefaultEditor(columnClass, editor);
		
		if (leftBarExists()) {
			getLeftBar().setDefaultEditor(columnClass, editor);
		}
	}

	protected RightTableModelWrapper getModelWrapper() {
		
		return (RightTableModelWrapper)super.getModel();
	}
	
	protected JTable getLeftBar() {
		return leftBar;
	}
	
	protected boolean leftBarExists() {
		return leftBarExists;
	}

	protected JTable createLeftBar() {
		final JTable	result = new JTable();
		
		if (getInputMap().keys() != null) {
			for (KeyStroke item : getInputMap().keys()) {
				result.getInputMap().put(item, getInputMap().get(item));
			}
		}
		if (getActionMap().keys() != null) {
			for (Object item : getActionMap().keys()) {
				result.getActionMap().put(item, getActionMap().get(item));
			}
		}
		result.setName("JFreezableTable.LeftBar");
		return result;
	}
	
	protected void addLeftBar(final JScrollPane parent) {
		getLeftBar().setPreferredScrollableViewportSize(new Dimension(200,0));
		parent.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, getLeftBar().getTableHeader());
		parent.setRowHeaderView(getLeftBar());
		getLeftBar().addFocusListener(new FocusListener() {
			@Override 
			public void focusLost(FocusEvent e) {
				processFocus(e, false, false);
			}
			
			@Override 
			public void focusGained(FocusEvent e) {
				processFocus(e, true, false);
			}
		});
		getLeftBar().getSelectionModel().addListSelectionListener(leftLSL);
		this.getSelectionModel().addListSelectionListener(rightLSL);
		getLeftBar().addKeyListener(leftKL);
		this.addKeyListener(rightKL);
	}
	
	protected void removeLeftBar(final JScrollPane parent) {
		getLeftBar().removeKeyListener(leftKL);
		this.removeKeyListener(rightKL);
		getLeftBar().getSelectionModel().removeListSelectionListener(leftLSL);
		this.getSelectionModel().removeListSelectionListener(rightLSL);
	}

	protected void processFocus(final FocusEvent e, final boolean gained, final boolean right) {
		if (reduceFocusCalls[0] != gained || reduceFocusCalls[1] != right) {
			reduceFocusCalls[0] = gained;
			reduceFocusCalls[1] = right;
			
			if (leftBarExists() && gained && getParent() != null) {
				if (transferFocusInside) {
					transferFocusInside = false;
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
								SwingUtilities.invokeLater(()->after.requestFocusInWindow());
							}
							else if (after == leftBar) {
								SwingUtilities.invokeLater(()->before.requestFocusInWindow());
							}
						}
						else {
							if (before == this) {
								SwingUtilities.invokeLater(()->after.requestFocusInWindow());
							}
							else if (after == this) {
								SwingUtilities.invokeLater(()->before.requestFocusInWindow());
							}
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

	private static class LeftTableModelWrapper implements TableModel {
		private final TableModel	nested;
		private final int			frozenColumns;
				
		private LeftTableModelWrapper(final TableModel nested, final int frozenColumns) {
			this.nested = nested;
			this.frozenColumns = frozenColumns;
		}

		@Override
		public int getRowCount() {
			return nested.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return frozenColumns;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return nested.getColumnName(columnIndex);
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return nested.getColumnClass(columnIndex);
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return nested.isCellEditable(rowIndex, columnIndex);
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return nested.getValueAt(rowIndex, columnIndex);
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			nested.setValueAt(aValue, rowIndex, columnIndex);
		}

		@Override
		public void addTableModelListener(final TableModelListener l) {
			nested.addTableModelListener(l);
		}

		@Override
		public void removeTableModelListener(final TableModelListener l) {
			nested.removeTableModelListener(l);
		}
	}

	private static class RightTableModelWrapper implements TableModel {
		private final TableModel	nested;
		private final int			frozenColumns;
		
		private RightTableModelWrapper(final TableModel nested, final int frozenColumns) {
			this.nested = nested;
			this.frozenColumns = frozenColumns;
		}

		@Override
		public int getRowCount() {
			return nested.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return nested.getColumnCount() - frozenColumns;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return nested.getColumnName(columnIndex + frozenColumns);
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return nested.getColumnClass(columnIndex + frozenColumns);
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return nested.isCellEditable(rowIndex, columnIndex + frozenColumns);
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return nested.getValueAt(rowIndex, columnIndex + frozenColumns);
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			nested.setValueAt(aValue, rowIndex, columnIndex + frozenColumns);
		}

		@Override
		public void addTableModelListener(final TableModelListener l) {
			nested.addTableModelListener(l);
		}

		@Override
		public void removeTableModelListener(final TableModelListener l) {
			nested.removeTableModelListener(l);
		}
	}
}

