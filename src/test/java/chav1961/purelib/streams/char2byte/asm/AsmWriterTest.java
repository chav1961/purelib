package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import chav1961.purelib.streams.char2byte.AsmWriter;

public class AsmWriterTest {

	@Test
	public void test() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final AsmWriter		wr = new AsmWriter(baos)) {
	
			wr.write("			.package chav1961.purelib.streams.char2byte.asm\n");
			wr.write("Test		.class public\n");
			wr.write("call		.method void public static\n");
			wr.write("			.stack 5\n");
			wr.write("			return\n");
			wr.write("call		.end\n");
			wr.write("Test		.end\n");
			wr.flush();
			
			final Class<?>		clazz = ClassContainerTest.loadClass("chav1961.purelib.streams.char2byte.asm.Test",baos);
			clazz.getMethod("call").invoke(null);
		}
	}
}
