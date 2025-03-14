package chav1961.purelib.fsys.interfaces;

import java.io.IOException;

/**
 * <p>This interface describes file system locker. It is used to lock/unlock entites in the file system</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.5
 */
public interface FileSystemLockInterface {
	/**
	 * <p>Try to lock file system resource.</p>
	 * @param path any resource name (usually, but not mandatory - file name)
	 * @param sharedMode lock resource in shared (false) or exclusive (true) mode;
	 * @return true on success, false otherwise
	 * @throws IOException if any exceptions was thrown
	 */
	boolean tryLock(String path, boolean sharedMode)  throws IOException;
	
	/**
	 * <p>Lock file system resource. Waits until lock is successful</p>
	 * @param path any resource name (usually, but not mandatory - file name)
	 * @param sharedMode lock resource in shared (false) or exclusive (true) mode;
	 * @throws IOException if any exceptions was thrown
	 */
	void lock(String path, boolean sharedMode) throws IOException;
	
	/**
	 * <p>Unlock file system resource.</p>
	 * @param path any resource name (usually, but not mandatory - file name)
	 * @param sharedMode unlock resource in shared (false) or exclusive (true) mode;
	 * @throws IOException if any exceptions was thrown
	 */
	void unlock(String path, boolean sharedMode)  throws IOException;
}
