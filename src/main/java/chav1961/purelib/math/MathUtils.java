package chav1961.purelib.math;

public class MathUtils {
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
