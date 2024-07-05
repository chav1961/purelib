package chav1961.purelib.basic.growablearrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.GrowableIntArray.PlainSpliterator;
import chav1961.purelib.basic.growablearrays.GrowableIntArray.SlicedSpliterator;
import chav1961.purelib.basic.interfaces.AnyGrowableArray;

/**
 * <p>This class implements functionality for the growable byte arrays.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.basic.growablearrays JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @last.update 0.0.7
 */

public class GrowableByteArray implements AnyGrowableArray {
	public static final int		MINIMUM_SPLIT_SIZE = 256;
	private static final byte[]	NULL_BYTE = new byte[0];
	
	private final boolean		usePlain;
	private byte[]				plain = null;
	private byte[][]			sliced = null;
	private final int			initialSize, initialPow;
	private final AbstractArrayContentManager<byte[]>	aacm;
	private int					filled = 0;

	private GrowableByteArray(final int initialPow, final int filled) {
		this.usePlain = true;
		this.initialSize = 1 << (this.initialPow = initialPow);
		this.aacm = new PlainManager(initialPow);
		this.aacm.checkSize(this.filled = filled);
	}
	
	/**
	 * <p>Create growable byte array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 */
	public GrowableByteArray(final boolean usePlain) {
		this(usePlain,AbstractArrayContentManager.DEFAULT_ARRAY_PIECE);
	}
	
	/**
	 * <p>Create growable byte array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 * @param initialPow 2^^initialPow is a piece size of the growable array.
	 */
	public GrowableByteArray(final boolean usePlain, final int initialPow) {
		this.usePlain = usePlain;
		this.aacm = usePlain ? new PlainManager(initialPow) : new SlicedManager(initialPow); 
		this.initialSize = 1 << (this.initialPow = initialPow);
	}
	
	@Override
	public Class<?> getComponentType() {
		return byte.class;
	}
	
	/**
	 * <p>Append data to the end of array
	 * @param data data to append
	 * @return self
	 */
	public GrowableByteArray append(final byte data) {
		aacm.checkSize(filled+1);
		
		if (usePlain) {
			plain[filled] = data;
		}
		else {
			sliced[aacm.toSliceIndex(filled)][aacm.toRelativeOffset(filled)] = data;
		}
		filled++;
		return this;
	}

	/**
	 * <p>Append data to the end of array
	 * @param data data to append
	 * @return self
	 * @throws NullPointerException when data reference is null
	 */
	public GrowableByteArray append(final byte[] data) throws NullPointerException {
		if (data == null) {
			throw new NullPointerException("Data array can't be null");
		}
		else {
			return append(data,0,data.length);
		}
	}

	/**
	 * <p>Append data to the end of array
	 * @param data data to append
	 * @param from start position in the data to append
	 * @param to end position in the data to append (excluding self)
	 * @return self
	 * @throws NullPointerException when data reference is null
	 * @throws ArrayIndexOutOfBoundsException when from and to indices out of data
	 */
	public GrowableByteArray append(final byte[] data, int from, final int to) throws NullPointerException, ArrayIndexOutOfBoundsException {
		final int	len = to - from;
		
		if (data == null) {
			throw new NullPointerException("Target data array can't be null");
		}
		else if (data.length == 0) {
			return this;
		}
		else if (from < 0 || from >= data.length) {
			throw new ArrayIndexOutOfBoundsException("From location ["+from+"] out of bounds. Valid range is 0.."+(data.length-1));
		}
		else if (to < 0 || to > data.length) {
			throw new ArrayIndexOutOfBoundsException("To location ["+to+"] out of bounds. Valid range is 0.."+(data.length));
		}
		else if (to < from) {
			throw new ArrayIndexOutOfBoundsException("To location ["+to+"] less than from location["+from+"]");
		}
		else if (len > 0) {
			aacm.checkSize(filled+len);
			
			if (usePlain) {
				System.arraycopy(data,from,plain,filled,len);
				filled += len;
			}
			else {
				int	rest = Math.min(len,initialSize - (filled & (initialSize-1))), actualLen = len;
				
				do {System.arraycopy(data,from,sliced[aacm.toSliceIndex(filled)],aacm.toRelativeOffset(filled),rest);
					filled += rest;
					from += rest;
					rest = Math.min(actualLen-=rest,initialSize - (filled & (initialSize-1)));
				} while(rest > 0);
			}
		}
		return this;
	}

