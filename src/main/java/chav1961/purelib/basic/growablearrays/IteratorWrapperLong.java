package chav1961.purelib.basic.growablearrays;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.DoubleToLongFunction;
import java.util.function.IntToLongFunction;

class IteratorWrapperLong implements OfLong {
	private enum WrapperType {
		Int, Long, Double;
	}
	
	private final WrapperType			type;
	private final OfInt					nestedInt;
	private final IntToLongFunction		mapperInt;
	private final OfDouble				nestedDouble;
	private final DoubleToLongFunction	mapperDouble;
	
	IteratorWrapperLong(final OfInt nested, final IntToLongFunction mapper) {
		this.type = WrapperType.Int;
		this.nestedInt = nested;
		this.mapperInt = mapper;
		this.nestedDouble = null;
		this.mapperDouble = null;
	}

	IteratorWrapperLong(final OfDouble nested, final DoubleToLongFunction mapper) {
		this.type = WrapperType.Double;
		this.nestedInt = null;
		this.mapperInt = null;
		this.nestedDouble = nested;
		this.mapperDouble = mapper;
	}
	
	@Override
	public boolean hasNext() {
		switch (type) {
			case Double	: return nestedDouble.hasNext();
			case Int	: return nestedInt.hasNext();
			case Long	:
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
	}

	@Override
	public long nextLong() {
		switch (type) {
			case Double	: return mapperDouble.applyAsLong(nestedDouble.nextDouble());
			case Int	: return mapperInt.applyAsLong(nestedInt.nextInt());
			case Long	:
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
		
	}

	@Override
	public String toString() {
		return "IteratorWrapperLong [nested=" + nestedInt + ", mapper=" + mapperInt + "]";
	}
}