package chav1961.purelib.concurrent;

interface ExtendedSyncSchemeControl extends SyncSchemeControl {
	int getChildrenCount();
	SyncSchemeControl getChild(int child);
}
