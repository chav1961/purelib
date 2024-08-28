package chav1961.purelib.ui.swing.useful;

import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

class JFreezableTableExample implements ChangeListener, PropertyChangeListener {
	private final JTable 		main;
	private final JTable 		fixed;
	private final JScrollPane	scrollPane;

	/*
	 *  Specify the number of columns to be fixed and the scroll pane
	 *  containing the table.
	 */
	public JFreezableTableExample(final int fixedColumns, final JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		this.main = ((JTable)scrollPane.getViewport().getView());
		
		final int totalColumns = main.getColumnCount();
		
		main.setAutoCreateColumnsFromModel(false);
		main.addPropertyChangeListener(this);

		this.fixed = new JTable();
		
		fixed.setAutoCreateColumnsFromModel(false);
		fixed.setModel( main.getModel() );
		fixed.setSelectionModel(main.getSelectionModel());
		fixed.setFocusable(false);

		//  Remove the fixed columns from the main table
		//  and add them to the fixed table

		for (int i = 0; i < fixedColumns; i++)
		{
	        TableColumnModel columnModel = main.getColumnModel();
	        TableColumn column = columnModel.getColumn( 0 );
    	    columnModel.removeColumn( column );
			fixed.getColumnModel().addColumn( column );
		}

		//  Add the fixed table to the scroll pane

        fixed.setPreferredScrollableViewportSize(fixed.getPreferredSize());
		scrollPane.setRowHeaderView( fixed );
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixed.getTableHeader());

		// Synchronize scrolling of the row header with the main table

		scrollPane.getRowHeader().addChangeListener( this );
	}

	/*
	 *  Return the table being used in the row header
	 */
	public JTable getFixedTable()
	{
		return fixed;
	}
//
//  Implement the ChangeListener
//
	public void stateChanged(ChangeEvent e)
	{
		//  Sync the scroll pane scrollbar with the row header

		JViewport viewport = (JViewport) e.getSource();
		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
	}
//
//  Implement the PropertyChangeListener
//
	public void propertyChange(PropertyChangeEvent e) {
		//  Keep the fixed table in sync with the main table

		if ("selectionModel".equals(e.getPropertyName()))
		{
			fixed.setSelectionModel( main.getSelectionModel() );
		}

		if ("model".equals(e.getPropertyName()))
		{
			fixed.setModel( main.getModel() );
		}
	}
}
