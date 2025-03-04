package chav1961.purelib.basic;

import java.util.Iterator;

/**
 * <p>This class is an implementation of empty iterator. Use it as singleton instance.</p>
 * <p>This class is thread-safe.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 * @thread.safe;
 * @param <T> any type to iterate.
 */
public final class NullIterator<T> implements Iterator<T> {
	private static final NullIterator<?>	SINGLETON = new NullIterator<>();  
	
	private NullIterator() {
	}
	
	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public T next() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> singleton() {
		return (Iterator<T>) SINGLETON;
	}
}
