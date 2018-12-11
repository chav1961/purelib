package chav1961.purelib.streams;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>This wrapper class can be used in conjunction with any Writer to get full access to Writer's methods without extending existent class to wrap.</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class WriterWrapper extends Writer {
	private final Writer	delegate;

	/**
	 * <p>Constructor of the class</p>
	 * @param delegate nested writer to wrap
	 */
	public WriterWrapper(final Writer delegate) {
		if (delegate == null) {
			throw new NullPointerException("Writer delegate can't be null");
		}
		else {
			this.delegate = delegate;
		}
	}
	
	@Override
    public void write(final int c) throws IOException {
		delegate.write(c);
    }

	@Override
    public void write(final char cbuf[]) throws IOException {
		delegate.write(cbuf);
    }

	@Override
    public void write(final char cbuf[], final int off, final int len) throws IOException {
		delegate.write(cbuf, off, len);
    }

	@Override
    public void write(final String str) throws IOException {
		delegate.write(str);
    }

	@Override
    public void write(final String str, final int off, final int len) throws IOException {
		delegate.write(str, off, len);
    }

	@Override
    public Writer append(final CharSequence csq) throws IOException {
		delegate.append(csq);
        return this;
    }

	@Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
		delegate.append(csq, start, end);
        return this;
    }

	@Override
    public Writer append(final char c) throws IOException {
		delegate.append(c);
        return this;
    }

	@Override
    public void flush() throws IOException {
		delegate.flush();
    }

	@Override
    public void close() throws IOException {
		delegate.close();
    }
}
