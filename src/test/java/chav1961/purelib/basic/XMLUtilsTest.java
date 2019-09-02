package chav1961.purelib.basic;

import java.awt.Color;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.XMLUtils.Angle;
import chav1961.purelib.basic.XMLUtils.Distance;
import chav1961.purelib.basic.XMLUtils.Frequency;
import chav1961.purelib.basic.XMLUtils.StylePropertiesStack;
import chav1961.purelib.basic.XMLUtils.Time;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class XMLUtilsTest {
	//
	//	Inner classes
	//
	
	@Test
	public void distanceClassTest() throws SyntaxException {
		final XMLUtils.Distance	dist1 = new XMLUtils.Distance(100,XMLUtils.Distance.Units.mm),
								dist2 = new XMLUtils.Distance(100,XMLUtils.Distance.Units.mm),
								dist3 = new XMLUtils.Distance(200,XMLUtils.Distance.Units.mm);
		
		Assert.assertEquals(100,dist1.getValue());
		Assert.assertEquals(XMLUtils.Distance.Units.mm,dist1.getUnit());
		
		Assert.assertEquals(dist1,dist2);
		Assert.assertEquals(dist1.hashCode(),dist2.hashCode());
		Assert.assertFalse(dist1.equals(dist3));
		
		Assert.assertEquals("100mm",dist1.toString());
		Assert.assertEquals("100mm",XMLUtils.Distance.valueOf(100,XMLUtils.Distance.Units.mm).toString());
		Assert.assertEquals("100mm",XMLUtils.Distance.valueOf("100mm").toString());
		Assert.assertEquals(dist1,XMLUtils.Distance.valueOf(dist2.toString()));
		
		Assert.assertTrue(XMLUtils.Distance.valueOf("100mm") == XMLUtils.Distance.valueOf("100mm"));
		Assert.assertFalse(XMLUtils.Distance.valueOf("200mm") == XMLUtils.Distance.valueOf("200mm"));

		try{new XMLUtils.Distance(-1,XMLUtils.Distance.Units.mm);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new XMLUtils.Distance(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{XMLUtils.Distance.valueOf(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Distance.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Distance.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{XMLUtils.Distance.valueOf(-1,XMLUtils.Distance.Units.mm);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Distance.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		
	
	@Test
	public void angleClassTest() throws SyntaxException {
		final XMLUtils.Angle	angle1 = new XMLUtils.Angle(1,XMLUtils.Angle.Units.rad),
								angle2 = new XMLUtils.Angle(1,XMLUtils.Angle.Units.rad),
								angle3 = new XMLUtils.Angle(200,XMLUtils.Angle.Units.rad);

		Assert.assertEquals(1,angle1.getValue(),0.0001f);
		Assert.assertEquals(XMLUtils.Angle.Units.rad,angle1.getUnit());
		
		Assert.assertEquals(angle1,angle2);
		Assert.assertEquals(angle1.hashCode(),angle2.hashCode());
		Assert.assertFalse(angle1.equals(angle3));
		
		Assert.assertEquals("1.0rad",angle1.toString());
		Assert.assertEquals("1.0rad",XMLUtils.Angle.valueOf(1,XMLUtils.Angle.Units.rad).toString());
		Assert.assertEquals("1.0rad",XMLUtils.Angle.valueOf("1rad").toString());
		Assert.assertEquals(angle1,XMLUtils.Angle.valueOf(angle2.toString()));
		
		Assert.assertTrue(XMLUtils.Angle.valueOf("1rad") == XMLUtils.Angle.valueOf("1rad"));
		Assert.assertFalse(XMLUtils.Angle.valueOf("200rad") == XMLUtils.Angle.valueOf("200rad"));
		
		Assert.assertEquals(angle1.getValue(),new XMLUtils.Angle(angle1.getValueAs(XMLUtils.Angle.Units.rad),XMLUtils.Angle.Units.rad).getValueAs(XMLUtils.Angle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new XMLUtils.Angle(angle1.getValueAs(XMLUtils.Angle.Units.deg),XMLUtils.Angle.Units.deg).getValueAs(XMLUtils.Angle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new XMLUtils.Angle(angle1.getValueAs(XMLUtils.Angle.Units.grad),XMLUtils.Angle.Units.grad).getValueAs(XMLUtils.Angle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new XMLUtils.Angle(angle1.getValueAs(XMLUtils.Angle.Units.turn),XMLUtils.Angle.Units.turn).getValueAs(XMLUtils.Angle.Units.rad),0.0001f);
		
		try{new XMLUtils.Angle(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{angle1.getValueAs(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{XMLUtils.Angle.valueOf(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Angle.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Angle.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{XMLUtils.Angle.valueOf(-1,XMLUtils.Angle.Units.rad);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Angle.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		
	
	@Test
	public void timeClassTest() throws SyntaxException {
		final XMLUtils.Time	time1 = new XMLUtils.Time(1,XMLUtils.Time.Units.msec),
							time2 = new XMLUtils.Time(1,XMLUtils.Time.Units.msec),
							time3 = new XMLUtils.Time(200,XMLUtils.Time.Units.msec);
		
		Assert.assertEquals(1,time1.getValue(),0.0001f);
		Assert.assertEquals(XMLUtils.Time.Units.msec,time1.getUnit());
		
		Assert.assertEquals(time1,time2);
		Assert.assertEquals(time1.hashCode(),time2.hashCode());
		Assert.assertFalse(time1.equals(time3));
		
		Assert.assertEquals("1.0msec",time1.toString());
		Assert.assertEquals("1.0msec",XMLUtils.Time.valueOf(1,XMLUtils.Time.Units.msec).toString());
		Assert.assertEquals("1.0msec",XMLUtils.Time.valueOf("1msec").toString());
		Assert.assertEquals(time1,XMLUtils.Time.valueOf(time2.toString()));
		
		Assert.assertTrue(XMLUtils.Time.valueOf("1msec") == XMLUtils.Time.valueOf("1msec"));
		Assert.assertFalse(XMLUtils.Time.valueOf("200msec") == XMLUtils.Time.valueOf("200msec"));
		
		Assert.assertEquals(time1.getValue(),new XMLUtils.Time(time1.getValueAs(XMLUtils.Time.Units.msec),XMLUtils.Time.Units.msec).getValueAs(XMLUtils.Time.Units.msec),0.0001f);
		Assert.assertEquals(time1.getValue(),new XMLUtils.Time(time1.getValueAs(XMLUtils.Time.Units.sec),XMLUtils.Time.Units.sec).getValueAs(XMLUtils.Time.Units.msec),0.0001f);
		
		try{new XMLUtils.Time(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{time1.getValueAs(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{XMLUtils.Time.valueOf(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Time.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Time.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{XMLUtils.Time.valueOf(-1,XMLUtils.Time.Units.msec);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Time.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		
	
	@Test
	public void frequencyClassTest() throws SyntaxException {
		final XMLUtils.Frequency	freq1 = new XMLUtils.Frequency(1,XMLUtils.Frequency.Units.Hz),
									freq2 = new XMLUtils.Frequency(1,XMLUtils.Frequency.Units.Hz),
									freq3 = new XMLUtils.Frequency(200,XMLUtils.Frequency.Units.Hz);
				
		Assert.assertEquals(1,freq1.getValue(),0.0001f);
		Assert.assertEquals(XMLUtils.Frequency.Units.Hz,freq1.getUnit());
		
		Assert.assertEquals(freq1,freq2);
		Assert.assertEquals(freq1.hashCode(),freq2.hashCode());
		Assert.assertFalse(freq1.equals(freq3));
		 
		Assert.assertEquals("1.0Hz",freq1.toString());
		Assert.assertEquals("1.0Hz",XMLUtils.Frequency.valueOf(1,XMLUtils.Frequency.Units.Hz).toString());
		Assert.assertEquals("1.0Hz",XMLUtils.Frequency.valueOf("1Hz").toString());
		Assert.assertEquals(freq1,XMLUtils.Frequency.valueOf(freq2.toString()));
		
		Assert.assertTrue(XMLUtils.Frequency.valueOf("1Hz") == XMLUtils.Frequency.valueOf("1Hz"));
		Assert.assertFalse(XMLUtils.Frequency.valueOf("200Hz") == XMLUtils.Frequency.valueOf("200Hz"));
		
		Assert.assertEquals(freq1.getValue(),new XMLUtils.Frequency(freq1.getValueAs(XMLUtils.Frequency.Units.Hz),XMLUtils.Frequency.Units.Hz).getValueAs(XMLUtils.Frequency.Units.Hz),0.0001f);
		Assert.assertEquals(freq1.getValue(),new XMLUtils.Frequency(freq1.getValueAs(XMLUtils.Frequency.Units.kHz),XMLUtils.Frequency.Units.kHz).getValueAs(XMLUtils.Frequency.Units.Hz),0.0001f);
		
		try{new XMLUtils.Frequency(-1,XMLUtils.Frequency.Units.Hz);
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new XMLUtils.Frequency(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{freq1.getValueAs(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{XMLUtils.Frequency.valueOf(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Frequency.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Frequency.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{XMLUtils.Frequency.valueOf(-1,XMLUtils.Frequency.Units.Hz);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Frequency.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		

	//
	//	Atomic type parsers
	//
	
	@Test
	public void parseAsColorTest() throws SyntaxException {
		Color	color = XMLUtils.asColor("#000000");
		
		Assert.assertEquals(255,color.getAlpha());
		Assert.assertEquals(0,color.getRed());
		Assert.assertEquals(0,color.getGreen());
		Assert.assertEquals(0,color.getBlue());

		color = XMLUtils.asColor("#FF0000");
		
		Assert.assertEquals(255,color.getAlpha());
		Assert.assertEquals(255,color.getRed());
		Assert.assertEquals(0,color.getGreen());
		Assert.assertEquals(0,color.getBlue());

		color = XMLUtils.asColor("#FF");
		
		Assert.assertEquals(255,color.getAlpha());
		Assert.assertEquals(0,color.getRed());
		Assert.assertEquals(0,color.getGreen());
		Assert.assertEquals(255,color.getBlue());

		color = XMLUtils.asColor("rgba(0,127,255,63)");
		
		Assert.assertEquals(63,color.getAlpha());
		Assert.assertEquals(0,color.getRed());
		Assert.assertEquals(127,color.getGreen());
		Assert.assertEquals(255,color.getBlue());
		
		color = XMLUtils.asColor("rgb(0,127,255)");
		
		Assert.assertEquals(255,color.getAlpha());
		Assert.assertEquals(0,color.getRed());
		Assert.assertEquals(127,color.getGreen());
		Assert.assertEquals(255,color.getBlue());
		
		color = XMLUtils.asColor("hsl(0,100%,100%)");
		
		Assert.assertEquals(255,color.getAlpha());
		Assert.assertEquals(255,color.getRed());
		Assert.assertEquals(0,color.getGreen());
		Assert.assertEquals(0,color.getBlue());
		
		color = XMLUtils.asColor("hsla(0,100%,100%,50%)");
		
		Assert.assertEquals(128,color.getAlpha());
		Assert.assertEquals(255,color.getRed());
		Assert.assertEquals(0,color.getGreen());
		Assert.assertEquals(0,color.getBlue());
		
		try{XMLUtils.asColor(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asColor("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{XMLUtils.asColor("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (SyntaxException exc) {
		}		
	}

	@Test
	public void parseAsDistanceTest() throws SyntaxException {
		Distance	dist = XMLUtils.asDistance("2mm");
		
		Assert.assertEquals(2,dist.getValue());
		
		try{XMLUtils.asDistance(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asDistance("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{XMLUtils.asDistance("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (SyntaxException exc) {
		}		
	}
	
	@Test
	public void parseAsAngleTest() throws SyntaxException {
		Angle	angle = XMLUtils.asAngle("1rad");
		
		Assert.assertEquals(1.0f,angle.getValue(),0.0001f);
		
		try{XMLUtils.asAngle(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asAngle("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{XMLUtils.asAngle("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (SyntaxException exc) {
		}		
	}

	@Test
	public void parseAsTimeTest() throws SyntaxException {
		Time	time = XMLUtils.asTime("1msec");
		
		Assert.assertEquals(1.0f,time.getValue(),0.0001f);
		
		try{XMLUtils.asTime(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asTime("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{XMLUtils.asTime("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (SyntaxException exc) {
		}		
	}

	@Test
	public void parseAsFrequencyTest() throws SyntaxException {
		Frequency	freq = XMLUtils.asFrequency("1Hz");
		
		Assert.assertEquals(1.0f,freq.getValue(),0.0001f);
		
		try{XMLUtils.asFrequency(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asFrequency("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{XMLUtils.asFrequency("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (SyntaxException exc) {
		}		
	}

	@Test
	public void stylePropertiesStackTest() throws SyntaxException {
		final StylePropertiesStack	stack = new StylePropertiesStack();
		final Map<String,Object>	item = new HashMap<String,Object>(); 
		
		Assert.assertEquals(0,stack.size());
		
		stack.push(item);
		Assert.assertEquals(1,stack.size());
		Assert.assertEquals(item,stack.peek());
		
		Assert.assertEquals(item,stack.pop());
		Assert.assertEquals(0,stack.size());
		
		try{stack.push(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{stack.peek();
			Assert.fail("Mandatory exception was not detected (empty stack)");
		} catch (EmptyStackException exc) {
		}
		try{stack.pop();
			Assert.fail("Mandatory exception was not detected (empty stack)");
		} catch (EmptyStackException exc) {
		}
	}
	
	//
	//	Style parsers
	//

	@Test
	public void parseCSSStyleTest() throws SyntaxException {
	}

	
	//	
	//	Selector parsers
	//

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

	//	
	//	Comples test
	//

	

}
