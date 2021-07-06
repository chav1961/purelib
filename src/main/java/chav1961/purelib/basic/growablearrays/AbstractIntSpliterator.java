package chav1961.purelib.basic.growablearrays;

import java.util.function.IntConsumer;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;

abstract class AbstractIntSpliterator implements SpliteratorOfInt {
	static final int	MINIMUM_SPLIT_SIZE = 256;
	
	protected int	from, to;
	protected int	index;
	
	AbstractIntSpliterator(final int from, final int to) {
		this.from = from;
		this.to = to;
		this.index = from;
	}

	@Override public abstract SpliteratorOfInt trySplit();
	@Override public abstract int characteristics();
	protected abstract int getValue(final int index);

	@Override
	public boolean tryAdvance(IntConsumer action) {
		while (index < to) {
			final int	value = getValue(index);
			
			if (mustBeProcessed(index++, value)) {
				action.accept(value);
				return true;
			}
		}
		return false;
	}

	@Override 
	public long estimateSize() {
		return to - from;
	}

	@Override
	public boolean mustBeProcessed(final long sequential, final int value) {
		return true;
	}
}