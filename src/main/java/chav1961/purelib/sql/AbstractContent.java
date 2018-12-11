package chav1961.purelib.sql;

public abstract class AbstractContent {
	public abstract int getRowCount();
	public abstract int getCurrentRow();
	public abstract void setCurrentRow(int row);
	public abstract Object[] getRow(int rowNum);
}