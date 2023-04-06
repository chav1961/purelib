package chav1961.purelib.fsys.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>This interface describes a data wrapper to get information about the file system entity.</p>
 * <p>This interface is a result for the {@link FileSystemInterface#createDataWrapper(URI) createDataWrapper(URI)} call in the {@link FileSystemInterface IFileSystem} interface.
 * It uses to describe properties and get access to content of the given file system entity. Association between this wrapper and file system entity is defined by 
 * the URL parameter in the {@link FileSystemInterface#createDataWrapper(URI) createDataWrapper(URI)} method. As an implementation example, see 
 * {@link chav1961.purelib.fsys.FileSystemOnFile FileSystemOnFile} class</p> 
 * 
 * @see chav1961.purelib.fsys.AbstractFileSystem AbstractFileSystem class  
 * @see chav1961.purelib.fsys.FileSystemOnFile FileSystemOnFile class  
 *   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.7
 */

public interface DataWrapperInterface extends FileSystemLockInterface {

	public static final String ATTR_SIZE = "size";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_ALIAS = "alias";
	public static final String ATTR_LASTMODIFIED = "lastModified";
	public static final String ATTR_DIR = "dir";
	public static final String ATTR_EXIST = "exist";
	public static final String ATTR_CANREAD = "canRead";
	public static final String ATTR_CANWRITE = "canWrite";

	/**
	 * <p>Get URI list for the current 
	 * @param pattern pattern to filter file system entity names<p>
	 * @return URI list from the given file system cursor. Can be empty but not null. Each URI need be started with '/', but need be relative to the {@link FileSystemInterface#createDataWrapper(URI) createDataWrapper(URI)} URI. 
	 * @throws IOException if any exceptions was thrown
	 */
	URI[] list(Pattern pattern) throws IOException;

	/**
	 * <p>Create directory for the given file system cursor location</p>
	 * @throws IOException if any exceptions was thrown
	 */
	void mkDir() throws IOException;

	/**
	 * <p>Create empty file for the given file system cursor location</p>
	 * @throws IOException if any exceptions was thrown
	 */
	void create() throws IOException;

	/**
	 * <p>Change name of the given file system cursor entity</p>
	 * @param name new name to set
	 * @throws IOException if any exceptions was thrown
	 */
	void setName(String name) throws IOException;

	/**
	 * <p>Delete the given file system cursor entity. You don't need implement recursive deletion for subdrirectories!</p>
	 * @throws IOException if any exceptions was thrown
	 */
	void delete() throws IOException;

	/**
	 * <p>Create an output stream to change or append date into file system cursor entity content</p>
	 * @param append true if data will be appended
	 * @return output stream to write data to the given content. You need override the 
	 * {@link java.io.OutputStream#close() close()} method of the stream to commit store operation in your file system   
	 * @throws IOException if any exceptions was thrown
	 */
	OutputStream getOutputStream(boolean append) throws IOException;

	/**
	 * <p>Create an input stream to get content from the file system cursor entity.</p> 
	 * @return stream to read content from. 
	 * @throws IOException if any exceptions was thrown
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * <p>Get attributes of the given file system cursor entity.</p>
	 * @return entity attributes. Need be not null and contain at least ATTR_EXIST key 
	 * @throws IOException if any exceptions was thrown
	 */
	Map<String, Object> getAttributes() throws IOException;

	/**
	 * <p>Associate attributes with the next {@link #getInputStream() getInputStream()} or {@link #getOutputStream(boolean) getOutputStream(boolean)} operation. This map can be empty. 
	 * You can use it as to get data from it so put data into it. It's exactly the map used in the read/write and reaChar/writeChar methods family. System guarantees that 
	 * this method always call before appropriative calling {@link #getInputStream() getInputStream()} or {@link #getOutputStream(boolean) getOutputStream(boolean)}.</p>
	 * @param attributes entity attributes. 
	 * @throws IOException if any exceptions was thrown
	 */
	void linkAttributes(Map<String, Object> attributes) throws IOException;
}