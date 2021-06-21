package chav1961.purelib.basic.growablearrays;

import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

class ArrayUtils {
	static final ForkJoinPool 	forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

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
		
		private final Spliterator.OfInt	spliterator;
		private final Supplier<R> 		supplier;
		private final ObjIntConsumer<R>	accumulator;
		private final BiConsumer<R, R> 	combiner;

        public ExecutesOfInt(final Spliterator.OfInt spliterator, final Supplier<R> supplier, final ObjIntConsumer<R> accumulator, final BiConsumer<R, R> combiner) {
            this.spliterator = spliterator;
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
        }

        @Override
        protected R compute() {
        	final Spliterator.OfInt		leftSplit = spliterator.trySplit();

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
			return "Executes [spliterator=" + spliterator + "]";
		}
    }
}
