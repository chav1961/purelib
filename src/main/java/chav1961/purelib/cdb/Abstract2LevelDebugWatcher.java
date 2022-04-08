package chav1961.purelib.cdb;

import java.util.Iterator;

import chav1961.purelib.cdb.interfaces.DebugWatcher;
import chav1961.purelib.cdb.interfaces.InnerWatchersKeeper;

abstract class Abstract2LevelDebugWatcher<T extends DebugWatcher> extends AbstractDebugWatcher implements InnerWatchersKeeper<T> {
	private final WatchersRepository<T>	watchers;
	
	Abstract2LevelDebugWatcher(Class<T> contentType) {
		super();
		this.watchers = new WatchersRepository<>(this, contentType);
	}

	Abstract2LevelDebugWatcher(final AbstractDebugWatcher parent, final Class<T> contentType) {
		super(parent);
		this.watchers = new WatchersRepository<>(this, contentType);
	}

	@Override 
	public Iterator<T> iterator() {
		return watchers.iterator();
	}
	
	@Override 
	public void addWatcher(T watcher) {
		watchers.addWatcher(watcher);
	}
	
	@Override 
	public void removeWatcher(T watcher) {
		watchers.removeWatcher(watcher);
	}
}
