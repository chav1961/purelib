package chav1961.purelib.basic.growablearrays;

import java.util.concurrent.RecursiveTask;

import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfDouble;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfInt;
import chav1961.purelib.basic.growablearrays.ArrayUtils.SpliteratorOfLong;

class Matches extends RecursiveTask<Boolean> {
	private static final long 		serialVersionUID = 1L;

	@FunctionalInterface
	interface MatchesTestInt {
		boolean test(final int value);
	}

	@FunctionalInterface
	interface MatchesTestLong {
		boolean test(final long value);
	}
	
	@FunctionalInterface
	interface MatchesTestDouble {
		boolean test(final double value);
	}

	private enum MatchType {
		Int, Long, Double;
	}
	
	private final MatchType					type;
	private final SpliteratorOfInt			spliteratorInt;
	private final Matches.MatchesTestInt	testInt;
	private final SpliteratorOfLong			spliteratorLong;
	private final Matches.MatchesTestLong	testLong;
	private final SpliteratorOfDouble		spliteratorDouble;
	private final Matches.MatchesTestDouble	testDouble;
	
    public Matches(final SpliteratorOfInt spliterator, final Matches.MatchesTestInt test) {
    	this.type = MatchType.Int;
        this.spliteratorInt = spliterator;
        this.testInt = test;
        this.spliteratorLong = null;
        this.testLong = null;
        this.spliteratorDouble = null;
        this.testDouble = null;
    }

    public Matches(final SpliteratorOfLong spliterator, final Matches.MatchesTestLong test) {
    	this.type = MatchType.Long;
        this.spliteratorInt = null;
        this.testInt = null;
        this.spliteratorLong = spliterator;
        this.testLong = test;
        this.spliteratorDouble = null;
        this.testDouble = null;
    }
    
    public Matches(final SpliteratorOfDouble spliterator, final Matches.MatchesTestDouble test) {
    	this.type = MatchType.Double;
        this.spliteratorInt = null;
        this.testInt = null;
        this.spliteratorLong = null;
        this.testLong = null;
        this.spliteratorDouble = spliterator;
        this.testDouble = test;
    }
    
    @Override
    protected Boolean compute() {
    	switch (type) {
			case Double	: return computeDouble();
			case Int	: return computeInt();
			case Long	: return computeLong();
			default 	: throw new UnsupportedOperationException("Match type ["+type+"] is not supported yet");
    	}
    }

    private boolean computeInt() {
    	final SpliteratorOfInt		leftSplit = (SpliteratorOfInt)spliteratorInt.trySplit();

       	if (leftSplit == null) {
       		final boolean[]	result = new boolean[] {false};
       		
       		while (!result[0] && spliteratorInt.tryAdvance((int value)->result[0] = testInt.test(value))) {
       			// empty body...
       		}
       		return result[0];
       	}
       	else {
           	final Matches		leftMatches = new Matches(leftSplit, testInt);
        	
           	leftMatches.fork();            	
        	final Boolean		right = this.compute();
        	
       		return right || leftMatches.join();
    	}
    }
    
    private boolean computeLong() {
    	final SpliteratorOfLong		leftSplit = (SpliteratorOfLong)spliteratorLong.trySplit();

       	if (leftSplit == null) {
       		final boolean[]	result = new boolean[] {false};
       		
       		while (!result[0] && spliteratorLong.tryAdvance((long value)->result[0] = testLong.test(value))) {
       			// empty body...
       		}
       		return result[0];
       	}
       	else {
           	final Matches		leftMatches = new Matches(leftSplit, testLong);
        	
           	leftMatches.fork();            	
        	final Boolean		right = this.compute();
        	
       		return right || leftMatches.join();
    	}
    }

    private boolean computeDouble() {
    	final SpliteratorOfDouble	leftSplit = (SpliteratorOfDouble)spliteratorDouble.trySplit();

       	if (leftSplit == null) {
       		final boolean[]	result = new boolean[] {false};
       		
       		while (!result[0] && spliteratorDouble.tryAdvance((double value)->result[0] = testDouble.test(value))) {
       			// empty body...
       		}
       		return result[0];
       	}
       	else {
           	final Matches		leftMatches = new Matches(leftSplit, testDouble);
        	
           	leftMatches.fork();            	
        	final Boolean		right = this.compute();
        	
       		return right || leftMatches.join();
    	}
    }
    
	@Override
	public String toString() {
		return "Matches [spliterator=" + spliteratorInt + "]";
	}
}