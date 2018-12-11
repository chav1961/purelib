package chav1961.purelib.streams.byte2byte;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;

public class ZLibStreamTest {
	@Test
	public void complexTest() throws IOException {
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
				
				Assert.assertEquals(brdr.readLine(),"test string");

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
			
			try {new ZLibOutputStream(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try {new ZLibInputStream(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
		}
	}
}
