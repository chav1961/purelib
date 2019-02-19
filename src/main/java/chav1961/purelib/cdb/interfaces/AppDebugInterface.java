package chav1961.purelib.cdb.interfaces;


import chav1961.purelib.basic.exceptions.DebuggingException;

public interface AppDebugInterface extends AutoCloseable {
	public enum EventType {
		BreakPointEvent, StepEvent;
	}
	
	public interface Location {
		ClassWrapper getClassInside() throws DebuggingException;
		MethodWrapper getMethodInside() throws DebuggingException;
		String getSourcePath() throws DebuggingException;
		int getLineNo() throws DebuggingException;
	}
	
	public interface Event {
		EventType getType() throws DebuggingException;
		ThreadWrapper getThreadAssociated() throws DebuggingException;
		Location getLocation() throws DebuggingException;
	}
	
	void close() throws DebuggingException;
	Event[] waitEvent() throws InterruptedException, DebuggingException;
	Event[] waitEvent(EventType... eventTypes) throws InterruptedException, DebuggingException;
	String[] getThreadNames() throws DebuggingException;
	String[] getClassNames() throws DebuggingException;
	String[] getClassNames(final Package... packages) throws DebuggingException;
	ThreadWrapper getThread(final String threadName) throws DebuggingException;
	ClassWrapper getClass(final String className) throws DebuggingException;
	void removeBreakpoint(int breakPoint) throws DebuggingException; 
	void removeAllBreakpoints() throws DebuggingException; 
	void exit(int exitCode) throws DebuggingException;
}
