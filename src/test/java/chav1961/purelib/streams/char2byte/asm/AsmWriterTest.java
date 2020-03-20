package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class AsmWriterTest {

	@Test
	public void basicTest() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ContentException, NullPointerException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final AsmWriter		wr = new AsmWriter(baos)) {

			wr.importClass(PseudoClass.class);
			
			wr.write("			.package chav1961.purelib.streams.char2byte.asm\n");
			wr.write("Test		.class public\n");
			wr.write("			.vartable\n");
			wr.write("call		.method void public static\n");
			wr.write("			.stack 5\n");
			wr.write("			return\n");
			wr.write("call		.end\n");
			wr.write("Test		.end\n");
			wr.build();
			
			final Class<?>		clazz = ClassContainerTest.loadClass(wr.getClassName(),baos);
			clazz.getMethod("call").invoke(null);
			
			try{wr.clone(null);
				Assert.fail("mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{wr.clone(new ByteArrayOutputStream()).clone(new ByteArrayOutputStream());
				Assert.fail("mandatory exception was not detected (clone for cloned instance)");
			} catch (IllegalStateException exc) {
			}
			try{wr.importClass(null);
				Assert.fail("mandatory exception was not detected (null 1-st argument)");
			} catch (ContentException | NullPointerException exc) {
			}
		}

		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final AsmWriter		wr = new AsmWriter(baos,new StringWriter())) {
	
			wr.importClass(PseudoClass.class);
			
			wr.write("			.package chav1961.purelib.streams.char2byte.asm\n");
			wr.write("TestWr	.class public\n");
			wr.write("call		.method void public static\n");
			wr.write("			.vartable\n");
			wr.write("			.stack 5\n");
			wr.write("			return\n");
			wr.write("call		.end\n");
			wr.write("TestWr	.end\n");
			wr.build();
			
			final Class<?>		clazz = ClassContainerTest.loadClass(wr.getClassName(),baos);
			clazz.getMethod("call").invoke(null);
			
			try{wr.clone(null);
				Assert.fail("mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{wr.clone(new ByteArrayOutputStream()).clone(new ByteArrayOutputStream());
				Assert.fail("mandatory exception was not detected (clone for cloned instance)");
			} catch (IllegalStateException exc) {
			}
			try{wr.importClass(null);
				Assert.fail("mandatory exception was not detected (null 1-st argument)");
			} catch (ContentException | NullPointerException exc) {
			}
		}
		
		try{new AsmWriter(null);
			Assert.fail("mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{new AsmWriter(null,new StringWriter());
			Assert.fail("mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new AsmWriter(new ByteArrayOutputStream(),null);
			Assert.fail("mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}

class PseudoClass {
	
}