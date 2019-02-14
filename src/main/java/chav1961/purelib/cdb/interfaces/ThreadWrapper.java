package chav1961.purelib.cdb.interfaces;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.cdb.interfaces.AppDebugInterface.Location;

public interface ThreadWrapper {
	Thread getThread() throws DebuggingException;
	Location getCurrentLocation() throws DebuggingException;
	int getStackSize() throws DebuggingException;
	StackWrapper[] getStackContent() throws DebuggingException;
	StackWrapper getStackContent(int depth) throws DebuggingException;
	void step() throws DebuggingException; 
	void stepInto() throws DebuggingException; 
	void stepOut() throws DebuggingException;
	void run() throws DebuggingException;
	void setBreakpoint() throws DebuggingException;
}
