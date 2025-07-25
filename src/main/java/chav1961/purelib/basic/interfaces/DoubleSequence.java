package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes sequence of unique doubles.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
@FunctionalInterface
public interface DoubleSequence {
	/**
	 * <p>Get next sequential value.</p>
	 * @return next sequential value
	 */
	double next();

	/**
	 * <p>Get zero-based sequence with step 1</p>
	 * @return zero-based sequence. Can't be null.
	 */
	public static DoubleSequence zero() {
		return of(0);
	}
	
	/**
	 * <p>Get sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static DoubleSequence of(final double from) {
		return of(from, 1);
	}
	
	/**
	 * <p>Get sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static DoubleSequence of(final double from, final double step) {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new DoubleSequence() {
				double	current = from;
				
				@Override
				public double next() {
					final double	result = current;
					
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
	public static DoubleSequence zeroSync() {
		return ofSync(0);
	}
	
	/**
	 * <p>Get thred-safe sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static DoubleSequence ofSync(final double from) {
		return ofSync(from, 1);
	}
	
	/**
	 * <p>Get thread-safe sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static DoubleSequence ofSync(final double from, final double step) {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new DoubleSequence() {
				double	current = from;
				
				@Override
				public synchronized double next() {
					final double	result = current;
					
					current += step;
					return result;
				}
			};
		}
	}
}
