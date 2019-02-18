package chav1961.purelib.concurrent.interfaces;

public interface ExecutionControl {
	void start() throws Exception;
	void suspend() throws Exception;
	void resume() throws Exception;
	void stop() throws Exception;
	
	boolean isStarted();
	boolean isSuspended();
}
