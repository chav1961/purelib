package chav1961.purelib.concurrent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.concurrent.interfaces.ExecutionControl;
import chav1961.purelib.concurrent.interfaces.ResourceDispatcherLock;

class SimpleObjectResourceDispatcher<Res> implements AutoCloseable, ExecutionControl {
	private static final AtomicInteger	AI = new AtomicInteger(1);
	
	// boolean[0] - lock/unlock, boolean[1] - marks to remove
	private final Map<Res,boolean[]>	resources = new HashMap<>();
	private final Set<Res> 				registered = new HashSet<>();
	private final ArrayBlockingQueue<OperationRequest<Res>>	queue = new ArrayBlockingQueue<>(10);
	private final List<OperationRequest<Res>>				awaited = new ArrayList<>();
	private volatile boolean	isStarted = false;
	private volatile boolean	isSuspended = false;
	private volatile boolean	isAvailable = false;
	private volatile Thread		dispatcher = null;
	
	@SafeVarargs
	public SimpleObjectResourceDispatcher(final Res... res) {
		if (res == null || Utils.checkArrayContent4Nulls(res) >= 0) {
			throw new IllegalArgumentException("Resource list is null or contains nulls inside");
		}
		else {
			for(Res item : res) {
				registered.add(item);
				resources.put(item, new boolean[2]);
			}
		}
	}

	public void registerResource(final Res resource) throws InterruptedException {
		if (resource == null) {
			throw new NullPointerException("Resource to register can't be null"); 
		}
		else {
			synchronized (registered) {
				if (registered.contains(resource)) {
					throw new IllegalArgumentException("Resource was registered already");
				}
				else {
					@SuppressWarnings("unchecked")
					final Res[]	item = (Res[])Array.newInstance(resource.getClass(), 1);
					
					Array.set(item, 0, resource);
					registered.add(resource);
					queue.put(new OperationRequest<Res>(OperationRequest.Action.ADD_RESOURCE, item));
				}
			}
		}
	}

	public void unregisterResource(final Res resource) throws InterruptedException {
		if (resource == null) {
			throw new NullPointerException("Resource to unregister can't be null"); 
		}
		else {
			synchronized (registered) {
				if (!registered.contains(resource)) {
					throw new IllegalArgumentException("Resource was registered already");
				}
				else {
					@SuppressWarnings("unchecked")
					final Res[]	item = (Res[])Array.newInstance(resource.getClass(), 1);
					
					Array.set(item, 0, resource);
					registered.remove(resource);
					queue.put(new OperationRequest<Res>(OperationRequest.Action.REMOVE_RESOURCE, item));
				}
			}
		}
	}

	public boolean isResourceRegistered(final Res resource) {
		if (resource == null) {
			throw new NullPointerException("Resource to unregister can't be null"); 
		}
		else {
			synchronized (registered) {
				return registered.contains(resource);
			}
		}
	}
	
	public boolean isResourceLocked(final Res resource) {
		return true;
	}
	
	public Iterable<Res> getRegisteredResources() {
		synchronized (registered) {
			return Collections.unmodifiableSet(registered);
		}
	}
	
	public ResourceDispatcherLock lock(@SuppressWarnings("unchecked") final Res... res) throws FlowException, InterruptedException {
		if (!isAvailable) {
			throw new FlowException("Resource lock unsuccessful (dispatcher suspended)"); 
		}
		else if (res == null || res.length == 0 || Utils.checkArrayContent4Nulls(res) >= 0) {
			throw new IllegalArgumentException("Resource list is null, empty or contains nulls inside"); 
		}
		else {
			final Res[]					clone = res.clone();
			final OperationRequest<Res>	rq = new OperationRequest<>(OperationRequest.Action.ALLOCATE, clone);
			
			queue.put(rq);
			rq.latch.await();
			if (rq.success) {
				return new ResourceDispatcherLock() {
					private AtomicBoolean closed = new AtomicBoolean(false);
					
					@Override
					public void close() throws InterruptedException {
						if (!closed.getAndSet(true)) {
							queue.put(new OperationRequest<Res>(OperationRequest.Action.FREE, clone));
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
			dispatcher.setName("ObjectResourceDispather-"+AI.getAndIncrement());
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

	private void dispatch() {
		OperationRequest<Res>	rq;
		int	count;
		
		awaited.clear();
		while (!Thread.interrupted()) {
			try{rq = queue.take();
				final Res[] 	mask = rq.mask;
				
				switch (rq.action) {
					case ALLOCATE	:
						count = 0;
						for(Res item : mask) {
							if (resources.get(item)[1]) {
								resources.remove(item);
								rq.success = false;
								rq.cause = "Some of the resources is not registered yet";
								rq.latch.countDown();
							}
							else if (!resources.get(item)[0]) {
								count++;
							}
						}						
						if (count == mask.length) {
							for(Res item : mask) {
								resources.get(item)[0] = true;
							}						
							rq.success = true;
							rq.latch.countDown();
						}
						else {
							awaited.add(0,rq);
						}
						break;
					case FREE		:
						for(Res item : mask) {
							if (resources.get(item)[1]) {
								resources.remove(item);
							}
							else {
								resources.get(item)[0] = false;
							}
						}
						
						for(int index = awaited.size() - 1; index >= 0; index--) {
							final OperationRequest<Res>	r = awaited.get(index); 
							final Res[]					itemMask = r.mask;
							
							count = 0;							
							for(Res item : itemMask) {
								if (resources.get(item)[1]) {
									resources.remove(item);
									r.success = false;
									r.cause = "Some of the resources is not registered yet";
									r.latch.countDown();
								}
								else if (!resources.get(item)[0]) {
									count++;
								}
							}						
							if (count == mask.length) {
								for(Res item : itemMask) {
									resources.get(item)[0] = true;
								}						
								r.success = true;
								r.latch.countDown();
							}
						}
						break;
					case ADD_RESOURCE		:
						resources.put(mask[0], new boolean[2]);
						break;
					case REMOVE_RESOURCE	:
						if (resources.get(mask[0])[0]) {	// was locked
							resources.get(mask[0])[1] = true;
						}
						else {
							resources.remove(mask[0]);
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
		for(OperationRequest<Res> item : awaited) {
			item.cause = "Dispatcher stopped";
			item.success = false;
			item.latch.countDown();
		}
		awaited.clear();
	}

	private static class OperationRequest<Res> {
		private static enum Action {
			ALLOCATE,
			FREE,
			ADD_RESOURCE,
			REMOVE_RESOURCE
		}
		
		final Action			action;
		final Res[]				mask;
		final CountDownLatch	latch;
		volatile boolean		success = true;
		volatile String			cause = null;
		
		OperationRequest(Action action, Res[] mask) {
			this.action = action;
			this.mask = mask;
			this.latch = action == Action.ALLOCATE ? new CountDownLatch(1) : null;
		}
	}
	
}
