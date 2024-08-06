package chav1961.purelib.streams.byte2char;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * <p>This class is an implementation of fast reader. This class considers, that input stream will be a set of 1-byte chars (for example ASCII chars).
 * Every char in the range 0..127 will be converted from byte to char 'as-is', all other chars will be converted by a decoding table, passed as an argument
 * into it's constructor. The decoding table can have size from 0 to 128 items, and every item in it is a char that appropriates to byte from the range 128..255
 * (the same first char in the table appropriates to (byte)128, the second - to (byte)129 etc. If byte value is outside the table, it will be replaced by '?'.</p>
 * <p>This class also contains two useful predefined tables for CP-866 and Windows-1251 encoding to use for fast reading russian content.</p>
 * <p>This class is not thread-safe</p>   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public class RawByteReader extends Reader {
	/**
	 * <p>Predefined table for CP-866 encoding</p>
	 */
	public static final char[]	CP866 = {
										//   0		1			2			3			4			5			6			7			8			9			A			B			C			D			E			F
							/* 0x80 */	'\u0410',	'\u0411',	'\u0412',	'\u0413',	'\u0414',	'\u0415',	'\u0416',	'\u0417',	'\u0418',	'\u0419',	'\u041A',	'\u041B',	'\u041C',	'\u041D',	'\u041E',	'\u041F',
							/* 0x90 */	'\u0420',	'\u0421',	'\u0422',	'\u0423',	'\u0424',	'\u0425',	'\u0426',	'\u0427',	'\u0428',	'\u0429',	'\u042A',	'\u042B',	'\u042C',	'\u042D',	'\u042E',	'\u042F',
							/* 0xA0 */	'\u0430',	'\u0431',	'\u0432',	'\u0433',	'\u0434',	'\u0435',	'\u0436',	'\u0437',	'\u0438',	'\u0439',	'\u043A',	'\u043B',	'\u043C',	'\u043D',	'\u043E',	'\u043F',
							/* 0xB0 */	'\u2591',	'\u2592',	'\u2593',	'\u2502',	'\u2524',	'\u2561',	'\u2562',	'\u2556',	'\u2555',	'\u2563',	'\u2551',	'\u2557',	'\u255D',	'\u255C',	'\u255B',	'\u2510',
							/* 0xC0 */	'\u2514',	'\u2534',	'\u252C',	'\u251C',	'\u2500',	'\u253C',	'\u255E',	'\u255F',	'\u255A',	'\u2554',	'\u2569',	'\u2566',	'\u2560',	'\u2550',	'\u256C',	'\u2567',
							/* 0xD0 */	'\u2568',	'\u2564',	'\u2565',	'\u2559',	'\u2558',	'\u2552',	'\u2553',	'\u256B',	'\u256A',	'\u2518',	'\u250C',	'\u2588',	'\u2584',	'\u258C',	'\u2590',	'\u2580',
							/* 0xE0 */	'\u0440',	'\u0441',	'\u0442',	'\u0443',	'\u0444',	'\u0445',	'\u0446',	'\u0447',	'\u0448',	'\u0449',	'\u044A',	'\u044B',	'\u044C',	'\u044D',	'\u044E',	'\u044F',
							/* 0xE0 */	'\u0401',	'\u0451',	'\u0404',	'\u0454',	'\u0407',	'\u0457',	'\u040E',	'\u045E',	'\u00B0',	'\u2219',	'\u00B7',	'\u221A',	'\u2116',	'\u00A4',	'\u25A0',	'\u00A0'
								};
	/**
	 * <p>Predefined table for Windows-1251 encoding</p>
	 */
	public static final char[]	CP1251 = {
										//   0		1			2			3			4			5			6			7			8			9			A			B			C			D			E			F
							/* 0x80 */	'\u0402',	'\u0403',	'\u201A',	'\u0453',	'\u201E',	'\u2026',	'\u2020',	'\u2021',	'\u20AC',	'\u2030',	'\u0409',	'\u2039',	'\u040A',	'\u040C',	'\u040B',	'\u040F',
							/* 0x90 */	'\u0452',	'\u2018',	'\u2019',	'\u201C',	'\u201D',	'\u2022',	'\u2013',	'\u2014',	'?',		'\u2122',	'\u0459',	'\u203A',	'\u045A',	'\u045C',	'\u045B',	'\u045F',	
							/* 0xA0 */	'\u00A0',	'\u040E',	'\u045E',	'\u0408',	'\u00A4',	'\u0490',	'\u00A6',	'\u00A7',	'\u0401',	'\u00A9',	'\u0404',	'\u00AB',	'\u00AC',	'\u00AD',	'\u00AE',	'\u0407',
							/* 0xB0 */	'\u00B0',	'\u00B1',	'\u0406',	'\u0456',	'\u0491',	'\u00B5',	'\u00B6',	'\u00B7',	'\u0451',	'\u2116',	'\u0454',	'\u00BB',	'\u0458',	'\u0405',	'\u0455',	'\u0457',
							/* 0xC0 */	'\u0410',	'\u0411',	'\u0412',	'\u0413',	'\u0414',	'\u0415',	'\u0416',	'\u0417',	'\u0418',	'\u0419',	'\u041A',	'\u041B',	'\u041C',	'\u041D',	'\u041E',	'\u041F',
							/* 0xD0 */	'\u0420',	'\u0421',	'\u0422',	'\u0423',	'\u0424',	'\u0425',	'\u0426',	'\u0427',	'\u0428',	'\u0429',	'\u042A',	'\u042B',	'\u042C',	'\u042D',	'\u042E',	'\u042F',
							/* 0xE0 */	'\u0430',	'\u0431',	'\u0432',	'\u0433',	'\u0434',	'\u0435',	'\u0436',	'\u0437',	'\u0438',	'\u0439',	'\u043A',	'\u043B',	'\u043C',	'\u043D',	'\u043E',	'\u043F',
							/* 0xF0 */	'\u0440',	'\u0441',	'\u0442',	'\u0443',	'\u0444',	'\u0445',	'\u0446',	'\u0447',	'\u0448',	'\u0449',	'\u044A',	'\u044B',	'\u044C',	'\u044D',	'\u044E',	'\u044F'
								};
	
	
	private static final char[]	NULL_FAST_CONVERSION_TABLE = new char[0];
	
	private final InputStream	nested;
	private final char[]		fct;
	private byte[]				buffer = new byte[1024];
	
	/**
	 * <p>Constructor of the class</p>
	 * @param nested nested input stream. Can't be null
	 */
	public RawByteReader(final InputStream nested) {
		this(nested, NULL_FAST_CONVERSION_TABLE);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param nested nested input stream. Can't be null
	 * @param fastConversionTable decoding table for bytes in range 128-255. Can't be null, but can have any size from 0 to 128.
	 */
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
		else if (off + len < 0 || off + len > cbuf.length) {
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
