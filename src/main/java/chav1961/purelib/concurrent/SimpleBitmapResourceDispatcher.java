package chav1961.purelib.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.concurrent.interfaces.ResourceDispatcherLock;

public class SimpleBitmapResourceDispatcher implements AutoCloseable, ExecutionControl  {
	private static final AtomicInteger	AI = new AtomicInteger(1);
	
	private final AtomicLong	bitmap;
	private final AtomicLong	locks = new AtomicLong();
	private final ArrayBlockingQueue<OperationRequest>	queue = new ArrayBlockingQueue<>(10);
	private final List<OperationRequest>				awaited = new ArrayList<>();
	private volatile boolean	isStarted = false;
	private volatile boolean	isSuspended = false;
	private volatile boolean	isAvailable = false;
	private volatile Thread		dispatcher = null;
	
	public SimpleBitmapResourceDispatcher(final long resourceBitmap) {
		this.bitmap = new AtomicLong(resourceBitmap);
	}
	
	public ResourceDispatcherLock lock(final long resourcebitmap) throws IllegalArgumentException, IllegalStateException, InterruptedException, ContentException {
		if (!isAvailable) {
			throw new ContentException("Resource lock unsuccessful (dispatcher suspended)"); 
		}
		else {
			final OperationRequest	rq = new OperationRequest(OperationRequest.Action.ALLOCATE, resourcebitmap);
			
			queue.put(rq);
			rq.latch.await();
			if (rq.success) {
				return new ResourceDispatcherLock() {
					@Override
					public void close() throws Exception {
						queue.put(new OperationRequest(OperationRequest.Action.FREE, resourcebitmap));
					}
				};
			}
			else {
				throw new ContentException("Resource lock unsuccessful ("+rq.cause+")"); 
			}
		}
	}

	public void registerResourceIndex(final int index) throws IllegalArgumentException {
		ensureIndexIsLegal(index);
		final long	mask = (1L << index);
		final long	result = bitmap.getAndUpdate((t)->isResourceIndexRegistered(index) ? t : t | mask);
		
		if ((result & mask) != 0) {
			throw new IllegalArgumentException("Resource index ["+index+"] is already registered");
		}
	}

	public void unregisterResourceIndex(final int index) throws IllegalArgumentException {
		ensureIndexIsLegal(index);
		final long	mask = (1L << index);
		final long	result = bitmap.getAndUpdate((t)->!isResourceIndexRegistered(index) ? t : t & ~mask);
		
		if ((result & mask) == 0) {
			throw new IllegalArgumentException("Resource index ["+index+"] is not registered yet or was unregistered earlier");
		}
	}

	public boolean isResourceIndexRegistered(final int index) throws IllegalArgumentException {
		ensureIndexIsLegal(index);
		return (bitmap.get() & (1 << index)) != 0;
	}
	
	public boolean isResourceIndexLocked(final int index) {
		ensureIndexIsLegal(index);
		return (locks.get() & (1L << index)) != 0;
	}
	
	public long getRegisteredResourceBitmap() {
		return bitmap.get();
	}
	
	@Override
	public synchronized void close() throws IllegalStateException {
		try{stop();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public synchronized void start() throws IllegalStateException {
		if (isStarted) {
			throw new IllegalStateException("Dispatcher is already started");
		}
		else {
			dispatcher = new Thread(()->dispatch());
			dispatcher.setName("BitmapResourceDispather-"+AI.getAndIncrement());
			dispatcher.start();
			isStarted = true;
			isAvailable = true;
		}
	}

	@Override
	public synchronized void suspend() throws IllegalStateException {
		if (isStarted) {
			throw new IllegalStateException("Dispatcher is already started");
		}
		else {
			isSuspended = true;
			isAvailable = false;
		}
	}

	@Override
	public synchronized void resume() throws IllegalStateException {
		if (isStarted) {
			throw new IllegalStateException("Dispatcher is already started");
		}
		else {
			isSuspended = false;
			isAvailable = true;
		}
	}

	@Override
	public synchronized void stop() throws IllegalStateException, InterruptedException {
		if (isStarted) {
			throw new IllegalStateException("Dispatcher is already started");
		}
		else {
			dispatcher.interrupt();
			dispatcher.join();
			dispatcher = null;
			isAvailable = false;
			isSuspended = false;
			isStarted = false;
		}
	}

	@Override
	public synchronized boolean isStarted() {
		return isStarted;
	}

	@Override
	public synchronized boolean isSuspended() {
		return isSuspended;
	}

	private void ensureIndexIsLegal(int index) {
		if (index < 0 || index > 63) {
			throw new IllegalArgumentException("Resource index ["+index+"] out of range 0..63"); 
		}
	}

	private void dispatch() {
		OperationRequest	rq;
		
		awaited.clear();
		while (!Thread.interrupted()) {
			try{rq = queue.take();
				final long mask = rq.mask;
				
				switch (rq.action) {
					case ALLOCATE	:
						if ((bitmap.get() & mask) != mask) {
							rq.success = false;
							rq.cause = "Some of the resources is not registered yet";
							rq.latch.countDown();
						}
						else if ((~locks.get() & mask) == mask) {
							locks.updateAndGet((t)->t | mask);
							rq.success = true;
							rq.latch.countDown();
						}
						else {
							awaited.add(0,rq);
						}
						break;
					case FREE		:
						locks.updateAndGet((t)->t & ~mask);
						
						for(int index = awaited.size() - 1; index >= 0; index--) {
							final OperationRequest 	item = awaited.get(index); 
							final long				itemMask = item.mask;
							
							if ((~locks.get() & itemMask) == itemMask) {
								locks.updateAndGet((t)->t | itemMask);
								item.success = true;
								item.latch.countDown();
								awaited.remove(index);
							}
						}
						break;
					default:
						throw new UnsupportedOperationException("Action ["+rq.action+"] is not implemented yet"); 
				}
			} catch (InterruptedException e) {
				break;
			} 
		}
		while ((rq = queue.peek()) != null) {
			rq.success = false;
			rq.cause = "Dispatcher stopped";
			if (rq.latch != null) {
				rq.latch.countDown();
			}
		}
		for(OperationRequest item : awaited) {
			item.cause = "Dispatcher stopped";
			item.success = false;
			item.latch.countDown();
		}
		awaited.clear();
	}

	
	private static class OperationRequest {
		private static enum Action {
			ALLOCATE,
			FREE
		}
		
		final Action			action;
		final long				mask;
		final CountDownLatch	latch;
		volatile boolean		success = true;
		volatile String			cause = null;
		
		OperationRequest(Action action, long mask) {
			this.action = action;
			this.mask = mask;
			this.latch = action == Action.ALLOCATE ? new CountDownLatch(1) : null;
		}
	}
}
