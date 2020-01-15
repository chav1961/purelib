package chav1961.purelib.basic;

import java.awt.datatransfer.MimeTypeParseException;

import org.junit.Assert;
import org.junit.Test;

public class MimeTypeTest {
	@Test
	public void basicTest() throws IllegalArgumentException, MimeTypeParseException {
		final MimeType	mt1 = new MimeType();
		
		Assert.assertEquals("application",mt1.getPrimaryType());
		Assert.assertEquals("*",mt1.getSubType());
		Assert.assertNull(mt1.getAttr());
		
		final MimeType	mt2 = new MimeType("application","*");
		
		Assert.assertEquals("application",mt2.getPrimaryType());
		Assert.assertEquals("*",mt2.getSubType());
		Assert.assertNull(mt2.getAttr());
		 
		final MimeType	mt3 = new MimeType("x-application","macumba");
		
		Assert.assertEquals("x-application",mt3.getPrimaryType());
		Assert.assertEquals("macumba",mt3.getSubType());
		Assert.assertNull(mt3.getAttr());
		
		try{new MimeType(null,"zzz");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{new MimeType("","zzz");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{new MimeType("application",null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{new MimeType("application","");
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}
		try{new MimeType("unknown","macumba");
			Assert.fail("Mandatory exception was not detected (invalid primary type)");
		} catch (MimeTypeParseException exc) {			
		}
	}

	@Test
	public void parsingTest() throws IllegalArgumentException, MimeTypeParseException {
		final MimeType	mt1 = new MimeType(" application / test ; key1 = value1; key2 = value2");
		
		Assert.assertEquals("application",mt1.getPrimaryType());
		Assert.assertEquals("test",mt1.getSubType());
		Assert.assertEquals(Utils.mkProps("key1","value1","key2","value2"),mt1.getAttr());

		final MimeType	mt2 = new MimeType(" application / * ; key1 = value1 ; key2 = value2;");
		
		Assert.assertEquals("application",mt2.getPrimaryType());
		Assert.assertEquals("*",mt2.getSubType());
		Assert.assertEquals(Utils.mkProps("key1","value1","key2","value2"),mt2.getAttr());

		final MimeType	mt3 = new MimeType(" application / * ;");
		
		Assert.assertEquals("application",mt3.getPrimaryType());
		Assert.assertEquals("*",mt3.getSubType());
		Assert.assertNull(mt3.getAttr());
		
		try{new MimeType(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new MimeType("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new MimeType("/");
			Assert.fail("Mandatory exception was not detected (primary type is missing)");
		} catch (MimeTypeParseException exc) {
		}
		try{new MimeType("application");
			Assert.fail("Mandatory exception was not detected ('/' is missing)");
		} catch (MimeTypeParseException exc) {
		}
		try{new MimeType("application/");
			Assert.fail("Mandatory exception was not detected (subtype is missing)");
		} catch (MimeTypeParseException exc) {
		}
		try{new MimeType("unknown/test");
			Assert.fail("Mandatory exception was not detected (unknown primary type)");
		} catch (MimeTypeParseException exc) {
		}
		try{new MimeType("application/text; 1");
			Assert.fail("Mandatory exception was not detected (attribute name is missing)");
		} catch (MimeTypeParseException exc) {
		}
		try{new MimeType("application/text; x");
			Assert.fail("Mandatory exception was not detected ('=' is missing)");
		} catch (MimeTypeParseException exc) {
		}
	}
}
