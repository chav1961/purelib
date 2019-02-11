package chav1961.purelib.nanoservice;


import java.io.IOException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.nanoservice.InternalUtils;

public class InternalUtilsTest {
	@Test
	public void mimesTest() throws MimeTypeParseException, IOException {
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
		
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT}, InternalUtils.buildMime(PureLibSettings.MIME_JSON_TEXT.toString(), PureLibSettings.MIME_PLAIN_TEXT.toString())));
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT}, InternalUtils.buildMime(PureLibSettings.MIME_JSON_TEXT.toString()+','+PureLibSettings.MIME_PLAIN_TEXT.toString())));
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT}, InternalUtils.buildMime(PureLibSettings.MIME_JSON_TEXT.toString()+','+PureLibSettings.MIME_PLAIN_TEXT.toString()+"; someshit")));

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
		
		try{InternalUtils.theSame(null,new MimeType[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.theSame(new MimeType[0],null);
			Assert.fail("Mandatory exception was not detected (null 2-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT},InternalUtils.defineMimeByExtension(".cre")));
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_CSS_TEXT},InternalUtils.defineMimeByExtension(".css")));
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_FAVICON},InternalUtils.defineMimeByExtension("favicon.ico")));
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT},InternalUtils.defineMimeByExtension("test.txt")));
		Assert.assertTrue(InternalUtils.theSame(new MimeType[]{PureLibSettings.MIME_OCTET_STREAM},InternalUtils.defineMimeByExtension("abracadabra")));

		try{InternalUtils.defineMimeByExtension(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.defineMimeByExtension("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertFalse(InternalUtils.intersects(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT}, new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT}));
		Assert.assertTrue(InternalUtils.intersects(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT}, new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_CREOLE_TEXT}));
		Assert.assertTrue(InternalUtils.intersects(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT}, new MimeType[]{new MimeType("*","*")}));
		
		try{InternalUtils.intersects(null,new MimeType[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.intersects(new MimeType[0],null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.intersects(new MimeType[]{null},new MimeType[0]);
			Assert.fail("Mandatory exception was not detected (nulls inside 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.intersects(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT},new MimeType[]{null});
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
