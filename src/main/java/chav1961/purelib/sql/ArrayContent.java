package chav1961.purelib.sql;

import java.sql.SQLException;
import java.util.Arrays;

public class ArrayContent extends AbstractContent {
	private final Object[][]	data;
	private int					currentRow = 0;
	
	public ArrayContent(final Object[]... data) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Data can't be null or empty array");
		}
		else {
			this.data = data;
		}
	}

	@Override
	public boolean isStreaming() {
		return false;
	}
	
	@Override
	public int getRowCount() throws SQLException {
		return data.length;
	}

	@Override
	public int getCurrentRow() throws SQLException {
		return currentRow;
	}

	@Override
	public boolean setCurrentRow(final int row) throws SQLException {
		if (row < 1 || row > getRowCount()) {
			throw new IllegalArgumentException("Row number ["+row+"] out of range 1.."+getRowCount());
		}
		else {
			this.currentRow = row;
			return true;
		}
	}

	@Override
	public Object[] getRow(final int row) throws SQLException {
		if (row < 1 || row > getRowCount()) {
			throw new IllegalArgumentException("Row number ["+row+"] out of range 1.."+getRowCount());
		}
		else {
			return data[row-1];
		}
	}

	@Override
	public void close() throws SQLException {
		Arrays.fill(data,null);
	}
	
	@Override
	public String toString() {
		try{final StringBuilder				sb = new StringBuilder("ArrayContent = [size="+getRowCount()+", current="+getCurrentRow()+", data:\n");
		
			for (Object[] item : data) {
				sb.append(Arrays.toString(item)).append('\n');
			}
			sb.append(']');
			return sb.toString();
		} catch (SQLException e) {
			return super.toString();
		}
	}
}
