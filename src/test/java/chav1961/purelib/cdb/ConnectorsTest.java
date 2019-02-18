package chav1961.purelib.cdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.sun.jdi.StackFrame;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.cdb.interfaces.AppDebugInterface;
import chav1961.purelib.cdb.interfaces.ArrayWrapper;
import chav1961.purelib.cdb.interfaces.StackWrapper;
import chav1961.purelib.cdb.interfaces.ThreadWrapper;

public class ConnectorsTest {

	@Test
	public void basicTest() throws DebuggingException, IOException, InterruptedException, NoSuchMethodException {
		final File		currentDir = new File("./target/test-classes");
		final String	clazz = PoorRabbit.class.getCanonicalName();
		final Process	p = new ProcessBuilder("java","-Xdebug","-agentlib:jdwp=transport=dt_shmem,server=y,suspend=n",clazz).directory(currentDir).start();
		
		try(final Reader			rdr = new InputStreamReader(p.getInputStream());
			final BufferedReader	brdr = new BufferedReader(rdr);
			final OutputStream		os = p.getOutputStream()) {
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				if (line.startsWith("PID=")) {
					try(final AppDebugInterface	adi = DbgClient.connectTo(Long.valueOf(line.substring(4)))) {
						Assert.assertTrue(contains("main",adi.getThreadNames()));	// Get information about content 
						Assert.assertTrue(contains(PoorRabbit.class.getCanonicalName(),adi.getClassNames()));
						Assert.assertEquals(1,adi.getClassNames(this.getClass().getPackage()).length);
						
						final ThreadWrapper	tw = adi.getThread("main");				// Get access to main thread
						
						Assert.assertEquals("running",tw.getCurrentState());
						Assert.assertTrue(tw.getExecutionControl().isStarted());
						Assert.assertFalse(tw.getExecutionControl().isSuspended());
						tw.getExecutionControl().suspend();							// Suspend main thread
						Assert.assertEquals("running",tw.getCurrentState());
						Assert.assertTrue(tw.getExecutionControl().isStarted());
						Assert.assertTrue(tw.getExecutionControl().isSuspended());
						
						Assert.assertEquals(5,tw.getStackSize());					// Stack frame access
						Assert.assertEquals(13,tw.getStackContent(4).getVarNames().length);
						Assert.assertTrue(contains("args",tw.getStackContent(4).getVarNames()));
						Assert.assertEquals(13,tw.getStackContent(PoorRabbit.class.getMethod("main",String[].class)).getVarNames().length);
						Assert.assertTrue(contains("args",tw.getStackContent(PoorRabbit.class.getMethod("main",String[].class)).getVarNames()));

						final StackWrapper	sw = tw.getStackContent(4);
						
						Assert.assertEquals(Byte.valueOf((byte)10),sw.getVar("varByte").getValue());
						Assert.assertEquals(Short.valueOf((short)20),sw.getVar("varShort").getValue());
						Assert.assertEquals(Character.valueOf('*'),sw.getVar("varChar").getValue());
						Assert.assertEquals(Integer.valueOf(30),sw.getVar("varInt").getValue());
						Assert.assertEquals(Long.valueOf(40L),sw.getVar("varLong").getValue());
						Assert.assertEquals(Float.valueOf(50.0f),sw.getVar("varFloat").getValue());
						Assert.assertEquals(Double.valueOf(60.0),sw.getVar("varDouble").getValue());
						Assert.assertEquals(Boolean.valueOf(true),sw.getVar("varBoolean").getValue());
						Assert.assertArrayEquals("test string".toCharArray(),(char[])((ArrayWrapper)sw.getVar("charContent").getValue()).get());
						Assert.assertArrayEquals(new String[]{"test string1","test string2"},(String[])((ArrayWrapper)sw.getVar("stringContent").getValue()).get());
//						Assert.assertArrayEquals(new Long[]{10L,20L},(Long[])((ArrayWrapper)sw.getVar("longContent").getValue()).get());
						
						tw.getExecutionControl().resume();
						next(os);
					}
				}
			}
			System.err.println("line="+line);
		}
		p.waitFor();
		System.err.println("The end! "+p.exitValue());
	}
	
	private static boolean contains(final String item, final String... list) {
		for (String entity : list) {
			if (item.equals(entity)) {
				return true;
			}
		}
		return false;
	}
	
	private static void next(final OutputStream os) throws IOException {
		os.write(0);
		os.flush();
	}
}
