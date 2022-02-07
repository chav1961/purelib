package chav1961.purelib.basic;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 *
 */
public class CharArrayPiece implements Cloneable, Serializable, Comparable<CharArrayPiece>, CharSequence {
	private static final long 	serialVersionUID = -2886853514513677359L;
	
	private final char[]		content;
	private final int			from, to, hash;
	private final boolean		copy;

	public CharArrayPiece(final String source) {
		this(source.toCharArray(), 0, source.length(), true);
	}
	
	public CharArrayPiece(final char[] source, final int from, final int to) {
		this(source, from, to, false);
	}
	
	public CharArrayPiece(final char[] source, final int from, final int to, final boolean copy) {
		if (source == null) {
			throw new NullPointerException("Source content can't be null"); 
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(source.length - 1)); 
		}
		else if (to < 0 || to >= source.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(source.length - 1)); 
		}
		else if (to < from) {
			throw new IllegalArgumentException("To position ["+to+"] can't be less than from position ["+from+"]"); 
		}
		else if (copy) {
			this.content = Arrays.copyOfRange(source, from, to);
			this.from = 0;
			this.to = this.content.length;
			this.copy = true;
			this.hash = hashCodeInternal(); 
		}
		else {
			this.content = source;
			this.from = from;
			this.to = to;
			this.copy = false;
			this.hash = 0;
		}
	}
	
	@Override
	public int compareTo(final CharArrayPiece o) {
		if (o == null) {
			throw new NullPointerException("Object to compare can't be null"); 
		}
		else if (o == this || o.content == this.content && o.from == this.from && o.to == this.to) {
			return 0;
		}
		else {
			return compareToInternal(o.content, o.from, o.to);
		}
	}

	@Override
	public int length() {
		return to - from;
	}

	@Override
	public char charAt(final int index) {
		if (index < 0 || index >= length()) {
			throw new ArrayIndexOutOfBoundsException(index); 
		}
		else {
			return content[from+index];
		}
	}

	@Override
	public CharSequence subSequence(final int start, final int end) {
		if (start < 0 || start-from >= to) {
			throw new IllegalArgumentException("Start index ["+start+"] out of range 0.."+(to - from - 1));
		}
		else if (end < 0 || end-from >= to) {
			throw new IllegalArgumentException("End index ["+end+"] out of range 0.."+(to - from - 1));
		}
		else if (end < start) {
			throw new IllegalArgumentException("End index ["+end+"] can't be less than start index ["+start+"]");
		}
		else {
			return new CharArrayPiece(content, from+start, from+end, copy); 
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	@Override
	public int hashCode() {
		if (copy) {
			return this.hash;
		}
		else {
			return hashCodeInternal();
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		final CharArrayPiece other = (CharArrayPiece) obj;

		if (other.length() != this.length()) {
			return false;
		}
		else {
			return compareToInternal(other.content, other.from, other.to) == 0;
		}
	}

	@Override
	public String toString() {
		return "CharArrayPiece [content=" + new String(content, from, to-from) + "]";
	}
	
	public int compareTo(final char[] content, final int from, final int to) {
		if (content == null) {
			throw new NullPointerException("Source content can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length - 1)); 
		}
		else if (to < 0 || to >= content.length) {
			throw new IllegalArgumentException("To position ["+to+"] out of range 0.."+(content.length - 1)); 
		}
		else if (to < from) {
			throw new IllegalArgumentException("To position ["+to+"] can't be less than from position ["+from+"]"); 
		}
		else {
			return compareToInternal(content, from, to);
		}
	}

	private int compareToInternal(final char[] content, final int from, final int to) {
		final char[]	right = content, left = this.content;
		final int		rightDispl = from, leftDispl = this.from;
		
		for (int index = 0, maxIndex = Math.min(to - from, this.to - this.from), delta; index < maxIndex; index++) {
			if ((delta = right[rightDispl + index] - left[leftDispl + index]) != 0) {
				return delta;
			}
		}
		return (to-from) - (this.to-this.from);
	}
	
	private int hashCodeInternal() {
		final char[]	content = this.content;
		final int 		prime = 31;
		int 			result = 1;
		
		for (int index = from; index < to; index++) {
			result = prime * result + content[index];
		}
		return result;
	}
}
