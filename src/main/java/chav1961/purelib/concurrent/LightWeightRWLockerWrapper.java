package chav1961.purelib.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import chav1961.purelib.basic.ReusableInstances;

/**
 * <p>This class is a wrapper to {@linkplain ReentrantReadWriteLock} and designs especially to use in the 
 * <b>try-with-resource</b> statements. Use of this class simplifies programming with the {@linkplain ReentrantReadWriteLock}:</p>
 * <code>final LightWeightRWLockerWrapper wrapper = new LightWeightRWLockerWrapper();<br>
 * . . .<br>
 * try (LightWeightRWLockerWrapper.Locker locker = wrapper.lock()) {<br>
 * 	. . .<br>
 * }<br>
 * . . .<br>
 * try (LightWeightRWLockerWrapper.Locker locker = wrapper.lock(false)) {<br>
 * 	. . .<br>
 * }<br>
 * . . .<br>
 * </code>
 * <p>All the {@linkplain Locker lockers} produced use the same instance of the {@linkplain ReentrantReadWriteLock} associated with
 * the given {@linkplain LightWeightRWLockerWrapper} instance. Good practice is to define and initialize wrapper variable as instance field 
 * in your class.</p>  
 * @see ReentrantReadWriteLock
 * @see chav1961.purelib.concurrent JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.3
 */
public class LightWeightRWLockerWrapper {
	private final ReentrantReadWriteLock	lock = new ReentrantReadWriteLock();
	private final ReusableInstances<Locker>	inst = new ReusableInstances<>(()->{return new Locker();}); 

	/**
	 * <p>Producer of read-only Locker.</p>
	 * @return locker that locks <b>try-with-resource</b> section is read-only mode 
	 */
	public Locker lock() {
		return lock(true);
	}

	/**
	 * <p>Producer of any type Locker.</p>
	 * @param sharedMode true if locker type need be read-only, false for the read-write locker mode 
	 * @return locker that locks <b>try-with-resource</b> section is required mode 
	 */
	public Locker lock(final boolean sharedMode) {
		final Locker	locker = inst.allocate();
		
		locker.lock(sharedMode);
		return locker;
	}
	
	public class Locker implements AutoCloseable {
		private boolean	mode;
		
		private Locker() {
		}

		private void lock(final boolean mode) {
			this.mode = mode;
			if (mode) {
				lock.readLock().lock();
			}
			else {
				lock.writeLock().lock();
			}
		}
		
		@Override
		public void close() {
			if (mode) {
				lock.readLock().unlock();
			}
			else {
				lock.writeLock().unlock();
			}
			inst.free(this);
		}
	}
}
