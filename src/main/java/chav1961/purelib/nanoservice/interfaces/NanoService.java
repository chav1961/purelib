package chav1961.purelib.nanoservice.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public interface NanoService extends ExecutionControl {
	void start() throws IOException;
	void suspend() throws IOException;
	void resume() throws IOException;
	void stop() throws IOException;
	
	boolean isStarted();
	boolean isSuspended();
	
	void deploy(String path, Object instance2deploy) throws IOException, ContentException, SyntaxException;	
	void undeploy(final String path) throws ContentException;
	FileSystemInterface getServiceRoot();
}
