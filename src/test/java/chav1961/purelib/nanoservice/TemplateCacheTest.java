package chav1961.purelib.nanoservice;


import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.nanoservice.TemplateCache;

@Tag("OrdinalTestCategory")
public class TemplateCacheTest {
	@Test
	public void basicTest() throws IOException {
		try (final TemplateCache<String>	cache = new TemplateCache<>()) {
			
			Assert.assertEquals(0,cache.size());
			
			cache.add("test","s1","s2","s3");
			Assert.assertEquals(1,cache.size());
			cache.add("test","s1","s2","s3");
			Assert.assertEquals(1,cache.size());
			
			Assert.assertEquals("test",cache.remove("s1","s2","s3"));
			Assert.assertEquals(1,cache.size());
			Assert.assertEquals("test",cache.remove("s1","s2","s3"));
			Assert.assertEquals(0,cache.size());

			cache.add("test1","s1","s2","s3-1");
			Assert.assertEquals(1,cache.size());
			cache.add("test2","s1","s2","s3-2");
			Assert.assertEquals(2,cache.size());
			cache.add("test3","s1","s2","s3-3");
			Assert.assertEquals(3,cache.size());
			Assert.assertEquals("test2",cache.get("s1","s2","s3-2"));
			
			final int[]	count = new int[]{0};
			
			Assert.assertTrue(cache.walk((keys)->{count[0]++; return true;}));
			Assert.assertEquals(3,count[0]);
			count[0] = 0;
			Assert.assertFalse(cache.walk((keys)->{count[0]++; return false;}));
			Assert.assertEquals(1,count[0]);
			
			cache.clear();
			Assert.assertEquals(0,cache.size());
			
			try{cache.add(null,"s1");
				Assert.fail("Mandatory exception as not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{cache.add("test",(String[])null);
				Assert.fail("Mandatory exception as not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{cache.add("test");
				Assert.fail("Mandatory exception as not detected (empty 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{cache.add("test","s1",null);
				Assert.fail("Mandatory exception as not detected (null inside 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}
}
