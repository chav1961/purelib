package chav1961.purelib.concurrent;

interface SyncSchemeControl extends AutoCloseable {
	public enum ControlState {
		INITIAL, STARTED, SUSPENDED, STOPPED, CLOSED
	}
	
	void start();
	void suspend();
	void resume();
	void stop();
	ControlState getCurrentState();
	
	long getControlPerformance();
	int getControlThreads();
	int getControlThreadsLimit();
	SyncSchemeControl setControlThreads(int numberofControlThreads);
	SyncSchemeControl setControlThreadsLimit(int controlThreadsLimit);
	boolean isAutoBalanced();
	SyncSchemeControl setAutoBalanced(boolean autoBalanced);
}
