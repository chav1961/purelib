package chav1961.purelib.basic.growablearrays;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This class implements functionality for the growable byte arrays.</p>
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.basic.growablearrays JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class GrowableByteArray {
	private static final int	DEFAULT_ARRAY_PIECE = 10;
	private static final int	MINIMAL_ARRAY_SIZE = 4;
	
	private final boolean		usePlain;
	private final int			initialSize, initialPow;
	private int					filled = 0;
	private byte[]				plain;
	private byte[][]			sliced;

	private GrowableByteArray(final int initialPow, final int filled, final byte[] data) {
		this.usePlain = true;
		this.initialPow = initialPow;
		this.initialSize = 1 << initialPow;
		this.filled = filled;
		this.plain = data;
		this.sliced = null;
	}
	
	/**
	 * <p>Create growable byte array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 */
	public GrowableByteArray(final boolean usePlain) {
		this(usePlain,DEFAULT_ARRAY_PIECE);
	}
	
	/**
	 * <p>Create growable byte array instance</p>
	 * @param usePlain true if you want to use plain array for data keeping. When true, the functionality of the class is similar 
	 * to the {@link ByteArrayOutputStream} class. Using 'false' reduces the memory used, but slow down access time
	 * @param initialPow 2^^initialPow is a piece size of the growable array.
	 */
	public GrowableByteArray(final boolean usePlain, final int initialPow) {
		if (initialPow < 5 || initialPow > 24) {
			throw new IllegalArgumentException("Initial size of the array is less than 2^5 or greater than 2^24 elements");
		}
		else { 
			this.usePlain = usePlain;
			this.initialPow = initialPow;
			this.initialSize = 1 << initialPow;
			internalClear();
		}
	}
	
	/**
	 * <p>Append data to the end of array
	 * @param data data to append
	 * @return self
	 */
	public GrowableByteArray append(final byte data) {
		if (usePlain) {
			if (filled + 1 >= plain.length) {
				expandPlain(1);
			}
			plain[filled++] = data;
		}
		else {
			if ((filled & (initialSize-1)) + 1 >= initialSize) {
				expandSliced(1);
			}
			sliced[filled >> initialPow][filled & (initialSize-1)] = data;
			filled++;
		}
		return this;
	}

	/**
	 * <p>Append data to the end of array
	 * @param data data to append
	 * @return self
	 */
	public GrowableByteArray append(final byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("Data array can't be null");
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
	 */
	public GrowableByteArray append(final byte[] data, int from, final int to) {
		final int	len = to - from;
		
		if (data == null) {
			throw new IllegalArgumentException("Target data array can't be null");
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
			if (usePlain) {
				if (filled + len >= plain.length) {
					expandPlain(len);
				}
				if (len > MINIMAL_ARRAY_SIZE) {
					System.arraycopy(data,from,plain,filled,len);
				}
				else {
					for (int index = 0; index < len; index++) {
						plain[filled+index] = data[from+index];
					}
				}
				filled += len;
			}
			else {
				if (((filled + len) >> initialPow) > (filled >> initialPow)) {
					expandSliced(len);
				}
				int	rest = Math.min(len,initialSize - (filled & (initialSize-1))), actualLen = len;
				
				do {System.arraycopy(data,from,sliced[filled >> initialPow],filled & (initialSize-1),rest);
					filled += rest;
					from += rest;
					rest = Math.min(actualLen-=rest,initialSize - (filled & (initialSize-1)));
				} while(rest > 0);
			}
		}
		return this;
	}

	/**
	 * <p>Read data from the given index</p>
	 * @param index array index to get data from
	 * @return data gotten
	 */
	public byte read(final int index) {
		if (index < 0 || index >= filled) {
			throw new ArrayIndexOutOfBoundsException("Index ["+index+"] out of bounds. Valid range is 0.."+(filled-1));
		}
		else if (usePlain) {
			return plain[index];
		}
		else {
			return sliced[index >> initialPow][index & (initialSize-1)];
		}
	}

	/**
	 * <p>Read a piece of data from the given index</p>
	 * @param index array index to get data from
	 * @param target place to store data to
	 * @return real data size gotten
	 */
	public int read(final int index, final byte[] target) {
		if (target == null) {
			throw new IllegalArgumentException("Target data array can't be null");
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
	 */
	public int read(int index, final byte[] target, int from, final int to) {
		if (target == null) {
			throw new IllegalArgumentException("Target data array can't be null");
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
		else {
			int	len = Math.min(to-from,filled-index), actualLen = len;

			if (len > 0) {
				if (usePlain) {
					System.arraycopy(plain,index,target,from,len);
				}
				else {
					int	rest = Math.min(len,initialSize - (index & (initialSize-1)));
					
					do {System.arraycopy(sliced[index >> initialPow],index & (initialSize-1),target,from,rest);
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
	 */
	public int length() {
		return filled;
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
			final int		len = nextPow(filled);
			final byte[]	buffer = new byte[len];
			
			for (int index = 0, size = 0; size < filled; index++, size += initialSize) {
				System.arraycopy(sliced[index],0,buffer,index*initialSize,Math.min(initialSize,filled-size));
			}
			return new GrowableByteArray(initialSize,filled,buffer);
		}
	}
	
	/**
	 * <p>Convert internal data to byte array.</p>
	 * @return internal array 'as-is' for plain or clone of the data
	 */
	public byte[] toArray() {
		if (usePlain) {
			return plain;
		}
		else {
			return toPlain().toArray();
		}
	}

	/**
	 * <p>Clear array content</p>
	 */
	public void clear() {
		internalClear();
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

	private void expandPlain(final int delta) {
		final byte[]	newData = new byte[2*plain.length];
		
		System.arraycopy(plain,0,newData,0,filled);
		plain = newData;
	}

	private void expandSliced(final int delta) {
		int		expand = 0;

		expand = ((((filled & (initialSize-1)) + delta) + initialSize - 1) >> initialPow);
		final byte[][]	newSliced = new byte[sliced.length+expand][];
		
		System.arraycopy(sliced,0,newSliced,0,sliced.length);
		for (int index = sliced.length; index < newSliced.length; index++) {
			newSliced[index] = new byte[initialSize];
		}
		sliced = newSliced;
	}

	private void internalClear() {
		if (this.usePlain) {
			this.sliced = null;
			this.plain = new byte[this.initialSize];
		}
		else {
			this.sliced = new byte[1][];
			this.sliced[0] = new byte[this.initialSize];
		}
	}

	private int nextPow(int value) {
		int	result =  2;

		while (value > 0) {
			result <<= 1;
			value >>= 1;
		}
		return result;
	}
}
