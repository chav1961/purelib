package chav1961.purelib.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import chav1961.purelib.basic.ReusableInstances;

class ThreadPoolTaskWrapper<Result> implements Future<Result> {
	@FunctionalInterface
	public interface StepByStrepTask<Result> {
		int RC_NEXT = 0;
		int RC_EXIT = -1;
		int call(int stepNo, ThreadPoolTaskWrapper<Result> wrapper) throws Exception;
	}

	private final ExecutorService 									threadPool;
	private final ThreadPoolTaskWrapper<Result>						parent;
	private final ReusableInstances<ThreadPoolTaskWrapper<Result>>	instances;
	private final Semaphore											sema = new Semaphore(0);
	private final Callable<Result>									callable = new Callable<Result>() {
																		@Override
																		public Result call() throws Exception {
																			try{final int	newStep = task.call(currentStep,ThreadPoolTaskWrapper.this);
																			
																				if (newStep == StepByStrepTask.RC_EXIT) {
																					done = true;
																				}
																				else {
																					currentStep = newStep == StepByStrepTask.RC_NEXT ? currentStep + 1 : newStep;
																				}
																			} catch (Exception exc) {
																				exception = exc;
																				done = true;
																			}
																			return null;
																		}
																	};
	private volatile boolean										cancelled, done;
	private volatile Future<Result>									future;
	private volatile Exception										exception;
	private volatile Result											result;
	private volatile StepByStrepTask<Result>						task;
	private volatile int											currentStep;
	
	public ThreadPoolTaskWrapper(final ExecutorService threadPool) {
		if (threadPool == null) {
			throw new NullPointerException("Thread pool ref can't be null"); 
		}
		else {
			this.threadPool = threadPool;
			this.instances = new ReusableInstances<ThreadPoolTaskWrapper<Result>>(
								()->{return new ThreadPoolTaskWrapper<Result>(ThreadPoolTaskWrapper.this);},
								(inst)->{
									inst.currentStep = 1;
									inst.result = null;
									inst.exception = null;
									inst.cancelled = false;
									inst.done = false;
									return inst;
								}
							);
			this.parent = null;
		}
	}

	protected ThreadPoolTaskWrapper(final ThreadPoolTaskWrapper<Result> parent) {
		this.threadPool = null;
		this.instances = null;
		this.parent = parent;
	}

	protected void setTask(final StepByStrepTask<Result> task) {
		if (instances != null) {
			throw new IllegalStateException("Attempt to set task for 'root' wrapper");
		}
		else if (task == null) {
			throw new IllegalStateException("Task to sert can't be null");
		}
		else {
			this.task = task;
		}
	}
	
	public Future<Result> submit(final StepByStrepTask<Result> task) {
		if (this.instances == null) {
			throw new IllegalStateException("Attempt to submit task to child ThreadPoolTaskWrapper instance");
		}
		else {
			final ThreadPoolTaskWrapper<Result>	item = instances.allocate();
			
			item.setTask(task);
			item.resume();
			return item;
		}
	}
	
	public void setResult(final Result result) {
		this.result = result;
	}
	
	public void resume() {
		resume(currentStep);
	}
	
	protected void resume(final int stepNo) {
		if (parent == null) {
			throw new IllegalStateException("Attempt to call method in the 'root' instance"); 
		}
		else {
			if (future == null) {
				future = parent.threadPool.submit(callable);
			}
			else if (exception == null && !done && !cancelled) {
				future = parent.threadPool.submit(callable);
			}
		}
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		if (parent == null) {
			throw new IllegalStateException("Attempt to call method in the 'root' instance"); 
		}
		else {
			return  cancelled = future.cancel(mayInterruptIfRunning);
		}
	}

	@Override
	public boolean isCancelled() {
		if (parent == null) {
			throw new IllegalStateException("Attempt to call method in the 'root' instance"); 
		}
		else {
			return cancelled;
		}
	}

	@Override
	public boolean isDone() {
		if (parent == null) {
			throw new IllegalStateException("Attempt to call method in the 'root' instance"); 
		}
		else {
			return done;
		}
	}

	@Override
	public Result get() throws InterruptedException, ExecutionException {
		sema.acquire();
		final Result	toReturn = result;
		final Exception	toThrow = exception; 
		
		parent.instances.free(this);
		if (toThrow != null) {
			throw new ExecutionException(toThrow); 
		}
		else {
			return toReturn;
		}
	}

	@Override
	public Result get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (sema.tryAcquire(timeout,unit)) {
			final Result	toReturn = result;
			final Exception	toThrow = exception; 
			
			parent.instances.free(this);
			if (toThrow != null) {
				throw new ExecutionException(toThrow); 
			}
			else {
				return toReturn;
			}
		}
		else {
			throw new TimeoutException();
		}
	}
}
