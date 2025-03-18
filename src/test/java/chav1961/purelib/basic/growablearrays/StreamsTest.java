package chav1961.purelib.basic.growablearrays;


import java.util.OptionalDouble;

import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("OrdinalTestCategory")
public class StreamsTest {
	@Test
	public void integerPrimaryTest() {
		final GrowableIntArray	gia = new GrowableIntArray(false);
		final AtomicInteger		sum = new AtomicInteger();
		final boolean[]			closed = {false};
		
		try (final IntStream	is = gia.toStream()) {
			
			is.onClose(()->closed[0] = true);

			try {is.onClose(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
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
		Assert.assertEquals(512, gia.toStream().sequential().count());
		Assert.assertEquals(130816, gia.toStream().sum());
		Assert.assertEquals(130816, gia.toStream().sequential().sum());
		Assert.assertEquals(OptionalInt.of(0), gia.toStream().min());
		Assert.assertEquals(OptionalInt.of(0), gia.toStream().sequential().min());
		Assert.assertEquals(OptionalInt.of(511), gia.toStream().max());
		Assert.assertEquals(OptionalInt.of(511), gia.toStream().sequential().max());
		Assert.assertEquals(OptionalDouble.of(255.5), gia.toStream().average());
		Assert.assertEquals(OptionalDouble.of(255.5), gia.toStream().sequential().average());

		Assert.assertTrue(gia.toStream().anyMatch((int value)->value == 511));
		Assert.assertTrue(gia.toStream().sequential().anyMatch((int value)->value == 511));
		Assert.assertTrue(gia.toStream().allMatch((int value)->value <= 511));
		Assert.assertTrue(gia.toStream().sequential().allMatch((int value)->value <= 511));
		Assert.assertTrue(gia.toStream().noneMatch((int value)->value > 511));
		Assert.assertTrue(gia.toStream().sequential().noneMatch((int value)->value > 511));

		Assert.assertArrayEquals(new int[] {130816}, gia.toStream().collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
		Assert.assertArrayEquals(new int[] {130816}, gia.toStream().sequential().collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
		Assert.assertEquals(130821, gia.toStream().reduce(5,(a,b)->a+b));
		Assert.assertEquals(130821, gia.toStream().sequential().reduce(5,(a,b)->a+b));
		Assert.assertEquals(OptionalInt.of(130816), gia.toStream().reduce((a,b)->a+b));
		Assert.assertEquals(OptionalInt.of(130816), gia.toStream().sequential().reduce((a,b)->a+b));

		sum.set(0);
		gia.toStream().forEach((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
		
		sum.set(0);
		gia.toStream().sequential().forEach((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
		
		sum.set(0);
		gia.toStream().forEachOrdered((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
		
		sum.set(0);
		gia.toStream().sequential().forEachOrdered((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
	}

	@Test
	public void longPrimaryTest() {
		final GrowableLongArray	gia = new GrowableLongArray(false);
		final AtomicLong		sum = new AtomicLong();
		final boolean[]			closed = {false};
		
		try (final LongStream	is = gia.toStream()) {
			
			is.onClose(()->closed[0] = true);

			try {is.onClose(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			Assert.assertTrue(is.isParallel());
			Assert.assertArrayEquals(new long[0], is.toArray());
			
			Assert.assertEquals(0, is.count());
			Assert.assertEquals(0, is.sum());
			Assert.assertEquals(OptionalLong.empty(), is.min());
			Assert.assertEquals(OptionalLong.empty(), is.max());
			Assert.assertEquals(OptionalDouble.empty(), is.average());
	
			Assert.assertEquals(OptionalLong.empty(), is.findFirst());
			Assert.assertEquals(OptionalLong.empty(), is.findAny());
	
			Assert.assertFalse(is.anyMatch((long value)->value == 0));
			Assert.assertFalse(is.allMatch((long value)->value == 0));
			Assert.assertFalse(is.noneMatch((long value)->value == 0));
			
			Assert.assertArrayEquals(new int[] {0}, is.collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
			Assert.assertEquals(5, is.reduce(5,(a,b)->a+b));
			Assert.assertEquals(OptionalLong.empty(), is.reduce((a,b)->a+b));
	
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
		Assert.assertEquals(512, gia.toStream().sequential().count());
		Assert.assertEquals(130816, gia.toStream().sum());
		Assert.assertEquals(130816, gia.toStream().sequential().sum());
		Assert.assertEquals(OptionalLong.of(0), gia.toStream().min());
		Assert.assertEquals(OptionalLong.of(0), gia.toStream().sequential().min());
		Assert.assertEquals(OptionalLong.of(511), gia.toStream().max());
		Assert.assertEquals(OptionalLong.of(511), gia.toStream().sequential().max());
		Assert.assertEquals(OptionalDouble.of(255.5), gia.toStream().average());
		Assert.assertEquals(OptionalDouble.of(255.5), gia.toStream().sequential().average());

		Assert.assertTrue(gia.toStream().anyMatch((long value)->value == 511));
		Assert.assertTrue(gia.toStream().sequential().anyMatch((long value)->value == 511));
		Assert.assertTrue(gia.toStream().allMatch((long value)->value <= 511));
		Assert.assertTrue(gia.toStream().sequential().allMatch((long value)->value <= 511));
		Assert.assertTrue(gia.toStream().noneMatch((long value)->value > 511));
		Assert.assertTrue(gia.toStream().sequential().noneMatch((long value)->value > 511));

		Assert.assertArrayEquals(new long[] {130816}, gia.toStream().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
		Assert.assertArrayEquals(new long[] {130816}, gia.toStream().sequential().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
		Assert.assertEquals(130821, gia.toStream().reduce(5,(a,b)->a+b));
		Assert.assertEquals(130821, gia.toStream().sequential().reduce(5,(a,b)->a+b));
		Assert.assertEquals(OptionalLong.of(130816), gia.toStream().reduce((a,b)->a+b));
		Assert.assertEquals(OptionalLong.of(130816), gia.toStream().sequential().reduce((a,b)->a+b));

		sum.set(0);
		gia.toStream().forEach((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
		
		sum.set(0);
		gia.toStream().sequential().forEach((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
		
		sum.set(0);
		gia.toStream().forEachOrdered((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
		
		sum.set(0);
		gia.toStream().sequential().forEachOrdered((value)->sum.addAndGet(value));
		Assert.assertEquals(130816, sum.intValue());
	}

	@Test
	public void doublePrimaryTest() {
		final GrowableDoubleArray	gia = new GrowableDoubleArray(false);
		final double[]				sum = new double[] {0};
		final boolean[]				closed = {false};
		
		try (final DoubleStream	is = gia.toStream()) {
			
			is.onClose(()->closed[0] = true);

			try {is.onClose(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			Assert.assertTrue(is.isParallel());
			Assert.assertArrayEquals(new double[0], is.toArray(), 0.0001);
			
			Assert.assertEquals(0, is.count());
			Assert.assertEquals(0, is.sum(), 0.0001);
			Assert.assertEquals(OptionalDouble.empty(), is.min());
			Assert.assertEquals(OptionalDouble.empty(), is.max());
			Assert.assertEquals(OptionalDouble.empty(), is.average());
	
			Assert.assertEquals(OptionalDouble.empty(), is.findFirst());
			Assert.assertEquals(OptionalDouble.empty(), is.findAny());
	
			Assert.assertFalse(is.anyMatch((double value)->value == 0));
			Assert.assertFalse(is.allMatch((double value)->value == 0));
			Assert.assertFalse(is.noneMatch((double value)->value == 0));
			
			Assert.assertArrayEquals(new double[] {0}, is.collect(()->new double[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]), 0.0001);
			Assert.assertEquals(5, is.reduce(5,(a,b)->a+b), 0.0001);
			Assert.assertEquals(OptionalDouble.empty(), is.reduce((a,b)->a+b));
	
			is.forEach((value)->sum[0] += value);
			Assert.assertEquals(0, sum[0], 0.0001);
			
			sum[0] = 0;
			is.forEachOrdered((value)->sum[0] += value);
			Assert.assertEquals(0, sum[0], 0.0001);
			
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
		Assert.assertArrayEquals(gia.extract(), gia.toStream().toArray(), 0.0001);
		
		Assert.assertEquals(512, gia.toStream().count());
		Assert.assertEquals(512, gia.toStream().sequential().count());
		Assert.assertEquals(130816, gia.toStream().sum(), 0.0001);
		Assert.assertEquals(130816, gia.toStream().sequential().sum(), 0.0001);
		Assert.assertEquals(OptionalDouble.of(0), gia.toStream().min());
		Assert.assertEquals(OptionalDouble.of(0), gia.toStream().sequential().min());
		Assert.assertEquals(OptionalDouble.of(511), gia.toStream().max());
		Assert.assertEquals(OptionalDouble.of(511), gia.toStream().sequential().max());
		Assert.assertEquals(OptionalDouble.of(255.5), gia.toStream().average());
		Assert.assertEquals(OptionalDouble.of(255.5), gia.toStream().sequential().average());

		Assert.assertTrue(gia.toStream().anyMatch((double value)->value == 511));
		Assert.assertTrue(gia.toStream().sequential().anyMatch((double value)->value == 511));
		Assert.assertTrue(gia.toStream().allMatch((double value)->value <= 511));
		Assert.assertTrue(gia.toStream().sequential().allMatch((double value)->value <= 511));
		Assert.assertTrue(gia.toStream().noneMatch((double value)->value > 511));
		Assert.assertTrue(gia.toStream().sequential().noneMatch((double value)->value > 511));

		Assert.assertArrayEquals(new long[] {130816}, gia.toStream().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
		Assert.assertArrayEquals(new long[] {130816}, gia.toStream().sequential().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0]));
		Assert.assertEquals(130821, gia.toStream().reduce(5,(a,b)->a+b), 0.0001);
		Assert.assertEquals(130821, gia.toStream().sequential().reduce(5,(a,b)->a+b), 0.0001);
		Assert.assertEquals(OptionalDouble.of(130816), gia.toStream().reduce((a,b)->a+b));
		Assert.assertEquals(OptionalDouble.of(130816), gia.toStream().sequential().reduce((a,b)->a+b));

		sum[0] = 0;
		gia.toStream().forEach((value)->{synchronized(sum){sum[0] += value;}});
		Assert.assertEquals(130816, sum[0], 0.0001);
		
		sum[0] = 0;
		gia.toStream().sequential().forEach((value)->sum[0] += value);
		Assert.assertEquals(130816, sum[0], 0.0001);
		
		sum[0] = 0;
		gia.toStream().forEachOrdered((value)->sum[0] += value);
		Assert.assertEquals(130816, sum[0], 0.0001);
		
		sum[0] = 0;
		gia.toStream().sequential().forEachOrdered((value)->sum[0] += value);
		Assert.assertEquals(130816, sum[0], 0.0001);
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
		Assert.assertEquals(261632, giaFull.toStream().sequential().map((int val)->2 * val).sum());
		
		try {giaEmpty.toStream().map(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		ai.set(0);
		Assert.assertEquals(0, giaEmpty.toStream().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(0, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(130816, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().sequential().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(130816, ai.intValue());
		
		try {giaEmpty.toStream().peek(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// To long.
		Assert.assertEquals(0, giaEmpty.toStream().mapToLong((int val)->2 * val).sum());
		Assert.assertEquals(261632, giaFull.toStream().mapToLong((int val)->2 * val).sum());
		Assert.assertEquals(261632, giaFull.toStream().sequential().mapToLong((int val)->2 * val).sum());

		try {giaEmpty.toStream().mapToLong(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
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
		Assert.assertEquals(261632, giaFull.toStream().sequential().mapToDouble((int val)->2 * val).sum(), 0.001);

		try {giaEmpty.toStream().mapToDouble(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
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

		ai.set(0);
		giaFull.toStream().sequential().mapToObj((int val)->Integer.valueOf(2 * val)).forEach((e)->ai.addAndGet(e.intValue()));
		Assert.assertEquals(261632, ai.intValue());
		
		try {giaEmpty.toStream().mapToObj(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().boxed().collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
		Assert.assertEquals(130816, giaFull.toStream().boxed().collect(()->new int[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
	} 

	@Test
	public void longSecondaryTest() {
		final GrowableLongArray	giaEmpty = new GrowableLongArray(false);
		final GrowableLongArray	giaFull = new GrowableLongArray(false);
		final AtomicLong		ai = new AtomicLong();
		
		for (int index = 0; index < GrowableIntArray.MINIMUM_SPLIT_SIZE; index++) {
			giaFull.append(2*index).append(2*index+1);
		}
		
		// To int. Must have terminal function call!
		Assert.assertEquals(0, giaEmpty.toStream().map((long val)->2 * val).sum());
		Assert.assertEquals(261632, giaFull.toStream().map((long val)->2 * val).sum());
		Assert.assertEquals(261632, giaFull.toStream().sequential().map((long val)->2 * val).sum());
		
		try {giaEmpty.toStream().map(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		ai.set(0);
		Assert.assertEquals(0, giaEmpty.toStream().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(0, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(130816, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().sequential().peek((e)->ai.addAndGet(e)).count());
		Assert.assertEquals(130816, ai.intValue());
		
		try {giaEmpty.toStream().peek(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// To int.
		Assert.assertEquals(0, giaEmpty.toStream().mapToInt((long val)->(int)(2 * val)).sum());
		Assert.assertEquals(261632, giaFull.toStream().mapToInt((long val)->(int)(2 * val)).sum());
		Assert.assertEquals(261632, giaFull.toStream().sequential().mapToInt((long val)->(int)(2 * val)).sum());

		try {giaEmpty.toStream().mapToInt(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// To double.
		Assert.assertEquals(0, giaEmpty.toStream().mapToDouble((long val)->2 * val).sum(), 0.001);
		Assert.assertEquals(261632, giaFull.toStream().mapToDouble((long val)->2 * val).sum(), 0.001);
		Assert.assertEquals(261632, giaFull.toStream().sequential().mapToDouble((long val)->2 * val).sum(), 0.001);

		try {giaEmpty.toStream().mapToDouble(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
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
		giaFull.toStream().mapToObj((long val)->Long.valueOf(2 * val)).forEach((e)->ai.addAndGet(e.intValue()));
		Assert.assertEquals(261632, ai.intValue());

		ai.set(0);
		giaFull.toStream().sequential().mapToObj((long val)->Long.valueOf(2 * val)).forEach((e)->ai.addAndGet(e.intValue()));
		Assert.assertEquals(261632, ai.intValue());
		
		try {giaEmpty.toStream().mapToObj(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().boxed().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
		Assert.assertEquals(130816, giaFull.toStream().boxed().collect(()->new long[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0]);
	} 

	@Test
	public void doubleSecondaryTest() {
		final GrowableDoubleArray	giaEmpty = new GrowableDoubleArray(false);
		final GrowableDoubleArray	giaFull = new GrowableDoubleArray(false);
		final AtomicInteger			ai = new AtomicInteger();
		
		for (int index = 0; index < GrowableIntArray.MINIMUM_SPLIT_SIZE; index++) {
			giaFull.append(2*index).append(2*index+1);
		}
		
		// To int. Must have terminal function call!
		Assert.assertEquals(0, giaEmpty.toStream().map((double val)->(int)(2 * val)).sum(), 0.0001);
		Assert.assertEquals(261632, giaFull.toStream().map((double val)->(int)(2 * val)).sum(), 0.0001);
		Assert.assertEquals(261632, giaFull.toStream().sequential().map((double val)->(int)(2 * val)).sum(), 0.0001);
		
		try {giaEmpty.toStream().map(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		ai.set(0);
		Assert.assertEquals(0, giaEmpty.toStream().peek((e)->ai.addAndGet((int)e)).count());
		Assert.assertEquals(0, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().peek((e)->ai.addAndGet((int)e)).count());
		Assert.assertEquals(130816, ai.intValue());

		ai.set(0);
		Assert.assertEquals(512, giaFull.toStream().sequential().peek((e)->ai.addAndGet((int)e)).count());
		Assert.assertEquals(130816, ai.intValue());
		
		try {giaEmpty.toStream().peek(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// To int.
		Assert.assertEquals(0, giaEmpty.toStream().mapToInt((double val)->(int)(2 * val)).sum());
		Assert.assertEquals(261632, giaFull.toStream().mapToInt((double val)->(int)(2 * val)).sum());
		Assert.assertEquals(261632, giaFull.toStream().sequential().mapToInt((double val)->(int)(2 * val)).sum());

		try {giaEmpty.toStream().mapToInt(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// To long.
		Assert.assertEquals(0, giaEmpty.toStream().mapToLong((double val)->(long)(2 * val)).sum(), 0.001);
		Assert.assertEquals(261632, giaFull.toStream().mapToLong((double val)->(long)(2 * val)).sum(), 0.001);
		Assert.assertEquals(261632, giaFull.toStream().sequential().mapToLong((double val)->(long)(2 * val)).sum(), 0.001);

		try {giaEmpty.toStream().mapToLong(null).sum();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		// To object
		Assert.assertEquals(0, giaEmpty.toStream().boxed().count());
		Assert.assertEquals(512, giaFull.toStream().boxed().count());

		ai.set(0);
		giaFull.toStream().mapToObj((double val)->Double.valueOf(2 * val)).forEach((e)->ai.addAndGet(e.intValue()));
		Assert.assertEquals(261632, ai.intValue());

		ai.set(0);
		giaFull.toStream().sequential().mapToObj((double val)->Double.valueOf(2 * val)).forEach((e)->ai.addAndGet(e.intValue()));
		Assert.assertEquals(261632, ai.intValue());
		
		try {giaEmpty.toStream().mapToObj(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().boxed().collect(()->new double[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0], 0.0001);
		Assert.assertEquals(130816, giaFull.toStream().boxed().collect(()->new double[] {0}, (acc, val) -> acc[0] += val, (left, right) -> left[0] += right[0])[0], 0.0001);
	} 
	
	@Test
	public void integerResizableTest() {
		final GrowableIntArray	giaEmpty = new GrowableIntArray(false);
		final GrowableIntArray	giaFull = new GrowableIntArray(false);
		
		for (int index = 0; index < GrowableIntArray.MINIMUM_SPLIT_SIZE; index++) {
			giaFull.append(2*index).append(2*index+1);
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().limit(10).count());
		Assert.assertEquals(10, giaFull.toStream().limit(10).count());

		try {giaEmpty.toStream().limit(-1).count();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().skip(10).count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE - 10, giaFull.toStream().skip(10).count());

		try {giaEmpty.toStream().skip(-1).count();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().filter((v)->v % 2 == 0).count());
		Assert.assertEquals(GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().filter((v)->v % 2 == 0).count());

		try {giaEmpty.toStream().filter(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().distinct().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().distinct().count());

		Assert.assertEquals(0, giaEmpty.toStream().sorted().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().sorted().count());

		Assert.assertEquals(0, giaEmpty.toStream().flatMap((e)->e % 2 == 0 ? null : IntStream.of(e,2*e,3*e)).count());
		Assert.assertEquals(3 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().flatMap((e)->e % 2 == 0 ? null : IntStream.of(e,2*e,3*e)).count());

		try {giaEmpty.toStream().flatMap(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void longResizableTest() {
		final GrowableLongArray	giaEmpty = new GrowableLongArray(false);
		final GrowableLongArray	giaFull = new GrowableLongArray(false);
		
		for (int index = 0; index < GrowableLongArray.MINIMUM_SPLIT_SIZE; index++) {
			giaFull.append(2*index).append(2*index+1);
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().limit(10).count());
		Assert.assertEquals(10, giaFull.toStream().limit(10).count());

		try {giaEmpty.toStream().limit(-1).count();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().skip(10).count());
		Assert.assertEquals(2 * GrowableLongArray.MINIMUM_SPLIT_SIZE - 10, giaFull.toStream().skip(10).count());

		try {giaEmpty.toStream().skip(-1).count();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().filter((v)->v % 2 == 0).count());
		Assert.assertEquals(GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().filter((v)->v % 2 == 0).count());

		try {giaEmpty.toStream().filter(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().distinct().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().distinct().count());

		Assert.assertEquals(0, giaEmpty.toStream().sorted().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().sorted().count());

		Assert.assertEquals(0, giaEmpty.toStream().flatMap((e)->e % 2 == 0 ? null : LongStream.of(e,2*e,3*e)).count());
		Assert.assertEquals(3 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().flatMap((e)->e % 2 == 0 ? null : LongStream.of(e,2*e,3*e)).count());

		try {giaEmpty.toStream().flatMap(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void doubleResizableTest() {
		final GrowableDoubleArray	giaEmpty = new GrowableDoubleArray(false);
		final GrowableDoubleArray	giaFull = new GrowableDoubleArray(false);
		
		for (int index = 0; index < GrowableDoubleArray.MINIMUM_SPLIT_SIZE; index++) {
			giaFull.append(2*index).append(2*index+1);
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().limit(10).count());
		Assert.assertEquals(10, giaFull.toStream().limit(10).count());

		try {giaEmpty.toStream().limit(-1).count();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().skip(10).count());
		Assert.assertEquals(2 * GrowableLongArray.MINIMUM_SPLIT_SIZE - 10, giaFull.toStream().skip(10).count());

		try {giaEmpty.toStream().skip(-1).count();
			Assert.fail("Mandatory exception was not detected (1-st argument out of range)");
		} catch (IllegalArgumentException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().filter((v)->v % 2 == 0).count());
		Assert.assertEquals(GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().filter((v)->v % 2 == 0).count());

		try {giaEmpty.toStream().filter(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(0, giaEmpty.toStream().distinct().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().distinct().count());

		Assert.assertEquals(0, giaEmpty.toStream().sorted().count());
		Assert.assertEquals(2 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().sorted().count());

		Assert.assertEquals(0, giaEmpty.toStream().flatMap((e)->e % 2 == 0 ? null : DoubleStream.of(e,2*e,3*e)).count());
		Assert.assertEquals(3 * GrowableIntArray.MINIMUM_SPLIT_SIZE, giaFull.toStream().flatMap((e)->e % 2 == 0 ? null : DoubleStream.of(e,2*e,3*e)).count());

		try {giaEmpty.toStream().flatMap(null).count();
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
}
