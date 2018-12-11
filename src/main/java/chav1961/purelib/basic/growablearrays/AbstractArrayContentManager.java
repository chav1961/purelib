package chav1961.purelib.basic.growablearrays;

abstract class AbstractArrayContentManager<T> {
	@FunctionalInterface
	interface Walker<T> {
		boolean process(T array, int from, int len);
	}
	
	static final int	DEFAULT_ARRAY_PIECE = 10;
	static final int	MINIMAL_ARRAY_SIZE = 4;
	static final int	MAXIMAL_ARRAY_SIZE = 23;
	
	abstract boolean checkSize(final int newSize);	
	abstract int toSliceIndex(final int offset);	
	abstract int toRelativeOffset(final int offset);	
	abstract int expandArray(final int newSize);	
	abstract int truncateArray(final int newSize);
	abstract void walk(Walker<T> walker);
}
