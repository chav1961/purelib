package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes sequence of unique longs.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
@FunctionalInterface
public interface LongSequence {
	/**
	 * <p>Get next sequential value.</p>
	 * @return next sequential value
	 */
	long next();

	/**
	 * <p>Get zero-based sequence with step 1</p>
	 * @return zero-based sequence. Can't be null.
	 */
	public static LongSequence zero() {
		return of(0);
	}
	
	/**
	 * <p>Get sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static LongSequence of(final long from) {
		return of(from, 1);
	}
	
	/**
	 * <p>Get sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static LongSequence of(final long from, final long step) {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new LongSequence() {
				long	current = from;
				
				@Override
				public long next() {
					final long	result = current;
					
					current += step;
					return result;
				}
			};
		}
	}

	/**
	 * <p>Get zero-based thred-safe sequence with step 1</p>
	 * @return zero-based sequence. Can't be null.
	 */
	public static LongSequence zeroSync() {
		return ofSync(0);
	}
	
	/**
	 * <p>Get thred-safe sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static LongSequence ofSync(final long from) {
		return ofSync(from, 1);
	}
	
	/**
	 * <p>Get thread-safe sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static LongSequence ofSync(final long from, final long step) {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new LongSequence() {
				long	current = from;
				
				@Override
				public synchronized long next() {
					final long	result = current;
					
					current += step;
					return result;
				}
			};
		}
	}
}
