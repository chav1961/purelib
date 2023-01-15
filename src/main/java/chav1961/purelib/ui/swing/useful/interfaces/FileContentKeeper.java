package chav1961.purelib.ui.swing.useful.interfaces;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface FileContentKeeper {
	boolean hasFileContentNow();
	Collection<File> getFileContent();
	boolean hasSelectedFileContentNow();
	Collection<File> getSelectedFileContent();
	void placeFileContent(final Point location, Iterable<File> content) throws IOException;
}
