package chav1961.purelib.streams.char2char;

import java.io.IOException;
import java.io.Writer;

public class AppendableWriter extends Writer {
	private final Appendable	app;
	
	public AppendableWriter(final Appendable app) {
		if (app == null) {
			throw new NullPointerException("Appendable to write can't be null");
		}
		else {
			this.app = app; 
		}
	}
	
	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		if (cbuf == null) {
			app.append(null);
		}
		else if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
        	throw new IndexOutOfBoundsException();
        } else if (len != 0) {
        	for (int index = off; index < off+len; index++) {
        		app.append(cbuf[index]);
        	}
        }
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}
}
