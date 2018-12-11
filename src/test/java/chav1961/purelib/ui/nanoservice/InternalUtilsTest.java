package chav1961.purelib.ui.nanoservice;


import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;

public class InternalUtilsTest {
	@Test
	public void mimesTest() throws MimeTypeParseException {
		Assert.assertTrue(InternalUtils.mimesAreCompatible(PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_PLAIN_TEXT));
		Assert.assertTrue(InternalUtils.mimesAreCompatible(PureLibSettings.MIME_PLAIN_TEXT,new MimeType("text/plain")));
		
		Assert.assertFalse(InternalUtils.mimesAreCompatible(PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_JSON_TEXT));
		Assert.assertFalse(InternalUtils.mimesAreCompatible(PureLibSettings.MIME_PLAIN_TEXT,new MimeType("application/json")));
		
		try{InternalUtils.mimesAreCompatible((MimeType)null,PureLibSettings.MIME_PLAIN_TEXT);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.mimesAreCompatible(PureLibSettings.MIME_PLAIN_TEXT,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertTrue(InternalUtils.mimesAreCompatible(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT},PureLibSettings.MIME_PLAIN_TEXT));

		Assert.assertFalse(InternalUtils.mimesAreCompatible(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_CREOLE_TEXT},PureLibSettings.MIME_PLAIN_TEXT));

		try{InternalUtils.mimesAreCompatible((MimeType[])null,PureLibSettings.MIME_PLAIN_TEXT);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.mimesAreCompatible(new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT},null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertArrayEquals(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT}, InternalUtils.buildMime(PureLibSettings.MIME_JSON_TEXT.toString(), PureLibSettings.MIME_PLAIN_TEXT.toString()));

		try{InternalUtils.buildMime((String[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.buildMime((String)null);
			Assert.fail("Mandatory exception was not detected (nulls inside 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.buildMime("");
			Assert.fail("Mandatory exception was not detected (empties inside 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
}
