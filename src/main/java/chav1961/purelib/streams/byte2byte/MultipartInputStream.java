package chav1961.purelib.streams.byte2byte;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.ZipInputStream;

import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.streams.MultipartEntry;

/**
 * <p>This class is a wrapper to process "miltipart/form-data" content. It's usage is identical to {@linkplain ZipInputStream}:</p>
 * <code>
 * MultipartInputStream mis = mew MultipartInputStream(&lt;someInputStream&gt;);
 * MultipartEntry me;
 * while ((me = mis.getNextEntry()) != null) {
 * 		readPartContent(mis);
 * }
 * // mis.close(); - optional
 * </code> 
 * @see ZipInputStream
 * @see MultipartEntry 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class MultipartInputStream extends FilterInputStream {
	private static BitCharSet	KEY_NAME_CHARS = new BitCharSet();
	private static final int	STATE_BEFORE_PART = 1;
	private static final int	STATE_IN_PART = 2;
	private static final int	STATE_AFTER_PART = 3;
	private static final int	STATE_AFTER_EOF = 4;
	
	private final byte[]		buffer = new byte[8192];
	private final byte[]		splitter;
	private final byte[]		forByte = new byte[1];
	private int					state = STATE_BEFORE_PART;
	private int					displ = 0, length = 0;
	
	static {
		KEY_NAME_CHARS.add('-');
		KEY_NAME_CHARS.addRange('a', 'z');
		KEY_NAME_CHARS.addRange('A', 'Z');
		KEY_NAME_CHARS.addRange('0', '9');
	}
	
	public MultipartInputStream(final InputStream nested) throws IOException {
		super(nested);
		loadPiece();
		if (ensureEndsWithNL()) {
			this.splitter = readSplitter();
		}
		else {
			throw new IOException();
		}
	}
	
	public MultipartEntry getNextEntry() throws IOException {
		switch (state) {
			case STATE_IN_PART		:
				closeEntry();
			case STATE_BEFORE_PART	: case STATE_AFTER_PART	:
				if (ensureEndsWithNL()) {
					final Properties	props = new Properties(); 
							
					while (!isLineEmpty()) {
						displ = skipLine(parseHeader(props));
					}
					displ = skipLine(displ);
					state = STATE_IN_PART;
					return new MultipartEntry(props.get("Content-Disposition:name").toString(), props);
				}
				else {
					state = STATE_AFTER_EOF;
					return null;
				}
			case STATE_AFTER_EOF	:
				return null;
			default :
				throw new UnsupportedOperationException("State ["+state+"] is not supported yet"); 
		}
		
	}

	public void closeEntry() throws IOException {
		switch (state) {
			case STATE_IN_PART		:
				boolean	notEOF = true;
				
				if (!compare(displ, splitter)) {
					while ((notEOF = ensureEndsWithNL()) && !compare(displ, splitter)) {
						displ = skipLine(displ);
					}
				}
				if (notEOF) {
					if (buffer[displ + splitter.length] == '-' && buffer[displ + splitter.length + 1] == '-') {
						state = STATE_AFTER_EOF;
					}
					else {
						state = STATE_AFTER_PART;
					}
					displ = skipLine(displ + splitter.length);
				}
				else {
					state = STATE_AFTER_EOF;
				}
				break;
			case STATE_BEFORE_PART	: case STATE_AFTER_PART	: case STATE_AFTER_EOF	:
				break;
			default :
				throw new UnsupportedOperationException("State ["+state+"] is not supported yet"); 
		}
	}

	@Override
	public int read() throws IOException {
		final int	result = read(forByte);
		
		if (result < 0 ) {
			return result;
		}
		else {
			return (byte)forByte[0];
		}
	}
	
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("Buffer to read can't be null");
        } 
        else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException("Offset and/or length out of range 0.."+(b.length-1));
        } 
        else if (len == 0) {
            return 0;
        }
        else {
    		switch (state) {
				case STATE_IN_PART		:
					return readContent(b, off, len);
				case STATE_BEFORE_PART	: case STATE_AFTER_PART	: case STATE_AFTER_EOF	:
					return -1;
				default :
					throw new UnsupportedOperationException("State ["+state+"] is not supported yet"); 
			}
        }
	}
	
	@Override
	public long skip(final long n) throws IOException {
		if (n < 0) {
			throw new IllegalArgumentException("Skip value ["+n+"] can't be negative"); 
		}
        else {
    		switch (state) {
				case STATE_IN_PART		:
					final byte[]	skip = new byte[8192];
					long			rest = n, count;
					
					while ((count = readContent(skip, 0, Math.min((int)(rest & Integer.MAX_VALUE), skip.length))) >= 0) {
						rest -= count;
					}
					return n - rest;
				case STATE_BEFORE_PART	: case STATE_AFTER_PART	: case STATE_AFTER_EOF	:
					return 0;
				default :
					throw new UnsupportedOperationException("State ["+state+"] is not supported yet"); 
			}
        }
	}
	
	@Override
	public int available() throws IOException {
		return 0;
	}
	
	@Override
	public synchronized void mark(int readlimit) {
	}
	
	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("Reset operation is not supported"); 
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	private byte[] readSplitter() {
		final int		start = displ;
		final byte[]	result;
		
		while (displ < length && buffer[displ] != '\r' && buffer[displ] != '\n') {
			displ++;
		}
		result = Arrays.copyOfRange(buffer, start, displ);
		displ = skipLine(displ);
		return result;
	}
	
	private int parseHeader(final Properties props) throws IOException {
		char	divider = ':';
		String	prefixKey = "";
		
		displ--;		// anti-algorithm for the next line
		do {displ = skipBlank(displ + 1);	// skip ';'
		
			final int	startKey = displ;
			
			while (displ < length && KEY_NAME_CHARS.contains((char)buffer[displ])) {
				displ++;
			}
			final String	key = new String(buffer, startKey, displ - startKey);
			
			displ = skipBlank(displ);
			if ((char)buffer[displ] == divider) {
				final int 		startValue = displ = skipBlank(displ + 1);
				final String	value;

				if (buffer[displ] == '\"') {
					final int	startQuoted = ++displ; 
					
					while (displ < length && (char)buffer[displ] != '\"' && (char)buffer[displ] != '\r' && (char)buffer[displ] != '\n') {
						displ++;
					}
					
					if (buffer[displ] == '\"') {
						value = new String(buffer, startQuoted, displ - startQuoted);
						displ++;
					}
					else {
						throw new IOException();
					}
				}
				else {
					while (displ < length && KEY_NAME_CHARS.contains((char)buffer[displ])) {
						displ++;
					}
					value = new String(buffer, startValue, displ - startValue);
				}
				
				props.setProperty(prefixKey + key, value);
				if (divider == ':') {
					divider = '=';
					prefixKey = key + ':';
				}
				displ = skipBlank(displ);
			}
			else {
				final String	content = new String(buffer, startKey, displ-startKey);
				
				throw new IOException("Missing ["+divider+"] in the header content ["+content+"]"); 
			}
		} while (buffer[displ] == ';');
		
		return displ;
	}

	private boolean isLineEmpty() throws IOException {
		if (ensureEndsWithNL()) {
			int	temp = displ;
			
			while (temp < length && (buffer[temp] == '\r' || buffer[temp] == '\n')) {
				temp++;
			}
			return temp > displ; 
		}
		else {
			return true;
		}
	}

	private int skipLine(int displ) {
		int	countR = 0, countN = 0;
		
		while (displ < length && buffer[displ] != '\r' && buffer[displ] != '\n') {
			displ++;
		}
		while (displ < length && (buffer[displ] == '\r' && countR == 0 || buffer[displ] == '\n' && countN == 0)) {	// Prevent to skip two sequential lines
			if (buffer[displ] == '\r') {
				countR++;
			}
			else if (buffer[displ] == '\n') {
				countN++;
			}
			displ++;
		}
		return displ;
	}

	private int skipBlank(int displ) {
		while (displ < length && buffer[displ] <= (byte)' ' && buffer[displ] != '\r' && buffer[displ] != '\n') {
			displ++;
		}
		return displ;
	}

	private int readContent(final byte[] b, final int off, final int len) throws IOException {
		if (ensureEndsWithNL()) {
			if (compare(displ, splitter)) {
				if (buffer[displ + splitter.length] == '-' && buffer[displ + splitter.length + 1] == '-') {
					state = STATE_AFTER_EOF;
				}
				else {
					state = STATE_AFTER_PART;
				}
				displ = skipLine(displ);
				return -1;
			}
			else {
				final int	oldDispl = displ, newDispl = skipLine(displ);
				final int 	toMove = Math.min(len, newDispl - oldDispl);
				
				if (toMove == 0) {
					return -1;
				}
				else {
					System.arraycopy(buffer, displ, b, off, toMove);
					displ += toMove;
					return toMove;
				}
			}
		}
		else {
			return -1;
		}
	}
	
	private boolean ensureEndsWithNL() throws IOException {
		int	temp = displ;
		
		while (temp < length && buffer[temp] != '\r' && buffer[temp] != '\n') {
			temp++;
		}
		if (temp >= length) {
			if (loadPiece()) {
				return ensureEndsWithNL();
			}
			else {
				return false;
			}
		}
		else {
			return true;
		}
	}

	private boolean compare(final int displ, final byte[] splitter) {
		for(int index = 0; index < splitter.length; index++) {
			if (buffer[displ + index] != splitter[index]) {
				return false;
			}
		}
		return true;
	}
	
	private boolean loadPiece() throws IOException {
		if (displ < length) {
			System.arraycopy(buffer, displ, buffer, 0, length - displ);
			displ = length - displ;
		}
		else {
			displ = 0;
		}
		if ((length = in.read(buffer, displ, buffer.length - displ)) < 0) {
			return displ > 0;
		}
		else {
			length += displ;
			displ = 0;
			return true;
		}
	}
}
