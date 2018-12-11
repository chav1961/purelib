package chav1961.purelib.sql;

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
	public int getRowCount() {
		return data.length;
	}

	@Override
	public int getCurrentRow() {
		return currentRow;
	}

	@Override
	public void setCurrentRow(final int row) {
		if (row < 1 || row > getRowCount()) {
			throw new IllegalArgumentException("Row number ["+row+"] out of range 1.."+getRowCount());
		}
		else {
			this.currentRow = row;
		}
	}

	@Override
	public Object[] getRow(final int row) {
		if (row < 1 || row > getRowCount()) {
			throw new IllegalArgumentException("Row number ["+row+"] out of range 1.."+getRowCount());
		}
		else {
			return data[row-1];
		}
	}
	
	@Override
	public String toString() {
		final StringBuilder	sb = new StringBuilder("ArrayContent = [size="+getRowCount()+", current="+getCurrentRow()+", data:\n");
		
		for (Object[] item : data) {
			sb.append(Arrays.toString(item)).append('\n');
		}
		sb.append(']');
		return sb.toString();
	}
}
