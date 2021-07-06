package chav1961.purelib.basic.growablearrays;

import java.util.Iterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

class IteratorWrapperObj<U> implements Iterator<U> {
	private final OfInt						nestedInt;
	private final IntFunction<? extends U>	opInt;
	private final OfLong					nestedLong;
	private final LongFunction<? extends U>	opLong;
	
	IteratorWrapperObj(final OfInt nested, final IntFunction<? extends U> mapper) {
		this.nestedInt = nested;
		this.opInt = mapper;
		this.nestedLong = null;
		this.opLong = null;
	}

	IteratorWrapperObj(final OfLong nested, final LongFunction<? extends U> mapper) {
		this.nestedInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = mapper;
	}

	@Override
	public boolean hasNext() {
		return nestedInt.hasNext();
	}

	@Override
	public U next() {
		return opInt.apply(nestedInt.nextInt());
	}
}