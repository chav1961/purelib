package chav1961.purelib.ui.nanoservice.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public interface NanoService {
	void start() throws IOException;
	void suspend() throws IOException;
	void resume() throws IOException;
	void stop() throws IOException;
	
	boolean isStarted();
	boolean isSuspended();
	
	void deploy(String path, Object instance2deploy) throws IOException, ContentException, SyntaxException;	
	void undeploy(final String path)throws ContentException;
}
