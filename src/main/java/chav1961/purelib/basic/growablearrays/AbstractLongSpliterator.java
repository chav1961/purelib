package chav1961.purelib.basic.growablearrays;

import java.util.function.LongConsumer;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

abstract class AbstractLongSpliterator implements SpliteratorOfLong {
	static final int	MINIMUM_SPLIT_SIZE = 256;
	
	protected int	from, to;
	protected int	index;
	
	AbstractLongSpliterator(final int from, final int to) {
		this.from = from;
		this.to = to;
		this.index = from;
	}

	@Override public abstract SpliteratorOfLong trySplit();
	@Override public abstract int characteristics();
	protected abstract long getValue(final int index);

	@Override
	public boolean tryAdvance(LongConsumer action) {
		while (index < to) {
			final long	value = getValue(index);
			
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
	public boolean mustBeProcessed(final long sequential, final long value) {
		return true;
	}
}