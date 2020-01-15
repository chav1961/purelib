package chav1961.purelib.nanoservice;


import java.awt.datatransfer.MimeTypeParseException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.SyntaxException;
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
		
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT}, InternalUtils.buildMime(PureLibSettings.MIME_JSON_TEXT.toString(), PureLibSettings.MIME_PLAIN_TEXT.toString())));
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT}, InternalUtils.buildMime(PureLibSettings.MIME_JSON_TEXT.toString()+','+PureLibSettings.MIME_PLAIN_TEXT.toString())));
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_JSON_TEXT, PureLibSettings.MIME_PLAIN_TEXT}, InternalUtils.buildMime(PureLibSettings.MIME_JSON_TEXT.toString()+','+PureLibSettings.MIME_PLAIN_TEXT.toString()+"; someshit")));

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
		
		try{InternalUtils.theSameMimes(null,new MimeType[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.theSameMimes(new MimeType[0],null);
			Assert.fail("Mandatory exception was not detected (null 2-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT},InternalUtils.defineMimeByExtension(".cre")));
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_CSS_TEXT},InternalUtils.defineMimeByExtension(".css")));
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_FAVICON},InternalUtils.defineMimeByExtension("favicon.ico")));
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT},InternalUtils.defineMimeByExtension("test.txt")));
		Assert.assertTrue(InternalUtils.theSameMimes(new MimeType[]{PureLibSettings.MIME_OCTET_STREAM},InternalUtils.defineMimeByExtension("abracadabra")));

		try{InternalUtils.defineMimeByExtension(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.defineMimeByExtension("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertFalse(InternalUtils.mimesIntersect(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT}, new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT}));
		Assert.assertTrue(InternalUtils.mimesIntersect(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT}, new MimeType[]{PureLibSettings.MIME_PLAIN_TEXT,PureLibSettings.MIME_CREOLE_TEXT}));
		Assert.assertTrue(InternalUtils.mimesIntersect(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT}, new MimeType[]{new MimeType("text","*")}));
		
		try{InternalUtils.mimesIntersect(null,new MimeType[0]);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.mimesIntersect(new MimeType[0],null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.mimesIntersect(new MimeType[]{null},new MimeType[0]);
			Assert.fail("Mandatory exception was not detected (nulls inside 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{InternalUtils.mimesIntersect(new MimeType[]{PureLibSettings.MIME_CREOLE_TEXT},new MimeType[]{null});
			Assert.fail("Mandatory exception was not detected (nulls inside 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void primitiveAndStringBuildersTest() throws MimeTypeParseException, IOException, SyntaxException {
		Assert.assertTrue(InternalUtils.buildBoolean("true".toCharArray()));
		Assert.assertFalse(InternalUtils.buildBoolean("false".toCharArray()));
		
		try{InternalUtils.buildBoolean(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals((byte)10,InternalUtils.buildByte("10".toCharArray()));
		
		try{InternalUtils.buildByte(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals((short)10,InternalUtils.buildShort("10".toCharArray()));
		
		try{InternalUtils.buildShort(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(10,InternalUtils.buildInt("10".toCharArray()));
		
		try{InternalUtils.buildInt(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(10L,InternalUtils.buildLong("10".toCharArray()));
		
		try{InternalUtils.buildLong(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(10.0f,InternalUtils.buildFloat("10".toCharArray()),0.001f);
		
		try{InternalUtils.buildFloat(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertEquals(10.0,InternalUtils.buildDouble("10".toCharArray()),0.001);
		
		try{InternalUtils.buildDouble(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertArrayEquals(new boolean[]{true, false},InternalUtils.buildBooleanArray("true\r\nfalse\n".toCharArray()));

		try{InternalUtils.buildBooleanArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertArrayEquals(new byte[]{(byte)10, (byte)0, (byte)-10},InternalUtils.buildByteArray(" 10\r\n 0\n-10".toCharArray()));

		try{InternalUtils.buildByteArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertArrayEquals(new short[]{(short)10, (short)0, (short)-10},InternalUtils.buildShortArray(" 10\r\n 0\n-10".toCharArray()));

		try{InternalUtils.buildShortArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertArrayEquals(new int[]{10, 0, -10},InternalUtils.buildIntArray(" 10\r\n 0\n-10".toCharArray()));

		try{InternalUtils.buildIntArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertArrayEquals(new long[]{10L, 0L, -10L},InternalUtils.buildLongArray(" 10\r\n 0\n-10".toCharArray()));

		try{InternalUtils.buildLongArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertArrayEquals(new float[]{10.0f, 0.0f, -10.0f},InternalUtils.buildFloatArray(" 10\r\n 0\n-10".toCharArray()),0.001f);

		try{InternalUtils.buildFloatArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertArrayEquals(new double[]{10.0, 0.0, -10.0},InternalUtils.buildDoubleArray(" 10\r\n 0\n-10".toCharArray()),0.001);

		try{InternalUtils.buildDoubleArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		} 

		Assert.assertArrayEquals(new String[]{"line1","line2","line3"},InternalUtils.buildStringArray(" line1\r\nline2 \nline3\r".toCharArray()));
		Assert.assertArrayEquals(new String[]{"line1","line2","line3"},InternalUtils.buildStringArray(" line1\r\nline2 \nline3\n".toCharArray()));

		try{InternalUtils.buildStringArray(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
