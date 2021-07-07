package chav1961.purelib.basic.growablearrays;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class SpliteratorWrapperObj<R> implements Spliterator<R> {
	private enum WrapperType {
		Int, Long, Double
	}
	
	private final WrapperType			type;
	private final SpliteratorOfInt		nestedInt;
	private final IntFunction<R>		opInt;
	private final SpliteratorOfLong		nestedLong;
	private final LongFunction<R>		opLong;
	private final SpliteratorOfDouble	nestedDouble;
	private final DoubleFunction<R>		opDouble;
	
	SpliteratorWrapperObj(final SpliteratorOfInt nested, final IntFunction<R> op) {
		this.type = WrapperType.Int; 
		this.nestedInt = nested;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = null;
		this.opDouble = null;
	}
	
	public SpliteratorWrapperObj(final SpliteratorOfLong nested, final LongFunction<R> op) {
		this.type = WrapperType.Long; 
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = op;
		this.nestedDouble = null;
		this.opDouble = null;
	}

	public SpliteratorWrapperObj(final SpliteratorOfDouble nested, final DoubleFunction<R> op) {
		this.type = WrapperType.Double; 
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = nested;
		this.opDouble = op;
	}
	
	@Override
	public long estimateSize() {
		switch (type) {
			case Double	: return nestedDouble.estimateSize();
			case Int	: return nestedInt.estimateSize();
			case Long	: return nestedLong.estimateSize();
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
	}

	@Override
	public int characteristics() {
		switch (type) {
			case Double	: return nestedDouble.characteristics();
			case Int	: return nestedInt.characteristics();
			case Long	: return nestedLong.characteristics();
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
	}

	@Override
	public Spliterator<R> trySplit() {
		switch (type) {
			case Double	:
				final SpliteratorOfDouble	resultDouble = (SpliteratorOfDouble)nestedDouble.trySplit();
				
				if (resultDouble != null) {
					return new SpliteratorWrapperObj<R>(resultDouble, opDouble);
				}
				else {
					return null;
				}
			case Int	:
				final SpliteratorOfInt	resultInt = (SpliteratorOfInt)nestedInt.trySplit();
				
				if (resultInt != null) {
					return new SpliteratorWrapperObj<R>(resultInt, opInt);
				}
				else {
					return null;
				}
			case Long	:
				final SpliteratorOfLong	resultLong = (SpliteratorOfLong)nestedLong.trySplit();
				
				if (resultLong != null) {
					return new SpliteratorWrapperObj<R>(resultLong, opLong);
				}
				else {
					return null;
				}
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
	}
	
	@Override
	public boolean tryAdvance(Consumer<? super R> action) {
		switch (type) {
			case Double	: return nestedDouble.tryAdvance((double e)->action.accept(opDouble.apply(e)));
			case Int	: return nestedInt.tryAdvance((int e)->action.accept(opInt.apply(e)));
			case Long	: return nestedLong.tryAdvance((long e)->action.accept(opLong.apply(e)));
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
		
	}

	@Override
	public String toString() {
		return "SpliteratorWrapperObj [type=" + type + ", nestedInt=" + nestedInt + ", opInt=" + opInt + ", nestedLong=" + nestedLong + ", opLong=" + opLong + ", nestedDouble=" + nestedDouble + ", opDouble=" + opDouble + "]";
	}
}