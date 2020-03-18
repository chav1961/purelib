package chav1961.purelib.basic.growablearrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

/**
 * <p>This class implements functionality for the growable character arrays. It also inplements {@linkplain CharSequence} interface and can be 
 * used everywhere this interface required</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.basic.growablearrays JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.4
 */

public class GrowableCharArray<T extends GrowableCharArray<?>> implements CharSequence, Appendable {
	private static final char[]	NULL_CHAR = new char[0];
	
	private final boolean		usePlain;
	private final int			initialSize, initialPow;
	private final AbstractArrayContentManager<char[]>	aacm;
	private int					filled = 0;
	private char[]				plain = null;
	private char[][]			sliced = null;

	private GrowableCharArray(final int initialPow, final int filled) {
		this.usePlain = true;
		this.initialSize = 1 << (this.initialPow = initialPow);
		this.aacm = new PlainManager(initialPow);
		this.aacm.checkSize(this.filled = filled);
	}
	
	/**
	 * <p>Create growable char array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 */
	public GrowableCharArray(final boolean usePlain) {
		this(usePlain,AbstractArrayContentManager.DEFAULT_ARRAY_PIECE);
	}
	
	/**
	 * <p>Create growable char array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 * @param initialPow 2^^initialPow is a piece size of the growable array.
	 */
	public GrowableCharArray(final boolean usePlain, final int initialPow) {
		this.usePlain = usePlain;
		this.aacm = usePlain ? new PlainManager(initialPow) : new SlicedManager(initialPow); 
		this.initialSize = 1 << (this.initialPow = initialPow);
	}
	
	/**
	 * <p>Append data to the end of array
	 * @param data data to append
	 * @return self
	 */
	@SuppressWarnings("unchecked")
	public T append(final char data) {
		aacm.checkSize(filled+1);
		
		if (usePlain) {
			plain[filled] = data;
		}
		else {
			sliced[aacm.toSliceIndex(filled)][aacm.toRelativeOffset(filled)] = data;
		}
		filled++;
		return (T) this;
	}

	/**
	 * <p>Append data to the end of array</p>
	 * @param data data to append
	 * @return self
	 * @throws NullPointerException when data reference is null
	 */
	@SuppressWarnings("unchecked")
	public T append(final char[] data) throws NullPointerException {
		if (data == null) {
			throw new NullPointerException("Data array can't be null");
		}
		else if (data.length > 0) {
			return (T) append(data,0,data.length);
		}
		else {
			return (T)this;
		}
	}

	/**
	 * <p>Append data to the end of array</p>
	 * @param data data to append
	 * @param from start position in the data to append
	 * @param to end position in the data to append (excluding self)
	 * @return self
	 * @throws NullPointerException when data reference is null
	 * @throws ArrayIndexOutOfBoundsException when from and to indices out of data
	 */
	@SuppressWarnings("unchecked")
	public T append(final char[] data, int from, final int to) throws NullPointerException, ArrayIndexOutOfBoundsException {
		final int	len = to - from;
		
		if (data == null) {
			throw new NullPointerException("Target data array can't be null");
		}
		else if (data.length == 0) {
			return (T) this;
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
		return (T) this;
	}

	/**
	 * <p>Append data to the end of array</p>
	 * @param data string to append
	 * @return self
	 * @throws NullPointerException on any null parameters
	 * @throws ArrayIndexOutOfBoundsException from or from + to value out of available range
	 * @since 0.0.2 
	 */
	public T append(final String data) throws NullPointerException, ArrayIndexOutOfBoundsException {
		if (data == null) {
			throw new NullPointerException("Target data array can't be null");
		}
		else {
			return (T) append(data,0,data.length());
		}
	}
	
	/**
	 * <p>Append data to the end of array</p>
	 * @param data string to append
	 * @param from start position in the data to append
	 * @param to end position in the data to append (excluding self)
	 * @return self
	 * @throws NullPointerException on any null parameters
	 * @throws ArrayIndexOutOfBoundsException from or from + to value out of available range
	 * @since 0.0.2 
	 */
	@SuppressWarnings("unchecked")
	public T append(final String data, int from, final int to) throws NullPointerException, ArrayIndexOutOfBoundsException {
		final int	len = to - from, stringLen;
		
		if (data == null) {
			throw new NullPointerException("Target data array can't be null");
		}
		else if ((stringLen = data.length()) == 0) {
			return (T) this;
		}
		else if (from < 0 || from >= stringLen) {
			throw new ArrayIndexOutOfBoundsException("From location ["+from+"] out of bounds. Valid range is 0.."+(stringLen-1));
		}
		else if (to < 0 || to > stringLen) {
			throw new ArrayIndexOutOfBoundsException("To location ["+to+"] out of bounds. Valid range is 0.."+(stringLen));
		}
		else if (to < from) {
			throw new ArrayIndexOutOfBoundsException("To location ["+to+"] less than from location["+from+"]");
		}
		else if (len > 0) {
			aacm.checkSize(filled+len);
			
			if (usePlain) {
				data.getChars(from,from+len, plain, filled);
				filled += len;
			}
			else {
				int	rest = Math.min(len,initialSize - (filled & (initialSize-1))), actualLen = len;
				
				do {data.getChars(from,from+rest,sliced[aacm.toSliceIndex(filled)],aacm.toRelativeOffset(filled));
					filled += rest;
					from += rest;
					rest = Math.min(actualLen-=rest,initialSize - (filled & (initialSize-1)));
				} while(rest > 0);
			}
		}
		return (T) this;
	}
	
	/**
	 * <p>Append name from syntax tree to the end of array</p>
	 * @param tree tree to add name from
	 * @param id name id in the tree
	 * @return self
	 * @throws NullPointerException when tree is null
	 * @throws IllegalArgumentException when tree doesn't contain name with the given id
	 * @since 0.0.4 
	 */
	@SuppressWarnings("unchecked")
	public T append(final SyntaxTreeInterface<?> tree, final long id) throws NullPointerException, IllegalArgumentException {
		if (tree == null) {
			throw new NullPointerException("Tree to add name from can't be null");
		}
		else {
			final int	len = tree.getNameLength(id);
			
			if (len < 0) {
				throw new IllegalArgumentException("Tree to add name from doesn't have name with id ["+id+"]");
			}
			else {
				aacm.checkSize(filled+len);
				
				if (usePlain) {
					tree.getName(id,plain,filled); 
					filled += len;
				}
				else {
					append(tree.getName(id));
				}
				return (T) this;
			}
		}
	}	
	
	/**
	 * <p>Append array content from the Reader</p>
	 * @param rdr reader to append content from
	 * @throws NullPointerException if the reader is null
	 * @throws IOException on any I/O errors
	 * @since 0.0.2 
	 */
	public void append(final Reader rdr) throws NullPointerException, IOException {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null"); 
		}
		else {
			final char[]	buffer = new char[8192];
			int				len;
			
			while ((len = rdr.read(buffer)) > 0) {
				append(buffer,0,len);
			}
		}
	}

