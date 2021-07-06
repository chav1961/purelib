package chav1961.purelib.basic.growablearrays;

import java.util.Spliterator.OfDouble;
import java.util.function.DoubleConsumer;
import java.util.function.IntToDoubleFunction;
import java.util.function.LongToDoubleFunction;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class SpliteratorWrapperDouble implements SpliteratorOfDouble {
	private final SpliteratorOfInt		nestedInt;
	private final IntToDoubleFunction	opInt;
	private final SpliteratorOfLong		nestedLong;
	private final LongToDoubleFunction	opLong;
	
	SpliteratorWrapperDouble(final SpliteratorOfInt nested, final IntToDoubleFunction op) {
		this.nestedInt = nested;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
	}
	
	public SpliteratorWrapperDouble(final SpliteratorOfLong nested, final LongToDoubleFunction op) {
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = op;
	}

	@Override
	public boolean mustBeProcessed(final long sequential, final double value) {
		return true;
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
	public OfDouble trySplit() {
		final SpliteratorOfInt	result = (SpliteratorOfInt)nestedInt.trySplit();
		
		if (result != null) {
			return new SpliteratorWrapperDouble(result, opInt);
		}
		else {
			return null;
		}
	}

	@Override
	public boolean tryAdvance(final DoubleConsumer action) {
		return nestedInt.tryAdvance((int e)->action.accept(opInt.applyAsDouble(e))); 
	}

	@Override
	public String toString() {
		return "SpliteratorWrapperLong [nested=" + nestedInt + ", op=" + opInt + "]";
	}
}