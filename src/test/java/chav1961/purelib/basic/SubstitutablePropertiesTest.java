package chav1961.purelib.basic;

import org.junit.Assert;
import org.junit.Test;

public class SubstitutablePropertiesTest {
	@Test
	public void test() {
		final SubstitutableProperties	props = new SubstitutableProperties(Utils.mkProps("key1","value1","key2","${key3}","key3","value3")), another = new SubstitutableProperties();

		Assert.assertTrue(props.containsKey("key1"));
		Assert.assertFalse(props.containsKey("unknown"));
		Assert.assertEquals("value1",props.getProperty("key1"));
		Assert.assertEquals("value3",props.getProperty("key3"));
		Assert.assertEquals("value3",props.getProperty("key3",String.class));
		Assert.assertNull(props.getProperty("unknown"));
		Assert.assertNull(props.getProperty("unknown",String.class));
		Assert.assertTrue(props.theSame(props));
		Assert.assertFalse(props.theSame(another));
		
		try{new SubstitutableProperties(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try{props.getProperty(null,String.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{props.getProperty("key1",(Class<?>)null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{props.theSame(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
