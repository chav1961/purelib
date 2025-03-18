package chav1961.purelib.concurrent;


import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.testing.TestingUtils;

@Tag("OrdinalTestCategory")
public class ListenablesTest {
	final PrintStream	ps = TestingUtils.err();
	
	/*
	 * Test scenario:
	 *   1. First thread sets current LL value to 1 and tests, that previous value was 0
	 *   2. Second thread sets current LL value to 2 and tests, that previous value was 1
	 *   3. First thread waits current LL value = 3
	 *   4. Second thread sets current LL value to 4. First thread remains awaiting
	 *   5. Second thread sets current LL value to 3. First thread wakes up
	 *   6. Second thread waits current LL value = 3 and returns immediately
	 */
	@Test
	public void intBasicTest() throws InterruptedException, TimeoutException {
		final ListenableInt		li = new ListenableInt();
		final CountDownLatch	latch = new CountDownLatch(2);
		final Exchanger			stepper1s = new Exchanger(), stepper1e = new Exchanger();
		final Thread			t1 = new Thread(()->{
									try{
										stepper1s.exchange(null);	// step 1 started
//										ps.println(Thread.currentThread().getName()+": step1 started");
										Assert.assertEquals(0,li.set(1));										
//										ps.println(Thread.currentThread().getName()+": step1 ended");
										stepper1e.exchange(null);	// step 1 ended
										
										stepper1s.exchange(null);	// step 3 started
//										ps.println(Thread.currentThread().getName()+": step3 started");
										li.await(3);										
//										ps.println(Thread.currentThread().getName()+": step3 ended");
										stepper1e.exchange(null);	// step 3 ended
										
										latch.countDown();
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		final Exchanger			stepper2s = new Exchanger(), stepper2e = new Exchanger();
		final Thread			t2 = new Thread(()->{
									try{
										stepper2s.exchange(null);	// step 2 started
//										ps.println(Thread.currentThread().getName()+": step2 started");
										Assert.assertEquals(1,li.set(2));
//										ps.println(Thread.currentThread().getName()+": step2 ended");
										stepper2e.exchange(null);	// step 2 ended
										
										stepper2s.exchange(null);	// step 4 started
//										ps.println(Thread.currentThread().getName()+": step4 started");
										li.set(4);
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										stepper2e.exchange(null);	// step 4 ended
									
										stepper2s.exchange(null);	// step 5 started
//										ps.println(Thread.currentThread().getName()+": step5 started");
										li.set(3);
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										
										stepper2s.exchange(null);	// step 6 started
//										ps.println(Thread.currentThread().getName()+": step6 started");
										li.await(3);
										stepper2e.exchange(null);	// step 6 ended
										latch.countDown();
//										ps.println(Thread.currentThread().getName()+": step6 ended");
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		
		t1.setDaemon(true);		
		t1.start();
		t2.setDaemon(true);		
		t2.start();
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 1
		stepper1e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 2 
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 3 
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 4
		
		try {stepper1e.exchange(null,100,TimeUnit.MILLISECONDS);	// Tests that thread 1 remains awaiting
			Assert.fail("Mandatory exception was not detected - thread 1 doesn't sleep!");
		} catch (TimeoutException exc) {
		}
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 5

		stepper1e.exchange(null,1,TimeUnit.SECONDS);		// test that thread 1 wakes up
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 6
		stepper2e.exchange(null,1,TimeUnit.SECONDS);

		latch.await(1,TimeUnit.SECONDS);	// Threads must be completed
	}

	@Test
	public void intExceptionsTest() throws InterruptedException, TimeoutException {
		final ListenableInt		li = new ListenableInt(10);
		
		Assert.assertEquals(10,li.get());
		
		try {li.set(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try {li.await(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try {li.await(0,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {li.await(0,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try {li.await(null,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {li.await(value->true,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {li.await(value->true,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	/*
	 * Test scenario:
	 *   1. First thread sets current LL value to 1 and tests, that previous value was 0
	 *   2. Second thread sets current LL value to 2 and tests, that previous value was 1
	 *   3. First thread waits current LL value = 3
	 *   4. Second thread sets current LL value to 4. First thread remains awaiting
	 *   5. Second thread sets current LL value to 3. First thread wakes up
	 *   6. Second thread waits current LL value = 3 and returns immediately
	 */
	@Test
	public void longBasicTest() throws InterruptedException, TimeoutException {
		final ListenableLong	ll = new ListenableLong();
		final CountDownLatch	latch = new CountDownLatch(2);
		final Exchanger			stepper1s = new Exchanger(), stepper1e = new Exchanger();
		final Thread			t1 = new Thread(()->{
									try{
										stepper1s.exchange(null);	// step 1 started
//										ps.println(Thread.currentThread().getName()+": step1 started");
										Assert.assertEquals(0,ll.set(1));										
//										ps.println(Thread.currentThread().getName()+": step1 ended");
										stepper1e.exchange(null);	// step 1 ended
										
										stepper1s.exchange(null);	// step 3 started
//										ps.println(Thread.currentThread().getName()+": step3 started");
										ll.await(3);										
//										ps.println(Thread.currentThread().getName()+": step3 ended");
										stepper1e.exchange(null);	// step 3 ended
										
										latch.countDown();
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		final Exchanger			stepper2s = new Exchanger(), stepper2e = new Exchanger();
		final Thread			t2 = new Thread(()->{
									try{
										stepper2s.exchange(null);	// step 2 started
//										ps.println(Thread.currentThread().getName()+": step2 started");
										Assert.assertEquals(1,ll.set(2));
//										ps.println(Thread.currentThread().getName()+": step2 ended");
										stepper2e.exchange(null);	// step 2 ended
										
										stepper2s.exchange(null);	// step 4 started
//										ps.println(Thread.currentThread().getName()+": step4 started");
										ll.set(4);
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										stepper2e.exchange(null);	// step 4 ended
									
										stepper2s.exchange(null);	// step 5 started
//										ps.println(Thread.currentThread().getName()+": step5 started");
										ll.set(3);
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										
										stepper2s.exchange(null);	// step 6 started
//										ps.println(Thread.currentThread().getName()+": step6 started");
										ll.await(3);
										stepper2e.exchange(null);	// step 6 ended
										latch.countDown();
//										ps.println(Thread.currentThread().getName()+": step6 ended");
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		
		t1.setDaemon(true);		
		t1.start();
		t2.setDaemon(true);		
		t2.start();
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 1
		stepper1e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 2 
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 3 
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 4
		
		try {stepper1e.exchange(null,100,TimeUnit.MILLISECONDS);	// Tests that thread 1 remains awaiting
			Assert.fail("Mandatory exception was not detected - thread 1 doesn't sleep!");
		} catch (TimeoutException exc) {
		}
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 5

		stepper1e.exchange(null,1,TimeUnit.SECONDS);		// test that thread 1 wakes up
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 6
		stepper2e.exchange(null,1,TimeUnit.SECONDS);

		latch.await(1,TimeUnit.SECONDS);	// Threads must be completed
	}

	@Test
	public void longExceptionsTest() throws InterruptedException, TimeoutException {
		final ListenableLong	ll = new ListenableLong(10);
		
		Assert.assertEquals(10,ll.get());
		
		try {ll.set(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try {ll.await(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try {ll.await(0,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {ll.await(0,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try {ll.await(null,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {ll.await(value->true,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {ll.await(value->true,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	/*
	 * Test scenario:
	 *   1. First thread sets current LL value to 1 and tests, that previous value was 0
	 *   2. Second thread sets current LL value to 2 and tests, that previous value was 1
	 *   3. First thread waits current LL value = 3
	 *   4. Second thread sets current LL value to 4. First thread remains awaiting
	 *   5. Second thread sets current LL value to 3. First thread wakes up
	 *   6. Second thread waits current LL value = 3 and returns immediately
	 */
	@Test
	public void doubleBasicTest() throws InterruptedException, TimeoutException {
		final ListenableDouble	ld = new ListenableDouble();
		final CountDownLatch	latch = new CountDownLatch(2);
		final Exchanger			stepper1s = new Exchanger(), stepper1e = new Exchanger();
		final Thread			t1 = new Thread(()->{
									try{
										stepper1s.exchange(null);	// step 1 started
//										ps.println(Thread.currentThread().getName()+": step1 started");
										Assert.assertEquals(0,ld.set(1),0.0001);										
//										ps.println(Thread.currentThread().getName()+": step1 ended");
										stepper1e.exchange(null);	// step 1 ended
										
										stepper1s.exchange(null);	// step 3 started
//										ps.println(Thread.currentThread().getName()+": step3 started");
										ld.await(3);										
//										ps.println(Thread.currentThread().getName()+": step3 ended");
										stepper1e.exchange(null);	// step 3 ended
										
										latch.countDown();
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		final Exchanger			stepper2s = new Exchanger(), stepper2e = new Exchanger();
		final Thread			t2 = new Thread(()->{
									try{
										stepper2s.exchange(null);	// step 2 started
//										ps.println(Thread.currentThread().getName()+": step2 started");
										Assert.assertEquals(1,ld.set(2),0.0001);
//										ps.println(Thread.currentThread().getName()+": step2 ended");
										stepper2e.exchange(null);	// step 2 ended
										
										stepper2s.exchange(null);	// step 4 started
//										ps.println(Thread.currentThread().getName()+": step4 started");
										ld.set(4);
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										stepper2e.exchange(null);	// step 4 ended
									
										stepper2s.exchange(null);	// step 5 started
//										ps.println(Thread.currentThread().getName()+": step5 started");
										ld.set(3);
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										
										stepper2s.exchange(null);	// step 6 started
//										ps.println(Thread.currentThread().getName()+": step6 started");
										ld.await(3);
										stepper2e.exchange(null);	// step 6 ended
										latch.countDown();
//										ps.println(Thread.currentThread().getName()+": step6 ended");
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		
		t1.setDaemon(true);		
		t1.start();
		t2.setDaemon(true);		
		t2.start();
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 1
		stepper1e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 2 
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 3 
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 4
		
		try {stepper1e.exchange(null,100,TimeUnit.MILLISECONDS);	// Tests that thread 1 remains awaiting
			Assert.fail("Mandatory exception was not detected - thread 1 doesn't sleep!");
		} catch (TimeoutException exc) {
		}
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 5

		stepper1e.exchange(null,1,TimeUnit.SECONDS);		// test that thread 1 wakes up
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 6
		stepper2e.exchange(null,1,TimeUnit.SECONDS);

		latch.await(1,TimeUnit.SECONDS);	// Threads must be completed
	}

	@Test
	public void doubleExceptionsTest() throws InterruptedException, TimeoutException {
		final ListenableDouble	ld = new ListenableDouble(10);
		
		Assert.assertEquals(10,ld.get(),0.0001);
		
		try {ld.set(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try {ld.await(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try {ld.await(0,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {ld.await(0,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try {ld.await(null,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {ld.await(value->true,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {ld.await(value->true,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	/*
	 * Test scenario:
	 *   1. First thread sets current LL value to "1" and tests, that previous value was 0
	 *   2. Second thread sets current LL value to "2" and tests, that previous value was 1
	 *   3. First thread waits current LL value = "3"
	 *   4. Second thread sets current LL value to "4". First thread remains awaiting
	 *   5. Second thread sets current LL value to "3". First thread wakes up
	 *   6. Second thread waits current LL value = "3" and returns immediately
	 */
	@Test
	public void refBasicTest() throws InterruptedException, TimeoutException {
		final ListenableRef<String>	ls = new ListenableRef<>("0");
		final CountDownLatch	latch = new CountDownLatch(2);
		final Exchanger			stepper1s = new Exchanger(), stepper1e = new Exchanger();
		final Thread			t1 = new Thread(()->{
									try{
										stepper1s.exchange(null);	// step 1 started
//										ps.println(Thread.currentThread().getName()+": step1 started");
										Assert.assertEquals("0",ls.set("1"));										
//										ps.println(Thread.currentThread().getName()+": step1 ended");
										stepper1e.exchange(null);	// step 1 ended
										
										stepper1s.exchange(null);	// step 3 started
//										ps.println(Thread.currentThread().getName()+": step3 started");
										ls.await("3");										
//										ps.println(Thread.currentThread().getName()+": step3 ended");
										stepper1e.exchange(null);	// step 3 ended
										
										latch.countDown();
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		final Exchanger			stepper2s = new Exchanger(), stepper2e = new Exchanger();
		final Thread			t2 = new Thread(()->{
									try{
										stepper2s.exchange(null);	// step 2 started
//										ps.println(Thread.currentThread().getName()+": step2 started");
										Assert.assertEquals("1",ls.set("2"));
//										ps.println(Thread.currentThread().getName()+": step2 ended");
										stepper2e.exchange(null);	// step 2 ended
										
										stepper2s.exchange(null);	// step 4 started
//										ps.println(Thread.currentThread().getName()+": step4 started");
										ls.set("4");
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										stepper2e.exchange(null);	// step 4 ended
									
										stepper2s.exchange(null);	// step 5 started
//										ps.println(Thread.currentThread().getName()+": step5 started");
										ls.set("3");
//										ps.println(Thread.currentThread().getName()+": step5 ended");
										
										stepper2s.exchange(null);	// step 6 started
//										ps.println(Thread.currentThread().getName()+": step6 started");
										ls.await("3");
										stepper2e.exchange(null);	// step 6 ended
										latch.countDown();
//										ps.println(Thread.currentThread().getName()+": step6 ended");
									} catch (Exception exc) {
										exc.printStackTrace();
									}
								});
		
		t1.setDaemon(true);		
		t1.start();
		t2.setDaemon(true);		
		t2.start();
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 1
		stepper1e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 2 
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper1s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 3 
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 4
		
		try {stepper1e.exchange(null,100,TimeUnit.MILLISECONDS);	// Tests that thread 1 remains awaiting
			Assert.fail("Mandatory exception was not detected - thread 1 doesn't sleep!");
		} catch (TimeoutException exc) {
		}
		stepper2e.exchange(null,1,TimeUnit.SECONDS);
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 5

		stepper1e.exchange(null,1,TimeUnit.SECONDS);		// test that thread 1 wakes up
		
		stepper2s.exchange(null,1,TimeUnit.SECONDS);		// initiate step 6
		stepper2e.exchange(null,1,TimeUnit.SECONDS);

		latch.await(1,TimeUnit.SECONDS);	// Threads must be completed
	}

	@Test
	public void refExceptionsTest() throws InterruptedException, TimeoutException {
		final ListenableRef		ls = new ListenableRef("10");
		
		Assert.assertEquals("10",ls.get());
		
		try {ls.set(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try {ls.await(null);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		try {ls.await(0,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {ls.await(0,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}

		try {ls.await(null,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {ls.await(value->true,-1,TimeUnit.SECONDS);
			Assert.fail("Manadtory exception was not detected (negative 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {ls.await(value->true,1,null);
			Assert.fail("Manadtory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}
}
