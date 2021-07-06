package chav1961.purelib.basic.growablearrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Set;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class LongStreamImpl implements LongStream {
	private final SpliteratorOfLong		spliterator;
	private final OfLong				iterator;
	private final AutoCloseable			close;
	private List<Runnable>				handlers = null;
	
	public LongStreamImpl(final GrowableLongArray gla) {
		this.spliterator = gla.getSpliterator();
		this.iterator = null;
		this.close = null;
	}

	public LongStreamImpl(final SpliteratorOfLong spliterator, final AutoCloseable close) {
		this.spliterator = spliterator;
		this.iterator = null;
		this.close = close; 
	}

	public LongStreamImpl(final OfLong iterator, final AutoCloseable close) {
		this.spliterator = null;
		this.iterator = iterator;
		this.close = close; 
	}
	
	@Override
	public boolean isParallel() {
		return spliterator != null ? (spliterator.characteristics() & Spliterator.SUBSIZED) != 0 : false;
	}

	@Override
	public LongStream unordered() {
		return this;
	}

	@Override
	public LongStream onClose(final Runnable closeHandler) {
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
	public LongStream filter(LongPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate can't be null");
		}
		else if (spliterator != null) {
			return sequential().filter(predicate);
		}
		else {
			final long[]	stored = new long[1];
			
			return new LongStreamImpl(new IteratorWrapperLong(iterator, (c)-> {
				while (iterator.hasNext()) {
					long value = iterator.nextLong();
					
					if (predicate.test(value)) {
						stored[0] = value;
						return true;
					}
				}
				return false;
			}, (e)->e){
				public long nextLong() {
					return stored[0];
				};
			} , this::close);
		}
	}

	@Override
	public LongStream map(final LongUnaryOperator mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return new LongStreamImpl(new SpliteratorWrapperLong(spliterator, mapper), this::close);
		}
		else {
			return new LongStreamImpl(new IteratorWrapperLong(iterator, (item)->true, mapper), this::close);
		}
	}

	@Override
	public <U> Stream<U> mapToObj(LongFunction<? extends U> mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return new ObjectStreamImpl<U>(new SpliteratorWrapperObj<U>(spliterator, (long val)->mapper.apply(val)), this::close);
		}
		else {
			return new ObjectStreamImpl<U>(new IteratorWrapperObj<U>(iterator, mapper), this::close);
		}
	}

	@Override
	public IntStream mapToInt(LongToIntFunction mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null"); 
		}
		else if (spliterator != null) {
			return new IntStreamImpl(new SpliteratorWrapperInt(spliterator,mapper), this::close);
		}
		else {
			return new IntStreamImpl(new IteratorWrapperInt(iterator,mapper), this::close);
		}
	}

	@Override
	public DoubleStream mapToDouble(LongToDoubleFunction mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null"); 
		}
		else if (spliterator != null) {
			return new DoubleStreamImpl(new SpliteratorWrapperDouble(spliterator,mapper), this::close);
		}
		else {
			return new DoubleStreamImpl(new IteratorWrapperDouble(iterator,mapper), this::close);
		}
	}

	@Override
	public LongStream flatMap(LongFunction<? extends LongStream> mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return sequential().flatMap(mapper);
		}
		else {
			final IteratorWrapperLong	ivi = new IteratorWrapperLong(iterator, null, (e)->e) {
												LongStream	nestedStream = null;
												OfLong		nested = null; 
				
												@Override
												public boolean hasNext() {
													if (nested == null) {
														if (iterator.hasNext()) {
															nestedStream = mapper.apply(iterator.nextLong());
															if (nestedStream != null) {
																nested = nestedStream.iterator();
															}
															else {
																nested = null;
															}
															return hasNext();
														}
														else {
															return false;
														}
													}
													else {
														if (nested.hasNext()) {
															return true;
														}
														else {
															nested = null;
															nestedStream.close();
															nestedStream = null;
															return hasNext();
														}
													}
												}
												
												@Override
												public long nextLong() {
													return nested.nextLong();
												}
											};
			return new LongStreamImpl(new IteratorWrapperLong(ivi, (c)->true, (e)->e), this::close);
		}
	}

	@Override
	public LongStream distinct() {
		final Set<Long>	values = new HashSet<>();
		
		return filter((e)->{
			if (values.contains(e)) {
				return false;
			}
			else {
				values.add(e);
				return true;
			}
		});
	}

	@Override
	public LongStream sorted() {
		if (spliterator != null) {
			return sequential().sorted();
		}
		else {
			final IteratorWrapperLong	ivi = new IteratorWrapperLong(iterator, null, (e)->e) {
											long[]	content;
											int		index = -1;
											
											@Override
											public boolean hasNext() {
												if (index < 0) {
													final GrowableLongArray	gia = new GrowableLongArray(false);
													
													while (iterator.hasNext()) {
														gia.append(iterator.nextLong());
													}
													content = gia.extract();
													Arrays.sort(content);
													index = 0;
												}
												return index < content.length;
											}
											
											@Override
											public long nextLong() {
												return content[index++];
											}
										};
			return new LongStreamImpl(new IteratorWrapperLong(ivi, (c)->true, (e)->e), this::close);
		}
	}

	@Override
	public LongStream peek(final LongConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null"); 
		}
		else {
			return map((long e)->{action.accept(e); return e;});
		}
	}

	@Override
	public LongStream limit(long maxSize) {
		if (maxSize < 0) {
			throw new IllegalArgumentException("Max size ["+maxSize+"] can't be negative"); 
		}
		else if (spliterator != null) {
			return sequential().limit(maxSize);
		}
		else {
			return new LongStreamImpl(new IteratorWrapperLong(iterator, (c)-> c < maxSize, (e)->e), this::close);
		}
	}

	@Override
	public LongStream skip(long n) {
		if (n < 0) {
			throw new IllegalArgumentException("Number of skips ["+n+"] can't be negative"); 
		}
		else if (spliterator != null) {
			return sequential().skip(n);
		}
		else {
			return new LongStreamImpl(new IteratorWrapperLong(iterator, (c)-> {
				if (c == 0) {
					boolean	result;
					int 	count = 0;
					
					while ((result = iterator.hasNext()) && count < n) {
						iterator.next();
						count++;
					}
					return result;
				}
				else {
					return true;
				}
			}, (e)->e), this::close);
		}
	}

	@Override
	public void forEach(LongConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			collect(()->null, (acc, val) ->action.accept(val), null);
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.nextLong());
			}
		}
	}

	@Override
	public void forEachOrdered(LongConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			while (spliterator.tryAdvance((long value) -> action.accept(value))) {
				// Empty body...
			}
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.nextLong());
			}
		}
	}

	@Override
	public long[] toArray() {
		if (spliterator != null) {
			if ((spliterator.characteristics() & Spliterator.SUBSIZED) != 0) {
				final long[]	result = new long[(int)spliterator.estimateSize()];
				
				for (int index = 0; index < result.length; index++) {
					final int	currentIndex = index;
					spliterator.tryAdvance((long e)->result[currentIndex] = e);
				}
				return result;
			}
			else {
				final GrowableLongArray	gia = new GrowableLongArray(false);
				
				while (spliterator.tryAdvance((long e)->gia.append(e))) {
				}
				return gia.extract();
			}
		}
		else {
			final GrowableLongArray	gia = new GrowableLongArray(false);
			
			while (iterator.hasNext()) {
				gia.append(iterator.nextLong());
			}
			return gia.extract();
		}
	}

	@Override
	public long reduce(long identity, LongBinaryOperator op) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OptionalLong reduce(LongBinaryOperator op) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> accumulator, BiConsumer<R, R> combiner) {
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
											
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.ExecutesOfLong<R>(spliterator, supplier, accumulator, simpleCombiner)).get();
				}
				else {
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.ExecutesOfLong<R>(spliterator, supplier, accumulator, combiner)).get();
				}
				 
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			final R	result = supplier.get();
			
			while (iterator.hasNext()) {
				accumulator.accept(result, iterator.nextLong());
			}
			return result;
		}
	}

	@Override
	public long sum() {
		return summaryStatistics().getSum();
	}

	@Override
	public OptionalLong min() {
		final LongSummaryStatistics	lss = summaryStatistics();
		
		return lss.getCount() == 0 ? OptionalLong.empty() : OptionalLong.of(lss.getMin());
	}

	@Override
	public OptionalLong max() {
		final LongSummaryStatistics	lss = summaryStatistics();
		
		return lss.getCount() == 0 ? OptionalLong.empty() : OptionalLong.of(lss.getMax());
	}

	@Override
	public long count() {
		return summaryStatistics().getCount();
	}

	@Override
	public OptionalDouble average() {
		final LongSummaryStatistics	lss = summaryStatistics();
		
		return lss.getCount() == 0 ? OptionalDouble.empty() : OptionalDouble.of(lss.getAverage());
	}

	@Override
	public LongSummaryStatistics summaryStatistics() {
		if (spliterator != null) {
			try{return ArrayUtils.forkJoinPool.submit(new Statistics(spliterator)).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else if (iterator.hasNext()) {
			long	value = iterator.nextLong();
			long	count = 1, sum = value;
			long	min = value, max = value;
			
			while (iterator.hasNext()) {
				value = iterator.nextLong();
				count++;
				sum += value;
				min = Math.min(min, value);
				max = Math.max(min, value);
			}
			return new LongSummaryStatistics(count, min, max, sum);
		}
		else {
			return new LongSummaryStatistics();
		}
	}

	@Override
	public boolean anyMatch(LongPredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allMatch(LongPredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean noneMatch(LongPredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OptionalLong findFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OptionalLong findAny() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream asDoubleStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Long> boxed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream sequential() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream parallel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OfLong iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Spliterator.OfLong spliterator() {
		// TODO Auto-generated method stub
		return null;
	}

	private static class SpliteratorWrapperLong implements SpliteratorOfLong {
		private final SpliteratorOfLong	nested;
		private final LongUnaryOperator		op;
		
		private SpliteratorWrapperLong(final SpliteratorOfLong nested, final LongUnaryOperator op) {
			this.nested = nested;
			this.op = op;
		}

		@Override
		public boolean mustBeProcessed(final long sequential, final long value) {
			return nested.mustBeProcessed(sequential, value);
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
		public SpliteratorOfLong trySplit() {
			final SpliteratorOfLong	result = (SpliteratorOfLong)nested.trySplit();
			
			if (result != null) {
				return new SpliteratorWrapperLong(result, op);
			}
			else {
				return null;
			}
		}

		@Override
		public boolean tryAdvance(final LongConsumer action) {
			return nested.tryAdvance((long e)->action.accept(op.applyAsLong(e))); 
		}

		@Override
		public String toString() {
			return "SpliteratorWrapperLong [nested=" + nested + ", op=" + op + "]";
		}
	}

	private static class IteratorWrapperLong implements OfLong {
		private final OfLong			nested;
		private final LongPredicate		test;
		private final LongUnaryOperator	op;
		private int						count = 0;
		
		private IteratorWrapperLong(final OfLong nested, final LongPredicate predicate, final LongUnaryOperator op) {
			this.nested = nested;
			this.test = predicate;
			this.op = op;
		}
		
		@Override
		public boolean hasNext() {
			return nested.hasNext() && test.test(count);
		}

		@Override
		public long nextLong() {
			count++;
			return op.applyAsLong(nested.nextLong());
		}

		@Override
		public String toString() {
			return "IteratorWrapperLong [nested=" + nested + ", op=" + op + "]";
		}
	}
	
	private static class Statistics extends RecursiveTask<LongSummaryStatistics> {
		private static final long 		serialVersionUID = 1L;
		
		private final Spliterator.OfLong	spliterator;

        public Statistics(final Spliterator.OfLong spliterator) {
            this.spliterator = spliterator;
        }

        @Override
        protected LongSummaryStatistics compute() {
        	final Spliterator.OfLong	leftSplit = spliterator.trySplit();

           	if (leftSplit == null) {
           		final long[]	result = new long[] {0, Long.MAX_VALUE, Long.MIN_VALUE, 0};
           		
           		while (spliterator.tryAdvance((long value)->{
           				result[0]++;
           				result[1] = Math.min(result[1], value);
           				result[2] = Math.max(result[2], value);
           				result[3] += value; 
           			})) {
           			// empty body...
           		}
           		return new LongSummaryStatistics(result[0], result[1], result[2], result[3]);
           	}
           	else {
               	final Statistics			leftStat = new Statistics(leftSplit);
            	
               	leftStat.fork();            	
            	final LongSummaryStatistics	right = this.compute(), left = leftStat.join();
            	
           		return new LongSummaryStatistics(left.getCount()+right.getCount(), Math.min(left.getMin(), right.getMin()), Math.max(left.getMax(), right.getMax()), left.getSum() + right.getSum());
        	}
        }

		@Override
		public String toString() {
			return "Statistics [spliterator=" + spliterator + "]";
		}
    }
}
