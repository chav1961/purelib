package chav1961.purelib.streams.byte2byte;



import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

@Tag("OrdinalTestCategory")
public class ZLibStreamTest {
	@Test
	public void lifeCycleTest() throws IOException {
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			try(final ZLibOutputStream		zlos = new ZLibOutputStream(baos)) {
		
				zlos.write("test string".getBytes());
				zlos.flush(); 
				
				try {zlos.write(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try {zlos.write(null,0,1);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try {zlos.write(new byte[1],2,1);
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try {zlos.write(new byte[1],0,2);
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
			}
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray());
				final ZLibInputStream		zlis = new ZLibInputStream(bais);
				final Reader				rdr = new InputStreamReader(zlis);
				final BufferedReader		brdr = new BufferedReader(rdr)) {
				
				final String				line = brdr.readLine(); 
				Assert.assertEquals("test string",line);

				try {zlis.read(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try {zlis.read(null,0,1);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try {zlis.read(new byte[1],2,1);
					Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
				try {zlis.read(new byte[1],0,2);
					Assert.fail("Mandatory exception was not detected (3-rd argument out of range)");
				} catch (IllegalArgumentException exc) {
				}
			}
			
			try(final OutputStream os = new ZLibOutputStream(null)) {
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try(final InputStream os = new ZLibInputStream(null)) {
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
		}
	}

	@Test
	public void basicTest() throws IOException {
		final byte[]	content = new byte[1<<20];
		
		for (int index = 0; index < content.length; index++) {
			content[index] = (byte)index;
		}
		
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			try(final ZLibOutputStream		zlos = new ZLibOutputStream(baos)) {
				
				zlos.write(content);
			}
			
			try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray());
				final ZLibInputStream		zlis = new ZLibInputStream(bais);
				final ByteArrayOutputStream	tmp = new ByteArrayOutputStream()) {
				
				Utils.copyStream(zlis,tmp);
				Assert.assertArrayEquals(content,tmp.toByteArray());
			}			
		}
	}
}
