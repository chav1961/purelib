package chav1961.purelib.streams.char2byte;


import java.io.IOException;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * <p>This class is a fast, not thread-safe implementation of the standard {@linkplain OutputStreamWriter} class that can be used everywhere instead of it.It is not
 * a child of the {@linkplain OutputStreamWriter} but replacement for it</p>
 * 
 * @see Writer
 * @see OutputStreamWriter
 * @see chav1961.purelib.streams JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */
 class BufferedOutputStreamWriter extends Writer {
	public static final int			DEFAULT_BUFFER_SIZE = 65536;

	public BufferedOutputStreamWriter(final OutputStream os) throws NullPointerException, IllegalArgumentException, IOException {
		this(os,Charset.defaultCharset());
	}
	
	public BufferedOutputStreamWriter(final OutputStream os, final String encoding) throws NullPointerException, IllegalArgumentException, IOException {
		this(os,Charset.forName(encoding));
	}

	public BufferedOutputStreamWriter(final OutputStream os, final Charset charset) throws NullPointerException, IllegalArgumentException, IOException {
		this(os,DEFAULT_BUFFER_SIZE,charset);
	}

	public BufferedOutputStreamWriter(final OutputStream os, final int bufferSize, final Charset charset) throws NullPointerException, IllegalArgumentException, IOException{
		
	}
	

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
