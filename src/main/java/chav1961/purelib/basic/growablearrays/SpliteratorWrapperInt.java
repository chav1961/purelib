package chav1961.purelib.basic.growablearrays;

import java.util.function.DoubleToIntFunction;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class SpliteratorWrapperInt implements SpliteratorOfInt {
	private enum SplitType {
		Int, Long, Double
	}
	
	private final SplitType				type;
	private final SpliteratorOfInt		nestedInt;
	private final IntUnaryOperator		opInt;
	private final SpliteratorOfLong		nestedLong;
	private final LongToIntFunction		opLong;
	private final SpliteratorOfDouble	nestedDouble;
	private final DoubleToIntFunction	opDouble;
	
	SpliteratorWrapperInt(final SpliteratorOfInt nested, final IntUnaryOperator op) {
		this.type = SplitType.Int;
		this.nestedInt = nested;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = null;
		this.opDouble = null;
	}
	
	 SpliteratorWrapperInt(final SpliteratorOfLong nested, final LongToIntFunction op) {
		this.type = SplitType.Long;
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = op;
		this.nestedDouble = null;
		this.opDouble = null;
	}

	 SpliteratorWrapperInt(final SpliteratorOfDouble nested, final DoubleToIntFunction op) {
		this.type = SplitType.Double;
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = nested;
		this.opDouble = op;
	}
	 
	@Override
	public boolean mustBeProcessed(final long sequential, final int value) {
		switch (type) {
			case Double	: return nestedDouble.mustBeProcessed(sequential, value);
			case Int	: return nestedInt.mustBeProcessed(sequential, value);
			case Long	: return nestedLong.mustBeProcessed(sequential, value);
			default		: throw new UnsupportedOperationException("Split type ["+type+"] is not supported yet"); 
		}
	}
	
	@Override
	public long estimateSize() {
		switch (type) {
			case Double	: return nestedDouble.estimateSize();
			case Int	: return nestedInt.estimateSize();
			case Long	: return nestedLong.estimateSize();
			default		: throw new UnsupportedOperationException("Split type ["+type+"] is not supported yet"); 
		}
	}

	@Override
	public int characteristics() {
		switch (type) {
			case Double	: return nestedDouble.characteristics();
			case Int	: return nestedInt.characteristics();
			case Long	: return nestedLong.characteristics();
			default		: throw new UnsupportedOperationException("Split type ["+type+"] is not supported yet"); 
		}
	}

	@Override
	public SpliteratorOfInt trySplit() {
		switch (type) {
			case Double	:
				final SpliteratorOfDouble	resultDouble = (SpliteratorOfDouble)nestedDouble.trySplit();
				
				if (resultDouble != null) {
					return new SpliteratorWrapperInt(resultDouble, opDouble);
				}
				else {
					return null;
				}
			case Int	: 
				final SpliteratorOfInt	resultInt = (SpliteratorOfInt)nestedInt.trySplit();
				
				if (resultInt != null) {
					return new SpliteratorWrapperInt(resultInt, opInt);
				}
				else {
					return null;
				}
			case Long	: 
				final SpliteratorOfLong	resultLong = (SpliteratorOfLong)nestedLong.trySplit();
				
				if (resultLong != null) {
					return new SpliteratorWrapperInt(resultLong, opLong);
				}
				else {
					return null;
				}
			default		: throw new UnsupportedOperationException("Split type ["+type+"] is not supported yet"); 
		}
	}

	@Override
	public boolean tryAdvance(final IntConsumer action) {
		switch (type) {
			case Double	: return nestedDouble.tryAdvance((double e)->action.accept(opDouble.applyAsInt(e)));
			case Int	: return nestedInt.tryAdvance((int e)->action.accept(opInt.applyAsInt(e)));
			case Long	: return nestedLong.tryAdvance((long e)->action.accept(opLong.applyAsInt(e)));
			default		: throw new UnsupportedOperationException("Split type ["+type+"] is not supported yet"); 
		}
		 
	}

	@Override
	public String toString() {
		return "SpliteratorWrapperInt [type=" + type + ", nestedInt=" + nestedInt + ", opInt=" + opInt + ", nestedLong=" + nestedLong + ", opLong=" + opLong + ", nestedDouble=" + nestedDouble + ", opDouble=" + opDouble + "]";
	}
}