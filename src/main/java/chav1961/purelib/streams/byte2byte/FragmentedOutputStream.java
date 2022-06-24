package chav1961.purelib.streams.byte2byte;

import java.io.IOException;
import java.io.OutputStream;

import chav1961.purelib.streams.byte2byte.FragmentedInputStream.PieceDescriptor;

public abstract class FragmentedOutputStream extends OutputStream {
	private final byte[]	buffer = new byte[1];
	private PieceDescriptor	head = null, tail = null;
	private int				currentDispl = 0, currentSize = 0;
	
	public FragmentedOutputStream() {
		
	}

	public FragmentedOutputStream(final byte[] content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			appendInternal(content, 0, content.length);
		}
	}

	public FragmentedOutputStream(final byte[] content, final int from, final int len) {
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
	
	public FragmentedOutputStream append(final byte[] content) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else {
			appendInternal(content, 0, content.length);
			return this;
		}
	}
	
	public FragmentedOutputStream append(final byte[] content, final int from, final int len) {
		if (content == null) {
			throw new NullPointerException("Content can't be null"); 
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length - 1)); 
		}
		else if (len < 0 || from + len > content.length) {
			throw new IllegalArgumentException("From position ["+from+"] + length ["+len+"] out of range 0.."+(content.length - 1)); 
		}
		else {
			appendInternal(content, 0, content.length);
			return this;
		}
	}
	
	@Override
	public void write(final int b) throws IOException {
		buffer[0] = (byte)b;
		write(buffer);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Buffer can't be null"); 
		}
		else if (off < 0 || off >= b.length) {
			throw new IllegalArgumentException("Offset position ["+off+"] out of range 0.."+(b.length - 1)); 
		}
		else if (len < 0 || off + len > b.length) {
			throw new IllegalArgumentException("Offset position ["+off+"] + length ["+len+"] out of range 0.."+(b.length - 1)); 
		}
		else if (head == null) {
			if (getNext()) {
				write(b, off, len);
			}
			else {
				throw new IOException("Content storage exhausted");
			}
		}
		else {
			final PieceDescriptor	current = head;
			final int				currentLen = Math.min(currentSize - currentDispl, len);
			
			System.arraycopy(b, off, current.content, currentDispl, currentLen);
			currentDispl += currentLen;
			
			if (currentLen < len) {
				if (!getNext()) {
					throw new IOException("Content storage exhausted");
				}
				else {
					write(b, off + currentLen, len - currentLen);
				}
			}
		}
	}
	
	public int getLastPieceFill() {
		return currentSize - currentDispl;
	}

	private void appendInternal(final byte[] content, final int from, final int len) {
		if (head == null) {
			final PieceDescriptor	desc = new PieceDescriptor(null, content, from, len);
			
			head = tail = desc;
		}
		else {
			final PieceDescriptor	last = tail;
			final PieceDescriptor	desc = new PieceDescriptor(last.next, content, from, len);
			
			tail = last.next = desc;
		}
	}

	private boolean getNext() throws IOException {
		final PieceDescriptor	current = head;
		
		if (current != null) {
			final PieceDescriptor	next = current.next;
			
			if (next == null) {
				head = null;
				if (morePieces()) {
					final PieceDescriptor	newPiece = head;
					
					currentDispl = newPiece.from;
					currentSize = newPiece.from + newPiece.length;
					return true;
				}
				else {
					throw new IOException("Content storage exhausted");
				}
			}
			else {
				current.next = null;
				currentDispl = next.from;
				currentSize = next.from + next.length;			
				head = next;
				return true;
			}
		}
		else if (morePieces()) {
			final PieceDescriptor	newPiece = head;
			
			currentDispl = newPiece.from;
			currentSize = newPiece.from + newPiece.length;			
			return true;
		}
		else {
			return false;
		}
	}

	
}
