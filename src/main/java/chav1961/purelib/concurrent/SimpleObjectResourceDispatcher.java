package chav1961.purelib.concurrent;

import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.concurrent.interfaces.ResourceDispatcherLock;

public class SimpleObjectResourceDispatcher<Res> implements AutoCloseable, ExecutionControl {

	public void registerResource(final Res resource) {
		
	}

	public void unregisterResource(final Res resource) {
		
	}

	public boolean isResourceRegistered(final Res resource) {
		return true;
	}
	
	public boolean isResourceLocked(final Res resource) {
		return true;
	}
	
	public Iterable<Res> getRegisteredResources() {
		return null;
	}
	
	
	public ResourceDispatcherLock lock(final Res... resources) {
		return null;
	}
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suspend() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}

}