	/**
	 * <p>Append array content from the input stream</p>
	 * @param is input stream to append content from
	 * @return self
	 * @throws NullPointerException if the input stream is null
	 * @throws IOException on any I/O errors
	 * @since 0.0.2 
	 */
	public GrowableByteArray append(final InputStream is) throws NullPointerException, IOException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null"); 
		}
		else {
			final byte[]	buffer = new byte[8192];
			int				len;
			
			while ((len = is.read(buffer)) > 0) {
				append(buffer,0,len);
			}
			return this;
		}
	}

	
	/**
	 * <p>Read data from the given index</p>
	 * @param index array index to get data from
	 * @return data got
	 * @throws ArrayIndexOutOfBoundsException when index outside the data
	 */
	public byte read(final int index) throws ArrayIndexOutOfBoundsException {
		if (index < 0 || index >= filled) {
			throw new ArrayIndexOutOfBoundsException("Index ["+index+"] out of bounds. Valid range is 0.."+(filled-1));
		}
		else {
			return uncheckedRead(index);
		}
	}

	/**
	 * <p>Read a piece of data from the given index</p>
	 * @param index array index to get data from
	 * @param target place to store data to
	 * @return real data size got
	 * @throws NullPointerException when target reference is null
	 * @throws ArrayIndexOutOfBoundsException when index outside the data
	 */
	public int read(final int index, final byte[] target) throws NullPointerException, ArrayIndexOutOfBoundsException {
		if (target == null) {
			throw new NullPointerException("Target data array can't be null");
		}
		else {
			return read(index,target,0,target.length);
		}
	}

	/**
	 * <p>Read a piece of data from the given index</p>
	 * @param index array index to get data from
	 * @param target place to store data to
	 * @param from start position to get data to
	 * @param to start position to get data to (excluding self)
	 * @return real data size got
	 * @throws NullPointerException when target reference is null
	 * @throws ArrayIndexOutOfBoundsException when index outside the data or from and to indices outside the target
	 */
	public int read(int index, final byte[] target, int from, final int to) throws NullPointerException, ArrayIndexOutOfBoundsException {
		if (target == null) {
			throw new NullPointerException("Target data array can't be null");
		}
		else if (index < 0 || index >= filled) {
			throw new ArrayIndexOutOfBoundsException("Index ["+index+"] out of bounds. Valid range is 0.."+(filled-1));
		}
		else if (from < 0 || from >= target.length) {
			throw new ArrayIndexOutOfBoundsException("From location ["+from+"] out of bounds. Valid range is 0.."+(target.length-1));
		}
		else if (to < 0 || to > target.length) {
			throw new ArrayIndexOutOfBoundsException("To location ["+to+"] out of bounds. Valid range is 0.."+(target.length));
		}
		else if (to < from) {
			throw new ArrayIndexOutOfBoundsException("To location ["+to+"] less than from location["+from+"]");
		}
		else if (filled == 0) {
			return 0;
		}
		else {
			return uncheckedRead(index,target,from,to);
		}
	}
	
	/**
	 * <p>Get current size of the array</p>
	 * @return current size of the array
	 */
	public int length() {
		return filled;
	}
	
	/**
	 * <p>Resize the array</p>
	 * @param newLength new size of the array
	 * @return self
	 * @since 0.0.2
	 */
	public GrowableByteArray length(final int newLength) {
		if (newLength != filled) {
			aacm.checkSize(newLength);
			filled = newLength;
		}
		return this;
	}

	
	/**
	 * <p>Convert internal array from sliced to plain form. Speeds up performance, but increase memory to store data</p> 
	 * @return self if the array was still plain, otherwise new cloned instance with the plain data array
	 */
	public GrowableByteArray toPlain() {
		if (usePlain) {
			return this;
		}
		else {
			final GrowableByteArray	result = new GrowableByteArray(initialPow,0);
	
			if (filled > 0) {
				for (int index = 0; index < sliced.length - 1; index++) {
					result.append(sliced[index]);
				}
				result.append(sliced[sliced.length - 1],0,aacm.toRelativeOffset(filled));
			}
			return result;
		}
	}
	
	/**
	 * <p>Convert internal data to byte array.</p>
	 * @return internal array 'as-is' for plain or clone of the data
	 */
	public byte[] toArray() {
		if (usePlain) {
			return plain == null ? NULL_BYTE : plain;
		}
		else {
			return toPlain().toArray();
		}
	}

	/**
	 * <p>Extract array content as a copy</p>
	 * @return array content copy
	 * @since 0.0.2 
	 */
	public byte[] extract() {
		final byte[]	result = new byte[length()];
		
		read(0,result);
		return result;
	}
	
	/**
	 * <p>Clear array content</p>
	 */
	public void clear() {
		length(0);
	}
	
	/**
	 * <p>Create input stream based on the array. Input stream doesn't change array content and/or state.
	 * Any number of the input streams can be opened on the same array simultaneously. You must not change 
	 * array content while input stream is opened</p> 
	 * @return input stream to read array content
	 * @since 0.0.2 
	 */
	public InputStream getInputStream() {
		return new InputStream() {
			int	totalDispl = 0;
			
			@Override
			public int read() throws IOException {
				if (totalDispl >= length()) {
					return -1;
				}
				else {
					return GrowableByteArray.this.read(totalDispl++);
				}
			}
			
			@Override
			public int read(final byte[] cbuf, final int off, final int len) throws IOException {
				if (totalDispl >= filled) {
					return -1;
				}
				else {
					final int	read = GrowableByteArray.this.read(totalDispl,cbuf,off,len);

					if (read > 0) {
						totalDispl += read;
					}
					else {
						totalDispl = filled;
					}
					return read;
				}					
			}				
		};
	}

	/**
	 * <p>Convert array content to stream</p> 
	 * @return immutable stream converted. Can't be null
	 * @since 0.0.5
	 */
	public IntStream toStream() {
		return new IntStreamImpl(this);
	}
	
	@Override
	public String toString() {
		return "GrowableByteArray [usePlain=" + usePlain + ", initialSize=" + initialSize + ", filled=" + filled + ", plain=" + Arrays.toString(plain) + ", sliced=" + Arrays.toString(sliced) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + filled;
		result = prime * result + Arrays.hashCode(plain);
		result = prime * result + Arrays.hashCode(sliced);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		GrowableByteArray other = (GrowableByteArray) obj;
		if (filled != other.filled) return false;
		if (!Arrays.equals(plain, other.plain)) return false;
		if (!Arrays.deepEquals(sliced, other.sliced)) return false;
		return true;
	}

	SpliteratorOfInt getSpliterator() {
		if (usePlain) {
			return new PlainSpliterator(0, length()); 
		}
		else {
			return new SlicedSpliterator(0, length(), MINIMUM_SPLIT_SIZE); 
		}
	}

	OfInt getIterator() {
		return new OfInt() {
			int	index = 0;
			
			@Override
			public boolean hasNext() {
				return index < length();
			}

			@Override
			public int nextInt() {
				if (usePlain) {
					return plain[index++];
				}
				else {
					final int	result = sliced[aacm.toSliceIndex(index)][aacm.toRelativeOffset(index)];
					
					index++;
					return result;
				}
			}
		};
	}

	protected class PlainSpliterator extends AbstractIntSpliterator {
		PlainSpliterator(final int from, final int to) {
			super(from,to);
		}

		@Override
		public int characteristics() {
			return Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;
		}

		@Override 
		public SpliteratorOfInt trySplit() {
			return null;
		}

		@Override
		protected int getValue(final int index) {
			return plain[index];
		}
		
		@Override
		public String toString() {
			return "PlainSpliterator [from=" + from + ", to=" + to + ", index=" + index + "]";
		}
	}
	
	protected class SlicedSpliterator extends AbstractIntSpliterator {
		protected final int					minimumSplitSize;
		protected final SlicedSpliterator	nested; 
		
		SlicedSpliterator(final int from, final int to, final int minimumSplitSize) {
			this(null, from, to, minimumSplitSize);
		}

		SlicedSpliterator(final SlicedSpliterator nested) {
			this(nested, nested.from, nested.to, nested.minimumSplitSize);
		}

		SlicedSpliterator(final SlicedSpliterator nested, final int from, final int to, final int minimumSplitSize) {
			super(from, to);
			this.minimumSplitSize = nested != null ? nested.minimumSplitSize : MINIMUM_SPLIT_SIZE;
			this.nested = nested;
		}
		
		@Override
		public int characteristics() {
			if (nested != null) {
				return nested.characteristics();
			}
			else {
				return Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SUBSIZED;
			}
		}

		@Override
		public SpliteratorOfInt trySplit() {
 			final int	delta = to - index, halfDelta = delta / 2;
			
			if (halfDelta >= minimumSplitSize) {
				final int	splittedTo = to;
				
				to = halfDelta;
				return new SlicedSpliterator(this, index + halfDelta, splittedTo, minimumSplitSize);
			}
			else {
				return null;
			}
		}

		@Override
		protected int getValue(final int index) {
			if (nested != null) {
				return nested.getValue(index);
			}
			else {
				return sliced[aacm.toSliceIndex(index)][aacm.toRelativeOffset(index)];
			}
		}
		
		@Override
		public String toString() {
			return "SlicedSpliterator [minimumSplitSize=" + minimumSplitSize + ", from=" + from + ", to=" + to + ", index=" + index + "]";
		}
	}
	
	protected byte uncheckedRead(final int index) {
		if (usePlain) {
			return plain[index];
		}
		else {
			return sliced[aacm.toSliceIndex(index)][aacm.toRelativeOffset(index)];
		}
	}
	
	protected int uncheckedRead(int index, final byte[] target, int from, final int to) {
		int	len = Math.min(to-from,filled-index), actualLen = len;

		if (len > 0) {
			if (usePlain) {
				System.arraycopy(plain,index,target,from,len);
			}
			else {
				int	rest = Math.min(len,initialSize - (index & (initialSize-1)));
				
				do {System.arraycopy(sliced[aacm.toSliceIndex(index)],aacm.toRelativeOffset(index),target,from,rest);
					actualLen -= rest;
					index += rest;
					from += rest;
					rest = Math.min(actualLen,initialSize - (index & (initialSize-1)));
				} while(actualLen > 0);
			}
			return len;
		}
		else {
			return 0;
		}
	}	
	
	protected int unckeckedWrite(final int displ, final byte data) throws IllegalArgumentException {
		if (displ >= 0 && displ < filled) {
			if (usePlain) {
				plain[displ] = data;
			}
			else {
				sliced[aacm.toSliceIndex(displ)][aacm.toRelativeOffset(displ)] = data;
			}
		}
		else if (displ == filled) {
			append(data);
		}
		else {
			throw new IllegalArgumentException("Displacement to write ["+displ+"] out of range 0.."+filled);
		}
		return 1;
	}	
	
	protected int unckeckedWrite(int displ, final byte[] data, int off, final int len) {
		if (displ >= 0 && displ < filled) {
			if (displ + len < filled) {
				if (usePlain) {
					System.arraycopy(data,off,plain,displ,len);
				}
				else {
					int	rest = Math.min(len,initialSize - (displ & (initialSize-1))), actualLen = len;
					
					do {System.arraycopy(data,off,sliced[aacm.toSliceIndex(displ)],aacm.toRelativeOffset(displ),rest);
						actualLen -= rest;
						displ += rest;
						off += rest;
						rest = Math.min(actualLen,initialSize - (displ & (initialSize-1)));
					} while(actualLen > 0);
				}
			}
			else {
				final int	delta = filled-displ;
				
				unckeckedWrite(displ,data,off,delta);
				unckeckedWrite(filled,data,off+delta,len-delta);
			}
		}
		else if (displ == filled) {
			append(data,off,len);
		}
		else {
			throw new IllegalArgumentException("Displacement to write ["+displ+"] out of range 0.."+filled);
		}
		return len;
	}	
	
	boolean isSliced() {
		return !usePlain;
	}
	
	private class PlainManager extends AbstractPlainContentManager<byte[]> {
		PlainManager(final int initialPow) {
			super(initialPow);
		}

		@Override
		int expandArray(final int newSize) {
			plain = plain == null ? new byte[newSize] : Arrays.copyOf(plain, newSize);
			return newSize;
		}

		@Override
		int truncateArray(final int newSize) {
			plain = Arrays.copyOf(plain, newSize);
			if (filled > newSize) {
				filled = newSize;
			}
			return newSize;
		}
	}

	private class SlicedManager extends AbstractSlicedContentManager<byte[]> {
		SlicedManager(final int initialPow) {
			super(initialPow);
		}

		@Override
		int expandArray(final int newSize) {
			sliced = sliced == null ? new byte[newSize / initialSize][] : Arrays.copyOf(sliced,toSliceIndex(newSize));

			for (int index = sliced.length-1; index >= 0 && sliced[index] == null; index--) {
				sliced[index] = new byte[initialSize];
			}
			return newSize;
		}

		@Override
		int truncateArray(final int newSize) {
			sliced = Arrays.copyOf(sliced,toSliceIndex(newSize));
			if (filled > newSize) {
				filled = newSize;
			}
			return newSize;
		}
	}
}
