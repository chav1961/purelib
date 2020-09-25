package chav1961.purelib.basic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class AbstractVersionableClassLoader<Version> extends SimpleURLClassLoader {
	private static final long					TIMER_TICK = 60000;
	private static final ReentrantReadWriteLock	SWAP_LOCK = new ReentrantReadWriteLock();
	
	public interface VersionRepositoryAdapter<Version> {
		Version getRepositoryVersion()throws IOException;
		boolean canUseCache();
		Version getCacheVersion()throws IOException;
		FileSystemInterface getRepositoryFileSystem() throws IOException;
		FileSystemInterface getCacheFileSystem() throws IOException;
		boolean versionChanged() throws IOException;
		Iterable<String> changes() throws IOException;
	}

	private final VersionRepositoryAdapter<Version>	adapter;
	private final SyntaxTreeInterface<ClassDesc>	tree = new AndOrTree<>();
	private final TimerTask							tt;
	private final List<ClassLoader>					versionedLoaders = new ArrayList<>();
	private volatile Version						lastVersionId = null;
	
	public AbstractVersionableClassLoader(final String name, final ClassLoader parent, final URLStreamHandlerFactory factory, final VersionRepositoryAdapter<Version> adapter) throws NullPointerException {
		super(name, new URL[0], parent, factory);
		if (adapter != null) {
			throw new NullPointerException("Version repository adapter can' tbe null");
		}
		else {
			this.adapter = adapter;
			checkVersionChanges();
			this.tt = new TimerTask() {
				@Override
				public void run() {
					checkVersionChanges();
				}
			};
			PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(tt,TIMER_TICK,TIMER_TICK);
		}
	}
	

	@Override
	public void close() throws IOException {
		tt.cancel();
		super.close();
	}

	public void startMain(final String className, final String... parameters) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (className == null || className.isEmpty()) {
			throw new NullPointerException("Class name can't be null or empty");
		}
		else if (parameters == null) {
			throw new NullPointerException("Parameters can't be null");
		}
		else if (Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new NullPointerException("Nulls inside parameters");
		}
		else {
			final Class<?>	cl = loadClass(className,true);
			final Method	m = cl.getMethod("main",String[].class);
			
			m.invoke(null,(Object[])parameters);
		}
	}

	private void checkVersionChanges() {
		try{if (adapter.versionChanged()) {
				if (adapter.canUseCache()) {
					swapClassLoader(reloadClasses(tree,adapter.getRepositoryFileSystem(),adapter.changes(),adapter.getCacheFileSystem()),true);
				}
				else {
					swapClassLoader(reloadClasses(tree,adapter.getRepositoryFileSystem(),adapter.changes()),false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void swapClassLoader(final ClassLoader newCLassLoader, final boolean incremental) {
		final WriteLock	lock = SWAP_LOCK.writeLock();
		
		try{lock.lock();
			// TODO Auto-generated method stub
			
		} finally {
			lock.unlock();
		}
	}

	private static ClassLoader reloadClasses(final SyntaxTreeInterface<ClassDesc> tree, final FileSystemInterface repositoryFileSystem, final Iterable<String> changes, final FileSystemInterface cacheFileSystem) throws IOException {
		try(final FileSystemInterface	from = repositoryFileSystem.clone();	// Reload file system cache
			final FileSystemInterface	to = cacheFileSystem.clone()) {
			
			for (String item : changes) {
				from.open(item).copy(to.open(item));
			}
		}
		return reloadClasses(tree,cacheFileSystem,changes);
	}

	private static ClassLoader reloadClasses(final SyntaxTreeInterface<ClassDesc> tree, final FileSystemInterface repositoryFileSystem, final Iterable<String> changes) {
		// TODO Auto-generated method stub
		return null;
	}

	static class ClassDesc {
		
	}
}
