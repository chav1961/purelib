package chav1961.purelib.json;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.json.JsonSaxDeserializerFactory;
import chav1961.purelib.streams.JsonPrimitives;
import chav1961.purelib.streams.JsonSaxParser;
import chav1961.purelib.streams.interfaces.JsonSaxDeserializer;
import chav1961.purelib.testing.OrdinalTestCategory;


@Category(OrdinalTestCategory.class)
public class JsonSaxHandlerFactoryTest {
	 
	@Test
	public void primitivesAndSingleValuesTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<JsonPrimitives>	d = JsonSaxDeserializerFactory.buildDeserializer(JsonPrimitives.class,false);
		final JsonSaxParser							p = new JsonSaxParser(d);
		
		p.parse("{\"x\":10,\"y\":20,\"z\":30.5,\"t\":40.5,\"a\":true,\"b\":100,\"c\":100,\"d\":48}".toCharArray());
		Assert.assertEquals(d.getInstance().x,10);
		Assert.assertEquals(d.getInstance().y,20);
		Assert.assertEquals(d.getInstance().z,30.5,0.0001);
		Assert.assertEquals(d.getInstance().t,40.5,0.0001);
		Assert.assertTrue(d.getInstance().a);
		Assert.assertEquals(d.getInstance().b,(byte)100);
		Assert.assertEquals(d.getInstance().c,(short)100);
		Assert.assertEquals(d.getInstance().d,'0');

		try{p.parse("20".toCharArray());
			Assert.fail("Mandatory exception was not detected (single value is not supported for class deserialization)");
		} catch (SyntaxException exc) {
		}
		
		try{p.parse("{\"unknown\":10}".toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown field 'unknown')");
		} catch (SyntaxException exc) {
		}
		
