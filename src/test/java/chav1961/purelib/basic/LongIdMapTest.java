package chav1961.purelib.basic;

import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class LongIdMapTest {
	@Test
	public void test() {
		final LongIdMap<String>	map = new LongIdMap<>(String.class);
		
		Assert.assertEquals(map.put(1,"test string 1"),map);
		Assert.assertEquals(map.put(1000000,"test string 2"),map);
		
		Assert.assertTrue(map.contains(1));
		Assert.assertTrue(map.contains(1000000));
		Assert.assertFalse(map.contains(2000000));
		
		Assert.assertEquals(map.get(1),"test string 1");
		Assert.assertEquals(map.get(1000000),"test string 2");
		Assert.assertEquals(1000000,map.maxValue());
		Assert.assertEquals(0,map.firstFree());
		
		try{map.put(-1,"test string 1");
		} catch (IllegalArgumentException exc) {			
		}
		try{map.put(1,null);
		} catch (NullPointerException exc) {			
		}
	}
}
