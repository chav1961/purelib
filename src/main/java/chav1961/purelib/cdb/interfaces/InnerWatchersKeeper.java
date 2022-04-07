package chav1961.purelib.cdb.interfaces;

public interface InnerWatchersKeeper<T extends DebugWatcher> extends Iterable<T> {
	void addWatcher(T watcher);
	void removeWatcher(T watcher);
}
