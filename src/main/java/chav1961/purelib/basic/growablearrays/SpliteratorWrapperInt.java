package chav1961.purelib.basic.growablearrays;

import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class SpliteratorWrapperInt implements SpliteratorOfInt {
	private final SpliteratorOfInt	nestedInt;
	private final IntUnaryOperator	opInt;
	private final SpliteratorOfLong	nestedLong;
	private final LongToIntFunction	opLong;
	
	SpliteratorWrapperInt(final SpliteratorOfInt nested, final IntUnaryOperator op) {
		this.nestedInt = nested;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
	}
	
	 SpliteratorWrapperInt(final SpliteratorOfLong nested, final LongToIntFunction op) {
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = op;
	}

	@Override
	public boolean mustBeProcessed(final long sequential, final int value) {
		return nestedInt.mustBeProcessed(sequential, value);
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
	public SpliteratorOfInt trySplit() {
		final SpliteratorOfInt	result = (SpliteratorOfInt)nestedInt.trySplit();
		
		if (result != null) {
			return new SpliteratorWrapperInt(result, opInt);
		}
		else {
			return null;
		}
	}

	@Override
	public boolean tryAdvance(final IntConsumer action) {
		return nestedInt.tryAdvance((int e)->action.accept(opInt.applyAsInt(e))); 
	}

	@Override
	public String toString() {
		return "SpliteratorWrapperInt [nested=" + nestedInt + ", op=" + opInt + "]";
	}
}