package chav1961.purelib.basic;

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
