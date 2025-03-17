package chav1961.purelib.basic;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class CSSUtilsTest {
	//
	//	Inner classes
	//

	@Test
	public void distanceClassTest() throws SyntaxException {
		final CSSUtils.Distance	dist1 = new CSSUtils.Distance(100,CSSUtils.Distance.Units.mm),
								dist2 = new CSSUtils.Distance(100,CSSUtils.Distance.Units.mm),
								dist3 = new CSSUtils.Distance(200,CSSUtils.Distance.Units.mm);
		
		Assert.assertEquals(100,dist1.getValue());
		Assert.assertEquals(CSSUtils.Distance.Units.mm,dist1.getUnit());
		
		Assert.assertEquals(dist1,dist2);
		Assert.assertEquals(dist1.hashCode(),dist2.hashCode());
		Assert.assertFalse(dist1.equals(dist3));
		
		Assert.assertEquals("100mm",dist1.toString());
		Assert.assertEquals("100mm",CSSUtils.Distance.valueOf(100,CSSUtils.Distance.Units.mm).toString());
		Assert.assertEquals("100mm",CSSUtils.Distance.valueOf("100mm").toString());
		Assert.assertEquals(dist1,CSSUtils.Distance.valueOf(dist2.toString()));
		
		Assert.assertTrue(CSSUtils.Distance.valueOf("100mm") == CSSUtils.Distance.valueOf("100mm"));
		Assert.assertFalse(CSSUtils.Distance.valueOf("200mm") == CSSUtils.Distance.valueOf("200mm"));

		Assert.assertEquals(2.54,CSSUtils.Distance.valueOf("1in").getValueAs(CSSUtils.Distance.Units.cm),0.001);
		Assert.assertEquals(25.4,CSSUtils.Distance.valueOf("1in").getValueAs(CSSUtils.Distance.Units.mm),0.001);
		
		try{new CSSUtils.Distance(-1,CSSUtils.Distance.Units.mm);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new CSSUtils.Distance(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{CSSUtils.Distance.valueOf((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Distance.valueOf(""); 
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Distance.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}

		try{CSSUtils.Distance.valueOf((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Distance.valueOf("".toCharArray()); 
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Distance.valueOf("illegal".toCharArray());
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{CSSUtils.Distance.valueOf(-1,CSSUtils.Distance.Units.mm);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Distance.valueOf(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
	}		
	
	@Test
	public void angleClassTest() throws SyntaxException {
		final CSSUtils.Angle	angle1 = new CSSUtils.Angle(1,CSSUtils.Angle.Units.rad),
								angle2 = new CSSUtils.Angle(1,CSSUtils.Angle.Units.rad),
								angle3 = new CSSUtils.Angle(200,CSSUtils.Angle.Units.rad);

		Assert.assertEquals(1,angle1.getValue(),0.0001f);
		Assert.assertEquals(CSSUtils.Angle.Units.rad,angle1.getUnit());
		
		Assert.assertEquals(angle1,angle2);
		Assert.assertEquals(angle1.hashCode(),angle2.hashCode());
		Assert.assertFalse(angle1.equals(angle3));
		
		Assert.assertEquals("1.0rad",angle1.toString());
		Assert.assertEquals("1.0rad",CSSUtils.Angle.valueOf(1,CSSUtils.Angle.Units.rad).toString());
		Assert.assertEquals("1.0rad",CSSUtils.Angle.valueOf("1rad").toString());
		Assert.assertEquals(angle1,CSSUtils.Angle.valueOf(angle2.toString()));
		
		Assert.assertTrue(CSSUtils.Angle.valueOf("1rad") == CSSUtils.Angle.valueOf("1rad"));
		Assert.assertFalse(CSSUtils.Angle.valueOf("200rad") == CSSUtils.Angle.valueOf("200rad"));
		
		Assert.assertEquals(angle1.getValue(),new CSSUtils.Angle(angle1.getValueAs(CSSUtils.Angle.Units.rad),CSSUtils.Angle.Units.rad).getValueAs(CSSUtils.Angle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new CSSUtils.Angle(angle1.getValueAs(CSSUtils.Angle.Units.deg),CSSUtils.Angle.Units.deg).getValueAs(CSSUtils.Angle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new CSSUtils.Angle(angle1.getValueAs(CSSUtils.Angle.Units.grad),CSSUtils.Angle.Units.grad).getValueAs(CSSUtils.Angle.Units.rad),0.0001f);
		Assert.assertEquals(angle1.getValue(),new CSSUtils.Angle(angle1.getValueAs(CSSUtils.Angle.Units.turn),CSSUtils.Angle.Units.turn).getValueAs(CSSUtils.Angle.Units.rad),0.0001f);
		
		try{new CSSUtils.Angle(1,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{angle1.getValueAs(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (NullPointerException exc) {
		}
		
		try{CSSUtils.Angle.valueOf(null);
			Assert.fail("Mandatory exception was not detected (null 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Angle.valueOf("");
			Assert.fail("Mandatory exception was not detected (empty 1-st agrument)"); 
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Angle.valueOf("illegal");
			Assert.fail("Mandatory exception was not detected (1-st agrument has wrong syntax)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{CSSUtils.Angle.valueOf(-1,CSSUtils.Angle.Units.rad);
			Assert.fail("Mandatory exception was not detected (negative 1-st agrument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.Angle.valueOf(1,null);
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
	public void parseAsDistanceTest() throws SyntaxException {
		
		Assert.assertTrue(CSSUtils.isValidDistance("2mm"));
		Assert.assertTrue(CSSUtils.isValidDistance("2mm".toCharArray()));
		
		CSSUtils.Distance	dist = CSSUtils.asDistance("2mm");
		Assert.assertEquals(2,dist.getValue());
		dist = CSSUtils.asDistance("2mm".toCharArray());
		Assert.assertEquals(2,dist.getValue());
		
		Assert.assertFalse(CSSUtils.isValidDistance("illegal"));
		Assert.assertFalse(CSSUtils.isValidDistance("illegal".toCharArray()));

		try{CSSUtils.isValidDistance((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.isValidDistance("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		
		try{CSSUtils.asDistance((String)null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asDistance("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CSSUtils.asDistance("illegal");
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		

		try{CSSUtils.isValidDistance((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.isValidDistance("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		
		try{CSSUtils.asDistance((char[])null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asDistance("".toCharArray());
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
		try{CSSUtils.asDistance("illegal".toCharArray());
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}		
	}
	
	@Test
	public void parseAsTimeTest() throws SyntaxException {
		CSSUtils.Time	time = CSSUtils.asTime("1msec");
		
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
		CSSUtils.Frequency	freq = CSSUtils.asFrequency("1Hz");
		
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

	@Test
	public void parseAsTransformTest() throws SyntaxException {
		final double[]	result = new double[6];
		AffineTransform	trans = CSSUtils.asTransform("rotate("+Math.PI+")");

		trans.getMatrix(result);
		Assert.assertArrayEquals(new double[] {-1,0,0,-1,0,0},result,0.0001);
		
		trans = CSSUtils.asTransform("scale(2,2)");

		trans.getMatrix(result);
		Assert.assertArrayEquals(new double[] {2,0,0,2,0,0},result,0.0001);
		
		trans = CSSUtils.asTransform("translate(1,1)");

		trans.getMatrix(result);
		Assert.assertArrayEquals(new double[] {1,0,0,1,1,1},result,0.0001);

		trans = CSSUtils.asTransform("rotate("+Math.PI+") scale(2,2) translate(1,1)");

		trans.getMatrix(result);
		Assert.assertArrayEquals(new double[] {-2,0,0,-2,-2,-2},result,0.0001);
		
		try{CSSUtils.asTransform(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{CSSUtils.asTransform("");
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try{CSSUtils.asTransform("unknown");
			Assert.fail("Mandatory exception was not detected (unknown reserved word)");
		} catch (SyntaxException exc) {
		}

		try{CSSUtils.asTransform("rotate");
			Assert.fail("Mandatory exception was not detected (missing '(' )");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("rotate(");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("rotate(-");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("rotate(-100");
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
		}

		try{CSSUtils.asTransform("scale");
			Assert.fail("Mandatory exception was not detected (missing '(' )");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("scale(");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("scale(-");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("scale(-100");
			Assert.fail("Mandatory exception was not detected (missing ',')");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("scale(-100,");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("scale(-100,-");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("scale(-100,-100");
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
		}

		try{CSSUtils.asTransform("translate");
			Assert.fail("Mandatory exception was not detected (missing '(' )");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("translate(");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("translate(-");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("translate(-100");
			Assert.fail("Mandatory exception was not detected (missing ',')");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("translate(-100,");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("translate(-100,-");
			Assert.fail("Mandatory exception was not detected (illegal number)");
		} catch (SyntaxException exc) {
		}
		try{CSSUtils.asTransform("translate(-100,-100");
			Assert.fail("Mandatory exception was not detected (missing ')')");
		} catch (SyntaxException exc) {
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
}
