package chav1961.purelib.sql;

import java.sql.SQLException;
import java.util.Arrays;

public class StreamContent extends AbstractContent {
	private final Object[]		currentData;
	private final RowGetter		getter;
	private final StreamCloser	closer;
	private int					currentRow = 0;
	private boolean				endOfData = false;
	
	@FunctionalInterface
	public interface RowGetter {
		boolean next(Object[] content) throws SQLException;
	}

	@FunctionalInterface
	public interface StreamCloser {
		void close() throws SQLException;
	}
	
	public StreamContent(final Object[] currentData, final RowGetter getter, final StreamCloser closer) {
		if (currentData == null || currentData.length == 0) {
			throw new IllegalArgumentException("Current row data can't be null or empty array");
		}
		else if (getter == null) {
			throw new NullPointerException("Row getter can't be null");
		}
		else if (closer == null) {
			throw new NullPointerException("Stream closer can't be null");
		}
		else {
			this.currentData = currentData;
			this.getter = getter;
			this.closer = closer;
		}
	}

	@Override
	public void close() throws SQLException {
		closer.close();
	}
	
	@Override
	public boolean isStreaming() {
		return true;
	}

	@Override
	public int getRowCount() throws SQLException {
		return endOfData ? 0 : currentRow+1;
	}

	@Override
	public int getCurrentRow() throws SQLException {
		return currentRow;
	}

	@Override
	public boolean setCurrentRow(int row) throws SQLException {
		if (row < 1 || row > getRowCount()) {
			throw new IllegalArgumentException("Row number ["+row+"] out of range 1.."+getRowCount());
		}
		else if (row != getCurrentRow()+1) {
			throw new SQLException("Forward-only cursor doesn't support any moving except next() [current row = "+getCurrentRow()+", new row = "+row+"]");
		}
		else {
			this.currentRow = row;
			return this.endOfData = !getter.next(currentData);
		}
	}

	@Override
	public Object[] getRow(int rowNum) throws SQLException {
		return currentData;
	}

	@Override
	public String toString() {
		return "StreamContent [currentData=" + Arrays.toString(currentData) + ", currentRow=" + currentRow + ", endOfData=" + endOfData + "]";
	}
}
