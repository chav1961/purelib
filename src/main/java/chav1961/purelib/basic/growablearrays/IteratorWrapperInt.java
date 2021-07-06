package chav1961.purelib.basic.growablearrays;

import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.LongToIntFunction;

class IteratorWrapperInt implements OfInt {
	private final OfInt				nestedInt;
	private final IntPredicate		testInt;
	private final IntUnaryOperator	opInt;
	private final OfLong			nestedLong;
	private final LongToIntFunction	opLong;
	private int						count = 0;
	
	IteratorWrapperInt(final OfInt nested, final IntPredicate predicate, final  IntUnaryOperator op) {
		this.nestedInt = nested;
		this.testInt = predicate;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
	}
	
	public IteratorWrapperInt(final OfLong nested, final LongToIntFunction op) {
		this.nestedInt = null;
		this.testInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = op;
	}

	@Override
	public boolean hasNext() {
		return nestedInt.hasNext() && testInt.test(count);
	}

	@Override
	public int nextInt() {
		count++;
		return opInt.applyAsInt(nestedInt.nextInt());
	}

	@Override
	public String toString() {
		return "IteratorWrapperInt [nested=" + nestedInt + ", op=" + opInt + "]";
	}
}