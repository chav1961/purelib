package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes sequence of unique floats.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
@FunctionalInterface
public interface FloatSequence {
	/**
	 * <p>Get next sequential value.</p>
	 * @return next sequential value
	 */
	float next();

	/**
	 * <p>Get zero-based sequence with step 1</p>
	 * @return zero-based sequence. Can't be null.
	 */
	public static FloatSequence zero() {
		return of(0);
	}
	
	/**
	 * <p>Get sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static FloatSequence of(final float from) {
		return of(from, 1);
	}
	
	/**
	 * <p>Get sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static FloatSequence of(final float from, final float step) throws IllegalArgumentException {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new FloatSequence() {
				float	current = from;
				
				@Override
				public float next() {
					final float	result = current;
					
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
	public static FloatSequence zeroSync() {
		return ofSync(0);
	}
	
	/**
	 * <p>Get thred-safe sequence with initial value and step 1</p>
	 * @param from initial value
	 * @return sequence. Can't be null
	 */
	public static FloatSequence ofSync(final float from) {
		return of(from, 1);
	}
	
	/**
	 * <p>Get thread-safe sequence.</p>
	 * @param from sequence initial value
	 * @param step sequence step. Can't be 0
	 * @return sequence. Can't be null
	 * @throws IllegalArgumentException sequence step equals 0
	 */
	public static FloatSequence ofSync(final float from, final float step) {
		if (step == 0) {
			throw new IllegalArgumentException("Sequence step can't be 0"); 
		}
		else {
			return new FloatSequence() {
				float	current = from;
				
				@Override
				public synchronized float next() {
					final float	result = current;
					
					current += step;
					return result;
				}
			};
		}
	}
}
