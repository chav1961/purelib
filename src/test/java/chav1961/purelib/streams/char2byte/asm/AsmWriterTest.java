package chav1961.purelib.streams.char2byte.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.streams.char2byte.AsmWriter;

public class AsmWriterTest {

	@Test
	public void test() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final AsmWriter		wr = new AsmWriter(baos);
			final PrintWriter	pw = new PrintWriter(wr)) {
	
			pw.println("			.package chav1961.purelib.streams.char2byte.asm");
			pw.println("Test		.class public");
			pw.println("call		.method void public static");
			pw.println("			.stack 5");
			pw.println("			return");
			pw.println("call		.end");
			pw.println("Test		.end");
			pw.flush();
			
			final Class			clazz = ClassContainerTest.loadClass("chav1961.purelib.streams.char2byte.asm.Test",baos);
			clazz.getMethod("call").invoke(null);
		}
	}
}
