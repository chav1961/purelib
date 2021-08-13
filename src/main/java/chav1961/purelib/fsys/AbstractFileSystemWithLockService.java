package chav1961.purelib.fsys;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.DataWrapperInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public abstract class AbstractFileSystemWithLockService<LS extends AutoCloseable, L extends AutoCloseable> extends AbstractFileSystem {
	
	private final AbstractFileSystemWithLockService<LS, L>	another;
	private final Map<String, LockerAndCounter<LS>>	locks;
	private final Map<String, LockerAndCounter<L>>	current_locks = new ConcurrentHashMap<>();
	
	public AbstractFileSystemWithLockService(final URI rootPath) {
		super(rootPath);
		this.locks = new ConcurrentHashMap<>();
		this.another = null;
	}

	protected AbstractFileSystemWithLockService() {
		super();
		this.locks = new ConcurrentHashMap<>();
		this.another = null;
	}

	protected AbstractFileSystemWithLockService(final AbstractFileSystemWithLockService<LS, L> another) {
		super(another);
		this.locks = null;
		this.another = another;
	}

	@Override
	public void close() throws IOException {
		if (locks != null) {
			locks.forEach((key, value)->{
				try{value.locker.close();
				} catch (Exception e) {
				}
			});
		}
		super.close();
	}	
	
	@Override public abstract  boolean canServe(final URI uriSchema);
	@Override public abstract FileSystemInterface newInstance(final URI uriSchema) throws EnvironmentException;
	@Override public abstract FileSystemInterface clone();
	@Override public abstract DataWrapperInterface createDataWrapper(final URI actualPath) throws IOException;
	protected abstract boolean sharedModeCheckRequired();
	protected abstract LS createLockerSource(final String path) throws IOException;
	protected abstract L tryCreateLocker(final LS source, final String path, final boolean sharedMode) throws IOException;
	protected abstract L createLocker(final LS source, final String path, final boolean sharedMode) throws IOException;
	
	@Override
	public boolean tryLock(final String path, final boolean sharedMode) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new NullPointerException("Path to lock can't be null"); 
		}
		else {
			locks.computeIfAbsent(path, (key)->{
				try {return new LockerAndCounter<LS>(createLockerSource(path));
				} catch (IOException exc) {
					return  null;
				}
			});
			final L		locker = tryCreateLocker(locks.get(path).locker, path, sharedMode);
			
			if (locker != null) {
				locks.get(path).counter.incrementAndGet();
				return true;
			}
			else {
				return false;
			}
		}
	}

	@Override
	public void lock(final String path, final boolean sharedMode) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new NullPointerException("Path to lock can't be null"); 
		}
		else {
			locks.computeIfAbsent(path, (key)->{
				try {return new LockerAndCounter<LS>(createLockerSource(path));
				} catch (IOException exc) {
					return null;
				}
			});
			locks.get(path).counter.incrementAndGet();
			current_locks.computeIfAbsent(path, (key)->{
				try {return new LockerAndCounter<L>(createLocker(locks.get(path).locker, path, sharedMode));
				} catch (IOException exc) {
					return null;
				}
			});
			current_locks.computeIfPresent(path, (key, value)->{
				value.counter.incrementAndGet();
				return value;
			});
		}
	}

	@Override
	public void unlock(final String path, final boolean sharedMode) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new NullPointerException("Path to unlock can't be null"); 
		}
		else {
			current_locks.computeIfPresent(path, (key, value)->{
				if (value.counter.decrementAndGet() <= 0) {
					try{
						value.locker.close();
						locks.computeIfPresent(path, (key2, value2)->{
							if (value2.counter.decrementAndGet() <= 0) {
								try{value2.locker.close();
								} catch (Exception e) {
								}
								return null;
							}
							else {
								return value2;
							}
						});
					} catch (Exception e) {
					}
					return null;
				}
				else {
					return value;
				}
			});
		}
	}
	
	private static class LockerAndCounter<L extends AutoCloseable> {
		private final AtomicInteger	counter = new AtomicInteger();
		private final L				locker;
		
		public LockerAndCounter(final L locker) {
			this.locker = locker;
		}

		@Override
		public String toString() {
			return "LockerAndCounter [counter=" + counter + ", locker=" + locker + "]";
		}
	}
}
