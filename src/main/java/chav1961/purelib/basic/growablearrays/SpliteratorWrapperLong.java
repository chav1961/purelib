package chav1961.purelib.basic.growablearrays;

import java.util.function.DoubleToLongFunction;
import java.util.function.IntToLongFunction;
import java.util.function.LongConsumer;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class SpliteratorWrapperLong implements SpliteratorOfLong {
	private enum WrapperType {
		Int, Long, Double
	}
	
	private final WrapperType			type;
	private final SpliteratorOfInt		nestedInt;
	private final IntToLongFunction		opInt;
	private final SpliteratorOfDouble	nestedDouble;
	private final DoubleToLongFunction	opDouble;
	
	SpliteratorWrapperLong(final SpliteratorOfInt nested, final IntToLongFunction op) {
		this.type = WrapperType.Int; 
		this.nestedInt = nested;
		this.opInt = op;
		this.nestedDouble = null;
		this.opDouble = null;
	}

	SpliteratorWrapperLong(final SpliteratorOfDouble nested, final DoubleToLongFunction op) {
		this.type = WrapperType.Double; 
		this.nestedInt = null;
		this.opInt = null;
		this.nestedDouble = nested;
		this.opDouble = op;
	}
	
	@Override
	public boolean mustBeProcessed(final long sequential, final long value) {
		return true;
	}
	
	@Override
	public long estimateSize() {
		switch (type) {
			case Double	: return nestedDouble.estimateSize();
			case Int	: return nestedInt.estimateSize();
			case Long	:
			default		: throw new UnsupportedOperationException("Wapper type ["+type+"] is not supported yet");
		}
	}

	@Override
	public int characteristics() {
		switch (type) {
			case Double	: return nestedDouble.characteristics();
			case Int	: return nestedInt.characteristics();
			case Long	:
			default		: throw new UnsupportedOperationException("Wapper type ["+type+"] is not supported yet");
		}
	}

	@Override
	public OfLong trySplit() {
		switch (type) {
			case Double	:
				final SpliteratorOfDouble	resultDouble = (SpliteratorOfDouble)nestedDouble.trySplit();
				
				if (resultDouble != null) {
					return new SpliteratorWrapperLong(resultDouble, opDouble);
				}
				else {
					return null;
				}
			case Int	:
				final SpliteratorOfInt	resultInt = (SpliteratorOfInt)nestedInt.trySplit();
				
				if (resultInt != null) {
					return new SpliteratorWrapperLong(resultInt, opInt);
				}
				else {
					return null;
				}
			case Long	:
			default		: throw new UnsupportedOperationException("Wapper type ["+type+"] is not supported yet");
		}
		
	}

	@Override
	public boolean tryAdvance(final LongConsumer action) {
		switch (type) {
			case Double	: return nestedDouble.tryAdvance((double e)->action.accept(opDouble.applyAsLong(e)));
			case Int	: return nestedInt.tryAdvance((int e)->action.accept(opInt.applyAsLong(e)));
			case Long	:
			default		: throw new UnsupportedOperationException("Wapper type ["+type+"] is not supported yet");
		}
		 
	}

	@Override
	public String toString() {
		return "SpliteratorWrapperLong [type=" + type + ", nestedInt=" + nestedInt + ", opInt=" + opInt + ", nestedDouble=" + nestedDouble + ", opDouble=" + opDouble + "]";
	}
}