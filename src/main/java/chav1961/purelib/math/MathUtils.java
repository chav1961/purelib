package chav1961.purelib.math;


/**
 * <p>This is an utility class for some useful mathematical utilities.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class MathUtils {
	/**
	 * <p>Convert integer to signum</p>
	 * @param signum integer to get signum for.
	 * @return signum calculated.
	 */
	public static int signum(final int signum) {
		if (signum < 0) {
			return -1;
		}
		else if (signum > 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
