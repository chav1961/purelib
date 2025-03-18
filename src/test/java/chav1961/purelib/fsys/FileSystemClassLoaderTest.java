package chav1961.purelib.fsys;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.fsys.internal.FileSystemOnFile;

@Tag("OrdinalTestCategory")
public class FileSystemClassLoaderTest {
	@Test
	public void test() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		try(final FileSystemInterface 	fsi = new FileSystemOnFile(URI.create("file:./src/test/resources"))) {
			final FileSystemClassLoader	loader = new FileSystemClassLoader(Thread.currentThread().getContextClassLoader(),fsi);
			
			final Class<?>	cl = loader.loadClass(TestClass.class.getName());
			Assert.assertEquals(42,((Integer)cl.getMethod("getValue").invoke(null)).intValue());
			
			try(final InputStream	is = loader.getResourceAsStream("/chav1961/purelib/fsys/classloader/file.txt");
				final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				
				Utils.copyStream(is,baos);
				Assert.assertEquals("test string",new String(baos.toByteArray()));
			}
		}
	}
}
