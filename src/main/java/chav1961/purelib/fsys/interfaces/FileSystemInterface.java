package chav1961.purelib.fsys.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.FileSystemFactory;

/**
 * <p>This interface describes an abstract file system. It uses to unify access to the any data hierarchical structures can be used to keep and manipulate persistent data</p>
 * <p>As the usual file system, the {@link FileSystemInterface FileSystemInterface} interface describes a data tree containing a set of <i>folders</i> and <i>files</i>. There is one <i>root</i> folder in the file system,
 * that is a parent of all other entities. You can locate in any point of the tree, but in the only point for every time. Point of your current location in the file
 * system is named <i>cursor</i>. Any operations on the file system always happen in the cursor point only. You can change the cursor location in the tree by using 
 * {@link #open(java.lang.String) open(String)} method. Note, that file systems, that are supported revisions, can process {@link #open(java.lang.String) open(String)} method argument in the form
 * "file_path#revision_id", where revision_id is an Id of the revision in the implementation-specific form. Current cursor location defines by it's <i>path</i> (as in the usual file system), and can be defined by
 * {@link #getPath() getPath()} method. The same last name of this path can be read by {@link #getName() getName()} method. Both methods must concatenate revision id to the result, when the one was typed for 
 * {@link #open(java.lang.String) open(String)} method argument.</p>
 * 
 * <p>When you walk on the data tree by using {@link #open(java.lang.String) open(String)}, you can also point the cursor to non-existent paths. If you do it, you can use 
 * {@link #exists() exists()} method to test existence of this path, and {@link #isFile() isFile()} / {@link #isDirectory() isDirectory()} methods to define kind of location point
 * of the cursor.</p>
 * 
 * <p>To create a new entity in the file system, you need firstly point the cursor to path you want (path can be existent or not), and call one of two methods:</p>
 * <ul>
 * <li>{@link #mkDir() mkDir()} to create a path with directory as the same last element</li>
 * <li>{@link #create() create()} to create a path with empty file as the same last element</li>
 * </ul>
 * <p>Names of the newly created entities are the same, as {@link #getName() getName()} method returns.</p>
 * <p>To remove any entity in the file system, you need firstly point the cursor to it, and call one of two methods:</p>  
 * <ul>
 * <li>{@link #delete() delete()} to delete file or empty directory</li>
 * <li>{@link #deleteAll() deleteAll()} to delete non-empty directory</li>
 * </ul>
 * <p>To copy or move file system content from one file system to another, you need firstly point the cursor to the leaf or subtree of source file system, and call one of two methods:</p>
 * <ul>
 * <li>{@link #copy(FileSystemInterface) copy(IFileSystem)} to copy leaf or subtree content</li>
 * <li>{@link #move(FileSystemInterface) move(IFileSystem)} to move leaf or subtree content</li>
 * </ul>
 * <p>Data from source file system will be copied/moved to the current location of target file system cursor, so use {@link #open(java.lang.String) open(String)} method for it to set the 
 * cursor to the location you wish. If you wish to copy/move data to the same file system, you need create the <i>copy</i> of you file system by calling {@link #clone() clone()} method.
 * Don't use the same file system directly in the copy/move methods because file system cursor is not a reentrant resource</p>
 * 
 * <p>Specific powerful ability of the FileSystemInterface is to <b>mount</b> one file system inside another. This functionality is similar to Linux mount/umount console commands and allow you to build
 * heterogenous file systems in your programs and work with them transparently. File system to mount is always mounting into the current cursor location of the owned file system. You can use:</p> 
 * <ul>
 * <li>{@link #mount(FileSystemInterface) mount(IFileSystem)} to mount new file system into the current cursor location</li>
 * <li>{@link #unmount() unmount()} to unmount file system from current cursor location</li>
 * </ul>
 * 
 * <p>Use a family of read()/readChar() methods to get content of the files in the file system, and a family of write()/wroteChar() methods to change it. Any input/output streams you
 * got from the file system need be closed by yourself, so use <b>try-with-resource</b> for processing it</p> 
 * 
 * <p>Every entity in the file system has a set of <i>attributes</i> associated with it. Mandatory attributes of all entities are:</p>
 * <ul>
 * <li> entity name (can be read by {@link #getName() getName()} method and can be changed by {@link #rename(java.lang.String) rename(String)} method)</li>
 * <li> entity size in bytes (can be read by {@link #size() size()} method and is valid for files only)</li>
 * <li> last modification time of the entity (can be read by {@link #lastModified() lastModified()} method</li>
 * </ul>
 * 
 * <p>All other attributes are optional and depend of the file system type. They can be read by {@link #getAttributes() getAttributes()} method and can be changed by 
 * {@link #setAttributes(java.util.Map) setAttributes(Map)} method. You also can make the same operations during read/write data from/to the file</p>
 * 
 *  <p>The most of all the methods return a {@linkplain FileSystemInterface} as a result. This result is a <b>'this'</b> reference (except {@link #unmount() unmount()} method), so you can use the interface in the 
 *  chained calls (for example <code>myFileSystem.open("something").create().write()</code>).</p>
 *  <p>Transactional file systems</p>
 *  <p>Transactional file systems can be used to support any transactions with the file systems. Typical usage is file systems that supports revisions.</p> 
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.7
 */

