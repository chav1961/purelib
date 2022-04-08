package chav1961.purelib.cdb;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.cdb.AbstractDebugWatcher.ContentChangeType;
import chav1961.purelib.cdb.interfaces.DebugWatcher;
import chav1961.purelib.cdb.interfaces.InnerWatchersKeeper;

class WatchersRepository<T extends DebugWatcher> implements InnerWatchersKeeper<T> {
	private final List<T>	watchers = new ArrayList<>();

	private final AbstractDebugWatcher	parent;
	private final Class					watcherClass;
	
	WatchersRepository(final AbstractDebugWatcher parent, final Class<T> watcherClass) {
		this.parent = parent;
		this.watcherClass = watcherClass;				
	}
	
	@Override
	public Iterator<T> iterator() {
		final T[]	content;
		
		synchronized (watchers) {
			content = watchers.toArray((T[])Array.newInstance(watcherClass, watchers.size()));
		}
		return Arrays.asList(content).iterator();
	}
	
	@Override
	public void addWatcher(T watcher) {
		synchronized (watchers) {
			if (watcher instanceof AbstractDebugWatcher) {
				((AbstractDebugWatcher)watcher).setParent(parent);
			}
			watchers.add(watcher);
			parent.fireContentChangeEvent((l)->l.process(parent, ContentChangeType.CONTENT_ADDED, watcher));
		}
	}

	@Override
	public void removeWatcher(T watcher) {
		synchronized (watchers) {
			watchers.remove(watcher);
			if (watcher instanceof AbstractDebugWatcher) {
				((AbstractDebugWatcher)watcher).clearParent();
			}
			parent.fireContentChangeEvent((l)->l.process(parent, ContentChangeType.CONTENT_REMOVED, watcher));
		}
	}
}
