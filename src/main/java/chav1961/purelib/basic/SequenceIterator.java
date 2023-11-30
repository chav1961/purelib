package chav1961.purelib.basic;

import java.util.Collection;
import java.util.Iterator;
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
 * @last.update 0.0.7
 */

public class SequenceIterator<T> implements Iterator<T> {
	private final Iterator<T>[]	list;
	private int					current = 0;

	/**
	 * <p>Constructor of the class</p>
	 * @param list collection of iterators. Can't be null</p>
	 * @throws IllegalArgumentException when connection is null or contains nulls inside
	 */
	@SuppressWarnings("unchecked")
	public SequenceIterator(final Collection<Iterator<T>> list) throws IllegalArgumentException {
		if (list == null || Utils.checkCollectionContent4Nulls(list) >= 0) {
			throw new IllegalArgumentException("List of iterators is null or contains nulls inside");
		}
		else {
			this.list = list.toArray(new Iterator[list.size()]); 
		}
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param list iterable list. Can't be null
	 * @throws IllegalArgumentException when connection is null or contains nulls inside
	 */
	public SequenceIterator(@SuppressWarnings("unchecked") final Iterable<T>... list) throws IllegalArgumentException {
		this(toIterators(list));
	}	
	
	/**
	 * <p>Constructor of the class</p>
	 * @param list iterator list. Can't be null
	 * @throws IllegalArgumentException when connection is null or contains nulls inside
	 */
	public SequenceIterator(@SuppressWarnings("unchecked") final Iterator<T>... list) throws IllegalArgumentException {
		if (list == null || Utils.checkArrayContent4Nulls(list) >= 0) {
			throw new IllegalArgumentException("List of iterators is null or contains nulls inside");
		}
		else {
			final int	nullItem = Utils.checkArrayContent4Nulls(list);
			
			if (nullItem >= 0) {
				throw new NullPointerException("The ["+nullItem+"]-th element of the iterator list is null!");
			}
			else {
				this.list = list; 
			}
		}
	}

	/**
	 * <p>Build iterator instance </p>
	 * @param <T> any referenced type
	 * @param list iterable list to iterate on it's content
	 * @return iterable instance. Can be empty, but no null
	 */
	@SafeVarargs
	public static <T> Iterable<T> iterable(final Iterable<T>... list){
		if (list == null || Utils.checkArrayContent4Nulls(list) >= 0) {
			throw new IllegalArgumentException("List of iterators is null or contains nulls inside");
		}
		else {
			return iterable(toIterators(list));
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
		if (list == null || Utils.checkArrayContent4Nulls(list) >= 0) {
			throw new IllegalArgumentException("List of iterators is null or contains nulls inside");
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

	@SafeVarargs
	static <T> Iterator<T>[] toIterators(final Iterable<T>... list) {
		if (list == null || Utils.checkArrayContent4Nulls(list) >= 0) {
			throw new IllegalArgumentException("Iterable list is null or contaoins nulls inside"); 
		}
		else {
			@SuppressWarnings("unchecked")
			final Iterator<T>[]	result = new Iterator[list.length];
			
			for(int index = 0; index < result.length; index++) {
				result[index] = list[index].iterator();
			}
			return result;
		}
	}
}
