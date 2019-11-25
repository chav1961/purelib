package chav1961.purelib.enumerations;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.enumerations.StylePropertiesSupported.ContentType;
import chav1961.purelib.enumerations.StylePropertiesSupported.Keyword;

public class StylePropertiesSupportedTest {
	@Test
	public void basicTest() {
		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
			Assert.assertTrue(item.getExternalName() != null && !item.getExternalName().isEmpty());
			Assert.assertNotNull(item.getContentType());
			Assert.assertTrue(item.getMinOccurence() >= 0);
			Assert.assertTrue(item.getMinOccurence() <= item.getMaxOccurence());
			Assert.assertEquals(item,StylePropertiesSupported.forName(item.getExternalName()));
			if (item.canBeInherited()) {
				Assert.assertEquals(Keyword.INHERITED,item.forValue(StylePropertiesSupported.INHERITED_KEYWORD));
			}
			if (item.canUseKeywords()) {
				for (Keyword kwd : item.getKeywordsSupported()) {
					Assert.assertNotNull(kwd);
					Assert.assertTrue(kwd.getId() >= 0);
					Assert.assertTrue(kwd.getName() != null && !kwd.getName().isEmpty());
				}
			}
		}
	}

	@Test
	public void colorSupportTest() {
		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
			if (item.getContentType() == ContentType.color) {
				Assert.assertFalse(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("black"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(Color.BLACK,item.forValue("black"));
			}
			if (item.getContentType() == ContentType.colorOrKeyword) {
				Assert.assertTrue(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("black"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(Color.BLACK,item.forValue("black"));
				for (Keyword kwd : item.getKeywordsSupported()) {
					Assert.assertEquals(kwd,item.forValue(kwd.getName()));
				}
			}
		}
	}

	@Test
	public void distanceSupportTest() {
		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
			if (item.getContentType() == ContentType.distance) {
				Assert.assertFalse(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10mm"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(new CSSUtils.Distance(10,CSSUtils.Distance.Units.mm),item.forValue("10mm"));
			}
			if (item.getContentType() == ContentType.distanceOrKeyword) {
				Assert.assertTrue(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10mm"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(new CSSUtils.Distance(10,CSSUtils.Distance.Units.mm),item.forValue("10mm"));
				for (Keyword kwd : item.getKeywordsSupported()) {
					Assert.assertEquals(kwd,item.forValue(kwd.getName()));
				}
			}
		}
	}

	@Test
	public void integerSupportTest() {
		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
			if (item.getContentType() == ContentType.integer) {
				Assert.assertFalse(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(Long.valueOf(10),item.forValue("10"));
			}
			if (item.getContentType() == ContentType.integerOrKeyword) {
				Assert.assertTrue(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(Long.valueOf(10),item.forValue("10"));
				for (Keyword kwd : item.getKeywordsSupported()) {
					Assert.assertEquals(kwd,item.forValue(kwd.getName()));
				}
			}
		}
	}

	@Test
	public void numberSupportTest() {
		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
			if (item.getContentType() == ContentType.number) {
				Assert.assertFalse(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(Double.valueOf(10),item.forValue("10"),0.0001);
			}
			if (item.getContentType() == ContentType.numberOrKeyword) {
				Assert.assertTrue(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(Double.valueOf(10),item.forValue("10"),0.0001);
				for (Keyword kwd : item.getKeywordsSupported()) {
					Assert.assertEquals(kwd,item.forValue(kwd.getName()));
				}
			}
		}
	}

	@Test
	public void timeSupportTest() {
		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
			if (item.getContentType() == ContentType.time) {
				Assert.assertFalse(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10sec"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(new CSSUtils.Time(10,CSSUtils.Time.Units.sec),item.forValue("10sec"));
			}
			if (item.getContentType() == ContentType.timeOrKeyword) {
				Assert.assertTrue(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10sec"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(new CSSUtils.Time(10,CSSUtils.Time.Units.sec),item.forValue("10sec"));
				for (Keyword kwd : item.getKeywordsSupported()) {
					Assert.assertEquals(kwd,item.forValue(kwd.getName()));
				}
			}
		}
	}

	@Test
	public void urlSupportTest() {
		for (StylePropertiesSupported item : StylePropertiesSupported.values()) {
			if (item.getContentType() == ContentType.url) {
				Assert.assertFalse(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("10sec"));
				Assert.assertFalse(item.isValidValue("unknown"));
				Assert.assertEquals(new CSSUtils.Time(10,CSSUtils.Time.Units.sec),item.forValue("10sec"));
			}
			if (item.getContentType() == ContentType.urlOrKeyword) {
				Assert.assertTrue(item.canUseKeywords());
				Assert.assertTrue(item.isValidValue("url(\"https://mail.ru\")"));
				Assert.assertFalse(item.isValidValue("unknown"));
			}
		}
	}
}
