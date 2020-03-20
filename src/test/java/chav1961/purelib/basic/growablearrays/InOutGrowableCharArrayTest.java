package chav1961.purelib.basic.growablearrays;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class InOutGrowableCharArrayTest {
	@Test
	public void test() throws PrintingException, IOException {
		try(final InOutGrowableCharArray	content = new InOutGrowableCharArray(true)) {
			final String					tail = new String(InOutGrowableCharArray.CRNL); 
			
			content.clear();	content.println();
			Assert.assertEquals(tail, new String(content.extract()));
	
			content.clear();	content.print('a');
			Assert.assertEquals("a", new String(content.extract()));
			content.clear();	content.println('a');
			Assert.assertEquals("a"+tail, new String(content.extract()));
	
			content.clear();	content.print((byte)10);
			Assert.assertEquals("10", new String(content.extract()));
			content.clear();	content.println((byte)10);
			Assert.assertEquals("10"+tail, new String(content.extract()));
	
			content.clear();	content.print((short)10);
			Assert.assertEquals("10", new String(content.extract()));
			content.clear();	content.println((short)10);
			Assert.assertEquals("10"+tail, new String(content.extract()));
	
			content.clear();	content.print((int)10);
			Assert.assertEquals("10", new String(content.extract()));
			content.clear();	content.println((int)10);
			Assert.assertEquals("10"+tail, new String(content.extract()));
	
			content.clear();	content.print((long)10);
			Assert.assertEquals("10", new String(content.extract()));
			content.clear();	content.println((long)10);
			Assert.assertEquals("10"+tail, new String(content.extract()));
		
			content.clear();	content.print((float)12.5);
			Assert.assertEquals("12.5", new String(content.extract()));
			content.clear();	content.println((float)12.5);
			Assert.assertEquals("12.5"+tail, new String(content.extract()));
		
			content.clear();	content.print((double)12.5);
			Assert.assertEquals("12.5", new String(content.extract()));
			content.clear();	content.println((double)12.5);
			Assert.assertEquals("12.5"+tail, new String(content.extract()));
	
			content.clear();	content.print(true);
			Assert.assertEquals("true", new String(content.extract()));
			content.clear();	content.println(false);
			Assert.assertEquals("false"+tail, new String(content.extract()));
	
			content.clear();	content.print("test");
			Assert.assertEquals("test", new String(content.extract()));
			content.clear();	content.println("test");
			Assert.assertEquals("test"+tail, new String(content.extract()));
			content.clear();	content.print((String)null);
			Assert.assertEquals("null", new String(content.extract()));
			content.clear();	content.println((String)null);
			Assert.assertEquals("null"+tail, new String(content.extract()));
	
			content.clear();	content.print("test",0,2);
			Assert.assertEquals("te", new String(content.extract()));
			content.clear();	content.println("test",0,2);
			Assert.assertEquals("te"+tail, new String(content.extract()));
			content.clear();	content.print((String)null,0,2);
			Assert.assertEquals("null", new String(content.extract()));
			content.clear();	content.println((String)null,0,2);
			Assert.assertEquals("null"+tail, new String(content.extract()));
	
			content.clear();	content.print("test".toCharArray());
			Assert.assertEquals("test", new String(content.extract()));
			content.clear();	content.println("test".toCharArray());
			Assert.assertEquals("test"+tail, new String(content.extract()));
			content.clear();	content.print((char[])null);
			Assert.assertEquals("null", new String(content.extract()));
			content.clear();	content.println((char[])null);
			Assert.assertEquals("null"+tail, new String(content.extract()));
	
			content.clear();	content.print("test".toCharArray(),0,2);
			Assert.assertEquals("te", new String(content.extract()));
			content.clear();	content.println("test".toCharArray(),0,2);
			Assert.assertEquals("te"+tail, new String(content.extract()));
			content.clear();	content.print((char[])null);
			Assert.assertEquals("null", new String(content.extract()));
			content.clear();	content.println((char[])null);
			Assert.assertEquals("null"+tail, new String(content.extract()));
	
			content.clear();	content.print(Integer.valueOf(10));
			Assert.assertEquals("10", new String(content.extract()));
			content.clear();	content.println(Integer.valueOf(10));
			Assert.assertEquals("10"+tail, new String(content.extract()));
			content.clear();	content.print((Object)null);
			Assert.assertEquals("null", new String(content.extract()));
			content.clear();	content.println((Object)null);
			Assert.assertEquals("null"+tail, new String(content.extract()));
		}
	}
}
