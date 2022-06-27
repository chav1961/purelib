package chav1961.purelib.basic;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

/**
 * <p>This class implements iterator functionality based on a list of nested iterators. When the first iterator in the list is exhausted, this class
 * automatically switches to the second iterator in the list and so on. Exhausting of the last iterator in the list stops iteration process</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @param <T> any referenced type
 * @see Iterator
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.6
 */

public class SequenceIterator<T> implements Iterator<T> {
	private final Iterator<T>[]	list;
	private int					current = 0;

	@SuppressWarnings("unchecked")
	public SequenceIterator(final List<Iterator<T>> list) {
		if (list == null) {
			throw new NullPointerException("List of iterators can't be null");
		}
		else {
			for (int index = 0; index < list.size(); index++) {
				if (list.get(index) == null) {
					throw new NullPointerException("The ["+index+"]-th element of the iterator list is null!");
				}
			}
			this.list = list.toArray(new Iterator[list.size()]); 
		}
	}
	
	
	public SequenceIterator(@SuppressWarnings("unchecked") final Iterator<T>... list) {
		if (list == null) {
			throw new NullPointerException("List of iterators can't be null");
		}
		else {
			for (int index = 0; index < list.length; index++) {
				if (list[index] == null) {
					throw new NullPointerException("The ["+index+"]-th element of the iterator list is null!");
				}
			}
			this.list = list; 
		}
	}

	/**
	 * <p>Build iterator instance </p>
	 * @param <T> any referenced type
	 * @param list iterator list to iterate on it's content
	 * @return iterable instance. Can be empty, but no null
	 */
	@SafeVarargs
	public static <T> Iterable<T> iterable(final Iterator<T>... list){
		if (list == null) {
			throw new NullPointerException("List of iterators can't be null");
		}
		else {
			return new Iterable<T>() {
				@Override
				public Iterator<T> iterator() {
					return new SequenceIterator<T>(list);
				}
			};
		}
	}
	
	@Override
	public boolean hasNext() {
		if (current < list.length) {
			if (list[current].hasNext()) {
				return true;
			}
			else {
				current++;
				return hasNext();
			}
		}
		else {
			return false;
		}
	}

	@Override
	public T next() {
		return list[current].next();
	}
	
	/**
	 * <p>Convert iterator to stream</p>
	 * @return stream based on this iterator. Iterator will be exhausted after calling this method!
	 */
	public Stream<T> toStream() {
		final Builder<T> 	builder = Stream.builder();
		
		while(hasNext()) {
			builder.accept(next());
		}
		return builder.build();
	}
	
	/**
	 * <p>Convert iterator to {@linkplain Iterable}</p>
	 * @return {@linkplain Iterable} instance
	 * @since 0.0.6
	 */
	public Iterable<T> toIterable() {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return SequenceIterator.this;
			}
		};
	}
}
