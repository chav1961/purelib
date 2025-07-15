package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes sequence of unique integers.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
@FunctionalInterface
public interface IntSequence {
	/**
	 * <p>Get next sequential value.</p>
	 * @return next sequential value
	 */
	int next();

	/**
	 * <p>Get zero-based sequence with step 1</p>
	 * @return zero-based sequence. Can't be null.
	 */
	public static IntSequence zero() {
		return of(0);
	}
	
	/**
	 * <p>Get sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static IntSequence of(final int from) {
		return of(from, 1);
	}
	
	/**
	 * <p>Get sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static IntSequence of(final int from, final int step) throws IllegalArgumentException {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new IntSequence() {
				int	current = from;
				
				@Override
				public int next() {
					final int	result = current;
					
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
	public static IntSequence zeroSync() {
		return ofSync(0);
	}
	
	/**
	 * <p>Get thred-safe sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static IntSequence ofSync(final int from) {
		return of(from, 1);
	}
	
	/**
	 * <p>Get thread-safe sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static IntSequence ofSync(final int from, final int step) {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new IntSequence() {
				int	current = from;
				
				@Override
				public synchronized int next() {
					final int	result = current;
					
					current += step;
					return result;
				}
			};
		}
	}
}
