package chav1961.purelib.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * <p>This class is a wrapper to collection(s) or arrays to implement {@linkplain Enumeration} interface for them.</p> 
 * <p>This class is not thread-safe</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 * @param <T> any type to iterate
 */
public class EnumerationWrapper<T> implements Enumeration<T> {
	final List<Iterable<T>>	list = new ArrayList<>();
	Iterator<T>	current = null;
	int			index = 0;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param iterable iterable to wrap. Can't be null.
	 * @throws NullPointerException iterable to wrap is null
	 */
	public EnumerationWrapper(final Iterable<T> iterable) throws NullPointerException {
		if (iterable == null) {
			throw new NullPointerException("Iterable can't be null");
		}
		else {
			this.list.add(iterable);
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param iterables iterable list to wrap. Can't be null and can't contain nulls inside
	 * @throws IllegalArgumentException iterable list is null or contaons nulls inside 
	 */
	@SafeVarargs
	public EnumerationWrapper(final Iterable<T>... iterables) throws IllegalArgumentException {
		if (iterables == null || Utils.checkArrayContent4Nulls(iterables) >= 0) {
			throw new IllegalArgumentException("Iterables is null or contaons nulls inside");
		}
		else {
			this.list.addAll(Arrays.asList(iterables));
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param iterables array to iterate. Can't be null
	 * @throws NullPointerException iterable to wrap is null
	 */
	@SafeVarargs
	public EnumerationWrapper(final T... iterables) throws NullPointerException {
		if (iterables == null) {
			throw new NullPointerException("Iterables can't be null");
		}
		else {
			this.list.add(Arrays.asList(iterables));
		}
	}

	@Override
	public boolean hasMoreElements() {
		if (index >= list.size()) {
			return false;
		}
		else {
			if (current == null) {
				current = list.remove(0).iterator();
			}
			if (!current.hasNext()) {
				index++;
				return hasMoreElements();
			}
			else {
				return true;
			}
		}
	}

	@Override
	public T nextElement() {
		return current.next();
	}
}
