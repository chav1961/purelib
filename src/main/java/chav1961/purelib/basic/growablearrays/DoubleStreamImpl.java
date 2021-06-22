package chav1961.purelib.basic.growablearrays;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfLong;
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
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream mapToInt(DoubleToIntFunction mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream mapToLong(DoubleToLongFunction mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream distinct() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream sorted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream peek(final DoubleConsumer action) {
		return map((double e)->{action.accept(e); return e;});
	}

	@Override
	public DoubleStream limit(long maxSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream skip(long n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forEach(DoubleConsumer action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void forEachOrdered(DoubleConsumer action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double reduce(double identity, DoubleBinaryOperator op) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OptionalDouble reduce(DoubleBinaryOperator op) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allMatch(DoublePredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean noneMatch(DoublePredicate predicate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OptionalDouble findFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OptionalDouble findAny() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<Double> boxed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream sequential() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DoubleStream parallel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OfDouble iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Spliterator.OfDouble spliterator() {
		// TODO Auto-generated method stub
		return null;
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
