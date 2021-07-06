package chav1961.purelib.basic.growablearrays;

import java.util.PrimitiveIterator.OfLong;
import java.util.function.IntToLongFunction;

class IteratorWrapperLong implements OfLong {
	private final OfInt				nested;
	private final IntToLongFunction	mapper;
	
	IteratorWrapperLong(final OfInt nested, final IntToLongFunction mapper) {
		this.nested = nested;
		this.mapper = mapper;
	}
	
	@Override
	public boolean hasNext() {
		return nested.hasNext();
	}

	@Override
	public long nextLong() {
		return mapper.applyAsLong(nested.nextInt());
	}

	@Override
	public String toString() {
		return "IteratorWrapperLong [nested=" + nested + ", mapper=" + mapper + "]";
	}
}