package chav1961.purelib.ui.swing.useful;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import chav1961.purelib.ui.swing.SwingUtils;

public class JFreezableTableNew extends JTable {
	private static final long serialVersionUID = -6943771950230940535L;
	
	private final JTable	fixedTable = new JTable();
	private final int		numberOfFrozen; 

	public static void main(final String[] args) {
		final JTable t = new JTable(new String[][] {new String[] {"1","2","1","2"}, new String[] {"3","4","3","4"}}, new String[] {"c1","c2","c3","c4"});
		final JFreezableTableNew tnew = new JFreezableTableNew(t.getModel(), "c1", "c2");
		
		final JScrollPane pane = new JScrollPane(tnew);
		
		tnew.setPreferredSize(new Dimension(200,200));
		tnew.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JOptionPane.showMessageDialog(null, pane);
	}
	
	public JFreezableTableNew(final TableModel model, final String... columns2freeze) {
		super(model);
		this.numberOfFrozen = columns2freeze.length;
				
		setAutoCreateColumnsFromModel( false );
		addPropertyChangeListener( this::propChange );
		
		fixedTable.setAutoCreateColumnsFromModel( false );
		fixedTable.setModel(model);
		fixedTable.setSelectionModel(getSelectionModel());
		fixedTable.setFocusable(true);
		
		final int[]	indices = new int[columns2freeze.length];
		int	where = 0;
		
loop:	for(String item : columns2freeze) {
			for(int index = 0; index < model.getColumnCount(); index++) {
				if (model.getColumnName(index).equals(item)) {
					indices[where++] = index;
					continue loop;
				}
			}
			throw new IllegalArgumentException("Column name ["+item+"] to freeze is missing in the model"); 
		}
        final TableColumnModel fromModel = getColumnModel();
        final TableColumnModel toModel = fixedTable.getColumnModel();

		for (int index : indices) {
			final TableColumn column = fromModel.getColumn(index);
			
    	    fromModel.removeColumn(column);
			toModel.addColumn(column);
		}
		new FocusTransferer(this, 0, fixedTable, numberOfFrozen-1, KeyEvent.VK_LEFT);
		new FocusTransferer(fixedTable, numberOfFrozen-1, this, 0, KeyEvent.VK_RIGHT);
	}

	@Override
    protected void configureEnclosingScrollPane() {
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            JViewport port = (JViewport) parent;
            Container gp = port.getParent();
            if (gp instanceof JScrollPane) {
                final JScrollPane scrollPane = (JScrollPane)gp;
                final JViewport viewport = scrollPane.getViewport();
                
                if (viewport == null || SwingUtilities.getUnwrappedView(viewport) != this) {
                    return;
                }
                scrollPane.setColumnHeaderView(getTableHeader());
                fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
        		
                scrollPane.setRowHeaderView(fixedTable);
                scrollPane.getRowHeader().addChangeListener((e)->{
            		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
                } );
                
        		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedTable.getTableHeader());
        		super.configureEnclosingScrollPane();
            }
        }
    }
	
	private void propChange(final PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
			case "selectionModel" :
				fixedTable.setSelectionModel(getSelectionModel());
				break;
			case "model" :
				fixedTable.setModel(getModel());
				break;
			default :
//				System.err.println("Props: "+e);
		}
	}
	
	private static class FocusTransferer implements TableColumnModelListener, KeyListener {
		private final JTable 	from;
		private final int		fromIndex;
		private final JTable 	to;
		private final int		toIndex;
		private final int 		keyCode;
		private int	currentSelection = -1;
		
		private FocusTransferer(final JTable from, final int fromIndex, final JTable to, final int toIndex, final int keyCode) {
			this.from = from;
			this.fromIndex = fromIndex;
			this.to = to;
			this.toIndex = toIndex;
			this.keyCode = keyCode;
			from.getColumnModel().addColumnModelListener(this);
			from.addKeyListener(this);
		}

		@Override
		public void columnSelectionChanged(final ListSelectionEvent e) {
			currentSelection = ((ListSelectionModel)e.getSource()).getAnchorSelectionIndex();
		}
		
		@Override
		public void keyPressed(final KeyEvent e) {
			if (currentSelection == fromIndex && e.getKeyCode() == keyCode) {
				to.requestFocusInWindow();
				to.getColumnModel().getSelectionModel().setAnchorSelectionIndex(toIndex);
			}
		}

		@Override public void columnAdded(TableColumnModelEvent e) {}
		@Override public void columnRemoved(TableColumnModelEvent e) {}
		@Override public void columnMoved(TableColumnModelEvent e) {}
		@Override public void columnMarginChanged(ChangeEvent e) {}
		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyReleased(KeyEvent e) {}
	}
}
