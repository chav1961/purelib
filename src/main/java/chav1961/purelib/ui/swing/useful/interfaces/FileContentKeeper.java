package chav1961.purelib.ui.swing.useful.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface FileContentKeeper {
	boolean hasFileContentNow();
	Collection<File> getFileContent();
	boolean hasSelectedFileContentNow();
	Collection<File> getSelectedFileContent();
	void placeFileContent(Iterable<File> content) throws IOException;
}
