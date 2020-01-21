package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.CharStreamPrinter;
import chav1961.purelib.basic.intern.UnsafedCharUtils;

/**
 * <p>This class is a wrapper to {@linkplain PrintStream} class. It is used to prevent suppression of all I/O exception by source {@linkplain PrintStream} class,
 * because it blinks I/O errors during execution and can be caused to unpredictable behavior.</p>    
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class PrintWriterWrapper implements CharStreamPrinter<PrintWriterWrapper> {
	static final char[]			CRNL = System.getProperty("line.separator").toCharArray();
	static final char[]			NULL = "null".toCharArray();
	static final char[]			TRUE = "true".toCharArray();
	static final char[]			FALSE = "false".toCharArray();
	private static final int	INITIAL_BUFFER_SIZE = 32;

	private final Writer	delegate;
	private char[]			buffer = new char[INITIAL_BUFFER_SIZE];
	
	/**
	 * <p>Constructor of the class</p>
	 * @param writer nested writer to print content to
	 * @throws NullPointerException when nested writer is null
	 */
	public PrintWriterWrapper(final Writer writer) throws NullPointerException {
		if (writer == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else {
			this.delegate = writer;
		}
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public PrintWriterWrapper println() throws PrintingException {
		try{delegate.write(CRNL);
		} catch (IOException e) {
			throw new PrintingException(e);
		}
		return this;
	}

	@Override
	public PrintWriterWrapper print(char data) throws PrintingException {
		buffer[0] = data;
		return print(buffer,0,1);
	}

	@Override
	public PrintWriterWrapper println(char data) throws PrintingException {
		buffer[0] = data;
		return println(buffer,0,1);
	}

	@Override
	public PrintWriterWrapper print(byte data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public PrintWriterWrapper println(byte data) throws PrintingException {
		return println((long)data);
	}

	@Override
	public PrintWriterWrapper print(short data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public PrintWriterWrapper println(short data) throws PrintingException {
		return println((long)data);
	}

	@Override
	public PrintWriterWrapper print(int data) throws PrintingException {
		return print((long)data);
	}

	@Override
	public PrintWriterWrapper println(int data) throws PrintingException {
		return println((long)data);
	}

	@Override
	public PrintWriterWrapper print(long data) throws PrintingException {
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
	public PrintWriterWrapper println(long data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public PrintWriterWrapper print(float data) throws PrintingException {
		return print((double)data);
	}

	@Override
	public PrintWriterWrapper println(float data) throws PrintingException {
		return println((double)data);
	}

	@Override
	public PrintWriterWrapper print(double data) throws PrintingException {
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
	public PrintWriterWrapper println(double data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public PrintWriterWrapper print(boolean data) throws PrintingException {
		try{delegate.write(data ? TRUE : FALSE);
		} catch (IOException e) {
			throw new PrintingException(e);
		}
		return this;
	}

	@Override
	public PrintWriterWrapper println(boolean data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public PrintWriterWrapper print(String data) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			try{delegate.write(data);
			} catch (IOException e) {
				throw new PrintingException(e);
			}
			return this;
		}
	}

	@Override
	public PrintWriterWrapper println(String data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public PrintWriterWrapper print(String data, int from, int len) throws PrintingException, StringIndexOutOfBoundsException {
		if (data == null) {
			return printNull();
		}
		else {
			return print(data.substring(from,from+len));
		}
	}

	@Override
	public PrintWriterWrapper println(String data, int from, int len) throws PrintingException, StringIndexOutOfBoundsException {
		if (data == null) {
			printNull();
			return println();
		}
		else {
			print(data.substring(from,from+len));
			return println();
		}
	}

	@Override
	public PrintWriterWrapper print(char[] data) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			return print(data,0,data.length);
		}
	}

	@Override
	public PrintWriterWrapper println(char[] data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public PrintWriterWrapper print(char[] data, int from, int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		if (data == null) {
			return printNull();
		}
		else {
			try{delegate.write(data,from,len);
			} catch (IOException e) {
				throw new PrintingException(e);
			}
			return this;
		}
	}

	@Override
	public PrintWriterWrapper println(char[] data, int from, int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		print(data,from,len);
		return println();
	}

	@Override
	public PrintWriterWrapper print(Object data) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			return print(data.toString());
		}
	}

	@Override
	public PrintWriterWrapper println(Object data) throws PrintingException {
		print(data);
		return println();
	}

	private PrintWriterWrapper printNull() throws PrintingException {
		try{delegate.write(NULL);
		} catch (IOException e) {
			throw new PrintingException(e);
		}
		return this;
	}
}
