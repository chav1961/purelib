package chav1961.purelib.testing;

import chav1961.purelib.basic.Utils;

/**
 * <p>This class can be used in debugging purposes to measure duration time of the code piece. Class instance 
 * measures duration time between creating it's instance and calling it's {@linkplain #close()} method, and prints
 * it to {@linkplain System#err} stream. It's strongly recommended to use this class with <b>try-with-resource</b> block:</p>
 * <code>
 * try(final Durator d = new Durator("test point")) {
 *    // code piece body 
 * }
 * </code>
 * <p>This class is thread-safe.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @thread.safe
 */
public class Durator implements AutoCloseable{
	private final String	caption;
	private final boolean	nanos;
	private final long		startTime;
	
	/**
	 * <p>Constructor of the class</p>
	 * @param caption text to print to System.err stream. Can't be null or empty
	 */
	public Durator(final String caption) {
		this(caption, false);
	}	
	
	/**
	 * <p>Constructor of the class</p>
	 * @param caption text to print to System.err stream. Can't be null or empty
	 * @param nanos calculate time in nanoseconds (true) or milliseconds (false)
	 */
	public Durator(final String caption, final boolean nanos) {
		if (Utils.checkEmptyOrNullString(caption)) {
			throw new IllegalArgumentException("Caption string can't be null or empty"); 
		}
		else {
			this.caption = caption;
			this.nanos = nanos;
			this.startTime = nanos ? System.nanoTime() : System.currentTimeMillis();
		}
	}

	@Override
	public void close() throws RuntimeException {
		final long	endTime = nanos ? System.nanoTime() : System.currentTimeMillis();
		
		if (nanos) {
			System.err.println(caption+": "+(endTime-startTime)+" nanosec");
		}
		else {
			System.err.println(caption+": "+(endTime-startTime)+" millisec");
		}
	}
}
