package chav1961.purelib.basic.growablearrays;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator.OfInt;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

class IntStreamImpl implements IntStream {
	private final Spliterator.OfInt	spliterator;
	private final OfInt				iterator;
	private final AutoCloseable		close;
	private List<Runnable>			handlers = null;
	
	IntStreamImpl(final GrowableIntArray gia) {
		this.spliterator = gia.getSpliterator();
		this.iterator = null;
		this.close = null;
	}

	IntStreamImpl(final OfInt iterator, final AutoCloseable close) {
		this.spliterator = null;
		this.iterator = iterator;
		this.close = close; 
	}

	IntStreamImpl(final Spliterator.OfInt spliterator, final AutoCloseable close) {
		this.spliterator = spliterator;
		this.iterator = null;
		this.close = close; 
	}
	
	@Override
	public boolean isParallel() {
		return spliterator != null ? (spliterator.characteristics() & Spliterator.SUBSIZED) != 0 : false;
	}

	@Override
	public IntStream unordered() {
		return this;
	}

	@Override
	public IntStream onClose(final Runnable closeHandler) {
		if (closeHandler == null) {
			throw new NullPointerException("Close handler can't be null"); 
		}
		else {
			if (handlers == null) {
				handlers = new ArrayList<>();
			}
			handlers.add(closeHandler);
			return this;
		}
	}

	@Override
	public void close() {
		try {
			this.close.close();
		} catch (Exception e) {
		}
		if (handlers != null) {
			for (Runnable item : handlers) {
				item.run();
			}
		}
	}

	@Override
	public IntStream filter(final IntPredicate predicate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream map(final IntUnaryOperator mapper) {
		if (spliterator != null) {
			return new IntStreamImpl(new SpliteratorWrapper(spliterator, mapper), close);
		}
		else {
			
		}
		return null;
	}

	@Override
	public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
		// TODO Auto-generated method stub
		if (spliterator != null) {
			return new ObjectStreamImpl<U>(new Spliterator<U>() {

				@Override
				public boolean tryAdvance(Consumer<? super U> action) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public Spliterator<U> trySplit() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public long estimateSize() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int characteristics() {
					// TODO Auto-generated method stub
					return 0;
				}}, this::close);
		}
		return null;
	}

	@Override
	public LongStream mapToLong(final IntToLongFunction mapper) {
		// TODO Auto-generated method stub
		if (spliterator != null) {
			return new LongStreamImpl((Spliterator.OfLong)null, this::close);
		}
		else {
			return null;
		}
	}

	@Override
	public DoubleStream mapToDouble(IntToDoubleFunction mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream flatMap(IntFunction<? extends IntStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream distinct() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream sorted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream peek(final IntConsumer action) {
		return map((int e)->{action.accept(e); return e;});
	}

	@Override
	public IntStream limit(long maxSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream skip(long n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forEach(final IntConsumer action) {
		forEachOrdered(action);
	}

	@Override
	public void forEachOrdered(final IntConsumer action) {
		// TODO Auto-generated method stub
	}

	@Override
	public int[] toArray() {
		if (spliterator != null) {
			if ((spliterator.characteristics() & Spliterator.SUBSIZED) != 0) {
				final int[]	result = new int[(int)spliterator.estimateSize()];
				
				for (int index = 0; index < result.length; index++) {
					final int	currentIndex = index;
					spliterator.tryAdvance((int e)->result[currentIndex] = e);
				}
				return result;
			}
			else {
				final GrowableIntArray	gia = new GrowableIntArray(false);
				
				while (spliterator.tryAdvance((int e)->gia.append(e))) {
				}
				return gia.extract();
			}
		}
		else {
			final GrowableIntArray	gia = new GrowableIntArray(false);
			
			while (iterator.hasNext()) {
				gia.append(iterator.nextInt());
			}
			return gia.extract();
		}
	}

	@Override
	public int reduce(int identity, IntBinaryOperator op) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OptionalInt reduce(IntBinaryOperator op) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int sum() {
		return (int)summaryStatistics().getSum();
	}

	@Override
	public OptionalInt min() {
		final IntSummaryStatistics	iss = summaryStatistics();
		
		return iss.getCount() == 0 ? OptionalInt.empty() : OptionalInt.of(iss.getMin());
	}

	@Override
	public OptionalInt max() {
		final IntSummaryStatistics	iss = summaryStatistics();
		
		return iss.getCount() == 0 ? OptionalInt.empty() : OptionalInt.of(iss.getMax());
	}

	@Override
	public long count() {
		return (int)summaryStatistics().getCount();
	}

	@Override
	public OptionalDouble average() {
		final IntSummaryStatistics	iss = summaryStatistics();
		
		return iss.getCount() == 0 ? OptionalDouble.empty() : OptionalDouble.of(1.0 * iss.getSum() / iss.getCount());
	}

	@Override
	public IntSummaryStatistics summaryStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean anyMatch(IntPredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allMatch(IntPredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean noneMatch(IntPredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OptionalInt findFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OptionalInt findAny() {
		return findFirst();
	}

	@Override
	public LongStream asLongStream() {
		return mapToLong((e)->(long)e);
	}

	@Override
	public DoubleStream asDoubleStream() {
		return mapToDouble((e)->(long)e);
	}

	@Override
	public Stream<Integer> boxed() {
		return mapToObj((e)->Integer.valueOf(e));
	}

	@Override
	public IntStream sequential() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream parallel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OfInt iterator() {
		return iterator;
	}

	@Override
	public Spliterator.OfInt spliterator() {
		return spliterator;
	}
	
	private static class SpliteratorWrapper implements Spliterator.OfInt {
		private final Spliterator.OfInt	nested;
		private final IntUnaryOperator	op;
		
		private SpliteratorWrapper(final Spliterator.OfInt nested, final IntUnaryOperator op) {
			this.nested = nested;
			this.op = op;
		}
		
		@Override
		public long estimateSize() {
			return nested.estimateSize();
		}

		@Override
		public int characteristics() {
			return nested.characteristics();
		}

		@Override
		public OfInt trySplit() {
			return nested.trySplit();
		}

		@Override
		public boolean tryAdvance(final IntConsumer action) {
			if (op != null) {
				return nested.tryAdvance((int e)->action.accept(op.applyAsInt(e)));
			}
			else {
				return nested.tryAdvance((int e)->action.accept(e));
			}
		}
	}
}
