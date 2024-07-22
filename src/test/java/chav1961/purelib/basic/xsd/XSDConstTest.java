package chav1961.purelib.basic.xsd;

import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class XSDConstTest {
	@Test
	public void staticTest() {
		Assert.assertNotNull(XSDConst.SCHEMA_LANGUAGE);
		Assert.assertNotNull(XSDConst.SCHEMA_SOURCE);
		
		Assert.assertNotNull(XSDConst.getResource("CreoleXML.xsd"));
		Assert.assertNotNull(XSDConst.getResourceAsStream("CreoleXML.xsd"));
		
		try{XSDConst.getResource(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XSDConst.getResource("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XSDConst.getResource("Unknown.xsd");
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{XSDConst.getResourceAsStream(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XSDConst.getResourceAsStream("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XSDConst.getResourceAsStream("Unknown.xsd");
			Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
