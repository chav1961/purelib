package chav1961.purelib.basic.growablearrays;

import java.util.concurrent.RecursiveTask;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class Reduces<T> extends RecursiveTask<T> {
	private static final long 			serialVersionUID = 1L;
	
	private enum ReducesType {
		Int, Long, Double;
	}
	
	private final ReducesType			type;
	private final SpliteratorOfInt		spliteratorInt;
	private final IntBinaryOperator		opInt;
	private final SpliteratorOfLong		spliteratorLong;
	private final LongBinaryOperator	opLong;
	private final SpliteratorOfDouble	spliteratorDouble;
	private final DoubleBinaryOperator	opDouble;

    public Reduces(final SpliteratorOfInt spliterator, final IntBinaryOperator op) {
    	this.type = ReducesType.Int;
        this.spliteratorInt = spliterator;
        this.opInt = op;
        this.spliteratorLong = null;
        this.opLong = null;
        this.spliteratorDouble = null;
        this.opDouble = null;
    }

    public Reduces(final SpliteratorOfLong spliterator, final LongBinaryOperator op) {
    	this.type = ReducesType.Long;
        this.spliteratorInt = null;
        this.opInt = null;
        this.spliteratorLong = spliterator;
        this.opLong = op;
        this.spliteratorDouble = null;
        this.opDouble = null;
    }

    public Reduces(final SpliteratorOfDouble spliterator, final DoubleBinaryOperator op) {
    	this.type = ReducesType.Double;
        this.spliteratorInt = null;
        this.opInt = null;
        this.spliteratorLong = null;
        this.opLong = null;
        this.spliteratorDouble = spliterator;
        this.opDouble = op;
    }
    
    @Override
    protected T compute() {
    	switch (type) {
			case Double	: return (T) computeDouble();
			case Int	: return (T) computeInt();
			case Long	: return (T) computeLong();
			default		: throw new UnsupportedOperationException("Reduce type ["+type+"] is not implemented yet");
    	}
    }

    protected int[] computeInt() {
    	final SpliteratorOfInt		leftSplit = (SpliteratorOfInt)spliteratorInt.trySplit();

       	if (leftSplit == null) {
       		final int[]	result = new int[1];
       		
       		while (spliteratorInt.tryAdvance((int value)->{
       				result[0] = opInt.applyAsInt(result[0],value);
       			})) {
       			// empty body...
       		}
       		return result;
       	}
       	else {
           	final Reduces<int[]>	leftReduce = new Reduces<>(leftSplit, opInt);
        	
           	leftReduce.fork();            	
        	final int[]				right = this.computeInt(), left = leftReduce.join();
        	
        	left[0] = opInt.applyAsInt(left[0], right[0]);
       		return left;
    	}
    }

    protected long[] computeLong() {
    	final SpliteratorOfLong		leftSplit = (SpliteratorOfLong)spliteratorLong.trySplit();

       	if (leftSplit == null) {
       		final long[]	result = new long[1];
       		
       		while (spliteratorLong.tryAdvance((long value)->{
       				result[0] = opLong.applyAsLong(result[0],value);
       			})) {
       			// empty body...
       		}
       		return result;
       	}
       	else {
           	final Reduces<long[]>	leftReduce = new Reduces<>(leftSplit, opLong);
        	
           	leftReduce.fork();            	
        	final long[]			right = this.computeLong(), left = leftReduce.join();
        	
        	left[0] = opLong.applyAsLong(left[0], right[0]);
       		return left;
    	}
    }

    protected double[] computeDouble() {
    	final SpliteratorOfDouble		leftSplit = (SpliteratorOfDouble)spliteratorDouble.trySplit();

       	if (leftSplit == null) {
       		final double[]	result = new double[1];
       		
       		while (spliteratorDouble.tryAdvance((double value)->{
       				result[0] = opDouble.applyAsDouble(result[0],value);
       			})) {
       			// empty body...
       		}
       		return result;
       	}
       	else {
           	final Reduces<double[]>	leftReduce = new Reduces<>(leftSplit, opDouble);
        	
           	leftReduce.fork();            	
        	final double[]			right = this.computeDouble(), left = leftReduce.join();
        	
        	left[0] = opDouble.applyAsDouble(left[0], right[0]);
       		return left;
    	}
    }
    
	@Override
	public String toString() {
		return "Reduces [spliterator=" + spliteratorInt + "]";
	}
}