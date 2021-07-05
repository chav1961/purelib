package chav1961.purelib.basic.growablearrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;
import chav1961.purelib.basic.growablearrays.GrowableIntArray.SlicedSpliterator;

class IntStreamImpl implements IntStream {
	private final SpliteratorOfInt	spliterator;
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

	IntStreamImpl(final SpliteratorOfInt spliterator, final AutoCloseable close) {
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
		if (predicate == null) {
			throw new NullPointerException("Predicate can't be null");
		}
		else if (spliterator != null) {
			return sequential().filter(predicate);
		}
		else {
			final int[]	stored = new int[1];
			
			return new IntStreamImpl(new IteratorWrapperInt(iterator, (c)-> {
				while (iterator.hasNext()) {
					int value = iterator.nextInt();
					
					if (predicate.test(value)) {
						stored[0] = value;
						return true;
					}
				}
				return false;
			}, (e)->e){
				public int nextInt() {
					return stored[0];
				};
			} , this::close);
		}
	}

	@Override
	public IntStream map(final IntUnaryOperator mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return new IntStreamImpl(new SpliteratorWrapperInt(spliterator, mapper), this::close);
		}
		else {
			return new IntStreamImpl(new IteratorWrapperInt(iterator, (item)->true, mapper), this::close);
		}
	}

	@Override
	public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return new ObjectStreamImpl<U>(new SpliteratorWrapperObj<U>(spliterator, (int val)->mapper.apply(val)), this::close);
		}
		else {
			return new ObjectStreamImpl<U>(new IteratorWrapperObj<U>(iterator, mapper), this::close);
		}
	}

	@Override
	public LongStream mapToLong(final IntToLongFunction mapper) {
		// TODO Auto-generated method stub
		if (spliterator != null) {
			return new LongStreamImpl(new SpliteratorWrapperLong(spliterator,mapper), this::close);
		}
		else {
			return null;
		}
	}

	@Override
	public DoubleStream mapToDouble(final IntToDoubleFunction mapper) {
		// TODO Auto-generated method stub
		if (spliterator != null) {
			return new DoubleStreamImpl(new SpliteratorWrapperDouble(spliterator,mapper), this::close);
		}
		else {
			return null;
		}
	}

	@Override
	public IntStream flatMap(IntFunction<? extends IntStream> mapper) {
		if (mapper == null) {
			throw new NullPointerException("Mapper can't be null");
		}
		else if (spliterator != null) {
			return sequential().flatMap(mapper);
		}
		else {
			return null;// TODO:
		}
	}

	@Override
	public IntStream distinct() {
		final Set<Integer>	values = new HashSet<>();
		
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
	public IntStream sorted() {
		if (spliterator != null) {
			return sequential().sorted();
		}
		else {
			final IteratorWrapperInt	ivi = new IteratorWrapperInt(iterator, null, (e)->e) {
											int[]	content;
											int		index = -1;
											
											@Override
											public boolean hasNext() {
												if (index < 0) {
													final GrowableIntArray	gia = new GrowableIntArray(false);
													
													while (iterator.hasNext()) {
														gia.append(iterator.nextInt());
													}
													content = gia.extract();
													Arrays.sort(content);
													index = 0;
												}
												return index < content.length;
											}
											
											@Override
											public int nextInt() {
												return content[index++];
											}
										};
			return new IntStreamImpl(new IteratorWrapperInt(ivi, (c)->true, (e)->e), this::close);
		}
	}

	@Override
	public IntStream peek(final IntConsumer action) {
		return map((int e)->{action.accept(e); return e;});
	}

	@Override
	public IntStream limit(final long maxSize) {
		if (maxSize < 0) {
			throw new IllegalArgumentException("Max size ["+maxSize+"] can't be negative"); 
		}
		else if (spliterator != null) {
			return sequential().limit(maxSize);
		}
		else {
			return new IntStreamImpl(new IteratorWrapperInt(iterator, (c)-> c < maxSize, (e)->e), this::close);
		}
	}

	@Override
	public IntStream skip(final long n) {
		if (n < 0) {
			throw new IllegalArgumentException("Number of skips ["+n+"] can't be negative"); 
		}
		else if (spliterator != null) {
			return sequential().skip(n);
		}
		else {
			return new IntStreamImpl(new IteratorWrapperInt(iterator, (c)-> {
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
	public void forEach(final IntConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			collect(()->null, (acc, val) ->action.accept(val), null);
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.nextInt());
			}
		}
	}

	@Override
	public void forEachOrdered(final IntConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else if (spliterator != null) {
			while (spliterator.tryAdvance((int value) -> action.accept(value))) {
				// Empty body...
			}
		}
		else {
			while (iterator.hasNext()) {
				action.accept(iterator.nextInt());
			}
		}
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
	public int reduce(final int identity, final IntBinaryOperator op) {
		if (op == null) {
			throw new NullPointerException("Binary operator can't be null"); 
		}
		else if (spliterator != null) {
			if (spliterator.hasCharacteristics(Spliterator.SIZED) && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
				if (spliterator.estimateSize() != 0) {
					try{
						return identity + ArrayUtils.forkJoinPool.submit(new Reduces(spliterator, op)).get()[0];
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e.getLocalizedMessage(),e);
					}
				}
				else {
					return identity;
				}
			}
			else {
				int[]		result = {identity};
				
				while (spliterator.tryAdvance((int e)->result[0] = op.applyAsInt(result[0], iterator.nextInt()))) {
					// Empty body
				}
				return result[0];
			}
		}
		else {
			int	result = identity;
			
			while (iterator.hasNext()) {
				result = op.applyAsInt(result, iterator.nextInt());
			}
			return result;
		}
	}

	@Override
	public OptionalInt reduce(final IntBinaryOperator op) {
		if (op == null) {
			throw new NullPointerException("Binary operator can't be null"); 
		}
		else if (spliterator != null) {
			if (spliterator.hasCharacteristics(Spliterator.SIZED) && spliterator.hasCharacteristics(Spliterator.SUBSIZED)) {
				if (spliterator.estimateSize() != 0) {
					try{
						return OptionalInt.of(ArrayUtils.forkJoinPool.submit(new Reduces(spliterator, op)).get()[0]);
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e.getLocalizedMessage(),e);
					}
				}
				else {
					return OptionalInt.empty();
				}
			}
			else {
				int[]		result = {0};
				boolean[]	inside = {false};
				
				while (spliterator.tryAdvance((int e)->{
					result[0] = op.applyAsInt(result[0], iterator.nextInt());
					inside[0] = true;})) {
					// Empty body
				}
				return inside[0] ? OptionalInt.of(result[0]) : OptionalInt.empty();
			}
		}
		else if (iterator.hasNext()) {
			int	result = iterator.nextInt();
			
			while (iterator.hasNext()) {
				result = op.applyAsInt(result, iterator.nextInt());
			}
			return OptionalInt.of(result);
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public <R> R collect(final Supplier<R> supplier, final ObjIntConsumer<R> accumulator, final BiConsumer<R, R> combiner) {
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
											
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.ExecutesOfInt<R>(spliterator, supplier, accumulator, simpleCombiner)).get();
				}
				else {
					return ArrayUtils.forkJoinPool.submit(new ArrayUtils.ExecutesOfInt<R>(spliterator, supplier, accumulator, combiner)).get();
				}
				
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			final R	result = supplier.get();
			
			while (iterator.hasNext()) {
				accumulator.accept(result, iterator.nextInt());
			}
			return result;
		}
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
		return summaryStatistics().getCount();
	}

	@Override
	public OptionalDouble average() {
		final IntSummaryStatistics	iss = summaryStatistics();
		
		return iss.getCount() == 0 ? OptionalDouble.empty() : OptionalDouble.of(iss.getAverage());
	}

	@Override
	public IntSummaryStatistics summaryStatistics() {
		if (spliterator != null) {
			try{return ArrayUtils.forkJoinPool.submit(new Statistics(spliterator)).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else if (iterator.hasNext()) {
			int		value = iterator.nextInt();
			long	count = 1, sum = value;
			int		min = value, max = value;
			
			while (iterator.hasNext()) {
				value = iterator.nextInt();
				count++;
				sum += value;
				min = Math.min(min, value);
				max = Math.max(min, value);
			}
			return new IntSummaryStatistics(count, min, max, sum);
		}
		else {
			return new IntSummaryStatistics();
		}
	}

	@Override
	public boolean anyMatch(final IntPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test mathes can't be null");
		}
		else if (spliterator != null) {
			final boolean[]	result = {false};
			
			while (!result[0] && spliterator.tryAdvance((int value)->{
					result[0] |= predicate.test(value);
				})) {
				// Empty body...
			}
			return result[0];
		}
		else {
			while (iterator.hasNext()) {
				if (predicate.test(iterator.nextInt())) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public boolean allMatch(final IntPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test mathes can't be null");
		}
		else if (spliterator != null) {
			final boolean[]	tested = {false};
			
			try{return !ArrayUtils.forkJoinPool.submit(new Matches(spliterator, (int value)->{
					tested[0] = true;
					return !predicate.test(value);
				})).get() && tested[0];
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			while (iterator.hasNext()) {
				if (!predicate.test(iterator.nextInt())) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public boolean noneMatch(final IntPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test mathes can't be null");
		}
		else if (spliterator != null) {
			final boolean[]	tested = {false};
			
			try{return !ArrayUtils.forkJoinPool.submit(new Matches(spliterator, (int value)->{
					tested[0] = true;
					return predicate.test(value);
				})).get() && tested[0];
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e.getLocalizedMessage(),e);
			}
		}
		else {
			while (iterator.hasNext()) {
				if (predicate.test(iterator.nextInt())) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public OptionalInt findFirst() {
		if (spliterator != null) {
			final int[]	result = new int[] {0};
			
			if (spliterator.tryAdvance((int value)->{result[0] = value;})) {
				return OptionalInt.of(result[0]);
			}
			else {
				return OptionalInt.empty();
			}
		}
		else if (iterator.hasNext()) {
			return OptionalInt.of(iterator.nextInt());
		}
		else {
			return OptionalInt.empty();
		}
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
		return mapToDouble((e)->(double)e);
	}

	@Override
	public Stream<Integer> boxed() {
		return mapToObj((e)->Integer.valueOf(e));
	}

	@Override
	public IntStream sequential() {
		final GrowableIntArray	gia = new GrowableIntArray(false);
		
		gia.append(toArray());
		return new IntStreamImpl(gia.getIterator(),this::close);
	}

	@Override
	public IntStream parallel() {
		if (spliterator != null && spliterator().hasCharacteristics(Spliterator.CONCURRENT)) {
			return this;
		}
		else {
			final GrowableIntArray	gia = new GrowableIntArray(false);
			
			gia.append(toArray());
			return new IntStreamImpl(gia);
		}
	}

	@Override
	public OfInt iterator() {
		return iterator;
	}

	@Override
	public SpliteratorOfInt spliterator() {
		return spliterator;
	}
	
	private static class SpliteratorWrapperInt implements SpliteratorOfInt {
		private final SpliteratorOfInt	nested;
		private final IntUnaryOperator	op;
		
		private SpliteratorWrapperInt(final SpliteratorOfInt nested, final IntUnaryOperator op) {
			this.nested = nested;
			this.op = op;
		}
		
		@Override
		public boolean mustBeProcessed(final long sequential, final int value) {
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
		public SpliteratorOfInt trySplit() {
			final SpliteratorOfInt	result = (SpliteratorOfInt)nested.trySplit();
			
			if (result != null) {
				return new SpliteratorWrapperInt(result, op);
			}
			else {
				return null;
			}
		}

		@Override
		public boolean tryAdvance(final IntConsumer action) {
			return nested.tryAdvance((int e)->action.accept(op.applyAsInt(e))); 
		}

		@Override
		public String toString() {
			return "SpliteratorWrapperInt [nested=" + nested + ", op=" + op + "]";
		}
	}

	private static class SpliteratorWrapperLong implements SpliteratorOfLong {
		private final SpliteratorOfInt	nested;
		private final IntToLongFunction	op;
		
		private SpliteratorWrapperLong(final SpliteratorOfInt nested, final IntToLongFunction op) {
			this.nested = nested;
			this.op = op;
		}
		
		@Override
		public boolean mustBeProcessed(final long sequential, final long value) {
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
		public OfLong trySplit() {
			final SpliteratorOfInt	result = (SpliteratorOfInt)nested.trySplit();
			
			if (result != null) {
				return new SpliteratorWrapperLong(result, op);
			}
			else {
				return null;
			}
		}

		@Override
		public boolean tryAdvance(final LongConsumer action) {
			return nested.tryAdvance((int e)->action.accept(op.applyAsLong(e))); 
		}

		@Override
		public String toString() {
			return "SpliteratorWrapperLong [nested=" + nested + ", op=" + op + "]";
		}
	}

	private static class SpliteratorWrapperDouble implements SpliteratorOfDouble {
		private final SpliteratorOfInt		nested;
		private final IntToDoubleFunction	op;
		
		private SpliteratorWrapperDouble(final SpliteratorOfInt nested, final IntToDoubleFunction op) {
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
		public OfDouble trySplit() {
			final SpliteratorOfInt	result = (SpliteratorOfInt)nested.trySplit();
			
			if (result != null) {
				return new SpliteratorWrapperDouble(result, op);
			}
			else {
				return null;
			}
		}

		@Override
		public boolean tryAdvance(final DoubleConsumer action) {
			return nested.tryAdvance((int e)->action.accept(op.applyAsDouble(e))); 
		}

		@Override
		public String toString() {
			return "SpliteratorWrapperLong [nested=" + nested + ", op=" + op + "]";
		}
	}
	
	private static class IteratorWrapperInt implements OfInt {
		private final OfInt				nested;
		private final IntPredicate		test;
		private final IntUnaryOperator	op;
		private int						count = 0;
		
		private IteratorWrapperInt(final OfInt nested, final IntPredicate predicate, final  IntUnaryOperator op) {
			this.nested = nested;
			this.test = predicate;
			this.op = op;
		}
		
		@Override
		public boolean hasNext() {
			return nested.hasNext() && test.test(count);
		}

		@Override
		public int nextInt() {
			count++;
			return op.applyAsInt(nested.nextInt());
		}

		@Override
		public String toString() {
			return "IteratorWrapperInt [nested=" + nested + ", op=" + op + "]";
		}
	}
	
	private static class SpliteratorWrapperObj<R> implements Spliterator<R> {
		private final SpliteratorOfInt	nested;
		private final IntFunction<R>	op;
		
		private SpliteratorWrapperObj(final SpliteratorOfInt nested, final IntFunction<R> op) {
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
		public Spliterator<R> trySplit() {
			final SpliteratorOfInt	result = (SpliteratorOfInt)nested.trySplit();
			
			if (result != null) {
				return new SpliteratorWrapperObj<R>(result, op);
			}
			else {
				return null;
			}
		}
		
		@Override
		public boolean tryAdvance(Consumer<? super R> action) {
			return nested.tryAdvance((int e)->action.accept(op.apply(e)));
		}

		@Override
		public String toString() {
			return "SpliteratorWrapperInt [nested=" + nested + ", op=" + op + "]";
		}
	}

	private static class IteratorWrapperObj<U> implements Iterator<U> {
		private final OfInt						nested;
		private final IntFunction<? extends U>	op;
		
		private IteratorWrapperObj(final OfInt nested, final IntFunction<? extends U> mapper) {
			this.nested = nested;
			this.op = mapper;
		}

		@Override
		public boolean hasNext() {
			return nested.hasNext();
		}

		@Override
		public U next() {
			return op.apply(nested.nextInt());
		}
	}

	private static class Reduces extends RecursiveTask<int[]> {
		private static final long 		serialVersionUID = 1L;
		
		private final SpliteratorOfInt	spliterator;
		private final IntBinaryOperator	op;

        public Reduces(final SpliteratorOfInt spliterator, final IntBinaryOperator op) {
            this.spliterator = spliterator;
            this.op = op;
        }

        @Override
        protected int[] compute() {
        	final SpliteratorOfInt		leftSplit = (SpliteratorOfInt)spliterator.trySplit();

           	if (leftSplit == null) {
           		final int[]	result = new int[1];
           		
           		while (spliterator.tryAdvance((int value)->{
           				result[0] = op.applyAsInt(result[0],value);
           			})) {
           			// empty body...
           		}
           		return result;
           	}
           	else {
               	final Reduces		leftReduce = new Reduces(leftSplit, op);
            	
               	leftReduce.fork();            	
            	final int[]			right = this.compute(), left = leftReduce.join();
            	
            	left[0] = op.applyAsInt(left[0], right[0]);
           		return left;
        	}
        }

		@Override
		public String toString() {
			return "Reduces [spliterator=" + spliterator + "]";
		}
    }
	
	private static class Statistics extends RecursiveTask<IntSummaryStatistics> {
		private static final long 		serialVersionUID = 1L;
		
		private final SpliteratorOfInt	spliterator;

        public Statistics(final SpliteratorOfInt spliterator) {
            this.spliterator = spliterator;
        }

        @Override
        protected IntSummaryStatistics compute() {
        	final SpliteratorOfInt		leftSplit = (SpliteratorOfInt)spliterator.trySplit();

           	if (leftSplit == null) {
           		final long[]	result = new long[] {0, Long.MAX_VALUE, Long.MIN_VALUE, 0};
           		
           		while (spliterator.tryAdvance((int value)->{
           				result[0]++;
           				result[1] = Math.min(result[1], value);
           				result[2] = Math.max(result[2], value);
           				result[3] += value; 
           			})) {
           			// empty body...
           		}
           		return new IntSummaryStatistics(result[0], (int)result[1], (int)result[2], result[3]);
           	}
           	else {
               	final Statistics			leftStat = new Statistics(leftSplit);
            	
               	leftStat.fork();            	
            	final IntSummaryStatistics	right = this.compute(), left = leftStat.join();
            	
           		return new IntSummaryStatistics(left.getCount()+right.getCount(), Math.min(left.getMin(), right.getMin()), Math.max(left.getMax(), right.getMax()), left.getSum() + right.getSum());
        	}
        }

		@Override
		public String toString() {
			return "Statistics [spliterator=" + spliterator + "]";
		}
    }

	private static class Matches extends RecursiveTask<Boolean> {
		private static final long 		serialVersionUID = 1L;

		@FunctionalInterface
		private interface MatchesTest {
			boolean test(final int value);
		}
		
		private final SpliteratorOfInt	spliterator;
		private final MatchesTest		test;

		
        public Matches(final SpliteratorOfInt spliterator, final MatchesTest test) {
            this.spliterator = spliterator;
            this.test = test;
        }

        @Override
        protected Boolean compute() {
        	final SpliteratorOfInt		leftSplit = (SpliteratorOfInt)spliterator.trySplit();

           	if (leftSplit == null) {
           		final boolean[]	result = new boolean[] {false};
           		
           		while (!result[0] && spliterator.tryAdvance((int value)->result[0] = test.test(value))) {
           			// empty body...
           		}
           		return result[0];
           	}
           	else {
               	final Matches		leftMatches = new Matches(leftSplit, test);
            	
               	leftMatches.fork();            	
            	final Boolean		right = this.compute();
            	
           		return right || leftMatches.join();
        	}
        }

		@Override
		public String toString() {
			return "Matches [spliterator=" + spliterator + "]";
		}
    }

}
