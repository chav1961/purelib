package chav1961.purelib.basic.growablearrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;

class DoubleStreamImpl implements DoubleStream {
	private final SpliteratorOfDouble	spliterator;
	private final OfDouble				iterator;
	private final AutoCloseable			close;
	private List<Runnable>				handlers = null;
	
	public DoubleStreamImpl(final GrowableDoubleArray gda) { 
		this.spliterator = gda.getSpliterator();
		this.iterator = null;
		this.close = null;
	}

	public DoubleStreamImpl(final SpliteratorOfDouble spliterator, final AutoCloseable close) {
		this.spliterator = spliterator;
		this.iterator = null;
		this.close = close; 
	}

	public DoubleStreamImpl(final OfDouble iterator, final AutoCloseable close) {
		this.spliterator = null;
		this.iterator = iterator;
		this.close = close; 
	}

	@Override
	public boolean isParallel() {
		return spliterator != null ? (spliterator.characteristics() & Spliterator.SUBSIZED) != 0 : false;
	}

	@Override
	public DoubleStream unordered() {
		return this;
	}

	@Override
	public DoubleStream onClose(Runnable closeHandler) {
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
	public DoubleStream filter(DoublePredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate can't be null");
		}
		else if (spliterator != null) {
			return sequential().filter(predicate);
		}
		else {
			final double[]	stored = new double[1];
			
			return new DoubleStreamImpl(new IteratorWrapperDouble(iterator, (c)-> {
				while (iterator.hasNext()) {
					double value = iterator.nextDouble();
					
					if (predicate.test(value)) {
						stored[0] = value;
						return true;
					}
				}
				return false;
			}, (e)->e){
				public double nextDouble() {
					return stored[0];
				};
			} , this::close);
		}
	}

