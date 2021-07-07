package chav1961.purelib.basic.growablearrays;

import java.util.PrimitiveIterator.OfInt;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.LongToIntFunction;

class IteratorWrapperInt implements OfInt {
	private enum WrapperType {
		Int, Long, Double
	}
	
	private final WrapperType			type;
	private final OfInt					nestedInt;
	private final IntPredicate			testInt;
	private final IntUnaryOperator		opInt;
	private final OfLong				nestedLong;
	private final LongToIntFunction		opLong;
	private final OfDouble				nestedDouble;
	private final DoubleToIntFunction	opDouble;
	private int							count = 0;
	
	IteratorWrapperInt(final OfInt nested, final IntPredicate predicate, final  IntUnaryOperator op) {
		this.type = WrapperType.Int;
		this.nestedInt = nested;
		this.testInt = predicate;
		this.opInt = op;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = null;
		this.opDouble = null;
	}
	
	public IteratorWrapperInt(final OfLong nested, final LongToIntFunction op) {
		this.type = WrapperType.Long;
		this.nestedInt = null;
		this.testInt = null;
		this.opInt = null;
		this.nestedLong = nested;
		this.opLong = op;
		this.nestedDouble = null;
		this.opDouble = null;
	}

	public IteratorWrapperInt(final OfDouble nested, final DoubleToIntFunction op) {
		this.type = WrapperType.Double;
		this.nestedInt = null;
		this.testInt = null;
		this.opInt = null;
		this.nestedLong = null;
		this.opLong = null;
		this.nestedDouble = nested;
		this.opDouble = op;
	}
	
	@Override
	public boolean hasNext() {
		switch (type) {
			case Double	: return nestedDouble.hasNext();
			case Int	: return nestedInt.hasNext() && testInt.test(count);
			case Long	: return nestedLong.hasNext();
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
	}

	@Override
	public int nextInt() {
		count++;
		switch (type) {
			case Double	: return opDouble.applyAsInt(nestedDouble.nextDouble());
			case Int	: return opInt.applyAsInt(nestedInt.nextInt());
			case Long	: return opLong.applyAsInt(nestedLong.nextLong());
			default		: throw new UnsupportedOperationException("Wrapper type ["+type+"] is not supported yet");
		}
	}

	@Override
	public String toString() {
		return "IteratorWrapperInt [nested=" + nestedInt + ", op=" + opInt + "]";
	}
}