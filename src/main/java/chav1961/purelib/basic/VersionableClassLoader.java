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
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.concurrent.LightWeightListenerList.LightWeightListenerCallback;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class VersionableClassLoader<Version> extends SimpleURLClassLoader {
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

	public interface VersionChangedListener<Version> {
		void versionChanged(Version oldVersion, Version newVersion, Iterable<String> changes);
	}
	
	private final LightWeightListenerList<VersionChangedListener<Version>>	listeners;
	private final VersionRepositoryAdapter<Version>	adapter;
	private final TimerTask							tt;
	private volatile VersionChain<Version>			lastVersion;
	
	public VersionableClassLoader(final String name, final Class<Version> clazz, final ClassLoader parent, final URLStreamHandlerFactory factory, final VersionRepositoryAdapter<Version> adapter) throws NullPointerException {
		super(name, new URL[0], parent, factory);
		if (adapter != null) {
			throw new NullPointerException("Version repository adapter can' tbe null");
		}
		else {
			this.listeners = new LightWeightListenerList(clazz);
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

	public <T> T getService(final Class<T> awaited) {
		return null;
	}

	public void addVersionChangedListener(final VersionChangedListener<Version> listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}
	
	public void removeVersionChangedListener(final VersionChangedListener<Version> listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(listener);
		}
	}
	
	private void checkVersionChanges() {
	}

	static class VersionChain<Version> {
		final VersionChain<Version>				prev;
		final Version							version;
		final ClassLoader						chainLoader;
		final SyntaxTreeInterface				tree = new AndOrTree<>();
		
		public VersionChain(final VersionChain<Version> prev, final Version version, final ClassLoader chainLoader) {
			this.prev = prev;
			this.version = version;
			this.chainLoader = chainLoader;
		}
	}
}
