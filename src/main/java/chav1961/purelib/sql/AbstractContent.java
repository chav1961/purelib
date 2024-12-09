package chav1961.purelib.sql;

import java.sql.SQLException;

/**
 * <p>This class is a container for data content<p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public abstract class AbstractContent implements AutoCloseable {
	/**
	 * <p>Is the content streaming</p>
	 * @return true if yes, false otherwise
	 */
	public abstract boolean isStreaming();
	
	/**
	 * <p>Get number of rows in the content.</p>
	 * @return number of rows.</p>
	 * @throws SQLException on any SQL error
	 */
	public abstract int getRowCount() throws SQLException;
	
	/**
	 * <p>Get number of current row</p>  
	 * @return current tow number
	 * @throws SQLException on any SQL error
	 */
	public abstract int getCurrentRow() throws SQLException;
	
	/**
	 * <p>Select current row in the content.</p>
	 * @param row row to select. Must be inside content.
	 * @return true if selected, false otherwise 
	 * @throws SQLException on any SQL error
	 */
	public abstract boolean setCurrentRow(int row) throws SQLException;
	
	/**
	 * <p>Get row content.</p> 
	 * @param rowNum row number to get content for. Must be inside content.
	 * @return content selected. Can't be neither null nor empty array
	 * @throws SQLException on any SQL error
	 */
	public abstract Object[] getRow(int rowNum) throws SQLException;

	@Override
	public abstract void close() throws SQLException;
}