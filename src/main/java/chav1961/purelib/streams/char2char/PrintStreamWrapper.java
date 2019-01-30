package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.CharStreamPrinter;

public class PrintStreamWrapper implements CharStreamPrinter<PrintStreamWrapper> {
	static final char[]			NULL = "null".toCharArray();
	
	private final PrintStream	delegate;
	
	public PrintStreamWrapper(final PrintStream writer) throws NullPointerException {
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
	public PrintStreamWrapper println() throws PrintingException {
		delegate.println();
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(char data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(char data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(byte data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(byte data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(short data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(short data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(int data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(int data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(long data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(long data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(float data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(float data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(double data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(double data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(boolean data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(boolean data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(String data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(String data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper print(String data, int from, int len) throws PrintingException, StringIndexOutOfBoundsException {
		if (data == null) {
			return printNull();
		}
		else {
			delegate.print(data.substring(from,from+len));
			return checkErrors();
		}
	}

	@Override
	public PrintStreamWrapper println(String data, int from, int len) throws PrintingException, StringIndexOutOfBoundsException {
		if (data == null) {
			printNull();
			return println();
		}
		else {
			delegate.println(data.substring(from,from+len));
			return checkErrors();
		}
	}

	@Override
	public PrintStreamWrapper print(char[] data) throws PrintingException {
		if (data == null) {
			return printNull();
		}
		else {
			delegate.print(data);
			return checkErrors();
		}
	}

	@Override
	public PrintStreamWrapper println(char[] data) throws PrintingException {
		if (data == null) {
			printNull();
			return println();
		}
		else {
			delegate.println(data);
			return checkErrors();
		}
	}

	@Override
	public PrintStreamWrapper print(char[] data, int from, int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		if (data == null) {
			return printNull();
		}
		else {
			delegate.print(Arrays.copyOfRange(data,from,from+len));
			return checkErrors();
		}
	}

	@Override
	public PrintStreamWrapper println(char[] data, int from, int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		if (data == null) {
			printNull();
			return println();
		}
		else {
			delegate.println(Arrays.copyOfRange(data,from,from+len));
			return checkErrors();
		}
	}

	@Override
	public PrintStreamWrapper print(Object data) throws PrintingException {
		delegate.print(data);
		return checkErrors();
	}

	@Override
	public PrintStreamWrapper println(Object data) throws PrintingException {
		delegate.println(data);
		return checkErrors();
	}

	private PrintStreamWrapper checkErrors() throws PrintingException {
		if (delegate.checkError()) {
			throw new PrintingException("I/O error in the print stream");
		}
		else {
			return this;
		}
	}

	private PrintStreamWrapper printNull() throws PrintingException {
		delegate.print(NULL);
		return checkErrors();
	}
}
