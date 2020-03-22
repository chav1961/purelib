package chav1961.purelib.ui.swing;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class JDBCursorTableModel extends DefaultTableModel {
	private static final long 				serialVersionUID = -818078791735060202L;
	
	private final ContentMetadataInterface	mdi;
	private final ResultSet					rs;
	private final boolean					readOnly;
	
	public JDBCursorTableModel(final ContentMetadataInterface mdi, final ResultSet rs, final boolean readOnly) {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (rs == null) {
			throw new NullPointerException("ResultSet can't be null");
		}
		else {
			this.mdi = mdi;
			this.rs = rs;
			this.readOnly = readOnly;
			
			final Set<String>	modelNames = new HashSet<>(), cursorNames = new HashSet<>();
		}
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}
}
