package chav1961.purelib.basic.growablearrays;


abstract class AbstractSlicedContentManager<T> extends AbstractArrayContentManager<T> {
	protected final int		initialPow, initialSize;
	protected int			currentSize;
	
	AbstractSlicedContentManager(final int initialPow) {
		if (initialPow < MINIMAL_ARRAY_SIZE || initialPow > MAXIMAL_ARRAY_SIZE) {
			throw new IllegalArgumentException("Initial size of the array is less than 2^"+MINIMAL_ARRAY_SIZE+" or greater than 2^"+MAXIMAL_ARRAY_SIZE+" elements");
		}
		else { 
			this.initialPow = initialPow;
			this.initialSize = 1 << initialPow;
			this.currentSize = 0;
		}
	}
	
	@Override abstract int expandArray(final int newSize);	
	@Override abstract int truncateArray(final int newSize);
	@Override abstract void walk(Walker<T> walker);
	
	@Override
	boolean checkSize(final int newSize) {
		if (newSize == currentSize) {
			return false;
		}
		else if (newSize > currentSize) {
			int	range = Math.max(currentSize,initialSize);
			
			while (range > 0 && range < newSize) {
				range <<= 1;
			}
			if (range < 0) {
				range = Integer.MAX_VALUE;
			}
			if (range < newSize) {
				throw new IllegalArgumentException("Attempt to add more than 2^31 elements into array"); 
			}
			else {
				currentSize = expandArray(range);
			}
			return true;
		}
		else if (currentSize > 0 && ((newSize + initialSize - 1) >> initialPow) < (currentSize >> initialPow) / 3) {
			int	range = currentSize;
			
			while (range > initialSize && range > 2 * newSize) {
				range >>= 1;
			}
			currentSize = truncateArray(range);
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	int toSliceIndex(final int offset) {
		return offset >>> initialPow;
	}

	@Override
	int toRelativeOffset(final int offset) {
		return offset - ((offset >>> initialPow) << initialPow); 
	}
}
