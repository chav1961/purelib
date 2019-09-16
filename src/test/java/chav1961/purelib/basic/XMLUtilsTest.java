package chav1961.purelib.basic;


import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.XMLUtils.Angle;
import chav1961.purelib.basic.XMLUtils.Distance;
import chav1961.purelib.basic.XMLUtils.Frequency;
import chav1961.purelib.basic.XMLUtils.StylePropValue;
import chav1961.purelib.basic.XMLUtils.StylePropertiesStack;
import chav1961.purelib.basic.XMLUtils.Time;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

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
		
		try{XMLUtils.Distance.valueOf((String)null);
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

		try{XMLUtils.Distance.valueOf((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Distance.valueOf("".toCharArray()); 
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.Distance.valueOf("illegal".toCharArray());
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
		Color 	temp;
		
		Assert.assertTrue(XMLUtils.isValidColor("#0f0f0f"));
		Assert.assertEquals(new Color(0x0F,0x0F,0x0F),XMLUtils.asColor("#0f0f0f"));
		
		Assert.assertTrue(XMLUtils.isValidColor("rgba(1,1,1,1)"));
		Assert.assertEquals(new Color(1,1,1,1),XMLUtils.asColor("rgba(1,1,1,1)"));
		
		Assert.assertTrue(XMLUtils.isValidColor("hsla(1,1%,1%,1%)"));
		temp = Color.getHSBColor(1/256.0f,0.01f,0.01f); 
		Assert.assertEquals(new Color(temp.getRed(),temp.getGreen(),temp.getBlue(),255),XMLUtils.asColor("hsla(1,1%,1%,100%)"));
		
		Assert.assertTrue(XMLUtils.isValidColor("rgb(1,1,1)"));
		Assert.assertEquals(new Color(1,1,1),XMLUtils.asColor("rgb(1,1,1)"));
		
		Assert.assertTrue(XMLUtils.isValidColor("hsl(1,1%,1%)"));
		temp = Color.getHSBColor(1/256.0f,0.01f,0.01f); 
		Assert.assertEquals(new Color(temp.getRed(),temp.getGreen(),temp.getBlue()),XMLUtils.asColor("hsl(1,1%,1%)"));
		
		Assert.assertTrue(XMLUtils.isValidColor("black"));
		Assert.assertEquals(Color.BLACK,XMLUtils.asColor("black"));
		
		Assert.assertFalse(XMLUtils.isValidColor("unknown"));
		try{XMLUtils.asColor("unknown");
			Assert.fail("Mandatory exception was not detected (invalid color description)");
		} catch (SyntaxException exc) {
		}

		try{XMLUtils.isValidColor((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.isValidColor("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.isValidColor((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.isValidColor("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{XMLUtils.asColor((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asColor("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asColor((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asColor("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void parseAsDistanceTest() throws SyntaxException {
		
		Assert.assertTrue(XMLUtils.isValidDistance("2mm"));
		Assert.assertTrue(XMLUtils.isValidDistance("2mm".toCharArray()));
		
		Distance	dist = XMLUtils.asDistance("2mm");
		Assert.assertEquals(2,dist.getValue());
		dist = XMLUtils.asDistance("2mm".toCharArray());
		Assert.assertEquals(2,dist.getValue());
		
		Assert.assertFalse(XMLUtils.isValidDistance("illegal"));
		Assert.assertFalse(XMLUtils.isValidDistance("illegal".toCharArray()));

		try{XMLUtils.isValidDistance((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.isValidDistance("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		
		try{XMLUtils.asDistance((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asDistance("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{XMLUtils.asDistance("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		

		try{XMLUtils.isValidDistance((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.isValidDistance("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		
		try{XMLUtils.asDistance((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.asDistance("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{XMLUtils.asDistance("illegal".toCharArray());
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
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
		} catch (IllegalArgumentException exc) {
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
		} catch (IllegalArgumentException exc) {
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
		} catch (IllegalArgumentException exc) {
		}		
	}

	//
	//	Style properties parsers
	//
	
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
	//	Styles parsers
	//

	@Test
	public void parseCSSStyleTest() throws SyntaxException {
		try{XMLUtils.parseStyle(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{XMLUtils.parseStyle("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		Map<String,StylePropValue<Object>>		result;
		
		result = XMLUtils.parseStyle("key:value;");
		Assert.assertEquals(1,result.size());
		Assert.assertEquals("value",result.get("key").getValue());

		result = XMLUtils.parseStyle("color:red;");
		Assert.assertEquals(1,result.size());
		Assert.assertEquals(Color.RED,result.get("color").getValue());

		result = XMLUtils.parseStyle("background-position:20px;");
		Assert.assertEquals(1,result.size());
		Assert.assertEquals(Distance.valueOf(20,Distance.Units.px),result.get("background-position").getValue());
		result = XMLUtils.parseStyle("background-position:inherited;");
		Assert.assertEquals(1,result.size());
		Assert.assertEquals(Distance.valueOf(20,Distance.Units.px),result.get("background-position").getValue());

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
	//	Complex test
	//

	
	//	
	//	Walking and attributes test
	//
	@Test
	public void walkingXMLTest() throws SyntaxException, ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory 	factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder 			builder = factory.newDocumentBuilder();
		final Document 					document = builder.parse(this.getClass().getResourceAsStream("walkingXML.xml"));
		final Set<String>				content = new HashSet<>(), toCompare = new HashSet<>();
		
		document.getDocumentElement().normalize();
		XMLUtils.walkDownXML(document.getDocumentElement(), (mode,node)->{
			if (mode == NodeEnterMode.ENTER) {
				content.add(node.getTagName());
				if (node.getChildNodes().getLength() == 1) {
					content.add(node.getTextContent());
				}
			}
			return ContinueMode.CONTINUE;
		});
		toCompare.addAll(Arrays.asList("content21","content11","content22","content12","root","level21","level1","level11","level12","level2"));
		Assert.assertEquals(toCompare,content);
		
		try{XMLUtils.walkDownXML(null,(mode,node)->{return ContinueMode.CONTINUE;});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{XMLUtils.walkDownXML(document.getDocumentElement(), null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}	

	@Test
	public void attributesXMLTest() throws SyntaxException, ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory 	factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder 			builder = factory.newDocumentBuilder();
		final Document 					document = builder.parse(this.getClass().getResourceAsStream("walkingXML.xml"));
		Properties						props, newProps; 
		
		document.normalize();

		final Element 					node = document.getElementById("id11");	// See walkingXML.dtd
		
		props = XMLUtils.getAttributes(node);
		
		Assert.assertEquals("value11",props.getProperty("key11"));
		
		try{XMLUtils.getAttributes(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		
		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),false,false);
		Assert.assertEquals("new",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertFalse(XMLUtils.getAttributes(node).containsKey("key12"));

		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),true,false);
		Assert.assertEquals("value11",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertFalse(XMLUtils.getAttributes(node).containsKey("key12"));

		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),true,true);
		Assert.assertEquals("value11",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertEquals("value12",XMLUtils.getAttributes(node).getProperty("key12"));

		newProps = XMLUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),false,true);
		Assert.assertEquals("new",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("new",XMLUtils.getAttributes(node).getProperty("key11"));
		Assert.assertEquals("value12",XMLUtils.getAttributes(node).getProperty("key12"));

		
		try{XMLUtils.joinAttributes(null,Utils.mkProps("key11","new","key12","value12"),false,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		try{XMLUtils.joinAttributes(node,null,false,true);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}		
		
		Assert.assertEquals("new",XMLUtils.getAttribute(node,"key11",String.class));
		Assert.assertEquals("value12",XMLUtils.getAttribute(node,"key12",String.class));

		try{XMLUtils.getAttribute(null,"key11",String.class); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		try{XMLUtils.getAttribute(node,null,String.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}		
		try{XMLUtils.getAttribute(node,"",String.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}		
		try{XMLUtils.getAttribute(node,"key11",null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {			
		}		
	}
}
