package chav1961.purelib.basic;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class InternalUtilsTest {
	@Test
	public void parseCommandLineTest()  {
		final List<String[]>	vars = new ArrayList<String[]>();
		
		// Parsing as-is and escaping
		Assert.assertTrue(parseCommandLine("String as is","String as is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String   as   is  ","String as is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String   as   is  "," String as is",vars) > 0);
		Assert.assertTrue(parseCommandLine("\\ [ ] { | } < > $","\\\\ \\[ \\] \\{ \\| \\} \\< \\> \\$",vars) > 0);
		
		// Parsing options
		Assert.assertTrue(parseCommandLine("String as is","String [as] is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String is","String [as] is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String   is  ","String [as] is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String as is","String as [is]",vars) > 0);
		Assert.assertTrue(parseCommandLine("String as","String as [is]",vars) > 0);
		Assert.assertTrue(parseCommandLine("String as is ","String as [is]",vars) > 0);
		Assert.assertTrue(parseCommandLine("String as is","[String] as is",vars) > 0);
		Assert.assertTrue(parseCommandLine("as is","[String] as is",vars) > 0);

		// Parsing cases
		Assert.assertTrue(parseCommandLine("String one is","String {one|two|} is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String two is","String {one|two|} is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String is","String {one|two|} is",vars) > 0);
		Assert.assertFalse(parseCommandLine("String is","String {one|two|three} is",vars) >= 0);
		Assert.assertFalse(parseCommandLine("String three is","String {one|two|} is",vars) >= 0);
		Assert.assertTrue(parseCommandLine("one is","{one|two|} is",vars) > 0);
		Assert.assertTrue(parseCommandLine("two is","{one|two|} is",vars) > 0);
		Assert.assertTrue(parseCommandLine("is","{one|two|} is",vars) > 0);
		Assert.assertTrue(parseCommandLine("String one","String {one|two|}",vars) > 0);
		Assert.assertTrue(parseCommandLine("String two","String {one|two|}",vars) > 0);
		Assert.assertTrue(parseCommandLine("String","String {one|two|}",vars) > 0);

		// Parsing repeats with seq char
		Assert.assertTrue(parseCommandLine("String oh shit","String <oh>,... shit",vars) > 0);
		Assert.assertTrue(parseCommandLine("String oh,oh shit","String <oh>,... shit",vars) > 0);
		Assert.assertTrue(parseCommandLine("String oh , oh shit","String <oh>,... shit",vars) > 0);
		Assert.assertTrue(parseCommandLine("oh , oh shit","<oh>,... shit",vars) > 0);
		Assert.assertTrue(parseCommandLine("String oh , oh","String <oh>,...",vars) > 0);

		// Parsing repeats without seq char
		Assert.assertTrue(parseCommandLine("String oh shit","String <oh>... shit",vars) > 0);
		Assert.assertTrue(parseCommandLine("String oh oh shit","String <oh>... shit",vars) > 0);
		Assert.assertTrue(parseCommandLine("oh oh shit","<oh>... shit",vars) > 0);
		Assert.assertTrue(parseCommandLine("String oh oh","String <oh>...",vars) > 0);

		// Parsing variable
		vars.clear();
		Assert.assertTrue(parseCommandLine("String text value","String ${key} value",vars) > 0);
		Assert.assertArrayEquals(vars.get(0),new String[]{"key","text"});
		vars.clear();
		Assert.assertTrue(parseCommandLine("String value","String ${key} value",vars) > 0);
		Assert.assertArrayEquals(vars.get(0),new String[]{"key",""});
		vars.clear();
		Assert.assertTrue(parseCommandLine("String    value","String ${key} value",vars) > 0);
		Assert.assertArrayEquals(vars.get(0),new String[]{"key",""});
		vars.clear();
		Assert.assertTrue(parseCommandLine("String value","String ${key}",vars) > 0);
		Assert.assertArrayEquals(vars.get(0),new String[]{"key","value"});
		vars.clear();
		Assert.assertTrue(parseCommandLine("String text","${key} text",vars) > 0);
		Assert.assertArrayEquals(vars.get(0),new String[]{"key","String"});
		Assert.assertTrue(parseCommandLine("\"String\" text","${key} text",vars) > 0);
		Assert.assertArrayEquals(vars.get(0),new String[]{"key","String"});
	
		// Complex test
		vars.clear();
		Assert.assertTrue(parseCommandLine("One,two,three let me see","<${name}>,... let [{me ${action}|you ${mode}}]",vars) > 0);
		Assert.assertEquals(vars.size(),4);
		Assert.assertArrayEquals(vars.get(0),new String[]{"name","One"});
		Assert.assertArrayEquals(vars.get(1),new String[]{"name","two"});
		Assert.assertArrayEquals(vars.get(2),new String[]{"name","three"});
		Assert.assertArrayEquals(vars.get(3),new String[]{"action","see"});

		vars.clear();
		Assert.assertTrue(parseCommandLine("+1-2=3","<{+${one}|-${two}}>...=${three}",vars) > 0);
		Assert.assertEquals(vars.size(),3);
		Assert.assertArrayEquals(vars.get(0),new String[]{"one","1"});
		Assert.assertArrayEquals(vars.get(1),new String[]{"two","2"});
		Assert.assertArrayEquals(vars.get(2),new String[]{"three","3"});
		
		
		// Illegal parameters
		try{parseCommandLine(null,"test",vars);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parseCommandLine("","test",vars);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parseCommandLine("test",null,vars);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parseCommandLine("test","",vars);
			Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{parseCommandLine("test","test",null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void convertTest() throws MalformedURLException  {
		// Illegal parameters
		try{InternalUtils.convert(null,double.class);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.convert("",double.class);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{InternalUtils.convert("test",null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		// String as-is
		Assert.assertEquals(InternalUtils.convert("Test",String.class),"Test");

		// Numeric primitives
		Assert.assertEquals(InternalUtils.convert("100",int.class),Integer.valueOf(100));
		Assert.assertEquals(InternalUtils.convert("100",long.class),Long.valueOf(100));
		Assert.assertEquals(InternalUtils.convert("100",float.class),Float.valueOf(100));
		Assert.assertEquals(InternalUtils.convert("100",double.class),Double.valueOf(100));
		
		try{InternalUtils.convert("illegal",double.class);
			Assert.fail("Mandatory exception was not detected (illegal numeric)");
		} catch (IllegalArgumentException exc) {
		}

		// Numeric wrappers
		Assert.assertEquals(InternalUtils.convert("100",Integer.class),Integer.valueOf(100));
		Assert.assertEquals(InternalUtils.convert("100",Long.class),Long.valueOf(100));
		Assert.assertEquals(InternalUtils.convert("100",Float.class),Float.valueOf(100));
		Assert.assertEquals(InternalUtils.convert("100",Double.class),Double.valueOf(100));
		
		// Boolean
		Assert.assertTrue(InternalUtils.convert("true",boolean.class));
		Assert.assertTrue(InternalUtils.convert("true",Boolean.class));
		Assert.assertFalse(InternalUtils.convert("false",boolean.class));
		Assert.assertFalse(InternalUtils.convert("false",Boolean.class));
		
		// File, URL, URI
		Assert.assertEquals(InternalUtils.convert("./",File.class),new File("./"));
		Assert.assertEquals(InternalUtils.convert("http://localhost",URL.class),new URL("http://localhost"));
		Assert.assertEquals(InternalUtils.convert("http://localhost",URI.class),URI.create("http://localhost"));
		
		// Enumerations
		Assert.assertEquals(InternalUtils.convert("value1",PseudoTestEnum.class),PseudoTestEnum.value1);
		
		// Conversion problems
		try{InternalUtils.convert("test",ArrayList.class);
			Assert.fail("Mandatory exception was not detected (unsupported conversion)");
		} catch (UnsupportedOperationException exc) {
		}		
		try{InternalUtils.convert("://localhost",URL.class);
			Assert.fail("Mandatory exception was not detected (invalid URL)");
		} catch (IllegalArgumentException exc) {
		}		
		try{InternalUtils.convert("://localhost",URI.class);
			Assert.fail("Mandatory exception was not detected (invalid URI)");
		} catch (IllegalArgumentException exc) {
		}		
	}
	
	
	
	private static int parseCommandLine(final String source, final String template, final List<String[]> pairs) {
		return InternalUtils.parseCommandLine(source,template,pairs); 
	}	
}

enum PseudoTestEnum {
	value1, value2
}