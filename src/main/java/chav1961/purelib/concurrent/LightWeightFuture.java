package chav1961.purelib.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import chav1961.purelib.concurrent.interfaces.RequestProcessor;

/**
 * <p>This class is light-weight implementation of {@linkplain Future} interface. It can be used to pass processing request to another thread. Typical example
 * to use it is:</p>
 * <code>
 * ArrayLockingQueue&lt;Future&lt;String,String&gt;&gt; queue = ...
 * // first thread
 * Future&lt;String,String&gt; f = new LightWeightFuture("source");
 * queue.put(f);
 * String result = f.get();
 * // second thread
 * Future&lt;String,String&gt; f = queue.take();
 * f.complete(f.take().toUpperCase());
 * </code>
 *
 * @param <F> type of source data to process
 * @param <T> type of result processed
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class LightWeightFuture<F,T> implements Future<T>, RequestProcessor<F,T> {
	private final F					content;
	private final CountDownLatch	latch = new CountDownLatch(1);
	private final AtomicBoolean		cancelled = new AtomicBoolean(false);
	private volatile T				result = null;
	private volatile Throwable		exception = null;
	
	public LightWeightFuture(final F content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null");
		}
		else {
			this.content = content;
		}
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return cancelled.getAndSet(true);
	}

	@Override
	public boolean isCancelled() {
		return cancelled.get();
	}

	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		latch.await();
		if (exception != null) {
			throw new ExecutionException(exception);
		}
		else {
			return result;
		}
	}

	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (timeout <= 0) {
			throw new IllegalArgumentException("Timeout value ["+timeout+"] must be greater than 0");
		}
		else if (unit == null) {
			throw new NullPointerException("Time unit can't be null");
		}
		else {
			latch.await(timeout, unit);
			if (exception != null) {
				throw new ExecutionException(exception);
			}
			else {
				return result;
			}
		}
	}

	@Override
	public F take() {
		return content;
	}
	
	@Override
	public void complete(final T result) {
		if (result == null) {
			throw new NullPointerException("Result can't be null");
		}
		else if (!isDone()) {
			this.result = result;
			this.exception = null;
			latch.countDown();
		}
	}

	@Override
	public void fail(final Throwable exception) {
		if (exception == null) {
			throw new NullPointerException("Exception can't be null");
		}
		else if (!isDone()) {
			this.exception = exception;
			this.result = null;
			latch.countDown();
		}
	}
	
	@Override
	public void reject() {
		if (!isDone()) {
			this.exception = null;
			this.result = null;
			cancelled.set(true);
			latch.countDown();
		}
	}
}
