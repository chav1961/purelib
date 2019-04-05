package chav1961.purelib.sql;

import java.sql.SQLException;

public class NullContent extends AbstractContent {
	@Override public boolean isStreaming() {return false;}
	@Override public int getRowCount() {return 0;}
	@Override public int getCurrentRow() {return 0;}
	@Override public boolean setCurrentRow(int row) {return true;}
	@Override public Object[] getRow(int rowNum) {return null;}
	@Override public void close() throws SQLException {}
}