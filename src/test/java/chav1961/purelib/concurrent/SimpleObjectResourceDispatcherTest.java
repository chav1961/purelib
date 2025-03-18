package chav1961.purelib.concurrent;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.concurrent.interfaces.ResourceDispatcherLock;

@Tag("OrdinalTestCategory")
public class SimpleObjectResourceDispatcherTest {
	private static final String		RES_1 = "res1";
	private static final String		RES_2 = "res2";
	private static final String		RES_3 = "res3";
	
	@Test
	public void basicTest() throws InterruptedException {
		try(final SimpleObjectResourceDispatcher<String>	rd = new SimpleObjectResourceDispatcher<>(RES_1)) {
			Assert.assertEquals(1,toArray(rd.getRegisteredResources()).length);
			Assert.assertTrue(rd.isResourceRegistered(RES_1));
			Assert.assertFalse(rd.isResourceRegistered(RES_2));
			
			rd.registerResource(RES_2);
			Assert.assertEquals(2,toArray(rd.getRegisteredResources()).length);
			Assert.assertTrue(rd.isResourceRegistered(RES_2));
			
			try{rd.registerResource(null);
				Assert.fail("Mandytory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{rd.registerResource(RES_1);
				Assert.fail("Mandytory exception was not detected (already registered)");
			} catch (IllegalArgumentException exc) {
			}

			rd.unregisterResource(RES_2);
			Assert.assertEquals(1,toArray(rd.getRegisteredResources()).length);
			Assert.assertFalse(rd.isResourceRegistered(RES_2));

			try{rd.unregisterResource(null);
				Assert.fail("Mandytory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{rd.unregisterResource(RES_2);
				Assert.fail("Mandytory exception was not detected (not registered)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}	
	
	@Test
	public void lifeCycleTest() throws IllegalStateException, InterruptedException {
		try(final SimpleObjectResourceDispatcher<String>	rd = new SimpleObjectResourceDispatcher<>(RES_1)) {
			Assert.assertFalse(rd.isStarted());
			Assert.assertFalse(rd.isSuspended());

			try{rd.suspend();
				Assert.fail("Mandytory exception was not detected (not started)");
			} catch (IllegalStateException exc) {
			}
			try{rd.resume();
				Assert.fail("Mandytory exception was not detected (not started)");
			} catch (IllegalStateException exc) {
			}
			
			rd.start();
			Assert.assertTrue(rd.isStarted());
			Assert.assertFalse(rd.isSuspended());

			try{rd.start();
				Assert.fail("Mandytory exception was not detected (already started)");
			} catch (IllegalStateException exc) {
			}
			
			rd.suspend();
			Assert.assertTrue(rd.isSuspended());
			
			try{rd.suspend();
				Assert.fail("Mandytory exception was not detected (already suspended)");
			} catch (IllegalStateException exc) {
			}
			
			rd.resume();
			Assert.assertFalse(rd.isSuspended());
			
			try{rd.resume();
				Assert.fail("Mandytory exception was not detected (not suspended)");
			} catch (IllegalStateException exc) {
			}
			
			rd.stop();
			Assert.assertFalse(rd.isStarted());
			Assert.assertFalse(rd.isSuspended());

			try{rd.stop();
				Assert.fail("Mandytory exception was not detected (not started)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void processingTest() throws IllegalStateException, InterruptedException, DebuggingException {
		final JUnitExecutor<String, String>		uex1 = new JUnitExecutor<>();
		final JUnitExecutor<String, String>		uex2 = new JUnitExecutor<>();
		
		try(final SimpleObjectResourceDispatcher<String>	rd = new SimpleObjectResourceDispatcher<>(RES_1)) {
			try{rd.lock(RES_1);
				Assert.fail("Mandytory exception was not detected (not started)");
			} catch (FlowException exc) {
			}
			
			rd.start();
			
			final Thread	t1 = new Thread(()->{
										while (!Thread.interrupted()) {
											try{uex1.waitCommand((String command, Object... parameters) -> {
													return processThread1(command, (SimpleObjectResourceDispatcher<String>)parameters[0]);
												});
											} catch (InterruptedException e) {
												break;
											}
										}
									});
			t1.setDaemon(true);
			t1.start();

			final Thread	t2 = new Thread(()->{
								while (!Thread.interrupted()) {
									try{uex2.waitCommand((String command, Object... parameters) -> {
											return processThread2(command, (SimpleObjectResourceDispatcher<String>)parameters[0]);
										});
									} catch (InterruptedException e) {
										break;
									}
								}
							});
			t2.setDaemon(true);
			t2.start();
			
			uex1.execute("step1",500,rd);
			Thread.sleep(500);
			uex2.execute("step1",500,rd);

			Assert.assertEquals("ok", uex1.getResponse(1));
			Assert.assertNull(uex2.getResponse(500));
			
			uex1.execute("step2",500,rd);
			Assert.assertEquals("ok", uex1.getResponse(1));
			Assert.assertEquals("ok", uex2.getResponse(500));

			uex2.execute("step2",500,rd);
			Assert.assertEquals("ok", uex2.getResponse(500));
			
			rd.stop();
		}
	}
	
	private static ResourceDispatcherLock lock1;
	
	private static String processThread1(final String action, final SimpleObjectResourceDispatcher<String> rd) throws FlowException, InterruptedException {
		switch (action) {
			case "step1" :
				lock1 = rd.lock(RES_1);
				return "ok";
			case "step2" :
				lock1.close();
				return "ok";
			default :
				return "ok";
		}
	}

	private static ResourceDispatcherLock lock2;
	
	private static String processThread2(final String action, final SimpleObjectResourceDispatcher<String> rd) throws FlowException, InterruptedException {
		switch (action) {
			case "step1" :
				lock2 = rd.lock(RES_1);
				return "ok";
			case "step2" :
				lock2.close();
				return "ok";
			default :
				return "ok";
		}
	}

	private static String[] toArray(final Iterable<String> content) {
		final List<String>	result = new ArrayList<>();
		
		for(String item : content) {
			result.add(item);
		}
		return result.toArray(new String[result.size()]);
	}
}
