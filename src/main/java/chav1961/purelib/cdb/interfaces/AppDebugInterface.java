package chav1961.purelib.cdb.interfaces;


import chav1961.purelib.basic.exceptions.DebuggingException;

public interface AppDebugInterface extends AutoCloseable {
	public enum EventType {
		BreakPointEvent;
	}
	
	public interface Location {
		InstanceWrapper getClassInside();
		MethodWrapper getMethodInside();
		String getSourcePath();
		int getLineNo();
	}
	
	public interface Event {
		EventType getType() throws DebuggingException;
		ThreadWrapper getThreadAssociated() throws DebuggingException;
		InstanceWrapper getClassAssociated() throws DebuggingException;
	}
	
	void close() throws DebuggingException;
	Event waitEevent() throws InterruptedException, DebuggingException;
	Event waitEevent(EventType... eventTypes) throws InterruptedException, DebuggingException;
	String[] getThreadNames() throws DebuggingException;
	String[] getClassNames() throws DebuggingException;
	String[] getClassNames(final Package... packages) throws DebuggingException;
	ThreadWrapper getThread(final String threadName) throws DebuggingException;
	InstanceWrapper getClass(final String className) throws DebuggingException;
	void removeAllBreakpoints() throws DebuggingException; 
	void exit(int exitCode) throws DebuggingException;
	void halt(int exitCode) throws DebuggingException;
}
