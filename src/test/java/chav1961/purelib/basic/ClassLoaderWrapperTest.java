package chav1961.purelib.basic;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

public class ClassLoaderWrapperTest {
	static boolean	alreadyTested = false;
	
	@Test
	public void test() throws IOException {
		if (!alreadyTested) {	// Protections against repeatable test with the same ClassLoader
			final Class<?>	clazz = new ClassLoaderWrapper().createClass("chav1961.purelib.basic.Test",new StringReader(" .package chav1961.purelib.basic\nTest .class public\nTest .end"));
			
			Assert.assertEquals(clazz.getName(),"chav1961.purelib.basic.Test");
			
			try{new ClassLoaderWrapper().createClass(null,new byte[100]);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ClassLoaderWrapper().createClass("",new byte[100]);
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ClassLoaderWrapper().createClass("myclass",(byte[])null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ClassLoaderWrapper().createClass("myclass",new byte[0]);
				Assert.fail("Mandatory exception was not detected (too short 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{new ClassLoaderWrapper().createClass(null,new StringReader(""));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)"); 
			} catch (IllegalArgumentException exc) {
			}
			try{new ClassLoaderWrapper().createClass("",new StringReader(""));
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{new ClassLoaderWrapper().createClass("myclass",(Reader)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			alreadyTested = true;
		}
	}
}
