package chav1961.purelib.basic;

import org.junit.Assert;
import org.junit.jupiter.api.Test;


class NamedValueTest {
	@Test
	void basicTest() {
		final NamedValue<String>	nv = new NamedValue<>("key", "value");
		
		Assert.assertEquals("key", nv.getName());
		Assert.assertEquals("value", nv.getValue());
		
		try{new NamedValue<>(null, "value");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new NamedValue<>("", "value");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new NamedValue<>("key", null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		try{NamedValue.of(null, "value");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{NamedValue.of("", "value");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{NamedValue.of("key", null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
