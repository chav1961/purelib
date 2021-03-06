package chav1961.purelib.concurrent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;

/**
 * <p>This class is a container for keeping long value and waiting to it's "change event"</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class ListenableLong {
	private final Object		awaitSync = new Object();
	private final AtomicInteger	awaitCounter = new AtomicInteger(0);
	private volatile long		currentValue;
	private volatile ReentrantReadWriteLock	rwLock = new ReentrantReadWriteLock();
	
	public ListenableLong() {
		this(0);
	}

	public ListenableLong(final long initial) {
		currentValue = initial;
	}
	
	public long get() {
		final Lock	lock = rwLock.readLock();
		
		try {lock.lock();
			
			return currentValue;
		} finally {
			lock.unlock();
		}
	}

	public long set(final long newValue) {
		return set((value)->newValue);
	}

	public long set(final LongUnaryOperator op) {
		final Lock	lock = rwLock.writeLock();
		
		try{lock.lock();
			final long	result = currentValue;  
			
			currentValue = op.applyAsLong(result);
			if (awaitCounter.get() == 0) {
				synchronized(awaitSync) {
					awaitSync.notifyAll();
				}
			}
			return result;
		} finally {
			lock.unlock();
		}
	}

	public void await(final long awaited) throws InterruptedException {
		await(value->value == awaited);
	}

	public void await(final long awaited, final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
		if (timeout < 0) {
			throw new IllegalArgumentException("Illegal negative timeout value ["+timeout+"]");
		}
		else if (unit == null) {
			throw new NullPointerException("Time units can't be null");
		}
		else {
			await(value->value == awaited,timeout,unit);
		}
	}
	
	public void await(final LongPredicate tester) throws InterruptedException {
		for (;;) {
			try{await(tester,Long.MAX_VALUE);
				return;
			} catch (TimeoutException e) {
			}
		}
	}

	public void await(final LongPredicate tester, final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
		if (tester == null) {
			throw new NullPointerException("Condition tester can't be null");
		}
		else if (timeout < 0) {
			throw new IllegalArgumentException("Illegal negative timeout value ["+timeout+"]");
		}
		else if (unit == null) {
			throw new NullPointerException("Time units can't be null");
		}
		else {
			await(tester,TimeUnit.MILLISECONDS.convert(timeout,unit));
		}
	}

	@Override
	public String toString() {
		return "ListenableLong [awaitCounter=" + awaitCounter + ", currentValue=" + currentValue + "]";
	}

	private void await(final LongPredicate tester, final long timeout) throws InterruptedException, TimeoutException {
		final Lock	lock = rwLock.readLock();
		
		lock.lock();
		
		if (tester.test(currentValue)) {
			lock.unlock();
			return;
		}
		else {
			synchronized(awaitSync) {
				lock.unlock();
				
				do {final long	startMillis = System.currentTimeMillis();
					
					awaitSync.wait(timeout,0);
					if (timeout - (System.currentTimeMillis() - startMillis) < 0) {
						throw new TimeoutException();
					}
				} while (!tester.test(currentValue));
			}
		}
	}
}