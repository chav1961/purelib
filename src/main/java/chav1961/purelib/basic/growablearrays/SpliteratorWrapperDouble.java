package chav1961.purelib.basic.growablearrays;

import java.util.function.DoubleConsumer;
import java.util.function.IntToDoubleFunction;
import java.util.function.LongToDoubleFunction;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class SpliteratorWrapperDouble implements SpliteratorOfDouble {
	private enum WrapperType {
		Int, Long, Double;
	}
	
	private final WrapperType			type;
	private final SpliteratorOfInt		nestedInt;
	private final IntToDoubleFunction	opInt;
	private final SpliteratorOfLong		nestedLong;
	private final LongToDoubleFunction	opLong;
	
	SpliteratorWrapperDouble(final SpliteratorOfInt nested, final IntToDoubleFunction op) {
		this.type = WrapperType.Int;
		this.nestedInt = nested;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
	}
	
	public SpliteratorWrapperDouble(final SpliteratorOfLong nested, final LongToDoubleFunction op) {
		this.type = WrapperType.Long;
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
		switch (type) {
			case Int	: return nestedInt.estimateSize();
			case Long	: return nestedLong.estimateSize();
			case Double	:
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet"); 
		}
	}

	@Override
	public int characteristics() {
		switch (type) {
			case Int	: return nestedInt.characteristics();
			case Long	: return nestedLong.characteristics();
			case Double	:
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet"); 
		}
	}

	@Override
	public OfDouble trySplit() {
		switch (type) {
			case Int	:
				final SpliteratorOfInt	resultInt = (SpliteratorOfInt)nestedInt.trySplit();
				
				if (resultInt != null) {
					return new SpliteratorWrapperDouble(resultInt, opInt);
				}
				else {
					return null;
				}
			case Long	:
				final SpliteratorOfLong	resultLong = (SpliteratorOfLong)nestedLong.trySplit();
				
				if (resultLong != null) {
					return new SpliteratorWrapperDouble(resultLong, opLong);
				}
				else {
					return null;
				}
			case Double	:
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet"); 
		}
	}

	@Override
	public boolean tryAdvance(final DoubleConsumer action) {
		switch (type) {
			case Int	: return nestedInt.tryAdvance((int e)->action.accept(opInt.applyAsDouble(e)));
			case Long	: return nestedLong.tryAdvance((long e)->action.accept(opLong.applyAsDouble(e)));
			case Double	:
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet"); 
		}
		 
	}

	@Override
	public String toString() {
		return "SpliteratorWrapperDouble [type=" + type + ", nestedInt=" + nestedInt + ", opInt=" + opInt + ", nestedLong=" + nestedLong + ", opLong=" + opLong + "]";
	}
}