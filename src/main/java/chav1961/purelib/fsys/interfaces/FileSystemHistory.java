package chav1961.purelib.fsys.interfaces;

import java.io.IOException;
import java.util.Date;

import chav1961.purelib.basic.exceptions.SyntaxException;

/**
 * <p>This interface describes current revision in the file systems that supports revisions of their content</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface FileSystemHistory {
	/**
	 * <p>Get string representation of revision Id</p>
	 * @return string representation of the revision Id. Can't be null or empty. Content of the representation is implementation-specific
	 * @throws IOException on any errors thrown
	 */
	String getId() throws IOException;
	
	/**
	 * <p>Get revision time stamp in UTC.</p>
	 * @return revision time stamp. Can't be null
	 * @throws IOException on any errors thrown
	 */
	Date getTimestamp() throws IOException;
	
	/**
	 * <p>Get user Id committed the revision. Can't be null or empty
	 * @return used Id committed the revision. Can't be null or empty. Content of the user Id is implementation-specific
	 * @throws IOException on any errors thrown
	 */
	String getUser() throws IOException;
	
	/**
	 * <p>Get revision description</p>
	 * @return revision description. Can't be null or empty</p>
	 * @throws IOException on any errors thrown
	 */
	String getDescription() throws IOException;

	/**
	 * <p>Extract revision list by the expression typed.</p>
	 * @param expression expression to extract. Can't be null or empty.
	 * @return revision list extracted. Can be empty but not null
	 * @throws IOException on any errors thrown
	 * @throws SyntaxException on any syntax errors in the expression
	 */
	Iterable<FileSystemHistory> getHistory(final String expression) throws IOException, SyntaxException;

	/**
	 * <p>Extract all revisions</p>
	 * @return revision list extracted. Can be empty but not null
	 * @throws IOException on any errors thrown
	 * @throws SyntaxException on any syntax errors in the expression
	 */
	default Iterable<FileSystemHistory> getHistory() throws IOException, SyntaxException {
		return getHistory("true");
	}
}
