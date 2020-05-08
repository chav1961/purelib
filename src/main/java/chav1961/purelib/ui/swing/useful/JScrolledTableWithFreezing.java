package chav1961.purelib.ui.swing.useful;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class JScrolledTableWithFreezing extends JScrollPane {
	private static final long serialVersionUID = 1182315446556348450L;

	private final SplittedTableModel	sptm;
	private final JTable				freezedT, freeT;
	private volatile boolean			freezedLeader = true;
	
	public JScrolledTableWithFreezing(final TableModel model, final int freezedColumns) {
		this.sptm = new SplittedTableModel(model,freezedColumns);
		this.freezedT = new JTable(sptm.getFreezedTableModel());
		this.freeT = new JTable(sptm.getFreeTableModel());
		freezedT.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		freeT.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		freezedT.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {}
			@Override 
			public void focusGained(FocusEvent e) {
				freezedLeader = true;
			}
		});
		freeT.addFocusListener(new FocusListener() {
			@Override public void focusLost(FocusEvent e) {}
			@Override 
			public void focusGained(FocusEvent e) {
				freezedLeader = false;
			}
		});
		freezedT.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (freezedLeader) {
					final int	index = ((DefaultListSelectionModel)e.getSource()).getLeadSelectionIndex();
					
					freeT.setRowSelectionInterval(index,index);
				}
			}
		});
		freezedT.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			
			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				System.err.println("SDAKSDJASJKLDJ");
			}
			
			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void columnMoved(TableColumnModelEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void columnMarginChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		freeT.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!freezedLeader) {
					final int	index = ((DefaultListSelectionModel)e.getSource()).getLeadSelectionIndex();
					
					freezedT.setRowSelectionInterval(index,index);
				}
			}
		});
		freezedT.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		freezedT.setPreferredScrollableViewportSize(freezedT.getPreferredSize());
		freeT.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setCorner(JScrollPane.UPPER_LEFT_CORNER,freezedT.getTableHeader());
		setRowHeaderView(freezedT);		
		setViewportView(freeT);
	}
	
	public JTable getFreezedPart() {
		return null;
	}

	public JTable getFreePart() {
		return null;
	}
	
	public int getFreezedColumns() {
		return 0;
	}
	
	public void setFreezedColumns() {
		
	}

	private static class SplittedTableModel {
		private final TableModel	nested;
		private final int			freezedColumns;
		private final TableModel	freezed, free;
		
		public SplittedTableModel(final TableModel nested, final int freezedColumns) {
			this.nested = nested;
			this.freezedColumns = freezedColumns;
			this.freezed = new FreezedTM();
			this.free = new FreeTM();
		}
		
		public TableModel getFreezedTableModel() {
			return freezed;
		}

		public TableModel getFreeTableModel() {
			return free;
		}
		
		private class FreezedTM extends DefaultTableModel {
			private static final long serialVersionUID = 3118377209290110119L;

			@Override
			public void fireTableChanged(TableModelEvent e) {
				if (nested instanceof DefaultTableModel) {
					((DefaultTableModel)nested).fireTableChanged(e);
				}
				else {
					super.fireTableChanged(e);
				}
			}
			
			@Override
			public int getRowCount() {
				return nested.getRowCount();
			}

			@Override
			public int getColumnCount() {
				return freezedColumns;
			}

			@Override
			public String getColumnName(int columnIndex) {
				return nested.getColumnName(columnIndex);
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return nested.getColumnClass(columnIndex);
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return nested.isCellEditable(rowIndex, columnIndex);
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return nested.getValueAt(rowIndex, columnIndex);
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				nested.setValueAt(aValue, rowIndex, columnIndex);
			}

			@Override
			public void addTableModelListener(TableModelListener l) {
				nested.addTableModelListener(l);
			}

			@Override
			public void removeTableModelListener(TableModelListener l) {
				nested.removeTableModelListener(l);
			}
		}
		
		private class FreeTM extends DefaultTableModel {
			private static final long serialVersionUID = -8284486541358431828L;

			@Override
			public void fireTableChanged(TableModelEvent e) {
				if (nested instanceof DefaultTableModel) {
					((DefaultTableModel)nested).fireTableChanged(new TableModelEvent((TableModel)e.getSource(),e.getFirstRow(),e.getLastRow(),Math.max(0,e.getColumn()+freezedColumns),e.getType()));
				}
				else {
					super.fireTableChanged(e);
				}
			}
			
			@Override
			public int getRowCount() {
				return nested.getRowCount();
			}

			@Override
			public int getColumnCount() {
				return nested.getColumnCount()-freezedColumns;
			}

			@Override
			public String getColumnName(int columnIndex) {
				return nested.getColumnName(columnIndex+freezedColumns);
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return nested.getColumnClass(columnIndex+freezedColumns);
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return nested.isCellEditable(rowIndex, columnIndex+freezedColumns);
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return nested.getValueAt(rowIndex, columnIndex+freezedColumns);
			}

			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				nested.setValueAt(aValue, rowIndex, columnIndex+freezedColumns);
			}

			@Override
			public void addTableModelListener(TableModelListener l) {
				nested.addTableModelListener(l);
			}

			@Override
			public void removeTableModelListener(TableModelListener l) {
				nested.removeTableModelListener(l);
			}
		}
	}
}
