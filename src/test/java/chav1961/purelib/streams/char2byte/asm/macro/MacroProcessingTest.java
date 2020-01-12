package chav1961.purelib.streams.char2byte.asm.macro;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.char2byte.asm.TestInterface;

public class MacroProcessingTest {
	static boolean alreadyTested = false;
	
	@Test
	public void test() throws ContentException, IOException, InstantiationException, IllegalAccessException {
		if (!alreadyTested) {	// Protections aginst repeatable tests with the same ClassLoader
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
				try(final InputStream		is = this.getClass().getResourceAsStream("source.txt");
					final Reader			rdr = new InputStreamReader(is);
					final AsmWriter			asm = new AsmWriter(baos)) {
					
					Utils.copyStream(rdr,asm);
				}
				
				@SuppressWarnings("unchecked")
				final Class<TestInterface>	cl = (Class<TestInterface>) new SimpleURLClassLoader(new URL[0]).createClass("chav1961.purelib.streams.char2byte.asm.MacroTest",baos.toByteArray());
				final TestInterface			inst = cl.newInstance();
				
				Assert.assertEquals(inst.sub(20,15),5);
				alreadyTested = true;
			}
		}
	}
}