		try{p.parse("{\"x\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'x' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"y\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'y' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"z\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'z' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"t\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 't' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"a\":10}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'a' and '10')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"b\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'b' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"c\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'c' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"d\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'd' and 'true')");
		} catch (SyntaxException exc) {
		}
	}

	@Test
	public void compiledPrimitivesAndSingleValuesTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<JsonPrimitives>	d = JsonSaxDeserializerFactory.buildDeserializer(JsonPrimitives.class,true);
		final JsonSaxParser							p = new JsonSaxParser(d);
		
		p.parse("{\"x\":10,\"y\":20,\"z\":30.5,\"t\":40.5,\"a\":true,\"b\":100,\"c\":100,\"d\":48}".toCharArray());
		Assert.assertEquals(d.getInstance().x,10);
		Assert.assertEquals(d.getInstance().y,20);
		Assert.assertEquals(d.getInstance().z,30.5,0.0001);
		Assert.assertEquals(d.getInstance().t,40.5,0.0001);
		Assert.assertTrue(d.getInstance().a);
		Assert.assertEquals(d.getInstance().b,(byte)100);
		Assert.assertEquals(d.getInstance().c,(short)100);
		Assert.assertEquals(d.getInstance().d,'0');

		try{p.parse("20".toCharArray());
			Assert.fail("Mandatory exception was not detected (single value is not supported for class deserialization)");
		} catch (SyntaxException exc) {
		}
		
		try{p.parse("{\"unknown\":10}".toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown field 'unknown')");
		} catch (SyntaxException exc) {
		}
		
		try{p.parse("{\"x\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'x' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"y\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'y' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"z\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'z' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"t\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 't' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"a\":10}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'a' and '10')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"b\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'b' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"c\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'c' and 'true')");
		} catch (SyntaxException exc) {
		}
		try{p.parse("{\"d\":true}".toCharArray());
			Assert.fail("Mandatory exception was not detected (uncompatible types of 'd' and 'true')");
		} catch (SyntaxException exc) {
		}
	}
	
	
	@Test
	public void stringsAndReusableTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<JsonStrings>	d = JsonSaxDeserializerFactory.buildDeserializer(JsonStrings.class,false);
		final JsonSaxParser						p = new JsonSaxParser(d);
		
		p.parse("{\"x\":\"test string 1\",\"y\":\"test string 2\"}".toCharArray());
		
		final JsonStrings	result = d.getInstance();
		
		Assert.assertEquals(result.x,"test string 1");
		Assert.assertEquals(result.y,"test string 2");
		
		p.parse("{\"x\":\"test string 3\",\"y\":null}".toCharArray());

		Assert.assertEquals(d.getInstance(),result);
		Assert.assertEquals(result.x,"test string 3");
		Assert.assertNull(result.y);
	}

	@Test
	public void primitiveArraysTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<JsonPrimitiveArrays>	d = JsonSaxDeserializerFactory.buildDeserializer(JsonPrimitiveArrays.class,false);
		final JsonSaxParser								p = new JsonSaxParser(d);
		
		p.parse("{\"a\":[1,2],\"b\":[3,4],\"c\":[],\"d\":[5,6],\"e\":[7,8,9],\"f\":[10],\"g\":[true,false],\"h\":[48]}".toCharArray());
		
		Assert.assertArrayEquals(d.getInstance().a,new byte[]{1,2});
		Assert.assertArrayEquals(d.getInstance().b,new short[]{3,4});
		Assert.assertArrayEquals(d.getInstance().c,new int[0]);
		Assert.assertArrayEquals(d.getInstance().d,new long[]{5,6});
		Assert.assertArrayEquals(d.getInstance().e,new float[]{7,8,9},0.001f);
		Assert.assertArrayEquals(d.getInstance().f,new double[]{10},0.001);
		Assert.assertTrue(d.getInstance().g[0]);
		Assert.assertFalse(d.getInstance().g[1]);
		Assert.assertArrayEquals(d.getInstance().h,new char[]{'0'});
	}

	@Test
	public void innerClassesTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<JsonOuterClass>	d = JsonSaxDeserializerFactory.buildDeserializer(JsonOuterClass.class,false);
		final JsonSaxParser							p = new JsonSaxParser(d);
		
		p.parse("{\"a\":10,\"b\":\"test string 1\",\"in\":{\"x\":20,\"y\":\"test string 2\"}}".toCharArray());
		
		Assert.assertEquals(d.getInstance().a,10);
		Assert.assertEquals(d.getInstance().b,"test string 1");
		Assert.assertEquals(d.getInstance().in.x,20);
		Assert.assertEquals(d.getInstance().in.y,"test string 2");		
	}

	@Test
	public void innerClassArraysTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<JsonOuterArrayClass>	d = JsonSaxDeserializerFactory.buildDeserializer(JsonOuterArrayClass.class,false);
		final JsonSaxParser								p = new JsonSaxParser(d);
		
		p.parse("{\"a\":10,\"in1\":[{\"x\":20,\"y\":\"test string 2\"},{\"x\":30,\"y\":\"test string 3\"}],\"in2\":[],\"b\":\"test string 1\"}".toCharArray());
		
		Assert.assertEquals(d.getInstance().a,10);
		Assert.assertEquals(d.getInstance().b,"test string 1");
		Assert.assertEquals(d.getInstance().in1.length,2);
		Assert.assertEquals(d.getInstance().in1[0].x,20);		
		Assert.assertEquals(d.getInstance().in1[0].y,"test string 2");		
		Assert.assertEquals(d.getInstance().in1[1].x,30);		
		Assert.assertEquals(d.getInstance().in1[1].y,"test string 3");		
		Assert.assertEquals(d.getInstance().in2.length,0);
	}

	@Test
	public void topPrimitiveArraysTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<byte[]>	dByte = JsonSaxDeserializerFactory.buildDeserializer(byte[].class,false);
		final JsonSaxParser					pByte = new JsonSaxParser(dByte);
		
		pByte.parse("[10,20,30]".toCharArray());
		
		Assert.assertEquals(dByte.getInstance().length,3);
		Assert.assertArrayEquals(dByte.getInstance(),new byte[]{10,20,30});
		
		final JsonSaxDeserializer<short[]>	dShort = JsonSaxDeserializerFactory.buildDeserializer(short[].class,false);
		final JsonSaxParser					pShort = new JsonSaxParser(dShort);
		
		pShort.parse("[10,20,30]".toCharArray());
		
		Assert.assertEquals(dShort.getInstance().length,3);
		Assert.assertArrayEquals(dShort.getInstance(),new short[]{10,20,30});

		final JsonSaxDeserializer<char[]>	dChar = JsonSaxDeserializerFactory.buildDeserializer(char[].class,false);
		final JsonSaxParser					pChar = new JsonSaxParser(dChar);
		
		pChar.parse("[10,20,30]".toCharArray());
		
		Assert.assertEquals(dChar.getInstance().length,3);
		Assert.assertArrayEquals(dChar.getInstance(),new char[]{10,20,30});
		
		final JsonSaxDeserializer<int[]>	d = JsonSaxDeserializerFactory.buildDeserializer(int[].class,false);
		final JsonSaxParser					p = new JsonSaxParser(d);
		
		p.parse("[10,20,30]".toCharArray());
		
		Assert.assertEquals(d.getInstance().length,3);
		Assert.assertArrayEquals(d.getInstance(),new int[]{10,20,30});

		final JsonSaxDeserializer<long[]>	dLong = JsonSaxDeserializerFactory.buildDeserializer(long[].class,false);
		final JsonSaxParser					pLong = new JsonSaxParser(dLong);
		
		pLong.parse("[10,20,30]".toCharArray());
		
		Assert.assertEquals(dLong.getInstance().length,3);
		Assert.assertArrayEquals(dLong.getInstance(),new long[]{10,20,30});

		final JsonSaxDeserializer<float[]>	dFloat = JsonSaxDeserializerFactory.buildDeserializer(float[].class,false);
		final JsonSaxParser					pFloat = new JsonSaxParser(dFloat);
		
		pFloat.parse("[10,20,30]".toCharArray());
		
		Assert.assertEquals(dFloat.getInstance().length,3);
		Assert.assertArrayEquals(dFloat.getInstance(),new float[]{10,20,30},0.0001f);

		final JsonSaxDeserializer<double[]>	dDouble = JsonSaxDeserializerFactory.buildDeserializer(double[].class,false);
		final JsonSaxParser					pDouble = new JsonSaxParser(dDouble);
		
		pDouble.parse("[10,20,30]".toCharArray());
		
		Assert.assertEquals(dDouble.getInstance().length,3);
		Assert.assertArrayEquals(dDouble.getInstance(),new double[]{10,20,30},0.0001);

		final JsonSaxDeserializer<boolean[]>	dBooelan = JsonSaxDeserializerFactory.buildDeserializer(boolean[].class,false);
		final JsonSaxParser						pBooelan = new JsonSaxParser(dBooelan);
		
		pBooelan.parse("[false,true,false]".toCharArray());
		
		Assert.assertEquals(dBooelan.getInstance().length,3);
		Assert.assertFalse(dBooelan.getInstance()[0]);
		Assert.assertTrue(dBooelan.getInstance()[1]);
		Assert.assertFalse(dBooelan.getInstance()[2]);
	}

	@Test
	public void theSameNamesTest() throws IOException, SyntaxException, ContentException {
		final JsonSaxDeserializer<JsonOuterTheSameClass>	d = JsonSaxDeserializerFactory.buildDeserializer(JsonOuterTheSameClass.class,false);
		final JsonSaxParser									p = new JsonSaxParser(d);
		
		p.parse("{\"x\":10,\"y\":\"test string 1\",\"in\":{\"x\":20,\"y\":\"test string 2\"}}".toCharArray());
		
		Assert.assertEquals(d.getInstance().x,10);
		Assert.assertEquals(d.getInstance().y,"test string 1");
		Assert.assertEquals(d.getInstance().in.x,20);
		Assert.assertEquals(d.getInstance().in.y,"test string 2");
	}
}
