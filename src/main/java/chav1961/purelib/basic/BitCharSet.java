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
 */

public class BitCharSet implements Cloneable {
	protected long[]			data = new long[2];

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
			throw new IllegalArgumentException("Char list can't be null"); 
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
			throw new IllegalArgumentException("Char list can't be null"); 
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
		try{return (data[symbol >> 6] & (1L << (symbol & 0x3F))) != 0;
		} catch (ArrayIndexOutOfBoundsException exc) {
			return false; 
		}
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

	@Override
	public BitCharSet clone() {
		return new BitCharSet(this); 
	}
	
	@Override
	public String toString() {
		return "BitCharSet " + Arrays.toString(toArray());
	}
}