public interface FileSystemInterface extends Cloneable, Closeable, SpiService<FileSystemInterface>, FileSystemLockInterface {
	/**
	 * <p>URI scheme for all the FileSystemInterface implementations</p>
	 */
	public static final String		FILESYSTEM_URI_SCHEME = "fsys";	
	
	/**
	 * <p>This interface uses as callback for processing directories content (see {@link #list(FileSystemListCallbackInterface) list(IFileSystemListCallback)} and {@link #list(java.lang.String,FileSystemListCallbackInterface) list(String,IFileSystemListCallback)}).
	 * This interface is lambda-oriented</p>
	 * 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	@FunctionalInterface
	public interface FileSystemListCallbackInterface {
		/**
		 * <p>Process IFileSystem item</p>
		 * @param item IFileSystem item for one of the directory content items
		 * @throws IOException if any exception was detected during processing
		 */
		ContinueMode process(FileSystemInterface item) throws IOException;
	}
	
	/**
	 * <p>Get file/directory name for the file system cursor</p>
	 * @return file/directory name. Can't be neither null nor empty. Root returns "/" instead of name
	 * @throws IOException if any exceptions was thrown
	 */
	String getName() throws IOException;

	/**
	 * <p>Get file/directory alias for the file system cursor</p>
	 * @return file/directory alias. Can't be neither null nor empty. Root returns "/" instead of alias
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.7
	 */
	String getAlias() throws IOException;
	
	/**
	 * <p>Get actual path for the file system cursor</p>
	 * @return actual path. Can't be neither null nor empty. Root returns "/" as name
	 * @throws IOException if any exceptions was thrown
	 */
	String getPath() throws IOException;
	
	/**
	 * <p>Convert current location to URI format</p>  
	 * @return URI for the current location
	 * @throws IOException on any I/O errors
	 */
	URI toURI() throws IOException;
	
	/**
	 * <p>Is the file/directory cursor exists</p>
	 * @return true if exists
	 * @throws IOException if any exceptions was thrown
	 */
	boolean exists() throws IOException;
	
	/**
	 * <p>Is the cursor a file</p>
	 * @return true if yes
	 * @throws IOException if any exceptions was thrown
	 */
	boolean isFile() throws IOException;
	
	/**
	 * <p>Is the cursor a directory</p>
	 * @return true if yes
	 * @throws IOException if any exceptions was thrown
	 */
	boolean isDirectory() throws IOException;
	
	/**
	 * <p>Can read data form the file/directory</p>
	 * @return true if yes
	 * @throws IOException if any exceptions was thrown
	 */
	boolean canRead() throws IOException;
	
	/**
	 * <p>Can write data to the file/directory</p>
	 * @return true if yes
	 * @throws IOException if any exceptions was thrown
	 */
	boolean canWrite() throws IOException;

	/**
	 * <p>Get last modified time for the directory/
	 * @return last modified date in milliseconds since 01.01.1970
	 * @throws IOException if any exceptions was thrown
	 */
	long lastModified() throws IOException;
	
	/**
	 * <p>Get size of the file.</p>
	 * @return file returns it's size, directory always returns o
	 * @throws IOException if any exceptions was thrown
	 */
	long size() throws IOException;
	
	/**
	 * <p>Change file system cursor to the given location</p>
	 * @param path new path (as relative, so absolute).
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface open(String path) throws IOException;

	/**
	 * <p>Push current file system cursor state and change it's location. Uses as pair for {@link #pop() pop()} method</p>
	 * @param path path to change
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface push(String path) throws IOException;

	/**
	 * <p>Restore file system cursor state saved by {@link #push(java.lang.String) push(String)} method.</p>
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface pop() throws IOException;
	
	/**
	 * <p>Process content of the directory</p>
	 * @param callback callback to be called on each entity in the given director
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface list(FileSystemListCallbackInterface callback) throws IOException;

	/**
	 * <p>Get list of all names in the given directory</p>
	 * @return list of all names in the given directory. Can be empty but not null
	 * @throws IOException if any exceptions was thrown
	 */
	String[] list() throws IOException;
	
	/**
	 * <p>Process content of the directory with the name filter</p>
	 * @param mask content entity names to process. Regular expression as describes for {@link java.util.regex.Pattern Pattern} class  
	 * @param callback callback to be called on each entity in the given directory
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface list(String mask, FileSystemListCallbackInterface callback) throws IOException;

	/**
	 * <p>Get list of all names in the given directory filtered by name</p>
	 * @param mask content entity names to process. Regular expression as describes for {@link java.util.regex.Pattern Pattern} class  
	 * @return list of all names in the given directory. Can be empty but not null
	 * @throws IOException if any exceptions was thrown
	 */
	String[] list(String mask) throws IOException;
	
	/**
	 * <p>Create directory for the given file system cursor</p>
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface mkDir() throws IOException;
	
	/**
	 * <p>Create empty file for the given cursor</p>
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface create() throws IOException;
	
	/**
	 * <p>Start appending write operation on the given file</p>
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface append() throws IOException;
	
	/**
	 * <p>Rename file/directory</p>
	 * @param name new name for the file/directory
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface rename(String name) throws IOException;
	
	/**
	 * <p>Delete file or empty directory</p>
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface delete() throws IOException;
	
	/**
	 * <p>Delete file or non-empty directory</p>
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface deleteAll() throws IOException;
	
	/**
	 * <p>Mount another filesystem to the given file system cursor location</p>
	 * @param another another filesystem to mount
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 * @see #isMound()
	 * @see #unmount()
	 * @see #mound()
	 */
	FileSystemInterface mount(FileSystemInterface another) throws IOException;
	
	/**
	 * <p>Unmount another file system from the given point</p>
	 * @return file system was unmounted from the actual file system cursor location
	 * @throws IOException if any exceptions was thrown
	 * @see #isMound()
	 * @see #mount(FileSystemInterface)
	 * @see #mound()
	 */
	FileSystemInterface unmount() throws IOException;

	/**
	 * <p>Check weather given node has children mound</p>
	 * @return true if has
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.3
	 * @see #mount(FileSystemInterface)
	 * @see #unmount()
	 * @see #mound()
	 */
	boolean isMound() throws IOException;

	/**
	 * <p>Get file system mound for the given node</p>
	 * @return file systems mound or null if none mound on the node
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.3
	 * @see #mount(FileSystemInterface)
	 * @see #unmount()
	 * @see #isMound()
	 */
	FileSystemInterface mound() throws IOException;
	
	/**
	 * <p>Join new file system with the current directory. New file system is always joining <i>after</i> current. 
	 * Multiple joins forms a <i>queue</i> of the file systems behind the current one.</p>
	 * @param another another file system
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.3
	 * @see #unjoin()
	 * @see #isJoined()
	 * @see #joinedList()
	 */
	FileSystemInterface join(FileSystemInterface another) throws IOException;

	/**
	 * <p>Unjoin file system from the current directory. All the queue of joined file systems will be returned after the call,
	 * so you must not call this method in loops</p>
	 * @return all the queue of file system joined
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.3
	 * @see #join(FileSystemInterface)
	 * @see #isJoined()
	 * @see #joinedList()
	 */
	FileSystemInterface unjoin() throws IOException;

	/**
	 * <p>Has the current directory any joined file systems.</p> 
	 * @return true if has. Calling this method on any children of the current directory with joins always returns false   
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.3
	 * @see #join(FileSystemInterface)
	 * @see #unjoin()
	 * @see #joinedList()
	 */
	boolean isJoined() throws IOException;

	/**
	 * <p>Get list of joined file systems from the current directory</p>
	 * @return list (stack) of joined file systems. Can be empty but not null 
	 * @throws IOException if any exceptions was thrown
	 * @see #join(FileSystemInterface)
	 * @see #unjoin()
	 * @see #isJoined()
	 */
	FileSystemInterface[] joinedList() throws IOException;
	
	/**
	 * <p>Copy content from one file system to another</p>
	 * @param another target file system to copy to. Copying makes to the actual getPath() for the target file system 
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface copy(FileSystemInterface another) throws IOException;
	
	/**
	 * <p>Move content from one file system to another</p>
	 * @param another target file system to move to. Copying makes to the actual getPath() for the target file system 
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface move(FileSystemInterface another) throws IOException;
	
	/**
	 * <p>Copy file content from current file system cursor to the given output stream</p>
	 * @param stream stream to copy content to
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface copy(OutputStream stream) throws IOException;
	
	/**
	 * <p>Copy file content from current file system cursor to the given output stream writer</p>
	 * @param stream stream to copy content to. Default encoding 'UTF-8' is used to get data. 
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface copy(Writer stream) throws IOException;
	
	/**
	 * <p>Copy file content from current file system cursor to the given output stream writer using the given encoding</p>
	 * @param stream stream to copy content to
	 * @param encoding source content encoding
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface copy(Writer stream, String encoding) throws IOException;

	/**
	 * <p>et attributes associated with the given file/directory</p>
	 * @return attributes associated. Can be empty but not null
	 * @throws IOException if any exceptions was thrown
	 */
	Map<String,Object> getAttributes() throws IOException;

	/**
	 * <p>Read content of the file from the current file system cursor</p>
	 * @return input stream to get access to the file content. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement
	 * @throws IOException if any exceptions was thrown
	 */
	InputStream read() throws IOException;

	/**
	 * <p>Read content of the file from the current file system cursor and fill the map with the file attributes</p>
	 * @param attributes map to fill attributes. Method fills the map, but not clears it before processing
	 * @return input stream to get access to the file content. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	InputStream read(Map<String,Object> attributes) throws IOException;
	
	/**
	 * <p>Read content of the file from the current file system cursor as character stream</p>
	 * @return input stream to get access to the file content. Default encoding 'UTF-8' is used to get data. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement
	 * @throws IOException if any exceptions was thrown
	 */
	Reader charRead() throws IOException;
	
	/**
	 * <p>Read content of the file from the current file system cursor as character stream, and fill the map with the file attributes</p>
	 * @param attributes map to fill attributes. Method fills the map, but not clears it before processing
	 * @return input stream to get access to the file content. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	Reader charRead(Map<String,Object> attributes) throws IOException;

	/**
	 * <p>Read content of the file from the current file system cursor as character stream with the given encoding</p>
	 * @param encoding source content encoding
	 * @return input stream to get access to the file content. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement
	 * @throws IOException if any exceptions was thrown
	 */
	Reader charRead(String encoding) throws IOException;

	/**
	 * <p>Read content of the file from the current file system cursor as character stream, and fill the map with the file attributes</p>
	 * @param attributes map to fill attributes. Method fills the map, but not clears it before processing
	 * @param encoding source content encoding
	 * @return input stream to get access to the file content. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	Reader charRead(Map<String,Object> attributes,String encoding) throws IOException;

	/**
	 * <p>Set the file/directory attributes</p>
	 * @param attributes map to set attributes. 
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface setAttributes(Map<String,Object> attributes) throws IOException;

	/**
	 * <p>Set the file/directory attributes</p>
	 * @param keyValuePairs list of pairs String/Object to set attributes (see)
	 * @return self
	 * @throws IOException if any exceptions was thrown
	 */
	FileSystemInterface setAttributes(Object... keyValuePairs) throws IOException;

	/**
	 * <p>Get stream to write content of the file</p>
	 * @return stream to write content to. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	OutputStream write() throws IOException;
	
	/**
	 * <p>Get stream to write content of the file and set the file/directory attributes</p>
	 * @param attributes map to set attributes. 
	 * @return stream to write content to. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	OutputStream write(Map<String,Object> attributes) throws IOException;

	/**
	 * <p>Get character stream to write content of the file</p>
	 * @return stream to write content to. Default encoding 'UTF-8' is used to put data. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	Writer charWrite() throws IOException;
	
	/**
	 * <p>Get character stream to write content of the file and set the file/directory attributes</p>
	 * @param attributes map to set attributes. 
	 * @return stream to write content to. Default encoding 'UTF-8' is used to put data. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	Writer charWrite(Map<String,Object> attributes) throws IOException;

	/**
	 * <p>Get character stream to write content of the file with the given encoding</p>
	 * @param encoding target content encoding
	 * @return stream to write content to. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	Writer charWrite(String encoding) throws IOException;
	
	/**
	 * <p>Get character stream to write content of the file and set the file/directory attributes</p>
	 * @param attributes map to set attributes. 
	 * @param encoding target content encoding
	 * @return stream to write content to. You need close this stream after processing, so let's use it in the <b>try-with-resource</b> statement 
	 * @throws IOException if any exceptions was thrown
	 */
	Writer charWrite(Map<String,Object> attributes,String encoding) throws IOException;

	/**
	 * <p>Clone file system description</p>
	 * @return this object clone. Clones all properties including file system mound.
	 */
	FileSystemInterface clone();

	/**
	 * <p>Get data wrapper interface for the given file system cursor path</p>
	 * @param actualPath path toget data wrapper for. Always is relative, normalized and always starts with the '/'.
	 * @return data wrapper for the actual path. Never can be null. If object with the given path is not exists, you need at least fill the ATR_EXISTS 
	 * attribute for the {@link DataWrapperInterface#getAttributes() getAttributes()} method call.    
	 * @throws IOException if any exceptions was thrown
	 */
	DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException;

	/**
	 * <p>Get absolute URI for the current location inside file system. URI will be a concatenation of the absolute URI and path of current location point</p> 
	 * @return absolute URI. Can't be null
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.4
	 */
	URI getAbsoluteURI() throws IOException;

	/**
	 * <p>Get options supported by the file system. Options list is implementation-specific, but "REVISIONS"=&gt;"true"/"false" is strongly recommended for file systems that supports revisions.</p>
	 * @return options supported. Can be empty but not null.
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.7
	 */
	default Properties getOptionsSupported() throws IOException {
		return new Properties();
	}
	
	/**
	 * <p>Is the file system identical to another file system and does it points to the same location in it.</p>
	 * @param another another system to compare
	 * @return true if file systems are identical and points to the same location now
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.4
	 */
	boolean isTheSame(FileSystemInterface another) throws IOException;

	/**
	 * <p>Build unique name in the current directory</p>
	 * @param prefix prefix of unique name. Can't be null but can be empty
	 * @param suffix suffix of unique name. Can't be null but can be empty
	 * @return unique name built. Can't be null or empty
	 * @throws IOException on any I/O errors or create unique name on the file
	 * @since 0.0.7
	 */
	String createUniqueName(String prefix, String suffix) throws IOException;
	
	/**
	 * <p>Start transaction on the file system</p>
	 * @param parameters transaction parameters. Can be empty but not null
	 * @return transaction interface. Can be the same as the current one
	 * @throws IOException if any exceptions was thrown
	 * @since 0.0.7
	 */
	default FileSystemInterface startTransaction(final Object... parameters) throws IOException {
		return this;
	}

	/**
	 * <p>Commit transaction started</p>
	 * @param parameters commit parameters.  Can be empty but not null
	 * @throws IOException if any exceptions was thrown
	 * @throws IllegalStateException if no transactions was started
	 * @since 0.0.7
	 */
	default void commit(final Object... parameters) throws IOException, IllegalStateException {		
	}
	
	/**
	 * <p>Rollback transaction started</p>
	 * @throws IOException if any exceptions was thrown
	 * @throws IllegalStateException if no transactions was started
	 * @since 0.0.7
	 */
	default void rollback() throws IOException, IllegalStateException {
	}

	/**
	 * <p>Is the interface in the transaction mode</p>
	 * @return true if yes
	 */
	default boolean isInTransaction() {
		return false;
	}
	
	/**
	 * <p>This class is a factory to get File system by it's URI. It implements a 'Factory' template and wraps call to {@linkplain FileSystemFactory#createFileSystem(URI)}</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.6
	 */
	public final static class Factory {
		private Factory() {}

		/**
		 * <p>Get localizer by URI.</p> 
		 * @param fsiUri localizer URI to get File system for. Can'tbe null and must have scheme {@value FileSystemInterface#FILESYSTEM_URI_SCHEME}
		 * @return file system created
		 * @throws IllegalArgumentException when file system URI is null or doesn't have {@value FileSystemInterface#FILESYSTEM_URI_SCHEME} scheme
		 * @throws IOException on any errors on creation file system
		 */
		public static FileSystemInterface newInstance(final URI fsiUri) throws IllegalArgumentException, IOException{
			if (fsiUri == null || !FILESYSTEM_URI_SCHEME.equals(fsiUri.getScheme())) {
				throw new IllegalArgumentException("Filesystem URI can't be null and must have scheme ["+FILESYSTEM_URI_SCHEME+"]"); 
			}
			else {
				return FileSystemFactory.createFileSystem(fsiUri);
			}
		}
	}
}
