package chav1961.purelib.sql;

import java.sql.SQLException;
import java.util.Iterator;

public class IteratorContent extends AbstractContent {
	private final Iterator<Object[]>	iterator;
	private Object[]					content;
	int		count = 0;
	
	public IteratorContent(final Iterator<Object[]> iterator) {
		this.iterator = iterator;
	}
	
	
	@Override
	public boolean isStreaming() {
		return true;
	}

	@Override
	public int getRowCount() throws SQLException {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getCurrentRow() throws SQLException {
		return count;
	}

	@Override
	public boolean setCurrentRow(final int row) throws SQLException {
		if (row == count + 1) {
			count = row;
			if (iterator.hasNext()) {
				content = iterator.next();
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public Object[] getRow(int rowNum) throws SQLException {
		if (rowNum == getCurrentRow()) {
			return content;
		}
		else {
			return null;
		}
	}

	@Override
	public void close() throws SQLException {
		while (iterator.hasNext()) {
			iterator.next();
		}
	}
}
