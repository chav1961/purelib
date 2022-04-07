package chav1961.purelib.cdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.cdb.interfaces.InnerWatchersKeeper;
import chav1961.purelib.cdb.AbstractDebugWatcher.ContentChangeListener.ChangeType;
import chav1961.purelib.cdb.interfaces.DebugWatcher;

abstract class Abstract2LevelDebugWatcher<T extends DebugWatcher> extends AbstractDebugWatcher implements InnerWatchersKeeper<T> {
	private final List<T>	watchers = new ArrayList<>();
	
	Abstract2LevelDebugWatcher() {
		super();
	}

	Abstract2LevelDebugWatcher(final AbstractDebugWatcher parent) {
		super(parent);
	}

	@Override 
	public abstract Iterator<T> iterator();
	
	@Override 
	public abstract void addWatcher(T watcher);
	
	@Override 
	public abstract void removeWatcher(T watcher);
	
	protected void addWatcherInternal(T watcher) {
		synchronized (watchers) {
			if (watcher instanceof AbstractDebugWatcher) {
				((AbstractDebugWatcher)watcher).setParent(this);
			}
			watchers.add(watcher);
			fireEvent((l)->l.process(this, ChangeType.CONTENT_ADDED, watcher));
		}
	}

	protected void removeWatcherInternal(T watcher) {
		synchronized (watchers) {
			watchers.remove(watcher);
			if (watcher instanceof AbstractDebugWatcher) {
				((AbstractDebugWatcher)watcher).clearParent();
			}
			fireEvent((l)->l.process(this, ChangeType.CONTENT_REMOVED, watcher));
		}
	}
}
