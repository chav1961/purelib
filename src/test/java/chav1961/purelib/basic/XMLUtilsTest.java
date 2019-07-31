package chav1961.purelib.basic;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;

public class XMLUtilsTest {
	@Test
	public void parseCSSTest() throws SyntaxException {
		Map<String,Properties>	result;
		
		try{XMLUtils.parseCSS(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.parseCSS("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		result = XMLUtils.parseCSS("* {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("*",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = XMLUtils.parseCSS("* {key-hyphen:value-hyphen;}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("*",item.getKey());
			Assert.assertEquals(Utils.mkProps("key-hyphen","value-hyphen"),item.getValue());
		}

		result = XMLUtils.parseCSS("tag {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("node-name(.)='tag'",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = XMLUtils.parseCSS("#id {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("@id='id'",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		
		result = XMLUtils.parseCSS(".class {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("contains(concat(' ',normalize-space(@class),' '),' class ')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		
		result = XMLUtils.parseCSS("[@attr] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("boolean(@attr)",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = XMLUtils.parseCSS("[@attr=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("@attr='10'",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = XMLUtils.parseCSS("[@attr^=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("starts-with(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = XMLUtils.parseCSS("[@attr|=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("(starts-with(@attr,'10') or @attr='10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = XMLUtils.parseCSS("[@attr*=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("contains(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = XMLUtils.parseCSS("[@attr~=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("contains(concat(' ',normalize-space(@attr),' '),' 10 ')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = XMLUtils.parseCSS("[@attr$=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("ends-with(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = XMLUtils.parseCSS("tag[@attr$=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("node-name(.)='tag' and ends-with(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = XMLUtils.parseCSS("tag1 > tag2 {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("(node-name(.)='tag1') and ./child[(node-name(.)='tag2')]",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}		
	}

	@Test
	public void parseCSSStyleTest() throws SyntaxException {
		Map<String,Object>	result;
	}

}