	@Override
	public DoubleStream map(DoubleUnaryOperator mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return new DoubleStreamImpl(new SpliteratorWrapperDouble(spliterator, mapper), this::close);
		}
		else {
			return new DoubleStreamImpl(new IteratorWrapperDouble(iterator, (item)->true, mapper), this::close);
		}
	}

	@Override
	public <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return new ObjectStreamImpl<U>(new SpliteratorWrapperObj<U>(spliterator, (double val)->mapper.apply(val)), this::close);
		}
		else {
			return new ObjectStreamImpl<U>(new IteratorWrapperObj<U>(iterator, mapper), this::close);
		}
	}

	@Override
	public IntStream mapToInt(DoubleToIntFunction mapper) {
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
	public LongStream mapToLong(DoubleToLongFunction mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null"); 
		}
		else if (spliterator != null) {
			return new LongStreamImpl(new SpliteratorWrapperLong(spliterator,mapper), this::close);
		}
		else {
			return new LongStreamImpl(new IteratorWrapperLong(iterator,mapper), this::close);
		}
	}

	@Override
	public DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return sequential().flatMap(mapper);
		}
		else {
			final IteratorWrapperDouble	ivi = new IteratorWrapperDouble(iterator, null, (e)->e) {
												DoubleStream	nestedStream = null;
												OfDouble		nested = null; 
				
												@Override
												public boolean hasNext() {
													if (nested == null) {
														if (iterator.hasNext()) {
															nestedStream = mapper.apply(iterator.nextDouble());
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
												public double nextDouble() {
													return nested.nextDouble();
												}
											};
			return new DoubleStreamImpl(new IteratorWrapperDouble(ivi, (c)->true, (e)->e), this::close);
		}
	}

	@Override
	public DoubleStream distinct() {
		final Set<Double>	values = new HashSet<>();
		
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
	public DoubleStream sorted() {
		if (spliterator != null) {
			return sequential().sorted();
		}
		else {
			final IteratorWrapperDouble	ivi = new IteratorWrapperDouble(iterator, null, (e)->e) {
											double[]	content;
											int			index = -1;
											
											@Override
											public boolean hasNext() {
												if (index < 0) {
													final GrowableDoubleArray	gia = new GrowableDoubleArray(false);
													
													while (iterator.hasNext()) {
														gia.append(iterator.nextDouble());
													}
													content = gia.extract();
													Arrays.sort(content);
													index = 0;
												}
												return index < content.length;
											}
											
											@Override
											public double nextDouble() {
												return content[index++];
											}
										};
			return new DoubleStreamImpl(new IteratorWrapperDouble(ivi, (c)->true, (e)->e), this::close);
		}
	}

	@Override
	public DoubleStream peek(final DoubleConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null"); 
		}
		else {
			return map((double e)->{action.accept(e); return e;});
		}
	}

	@Override
	public DoubleStream limit(long maxSize) {
		if (maxSize < 0) {
			throw new IllegalArgumentException("Max size ["+maxSize+"] can't be negative"); 
		}
		else if (spliterator != null) {
			return sequential().limit(maxSize);
		}
		else {
			return new DoubleStreamImpl(new IteratorWrapperDouble(iterator, (c)-> c < maxSize, (e)->e), this::close);
		}
	}

	@Override
	public DoubleStream skip(long n) {
		if (n < 0) {
			throw new IllegalArgumentException("Number of skips ["+n+"] can't be negative"); 
		}
		else if (spliterator != null) {
			return sequential().skip(n);
		}
		else {
			return new DoubleStreamImpl(new IteratorWrapperDouble(iterator, (c)-> {
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
	public void forEach(DoubleConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			collect(()->null, (acc, val) ->action.accept(val), null);
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.nextDouble());
			}
		}
	}

	@Override
	public void forEachOrdered(DoubleConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			while (spliterator.tryAdvance((double value) -> action.accept(value))) {
				// Empty body...
			}
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.nextDouble());
			}
		}
	}

	@Override
	public double[] toArray() {
		if (spliterator != null) {
			if ((spliterator.characteristics() & Spliterator.SUBSIZED) != 0) {
				final double[]	result = new double[(int)spliterator.estimateSize()];
				
				for (int index = 0; index < result.length; index++) {
					final int	currentIndex = index;
					spliterator.tryAdvance((double e)->result[currentIndex] = e);
				}
				return result;
			}
			else {
				final GrowableDoubleArray	gia = new GrowableDoubleArray(false);
				
				while (spliterator.tryAdvance((double e)->gia.append(e))) {
				}
				return gia.extract();
			}
		}
		else {
			final GrowableDoubleArray	gia = new GrowableDoubleArray(false);
			
			while (iterator.hasNext()) {
				gia.append(iterator.nextDouble());
			}
			return gia.extract();
		}
	}

	@Override
	public double reduce(double identity, DoubleBinaryOperator op) {
		if (op == null) {
			throw new NullPointerException("Binary operator can't be null"); 
		}
		else if (spliterator != null) {
			if (spliterator.hasCharacteristics(Spliterator.SIZED) && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
				if (spliterator.estimateSize() != 0) {
					try{
						return identity + ArrayUtils.forkJoinPool.submit(new Reduces<double[]>(spliterator, op)).get()[0];
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e.getLocalizedMessage(),e);
					}
				}
				else {
					return identity;
				}
			}
			else {
				double[]	result = {identity};
				
				while (spliterator.tryAdvance((double e)->result[0] = op.applyAsDouble(result[0], iterator.nextDouble()))) {
					// Empty body
				}
				return result[0];
			}
		}
		else {
			double result = identity;
			
			while (iterator.hasNext()) {
				result = op.applyAsDouble(result, iterator.nextDouble());
			}
			return result;
		}
	}

	@Override
	public OptionalDouble reduce(DoubleBinaryOperator op) {
		if (op == null) {
			throw new NullPointerException("Binary operator can't be null"); 
		}
		else if (spliterator != null) {
			if (spliterator.hasCharacteristics(Spliterator.SIZED) && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
				if (spliterator.estimateSize() != 0) {
					try{
						return OptionalDouble.of(ArrayUtils.forkJoinPool.submit(new Reduces<double[]>(spliterator, op)).get()[0]);
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e.getLocalizedMessage(),e);
					}
				}
				else {
					return OptionalDouble.empty();
				}
			}
			else {
				double[]	result = {0};
				boolean[]	inside = {false};
				
				while (spliterator.tryAdvance((double e)->{
					result[0] = op.applyAsDouble(result[0], iterator.nextDouble());
					inside[0] = true;})) {
					// Empty body
				}
				return inside[0] ? OptionalDouble.of(result[0]) : OptionalDouble.empty();
			}
		}
		else if (iterator.hasNext()) {
			double result = iterator.nextDouble();
			
			while (iterator.hasNext()) {
				result = op.applyAsDouble(result, iterator.nextDouble());
			}
			return OptionalDouble.of(result);
		}
		else {
			return OptionalDouble.empty();
		}
	}

	@Override
	public <R> R collect(final Supplier<R> supplier, final ObjDoubleConsumer<R> accumulator, final BiConsumer<R, R> combiner) {
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
											
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.ExecutesOfDouble<R>(spliterator, supplier, accumulator, simpleCombiner)).get();
				}
				else {
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.ExecutesOfDouble<R>(spliterator, supplier, accumulator, combiner)).get();
				}
				
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			final R	result = supplier.get();
			
			while (iterator.hasNext()) {
				accumulator.accept(result, iterator.nextDouble());
			}
			return result;
		}
	}

	@Override
	public double sum() {
		return summaryStatistics().getSum();
	}

	@Override
	public OptionalDouble min() {
		final DoubleSummaryStatistics	lss = summaryStatistics();
		
		return lss.getCount() == 0 ? OptionalDouble.empty() : OptionalDouble.of(lss.getMin());
	}

	@Override
	public OptionalDouble max() {
		final DoubleSummaryStatistics	lss = summaryStatistics();
		
		return lss.getCount() == 0 ? OptionalDouble.empty() : OptionalDouble.of(lss.getMax());
	}

	@Override
	public long count() {
		return summaryStatistics().getCount();
	}

	@Override
	public OptionalDouble average() {
		final DoubleSummaryStatistics	lss = summaryStatistics();
		
		return lss.getCount() == 0 ? OptionalDouble.empty() : OptionalDouble.of(lss.getAverage());
	}

	@Override
	public DoubleSummaryStatistics summaryStatistics() {
		if (spliterator != null) {
			try{return ArrayUtils.forkJoinPool.submit(new Statistics(spliterator)).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else if (iterator.hasNext()) {
			double	value = iterator.nextDouble();
			long	count = 1;
			double	sum = value, min = value, max = value;
			
			while (iterator.hasNext()) {
				value = iterator.nextDouble();
				count++;
				sum += value;
				min = Math.min(min, value);
				max = Math.max(min, value);
			}
			return new DoubleSummaryStatistics(count, min, max, sum);
		}
		else {
			return new DoubleSummaryStatistics();
		}
	}

	@Override
	public boolean anyMatch(DoublePredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test mathes can't be null");
		}
		else if (spliterator != null) {
			final boolean[]	result = {false};
			
			while (!result[0] && spliterator.tryAdvance((double value)->{
					result[0] |= predicate.test(value);
				})) {
				// Empty body...
			}
			return result[0];
		}
		else {
			while (iterator.hasNext()) {
				if (predicate.test(iterator.nextDouble())) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public boolean allMatch(DoublePredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test mathes can't be null");
		}
		else if (spliterator != null) {
			final boolean[]	tested = {false};
			
			try{return !ArrayUtils.forkJoinPool.submit(new Matches(spliterator, (double value)->{
					tested[0] = true;
					return !predicate.test(value);
				})).get() && tested[0];
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			while (iterator.hasNext()) {
				if (!predicate.test(iterator.nextDouble())) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public boolean noneMatch(DoublePredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test mathes can't be null");
		}
		else if (spliterator != null) {
			final boolean[]	tested = {false};
			
			try{return !ArrayUtils.forkJoinPool.submit(new Matches(spliterator, (double value)->{
					tested[0] = true;
					return predicate.test(value);
				})).get() && tested[0];
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			while (iterator.hasNext()) {
				if (predicate.test(iterator.nextDouble())) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public OptionalDouble findFirst() {
		if (spliterator != null) {
			final double[]	result = new double[] {0};
			
			if (spliterator.tryAdvance((double value)->{result[0] = value;})) {
				return OptionalDouble.of(result[0]);
			}
			else {
				return OptionalDouble.empty();
			}
		}
		else if (iterator.hasNext()) {
			return OptionalDouble.of(iterator.nextDouble());
		}
		else {
			return OptionalDouble.empty();
		}
	}

	@Override
	public OptionalDouble findAny() {
		return findFirst();
	}

	@Override
	public Stream<Double> boxed() {
		return mapToObj((e)->Double.valueOf(e));
	}

	@Override
	public DoubleStream sequential() {
		final GrowableDoubleArray	gia = new GrowableDoubleArray(false);
		
		gia.append(toArray());
		return new DoubleStreamImpl(gia.getIterator(),this::close);
	}

	@Override
	public DoubleStream parallel() {
		if (spliterator != null && spliterator().hasCharacteristics(Spliterator.CONCURRENT)) {
			return this;
		}
		else {
			final GrowableDoubleArray	gia = new GrowableDoubleArray(false);
			
			gia.append(toArray());
			return new DoubleStreamImpl(gia);
		}
	}

	@Override
	public OfDouble iterator() {
		return iterator;
	}

	@Override
	public java.util.Spliterator.OfDouble spliterator() {
		return spliterator();
	}

	private static class SpliteratorWrapperDouble implements SpliteratorOfDouble {
		private final SpliteratorOfDouble	nested;
		private final DoubleUnaryOperator	op;
		
		private SpliteratorWrapperDouble(final SpliteratorOfDouble nested, final DoubleUnaryOperator op) {
			this.nested = nested;
			this.op = op;
		}

		@Override
		public boolean mustBeProcessed(final long sequential, final double value) {
			return true;
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
		public SpliteratorOfDouble trySplit() {
			final SpliteratorOfDouble	result = (SpliteratorOfDouble)nested.trySplit();
			
			if (result != null) {
				return new SpliteratorWrapperDouble(result, op);
			}
			else {
				return null;
			}
		}

		@Override
		public boolean tryAdvance(final DoubleConsumer action) {
			return nested.tryAdvance((double e)->action.accept(op.applyAsDouble(e))); 
		}

		@Override
		public String toString() {
			return "SpliteratorWrapperDouble [nested=" + nested + ", op=" + op + "]";
		}
	}

	private static class IteratorWrapperDouble implements OfDouble {
		private final OfDouble				nested;
		private final DoublePredicate		test;
		private final DoubleUnaryOperator	op;
		private int						count = 0;
		
		private IteratorWrapperDouble(final OfDouble nested, final DoublePredicate predicate, final DoubleUnaryOperator op) {
			this.nested = nested;
			this.test = predicate;
			this.op = op;
		}
		
		@Override
		public boolean hasNext() {
			return nested.hasNext() && test.test(count);
		}

		@Override
		public double nextDouble() {
			count++;
			return op.applyAsDouble(nested.nextDouble());
		}

		@Override
		public String toString() {
			return "IteratorWrapperDouble [nested=" + nested + ", op=" + op + "]";
		}
	}
	
	private static class Statistics extends RecursiveTask<DoubleSummaryStatistics> {
		private static final long 		serialVersionUID = 1L;
		
		private final Spliterator.OfDouble	spliterator;

        public Statistics(final Spliterator.OfDouble spliterator) {
            this.spliterator = spliterator;
        }

        @Override
        protected DoubleSummaryStatistics compute() {
        	final Spliterator.OfDouble	leftSplit = spliterator.trySplit();

           	if (leftSplit == null) {
           		final long[]	result = new long[] {0};
           		final double[]	resultD = new double[] {Double.MAX_VALUE, Double.MIN_VALUE, 0};
           		
           		while (spliterator.tryAdvance((double value)->{
           				result[0]++;
           				resultD[0] = Math.min(resultD[0], value);
           				resultD[1] = Math.max(resultD[1], value);
           				resultD[2] += value; 
           			})) {
           			// empty body...
           		}
           		return new DoubleSummaryStatistics(result[0], resultD[0], resultD[1], resultD[2]);
           	}
           	else {
               	final Statistics			leftStat = new Statistics(leftSplit);
            	
               	leftStat.fork();            	
            	final DoubleSummaryStatistics	right = this.compute(), left = leftStat.join();
            	
           		return new DoubleSummaryStatistics(left.getCount()+right.getCount(), Math.min(left.getMin(), right.getMin()), Math.max(left.getMax(), right.getMax()), left.getSum() + right.getSum());
        	}
        }

		@Override
		public String toString() {
			return "Statistics [spliterator=" + spliterator + "]";
		}
    }
	
}
