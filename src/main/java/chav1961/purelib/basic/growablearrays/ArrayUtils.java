package chav1961.purelib.basic.growablearrays;

import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

class ArrayUtils {
	static final ForkJoinPool 	forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

	@FunctionalInterface
	interface ProcessedOfInt {
		boolean mustBeProcessed(long sequential, int value);
	}
	
	interface SpliteratorOfInt extends Spliterator.OfInt, ProcessedOfInt {
		// Empty body...
	}

	@FunctionalInterface
	interface ProcessedOfLong {
		boolean mustBeProcessed(long sequential, long value);
	}
	
	interface SpliteratorOfLong extends Spliterator.OfLong, ProcessedOfLong {
		// Empty body...
	}

	@FunctionalInterface
	interface ProcessedOfDouble {
		boolean mustBeProcessed(long sequential, double value);
	}
	
	interface SpliteratorOfDouble extends Spliterator.OfDouble, ProcessedOfDouble {
		// Empty body...
	}
	
	static class Executes<R,T> extends RecursiveTask<R> {
		private static final long 		serialVersionUID = 1L;

		@FunctionalInterface
		interface MatchesTest {
			boolean test(final int value);
		}
		
		private final Spliterator<T>			spliterator;
		private final Supplier<R> 				supplier;
		private final BiConsumer<R,? super T>	accumulator;
		private final BiConsumer<R, R> 			combiner;

        public Executes(final Spliterator<T> spliterator, final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
            this.spliterator = spliterator;
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
        }
 
        @Override
        protected R compute() {
        	final Spliterator<T>		leftSplit = spliterator.trySplit();

           	if (leftSplit == null) {
           		final R	result = supplier.get();
           		
           		while (spliterator.tryAdvance((value)->accumulator.accept(result, value))) {
           			// empty body...
           		}
           		return result;
           	}
           	else {
               	final Executes<R,T>		leftExec = new Executes<R,T>(leftSplit, supplier, accumulator, combiner);
            	
               	leftExec.fork();            	
            	final R					right = this.compute(), left = leftExec.join();
            	
           		combiner.accept(left, right);
           		return left;
        	}
        }

		@Override
		public String toString() {
			return "Executes [spliterator=" + spliterator + "]";
		}
    }
	
	static class ExecutesOfInt<R> extends RecursiveTask<R> {
		private static final long 		serialVersionUID = 1L;

		@FunctionalInterface
		interface MatchesTest {
			boolean test(final int value);
		}
		
		private final SpliteratorOfInt	spliterator;
		private final Supplier<R> 		supplier;
		private final ObjIntConsumer<R>	accumulator;
		private final BiConsumer<R, R> 	combiner;

        public ExecutesOfInt(final SpliteratorOfInt spliterator, final Supplier<R> supplier, final ObjIntConsumer<R> accumulator, final BiConsumer<R, R> combiner) {
            this.spliterator = spliterator;
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
        }

        @Override
        protected R compute() {
        	final SpliteratorOfInt		leftSplit = (SpliteratorOfInt)spliterator.trySplit();

           	if (leftSplit == null) {
           		final R	result = supplier.get();
           		
           		while (spliterator.tryAdvance((int value)->accumulator.accept(result, value))) {
           			// empty body...
           		}
           		return result;
           	}
           	else {
               	final ExecutesOfInt<R>		leftExec = new ExecutesOfInt<R>(leftSplit, supplier, accumulator, combiner);
            	
               	leftExec.fork();            	
            	final R					right = this.compute(), left = leftExec.join();
            	
           		combiner.accept(left, right);
           		return left;
        	}
        }

		@Override
		public String toString() {
			return "ExecutesOfInt [spliterator=" + spliterator + "]";
		}
    }

	static class ExecutesOfLong<R> extends RecursiveTask<R> {
		private static final long 		serialVersionUID = 1L;

		@FunctionalInterface
		interface MatchesTest {
			boolean test(final int value);
		}
		
		private final SpliteratorOfLong		spliterator;
		private final Supplier<R> 			supplier;
		private final ObjLongConsumer<R>	accumulator;
		private final BiConsumer<R, R> 		combiner;

        public ExecutesOfLong(final SpliteratorOfLong spliterator, final Supplier<R> supplier, final ObjLongConsumer<R> accumulator, final BiConsumer<R, R> combiner) {
            this.spliterator = spliterator;
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
        }

        @Override
        protected R compute() {
        	final SpliteratorOfLong		leftSplit = (SpliteratorOfLong)spliterator.trySplit();

           	if (leftSplit == null) {
           		final R	result = supplier.get();
           		
           		while (spliterator.tryAdvance((long value)->accumulator.accept(result, value))) {
           			// empty body...
           		}
           		return result;
           	}
           	else {
               	final ExecutesOfLong<R>		leftExec = new ExecutesOfLong<R>(leftSplit, supplier, accumulator, combiner);
            	
               	leftExec.fork();            	
            	final R					right = this.compute(), left = leftExec.join();
            	
           		combiner.accept(left, right);
           		return left;
        	}
        }

		@Override
		public String toString() {
			return "ExecutesOfLong [spliterator=" + spliterator + "]";
		}
    }

	static class ExecutesOfDouble<R> extends RecursiveTask<R> {
		private static final long 		serialVersionUID = 1L;

		@FunctionalInterface
		interface MatchesTest {
			boolean test(final int value);
		}
		
		private final SpliteratorOfDouble	spliterator;
		private final Supplier<R> 			supplier;
		private final ObjDoubleConsumer<R>	accumulator;
		private final BiConsumer<R, R> 		combiner;

        public ExecutesOfDouble(final SpliteratorOfDouble spliterator, final Supplier<R> supplier, final ObjDoubleConsumer<R> accumulator, final BiConsumer<R, R> combiner) {
            this.spliterator = spliterator;
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
        }

        @Override
        protected R compute() {
        	final SpliteratorOfDouble		leftSplit = (SpliteratorOfDouble)spliterator.trySplit();

           	if (leftSplit == null) {
           		final R	result = supplier.get();
           		
           		while (spliterator.tryAdvance((double value)->accumulator.accept(result, value))) {
           			// empty body...
           		}
           		return result;
           	}
           	else {
               	final ExecutesOfDouble<R>	leftExec = new ExecutesOfDouble<R>(leftSplit, supplier, accumulator, combiner);
            	
               	leftExec.fork();            	
            	final R					right = this.compute(), left = leftExec.join();
            	
           		combiner.accept(left, right);
           		return left;
        	}
        }

		@Override
		public String toString() {
			return "ExecutesOfDouble [spliterator=" + spliterator + "]";
		}
    }
}
