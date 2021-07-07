package chav1961.purelib.basic.growablearrays;

import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntToDoubleFunction;
import java.util.function.LongToDoubleFunction;

class IteratorWrapperDouble implements OfDouble {
	private final OfInt					nestedInt;
	private final IntToDoubleFunction	mapperInt;
	private final OfLong				nestedLong;
	private final LongToDoubleFunction	mapperLong;
	
	IteratorWrapperDouble(final OfInt nested, final IntToDoubleFunction mapper) {
		this.nestedInt = nested;
		this.mapperInt = mapper;
		this.nestedLong = null;
		this.mapperLong = null;
	}
	
	public IteratorWrapperDouble(final OfLong nested, final LongToDoubleFunction mapper) {
		this.nestedInt = null;
		this.mapperInt = null;
		this.nestedLong = nested;
		this.mapperLong = mapper;
	}

	@Override
	public boolean hasNext() {
		return nestedInt != null ? nestedInt.hasNext() : nestedLong.hasNext();
	}

	@Override
	public double nextDouble() {
		if (mapperInt != null) {
			return mapperInt.applyAsDouble(nestedInt.nextInt());
		}
		else {
			return mapperLong.applyAsDouble(nestedLong.nextLong());
		}
	}

	@Override
	public String toString() {
		return "IteratorWrapperDouble [nestedInt=" + nestedInt + ", mapperInt=" + mapperInt + ", nestedLong=" + nestedLong + ", mapperLong=" + mapperLong + "]";
	}
}