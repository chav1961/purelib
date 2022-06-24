package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.InputStream;

public abstract class FragmentedInputStream extends InputStream {
	private PieceDescriptor	head = null, tail = null;
	private int				currentDispl = 0, currentSize = 0;
	
	public FragmentedInputStream() {
	}

	public FragmentedInputStream(final byte[] content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			appendInternal(content, 0, content.length);
		}
	}

	public FragmentedInputStream(final byte[] content, final int from, final int len) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length - 1)); 
		}
		else if (len < 0 || from + len >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] + length ["+len+"] out of range 0.."+(content.length - 1)); 
		}
		else {
			appendInternal(content, 0, content.length);
		}
	}

	protected abstract boolean morePieces() throws IOException;
	
	public FragmentedInputStream append(final byte[] content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			appendInternal(content, 0, content.length);
		}
		return this;
	}

	public FragmentedInputStream append(final byte[] content, final int from, final int len) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length - 1)); 
		}
		else if (len < 0 || from + len >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] + length ["+len+"] out of range 0.."+(content.length - 1)); 
		}
		else {
			appendInternal(content, 0, content.length);
			return this;
		}
	}
	
	@Override
	public int read() throws IOException {
		if (ensureData(1)) {
			return head.content[currentDispl++];
		} else if (getNext() || morePieces() && getNext()) {
			return read();
		}
		else {
			return -1;
		}
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset position ["+off+"] out of range 0.."+(b.length - 1)); 
		}
		else if (len < 0 || off + len >= b.length) {
			throw new IllegalArgumentException("Offset position ["+off+"] + length ["+len+"] out of range 0.."+(b.length - 1)); 
		}
		else {
			if (ensureData(len)) {
				final PieceDescriptor	pd = head;
				
				System.arraycopy(pd.content, currentDispl, b, off, len);
				currentDispl += len;
				return len;
			}
			else if (currentDispl >= currentSize) {
				if (getNext() || morePieces() && getNext()) {
					return read(b,off, len);
				}
				else {
					return -1;
				}
			}
			else {
				final PieceDescriptor	pd = head;
				final int				tailLen = currentSize - currentDispl;
				
				System.arraycopy(pd.content, currentDispl, b, off, tailLen);
				currentDispl += tailLen;
				return tailLen;
			}
		}
	}

	private boolean ensureData(final int size) throws IOException {
		if (head == null) {
			return false;
		}
		else if (head != null && currentDispl >= currentSize) {
			return getNext();
		}
		else {
			return currentDispl + size < currentSize;
		}
	}

	private void appendInternal(final byte[] content, final int from, final int len) {
		if (head == null) {
			final PieceDescriptor	desc = new PieceDescriptor(null, content, from, len);
			
			head = tail = desc;
		}
		else {
			final PieceDescriptor	last = tail;
			final PieceDescriptor	desc = new PieceDescriptor(last.next, content, from, len);
			
			tail = desc;
		}
	}
	
	private boolean getNext() {
		if (head == null) {
			return false;
		}
		else {
			final PieceDescriptor	current = head, next = current.next;
			
			currentDispl = next.from;
			currentSize = next.from + next.length;
			current.next = null;
			head = next;
			return true;
		}
	}
	
	static class PieceDescriptor {
		PieceDescriptor	next;
		final byte[]	content;
		final int		from;
		final int		length;
		
		public PieceDescriptor(final PieceDescriptor next, final byte[] content, final int from, final int length) {
			this.next = next;
			this.content = content;
			this.from = from;
			this.length = length;
		}

		@Override
		public String toString() {
			return "PieceDescriptor [next=" + next + ", from=" + from + ", length=" + length + "]";
		}
	}
}
