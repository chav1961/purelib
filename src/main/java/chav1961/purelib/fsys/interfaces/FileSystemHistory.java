package chav1961.purelib.fsys.interfaces;

import java.io.IOException;
import java.util.Date;

public interface FileSystemHistory {
	String getId() throws IOException;
	Date getTimestamp() throws IOException;
	String getUser() throws IOException;
	String getDescription() throws IOException;
	
	Iterable<FileSystemHistory> getHistory(final String expression) throws IOException;

	default Iterable<FileSystemHistory> getHistory() throws IOException {
		return getHistory("true");
	}
}
