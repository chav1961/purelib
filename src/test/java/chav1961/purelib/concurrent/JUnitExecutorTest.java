package chav1961.purelib.concurrent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class JUnitExecutorTest {
	@Test
	public void basicTest() throws InterruptedException, FlowException {
		final JUnitExecutor<String,String>	ex = new JUnitExecutor<>();
		final Thread	t = new Thread(()->{
							System.err.println("Child started");
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
							System.err.println("Child ended");
						});
		
		t.setDaemon(true);
		t.start();
		Thread.sleep(1000);

		"LOWER".equals(ex.call("lower"));
		
		try{ex.call("throw");
			Assert.fail("Mandatory exception was not detected (Throwable throws)");
		} catch (FlowException exc) {
		}
		
		t.interrupt();
		Thread.sleep(1000);
		
		System.err.println("Waiting up to 20 sec...");
		try{ex.call("lower");
			Assert.fail("Mandatory exception was not detected (Thread is dead)");
		} catch (FlowException exc) {
		}
		
		try{ex.call(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
