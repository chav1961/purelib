package chav1961.purelib.cdb.interfaces;

import java.lang.reflect.Method;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.cdb.interfaces.AppDebugInterface.Location;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;

public interface ThreadWrapper {
	public interface DebugExecutionControl extends ExecutionControl {
		void start() throws DebuggingException;
		void suspend() throws DebuggingException;
		void resume() throws DebuggingException;
		void stop() throws DebuggingException;
		
		boolean isStarted();
		boolean isSuspended();

		void step() throws DebuggingException; 
		void stepInto() throws DebuggingException; 
		void stepOut() throws DebuggingException;
		void run() throws DebuggingException;
		void setBreakpoint() throws DebuggingException;
	}
	
	DebugExecutionControl getExecutionControl() throws DebuggingException;
	String getCurrentState() throws DebuggingException;
	Location getCurrentLocation() throws DebuggingException;
	int getStackSize() throws DebuggingException;
	StackWrapper[] getStackContent() throws DebuggingException;
	StackWrapper getStackContent(int depth) throws DebuggingException;
	StackWrapper getStackContent(final String methodSignature) throws DebuggingException;
	StackWrapper getStackContent(final Method method) throws DebuggingException;
}
