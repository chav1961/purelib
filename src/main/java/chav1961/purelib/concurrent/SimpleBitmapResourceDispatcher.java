package chav1961.purelib.concurrent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.concurrent.interfaces.ResourceDispatcherLock;

/**
 * <p>This class implements simple resource dispatcher. It supports locking and unlocking a <b>group</b> of resources, up to 64 items in the group.
 * Every resource in the dispatcher identifies by it's index (0..63). There is an example to use this class:</p>
 * <pre>
 * {@code
 * 	try(final SimpleBitmapResourceDispatcher disp = new SimpleBitmapResourceDispatcher((1L << 0) | (1L << 1))) { // initially register resources with index 0 and 1
 *    disp.start();
 *    . . .
 *    try(final ResourceDispatcherLock lock = disp.lock((1L << 0) | (1L << 1))) { // lock group of two resources with index 0 and 1
 *    // process locked resources 
 *    }
 *    . . .
 *    disp.registerResourceIndex(2);
 *    . . .
 *    try(final ResourceDispatcherLock lock = disp.lock((1L << 0) | (1L << 2))) { // lock group of two resources with index 0 and 2
 *    // process locked resources 
 *    }
 *    . . .
 *    disp.unregisterResourceIndex(2);
 *    . . .
 *    disp.stop();
 * 	}
 * }
 * </pre>
 * <p>The main usage of this class is to avoid deadlocks, when more than one resource locking is required. It works similar to:</p>
 * <code>
 * synchronized(resourceIndex0) {
 *   synchronized(resourceIndex1) {
 *     . . .
 *     synchronized(resourceIndexN) {
 *        // process locked resources
 *     }
 *     . . .
 *   }
 * }
 * </code>
 * <p>This class is thread-safe</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 *
 */
public class SimpleBitmapResourceDispatcher implements AutoCloseable, ExecutionControl  {
	private static final AtomicInteger	AI = new AtomicInteger(1);
	
	private final AtomicLong	bitmap;
	private final AtomicLong	locks = new AtomicLong();
	private final ArrayBlockingQueue<OperationRequest>	queue = new ArrayBlockingQueue<>(10);
	private final List<OperationRequest>				awaited = new LinkedList<>();
	private volatile boolean	isStarted = false;
	private volatile boolean	isSuspended = false;
	private volatile boolean	isAvailable = false;
	private volatile Thread		dispatcher = null;
	
	public SimpleBitmapResourceDispatcher(final long resourceBitmap) {
		this.bitmap = new AtomicLong(resourceBitmap);
	}
	
	public ResourceDispatcherLock lock(final long resourcebitmap) throws IllegalArgumentException, IllegalStateException, InterruptedException, FlowException {
		if (!isAvailable) {
			throw new FlowException("Resource lock unsuccessful (dispatcher suspended)"); 
		}
		else {
			final OperationRequest	rq = new OperationRequest(OperationRequest.Action.ALLOCATE, resourcebitmap);
			
			queue.put(rq);
			rq.latch.await();
			if (rq.success) {
				return new ResourceDispatcherLock() {
					private AtomicBoolean closed = new AtomicBoolean(false);
					
					@Override
					public void close() throws InterruptedException {
						if (!closed.getAndSet(true)) {
							queue.put(new OperationRequest(OperationRequest.Action.FREE, resourcebitmap));
						}
						else {
							throw new IllegalStateException("Resource is already unlocked");
						}
					}
				};
			}
			else {
				throw new FlowException("Resource lock unsuccessful ("+rq.cause+")"); 
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
		try{if (isStarted()) {
				stop();
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public synchronized void start() throws IllegalStateException {
		if (isStarted()) {
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
		if (!isStarted()) {
			throw new IllegalStateException("Dispatcher is not started");
		}
		else if (isSuspended()) {
			throw new IllegalStateException("Dispatcher is already suspended");
		}
		else {
			isSuspended = true;
			isAvailable = false;
		}
	}

	@Override
	public synchronized void resume() throws IllegalStateException {
		if (!isStarted()) {
			throw new IllegalStateException("Dispatcher is not started");
		}
		else if (!isSuspended()) {
			throw new IllegalStateException("Dispatcher is not suspended");
		}
		else {
			isSuspended = false;
			isAvailable = true;
		}
	}

	@Override
	public synchronized void stop() throws IllegalStateException, InterruptedException {
		if (!isStarted()) {
			throw new IllegalStateException("Dispatcher is not started");
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
						else {
							awaited.add(0, rq);
						}
						break;
					case FREE		:
						locks.updateAndGet((t)->t & ~mask);
						break;
					default:
						throw new UnsupportedOperationException("Action ["+rq.action+"] is not implemented yet"); 
				}
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
