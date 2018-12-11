package chav1961.purelib.streams;

import java.io.IOException;
import java.io.Reader;

/**
 * <p>This wrapper class can be used in conjunction with any Reader to get full access to Reader's methods without extending existent class to wrap.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class ReaderWrapper extends Reader {
	private final Reader	delegate;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param delegate nested reader to wrap
	 */
	public ReaderWrapper(final Reader delegate) {
		if (delegate == null) {
			throw new NullPointerException("Reader delegate can't be null"); 
		}
		else {
			this.delegate = delegate;
		}
	}

	@Override
	public int read(final java.nio.CharBuffer target) throws IOException {
		return delegate.read(target);
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	@Override
	public int read(final char cbuf[]) throws IOException {
		return delegate.read(cbuf);
	}

	@Override
	public int read(final char cbuf[], final int off, final int len) throws IOException {
		return delegate.read(cbuf, off, len);
	}

	@Override
	public long skip(final long n) throws IOException {
		return delegate.skip(n);
	}

	@Override
	public boolean ready() throws IOException {
		return delegate.ready();
	}

	@Override
	public boolean markSupported() {
	    return delegate.markSupported();
	}

	@Override
	public void mark(final int readAheadLimit) throws IOException {
		delegate.mark(readAheadLimit);
	}

	@Override
	public void reset() throws IOException {
		delegate.reset();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
