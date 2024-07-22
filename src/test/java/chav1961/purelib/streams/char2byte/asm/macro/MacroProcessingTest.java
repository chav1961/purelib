package chav1961.purelib.streams.char2byte.asm.macro;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.char2byte.AsmWriter;
import chav1961.purelib.streams.char2byte.asm.TestInterface;

@Tag("OrdinalTestCategory")
public class MacroProcessingTest {
//	@Test
	public void simpleTest() throws ContentException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final InputStream		is = this.getClass().getResourceAsStream("source.txt");
				final Reader			rdr = new InputStreamReader(is);
				final AsmWriter			asm = new AsmWriter(baos)) {
				
				Utils.copyStream(rdr,asm);
			}
			
			@SuppressWarnings("unchecked")
			final Class<TestInterface>	cl = (Class<TestInterface>) new SimpleURLClassLoader(new URL[0]).createClass("chav1961.purelib.streams.char2byte.asm.MacroTest",baos.toByteArray());
			final TestInterface			inst = cl.getConstructor().newInstance();
			
			Assert.assertEquals(inst.sub(20,15),5);
		}
	}

	@Test
	public void parameterTest() throws ContentException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			try(final InputStream		is = this.getClass().getResourceAsStream("parameters.txt");
				final Reader			rdr = new InputStreamReader(is);
				final AsmWriter			asm = new AsmWriter(baos,new PrintWriter(System.err))) {
				
				Utils.copyStream(rdr,asm);
			}
			
			@SuppressWarnings("unchecked")
			final Class<TestInterface>	cl = (Class<TestInterface>) new SimpleURLClassLoader(new URL[0]).createClass("chav1961.purelib.streams.char2byte.asm.MacroTestP",baos.toByteArray());
			final TestInterface			inst = cl.getConstructor().newInstance();
			
			Assert.assertEquals("p1=1,p2=2.0,p3=3,p4=true,p5=10,p6=20.0,p7=30,p8=false,p9=10,p10=20.0,p11=test,p12a=false,p12b=true",inst.call());
		}
	}
}
