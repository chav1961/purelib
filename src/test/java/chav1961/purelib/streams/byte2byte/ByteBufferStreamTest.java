package chav1961.purelib.streams.byte2byte;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class ByteBufferStreamTest {

	@Test
	public void basicTest() throws IOException {
		final ByteBuffer	bb = ByteBuffer.allocate(16); 
		
		try(final OutputStream	os = new ByteBufferOutputStream(bb)) {
			os.write("test string".getBytes());
			try {
				os.write("overflow string".getBytes());
				Assert.fail("Mandatory exception was not detected (buffer overflow)");
			} catch (IOException exc) {
			}
			os.flush();
		}
		
		try{new ByteBufferOutputStream(null).close();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try(final InputStream	is = new ByteBufferInputStream(bb)) {
			final byte[]	content = new byte[100];
			final int		len = is.read(content);
			
			Assert.assertEquals("test string", new String(content, 0, len));
		}

		try{new ByteBufferInputStream(null).close();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
