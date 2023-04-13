package chav1961.purelib.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class LightWeightFutureTest {
	@Test
	public void lifeCycleTest() throws InterruptedException, ExecutionException {
		final LightWeightFuture<String, String>	f = new LightWeightFuture<>("test");
		
		Assert.assertFalse(f.isDone());
		Assert.assertFalse(f.isCancelled());
		Assert.assertEquals("test", f.take());
		
		f.complete("result");
		Assert.assertTrue(f.isDone());
		Assert.assertFalse(f.isCancelled());
		Assert.assertEquals("result", f.get());
	}

	@Test
	public void cancellationTest() throws InterruptedException, ExecutionException {
		final LightWeightFuture<String, String>	f = new LightWeightFuture<>("test");
		
		Assert.assertFalse(f.isDone());
		Assert.assertFalse(f.isCancelled());
		Assert.assertEquals("test", f.take());
		
		f.cancel(false);
		Assert.assertFalse(f.isDone());
		Assert.assertTrue(f.isCancelled());
		
		f.reject();
		Assert.assertTrue(f.isDone());
		Assert.assertTrue(f.isCancelled());
		Assert.assertNull(f.get());
	}
	
	@Test
	public void exceptionsTest() throws InterruptedException, ExecutionException, TimeoutException {
		try{new LightWeightFuture<String,String>(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		final LightWeightFuture<String, String>	f = new LightWeightFuture<>("test");

		try{f.complete(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{f.fail(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		final RuntimeException	e = new RuntimeException("test");

		f.fail(e);
		
		try{f.get();
			Assert.fail("Mandatory exception was not detected (execution exception waited)");
		} catch (ExecutionException exc) {
		}
		
		try{f.get(-1, TimeUnit.SECONDS);
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		try{f.get(1, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{f.get(1, TimeUnit.SECONDS);
			Assert.fail("Mandatory exception was not detected (execution exception waited)");
		} catch (ExecutionException exc) {
		}
	}
}
