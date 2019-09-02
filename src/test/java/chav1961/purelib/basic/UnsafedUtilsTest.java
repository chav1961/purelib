package chav1961.purelib.basic;

import org.junit.Assert;
import org.junit.Test;

public class UnsafedUtilsTest {

	@Test
	public void getStringContentTest() {
		final char[]	content = UnsafedUtils.getStringContent("test");
		
		Assert.assertArrayEquals("test".toCharArray(),content);
		
		try {UnsafedUtils.getStringContent(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
