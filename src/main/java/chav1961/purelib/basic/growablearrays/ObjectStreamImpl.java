package chav1961.purelib.basic.growablearrays;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class ObjectStreamImpl<T> implements Stream<T> {
	private final Spliterator<T>	spliterator;
	private final Iterator<T>		iterator;
	private final AutoCloseable		close;
	
	ObjectStreamImpl(final Spliterator<T> spliterator, final AutoCloseable close) {
		this.spliterator = spliterator;
		this.iterator = null;
		this.close = close;
	}

	ObjectStreamImpl(final Iterator<T> iterator, final AutoCloseable close) {
		this.spliterator = null;
		this.iterator = iterator;
		this.close = close;
	}
	
	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Spliterator<T> spliterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isParallel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Stream<T> sequential() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> parallel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> unordered() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> onClose(Runnable closeHandler) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Stream<T> filter(Predicate<? super T> predicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super T> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super T> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> distinct() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> sorted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> sorted(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> peek(Consumer<? super T> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> limit(long maxSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<T> skip(long n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forEach(final Consumer<? super T> action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			collect(()->null, (acc, val) ->action.accept(val), null);
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.next());
			}
		}
	}

	@Override
	public void forEachOrdered(Consumer<? super T> action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			while (spliterator.tryAdvance((value) -> action.accept(value))) {
				// Empty body...
			}
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.next());
			}
		}
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T reduce(T identity, BinaryOperator<T> accumulator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<T> reduce(BinaryOperator<T> accumulator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		if (supplier == null) {
			throw new NullPointerException("Supplier can't be null"); 
		}
		else if (accumulator == null) {
			throw new NullPointerException("Accumulator can't be null"); 
		}
		else if (spliterator != null) {
			try{if (combiner == null) {
					final BiConsumer<R, R>	simpleCombiner = new BiConsumer<R, R>(){
												@Override public void accept(R t, R u) {}
											};
											
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.Executes<R,T>(spliterator, supplier, accumulator, simpleCombiner)).get();
				}
				else {
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.Executes<R,T>(spliterator, supplier, accumulator, combiner)).get();
				}
				
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			final R	result = supplier.get();
			
			while (iterator.hasNext()) {
				accumulator.accept(result, iterator.next());
			}
			return result;
		}
	}

	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count() {
		long	result = 0;
		
		if (spliterator != null) {
			if (spliterator.hasCharacteristics(Spliterator.SIZED)) {
				return spliterator.estimateSize();
			}
			else {
				while (spliterator.tryAdvance((val)->{})) {
					// Empty body;
					result++;
				}
				return result;
			}
		}
		else {
			while (iterator.hasNext()) {
				iterator.next();
				result++;
			}
			return result;
		}
	}

	@Override
	public boolean anyMatch(Predicate<? super T> predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allMatch(Predicate<? super T> predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean noneMatch(Predicate<? super T> predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<T> findFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<T> findAny() {
		// TODO Auto-generated method stub
		return null;
	}

}
