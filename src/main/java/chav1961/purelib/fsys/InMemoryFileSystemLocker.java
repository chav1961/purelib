package chav1961.purelib.fsys;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import chav1961.purelib.fsys.interfaces.FileSystemLockInterface;

public class InMemoryFileSystemLocker implements FileSystemLockInterface, Closeable  {
	
	private final boolean checkSharedModeOnly;
	private final Map<String, ReentrantReadWriteLock>	locks = new ConcurrentHashMap<>();
	
	public InMemoryFileSystemLocker(final boolean checkSharedModeOnly) {
		this.checkSharedModeOnly = checkSharedModeOnly;
	}

	@Override
	public void close() throws IOException {
		// TODO:
	}
	
	@Override
	public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path to try lock can't be null or empty"); 
		}
		else if (checkSharedModeOnly && !sharedMode) {
			throw new IllegalArgumentException("Can use sharedMode=false for shared-only lock"); 
		}
		else {
			locks.computeIfAbsent(path, (key)->new ReentrantReadWriteLock());
			if (sharedMode) {
				return locks.get(path).readLock().tryLock();
			}
			else {
				return locks.get(path).writeLock().tryLock();
			}
		}
	}

	@Override
	public void lock(final String path, final boolean sharedMode) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path to try lock can't be null or empty"); 
		}
		else if (checkSharedModeOnly && !sharedMode) {
			throw new IllegalArgumentException("Can use sharedMode=false for shared-only lock"); 
		}
		else {
			locks.computeIfAbsent(path, (key)->new ReentrantReadWriteLock());
			if (sharedMode) {
				locks.get(path).readLock().lock();
			}
			else {
				locks.get(path).writeLock().lock();
			}
		}
	}

	@Override
	public void unlock(final String path, final boolean sharedMode) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path to try lock can't be null or empty"); 
		}
		else if (checkSharedModeOnly && !sharedMode) {
			throw new IllegalArgumentException("Can use sharedMode=false for shared-only lock"); 
		}
		else {
			if (sharedMode) {
				locks.get(path).readLock().lock();
			}
			else {
				locks.get(path).writeLock().lock();
			}
			locks.computeIfPresent(path, (key, value)->value.getReadLockCount() == 0 && !value.isWriteLocked() ?  null : value);
		}
	}

	@Override
	public String toString() {
		return "FileSystemLocker [checkSharedModeOnly=" + checkSharedModeOnly + ", locks=" + locks + "]";
	}
}
