package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Arrays;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.CodeCharStreamPrinter;

public class CodePrintStreamWrapper implements CodeCharStreamPrinter<CodePrintStreamWrapper> {
	static final char[]			NULL = "null".toCharArray();
	
	private final PrintStream	delegate;
	private final char[]		buffer = new char[1024];
	private boolean				startLine = true;
	private int					depth = 0;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param writer nested writer to print content to
	 * @throws NullPointerException when nested writer is null
	 */
	public CodePrintStreamWrapper(final PrintStream writer) throws NullPointerException {
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
	public CodePrintStreamWrapper println() throws PrintingException {
		delegate.println();
		startLine = true;
		return this;
	}

	@Override
	public CodePrintStreamWrapper print(final char data) throws PrintingException {
		printPrefix();
		delegate.append(data);
		return this;
	}


	@Override
	public CodePrintStreamWrapper println(final char data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final byte data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final byte data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final short data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final short data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final int data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final int data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final long data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final long data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final float data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final float data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final double data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final double data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final boolean data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final boolean data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final String data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final String data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final String data, final int from, final int len) throws PrintingException, StringIndexOutOfBoundsException {
		printPrefix();
		if (data == null) {
			print(NULL);
		}
		else {
			delegate.print(data.substring(from, from + len));
		}
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(String data, int from, int len) throws PrintingException, StringIndexOutOfBoundsException {
		print(data, from, len);
		return this;
	}

	@Override
	public CodePrintStreamWrapper print(final char[] data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final char[] data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final char[] data, final int from, final int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		printPrefix();
		if (data == null) {
			print(NULL);
		}
		else {
			delegate.print(Arrays.copyOfRange(data, from, from + len));
		}
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final char[] data, final int from, final int len) throws PrintingException, ArrayIndexOutOfBoundsException {
		print(data, from, len);
		return this;
	}

	@Override
	public CodePrintStreamWrapper print(final Object data) throws PrintingException {
		printPrefix();
		delegate.print(data);
		return this;
	}

	@Override
	public CodePrintStreamWrapper println(final Object data) throws PrintingException {
		print(data);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final String format, final Object... parameters) throws PrintingException, IllegalArgumentException {
		if (Utils.checkEmptyOrNullString(format)) {
			throw new IllegalArgumentException("Format string can't be null or empty");
		}
		else {
			print(format.formatted(parameters));
			return this;
		}
	}

	@Override
	public CodePrintStreamWrapper println(final String format, final Object... parameters) throws PrintingException, IllegalArgumentException {
		print(format, parameters);
		return this;
	}

	@Override
	public CodePrintStreamWrapper enter() throws PrintingException {
		depth++;
		return this;
	}

	@Override
	public CodePrintStreamWrapper leave() throws PrintingException, IllegalStateException {
		if (depth <= 0) {
			throw new IllegalStateException("Depth level exhausted. Check all the leave() calls has paired enter() calls");
		}
		else {
			depth--;
			return this;
		}
	}

	@Override
	public CodePrintStreamWrapper print(final Reader rdr) throws PrintingException, NullPointerException {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else {
			try{
				int	len;
				
				while ((len = rdr.read(buffer)) > 0) {
					print(buffer, 0, len);
				}
				return this;
			} catch (IOException e) {
				throw new PrintingException(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public CodePrintStreamWrapper println(final Reader rdr) throws PrintingException, NullPointerException {
		print(rdr);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final Reader rdr, final SubstitutionSource src) throws PrintingException, NullPointerException {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else if (src == null) {
			throw new NullPointerException("Substitution source can't be null");
		}
		else {
			try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)->substitute(data, from, length, src))) {
				lblp.write(rdr);
				lblp.flush();
				return this;
			} catch (IOException | SyntaxException e) {
				throw new PrintingException(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public CodePrintStreamWrapper println(final Reader rdr, final SubstitutionSource src) throws PrintingException, NullPointerException {
		print(rdr, src);
		return println();
	}

	@Override
	public CodePrintStreamWrapper print(final Reader rdr, final CharSubstitutionSource src) throws PrintingException, NullPointerException {
		if (rdr == null) {
			throw new NullPointerException("Reader can't be null");
		}
		else if (src == null) {
			throw new NullPointerException("Substitution source can't be null");
		}
		else {
			try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)->substitute(data, from, length, src))) {
				lblp.write(rdr);
				lblp.flush();
				return this;
			} catch (IOException | SyntaxException e) {
				throw new PrintingException(e.getLocalizedMessage());
			}
		}
	}

	@Override
	public CodePrintStreamWrapper println(final Reader rdr, final CharSubstitutionSource src) throws PrintingException, NullPointerException {
		print(rdr, src);
		return println();
	}

	private void printPrefix() throws PrintingException {
		if (startLine) {
			for(int index = 0; index < depth; index++) {
				delegate.print('\t');
			}
			startLine = false;
		}
	}
	
	private void substitute(final char[] data, final int from, final int length, final SubstitutionSource src) throws IOException, SyntaxException {
		CharUtils.substitute("code", new String(data, from, length), src);
	}
	
	private void substitute(final char[] data, final int from, final int length, final CharSubstitutionSource src) throws IOException, SyntaxException {
		CharUtils.substitute("code", data, from, length, src);
	}
}
