package chav1961.purelib.cdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.cdb.interfaces.AppDebugInterface;
import chav1961.purelib.cdb.interfaces.AppDebugInterface.Event;
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.cdb.interfaces.ArrayWrapper;
import chav1961.purelib.cdb.interfaces.ClassWrapper;
import chav1961.purelib.cdb.interfaces.ObjectWrapper;
import chav1961.purelib.cdb.interfaces.StackWrapper;
import chav1961.purelib.cdb.interfaces.ThreadWrapper;

@Category(OrdinalTestCategory.class)
public class ConnectorsTest {

	@Test
	public void basicTest() throws DebuggingException, IOException, InterruptedException, NoSuchMethodException {
		
	}
	
//	@Test
	public void basicTest1() throws DebuggingException, IOException, InterruptedException, NoSuchMethodException {
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

																					// Suspend location access
						Assert.assertEquals(FileInputStream.class,tw.getCurrentLocation().getClassInside().contentType());
						Assert.assertEquals("readBytes",tw.getCurrentLocation().getMethodInside().name());
						
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
						Assert.assertEquals(2,((ObjectWrapper[])((ArrayWrapper)sw.getVar("longContent").getValue()).get()).length);

						Assert.assertNull(sw.getThis());
						
						final ClassWrapper	cw = adi.getClass(PoorRabbit.class.getCanonicalName());
						
						Assert.assertArrayEquals(new String[]{"x"},cw.getFieldNames());
						Assert.assertEquals(int.class,cw.getClassField("x").contentType());
						Assert.assertEquals(Integer.valueOf(10),cw.getClassField("x").get());
						
						final int	bp = tw.getExecutionControl().setBreakpoint(PoorRabbit.class, PoorRabbit.class.getDeclaredMethod("init"),17);
						
						tw.getExecutionControl().resume();
						next(os);
						
						for (Event event : adi.waitEvent()) {
							switch (event.getType()) {
								case BreakPointEvent	:
									final StackWrapper	initSw = tw.getStackContent(0);
									
									Assert.assertEquals(tw.getCurrentState(),"running");
									Assert.assertTrue(tw.getExecutionControl().isSuspended());
									Assert.assertEquals(17,tw.getCurrentLocation().getLineNo());
									Assert.assertEquals(Integer.valueOf(20),initSw.getVar("y").getValue());
									break;
								default:
									break;
							}
						}
						
						tw.getExecutionControl().step();
						tw.getExecutionControl().resume();

						for (Event event : adi.waitEvent()) {
							switch (event.getType()) {
								case StepEvent			:
									final StackWrapper	initSw = tw.getStackContent(0);
									
									Assert.assertEquals(tw.getCurrentState(),"running");
									Assert.assertTrue(tw.getExecutionControl().isSuspended());
									Assert.assertEquals(18,tw.getCurrentLocation().getLineNo());
									Assert.assertEquals(Integer.valueOf(20),initSw.getVar("z").getValue());
									break;
								default:
									break;
							}
						}
						
						tw.getExecutionControl().resume();
					}
				}
			}
		}
		p.waitFor();
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