	@Override
	public T append(final CharSequence csq) throws IOException {
		if (csq == null) {
			throw new NullPointerException("Char sequence to append can't be null"); 
		}
		else {
			return append(csq,0,csq.length()-1);
		}
	}

	@Override
	public T append(final CharSequence csq, final int start, final int end) throws IOException {
		if (csq == null) {
			throw new NullPointerException("Char sequence to append can't be null"); 
		}
		else if (start < 0 || start >= csq.length()) {
			throw new IllegalArgumentException("Start position ["+start+"] out of range 0.."+(csq.length()-1)); 
		}
		else if (end < 0 || end >= csq.length()) {
			throw new IllegalArgumentException("End position ["+end+"] out of range 0.."+(csq.length()-1)); 
		}
		else if (end < start) {
			throw new IllegalArgumentException("End position ["+end+"] less than start position ["+start+"]"); 
		}
		else {
			for (int index = start, maxIndex = csq.length(); index < maxIndex; index++) {
				append(csq.charAt(index));
			}
			return (T)this;
		}
	}
	
	
	/**
	 * <p>Read data from the given index</p>
	 * @param index array index to get data from
	 * @return data gotten
	 * @throws ArrayIndexOutOfBoundsException when index outside the data
	 */
	public char read(final int index) throws ArrayIndexOutOfBoundsException {
		if (index < 0 || index >= filled) {
			throw new ArrayIndexOutOfBoundsException("Index ["+index+"] out of bounds. Valid range is 0.."+(filled-1));
		}
		else if (usePlain) {
			return plain[index];
		}
		else {
			return sliced[aacm.toSliceIndex(index)][aacm.toRelativeOffset(index)];
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
	public int read(final int index, final char[] target) throws NullPointerException, ArrayIndexOutOfBoundsException {
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
	 * @return real data size gotten
	 * @throws NullPointerException when target reference is null
	 * @throws ArrayIndexOutOfBoundsException when index outside the data or from and to indices outside the target
	 */
	public int read(int index, final char[] target, int from, final int to) throws NullPointerException, ArrayIndexOutOfBoundsException {
		if (target == null) {
			throw new NullPointerException("Target data array can't be null");
		}
		else if (index < 0 || index > filled) {
			throw new ArrayIndexOutOfBoundsException("Index ["+index+"] out of bounds. Valid range is 0.."+(filled-1));
		}
		else if (from < 0 || from > target.length) {
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
	}
	
	/**
	 * <p>Get current size of the array</p>
	 * @return current size of the array
	 * @since 0.0.2
	 */
	@Override
	public int length() {
		return filled;
	}

	@Override
	public char charAt(final int index) {
		if (index < 0 || index >= filled) {
			throw new IndexOutOfBoundsException("Character index ["+index+"] outside the range 0.."+(filled-1));
		}
		else {
			return read(index);
		}
	}

	@Override
	public CharSequence subSequence(final int start, final int end) throws IndexOutOfBoundsException {
		if (start < 0 || start >= filled) {
			throw new IndexOutOfBoundsException("Start position ["+start+"] outside the range 0.."+(filled-1));
		}
		else if (end < 0 || end >= filled) {
			throw new IndexOutOfBoundsException("End position ["+end+"] outside the range 0.."+(filled-1));
		}
		else if (end < start) {
			throw new IllegalArgumentException("End position ["+end+"] less than start position ["+start+"]");
		}
		else {
			return new CharSequence() {
				@Override
				public CharSequence subSequence(final int startNew, final int endNew) {
					return GrowableCharArray.this.subSequence(start+startNew,start+endNew);
				}
				
				@Override
				public int length() {
					return end-start;
				}
				
				@Override
				public char charAt(final int index) {
					return read(start+index);
				}
			};
		}
	}
	
	
	/**
	 * <p>Resize the array</p>
	 * @param newLength new size of the array
	 * @return self
	 * @since 0.0.2
	 */
	@SuppressWarnings("unchecked")
	public T length(final int newLength) {
		if (newLength != filled) {
			aacm.checkSize(newLength);
			filled = newLength;
		}
		return (T)this;
	}
	
	/**
	 * <p>Convert internal array from sliced to plain form. Speeds up performance, but increase memory to store data</p> 
	 * @return self if the array was still plain, otherwise new cloned instance with the plain data array
	 */
	@SuppressWarnings("unchecked")
	public T toPlain() {
		if (usePlain) {
			return (T)this;
		}
		else {
			final GrowableCharArray<T>	result = new GrowableCharArray<>(initialPow,0);
	
			if (filled > 0) {
				for (int index = 0; index < sliced.length - 1; index++) {
					result.append(sliced[index]);
				}
				result.append(sliced[sliced.length - 1],0,aacm.toRelativeOffset(filled));
			}
			return (T)result;
		}
	}
	
	/**
	 * <p>Convert internal data to array.</p>
	 * @return internal array 'as-is' for plain or clone of the data
	 */
	public char[] toArray() {
		if (usePlain) {
			return plain == null ? NULL_CHAR : plain;
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
	public char[] extract() {
		final char[]	result = new char[length()];
		
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
	 * <p>Create reader based on the array. Reader doesn't change array content and/or state.
	 * Any number of the readers can be opened on the same array simultaneously. You must not change 
	 * array content while reader is opened</p>
	 * @return reader to get content of the char array. Can be empty but not null
	 * @since 0.0.2 
	 */
	public Reader getReader() {
		return new Reader() {
			int totalDispl = 0;
			
			@Override
			public int read(final char[] cbuf, final int off, final int len) throws IOException {
				if (totalDispl >= filled) {
					return -1;
				}
				else {
					final int	read = GrowableCharArray.this.read(totalDispl,cbuf,off,len);

					if (read > 0) {
						totalDispl += read;
					}
					else {
						totalDispl = filled;
					}
					return read;
				}
			}

			@Override
			public void close() throws IOException {
			}
		};
	}
	
	@Override
	public String toString() {
		return "GrowableCharArray [usePlain=" + usePlain + ", initialSize=" + initialSize + ", filled=" + filled + ", plain=" + Arrays.toString(plain) + ", sliced=" + Arrays.toString(sliced) + "]";
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
		GrowableCharArray<?> other = (GrowableCharArray<?>) obj;
		if (filled != other.filled) return false;
		if (!Arrays.equals(plain, other.plain)) return false;
		if (!Arrays.deepEquals(sliced, other.sliced)) return false;
		return true;
	}

	private class PlainManager extends AbstractPlainContentManager<char[]> {
		PlainManager(final int initialPow) {
			super(initialPow);
		}

		@Override
		int expandArray(final int newSize) {
			plain = plain == null ? new char[newSize] : Arrays.copyOf(plain, newSize);
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

//		@Override
//		void walk(Walker<char[]> walker) {
//			walker.process(plain,0,currentSize);
//		}
	}

	private class SlicedManager extends AbstractSlicedContentManager<char[]> {
		SlicedManager(final int initialPow) {
			super(initialPow);
		}

		@Override
		int expandArray(final int newSize) {
			sliced = sliced == null ? new char[newSize / initialSize][] : Arrays.copyOf(sliced,toSliceIndex(newSize));

			for (int index = sliced.length-1; index >= 0 && sliced[index] == null; index--) {
				sliced[index] = new char[initialSize];
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

//		@Override
//		void walk(Walker<char[]> walker) {
//			int	size = currentSize;
//			
//			for (int index = 0, maxIndex = sliced.length; index < maxIndex; index++) {
//				if (sliced[index] != null) {
//					if (!walker.process(sliced[index],0,Math.min(sliced[index].length,size))) {
//						return;
//					}
//					else {
//						size -= sliced[index].length; 
//					}
//				}
//			}
//		}
	}
}
