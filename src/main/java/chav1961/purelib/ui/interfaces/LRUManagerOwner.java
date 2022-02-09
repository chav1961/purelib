package chav1961.purelib.ui.interfaces;

import chav1961.purelib.ui.LRUManager;

@FunctionalInterface
public interface LRUManagerOwner {
	LRUManager getLRUManager();
}
