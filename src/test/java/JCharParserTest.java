import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonSaxParser;
import chav1961.purelib.streams.interfaces.JsonSaxHandler;

public class JCharParserTest {
	int			level = 0, value, maxIndex, count;
	boolean		awaited, called, startDoc, endDoc, startArray, endArray, startObj, endObj;

	@Test
	public void valuesTest() throws IOException, SyntaxException {
		final JsonSaxHandler	jch	= new JsonSaxHandler(){
										@Override public void startDoc() {level++; startDoc = true;}
										@Override public void endDoc() {level--; endDoc = true;}
										@Override public void startObj() {}
										@Override public void endObj() {}
										@Override public void startArr() {}
										@Override public void endArr() {}
										@Override public void startName(char[] data, int from, int len) {}
										@Override public void startName(String name) {}
										@Override public void startName(long id) {}
										@Override public void endName() {}
										@Override public void startIndex(int index) {}
										@Override public void endIndex() {}
										@Override public void value(char[] data, int from, int len) {Assert.assertEquals(new String(data,from,len),"TEST");}
										@Override public void value(String data) {Assert.assertEquals(data,"TEST\n");}
										@Override public void value(int data) {Assert.assertEquals(data,value);}
										@Override public void value(long data) {Assert.assertEquals(data,1000000000000L);}
										@Override public void value(double data) {Assert.assertEquals(data,1.2,0.0001);}
										@Override public void value(boolean data) {Assert.assertEquals(data,awaited);}
										@Override public void value() {called = true;}
									};
		final JsonSaxParser	jcp = new JsonSaxParser(jch);
		
		startDoc = endDoc = false;		
		awaited = true;		jcp.parse("true\n".toCharArray());	Assert.assertTrue(startDoc);	Assert.assertTrue(endDoc);
		
		awaited = false;	jcp.parse("false\n".toCharArray()); 
		called = false;		jcp.parse("null\n".toCharArray());	Assert.assertTrue(called);
		
		value = 100;		jcp.parse("100\n".toCharArray());
		value = -100;		jcp.parse("-100\n".toCharArray());
		jcp.parse("1000000000000\n".toCharArray());
		jcp.parse("1.2\n".toCharArray());

		jcp.parse("\"TEST\"\n".toCharArray());
		jcp.parse("\"TEST\\n\"\n".toCharArray());

		awaited = true;
		try{jcp.parse("true true\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (extra data in the input)");
		} catch (IllegalArgumentException exc) {
		}
		awaited = false;
		try{jcp.parse("false false\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (extra data in the input)");
		} catch (IllegalArgumentException exc) {
		}
		try{jcp.parse("null null\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (extra data in the input)");
		} catch (IllegalArgumentException exc) {
		}
		value = 100;
		try{jcp.parse("100 100\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (extra data in the input)");
		} catch (IllegalArgumentException exc) {
		}
		try{jcp.parse("\"TEST\" \"TEST\"\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (extra data in the input)");
		} catch (IllegalArgumentException exc) {
		}

		try{jcp.parse("truth\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown sequence)");
		} catch (IllegalArgumentException exc) {
		}
		try{jcp.parse("\"\\z\"\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (unknown escaping)");
		} catch (IllegalArgumentException exc) {
		}
		try{jcp.parse("\"TRUE\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (unclosed conatant)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void arraysTest() throws IOException, SyntaxException {
		final JsonSaxHandler	jch	= new JsonSaxHandler(){
										@Override public void startDoc() {}
										@Override public void endDoc() {}
										@Override public void startObj() {}
										@Override public void endObj() {}
										@Override public void startArr() {level++; startArray = true;}
										@Override public void endArr() {level--; endArray = true;}
										@Override public void startName(char[] data, int from, int len) {}
										@Override public void startName(String name) {}
										@Override public void startName(long id) {}
										@Override public void endName() {}
										@Override public void startIndex(int index) {maxIndex = Math.max(index, maxIndex); count++;}
										@Override public void endIndex() {count++;}
										@Override public void value(char[] data, int from, int len) {Assert.assertEquals(new String(data,from,len),"TEST");}
										@Override public void value(String data) {Assert.assertEquals(data,"TEST\n");}
										@Override public void value(int data) {Assert.assertEquals(data,value);}
										@Override public void value(long data) {Assert.assertEquals(data,1000000000000L);}
										@Override public void value(double data) {Assert.assertEquals(data,1.2,0.0001);}
										@Override public void value(boolean data) {Assert.assertEquals(data,awaited);}
										@Override public void value() {called = true;}
									};
		final JsonSaxParser	jcp = new JsonSaxParser(jch);
		
		startArray = endArray = false;
		jcp.parse("[]\n".toCharArray());
		Assert.assertTrue(startArray);
		Assert.assertTrue(endArray);
		
		
		value = 100;	jcp.parse("[100]\n".toCharArray());
		value = 100;	jcp.parse("[100,100]\n".toCharArray());
		
		awaited = true;	value = 100; maxIndex = 0; count = 0;		
		jcp.parse("[100,\"TEST\",true]\n".toCharArray());	
		Assert.assertEquals(maxIndex,2);	Assert.assertEquals(count,6);
		
		try{jcp.parse("[100,100\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (missing close bracket)");
		} catch (IllegalArgumentException exc) {
		}
		try{jcp.parse("[100 100]\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (missing delimiter)");
		} catch (IllegalArgumentException exc) {
		}
		
	}
	
	@Test
	public void objTest() throws IOException, SyntaxException {
		final JsonSaxHandler	jch	= new JsonSaxHandler(){
										@Override public void startDoc() {}
										@Override public void endDoc() {}
										@Override public void startObj() {startObj = true;}
										@Override public void endObj() {endObj = true;}
										@Override public void startArr() {}
										@Override public void endArr() {}
										@Override public void startName(char[] data, int from, int len) {startName(new String(data,from,len));}
										@Override public void startName(String name) {level++; count++; Assert.assertEquals(name,"field");}
										@Override public void startName(long id) {}
										@Override public void endName() {level--; count++;}
										@Override public void startIndex(int index) {}
										@Override public void endIndex() {}
										@Override public void value(char[] data, int from, int len) {Assert.assertEquals(new String(data,from,len),"TEST");}
										@Override public void value(String data) {Assert.assertEquals(data,"TEST\n");}
										@Override public void value(int data) {Assert.assertEquals(data,value);}
										@Override public void value(long data) {Assert.assertEquals(data,1000000000000L);}
										@Override public void value(double data) {Assert.assertEquals(data,1.2,0.0001);}
										@Override public void value(boolean data) {Assert.assertEquals(data,awaited);}
										@Override public void value() {called = true;}
									};
		final JsonSaxParser	jcp = new JsonSaxParser(jch);

		startObj = endObj = false;
		jcp.parse("{}\n".toCharArray());
		Assert.assertTrue(startObj);
		Assert.assertTrue(endObj);

		value = 100;	jcp.parse("{\"field\":100}\n".toCharArray());
		value = 100;	jcp.parse("{\"field\":100,\"field\":\"TEST\"}\n".toCharArray());

		try{jcp.parse("{\"field\":100\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (missing close bracket)");
		} catch (IllegalArgumentException exc) {
		}
		try{jcp.parse("{\"field\"}\n".toCharArray());
			Assert.fail("Mandatory exception was not detected (missing value)");
		} catch (IllegalArgumentException exc) {
		}
	}	

	@Test
	public void complexTest() throws IOException, SyntaxException {
		final JsonSaxHandler	jch	= new JsonSaxHandler(){
										@Override public void startDoc() {}
										@Override public void endDoc() {}
										@Override public void startObj() {}
										@Override public void endObj() {}
										@Override public void startArr() {}
										@Override public void endArr() {}
										@Override public void startName(char[] data, int from, int len) {}
										@Override public void startName(String name) {}
										@Override public void startName(long id) {}
										@Override public void endName() {}
										@Override public void startIndex(int index) {}
										@Override public void endIndex() {}
										@Override public void value(char[] data, int from, int len) {}
										@Override public void value(String data) {}
										@Override public void value(int data) {}
										@Override public void value(long data) {}
										@Override public void value(double data) {}
										@Override public void value(boolean data) {}
										@Override public void value() {}
									};
		final JsonSaxParser	jcp = new JsonSaxParser(jch);
		
		jcp.parse("{\"a\":[{\"b\":true},{\"c\":false}]}\n".toCharArray());
	}
}
