package chav1961.purelib.streams.char2byte.asm.macro;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class MacroClassLoaderTest {
	@Test
	public void complexTest() throws CalculationException, IOException {
		try(final MacroClassLoader	mcl = new MacroClassLoader(this.getClass().getClassLoader())) {
			final GrowableCharArray	gca = new GrowableCharArray(false);
			final String			packageName = MacroClassLoaderTest.class.getPackage().getName();
			final String			className = "X"+((int)(1000000*Math.random()));
			
			gca.append(" .package "+packageName+"\n"+className+" .class public\n"+className+" .end\n");
			mcl.createClass(packageName+"."+className,gca);
			
			try{mcl.createClass(null,gca);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{mcl.createClass("",gca);
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{mcl.createClass(packageName+"."+className,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}

		try(final Writer			wr = new StringWriter();
			final MacroClassLoader	mcl = new MacroClassLoader(this.getClass().getClassLoader(),wr)) {
			final GrowableCharArray	gca = new GrowableCharArray(false);
			final String			packageName = MacroClassLoaderTest.class.getPackage().getName();
			final String			className = "X"+((int)(1000000*Math.random()));
			
			gca.append(" .import "+MacroClassLoader.class.getName()+"\n");
			gca.append(" .package "+packageName+"\n"+className+" .class public extends "+MacroClassLoader.class.getName()+"\n"+className+" .end\n");
			mcl.createClass(packageName+"."+className,gca);
			
			try{mcl.createClass(null,gca);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{mcl.createClass("",gca);
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{mcl.createClass(packageName+"."+className,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}
		
		try{new MacroClassLoader(this.getClass().getClassLoader(),null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
