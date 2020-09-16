package chav1961.purelib.streams.byte2byte;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Test;

public class CipherStreamTest {
	private static final byte[]		PSEUDOKEY = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	
	@Test
	public void basicTest() throws IOException {
		final SecretKeySpec 	key = new SecretKeySpec(PSEUDOKEY,"AES");
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final OutputStream 	cos = new CipherOutputStream(baos,"AES",key);
				final PrintWriter	pwr = new PrintWriter(cos)) {
				
				pwr.println("test string");
			}
			
			try{new CipherOutputStream(null,"AES",key);
				Assert.fail("Mandatory exception was not detected (null 1-st argument");
			} catch (NullPointerException exc) {
			}
			try{new CipherOutputStream(baos,null,key);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument");
			} catch (IllegalArgumentException exc) {
			}
			try{new CipherOutputStream(baos,"",key);
				Assert.fail("Mandatory exception was not detected (empty 2-st argument");
			} catch (IllegalArgumentException exc) {
			}
			try{new CipherOutputStream(baos,"AES",null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument");
			} catch (NullPointerException exc) {
			}
			
			try(final InputStream		is = new ByteArrayInputStream(baos.toByteArray());
				final InputStream		cis = new CipherInputStream(is,"AES",key);
				final Reader			rdr = new InputStreamReader(cis);
				final BufferedReader	brdr = new BufferedReader(rdr)) {
				
				Assert.assertEquals("test string",brdr.readLine());
				
				try{new CipherInputStream(null,"AES",key);
					Assert.fail("Mandatory exception was not detected (null 1-st argument");
				} catch (NullPointerException exc) {
				}
				try{new CipherInputStream(is,null,key);
					Assert.fail("Mandatory exception was not detected (null 2-nd argument");
				} catch (IllegalArgumentException exc) {
				}
				try{new CipherInputStream(is,"",key);
					Assert.fail("Mandatory exception was not detected (empty 2-nd argument");
				} catch (IllegalArgumentException exc) {
				}
				try{new CipherInputStream(is,"AES",null);
					Assert.fail("Mandatory exception was not detected (null 3-rd argument");
				} catch (NullPointerException exc) {
				}
			}
		}
	}
}
