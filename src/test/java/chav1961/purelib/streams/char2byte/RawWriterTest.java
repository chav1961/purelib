package chav1961.purelib.streams.char2byte;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.streams.byte2char.RawReader;

@Tag("OrdinalTestCategory")
public class RawWriterTest {
	@Test
	public void basicTest() throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final RawWriter			rwr = new RawWriter(baos, 16)) {
				rwr.write("test1");
				rwr.write("test string1");
				rwr.write("test2");
				rwr.flush();
				rwr.write("test string2");
				
				try{rwr.write((char[])null, 0, 0);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{rwr.write(new char[] {'s'}, -1, 0);
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{rwr.write(new char[] {'s'}, 0, 100);
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
			}
			final byte[]	buffer = baos.toByteArray();
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(buffer);
				final Reader				rdr = new RawReader(bais, 100)) {
				final char[]				result = new char[100];
				
				Assert.assertEquals(5, rdr.read(result,0,5));
				Assert.assertEquals("test1", new String(result,0,5));
				Assert.assertEquals(12, rdr.read(result,0,12));
				Assert.assertEquals("test string1", new String(result,0,12));
				Assert.assertEquals(5, rdr.read(result,0,5));
				Assert.assertEquals("test2", new String(result,0,5));
				Assert.assertEquals(12, rdr.read(result,0,12));
				Assert.assertEquals("test string2", new String(result,0,12));
				Assert.assertEquals(-1, rdr.read(result));
				Assert.assertEquals(-1, rdr.read(result));
			}
			
			try{new RawWriter(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{new RawWriter(baos, 0);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}
}
