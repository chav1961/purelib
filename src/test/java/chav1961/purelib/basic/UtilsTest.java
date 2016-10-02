package chav1961.purelib.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {
	@Test
	public void copyStreamTest() throws IOException {
		try(final ByteArrayInputStream	bais = new ByteArrayInputStream("test string".getBytes());
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
		
			Assert.assertEquals(Utils.copyStream(bais,baos),"test string".length());
			Assert.assertEquals(baos.toString(),"test string");
			
			try{Utils.copyStream(null,baos);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try{Utils.copyStream(bais,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
		}

		try(final Reader		rdr = new StringReader("test string");
			final StringWriter	wr = new StringWriter()) {
		
			Assert.assertEquals(Utils.copyStream(rdr,wr),"test string".length());
			Assert.assertEquals(wr.toString(),"test string");
			
			try{Utils.copyStream(null,wr);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {				
			}
			try{Utils.copyStream(rdr,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {				
			}
		}		
	}

	@Test
	public void mkMapAndSetTest() throws IOException {
		final Map<String,Object>	etalon = new HashMap<String,Object>(){{put("key1","value1"); put("key2","value2");}};
		
		Assert.assertEquals(Utils.mkMap("key1","value1","key2","value2"),etalon);
		
		try{Utils.mkMap((Object[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {				
		}
		try{Utils.mkMap("key1");
			Assert.fail("Mandatory exception was not detected (odd amount of parameters)");
		} catch (IllegalArgumentException exc) {				
		}
		try{Utils.mkMap(null,"value1");
			Assert.fail("Mandatory exception was not detected (key is null)");
		} catch (IllegalArgumentException exc) {				
		}
		
		final Set<String>		etalonSet = new HashSet<String>(){{add("key1"); add("key2");}};

		Assert.assertEquals(Utils.mkSet(String.class,"key1","key2"),etalonSet);

		try{Utils.mkSet(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {				
		}
		try{Utils.mkSet(String.class,(String[])null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {				
		}
	}
}
