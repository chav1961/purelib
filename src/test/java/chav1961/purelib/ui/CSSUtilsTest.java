package chav1961.purelib.ui;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
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
import org.xml.sax.SAXException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.ui.CSSUtils.CSSDistance;
import chav1961.purelib.ui.CSSUtils.CSSDistance.Units;
import chav1961.purelib.ui.CSSUtils.Frequency;
import chav1961.purelib.ui.CSSUtils.Time;

public class CSSUtilsTest {
	//
	//	Inner classes
	//

	@Test
	public void distanceClassTest() throws SyntaxException {
		final CSSUtils.CSSDistance	dist1 = new CSSUtils.CSSDistance(100,CSSUtils.CSSDistance.Units.mm),
								dist2 = new CSSUtils.CSSDistance(100,CSSUtils.CSSDistance.Units.mm),
								dist3 = new CSSUtils.CSSDistance(200,CSSUtils.CSSDistance.Units.mm);
		
		Assert.assertEquals(100,dist1.getValue(),0.001);
		Assert.assertEquals(CSSUtils.CSSDistance.Units.mm,dist1.getUnit());
		
		Assert.assertEquals(dist1,dist2);
		Assert.assertEquals(dist1.hashCode(),dist2.hashCode());
		Assert.assertFalse(dist1.equals(dist3));

		Assert.assertTrue(dist1.isAbsolute());
		Assert.assertFalse(dist1.isRelative());
		
		Assert.assertEquals("100mm",dist1.toString());
		Assert.assertEquals("100mm",CSSUtils.CSSDistance.valueOf(100,CSSUtils.CSSDistance.Units.mm).toString());
		Assert.assertEquals("100mm",CSSUtils.CSSDistance.valueOf("100mm").toString());
		Assert.assertEquals(dist1,CSSUtils.CSSDistance.valueOf(dist2.toString()));
		
		Assert.assertTrue(CSSUtils.CSSDistance.isValidDistance("10mm"));
		Assert.assertFalse(CSSUtils.CSSDistance.isValidDistance("illegal"));
		Assert.assertTrue(CSSUtils.CSSDistance.isValidDistance("10mm".toCharArray(),0));
		Assert.assertFalse(CSSUtils.CSSDistance.isValidDistance("illegal".toCharArray(),0));

		final CSSDistance[] result = new CSSDistance[1];
		
		Assert.assertEquals(6,CSSUtils.CSSDistance.parse(" 100mm ".toCharArray(),0,result));
		Assert.assertEquals(dist1,result[0]);

		for (Units item : Units.values()) {
			if (CSSUtils.CSSDistance.isAbsolute(item)) {
				Assert.assertEquals("Check failed for item ["+item+"]: ",dist1.getValue(),CSSUtils.CSSDistance.valueOf(dist1.getValueAs(item),item).getValueAs(dist1.getUnit()),0.001);
			}
		}
		
		try{new CSSUtils.CSSDistance(-1,CSSUtils.CSSDistance.Units.mm);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new CSSUtils.CSSDistance(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{CSSUtils.CSSDistance.valueOf((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSDistance.valueOf(""); 
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSDistance.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}

		try{CSSUtils.CSSDistance.isValidDistance((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSDistance.isValidDistance(""); 
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}

		try{CSSUtils.CSSDistance.isValidDistance((char[])null,0);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSDistance.isValidDistance("".toCharArray(),0); 
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSDistance.isValidDistance("10mm".toCharArray(),100); 
			Assert.fail("Mandatory exception was not detected (2-nd agrument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{CSSUtils.CSSDistance.valueOf(-1,CSSUtils.CSSDistance.Units.mm);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument for absolute units)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSDistance.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		
	
	@Test
	public void angleClassTest() throws SyntaxException {
		final CSSUtils.CSSAngle	angle1 = new CSSUtils.CSSAngle(1,CSSUtils.CSSAngle.Units.rad),
								angle2 = new CSSUtils.CSSAngle(1,CSSUtils.CSSAngle.Units.rad),
								angle3 = new CSSUtils.CSSAngle(200,CSSUtils.CSSAngle.Units.rad);

		Assert.assertEquals(1,angle1.getValue(),0.0001f);
		Assert.assertEquals(CSSUtils.CSSAngle.Units.rad,angle1.getUnit());
		
		Assert.assertEquals(angle1,angle2);
		Assert.assertEquals(angle1.hashCode(),angle2.hashCode()); 
		Assert.assertFalse(angle1.equals(angle3));
		
		Assert.assertEquals("1.0rad",angle1.toString());
		Assert.assertEquals("1.0rad",CSSUtils.CSSAngle.valueOf(1,CSSUtils.CSSAngle.Units.rad).toString());
		Assert.assertEquals("1.0rad",CSSUtils.CSSAngle.valueOf("1rad").toString());
		Assert.assertEquals(angle1,CSSUtils.CSSAngle.valueOf(angle2.toString()));
		
		Assert.assertEquals(angle1.getValue(),new CSSUtils.CSSAngle(angle1.getValueAs(CSSUtils.CSSAngle.Units.rad),CSSUtils.CSSAngle.Units.rad).getValueAs(CSSUtils.CSSAngle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new CSSUtils.CSSAngle(angle1.getValueAs(CSSUtils.CSSAngle.Units.deg),CSSUtils.CSSAngle.Units.deg).getValueAs(CSSUtils.CSSAngle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new CSSUtils.CSSAngle(angle1.getValueAs(CSSUtils.CSSAngle.Units.grad),CSSUtils.CSSAngle.Units.grad).getValueAs(CSSUtils.CSSAngle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new CSSUtils.CSSAngle(angle1.getValueAs(CSSUtils.CSSAngle.Units.turn),CSSUtils.CSSAngle.Units.turn).getValueAs(CSSUtils.CSSAngle.Units.rad),0.0001f);
		
		try{new CSSUtils.CSSAngle(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{angle1.getValueAs(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{CSSUtils.CSSAngle.valueOf(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSAngle.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSAngle.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{CSSUtils.CSSAngle.valueOf(-1,CSSUtils.CSSAngle.Units.rad);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.CSSAngle.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		
	
	@Test
	public void timeClassTest() throws SyntaxException {
		final CSSUtils.Time	time1 = new CSSUtils.Time(1,CSSUtils.Time.Units.msec),
							time2 = new CSSUtils.Time(1,CSSUtils.Time.Units.msec),
							time3 = new CSSUtils.Time(200,CSSUtils.Time.Units.msec);
		
		Assert.assertEquals(1,time1.getValue(),0.0001f);
		Assert.assertEquals(CSSUtils.Time.Units.msec,time1.getUnit());
		
		Assert.assertEquals(time1,time2);
		Assert.assertEquals(time1.hashCode(),time2.hashCode());
		Assert.assertFalse(time1.equals(time3));
		
		Assert.assertEquals("1.0msec",time1.toString());
		Assert.assertEquals("1.0msec",CSSUtils.Time.valueOf(1,CSSUtils.Time.Units.msec).toString());
		Assert.assertEquals("1.0msec",CSSUtils.Time.valueOf("1msec").toString());
		Assert.assertEquals(time1,CSSUtils.Time.valueOf(time2.toString()));
		
		Assert.assertTrue(CSSUtils.Time.valueOf("1msec") == CSSUtils.Time.valueOf("1msec"));
		Assert.assertFalse(CSSUtils.Time.valueOf("200msec") == CSSUtils.Time.valueOf("200msec"));
		
		Assert.assertEquals(time1.getValue(),new CSSUtils.Time(time1.getValueAs(CSSUtils.Time.Units.msec),CSSUtils.Time.Units.msec).getValueAs(CSSUtils.Time.Units.msec),0.0001f);
		Assert.assertEquals(time1.getValue(),new CSSUtils.Time(time1.getValueAs(CSSUtils.Time.Units.sec),CSSUtils.Time.Units.sec).getValueAs(CSSUtils.Time.Units.msec),0.0001f);
		
		try{new CSSUtils.Time(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{time1.getValueAs(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{CSSUtils.Time.valueOf((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Time.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Time.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}

		try{CSSUtils.Time.valueOf((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Time.valueOf(new char[0]);
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Time.valueOf("illegal".toCharArray());
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		
		try{CSSUtils.Time.valueOf(-1,CSSUtils.Time.Units.msec);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Time.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		
	
	@Test
	public void frequencyClassTest() throws SyntaxException {
		final CSSUtils.Frequency	freq1 = new CSSUtils.Frequency(1,CSSUtils.Frequency.Units.Hz),
									freq2 = new CSSUtils.Frequency(1,CSSUtils.Frequency.Units.Hz),
									freq3 = new CSSUtils.Frequency(200,CSSUtils.Frequency.Units.Hz);
				
		Assert.assertEquals(1,freq1.getValue(),0.0001f);
		Assert.assertEquals(CSSUtils.Frequency.Units.Hz,freq1.getUnit());
		
		Assert.assertEquals(freq1,freq2);
		Assert.assertEquals(freq1.hashCode(),freq2.hashCode());
		Assert.assertFalse(freq1.equals(freq3));
		 
		Assert.assertEquals("1.0Hz",freq1.toString());
		Assert.assertEquals("1.0Hz",CSSUtils.Frequency.valueOf(1,CSSUtils.Frequency.Units.Hz).toString());
		Assert.assertEquals("1.0Hz",CSSUtils.Frequency.valueOf("1Hz").toString());
		Assert.assertEquals(freq1,CSSUtils.Frequency.valueOf(freq2.toString()));
		
		Assert.assertTrue(CSSUtils.Frequency.valueOf("1Hz") == CSSUtils.Frequency.valueOf("1Hz"));
		Assert.assertFalse(CSSUtils.Frequency.valueOf("200Hz") == CSSUtils.Frequency.valueOf("200Hz"));
		
		Assert.assertEquals(freq1.getValue(),new CSSUtils.Frequency(freq1.getValueAs(CSSUtils.Frequency.Units.Hz),CSSUtils.Frequency.Units.Hz).getValueAs(CSSUtils.Frequency.Units.Hz),0.0001f);
		Assert.assertEquals(freq1.getValue(),new CSSUtils.Frequency(freq1.getValueAs(CSSUtils.Frequency.Units.kHz),CSSUtils.Frequency.Units.kHz).getValueAs(CSSUtils.Frequency.Units.Hz),0.0001f);
		
		try{new CSSUtils.Frequency(-1,CSSUtils.Frequency.Units.Hz);
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new CSSUtils.Frequency(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{freq1.getValueAs(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{CSSUtils.Frequency.valueOf(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Frequency.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Frequency.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{CSSUtils.Frequency.valueOf(-1,CSSUtils.Frequency.Units.Hz);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Frequency.valueOf(1,null);
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
		
		Assert.assertTrue(CSSUtils.isValidColor("#0f0f0f"));
		Assert.assertEquals(new Color(0x0F,0x0F,0x0F),CSSUtils.asColor("#0f0f0f"));
		
		Assert.assertTrue(CSSUtils.isValidColor("rgba(1,1,1,1)"));
		Assert.assertEquals(new Color(1,1,1,1),CSSUtils.asColor("rgba(1,1,1,1)"));
		
		Assert.assertTrue(CSSUtils.isValidColor("hsla(1,1%,1%,1%)"));
		temp = Color.getHSBColor(1/256.0f,0.01f,0.01f); 
		Assert.assertEquals(new Color(temp.getRed(),temp.getGreen(),temp.getBlue(),255),CSSUtils.asColor("hsla(1,1%,1%,100%)"));
		
		Assert.assertTrue(CSSUtils.isValidColor("rgb(1,1,1)"));
		Assert.assertEquals(new Color(1,1,1),CSSUtils.asColor("rgb(1,1,1)"));
		
		Assert.assertTrue(CSSUtils.isValidColor("hsl(1,1%,1%)"));
		temp = Color.getHSBColor(1/256.0f,0.01f,0.01f); 
		Assert.assertEquals(new Color(temp.getRed(),temp.getGreen(),temp.getBlue()),CSSUtils.asColor("hsl(1,1%,1%)"));
		
		Assert.assertTrue(CSSUtils.isValidColor("black"));
		Assert.assertEquals(Color.BLACK,CSSUtils.asColor("black"));
		
		Assert.assertFalse(CSSUtils.isValidColor("unknown"));
		try{CSSUtils.asColor("unknown");
			Assert.fail("Mandatory exception was not detected (invalid color description)");
		} catch (SyntaxException exc) {
		}

		try{CSSUtils.isValidColor((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.isValidColor("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.isValidColor((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.isValidColor("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{CSSUtils.asColor((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asColor("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asColor((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asColor("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void parseAsTimeTest() throws SyntaxException {
		Time	time = CSSUtils.asTime("1msec");
		
		Assert.assertEquals(1.0f,time.getValue(),0.0001f);
		
		try{CSSUtils.asTime((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asTime("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CSSUtils.asTime("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
	}

	@Test
	public void parseAsFrequencyTest() throws SyntaxException {
		Frequency	freq = CSSUtils.asFrequency("1Hz");
		
		Assert.assertEquals(1.0f,freq.getValue(),0.0001f);
		
		try{CSSUtils.asFrequency(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asFrequency("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CSSUtils.asFrequency("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
	}

	//	
	//	Selector parsers
	//

	@Test
	public void parseCSSTest() throws SyntaxException {
		Map<String,Properties>	result;
		
		try{CSSUtils.parseCSS(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.parseCSS("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		result = CSSUtils.parseCSS("* {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("*",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = CSSUtils.parseCSS("* {key-hyphen:value-hyphen;}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("*",item.getKey());
			Assert.assertEquals(Utils.mkProps("key-hyphen","value-hyphen"),item.getValue());
		}

		result = CSSUtils.parseCSS("tag {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("node-name(.)='tag'",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = CSSUtils.parseCSS("#id {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("@id='id'",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		
		result = CSSUtils.parseCSS(".class {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("contains(concat(' ',normalize-space(@class),' '),' class ')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		
		result = CSSUtils.parseCSS("[@attr] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("boolean(@attr)",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = CSSUtils.parseCSS("[@attr=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("@attr='10'",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = CSSUtils.parseCSS("[@attr^=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("starts-with(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = CSSUtils.parseCSS("[@attr|=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("(starts-with(@attr,'10') or @attr='10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = CSSUtils.parseCSS("[@attr*=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("contains(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = CSSUtils.parseCSS("[@attr~=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("contains(concat(' ',normalize-space(@attr),' '),' 10 ')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}
		result = CSSUtils.parseCSS("[@attr$=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("ends-with(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = CSSUtils.parseCSS("tag[@attr$=\"10\"] {key : value}");
		Assert.assertEquals(1,result.size());
		for (Entry<String, Properties> item : result.entrySet()) {
			Assert.assertEquals("node-name(.)='tag' and ends-with(@attr,'10')",item.getKey());
			Assert.assertEquals(Utils.mkProps("key","value"),item.getValue());
		}

		result = CSSUtils.parseCSS("tag1 > tag2 {key : value}");
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
		CSSUtils.walkDownXML(document.getDocumentElement(), (mode,node)->{
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
		
		try{CSSUtils.walkDownXML(null,(mode,node)->{return ContinueMode.CONTINUE;});
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{CSSUtils.walkDownXML(document.getDocumentElement(), null);
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
		
		props = CSSUtils.getAttributes(node);
		
		Assert.assertEquals("value11",props.getProperty("key11"));
		
		try{CSSUtils.getAttributes(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		
		newProps = CSSUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),false,false);
		Assert.assertEquals("new",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",CSSUtils.getAttributes(node).getProperty("key11"));
		Assert.assertFalse(CSSUtils.getAttributes(node).containsKey("key12"));

		newProps = CSSUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),true,false);
		Assert.assertEquals("value11",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",CSSUtils.getAttributes(node).getProperty("key11"));
		Assert.assertFalse(CSSUtils.getAttributes(node).containsKey("key12"));

		newProps = CSSUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),true,true);
		Assert.assertEquals("value11",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("value11",CSSUtils.getAttributes(node).getProperty("key11"));
		Assert.assertEquals("value12",CSSUtils.getAttributes(node).getProperty("key12"));

		newProps = CSSUtils.joinAttributes(node,Utils.mkProps("key11","new","key12","value12"),false,true);
		Assert.assertEquals("new",newProps.getProperty("key11"));
		Assert.assertEquals("value12",newProps.getProperty("key12"));
		Assert.assertEquals("new",CSSUtils.getAttributes(node).getProperty("key11"));
		Assert.assertEquals("value12",CSSUtils.getAttributes(node).getProperty("key12"));

		
		try{CSSUtils.joinAttributes(null,Utils.mkProps("key11","new","key12","value12"),false,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		try{CSSUtils.joinAttributes(node,null,false,true);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {			
		}		
		
		Assert.assertEquals("new",CSSUtils.getAttribute(node,"key11",String.class));
		Assert.assertEquals("value12",CSSUtils.getAttribute(node,"key12",String.class));

		try{CSSUtils.getAttribute(null,"key11",String.class); 
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}		
		try{CSSUtils.getAttribute(node,null,String.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}		
		try{CSSUtils.getAttribute(node,"",String.class);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {			
		}		
		try{CSSUtils.getAttribute(node,"key11",null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {			
		}		
	}
}
