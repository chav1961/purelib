package chav1961.purelib.basic.growablearrays;


import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class StreamsTest {
	@Test
	public void integerPrimaryTest() {
		final GrowableIntArray	gia = new GrowableIntArray(false);
		final AtomicInteger		sum = new AtomicInteger();
		final boolean[]			closed = {false};
		
		try (final IntStream	is = gia.toStream()) {
			
			is.onClose(()->closed[0] = true);
			
			Assert.assertTrue(is.isParallel());
			Assert.assertArrayEquals(new int[0], is.toArray());
			
			Assert.assertEquals(0, is.count());
			Assert.assertEquals(0, is.sum());
			Assert.assertEquals(OptionalInt.empty(), is.min());
			Assert.assertEquals(OptionalInt.empty(), is.max());
			Assert.assertEquals(OptionalDouble.empty(), is.average());
	
			Assert.assertEquals(OptionalInt.empty(), is.findFirst());
			Assert.assertEquals(OptionalInt.empty(), is.findAny());
	
			Assert.assertFalse(is.anyMatch((int value)->value == 0));
			Assert.assertFalse(is.allMatch((int value)->value == 0));
			Assert.assertFalse(is.noneMatch((int value)->value == 0));
			
			Assert.assertArrayEquals(new int[] {0}, is.collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
			Assert.assertEquals(5, is.reduce(5,(a,b)->a+b));
			Assert.assertEquals(OptionalInt.empty(), is.reduce((a,b)->a+b));
	
			is.forEach((value)->sum.addAndGet(value));
			Assert.assertEquals(0, sum.intValue());
			
			sum.set(0);
			is.forEachOrdered((value)->sum.addAndGet(value));
			Assert.assertEquals(0, sum.intValue());
			
			try {is.allMatch(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {is.anyMatch(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {is.noneMatch(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try {is.collect(null, (acc, val) -> {}, (left, right) -> {});
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {is.collect(()->new int[] {0}, null, (left, right) -> left[0] += right[0]);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			try {is.forEach(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}

			try {is.forEachOrdered(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
		Assert.assertTrue(closed[0]);
		
		for (int index = 0; index < GrowableIntArray.MINIMUM_SPLIT_SIZE; index++) {
			gia.append(2*index).append(2*index+1);
		}

		// Stream is immutable, and terminal functions exhaust it!!!
		Assert.assertTrue(gia.toStream().isParallel());
		Assert.assertArrayEquals(gia.extract(), gia.toStream().toArray());
		
		Assert.assertEquals(512, gia.toStream().count());
		Assert.assertEquals(130816, gia.toStream().sum());
		Assert.assertEquals(OptionalInt.of(0), gia.toStream().min());
		Assert.assertEquals(OptionalInt.of(511), gia.toStream().max());
		Assert.assertEquals(OptionalDouble.of(255.5), gia.toStream().average());

		Assert.assertTrue(gia.toStream().anyMatch((int value)->value == 511));
		Assert.assertTrue(gia.toStream().allMatch((int value)->value <= 511));
		Assert.assertTrue(gia.toStream().noneMatch((int value)->value > 511));

		Assert.assertArrayEquals(new int[] {130816}, gia.toStream().collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
		Assert.assertEquals(130821, gia.toStream().reduce(5,(a,b)->a+b));
		Assert.assertEquals(OptionalInt.of(130816), gia.toStream().reduce((a,b)->a+b));

		sum.set(0);
		gia.toStream().forEach((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
		
		sum.set(0);
		gia.toStream().forEachOrdered((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
	}
 
	@Test
	public void integerSecondaryTest() {
		final GrowableIntArray	giaEmpty = new GrowableIntArray(false);
		final GrowableIntArray	giaFull = new GrowableIntArray(false);
		final AtomicInteger		ai = new AtomicInteger();
		
		for (int index = 0; index < GrowableIntArray.MINIMUM_SPLIT_SIZE; index++) {
			giaFull.append(2*index).append(2*index+1);
		}
		
		// To int. Must have terminal function call!
		Assert.assertEquals(0, giaEmpty.toStream().map((int val)->2 * val).sum());
		Assert.assertEquals(261632, giaFull.toStream().map((int val)->2 * val).sum());
		
		ai.set(0);
		Assert.assertEquals(0, giaEmpty.toStream().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(0, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(130816, ai.intValue());

		// To long.
		Assert.assertEquals(0, giaEmpty.toStream().mapToLong((int val)->2 * val).sum());
		Assert.assertEquals(261632, giaFull.toStream().mapToLong((int val)->2 * val).sum());
		
		Assert.assertEquals(0, giaEmpty.toStream().asLongStream().sum());
		Assert.assertEquals(130816, giaFull.toStream().asLongStream().sum());

		ai.set(0);
		Assert.assertEquals(0, giaEmpty.toStream().asLongStream().peek((e)->ai.addAndGet((int)e)).count());
		Assert.assertEquals(0, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().asLongStream().peek((e)->ai.addAndGet((int)e)).count());
		Assert.assertEquals(130816, ai.intValue());

		Assert.assertEquals(0, giaEmpty.toStream().asLongStream().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
		Assert.assertEquals(130816, giaFull.toStream().asLongStream().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
		
		// To double.
		Assert.assertEquals(0, giaEmpty.toStream().mapToDouble((int val)->2 * val).sum(), 0.001);
		Assert.assertEquals(261632, giaFull.toStream().mapToDouble((int val)->2 * val).sum(), 0.001);
		
		Assert.assertEquals(0, giaEmpty.toStream().asDoubleStream().sum(), 0.001);
		Assert.assertEquals(130816, giaFull.toStream().asDoubleStream().sum(), 0.001);

		ai.set(0);
		Assert.assertEquals(0, giaEmpty.toStream().asDoubleStream().peek((e)->ai.addAndGet((int)e)).count());
		Assert.assertEquals(0, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().asDoubleStream().peek((e)->ai.addAndGet((int)e)).count());
		Assert.assertEquals(130816, ai.intValue());
		
		Assert.assertEquals(0, giaEmpty.toStream().asDoubleStream().collect(()->new double[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0], 0.001);
		Assert.assertEquals(130816, giaFull.toStream().asDoubleStream().collect(()->new double[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0], 0.001);
		
		// To object
		Assert.assertEquals(0, giaEmpty.toStream().boxed().count());
		Assert.assertEquals(512, giaFull.toStream().boxed().count());

		ai.set(0);
		giaFull.toStream().mapToObj((int val)->Integer.valueOf(2 * val)).forEach((e)->ai.addAndGet(e.intValue()));
		Assert.assertEquals(261632, ai.intValue());

		Assert.assertEquals(0, giaEmpty.toStream().boxed().collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
		Assert.assertEquals(130816, giaFull.toStream().boxed().collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
	} 

	@Test
	public void integerResizableTest() {
		final GrowableIntArray	giaEmpty = new GrowableIntArray(false);
		final GrowableIntArray	giaFull = new GrowableIntArray(false);
		final AtomicInteger		ai = new AtomicInteger();
		
		for (int index = 0; index < GrowableIntArray.MINIMUM_SPLIT_SIZE; index++) {
			giaFull.append(2*index).append(2*index+1);
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().limit(10).count());
		Assert.assertEquals(10, giaFull.toStream().limit(10).count());

		Assert.assertEquals(0, giaEmpty.toStream().skip(10).count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE - 10, giaFull.toStream().skip(10).count());

		Assert.assertEquals(0, giaEmpty.toStream().filter((v)->v % 2 == 0).count());
		Assert.assertEquals(GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().filter((v)->v % 2 == 0).count());

		Assert.assertEquals(0, giaEmpty.toStream().distinct().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().distinct().count());

		Assert.assertEquals(0, giaEmpty.toStream().sorted().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().sorted().count());
	}
}
