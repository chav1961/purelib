package chav1961.purelib.basic.xsd;

import org.junit.Assert;
import org.junit.Test;

public class XSDConstTest {
	@Test
	public void basicTest() {
		Assert.assertNotNull(XSDConst.SCHEMA_LANGUAGE);
		Assert.assertNotNull(XSDConst.SCHEMA_SOURCE);
	}
}
