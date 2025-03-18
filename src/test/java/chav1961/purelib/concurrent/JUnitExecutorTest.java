package chav1961.purelib.concurrent;

import java.io.PrintStream;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.testing.TestingUtils;

@Tag("OrdinalTestCategory")
public class JUnitExecutorTest {
	final PrintStream	ps = TestingUtils.err();
	
	@Test
	public void basicTest() throws InterruptedException, DebuggingException {
		final JUnitExecutor<String,String>	ex = new JUnitExecutor<>();
		
		final Thread	t = new Thread(()->{
							ps.println("Child started");
							for (;;) {
								try{ex.waitCommand((cmd,parms)->{
										if ("throw".equals(cmd)) {
											throw new Throwable(cmd);
										}
										else {
											return cmd.toUpperCase();
										}
									});
								} catch (InterruptedException e) {
									break;
								}
							}
							ps.println("Child ended");
						});
		
		t.setDaemon(true);
		t.start();
		Thread.sleep(1000);

		"LOWER".equals(ex.call("lower"));
		
		try{ex.call("throw");
			Assert.fail("Mandatory exception was not detected (Throwable throws)");
		} catch (DebuggingException exc) {
		}
		
		t.interrupt();
		Thread.sleep(1000);
		
		ps.println("Waiting up to 20 sec...");
		try{ex.call("lower");
			Assert.fail("Mandatory exception was not detected (Thread is dead)");
		} catch (DebuggingException exc) {
		}
		
		try{ex.call(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
