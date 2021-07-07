package chav1961.purelib.basic.growablearrays;

import java.util.Iterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

class IteratorWrapperObj<U> implements Iterator<U> {
	private enum WrapperType {
		Int, Long, Double;
	}
	
	private final WrapperType					type;
	private final OfInt							nestedInt;
	private final IntFunction<? extends U>		opInt;
	private final OfLong						nestedLong;
	private final LongFunction<? extends U>		opLong;
	private final OfDouble						nestedDouble;
	private final DoubleFunction<? extends U>	opDouble;
	
	IteratorWrapperObj(final OfInt nested, final IntFunction<? extends U> mapper) {
		this.type = WrapperType.Int;
		this.nestedInt = nested;
		this.opInt = mapper;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = null;
		this.opDouble = null;
	}

	IteratorWrapperObj(final OfLong nested, final LongFunction<? extends U> mapper) {
		this.type = WrapperType.Long;
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = mapper;
		this.nestedDouble = null;
		this.opDouble = null;
	}

	IteratorWrapperObj(final OfDouble nested, final DoubleFunction<? extends U> mapper) {
		this.type = WrapperType.Double;
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = nested;
		this.opDouble = mapper;
	}
	
	@Override
	public boolean hasNext() {
		switch (type) {
			case Double	: return nestedDouble.hasNext();
			case Int	: return nestedInt.hasNext();
			case Long	: return nestedLong.hasNext();
			default		: throw new UnsupportedOperationException("Wapper type ["+type+"] is not suported yet");
		}
	}

	@Override
	public U next() {
		switch (type) {
			case Double	: return opDouble.apply(nestedDouble.nextDouble());
			case Int	: return opInt.apply(nestedInt.nextInt());
			case Long	: return opLong.apply(nestedLong.nextLong());
			default		: throw new UnsupportedOperationException("Wapper type ["+type+"] is not suported yet");
		}
	}

	@Override
	public String toString() {
		return "IteratorWrapperObj [type=" + type + ", nestedInt=" + nestedInt + ", opInt=" + opInt + ", nestedLong=" + nestedLong + ", opLong=" + opLong + ", nestedDouble=" + nestedDouble + ", opDouble=" + opDouble + "]";
	}
}