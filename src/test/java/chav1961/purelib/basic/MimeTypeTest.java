package chav1961.purelib.basic;

import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.MimeParseException;

@Tag("OrdinalTestCategory")
public class MimeTypeTest {
	@Test
	public void basicTest() throws IllegalArgumentException, MimeParseException {
		final MimeType	mt1 = new MimeType();
		
		Assert.assertEquals("application",mt1.getPrimaryType());
		Assert.assertEquals("*",mt1.getSubType());
		Assert.assertTrue(mt1.getAttr().isEmpty());
		
		final MimeType	mt2 = new MimeType("application","*");
		
		Assert.assertEquals("application",mt2.getPrimaryType());
		Assert.assertEquals("*",mt2.getSubType());
		Assert.assertTrue(mt2.getAttr().isEmpty());
		 
		final MimeType	mt3 = new MimeType("x-application","macumba");
		
		Assert.assertEquals("x-application",mt3.getPrimaryType());
		Assert.assertEquals("macumba",mt3.getSubType());
		Assert.assertTrue(mt3.getAttr().isEmpty());
		
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
		} catch (MimeParseException exc) {			
		}
	}

	@Test
	public void parsingTest() throws IllegalArgumentException, MimeParseException {
		final MimeType[]	mt1 = MimeType.parseMimeList(" application / test ; key1 = value1; key2 = value2");
		
		Assert.assertEquals("application",mt1[0].getPrimaryType());
		Assert.assertEquals("test",mt1[0].getSubType());
		Assert.assertEquals(Utils.mkProps("key1","value1","key2","value2"),mt1[0].getAttr());

		final MimeType[]	mt2 = MimeType.parseMimeList(" application / * ; key1 = value1 ; key2 = value2;");
		
		Assert.assertEquals("application",mt2[0].getPrimaryType());
		Assert.assertEquals("*",mt2[0].getSubType());
		Assert.assertEquals(Utils.mkProps("key1","value1","key2","value2"),mt2[0].getAttr());

		final MimeType[]	mt3 = MimeType.parseMimeList(" application / * ;");
		
		Assert.assertEquals("application",mt3[0].getPrimaryType());
		Assert.assertEquals("*",mt3[0].getSubType());
		Assert.assertTrue(mt3[0].getAttr().isEmpty());

		final MimeType[]	mt4 = MimeType.parseMimeList(" text / * , application / * ; key1 = \"value1\"; key2 = \"value2\" ");
		
		Assert.assertEquals("text",mt4[0].getPrimaryType());
		Assert.assertEquals("*",mt4[0].getSubType());
		Assert.assertEquals("application",mt4[1].getPrimaryType());
		Assert.assertEquals("*",mt4[1].getSubType());
		Assert.assertEquals(Utils.mkProps("key1","value1","key2","value2"),mt4[0].getAttr());
		
		final MimeType[]	mt5 = MimeType.parseMimeList("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		
		Assert.assertEquals("text",mt5[0].getPrimaryType());
		Assert.assertEquals("html",mt5[0].getSubType());
		Assert.assertEquals(Utils.mkProps("q","0.9"),mt5[0].getAttr());
		Assert.assertEquals("*",mt5[4].getPrimaryType());
		Assert.assertEquals("*",mt5[4].getSubType());
		Assert.assertEquals(Utils.mkProps("q","0.8"),mt5[4].getAttr());
		
		try{MimeType.parseMimeList(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{MimeType.parseMimeList("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{MimeType.parseMimeList("/");
			Assert.fail("Mandatory exception was not detected (primary type is missing)");
		} catch (MimeParseException exc) {
		}
		try{MimeType.parseMimeList("application");
			Assert.fail("Mandatory exception was not detected ('/' is missing)");
		} catch (MimeParseException exc) {
		}
		try{MimeType.parseMimeList("application/");
			Assert.fail("Mandatory exception was not detected (subtype is missing)");
		} catch (MimeParseException exc) {
		}
		try{MimeType.parseMimeList("unknown/test");
			Assert.fail("Mandatory exception was not detected (unknown primary type)");
		} catch (MimeParseException exc) {
		}
		try{MimeType.parseMimeList("application/text; 1");
			Assert.fail("Mandatory exception was not detected (attribute name is missing)");
		} catch (MimeParseException exc) {
		}
		try{MimeType.parseMimeList("application/text; x");
			Assert.fail("Mandatory exception was not detected ('=' is missing)");
		} catch (MimeParseException exc) {
		}
	}
}
