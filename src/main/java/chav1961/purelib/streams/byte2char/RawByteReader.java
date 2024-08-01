package chav1961.purelib.streams.byte2char;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * <p>This class is an implementation of fast reader. This class considers, that input stream will be a set of 1-byte chars (for example ASCII chars).
 * Every char in the range 0..127 will be converted from byte to char 'as-is', all other chars will be converted by a decoding table, passed as an argument
 * into it's constructor. The decoding table can have size from 0 to 128 items, and every item in it is a char that appropriates to byte from the range 128..255
 * (the same first char in the table appropriates to (byte)128, the second - to (byte)129 etc. If byte value is outside the table, it will be replaced by '?'.</p>   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class RawByteReader extends Reader {
	private static final char[]	NULL_FAST_CONVERSION_TABLE = new char[0];
	
	private final InputStream	nested;
	private final char[]		fct;
	private volatile byte[]		buffer = new byte[1024];
	
	public RawByteReader(final InputStream nested) {
		this(nested, NULL_FAST_CONVERSION_TABLE);
	}

	public RawByteReader(final InputStream nested, final char[] fastConversionTable) {
		if (nested == null) {
			throw new NullPointerException("Nested input stream can't be null");
		}
		else if (fastConversionTable == null) {
			throw new NullPointerException("Fast convertsion table can't be null");
		}
		else {
			this.nested = nested;
			this.fct = fastConversionTable;
		}
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		if (cbuf == null) {
			throw new NullPointerException("Buffer to read can't be null");
		}
		else if (off < 0 || off >= cbuf.length) {
			throw new IllegalArgumentException("Offset ["+off+"] out of range 0.."+(cbuf.length-1));
		}
		else if (len <= 0) {
			throw new IllegalArgumentException("Len ["+len+"] must be greater than 0");
		}
		else if (off + len < 0 || off + len >= cbuf.length) {
			throw new IllegalArgumentException("Offset+len ["+(off+len)+"] out of range 0.."+(cbuf.length-1));
		}
		else {
			byte[]	content = buffer;
			int		length = content.length;
			
			while (length < len) {
				length *= 2; 
			}
			if (content.length < length) {
				buffer = content = new byte[length];
			}
			int		returned = nested.read(content, 0, len);
			
			if (returned > 0) {
				final char[]	table = fct;
				final int		tableLen = table.length; 
				
				for(int index = 0; index < len; index++) {
					char	symbol = (char)(0x00FF & content[index]);
					
					if (symbol >= 128) {
						if (symbol - 128 < tableLen) {
							symbol = table[symbol - 128];
						}
						else {
							symbol = '?';
						}
					}
					cbuf[off + index] = symbol;
				}
			}
			return returned; 
		}
		
	}

	@Override
	public void close() throws IOException {
	}
}
