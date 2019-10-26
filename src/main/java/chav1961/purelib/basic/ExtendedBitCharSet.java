package chav1961.purelib.basic;

import java.util.Arrays;

/**
 * <p>This class implements a quick non-restricted set of characters.</p>
 * 
 * <p>The main aim of this class is to reduce time for typical set operations for the character sets. Unlike parent class {@link BitCharSet}, the class 
 * supports all range of characters, but can occupy much more memory, and can work a few slowly the parent one.</p>
 * 
 * @see BitCharSet
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class ExtendedBitCharSet extends BitCharSet {
	protected ExtendedBitCharSet(final ExtendedBitCharSet another) {
		super(another);
	}
	
	/**
	 * <p>Create empty set of characters</p>
	 */
	public ExtendedBitCharSet() {
		super();
	}

	/**
	 * <p>Create set of characters with the initial values</p>
	 * @param chars initial characters in the set
	 */
	public ExtendedBitCharSet(char... chars) {
		super(chars);
	}

	/**
	 * <p>Build charset via criteria</p>
	 * @param callback callback to implements charset criteria
	 * @return charset built
	 * @throws NullPointerException when callback is null
	 * @since 0.0.2
	 */
	public static ExtendedBitCharSet buildCharSet(final BuildCharSetCallback callback) throws NullPointerException {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			final ExtendedBitCharSet	result = new ExtendedBitCharSet();
			
			for (int symbol = Character.MAX_VALUE; symbol >= 0; symbol--) {	// Optimize charset extension
				if (callback.contains((char)symbol)) {
					result.add((char)symbol);
				}
			}
			return result;
		}
	}
	
	@Override
	public BitCharSet add(final char symbol) {
		if ((symbol >> 6) >= data.length) {
			final long[]	newData = new long[((symbol + 63) >> 6) + 1];
			
			System.arraycopy(data,0,newData,0,data.length);
			data = newData;
		}
		return super.add(symbol);
	}

	@Override
	public BitCharSet remove(final char symbol) {
		if ((symbol >> 6) < data.length) {
			super.remove(symbol);
		}
		return this;
	}

	@Override
	public ExtendedBitCharSet clone() {
		return new ExtendedBitCharSet(this); 
	}
	
	
	@Override
	public String toString() {
		return "ExtendedBitCharSet " + Arrays.toString(toArray());
	}
}
