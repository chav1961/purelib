package chav1961.purelib.ui.html;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class SwapManager implements Closeable {
	private static final AtomicInteger					UNIQUE_ID = new AtomicInteger();
	
	private final SubstitutableProperties				props;
	private final FileSystemInterface 					root;
	private final SimpleURLClassLoader					loader = new SimpleURLClassLoader(new URL[0]);
	private final IdentityHashMap<Class<?>, Serializer>	clazzMap = new IdentityHashMap<Class<?>, Serializer>();
	private final ReentrantReadWriteLock				loaderLock = new ReentrantReadWriteLock();
	private final ReadLock								readLoaderLock = loaderLock.readLock();
	private final WriteLock								writeLoaderLock = loaderLock.writeLock();
	private final ReentrantReadWriteLock				sessionLock = new ReentrantReadWriteLock();
	private final ReadLock								readSessionLock = sessionLock.readLock();
	private final WriteLock								writeSessionLock = sessionLock.writeLock();
	
	public SwapManager(final SubstitutableProperties props, final FileSystemInterface root) {
		if (props == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else if (root == null) {
			throw new NullPointerException("Swap filesystem root can't be null"); 
		}
		else {
			this.props = props;
			this.root = root;
		}
	}

	public void registerClass2Swap(final Class<ModuleAccessor> clazz) throws InterruptedException, ContentException {
		if (clazz == null) {
			throw new NullPointerException("Class to register can't be null"); 
		}
		else {
			try{writeLoaderLock.lockInterruptibly();
				clazzMap.put(clazz, buildSerializer(clazz));
			} finally {
				writeLoaderLock.unlock();
			}
		}
	}

	public void unregisterClass2Swap(final Class<ModuleAccessor> clazz) throws InterruptedException {
		if (clazz == null) {
			throw new NullPointerException("Class to register can't be null"); 
		}
		else {
			try{writeLoaderLock.lockInterruptibly();
				clazzMap.remove(clazz);
			} finally {
				writeLoaderLock.unlock();
			}
		}
	}
	
	public void createSessionSwap(final UUID sessionId, final long ttl) throws InterruptedException, IOException {
		try{writeSessionLock.lockInterruptibly();
			try(FileSystemInterface	fsi = root.clone().open("/"+sessionId).mkDir()) {
			}
			if (ttl > 0) {
				final TimerTask	tt = new TimerTask() {
					@Override
					public void run() {
						try{dropSessionSwap(sessionId);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						} catch (IOException e) {
						}
					}
				};
				PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt, ttl);
			}
		} finally {
			writeSessionLock.unlock();
		}
	}
	
	public void dropSessionSwap(final UUID sessionId) throws InterruptedException, IOException {
		try{writeSessionLock.lockInterruptibly();
			try(FileSystemInterface	fsi = root.clone().open("/"+sessionId).deleteAll()) {
			}
		} finally {
			writeSessionLock.unlock();
		}
	}
	
	public <T> void serialize(final UUID sessionId, final long instanceId, final T instance) throws IOException, InterruptedException {
		if (sessionId == null) {
			throw new NullPointerException("Session id can't be null"); 
		}
		else if (instance == null) {
			throw new NullPointerException("INstance to serialize can't be null"); 
		}
		else {
			try{readLoaderLock.lockInterruptibly();
				final Class<ModuleAccessor>	clazz = (Class<ModuleAccessor>) instance.getClass();
				final Serializer<T>			ser = clazzMap.get(clazz);
				
				if (ser == null) {
					throw new IllegalArgumentException("Class ["+clazz.getCanonicalName()+"] to serialize was not registered. Call registerSwap2Class(...) firstly"); 
				}
				else {
					try(final FileSystemInterface	item = root.clone().open("/"+sessionId+"/"+clazz.getCanonicalName()).mkDir().open("./"+instanceId).create()) {
						try(final OutputStream		os = item.write();
							final DataOutputStream	dos = new DataOutputStream(os)) {
							
							ser.serialize(instance, dos);
							os.flush();
						}
					}
				}
			} finally {
				readLoaderLock.unlock();
			}
		}
	}

	public <T> void deserialize(final UUID sessionId, final long instanceId, final T instance) throws IOException, InterruptedException {
		if (sessionId == null) {
			throw new NullPointerException("Session id can't be null"); 
		}
		else if (instance == null) {
			throw new NullPointerException("INstance to serialize can't be null"); 
		}
		else {
			try{readLoaderLock.lockInterruptibly();
				final Class<ModuleAccessor>	clazz = (Class<ModuleAccessor>) instance.getClass();
				final Serializer<T>			ser = clazzMap.get(clazz);
				
				if (ser == null) {
					throw new IllegalArgumentException("Class ["+clazz.getCanonicalName()+"] to serialize was not registered. Call registerSwap2Class(...) firstly"); 
				}
				else {
					try(final FileSystemInterface	item = root.clone().open("/"+sessionId+"/"+clazz.getCanonicalName()+"/"+instanceId)) {
						try(final InputStream		is = item.read();
							final DataInputStream	dis = new DataInputStream(is)) {
							
							ser.deserialize(instance, dis);
						}
					}
				}
			} finally {
				readLoaderLock.unlock();
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		loader.close();
		this.root.deleteAll();
	}

	interface Serializer<T> {
		void serialize(T instance, DataOutputStream os) throws IOException;
		void deserialize(T instance, DataInputStream is) throws IOException;
	}
	
	static <T> Serializer<T> buildSerializer(final Class<ModuleAccessor> clazz) throws ContentException {
		return null;
	}
}
