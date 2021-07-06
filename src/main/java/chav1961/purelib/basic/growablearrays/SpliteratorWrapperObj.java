package chav1961.purelib.basic.growablearrays;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class SpliteratorWrapperObj<R> implements Spliterator<R> {
	private final SpliteratorOfInt	nestedInt;
	private final IntFunction<R>	opInt;
	private final SpliteratorOfLong	nestedLong;
	private final LongFunction<R>	opLong;
	
	SpliteratorWrapperObj(final SpliteratorOfInt nested, final IntFunction<R> op) {
		this.nestedInt = nested;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
	}
	
	public SpliteratorWrapperObj(final SpliteratorOfLong nested, final LongFunction<R> op) {
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = op;
	}

	@Override
	public long estimateSize() {
		return nestedInt.estimateSize();
	}

	@Override
	public int characteristics() {
		return nestedInt.characteristics();
	}

	@Override
	public Spliterator<R> trySplit() {
		final SpliteratorOfInt	result = (SpliteratorOfInt)nestedInt.trySplit();
		
		if (result != null) {
			return new SpliteratorWrapperObj<R>(result, opInt);
		}
		else {
			return null;
		}
	}
	
	@Override
	public boolean tryAdvance(Consumer<? super R> action) {
		return nestedInt.tryAdvance((int e)->action.accept(opInt.apply(e)));
	}

	@Override
	public String toString() {
		return "SpliteratorWrapperInt [nested=" + nestedInt + ", op=" + opInt + "]";
	}
}