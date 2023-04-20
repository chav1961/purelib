package chav1961.purelib.concurrent;

import java.util.concurrent.atomic.AtomicLong;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.concurrent.interfaces.ResourceDispatcherLock;

public class SimpleBitmapResourceDispatcher implements AutoCloseable, ExecutionControl  {
	private final LoggerFacade	logger;
	private final AtomicLong	bitmap;
	
	public SimpleBitmapResourceDispatcher(final long resourceBitmap) {
		this(PureLibSettings.CURRENT_LOGGER, resourceBitmap);
	}

	public SimpleBitmapResourceDispatcher(final LoggerFacade logger, final long resourceBitmap) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else {
			this.logger = logger;
			this.bitmap = new AtomicLong(resourceBitmap);
		}
	}
	
	public ResourceDispatcherLock lock(final long resourcebitmap) {
		return null;
	}

	public void registerResourceIndex(final int index) {
		
	}

	public void unregisterResourceIndex(final int index) {
		
	}

	public boolean isResourceIndexRegistered(final int index) {
		return true;
	}
	
	public boolean isResourceIndexLocked(final int index) {
		return true;
	}
	
	public long getRegisteredResourceBitmap() {
		return 0;
	}
	
	@Override
	public void close() throws RuntimeException {
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
