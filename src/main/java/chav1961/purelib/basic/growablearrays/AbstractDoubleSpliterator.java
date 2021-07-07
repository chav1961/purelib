package chav1961.purelib.basic.growablearrays;

import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

abstract class AbstractDoubleSpliterator implements SpliteratorOfDouble {
	static final int	MINIMUM_SPLIT_SIZE = 256;
	
	protected int	from, to;
	protected int	index;
	
	AbstractDoubleSpliterator(final int from, final int to) {
		this.from = from;
		this.to = to;
		this.index = from;
	}

	@Override public abstract SpliteratorOfDouble trySplit();
	@Override public abstract int characteristics();
	protected abstract double getValue(final int index);

	@Override
	public boolean tryAdvance(DoubleConsumer action) {
		while (index < to) {
			final double	value = getValue(index);
			
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
	public boolean mustBeProcessed(final long sequential, final double value) {
		return true;
	}
}