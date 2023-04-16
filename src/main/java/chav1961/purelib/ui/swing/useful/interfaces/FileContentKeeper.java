package chav1961.purelib.ui.swing.useful.interfaces;

import java.awt.Point;
import java.io.IOException;
import java.util.Collection;

import chav1961.purelib.ui.swing.useful.JFileItemDescriptor;
import chav1961.purelib.ui.swing.useful.JFileList;
import chav1961.purelib.ui.swing.useful.JFileTree;

/**
 * <p>This interface describes any visual 'file' content keeper. It usually used by {@linkplain JFileList} and {@linkplain JFileTree}
 * classes</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface FileContentKeeper {
	/**
	 * <p>Does keeper have any file content now</p>
	 * @return true if yes
	 */
	boolean hasFileContentNow();
	
	/**
	 * <p>Get all the file content from the keeper</p>
	 * @return all the file content. Can be empty but not null
	 */
	Collection<JFileItemDescriptor> getFileContent();
	
	/**
	 * <p>Does keeper have any selected file content now</p>
	 * @return true if yes
	 */
	boolean hasSelectedFileContentNow();
	
	/**
	 * <p>Get selected file content from the keeper</p> 
	 * @return selected file content. Can be empty but not null
	 */
	Collection<JFileItemDescriptor> getSelectedFileContent();
	
	/**
	 * <p>Place file content list into the keeper</p> 
	 * @param location location point to place content. Can't be null
	 * @param content content to place. Can be empty but not null
	 * @throws IOException on any I/O errors
	 */
	void placeFileContent(final Point location, Iterable<JFileItemDescriptor> content) throws IOException;
}
