package chav1961.purelib.streams.byte2char;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BufferedInputStreamReaderTest {
	public static final String	TEXT_CONTENT = "abcdefghijklmnopqrstuvwxys��������������������������������0123456789"; 
	
	public byte[]		content = new byte[Integer.MAX_VALUE/2];
	public char[]		buffer = new char[8192];
	public InputStream	stream;
	
	@Before
	public void prepare() throws UnsupportedEncodingException {
		final byte[]	pieceOfData = TEXT_CONTENT.getBytes("UTF-8");
		int				displ = 0;
		
		while (displ + pieceOfData.length < content.length) {
			System.arraycopy(pieceOfData,0,content,displ,pieceOfData.length);
			displ += pieceOfData.length;
		}
		stream = new ByteArrayInputStream(content,0,displ);
	}

	@After
	public void unprepare() throws IOException {
		stream.close();
		content = null;
	}
	
	@Test
	public void basicTest() {
	}

	@Test
	public void basicPerformanceTest() throws IOException {
		int					sum = 0;
		final byte[] 		buffer = new byte[65536];
		
		try{final long		nanos = System.nanoTime();
			int				len, index;
			
			while((len = stream.read(buffer)) > 0) {
				for (index  = 0; index < len; index++) {
					sum += buffer[index];
				}
			}
			System.err.println("Empty duration="+((System.nanoTime()-nanos)/1000000)+" msec, sum="+sum);
		} finally {
			stream.close();
		}
	}
	
	@Test
	public void standardPerformanceTest() throws IOException {
		int sum = 0;
		
		try(final Reader 	rdr = new InputStreamReader(stream,"UTF-8")) {
			final long		nanos = System.nanoTime();
			int				len, index;
			
			while((len = rdr.read(buffer)) > 0) {
				for (index  = 0; index < len; index++) {
					sum += buffer[index];
				}
			}
			System.err.println("Standard duration="+((System.nanoTime()-nanos)/1000000)+" msec, sum="+sum);
		}
	}

	@Test
	public void thisPerformanceTest() throws IOException {
		int sum = 0;
		
		try(final Reader 	rdr = new BufferedInputStreamReader(stream,"UTF-8")) {
			final long		nanos = System.nanoTime();
			int				len, index;
			
			while((len = rdr.read(buffer)) > 0) {
				for (index  = 0; index < len; index++) {
					sum += buffer[index];
				}
			}
			System.err.println("This duration="+((System.nanoTime()-nanos)/1000000)+" msec, sum="+sum);
		}
	}
}
