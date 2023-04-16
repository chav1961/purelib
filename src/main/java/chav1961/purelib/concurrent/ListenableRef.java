package chav1961.purelib.concurrent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;

/**
 * <p>This class is a container for keeping reference value and waiting to it's "change event"</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @param <T> reference type
 */
public class ListenableRef<T> {
	private final Object		awaitSync = new Object();
	private final AtomicInteger	awaitCounter = new AtomicInteger(0);
	private volatile T			currentValue;
	private volatile ReentrantReadWriteLock	rwLock = new ReentrantReadWriteLock();
	
	public ListenableRef() {
		this(null);
	}

	public ListenableRef(final T initial) {
		currentValue = initial;
	}
	
	public T get() {
		final Lock	lock = rwLock.readLock();
		
		try {lock.lock();
			
			return currentValue;
		} finally {
			lock.unlock();
		}
	}

	public T set(final T newValue) {
		return set((value)->newValue);
	}

	public T set(final Function<T,T> op) {
		final Lock	lock = rwLock.writeLock();
		
		try{lock.lock();
			final T	result = currentValue;  
			
			currentValue = op.apply(result);
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

	public void await(final T awaited) throws InterruptedException {
		await(value->value == null ? value == awaited : value.equals(awaited));
	}

	public void await(final T awaited, final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
		if (timeout < 0) {
			throw new IllegalArgumentException("Illegal negative timeout value ["+timeout+"]");
		}
		else if (unit == null) {
			throw new NullPointerException("Time units can't be null");
		}
		else {
			await(value->value == null ? value == awaited : value.equals(awaited),timeout,unit);
		}
	}
	
	public void await(final Predicate<T> tester) throws InterruptedException {
		for (;;) {
			try{await(tester,Long.MAX_VALUE);
				return;
			} catch (TimeoutException e) {
			}
		}
	}

	public void await(final Predicate<T> tester, final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
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
		return "ListenableRef [awaitCounter=" + awaitCounter + ", currentValue=" + currentValue + "]";
	}

	private void await(final Predicate<T> tester, final long timeout) throws InterruptedException, TimeoutException {
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