package chav1961.purelib.streams.byte2char;

import org.junit.Test;

import chav1961.purelib.streams.char2byte.RawWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;

import org.junit.Assert;

public class RawReaderTest {
	@Test
	public void basicTest() throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final RawWriter			rwr = new RawWriter(baos, 16)) {
				rwr.write("test1");
				rwr.write("test string1");
				rwr.write("test2");
				rwr.flush();
				rwr.write("test string2");
			}
			final byte[]	buffer = baos.toByteArray();
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(buffer);
				final Reader				rdr = new RawReader(bais, 8)) {
				final char[]				result = new char[100];
				
				Assert.assertEquals(5, rdr.read(result,0,5));
				Assert.assertEquals("test1", new String(result,0,5));
				Assert.assertEquals(12, rdr.read(result,0,12));
				Assert.assertEquals("test string1", new String(result,0,12));
				Assert.assertEquals(5, rdr.read(result,0,5));
				Assert.assertEquals("test2", new String(result,0,5));
				Assert.assertEquals(12, rdr.read(result,0,12));
				Assert.assertEquals("test string2", new String(result,0,12));
				Assert.assertEquals(-1, rdr.read(result,0,1));
				Assert.assertEquals(-1, rdr.read(result,0,1));
				
				try{rdr.read((char[])null,0,1);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{rdr.read(result,-1,1);
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try{rdr.read(result,0,1000000);
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				
				try{new RawReader(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{new RawReader(bais, -1);
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
			}
		}
	}
}
