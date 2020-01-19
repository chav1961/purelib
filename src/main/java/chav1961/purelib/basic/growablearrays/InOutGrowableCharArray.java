package chav1961.purelib.basic.growablearrays;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.CharStreamPrinter;
import chav1961.purelib.basic.intern.UnsafedCharUtils;

/**
 * <p>This class implements {@linkplain CharStreamPrinter} interface for {@linkplain GrowableCharArray} class. It allow the class 
 * use print and println methods to put data into. It implements {@linkplain CharStreamPrinter} interface, not extends {@linkplain PrintStream}
 * or {@linkplain PrintWriter} classes, because these classes suppress all {@linkplain IOException} during call and there is no guarantee for successful
 * completion of it's methods</p> 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.basic.growablearrays JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class InOutGrowableCharArray extends GrowableCharArray implements CharStreamPrinter<InOutGrowableCharArray> {
	static final char[]			CRNL = System.getProperty("line.separator").toCharArray();
	static final char[]			NULL = "null".toCharArray();
	static final char[]			TRUE = "true".toCharArray();
	static final char[]			FALSE = "false".toCharArray();
	private static final int	INITIAL_BUFFER_SIZE = 32;

	private char[]	buffer = new char[INITIAL_BUFFER_SIZE];
	
	public InOutGrowableCharArray(final boolean usePlain) {
		super(usePlain);
	}

	public InOutGrowableCharArray(final boolean usePlain, final int initialPow) {
		super(usePlain, initialPow);
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}
	
	@Override
	public InOutGrowableCharArray println() throws PrintingException {
		append(CRNL);
		return this;
	}

	@Override
	public InOutGrowableCharArray print(final char data) throws PrintingException {
		append(data);
		return this;
	}

	@Override
	public InOutGrowableCharArray println(final char data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final byte data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public InOutGrowableCharArray println(final byte data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final short data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public InOutGrowableCharArray println(final short data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final int data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public InOutGrowableCharArray println(final int data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final long data) throws PrintingException {
		final int	len = UnsafedCharUtils.uncheckedPrintLong(buffer, 0, data, true);
		
		if (len < 0) {
			buffer = Arrays.copyOf(buffer,2*buffer.length);
			return print(data);
		}
		else {
			return print(buffer,0,len);
		}
	}

	@Override
	public InOutGrowableCharArray println(final long data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final float data) throws PrintingException {
		return print((double)data);
	}

	@Override
	public InOutGrowableCharArray println(final float data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final double data) throws PrintingException {
		final int	len = UnsafedCharUtils.uncheckedPrintDouble(buffer, 0, data, true);
		
		if (len < 0) {
			buffer = Arrays.copyOf(buffer,2*buffer.length);
			return print(data);
		}
		else {
			return print(buffer,0,len);
		}
	}

	@Override
	public InOutGrowableCharArray println(final double data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final boolean data) throws PrintingException {
		print(data ? TRUE : FALSE);
		return this;
	}

	@Override
	public InOutGrowableCharArray println(final boolean data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final String data) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			append(data);
			return this;
		}
	}

	@Override
	public InOutGrowableCharArray println(final String data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final String data, int from, int len) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			append(data,from,len);
			return this;
		}
	}

	@Override
	public InOutGrowableCharArray println(final String data, int from, int len) throws PrintingException {
		print(data,from,len);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final char[] data) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			append(data);
			return this;
		}
	}

	@Override
	public InOutGrowableCharArray println(final char[] data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final char[] data, final int from, final int len) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			append(data,from,len);
			return this;
		}
	}

	@Override
	public InOutGrowableCharArray println(final char[] data, final int from, final int len) throws PrintingException {
		print(data,from,len);
		return println();
	}

	@Override
	public InOutGrowableCharArray print(final Object data) throws PrintingException {
		return data == null ? printNull() : print(data.toString());
	}

	@Override
	public InOutGrowableCharArray println(final Object data) throws PrintingException {
		print(data);
		return println();
	}
	
	private InOutGrowableCharArray printNull() {
		append(NULL);
		return this;
	}
}
