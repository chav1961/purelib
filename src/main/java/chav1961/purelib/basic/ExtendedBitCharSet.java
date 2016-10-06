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

	@Override
	public BitCharSet add(final char symbol) {
		if ((symbol >> 6) >= data.length) {
			final long[]	newData = new long[(symbol + 63) >> 6];
			
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
	public boolean contains(final char symbol) {
		if ((symbol >> 6) < data.length) {
			return super.contains(symbol);
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "ExtendedBitCharSet " + Arrays.toString(toArray());
	}
}
