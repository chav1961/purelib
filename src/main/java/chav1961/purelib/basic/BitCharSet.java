package chav1961.purelib.basic;

import java.util.Arrays;

/**
 * <p>This class implements a quick restricted set of characters.</p>
 * 
 * <p>The main aim of this class is to reduce time for typical set operations for the character sets. The class supports only restricted
 * char set for the first 0..127 characters in the code table. Use the {@link ExtendedBitCharSet} class to operate with the whole range 
 * of available characters in the set. The class not supports a set-specific operations (union, intersect, minus)</p>
 * 
 * <p>To reduce time for operations, the class doesn't check parameters of it's methods, but catches available exceptions during call processing.
 * To avoid strong reducing of the performance, never use this class to work with the symbols out of 0..127 range! Use {@link ExtendedBitCharSet}
 * class instead</p>
 * 
 * <p>The most of all methods of the class return the {@link BitCharSet} type. It's exactly <b>this</b> reference, so you can use them in the
 * chained operations (for example new BitCharSet().add('a','b','c').addRange('0','9') etc)</p>
 * 
 * @see ExtendedBitCharSet
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.2
 */

public class BitCharSet implements Cloneable {
	protected long[]			data = new long[2];

	/**
	 * <p>This interface describes callback for building charset by criteria</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface BuildCharSetCallback {
		/**
		 * <p>Does the charset contains this character 
		 * @param symbol character to check
		 * @return true if contains
		 */
		boolean contains(char symbol);
	}
	
	protected BitCharSet(final BitCharSet another) {
		this.data = another.data.clone();
	}
	
	/**
	 * <p>Create empty set of characters</p>
	 */
	public BitCharSet() {}
	
	/**
	 * <p>Create set of characters with the initial values</p>
	 * @param chars initial characters in the set
	 */
	public BitCharSet(final char... chars) {
		add(chars);
	}

	/**
	 * <p>Create set of characters with the initial values</p>
	 * @param chars initial characters in the set
	 */
	public BitCharSet(final String chars) {
		if (chars == null) {
			throw new NullPointerException("String chars can't be null");
		}
		else {
			for (int index = 0, maxIndex = chars.length(); index < maxIndex; index++) {
				add(chars.charAt(index));
			}
		}
	}

	/**
	 * <p>Build charset via criteria</p>
	 * @param callback callback to implements charset criteria
	 * @return charset built
	 * @throws NullPointerException when callback is null
	 * @since 0.0.2
	 */
	public static BitCharSet buildCharSet(final BuildCharSetCallback callback) throws NullPointerException {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			final BitCharSet	result = new BitCharSet();
			
			for (char symbol = 0; symbol <= 0x3F; symbol++) {
				if (callback.contains(symbol)) {
					result.add(symbol);
				}
			}
			return result;
		}
	}
	
	/**
	 * <p>Add symbol to the character set</p> 
	 * @param symbol symbol to add
	 * @return self
	 */
	public BitCharSet add(final char symbol) {
		try{data[symbol >> 6] |= (1L << (symbol & 0x3F));
			return this;
		} catch (ArrayIndexOutOfBoundsException exc) {
			throw new IllegalArgumentException("BitCharSet is available on the 0.."+(64*data.length-1)+" range only"); 
		}
	}
	
	/**
	 * <p>Add list of characters to the character set</p>
	 * @param chars characters to add
	 * @return self
	 */
	public BitCharSet add(final char... chars) {
		if (chars == null) {
			throw new NullPointerException("Char list can't be null"); 
		}
		else {
			for (char item : chars) {
				add(item);
			}
			return this;
		}
	}
	
	/**
	 * <p>Add all character for the given range to the character set</p>
	 * @param from start character range to add
	 * @param to end character range to add
	 * @return self
	 */
	public BitCharSet addRange(char from, final char to) {
		if (from > to) {
			throw new IllegalArgumentException("Low range ["+from+"] is greater than high range ["+to+"]"); 
		}
		else {
			while (from <= to) {
				add(from++);
			}
			return this;
		}
	}

	/**
	 * <p>Remove character from the character set</p>
	 * @param symbol character to remove
	 * @return self
	 */
	public BitCharSet remove(final char symbol) {
		try{data[symbol >> 6] &= ~(1L << (symbol & 0x3F));
			return this;
		} catch (ArrayIndexOutOfBoundsException exc) {
			throw new IllegalArgumentException("BitCharSet is available on the 0.."+(64*data.length-1)+" range only"); 
		}
	}
	
	/**
	 * <p>Remove list of characters from the character set</p>
	 * @param chars characters to remove
	 * @return self
	 */
	public BitCharSet remove(final char... chars) {
		if (chars == null) {
			throw new NullPointerException("Char list can't be null"); 
		}
		else {
			for (char item : chars) {
				remove(item);
			}
			return this;
		}
	}

	/**
	 * <p>Remove all character for the given range from the character set</p>
	 * @param from start character range to remove
	 * @param to end character range to remove
	 * @return self
	 */
	public BitCharSet removeRange(char from, final char to) {
		if (from > to) {
			throw new IllegalArgumentException("Low range ["+from+"] is greater than high range ["+to+"]"); 
		}
		else {
			while (from <= to) {
				remove(from++);
			}
			return this;
		}
	}
	
	/**
	 * <p>Test the set contains the given character. Character can be out of range 0..127, but such call strongly reduces class performance!</p>
	 * @param symbol character to test
	 * @return true if contains
	 */
	public boolean contains(final char symbol) {
		if ((symbol >> 6) < data.length) {
			return (data[symbol >> 6] & (1L << (symbol & 0x3F))) != 0;
		}
		else {
			return false; 
		}
	}

	/**
	 * <p>Build union of two BitCharSets. This charset will be modified</p> 
	 * @param another another charset to union with
	 * @return self
	 * @throws NullPointerException if another charset is null
	 * @since 0.0.2
	 */
	public BitCharSet union(final BitCharSet another) throws NullPointerException {
		if (another == null) {
			throw new NullPointerException("Another charset can't be null");
		}
		else {
			for (int index = 0, maxIndex = Math.min(data.length,another.data.length); index < maxIndex; index++) {
				data[index] |= another.data[index];
			}
			return this;
		}
	}

	/**
	 * <p>Build intersection of two BitCharSets. This charset will be modified</p>
	 * @param another another charset to intersect with
	 * @return self
	 * @throws NullPointerException if another charset is null
	 * @since 0.0.2
	 */
	public BitCharSet intersect(final BitCharSet another) throws NullPointerException {
		if (another == null) {
			throw new NullPointerException("Another charset can't be null");
		}
		else {
			for (int index = 0, maxIndex = Math.min(data.length,another.data.length); index < maxIndex; index++) {
				data[index] &= another.data[index];
			}
			return this;
		}
	}

	/**
	 * <p>Build subtraction of two BitCharSets. This charset will be modified</p>
	 * @param another another charset to minus
	 * @return self
	 * @throws NullPointerException if another charset is null
	 * @since 0.0.2
	 */
	public BitCharSet minus(final BitCharSet another) throws NullPointerException {
		if (another == null) {
			throw new NullPointerException("Another charset can't be null");
		}
		else {
			for (int index = 0, maxIndex = Math.min(data.length,another.data.length); index < maxIndex; index++) {
				data[index] &= ~(data[index] & another.data[index]);
			}
			return this;
		}
	}

	/**
	 * <p>Invert charset content. This charset will be modified</p>
	 * @return self
	 * @since 0.0.2
	 */
	public BitCharSet invert() {
		for (int index = 0, maxIndex = data.length; index < maxIndex; index++) {
			data[index] ^= ~0;
		}
		return this;
	}
	
	/**
	 * <p>Calculate cardinality (aka size) of the set</p>
	 * @return set cardinality
	 */
	public int size() {
		int	count = 0;
		
		for (long item : data) {
			for (int index = 0; index < 64; index++) {
				if ((item & (1L << index)) != 0) {
					count++;
				}
			}
		}
		return count;
	}
	
	/**
	 * <p>Convert the set content to the character array</p>
	 * @return ordered character array containing set content. Can be empty but not null
	 */
	public char[] toArray() {
		final char[]	result = new char[size()];
		char			value = 0;
		int 			count = 0;
		
		for (int part = 0, maxPart = data.length; part < maxPart; part++) {
			for (int index = 0; index < 64; index++) {
				if ((data[part] & (1L << index)) != 0) {
					result[count++] = value;
				}
				value++;
			}
		}
		
		return	result;
	}

	/**
	 * <p>Convert the set content to the character array pairs. Every element of the array pairs is either single char array for the individual character, or two character array for the contiguous character range</p>
	 * @return ordered character array pairs containing set content. Can be empty but not null
	 * @since 0.0.2
	 */
	public char[][] toArrayPairs() {
		char			value = 0, from = 0, to = 0;
		int 			pairs = 0;
		boolean			detected = false;
		
		for (int part = 0, maxPart = data.length; part < maxPart; part++) {
			for (int index = 0; index < 64; index++) {
				if ((data[part] & (1L << index)) != 0) {
					if (!detected) {
						detected = true;
						pairs++;
					}
				}
				else {
					detected = false;
				}
				value++;
			}
		}
		
		final char[][]	result = new char[pairs][];

		detected = false;
		pairs = -1;
		value = 0;
		for (int part = 0, maxPart = data.length; part < maxPart; part++) {
			for (int index = 0; index < 64; index++) {
				if ((data[part] & (1L << index)) != 0) {
					if (!detected) {
						pairs++;
						detected = true;
						from = to = value;
					}
					else {
						to = value;
					}
				}
				else {
					if (detected) {
						result[pairs] = from == to ? new char[] {from} : new char[] {from, to};  
						detected = false;
					}
				}
				value++;
			}
		}
		if (detected) {
			result[pairs] = from == to ? new char[] {from} : new char[] {from, to};  
		}
		return result;
	}
	
	@Override
	public BitCharSet clone() {
		return new BitCharSet(this); 
	}
	
	@Override
	public String toString() {
		return "BitCharSet " + Arrays.toString(toArray());
	}
}
